package com.example.myapplication.Models

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
    val appointmentNotes: String? = null
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