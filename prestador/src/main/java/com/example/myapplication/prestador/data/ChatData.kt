package com.example.myapplication.prestador.data

import androidx.compose.ui.graphics.Color
import com.example.myapplication.prestador.data.model.ChatConversation
import com.example.myapplication.prestador.data.model.Message

/**
 * Datos de prueba para el chat del prestador
 */
object ChatData {
    
    // Lista de conversaciones con clientes
    val conversations = listOf(
        ChatConversation(
            userId = "client_1",
            name = "María González",
            job = "Reparación de tubería",
            avatarColor = Color(0xFF3B82F6), // Azul
            lastMessage = "¿Cuándo puedes venir?",
            lastMessageTime = "10:05",
            isOnline = true
        ),
        ChatConversation(
            userId = "client_2",
            name = "Juan Pérez",
            job = "Instalación eléctrica",
            avatarColor = Color(0xFFEF4444), // Rojo
            lastMessage = "Gracias por tu ayuda",
            lastMessageTime = "Ayer",
            isOnline = false
        ),
        ChatConversation(
            userId = "client_3",
            name = "Ana Martínez",
            job = "Limpieza general",
            avatarColor = Color(0xFF10B981), // Verde
            lastMessage = "¿Cuál es el precio?",
            lastMessageTime = "Hace 2 días",
            isOnline = true
        ),
        ChatConversation(
            userId = "client_4",
            name = "Carlos Ruiz",
            job = "Reparación de lavadora",
            avatarColor = Color(0xFF8B5CF6), // Púrpura
            lastMessage = "Perfecto, nos vemos mañana",
            lastMessageTime = "15/01",
            isOnline = false
        )
    )
    
    // Mensajes de ejemplo
    private val messagesForMaria = listOf(
        Message(
            id = 1L,
            text = "Hola, necesito reparar una tubería",
            isFromMe = false,
            time = "10:00"
        ),
        Message(
            id = 2L,
            text = "¡Hola María! Con gusto te ayudo. ¿Qué tipo de reparación necesitas?",
            isFromMe = true,
            time = "10:02"
        ),
        Message(
            id = 3L,
            text = "Tengo una fuga en la cocina",
            isFromMe = false,
            time = "10:03"
        ),
        Message(
            id = 4L,
            text = "¿Cuándo puedes venir?",
            isFromMe = false,
            time = "10:05"
        )
    )
    
    private val messagesForJuan = listOf(
        Message(
            id = 1L,
            text = "El trabajo quedó excelente",
            isFromMe = false,
            time = "14:30"
        ),
        Message(
            id = 2L,
            text = "Gracias por tu ayuda",
            isFromMe = false,
            time = "14:35"
        ),
        Message(
            id = 3L,
            text = "¡Gracias a ti Juan! Cualquier cosa me avisas",
            isFromMe = true,
            time = "14:40"
        )
    )
    
    // Función para obtener mensajes por userId
    fun getMessagesForUser(userId: String): List<Message> {
        return when (userId) {
            "client_1" -> messagesForMaria
            "client_2" -> messagesForJuan
            else -> emptyList()
        }
    }
    
    // Función para obtener una conversación por userId
    fun getConversationById(userId: String): ChatConversation? {
        return conversations.find { it.userId == userId }
    }
}
