package com.example.myapplication.prestador.ui.chat

import android.Manifest
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.ChatData
import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.ui.presupuesto.BudgetItem
import com.example.myapplication.prestador.ui.presupuesto.BudgetMiscExpense
import com.example.myapplication.prestador.ui.presupuesto.BudgetProfessionalFee
import com.example.myapplication.prestador.ui.presupuesto.BudgetPreviewPDFDialog
import com.example.myapplication.prestador.ui.presupuesto.BudgetService
import com.example.myapplication.prestador.ui.presupuesto.BudgetTax
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.AppointmentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.appendPlaceholders
import com.example.myapplication.prestador.utils.NotificationHelper
import com.example.myapplication.prestador.utils.displayAddress
import com.example.myapplication.prestador.utils.displayCompanyOrFullName
import com.example.myapplication.prestador.utils.toPrestadorProfileFalso
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    userId: String,
    userName: String,
    providerId: String,  // Ahora es requerido, se pasa desde ChatScreen
    onBack: () -> Unit,
    onNavigateToPresupuesto: () -> Unit = {},
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    editProfileViewModel: com.example.myapplication.prestador.viewmodel.EditProfileViewModel = hiltViewModel(),
    rentalSpacesViewModel: com.example.myapplication.prestador.viewmodel.RentalSpacesViewModel = hiltViewModel(),
    empleadosViewModel: com.example.myapplication.prestador.viewmodel.EmpleadosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    
    // Estado para saber si este chat está visible
    var isChatVisible by remember { mutableStateOf(true) }
    
    // Detectar cuando el chat se muestra/oculta
    DisposableEffect(Unit) {
        isChatVisible = true
        println("🟢 ChatConversationScreen con $userName ahora VISIBLE")
        
        onDispose {
            isChatVisible = false
            println("🔴 ChatConversationScreen con $userName ahora OCULTO (DESTRUIDO)")
        }
    }
    
    //Solicitar permisos de notficaciones en Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("Permiso de notificaciones otorgado")
        } else {
            println("Permiso de notificaciones denegado")
        }
    }

    //Pedir permiso al iniciar
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationHelper.hasNotificationPermission()) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    


    val colors = getPrestadorColors()
    
    // 🎯 OBSERVAR StateFlow inmutable del nuevo manager
    val messages by com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager
        .getMessagesFlow(userId)
        .collectAsState()
    
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Estados para adjuntos y grabación
    var showAttachMenu by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }

    // Estado del diálogo de agendar cita
    var showScheduleDialog by remember { mutableStateOf(false) }

    // Estado del sheet de presupuesto
    var showBudgetSheet by remember { mutableStateOf(false) }

    // Presupuesto a visualizar al hacer clic en "Ver presupuesto"
    var presupuestoMsgToView by remember { mutableStateOf<Message?>(null) }

    // Datos según tipo de servicio
    val profileState by editProfileViewModel.profileState.collectAsState()
    val businessEntity by editProfileViewModel.businessEntity.collectAsState()

    val currentServiceType = remember(profileState) {
        when (profileState) {
            is com.example.myapplication.prestador.viewmodel.ProfileState.Success ->
                com.example.myapplication.prestador.data.model.ServiceType.fromString(
                    (profileState as com.example.myapplication.prestador.viewmodel.ProfileState.Success).provider.serviceType
                )
            else -> com.example.myapplication.prestador.data.model.ServiceType.PROFESSIONAL
        }
    }

    // En algunos flujos, el providerId real para horarios es el del perfil (Room/Firebase),
    // no necesariamente el UID de Auth.
    val effectiveProviderId = remember(profileState, providerId) {
        val fromProfile = (profileState as? com.example.myapplication.prestador.viewmodel.ProfileState.Success)?.provider?.id
        if (!fromProfile.isNullOrBlank()) fromProfile else providerId
    }

    val provider = (profileState as? com.example.myapplication.prestador.viewmodel.ProfileState.Success)?.provider
    val providerDisplayName = provider?.displayCompanyOrFullName(businessEntity).orEmpty()
    val providerDisplayAddress = provider?.displayAddress(businessEntity).orEmpty()
    val rentalSpaces by rentalSpacesViewModel.rentalSpaces.collectAsState()
    val availableSpaces = remember(rentalSpaces) {
        rentalSpaces.filter { it.isActive }.map { it.id to it.name }
    }
    LaunchedEffect(providerId, currentServiceType) {
        if (currentServiceType == com.example.myapplication.prestador.data.model.ServiceType.RENTAL && providerId.isNotBlank())
        {
            rentalSpacesViewModel.setProviderId(providerId)
        }
    }
    val availableSlots by appointmentViewModel.availabilitySlots.collectAsState()
    val slotsLoading by appointmentViewModel.availabilityLoading.collectAsState()
    val empleadosState by empleadosViewModel.uiState.collectAsState()
    val availableEmployees = remember(empleadosState) {
        when (val state = empleadosState) {
            is com.example.myapplication.prestador.viewmodel.EmpleadosUiState.Success ->
                state.empleados.filter { it.activo }.map { it.id to it.nombreCompleto() }

            else -> emptyList()
        }
    }
    
    // Estado para guardar datos de cita temporalmente
    var pendingAppointmentDate by remember { mutableStateOf("") }
    var pendingAppointmentTime by remember { mutableStateOf("") }
    
    // Estados para imágenes
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Estados para grabación de audio
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFilePath by remember { mutableStateOf<String?>(null) }

    // Estado para rastrear propuestas pendientes y sus timers
    var pendingProposals by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Estado para mostrar indicador de "escribiendo..."
    var isClientTyping by remember { mutableStateOf(false) }
    var typingClientName by remember { mutableStateOf("") }

    // 🔄 Sincronizar periódicamente desde ChatData para capturar mensajes del simulador
    LaunchedEffect(userId, isChatVisible) {
        if (isChatVisible) {
            while (true) {
                delay(1000) // Sincronizar cada segundo
                com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.syncFromMock(userId)
            }
        }
    }

    // ⚠️ NOTA: La lógica de auto-respuesta y mensajes espontáneos
    // ahora está en ChatScreen.kt para que persista al salir del chat
    
    // LaunchedEffect para contador de tiempo de grabación
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTime = 0
            while (isRecording) {
                delay(1000)
                recordingTime++
            }
        } else {
            recordingTime = 0
        }
    }

    // LaunchedEffect para abrir el teclado automáticamente
    LaunchedEffect(Unit) {
        delay(600)
        keyboardController?.show()
    }
    
    // SideEffect para forzar el foco en el campo de texto
    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            delay(700)
            keyboardController?.show()
        }
        onDispose {
            job.cancel()
        }
    }
    
    // Crear URI temporal para la foto
    val tempPhotoUri = remember {
        val photoFile = File(
            context.cacheDir,
            "camera_photo_${System.currentTimeMillis()}.jpg"
        )
        FileProvider.getUriForFile(
            context,
            "com.example.myapplication.prestador.fileprovider",
            photoFile
        )
    }
    
    // Launcher para tomar foto con la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri = tempPhotoUri
            // Enviar la foto como mensaje
            val newMessage = Message(
                id = (messages.size + 1).toString(),
                text = "",
                imageUrl = tempPhotoUri.toString(),
                timestamp = System.currentTimeMillis(),
                isFromCurrentUser = true,
                type = Message.MessageType.IMAGE
            )
            com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.addMessage(userId, newMessage)
            cameraImageUri = null
            
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempPhotoUri)
        } else {
            // Mostrar mensaje de error
        }
    }
    
    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            // Enviar la imagen como mensaje
            val newMessage = Message(
                id = (messages.size + 1).toString(),
                text = "",
                imageUrl = uri.toString(),
                timestamp = System.currentTimeMillis(),
                isFromCurrentUser = true,
                type = Message.MessageType.IMAGE
            )
            com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.addMessage(userId, newMessage)
            selectedImageUri = null
            
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    // Función para iniciar grabación
    fun startRecording() {
        val audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
        audioFilePath = audioFile.absolutePath
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFilePath)
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: Exception) {
                e.printStackTrace()
                isRecording = false
            }
        }
    }
    
    // Launcher para permisos de audio
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        }
    }
    
    // Launcher para permisos de calendario
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[android.Manifest.permission.READ_CALENDAR] ?: false
        val writeGranted = permissions[android.Manifest.permission.WRITE_CALENDAR] ?: false
        
        if (readGranted && writeGranted) {
            // Permisos concedidos, mostrar el diálogo
            showScheduleDialog = true
        } else {
            android.widget.Toast.makeText(
                context,
                "Se necesitan permisos de calendario para agendar citas",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    // Launcher para permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permiso concedido, obtener ubicación
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val newMessage = Message(
                            id = (messages.size + 1).toString(),
                            text = "Mi ubicación",
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = System.currentTimeMillis(),
                            isFromCurrentUser = true,
                            type = Message.MessageType.LOCATION
                        )
                        com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.addMessage(userId, newMessage)
                        
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "No se pudo obtener tu ubicación actual",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: SecurityException) {
                android.widget.Toast.makeText(
                    context,
                    "Error al obtener ubicación",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Permiso denegado
            android.widget.Toast.makeText(
                context,
                "Se necesita permiso de ubicación para compartir tu posición",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Función para detener grabación y enviar
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
                    id = (messages.size + 1).toString(),
                    text = "[Audio]",
                    audioUrl = audioUri.toString(),
                    timestamp = System.currentTimeMillis(),
                    isFromCurrentUser = true,
                    type = Message.MessageType.AUDIO
                )
                com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.addMessage(userId, newMessage)
                
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            }
            
            isRecording = false
            recordingTime = 0
            audioFilePath = null
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
            recordingTime = 0
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
            
            isRecording = false
            recordingTime = 0
            audioFilePath = null
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
            recordingTime = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color(0xFFF97316)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Column {
                            Text(
                                text = userName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "En línea",
                                fontSize = 12.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.textPrimary
                        )
                    }
                },
                actions = {
                    // Botones de acción removidos temporalmente
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceColor
                )
            )
        },
        containerColor = colors.backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundColor)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.backgroundColor)
            ) {
                // Lista de mensajes
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mostrar indicador de "escribiendo..." si está activo
                    if (isClientTyping) {
                        item(key = "typing_indicator") {
                            TypingIndicator(userName = typingClientName)
                        }
                    }
                    
                    items(
                        messages.reversed(),
                        key = { message ->
                            // El StateFlow garantiza que cada cambio crea nueva instancia
                            "${message.id}-${message.timestamp}"
                        }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            isFromCurrentUser = message.isFromCurrentUser,
                            onReschedule = {
                                // Abrir diálogo para reprogramar
                                showScheduleDialog = true
                            },
                            onVerPresupuesto = if (message.type == Message.MessageType.BUDGET) {
                                { presupuestoMsgToView = message }
                            } else null
                        )
                    }
                }
                
                // Barra de entrada de mensajes
                MessageInputBar(
                    messageText = messageText,
                    onMessageTextChange = { messageText = it },
                    onSendMessage = {
                        if (messageText.isNotBlank()) {
                            val newMessage = Message(
                                id = (messages.size + 1).toString(),
                                text = messageText,
                                timestamp = System.currentTimeMillis(),
                                isFromCurrentUser = true,
                                type = Message.MessageType.TEXT
                            )
                            com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.addMessage(userId, newMessage)
                            messageText = ""
                            
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                    },
                    onAttachClick = { showAttachMenu = !showAttachMenu },
                    onCameraClick = {
                        // Verificar si ya tiene permisos de cámara
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            // Ya tiene permiso, abrir cámara directamente
                            cameraLauncher.launch(tempPhotoUri)
                        } else {
                            // Solicitar permisos
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onMicClick = {
                        if (isRecording) {
                            // Ya está grabando, detener y enviar
                            stopRecordingAndSend()
                        } else {
                            // Verificar permisos y empezar a grabar
                            if (androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                // Ya tiene permiso, iniciar grabación directamente
                                startRecording()
                            } else {
                                // Solicitar permisos
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    onCancelAudio = {
                        cancelRecording()
                    },
                    isRecording = isRecording,
                    recordingTime = recordingTime
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
                    serviceType = currentServiceType,
                    onDismiss = { showAttachMenu = false },
                    onImageClick = {
                        showAttachMenu = false
                        galleryLauncher.launch("image/*")
                    },
                    onCameraClick = {
                        showAttachMenu = false
                        // Verificar si ya tiene permisos de cámara
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            // Ya tiene permiso, abrir cámara directamente
                            cameraLauncher.launch(tempPhotoUri)
                        } else {
                            // Solicitar permisos
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onLocationClick = {
                        showAttachMenu = false
                        // Verificar permisos de ubicacion
                        val hasFineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, 
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        val hasCoarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        if (hasFineLocation || hasCoarseLocation) {
                            // Obtener ubicacion actual
                            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        // Enviar mensaje con ubicacion
                                        val newMessage = Message(
                                            id = (messages.size + 1).toString(),
                                            text = "Mi ubicación",
                                            latitude = location.latitude,
                                            longitude = location.longitude,
                                            timestamp = System.currentTimeMillis(),
                                            isFromCurrentUser = true,
                                            type = Message.MessageType.LOCATION
                                        )
                                        com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.addMessage(userId, newMessage)
                                        coroutineScope.launch { 
                                            listState.animateScrollToItem(0) 
                                        }
                                    } else {
                                        // No se pudo obtener la ubicación
                                        android.widget.Toast.makeText(
                                            context, 
                                            "No se pudo obtener tu ubicación",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: SecurityException) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Error al obtener ubicación",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Solicitar permisos
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    onDocumentClick = {
                        showAttachMenu = false
                        showBudgetSheet = true
                    },
                    onScheduleClick = {
                        showAttachMenu = false
                        // Verificar permisos de calendario
                        val hasReadPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.READ_CALENDAR
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        val hasWritePermission = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.WRITE_CALENDAR
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        if (hasReadPermission && hasWritePermission) {
                            // Ya tiene permisos, mostrar diálogo
                            showScheduleDialog = true
                        } else {
                            // Solicitar permisos
                            calendarPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_CALENDAR,
                                    android.Manifest.permission.WRITE_CALENDAR
                                )
                            )
                        }
                    }
                )
            }
        }
        
        // Diálogo de agendar cita
        if (showScheduleDialog) {
            com.example.myapplication.prestador.ui.appointments.CreateAppointmentDialog(
                serviceType = currentServiceType,
                onDismiss = { showScheduleDialog = false },
                onRequestSlots = { date, duration ->
                    if (effectiveProviderId.isNotBlank()) {
                        println("🟠 Chat: loadAvailabilitySlots providerId=$effectiveProviderId date=$date duration=$duration")
                        appointmentViewModel.loadAvailabilitySlots(effectiveProviderId, date, duration)
                    } else {
                        println("🔴 Chat: providerId vacío, no se pueden cargar turnos")
                    }
                },
                slotsRequestKey = effectiveProviderId,
                isSlotsLoading = slotsLoading,
                onConfirm = { clientName, service, date, time, duration, rentalSpaceId, scheduleId, notes, assignedEmployeeIds, peopleCount ->
                    showScheduleDialog = false
                    val appointmentId= "apt_${userId}_${System.currentTimeMillis()}"
                    val newAppointment = AppointmentEntity(
                        id = appointmentId,
                        clientId = userId,
                        clientName = userName,
                        providerId = effectiveProviderId,
                        service = service,
                        date = date,
                        time = time,
                        duration = duration,
                    status = "pending",
                        notes = notes,
                        serviceType = currentServiceType.name,
                        rentalSpaceId = rentalSpaceId,
                        scheduleId = scheduleId,
                        assignedEmployeeIds = assignedEmployeeIds,
                        peopleCount = peopleCount
                    )
                    appointmentViewModel.saveAppointment(newAppointment)

                    val newMessage = Message(
                        id = "msg_appointment_${System.currentTimeMillis()}_${userId}", text = "Cita prpuesta",
                        timestamp = System.currentTimeMillis(),
                        isFromCurrentUser = true,
                        type = Message.MessageType.APPOINTMENT,
                        appointmentDate = date,
                        appointmentTime = time,
                        appointmentId = appointmentId,
                        appointmentStatus = Message.AppointmentProposalStatus.PENDING,
                        appointmentTitle = service
                    )

                    com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.addMessage(userId, newMessage)
                    coroutineScope.launch { listState.animateScrollToItem(0) }


                },
                colors = com.example.myapplication.prestador.ui.theme.getPrestadorColors(),
                availableSpaces = availableSpaces,
                availableSlots = availableSlots,
                availableEmployees = if (currentServiceType == com.example.myapplication.prestador.data.model.ServiceType.TECHNICAL)
                    availableEmployees else emptyList(),
                initialClientName = userName
            )

        }

        // Sheet de presupuesto en el chat
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && showBudgetSheet) {
            com.example.myapplication.prestador.ui.presupuesto.BudgetChatSheet(
                userId = userId,
                userName = userName,
                onDismiss = { showBudgetSheet = false }
            )
        }

        // Vista previa de presupuesto al hacer clic en "Ver presupuesto"
        presupuestoMsgToView?.let { msg ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fun parseItems(json: String?) = if (json.isNullOrBlank()) emptyList() else
                    json.split("|").mapNotNull { s ->
                        val p = s.split(";")
                        if (p.size >= 4) BudgetItem(
                            id = 0L, code = p[0], description = p[1],
                            quantity = p[2].toIntOrNull() ?: 1,
                            unitPrice = p[3].toDoubleOrNull() ?: 0.0,
                            taxPercentage = p.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                            discountPercentage = p.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                        ) else null
                    }
                fun parseServices(json: String?) = if (json.isNullOrBlank()) emptyList() else
                    json.split("|").mapNotNull { s ->
                        val p = s.split(";")
                        if (p.size >= 2) BudgetService(id = 0L, code = p[0], description = p[1], total = p.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
                    }
                fun parseFees(json: String?) = if (json.isNullOrBlank()) emptyList() else
                    json.split("|").mapNotNull { s ->
                        val p = s.split(";")
                        if (p.size >= 2) BudgetProfessionalFee(id = 0L, code = p[0], description = p[1], total = p.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
                    }
                fun parseMisc(json: String?) = if (json.isNullOrBlank()) emptyList() else
                    json.split("|").mapNotNull { s ->
                        val p = s.split(";")
                        if (p.size >= 2) BudgetMiscExpense(id = 0L, description = p[0], amount = p[1].toDoubleOrNull() ?: 0.0) else null
                    }
                fun parseTaxes(json: String?) = if (json.isNullOrBlank()) emptyList() else
                    json.split("|").mapNotNull { s ->
                        val p = s.split(";")
                        if (p.size >= 2) BudgetTax(id = 0L, description = p[0], amount = p[1].toDoubleOrNull() ?: 0.0) else null
                    }

                val prestador = remember(msg.id, provider?.id, providerDisplayName, providerDisplayAddress) {
                    provider?.toPrestadorProfileFalso(businessEntity) ?: PPrestadorProfileFalso(
                        id = effectiveProviderId.ifBlank { "demo" },
                        name = providerDisplayName.ifBlank { "Prestador" },
                        lastName = "",
                        profileImageUrl = "",
                        bannerImageUrl = null,
                        rating = 0f,
                        isVerified = false,
                        isOnline = false,
                        services = emptyList(),
                        companyName = null,
                        address = providerDisplayAddress,
                        email = "",
                        doesHomeVisits = false,
                        hasPhysicalLocation = false,
                        works24h = false,
                        galleryImages = emptyList(),
                        isFavorite = false,
                        isSubscribed = false
                    )
                }

                BudgetPreviewPDFDialog(
                    prestador = prestador,
                    items = parseItems(msg.budgetItemsJson),
                    services = parseServices(msg.budgetServiciosJson),
                    professionalFees = parseFees(msg.budgetHonorariosJson),
                    miscExpenses = parseMisc(msg.budgetGastosJson),
                    taxes = parseTaxes(msg.budgetImpuestosJson),
                    grandTotal = msg.budgetTotal ?: 0.0,
                    subtotal = msg.budgetSubtotal ?: 0.0,
                    taxAmount = msg.budgetImpuestos ?: 0.0,
                    discountAmount = 0.0,
                    showSendButton = false,
                    providerName = providerDisplayName,
                    providerAddress = providerDisplayAddress,
                    isProfessional = currentServiceType == com.example.myapplication.prestador.data.model.ServiceType.PROFESSIONAL,
                    presupuestoNumero = msg.budgetNumero ?: "",
                    onDismiss = { presupuestoMsgToView = null },
                    onEnviar = { presupuestoMsgToView = null }
                )
            }
        }
    }
}
