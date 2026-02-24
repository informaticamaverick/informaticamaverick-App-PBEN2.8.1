package com.example.myapplication.prestador.ui.chat

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import coil.request.ImageRequest
import com.example.myapplication.prestador.R
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    onReschedule: (() -> Unit)? = null
) {
    val colors = getPrestadorColors()
    val bubbleColor = if (isFromCurrentUser) {
        colors.primaryOrange
    } else {
        colors.surfaceElevated
    }
    val textColor = if (isFromCurrentUser) {
        Color.White
    } else {
        colors.textPrimary
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
            ),
            color = bubbleColor,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                when (message.type) {
                    Message.MessageType.TEXT -> {
                        TextMessageContent(
                            text = message.text ?: "",
                            textColor = textColor
                        )
                    }
                    Message.MessageType.IMAGE -> {
                        ImageMessageContent(
                            imageUrl = message.imageUrl,
                            text = message.text
                        )
                    }
                    Message.MessageType.AUDIO -> {
                        // Audio con timestamp integrado en el mismo nivel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AudioMessageContent(
                                audioUrl = message.audioUrl,
                                duration = message.audioDuration ?: 0,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Timestamp a la derecha, alineado abajo
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatMessageTime(message.timestamp),
                                    fontSize = 11.sp,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                                
                                if (isFromCurrentUser) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = null,
                                        tint = textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Message.MessageType.LOCATION -> {
                        LocationMessageContent(
                            latitude = message.latitude ?: 0.0,
                            longitude = message.longitude ?: 0.0
                        )
                    }
                    Message.MessageType.DOCUMENT -> {
                        DocumentMessageContent(
                            fileName = message.fileName ?: "Archivo",
                            fileSize = message.fileSize ?: 0
                        )
                    }
                    Message.MessageType.APPOINTMENT -> {
                        // 🐛 DEBUG: Log del mensaje de cita
                        println("🎨 MessageBubble APPOINTMENT - ID: ${message.appointmentId}, Status: ${message.appointmentStatus}")
                        
                        AppointmentMessageContent(
                            title = message.appointmentTitle ?: "",
                            date = message.appointmentDate ?: "",
                            time = message.appointmentTime ?: "",
                            status = message.appointmentStatus,
                            rejectionReason = message.rejectionReason,
                            onReschedule = onReschedule
                        )
                    }
                }
                
                // Timestamp y estado (excepto para AUDIO que ya lo tiene integrado)
                if (message.type != Message.MessageType.AUDIO) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTime(message.timestamp),
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        
                        if (isFromCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Done,
                                contentDescription = null,
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Contenido de mensaje de texto
@Composable
fun TextMessageContent(
    text: String,
    textColor: Color
) {
    // Detectar URLs en el texto
    val urlPattern = Regex("(https?://[^\\s]+)")
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        urlPattern.findAll(text).forEach { match ->
            // Texto antes del URL
            append(text.substring(lastIndex, match.range.first))
            
            // URL con estilo
            pushStringAnnotation(tag = "URL", annotation = match.value)
            withStyle(
                style = SpanStyle(
                    color = Color(0xFF60A5FA),
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(match.value)
            }
            pop()
            
            lastIndex = match.range.last + 1
        }
        // Texto restante
        append(text.substring(lastIndex))
    }
    
    val context = LocalContext.current
    ClickableText(
        text = annotatedString,
        style = androidx.compose.ui.text.TextStyle(
            color = textColor,
            fontSize = 15.sp
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                    context.startActivity(intent)
                }
        }
    )
}

// Contenido de mensaje con imagen
@Composable
fun ImageMessageContent(
    imageUrl: String?,
    text: String?
) {
    Column {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        if (!text.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}

// Contenido de mensaje de audio
@Composable
fun AudioMessageContent(
    audioUrl: String?,
    duration: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var audioDuration by remember { mutableStateOf(duration) }
    
    // Actualizar posición mientras reproduce
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    currentPosition = it.currentPosition
                }
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
            if (mediaPlayer == null && audioUrl != null) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(audioUrl)
                        prepare()
                        audioDuration = this.duration
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
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón de play/pause con padding para bajar
        Box(
            modifier = Modifier.padding(top = 6.dp)
        ) {
            IconButton(
                onClick = { togglePlayPause() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Barra de progreso y tiempo
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Barra de progreso
            LinearProgressIndicator(
                progress = if (audioDuration > 0) currentPosition.toFloat() / audioDuration.toFloat() else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Tiempo abajo de la barra
            Text(
                text = formatAudioDuration(currentPosition),
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

// Contenido de mensaje de ubicación
@Composable
fun LocationMessageContent(
    latitude: Double,
    longitude: Double
) {
    val context = LocalContext.current
    val colors = getPrestadorColors()
    
    // Generar link de Google Maps
    val mapsUrl = "https://www.google.com/maps?q=$latitude,$longitude"
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val uri = Uri.parse(mapsUrl)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
    ) {
        // Icono de ubicación
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Ubicación",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Ubicación compartida",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Link clickeable
        Text(
            text = mapsUrl,
            fontSize = 13.sp,
            color = Color(0xFF3B82F6), // Azul para link
            style = androidx.compose.ui.text.TextStyle(
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
            )
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Coordenadas
        Text(
            text = "📍 ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}",
            fontSize = 11.sp,
            color = colors.textSecondary
        )
    }
}

// Contenido de mensaje de documento
@Composable
fun DocumentMessageContent(
    fileName: String,
    fileSize: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF97316)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = formatFileSize(fileSize),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        IconButton(onClick = { /* TODO: Descargar */ }) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Descargar",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Contenido de mensaje de cita/appointment
@Composable
fun AppointmentMessageContent(
    title: String,
    date: String,
    time: String,
    status: Message.AppointmentProposalStatus? = null,
    rejectionReason: String? = null,
    onReschedule: (() -> Unit)? = null
) {
    val colors = getPrestadorColors()
    
    // 🐛 DEBUG: Log del estado recibido
    println("🎨 AppointmentMessageContent - Status recibido: $status")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado según el estado
        when (status) {
            Message.AppointmentProposalStatus.PENDING -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PROPUESTA ENVIADA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            Message.AppointmentProposalStatus.ACCEPTED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "✓ CITA CONFIRMADA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
            Message.AppointmentProposalStatus.REJECTED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "✗",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "PROPUESTA RECHAZADA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
            null -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PROPUESTA ENVIADA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // FECHA Y HORA EN EL MEDIO (más grande)
        Text(
            text = "$date, $time",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        when ( status) {
            Message.AppointmentProposalStatus.PENDING -> {
                Text(
                    text = "Esperando respuesta del cliente...",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }

            Message.AppointmentProposalStatus.ACCEPTED -> {
                Text(
                    text = "El cliente ha confirmaddo esta cita",
                    fontSize = 12.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }

            Message.AppointmentProposalStatus.REJECTED -> {
                if (!rejectionReason.isNullOrBlank()) {
                    Text(
                        text = "Rszón \"$rejectionReason\"",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { onReschedule?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryOrange
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon (
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Proponer Nueva Fecha", fontSize = 12.sp)
                }
            }
            null -> {
                Text(
                    text = "Esperando respuesta del cliente...",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Funciones auxiliares
private fun formatMessageTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun formatAudioDuration(millis: Int): String {
    val seconds = millis / 1000
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

// Indicador de "escribiendo..." con animación de puntos
@Composable
fun TypingIndicator(
    userName: String
) {
    val colors = getPrestadorColors()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 200.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 4.dp,
                bottomEnd = 16.dp
            ),
            color = colors.surfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = userName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryOrange
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "escribiendo",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    
                    // Animación de puntos
                    TypingDots()
                }
            }
        }
    }
}

@Composable
fun TypingDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            var alpha by remember { mutableStateOf(0.3f) }
            
            LaunchedEffect(Unit) {
                while (true) {
                    delay((index * 200).toLong())
                    alpha = 1f
                    delay(300)
                    alpha = 0.3f
                    delay(300)
                }
            }
            
            Text(
                text = "•",
                fontSize = 16.sp,
                color = Color.Gray.copy(alpha = alpha)
            )
        }
    }
}
