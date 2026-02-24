package com.example.myapplication.data.model

data class Appointment(
    val id: String = "",
    val clientId: String = "",
    val provider: String = "",
    val clientName: String = "",
    val providerName: String = "",
    val requestDate: Long = 0L, //Timestamp en milisegundos
    val requestTime: String = "",
    val notes: String = "",
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val proposedDate: Long? = null,
    val proposedTime: String? = null,
    val createdAt: Long =
        System.currentTimeMillis(),
    val chatId: String = ""
)

enum class AppointmentStatus {
    PENDING,  //Pendiente de respuesta
    ACCEPTED, //Aceptada por el prestador
    REJECTED, //Rechazada por el prestador
    RESCHEDULED //Prestador propuso otra fecha
}
