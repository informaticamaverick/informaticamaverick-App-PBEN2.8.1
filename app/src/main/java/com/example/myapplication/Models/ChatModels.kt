package com.example.myapplication.Models

import androidx.compose.ui.graphics.Color

/**
 * Representa un mensaje individual en el chat
 */
data class Message(
    val id: Long,
    val text: String,
    val isFromMe: Boolean,  // true = yo envié, false = el otro usuario
    val time: String,        // Formato: "10:05"
    val timestamp: Long = System.currentTimeMillis()
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
