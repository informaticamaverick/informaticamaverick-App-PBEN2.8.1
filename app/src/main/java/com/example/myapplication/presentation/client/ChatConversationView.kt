package com.example.myapplication.presentation.client

import android.Manifest
import android.R
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.presentation.components.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationView(
    provider: Provider,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    appColors: AppColors
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showTenderSelectionDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-detener grabación después de 60 segundos
    var recordingSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
                if (recordingSeconds >= 60) {
                    viewModel.stopRecordingAndSend()
                    break
                }
            }
        } else {
            recordingSeconds = 0
        }
    }
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) {
        permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val fusedLocation = com.google.android.gms.location.LocationServices
                .getFusedLocationProviderClient(context)
            try {
                fusedLocation.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.sendLocation(location.latitude, location.longitude)
                    }
                }
            } catch (e: SecurityException) {}
        }
    }

    // --- AUDIO ---
    val audioPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startRecording(context)
    }
    fun launchAudio() {
        val perm = android.Manifest.permission.RECORD_AUDIO
        if (context.checkSelfPermission(perm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.startRecording(context)
        } else {
            audioPermissionLauncher.launch(perm)
        }
    }

    // --- CAMARA Y GALERIA ---
    var cameraImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.sendImage(it); showAttachMenu = false }
    }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) cameraImageUri?.let { viewModel.sendImage(it) }
    }
    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            try {
                val file = java.io.File.createTempFile("img_", ".jpg", context.cacheDir)
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) { }
        }
    }
    fun launchCamera() {
        val perm = android.Manifest.permission.CAMERA
        if (context.checkSelfPermission(perm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                val file = java.io.File.createTempFile("img_", ".jpg", context.cacheDir)
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) { }
        } else {
            cameraPermissionLauncher.launch(perm)
        }
    }

    // 🔥 CORRECCIÓN: Como getMatchingTenders probablemente esperaba un String en el ViewModel,
    // le pasamos la primera categoría principal del prestador, o un texto vacío si no tiene.
    val mainCategory = provider.categories.firstOrNull() ?: ""
    val matchingTenders by viewModel.getMatchingTenders(mainCategory)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // --- VISOR DE PRESUPUESTO A4 ---
    if (selectedBudget != null) {
        Dialog(
            onDismissRequest = { viewModel.clearSelectedBudget() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BudgetMultiPageScreen(
                budget = selectedBudget!!,
                onBack = { viewModel.clearSelectedBudget() },
                // Aquí puedes pasar las lambdas onAccept/onReject si es necesario
            )
        }
    }

    if (showScheduleDialog) {
        com.example.myapplication.ui.screens.client.chat.ScheduleAppointmentDialog(
            onDismiss = { showScheduleDialog = false },
            onConfirm = { date, time, notes -> showScheduleDialog = false
            viewModel.sendAppointment(date, time, notes)
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime),
        topBar = {
            ChatHeader(
                providerName = provider.displayName,
                providerPhoto = provider.photoUrl,
                isOnline = provider.isOnline,
                onBack = onBack,
                appColors = appColors
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(appColors.backgroundColor)) {
                AnimatedVisibility(visible = showAttachMenu) {
                    AttachmentOptionsMenu(
                        onDismiss = { showAttachMenu = false },
                        onImageClick = { galleryLauncher.launch("image/*"); showAttachMenu = false },
                        onLocationClick = {
                            showAttachMenu = false
                            val fusedLocation = com.google.android.gms.location.LocationServices
                                .getFusedLocationProviderClient(context)
                            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                                context, android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                                context, android.Manifest.permission.ACCESS_COARSE_LOCATION

                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (hasFine || hasCoarse) {
                                try {
                                    fusedLocation.lastLocation.addOnSuccessListener { location ->
                                        if (location != null) {
                                            viewModel.sendLocation(location.latitude, location.longitude)
                                        } else {
                                            android.widget.Toast.makeText(
                                                context, "No se pudo obtener la ubicación",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: SecurityException) {
                                    android.widget.Toast.makeText(context, "Error al obtener ubicación", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else{
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },

                        onScheduleClick = { showScheduleDialog = true; showAttachMenu = false},
                        onInviteClick = { showTenderSelectionDialog = true }
                    )
                }
                MessageInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = { viewModel.sendText(it); inputText = "" },
                    appColors = appColors,
                    onAttachMenuToggle = { showAttachMenu = !showAttachMenu },
                    onCameraClick = { launchCamera() },
                    onAudioClick = {
                        if (isRecording) viewModel.stopRecordingAndSend()
                        else launchAudio()
                    },
                    onCancelAudio = { viewModel.cancelRecording() },
                    isRecordingAudio = isRecording
                )
            }
        },
        containerColor = appColors.backgroundColor
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(messages) { index, message ->
                if (index == 0 || !isSameDay(message.timestamp, messages[index - 1].timestamp)) {
                    DateSeparator(timestamp = message.timestamp, appColors = appColors)
                }

                when (message.type) {
                    MessageType.BUDGET -> BudgetBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors,
                        onClick = { message.relatedId?.let { viewModel.onBudgetClicked(it) } }
                    )

                    MessageType.IMAGE -> {
                        val isFromMe = message.senderId == viewModel.currentUserId
                        val imageBitmap = remember(message.content) {
                            try {
                                val bytes = android.util.Base64.decode(message.content, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } catch (e: Exception) { null }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                modifier = Modifier.widthIn(max = 280.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isFromMe) MaterialTheme.colorScheme.primary else appColors.surfaceColor
                            ) {
                                if (imageBitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = imageBitmap,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { zoomedImageUrl = message.content },
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    coil.compose.AsyncImage(
                                        model = message.content,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { zoomedImageUrl = message.content },
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                    MessageType.LOCATION -> {
                        if (message.latitude != null && message.longitude != null) {
                            val isFromMe = message.senderId == viewModel.currentUserId
                            val mapsUrl = "https://www.google.com/maps?q=${message.latitude},${message.longitude}"
                            val staticMapUrl = "https://maps.wikimedia.org/img/osm-intl,15,${message.latitude},${message.longitude},300x150.png"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .widthIn(max = 280.dp)
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                                            context.startActivity(intent)},
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isFromMe) MaterialTheme.colorScheme.primary else appColors.surfaceColor,
                                            shadowElevation = 2.dp



                                ) {
                                    Column {
                                        coil.compose.SubcomposeAsyncImage(
                                            model = staticMapUrl,
                                            contentDescription = "Mapa",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                            contentScale = ContentScale.Crop,
                                            error = {
                                                //Fallback si Wikimedia falla
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                                colors = listOf(Color(0xFF81C784), Color(0xFF388E3C))
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(Icons.Default.LocationOn,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(40.dp)
                                                        )
                                                        Text(
                                                            text = "%.4f, %.4f".format(message.latitude, message.longitude),
                                                            fontSize = 10.sp,
                                                            color = Color.White.copy(alpha = 0.85f)
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp,
                                                vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = if (isFromMe) Color.White else appColors.accentBlue,
                                                modifier = Modifier.size(18.dp)
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Ubicacion compartida",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isFromMe) Color.White else appColors.textPrimaryColor
                                                )

                                                Text(
                                                    text = "Toca para abrir en Maps",

                                                    fontSize = 11.sp,
                                                    color = if (isFromMe)
                                                    Color.White.copy(0.7f) else
                                                    appColors.textSecondaryColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MessageType.VISIT -> {
                        val isFromMe = message.senderId == viewModel.currentUserId
                        val parts = message.content.removePrefix("Solicitud de cita|").split("|")
                        val date = parts.getOrNull(0) ?: ""
                        val time = parts.getOrNull(1) ?: ""
                        val notes = parts.getOrNull(2) ?: ""
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = appColors.surfaceColor,
                                shadowElevation = 4.dp,
                                modifier = Modifier.widthIn(min = 240.dp, max = 300.dp)
                            ) {
                                Column {
                                    // Heades con color
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF1565C0), Color(0xFF1E88E5))
                                                ),
                                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "SOLICITUD DE CITA",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                    //Cuerpo
                                    Column(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.CalendarToday, contentDescription = null,
                                                tint = Color(0xFF1E88E5),
                                                modifier = Modifier.size(16.dp))
                                            Text(date, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                                color = appColors.textPrimaryColor)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null,
                                                tint = Color(0xFF43A047),
                                                modifier = Modifier.size(16.dp))
                                            Text(time, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                                color = appColors.textPrimaryColor)
                                        }
                                        if (notes.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.Notes, contentDescription = null,
                                                    tint = Color(0xFFFB8C00),
                                                    modifier = Modifier.size(16.dp))
                                                Text(notes, fontSize = 13.sp,
                                                    color = appColors.textSecondaryColor)
                                            }
                                        }

                                        Divider(
                                            color = if (isFromMe) Color.White.copy(0.3f) else Color.Gray.copy(0.2f),
                                            thickness = 0.5.dp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.HourglassEmpty, contentDescription = null,
                                                tint = Color(0xFFE53935),
                                                modifier = Modifier.size(14.dp))
                                            Text(
                                                text = "Esperando confirmación",
                                                fontSize = 12.sp,
                                                color = appColors.textSecondaryColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MessageType.AUDIO -> AudioMessageBubble(
                        audioPath = message.content,
                        duration = message.durationSeconds ?: 0,
                        timestamp = message.timestamp,
                        appColors = appColors,
                        isFromMe = message.senderId == viewModel.currentUserId,
                        senderAvatarUrl = if (message.senderId != viewModel.currentUserId) provider.photoUrl else null
                    )
                    else -> EnhancedMessageBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors
                    )
                }
            }
        }

        if (showTenderSelectionDialog) {
            TenderSelectionDialog(
                matchingTenders = matchingTenders,
                providerCategories = provider.categories,
                appColors = appColors,
                onDismiss = { showTenderSelectionDialog = false },
                onSelect = { viewModel.sendTenderInvitation(it); showTenderSelectionDialog = false }
            )
        }

        // Visor de imagen con zoom al tocar una imagen en el chat
        zoomedImageUrl?.let { url ->
            ImageZoomDialog(
                imageUrl = url,
                onDismiss = { zoomedImageUrl = null }
            )
        }
    }
}

// --- COMPONENTE BURBUJA PARA AUDIO (estilo WhatsApp) ---

@Composable
fun AudioMessageBubble(
    audioPath: String,
    duration: Int,
    timestamp: Long,
    appColors: AppColors,
    isFromMe: Boolean,
    senderAvatarUrl: String? = null
) {
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var audioDuration by remember { mutableIntStateOf(if (duration > 0) duration * 1000 else 0) }

    androidx.compose.runtime.DisposableEffect(audioPath) { onDispose { mediaPlayer?.release() } }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { currentPosition = it.currentPosition }
            kotlinx.coroutines.delay(100)
        }
    }

    fun togglePlay() {
        if (isPlaying) { mediaPlayer?.pause(); isPlaying = false }
        else {
            if (mediaPlayer == null) {
                try {
                    mediaPlayer = android.media.MediaPlayer().apply {
                        setDataSource(audioPath)
                        prepare()
                        audioDuration = this.duration
                        setOnCompletionListener { isPlaying = false; currentPosition = 0 }
                        start()
                    }
                    isPlaying = true
                } catch (e: Exception) { e.printStackTrace() }
            } else { mediaPlayer?.start(); isPlaying = true }
        }
    }

    fun formatMs(ms: Int): String {
        val s = ms / 1000
        return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar con badge mic (solo mensajes recibidos)
        if (!isFromMe) {
            Box(modifier = Modifier.size(44.dp)) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = appColors.surfaceColor
                ) {
                    if (senderAvatarUrl != null) {
                        coil.compose.AsyncImage(
                            model = senderAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = appColors.textSecondaryColor, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF2A2F32), androidx.compose.foundation.shape.CircleShape)
                        .border(1.5.dp, appColors.backgroundColor, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            Spacer(Modifier.width(6.dp))
        }

        // Burbuja
        Surface(
            modifier = Modifier.widthIn(min = 220.dp, max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            color = if (isFromMe) MaterialTheme.colorScheme.primary else appColors.surfaceColor,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isFromMe) Color.White else primaryColor,
                        modifier = Modifier.size(28.dp).clickable { togglePlay() }
                    )
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                    ) {
                        val dotCount = 28
                        val dotR = 2.5.dp.toPx()
                        val scrubR = 5.5.dp.toPx()
                        val spacing = size.width / dotCount
                        val progress = if (audioDuration > 0) currentPosition.toFloat() / audioDuration else 0f
                        val sx = (progress * size.width).coerceIn(0f, size.width)
                        val activeColor = if (isFromMe) Color.White else primaryColor
                        repeat(dotCount) { i ->
                            val x = i * spacing + spacing / 2f
                            drawCircle(
                                color = if (x < sx) activeColor else activeColor.copy(0.35f),
                                radius = dotR,
                                center = androidx.compose.ui.geometry.Offset(x, size.height / 2f)
                            )
                        }
                        drawCircle(
                            color = activeColor,
                            radius = scrubR,
                            center = androidx.compose.ui.geometry.Offset(sx.coerceIn(scrubR, size.width - scrubR), size.height / 2f)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                val textColor = if (isFromMe) Color.White.copy(0.75f) else appColors.textSecondaryColor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatMs(currentPosition), fontSize = 11.sp, color = textColor)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)),
                            fontSize = 11.sp,
                            color = textColor
                        )
                        if (isFromMe) {
                            Icon(Icons.Default.Done, null, tint = textColor, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTE BURBUJA PARA PRESUPUESTOS ---

@Composable
fun BudgetBubble(
    message: com.example.myapplication.data.local.MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    onClick: () -> Unit
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else appColors.surfaceColor,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.widthIn(max = 280.dp).clickable(onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, "Presupuesto", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Propuesta Técnica", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(message.content, fontSize = 14.sp, color = appColors.textPrimaryColor)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text("VER DETALLE")
                }
            }
        }
    }
}

// --- OTROS COMPONENTES ---

@Composable
fun DateSeparator(timestamp: Long, appColors: AppColors) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(color = appColors.surfaceColor.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp)) {
            Text(
                text = SimpleDateFormat("dd 'de' MMMM", Locale.getDefault()).format(Date(timestamp)),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 11.sp,
                color = appColors.textSecondaryColor
            )
        }
    }
}

@Composable
fun EnhancedMessageBubble(message: com.example.myapplication.data.local.MessageEntity, isMe: Boolean, appColors: AppColors) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val colors = if (isMe) {
        BubbleColors(container = MaterialTheme.colorScheme.primary, content = MaterialTheme.colorScheme.onPrimary)
    } else {
        BubbleColors(container = appColors.surfaceColor, content = appColors.textPrimaryColor)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(color = colors.container, shape = RoundedCornerShape(16.dp)) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp),
                color = colors.content
            )
        }
    }
}

data class BubbleColors(val container: Color, val content: Color)

// 🔥 CORRECCIÓN: Actualizada la firma para recibir una lista (List<String>)
@Composable
fun TenderSelectionDialog(
    matchingTenders: List<com.example.myapplication.data.local.TenderEntity>,
    providerCategories: List<String>,
    appColors: AppColors,
    onDismiss: () -> Unit,
    onSelect: (com.example.myapplication.data.local.TenderEntity) -> Unit
) {
    // ...
}

fun isSameDay(t1: Long, t2: Long): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return fmt.format(Date(t1)) == fmt.format(Date(t2))
}




/**
package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationView(
    provider: Provider,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    appColors: AppColors
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showTenderSelectionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val matchingTenders by viewModel.getMatchingTenders(provider.categories)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // --- VISOR DE PRESUPUESTO A4 ---
    if (selectedBudget != null) {
        Dialog(
            onDismissRequest = { viewModel.clearSelectedBudget() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BudgetMultiPageScreen(
                budget = selectedBudget!!,
                onBack = { viewModel.clearSelectedBudget() },
                // Aquí puedes pasar las lambdas onAccept/onReject si es necesario
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime),
        topBar = {
            ChatHeader(
                providerName = provider.displayName,
                providerPhoto = provider.photoUrl,
                isOnline = provider.isOnline,
                onBack = onBack,
                appColors = appColors
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(appColors.backgroundColor)) {
                AnimatedVisibility(visible = showAttachMenu) {
                    AttachmentOptionsMenu(
                        onDismiss = { showAttachMenu = false },
                        onImageClick = { /*TODO*/ },
                        onLocationClick = { viewModel.sendLocation(-26.8083, -65.2176, "Tucumán") },
                        onScheduleClick = { /*TODO*/ },
                        onInviteClick = { showTenderSelectionDialog = true }
                    )
                }
                MessageInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = { viewModel.sendText(it); inputText = "" },
                    appColors = appColors,
                    onAttachMenuToggle = { showAttachMenu = !showAttachMenu },
                    onCameraClick = { /*TODO*/ },
                    onAudioClick = {
                        if (isRecording) viewModel.stopRecordingAndSend()
                        else viewModel.startRecording(context)
                    },
                    onCancelAudio = { viewModel.cancelRecording() },
                    isRecordingAudio = isRecording
                )
            }
        },
        containerColor = appColors.backgroundColor
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(messages) { index, message ->
                if (index == 0 || !isSameDay(message.timestamp, messages[index - 1].timestamp)) {
                    DateSeparator(timestamp = message.timestamp, appColors = appColors)
                }

                when (message.type) {
                    MessageType.BUDGET -> BudgetBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors,
                        onClick = { message.relatedId?.let { viewModel.onBudgetClicked(it) } }
                    )
                    MessageType.IMAGE -> ImageMessageBubble(
                        imageUri = message.content,
                        timestamp = message.timestamp,
                        appColors = appColors,
                        isFromMe = message.senderId == viewModel.currentUserId,
                        onImageClick = { zoomedImageUrl = message.content }
                    )
                    else -> EnhancedMessageBubble(
                        message = message,
                        isMe = message.senderId == viewModel.currentUserId,
                        appColors = appColors
                    )
                }
            }
        }

        if (showTenderSelectionDialog) {
            TenderSelectionDialog(
                matchingTenders = matchingTenders,
                providerCategory = provider.categories,
                appColors = appColors,
                onDismiss = { showTenderSelectionDialog = false },
                onSelect = { viewModel.sendTenderInvitation(it); showTenderSelectionDialog = false }
            )
        }

        // Visor de imagen con zoom al tocar una imagen en el chat
        zoomedImageUrl?.let { url ->
            ImageZoomDialog(
                imageUrl = url,
                onDismiss = { zoomedImageUrl = null }
            )
        }
    }
}

// --- COMPONENTE BURBUJA PARA PRESUPUESTOS ---

@Composable
fun BudgetBubble(
    message: com.example.myapplication.data.local.MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    onClick: () -> Unit
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else appColors.surfaceColor,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.widthIn(max = 280.dp).clickable(onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, "Presupuesto", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Propuesta Técnica", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(message.content, fontSize = 14.sp, color = appColors.textPrimaryColor)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text("VER DETALLE")
                }
            }
        }
    }
}

// --- OTROS COMPONENTES ---

@Composable
fun DateSeparator(timestamp: Long, appColors: AppColors) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(color = appColors.surfaceColor.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp)) {
            Text(
                text = SimpleDateFormat("dd 'de' MMMM", Locale.getDefault()).format(Date(timestamp)),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 11.sp,
                color = appColors.textSecondaryColor
            )
        }
    }
}

@Composable
fun EnhancedMessageBubble(message: com.example.myapplication.data.local.MessageEntity, isMe: Boolean, appColors: AppColors) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val colors = if (isMe) {
        BubbleColors(container = MaterialTheme.colorScheme.primary, content = MaterialTheme.colorScheme.onPrimary)
    } else {
        BubbleColors(container = appColors.surfaceColor, content = appColors.textPrimaryColor)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = alignment) {
        Surface(color = colors.container, shape = RoundedCornerShape(16.dp)) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp),
                color = colors.content
            )
        }
    }
}

data class BubbleColors(val container: Color, val content: Color)

@Composable
fun TenderSelectionDialog(matchingTenders: List<com.example.myapplication.data.local.TenderEntity>, providerCategory: String, appColors: AppColors, onDismiss: () -> Unit, onSelect: (com.example.myapplication.data.local.TenderEntity) -> Unit) {
    // ...
}

fun isSameDay(t1: Long, t2: Long): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return fmt.format(Date(t1)) == fmt.format(Date(t2))
}
**/