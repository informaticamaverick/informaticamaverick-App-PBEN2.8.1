package com.example.myapplication.Data

import androidx.compose.ui.graphics.Color
import com.example.myapplication.Models.ChatConversation
import com.example.myapplication.Models.Message
import java.util.Calendar

/**
 * Datos de prueba para el sistema de chat
 */
object ChatData {
    
    // Lista de conversaciones disponibles
    val conversations = listOf(
        ChatConversation(
            userId = "user_1",
            name = "Mario Bross",
            job = "Plomero",
            avatarColor = Color(0xFFEF4444), // Rojo
            lastMessage = "¡Hola! Sí, claro. ¿Qué necesitas?",
            lastMessageTime = "10:05",
            isOnline = true
        ),
        ChatConversation(
            userId = "user_2",
            name = "Ana López",
            job = "Limpieza",
            avatarColor = Color(0xFF8B5CF6), // Púrpura
            lastMessage = "Servicio finalizado con éxito.",
            lastMessageTime = "Ayer",
            isOnline = false
        ),
        ChatConversation(
            userId = "user_3",
            name = "Carlos Ruiz",
            job = "Electricista",
            avatarColor = Color(0xFF10B981), // Verde
            lastMessage = "Perfecto, entonces mañana a las 3pm",
            lastMessageTime = "Hace 2 días",
            isOnline = false
        ),
        ChatConversation(
            userId = "user_4",
            name = "Laura Méndez",
            job = "Electricista",
            avatarColor = Color(0xFF3B82F6), // Azul
            lastMessage = "Muchas gracias por el servicio",
            lastMessageTime = "15/01",
            isOnline = true
        ),
        ChatConversation(
            userId = "user_5",
            name = "José Electric",
            job = "Electricista",
            avatarColor = Color(0xFFFBBF24), // Amarillo
            lastMessage = "¿Te sirvió el presupuesto?",
            lastMessageTime = "Ayer",
            isOnline = false
        )
    )

    private fun getTimestamp(daysAgo: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return calendar.timeInMillis
    }
    
    // Mensajes de ejemplo para cada conversación
    private val messagesForMario = listOf(
        Message(
            id = 1L,
            text = "Hola, ¿estás disponible para una reparación?",
            isFromMe = true,
            time = "10:00",
            timestamp = getTimestamp(1, 10, 0)
        ),
        Message(
            id = 2L,
            text = "¡Hola! Sí, claro. ¿Qué necesitas?",
            isFromMe = false,
            time = "10:05",
            timestamp = getTimestamp(1, 10, 5)
        ),
        Message(
            id = 3L,
            text = "Necesito reparar mi lavadora, no está centrifugando bien",
            isFromMe = true,
            time = "10:07",
            timestamp = getTimestamp(0, 10, 7)
        ),
        Message(
            id = 4L,
            text = "Entendido. ¿Cuándo te vendría bien que pase a revisarla?",
            isFromMe = false,
            time = "10:10",
            timestamp = getTimestamp(0, 10, 10)
        )
    )
    
    private val messagesForAna = listOf(
        Message(
            id = 1L,
            text = "Hola Ana, ya terminé el servicio",
            isFromMe = false,
            time = "14:30",
            timestamp = getTimestamp(2, 14, 30)
        ),
        Message(
            id = 2L,
            text = "Servicio finalizado con éxito.",
            isFromMe = false,
            time = "14:35",
            timestamp = getTimestamp(2, 14, 35)
        ),
        Message(
            id = 3L,
            text = "Muchas gracias! Todo quedó perfecto",
            isFromMe = true,
            time = "14:40",
            timestamp = getTimestamp(1, 14, 40)
        )
    )
    
    private val messagesForCarlos = listOf(
        Message(
            id = 1L,
            text = "¿Puedes venir mañana?",
            isFromMe = true,
            time = "09:15",
            timestamp = getTimestamp(1, 9, 15)
        ),
        Message(
            id = 2L,
            text = "Sí, ¿a qué hora te viene bien?",
            isFromMe = false,
            time = "09:20",
            timestamp = getTimestamp(1, 9, 20)
        ),
        Message(
            id = 3L,
            text = "A las 3pm estaría genial",
            isFromMe = true,
            time = "09:22",
            timestamp = getTimestamp(0, 9, 22)
        ),
        Message(
            id = 4L,
            text = "Perfecto, entonces mañana a las 3pm",
            isFromMe = false,
            time = "09:25",
            timestamp = getTimestamp(0, 9, 25)
        )
    )
    
    private val messagesForLaura = listOf(
        Message(
            id = 1L,
            text = "El trabajo quedó excelente",
            isFromMe = true,
            time = "16:00",
            timestamp = getTimestamp(3, 16, 0)
        ),
        Message(
            id = 2L,
            text = "Muchas gracias por el servicio",
            isFromMe = true,
            time = "16:01",
            timestamp = getTimestamp(3, 16, 1)
        ),
        Message(
            id = 3L,
            text = "¡Gracias a ti! Cualquier cosa me avisas",
            isFromMe = false,
            time = "16:05",
            timestamp = getTimestamp(2, 16, 5)
        )
    )
    
    // Función para obtener mensajes por userId
    fun getMessagesForUser(userId: String): List<Message> {
        return when (userId) {
            "user_1" -> messagesForMario
            "user_2" -> messagesForAna
            "user_3" -> messagesForCarlos
            "user_4" -> messagesForLaura
            else -> emptyList()
        }
    }
    
    // Función para obtener una conversación por userId
    fun getConversationById(userId: String): ChatConversation? {
        return conversations.find { it.userId == userId }
    }
}