package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.repository.CalendarRepository
import com.example.myapplication.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * --- VIEWMODEL DEL CALENDARIO ---
 * Conecta la UI de CalendarScreen con la base de datos Room y envía
 * mensajes automáticos a través de ChatRepository.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val chatRepository: ChatRepository // Usado para enviar notificaciones al chat
) : ViewModel() {

    /**
     * FLUJO DE EVENTOS EN TIEMPO REAL
     * Observa la base de datos. Cualquier cambio (inserción, actualización o borrado)
     * se reflejará instantáneamente en la UI de CalendarScreen.
     */
    val allEvents: StateFlow<List<CalendarEventEntity>> = calendarRepository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Cancela un evento y envía un mensaje automático al prestador.
     */
    fun cancelEvent(event: CalendarEventEntity, currentUserId: String) {
        viewModelScope.launch {
            // 1. Actualizar el estado en la base de datos (Room) a CANCELLED
            calendarRepository.cancelEvent(event.id)

            // 2. Enviar el mensaje automático al chat
            val messageText = "Hola ${event.provider}, me comunico para informarte que he cancelado el evento programado para el día ${event.date} a las ${event.time} hs. Disculpa las molestias."
            sendAutomatedMessage(currentUserId, event.providerId, messageText)
        }
    }

    /**
     * Inicia el proceso de reprogramación enviando un mensaje automático.
     * Nota: Esto no cambia la fecha en la DB aún, solo inicia la conversación.
     */
    fun requestReschedule(event: CalendarEventEntity, currentUserId: String) {
        viewModelScope.launch {
            val messageText = "Hola ${event.provider}, necesito reprogramar nuestra cita del día ${event.date} a las ${event.time} hs. ¿Qué horarios tienes disponibles?"
            sendAutomatedMessage(currentUserId, event.providerId, messageText)
        }
    }

    /**
     * Elimina completamente un evento de la base de datos.
     */
    fun deleteEventPermanently(eventId: String) {
        viewModelScope.launch {
            calendarRepository.deleteEvent(eventId)
        }
    }

    /**
     * Helper privado para enviar mensajes a través de ChatRepository
     */
    private suspend fun sendAutomatedMessage(senderId: String, receiverId: String, text: String) {
        // Asumimos que el ID del chat se forma combinando los IDs de cliente y prestador
        val chatId = "chat_${senderId}_${receiverId}"

        val message = com.example.myapplication.data.local.MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            type = MessageType.TEXT,
            content = text,
            timestamp = System.currentTimeMillis(),
            status = "SENT"
        )
        chatRepository.sendMessage(message)
    }
}