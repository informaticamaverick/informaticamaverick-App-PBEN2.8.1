package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.text.Normalizer
import java.util.Locale
import kotlin.math.absoluteValue

// ==========================================================================================
// --- SECCIÓN 1: UTILIDADES Y EXTENSIONES ---
// ==========================================================================================
/**
 * Función para limpiar textos, arreglar mayúsculas/minúsculas y opcionalmente quitar acentos.
 */
fun String.formatearTexto(quitarAcentos: Boolean = false): String {
    if (this.isBlank()) return this

    // Convierte a minúsculas y capitaliza la primera letra
    val textoFormateado = this.lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
    // Si se requiere, elimina acentos y diacríticos
    return if (quitarAcentos) {
        val normalizado = Normalizer.normalize(textoFormateado, Normalizer.Form.NFD)
        "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalizado, "")
    } else {
        textoFormateado
    }
}
// ==========================================================================================
// --- SECCIÓN 2: CONFIGURACIÓN VISUAL (GEMINI CYBERPUNK) ---
// ==========================================================================================
/**
 * Función unificada para generar el gradiente animado de Gemini.
 * Combina la paleta de 4 colores con un desplazamiento fluido.
 * * @param isAnimated Controla si el gradiente debe desplazarse automáticamente.
 */
@Composable
fun geminiGradientBrush(isAnimated: Boolean = true): Brush {
    // Paleta completa de colores de Gemini
    val colors = listOf(
        Color(0xFF2197F5), // Azul
        Color(0xFF9B51E0), // Púrpura
        Color(0xFFE91E63), // Rosa
        Color(0xFF4285F4)  // Azul Google (Acento)
    )

    val offset = if (isAnimated) {
        val infiniteTransition = rememberInfiniteTransition(label = "geminiAnim")
        val animatedValue by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2000f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        )
        animatedValue
    } else {
        0f
    }

    return Brush.linearGradient(
        colors = colors,
        // Se ajustan los offsets para que el degradado cubra bien el área y se mueva suavemente
        start = Offset(offset - 1000f, offset - 1000f),
        end = Offset(offset, offset ),
        tileMode = TileMode.Mirror
    )
}
// ==========================================================================================
// --- SECCIÓN 3: COMPONENTES BÁSICOS (UI ATOMS) ---
// ==========================================================================================

/**
 * Componente que muestra una etiqueta con el nombre de la categoría, su color e ícono.
 */
@Composable
fun ServiceTag(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!icon.isNullOrEmpty()) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                text = text.formatearTexto(),
                style = MaterialTheme.typography.labelSmall,
                color = if (color.luminance() > 0.4f) Color.Black else Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Sobrecarga de ServiceTag que acepta una CategoryEntity completa.
 */
@Composable
fun ServiceTag(category: CategoryEntity, modifier: Modifier = Modifier) {
    ServiceTag(
        text = category.name,
        color = Color(category.color),
        icon = category.icon,
        modifier = modifier
    )
}
@Composable
fun AutoResizingText(
    text: String,
    color: Color,
    maxFontSize: TextUnit,
    minFontSize: TextUnit = 8.sp,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default
) {
    var fontSizeValue by remember(text) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    // Aplicamos formato al texto recibido
    val textoLimpio = text.formatearTexto()

    Text(
        text = textoLimpio,
        color = color,
        fontWeight = FontWeight.Black,
        fontSize = fontSizeValue,
        lineHeight = fontSizeValue * 1.1f,
        maxLines = 2,
        letterSpacing = 1.1.sp,
        overflow = TextOverflow.Ellipsis,
        style = style,
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
                if (readyToDraw) drawContent()
            },
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSizeValue > minFontSize) {
                fontSizeValue *= 0.9f
            } else {
                readyToDraw = true
            }
        }
    )
}

// ==========================================================================================
// --- SECCIÓN 4: BANNERS Y CARRUSELES ---
// ==========================================================================================

enum class BannerType(val label: String) {
    GOOGLE_AD("SPONSORED"),
    PROMO("PROMOCIÓN"),
    NEW_CATEGORY("NUEVA CATEGORÍA"),
    NEW_PROVIDER("NUEVOS PRESTADORES"),
    PRODUCT_SALE("VENTA DE PRODUCTO"),
    SERVICE_SALE("SERVICIO DESTACADO")
}

data class AccordionBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color,
    val type: BannerType,
    val originalCategory: CategoryEntity? = null,
    val isNew: Boolean = false,
    val imageUrl: String? = null,
    val discount: Int? = null,
    val provider: Provider? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumLensCarousel(
    items: List<AccordionBanner>,
    isPaused: Boolean = false, // 🔥 AGREGA ESTA LÍNEA
    onSettingsClick: () -> Unit,
    onItemClick: (AccordionBanner) -> Unit,
    modifier: Modifier = Modifier,
    autoplayDelay: Long = 2000L
) {
    // Si no hay ítems, no renderizamos nada
    if (items.isEmpty()) return

    // Estados para controlar el menú de filtros y los filtros activos
    var expandedMenu by remember { mutableStateOf(false) }
    var activeFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var tempFilters by remember { mutableStateOf<Set<String>>(emptySet()) }

    // --- ANIMACIÓN DE ROTACIÓN PARA EL ENGRANAJE ---
    // Definimos una animación para que el engranaje gire 360 grados cuando el menú se expande.
    val gearRotation by animateFloatAsState(
        targetValue = if (expandedMenu) 360f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gearRotation"
    )

    // Lógica de filtrado base: Mantiene anuncios y filtra contenido según selección
    val baseFilteredItems = remember(items, activeFilters) {
        if (activeFilters.isEmpty()) items
        else items.filter {
            // Los anuncios de Google siempre se mantienen para inyectarlos luego con frecuencia
            if (it.type == BannerType.GOOGLE_AD) true
            else {
                val isNovedad = it.type == BannerType.NEW_CATEGORY || it.type == BannerType.NEW_PROVIDER
                val isPromo = it.type == BannerType.PROMO || it.discount != null
                val isProd = it.type == BannerType.PRODUCT_SALE
                val isServ = it.type == BannerType.SERVICE_SALE

                (activeFilters.contains("NOVEDADES") && isNovedad) ||
                        (activeFilters.contains("PROMOCIONES") && isPromo) ||
                        (activeFilters.contains("PRODUCTOS") && isProd) ||
                        (activeFilters.contains("SERVICIOS") && isServ)
            }
        }
    }

    // Aumentamos la frecuencia de anuncios de Google inyectándolos cada 2 ítems de contenido real
    val displayItems = remember(baseFilteredItems, items) {
        val ads = items.filter { it.type == BannerType.GOOGLE_AD }
        val contentOnly = baseFilteredItems.filter { it.type != BannerType.GOOGLE_AD }
        
        if (ads.isEmpty() || contentOnly.isEmpty()) {
            baseFilteredItems.ifEmpty { items }
        } else {
            val result = mutableListOf<AccordionBanner>()
            var adCounter = 0
            contentOnly.forEachIndexed { index, item ->
                result.add(item)
                // Inyectar publicidad cada 2 elementos de contenido para aumentar visibilidad
                if ((index + 1) % 2 == 0) {
                    result.add(ads[adCounter % ads.size])
                    adCounter++
                }
            }
            // Si la lista procesada es válida, la usamos
            if (result.isNotEmpty()) result else baseFilteredItems
        }
    }

    // Configuración del Pager Infinito
    // Usamos Int.MAX_VALUE para que el carrusel nunca acabe (vuelve a comenzar cíclicamente)
    val infiniteCount = Int.MAX_VALUE
    val initialPage = infiniteCount / 2 - (infiniteCount / 2 % displayItems.size.coerceAtLeast(1))
    val pagerState = rememberPagerState(initialPage = initialPage) { infiniteCount }

    // 🔥 SOLUCIÓN: Unificamos los efectos en uno solo que respete isPaused
    LaunchedEffect(isPaused, displayItems) {
        // Solo iniciamos el bucle si NO está pausado y hay más de un ítem
        if (!isPaused && displayItems.size > 1) {
            while (true) {
                delay(autoplayDelay)
                // Verificamos de nuevo isPaused por si cambió durante el delay
                 if (!isPaused) {
                  pagerState.animateScrollToPage(pagerState.currentPage + 1)
             }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Cabecera con título y botones de acción (Filtros y Limpieza)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 3.dp)
                .animateContentSize( // 🔥 Esto anima el crecimiento/achique del divider
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- ESTRUCTURA: TEXTO + DIVISOR DINÁMICO ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 1.dp)
            ) {
                Text(
                    text = "DESTACADOS & NOVEDADES",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 0.5.dp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // --- CONTENEDOR DE BOTONES DE ACCIÓN ---
            Row(verticalAlignment = Alignment.CenterVertically) {

                // 🔥 ANIMACIÓN: El botón X brota y se esconde detrás del engranaje
                AnimatedVisibility(
                    visible = activeFilters.isNotEmpty(),
                    enter = fadeIn(tween(400)) +
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            ),
                    exit = fadeOut(tween(300)) +
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = {
                                activeFilters = emptySet()
                                tempFilters = emptySet()
                            },
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color(0xFF1A1F26),
                            border = BorderStroke(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.White.copy(0.7f), Color.Transparent)
                                )
                            ),
                            shadowElevation = 6.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFFEF4444).copy(0.15f), Color.Transparent)
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar Filtros",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                // Botón Engranaje
                Box {
                    Surface(
                        onClick = {
                            tempFilters = activeFilters
                            expandedMenu = !expandedMenu
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer(rotationZ = gearRotation),
                        shape = CircleShape,
                        color = Color(0xFF1A1F26),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White.copy(0.7f), Color.Transparent)
                            )
                        ),
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = "⚙️", fontSize = 16.sp)
                        }
                    }

                    // DropdownMenu sigue aquí debajo...
                    // Menú desplegable premium
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        offset = DpOffset(x = (0).dp, y = 0.dp),
                        modifier = Modifier
                            .width(220.dp) // Ensanchado un poco para que entren ambos botones cómodamente
                            .background(Color(0xFF0F1419)) // Fondo más oscuro para contraste
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        // CABECERA DEL MENÚ CON BOTONES PREMIUM
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FILTROS",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )

                            // CONTENEDOR DE BOTONES (LIMPIAR Y APLICAR)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                          // BOTÓN CHECK (APLICAR) ESTILO PREMIUM
                                Surface(
                                    onClick = {
                                        activeFilters = tempFilters
                                        expandedMenu = false
                                    },
                                    modifier = Modifier.size(36.dp), // Más grande para denotar acción principal
                                    shape = CircleShape,
                                    color = Color(0xFF1A1F26),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color.White.copy(0.2f), Color.Transparent)
                                        )
                                    ),
                                    shadowElevation = 6.dp
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(
                                                        Color(0xFF10B981).copy(0.15f),
                                                        Color.Transparent
                                                    ) // Resplandor Verde
                                                )
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Aplicar",
                                            modifier = Modifier.size(18.dp),
                                            tint = Color(0xFF10B981) // Verde esmeralda premium
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = Color.White.copy(alpha = 0.08f)
                        )

                        val options = listOf(
                            "NOVEDADES" to "🚀",
                            "PROMOCIONES" to "🔥",
                            "PRODUCTOS" to "🛍️",
                            "SERVICIOS" to "🛠️"
                        )

                        options.forEach { (option, emoji) ->
                            val isSelected = tempFilters.contains(option)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(emoji, fontSize = 14.sp)
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = option,
                                            color = if (isSelected) Color(0xFF22D3EE) else Color.White.copy(0.8f),
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF22D3EE),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    tempFilters = if (isSelected) tempFilters - option else tempFilters + option
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

       // HorizontalDivider(color = Color.White.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(4.dp))

        // Carrusel Horizontal con scroll infinito
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(300.dp),
            pageSpacing = 4.dp,
            contentPadding = PaddingValues(start = 10.dp, end = 64.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) { index ->
            // El índice real se obtiene con el operador módulo sobre la lista de ítems a mostrar
            val actualIndex = index % displayItems.size
            val item = displayItems[actualIndex]

            val pageOffset = ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).absoluteValue
            Box(modifier = Modifier.graphicsLayer {
                val scale = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                scaleX = scale
                scaleY = scale
                alpha = lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            }) {
                // Renderizado condicional según el tipo de ítem
                if (item.type == BannerType.GOOGLE_AD) {
                    // Llamada a la función AdBannerItem ubicada en AdBannerGoogle.kt
                    AdBannerItem(item = item)
                } else if (item.type == BannerType.PROMO || item.discount != null) {
                    // Banner de promociones
                    PromotionBannerItem(item = item, onClick = { onItemClick(item) })
                } else {
                    // Banner premium normal
                    PremiumBannerItem(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun PremiumBannerItem(item: AccordionBanner, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp) // Espacio para que las etiquetas no se corten
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = item.color),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.4f)
                Box(modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        val gradient = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent
                            ), start = Offset(0f, 0f), end = Offset(size.width, size.height)
                        ); onDrawWithContent {
                        drawContent(); drawRect(
                        gradient,
                        blendMode = BlendMode.Overlay
                    )
                    }
                    })
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent
                            ), startX = 0f, endX = 600f
                        )
                    ))

                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                        .padding(start = 10.dp, top = 20.dp, bottom = 16.dp), contentAlignment = Alignment.CenterStart) {
                        Column { AutoResizingText(text = item.title.uppercase(), color = Color.White, maxFontSize = 20.sp); Text(text = item.subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp); Spacer(modifier = Modifier.height(10.dp)); Box(modifier = Modifier
                            .width(40.dp)
                            .height(3.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))) }
                    }
                    Box(modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.7f)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        ))

                    Box(modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = item.icon,
                            fontSize = 100.sp,
                            modifier = Modifier.offset(x = 20.dp),
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    offset = Offset(-10f, 15f),
                                    blurRadius = 20f
                                )
                            )
                        )
                    }
                }
            }
        }

        // --- ETIQUETAS SUPERPUESTAS AL BORDE SUPERIOR ---
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .zIndex(1f)) {
            Row(modifier = Modifier.align(Alignment.TopStart), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Etiqueta principal (NUEVO / NUEVA CATEGORÍA / NUEVOS PRESTADORES)
                if (item.isNew || item.type == BannerType.NEW_CATEGORY || item.type == BannerType.NEW_PROVIDER) {
                    val labelText = when (item.type) {
                        BannerType.NEW_CATEGORY -> "🚀 NUEVA CATEGORÍA"
                        BannerType.NEW_PROVIDER -> "👥 NUEVOS PRESTADORES"
                        else -> "✨ NUEVO"
                    }
                    Surface(
                        color = Color(0xFFFFD600),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = labelText,
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Etiqueta informativa del tipo de banner
                if (item.type != BannerType.NEW_CATEGORY && item.type != BannerType.NEW_PROVIDER && item.type != BannerType.PROMO) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = item.type.label,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionBannerItem(item: AccordionBanner, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = item.color),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.4f)
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.9f),
                                Color.Transparent
                            ), startX = 0f, endX = 500f
                        )
                    ))

                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                        .padding(start = 16.dp, top = 20.dp, bottom = 4.dp), contentAlignment = Alignment.CenterStart) {
                        Column {
                            AutoResizingText(text = item.title.uppercase(), color = Color.White, maxFontSize = 18.sp)
                            Text(text = item.subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)

                            Spacer(modifier = Modifier.weight(1f))

                            item.provider?.let { provider ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(
                                            Color.Black.copy(alpha = 0.3f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(end = 10.dp)
                                ) {
                                    AsyncImage(
                                        model = provider.photoUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .border(
                                                1.5.dp,
                                                Color.White.copy(alpha = 0.5f),
                                                CircleShape
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (provider.isVerified) {
                                            Icon(
                                                Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = Color(0xFF2197F5),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = provider.displayName.formatearTexto(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.icon,
                                fontSize = 80.sp,
                                modifier = Modifier.offset(y = (-8).dp),
                                style = TextStyle(shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), offset = Offset(0f, 10f), blurRadius = 15f))
                            )

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp),
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = "VER OFERTA",
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- ETIQUETAS DE DESCUENTO SUPERPUESTA ---
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .zIndex(1f)) {
            Surface(
                modifier = Modifier.align(Alignment.TopStart),
                color = Color(0xFFE91E63),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                shadowElevation = 6.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                        if (item.discount != null) {
                        Spacer(Modifier.width(4.dp))
                        Text(text = "${item.discount}% OFF", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}


// ==========================================================================================
// --- SECCIÓN 5: CONTENEDORES ESTILIZADOS (WRAPPERS) ---
// ==========================================================================================
/**
 * Wrapper Premium que aplica el efecto visual de Gemini.
 * Incluye resplandor exterior, borde animado y efecto de profundidad.
 */
@Composable
fun GeminiCyberWrapper(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    borderThickness: Dp = 1.2.dp,
    isAnimated: Boolean = true,
    showGlow: Boolean = true,
    content: @Composable () -> Unit
) {
    val geminiBrush = geminiGradientBrush(isAnimated = isAnimated)
    val cyberBackground = Color(0xFF0A0E14)

    Box(modifier = modifier.padding(12.dp)) { // Padding extra para que el neón no se corte
        // 1. Capa de Neón Profundo (Glow amplio)
        if (showGlow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = 0.15f }
                    .blur(25.dp)
                    .background(geminiBrush, RoundedCornerShape(cornerRadius))
            )
            // 2. Capa de Neón Cercano (Brillo intenso junto al borde)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = 0.35f }
                    .blur(8.dp)
                    .background(geminiBrush, RoundedCornerShape(cornerRadius))
            )
        }

        // 3. Marco del Borde Fino
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(geminiBrush, RoundedCornerShape(cornerRadius))
                .padding(borderThickness)
        ) {
            // 4. Superficie Interna con Glassmorphism
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(cornerRadius - borderThickness)),
                color = cyberBackground
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Reflejo de cristal superior (Luces altas)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    content()
                }
            }
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 6: PREVIEWS ---
// ==========================================================================================
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun GeminiCyberWrapperPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            GeminiCyberWrapper(isAnimated = true) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Contenido dentro del Wrapper", color = Color.White)
                    Text("Estilo Cyberpunk", color = Color.Cyan)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun ServiceTagPreview() {
    MyApplicationTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServiceTag(text = "Médico", color = Color(0xFFB2EBF2), icon = "🩺")
            ServiceTag(text = "Plomería", color = Color(0xFFBCAAA4), icon = "🪠")
            ServiceTag(text = "Urgente", color = Color(0xFFE91E63), icon = "🚨")
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PremiumBannerItemPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier
            .padding(16.dp)
            .size(width = 300.dp, height = 140.dp)) {
            PremiumBannerItem(
                item = AccordionBanner(
                    id = "1",
                    title = "Servicio de Limpieza",
                    subtitle = "Profesionales a tu alcance",
                    icon = "🧹",
                    color = Color(0xFF2197F5),
                    type = BannerType.SERVICE_SALE,
                    isNew = true
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PremiumLensCarouselPreview() {
    val sampleProvider = Provider(
        uid = "p1",
        email = "contacto@maverick.com",
        displayName = "Maverick Soluciones",
        name = "Maverick",
        lastName = "Soluciones",
        phoneNumber = "123456789",
        matricula = null,
        titulo = null,
        cuilCuit = null,
        address = null,
        isSubscribed = true,
        isVerified = true,
        isOnline = true,
        isFavorite = false,
        rating = 4.8f,
        hasCompanyProfile = true,
        photoUrl = "https://picsum.photos/200",
        bannerImageUrl = null,
        createdAt = System.currentTimeMillis()
    )

    val sampleItems = listOf(
        AccordionBanner(
            id = "1",
            title = "Limpieza de Hogar",
            subtitle = "Los mejores profesionales para tu casa",
            icon = "🧹",
            color = Color(0xFF2197F5),
            type = BannerType.SERVICE_SALE,
            isNew = true
        ),
        AccordionBanner(
            id = "2",
            title = "Oferta Plomería",
            subtitle = "20% de descuento en reparaciones",
            icon = "🪠",
            color = Color(0xFFE91E63),
            type = BannerType.PROMO,
            discount = 20,
            provider = sampleProvider
        ),
        AccordionBanner(
            id = "3",
            title = "Nuevos Médicos",
            subtitle = "Especialistas cerca de ti",
            icon = "🩺",
            color = Color(0xFF9B51E0),
            type = BannerType.NEW_PROVIDER
        ),
        AccordionBanner(
            id = "4",
            title = "Publicidad Google",
            subtitle = "Anuncio patrocinado",
            icon = "🌐",
            color = Color.Gray,
            type = BannerType.GOOGLE_AD,
            imageUrl = "https://picsum.photos/400"
        )
    )

    MyApplicationTheme {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            PremiumLensCarousel(
                items = sampleItems,
                onSettingsClick = {},
                onItemClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PromotionBannerItemPreview() {
    val sampleProvider = Provider(
        uid = "p1",
        email = "contacto@maverick.com",
        displayName = "Maverick Soluciones",
        name = "Maverick",
        lastName = "Soluciones",
        phoneNumber = "123456789",
        matricula = null,
        titulo = null,
        cuilCuit = null,
        address = null,
        isSubscribed = true,
        isVerified = true,
        isOnline = true,
        isFavorite = false,
        rating = 4.8f,
        hasCompanyProfile = true,
        photoUrl = "https://picsum.photos/200",
        bannerImageUrl = null,
        createdAt = System.currentTimeMillis()
    )

    MyApplicationTheme {
        Box(modifier = Modifier
            .padding(16.dp)
            .size(width = 300.dp, height = 140.dp)) {
            PromotionBannerItem(
                item = AccordionBanner(
                    id = "2",
                    title = "Oferta Plomería",
                    subtitle = "20% de descuento en reparaciones",
                    icon = "🪠",
                    color = Color(0xFFE91E63),
                    type = BannerType.PROMO,
                    discount = 20,
                    provider = sampleProvider
                ),
                onClick = {}
            )
        }
    }
}

