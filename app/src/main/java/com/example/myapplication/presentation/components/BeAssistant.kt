package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

// ==========================================================================================
// --- 1. MÁQUINA DE ESTADOS Y EMOCIONES ---
// ==========================================================================================
enum class BeState {
    IDLE,               // Reposo: Ojos inquietos, flota.
    NOTIFICATION_READY, // Alerta: Foco en la esquina, Be lo mira.
    TALKING             // Hablando: Burbuja activa.
}

enum class BeEmotion {
    NORMAL,
    HAPPY,      // Ojos en forma de arco (^^)
    SURPRISED,  // Cejas levantadas, pupilas pequeñas
    ANGRY       // Cejas fruncidas
}

data class BeMessage(
    val icon: String,
    val text: String,
    val bubbleColor: Color,
    val textColor: Color = Color(0xFF05070A),
    val emotion: BeEmotion = BeEmotion.NORMAL
)

// ==========================================================================================
// --- 2. COMPONENTE PRINCIPAL: BE ASSISTANT ---
// ==========================================================================================
@Composable
fun BeAssistantSearchFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(BeState.IDLE) }
    var currentMessage by remember { mutableStateOf<BeMessage?>(null) }

    // --- DICCIONARIO DE CONSEJOS CON EMOCIONES ---
    val messages = remember {
        listOf(
            BeMessage("💡", "La Zona Óptima te ayuda a no pagar de más en presupuestos. 📉", Color(0xFF22D3EE), emotion = BeEmotion.HAPPY),
            BeMessage("⚠️", "¡Toca la pestaña de Verificados para mayor seguridad! ✅", Color(0xFFF59E0B), emotion = BeEmotion.SURPRISED),
            BeMessage("🚨", "¡Maverick FAST busca profesionales 24hs por ti en segundos!", Color(0xFFE91E63), Color.White, emotion = BeEmotion.ANGRY),
            BeMessage("💬", "¿Tienes dudas? Envíale un mensaje directo al prestador.", Color(0xFF10B981), emotion = BeEmotion.NORMAL)
        )
    }

    // --- 3. CEREBRO DEL ASISTENTE ---
    LaunchedEffect(state) {
        when (state) {
            BeState.IDLE -> {
                delay((8000..15000).random().toLong())
                currentMessage = messages.random()
                state = BeState.NOTIFICATION_READY
            }
            BeState.NOTIFICATION_READY -> {
                delay(6000)
                state = BeState.IDLE
            }
            BeState.TALKING -> {
                delay(6000)
                state = BeState.IDLE
            }
        }
    }

    // --- 4. EYE TRACKING (Mirada Orgánica) ---
    var targetPupilX by remember { mutableFloatStateOf(0f) }
    var targetPupilY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(state) {
        if (state == BeState.IDLE) {
            // Ojos inquietos para no incomodar al usuario
            while (true) {
                targetPupilX = 0f
                targetPupilY = 0f
                delay((1500..4000).random().toLong()) // Mira al frente un rato

                targetPupilX = (-4..4).random().toFloat()
                targetPupilY = (-3..3).random().toFloat()
                delay((800..2000).random().toLong()) // Mira a los costados
            }
        } else if (state == BeState.NOTIFICATION_READY) {
            // Mira fijamente a la notificación (Arriba a la derecha)
            while (true) {
                targetPupilX = 6f
                targetPupilY = -6f
                delay(1200)
                targetPupilX = 0f
                targetPupilY = 0f  // Te mira a ti
                delay(800)
            }
        } else { // TALKING
            targetPupilX = 0f
            targetPupilY = 0f
        }
    }

    val pupilX by animateFloatAsState(targetValue = targetPupilX, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "pupilX")
    val pupilY by animateFloatAsState(targetValue = targetPupilY, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "pupilY")

    // --- 5. ANIMACIONES FÍSICAS ---
    val infiniteTransition = rememberInfiniteTransition(label = "be_float")

    val floatY by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(if (state == BeState.TALKING) 350 else 1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float_y"
    )

    val blushAlpha by animateFloatAsState(
        targetValue = if (state != BeState.IDLE) 0.6f else 0f, tween(500), label = "blush_alpha"
    )

    // Agitación de la notificación (Wiggle)
    val notifRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2500
                if (state == BeState.NOTIFICATION_READY) {
                    0f at 0; 15f at 200; -15f at 300; 10f at 400; -10f at 500; 0f at 600; 0f at 2500
                } else { 0f at 0 }
            }
        ), label = "notif_rot"
    )

    val notifScale by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2500
                if (state == BeState.NOTIFICATION_READY) {
                    0f at 0; 1.2f at 200; 1.1f at 400; 1f at 600; 1f at 2300; 0f at 2500
                } else { 0f at 0 }
            }
        ), label = "notif_scale"
    )

    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2000..6000).random().toLong())
            isBlinking = true; delay(150); isBlinking = false
        }
    }
    val eyeScaleY by animateFloatAsState(targetValue = if (isBlinking) 0.1f else 1f, tween(100), label = "blink")

    // --- 6. RENDERIZADO VISUAL ---
    Box(
        modifier = modifier
            .size(68.dp) // 🔥 MÁS GRANDE
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null,
                onClick = {
                    when (state) {
                        BeState.IDLE -> onClick()
                        BeState.NOTIFICATION_READY -> state = BeState.TALKING
                        BeState.TALKING -> { state = BeState.IDLE; onClick() }
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // --- CAPA A: BURBUJA CÓMIC ---
        AnimatedVisibility(
            visible = state == BeState.TALKING,
            enter = scaleIn(transformOrigin = TransformOrigin(1f, 1f), animationSpec = spring(dampingRatio = 0.6f)) + fadeIn(),
            exit = scaleOut(transformOrigin = TransformOrigin(1f, 1f)) + fadeOut(),
            modifier = Modifier.offset(x = 10.dp, y = (-75).dp).zIndex(20f)
        ) {
            currentMessage?.let { msg ->
                Box {
                    Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(Color(0xFF05070A), RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)))
                    Surface(color = msg.bubbleColor, border = BorderStroke(2.5.dp, Color(0xFF05070A)), shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp), modifier = Modifier.widthIn(max = 190.dp)) {
                        Text(text = msg.text, color = msg.textColor, fontWeight = FontWeight.Black, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }

        // --- CAPA B: ICONO FLOTANTE DE NOTIFICACIÓN (Corregido para evitar recortes) ---
        AnimatedVisibility(
            visible = state == BeState.NOTIFICATION_READY && currentMessage != null,
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.4f)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd) // El anclaje se hace en el AnimatedVisibility
                .offset(x = 16.dp, y = 6.dp) // 🔥 CORRECCIÓN: Más a la derecha (16dp) y más abajo (6dp)
                .zIndex(15f)
        ) {
            currentMessage?.let { msg ->
                Text(
                    text = msg.icon,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = notifRotation
                            scaleX = notifScale
                            scaleY = notifScale
                            shadowElevation = 25f
                            ambientShadowColor = msg.bubbleColor
                            spotShadowColor = msg.bubbleColor
                        }
                )
            }
        }

        // --- CAPA C: DIBUJO DEL PERSONAJE CON EMOCIONES ---
        val currentEmotion = if (state != BeState.IDLE) currentMessage?.emotion ?: BeEmotion.NORMAL else BeEmotion.NORMAL

        Canvas(
            modifier = Modifier
                .size(68.dp)
                .offset(y = floatY.dp)
                .zIndex(10f)
        ) {
            scale(size.width / 100f, size.height / 100f) {
                // Mango
                drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(73f, 73f), Offset(97f, 97f), 16f, StrokeCap.Round)
                drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                drawLine(Color(0xFF22D3EE), Offset(75f, 75f), Offset(90f, 90f), 14f, StrokeCap.Round)
                drawLine(Color(0xFF0891B2), Offset(78f, 78f), Offset(87f, 87f), 6f, StrokeCap.Round)

                // Lente
                drawCircle(Color(0xFF0A0E14), 36f, Offset(50f, 50f))
                drawCircle(Color(0xFF2197F5).copy(alpha = 0.15f), 32f, Offset(50f, 50f))
                drawCircle(Color(0xFF2197F5), 32f, Offset(50f, 50f), style = Stroke(6f))
                drawArc(color = Color.White.copy(alpha = 0.6f), startAngle = 180f, sweepAngle = 90f, useCenter = false, style = Stroke(4f, cap = StrokeCap.Round), topLeft = Offset(18f, 18f), size = Size(64f, 64f))

                // --- EMOCIONES Y OJOS ---
                if (currentEmotion == BeEmotion.HAPPY) {
                    // Ojos Felices ^^
                    drawPath(
                        Path().apply { moveTo(29f, 50f); quadraticBezierTo(37f, 38f, 45f, 50f) },
                        color = Color.White, style = Stroke(5f, cap = StrokeCap.Round)
                    )
                    drawPath(
                        Path().apply { moveTo(55f, 50f); quadraticBezierTo(63f, 38f, 71f, 50f) },
                        color = Color.White, style = Stroke(5f, cap = StrokeCap.Round)
                    )
                } else {
                    // Ojos Normales
                    drawOval(Color.White, Offset(29f, 50f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))
                    drawOval(Color.White, Offset(55f, 50f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))

                    // Tamaño de pupila (Chica si está sorprendido)
                    val pupilSize = if (currentEmotion == BeEmotion.SURPRISED) 2.5f else 4f

                    // Pupilas con Tracking (Centradas en 37 y 63 respectivamente)
                    drawCircle(Color(0xFF05070A), pupilSize * eyeScaleY, Offset(37f + pupilX, 50f + pupilY))
                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(38f + pupilX, 49f + pupilY))

                    drawCircle(Color(0xFF05070A), pupilSize * eyeScaleY, Offset(63f + pupilX, 50f + pupilY))
                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(64f + pupilX, 49f + pupilY))

                    // Cejas Dinámicas
                    if (currentEmotion == BeEmotion.ANGRY) {
                        drawLine(Color(0xFF2197F5), Offset(26f, 38f), Offset(44f, 44f), strokeWidth = 4f, cap = StrokeCap.Round)
                        drawLine(Color(0xFF2197F5), Offset(74f, 38f), Offset(56f, 44f), strokeWidth = 4f, cap = StrokeCap.Round)
                    } else if (currentEmotion == BeEmotion.SURPRISED) {
                        drawArc(Color(0xFF2197F5), 180f, 180f, false, Offset(29f, 32f), Size(16f, 10f), style = Stroke(3f, cap = StrokeCap.Round))
                        drawArc(Color(0xFF2197F5), 180f, 180f, false, Offset(55f, 32f), Size(16f, 10f), style = Stroke(3f, cap = StrokeCap.Round))
                    }
                }

                // Rubor
                drawOval(Color(0xFFE91E63).copy(alpha = blushAlpha), Offset(22.5f, 54.5f), Size(9f, 5f))
                drawOval(Color(0xFFE91E63).copy(alpha = blushAlpha), Offset(68.5f, 54.5f), Size(9f, 5f))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun BeAssistantSearchFabPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(40.dp)) {
            BeAssistantSearchFab(onClick = {})
        }
    }
}

/**
package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

// ==========================================================================================
// --- 1. MÁQUINA DE ESTADOS DEL ASISTENTE ---
// Define en qué "modo" se encuentra Be actualmente.
// ==========================================================================================
enum class BeState {
    IDLE,               // Reposo: Solo flota. Si lo tocan -> Abre la búsqueda.
    NOTIFICATION_READY, // Alerta: Muestra icono (💡, ⚠️) sobre la cabeza. Si lo tocan -> Pasa a TALKING.
    TALKING             // Hablando: Muestra burbuja de texto. Si lo tocan -> Cierra burbuja y abre búsqueda.
}

// ==========================================================================================
// --- 2. ESTRUCTURA DE MENSAJES (AGREGA MÁS AQUÍ) ---
// Aquí puedes configurar qué dice Be, qué icono muestra y qué color usa.
// ==========================================================================================
data class BeMessage(
    val icon: String,         // Emoji flotante (💡 Idea, ⚠️ Alerta, ✅ Éxito)
    val text: String,         // El texto de la burbuja
    val bubbleColor: Color,   // Color de fondo de la burbuja
    val textColor: Color = Color(0xFF05070A) // Color de la letra
)

// ==========================================================================================
// --- 3. COMPONENTE PRINCIPAL: BE ASSISTANT ---
// ==========================================================================================
@Composable
fun BeAssistantSearchFab(
    onClick: () -> Unit, // Acción que dispara la barra de búsqueda superior
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(BeState.IDLE) }
    var currentMessage by remember { mutableStateOf<BeMessage?>(null) }

    // --- DICCIONARIO DE CONSEJOS (AÑADE TUS FRASES AQUÍ) ---
    // Si quieres que Be diga cosas nuevas, simplemente agrégalas a esta lista.
    val messages = remember {
        listOf(
            BeMessage("💡", "La Zona Óptima te ayuda a no pagar de más en presupuestos. 📉", Color(0xFF22D3EE)),
            BeMessage("⚠️", "¡Toca la pestaña de Verificados para mayor seguridad! ✅", Color(0xFFF59E0B)),
            BeMessage("🚨", "¡Maverick FAST busca profesionales 24hs por ti en segundos!", Color(0xFFE91E63), Color.White),
            BeMessage("💬", "¿Tienes dudas? Envíale un mensaje directo al prestador.", Color(0xFF10B981))
        )
    }

    // --- 4. CEREBRO DEL ASISTENTE (TIMERS AUTOMÁTICOS) ---
    // Controla cuándo Be tiene una idea, cuánto tiempo la muestra y cuándo vuelve a dormir.
    LaunchedEffect(state) {
        when (state) {
            BeState.IDLE -> {
                delay((8000..15000).random().toLong()) // Entre 8 y 15 segundos flotando en paz
                currentMessage = messages.random()
                state = BeState.NOTIFICATION_READY     // Muestra la idea 💡
            }
            BeState.NOTIFICATION_READY -> {
                delay(6000) // Si el usuario no toca el foco en 6 seg, lo esconde
                state = BeState.IDLE
            }
            BeState.TALKING -> {
                delay(6000) // Da 6 segundos para leer la burbuja de cómic
                state = BeState.IDLE
            }
        }
    }

    // --- 5. ANIMACIONES FÍSICAS FLUIDAS ---
    val infiniteTransition = rememberInfiniteTransition(label = "be_float")

    // Animación 1: Flotación Vertical. Si habla, salta más cortito y rápido.
    val floatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == BeState.TALKING) 350 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    // Animación 2: Sonrojo (Alpha). Solo se sonroja cuando tiene notificaciones o habla.
    val blushAlpha by animateFloatAsState(
        targetValue = if (state != BeState.IDLE) 0.6f else 0f,
        animationSpec = tween(500),
        label = "blush_alpha"
    )

    // Animación 3: Parpadeo Aleatorio
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2000..6000).random().toLong())
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }
    val eyeScaleY by animateFloatAsState(targetValue = if (isBlinking) 0.1f else 1f, animationSpec = tween(100), label = "blink")

    // --- 6. CONTENEDOR MAESTRO DE RENDERIZADO ---
    Box(
        modifier = modifier
            // FIX ALINEACIÓN: Ahora tiene una forma perfectamente cuadrada y simétrica
            .size(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Oculta el brillo gris de Android para que parezca un personaje vivo
                onClick = {
                    // FLUJO DE INTERACCIÓN (UX) AL TOCAR A BE
                    when (state) {
                        BeState.IDLE -> onClick() // Si estaba normal, ejecuta la búsqueda
                        BeState.NOTIFICATION_READY -> state = BeState.TALKING // Si tenía un icono 💡, abre el diálogo
                        BeState.TALKING -> {
                            state = BeState.IDLE // Oculta el diálogo
                            onClick() // Ejecuta la búsqueda
                        }
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {

        // --- CAPA A: BURBUJA ESTILO CÓMIC ---
        AnimatedVisibility(
            visible = state == BeState.TALKING,
            enter = scaleIn(transformOrigin = TransformOrigin(1f, 1f), animationSpec = spring(dampingRatio = 0.6f)) + fadeIn(),
            exit = scaleOut(transformOrigin = TransformOrigin(1f, 1f)) + fadeOut(),
            modifier = Modifier
                .offset(x = 10.dp, y = (-75).dp) // Centrado dinámicamente arriba
                .zIndex(20f)
        ) {
            currentMessage?.let { msg ->
                Box {
                    // Sombra Neo-Brutalism
                    Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(Color(0xFF05070A), RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)))

                    // Cuerpo del mensaje
                    Surface(
                        color = msg.bubbleColor,
                        border = BorderStroke(2.5.dp, Color(0xFF05070A)),
                        shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                        modifier = Modifier.widthIn(max = 190.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = msg.textColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // --- CAPA B: ICONO FLOTANTE DE NOTIFICACIÓN (💡, ⚠️) ---
        AnimatedVisibility(
            visible = state == BeState.NOTIFICATION_READY,
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.4f)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
            modifier = Modifier
                .offset(y = (-55).dp) // Justo encima de su cabeza
                .zIndex(15f)
        ) {
            currentMessage?.let { msg ->
                Text(
                    text = msg.icon,
                    fontSize = 32.sp,
                    modifier = Modifier.graphicsLayer {
                        shadowElevation = 20f
                        ambientShadowColor = msg.bubbleColor
                        spotShadowColor = msg.bubbleColor
                    }
                )
            }
        }

        // --- CAPA C: RENDERIZADO DEL CUERPO EN CANVAS (100% Nativo, No imágenes) ---
        // FIX ALINEACIÓN: Se corrigieron las coordenadas (+5x, +5y) para que el centro
        // visual del dibujo (El lente de la lupa) quede exactamente en el centro de la caja.
        Canvas(
            modifier = Modifier
                .size(64.dp)
                .offset(y = floatY.dp) // Aplica la flotación matemática calculada arriba
                .zIndex(10f)
        ) {
            // Escalar sistema de coordenadas a 100x100 para dibujar sin usar decimales complicados
            scale(size.width / 100f, size.height / 100f) {

                // Mango de la lupa (Desplazado al centro)
                drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(73f, 73f), Offset(97f, 97f), 16f, StrokeCap.Round)
                drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                drawLine(Color(0xFF22D3EE), Offset(75f, 75f), Offset(90f, 90f), 14f, StrokeCap.Round)
                drawLine(Color(0xFF0891B2), Offset(78f, 78f), Offset(87f, 87f), 6f, StrokeCap.Round)

                // Lente (Cabeza Principal centrada en 50,50)
                drawCircle(Color(0xFF0A0E14), 36f, Offset(50f, 50f))
                drawCircle(Color(0xFF2197F5).copy(alpha = 0.15f), 32f, Offset(50f, 50f))
                drawCircle(Color(0xFF2197F5), 32f, Offset(50f, 50f), style = Stroke(6f))

                // Reflejo curvo del cristal
                drawArc(
                    color = Color.White.copy(alpha = 0.6f),
                    startAngle = 180f, sweepAngle = 90f, useCenter = false,
                    style = Stroke(4f, cap = StrokeCap.Round),
                    topLeft = Offset(18f, 18f), size = Size(64f, 64f)
                )

                // Ojo Izquierdo con parpadeo aplicado
                drawOval(Color.White, Offset(29f, 50f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))
                drawCircle(Color(0xFF05070A), 4f * eyeScaleY, Offset(39f, 50f))
                drawCircle(Color.White, 1.5f * eyeScaleY, Offset(40f, 49f))

                // Ojo Derecho con parpadeo aplicado
                drawOval(Color.White, Offset(55f, 50f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))
                drawCircle(Color(0xFF05070A), 4f * eyeScaleY, Offset(65f, 50f))
                drawCircle(Color.White, 1.5f * eyeScaleY, Offset(66f, 49f))

                // Rubor Dinámico
                drawOval(Color(0xFFE91E63).copy(alpha = blushAlpha), Offset(22.5f, 54.5f), Size(9f, 5f))
                drawOval(Color(0xFFE91E63).copy(alpha = blushAlpha), Offset(68.5f, 54.5f), Size(9f, 5f))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BeAssistantSearchFabPreview() {
    MyApplicationTheme {
        BeAssistantSearchFab(onClick = {})
    }
}

**/