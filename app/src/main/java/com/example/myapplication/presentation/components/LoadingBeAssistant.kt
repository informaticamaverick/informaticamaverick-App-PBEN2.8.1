package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

/**
 * --- PANTALLA DE CARGA INMERSIVA "BE INVESTIGATOR" ---
 * Bloquea la pantalla con un fondo cyber-oscuro.
 * Muestra a Be volando en una ruta predefinida escaneando con un haz de luz holográfico.
 */
@Composable
fun LoadingBeAssistantScreen(
    mainText: String = "Buscando Prestadores...",
    subText: String = "Analizando base de datos local",
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    val infiniteTransition = rememberInfiniteTransition(label = "be_investigator")

    // --- 1. ANIMACIÓN DE VUELO (RUTA DE BÚSQUEDA MISION) ---
    val beOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                -screenWidth * 0.3f at 700
                -screenWidth * 0.28f at 1400
                screenWidth * 0.3f at 2450
                screenWidth * 0.28f at 3150
                -screenWidth * 0.25f at 4200
                -screenWidth * 0.22f at 4900
                screenWidth * 0.25f at 5600
                screenWidth * 0.22f at 6300
                0f at 7000
            }
        ), label = "fly_x"
    )

    val beOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                screenHeight * 0.25f at 700
                screenHeight * 0.22f at 1400
                -screenHeight * 0.25f at 2450
                -screenHeight * 0.22f at 3150
                -screenHeight * 0.3f at 4200
                -screenHeight * 0.28f at 4900
                screenHeight * 0.3f at 5600
                screenHeight * 0.28f at 6300
                0f at 7000
            }
        ), label = "fly_y"
    )

    val beRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                -20f at 700
                -10f at 1400
                25f at 2450
                15f at 3150
                -30f at 4200
                -15f at 4900
                35f at 5600
                15f at 6300
                0f at 7000
            }
        ), label = "fly_rot"
    )

    val beScale by infiniteTransition.animateFloat(
        initialValue = 1.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                1.2f at 0
                0.9f at 700
                0.9f at 1400
                1.1f at 2450
                1.1f at 3150
                0.8f at 4200
                0.8f at 4900
                1.3f at 5600
                1.3f at 6300
                1.2f at 7000
            }
        ), label = "fly_scale"
    )

    // --- 2. ANIMACIÓN DEL HAZ DE LUZ (ESCANEO) ---
    val beamRotation by infiniteTransition.animateFloat(
        initialValue = -35f, targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "beam_sweep"
    )

    // --- 3. ANIMACIÓN DE OJOS FRENÉTICOS ---
    val pupilX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0; -3f at 200; 3f at 400; 4f at 800; -2f at 1000; -4f at 1400; 2f at 1600; 0f at 2000
            }
        ), label = "pupil_x"
    )
    val pupilY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0; 3f at 200; 3f at 400; -2f at 800; -4f at 1000; 2f at 1400; 4f at 1600; 0f at 2000
            }
        ), label = "pupil_y"
    )

    // Parpadeo normal
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }
    val eyeScaleY by animateFloatAsState(targetValue = if (isBlinking) 0.1f else 1f, animationSpec = tween(100), label = "blink")

    // Animación del texto degradado
    val textGradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "text_grad"
    )

    // --- RENDERIZADO ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020408)) // Fondo base profundo
            .pointerInput(Unit) {}, // Atrapa los clics para bloquear la pantalla debajo
        contentAlignment = Alignment.Center
    ) {
        // Fondo Radial
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0xFF020408).copy(alpha = 0.8f)),
                        radius = 1500f
                    )
                )
        )

        // TEXTO CENTRAL (HUD)
        Box(
            modifier = Modifier
                .background(Color(0xFF05070A).copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF2197F5).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(horizontal = 40.dp, vertical = 20.dp)
                .zIndex(50f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val textBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2197F5), Color(0xFF9B51E0), Color(0xFFE91E63), Color(0xFF2197F5)),
                    start = Offset(textGradientOffset - 500f, 0f),
                    end = Offset(textGradientOffset, 0f)
                )
                Text(
                    text = mainText.uppercase(),
                    style = TextStyle(brush = textBrush),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subText,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- EL INVESTIGADOR "BE" (Y SU HAZ DE LUZ) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(120.dp) // Tamaño base de Be
                    .graphicsLayer {
                        translationX = beOffsetX.dp.toPx()
                        translationY = beOffsetY.dp.toPx()
                        scaleX = beScale
                        scaleY = beScale
                        rotationZ = beRotation
                        shadowElevation = 30f
                        ambientShadowColor = Color.Black
                        spotShadowColor = Color.Black
                    }
            ) {
                // Pre-calculamos los valores en pixeles antes del withTransform para evitar errores
                val cx = 50.dp.toPx()
                val cy = 45.dp.toPx()
                val beamLeftX = (-150).dp.toPx()
                val beamRightX = 250.dp.toPx()
                val beamBottomY = 600.dp.toPx()
                val gradEndY = 500.dp.toPx()

                // 1. DIBUJAR EL HAZ DE LUZ (ESCANER)
                withTransform({
                    translate(left = cx, top = cy) // Origen en el centro de la lente
                    rotate(beamRotation)
                    translate(left = -cx, top = -cy) // Volver atrás
                }) {
                    val beamPath = Path().apply {
                        moveTo(cx, cy) // Pico en el centro de la lente
                        lineTo(beamLeftX, beamBottomY) // Se abre a la izquierda abajo
                        lineTo(beamRightX, beamBottomY) // Se abre a la derecha abajo
                        close()
                    }
                    drawPath(
                        path = beamPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2197F5).copy(alpha = 0.5f),
                                Color(0xFF9B51E0).copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            startY = cy,
                            endY = gradEndY
                        ),
                        blendMode = BlendMode.Screen // Efecto holográfico
                    )
                }

                // 2. DIBUJAR EL CUERPO DE "BE"
                scale(size.width / 100f, size.height / 100f) {
                    // Mango de la lupa
                    drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(68f, 68f), Offset(92f, 92f), 16f, StrokeCap.Round)
                    drawLine(Color(0xFF1E293B), Offset(65f, 65f), Offset(90f, 90f), 14f, StrokeCap.Round)
                    drawLine(Color(0xFF22D3EE), Offset(70f, 70f), Offset(85f, 85f), 14f, StrokeCap.Round)
                    drawLine(Color(0xFF0891B2), Offset(73f, 73f), Offset(82f, 82f), 6f, StrokeCap.Round)

                    // Lente (Cabeza Principal)
                    drawCircle(Color(0xFF0A0E14), 36f, Offset(45f, 45f))
                    drawCircle(Color(0xFF2197F5).copy(alpha = 0.2f), 32f, Offset(45f, 45f))
                    drawCircle(Color(0xFF2197F5), 32f, Offset(45f, 45f), style = Stroke(6f))

                    // Reflejo curvo del cristal
                    drawArc(
                        color = Color.White.copy(alpha = 0.7f),
                        startAngle = 180f, sweepAngle = 90f, useCenter = false,
                        style = Stroke(4f, cap = StrokeCap.Round),
                        topLeft = Offset(13f, 13f), size = androidx.compose.ui.geometry.Size(64f, 64f)
                    )

                    // --- OJOS FRENÉTICOS ---
                    // Ojo Izquierdo
                    drawOval(Color.White, Offset(24f, 45f - (12f * eyeScaleY)), androidx.compose.ui.geometry.Size(16f, 24f * eyeScaleY))
                    drawCircle(Color(0xFF05070A), 4.5f * eyeScaleY, Offset(34f + pupilX, 45f + pupilY)) // Pupila inquieta
                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(35f + pupilX, 44f + pupilY))

                    // Ojo Derecho
                    drawOval(Color.White, Offset(50f, 45f - (12f * eyeScaleY)), androidx.compose.ui.geometry.Size(16f, 24f * eyeScaleY))
                    drawCircle(Color(0xFF05070A), 4.5f * eyeScaleY, Offset(60f + pupilX, 45f + pupilY)) // Pupila inquieta
                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(61f + pupilX, 44f + pupilY))
                }
            }
        }
    }
}

// ==========================================================================================
// --- PREVIEW ---
// ==========================================================================================
@Preview(showBackground = true, backgroundColor = 0xFF020408, widthDp = 360, heightDp = 800)
@Composable
fun LoadingBeAssistantPreview() {
    MyApplicationTheme {
        LoadingBeAssistantScreen(
            mainText = "Sincronizando...",
            subText = "Conectando con la red Maverick"
        )
    }
}


/**
package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.time.delay

/**
 * --- PANTALLA DE CARGA INMERSIVA "BE INVESTIGATOR" ---
 * Bloquea la pantalla con un fondo cyber-oscuro.
 * Muestra a Be volando en una ruta predefinida escaneando con un haz de luz holográfico.
 */
@Composable
fun LoadingBeAssistantScreen(
    mainText: String = "Buscando Prestadores...",
    subText: String = "Analizando base de datos local",
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    val infiniteTransition = rememberInfiniteTransition(label = "be_investigator")

    // --- 1. ANIMACIÓN DE VUELO (RUTA DE BÚSQUEDA MISION) ---
    val beOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                -screenWidth * 0.3f at 700
                -screenWidth * 0.28f at 1400
                screenWidth * 0.3f at 2450
                screenWidth * 0.28f at 3150
                -screenWidth * 0.25f at 4200
                -screenWidth * 0.22f at 4900
                screenWidth * 0.25f at 5600
                screenWidth * 0.22f at 6300
                0f at 7000
            }
        ), label = "fly_x"
    )

    val beOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                screenHeight * 0.25f at 700
                screenHeight * 0.22f at 1400
                -screenHeight * 0.25f at 2450
                -screenHeight * 0.22f at 3150
                -screenHeight * 0.3f at 4200
                -screenHeight * 0.28f at 4900
                screenHeight * 0.3f at 5600
                screenHeight * 0.28f at 6300
                0f at 7000
            }
        ), label = "fly_y"
    )

    val beRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                -20f at 700
                -10f at 1400
                25f at 2450
                15f at 3150
                -30f at 4200
                -15f at 4900
                35f at 5600
                15f at 6300
                0f at 7000
            }
        ), label = "fly_rot"
    )

    val beScale by infiniteTransition.animateFloat(
        initialValue = 1.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                1.2f at 0
                0.9f at 700
                0.9f at 1400
                1.1f at 2450
                1.1f at 3150
                0.8f at 4200
                0.8f at 4900
                1.3f at 5600
                1.3f at 6300
                1.2f at 7000
            }
        ), label = "fly_scale"
    )

    // --- 2. ANIMACIÓN DEL HAZ DE LUZ (ESCANEO) ---
    val beamRotation by infiniteTransition.animateFloat(
        initialValue = -35f, targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "beam_sweep"
    )

    // --- 3. ANIMACIÓN DE OJOS FRENÉTICOS ---
    val pupilX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0; -3f at 200; 3f at 400; 4f at 800; -2f at 1000; -4f at 1400; 2f at 1600; 0f at 2000
            }
        ), label = "pupil_x"
    )
    val pupilY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0; 3f at 200; 3f at 400; -2f at 800; -4f at 1000; 2f at 1400; 4f at 1600; 0f at 2000
            }
        ), label = "pupil_y"
    )

    // Parpadeo normal
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }
    val eyeScaleY by animateFloatAsState(targetValue = if (isBlinking) 0.1f else 1f, animationSpec = tween(100), label = "blink")

    // Animación del texto degradado
    val textGradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "text_grad"
    )

    // --- RENDERIZADO ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020408)) // Fondo base profundo
            .pointerInput(Unit) {}, // Atrapa los clics para bloquear la pantalla debajo
        contentAlignment = Alignment.Center
    ) {
        // Fondo Radial
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0xFF020408).copy(alpha = 0.8f)),
                        radius = 1500f
                    )
                )
        )

        // TEXTO CENTRAL (HUD)
        Box(
            modifier = Modifier
                .background(Color(0xFF05070A).copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF2197F5).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(horizontal = 40.dp, vertical = 20.dp)
                .zIndex(50f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val textBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2197F5), Color(0xFF9B51E0), Color(0xFFE91E63), Color(0xFF2197F5)),
                    start = Offset(textGradientOffset - 500f, 0f),
                    end = Offset(textGradientOffset, 0f)
                )
                Text(
                    text = mainText.uppercase(),
                    style = TextStyle(brush = textBrush),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subText,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- EL INVESTIGADOR "BE" (Y SU HAZ DE LUZ) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(120.dp) // Tamaño base de Be
                    .graphicsLayer {
                        translationX = beOffsetX.dp.toPx()
                        translationY = beOffsetY.dp.toPx()
                        scaleX = beScale
                        scaleY = beScale
                        rotationZ = beRotation
                        shadowElevation = 30f
                        ambientShadowColor = Color.Black
                        spotShadowColor = Color.Black
                    }
            ) {
                // 1. DIBUJAR EL HAZ DE LUZ (ESCANER)
                // Se dibuja detrás de Be, rotando desde el centro de la lente.
                withTransform({
                    translate(left = 50.dp.toPx(), top = 45.dp.toPx()) // Origen en el centro de la lente
                    rotate(beamRotation)
                    translate(left = -50.dp.toPx(), top = -45.dp.toPx()) // Volver atrás
                }) {
                    val beamPath = Path().apply {
                        moveTo(50.dp.toPx(), 45.dp.toPx()) // Pico en el centro de la lente
                        lineTo(-150.dp.toPx(), 600.dp.toPx()) // Se abre a la izquierda abajo
                        lineTo(250.dp.toPx(), 600.dp.toPx()) // Se abre a la derecha abajo
                        close()
                    }
                    drawPath(
                        path = beamPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2197F5).copy(alpha = 0.5f),
                                Color(0xFF9B51E0).copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            startY = 45.dp.toPx(),
                            endY = 500.dp.toPx()
                        ),
                        blendMode = BlendMode.Screen // Efecto holográfico
                    )
                }

                // 2. DIBUJAR EL CUERPO DE "BE"
                scale(size.width / 100f, size.height / 100f) {

                    // Mango de la lupa
                    drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(68f, 68f), Offset(92f, 92f), 16f, StrokeCap.Round)
                    drawLine(Color(0xFF1E293B), Offset(65f, 65f), Offset(90f, 90f), 14f, StrokeCap.Round)
                    drawLine(Color(0xFF22D3EE), Offset(70f, 70f), Offset(85f, 85f), 14f, StrokeCap.Round)
                    drawLine(Color(0xFF0891B2), Offset(73f, 73f), Offset(82f, 82f), 6f, StrokeCap.Round)

                    // Lente (Cabeza Principal)
                    drawCircle(Color(0xFF0A0E14), 36f, Offset(45f, 45f))
                    drawCircle(Color(0xFF2197F5).copy(alpha = 0.2f), 32f, Offset(45f, 45f))
                    drawCircle(Color(0xFF2197F5), 32f, Offset(45f, 45f), style = Stroke(6f))

                    // Reflejo curvo del cristal
                    drawArc(
                        color = Color.White.copy(alpha = 0.7f),
                        startAngle = 180f, sweepAngle = 90f, useCenter = false,
                        style = Stroke(4f, cap = StrokeCap.Round),
                        topLeft = Offset(13f, 13f), size = androidx.compose.ui.geometry.Size(64f, 64f)
                    )

                    // --- OJOS FRENÉTICOS ---
                    // Ojo Izquierdo
                    drawOval(Color.White, Offset(24f, 45f - (12f * eyeScaleY)), androidx.compose.ui.geometry.Size(16f, 24f * eyeScaleY))
                    drawCircle(Color(0xFF05070A), 4.5f * eyeScaleY, Offset(34f + pupilX, 45f + pupilY)) // Pupila inquieta
                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(35f + pupilX, 44f + pupilY))

                    // Ojo Derecho
                    drawOval(Color.White, Offset(50f, 45f - (12f * eyeScaleY)), androidx.compose.ui.geometry.Size(16f, 24f * eyeScaleY))
                    drawCircle(Color(0xFF05070A), 4.5f * eyeScaleY, Offset(60f + pupilX, 45f + pupilY)) // Pupila inquieta
                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(61f + pupilX, 44f + pupilY))
                }
            }
        }
    }
}

// ==========================================================================================
// --- PREVIEW ---
// ==========================================================================================
@Preview(showBackground = true, backgroundColor = 0xFF020408, widthDp = 360, heightDp = 800)
@Composable
fun LoadingBeAssistantPreview() {
    MyApplicationTheme {
        LoadingBeAssistantScreen(
            mainText = "Sincronizando...",
            subText = "Conectando con la red Maverick"
        )
    }
}
 **/