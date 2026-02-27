
package com.example.myapplication.presentation.components

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*


// --- 1. HEADER CON ESTILO GLASS ---
@Composable
fun ChatHeader(
    providerName: String,
    providerPhoto: String?,
    isOnline: Boolean,
    onBack: () -> Unit,
    appColors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.8f), // Fondo oscuro profundo
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
            }

            AsyncImage(
                model = providerPhoto,
                contentDescription = null,
                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(0.1f)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.iconapp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(providerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (isOnline) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).background(Color(0xFF00FFFF), CircleShape)) // Cyan neón
                        Text("En línea", fontSize = 11.sp, color = Color(0xFF00FFFF))
                    }
                }
            }

            IconButton(onClick = { /* Acción de llamada */ }) {
                Icon(Icons.Default.Call, null, tint = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

// --- 2. BARRA DE ENTRADA (MÁXIMA TECNOLOGÍA) ---
@Composable
fun MessageInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    appColors: AppColors,
    onAttachMenuToggle: () -> Unit,
    onCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onCancelAudio: () -> Unit,
    isRecordingAudio: Boolean
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var recordingTime by remember { mutableIntStateOf(0) }
    val isCancelling = dragOffsetX < -160f

    // Timer para la grabación
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingTime = 0
            while (isRecordingAudio) {
                delay(1000)
                recordingTime++
            }
        } else {
            dragOffsetX = 0f
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRecordingAudio) {
                // MODO NORMAL
                IconButton(onClick = onAttachMenuToggle) {
                    Icon(Icons.Default.Add, null, tint = appColors.accentBlue)
                }

                TextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe algo...", color = Color.Gray, fontSize = 15.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White
                    ),
                    maxLines = 4
                )

                if (inputText.isEmpty()) {
                    IconButton(onClick = onCameraClick) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.Gray)
                    }
                }

                // BOTÓN DINÁMICO (ENVIAR O GRABAR)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(appColors.accentBlue)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onAudioClick() }, // Inicia grabación
                                onTap = { if (inputText.isNotEmpty()) onSendMessage(inputText) }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (inputText.isEmpty()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                        null, tint = Color.White, modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // MODO GRABACIÓN (ESTILO WHATSAPP)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Mic, null, tint = Color.Red,
                        modifier = Modifier.size(20.dp).graphicsLayer {
                            scaleX = 1.2f; scaleY = 1.2f
                        }
                    )

                    Text(
                        text = String.format("%02d:%02d", recordingTime / 60, recordingTime % 60),
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Spacer(Modifier.weight(1f))

                    // "Desliza para cancelar" animado
                    Row(
                        modifier = Modifier
                            .graphicsLayer { translationX = dragOffsetX }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        event.changes.forEach { change ->
                                            if (change.pressed) {
                                                val delta = change.position.x - change.previousPosition.x
                                                dragOffsetX = (dragOffsetX + delta).coerceAtMost(0f)
                                                if (dragOffsetX < -180f) onCancelAudio()
                                            } else {
                                                if (dragOffsetX > -180f) onAudioClick() // Enviar si no canceló
                                                dragOffsetX = 0f
                                            }
                                        }
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Text("Desliza para cancelar", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// --- 3. MENÚ DE ADJUNTOS TIPO "FLOATING GLASS" ---
@Composable
fun AttachmentOptionsMenu(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onLocationClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onInviteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
        color = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AttachmentIcon(Icons.Default.Image, "Imagen", Color(0xFF8B5CF6), onImageClick)
            AttachmentIcon(Icons.Default.LocationOn, "Mapa", Color(0xFF10B981), onLocationClick)
            AttachmentIcon(Icons.Default.CalendarMonth, "Cita", Color(0xFF3B82F6), onScheduleClick)
            AttachmentIcon(Icons.Default.Description, "Cotizar", Color(0xFF4F46E5), onInviteClick)
        }
    }
}

@Composable
fun AttachmentIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

// --- 4. DIÁLOGOS DE AGENDAMIENTO REAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String, notes: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()

    // Aquí podrías integrar un real DatePickerDialog de Android. Por ahora, estética moderna:
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text("Programar Visita", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecciona una fecha y hora para que el prestador visite tu domicilio.", color = Color.Gray, fontSize = 13.sp)

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Instrucciones o Notas", color = Color.Cyan) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Cyan
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm("15/02/2026", "10:30", notes) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
            ) {
                Text("Enviar Propuesta", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        }
    )
}

// --- 5. TARJETA DE PRESUPUESTO ESTILO CYBER ---
@Composable
fun TarjetaPresupuestoChat(
    title: String,
    amount: String,
    status: String,
    isFromMe: Boolean,
    appColors: AppColors,
    onClick: () -> Unit
) {
    val neonColor = Color(0xFF00FF9F) // Verde Neón

    Surface(
        modifier = Modifier
            .width(260.dp)
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, neonColor.copy(alpha = 0.3f))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(
                Brush.horizontalGradient(listOf(neonColor, Color.Transparent))
            ))

            Column(modifier = Modifier.padding(16.dp)) {
                Text("PRESUPUESTO RECIBIDO", color = neonColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(amount, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)

                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = neonColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "VER DETALLES",
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        color = neonColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * COMPONENTES DE CHAT (Room-Compatible)
 * Estos componentes consumen MessageEntity y están diseñados para el flujo Offline-First.
 */
/**
@Composable
fun ChatHeader(
    providerName: String, // Pasamos Strings simples para desacoplar
    providerPhoto: String?,
    isOnline: Boolean,
    onBack: () -> Unit,
    appColors: AppColors
) {
    Surface(
        //modifier = Modifier.fillMaxWidth(),
        //color = appColors.surfaceColor,
        //shadowElevation = 2.dp
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.8f), // Fondo oscuro profundo
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .statusBarsPadding(), // Importante para que no quede detrás de la barra de estado
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = appColors.textPrimaryColor)
            }

            // Avatar Pequeño
            AsyncImage(
                model = providerPhoto,
                contentDescription = null,
                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(0.1f)),
               // modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray.copy(0.2f)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.iconapp) // Tu icono por defecto
            )

            // Nombre y estado
            Column(modifier = Modifier.weight(1f)) {
                Text(providerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (isOnline) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).background(Color(0xFF00FFFF), CircleShape)) // Cyan neón
                        Text("En línea", fontSize = 11.sp, color = Color(0xFF00FFFF))
                    }
                }
            }

            IconButton(onClick = { /* Acción de llamada */ }) {
                Icon(Icons.Default.Call, null, tint = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

        //{
              //  Text(providerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = appColors.textPrimaryColor)
               // if (isOnline) {
                //    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                //        Box(Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                //        Text("En línea", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
                //    }
               // }
           // }




// --- 2. UTILIDADES DE FECHA ---
fun formatDate(timestamp: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

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
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            tonalElevation = 0.dp
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

// --- 3. BURBUJAS DE MENSAJE (CORE UI) ---
@Composable
fun MessageBubble(
    message: MessageEntity, // Usamos tu nueva Entidad
    appColors: AppColors,
    currentUserId: String = "currentUser", // ID para saber si es mío
    onBudgetClick: (String) -> Unit = {} // <--- Callback para navegar al detalle
) {
    val isFromMe = message.senderId == currentUserId
    val context = LocalContext.current

    // Decidimos qué dibujar según el TIPO de mensaje (Enum)
    when (message.type) {
        MessageType.LOCATION -> {
            // Burbuja de ubicación interactiva
            Surface(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .widthIn(max = 280.dp)
                    .clickable {
                        // Al hacer clic, abrimos Google Maps con las coordenadas guardadas en Room
                        if (message.latitude != null && message.longitude != null) {
                            val uri = "geo:${message.latitude},${message.longitude}?q=${message.latitude},${message.longitude}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            context.startActivity(intent)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor,
                shadowElevation = 1.dp
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = if (isFromMe) Color.White else appColors.accentBlue)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = message.content, // Muestra la dirección guardada por el Geocoder
                            color = if (isFromMe) Color.White else appColors.textPrimaryColor,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = formatTime(message.timestamp),
                        fontSize = 10.sp,
                        color = if (isFromMe) Color.White.copy(0.7f) else appColors.textSecondaryColor,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                }
            }
        }
        
        MessageType.VISIT -> {
            // Formato contenido: "Titulo|Fecha|Hora|Notas"
            val parts = message.content.split("|")
            TarjetaMensajeCita(
                title = parts.getOrNull(0) ?: "Solicitud",
                date = parts.getOrNull(1) ?: "",
                time = parts.getOrNull(2) ?: "",
                notes = parts.getOrNull(3) ?: "",
                isFromMe = isFromMe,
                appColors = appColors
            )
        }

        MessageType.AUDIO -> {
            AudioMessageBubble(
                audioPath = message.content, // La ruta del archivo
                duration = message.durationSeconds ?: 0,
                timestamp = message.timestamp,
                appColors = appColors,
                isFromMe = isFromMe
            )
        }

        MessageType.IMAGE -> {
            ImageMessageBubble(imageUri = message.content, timestamp = message.timestamp, appColors = appColors, isFromMe = isFromMe)
        }

        MessageType.BUDGET -> {
            // El contenido viene en formato: "Título|Monto|Estado"
            // Ejemplo: "Reparación PC|$15000|Pendiente"
            val parts = message.content.split("|")
            val title = parts.getOrNull(0) ?: "Presupuesto"
            val amount = parts.getOrNull(1) ?: "$0"
            val status = parts.getOrNull(2) ?: "Pendiente"
            val budgetId = message.relatedId ?: ""

            TarjetaPresupuestoChat(
                title = title,
                amount = amount,
                status = status,
                isFromMe = isFromMe,
                appColors = appColors,
                onClick = { onBudgetClick(budgetId) }
            )
        }

        else -> {
            TextMessageBubble(text = message.content, timestamp = message.timestamp, appColors = appColors, isFromMe = isFromMe)
        }
    }
}

// --- SUB-COMPONENTES DE BURBUJAS ---

@Composable
fun TextMessageBubble(text: String, timestamp: Long, appColors: AppColors, isFromMe: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isFromMe) 16.dp else 4.dp, bottomEnd = if (isFromMe) 4.dp else 16.dp),
            color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor,
            shadowElevation = 1.dp
        ) {
            Column(Modifier.padding(12.dp)) {
                val context = LocalContext.current
                val annotatedText = buildAnnotatedStringWithLinks(text, if (isFromMe) Color.White else Color(0xFF2563EB))
                ClickableText(
                    text = annotatedText,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = if (isFromMe) Color.White else appColors.textPrimaryColor),
                    onClick = { offset ->
                        annotatedText.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { 
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.item)))
                        }
                    }
                )
                Text(formatTime(timestamp), fontSize = 10.sp, color = if (isFromMe) Color.White.copy(0.7f) else appColors.textSecondaryColor, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
fun ImageMessageBubble(imageUri: String, timestamp: Long, appColors: AppColors, isFromMe: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start) {
        Surface(Modifier.widthIn(max = 280.dp), shape = RoundedCornerShape(16.dp), color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor) {
            Column(Modifier.padding(4.dp)) {
                AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                Text(formatTime(timestamp), fontSize = 10.sp, color = Color.White, modifier = Modifier.align(Alignment.End).padding(4.dp))
            }
        }
    }
}

@Composable
fun AudioMessageBubble(audioPath: String, duration: Int, timestamp: Long, appColors: AppColors, isFromMe: Boolean) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var audioDuration by remember { mutableIntStateOf(0) }

    DisposableEffect(audioPath) { onDispose { mediaPlayer?.release() } }

    // Loop de progreso
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { currentPosition = it.currentPosition }
            delay(100)
        }
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start) {
        Surface(Modifier.widthIn(min = 200.dp, max = 280.dp), shape = RoundedCornerShape(16.dp), color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {
                    if (isPlaying) { mediaPlayer?.pause(); isPlaying = false }
                    else {
                        if (mediaPlayer == null) {
                            try {
                                mediaPlayer = MediaPlayer().apply {
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
                }) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = if (isFromMe) Color.White else appColors.accentBlue)
                }
                Column(Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { if (audioDuration > 0) currentPosition.toFloat() / audioDuration else 0f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = if (isFromMe) Color.White else appColors.accentBlue
                    )
                   // Text(formatAudioDuration(if (audioDuration > 0) audioDuration else duration * 1000), fontSize = 10.sp, color = if (isFromMe) Color.White.copy(0.8f) else appColors.textSecondaryColor)
                }
            }
        }
    }
}

@Composable
fun TarjetaMensajeCita(title: String, date: String, time: String, notes: String, isFromMe: Boolean, appColors: AppColors) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start) {
        Column(Modifier.widthIn(min = 220.dp, max = 280.dp).background(if (isFromMe) appColors.accentBlue else appColors.surfaceColor, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = if (isFromMe) Color.White else appColors.textPrimaryColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(title.uppercase(), color = if (isFromMe) Color.White else appColors.textPrimaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text("$date, $time", color = if (isFromMe) Color.White else appColors.textPrimaryColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
            if (notes.isNotEmpty()) Text(notes, color = if (isFromMe) Color.White.copy(0.8f) else appColors.textSecondaryColor, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// --- 4. BARRA DE ENTRADA (CON ANIMACIÓN DE BASURA) ---
// --- 2. BARRA DE ENTRADA (MÁXIMA TECNOLOGÍA) ---
@Composable
fun MessageInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    appColors: AppColors,
    onAttachMenuToggle: () -> Unit,
    onCameraClick: () -> Unit,
    onAudioClick: @Composable () -> Unit,
    onCancelAudio: () -> Unit,
    isRecordingAudio: Boolean
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var recordingTime by remember { mutableIntStateOf(0) }
    val isCancelling = dragOffsetX < -160f

    // Timer para la grabación
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingTime = 0
            while (isRecordingAudio) {
                delay(1000)
                recordingTime++
            }
        } else {
            dragOffsetX = 0f
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRecordingAudio) {
                // MODO NORMAL
                IconButton(onClick = onAttachMenuToggle) {
                    Icon(Icons.Default.Add, null, tint = appColors.accentBlue)
                }

                TextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe algo...", color = Color.Gray, fontSize = 15.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White
                    ),
                    maxLines = 4
                )

                if (inputText.isEmpty()) {
                    IconButton(onClick = onCameraClick) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.Gray)
                    }
                }

                // BOTÓN DINÁMICO (ENVIAR O GRABAR)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(appColors.accentBlue)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onAudioClick() }, // Inicia grabación
                                onTap = { if (inputText.isNotEmpty()) onSendMessage(inputText) }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (inputText.isEmpty()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                        null, tint = Color.White, modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // MODO GRABACIÓN (ESTILO WHATSAPP)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Mic, null, tint = Color.Red,
                        modifier = Modifier.size(20.dp).graphicsLayer {
                            scaleX = 1.2f; scaleY = 1.2f
                        }
                    )

                    Text(
                        text = String.format("%02d:%02d", recordingTime / 60, recordingTime % 60),
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Spacer(Modifier.weight(1f))

                    // "Desliza para cancelar" animado
                    Row(
                        modifier = Modifier
                            .graphicsLayer { translationX = dragOffsetX }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        event.changes.forEach { change ->
                                            if (change.pressed) {
                                                val delta = change.position.x - change.previousPosition.x
                                                dragOffsetX = (dragOffsetX + delta).coerceAtMost(0f)
                                                if (dragOffsetX < -180f) onCancelAudio()
                                            } else {
                                                if (dragOffsetX > -180f) onAudioClick() // Enviar si no canceló
                                                dragOffsetX = 0f
                                            }
                                        }
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Text("Desliza para cancelar", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}



/**
@Composable
fun MessageInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    appColors: AppColors,
    onAttachMenuToggle: () -> Unit,
    onCameraClick: () -> Unit,
    onAudioClick: @Composable () -> Unit, // Inicia/Envía grabación
    onCancelAudio: () -> Unit, // Cancela grabación
    isRecordingAudio: Boolean
) {
    // Estados de animación local
    var recordingTime by remember { mutableIntStateOf(0) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var isCancelling by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Timer visual
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingTime = 0
            while (isRecordingAudio) {
                delay(1000)
                recordingTime++
            }
        } else {
            recordingTime = 0
            dragOffsetX = 0f // Reset al terminar
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = appColors.surfaceColor,
        shadowElevation = 8.dp
    ) {
        Crossfade(targetState = isRecordingAudio, label = "inputState") { isRecording ->
            if (!isRecording) {
                // --- MODO TEXTO NORMAL ---
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Botón Adjuntar
                    IconButton(onClick = onAttachMenuToggle) {
                        Icon(Icons.Default.AttachFile, null, tint = appColors.textSecondaryColor)
                    }

                    // Campo de Texto
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = appColors.backgroundColor
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                value = inputText,
                                onValueChange = onInputChange,
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        "Mensaje...",
                                        color = appColors.textSecondaryColor
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                maxLines = 4
                            )
                            // Botón Cámara (solo si no hay texto)
                            if (inputText.isEmpty()) {
                                IconButton(onClick = onCameraClick) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        null,
                                        tint = appColors.textSecondaryColor
                                    )
                                }
                            }
                        }
                    }

                    // Botón Enviar / Grabar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(appColors.accentBlue, CircleShape)
                            .clip(CircleShape)
                            .then(if (inputText.isEmpty()) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                       // onLongPress = { onAudioClick() } // Inicia grabación
                                    )
                                }
                            } else {
                                Modifier.clickable { onSendMessage(inputText) }
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (inputText.isEmpty()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            } else {
                // --- MODO GRABACIÓN (SLIDE TO CANCEL) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    // Detectar cuando suelta el dedo para enviar
                                    tryAwaitRelease()
                                    //if (!isCancelling) onAudioClick() // Enviar al soltar
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    event.changes.forEach { change ->
                                        val dragX = change.position.x - change.previousPosition.x
                                        if (dragX < 0) { // Arrastrar a la izquierda
                                            dragOffsetX = (dragOffsetX + dragX).coerceAtLeast(-400f)
                                        }
                                        if (change.changedToUp()) {
                                            if (dragOffsetX < -150f) {
                                                isCancelling = true
                                                onCancelAudio()
                                            } else {
                                                // onAudioClick() // Enviar (Manejado por onPress release)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    // Papelera animada
                    val trashScale by animateFloatAsState(if (dragOffsetX < -150f) 1.2f else 1f, label = "scale")
                    Icon(
                        Icons.Default.Delete, null, tint = Color.Red,
                        modifier = Modifier.align(Alignment.CenterStart).scale(trashScale)
                    )

                                        // Texto Deslizar
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer { translationX = dragOffsetX },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.Gray)
                        Text("Desliza para cancelar", color = Color.Gray)
                    }

                                        // Contador y Micro
                    Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            String.format("%02d:%02d", recordingTime / 60, recordingTime % 60),
                            modifier = Modifier.padding(end = 16.dp),
                            color = appColors.textPrimaryColor
                        )
                        Box(
                            modifier = Modifier.size(48.dp).background(Color.Red, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, null, tint = Color.White) // Icono estático mientras graba
                        }
                    }
                }
            }
        }
    }
}
**/
                    // --- 5. MENÚS Y DIÁLOGOS ---
@Composable
fun AttachmentOptionsMenu(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onLocationClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onInviteClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AttachmentBubble(R.drawable.ic_launcher_foreground, "Imágenes", Color(0xFF8B5CF6), onImageClick) // Usa tu drawable real
        AttachmentBubble(R.drawable.ic_launcher_foreground, "Agendar cita", Color(0xFF3B82F6), onScheduleClick)
        AttachmentBubble(R.drawable.ic_launcher_foreground, "Ubicación", Color(0xFF10B981), onLocationClick)
// 🔥 AGREGAR EL BOTÓN VISUAL:
        AttachmentBubble(
            icon = com.example.myapplication.R.drawable.ic_launcher_foreground,
            label = "Invitar a Cotizar",
            color = Color(0xFF4F46E5), // Indigo
            onClick = onInviteClick
        )
    }
}

                    @Composable
                    fun AttachmentBubble(icon: Int, label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder Icon (Reemplaza con tus painterResource)
            Icon(Icons.Default.Add, null, tint = Color.White)
        }
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 4.dp) {
            Text(label, modifier = Modifier.padding(12.dp, 6.dp), fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }

// --- HELPER STRINGS ---
                    fun buildAnnotatedStringWithLinks(text: String, linkColor: Color) =
                        buildAnnotatedString {
                            val urlPattern = Regex("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+")
                            var lastIndex = 0
                            urlPattern.findAll(text).forEach { matchResult ->
                                append(text.substring(lastIndex, matchResult.range.first))
                                pushStringAnnotation("URL", matchResult.value)
                                withStyle(
                                    SpanStyle(
                                        color = linkColor,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append(
                                        matchResult.value
                                    )
                                }
                                pop()
                                lastIndex = matchResult.range.last + 1
                            }
                            append(text.substring(lastIndex))
                        }



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String, notes: String) -> Unit
) {
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    // Implementación simplificada para mantener el archivo limpio.
    // Aquí irían tus DatePicker y TimePicker originales.

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar Cita") },
        text = {
            Column {
                OutlinedTextField(value = selectedDate, onValueChange = { selectedDate = it }, label = { Text("Fecha (DD/MM/AAAA)") })
                OutlinedTextField(value = selectedTime, onValueChange = { selectedTime = it }, label = { Text("Hora (HH:MM)") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDate, selectedTime, notes) }) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun TarjetaPresupuestoChat(
    title: String,
    amount: String,
    status: String, // "Pendiente", "Aprobado", etc.
    isFromMe: Boolean,
    appColors: AppColors,
    onClick: () -> Unit
) {
    val statusColor = when (status.lowercase()) {
        "aprobado" -> Color(0xFF10B981) // Verde
        "rechazado" -> Color(0xFFEF4444) // Rojo
        else -> Color(0xFFF59E0B) // Naranja (Pendiente)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .width(260.dp)
                .clickable { onClick() }, // Al hacer clic, ver detalle completo
            shape = RoundedCornerShape(12.dp),
            color = if (isFromMe) appColors.surfaceColor else Color.White,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(0.2f))
        ) {
            Column {
                // Franja de color según estado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(statusColor)
                )

                Column(modifier = Modifier.padding(12.dp)) {
                    // Encabezado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "PRESUPUESTO",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.AttachMoney, // O Icons.Default.Receipt
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Título del trabajo
                    Text(
                        text = title,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimaryColor
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Precio
                    Text(
                        text = amount,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = appColors.textPrimaryColor
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Ver Detalle (Visual)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(appColors.backgroundColor, RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VER DETALLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textSecondaryColor
                        )
                    }
                }
            }
        }
    }
}
**/