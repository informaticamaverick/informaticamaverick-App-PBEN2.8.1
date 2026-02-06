package com.example.myapplication.presentation.client

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ripple
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.data.model.ChatData
import com.example.myapplication.data.model.Message
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.content.FileProvider
import java.io.File
import android.media.MediaRecorder
import android.media.MediaPlayer
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import android.os.Build
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.components.PrestadorCard
//import com.example.myapplication.presentation.components.PrestadorCard
import com.example.myapplication.presentation.components.geminiGradientEffect
import kotlin.collections.distinct

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    initialProviderId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {},
    providerViewModel: ProviderViewModel = hiltViewModel()
) {
    // Colores adaptativos
    val appColors = getAppColors()

    // Recuperar prestadores desde el ViewModel (Room -> Repository -> ViewModel)
    val providers by providerViewModel.providers.collectAsStateWithLifecycle()

    // Estado para la conversación activa
    var activeChatUserId by remember { mutableStateOf(initialProviderId) }

    // Notificar cuando cambia el estado de conversación
    LaunchedEffect(activeChatUserId) {
        onInConversationChange(activeChatUserId != null)
    }

    // Manejar botón atrás del sistema
    BackHandler {
        if (activeChatUserId != null) {
            // si hay conversacion activa, cerrarla (volver a lista de chats)
            activeChatUserId = null
        } else {
            // si esta en la lista de chats, salir de chatscreen
            onBack()
        }
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
        val photoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }

    // Launcher para tomar foto
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
        if (isGranted) cameraLauncher.launch(tempPhotoUri)
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
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                try {
                    prepare()
                    start()
                    isRecordingAudio = true
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    // Función para detener grabación
    fun stopRecordingAndSend() {
        try {
            mediaRecorder?.apply { stop(); release() }
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
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder = null
            audioFilePath?.let { path -> File(path).delete() }
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
            providersList = providers,
            onChatClick = { userId -> activeChatUserId = userId },
            onBack = onBack,
            appColors = appColors,
            navController = navController
        )
    } else {
        val provider = providers.find { it.id == activeChatUserId }
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
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(tempPhotoUri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onGalleryClick = { imagePickerLauncher.launch("image/*") },
                onAudioClick = {
                    if (isRecordingAudio) stopRecordingAndSend()
                    else {
                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            val audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
                            audioFilePath = audioFile.absolutePath
                            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
                            mediaRecorder?.apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                setOutputFile(audioFilePath)
                                try { prepare(); start(); isRecordingAudio = true } catch (e: Exception) { e.printStackTrace() }
                            }
                        } else audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onCancelAudio = { cancelRecording() },
                isRecordingAudio = isRecordingAudio
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListView(
    providersList: List<Provider>,
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors,
    navController: NavHostController? = null
) {
    val colors = MaterialTheme.colorScheme
    var providerToDelete by remember { mutableStateOf<Provider?>(null) }
    val categories = remember(providersList) { providersList.map { it.category }.distinct() }
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var menuState by remember { mutableIntStateOf(0) }
    val showSettingsMenu = menuState == 0
    val showVerticalMenu = menuState == 1
    var onlyUnread by remember { mutableStateOf(false) }
    var sortByAlphabetical by remember { mutableStateOf(false) }

    // Diálogos y preferencias
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showDataVisibilityDialog by remember { mutableStateOf(false) }
    var showTimePeriodDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Preferencias de usuario
    var viewMode by remember { mutableStateOf("Detallada") } // "Compacta", "Detallada", "Tarjetas"
    var timePeriod by remember { mutableStateOf("Todo") } // "Semana", "Mes", "3 Meses", "Todo"
    var showDates by remember { mutableStateOf(true) }
    var showAvatars by remember { mutableStateOf(true) }
    var showPreviews by remember { mutableStateOf(true) }
    var showBadges by remember { mutableStateOf(true) }
    var notifyNewMessages by remember { mutableStateOf(true) }
    var notifyMentions by remember { mutableStateOf(true) }
    var notifyReactions by remember { mutableStateOf(true) }

    // Preferencias de privacidad
    var showReadReceipts by remember { mutableStateOf(true) }
    var showLastSeen by remember { mutableStateOf(true) }
    var showProfilePhoto by remember { mutableStateOf(true) }
    var showProfileInfo by remember { mutableStateOf(true) }

    // [CORREGIDO] Filtramos los prestadores basándonos en la propiedad `category`.
    val filteredProviders =
        remember(selectedCategories, providersList, searchQuery, onlyUnread, sortByAlphabetical) {
            var result = providersList
            if (selectedCategories.isNotEmpty()) result =
                result.filter { it.category in selectedCategories }
            if (searchQuery.isNotEmpty()) {
                result = result.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.category.contains(searchQuery, ignoreCase = true) // Forma correcta
                }
            }

            // Filtro por no leídos (simulado)
            if (onlyUnread) {
                result = result.filter {
                    // Simulación: 50% de los chats tienen mensajes sin leer
                    it.id.hashCode() % 2 == 0
                }
            }

            // Ordenar
            if (sortByAlphabetical) {
                result = result.sortedBy { it.name }
            } else {
                // Por defecto ordenar por más reciente (simulado usando ID)
                result = result.sortedByDescending { it.id }
            }
            if (onlyUnread) result = result.filter { it.id.hashCode() % 2 == 0 }
            if (sortByAlphabetical) result = result.sortedBy { it.name }
            else result = result.sortedByDescending { it.id }
            result
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mensajes") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Volver"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.surfaceColor)
                )
            },
            floatingActionButton = {
                val rainbowBrush = geminiGradientEffect()
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 70.dp)
                ) {
                    // Menú vertical de configuración (aparece arriba del engranaje)
                    AnimatedVisibility(
                        visible = showVerticalMenu && !isSearchActive,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Opción: Alertas
                            Surface(
                                modifier = Modifier.size(64.dp),
                                onClick = {
                                    showNotificationsDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = appColors.surfaceColor,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        "Alertas",
                                        tint = appColors.textPrimaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Alertas",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.textPrimaryColor,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Opción: Mostrar Datos
                            Surface(
                                modifier = Modifier.size(64.dp),
                                onClick = {
                                    showDataVisibilityDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = appColors.surfaceColor,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Visibility,
                                        "Mostrar Datos",
                                        tint = appColors.textPrimaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Datos",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.textPrimaryColor,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Opción: Período
                            Surface(
                                modifier = Modifier.size(64.dp),
                                onClick = {
                                    showTimePeriodDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = appColors.surfaceColor,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        "Período",
                                        tint = appColors.textPrimaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Período",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.textPrimaryColor,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Opción: Privacidad
                            Surface(
                                modifier = Modifier.size(64.dp),
                                onClick = {
                                    showPrivacyDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = appColors.surfaceColor,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        "Privacidad",
                                        tint = appColors.textPrimaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Privacidad",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.textPrimaryColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Botones de búsqueda y engranaje
                    AnimatedVisibility(
                        visible = !isSearchActive,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        val gearRotation by animateFloatAsState(
                            targetValue = if (menuState == 2) 0f else 45f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Botones de filtros expandibles (aparecen a la izquierda)
                            AnimatedVisibility(
                                visible = showSettingsMenu && !isSearchActive,
                                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(start = 32.dp, end = 8.dp)
                                ) {
                                    // Botón: No Leídos
                                    Surface(
                                        modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                        onClick = {
                                            onlyUnread = !onlyUnread
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (onlyUnread) appColors.accentBlue else appColors.surfaceColor,
                                        shadowElevation = 6.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize()
                                                .padding(vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.MarkEmailUnread,
                                                "No Leídos",
                                                tint = if (onlyUnread) Color.White else appColors.textPrimaryColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (onlyUnread) "Activo" else "No Leídos",
                                                color = if (onlyUnread) Color.White else appColors.textPrimaryColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Botón: Ordenar
                                    Surface(
                                        modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                        onClick = {
                                            sortByAlphabetical = !sortByAlphabetical
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (sortByAlphabetical) appColors.accentBlue else appColors.surfaceColor,
                                        shadowElevation = 6.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize()
                                                .padding(vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                if (sortByAlphabetical) Icons.Default.SortByAlpha else Icons.Default.Schedule,
                                                "Ordenar",
                                                tint = if (sortByAlphabetical) Color.White else appColors.textPrimaryColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (sortByAlphabetical) "A-Z" else "Reciente",
                                                color = if (sortByAlphabetical) Color.White else appColors.textPrimaryColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Botón Dividido (Buscar + Engranaje)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Parte Izquierda: Buscar
                                Surface(
                                    onClick = { isSearchActive = true },
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(
                                        topStart = 28.dp,
                                        bottomStart = 28.dp,
                                        topEnd = 10.dp,
                                        bottomEnd = 10.dp
                                    ),
                                    color = appColors.surfaceColor,
                                    border = BorderStroke(2.5.dp, rainbowBrush),
                                    shadowElevation = 12.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Search,
                                            null,
                                            tint = appColors.textPrimaryColor,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }

                                // Parte Derecha: Ajustes / Cerrar
                                Surface(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .combinedClickable(
                                            onClick = {
                                                menuState = (menuState + 1) % 3
                                            },
                                            onLongClick = { }
                                        ),
                                    shape = RoundedCornerShape(
                                        topStart = 10.dp,
                                        bottomStart = 10.dp,
                                        topEnd = 28.dp,
                                        bottomEnd = 28.dp
                                    ),
                                    color = appColors.surfaceColor,
                                    border = BorderStroke(2.5.dp, rainbowBrush),
                                    shadowElevation = 12.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Settings,
                                            "Ajustes",
                                            tint = appColors.textPrimaryColor,
                                            modifier = Modifier.size(26.dp).rotate(gearRotation)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { paddingValues ->

            // Diálogo: Alertas de Notificaciones
            if (showNotificationsDialog) {
                AlertDialog(
                    onDismissRequest = { showNotificationsDialog = false },
                    title = { Text("Alertas", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Configurar notificaciones:", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Notificar mensajes nuevos")
                                Switch(
                                    checked = notifyNewMessages,
                                    onCheckedChange = { notifyNewMessages = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Notificar menciones")
                                Switch(
                                    checked = notifyMentions,
                                    onCheckedChange = { notifyMentions = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Notificar reacciones")
                                Switch(
                                    checked = notifyReactions,
                                    onCheckedChange = { notifyReactions = it }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showNotificationsDialog = false
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNotificationsDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo: Mostrar Datos
            if (showDataVisibilityDialog) {
                AlertDialog(
                    onDismissRequest = { showDataVisibilityDialog = false },
                    title = { Text("Mostrar Datos", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Configurar visibilidad de datos:", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Mostrar fechas")
                                Switch(
                                    checked = showDates,
                                    onCheckedChange = { showDates = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Mostrar avatares")
                                Switch(
                                    checked = showAvatars,
                                    onCheckedChange = { showAvatars = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Mostrar vista previa")
                                Switch(
                                    checked = showPreviews,
                                    onCheckedChange = { showPreviews = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Mostrar insignias")
                                Switch(
                                    checked = showBadges,
                                    onCheckedChange = { showBadges = it }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showDataVisibilityDialog = false
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDataVisibilityDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo: Período de Tiempo
            if (showTimePeriodDialog) {
                AlertDialog(
                    onDismissRequest = { showTimePeriodDialog = false },
                    title = { Text("Período de Tiempo", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Filtrar mensajes por período:", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(12.dp))

                            val periods = listOf("Semana", "Mes", "3 Meses", "Todo")
                            periods.forEach { period ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { timePeriod = period }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(period)
                                    RadioButton(
                                        selected = timePeriod == period,
                                        onClick = { timePeriod = period }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showTimePeriodDialog = false
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePeriodDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo: Privacidad
            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("Privacidad", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(
                                "Configurar opciones de privacidad:",
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Confirmación de lectura", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Mostrar cuando leíste mensajes",
                                        fontSize = 12.sp,
                                        color = appColors.textSecondaryColor
                                    )
                                }
                                Switch(
                                    checked = showReadReceipts,
                                    onCheckedChange = { showReadReceipts = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Última conexión", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Mostrar tu última conexión",
                                        fontSize = 12.sp,
                                        color = appColors.textSecondaryColor
                                    )
                                }
                                Switch(
                                    checked = showLastSeen,
                                    onCheckedChange = { showLastSeen = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Foto de perfil", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Quién puede ver tu foto",
                                        fontSize = 12.sp,
                                        color = appColors.textSecondaryColor
                                    )
                                }
                                Switch(
                                    checked = showProfilePhoto,
                                    onCheckedChange = { showProfilePhoto = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Información de perfil", fontWeight = FontWeight.Medium)
                                    Text(
                                        "Mostrar datos de contacto",
                                        fontSize = 12.sp,
                                        color = appColors.textSecondaryColor
                                    )
                                }
                                Switch(
                                    checked = showProfileInfo,
                                    onCheckedChange = { showProfileInfo = it }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showPrivacyDialog = false
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appColors.backgroundColor)
                    .padding(paddingValues)
            ) {

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
                                    @Composable {
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
                                navController?.navigate("perfil_prestador/${provider.id}")
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
                            },
                            showAvatars = showAvatars,
                            showPreviews = showPreviews,
                            showBadges = showBadges
                        )
                    }
                }
            }
        } // Cierre del Column + Scaffold (paddingValues)

// Diálogo de confirmación para eliminar
        if (providerToDelete != null) {
            AlertDialog(
                onDismissRequest = { providerToDelete = null },
                title = { Text("Eliminar Conversación") },
                text = {
                    Text("¿Estás seguro de que deseas eliminar el chat con ${providerToDelete?.name}? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    // Aquí estaba el error: El Button debe envolver a los parámetros
                    Button(
                        onClick = {
                            // Tu lógica de eliminación
                            //providersList = providersList.filter { it.id != providerToDelete?.id }
                            //providerToDelete = null
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



/**
        // Diálogo de confirmación para eliminar
        if (providerToDelete != null) {
            AlertDialog(
                onDismissRequest = { providerToDelete = null },
                // title = { Text("Eliminar Conversación") },
                //text = { Text("¿Estás seguro de que deseas eliminar el chat con ${providerToDelete!!.name}? Esta acción no se puede deshacer.") },
                confirmButton = {
                    //Button(
                    //onClick = {
                    //  providersList = providersList.filter { it.id != providerToDelete!!.id }
                    // providerToDelete = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                // Text("Eliminar", color = Color.White)
            }
        }
        // dismissButton = {
        //   TextButton(onClick = { providerToDelete = null }) {
        //     Text("Cancelar")
        // }
        //  }
        // )
**/




    }



    // Barra de búsqueda flotante (igual que en PresupuestosScreen)
    if (isSearchActive) {
        val rainbowBrush =
            geminiGradientEffect()
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp)
                .zIndex(10f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                    shadowElevation = 12.dp,
                    border = BorderStroke(2.5.dp, rainbowBrush)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = colors.onSurface.copy(0.8f),
                            modifier = Modifier.padding(start = 24.dp).size(20.dp)
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(color = colors.onSurface, fontSize = 17.sp),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Buscar conversaciones...", color = colors.onSurfaceVariant, fontSize = 16.sp)
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.size(56.dp),
                onClick = {
                    isSearchActive = false
                    searchQuery = ""
                    keyboardController?.hide()
                },
                shape = CircleShape,
                color = colors.surface,
                border = BorderStroke(2.5.dp, rainbowBrush),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, "Cerrar", tint = colors.onSurface, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
    } // Cierre del Box externo
//}

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
    provider: Provider,
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
    provider: Provider,
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
                    text = "${provider.name} ",
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

// Función para formatear la duración del audio
fun formatAudioDuration(millis: Int): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

// Componente para mostrar mensajes de audio
@Composable
fun AudioMessageBubble(
    message: Message,
    appColors: com.example.myapplication.ui.theme.AppColors,
    isFromMe: Boolean
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }

    // Actualizar posición mientras reproduce
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let {
                currentPosition = it.currentPosition
            }
            delay(100)
        }
    }

    // Limpiar MediaPlayer cuando se destruye el composable
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            // Pausar
            mediaPlayer?.pause()
            isPlaying = false
        } else {
            // Reproducir
            if (mediaPlayer == null) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(message.imageUri)
                        prepare()
                        duration = this.duration
                        setOnCompletionListener {
                            isPlaying = false
                            currentPosition = 0
                        }
                        start()
                    }
                    isPlaying = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                mediaPlayer?.start()
                isPlaying = true
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(min = 200.dp, max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Play/Pause
                IconButton(
                    onClick = { togglePlayPause() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = if (isFromMe) Color.White else appColors.accentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Barra de progreso y duración
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.Bottom)
                ) {
                    // Barra de progreso
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isFromMe) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(
                                    if (duration > 0) currentPosition.toFloat() / duration else 0f
                                )
                                .background(if (isFromMe) Color.White else appColors.accentBlue)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tiempo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatAudioDuration(currentPosition),
                            fontSize = 11.sp,
                            color = if (isFromMe) Color.White.copy(alpha = 0.8f) else appColors.textSecondaryColor
                        )
                        Text(
                            text = formatAudioDuration(duration),
                            fontSize = 11.sp,
                            color = if (isFromMe) Color.White.copy(alpha = 0.8f) else appColors.textSecondaryColor
                        )
                    }
                }

                // Icono de micrófono
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Audio",
                    tint = if (isFromMe) Color.White.copy(alpha = 0.7f) else appColors.textSecondaryColor,
                    modifier = Modifier
                        .size(20.dp)
                        .alpha(0.7f)
                )
            }
        }
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
        //Detectar si es audio o imagen
        val isAudio = message.text == "[Audio]" && message.imageUri.endsWith(".m4a")

        if (isAudio) {
            // Mensaje de audio
            AudioMessageBubble(
                message = message,
                appColors = appColors,
                isFromMe = isFromMe
            )
        } else {
            //Mensaje con imagen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isFromMe) Arrangement.End else
                Arrangement.Start
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
                        //Mostrar la imagen
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

                        //Texto si hay
                        if (message.text.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = if (isFromMe) Color.White else
                                appColors.textPrimaryColor
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
        }
    } else {
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
                Box(
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
                                            isDraggingToCancel = dragOffsetX < -120f
                                        }

                                        // Detectar cuando se suelta el dedo
                                        if (change.changedToUp()) {
                                            if (dragOffsetX < -180f) {
                                                // Activar animación de cancelación
                                                isCancelling = true

                                                coroutineScope.launch {
                                                    // Animación de papelera tragándose el micrófono
                                                    delay(300)
                                                    trashLidClosed = true
                                                    delay(200)
                                                    animationCompleted = true
                                                    onCancelAudio?.invoke()
                                                    delay(300)

                                                    // Resetear estados
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
                        }
                ) {
                    // PAPELERA A LA IZQUIERDA (animada)
                    val trashScale by animateFloatAsState(
                        targetValue = when {
                            isCancelling -> 0f
                            dragOffsetX < -180f -> 1.3f
                            dragOffsetX < -120f -> 1.1f
                            else -> 0.8f
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "trashScale"
                    )

                    val trashColor by animateColorAsState(
                        targetValue = if (dragOffsetX < -180f) Color.Red else Color(0xFF757575),
                        label = "trashColor"
                    )

                    val trashAlpha by animateFloatAsState(
                        targetValue = if (dragOffsetX < -50f) 1f else 0.3f,
                        label = "trashAlpha"
                    )

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Cancelar",
                        tint = trashColor,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(32.dp)
                            .graphicsLayer {
                                scaleX = trashScale
                                scaleY = trashScale
                                alpha = trashAlpha
                            }
                    )

                    // CONTENIDO CENTRAL (deslizable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .graphicsLayer {
                                translationX = dragOffsetX
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Espaciador para empujar el contenido a la derecha
                        Spacer(modifier = Modifier.weight(1f))

                        // CENTRO: Indicador desliza para cancelar
                        val textAlpha by animateFloatAsState(
                            targetValue = if (isDraggingToCancel || isCancelling) 0.2f else 1f,
                            label = "textAlpha"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.alpha(textAlpha)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = appColors.textSecondaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Desliza para cancelar",
                                color = appColors.textSecondaryColor,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // DERECHA: Tiempo y micrófono pulsante
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            // Tiempo de grabación
                            Text(
                                text = String.format("%d:%02d", recordingTime / 60, recordingTime % 60),
                                color = appColors.textPrimaryColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // Micrófono pulsante
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseScale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                                    .background(Color.Red, shape = CircleShape),
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
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MyApplicationTheme {
        ChatScreen(onBack = {})
    }
}
/**
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
**/
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

@Composable
fun ActionContent(
    inDeleteMode: Boolean,
    onMessageClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!inDeleteMode) {
            IconButton(onClick = onMessageClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Enviar mensaje",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        IconButton(onClick = onDeleteRequest) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
