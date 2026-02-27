package com.example.myapplication.presentation.client

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.MessageEntity // Asegúrate de que este import sea correcto según tu proyecto
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.presentation.util.NotificationHelper // Importamos el Helper que creamos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * VIEWMODEL DE CHAT (Room-First + Bot Simulator)
 * Centraliza la lógica de mensajería, multimedia y simulación de respuestas automáticas.
 */
class ChatViewModel(
    private val repository: ChatRepository,
    private val chatId: String,
    val currentUserId: String,
    private val receiverId: String,
    private val context: Context // [NUEVO] Necesario para las notificaciones
) : ViewModel() {

    // Helper para lanzar notificaciones en la barra de estado
    private val notificationHelper = NotificationHelper(context)

    // Lista de mensajes reactiva desde Room (se actualiza sola cuando el Bot responde)
    val messages: StateFlow<List<MessageEntity>> = repository.getMessages(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado de grabación de audio (para la UI: micro rojo)
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    // [NUEVO] Estado para el presupuesto seleccionado
    private val _selectedBudget = MutableStateFlow<BudgetEntity?>(null)
    val selectedBudget: StateFlow<BudgetEntity?> = _selectedBudget.asStateFlow()

    // Variables internas para lógica multimedia
    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioPath: String? = null

    // --- ACCIONES DE MENSAJES (USUARIO) ---

    fun sendText(text: String) {
        if (text.isBlank()) return
        // 1. Guardar mi mensaje
        sendMessageToRepo(createMessage(MessageType.TEXT, text))
        // 2. Activar respuesta automática del Bot
        triggerBotResponse(text)
    }

    fun sendImage(uri: Uri) {
        // Enviar imagen
        sendMessageToRepo(createMessage(MessageType.IMAGE, uri.toString()))
        // El bot responde a la imagen
        triggerBotResponse("IMAGEN_ENVIADA")
    }

    fun sendLocation(lat: Double, lng: Double, address: String? = null) {
        val msg = createMessage(MessageType.LOCATION, address ?: "Ubicación compartida")
            .copy(latitude = lat, longitude = lng, locationAddress = address) // Asegúrate que tu Entity tenga estos campos, si no, quítalos del copy
        sendMessageToRepo(msg)
        triggerBotResponse("UBICACION_ENVIADA")
    }

    fun sendAppointment(date: String, time: String, notes: String) {
        val content = "Solicitud de cita|$date|$time|$notes"
        val msg = createMessage(MessageType.VISIT, content)
        // .copy(appointmentDate = date, appointmentTime = time) // Descomentar si tu Entity tiene estos campos
        sendMessageToRepo(msg)
        triggerBotResponse("CITA_SOLICITADA")
    }

    fun sendBudget(budgetId: String, summary: String = "Nuevo presupuesto recibido") {
        val msg = createMessage(MessageType.BUDGET, summary)
            .copy(relatedId = budgetId)
        sendMessageToRepo(msg)
        // El bot no responde a su propio presupuesto, pero si tú lo envías, podrías activarlo
    }

    // [NUEVO] Acción al hacer clic en un presupuesto
    fun onBudgetClicked(budgetId: String) {
        viewModelScope.launch {
            _selectedBudget.value = repository.getBudgetById(budgetId)
        }
    }

    // [NUEVO] Acción para cerrar el visor de presupuesto
    fun clearSelectedBudget() {
        _selectedBudget.value = null
    }

    // =====================================================
    // 🔥 AGREGAR ESTAS FUNCIONES NUEVAS PARA LICITACIONES
    // =====================================================

    // 1. Obtener licitaciones compatibles (Soluciona "Unresolved reference 'getMatchingTenders'")
    fun getMatchingTenders(providerCategory: String): Flow<List<TenderEntity>> {
        return repository.getOpenTendersByCategory(providerCategory)
    }

    // 2. Enviar invitación (Soluciona "Unresolved reference 'sendTenderInvitation'")
    fun sendTenderInvitation(tender: TenderEntity) {
        // Guardamos Título y Descripción separados por |
        val content = "${tender.title}|${tender.description}"

        val msg = createMessage(MessageType.TENDER, content)
            .copy(relatedId = tender.tenderId)

        sendMessageToRepo(msg)
        triggerBotResponse("INVITACION_LICITACION")
    }




    // --- LÓGICA DEL BOT (Simulación de IA/Prestador) ---

    private fun triggerBotResponse(userMessage: String) {
        viewModelScope.launch @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS) {
            // 1. Simular "Escribiendo..." (espera 3 segundos)
            delay(3000)

            // 2. Lógica básica de respuesta (Switch-case inteligente)
            val lowerCaseMsg = userMessage.lowercase()
            val botResponseText = when {
                "hola" in lowerCaseMsg || "buenas" in lowerCaseMsg ->
                    "¡Hola! 👋 Soy el asistente automático. ¿En qué puedo ayudarte hoy?"

                "precio" in lowerCaseMsg || "cuanto" in lowerCaseMsg ->
                    "El precio depende del trabajo. ¿Podrías enviarme una foto del problema? 📸"

                "cita" in lowerCaseMsg || "turno" in lowerCaseMsg || "CITA_SOLICITADA" == userMessage ->
                    "Perfecto, he recibido tu solicitud. Confirmaré la disponibilidad en breve. 📅"

                "ubicacion" in lowerCaseMsg || "donde" in lowerCaseMsg || "UBICACION_ENVIADA" == userMessage ->
                    "Gracias por la ubicación. Estamos cerca, podemos ir mañana."

                "IMAGEN_ENVIADA" == userMessage ->
                    "Recibí la foto. Déjame analizarla un momento..."

                "gracias" in lowerCaseMsg ->
                    "¡De nada! 👍"

                else ->
                    "Entendido. Un especialista humano revisará tu mensaje pronto."
            }



            viewModelScope.launch {
                // 3. Crear el mensaje del Bot
                // IMPORTANTE: Invertimos los IDs. El 'sender' es el prestador (receiverId)
                val botMessage = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    senderId = receiverId, // <--- El Bot es el sender
                    receiverId = currentUserId, // <--- Tú eres el receiver
                    type = MessageType.TEXT,
                    content = botResponseText,
                    timestamp = System.currentTimeMillis(),
                    status = "SENT"
                )

                // 4. Guardar en Room (Aparece en el chat)
                repository.sendMessage(botMessage)

                // 5. Lanzar Notificación al sistema
                notificationHelper.showNotification("Nuevo mensaje", botResponseText)
            }
        }
    }

    // --- LÓGICA DE AUDIO ---

    fun startRecording(context: Context) {
        try {
            val audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
            currentAudioPath = audioFile.absolutePath
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(currentAudioPath)
                prepare()
                start()
            }
            _isRecording.value = true
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun stopRecordingAndSend() {
        if (!_isRecording.value) return
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            _isRecording.value = false
            currentAudioPath?.let { path ->
                // Enviar audio
                sendMessageToRepo(createMessage(MessageType.AUDIO, path).copy(durationSeconds = 0))
                // Bot responde al audio
                triggerBotResponse("Audio recibido")
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            _isRecording.value = false
            currentAudioPath?.let { File(it).delete() }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // Helper para crear mensajes MÍOS (Sender = Yo)
    private fun createMessage(type: MessageType, content: String) = MessageEntity(
        id = UUID.randomUUID().toString(),
        chatId = chatId,
        senderId = currentUserId,
        receiverId = receiverId,
        type = type,
        content = content,
        timestamp = System.currentTimeMillis()
    )

    private fun sendMessageToRepo(message: MessageEntity) {
        viewModelScope.launch {
            repository.sendMessage(message)
            // Aquí iría la lógica de Firebase en el futuro
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
    }



}

// ... (Lógica de audio startRecording/stopRecording) ...
fun startRecording(context: Context) { /* ... */ }
fun stopRecordingAndSend() { /* ... */ }
fun cancelRecording() { /* ... */ }



// --- FACTORY ACTUALIZADA ---
// Ahora acepta 'context' en el constructor

class ChatViewModelFactory(
    private val repository: ChatRepository,
    private val chatId: String,
    private val currentUserId: String,
    private val receiverId: String,
    private val context: Context // [NUEVO]
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, chatId, currentUserId, receiverId, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// --- FACTORY PARA INYECTAR PARÁMETROS ---
// Como el ViewModel recibe parámetros (chatId, userId), necesitamos una Factory manual.
/**
class ChatViewModelFactory(
    private val repository: ChatRepository,
    private val chatId: String,
    private val currentUserId: String,
    private val receiverId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, chatId, currentUserId, receiverId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}**/