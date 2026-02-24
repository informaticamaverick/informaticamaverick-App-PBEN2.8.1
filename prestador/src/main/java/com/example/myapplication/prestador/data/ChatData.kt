package com.example.myapplication.prestador.data

import androidx.compose.ui.graphics.Color
import com.example.myapplication.prestador.data.mock.ClientesMockData
import com.example.myapplication.prestador.data.mock.ConversacionesMock
import com.example.myapplication.prestador.data.model.ChatConversation
import com.example.myapplication.prestador.data.model.Message

/**
 * Datos de prueba para el chat del prestador
 * Usa los clientes mock y conversaciones realistas
 */
object ChatData {
    
    // Clase de conversación para la lista de chats
    data class Conversation(
        val userId: String,
        val userName: String,
        val lastMessage: String,
        val timestamp: Long,
        val unreadCount: Int = 0,
        val notificationsEnabled: Boolean = true,
        val isVisible: Boolean = true,
        val isLocked: Boolean = false
    )
    
    // Generar conversaciones desde ClientesMock
    val conversations = ClientesMockData.clientes.take(10).mapIndexed { index, cliente ->
        ChatConversation(
            userId = cliente.id,
            name = "${cliente.nombre} ${cliente.apellido}",
            job = "Servicio de peluquería", // TODO: podría ser más específico
            avatarColor = getAvatarColor(index),
            lastMessage = cliente.ultimoMensaje ?: "Sin mensajes",
            lastMessageTime = formatLastMessageTime(index),
            isOnline = cliente.isOnline
        )
    }
    
    // Colores para avatares (rotar entre una paleta)
    private fun getAvatarColor(index: Int): Color {
        val colors = listOf(
            Color(0xFF3B82F6), // Azul
            Color(0xFFEF4444), // Rojo
            Color(0xFF10B981), // Verde
            Color(0xFF8B5CF6), // Púrpura
            Color(0xFFF59E0B), // Amarillo
            Color(0xFFEC4899), // Rosa
            Color(0xFF14B8A6), // Teal
            Color(0xFFF97316), // Naranja
            Color(0xFF6366F1), // Índigo
            Color(0xFF84CC16)  // Lima
        )
        return colors[index % colors.size]
    }
    
    // Formatear tiempo del último mensaje
    private fun formatLastMessageTime(index: Int): String {
        return when (index) {
            0 -> "Hace 10 min"
            1 -> "Hace 30 min"
            2 -> "Hace 1 hora"
            3 -> "Hace 2 horas"
            4 -> "Ayer"
            5 -> "Hace 2 días"
            6 -> "Hace 3 días"
            7 -> "Lunes"
            8 -> "15/02"
            else -> "14/02"
        }
    }
    
    // Método para obtener todas las conversaciones
    fun getAllConversations(): List<Conversation> {
        return conversations.map { chat ->
            Conversation(
                userId = chat.userId,
                userName = chat.name,
                lastMessage = chat.lastMessage,
                timestamp = System.currentTimeMillis() - (1..10000000).random(),
                unreadCount = if (chat.isOnline) (0..3).random() else 0,
                notificationsEnabled = true,
                isVisible = true,
                isLocked = false
            )
        }
    }
    
    // Función para obtener mensajes por userId
    fun getMessagesForUser(userId: String): List<Message> {
        return ConversacionesMock.obtenerMensajes(userId)
    }
    
    // Función para obtener una conversación por userId
    fun getConversationById(userId: String): ChatConversation? {
        return conversations.find { it.userId == userId }
    }
    
    // Función para agregar mensaje a un usuario específico
    fun addMessageToUser(userId: String, message: Message) {
        ConversacionesMock.agregarMensaje(userId, message)
    }
    
    // Función para actualizar una propuesta de cita con nueva fecha/hora
    fun updateAppointmentProposal(
        userId: String,
        appointmentId: String,
        newDate: String,
        newTime: String
    ) {
        ConversacionesMock.actualizarPropuestaCita(userId, appointmentId, newDate, newTime)
    }
    
    // Función para actualizar estado de propuesta de cita
    fun updateAppointmentStatus(
        userId: String,
        appointmentId: String,
        newStatus: Message.AppointmentProposalStatus,
        rejectionReason: String? = null
    ) {
        ConversacionesMock.actualizarEstadoPropuesta(userId, appointmentId, newStatus, rejectionReason)
    }
}
