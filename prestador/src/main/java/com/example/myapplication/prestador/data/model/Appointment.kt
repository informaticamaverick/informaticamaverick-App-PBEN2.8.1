package com.example.myapplication.prestador.data.model

/**
 * Modelo de datos para una cita agendada
 */
data class Appointment(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val service: String = "",
    val date: String = "",  // Formato: "YYYY-MM-DD"
    val time: String = "",  // Formato: "HH:MM"
    val status: String = "pending",  // "pending", "confirmed", "cancelled", "completed"
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val proposedBy: String = "provider"  // "provider" o "client"
)

/**
 * Estados posibles de una cita
 */
enum class AppointmentStatus(val value: String) {
    CONFIRMED("confirmed"),
    PENDING("pending"),
    CANCELLED("cancelled"),
    COMPLETED("completed")
}
