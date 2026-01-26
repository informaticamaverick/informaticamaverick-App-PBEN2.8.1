package com.example.myapplication.prestador.data.model

import androidx.compose.ui.graphics.Color

/**
 * Representa un mensaje individual en el chat
 */
data class Message(
    val id: Long,
    val text: String,
    val isFromMe: Boolean,  // true = yo envié, false = el cliente
    val time: String,        // Formato: "10:05"
    val imageUri: String? = null,  // URI de la imagen (opcional)
    val type: String = "text",  // "text", "appointment", "image"
    val appointmentId: String? = null,  // ID de la cita en Firebase (si es appointment)
    val appointmentStatus: String = "pending"  // "pending", "confirmed", "cancelled"
)

/**
 * Representa una conversación/chat en la lista
 */
data class ChatConversation(
    val userId: String,           // ID único del cliente
    val name: String,             // Nombre del cliente
    val job: String,              // Servicio solicitado
    val avatarColor: Color,       // Color del avatar
    val lastMessage: String,      // Último mensaje enviado
    val lastMessageTime: String,  // "10:05", "Ayer", etc.
    val isOnline: Boolean = false // Estado en línea
)
