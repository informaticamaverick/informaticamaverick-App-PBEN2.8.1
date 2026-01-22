package com.example.myapplication.Client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.R
import androidx.compose.ui.window.DialogProperties


@OptIn(ExperimentalMaterial3Api::class)
data class Promotion(
    val id: Int,
    val imageUrl: String,
    val providerName: String,
    val description: String,
    val providerId: String,
    val rating: Float,
    val likes: Int,
    val isLiked: Boolean
)

@Composable
fun PromotionCard(
    promotion: Promotion,
    onMessageClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onImageClick: (Promotion) -> Unit,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(
                    model = promotion.imageUrl,
                    placeholder = painterResource(id = R.drawable.logo_app)
                ),
                contentDescription = "Imagen de promoción",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { onImageClick(promotion) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = promotion.providerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Esta llamada ahora usará la función de FunComunesIUClienteFalso.kt
               // RatingBar(rating = promotion.rating)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = promotion.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconToggleButton(
                            checked = promotion.isLiked,
                            onCheckedChange = { onLikeClick() }
                        ) {
                            Icon(
                                imageVector = if (promotion.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = if (promotion.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = promotion.likes.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onProfileClick(promotion.providerId) }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Ver perfil",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { onMessageClick(promotion.providerId) }) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = "Enviar mensaje",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionCardVertical(
    promotion: Promotion,
    onMessageClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onImageClick: (Promotion) -> Unit,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max) 
        ) {
            // Left part: Image
            Image(
                painter = rememberAsyncImagePainter(
                    model = promotion.imageUrl,
                    placeholder = painterResource(id = R.drawable.ic_google_logo)
                ),
                contentDescription = "Imagen de promoción",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f) 
                    .fillMaxHeight()
                    .clickable { onImageClick(promotion) }
            )

            // Right part: Descriptions and buttons
            Column(
                modifier = Modifier
                    .weight(1f) 
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = promotion.providerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                //RatingBar(rating = promotion.rating)

                Spacer(modifier = Modifier.height(8.dp))

                // This Text will take the available space, pushing the buttons to the bottom
                Text(
                    text = promotion.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconToggleButton(
                            checked = promotion.isLiked,
                            onCheckedChange = { onLikeClick() }
                        ) {
                            Icon(
                                imageVector = if (promotion.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = if (promotion.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = promotion.likes.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }

                    Row {
                        IconButton(onClick = { onProfileClick(promotion.providerId) }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Ver perfil",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { onMessageClick(promotion.providerId) }) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = "Enviar mensaje",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PromotionCardVerticalPreview() {
    val samplePromotion = Promotion(
        id = 1,
        imageUrl = "https://via.placeholder.com/200x400/FF0000/FFFFFF?text=Vertical",
        providerName = "Electricista Rápido",
        description = "Reparaciones eléctricas urgentes a domicilio. ¡Descuento del 15% esta semana!",
        providerId = "provider_123",
        rating = 4.5f,
        likes = 120,
        isLiked = false
    )
    MyApplicationTheme {
        PromotionCardVertical(
            promotion = samplePromotion,
            onMessageClick = {},
            onProfileClick = {},
            onImageClick = {},
            onLikeClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoScreen(
    onBack: () -> Unit,
    onMessageClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {}
) {
    var selectedPromotion by remember { mutableStateOf<Promotion?>(null) }

    val promotions = remember {
        mutableStateListOf(
            Promotion(1, "https://via.placeholder.com/200x400/FF6347/FFFFFF?text=Vertical+1", "Electricista Rápido", "Reparaciones eléctricas urgentes a domicilio. ¡Descuento del 15% esta semana!", "provider_123", 4.8f, 235, false),
            Promotion(2, "https://via.placeholder.com/400x200/4682B4/FFFFFF?text=Horizontal+2", "Plomería Total", "Soluciones de plomería para tu hogar. Instalaciones y reparaciones garantizadas.", "provider_456", 4.5f, 198, true),
            Promotion(3, "https://via.placeholder.com/200x400/32CD32/FFFFFF?text=Vertical+3", "Pinturas Brillantes", "Renueva tus espacios con nuestros servicios de pintura profesional.", "provider_789", 4.7f, 312, false),
            Promotion(4, "https://via.placeholder.com/400x200/FFD700/000000?text=Horizontal+4", "Limpieza Express", "Servicio de limpieza profunda para tu hogar o negocio.", "provider_101", 4.9f, 450, true),
            Promotion(5, "https://via.placeholder.com/200x400/6A5ACD/FFFFFF?text=Vertical+5", "Jardinería Creativa", "Diseño y mantenimiento de jardines. Dale vida a tus áreas verdes.", "provider_112", 4.6f, 280, false),
            Promotion(6, "https://via.placeholder.com/400x200/FF4500/FFFFFF?text=Horizontal+6", "Carpintería Moderna", "Muebles a medida y diseños exclusivos en madera. Calidad y estilo.", "provider_113", 4.8f, 321, true),
            Promotion(7, "https://via.placeholder.com/200x400/20B2AA/FFFFFF?text=Vertical+7", "Aire Acondicionado Polar", "Instalación y mantenimiento de sistemas de climatización. ¡Verano sin calor!", "provider_114", 4.9f, 510, false),
            Promotion(8, "https://via.placeholder.com/400x200/9932CC/FFFFFF?text=Horizontal+8", "Seguridad Total 24/7", "Instalación de cámaras y sistemas de alarma para tu tranquilidad.", "provider_115", 5.0f, 623, true),
            Promotion(9, "https://via.placeholder.com/200x400/8A2BE2/FFFFFF?text=Vertical+9", "Mudanzas Fáciles", "Servicio de mudanza completo y sin estrés. Embalaje y transporte.", "provider_116", 4.7f, 150, false),
            Promotion(10, "https://via.placeholder.com/400x200/5F9EA0/FFFFFF?text=Horizontal+10", "Remodelaciones & Diseño", "Transforma tu casa con nuestro equipo de arquitectos y diseñadores.", "provider_117", 4.9f, 489, true),
            Promotion(11, "https://via.placeholder.com/200x400/D2691E/FFFFFF?text=Vertical+11", "Tecno Soporte PC", "Reparación y mantenimiento de computadoras y laptops. Soluciones rápidas.", "provider_118", 4.8f, 290, false),
            Promotion(12, "https://via.placeholder.com/400x200/DC143C/FFFFFF?text=Horizontal+12", "Catering Delicioso", "El mejor sabor para tus eventos. Bodas, cumpleaños y reuniones.", "provider_119", 4.9f, 530, true),
            Promotion(13, "https://via.placeholder.com/200x400/00FFFF/000000?text=Vertical+13", "Lavado de Autos Premium", "Dejamos tu auto como nuevo. Lavado, pulido y encerado profesional.", "provider_120", 4.7f, 333, false),
            Promotion(14, "https://via.placeholder.com/400x200/B8860B/FFFFFF?text=Horizontal+14", "Asesoría Legal Confiable", "Abogados expertos a tu servicio. Consultas y representación legal.", "provider_121", 5.0f, 720, true)
        )
    }

    fun handleLikeClick(promotionId: Int) {
        val index = promotions.indexOfFirst { it.id == promotionId }
        if (index != -1) {
            val oldPromotion = promotions[index]
            val newLikes = if (oldPromotion.isLiked) oldPromotion.likes - 1 else oldPromotion.likes + 1
            promotions[index] = oldPromotion.copy(
                isLiked = !oldPromotion.isLiked,
                likes = newLikes
            )
        }
    }

    MyApplicationTheme {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Promociones") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { // <--- Acción conectada
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            },
            content = { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(promotions, key = { it.id }) { promotion ->
                        val onLike = { handleLikeClick(promotion.id) }
                        if (promotion.id % 2 != 0) {
                            PromotionCardVertical(
                                promotion = promotion,
                                onMessageClick = onMessageClick,
                                onProfileClick = onProfileClick,
                                onImageClick = { selectedPromotion = it },
                                onLikeClick = onLike
                            )
                        } else {
                            PromotionCard(
                                promotion = promotion,
                                onMessageClick = onMessageClick,
                                onProfileClick = onProfileClick,
                                onImageClick = { selectedPromotion = it },
                                onLikeClick = onLike
                            )
                        }
                    }
                }
            }
        )

        selectedPromotion?.let { promotion ->
            FullScreenPromotionView(
                promotion = promotion,
                onDismiss = { selectedPromotion = null },
                onMessageClick = onMessageClick,
                onProfileClick = onProfileClick,
                onLikeClick = { handleLikeClick(promotion.id) }
            )
        }
    }
}

@Composable
fun FullScreenPromotionView(
    promotion: Promotion,
    onDismiss: () -> Unit,
    onMessageClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onLikeClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Usa toda la pantalla
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() } // Cierra al hacer clic en cualquier lugar
        ) {
            // Imagen de fondo
            Image(
                painter = rememberAsyncImagePainter(
                    model = promotion.imageUrl,
                    placeholder = painterResource(id = R.drawable.ic_launcher_background)
                ),
                contentDescription = "Imagen de promoción a pantalla completa",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Capa semitransparente para legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 72.dp), // Padding aumentado
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = promotion.providerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                //RatingBar(rating = promotion.rating, starColor = Color.White)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = promotion.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconToggleButton(
                            checked = promotion.isLiked,
                            onCheckedChange = { onLikeClick() }
                        ) {
                            Icon(
                                imageVector = if (promotion.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = if (promotion.isLiked) Color(0xFFFFD700) else Color.White
                            )
                        }
                        Text(
                            text = promotion.likes.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)){
                        FloatingActionButton(
                            onClick = { onProfileClick(promotion.providerId) },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Ver perfil",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }

                        FloatingActionButton(
                            onClick = { onMessageClick(promotion.providerId) },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = "Enviar mensaje",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PromoScreenPreview() {
    PromoScreen(onBack = {}) // Llamar con el parámetro onBack
}
