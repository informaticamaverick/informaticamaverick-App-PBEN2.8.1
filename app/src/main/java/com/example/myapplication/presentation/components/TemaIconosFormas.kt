package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

/**
 * COLORES Y GRADIENTES PREMIUM (ESTILO GLASSMORPHISM)
 */

/**
 * Colores temáticos ROG
 */
val ROG_Cyan = Color(0xFF00F0FF)
val ROG_Dark_Bg = Color(0xFF0D0D12)
val ROG_Text_Main = Color(0xFFE2E2E8)
val ROG_Glass_White = Color(0xFFFFFFFF).copy(alpha = 0.05f)
val BentoDarkGlassBackground = Color(0xFF12121A).copy(alpha = 0.65f)
val BentoDarkGlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)
val BottomSheetMatteColor = Color(0xFF0A0A0F).copy(alpha = 0.95f)
val GeminiAccent = Color(0xFFA78BFA)

// Color de acento por defecto estilo ROG (Cyan/Rojo)

// Gradiente para simular refracción de cristal
val BentoGlassBrush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.12f),
        Color.White.copy(alpha = 0.03f),
        Color.Black.copy(alpha = 0.3f)
    )
)

// Gradiente para bordes que brillan
val BentoBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.3f),
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.2f)
    )
)

/**
 * 1. MODIFICADOR SHAKE (EFECTO DE TEMBLOR)
 */
fun Modifier.shakeClick(
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    onClick: () -> Unit
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    clickable(
        interactionSource = interactionSource,
        indication = LocalIndication.current,
        onClick = {
            onClick()
            coroutineScope.launch {
                val animationSpec = tween<Float>(durationMillis = 50, easing = LinearEasing)
                offsetX.animateTo(12f, animationSpec)
                offsetX.animateTo(-12f, animationSpec)
                offsetX.animateTo(8f, animationSpec)
                offsetX.animateTo(-8f, animationSpec)
                offsetX.animateTo(0f, animationSpec)
            }
        }
    ).graphicsLayer {
        translationX = offsetX.value
    }
}

/**
 * 2. DIVISORES PREMIUM
 */
@Composable
fun PremiumHorizontalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun PremiumVerticalDivider(modifier: Modifier = Modifier, height: Dp = 32.dp) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun SectionHeaderWithDivider(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )
    }
}

/**
 * 3. CARCASA CIRCULAR DE ACCIÓN
 */
@Composable
fun CarcasaAccionBento(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.White,
    onClick: () -> Unit,
    size: Dp = 62.dp,
    emojiSize: TextUnit = 28.sp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(elevation = 16.dp, shape = CircleShape, ambientColor = accentColor, spotColor = accentColor)
                .clip(CircleShape)
                .background(BentoDarkGlassBackground)
                .background(BentoGlassBrush) // Capa de cristal
                .background(accentColor.copy(alpha = 0.1f)) // Tinte
                .border(1.5.dp, BentoBorderBrush, CircleShape)
                .shakeClick { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = emojiSize)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

object IconosAccion {
    @Composable fun Mensaje(onClick: () -> Unit) = CarcasaAccionBento("💬", "Mensaje", onClick = onClick, accentColor = Color(0xFF3B82F6))
    @Composable fun Cancelar(onClick: () -> Unit) = CarcasaAccionBento("❌", "Cancelar", onClick = onClick, accentColor = Color(0xFFEF4444))
    @Composable fun Aceptar(onClick: () -> Unit) = CarcasaAccionBento("✅", "Aceptar", onClick = onClick, accentColor = Color(0xFF22C55E))
    @Composable fun Editar(onClick: () -> Unit) = CarcasaAccionBento("✏️", "Editar", onClick = onClick, accentColor = Color(0xFFEAB308))
    @Composable fun Guardar(onClick: () -> Unit) = CarcasaAccionBento("💾", "Guardar", onClick = onClick, accentColor = Color(0xFF6366F1))
    @Composable fun Agregar(onClick: () -> Unit) = CarcasaAccionBento("➕", "Agregar", onClick = onClick, accentColor = Color.White)
}

/**
 * 4. BOTÓN DE ACCIÓN BENTO
 */
@Composable
fun BentoActionButton(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    color: Color = GeminiAccent,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(BentoDarkGlassBackground)
            .background(BentoGlassBrush)
            .background(color.copy(alpha = 0.15f))
            .border(1.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .shakeClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
            }

            //Text(text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
/**
 * 5. MENÚ PÍLDORA (PILL MENU)
 */
@Composable
fun BentoPillMenu(
    items: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(35.dp, RoundedCornerShape(50), ambientColor = Color.Black, spotColor = GeminiAccent)
            .clip(RoundedCornerShape(50))
            .background(BentoDarkGlassBackground)
            .background(BentoGlassBrush)
            .border(1.5.dp, BentoBorderBrush, RoundedCornerShape(50))
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            Text(
                text = item.first,
                fontSize = 24.sp,
                modifier = Modifier.shakeClick { item.second() }
            )
            if (index < items.lastIndex) {
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.White.copy(alpha = 0.2f)))
            }
        }
    }
}

/**
 * 6. TARJETAS PREMIUM
 */
@Composable
fun BentoCardPremium(
    title: String,
    modifier: Modifier = Modifier,
    headerEmoji: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black)
            .clip(RoundedCornerShape(28.dp))
            .background(BentoDarkGlassBackground)
            .background(BentoGlassBrush)
            .border(1.2.dp, BentoBorderBrush, RoundedCornerShape(28.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            SectionHeaderWithDivider(text = title, emoji = headerEmoji)
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun BentoCardPremium2(
    title: String,
    modifier: Modifier = Modifier,
    headerEmoji: String? = null,
    initialExpanded: Boolean = true,
    accentColor: Color = GeminiAccent,
    topCornerRadius: Dp = 8.dp, // Ligeramente más angulado para look Tech
    bottomCornerRadius: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }

    // Animación de rotación rápida y mecánica para la flecha
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "ArrowRotation"
    )

    // Padding bottom crucial para separar las tarjetas apiladas
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp)
    ) {
        // --- CABECERA ESTILO HUD EXTERNA ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)) // Ripple contenido
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 1.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (headerEmoji != null) {
                // EMOJI BADGE (Look geométrico/hardware)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = headerEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                // DIVISOR VERTICAL TECNOLÓGICO
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    accentColor.copy(alpha = 0.9f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.width(10.dp))
            }

            // TÍTULO HUD
            Text(
                text = title.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp // Tracking amplio ROG style
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            // LÍNEA DE DATOS HORIZONTAL
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // INDICADOR DE ESTADO (Flecha)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir/Colapsar",
                    tint = accentColor, // Usa el color de acento para indicar que es interactivo
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(arrowRotation)
                )
            }
        }

        // --- CONTENIDO EXPANDIBLE (Cuerpo de Cristal/Tech) ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(250)) + fadeIn(tween(200)),
            exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(tween(200))
        ) {
            val cardShape = RoundedCornerShape(
                topStart = topCornerRadius,
                topEnd = topCornerRadius,
                bottomStart = bottomCornerRadius,
                bottomEnd = bottomCornerRadius
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp)
                    // Sombra pronunciada para despegarlo del fondo negro mate
                    .shadow(
                        elevation = 16.dp,
                        shape = cardShape,
                        ambientColor = Color.Black,
                        spotColor = accentColor.copy(alpha = 0.15f) // Glow sutil del color de acento
                    )
                    .clip(cardShape)
                    // 1. FONDO GRIS CLARO TRANSLÚCIDO (Efecto frosted/aluminio oscuro)
                    .background(Color(0xFF8A8A9E).copy(alpha = 0.12f))
                    // 2. BORDE PREMIUM DE CONTRASTE (Distintivo entre tarjetas)
/**
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f), // Brillo superior
                                Color.White.copy(alpha = 0.02f)  // Sombra inferior
                            )
                        ),
                        shape = cardShape
                    )
**/
            ) {

                /**
                // 3. CAPA DE RUIDO/BLUR INTERNO (Le da textura al cristal)
                if (headerEmoji != null) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(radius = 65.dp) // Blur muy fuerte para esparcir el color
                            .alpha(0.15f) // Opacidad controlada
                    ) {
                        Text(
                            text = headerEmoji,
                            fontSize = 280.sp, // Emoji gigante para crear manchas de color
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = 40.dp, y = 40.dp)
                        )
                    }
                }
**/
                Column(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * 7. CAJA DE TEXTO EDICION M3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoTextFieldM3(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    emoji: String? = null,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        placeholder = { Text(placeholder) },
        leadingIcon = if (emoji != null) {
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(text = emoji, fontSize = 20.sp)

                    Spacer(modifier = Modifier.width(12.dp))

                    // --- DIVISOR VERTICAL CON GRADIENTE ---
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0f),
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0f)
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        } else null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.Black.copy(alpha = 0.4f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
            focusedBorderColor = GeminiAccent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = GeminiAccent,
            unfocusedLabelColor = Color.Gray,
            cursorColor = GeminiAccent
        )
    )
}

/**
 * Componente BentoDisplayFieldM3
 * Rediseñado con estética de Material Design 3 (Google Pixel / ROG Style) y estilo Bento Premium.
 * Presenta un icono a la izquierda centrado, un divisor vertical y luego el contenido.
 *
 * @param label Descripción del dato (ej: "Nombre Completo").
 * @param value El texto o dato a mostrar.
 * @param emoji Icono representativo opcional.
 * @param accentColor Color para la etiqueta (por defecto GeminiAccent).
 */

@Composable
fun BentoDisplayFieldM3(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    supportingText: String? = null,
    accentColor: Color = Color(0xFF00FFFF),
    containerColor: Color = Color.Black.copy(alpha = 0.2f), // 1. Cambiado a semi-transparente por defecto
    borderColor: Color = Color.Transparent,    // 2. Bordes transparentes
    cornerRadius: Dp = 12.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            // --- CUERPO PRINCIPAL ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    // Eliminamos el drawWithContent que dibujaba los bordes laterales y superior
                    .background(containerColor, RoundedCornerShape(cornerRadius))
            ) {
                // CONTENIDO INTERNO (Emoji + Valor)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 2.dp, end = 4.dp, bottom = 8.dp), // Reducido para que alinee con el borde
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (emoji != null) {
                        Text(text = emoji, fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 2.dp) )// Ajuste fino para el emoji)
                        // Divisor vertical sutil
                        // DIVISOR VERTICAL TECNOLÓGICO
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            accentColor.copy(alpha = 0.7f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                   }

                    Text(
                        text = value,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 8.dp,end = 4.dp, bottom = 1.dp) // Ajuste fino para el texto
                    )
                }

                // --- 2. EL INDICADOR INFERIOR (Único borde visible) ---
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth() // Ocupa todo el ancho inferior
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.8f),
                                    accentColor.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // --- 3. EL LABEL (FLOTANTE SOBRE LA LÍNEA INVISIBLE) ---
            // Lo movemos un poco para que parezca que flota sobre el campo
            Text(
                text = label.uppercase(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = (-10).dp, x = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accentColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}


/**
@Composable
fun BentoDisplayFieldM3(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    supportingText: String? = null,
    accentColor: Color = Color(0xFF00FFFF),
    // Gris traslúcido sólido (sin degradado)
    //containerColor: Color = Color.White.copy(alpha = 0.08f),
    containerColor: Color = Color.Black.copy(alpha = 0.2f),
    borderColor: Color = Color.White.copy(alpha = 0.3f),
    cornerRadius: Dp = 12.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp) // Espacio para el label que sobresale
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            // 1. CUERPO PRINCIPAL CON BORDE RECORTADO (NOTCH)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    // Dibujamos el borde manualmente para dejar el hueco del label
                    .drawWithContent {
                        drawContent() // Dibuja el fondo (background) primero

                        val strokeWidth = 1.dp.toPx()
                        val cornerRadiusPx = cornerRadius.toPx()

                        // Calculamos el espacio que ocupa el texto (aproximado)
                        // label.length * factor de ancho de fuente
                        val gapWidth = (label.length * 7.sp.toPx()) + 16.dp.toPx()
                        val gapStart = 12.dp.toPx()

                        val path = Path().apply {
                            // Empezamos después del hueco del label
                            moveTo(gapStart + gapWidth, 0f)
                            lineTo(size.width - cornerRadiusPx, 0f)
                            // Esquina superior derecha
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(
                                    size.width - cornerRadiusPx * 2, 0f, size.width, cornerRadiusPx * 2
                                ),
                                startAngleDegrees = 270f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(size.width, size.height - cornerRadiusPx)
                            // Esquina inferior derecha
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(
                                    size.width - cornerRadiusPx * 2, size.height - cornerRadiusPx * 2, size.width, size.height
                                ),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(cornerRadiusPx, size.height)
                            // Esquina inferior izquierda
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(
                                    0f, size.height - cornerRadiusPx * 2, cornerRadiusPx * 2, size.height
                                ),
                                startAngleDegrees = 90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(0f, cornerRadiusPx)
                            // Esquina superior izquierda
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(
                                    0f, 0f, cornerRadiusPx * 2, cornerRadiusPx * 2
                                ),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(gapStart, 0f)
                        }

                        drawPath(
                            path = path,
                            color = borderColor,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                    .background(containerColor, RoundedCornerShape(cornerRadius))
            ) {
                // CONTENIDO INTERNO (Emoji + Valor)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (emoji != null) {
                        Text(text = emoji, fontSize = 18.sp)
                        // Divisor vertical sutil
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.White.copy(alpha = 0.2f))
                        )
                    }

                    Text(
                        text = value,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // 2. INDICADOR INFERIOR (GLOW EFFECT)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.8f) // Brillo centrado al 70%
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    accentColor,
                                    accentColor,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // 3. EL LABEL (FLOTANTE Y TRANSPARENTE)
            Text(
                text = label.uppercase(),
                color = accentColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier
                    .padding(start = 18.dp) // Alineado con el hueco del borde
                    .offset(y = (-28).dp) // Posición exacta sobre el borde
            )
        }

        // 4. SUPPORTING TEXT
        if (supportingText != null) {
            Text(
                text = supportingText,
                color = Color.White.copy(alpha = 0.4f),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(top = 6.dp, start = 12.dp)
            )
        }
    }
}
**/


/**
 * 8. BOTTOM SHEET MATTE NEGRO REFORMATEADO
 */
@Composable
fun BentoBottomSheetContent(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String = "✨",
    showPrimaryButton: Boolean = false,
    primaryButtonText: String = "Aceptar",
    primaryButtonEmoji: String = "✅",
    primaryButtonColor: Color = Color(0xFF22C55E),
    onPrimaryButtonClick: () -> Unit = {},
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // --- ESTADOS DE ANIMACIÓN SHAKE (Sacudida) ---
    val shakeOffsetBtn = remember { Animatable(0f) }
    val shakeOffsetClose = remember { Animatable(0f) }

    // Función suspendida para ejecutar la sacudida
    suspend fun triggerShake(animatable: Animatable<Float, AnimationVector1D>) {
        repeat(4) {
            animatable.animateTo(10f, tween(50, easing = LinearEasing))
            animatable.animateTo(-10f, tween(50, easing = LinearEasing))
        }
        animatable.animateTo(0f, tween(50, easing = LinearEasing))
    }

    // Animación de entrada para el contenido (Fade In + Slide)
    val contentAlpha = remember { Animatable(0f) }
    val translateY = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { contentAlpha.animateTo(1f, tween(600)) }
        launch { translateY.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // --- CUERPO DEL SHEET ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .shadow(40.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFF0F0F12)) // Matte Negro
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp)
        ) {
            // Handle (Tirador decorativo superior)
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- CABECERA (Emoji + Divider + Título) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 22.sp)

                Spacer(modifier = Modifier.width(12.dp))

                // Divisor Vertical Premium
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Divisor Horizontal que se desvanece
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- CONTENIDO CENTRAL (Animado) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = contentAlpha.value
                        translationY = translateY.value
                    }
            ) {
                content()
            }

            // Botón de acción primario con efecto Shake
            if (showPrimaryButton) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.offset(x = shakeOffsetBtn.value.dp)) {
                    BentoActionButton(
                        text = primaryButtonText,
                        emoji = primaryButtonEmoji,
                        color = primaryButtonColor,
                        onClick = {
                            coroutineScope.launch {
                                triggerShake(shakeOffsetBtn)
                                onPrimaryButtonClick()
                            }
                        }
                    )
                }
            }

            // Contenido inferior (opcional)
            bottomContent?.let {
                Spacer(modifier = Modifier.height(16.dp))
                it()
            }
        }

        // --- BOTÓN DE CERRAR FLOTANTE (Con Shake) ---
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
                .offset(y = 10.dp, x = shakeOffsetClose.value.dp)
        ) {
            CarcasaAccionBento(
                emoji = "❌",
                label = "",
                accentColor = Color(0xFFEF4444),
                onClick = {
                    coroutineScope.launch {
                        triggerShake(shakeOffsetClose)
                        kotlinx.coroutines.delay(250) // Espera pequeña para que se aprecie la vibración
                        onClose()
                    }
                },
                size = 42.dp,
                emojiSize = 20.sp
            )
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewPremiumHorizontalDivider() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF1A1A2E)).padding(20.dp)) {
            PremiumHorizontalDivider()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewSectionHeaderWithDivider() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF1A1A2E)).padding(20.dp)) {
            SectionHeaderWithDivider(text = "Configuración", emoji = "⚙️")
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewIconosAccionGallery() {
    MyApplicationTheme {
        Column(
            modifier = Modifier.background(Color(0xFF1A1A2E)).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row {
                IconosAccion.Aceptar {}
                IconosAccion.Cancelar {}
                IconosAccion.Mensaje {}
            }
            Row {
                IconosAccion.Editar {}
                IconosAccion.Guardar {}
                IconosAccion.Agregar {}
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewBentoPillMenu() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF1A1A2E)).padding(20.dp), contentAlignment = Alignment.Center) {
            BentoPillMenu(
                items = listOf(
                    "🏠" to {},
                    "🔍" to {},
                    "🔔" to {},
                    "👤" to {}
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewBentoCardPremium() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF1A1A2E)).padding(20.dp)) {
            BentoCardPremium(title = "Mi Perfil", headerEmoji = "👤") {
                Text(
                    text = "Este es un ejemplo de contenido dentro de una BentoCardPremium con efecto glassmorphism.",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewBentoActionButton() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF1A1A2E)).padding(20.dp)) {
            BentoActionButton(text = "Enviar Mensaje", emoji = "📩", color = GeminiAccent) {}
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewBentoTextFieldM3() {
    MyApplicationTheme {
        var text by remember { mutableStateOf("") }
        Box(modifier = Modifier.background(Color(0xFF1A1A2E)).padding(20.dp)) {
            BentoTextFieldM3(
                value = text,
                onValueChange = { text = it },
                label = "Correo electrónico",
                placeholder = "ejemplo@correo.com",
                emoji = "📧"
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewBentoDisplayFieldM3() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF1A1A2E)).padding(20.dp)) {
            BentoDisplayFieldM3(
                label = "Dirección de Residencia",
                value = "Av. Siempre Viva 742, Springfield",
                emoji = "📍"
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewBentoCardPremium2() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF1A1A2E)).padding(20.dp)) {
            BentoCardPremium2(
                title = "Configuración Avanzada",
                headerEmoji = "🛠️",
                initialExpanded = true
            ) {
                Text(
                    text = "Este es el contenido expandible de BentoCardPremium2 con animaciones suaves y diseño glassmorphism.",
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                BentoActionButton(text = "Acción Primaria", emoji = "⚡", color = GeminiAccent) {}
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
fun PreviewBentoBottomSheetContent() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF1A1A2E)).padding(16.dp)) {
            BentoBottomSheetContent(
                title = "Opciones Avanzadas",
                emoji = "🚀",
                onClose = {},
                showPrimaryButton = true,
                onPrimaryButtonClick = {}
            ) {
                Text(
                    text = "Este es un ejemplo de contenido dentro de un BentoBottomSheetContent con botón de confirmación integrado.",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}