package com.example.myapplication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.presentation.client.SuperCategory
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.draw.drawBehind

// ==========================================================================================
// ------------------------ NUEVA TARJETA CATEGORIA VERTICAL BENTO GLASS ---
// ==========================================================================================
@Composable
fun CompactCategoryCard(item: CategoryEntity, onClick: () -> Unit) {
    val baseColor = Color(item.color)
    var expandedBadge by remember { mutableStateOf<String?>(null) }
    var expandedInfoBadge by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(195.dp) // Altura equilibrada
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // --- 1. TARJETA BASE ---
        Card(
            shape = RoundedCornerShape(10.dp), // Esquinas profesionales
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxSize()
                //.padding(top = 4.dp) // Espacio exacto para badges flotantes
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // FONDO: Saturado Mate (Top) -> Negro Mate (Bottom)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    baseColor.copy(alpha = 1f),
                                    Color(0xFF080A0F) // Negro mate profundo
                                ),
                                startY = 100f,
                                endY = 550f
                            )
                        )
                )
                // CAPA DE DIFUMINADO SUPERIOR (Neblina de color)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.73f)
                        .blur(5.dp)
                        .background(baseColor.copy(alpha = 0.9f))
                )
                // EMOJI CENTRAL CON SOMBRA NATURAL (Solución a los "2 iconos")
                Text(
                    text = item.icon,
                    fontSize = 90.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-30).dp),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.99f), // Sombra más suave
                            offset = Offset(0f, 6f), // Offset reducido para evitar duplicados
                            //blurRadius = 9f // Blur aumentado para profundidad real
                        )
                    )
                )
                // SECCIÓN INFERIOR: Divider, Info Badge y Nombre
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 0.dp), // [MODIFICADO] 0.dp para que llegue al borde inferior
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Contenedor para el Divider y el Badge Informativo (Derecha centrado)
                    Box(
                        modifier = Modifier.fillMaxWidth().height(23.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.45f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 1.dp)
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(end = 0.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            CategoryBadgeItem(
                                icon = "ℹ️",
                                tooltip = "Información sobre esta categoría.",
                                isExpanded = expandedInfoBadge,
                                onToggle = { expandedInfoBadge = !expandedInfoBadge }
                            )
                            // --- 2. BADGES SUPERIORES (Centrados y tamaño correcto) ---
                            Row(
                                modifier = Modifier.align(Alignment.TopStart),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                if (item.isNew) CategoryBadgeItem("✨", "Esta es una NUEVA Categoria", expandedBadge == "new", { expandedBadge = if (expandedBadge == "new") null else "new" })
                                if (item.isNewPrestador) CategoryBadgeItem("🔔", "Esta categoria Tiene Nuevos Prestadores de Servicios para Vos", expandedBadge == "prestador", { expandedBadge = if (expandedBadge == "prestador") null else "prestador" })
                                if (item.isAd) CategoryBadgeItem("🔥", "Hay Promosiones Nuevas para ver Aquí", expandedBadge == "promo", { expandedBadge = if (expandedBadge == "promo") null else "promo" })
                                CategoryBadgeItem("🛍️", "En esta Categoria Hay Productos para comprar, depende del prestador", expandedBadge == "prod", { expandedBadge = if (expandedBadge == "prod") null else "prod" })
                                CategoryBadgeItem("🛠️", "En esta categoria Hay diferentes Prestadores de Servicios", expandedBadge == "serv", { expandedBadge = if (expandedBadge == "serv") null else "serv" })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // [MODIFICADO] Altura fija intacta, la lógica de auto-size va adentro
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Estados para controlar el tamaño y cuándo dibujar para evitar parpadeos
                        var textSize by remember { mutableStateOf(12.sp) }
                        var readyToDraw by remember { mutableStateOf(false) }

                        Text(
                            text = item.name.uppercase(),
                            color = Color.White,
                            fontSize = textSize, // Tamaño dinámico
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier
                                .fillMaxSize() // Abarcar todo el ancho y alto posible dentro de los 36.dp
                                .padding(horizontal = 1.dp) // Pequeño margen para no tocar los bordes físicos de la card
                                .drawWithContent {
                                    // Solo dibujamos el texto cuando ha terminado de calcular su tamaño
                                    if (readyToDraw) drawContent()
                                },
                            onTextLayout = { textLayoutResult ->
                                // Si desborda (visual) y el texto no es absurdamente chico, lo achicamos un 10%
                                if (textLayoutResult.hasVisualOverflow && textSize.value > 8f) {
                                    textSize = (textSize.value * 0.9f).sp
                                } else {
                                    // Si entra bien, lo dibujamos
                                    readyToDraw = true
                                }
                            }
                        )
                    }
                }
            }
        }

    }
}

/**
 * Componente interno: Icono Circular Estilo Dark
 */
@Composable
fun CategoryBadgeItem(icon: String, tooltip: String, isExpanded: Boolean, onToggle: () -> Unit) {
    Box {
        Surface(
            onClick = onToggle,
            modifier = Modifier.size(26.dp), // Tamaño reducido
            shape = CircleShape,
            color = Color(0xFF1A1F26).copy(alpha = 0.95f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 11.sp)
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = onToggle,
            modifier = Modifier
                .background(Color(0xFF0C0F14))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        ) {
            DropdownMenuItem(
                text = {
                    Text(text = tooltip, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                },
                onClick = onToggle
            )
        }
    }
}

// ==========================================================================================
// ------------------- TARJETA CATEGORIA HORIZONTAL--------------------------------
// ==========================================================================================
@Composable
fun CompactCategoryCardHorizontal(item: CategoryEntity, onClick: () -> Unit) {
    val baseColor = Color(item.color)
    var expandedBadge by remember { mutableStateOf<String?>(null) }
    var expandedInfoBadge by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // EFECTO OVERLAY (Brillo de cristal)
                Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f).drawWithCache {
                    val gradient = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    );
                    onDrawWithContent { drawContent(); drawRect(gradient, blendMode = BlendMode.Overlay) }
                })

                // DEGRADADO OSCURO
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.99f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            startX = 100f,
                            endX = 600f
                        )
                    )
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SECCIÓN IZQUIERDA (Texto)
                    Box(
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxHeight()
                            .padding(start = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text(
                                text = item.name.uppercase(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // DIVIDER INFERIOR DE TEXTO
                            Box(modifier = Modifier.width(30.dp).height(2.dp).background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                        }
                    }

                    // DIVIDER VERTICAL + BADGE INFORMATIVO CENTRADO
                    Box(
                        modifier = Modifier.fillMaxHeight().width(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Línea del Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(1f)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent)
                                    )
                                )
                        )
                        // Badge Informativo
                        Box(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                        ) {
                            CategoryBadgeItem(
                                icon = "ℹ️",
                                tooltip = "Detalles de la categoría.",
                                isExpanded = expandedInfoBadge,
                                onToggle = { expandedInfoBadge = !expandedInfoBadge }
                            )
                        }
                    }
                    // SECCIÓN DERECHA (Icono)
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.icon,
                            fontSize = 64.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    offset = Offset(0f, 8f),
                                    blurRadius = 15f
                                )
                            )
                        )
                    }
                }
            }
        }
        // BADGES SUPERIORES (Centrados a la izquierda)
        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (item.isNew) CategoryBadgeItem("✨", "Nueva", expandedBadge == "new", { expandedBadge = if (expandedBadge == "new") null else "new" })
            if (item.isNewPrestador) CategoryBadgeItem("🔔", "Novedades", expandedBadge == "prestador", { expandedBadge = if (expandedBadge == "prestador") null else "prestador" })
            if (item.isAd) CategoryBadgeItem("🔥", "Promos", expandedBadge == "promo", { expandedBadge = if (expandedBadge == "promo") null else "promo" })
        }
    }
}
// ==========================================================================================
// ------------------- TARJETA ESTILO BENTO PARA SUPERCATEGORÍAS (Extraída de HomeScreenCliente3)--------------------------------
// ==========================================================================================
@Composable
fun BentoSuperCategoryCard(
    superCategory: SuperCategory, 
    emoji: String, 
    height: Dp, 
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clickable(onClick = onClick)
            .shadow(12.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo con degradado
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(0.85f)
                        )
                    )
                ))
            // Iconos internos difuminados
            Box(modifier = Modifier
                .fillMaxSize()
                .blur(radius = 20.dp)
                .alpha(0.35f)) {
                LazyVerticalGrid(GridCells.Fixed(2), userScrollEnabled = false) {
                    items(items = superCategory.items, key = { it.name }) { item ->
                        Text(item.icon, fontSize = 55.sp, modifier = Modifier
                            .padding(8.dp)
                            .alpha(0.5f))
                    }
                }
            }
            // Info inferior
            Column(modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)) {
                Text(
                    text = superCategory.title, 
                    color = Color.White, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${superCategory.items.size} servicios", 
                    color = Color.White.copy(alpha = 0.7f), 
                    style = MaterialTheme.typography.labelMedium
                )
            }
           // Emoji identificador superior
            Text(emoji, fontSize = 44.sp, modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .alpha(0.85f))
        }
    }
}
//==========================================================================================
// ------------------- PREVIEWS--------------------------------
// ==========================================================================================
@Preview(showBackground = true)
@Composable
fun BentoSuperCategoryCardPreview() {
    val sampleCategories = listOf(
        CategoryEntity(
            name = "Limpieza", icon = "🧹", color = 0xFFFAD2E1L, superCategory = "Hogar",
            superCategoryIcon = "🏠", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false
        ),
        CategoryEntity(
            name = "Plomería", icon = "🪠", color = 0xFFBCAAA4L, superCategory = "Hogar",
            superCategoryIcon = "🏠", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false
        ),
        CategoryEntity(
            name = "Electricidad", icon = "⚡", color = 0xFFFFF59DL, superCategory = "Hogar",
            superCategoryIcon = "🏠", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false
        ),
        CategoryEntity(
            name = "Carpintería", icon = "🪚", color = 0xFFD7CCC8L, superCategory = "Hogar",
            superCategoryIcon = "🏠", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false
        )
    )
    val sampleSuperCat = SuperCategory(
        title = "Hogar y Construcción",
        icon = "🏠",
        items = sampleCategories
    )
    MyApplicationTheme {
        Box(modifier = Modifier
            .padding(16.dp)
            .width(300.dp)) {
            BentoSuperCategoryCard(
                superCategory = sampleSuperCat,
                emoji = sampleSuperCat.icon,
                height = 200.dp,
                onClick = {}
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun CompactCategoryCardhHorizontalPreview() {
    MyApplicationTheme {
        val sampleItem = CategoryEntity(
            name = "Peluquería",
            icon = "✂️",
            color = 0xFFF8BBD0,
            superCategory = "Cuidado Personal y Moda",
            isNew = true,
            isNewPrestador = true,
            isAd = true,
            imageUrl = null
        )
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            CompactCategoryCardHorizontal(
                item = sampleItem,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompactCategoryCardPreview() {
    val sampleItem = CategoryEntity(
        name = "Electricidad",
        icon = "⚡",
        color = 0xFFFFD54F,
        superCategory = "Hogar",
        isNew = true,
        isNewPrestador = true,
        isAd = true,
        imageUrl = null
    )
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp).width(160.dp)) {
            CompactCategoryCard(
                item = sampleItem,
                onClick = {}
            )
        }
    }
}
