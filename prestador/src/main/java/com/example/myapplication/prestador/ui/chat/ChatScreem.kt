package com.example.myapplication.prestador.ui.chat

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.net.Uri
import android.view.PixelCopy
import android.view.Surface
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.ChatData
import com.example.myapplication.prestador.data.model.ChatConversation
import com.example.myapplication.prestador.data.model.Message
import com.google.android.gms.fido.fido2.api.common.Attachment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import java.security.Security
import kotlin.contracts.contract

/**
 * Pantalla principal de chat para el prestador
 */
@Composable
fun PrestadorChatScreen(
    onBack: () -> Unit = {},
    onInConversationChange: (Boolean) -> Unit = {},
    onNavigateToPresupuesto: () -> Unit = {},
) {
    // Estado: null = lista de chats, non-null = conversación activa
    var activeChatUserId by remember { mutableStateOf<String?>(null) }
    
    // Estado: mensajes de la conversación activa
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    
    // Estado: texto del input
    var inputText by remember { mutableStateOf("") }
    
    // Notificar cuando cambia el estado de conversación
    LaunchedEffect(activeChatUserId) {
        onInConversationChange(activeChatUserId != null)
        if (activeChatUserId != null) {
            messages = ChatData.getMessagesForUser(activeChatUserId!!).toMutableList()
        }
    }
    
    // Manejar botón atrás del sistema
    BackHandler {
        if (activeChatUserId != null) {
            activeChatUserId = null
            inputText = ""
        } else {
            onBack()
        }
    }
    
    // Animación con Crossfade (simple y confiable)
    Crossfade(
        targetState = activeChatUserId,
        animationSpec = tween(300),
        label = "chat_transition"
    ) { chatUserId ->
        if (chatUserId == null) {
            // VISTA: Lista de conversaciones
            ChatListView(
                onChatClick = { userId ->
                    activeChatUserId = userId
                },
                onBack = onBack
            )
        } else {
            // VISTA: Conversación activa
            val conversation = ChatData.getConversationById(chatUserId)
            
            if (conversation != null) {
                ChatConversationView(
                    conversation = conversation,
                    messages = messages,
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = { messageText ->
                        // Detectar si es un mensaje de cita (empieza con 📅)
                        val isAppointment = messageText.startsWith("📅")
                        
                        // Agregar nuevo mensaje de texto
                        val newMessage = Message(
                            id = System.currentTimeMillis(),
                            text = if (isAppointment) messageText.removePrefix("📅 Cita agendada para el ").trim() else messageText,
                            isFromMe = true,
                            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                            type = if (isAppointment) "appointment" else "text"
                        )
                        messages = messages + newMessage
                        inputText = ""
                    },
                    onSendImage = { imageUri ->
                        // Agregar nuevo mensaje con imagen
                        val newMessage = Message(
                            id = System.currentTimeMillis(),
                            text = "",
                            isFromMe = true,
                            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                            imageUri = imageUri.toString()
                        )
                        messages = messages + newMessage
                    },
                    onBack = {
                        activeChatUserId = null
                        inputText = ""
                    },
                    onNavigateToPresupuesto = onNavigateToPresupuesto
                )
            }
        }
    }
}

/**
 * Preview para Android Studio
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewPrestadorChat() {
    MaterialTheme {
        PrestadorChatScreen(onBack = {})
    }
}

/**
 * Vista de lista de conversaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListView(
    onChatClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val conversations = ChatData.conversations
    
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF6B35),
                                Color(0xFFFF9F66)
                            )
                        )
                    )
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Mensajes",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${conversations.size} conversaciones",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF8F3))
                .padding(paddingValues)
        ) {
            items(conversations) { conversation ->
                ChatListItem(
                    conversation = conversation,
                    onClick = { onChatClick(conversation.userId) }
                )
            }
        }
    }
}

/**
 * Item de conversación en la lista
 */
@Composable
fun ChatListItem(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(conversation.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.name.first().uppercase(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Indicador online
            if (conversation.isOnline) {
                Box(
                    modifier = Modifier
                        .offset(x = (-12).dp, y = 18.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Contenido
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = conversation.lastMessageTime,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = conversation.job,
                    fontSize = 13.sp,
                    color = Color(0xFFFF6B35),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Divider
        HorizontalDivider(
            modifier = Modifier.padding(start = 84.dp),
            color = Color(0xFFE5E7EB)
        )
    }
}

/**
 * Vista de conversación individual
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationView(
    conversation: ChatConversation,
    messages: List<Message>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSendImage: (Uri) -> Unit,
    onBack: () -> Unit,
    onNavigateToPresupuesto: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Repositorio para guardar citas
    val appointmentRepository = remember { com.example.myapplication.prestador.data.repository.AppointmentRepository() }



    //Estado para imagenes seleccionadas
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showImagePreview by remember { mutableStateOf(false) }

    //Launcher para seleccionar multiples imagenes con UI moderna
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImages = uris
            showImagePreview = true
        }
    }

    //Estado para ubicacion
    var isGettingLocation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    //Funcion para obtener ubicacion actual
    fun getLocationAndSend() {
        val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude

                    //crea el enlace de google maps
                    val googleMapsUrl = "https://maps.google.com/?q=$lat,$lng"
                    //crea un mensaje con la ubicacion
                    onSendMessage("📍 Mi ubicación\n$googleMapsUrl")
                    isGettingLocation = false
                }
            }
        } catch (e: SecurityException) {
            isGettingLocation = false
        }
    }
    
    //Launcher para pedir permiso de ubicacion
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION]?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            // Permiso concedido, obtener ubicación
            getLocationAndSend()
        }
    }
    
    // Estado del menú de adjuntos
    var showAttachMenu by remember { mutableStateOf(false) }
    
    // Estado del diálogo de agendar cita
    var showScheduleDialog by remember { mutableStateOf(false) }
    
    // Estado para guardar datos de cita temporalmente
    var pendingAppointmentDate by remember { mutableStateOf("") }
    var pendingAppointmentTime by remember { mutableStateOf("") }
    
    // Función para guardar cita en calendario
    fun saveToCalendar(date: String, time: String) {
        val dateParts = date.split("-")
        val formattedDate = "${dateParts[2]}/${dateParts[1]}/${dateParts[0]}"
        
        // Guardar en Firebase como PENDING (esperando confirmación del cliente)
        coroutineScope.launch {
            val appointment = com.example.myapplication.prestador.data.model.Appointment(
                clientId = conversation.userId,
                clientName = conversation.name,
                service = conversation.job,
                date = date,
                time = time,
                status = "pending",
                notes = "Propuesta de cita desde el prestador",
                proposedBy = "provider"
            )
            
            appointmentRepository.saveAppointment(appointment).fold(
                onSuccess = { appointmentId ->
                    // Éxito al guardar en Firebase
                    android.util.Log.d("ChatScreen", "Propuesta de cita guardada en Firebase: $appointmentId")
                    android.widget.Toast.makeText(
                        context,
                        "Propuesta de cita enviada. Esperando confirmación del cliente.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    
                    // Enviar mensaje al chat con el ID de la cita
                    val appointmentMessage = "📅 Cita agendada para el $formattedDate, $time"
                    onSendMessage(appointmentMessage)
                },
                onFailure = { error ->
                    android.util.Log.e("ChatScreen", "Error al guardar propuesta", error)
                    android.widget.Toast.makeText(
                        context,
                        "Error al enviar propuesta de cita",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
    
    // Launcher para pedir permisos de calendario
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[android.Manifest.permission.READ_CALENDAR] ?: false
        val writeGranted = permissions[android.Manifest.permission.WRITE_CALENDAR] ?: false
        
        if (readGranted && writeGranted) {
            // Permisos concedidos, guardar la cita
            saveToCalendar(pendingAppointmentDate, pendingAppointmentTime)
        } else {
            android.widget.Toast.makeText(
                context,
                "Se necesitan permisos de calendario para guardar la cita",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    // Auto-scroll al final cuando se agregan mensajes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF6B35),
                                Color(0xFFFF9F66)
                            )
                        )
                    )
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar pequeño
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(conversation.avatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conversation.name.first().uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = conversation.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (conversation.isOnline) "En línea" else "Desconectado",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChange = onInputChange,
                onSend = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText.trim())
                    }
                },
                showAttachMenu = showAttachMenu,
                onAttachMenuToggle = { showAttachMenu = !showAttachMenu },
                onImageSend = onSendImage
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF8F3))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            state = listState
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            items(messages) { message ->
                MessageBubble(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Menú flotante de adjuntos con animación - posicionado de forma absoluta
    AnimatedVisibility(
        visible = showAttachMenu,
        // ENTRADA: Crece desde la esquina inferior izquierda (0f, 1f)
        enter = scaleIn(
            animationSpec = tween(300),
            transformOrigin = TransformOrigin(0f, 1f) // 0f=Izquierda, 1f=Abajo
        ) + fadeIn(tween(200)),
        // SALIDA: Se encoge hacia la esquina
        exit = scaleOut(
            animationSpec = tween(200),
            transformOrigin = TransformOrigin(0f, 1f)
        ) + fadeOut(tween(200)),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 5.dp, start = 4.dp)
    ) {
        AttachmentOptionsMenu(
            onDismiss = { showAttachMenu = false },
            onBudgetClick = {
                showAttachMenu = false
                // abrir presupuesto
                onNavigateToPresupuesto()
            },
            onLocationClick = {
                showAttachMenu = false
                // Verificar si ya tiene permisos
                if (android.content.pm.PackageManager.PERMISSION_GRANTED == 
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )) {
                    // Ya tiene permiso, obtener ubicación directamente
                    getLocationAndSend()
                } else {
                    // Pedir permisos
                    locationPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            onScheduleClick = {
                showAttachMenu = false
                showScheduleDialog = true
            },
            onImageClick = {
                showAttachMenu = false
                galleryLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )
    }
    
    // Diálogo de preview de imágenes
    if (showImagePreview) {
        ImagePreviewDialog(
            images = selectedImages,
            onDismiss = { 
                showImagePreview = false
                selectedImages = emptyList()
            },
            onSend = { uris ->
                // Enviar todas las imágenes
                uris.forEach { uri ->
                    onSendImage(uri)
                }
                showImagePreview = false
                selectedImages = emptyList()
            }
        )
    }
    
    // Diálogo de agendar cita
    if (showScheduleDialog) {
        ScheduleAppointmentDialog(
            onDismiss = { showScheduleDialog = false },
            onConfirm = { date, time ->
                showScheduleDialog = false
                
                // Guardar datos temporalmente
                pendingAppointmentDate = date
                pendingAppointmentTime = time
                
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
                    // Ya tiene permisos, guardar directamente
                    saveToCalendar(date, time)
                } else {
                    // Pedir permisos
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
}

/**
 * Burbuja de mensaje (detecta el tipo y muestra el diseño apropiado)
 */
@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        // Si es un mensaje de cita, mostrar diseño especial
        if (message.type == "appointment") {
            TarjetaMensajeCita(
                fechaHora = message.text,
                esMio = message.isFromMe,
                status = message.appointmentStatus,
                onAccept = if (!message.isFromMe && message.appointmentStatus == "pending") {
                    {
                        // TODO: Implementar aceptación de cita
                    }
                } else null,
                onReject = if (!message.isFromMe && message.appointmentStatus == "pending") {
                    {
                        // TODO: Implementar rechazo de cita
                    }
                } else null
            )
        } else {
            // Mensaje normal (texto o imagen)
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                    bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                ),
                color = if (message.isFromMe) Color(0xFFFF6B35) else Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Mostrar imagen si existe
                    message.imageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Imagen enviada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        if (message.text.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    // Mostrar texto si no está vacío
                    if (message.text.isNotBlank()) {
                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        val urlPattern = Regex("https?://[^\\s]+")
                        val urls = urlPattern.findAll(message.text).map { it.value }.toList()
                        
                        if (urls.isNotEmpty()) {
                            // Texto con enlaces clicables
                            androidx.compose.foundation.text.ClickableText(
                                text = androidx.compose.ui.text.buildAnnotatedString {
                                    val text = message.text
                                    var lastIndex = 0
                                    
                                    urls.forEach { url ->
                                        val startIndex = text.indexOf(url, lastIndex)
                                        val endIndex = startIndex + url.length
                                        
                                        // Texto antes del enlace
                                        append(text.substring(lastIndex, startIndex))
                                        
                                        // Enlace con estilo
                                        pushStyle(
                                            androidx.compose.ui.text.SpanStyle(
                                                color = if (message.isFromMe) Color.White else Color(0xFF2563EB),
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                            )
                                        )
                                        pushStringAnnotation(tag = "URL", annotation = url)
                                        append(url)
                                        pop()
                                        
                                        lastIndex = endIndex
                                    }
                                    
                                    // Texto después del último enlace
                                    if (lastIndex < text.length) {
                                        append(text.substring(lastIndex))
                                    }
                                },
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    color = if (message.isFromMe) Color.White else Color(0xFF1F2937),
                                    lineHeight = 20.sp
                                ),
                                onClick = { offset ->
                                    urls.forEach { url ->
                                        val startIndex = message.text.indexOf(url)
                                        val endIndex = startIndex + url.length
                                        
                                        if (offset in startIndex until endIndex) {
                                            uriHandler.openUri(url)
                                        }
                                    }
                                }
                            )
                        } else {
                            // Texto normal sin enlaces
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                color = if (message.isFromMe) Color.White else Color(0xFF1F2937),
                                lineHeight = 20.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = message.time,
                        fontSize = 11.sp,
                        color = if (message.isFromMe) Color.White.copy(alpha = 0.8f) else Color(0xFF9CA3AF),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta especial para mensajes de cita agendada
 */
@Composable
fun TarjetaMensajeCita(
    fechaHora: String, // Ej: "24/01/2026, 09:00"
    esMio: Boolean = true,
    status: String = "pending", // "pending", "confirmed", "cancelled"
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    val colorFondo = if (esMio) Color(0xFFEA580C) else Color.White
    val colorTexto = if (esMio) Color.White else Color(0xFF334155)
    
    Column(
        modifier = Modifier
            .widthIn(min = 220.dp, max = 280.dp)
            .background(
                color = colorFondo,
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
                painter = painterResource(com.example.myapplication.prestador.R.drawable.ic_calendar),
                contentDescription = null,
                tint = colorTexto.copy(alpha = 0.9f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (status) {
                    "pending" -> if (esMio) "PROPUESTA ENVIADA" else "PROPUESTA DE CITA"
                    "confirmed" -> "CITA CONFIRMADA"
                    "cancelled" -> "CITA CANCELADA"
                    else -> "CITA AGENDADA"
                },
                color = colorTexto.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // 2. FECHA GRANDE
        Text(
            text = fechaHora,
            color = colorTexto,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 3. SUBTEXTO
        Text(
            text = when (status) {
                "pending" -> if (esMio) "Esperando confirmación del cliente" else "¿Confirmas esta fecha y hora?"
                "confirmed" -> "Visita técnica confirmada"
                "cancelled" -> "Esta cita fue cancelada"
                else -> "Visita técnica"
            },
            color = colorTexto.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 4. FOOTER
        when {
            // Si no es mío, está pendiente y tengo callbacks -> Mostrar botones Aceptar/Rechazar
            !esMio && status == "pending" && onAccept != null && onReject != null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Rechazar
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF334155)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Rechazar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    // Botón Aceptar
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Aceptar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // Si está confirmada -> Mostrar "Guardada en Agenda"
            status == "confirmed" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (esMio) Color.White.copy(alpha = 0.2f) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colorTexto,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Guardada en Agenda",
                            color = colorTexto,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            // Si es mío y está pendiente -> Mostrar estado
            esMio && status == "pending" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏳ Esperando respuesta",
                        color = colorTexto,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Barra de input para enviar mensajes
 */
@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    showAttachMenu: Boolean,
    onAttachMenuToggle: () -> Unit,
    onImageSend: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Función para crear URI temporal
    fun createImageUri(): Uri {
        val image = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "com.example.myapplication.prestador.fileprovider",
            image
        )
    }

    //Launcher para tomar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // Enviar la imagen
            onImageSend(photoUri!!)
        }
    }

    // Launcher para pedir permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, abrir cámara
            photoUri = createImageUri()
            cameraLauncher.launch(photoUri!!)
        }
    }



    Surface(
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {

            //Boton clip
            val clipInteractionSource = remember { MutableInteractionSource() }
            val isClipPressed by clipInteractionSource.collectIsPressedAsState()

            IconButton(
                onClick = onAttachMenuToggle,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isClipPressed)
                            Color(0xFFFF6B35) else Color(0xFFF3F4F6)
                    ),
                interactionSource = clipInteractionSource
            ) {
                Icon(
                    painter = painterResource(id = com.example.myapplication.prestador.R.drawable.ic_attach),
                    contentDescription = "Adjuntar archivo",
                    tint = if (isClipPressed)
                    Color.White else Color(0xFFFF6B35),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = "Escribe un mensaje...",
                        color = Color(0xFF9CA3AF)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            // Botón cámara con efecto de presionado
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            IconButton(
                onClick = {
                    // Pedir permiso de cámara
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPressed) Color(0xFFFF6B35) else Color(0xFFF3F4F6)
                    ),
                interactionSource = interactionSource
            ) {
                Icon(
                    painter = painterResource(id = com.example.myapplication.prestador.R.drawable.ic_camera),
                    contentDescription = "Agregar foto",
                    tint = if (isPressed) Color.White else Color(0xFFFF6B35),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            
            // Botón enviar
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank()) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF6B35),
                                    Color(0xFFFF9F66)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFD1D5DB),
                                    Color(0xFFD1D5DB)
                                )
                            )
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


/**
 * Menú de opciones para adjuntar archivos
 */
@Composable
fun AttachmentOptionsMenu(
    onDismiss: () -> Unit,
    onBudgetClick: () -> Unit,
    onLocationClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onImageClick: () -> Unit
) {
    // Menú de burbujas simple
    Column(
        modifier = Modifier.padding(start = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Imágenes
        AttachmentBubble(
            icon = com.example.myapplication.prestador.R.drawable.ic_image,
            label = "Imágenes",
            color = Color(0xFF8B5CF6),
            onClick = onImageClick
        )
        
        // Agendar cita
        AttachmentBubble(
            icon = com.example.myapplication.prestador.R.drawable.ic_calendar,
            label = "Agendar cita",
            color = Color(0xFF3B82F6),
            onClick = onScheduleClick
        )
        
        // Ubicación
        AttachmentBubble(
            icon = com.example.myapplication.prestador.R.drawable.ic_location,
            label = "Ubicación",
            color = Color(0xFF10B981),
            onClick = onLocationClick
        )
        
        // Presupuesto
        AttachmentBubble(
            icon = com.example.myapplication.prestador.R.drawable.ic_budget,
            label = "Presupuesto",
            color = Color(0xFFF59E0B),
            onClick = onBudgetClick
        )
    }
}





/**
 * Burbuja individual de opción
 */
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

/**
 * Diálogo de preview de imágenes seleccionadas
 */
@Composable
fun ImagePreviewDialog(
    images: List<Uri>,
    onDismiss: () -> Unit,
    onSend: (List<Uri>) -> Unit
) {
        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B35),
                                        Color(0xFFFF9F66)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Imágenes seleccionadas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${images.size} imagen${if (images.size != 1) "es" else ""}",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    
                    // Grid de imágenes
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(images) { uri ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 4.dp
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Imagen seleccionada",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    
                    // Botones
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Cancelar
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF6B35)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color(0xFFFF6B35)
                            )
                        ) {
                            Text(
                                text = "Cancelar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Botón Enviar
                        Button(
                            onClick = { onSend(images) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6B35)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enviar ${images.size}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }


/**
 * Diálogo compacto para agendar una cita desde el chat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // Fecha por defecto: Mañana
    var selectedDate by remember {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        mutableStateOf("$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}")
    }
    
    // Hora por defecto: 09:00
    var selectedTime by remember { mutableStateOf("09:00") }
    
    // DatePicker nativo de Android
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedMonth = (month + 1).toString().padStart(2, '0')
            val formattedDay = dayOfMonth.toString().padStart(2, '0')
            selectedDate = "$year-$formattedMonth-$formattedDay"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    // TimePicker nativo de Android
    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedHour = hourOfDay.toString().padStart(2, '0')
            val formattedMinute = minute.toString().padStart(2, '0')
            selectedTime = "$formattedHour:$formattedMinute"
        },
        9, 0, true // 24 horas
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nueva Cita",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo Fecha
                Text(
                    text = "FECHA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(com.example.myapplication.prestador.R.drawable.ic_calendar),
                            contentDescription = null,
                            tint = Color(0xFFFF6B35)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color(0xFFE2E8F0),
                        disabledContainerColor = Color(0xFFF1F5F9)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo Hora
                Text(
                    text = "HORA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialog.show() },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(com.example.myapplication.prestador.R.drawable.ic_calendar),
                            contentDescription = null,
                            tint = Color(0xFFFF6B35)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color(0xFFE2E8F0),
                        disabledContainerColor = Color(0xFFF1F5F9)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón Confirmar
                Button(
                    onClick = {
                        onConfirm(selectedDate, selectedTime)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Confirmar y Agendar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
