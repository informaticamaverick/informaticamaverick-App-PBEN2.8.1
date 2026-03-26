package com.example.myapplication.data.model

import androidx.compose.ui.graphics.Color

/**
 * Representa un mensaje individual en el chat
 */
data class Message(
    val id: String,
    val text: String,
    val senderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "sent",
    val imageUri: String? = null,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val locationAddress: String? = null,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val appointmentNotes: String? = null,
    // Para mensajes de presupuesto (solo strings)
    val budgetNumero: String? = null,
    val budgetTotal: Double? = null,
    val budgetSubtotal: Double? = null,
    val budgetImpuestos: Double? = null,
    val budgetItemsJson: String? = null,
    val budgetServiciosJson: String? = null,
    val budgetHonorariosJson: String? = null,
    val budgetGastosJson: String? = null,
    val budgetImpuestosJson: String? = null,
    val budgetNotas: String? = null,
    val budgetValidezDias: Int? = null
)

/**
 * Representa una conversación/chat en la lista
 */
data class ChatConversation(
    val userId: String,           // ID único del usuario
    val name: String,             // Nombre del usuario
    val job: String,              // Profesión del usuario (para agrupar)
    val avatarColor: Color,       // Color del avatar
    val lastMessage: String,      // Último mensaje enviado
    val lastMessageTime: String,  // "10:05", "Ayer", etc.
    val isOnline: Boolean = false // Estado en línea
)