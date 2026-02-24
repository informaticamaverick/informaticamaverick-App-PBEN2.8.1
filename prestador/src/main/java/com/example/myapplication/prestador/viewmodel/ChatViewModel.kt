package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _conversations = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val conversations: StateFlow<List<ConversationEntity>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadConversationsByProvider(providerId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getConversationsByProvider(providerId).collect { conversations ->
                    _conversations.value = conversations
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar conversaciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMessagesByConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getMessagesByConversation(conversationId).collect { messages ->
                    _messages.value = messages
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar mensajes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(message: MessageEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.saveMessage(message)
                _successMessage.value = "Mensaje enviado"
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar mensaje: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.saveConversation(conversation)
                _successMessage.value = "Conversación creada"
            } catch (e: Exception) {
                _errorMessage.value = "Error al crear conversación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markMessagesAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                repository.markMessagesAsRead(conversationId)
            } catch (e: Exception) {
                _errorMessage.value = "Error al marcar mensajes: ${e.message}"
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteMessage(messageId)
                _successMessage.value = "Mensaje eliminado"
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar mensaje: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateAppointmentStatus(messageId: String, status: String, reason: String? = null) {
        viewModelScope.launch {
            try {
                repository.updateAppointmentStatus(messageId, status, reason)
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar estado de cita: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
