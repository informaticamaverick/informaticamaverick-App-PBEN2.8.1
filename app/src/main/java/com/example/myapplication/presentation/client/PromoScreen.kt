package com.example.myapplication.presentation.client

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ==========================================================================================
// --- CONSTANTES VISUALES MAVERICK PRO ULTRA ---
// ==========================================================================================
private val DarkBg = Color(0xFF020408)
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val NeonCyber = Color(0xFF00FFC2)
private val DiscountRed = Color(0xFFE91E63)
private val AdYellow = Color(0xFFFFC107)

// ==========================================================================================
// --- SECCIÓN 1: ENUMS MAESTROS Y MODELOS DE DATOS ---
// ==========================================================================================

/**
 * Estos Enums aseguran que el Prestador (al crear la publi) y el Cliente (al filtrarla)
 * usen exactamente las mismas variables, evitando errores de tipeo.
 */
enum class PromoType(val label: String, val icon: String, val color: Color) {
    PRODUCT("PRODUCTO", "🛍️", Color(0xFFF59E0B)), // Amber/Naranja
    SERVICE("SERVICIO", "🛠️", Color(0xFF3B82F6))   // Azul Maverick
}

enum class PromoTag(val label: String) {
    HOT_SALE("HOT SALE"),
    TWO_FOR_ONE("2x1"),
    FREE_SHIPPING("ENVÍO GRATIS"),
    INSTALLMENTS("CUOTAS SIN INTERÉS"),
    NEW_ARRIVAL("NUEVO INGRESO")
}

data class Promotion(
    val id: String,
    val type: PromoType,      // <-- Especifica si es Producto o Servicio
    val tag: PromoTag?,       // <-- Etiqueta extra opcional
    val imageUrls: List<String>,
    val providerImageUrl: String?,
    val providerName: String,
    val description: String,
    val providerId: String,
    val categories: List<String>,
    val rating: Float,
    val likes: Int,
    var isLiked: Boolean,
    val discount: Int? = null
)

data class ProviderPromotions(
    val provider: Provider,
    val promotions: List<Promotion>
)

// Uso de Interfaz Sellada para diferenciar Promociones Orgánicas de Anuncios Google
sealed interface PromoListItem {
    data class ProviderPromoItem(val providerPromotions: ProviderPromotions) : PromoListItem
    data class AdItem(
        val id: String,
        val title: String,
        val description: String,
        val imageUrl: String,
        val cta: String
    ) : PromoListItem
}

// ==========================================================================================
// --- SECCIÓN 2: PANTALLA PRINCIPAL (STATEFUL MVVM) ---
// ==========================================================================================

@Composable
fun PromoScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    viewModel: ProviderViewModel = hiltViewModel(),
    bottomPadding: PaddingValues = PaddingValues(bottom = 80.dp)
) {
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    PromoScreenContent(
        providers = providers,
        isLoading = isLoading,
        onBack = onBack,
        navController = navController,
        bottomPadding = bottomPadding
    )
}

// ==========================================================================================
// --- SECCIÓN 3: CONTENIDO STATELESS (UI PURA Y RÁPIDA) ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoScreenContent(
    providers: List<Provider>,
    isLoading: Boolean,
    onBack: () -> Unit,
    navController: NavHostController,
    bottomPadding: PaddingValues
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- ESTADOS INTERACTIVOS ---
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isFabExpanded by remember { mutableStateOf(false) }
    var activeFilters by remember { mutableStateOf(setOf<String>()) }
    var viewedFavorites by remember { mutableStateOf(setOf<String>()) }

    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedAd by remember { mutableStateOf<PromoListItem.AdItem?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    // --- LÓGICA DE MAPEO MOCK (Proveedores -> Promociones y Anuncios) ---
    val (listItems, dynamicCategories) = remember(providers, isRefreshing) {
        val providersWithPromos = mutableListOf<ProviderPromotions>()
        val categoriesSet = mutableSetOf<String>()

        val subscribedProviders = providers.filter { it.isSubscribed }.shuffled()

        subscribedProviders.forEachIndexed { index, provider ->
            val mainCompany = provider.companies.firstOrNull()
            val services = mainCompany?.categories ?: provider.categories
            categoriesSet.addAll(services)

            val promoList = mutableListOf<Promotion>()
            val mockImages = if (index % 2 == 0) {
                listOf(
                    "https://picsum.photos/seed/${provider.uid}1/800/800",
                    "https://picsum.photos/seed/${provider.uid}2/800/800"
                )
            } else {
                listOf("https://picsum.photos/seed/${provider.uid}3/800/600")
            }

            // Asignación de variables estrictas (Enum)
            val isProduct = Random.nextBoolean()

            promoList.add(
                Promotion(
                    id = "promo_${provider.uid}",
                    type = if (isProduct) PromoType.PRODUCT else PromoType.SERVICE,
                    tag = if (Random.nextBoolean()) PromoTag.entries.random() else null,
                    imageUrls = mockImages,
                    providerImageUrl = provider.photoUrl,
                    providerName = provider.displayName,
                    description = mainCompany?.description
                        ?: "¡Descubre nuestros servicios profesionales con la mejor garantía del mercado!",
                    providerId = provider.uid,
                    rating = provider.rating,
                    likes = (50..500).random(),
                    isLiked = provider.isFavorite,
                    discount = if (index % 2 == 0) (10..50).random() else null,
                    categories = services
                )
            )

            providersWithPromos.add(ProviderPromotions(provider, promoList))
        }

        // Generar lista final intercalando Anuncios con MAYOR frecuencia (cada 2 publicaciones)
        val finalItems = mutableListOf<PromoListItem>()
        val adTemplates = listOf(
            PromoListItem.AdItem(
                "ad1",
                "Seguros para Profesionales",
                "Asegura tu equipo de trabajo contra robos o daños. Cobertura en toda la provincia.",
                "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?q=80&w=800&auto=format&fit=crop",
                "Cotizar Gratis"
            ),
            PromoListItem.AdItem(
                "ad2",
                "Herramientas Bosch 30% OFF",
                "Renueva tu caja de herramientas con la mejor calidad del mercado. Hasta 6 cuotas sin interés.",
                "https://images.unsplash.com/photo-1504148455328-c376907d081c?q=80&w=800&auto=format&fit=crop",
                "Ver Catálogo"
            ),
            PromoListItem.AdItem(
                "ad3",
                "Google Cloud for Business",
                "Potencia tu negocio con las herramientas de la nube más seguras y escalables.",
                "https://images.unsplash.com/photo-1573164713988-8665fc963095?q=80&w=800&auto=format&fit=crop",
                "Más Información"
            )
        )

        var adCounter = 0
        providersWithPromos.forEachIndexed { index, providerPromo ->
            finalItems.add(PromoListItem.ProviderPromoItem(providerPromo))
            // Inserta Anuncio cada 2 posts de proveedores
            if ((index + 1) % 2 == 0) {
                finalItems.add(adTemplates[adCounter % adTemplates.size].copy(id = "ad_${System.currentTimeMillis()}_$adCounter"))
                adCounter++
            }
        }

        val cats = categoriesSet.map { catName ->
            ControlItem(label = catName, icon = null, emoji = "🏷️", color = MaverickBlue, id = "cat_${catName.lowercase()}")
        }

        finalItems to cats
    }

    val promosState = remember(listItems) {
        mutableStateMapOf<String, Promotion>().apply {
            listItems.forEach { item ->
                if (item is PromoListItem.ProviderPromoItem) {
                    item.providerPromotions.promotions.forEach { promo -> put(promo.id, promo) }
                }
            }
        }
    }

    fun handleLikeClick(promotionId: String) {
        promosState[promotionId]?.let {
            val updatedLikes = if (it.isLiked) it.likes - 1 else it.likes + 1
            promosState[promotionId] = it.copy(isLiked = !it.isLiked, likes = updatedLikes)
        }
    }

    val sharePromo: (Promotion) -> Unit = { promo ->
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "Oferta de ${promo.providerName}")
            putExtra(Intent.EXTRA_TEXT, "¡Mira esta promoción de ${promo.providerName} en Maverick!\n\n${promo.description}\n\nDescarga la app para contactarlo.")
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir promoción")
        context.startActivity(shareIntent)
    }

    // --- LÓGICA DE FILTRADO TÁCTICO INTEGRADA CON ENUMS ---
    val filteredListItems = remember(activeFilters, searchQuery, listItems) {
        var filtered = listItems.mapNotNull { item ->
            when (item) {
                is PromoListItem.AdItem -> item
                is PromoListItem.ProviderPromoItem -> {
                    val matchingPromos = item.providerPromotions.promotions.filter { promo ->
                        val selectedCats = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
                        val catMatch = selectedCats.isEmpty() || promo.categories.any { it.lowercase() in selectedCats }
                        val searchMatch = searchQuery.isEmpty() || promo.description.contains(searchQuery, ignoreCase = true) || promo.providerName.contains(searchQuery, ignoreCase = true)

                        // Lógica para Productos / Servicios (Exclusive OR Logic)
                        val typeMatch = when {
                            activeFilters.contains("filter_products") && !activeFilters.contains("filter_services") -> promo.type == PromoType.PRODUCT
                            activeFilters.contains("filter_services") && !activeFilters.contains("filter_products") -> promo.type == PromoType.SERVICE
                            else -> true // Muestra todo si ninguno o ambos están seleccionados
                        }

                        catMatch && searchMatch && typeMatch
                    }
                    if (matchingPromos.isNotEmpty()) PromoListItem.ProviderPromoItem(item.providerPromotions.copy(promotions = matchingPromos)) else null
                }
            }
        }

        if (activeFilters.contains("sort_precio_desc")) {
            filtered = filtered.sortedByDescending { (it as? PromoListItem.ProviderPromoItem)?.providerPromotions?.promotions?.maxOfOrNull { p -> p.discount ?: 0 } ?: 0 }
        } else if (activeFilters.contains("sort_rank")) {
            filtered = filtered.sortedByDescending { (it as? PromoListItem.ProviderPromoItem)?.providerPromotions?.provider?.rating ?: 0f }
        }

        filtered
    }

    val favoriteProviders = remember(listItems) {
        listItems.mapNotNull { if (it is PromoListItem.ProviderPromoItem && it.providerPromotions.provider.isFavorite) it.providerPromotions else null }
    }

    // --- RENDERIZADO UI ---
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (!isSearchActive) {
                    TopAppBar(
                        title = {
                            Text("Promociones", fontWeight = FontWeight.Black, color = Color.White, fontSize = 22.sp, letterSpacing = (-0.5).sp)
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                        },
                        actions = {
                            // 🔥 MODIFICACIÓN: Se integran MenuFiltros y MenuOrdenamiento en la cabecera, quitando la lupa
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MenuFiltros(
                                    activeFilters = activeFilters,
                                    dynamicCategories = dynamicCategories,
                                    onAction = { filterId ->
                                        val current = activeFilters.toMutableSet()
                                        if (!current.add(filterId)) current.remove(filterId)
                                        activeFilters = current
                                    },
                                    onApply = { /* Cierre automático */ },
                                    onClearFilters = { activeFilters = emptySet() }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                MenuOrdenamiento(
                                    activeFilters = activeFilters,
                                    onAction = { sortId ->
                                        activeFilters = activeFilters.filter { !it.startsWith("sort_") }.toSet() + if (sortId.isEmpty()) emptySet() else setOf(sortId)
                                    },
                                    onApply = { /* Cierre automático */ },
                                    onClearFilters = { activeFilters = activeFilters.filter { !it.startsWith("sort_") }.toSet() },
                                    showRank = true
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF05070A).copy(alpha = 0.85f))
                    )
                }
            }
        ) { paddingValues ->

            if (isLoading || isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaverickBlue)
                }
            } else if (filteredListItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalOffer, null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No se encontraron promociones con esos filtros.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 140.dp) // Espacio para el FAB
                ) {
                    // --- HISTORIAS (STORIES ROW) ---
                    if (favoriteProviders.isNotEmpty() && searchQuery.isEmpty()) {
                        item {
                            ProviderStoriesRow(
                                providers = favoriteProviders,
                                viewedProviderIds = viewedFavorites,
                                onStoryClick = { providerPromo ->
                                    viewedFavorites = viewedFavorites + providerPromo.provider.uid
                                    fullscreenImageUrl = providerPromo.promotions.firstOrNull()?.imageUrls?.firstOrNull()
                                }
                            )
                        }
                    }

                    // --- FEED PRINCIPAL TIPO INSTAGRAM ---
                    items(filteredListItems, key = {
                        when (it) {
                            is PromoListItem.AdItem -> it.id
                            is PromoListItem.ProviderPromoItem -> "provider_${it.providerPromotions.provider.uid}"
                        }
                    }) { item ->
                        when (item) {
                            is PromoListItem.AdItem -> {
                                AdCardInstagramStyle(ad = item, onClick = { selectedAd = item })
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                            }
                            is PromoListItem.ProviderPromoItem -> {
                                val promotion = item.providerPromotions.promotions.first()
                                val updatedPromo = promosState[promotion.id] ?: promotion

                                PromotionCardInstagramStyle(
                                    promotion = updatedPromo,
                                    onMessageClick = { navController.navigate("chat?providerId=${promotion.providerId}") },
                                    onProfileClick = { navController.navigate("perfil_prestador/${promotion.providerId}") },
                                    onImageClick = { url -> fullscreenImageUrl = url },
                                    onLikeClick = { handleLikeClick(updatedPromo.id) },
                                    onShareClick = { sharePromo(updatedPromo) }
                                )

                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                            }
                        }
                    }
                }
            }

        }

        // --- FULLSCREEN IMAGE OVERLAY ---
        AnimatedVisibility(
            visible = fullscreenImageUrl != null,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.zIndex(200f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { fullscreenImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = fullscreenImageUrl != null,
                    enter = scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.7f)),
                    exit = scaleOut(targetScale = 0.8f)
                ) {
                    AsyncImage(
                        model = fullscreenImageUrl,
                        contentDescription = "Imagen a pantalla completa",
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
                        contentScale = ContentScale.Fit
                    )
                }

                IconButton(
                    onClick = { fullscreenImageUrl = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp).size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        }

        // --- AD POPUP MODAL ---
        if (selectedAd != null) {
            Dialog(onDismissRequest = { selectedAd = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                    Card(modifier = Modifier.fillMaxWidth(0.9f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CardSurface), border = BorderStroke(1.dp, AdYellow.copy(alpha = 0.5f))) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                                AsyncImage(model = selectedAd!!.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                IconButton(onClick = { selectedAd = null }, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha=0.5f), CircleShape)) {
                                    Icon(Icons.Default.Close, null, tint = Color.White)
                                }
                            }
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("CONTENIDO PATROCINADO", color = AdYellow, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(selectedAd!!.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(selectedAd!!.description, color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { selectedAd = null }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AdYellow)) {
                                    Text(selectedAd!!.cta.uppercase(), color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

// ==========================================================================================
// --- SECCIÓN 4: COMPONENTES DE TARJETA (MAVERICK PRO ULTRA) ---
// ==========================================================================================

@Composable
fun ProviderStoriesRow(
    providers: List<ProviderPromotions>,
    viewedProviderIds: Set<String>,
    onStoryClick: (ProviderPromotions) -> Unit
) {
    val sortedProviders = remember(providers, viewedProviderIds) {
        providers.sortedBy { it.provider.uid in viewedProviderIds }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(
            text = "Ofertas de tus Favoritos",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sortedProviders, key = { it.provider.uid }) { providerPromo ->
                val isViewed = providerPromo.provider.uid in viewedProviderIds
                val ringModifier = if (isViewed) Modifier.background(Color.White.copy(alpha = 0.15f)) else Modifier.background(geminiGradientBrush(isAnimated = true))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(72.dp).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onStoryClick(providerPromo) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .then(ringModifier)
                                .padding(if (isViewed) 2.dp else 3.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(DarkBg).padding(3.dp)) {
                                AsyncImage(
                                    model = providerPromo.provider.photoUrl,
                                    contentDescription = null,
                                    fallback = rememberVectorPainter(Icons.Default.Person),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(CardSurface)
                                )
                            }
                        }

                        if (!isViewed && providerPromo.promotions.any { it.discount != null }) {
                            Surface(
                                color = DiscountRed,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, DarkBg),
                                modifier = Modifier.align(Alignment.BottomCenter).offset(y = 6.dp).zIndex(10f)
                            ) {
                                Text(
                                    text = "% OFF",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = providerPromo.provider.displayName,
                        color = if (isViewed) Color.Gray else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * TARJETA DE ANUNCIO GOOGLE (EDGE-TO-EDGE)
 */
@Composable
fun AdCardInstagramStyle(
    ad: PromoListItem.AdItem,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onClick() }) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFF1E3A8A), CircleShape).border(1.dp, Color.White.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Campaign, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ad.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Patrocinado", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
        }

        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(CardSurface)) {
            AsyncImage(
                model = ad.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                color = AdYellow.copy(alpha = 0.95f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                shadowElevation = 8.dp
            ) {
                Text("ANUNCIO", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), letterSpacing = 1.sp)
            }

            Surface(
                color = MaverickBlue,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                shadowElevation = 8.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(ad.cta, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(text = ad.description, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}


/**
 * TARJETA ESTILO INSTAGRAM (EDGE-TO-EDGE)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromotionCardInstagramStyle(
    promotion: Promotion,
    onMessageClick: () -> Unit,
    onProfileClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {

        // --- 1. HEADER DEL POST ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = promotion.providerImageUrl,
                contentDescription = null,
                fallback = rememberVectorPainter(Icons.Default.Person),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(0.1f), CircleShape)
                    .clickable { onProfileClick() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f).clickable { onProfileClick() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(promotion.providerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.Verified, null, tint = Color(0xFF9B51E0), modifier = Modifier.size(14.dp))
                }
                Text("Tucumán • ${(promotion.categories.firstOrNull() ?: "Servicios")}", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = { /* Menú de opciones */ }) { Icon(Icons.Default.MoreVert, null, tint = Color.Gray) }
        }

        // --- 2. IMAGEN DEL POST (EDGE-TO-EDGE) ---
        val pagerState = rememberPagerState(pageCount = { promotion.imageUrls.size })
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(CardSurface)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = promotion.imageUrls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clickable { onImageClick(promotion.imageUrls[page]) }
                )
            }

            // --- ETIQUETAS DE TIPO Y TAG (TOP LEFT) ---
            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Etiqueta de Producto/Servicio (Enum)
                Surface(
                    color = promotion.type.color.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shadowElevation = 10.dp
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(promotion.type.icon, fontSize = 10.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(promotion.type.label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }

                // Etiqueta Extra Adicional (Hot Sale, Envío, etc)
                if (promotion.tag != null) {
                    Surface(
                        color = MaverickBlue.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        shadowElevation = 10.dp
                    ) {
                        Text(
                            text = promotion.tag.label,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Badge Flotante de Descuento (Top Right)
            if (promotion.discount != null) {
                Surface(
                    color = DiscountRed.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shadowElevation = 10.dp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("DESCUENTO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text("${promotion.discount}%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, lineHeight = 18.sp)
                    }
                }
            }

            // BOTÓN CHAT IMMERSIVO (Bottom Right - Reubicado a la Derecha)
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                Surface(
                    onClick = onMessageClick,
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)),
                    shadowElevation = 15.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(geminiGradientBrush(isAnimated = true)), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.Message, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                // Punto verde de disponibilidad
                Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp).size(14.dp).background(Color.Green, CircleShape).border(2.dp, Color(0xFF0A0E14), CircleShape))
            }

            // Pager Indicators (Dots)
            if (promotion.imageUrls.size > 1) {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val isCurrent = pagerState.currentPage == iteration
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) MaverickBlue else Color.White.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        // --- 3. ACCIONES Y CONTENIDO SOCIAL ---
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                val iconScale by animateFloatAsState(targetValue = if (promotion.isLiked) 1.3f else 1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f), label = "likeAnim")
                Icon(
                    imageVector = if (promotion.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Me gusta",
                    tint = if (promotion.isLiked) DiscountRed else Color.White,
                    modifier = Modifier.size(30.dp).graphicsLayer { scaleX = iconScale; scaleY = iconScale }.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onLikeClick() }
                )

                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onShareClick() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "${promotion.likes} Me gusta", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = promotion.description, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, lineHeight = 18.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "HACE 2 HORAS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

// ==========================================================================================
// --- PREVIEW ---
// ==========================================================================================
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PromoScreenPreview() {
    MyApplicationTheme {
        val fakeProvider = Provider(
            uid = "test_1",
            email = "test@test.com",
            displayName = "Maverick Tech",
            name = "Maverick",
            lastName = "Tech",
            phoneNumber = "+54 381 123 4567",
            matricula = "MAT-1234",
            titulo = "Ingeniero",
            cuilCuit = "20-12345678-9",
            address = null,
            isSubscribed = true,
            isVerified = true,
            isOnline = true,
            isFavorite = true,
            rating = 5.0f,
            categories = listOf("Informatica"),
            hasCompanyProfile = true,
            photoUrl = null,
            bannerImageUrl = null,
            createdAt = System.currentTimeMillis(),
            companies = emptyList()
        )

        val fakeProvider2 = fakeProvider.copy(
            uid = "test_2",
            displayName = "Carlos Plomero",
            name = "Carlos",
            lastName = "Perez",
            categories = listOf("Plomería"),
            isFavorite = false
        )

        PromoScreenContent(
            providers = listOf(fakeProvider, fakeProvider2),
            isLoading = false,
            onBack = {},
            navController = rememberNavController(),
            bottomPadding = PaddingValues(0.dp)
        )
    }
}
