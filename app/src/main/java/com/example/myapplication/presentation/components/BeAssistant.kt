package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ==========================================================================================
// --- 1. MODELOS DE ESTADO (Consumidos desde BeBrainViewModel) ---
// ==========================================================================================
enum class BeState {
    IDLE,               // Reposo
    NOTIFICATION_READY, // Alerta (Badge)
    TALKING             // Mostrando burbuja
}
enum class BeEmotion {
    NORMAL, HAPPY, SURPRISED, ANGRY, THINKING, SLEEPING
}
data class BeMessage(
    val icon: String,
    val text: String,
    val actionText: String? = null,
    val bubbleColor: Color,
    val textColor: Color = Color(0xFF05070A),
    val emotion: BeEmotion = BeEmotion.NORMAL
)
// ==========================================================================================
// --- 2. COMPONENTE PRINCIPAL: BE ASSISTANT ---
// ==========================================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BeAssistantSearchFab(
    modifier: Modifier = Modifier,
    // Estados sincronizados con BeBrainViewModel
    isSearchActive: Boolean = false,
    searchQuery: String = "", // 🔥 MODIFICACIÓN: Habilitado para búsqueda en tiempo real
    contextMessages: List<BeMessage> = emptyList(),
    isDormido: Boolean = false,
    //isThinking: Boolean = false,
    currentActions: List<BeSmallActionModel> = emptyList(),
    showSmallActions: Boolean = false,
    requestKeyboard: Boolean = false, // 🔥 NUEVO: Recibe el estado del teclado del ViewModel
    // Callbacks de acción
    onSearchQueryChange: (String) -> Unit = {}, // 🔥 MODIFICACIÓN: Habilitado para búsqueda en tiempo real
    onSearchStateChange: (Boolean) -> Unit = {},
    onBubbleActionClick: () -> Unit = {},
    onToggleSearch: () -> Unit = {},
    onToggleActions: () -> Unit = {},
    onToggleSleep: () -> Unit = {}
) {
// --- ESTADO INTERNO ----------------------------------------------------------------------------------
    var state by remember { mutableStateOf(BeState.IDLE) }
    var currentTipIndex by remember { mutableIntStateOf(0) }
// --- COORDENADAS DE ARRASTRE PERSISTENTES -----------------------------------------------------------------
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    val isDraggedToLeft = if (isSearchActive) false else offsetX < -120f
    val assistantSize by animateDpAsState(targetValue = if (isDormido) 40.dp else 56.dp, label = "AssistantSize")
    val alpha by animateFloatAsState(if (isDormido) 0.5f else 1f, label = "AlphaDormido")
// --- TECLADO Y BÚSQUEDA --------------------------------------------------------------------------------------
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
   // val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    // 🔥 MODIFICACIÓN: Separamos la lógica del teclado del estado de búsqueda activa
    // --- EFECTO PARA SOLICITUD EXPLÍCITA DE TECLADO ---
    LaunchedEffect(requestKeyboard) {
        if (requestKeyboard) {
            // Solo si se pide explícitamente (ej: openKeyboard() en ViewModel)
            delay(300)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // --- EFECTO PARA GESTIÓN DE FOCO Y ESTADOS SEGÚN BÚSQUEDA ---
    LaunchedEffect(isSearchActive) {
        if (!isSearchActive) {
            // Si la búsqueda se apaga, cerramos teclado y quitamos foco
            keyboardController?.hide()
            focusManager.clearFocus()
        }
        // Si entramos en búsqueda, nos aseguramos de que Be no esté en modo "Talking"
        if (isSearchActive && state == BeState.TALKING) state = BeState.IDLE
    }

    // --- CEREBRO DEL ASISTENTE (Lógica de notificación local) ---
    LaunchedEffect(state, contextMessages, isDormido) {
        if (isDormido) { state = BeState.IDLE; return@LaunchedEffect }
        when (state) {
            BeState.IDLE -> {
                delay((6000..15000).random().toLong())
                if (contextMessages.isNotEmpty() && !isSearchActive) {
                    currentTipIndex = contextMessages.indices.random()
                    state = BeState.NOTIFICATION_READY
                }
            }
            BeState.NOTIFICATION_READY -> {
                delay(12000)
                if (state == BeState.NOTIFICATION_READY) state = BeState.IDLE
            }
            BeState.TALKING -> { /* Permanece abierto */ }
        }
    }
    // --- ANIMACIONES FÍSICAS CONTINUAS ---
    val infiniteTransition = rememberInfiniteTransition(label = "be_animations")
    val floatY by infiniteTransition.animateFloat(initialValue = -3f, targetValue = 3f, animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float_y")
    val wiggleRotation by infiniteTransition.animateFloat(initialValue = -12f, targetValue = 12f, animationSpec = infiniteRepeatable(tween(150, easing = LinearEasing), RepeatMode.Reverse), label = "wiggle")
    val badgeScale by animateFloatAsState(targetValue = if (state == BeState.NOTIFICATION_READY) 1f else 0f, label = "badge_scale")
    val floatingAuraAlpha by infiniteTransition.animateFloat(initialValue = 0.1f, targetValue = 0.6f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "aura")
    // Vuelo de Be
    val flyUpPx by animateFloatAsState(targetValue = if (isSearchActive) -offsetY else 0f, animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow), label = "fly_up")
    val flySidePx by animateFloatAsState(targetValue = if (isSearchActive) -offsetX else 0f, animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow), label = "fly_side")

    // MOVIMIENTO Ocular
    var targetPupilX by remember { mutableFloatStateOf(0f) }
    var targetPupilY by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(state) {
        while (true) {
            when (state) {
                BeState.IDLE -> { targetPupilX = (-2..2).random().toFloat(); targetPupilY = (-3..3).random().toFloat(); delay((2000..4000).random().toLong()) }
                BeState.NOTIFICATION_READY -> { targetPupilX = 2.5f; targetPupilY = -3f; delay(1500); targetPupilX = 0f; targetPupilY = 0f; delay(1000) }
                BeState.TALKING -> { targetPupilX = 0f; targetPupilY = 0f; delay(1000) }
            }
        }
    }
    val pupilX by animateFloatAsState(targetValue = targetPupilX, animationSpec = tween(400), label = "pupilX")
    val pupilY by animateFloatAsState(targetValue = targetPupilY, animationSpec = tween(400), label = "pupilY")
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { delay((2500..7000).random().toLong()); isBlinking = true; delay(150); isBlinking = false } }
    val eyeScaleY by animateFloatAsState(targetValue = if (isBlinking) 0.1f else 1f, tween(120), label = "blink")

    // ==========================================================================================
    // --- RENDERIZADO PRINCIPAL ---
    // ==========================================================================================
    Box(
        modifier = (if (showSmallActions) Modifier.fillMaxSize() else modifier.fillMaxWidth())
            .zIndex(if (isDragging || state == BeState.TALKING || isSearchActive) 200f else 100f),
        contentAlignment = Alignment.BottomEnd // Anclamos todo al extremo derecho inferior
    ) {
        // --- CAPA 0: SCRIM GLOBAL PARA CERRAR ACCIONES ---
        AnimatedVisibility(
            visible = showSmallActions,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f)) // Sutil oscurecimiento
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onToggleActions() })
                    }
            )
        }

        // --- NUEVA CAPA: SCRIM SUPERIOR PARA BÚSQUEDA (INVERTIDO) ---
        // Este bloque añade el Blur y el degradado que pediste cuando la búsqueda está activa
        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn(tween(400)) + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut(tween(400)) + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp) // Altura similar a la de herramientas pero arriba
                    .align(Alignment.TopCenter)
                    .blur(15.dp) // Mismo nivel de desenfoque
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.98f), // 🔥 Inicia oscuro arriba
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent               // 🔥 Termina transparente hacia abajo
                            )
                        )
                    )
            )
        }

        // --- CAPA 1: HERRAMIENTAS (ESTÁTICA) ---
        // Estas herramientas se quedan en la posición inicial y no siguen el movimiento de Be.
        if (!isDormido && !isSearchActive) {
            // 🔥 MODIFICACIÓN: Transición fluida entre bandas.
            // Ambas viven en el mismo Box para que las animaciones de BeBuild.kt se solapen correctamente.
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                
                // 1. Barra Extendida (BeBuild): Solo si se mantiene presionado
                BeSmallActionsBuilder(
                    isVisible = showSmallActions,
                    actions = currentActions.filter { !it.isDefault }
                )

                // 2. Barra por Defecto (Fast, Lic, Fav): Solo si NO está la extendida
                BeDefaultActionsBand(
                    isVisible = !showSmallActions,
                    actions = currentActions.filter { it.isDefault }
                )
            }
        }

        // --- CAPA 2: ASISTENTE BE (MÓVIL) ---
        // Esta capa responde al arrastre (offsetX/offsetY) y al vuelo táctico.

        Row(
            modifier = Modifier
                .offset { IntOffset((offsetX + flySidePx).roundToInt(), (offsetY + flyUpPx).roundToInt()) }
                // 🔥 MODIFICACIÓN: Si la búsqueda está activa, la Row toma todo el ancho de la pantalla
                .then(if (isSearchActive) Modifier.fillMaxWidth().padding(start = 16.dp) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (isSearchActive) {
                // 🔥 MODIFICACIÓN: SearchBarComponent ahora recibe query y focusRequester
                SearchBarComponent(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    focusRequester = focusRequester,
                    onSearchClick = { keyboardController?.show(); focusRequester.requestFocus() },
                    modifier = Modifier.weight(1f) // 🔥 MODIFICACIÓN: Obliga a la barra a expandirse ocupando el espacio restante
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Cuerpo de Be
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .pointerInput(isDormido, isSearchActive) {
                        detectTapGestures(
                            onTap = { if (isDormido) onToggleSleep() else { state = BeState.IDLE; onSearchStateChange(true); onToggleSearch() } },
                            onLongPress = { if (!isDormido && !isSearchActive) onToggleActions() },
                            onDoubleTap = { onToggleSleep() }
                        )
                    }
                    .pointerInput(isSearchActive) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDrag = { change, dragAmount -> if (!isSearchActive) { change.consume(); offsetX += dragAmount.x; offsetY += dragAmount.y } }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                val currentMessage = if (contextMessages.isNotEmpty()) contextMessages.getOrNull(currentTipIndex) else null
                val currentEmotion = if (state != BeState.IDLE) currentMessage?.emotion ?: BeEmotion.NORMAL else BeEmotion.NORMAL
                
                Box(
                    modifier = Modifier.fillMaxSize().offset(y = floatY.dp).size(assistantSize).scale(if (isDormido) 0.8f else 1f).alpha(alpha),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDormido) { Text("💤", fontSize = 18.sp) } 
                    else {
                        if (isDragging) { Box(modifier = Modifier.offset(x = 5.dp, y = 5.dp).size(54.dp).scale(1.2f).alpha(floatingAuraAlpha).background(Color(0xFF22D3EE), CircleShape).blur(8.dp)) }
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            this.scale(size.width / 100f, size.height / 100f, pivot = Offset.Zero) {
                                drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(70f, 70f), Offset(94.5f, 94.5f), 16f, StrokeCap.Round)
                                drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                                drawLine(Color(0xFF22D3EE), Offset(73f, 73f), Offset(92f, 92f), 10f, StrokeCap.Round)
                                drawLine(Color(0xFF0891B2), Offset(76f, 76f), Offset(89f, 89f), 6f, StrokeCap.Round)
                                drawCircle(Color(0xFF0A0E14), 36f, Offset(50f, 50f))
                                drawCircle(Color(0xFF2197F5).copy(alpha = 0.2f), 32f, Offset(50f, 50f))
                                drawCircle(Color(0xFF2197F5), 32f, Offset(50f, 50f), style = Stroke(6f))
                                drawArc(color = Color.White.copy(alpha = 0.5f), startAngle = 180f, sweepAngle = 90f, useCenter = false, style = Stroke(4f, cap = StrokeCap.Round), topLeft = Offset(18f, 18f), size = Size(64f, 64f))
                                if (currentEmotion == BeEmotion.HAPPY) {
                                    drawPath(Path().apply { moveTo(33f, 50f); quadraticTo(40f, 38f, 47f, 50f) }, color = Color.White, style = Stroke(5f, cap = StrokeCap.Round))
                                    drawPath(Path().apply { moveTo(53f, 50f); quadraticTo(60f, 38f, 67f, 50f) }, color = Color.White, style = Stroke(5f, cap = StrokeCap.Round))
                                } else {
                                    drawOval(Color.White, Offset(32f, 50f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))
                                    drawOval(Color.White, Offset(52f, 50f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))
                                    val pupilRadius = if (currentEmotion == BeEmotion.SURPRISED) 2.5f else 4.5f
                                    drawCircle(Color(0xFF05070A), pupilRadius * eyeScaleY, Offset(40f + pupilX, 50f + pupilY))
                                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(41f + pupilX, 48f + pupilY))
                                    drawCircle(Color(0xFF05070A), pupilRadius * eyeScaleY, Offset(60f + pupilX, 50f + pupilY))
                                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(61f + pupilX, 48f + pupilY))
                                    if (currentEmotion == BeEmotion.ANGRY) {
                                        drawLine(Color(0xFF2197F5), Offset(28f, 36f), Offset(46f, 42f), strokeWidth = 5f, cap = StrokeCap.Round)
                                        drawLine(Color(0xFF2197F5), Offset(72f, 36f), Offset(54f, 42f), strokeWidth = 5f, cap = StrokeCap.Round)
                                    } else if (currentEmotion == BeEmotion.SURPRISED) {
                                        drawArc(Color(0xFF2197F5), 180f, 180f, false, Offset(32f, 32f), Size(16f, 10f), style = Stroke(3.5f, cap = StrokeCap.Round))
                                        drawArc(Color(0xFF2197F5), 180f, 180f, false, Offset(52f, 32f), Size(16f, 10f), style = Stroke(3.5f, cap = StrokeCap.Round))
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (badgeScale > 0.01f && currentMessage != null && !isSearchActive) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-14).dp).wrapContentSize(unbounded = true).graphicsLayer { scaleX = badgeScale; scaleY = badgeScale; rotationZ = wiggleRotation }.zIndex(20f).background(Color(0xFF05070A), CircleShape).border(2.5.dp, currentMessage.bubbleColor, CircleShape).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { state = BeState.TALKING }.padding(6.dp), contentAlignment = Alignment.Center) { Text(text = currentMessage.icon, fontSize = 16.sp) }
                }
                
                BeBubbleComic(isVisible = state == BeState.TALKING && !isSearchActive, isDraggedToLeft = isDraggedToLeft, message = currentMessage, allMessagesSize = contextMessages.size, currentIndex = currentTipIndex, onCloseClick = { state = BeState.IDLE }, onPrevClick = { if (currentTipIndex > 0) currentTipIndex-- }, onNextClick = { if (currentTipIndex < contextMessages.size - 1) currentTipIndex++ }, onActionClick = { state = BeState.IDLE; onBubbleActionClick() })
            }
        }
    }
}

// ==========================================================================================
// --- COMPONENTES AUXILIARES ---
// ==========================================================================================
//* SearchBarComponent: Barra de búsqueda premium con integración de BasicTextField.
@Composable
fun SearchBarComponent(query: String, onQueryChange: (String) -> Unit, focusRequester: FocusRequester, onSearchClick: () -> Unit, modifier: Modifier = Modifier) {
    GeminiCyberWrapper(modifier = modifier.fillMaxWidth(), isAnimated = true, showGlow = true, cornerRadius = 32.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(56.dp).clickable { onSearchClick() }.padding(horizontal = 20.dp)) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            BasicTextField(value = query, onValueChange = onQueryChange, modifier = Modifier.weight(1f).focusRequester(focusRequester), textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp), cursorBrush = SolidColor(Color(0xFF22D3EE)), decorationBox = { innerTextField -> Box(contentAlignment = Alignment.CenterStart) { if (query.isEmpty()) { Text(text = "Busca con ayuda de Be...", color = Color(0xFF94A3B8).copy(alpha = 0.9f), fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp) } ; innerTextField() } })
        }
    }
}

// ==========================================================================================
// --- PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun SearchBarComponentPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SearchBarComponent(
                query = "",
                onQueryChange = {},
                focusRequester = remember { FocusRequester() },
                onSearchClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun BeAssistantSearchFabPreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            BeAssistantSearchFab(
                contextMessages = listOf(
                    BeMessage("💡", "¡Hola! Soy Be, tu asistente.", null, Color(0xFF22D3EE))
                )
            )
        }
    }
}

@Preview(name = "Be Assistant - Search Active", showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun BeAssistantSearchFabActivePreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            BeAssistantSearchFab(
                isSearchActive = true,
                searchQuery = "Carpintero"
            )
        }
    }
}
