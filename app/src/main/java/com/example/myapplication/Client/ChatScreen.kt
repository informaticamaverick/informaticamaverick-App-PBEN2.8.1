package com.example.myapplication.Client

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.Data.ChatData
import com.example.myapplication.Models.Message
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.app.AlertDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import android.content.Intent
import android.icu.number.NumberFormatter
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.content.FileProvider
import java.io.File
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    initialProviderId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {}
) {
    // Manejar botón atrás del sistema
    BackHandler {
        onBack()
    }

    // Colores adaptativos
    val appColors = getAppColors()

    // Estado para la conversación activa
    var activeChatUserId by remember { mutableStateOf(initialProviderId) }

    // Notificar cuando cambia el estado de conversación
    LaunchedEffect(activeChatUserId) {
        onInConversationChange(activeChatUserId != null)
    }

    // Estado para los mensajes de la conversación activa
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }

    // Estado para el texto del input
    var inputText by remember { mutableStateOf("") }
    //Estado para mostrar menú de adjuntos
    var showAttachMenu by remember { mutableStateOf(false) }
    //Estado para la imagen seleccionada
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    //Launcher para seleccionar imagen de galeria
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            //Enviar la imagen como mensaje
            val newMessage = Message(
                id = UUID.randomUUID().toString(),
                text = "",
                imageUri = it.toString(),
                senderId = "currentUser",
                timestamp = System.currentTimeMillis(),
                status = "sent"
            )
            messages = messages + newMessage
            selectedImageUri = null
        }
    }

    //Estado para la URI de la foto de la camara
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    
    //Crear URI temporal para la foto
    val context = LocalContext.current
    val tempPhotoUri = remember {
        val photoFile = File(
            context.cacheDir,
            "camera_photo_${System.currentTimeMillis()}.jpg"
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
    }

    //Launcher para tomar foto con la camara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri = tempPhotoUri
            // Enviar la foto como mensaje
            val newMessage = Message(
                id = UUID.randomUUID().toString(),
                text = "",
                imageUri = tempPhotoUri.toString(),
                senderId = "currentUser",
                timestamp = System.currentTimeMillis(),
                status = "sent"
            )
            messages = messages + newMessage
            cameraImageUri = null
        }
    }

    // Launcher para permisos de camara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempPhotoUri)
        } else {
            //Mostrar mensaje de error
        }
    }

    // Estados para grabación de audio
    var isRecordingAudio by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFilePath by remember { mutableStateOf<String?>(null) }

    // Launcher para permisos de audio
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Iniciar grabación
            val audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
            audioFilePath = audioFile.absolutePath
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                try {
                    prepare()
                    start()
                    isRecordingAudio = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Función para detener grabación
    fun stopRecordingAndSend() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            audioFilePath?.let { path ->
                val audioUri = Uri.fromFile(File(path))
                val newMessage = Message(
                    id = UUID.randomUUID().toString(),
                    text = "[Audio]",
                    imageUri = audioUri.toString(),
                    senderId = "currentUser",
                    timestamp = System.currentTimeMillis(),
                    status = "sent"
                )
                messages = messages + newMessage
            }
            
            isRecordingAudio = false
            audioFilePath = null
        } catch (e: Exception) {
            e.printStackTrace()
            isRecordingAudio = false
        }
    }
    
    // Función para cancelar grabación
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            // Eliminar archivo de audio
            audioFilePath?.let { path ->
                File(path).delete()
            }
            
            isRecordingAudio = false
            audioFilePath = null
        } catch (e: Exception) {
            e.printStackTrace()
            isRecordingAudio = false
        }
    }

    // Cargar mensajes cuando se selecciona un chat
    LaunchedEffect(activeChatUserId) {
        if (activeChatUserId != null) {
            val chatMessages = ChatData.getMessagesForUser(activeChatUserId!!)
            messages = chatMessages.ifEmpty {
                ChatData.getMessagesForUser("user_$activeChatUserId")
            }.toMutableList()
        } else {
            messages = emptyList()
        }
    }

    // Decidir qué vista mostrar
    if (activeChatUserId == null) {
        ChatListView(
            //En lugar de: activeChatUserid = userId
            //Navegar a conversacion
            onChatClick = { userId ->
                if (navController != null) {
                    navController.navigate("chat_conversation/$userId")
                } else {
                    activeChatUserId = userId
                }
            },
            onBack = onBack,
            appColors = appColors
        )

    } else {
        // VISTA: Conversación activa
        // Obtenemos los datos del prestador para el header de la conversación
        val provider = SampleDataFalso.getPrestadorById(activeChatUserId!!)
        if (provider != null) {
            ChatConversationView(
                provider = provider,
                messages = messages,
                inputText = inputText,
                onInputChange = { inputText = it },
                onSendMessage = { messageText ->
                    // Agregar nuevo mensaje
                    val newMessage = Message(
                        id = UUID.randomUUID().toString(),
                        text = messageText,
                        senderId = "currentUser",
                        timestamp = System.currentTimeMillis()
                    )
                    messages = messages + newMessage
                    inputText = ""
                },
                onBack = {
                    activeChatUserId = null
                    inputText = ""
                },
                appColors = appColors,
                onAddImageMessage = { imageMessage ->
                    messages = messages + imageMessage
                },
                onCameraClick = {
                    // Verificar permisos de cámara
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraLauncher.launch(tempPhotoUri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onGalleryClick = {
                    imagePickerLauncher.launch("image/*")
                },
                onAudioClick = {
                    // Verificar si ya está grabando
                    if (isRecordingAudio) {
                        // Detener y enviar
                        stopRecordingAndSend()
                    } else {
                        // Verificar permisos y empezar a grabar
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            // Ya tiene permiso, iniciar grabación directamente
                            val audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
                            audioFilePath = audioFile.absolutePath
                            
                            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                MediaRecorder(context)
                            } else {
                                MediaRecorder()
                            }.apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                setOutputFile(audioFilePath)
                                try {
                                    prepare()
                                    start()
                                    isRecordingAudio = true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                onCancelAudio = {
                    cancelRecording()
                },
                isRecordingAudio = isRecordingAudio
            )
        } else {
            // Manejar caso donde el providerId no es válido o no se encuentra
            // Podríamos mostrar un mensaje de error o volver a la lista de chats.
            Text("Error: Prestador no encontrado para ID $activeChatUserId", color = appColors.textPrimaryColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListView(
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    // PASO 1: Obtenemos una lista de 10 prestadores de ejemplo para simular chats
    var providersList by remember { mutableStateOf(SampleDataFalso.prestadores.take(10)) }

    // Estado para el diálogo de eliminación
    var providerToDelete by remember { mutableStateOf<PrestadorProfileFalso?>(null) }

    // Obtenemos las categorías (profesiones) de forma dinámica de los prestadores en el chat.
    val categories = remember(providersList) { providersList.map { it.services.first() }.distinct() }

    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Filtramos los prestadores basándonos en los filtros seleccionados.
    val filteredProviders = remember(selectedCategories, providersList) {
        if (selectedCategories.isEmpty()) {
            providersList
        } else {
            providersList.filter { it.services.first() in selectedCategories }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.backgroundColor)
    ) {
        // --- Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.surfaceColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = appColors.textPrimaryColor
                    )
                }
                Text(
                    text = "Mensajes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimaryColor
                )
                Box(modifier = Modifier.size(48.dp))
            }
        }

        // --- Filtros dinámicos ---
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text(
                text = "Filtrar por profesión:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategories.contains(category)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategories = if (isSelected) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                        },
                        label = { Text(category) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Seleccionado",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }

        // --- Lista de Chats (Tarjetas rediseñadas) ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProviders, key = { it.id }) { provider ->
                PrestadorCard(
                    provider = provider,
                    onClick = { 
                        onChatClick(provider.id)
                    },
                    // onLongClick eliminado
                    onChat = { onChatClick(provider.id) },
                    onDeleteRequest = { providerToDelete = provider },
                    actionContent = {
                        ActionContent(
                            inDeleteMode = false,
                            onMessageClick = { onChatClick(provider.id) },
                            onDeleteRequest = { providerToDelete = provider }
                        )
                    }
                )
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (providerToDelete != null) {
        AlertDialog(
            onDismissRequest = { providerToDelete = null },
            title = { Text("Eliminar Conversación") },
            text = { Text("¿Estás seguro de que deseas eliminar el chat con ${providerToDelete!!.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        providersList = providersList.filter { it.id != providerToDelete!!.id }
                        providerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { providerToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

fun formatDate(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp

    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)

    return when {
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Hoy"
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Ayer"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun DateLabel(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 2.dp
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}


@Composable
fun ChatConversationView(
    provider: PrestadorProfileFalso,
    messages: List<Message>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors,
    onAddImageMessage: ((Message) -> Unit)? = null,
    onCameraClick: (() -> Unit)? = null,
    onGalleryClick: (() -> Unit)? = null,
    onAudioClick: (() -> Unit)? = null,
    onCancelAudio: (() -> Unit)? = null,
    isRecordingAudio: Boolean = false
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val groupedMessages = messages.groupBy { formatDate(it.timestamp) }
    var showAttachMenu by remember { mutableStateOf(false) }
    //Estados para agendar cita
    var showScheduleDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var appointmentNotes by remember { mutableStateOf("") }
    val context = LocalContext.current
    var isGettingLocation by remember { mutableStateOf(false) }

    // Función para obtener ubicación y enviar
    fun getLocationAndSend() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    // Crea el enlace de Google Maps
                    val googleMapsUrl = "https://maps.google.com/?q=$lat,$lng"
                    // Crea un mensaje con la ubicación
                    onSendMessage("📍 Mi ubicación\n$googleMapsUrl")
                    isGettingLocation = false
                }
            }
        } catch (e: SecurityException) {
            isGettingLocation = false
        }
    }

    // Launcher para pedir permiso de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            // Permiso concedido, obtener ubicación
            getLocationAndSend()
        }
    }

    // Auto-scroll al final cuando cambian los mensajes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                delay(100)
                listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.backgroundColor)
        ) {
            // Header
            ChatHeader(
                provider = provider,
                onBack = onBack,
                appColors = appColors
            )

            // Mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedMessages.forEach { (date, messagesOnDate) ->
                    item {
                        DateLabel(date)
                    }
                    items(messagesOnDate) { message ->
                        MessageBubble(
                            message = message,
                            appColors = appColors
                        )
                    }
                }
            }

            // Input
            MessageInputBar(
                inputText = inputText,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage,
                appColors = appColors,
                onAttachMenuToggle = { showAttachMenu = !showAttachMenu },
                onCameraClick = onCameraClick,
                onAudioClick = onAudioClick,
                onCancelAudio = onCancelAudio,
                isRecordingAudio = isRecordingAudio
            )
        }
        
        // Menú flotante de adjuntos - FUERA del flujo normal
        AnimatedVisibility(
            visible = showAttachMenu,
            enter = scaleIn(
                animationSpec = tween(300),
                transformOrigin = TransformOrigin(0f, 1f)
            ) + fadeIn(tween(200)),
            exit = scaleOut(
                animationSpec = tween(200),
                transformOrigin = TransformOrigin(0f, 1f)
            ) + fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 16.dp, start = 5.dp)
        ) {
            AttachmentOptionsMenu(
                onDismiss = { showAttachMenu = false },
                onImageClick = {
                    showAttachMenu = false
                    onGalleryClick?.invoke()
                },
                onLocationClick = {
                    showAttachMenu = false
                    // Verificar si ya tiene permisos
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        // Ya tiene permiso, obtener ubicación
                        getLocationAndSend()
                    } else {
                        // Pedir permisos
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                onScheduleClick = {
                    showAttachMenu = false
                    showScheduleDialog = true
                }
            )
        }

        //Diálogo para agendar cita
        if (showScheduleDialog) {
            ScheduleAppointmentDialog(
                onDismiss = { showScheduleDialog = false },
                onConfirm = { date, time, notes ->
                    selectedDate = date
                    selectedTime = time
                    appointmentNotes = notes
                    showScheduleDialog = false


                    //TODO: Guardar en Firebase y enviar mensaje
                    // Formatear fecha para mensaje
                    val formattedMessage = "📅 Solicitud de cita|$date|$time|$notes"
                    onSendMessage(formattedMessage)
                }
            )
        }
    }
}

@Composable
fun ChatHeader(
    provider: PrestadorProfileFalso,
    onBack: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = appColors.surfaceColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = appColors.textPrimaryColor
                )
            }

            // Avatar
            Box {
                // Contenido del avatar... (sin cambios)
            }

            // Nombre y estado
            Column {
                Text(
                    text = "${provider.name} ${provider.lastName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimaryColor
                )
                if (provider.isOnline) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "En línea",
                            fontSize = 12.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun buildAnnotatedStringWithLinks(text: String, linkColor: Color): AnnotatedString {
    val urlPattern = Regex("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+")
    return buildAnnotatedString {
        var lastIndex = 0
        urlPattern.findAll(text).forEach { matchResult ->
            // Texto antes del link
            append(text.substring(lastIndex, matchResult.range.first))
            
            // El link
            pushStringAnnotation(
                tag = "URL",
                annotation = matchResult.value
            )
            withStyle(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(matchResult.value)
            }
            pop()
            
            lastIndex = matchResult.range.last + 1
        }
        // Texto después del último link
        append(text.substring(lastIndex))
    }
}

@Composable
fun MessageBubble(
    message: Message,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    val isFromMe = message.senderId == "currentUser"
    
    // Detectar si es un mensaje de cita
    val isAppointment = message.text.startsWith("📅 Solicitud de cita|")
    
    if (isAppointment) {
        // Extraer información de la cita
        val parts = message.text.removePrefix("📅 Solicitud de cita|").split("|")
        val date = parts.getOrNull(0) ?: ""
        val time = parts.getOrNull(1) ?: ""
        val notes = parts.getOrNull(2) ?: ""
        
        TarjetaMensajeCita(
            date = date,
            time = time,
            notes = notes,
            isFromMe = isFromMe,
            appColors = appColors
        )
    } else if (message.imageUri != null) {
        // Mensaje con imagen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 280.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Mostrar la imagen
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(Uri.parse(message.imageUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen enviada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Texto si hay
                    if (message.text.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            color = if (isFromMe) Color.White else appColors.textPrimaryColor
                        )
                    }
                    
                    // Hora
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTime(message.timestamp),
                        fontSize = 11.sp,
                        color = if (isFromMe) Color.White.copy(alpha = 0.7f) else appColors.textSecondaryColor,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    } else {
        // Mensaje normal de texto
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 280.dp),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isFromMe) 16.dp else 4.dp,
                    bottomEnd = if (isFromMe) 4.dp else 16.dp
                ),
                color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    val context = LocalContext.current
                    val annotatedText = buildAnnotatedStringWithLinks(
                        text = message.text,
                        linkColor = if (isFromMe) Color.White else Color(0xFF2563EB)
                    )
                    
                    ClickableText(
                        text = annotatedText,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = if (isFromMe) Color.White else appColors.textPrimaryColor,
                            lineHeight = 20.sp
                        ),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                // Abrir el link en el navegador/Google Maps
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                context.startActivity(intent)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTime(message.timestamp),
                        fontSize = 11.sp,
                        color = if (isFromMe) Color.White.copy(alpha = 0.7f) else appColors.textSecondaryColor,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors,
    onAttachMenuToggle: () -> Unit,
    onCameraClick: (() -> Unit)? = null,
    onAudioClick: (() -> Unit)? = null,
    onCancelAudio: (() -> Unit)? = null,
    isRecordingAudio: Boolean = false
) {
    // Estado para el contador de tiempo
    var recordingTime by remember { mutableStateOf(0) }
    var isDraggingToCancel by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    var isCancelling by remember { mutableStateOf(false) }
    var trashLidClosed by remember { mutableStateOf(false) }
    var animationCompleted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Contador de tiempo mientras graba
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingTime = 0
            animationCompleted = false
            while (isRecordingAudio) {
                delay(1000)
                recordingTime++
            }
        } else {
            recordingTime = 0
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = appColors.surfaceColor,
        shadowElevation = 4.dp
    ) {
        // Usar Crossfade en lugar de AnimatedVisibility para transición suave
        Crossfade(
            targetState = isRecordingAudio,
            label = "recordingState"
        ) { isRecording ->
            if (!isRecording) {
                // UI normal de input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Botón de adjuntar con ripple azul
                    var isAttachPressed by remember { mutableStateOf(false) }
                    val attachIconColor by animateColorAsState(
                        targetValue = if (isAttachPressed) appColors.accentBlue else appColors.textSecondaryColor,
                        animationSpec = tween(durationMillis = 150),
                        label = "AttachIconColor"
                    )

                    val attachInteractionSource = remember { MutableInteractionSource() }

                    LaunchedEffect(Unit) {
                        attachInteractionSource.interactions.collect { interaction ->
                            isAttachPressed = when (interaction) {
                                is PressInteraction.Press -> true
                                is PressInteraction.Release,
                                is PressInteraction.Cancel -> false

                                else -> isAttachPressed
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                interactionSource = attachInteractionSource,
                                indication = ripple(
                                    bounded = false,
                                    radius = 24.dp,
                                    color = appColors.accentBlue
                                ),
                                onClick = onAttachMenuToggle
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Adjuntar",
                            tint = attachIconColor
                        )
                    }

                    // Campo de texto con botón integrado
                    val isTextEmpty = inputText.trim().isEmpty()

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = appColors.backgroundColor
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            TextField(
                                value = inputText,
                                onValueChange = onInputChange,
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        text = "Mensaje",
                                        color = appColors.textSecondaryColor
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = appColors.textPrimaryColor,
                                    unfocusedTextColor = appColors.textPrimaryColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = appColors.accentBlue
                                ),
                                maxLines = 4
                            )

                            // Botón de cámara (solo visible cuando no hay texto) con animación
                            AnimatedVisibility(
                                visible = isTextEmpty,
                                enter = fadeIn(tween(200)) + scaleIn(
                                    tween(200),
                                    initialScale = 0.8f
                                ),
                                exit = fadeOut(tween(200)) + scaleOut(
                                    tween(200),
                                    targetScale = 0.8f
                                )
                            ) {
                                var isCameraPressed by remember { mutableStateOf(false) }
                                val cameraIconColor by animateColorAsState(
                                    targetValue = if (isCameraPressed) appColors.accentBlue else appColors.textSecondaryColor,
                                    animationSpec = tween(durationMillis = 150),
                                    label = "CameraIconColor"
                                )

                                val cameraInteractionSource =
                                    remember { MutableInteractionSource() }

                                LaunchedEffect(Unit) {
                                    cameraInteractionSource.interactions.collect { interaction ->
                                        isCameraPressed = when (interaction) {
                                            is PressInteraction.Press -> true
                                            is PressInteraction.Release,
                                            is PressInteraction.Cancel -> false

                                            else -> isCameraPressed
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable(
                                            interactionSource = cameraInteractionSource,
                                            indication = ripple(
                                                bounded = false,
                                                radius = 20.dp,
                                                color = appColors.accentBlue
                                            ),
                                            onClick = {
                                                onCameraClick?.invoke()
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Cámara",
                                        tint = cameraIconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Botón de enviar o micrófono con gestos para grabar
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = appColors.accentBlue,
                                        shape = CircleShape
                                    )
                                    .then(
                                        if (isTextEmpty) {
                                            Modifier.pointerInput(Unit) {
                                                detectTapGestures(
                                                    onPress = {
                                                        // Esperar un poco para distinguir tap de long press
                                                        val pressed = tryAwaitRelease()
                                                        if (!pressed) {
                                                            // Se canceló el gesto
                                                            return@detectTapGestures
                                                        }
                                                    },
                                                    onLongPress = {
                                                        // Iniciar grabación
                                                        onAudioClick?.invoke()
                                                    }
                                                )
                                            }
                                        } else {
                                            Modifier.clickable {
                                                onSendMessage(inputText)
                                            }
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Solo el ícono se anima, no el botón
                                AnimatedContent(
                                    targetState = !isTextEmpty,
                                    transitionSpec = {
                                        scaleIn(
                                            animationSpec = tween(200),
                                            initialScale = 0.5f
                                        ) + fadeIn(tween(150)) togetherWith
                                                scaleOut(
                                                    animationSpec = tween(200),
                                                    targetScale = 0.5f
                                                ) + fadeOut(tween(150))
                                    },
                                    label = "IconAnimation"
                                ) { hasText ->
                                    Icon(
                                        imageVector = if (hasText) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                                        contentDescription = if (hasText) "Enviar" else "Grabar audio",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // UI de grabación (slide to cancel)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appColors.surfaceColor)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    
                                    event.changes.forEach { change ->
                                        // Detectar movimiento horizontal
                                        val dragAmountX = change.position.x - change.previousPosition.x
                                        if (dragAmountX < 0) {
                                            dragOffsetX = (dragOffsetX + dragAmountX).coerceAtLeast(-300f)
                                            isDraggingToCancel = dragOffsetX < -150f
                                        }
                                        
                                        // Detectar cuando se suelta el dedo
                                        if (change.changedToUp()) {
                                            if (dragOffsetX < -200f) {
                                                // Activar animación de cancelación
                                                isCancelling = true
                                                
                                                coroutineScope.launch {
                                                    // FASE 1: Micrófono cae dentro del tacho (200ms)
                                                    delay(200)
                                                    
                                                    // FASE 2: Tapa se cierra (150ms)
                                                    trashLidClosed = true
                                                    delay(150)
                                                    
                                                    // FASE 3: Todo se esconde hacia abajo (200ms)
                                                    delay(200)
                                                    
                                                    // Marcar que la animación terminó
                                                    animationCompleted = true
                                                    
                                                    // Cancelar el audio PRIMERO para cambiar la UI
                                                    onCancelAudio?.invoke()
                                                    
                                                    // Esperar a que el Crossfade termine su transición (300ms)
                                                    delay(300)
                                                    
                                                    // AHORA SÍ resetear todos los estados
                                                    isCancelling = false
                                                    isDraggingToCancel = false
                                                    trashLidClosed = false
                                                    dragOffsetX = 0f
                                                    animationCompleted = false
                                                }
                                            } else {
                                                // Enviar audio
                                                onAudioClick?.invoke()
                                                // Resetear
                                                dragOffsetX = 0f
                                                isDraggingToCancel = false
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            // LADO IZQUIERDO: Micrófono rojo pulsante con contador
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Icono de micrófono pulsante
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val pulseScale by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.15f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "pulse"
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Grabando",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer(
                                            scaleX = pulseScale,
                                            scaleY = pulseScale
                                        )
                                )
                                
                                Text(
                                    text = String.format("%d:%02d", recordingTime / 60, recordingTime % 60),
                                    color = appColors.textPrimaryColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            // CENTRO: Texto desliza para cancelar
                            val textAlpha by animateFloatAsState(
                                targetValue = if (isDraggingToCancel || isCancelling) 0.3f else 1f,
                                label = "textAlpha"
                            )
                            
                            Text(
                                text = "◄ Desliza para cancelar",
                                color = appColors.textSecondaryColor,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .alpha(textAlpha)
                            )
                            
                            // LADO DERECHO: Micrófono en círculo rojo pulsante
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                                    .size(40.dp)
                                    .graphicsLayer(
                                        scaleX = pulseScale,
                                        scaleY = pulseScale
                                    )
                                    .background(Color.Red, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Grabando",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MyApplicationTheme {
        ChatScreen(onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ChatConversationViewPreview() {
    MyApplicationTheme {
        ChatConversationView(
            provider = SampleDataFalso.prestadores[0],
            messages = ChatData.getMessagesForUser("user_1"),
            inputText = "Hola, necesito ayuda",
            onInputChange = {},
            onSendMessage = {},
            onBack = {},
            appColors = getAppColors(),
            onAddImageMessage = null,
            onCameraClick = null,
            onGalleryClick = null,
            onAudioClick = null,
            onCancelAudio = null,
            isRecordingAudio = false
        )
    }
}

@Composable
fun AttachmentOptionsMenu(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onLocationClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    // Menú de burbujas simple
    Column(
        modifier = Modifier.padding(start = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Imágenes
        AttachmentBubble(
            icon = com.example.myapplication.R.drawable.ic_image,
            label = "Imágenes",
            color = Color(0xFF8B5CF6),
            onClick = onImageClick
        )

        // Agendar cita
        AttachmentBubble(
            icon = com.example.myapplication.R.drawable.ic_calendar,
            label = "Agendar cita",
            color = Color(0xFF3B82F6),
            onClick = onScheduleClick
        )

        // Ubicación
        AttachmentBubble(
            icon = com.example.myapplication.R.drawable.ic_location,
            label = "Ubicación",
            color = Color(0xFF10B981),
            onClick = onLocationClick
        )
    }
}

@Composable
fun AttachmentBubble(
    icon: Int,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ícono circular
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Etiqueta
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDialog(
            onDismiss: () -> Unit,
            onConfirm: (date: String, time: String, notes: String) -> Unit
        ) {
            val appColors = getAppColors()
            var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
            var selectedTime by remember { mutableStateOf("") }
            var notes by remember { mutableStateOf("") }
            var showDatePicker by remember { mutableStateOf(false) }
            var showTimePicker by remember { mutableStateOf(false) }
            val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

            val selectedDateText = selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: ""

            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = appColors.accentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Agendar Cita",
                            style = MaterialTheme.typography.titleLarge,
                            color = appColors.textPrimaryColor
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Selector de fecha
                        val dateInteractionSource = remember { MutableInteractionSource() }
                        OutlinedTextField(
                            value = selectedDateText,
                            onValueChange = {},
                            label = { Text("Fecha") },
                            placeholder = { Text("Seleccionar fecha") },
                            readOnly = true,
                            interactionSource = dateInteractionSource,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = appColors.accentBlue
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = appColors.accentBlue,
                                focusedLabelColor = appColors.accentBlue,
                                unfocusedBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                                disabledBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                                disabledTextColor = appColors.textPrimaryColor
                            ),
                            singleLine = true
                        )

                        // Capturar el clic en el campo de fecha
                        LaunchedEffect(dateInteractionSource) {
                            dateInteractionSource.interactions.collect { interaction ->
                                if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                    showDatePicker = true
                                }
                            }
                        }

                        //Selector de hora
                        val timeInteractionSource = remember { MutableInteractionSource() }
                        OutlinedTextField(
                            value = selectedTime,
                            onValueChange = {},
                            readOnly = true,
                            interactionSource = timeInteractionSource,
                            label = { Text("Hora") },
                            placeholder = { Text("Seleccionar hora") },
                            trailingIcon = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = appColors.accentBlue
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = appColors.accentBlue,
                                focusedLabelColor = appColors.accentBlue,
                                unfocusedBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                                disabledBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                                disabledTextColor = appColors.textPrimaryColor
                            ),
                            singleLine = true
                        )

                        // Capturar el clic en el campo de hora
                        LaunchedEffect(timeInteractionSource) {
                            timeInteractionSource.interactions.collect { interaction ->
                                if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                    showTimePicker = true
                                }
                            }
                        }

                        //Notas
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notas (opcional)") },
                            placeholder = { Text("Detalles del servicio...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = appColors.accentBlue,
                                focusedLabelColor = appColors.accentBlue,
                                unfocusedBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f)
                            ),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedDateText.isNotEmpty() && selectedTime.isNotEmpty()) {
                                onConfirm(selectedDateText, selectedTime, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.accentBlue,
                            disabledContainerColor = appColors.accentBlue.copy(alpha = 0.5f)
                        ),
                        enabled = selectedDateText.isNotEmpty() && selectedTime.isNotEmpty()
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = appColors.textSecondaryColor
                        )
                    ) {
                        Text("Cancelar")
                    }
                },
                containerColor = appColors.surfaceColor,
                titleContentColor = appColors.textPrimaryColor,
                textContentColor = appColors.textPrimaryColor
            )

            // DatePicker Dialog
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedDate = datePickerState.selectedDateMillis
                                val today = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis

                                if (selectedDate != null && selectedDate >= today) {
                                    selectedDateMillis = selectedDate
                                    showDatePicker = false
                                }
                            }
                        ) {
                            Text("Aceptar", color = appColors.accentBlue)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar", color = appColors.textSecondaryColor)
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = appColors.surfaceColor
                    )
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = appColors.surfaceColor,
                            titleContentColor = appColors.textPrimaryColor,
                            headlineContentColor = appColors.textPrimaryColor,
                            weekdayContentColor = appColors.textSecondaryColor,
                            subheadContentColor = appColors.textSecondaryColor,
                            dayContentColor = appColors.textPrimaryColor,
                            selectedDayContainerColor = appColors.accentBlue,
                            todayContentColor = appColors.accentBlue,
                            todayDateBorderColor = appColors.accentBlue
                        )
                    )
                }
            }

            // TimePicker personalizado
            if (showTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = selectedTime.split(":").getOrNull(0)?.toIntOrNull() ?: 9,
                    initialMinute = selectedTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
                    is24Hour = true
                )

                androidx.compose.ui.window.Dialog(onDismissRequest = { showTimePicker = false }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = appColors.surfaceColor,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Seleccionar Hora",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimaryColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    clockDialColor = appColors.surfaceColor,
                                    clockDialSelectedContentColor = Color.White,
                                    clockDialUnselectedContentColor = appColors.textPrimaryColor,
                                    selectorColor = appColors.accentBlue,
                                    containerColor = appColors.surfaceColor,
                                    periodSelectorBorderColor = appColors.textSecondaryColor.copy(
                                        alpha = 0.3f
                                    ),
                                    periodSelectorSelectedContainerColor = appColors.accentBlue,
                                    periodSelectorUnselectedContainerColor = Color.Transparent,
                                    periodSelectorSelectedContentColor = Color.White,
                                    periodSelectorUnselectedContentColor = appColors.textSecondaryColor,
                                    timeSelectorSelectedContainerColor = appColors.accentBlue.copy(
                                        alpha = 0.2f
                                    ),
                                    timeSelectorUnselectedContainerColor = appColors.surfaceColor,
                                    timeSelectorSelectedContentColor = appColors.accentBlue,
                                    timeSelectorUnselectedContentColor = appColors.textPrimaryColor
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showTimePicker = false }) {
                                    Text("Cancelar", color = appColors.textSecondaryColor)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = {
                                        val hour = timePickerState.hour.toString().padStart(2, '0')
                                        val minute =
                                            timePickerState.minute.toString().padStart(2, '0')
                                        selectedTime = "$hour:$minute"
                                        showTimePicker = false
                                    }
                                ) {
                                    Text(
                                        "Aceptar",
                                        color = appColors.accentBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

@Composable
fun TarjetaMensajeCita(
    date: String,
    time: String,
    notes: String,
    isFromMe: Boolean,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 220.dp, max = 280.dp)
            .background(
                color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // 1. CABECERA (Icono + Título)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = if (isFromMe) Color.White.copy(alpha = 0.9f) else appColors.textPrimaryColor.copy(
                    alpha = 0.9f
                ),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFromMe) "SOLICITUD ENVIADA" else "SOLICITUD DE CITA",
                color = if (isFromMe) Color.White.copy(alpha = 0.9f) else appColors.textPrimaryColor.copy(
                    alpha = 0.9f
                ),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // 2. FECHA Y HORA
        Text(
            text = "$date, $time",
            color = if (isFromMe) Color.White else appColors.textPrimaryColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 3. NOTAS (si hay)
        if (notes.isNotEmpty()) {
            Text(
                text = notes,
                color = if (isFromMe) Color.White.copy(alpha = 0.8f) else appColors.textSecondaryColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 4. FOOTER - Estado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isFromMe) Color.White.copy(alpha = 0.2f) else appColors.backgroundColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFromMe) "⏳ Esperando confirmación" else "Propuesta recibida",
                color = if (isFromMe) Color.White else appColors.textPrimaryColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        }
    }
}


