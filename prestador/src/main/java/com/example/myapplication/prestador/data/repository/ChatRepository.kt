package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.ConversationDao
import com.example.myapplication.prestador.data.local.dao.MessageDao
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.data.model.Message
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

import javax.inject.Singleton

/**
 * Repositorio híbrido para manejar operaciones de chat y mensajes
 */
@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) {
    // ============ MÉTODOS PARA VIEWMODELS ============
    
    fun getConversationsByProvider(providerId: String): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    suspend fun saveConversation(conversation: ConversationEntity) {
        conversationDao.insertConversation(conversation)
    }

    suspend fun saveMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun markMessagesAsRead(conversationId: String) {
        conversationDao.resetUnreadCount(conversationId)
        messageDao.markAllAsRead(conversationId)
    }

    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }
    
    // ============ CONVERSACIONES ============
    
    fun getAllConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    fun getActiveConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getActiveConversations()
    }

    fun getArchivedConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getArchivedConversations()
    }

    suspend fun getConversationById(conversationId: String): ConversationEntity? {
        return conversationDao.getConversationById(conversationId)
    }

    suspend fun getConversationByUserId(userId: String): ConversationEntity? {
        return conversationDao.getConversationByUserId(userId)
    }

    suspend fun createConversation(
        userId: String,
        userName: String,
        serviceType: String? = null,
        userAvatarUrl: String? = null
    ): ConversationEntity {
        val conversation = ConversationEntity(
            conversationId = UUID.randomUUID().toString(),
            userId = userId,
            userName = userName,
            userAvatarUrl = userAvatarUrl,
            serviceType = serviceType,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        conversationDao.insertConversation(conversation)
        return conversation
    }

    suspend fun updateConversation(conversation: ConversationEntity) {
        conversationDao.updateConversation(conversation)
    }

    suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteConversationById(conversationId)
    }

    suspend fun archiveConversation(conversationId: String, archive: Boolean) {
        conversationDao.setArchived(conversationId, archive)
    }

    suspend fun pinConversation(conversationId: String, pin: Boolean) {
        conversationDao.setPinned(conversationId, pin)
    }

    suspend fun muteConversation(conversationId: String, mute: Boolean) {
        conversationDao.setMuted(conversationId, mute)
    }

    suspend fun blockConversation(conversationId: String, block: Boolean) {
        conversationDao.setBlocked(conversationId, block)
    }

    suspend fun lockConversation(conversationId: String, lock: Boolean, password: String? = null) {
        conversationDao.setLocked(conversationId, lock, password)
    }

    suspend fun markConversationAsRead(conversationId: String) {
        conversationDao.resetUnreadCount(conversationId)
        messageDao.markAllAsRead(conversationId)
    }

    fun getTotalUnreadCount(): Flow<Int> {
        return conversationDao.getTotalUnreadCountFlow()
    }

    suspend fun searchConversations(query: String): List<ConversationEntity> {
        return conversationDao.searchConversations(query)
    }

    // ============ MENSAJES ============
    
    fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByConversation(conversationId)
    }

    suspend fun getMessageById(messageId: String): MessageEntity? {
        return messageDao.getMessageById(messageId)
    }

    suspend fun sendTextMessage(conversationId: String, text: String): MessageEntity {
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "TEXT"
        )

        messageDao.insertMessage(message)
        updateConversationLastMessage(conversationId, text, message.timestamp, "TEXT")
        
        return message
    }

    suspend fun sendImageMessage(
        conversationId: String,
        imageUrl: String,
        imageLocalPath: String? = null,
        caption: String? = null
    ): MessageEntity {
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            text = caption,
            imageUrl = imageUrl,
            imageLocalPath = imageLocalPath,
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "IMAGE"
        )

        messageDao.insertMessage(message)
        updateConversationLastMessage(conversationId, "📷 Imagen", message.timestamp, "IMAGE")
        
        return message
    }

    suspend fun sendAudioMessage(
        conversationId: String,
        audioUrl: String,
        audioLocalPath: String? = null,
        duration: Int
    ): MessageEntity {
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            audioUrl = audioUrl,
            audioLocalPath = audioLocalPath,
            audioDuration = duration,
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "AUDIO"
        )

        messageDao.insertMessage(message)
        updateConversationLastMessage(conversationId, "🎤 Audio", message.timestamp, "AUDIO")
        
        return message
    }

    suspend fun sendLocationMessage(
        conversationId: String,
        latitude: Double,
        longitude: Double,
        address: String? = null
    ): MessageEntity {
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            latitude = latitude,
            longitude = longitude,
            locationAddress = address,
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "LOCATION"
        )

        messageDao.insertMessage(message)
        updateConversationLastMessage(conversationId, "📍 Ubicación", message.timestamp, "LOCATION")
        
        return message
    }
    
    suspend fun sendDocumentMessage(
        conversationId: String,
        documentUrl: String,
        documentLocalPath: String? = null,
        fileName: String,
        fileSize: Long,
        mimeType: String
    ): MessageEntity {
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            documentUrl = documentUrl,
            documentLocalPath = documentLocalPath,
            fileName = fileName,
            fileSize = fileSize,
            fileMimeType = mimeType,
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "DOCUMENT"
        )

        messageDao.insertMessage(message)
        updateConversationLastMessage(conversationId, "📄 $fileName", message.timestamp, "DOCUMENT")
        
        return message
    }

    suspend fun sendAppointmentMessage(
        conversationId: String,
        appointmentId: String,
        title: String,
        date: String,
        time: String
    ): MessageEntity {
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            appointmentId = appointmentId,
            appointmentTitle = title,
            appointmentDate = date,
            appointmentTime = time,
            appointmentStatus = "PENDING",
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            messageType = "APPOINTMENT"
        )

        messageDao.insertMessage(message)
        updateConversationLastMessage(conversationId, "📅 Cita programada", message.timestamp, "APPOINTMENT")
        
        return message
    }

    suspend fun receiveMessage(message: MessageEntity) {
        messageDao.insertMessage(message)

        // Incrementar contador de no leídos si no es del usuario actual
        if (!message.isFromCurrentUser) {
            conversationDao.incrementUnreadCount(message.conversationId)
        }

        // Actualizar último mensaje de la conversación
        val lastMessageText = when (message.messageType) {
            "TEXT" -> message.text ?: ""
            "IMAGE" -> "📷 Imagen"
            "AUDIO" -> "🎤 Audio"
            "LOCATION" -> "📍 Ubicación"
            "DOCUMENT" -> "📄 ${message.fileName ?: "Documento"}"
            "APPOINTMENT" -> "📅 Cita"
            else -> "Mensaje"
        }

        updateConversationLastMessage(
            message.conversationId,
            lastMessageText,
            message.timestamp,
            message.messageType
        )
    }

    suspend fun markMessageAsRead(messageId: String) {
        messageDao.markAsRead(messageId)
    }

    suspend fun searchMessagesInConversation(conversationId: String, query: String): List<MessageEntity> {
        return messageDao.searchMessages(conversationId, query)
    }

    fun getUnreadCount(conversationId: String): Flow<Int> {
        return messageDao.getUnreadCount(conversationId)
    }
    
    suspend fun updateAppointmentStatus(messageId: String, status: String, reason: String?) {
        messageDao.updateAppointmentStatus(messageId, status, reason)
    }

    // ============ HELPERS PRIVADOS ============
    
    private suspend fun updateConversationLastMessage(
        conversationId: String,
        lastMessage: String,
        timestamp: Long,
        messageType: String
    ) {
        conversationDao.updateLastMessage(conversationId, lastMessage, timestamp, messageType)
    }

    // ============ CONVERSIÓN DE MODELOS ============
    
    fun MessageEntity.toMessage(): Message {
        return Message(
            id = messageId,
            text = text,
            timestamp = timestamp,
            isFromCurrentUser = isFromCurrentUser,
            type = when (messageType) {
                "TEXT" -> Message.MessageType.TEXT
                "IMAGE" -> Message.MessageType.IMAGE
                "AUDIO" -> Message.MessageType.AUDIO
                "LOCATION" -> Message.MessageType.LOCATION
                "DOCUMENT" -> Message.MessageType.DOCUMENT
                "APPOINTMENT" -> Message.MessageType.APPOINTMENT
                else -> Message.MessageType.TEXT
            },
            imageUrl = imageUrl,
            audioUrl = audioUrl,
            audioDuration = audioDuration,
            latitude = latitude,
            longitude = longitude,
            fileName = fileName,
            fileSize = fileSize,
            appointmentTitle = appointmentTitle,
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime,
            appointmentId = appointmentId,
            appointmentStatus = appointmentStatus?.let { 
                when(it) {
                    "PENDING" -> Message.AppointmentProposalStatus.PENDING
                    "ACCEPTED" -> Message.AppointmentProposalStatus.ACCEPTED
                    "REJECTED" -> Message.AppointmentProposalStatus.REJECTED
                    else -> Message.AppointmentProposalStatus.PENDING
                }
            },
            rejectionReason = rejectionReason
        )
    }
}