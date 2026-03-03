package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.client.BeBrainViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Componente de texto que reduce automáticamente su tamaño si es demasiado largo
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.titleLarge
) {
    var multiplier by remember { mutableFloatStateOf(1f) }

    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style.copy(fontSize = style.fontSize * multiplier),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && multiplier > 0.5f) {
                multiplier *= 0.9f
            }
        }
    )
}

/**
 * Pantalla de resultados inteligente de Be.
 * Replica el diseño de SuperCategoryDetailsPanel para consistencia visual.
 */
@Composable
fun BeResultadoScreen(
    viewModel: BeBrainViewModel,
    onClose: () -> Unit,
    onProviderClick: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isVisible by viewModel.isResultadoVisible.collectAsStateWithLifecycle()

    // 🔥 Ciclo de Vida: Replicamos la lógica de SuperCategoryDetailsPanel de HomeScreenCliente3
    DisposableEffect(isVisible) {
        if (isVisible) {
            viewModel.setSearchActive(true)
            viewModel.setBottomBarVisible(false)
        }
        onDispose {
            // No reseteamos búsqueda aquí para permitir que el usuario vea lo que escribió si reabre
        }
    }

    BeResultadoContent(
        searchQuery = searchQuery,
        isVisible = isVisible,
        onClose = onClose,
        onProviderClick = onProviderClick
    )
}

/**
 * Contenido de la pantalla de resultados de Be, extraído para permitir Previews y separar lógica de ViewModel.
 */
@Composable
fun BeResultadoContent(
    searchQuery: String,
    isVisible: Boolean,
    onClose: () -> Unit,
    onProviderClick: (String) -> Unit
) {
    val cyberBackground = Color(0xFF0A0E14)
    val textMain = Color(0xFFE2E8F0)
    val textMuted = Color(0xFF94A3B8)

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrim de fondo para el overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                //.background(Color.Transparent)
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClose() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.78f) // 🔥 Altura perfecta según SuperCategoryDetailsPanel
                    .clickable(enabled = false) { }, // Evita que clicks en el panel cierren el overlay
                color = cyberBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                tonalElevation = 16.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Borde brillante superior (Efecto Gemini)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(geminiGradientBrush(isAnimated = true))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        // Header: Título y botón de Cerrar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                AutoSizeText(
                                    text = if (searchQuery.isEmpty()) "Análisis de Be" else "Resultados para: $searchQuery",
                                    color = textMain,
                                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                )
                                Text(
                                    text = "Inteligencia Maverick en acción ✨",
                                    style = TextStyle(
                                        brush = geminiGradientBrush(isAnimated = false),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Botón 'X'
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                    .clickable { onClose() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Lista de Resultados
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item {
                                SmartResultCard(
                                    title = "Búsqueda predictiva",
                                    description = "Encuentra lo que necesitas escribiendo arriba o hablando con Be.",
                                    icon = Icons.Default.Star,
                                    badgeText = "TIP"
                                )
                            }
                            item {
                                SmartResultCard(
                                    title = "Sugerencias inteligentes",
                                    description = "Be analiza los mejores prestadores cercanos a tu ubicación.",
                                    icon = Icons.Default.Settings
                                )
                            }
                            // Aquí se inyectarían los resultados reales del ViewModel
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de resultado individual (Smart Card).
 */
@Composable
fun SmartResultCard(
    title: String,
    description: String,
    icon: ImageVector,
    badgeText: String? = null,
    opacity: Float = 1f
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.03f * opacity),
                        Color.White.copy(alpha = 0.01f * opacity)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.05f * opacity), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2197F5).copy(alpha = 0.1f * opacity)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF2197F5).copy(alpha = opacity),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.padding(end = if (badgeText != null) 60.dp else 0.dp)) {
                Text(
                    text = title,
                    color = Color(0xFFE2E8F0).copy(alpha = opacity),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color(0xFF94A3B8).copy(alpha = opacity),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF9B51E0).copy(alpha = 0.2f * opacity))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color(0xFFD8B4FE).copy(alpha = opacity),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BeResultadoContentPreview() {
    MyApplicationTheme(darkTheme = true) {
        BeResultadoContent(
            searchQuery = "Soporte Técnico",
            isVisible = true,
            onClose = {},
            onProviderClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SmartResultCardPreview() {
    MyApplicationTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp).background(Color(0xFF0A0E14))) {
            SmartResultCard(
                title = "Resultado de Ejemplo",
                description = "Esta es una descripción detallada de un resultado inteligente de Be.",
                icon = Icons.Default.Star,
                badgeText = "NUEVO"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AutoSizeTextPreview() {
    MyApplicationTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp).background(Color(0xFF0A0E14))) {
            AutoSizeText(
                text = "Texto corto",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            AutoSizeText(
                text = "Este es un texto extremadamente largo para probar la funcionalidad de auto size",
                color = Color.White,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}
