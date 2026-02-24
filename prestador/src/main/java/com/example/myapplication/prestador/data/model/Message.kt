package com.example.myapplication.prestador.data.model

data class Message(
    val id: String,
    val text: String? = null,
    val timestamp: Long,
    val isFromCurrentUser: Boolean,
    val type: MessageType = MessageType.TEXT,
    
    // Para mensajes de imagen
    val imageUrl: String? = null,
    
    // Para mensajes de audio
    val audioUrl: String? = null,
    val audioDuration: Int? = null,
    
    // Para mensajes de ubicación
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Para mensajes de documento
    val fileName: String? = null,
    val fileSize: Long? = null,
    
    // Para mensajes de cita
    val appointmentTitle: String? = null,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val appointmentId: String? = null,  // ID de la cita en la base de datos
    val appointmentStatus: AppointmentProposalStatus? = null,  // Estado de la propuesta
    val rejectionReason: String? = null  // Razón si fue rechazada
) {
    enum class MessageType {
        TEXT,
        IMAGE,
        AUDIO,
        LOCATION,
        DOCUMENT,
        APPOINTMENT
    }
    
    enum class AppointmentProposalStatus {
        PENDING,    // Esperando respuesta del cliente
        ACCEPTED,   // Cliente aceptó
        REJECTED    // Cliente rechazó
    }
}
