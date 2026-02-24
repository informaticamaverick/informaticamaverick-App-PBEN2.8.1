package com.example.myapplication.prestador.viewmodel

import android.util.Log
import com.example.myapplication.prestador.data.mock.ConversacionesMock
import com.example.myapplication.prestador.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 🎯 NUEVO SISTEMA INMUTABLE para reprogramación de citas
 * 
 * Soluciona el problema de recomposición usando StateFlow inmutable.
 * NO MUTA objetos - siempre crea nuevas instancias.
 */
object AppointmentRescheduleManager {
    
    private val TAG = "🔥 RescheduleManager"
    
    // StateFlow para cada conversación: clientId -> List<Message>
    private val _conversationFlows = mutableMapOf<String, MutableStateFlow<List<Message>>>()
    
    /**
     * Obtiene el StateFlow de mensajes para un cliente específico
     */
    fun getMessagesFlow(clientId: String): StateFlow<List<Message>> {
        return _conversationFlows.getOrPut(clientId) {
            // Inicializa con mensajes actuales de ConversacionesMock
            val initialMessages = ConversacionesMock.obtenerMensajes(clientId)
            Log.d(TAG, "📥 Inicializando flow para $clientId con ${initialMessages.size} mensajes")
            MutableStateFlow(initialMessages.map { it.copy() }) // Copias inmutables
        }.asStateFlow()
    }
    
    /**
     * Actualiza propuesta de cita con NUEVA fecha/hora
     * Crea NUEVA instancia del mensaje, NO muta el existente
     */
    fun updateAppointmentProposal(
        clientId: String,
        appointmentId: String,
        newDate: String,
        newTime: String
    ) {
        Log.d(TAG, "📝 Actualizando propuesta: appointmentId=$appointmentId, fecha=$newDate, hora=$newTime")
        Log.d(TAG, "📝 ClientId recibido: $clientId")
        
        // PRIMERO actualizar en ConversacionesMock
        ConversacionesMock.actualizarPropuestaCita(clientId, appointmentId, newDate, newTime)
        Log.d(TAG, "📝 ConversacionesMock actualizado")
        
        // LUEGO sincronizar el flow con los datos actualizados
        val freshMessages = ConversacionesMock.obtenerMensajes(clientId)
        val flow = _conversationFlows.getOrPut(clientId) {
            MutableStateFlow(freshMessages.map { it.copy() })
        }
        
        // Actualizar el flow con mensajes frescos
        flow.value = freshMessages.map { it.copy() }
        
        Log.d(TAG, "🔄 Flow actualizado con ${flow.value.size} mensajes")
        
        // Debug: verificar que el mensaje está en el flow
        val appointmentMsg = flow.value.find { it.appointmentId == appointmentId }
        if (appointmentMsg != null) {
            Log.d(TAG, "✅ Mensaje en flow: appointmentId=${appointmentMsg.appointmentId}, status=${appointmentMsg.appointmentStatus}, date=${appointmentMsg.appointmentDate}, time=${appointmentMsg.appointmentTime}")
        } else {
            Log.e(TAG, "❌ ERROR: Mensaje NO encontrado en flow después de actualizar!")
        }
    }
    
    /**
     * Actualiza estado de propuesta (ACCEPTED/REJECTED)
     * Crea NUEVA instancia del mensaje, NO muta el existente
     */
    fun updateAppointmentStatus(
        clientId: String,
        appointmentId: String,
        newStatus: Message.AppointmentProposalStatus
    ) {
        Log.d(TAG, "🎯 Actualizando estado: appointmentId=$appointmentId, status=$newStatus")
        
        val flow = _conversationFlows.getOrPut(clientId) {
            MutableStateFlow(ConversacionesMock.obtenerMensajes(clientId).map { it.copy() })
        }
        
        val currentMessages = flow.value
        val updatedMessages = currentMessages.map { message ->
            if (message.type == Message.MessageType.APPOINTMENT && message.appointmentId == appointmentId) {
                // 🎯 CREAR NUEVO MENSAJE - NO MUTAR
                val updatedMessage = message.copy(
                    appointmentStatus = newStatus,
                    timestamp = System.currentTimeMillis() // Nuevo timestamp
                )
                Log.d(TAG, "✅ Estado actualizado: id=${message.id}, status=${updatedMessage.appointmentStatus}")
                updatedMessage
            } else {
                message
            }
        }
        
        // Emitir NUEVA lista para que Compose detecte el cambio
        flow.value = updatedMessages
        Log.d(TAG, "🔄 Flow actualizado con estado $newStatus")
        
        // También actualizar en ConversacionesMock para persistencia
        ConversacionesMock.actualizarEstadoPropuesta(clientId, appointmentId, newStatus)
    }
    
    /**
     * Sincroniza con ConversacionesMock (útil al inicio o después de cambios externos)
     */
    fun syncFromMock(clientId: String) {
        val freshMessages = ConversacionesMock.obtenerMensajes(clientId)
        val flow = _conversationFlows.getOrPut(clientId) {
            MutableStateFlow(emptyList())
        }
        flow.value = freshMessages.map { it.copy() }
        Log.d(TAG, "🔄 Sincronizado desde mock: ${freshMessages.size} mensajes")
    }
    
    /**
     * Agrega un nuevo mensaje a la conversación
     */
    fun addMessage(clientId: String, message: Message) {
        Log.d(TAG, "➕ Agregando mensaje: id=${message.id}, type=${message.type}")
        
        val flow = _conversationFlows.getOrPut(clientId) {
            MutableStateFlow(ConversacionesMock.obtenerMensajes(clientId).map { it.copy() })
        }
        
        // Agregar mensaje a la lista actual
        flow.value = flow.value + message
        
        // También agregar a ConversacionesMock para persistencia
        ConversacionesMock.agregarMensaje(clientId, message)
        
        Log.d(TAG, "✅ Mensaje agregado. Total: ${flow.value.size}")
    }
    
    /**
     * Reinicia el flow de un cliente (útil para testing)
     */
    fun reset(clientId: String) {
        _conversationFlows.remove(clientId)
        Log.d(TAG, "🗑️ Flow reiniciado para $clientId")
    }
}