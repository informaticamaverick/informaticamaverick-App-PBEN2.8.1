package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.BudgetDao
import com.example.myapplication.data.local.ChatDao
import com.example.myapplication.data.local.ChatUnreadCount
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.utils.ChatIdHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val budgetDao: BudgetDao,
    private val firestore: FirebaseFirestore,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) {

    private var messageListener: ListenerRegistration? = null

    fun getMessages(chatId: String): Flow<List<MessageEntity>> {
        return chatDao.getMessagesForChat(chatId)
    }

    suspend fun sendMessage(message: MessageEntity) {
        chatDao.insertMessage(message)
        try {
            coroutineScope {
                val msgWrite = async {
                    val audioBase64 = if (message.type == MessageType.AUDIO) try {
                        val f = java.io.File(message.content)
                        if (f.exists()) android.util.Base64.encodeToString(f.readBytes(), android.util.Base64.NO_WRAP)
                        else message.content
                    } catch (e: Exception) { message.content } else null

                    val firestoreData = hashMapOf(
                        "messageId" to message.id,
                        "chatId" to message.chatId,
                        "senderId" to message.senderId,
                        "text" to if (message.type == MessageType.AUDIO) "[Audio]" else message.content,
                        "type" to message.type.name,
                        "timestamp" to message.timestamp,
                        "isRead" to false,
                        "audioUrl" to audioBase64,
                        "audioDuration" to if (message.type == MessageType.AUDIO) message.durationSeconds else null,
                        "appointmentDate" to message.appointmentDate,
                        "appointmentTime" to message.appointmentTime,
                        "latitude" to message.latitude,
                        "longitude" to message.longitude
                    )
                    firestore.collection("chats")
                        .document(message.chatId)
                        .collection("messages")
                        .document(message.id)
                        .set(firestoreData)
                        .await()
                }
                val metaWrite = async {
                    val chatMeta = hashMapOf(
                        "participants" to listOf(message.senderId, message.receiverId),
                        "lastMessage" to message.content,
                        "lastMessageTimestamp" to message.timestamp,
                        "lastSenderId" to message.senderId
                    )
                    firestore.collection("chats")
                        .document(message.chatId)
                        .set(chatMeta, SetOptions.merge())
                        .await()
                }
                msgWrite.await()
                metaWrite.await()
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error enviando a Firestore: ${e.message}")
        }
    }

    fun startListening(chatId: String, myUserId: String) {
        messageListener?.remove()
        messageListener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    val senderId = doc.getString("senderId") ?: continue
                    if (senderId == myUserId) continue

                    val msgType = try {
                        MessageType.valueOf(doc.getString("type") ?: "TEXT")
                    } catch (e: Exception) { MessageType.TEXT }

                    val content = if (msgType == MessageType.AUDIO) {
                        val base64 = doc.getString("audioUrl") ?: doc.getString("text") ?: ""
                        try {
                            val bytes = android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
                            val tmp = java.io.File(context.cacheDir, "audio_recv_${doc.id}.m4a")
                            tmp.writeBytes(bytes)
                            tmp.absolutePath
                        } catch (e: Exception) { doc.getString("text") ?: "" }
                    } else doc.getString("text") ?: ""

                    val message = MessageEntity(
                        id = doc.getString("messageId") ?: doc.id,
                        chatId = chatId,
                        senderId = senderId,
                        receiverId = myUserId,
                        type = msgType,
                        content = content,
                        durationSeconds = doc.getLong("audioDuration")?.toInt(),
                        latitude = doc.getDouble("latitude"),
                        longitude = doc.getDouble("longitude"),
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        isRead = false
                    )
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        chatDao.insertMessage(message)
                    }
                }
            }
    }

    fun stopListening() {
        messageListener?.remove()
        messageListener = null
    }

    suspend fun markChatAsRead(chatId: String, myUserId: String) {
        chatDao.markChatAsRead(chatId, myUserId)
    }

    fun getTotalUnreadCount(myUserId: String): Flow<Int> {
        return chatDao.getTotalUnreadCount(myUserId)
    }

    fun getUnreadCountsPerChat(myUserId: String): Flow<List<ChatUnreadCount>> {
        return chatDao.getUnreadCountsPerChat(myUserId)
    }

    suspend fun getBudgeById(budgetId: String): BudgetEntity? {
        return budgetDao.getBudgetById(budgetId)
    }

    fun getActiveChatIds(myUserId: String): Flow<List<String>> {
        return chatDao.getActiveConversationIds(myUserId)
    }

    fun getOpenTendersByCategory(category: String): Flow<List<TenderEntity>> {
        return budgetDao.getOpenTendersByCategory(category)
    }
}
