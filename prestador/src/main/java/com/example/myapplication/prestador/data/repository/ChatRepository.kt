package com.example.myapplication.prestador.data.repository

import android.util.Log
import com.example.myapplication.prestador.data.local.dao.ConversationDao
import com.example.myapplication.prestador.data.local.dao.MessageDao
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao:
    ConversationDao,
    private val firestore: FirebaseFirestore, @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) {
    private var messageListener:
            ListenerRegistration? = null
    private val scope =
        CoroutineScope(Dispatchers.IO)

    suspend fun sendMessage(conversationId:
                            String, text: String, myUserId: String):
            MessageEntity {
        val message = MessageEntity(
            messageId =
                UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = text,
            timestamp =
                System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "TEXT"
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, text, message.timestamp, "TEXT")
        try {
            val data = hashMapOf(
                "messageId" to
                        message.messageId,
                "chatId" to conversationId,
                "senderId" to myUserId,
                "text" to text,
                "type" to "TEXT",
                "timestamp" to
                        message.timestamp,
                "isRead" to false
            )
            firestore.collection("chats")
                .document(conversationId)
                .collection("messages")
                .document(message.messageId)
                .set(data)
                .await()
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error Firestore: ${e.message}")
        }
        return message
    }

    suspend fun sendImageMessage(conversationId: String, imageBase64: String, senderId: String) {
        val messageId = java.util.UUID.randomUUID().toString()
        val data = hashMapOf(
            "messageId" to messageId,
            "chatId" to conversationId,
            "senderId" to senderId,
            "text" to imageBase64,
            "type" to "IMAGE",
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )
        firestore.collection("chats")
            .document(conversationId)
            .collection("messages")
            .document(messageId)
            .set(data)
            .await()
    }

    suspend fun sendLocationMessage(
        conversationId: String,
        latitude: Double,
        longitude: Double,
        senderId: String
    ) {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            text = "Ubicación compartida",
            timestamp = timestamp,
            isFromCurrentUser = true,
            messageType = "LOCATION",
            latitude = latitude,
            longitude = longitude
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "\uD83D\uDCCD Ubicación compartida", timestamp, "LOCATION")
        try {
            val data = hashMapOf(
                "messageId" to messageId,
                "chatId" to conversationId,
                "senderId" to senderId,
                "text" to "Ubicación compartida",
                "type" to "LOCATION",
                "latitude" to latitude,
                "longitude" to longitude,
                "timestamp" to timestamp,
                "isRead" to false
            )
            firestore.collection("chats")
                .document(conversationId)
                .collection("messages")
                .document(messageId)
                .set(data)
                .await()
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error Firestore location: ${e.message}")
        }
    }

    suspend fun sendAudioMessage(
        conversationId: String,
        audioPath: String,
        durationSeconds: Int,
        senderId: String
    ) {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val audioBase64 = try {
            val file = java.io.File(audioPath)
            if (file.exists() && file.length() > 0)
                android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.NO_WRAP)
            else audioPath
        } catch (e: Exception) { Log.e("ChatRepo", "Error enconding audio: ${e.message}"); audioPath }
        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            text = "[Audio]",
            timestamp = timestamp,
            isFromCurrentUser = true,
            messageType = "AUDIO",
            audioUrl = audioPath,
            audioLocalPath = audioPath,
            audioDuration = durationSeconds
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, "[Audio]", timestamp, "AUDIO")
        if (true) {
            try {
                val data = hashMapOf(
                    "messageId" to messageId,
                    "chatId" to conversationId,
                    "senderId" to senderId,
                    "text" to "[AUDIO]",
                    "audioUrl" to audioBase64,
                    "audioDuration" to durationSeconds,
                    "type" to "AUDIO",
                    "timestamp" to timestamp,
                    "isRead" to false
                )
                firestore.collection("chats")
                    .document(conversationId)
                    .collection("messages")
                    .document(messageId)
                    .set(data).await()
            } catch (e: Exception) {
                Log.e("ChatRepo", "Error Firestore audio: ${e.message}")
            }
        }

    }

    fun startListening(conversationId: String,
                       myUserId: String) {
        messageListener?.remove()
        messageListener =
            firestore.collection("chats")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot,
                                       error ->
                    if (error != null || snapshot
                        == null) return@addSnapshotListener
                    for (change in
                    snapshot.documentChanges) {
                        val doc = change.document
                        val senderId =
                            doc.getString("senderId") ?: continue
                        val isOwn = senderId == myUserId
                        val msgType = doc.getString("type") ?: "TEXT"
                        val resolvedAudioUrl: String? = if (msgType == "AUDIO") {
                            val base64 = doc.getString("audioUrl")
                            if (base64 != null) try {
                                val bytes = android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
                                val tmp = java.io.File(context.cacheDir, "audio_recv_${doc.id}.m4a")
                                tmp.writeBytes(bytes)
                                tmp.absolutePath
                            } catch (e: Exception) { null }
                            else null
                        } else null
                        val msg = MessageEntity(
                            messageId = doc.getString("messageId") ?: doc.id,
                            conversationId = conversationId,
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            isFromCurrentUser = isOwn,
                            messageType = if (msgType == "VISIT") "APPOINTMENT" else msgType,
                            audioUrl = resolvedAudioUrl,
                            audioDuration = doc.getLong("audioDuration")?.toInt(),
                            latitude = doc.getDouble("latitude"),
                            longitude = doc.getDouble("longitude"),
                            appointmentId = doc.getString("appointmentId"),
                            appointmentTitle = doc.getString("appointmentTitle"),
                            appointmentDate = doc.getString("appointmentDate"),
                            appointmentTime = doc.getString("appointmentTime"),
                            appointmentStatus = doc.getString("appointmentStatus"),
                            rejectionReason = doc.getString("rejectionReason")
                        )
                        scope.launch {
                            messageDao.insertMessage(msg)
                            if (!isOwn) {
                                conversationDao.incrementUnreadCount(conversationId)
                            }
                            conversationDao.updateLastMessage(conversationId, msg.text ?: "",
                                msg.timestamp, "TEXT")
                        }
                    }
                }
    }

    fun stopListening() {
        messageListener?.remove()
        messageListener = null
    }

    // Escuchar en Firestore los chats donde el prestador es participante
    // y guardarlos en Room para mostrarlos en la lista de conversaciones
    fun syncConversationsFromFirestore(myUserId: String) {
        firestore.collection("chats")
            .whereArrayContains("participants", myUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("ChatRepo", "Error sync conversaciones: ${error?.message}")
                    return@addSnapshotListener
                }
                scope.launch {
                    for (doc in snapshot.documents) {
                        @Suppress("UNCHECKED_CAST")
                        val participants = doc.get("participants") as? List<String> ?: continue
                        val otherUserId = participants.firstOrNull { it != myUserId } ?: continue
                        val existing = conversationDao.getConversationById(doc.id)
                        // Si ya tenemos el nombre real, lo usamos; si no, lo buscamos en Firestore
                        val displayName = if (existing?.userName != null && existing.userName != otherUserId) {
                            existing.userName
                        } else {
                            try {
                                val userDoc = firestore.collection("usuarios")
                                    .document(otherUserId).get().await()
                                userDoc.getString("displayName") ?: otherUserId
                            } catch (e: Exception) {
                                Log.e("ChatRepo", "Error fetching user name: ${e.message}")
                                otherUserId
                            }
                        }
                        val conversation = ConversationEntity(
                            conversationId = doc.id,
                            userId = otherUserId,
                            userName = displayName,
                            userAvatarUrl = existing?.userAvatarUrl,
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastMessageTimestamp = doc.getLong("lastMessageTimestamp") ?: 0L,
                            unreadCount = existing?.unreadCount ?: 0,
                            notificationsEnabled = existing?.notificationsEnabled ?: true,
                            isVisible = existing?.isVisible ?: true,
                            isLocked = existing?.isLocked ?: false,
                            isSynced = true
                        )
                        conversationDao.insertConversation(conversation)
                    }
                }
            }
    }

    fun getConversationsByProvider(providerId:
                                   String): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    fun
            getMessagesByConversation(conversationId:
                                      String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByConversation(conversationId)
    }

    fun getAllConversations():
            Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    fun getActiveConversations():
            Flow<List<ConversationEntity>> {
        return conversationDao.getActiveConversations()
    }

    fun getTotalUnreadCount(): Flow<Int> {
        return conversationDao.getTotalUnreadCountFlow()
    }

    suspend fun saveMessage(message:
                            MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun saveConversation(conversation:
                                 ConversationEntity) {
        conversationDao.insertConversation(conversation)
    }

    suspend fun
            markMessagesAsRead(conversationId: String) {
        conversationDao.resetUnreadCount(conversationId)

        messageDao.markAllAsRead(conversationId)
    }

    suspend fun deleteMessage(messageId: String) { messageDao.deleteMessageById(messageId)
    }

    suspend fun
            updateAppointmentStatus(messageId: String,
                                    status: String, reason: String?) {

        messageDao.updateAppointmentStatus(messageId,
            status, reason)
    }

    suspend fun
            getConversationById(conversationId: String):
            ConversationEntity? {
        return conversationDao.getConversationById(conversationId)
    }
}