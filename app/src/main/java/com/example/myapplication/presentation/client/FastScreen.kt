package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.presentation.components.geminiGradientBrush
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

/**
 * PANTALLA FAST
 * Simula la búsqueda urgente de prestadores con estética Cyber-Black.
 */
@Composable
fun FastScreen() {
    var isSearching by remember { mutableStateOf(false) }
    var providerFound by remember { mutableStateOf(false) }

    // Simulación de proceso de búsqueda
    LaunchedEffect(isSearching) {
        if (isSearching) {
            delay(4000) // 4 segundos de "radar"
            isSearching = false
            providerFound = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF05070A))) {

        // 1. FONDO: MAPA TÁCTICO MOCKUP
        TacticalMapBackground(isSearching)

        // 2. HUD SUPERIOR: UBICACIÓN
        TopLocationHud(modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp))

        // 3. CAPA DE BÚSQUEDA (OVERLAY)
        if (isSearching) {
            SearchingOverlay(onCancel = { isSearching = false })
        }

        // 4. BOTTOM SHEET / TARJETA DE RESULTADO
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            if (providerFound) {
                ProviderMatchCard(
                    onClose = { providerFound = false }
                )
            } else if (!isSearching) {
                FastSearchBottomSheet(onStartSearch = { isSearching = true })
            }
        }
    }
}

// ==========================================================================================
// --- COMPONENTES DE BÚSQUEDA Y MAPA ---
// ==========================================================================================

@Composable
fun TacticalMapBackground(isSearching: Boolean) {
    val gridColor = if (isSearching) Color(0xFF22D3EE).copy(0.05f) else Color(0xFF1A1F26)

    Box(modifier = Modifier.fillMaxSize().drawBehind {
        // Dibujamos una rejilla técnica
        val step = 40.dp.toPx()
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 1f)
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(gridColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f)
        }
    }) {
        // Radar Pulses en el centro
        Box(modifier = Modifier.align(Alignment.Center)) {
            RadarPulse(delay = 0)
            RadarPulse(delay = 1000)
            RadarPulse(delay = 2000)

            // Icono de posición del usuario
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFF22D3EE),
                border = BorderStroke(4.dp, Color(0xFF05070A)),
                shadowElevation = 15.dp
            ) {
                Icon(Icons.Default.Navigation, null, modifier = Modifier.padding(8.dp), tint = Color(0xFF05070A))
            }
        }
    }
}

@Composable
fun RadarPulse(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(3000, delayMillis = delay, easing = LinearEasing)),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3000, delayMillis = delay, easing = LinearEasing)),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(150.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(alpha)
            .border(2.dp, Color(0xFF22D3EE).copy(0.4f), CircleShape)
    )
}

// ==========================================================================================
// --- TARJETA BLACK GEMINI (PEDIDO ESPECÍFICO) ---
// ==========================================================================================

@Composable
fun ProviderMatchCard(onClose: () -> Unit) {
    // Gradientes definidos por el usuario
    val staticGemini = Brush.linearGradient(listOf(Color(0xFF2197F5), Color(0xFF9B51E0), Color(0xFFE91E63), Color(0xFF4285F4)))
    val blackGradient = Brush.verticalGradient(listOf(Color(0xFF1A1F26), Color(0xFF05070A)))
    val animatedGemini = geminiGradientBrush(isAnimated = true)

    Box(modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight()) {
        // BORDE GEMINI ESTÁTICO
        Box(modifier = Modifier.fillMaxWidth().background(staticGemini, RoundedCornerShape(28.dp)).padding(1.5.dp)) {
            // TARJETA BLACK CON GRADIENTE VERTICAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(modifier = Modifier.background(blackGradient).padding(20.dp)) {
                    // EMOJI SUPERPUESTO (Simulación con Text)
                    Text(
                        "👨‍🔧",
                        fontSize = 50.sp,
                        modifier = Modifier.align(Alignment.TopEnd).offset(y = (-45).dp, x = (-10).dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar con punto online
                        Box {
                            AsyncImage(
                                model = "https://i.pravatar.cc/150?u=carlos",
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.size(16.dp).offset(x = (-4).dp, y = (-4).dp).background(Color(0xFF00E676), CircleShape).border(2.dp, Color(0xFF0A0E14), CircleShape))
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Carlos Técnico", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Filled.Verified, null, tint = Color(0xFF9B51E0), modifier = Modifier.size(16.dp))
                            }
                            Text("Llegada en 5-10 min", color = Color(0xFF22D3EE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                Text(" 4.9 (120 servicios)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // BOTÓN ENVIAR CON EFECTO GEMINI ANIMADO
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(48.dp).background(animatedGemini, RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- UI OVERLAYS Y BOTTOM SHEET ---
// ==========================================================================================

@Composable
fun FastSearchBottomSheet(onStartSearch: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.Gray, CircleShape).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(20.dp))
            Text("Maverick FAST", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Selecciona el servicio de emergencia", color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(24.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(listOf("⚡" to "Eléctrico", "🔧" to "Plomero", "🚛" to "Flete", "🔑" to "Cerrajero")) { (emoji, label) ->
                    CategoryChip(emoji, label)
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onStartSearch,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))
            ) {
                Icon(Icons.Default.Bolt, null, tint = Color(0xFF05070A))
                Spacer(Modifier.width(8.dp))
                Text("SOLICITAR ASISTENCIA AHORA", color = Color(0xFF05070A), fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun CategoryChip(emoji: String, label: String) {
    Surface(
        modifier = Modifier.size(width = 100.dp, height = 110.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(0.03f),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun TopLocationHud(modifier: Modifier = Modifier) {
    val staticGemini = Brush.linearGradient(listOf(Color(0xFF2197F5), Color(0xFF9B51E0), Color(0xFFE91E63), Color(0xFF4285F4)))
    Box(modifier = modifier.fillMaxWidth(0.9f).background(staticGemini, RoundedCornerShape(28.dp)).padding(1.5.dp)) {
        Row(
            modifier = Modifier.background(Color(0xFF1A1F26), RoundedCornerShape(26.5.dp)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).background(Color(0xFF22D3EE).copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Map, null, tint = Color(0xFF22D3EE))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("TU UBICACIÓN ACTUAL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Barrio Matienzo, 1339", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
        }
    }
}

@Composable
fun SearchingOverlay(onCancel: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF05070A).copy(0.8f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                RadarPulse(0)
                Icon(Icons.Default.Search, null, modifier = Modifier.size(48.dp), tint = Color.White)
            }
            Spacer(Modifier.height(32.dp))
            Text("Buscando Prestador...", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("Maverick FAST está contactando profesionales", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(48.dp))
            TextButton(onClick = onCancel) {
                Text("CANCELAR BÚSQUEDA", color = Color.White.copy(0.6f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FastScreenPreview() {
    MyApplicationTheme {
        FastScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ProviderMatchCardPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF05070A)).padding(16.dp)) {
            ProviderMatchCard(onClose = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FastSearchBottomSheetPreview() {
    MyApplicationTheme {
        FastSearchBottomSheet(onStartSearch = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TopLocationHudPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.background(Color(0xFF05070A)).padding(16.dp)) {
            TopLocationHud()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchingOverlayPreview() {
    MyApplicationTheme {
        SearchingOverlay(onCancel = {})
    }
}