package com.example.myapplication.Client

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

data class PromotionItem(
    val id: Int,
    val title: String,
    val description: String,
    val isNew: Boolean = false,
    val expiresInHours: Int? = null
)

val samplePromotions = listOf(
    PromotionItem(1, "20% Descuento en Plomería", "Válido para reparaciones de emergencia.", isNew = true, expiresInHours = 48),
    PromotionItem(2, "Limpieza de Hogar a $500/hr", "Oferta de tiempo limitado."),
    PromotionItem(3, "Instalación de AC con 15% Off", "Prepárate para el verano.", isNew = true),
    PromotionItem(4, "Revisión Eléctrica Gratuita", "Al contratar cualquier servicio eléctrico.", expiresInHours = 24),
    PromotionItem(5, "Pintura de Interiores", "Renueva tu espacio con un 10% de descuento.")
)

data class StoryPromotion(
    val id: Int,
    val providerName: String,
    val iconColor: Color
)

val sampleStories = listOf(
    StoryPromotion(1, "Plomería Veloz", Color(0xFF3B82F6)),
    StoryPromotion(2, "Electricistas 24/7", Color(0xFFFBBF24)),
    StoryPromotion(3, "Limpieza Total", Color(0xFF10B981)),
    StoryPromotion(4, "Pintores Pro", Color(0xFF8B5CF6)),
    StoryPromotion(5, "Jardinería Fresh", Color(0xFFEF4444))
)

@Composable
fun PromotionsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Stories Section
        item {
            StoriesSection()
        }

        // Hero Banner
        item {
            HeroBanner()
        }

        // Gamification Card
        item {
            ScratchCardSection()
        }
        
        // Limited Time Offers
        item {
            Column {
                Text(
                    text = "¡Rápido, que se acaban!",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                samplePromotions.filter { it.expiresInHours != null }.forEach { promotion ->
                    PromotionCard(promotion = promotion)
                }
            }
        }

        // Promotions Section
        item {
            Text(
                text = "Más ofertas para ti",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
                fontWeight = FontWeight.Bold
            )
        }

        items(samplePromotions.filter { it.expiresInHours == null }) { promotion ->
            PromotionCard(promotion = promotion)
        }
    }
}

@Composable
fun StoriesSection() {
    Column {
        Text(
            text = "Ofertas Flash",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleStories) { story ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(story.iconColor)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = story.providerName, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "¡Ofertas de Verano!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Descuentos exclusivos en servicios seleccionados",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun ScratchCardSection() {
    var revealed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(Unit) {
                    detectDragGestures { _, _ -> revealed = true }
                },
            contentAlignment = Alignment.Center
        ) {
            if (revealed) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¡GANASTE!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text("10% EXTRA", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("RASCA AQUÍ", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
fun PromotionCard(promotion: PromotionItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = promotion.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (promotion.isNew) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NUEVO",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = promotion.description,
                style = MaterialTheme.typography.bodyMedium
            )
            promotion.expiresInHours?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Termina en: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$it horas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PromotionsScreenPreview() {
    MyApplicationTheme {
        PromotionsScreen()
    }
}
