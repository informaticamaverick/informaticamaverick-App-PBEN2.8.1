package com.example.myapplication.prestador.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.ChatData
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.data.mock.ClientesMockData
import com.example.myapplication.prestador.data.repository.AppointmentRepository
import com.example.myapplication.prestador.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que maneja la simulación de respuestas automáticas en el chat.
 * Persiste mientras la app esté en memoria, NO se destruye al cambiar de pantalla.
 */
@HiltViewModel
class ChatSimulationViewModel @Inject constructor(
    application: Application,
    private val appointmentRepository: AppointmentRepository  // Inyectar repository en lugar de ViewModel
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val notificationHelper = NotificationHelper(context)
    // 🔄 Trackear timestamp del último procesamiento por mensaje ID (no appointmentId)
    // Esto permite reprocesar la misma cita si se actualiza el mensaje
    private val processedMessageTimestamps = mutableMapOf<String, Long>()
    
    init {
        println("🚀🚀🚀 ChatSimulationViewModel CREADO - Simulación iniciando...")
        startAutoResponseSimulation()
        startSpontaneousMessages()
    }

    /**
     * Inicia la simulación de respuestas automáticas a propuestas de citas
     * Corre en viewModelScope que persiste mientras el ViewModel exista
     */
    private fun startAutoResponseSimulation() {
        viewModelScope.launch {
            println("✅ Auto-respuesta iniciada en viewModelScope")
            while (true) {
                delay(2000) // Revisar cada 2 segundos
                println("🔍 Revisando conversaciones desde ViewModel...")
                
                try {
                    // Revisar TODAS las conversaciones
                    ChatData.conversations.forEach { conversation ->
                        val userId = conversation.userId
                        val messages = ChatData.getMessagesForUser(userId)
                        
                        messages.forEach { message ->
                            if (message.type == Message.MessageType.APPOINTMENT &&
                                message.appointmentStatus == Message.AppointmentProposalStatus.PENDING &&
                                message.appointmentId != null
                            ) {
                                // 🔒 Thread-safe: Verificar por timestamp del mensaje
                                synchronized(processedMessageTimestamps) {
                                    val lastProcessedTime = processedMessageTimestamps[message.id]
                                    
                                    // Procesar si:
                                    // 1. Nunca se ha procesado este mensaje, O
                                    // 2. El timestamp cambió (fue actualizado por reprogramación)
                                    if (lastProcessedTime == null || lastProcessedTime != message.timestamp) {
                                        processedMessageTimestamps[message.id] = message.timestamp
                                        println("📅 PROPUESTA ENCONTRADA: ${message.appointmentId} (msg: ${message.id}) de ${conversation.name}")
                                        println("🔐 Timestamp: ${message.timestamp}. Total procesadas: ${processedMessageTimestamps.size}")
                                        
                                        // Procesar en corrutina separada
                                        viewModelScope.launch {
                                            processAppointmentProposal(userId, conversation.name, message)
                                        }
                                    } else {
                                        // Ya procesado y no ha cambiado
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Error en simulación: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Procesa una propuesta de cita específica
     */
    private suspend fun processAppointmentProposal(
        userId: String,
        userName: String,
        message: Message
    ) {
        try {
            println("🚀 INICIANDO processAppointmentProposal para ${message.appointmentId}")
            val waitTime = (5000..10000).random().toLong()
            println("⏱️ Esperando ${waitTime}ms antes de responder a ${message.appointmentId}...")
            delay(waitTime)
            
            val accepted = (0..1).random() == 1
            println("🎲 Cliente ${if (accepted) "ACEPTA ✅" else "RECHAZA ❌"} - AppointmentId: ${message.appointmentId}")
            
            if (accepted) {
                // ✅ ACEPTAR CITA
                println("✅ Cliente $userId ACEPTA la cita ${message.appointmentId}")
                
                // 🎯 USAR NUEVO MANAGER INMUTABLE
                AppointmentRescheduleManager.updateAppointmentStatus(
                    clientId = userId,
                    appointmentId = message.appointmentId!!,
                    newStatus = Message.AppointmentProposalStatus.ACCEPTED
                )
                
                val acceptMessage = Message(
                    id = "msg_${System.currentTimeMillis()}_${userId}",
                    text = listOf(
                        "¡Perfecto! Confirmo la cita para ${message.appointmentDate} a las ${message.appointmentTime} ✅",
                        "Confirmo la cita, nos vemos el ${message.appointmentDate} 👍",
                        "Me parece bien, aceptada la cita para ${message.appointmentDate} a las ${message.appointmentTime}",
                        "¡Listo! Nos vemos el ${message.appointmentDate} a las ${message.appointmentTime} ✓"
                    ).random(),
                    isFromCurrentUser = false,
                    timestamp = System.currentTimeMillis(),
                    type = Message.MessageType.TEXT
                )
                ChatData.addMessageToUser(userId, acceptMessage)
                
                // 💾 GUARDAR/ACTUALIZAR CITA
                println("💾 Guardando/Actualizando cita en base de datos...")
                println("💾 appointmentId: ${message.appointmentId}")
                println("💾 appointmentDate: ${message.appointmentDate}")
                println("💾 appointmentTime: ${message.appointmentTime}")
                println("💾 appointmentTitle: ${message.appointmentTitle}")
                
                val providerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                println("💾 providerId: $providerId")
                
                // Verificar si la cita ya existe para mantener createdAt original
                val existingAppointment = try {
                    appointmentRepository.getAppointmentByIdSync(message.appointmentId!!)
                } catch (e: Exception) {
                    println("💾 No existe cita previa (es nueva): ${e.message}")
                    null
                }
                
                val createdTimestamp = existingAppointment?.createdAt ?: System.currentTimeMillis()
                val isUpdate = existingAppointment != null
                
                println("💾 ${if (isUpdate) "ACTUALIZANDO" else "CREANDO"} cita: ${message.appointmentId}")
                if (isUpdate) {
                    println("💾 Fecha original: ${existingAppointment?.date} ${existingAppointment?.time}")
                    println("💾 Nueva fecha: ${message.appointmentDate} ${message.appointmentTime}")
                }
                
                val appointmentEntity = AppointmentEntity(
                    id = message.appointmentId,
                    clientId = userId,
                    clientName = userName,
                    providerId = providerId,
                    service = message.appointmentTitle ?: existingAppointment?.service ?: "Servicio técnico",
                    date = message.appointmentDate ?: "",
                    time = message.appointmentTime ?: "",
                    duration = existingAppointment?.duration ?: 60,
                    status = "confirmed",
                    notes = if (isUpdate) "Cita reprogramada y confirmada por el cliente" else "Cita confirmada por el cliente",
                    proposedBy = "provider",
                    serviceType = existingAppointment?.serviceType ?: "PROFESSIONAL",
                    createdAt = createdTimestamp,  // ✅ Mantener fecha de creación original
                    updatedAt = System.currentTimeMillis()  // ✅ Actualizar timestamp de modificación
                )
                
                println("💾 Intentando guardar cita...")
                println("💾 AppointmentEntity: id=${appointmentEntity.id}, date=${appointmentEntity.date}, time=${appointmentEntity.time}")
                
                try {
                    appointmentRepository.saveAppointment(appointmentEntity)
                    println("✅✅✅ Cita ${if (isUpdate) "ACTUALIZADA" else "GUARDADA"}: ${appointmentEntity.id}")
                    println("✅ Nueva fecha/hora: ${appointmentEntity.date} a las ${appointmentEntity.time}")
                    // Programar recordatorios 24h y 1h antes
                    com.example.myapplication.prestador.utils.AppointmentReminderScheduler.schedule(context, appointmentEntity)
                } catch (e: Exception) {
                    println("❌❌❌ ERROR guardando cita: ${e.message}")
                    e.printStackTrace()
                }
                
                // 📬 NOTIFICACIÓN
                notificationHelper.showAppointmentConfirmedNotification(
                    context = context,
                    clientName = userName,
                    date = appointmentEntity.date,
                    time = appointmentEntity.time
                )
                
            } else {
                // ❌ RECHAZAR CITA
                val motivoRechazo = listOf(
                    "disculpa, ese horario no me viene bien",
                    "no puedo en ese horario, ¿tienes otro disponible?",
                    "ese día tengo otro compromiso",
                    "¿podríamos cambiar la hora?"
                ).random()
                
                // 🎯 USAR NUEVO MANAGER INMUTABLE
                AppointmentRescheduleManager.updateAppointmentStatus(
                    clientId = userId,
                    appointmentId = message.appointmentId!!,
                    newStatus = Message.AppointmentProposalStatus.REJECTED
                )
                
                val rejectMessage = Message(
                    id = "msg_${System.currentTimeMillis()}_${userId}",
                    text = "Lo siento, $motivoRechazo 😕",
                    isFromCurrentUser = false,
                    timestamp = System.currentTimeMillis(),
                    type = Message.MessageType.TEXT
                )
                ChatData.addMessageToUser(userId, rejectMessage)

                // 💾 Marcar cita como cancelada en Room
                try {
                    appointmentRepository.updateAppointmentStatus(message.appointmentId!!, "cancelled")
                    com.example.myapplication.prestador.utils.AppointmentReminderScheduler.cancel(context, message.appointmentId!!)
                } catch (e: Exception) {
                    println("❌ Error actualizando estado cancelado: ${e.message}")
                }

                notificationHelper.showMessageNotification(
                    context = context,
                    clientName = userName,
                    message = "Rechazó la cita: $motivoRechazo"
                )
            }
            
        } catch (e: Exception) {
            println("❌ Error procesando propuesta: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Inicia la simulación de mensajes espontáneos
     */
    private fun startSpontaneousMessages() {
        viewModelScope.launch {
            delay(10000) // Esperar 10 segundos al inicio
            println("✅ Mensajes espontáneos iniciados en viewModelScope")
            
            while (true) {
                val waitTime = (120000..300000).random().toLong() // 2-5 min
                delay(waitTime)
                
                try {
                    val randomClient = ClientesMockData.clientes.random()
                    val randomMessages = listOf(
                        "Hola, ¿tienes disponibilidad esta semana?",
                        "Buenos días, necesito una reparación urgente",
                        "¿Cuánto cobras por una revisión general?",
                        "Hola! Me recomendaron tu servicio",
                        "¿Podrías venir a ver un problema que tengo?",
                        "Necesito presupuesto para un trabajo",
                        "Hola, ¿trabajas los fines de semana?",
                        "Buenos días, ¿cuál es tu horario de atención?",
                        "Hola! Necesito ayuda con algo urgente",
                        "¿Puedes venir hoy por la tarde?",
                        "Me urge una reparación, ¿cuándo puedes?",
                        "Hola, ¿sigues prestando servicios?",
                        "Necesito cotización para un proyecto",
                        "¿Tienes tiempo disponible mañana?",
                        "Hola! ¿Cuánto demoras en responder?"
                    )
                    
                    val newMessage = Message(
                        id = "msg_spontaneous_${System.currentTimeMillis()}_${randomClient.id}",
                        text = randomMessages.random(),
                        isFromCurrentUser = false,
                        timestamp = System.currentTimeMillis(),
                        type = Message.MessageType.TEXT
                    )
                    
                    ChatData.addMessageToUser(randomClient.id, newMessage)
                    println("💬 Mensaje espontáneo de ${randomClient.nombre}: ${newMessage.text}")
                    
                    notificationHelper.showMessageNotification(
                        context = context,
                        clientName = randomClient.nombre,
                        message = newMessage.text ?: "Nuevo mensaje"
                    )
                } catch (e: Exception) {
                    println("❌ Error en mensaje espontáneo: ${e.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("🔴 ChatSimulationViewModel DESTRUIDO")
    }
}
