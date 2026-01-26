package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.model.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar citas en Firebase
 */
class AppointmentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val appointmentsCollection = db.collection("appointments")
    
    /**
     * Guarda una nueva cita en Firebase
     */
    suspend fun saveAppointment(appointment: Appointment): Result<String> {
        return try {
            val docRef = appointmentsCollection.document()
            val appointmentWithId = appointment.copy(id = docRef.id)
            docRef.set(appointmentWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todas las citas del prestador
     */
    suspend fun getAppointments(): Result<List<Appointment>> {
        return try {
            val snapshot = appointmentsCollection
                .orderBy("date")
                .orderBy("time")
                .get()
                .await()
            
            val appointments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza el estado de una cita
     */
    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}