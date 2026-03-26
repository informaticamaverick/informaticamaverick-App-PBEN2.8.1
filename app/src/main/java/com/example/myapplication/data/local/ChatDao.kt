package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // Escucha en tiempo real los mensajes de un chat ordenados por fecha
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    // Inserta o actualiza un mensaje
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // Marcar todos los mensajes de una conversación como leídos
    @Query("UPDATE messages SET isRead = 1 WHERE chatId = :chatId AND receiverId = :myUserId AND isRead = 0")
    suspend fun markChatAsRead(chatId: String, myUserId: String)

    // Contar total de mensajes no leídos para el usuario actual
    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :myUserId AND isRead = 0")
    fun getTotalUnreadCount(myUserId: String): Flow<Int>

    // Obtener un mapa de chatId -> cantidad de no leídos
    @Query("SELECT chatId, COUNT(*) as count FROM messages WHERE receiverId = :myUserId AND isRead = 0 GROUP BY chatId")
    fun getUnreadCountsPerChat(myUserId: String): Flow<List<ChatUnreadCount>>

    // Obtener IDs de usuarios con los que tengo chats (ordenados por el mensaje más reciente)
    @Query("""
        SELECT userId FROM (
            SELECT receiverId as userId, MAX(timestamp) as lastMsg FROM messages WHERE senderId = :myUserId GROUP BY receiverId
            UNION
            SELECT senderId as userId, MAX(timestamp) as lastMsg FROM messages WHERE receiverId = :myUserId GROUP BY senderId
        ) GROUP BY userId ORDER BY MAX(lastMsg) DESC
    """)
    fun getActiveConversationIds(myUserId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<MessageEntity>)
}

// Clase de apoyo para el resultado del GROUP BY
data class ChatUnreadCount(
    val chatId: String,
    val count: Int
)
