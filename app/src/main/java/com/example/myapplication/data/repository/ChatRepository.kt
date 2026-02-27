package com.example.myapplication.data.repository

import com.example.myapplication.data.local.BudgetDao
import com.example.myapplication.data.local.ChatDao
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.data.local.ChatUnreadCount
import com.example.myapplication.data.local.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val budgetDao: BudgetDao
) {

    // --- OBTENER MENSAJES ---
    fun getMessages(chatId: String): Flow<List<MessageEntity>> {
        return chatDao.getMessagesForChat(chatId)
    }

    // --- ENVIAR MENSAJE ---
    suspend fun sendMessage(message: MessageEntity) {
        chatDao.insertMessage(message)
    }

    // --- MARCAR COMO LEÍDO ---
    suspend fun markChatAsRead(chatId: String, myUserId: String) {
        chatDao.markChatAsRead(chatId, myUserId)
    }

    // --- CONTADORES DE NO LEÍDOS ---
    fun getTotalUnreadCount(myUserId: String): Flow<Int> {
        return chatDao.getTotalUnreadCount(myUserId)
    }

    fun getUnreadCountsPerChat(myUserId: String): Flow<List<ChatUnreadCount>> {
        return chatDao.getUnreadCountsPerChat(myUserId)
    }

    // Obtener un presupuesto por ID (para abrir desde el chat)
    suspend fun getBudgetById(budgetId: String): BudgetEntity? {
        return budgetDao.getBudgetById(budgetId)
    }

    // Obtener lista de IDs de chats activos (ordenados por fecha)
    fun getActiveChatIds(myUserId: String): Flow<List<String>> {
        return chatDao.getActiveConversationIds(myUserId)
    }

    // --- NUEVA FUNCIÓN PARA LICITACIONES ---
    fun getOpenTendersByCategory(category: String): Flow<List<TenderEntity>> {
        return budgetDao.getOpenTendersByCategory(category)
    }
}
