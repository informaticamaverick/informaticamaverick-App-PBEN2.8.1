package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.AppointmentDao
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.model.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio híbrido para gestionar citas en Firebase y Room
 */
@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao
) {
    private val db = FirebaseFirestore.getInstance()
    private val appointmentsCollection = db.collection("appointments")
    
    // ============ FIREBASE METHODS ============
    
    /**
     * Guarda una nueva cita en Firebase
     */
    suspend fun saveAppointmentToFirebase(appointment: Appointment): Result<String> {
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
     * Obtiene todas las citas del prestador desde Firebase
     */
    suspend fun getAppointmentsFromFirebase(): Result<List<Appointment>> {
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
     * Actualiza el estado de una cita en Firebase
     */
    suspend fun updateAppointmentStatusInFirebase(appointmentId: String, status: String): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ ROOM METHODS ============
    
    suspend fun saveAppointment(appointment: AppointmentEntity) {
        println("📍 AppointmentRepository.saveAppointment() - Inicio")
        println("📍 Appointment: ${appointment.id}, ${appointment.clientName}, ${appointment.date}, ${appointment.time}")
        try {
            appointmentDao.insertAppointment(appointment)
            println("📍 AppointmentRepository.saveAppointment() - ✅ Guardado en DAO")
        } catch (e: Exception) {
            println("📍 AppointmentRepository.saveAppointment() - ❌ Error: ${e.message}")
            throw e
        }
    }
    
    suspend fun updateAppointment(appointment: AppointmentEntity) {
        appointmentDao.updateAppontment(appointment)
    }
    
    suspend fun deleteAppointment(appointmentId: String) {
        appointmentDao.deleteAppointmentById(appointmentId)
    }
    
    fun getAppointmentById(appointmentId: String): Flow<AppointmentEntity?> {
        return appointmentDao.getAppointmentByIdFlow(appointmentId)
    }
    
    suspend fun getAppointmentByIdSync(appointmentId: String): AppointmentEntity? {
        return appointmentDao.getAppointmentById(appointmentId)
    }
    
    fun getAppointmentsByProvider(providerId: String): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAllAppointments(providerId)
    }
    
    fun getAppointmentsByStatus(providerId: String, status: String): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAppointmentsByStatus(providerId, status)
    }
    
    fun getAllAppointments(): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAllAppointmentsFlow()
    }
    
    suspend fun updateAppointmentStatus(appointmentId: String, status: String) {
        appointmentDao.updateAppointmentStatus(appointmentId, status, System.currentTimeMillis())
    }
}