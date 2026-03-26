package com.example.myapplication.prestador.viewmodel

import android.R
import androidx.compose.animation.core.animateDecay
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
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

    private val myUserId =
        FirebaseAuth.getInstance().currentUser?.uid ?:
        ""
    private var currentConversationId = ""

    private val _isLoading =
        MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _conversations = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val conversations:
            StateFlow<List<ConversationEntity>> =
        _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages:
            StateFlow<List<MessageEntity>> =
        _messages.asStateFlow()

    private val _errorMessage =
        MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    private val _successMessage =
        MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()

    fun
            loadConversationsByProvider(providerId: String)
    {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getConversationsByProvider(providerId).collect { list ->
                    _conversations.value =
                        list
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erroral cargar conversaciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun
            loadMessagesByConversation(conversationId:
                                       String) {
        currentConversationId = conversationId

        repository.startListening(conversationId,
            myUserId)
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getMessagesByConversation(conversationId).collect { list ->
                    _messages.value = list
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erroral cargar mensajes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() ||
            currentConversationId.isEmpty()) return
        viewModelScope.launch {
            try {

                repository.sendMessage(currentConversationId,
                    text, myUserId)
            } catch (e: Exception) {
                _errorMessage.value = "Erroral enviar mensaje: ${e.message}"
            }
        }
    }


    fun sendImage(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val base64 = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri) ?:
                    return@withContext null
                    val rawBytes = inputStream.readBytes()
                    inputStream.close()
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                    val maxDim = 800
                    val scaled = if (bitmap != null && (bitmap.width > maxDim || bitmap.height > maxDim)) {
                        val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
                        android.graphics.Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * scale).toInt(),
                            (bitmap.height * scale).toInt(),
                            true
                        )
                    } else bitmap
                    val out = java.io.ByteArrayOutputStream()
                    scaled?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, out)
                    android.util.Base64.encodeToString(out.toByteArray(),
                        android.util.Base64.NO_WRAP
                    )
                } ?: return@launch
                repository.sendImageMessage(currentConversationId, base64, myUserId)
            } catch (e: Exception) {}
        }
    }


    fun sendLocation(latitude: Double, longitude: Double) {
        if (currentConversationId.isEmpty())
            return
        viewModelScope.launch {
            try {
                repository.sendLocationMessage(
                    conversationId = currentConversationId,
                    latitude = latitude,
                    longitude = longitude,
                    senderId = myUserId
                )

            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar ubicacion: ${e.message}"
            }
        }
    }



    fun sendAudioMessage(audioPath: String, durationSeconds: Int) {
        if (currentConversationId.isEmpty())
            return
        viewModelScope.launch {
            try {
                repository.sendAudioMessage(currentConversationId, audioPath, durationSeconds, myUserId)
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar audio: ${e.message}"
            }
        }
    }



    fun createConversation(conversation:
                           ConversationEntity) {
        viewModelScope.launch {
            try {

                repository.saveConversation(conversation)
                _successMessage.value =
                    "Conversación creada"
            } catch (e: Exception) {
                _errorMessage.value = "Erroral crear conversación: ${e.message}"
            }
        }
    }

    fun markMessagesAsRead(conversationId:
                           String) {
        viewModelScope.launch {
            try {

                repository.markMessagesAsRead(conversationId)
            } catch (e: Exception) {
                _errorMessage.value = "Erroral marcar mensajes: ${e.message}"
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {

                repository.deleteMessage(messageId)
            } catch (e: Exception) {
                _errorMessage.value = "Erroral eliminar mensaje: ${e.message}"
            }
        }
    }

    fun updateAppointmentStatus(messageId:
                                String, status: String, reason: String? = null)
    {
        viewModelScope.launch {
            try {

                repository.updateAppointmentStatus(messageId,
                    status, reason)
            } catch (e: Exception) {
                _errorMessage.value = "Erroral actualizar cita: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // Inicia la escucha en Firestore para descubrir conversaciones nuevas del prestador
    // y actualiza _conversations desde Room automáticamente
    fun syncConversations() {
        if (myUserId.isEmpty()) return
        repository.syncConversationsFromFirestore(myUserId)
        loadConversationsByProvider(myUserId)
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopListening()
    }
}