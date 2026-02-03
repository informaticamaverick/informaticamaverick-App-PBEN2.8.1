package com.example.myapplication.Client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.zIndex
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.random.Random


data class Promotion(
    val id: Int,
    val imageUrls: List<Any?>,
    val providerImageUrl: Any?,
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
    val provider: UserFalso,
    val promotions: List<Promotion>
)

sealed interface PromoListItem {
    data class ProviderPromoItem(val providerPromotions: ProviderPromotions) : PromoListItem
    data class AdItem(val id: Int) : PromoListItem
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
            .height(380.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val pagerState = rememberPagerState(pageCount = { promotion.imageUrls.size })
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = promotion.imageUrls[page],
                            placeholder = painterResource(id = R.drawable.logo_app)
                        ),
                        contentDescription = "Imagen de promoción ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick(promotion) }
                    )
                }
                if (promotion.discount != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "En Promoción",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error,
                        ) {
                            Text(
                                text = "${promotion.discount}% OFF",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                // Pager Indicator for Images
                Row(
                    Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }

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
                                tint = if (promotion.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.6f
                                )
                            )
                        }
                        Text(
                            text = promotion.likes.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { onProfileClick(promotion.providerId) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = promotion.providerImageUrl,
                                    placeholder = painterResource(id = R.drawable.logo_app)
                                ),
                                contentDescription = "Ver perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                        }
                        IconButton(
                            onClick = { onMessageClick(promotion.providerId) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "Enviar mensaje",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
            .height(300.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            val pagerState = rememberPagerState(pageCount = { promotion.imageUrls.size })
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = promotion.imageUrls[page],
                            placeholder = painterResource(id = R.drawable.logo_app)
                        ),
                        contentDescription = "Imagen de promoción ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick(promotion) }
                    )
                }
                if (promotion.discount != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "En Promoción",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error,
                        ) {
                            Text(
                                text = "${promotion.discount}% OFF",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Row(
                    Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = promotion.providerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = promotion.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 3
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconToggleButton(
                            checked = promotion.isLiked,
                            onCheckedChange = { onLikeClick() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (promotion.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = if (promotion.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.6f
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = promotion.likes.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onProfileClick(promotion.providerId) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = promotion.providerImageUrl,
                                    placeholder = painterResource(id = R.drawable.logo_app)
                                ),
                                contentDescription = "Ver perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                        }
                        IconButton(
                            onClick = { onMessageClick(promotion.providerId) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "Enviar mensaje",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Anuncio de Google Ads")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PromoScreen(
    onBack: () -> Unit,
    navController: NavHostController
) {
    val colors = MaterialTheme.colorScheme
    val isSystemInDarkMode = isSystemInDarkTheme()
    
    var selectedPromotion by remember { mutableStateOf<Promotion?>(null) }
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var providerForDialog by remember { mutableStateOf<ProviderPromotions?>(null) }
    var viewedFavorites by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Estados para FABs y búsqueda
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Estado cíclico del menú: 0 = Filtros, 1 = Menú config, 2 = Todo oculto
    var menuState by remember { mutableIntStateOf(0) }
    val showSettingsMenu = menuState == 0
    val showVerticalMenu = menuState == 1
    
    // Estados para filtros adicionales
    var sortByDiscount by remember { mutableStateOf(false) } // false = Más reciente, true = Mayor descuento
    var onlyWithDiscount by remember { mutableStateOf(false) }
    
    // Estados del menú de configuración
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showDataVisibilityDialog by remember { mutableStateOf(false) }
    var showTimePeriodDialog by remember { mutableStateOf(false) }
    
    // Preferencias de usuario
    var viewMode by remember { mutableStateOf("Detallada") } // "Compacta", "Detallada", "Tarjetas"
    var timePeriod by remember { mutableStateOf("Todo") } // "Semana", "Mes", "3 Meses", "Todo"
    var showExpiry by remember { mutableStateOf(true) }
    var showProviderInfo by remember { mutableStateOf(true) }
    var showBadges by remember { mutableStateOf(true) }
    var notifyNewPromotions by remember { mutableStateOf(true) }
    var notifyExpiring by remember { mutableStateOf(true) }
    var notifyFlashSales by remember { mutableStateOf(true) }
    
    // Estados para el BottomSheet de filtros
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var tempSelectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

    val (listItems, allCategories) = remember {
        val providersWithPromos = mutableListOf<ProviderPromotions>()
        val categories = mutableSetOf<String>()
        val subscribedProviders = SampleDataFalso.prestadores.filter { it.isSubscribed }

        subscribedProviders.forEach { provider ->
            val numPromos = (1..3).random()
            val providerPromotionsList = (1..numPromos).map { promoIndex ->
                val imageCount = (1..3).random()
                val imageUrls = (1..imageCount).map { "https://picsum.photos/seed/${provider.id}_${promoIndex}_$it/400/200" }
                val hasDiscount = Random.nextBoolean()
                categories.addAll(provider.services)
                Promotion(
                    id = (provider.id.toInt() * 100) + promoIndex,
                    imageUrls = imageUrls,
                    providerImageUrl = provider.profileImageUrl,
                    providerName = "${provider.name} ${provider.lastName}",
                    description = "Oferta especial #${promoIndex} en ${provider.services.first()}",
                    providerId = provider.id,
                    rating = provider.rating,
                    likes = (100..1000).random(),
                    isLiked = Random.nextBoolean(),
                    discount = if (hasDiscount) (10..30).random() else null,
                    categories = provider.services
                )
            }
            providersWithPromos.add(ProviderPromotions(provider, providerPromotionsList))
        }

        val finalItems = mutableListOf<PromoListItem>()
        var adCounter = 0
        providersWithPromos.forEach { providerPromo ->
            finalItems.add(PromoListItem.ProviderPromoItem(providerPromo))
            if ((finalItems.size + 1) % 3 == 0) {
                finalItems.add(PromoListItem.AdItem(adCounter++))
            }
        }
        finalItems to categories.toList().sorted()
    }

    val promosState = remember {
        mutableStateMapOf<Int, Promotion>().apply {
            listItems.forEach { item ->
                if (item is PromoListItem.ProviderPromoItem) {
                    item.providerPromotions.promotions.forEach { promo ->
                        put(promo.id, promo)
                    }
                }
            }
        }
    }

    val favoriteProviders = remember(listItems, viewedFavorites) {
        val favs = listItems.mapNotNull {
            if (it is PromoListItem.ProviderPromoItem && it.providerPromotions.provider.isFavorite) it.providerPromotions else null
        }
        // Ordenar: primero los que NO están vistos (false < true), manteniendo estabilidad
        favs.sortedBy { it.provider.id in viewedFavorites }
    }

    fun handleLikeClick(promotionId: Int) {
        promosState[promotionId]?.let {
            val updatedLikes = if (it.isLiked) it.likes - 1 else it.likes + 1
            promosState[promotionId] = it.copy(isLiked = !it.isLiked, likes = updatedLikes)
        }
    }

    val filteredListItems = remember(selectedCategories, promosState, searchQuery, onlyWithDiscount, sortByDiscount, timePeriod) {
        var filtered = if (selectedCategories.isEmpty()) {
            listItems
        } else {
            listItems.mapNotNull { item ->
                when (item) {
                    is PromoListItem.AdItem -> item
                    is PromoListItem.ProviderPromoItem -> {
                        val filteredPromos = item.providerPromotions.promotions.filter { promo ->
                            promo.categories.any { it in selectedCategories }
                        }
                        if (filteredPromos.isNotEmpty()) {
                            PromoListItem.ProviderPromoItem(item.providerPromotions.copy(promotions = filteredPromos))
                        } else {
                            null
                        }
                    }
                }
            }
        }
        
        // Filtro por búsqueda
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.mapNotNull { item ->
                when (item) {
                    is PromoListItem.AdItem -> item
                    is PromoListItem.ProviderPromoItem -> {
                        val matchingPromos = item.providerPromotions.promotions.filter { promo ->
                            promo.description.contains(searchQuery, ignoreCase = true) ||
                            promo.providerName.contains(searchQuery, ignoreCase = true) ||
                            promo.categories.any { it.contains(searchQuery, ignoreCase = true) }
                        }
                        if (matchingPromos.isNotEmpty()) {
                            PromoListItem.ProviderPromoItem(item.providerPromotions.copy(promotions = matchingPromos))
                        } else {
                            null
                        }
                    }
                }
            }
        }
        
        // Filtro por descuento
        if (onlyWithDiscount) {
            filtered = filtered.mapNotNull { item ->
                when (item) {
                    is PromoListItem.AdItem -> item
                    is PromoListItem.ProviderPromoItem -> {
                        val discountPromos = item.providerPromotions.promotions.filter { it.discount != null }
                        if (discountPromos.isNotEmpty()) {
                            PromoListItem.ProviderPromoItem(item.providerPromotions.copy(promotions = discountPromos))
                        } else {
                            null
                        }
                    }
                }
            }
        }
        
        // Ordenar por descuento
        if (sortByDiscount) {
            filtered = filtered.map { item ->
                when (item) {
                    is PromoListItem.AdItem -> item
                    is PromoListItem.ProviderPromoItem -> {
                        val sortedPromos = item.providerPromotions.promotions.sortedByDescending { it.discount ?: 0 }
                        PromoListItem.ProviderPromoItem(item.providerPromotions.copy(promotions = sortedPromos))
                    }
                }
            }
        }
        
        // Filtro por período de tiempo
        val currentDate = java.util.Calendar.getInstance()
        if (timePeriod != "Todo") {
            val daysToSubtract = when (timePeriod) {
                "Semana" -> 7
                "Mes" -> 30
                "3 Meses" -> 90
                else -> 0
            }
            if (daysToSubtract > 0) {
                val cutoffDate = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, -daysToSubtract)
                }
                filtered = filtered.mapNotNull { item ->
                    when (item) {
                        is PromoListItem.AdItem -> item
                        is PromoListItem.ProviderPromoItem -> {
                            // En una app real, cada promoción tendría fecha de creación
                            // Por ahora, dejamos pasar todas las promociones
                            item
                        }
                    }
                }
            }
        }
        
        filtered
    }

    val orderedCategories = remember(selectedCategories) {
        val selected = selectedCategories.toList().sorted()
        val unselected = allCategories.filterNot { it in selectedCategories }
        selected + unselected
    }

    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Promociones") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                )
            },
                        floatingActionButton = {
                val rainbowBrush = geminiGradientEffect()
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 70.dp)
                ) {
                    // Menú vertical de configuración (aparece arriba del engranaje)
                    AnimatedVisibility(
                        visible = showVerticalMenu && !isSearchActive,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Opción: Modo de Vista
                            Surface(
                                modifier = Modifier.size(64.dp),
                                onClick = { 
                                    viewMode = when(viewMode) {
                                        "Compacta" -> "Detallada"
                                        "Detallada" -> "Tarjetas"
                                        else -> "Compacta"
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surface,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.ViewModule,
                                        "Modo Vista",
                                        tint = colors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = when(viewMode) {
                                            "Compacta" -> "Comp"
                                            "Detallada" -> "Det"
                                            "Tarjetas" -> "Card"
                                            else -> "Vista"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            // Opción: Alertas
                            Surface(
                                modifier = Modifier.size(64.dp),
                                onClick = { 
                                    showNotificationsDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surface,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        "Alertas",
                                        tint = colors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Alertas",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            // Opción: Mostrar Datos
                            Surface(
                                modifier = Modifier.size(64.dp),
                                onClick = { 
                                    showDataVisibilityDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surface,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Visibility,
                                        "Mostrar Datos",
                                        tint = colors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Datos",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            // Opción: Período
                            Surface(
                                modifier = Modifier.size(64.dp),
                                onClick = { 
                                    showTimePeriodDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surface,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        "Período",
                                        tint = colors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Período",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                
                    // Botones de búsqueda y engranaje
                    AnimatedVisibility(
                        visible = !isSearchActive,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        val gearRotation by animateFloatAsState(
                            targetValue = if (menuState == 2) 0f else 45f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                        // Botones de filtros expandibles (aparecen a la izquierda)
                        AnimatedVisibility(
                            visible = showSettingsMenu && !isSearchActive,
                            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(start = 32.dp, end = 8.dp)
                            ) {
                                // Botón: Categorías
                                Surface(
                                    modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                    onClick = { 
                                        tempSelectedCategories = selectedCategories
                                        showFilterSheet = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedCategories.isNotEmpty()) colors.primaryContainer else colors.surface,
                                    shadowElevation = 6.dp
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Category,
                                            "Categorías",
                                            tint = if (selectedCategories.isNotEmpty()) colors.onPrimaryContainer else colors.onSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (selectedCategories.isNotEmpty()) "Activo" else "Categorías",
                                            color = if (selectedCategories.isNotEmpty()) colors.onPrimaryContainer else colors.onSurface,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                                
                                // Botón: Con Descuento
                                Surface(
                                    modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                    onClick = { 
                                        onlyWithDiscount = !onlyWithDiscount
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (onlyWithDiscount) colors.primaryContainer else colors.surface,
                                    shadowElevation = 6.dp
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.LocalOffer,
                                            "Descuentos",
                                            tint = if (onlyWithDiscount) colors.onPrimaryContainer else colors.onSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (onlyWithDiscount) "Activo" else "Descuentos",
                                            color = if (onlyWithDiscount) colors.onPrimaryContainer else colors.onSurface,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Botón Dividido (Buscar + Engranaje)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Parte Izquierda: Buscar
                            Surface(
                                onClick = { isSearchActive = true },
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                                color = colors.surface,
                                border = BorderStroke(2.5.dp, rainbowBrush),
                                shadowElevation = 12.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Search, null, tint = colors.onSurface, modifier = Modifier.size(26.dp))
                                }
                            }
                            
                            // Parte Derecha: Ajustes / Cerrar
                            Surface(
                                modifier = Modifier
                                    .size(56.dp)
                                    .combinedClickable(
                                        onClick = { 
                                            menuState = (menuState + 1) % 3
                                        },
                                        onLongClick = { }
                                    ),
                                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 28.dp, bottomEnd = 28.dp),
                                color = colors.surface,
                                border = BorderStroke(2.5.dp, rainbowBrush),
                                shadowElevation = 12.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Settings,
                                        "Ajustes",
                                        tint = colors.onSurface,
                                        modifier = Modifier.size(26.dp).rotate(gearRotation)
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            },
floatingActionButtonPosition = FabPosition.End
        ) { paddingValues ->
        
        // Diálogos de configuración
        // Diálogo: Alertas de Notificaciones
        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                title = { Text("Alertas", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Configurar notificaciones:", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notificar nuevas promociones")
                            Switch(
                                checked = notifyNewPromotions,
                                onCheckedChange = { notifyNewPromotions = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notificar ofertas por vencer")
                            Switch(
                                checked = notifyExpiring,
                                onCheckedChange = { notifyExpiring = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notificar ventas flash")
                            Switch(
                                checked = notifyFlashSales,
                                onCheckedChange = { notifyFlashSales = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showNotificationsDialog = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationsDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Diálogo: Mostrar Datos
        if (showDataVisibilityDialog) {
            AlertDialog(
                onDismissRequest = { showDataVisibilityDialog = false },
                title = { Text("Mostrar Datos", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Configurar visibilidad de datos:", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar fecha de expiración")
                            Switch(
                                checked = showExpiry,
                                onCheckedChange = { showExpiry = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar info del prestador")
                            Switch(
                                checked = showProviderInfo,
                                onCheckedChange = { showProviderInfo = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar insignias 'NUEVO'")
                            Switch(
                                checked = showBadges,
                                onCheckedChange = { showBadges = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showDataVisibilityDialog = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDataVisibilityDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Diálogo: Período de Tiempo
        if (showTimePeriodDialog) {
            AlertDialog(
                onDismissRequest = { showTimePeriodDialog = false },
                title = { Text("Período de Tiempo", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Filtrar promociones por período:", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val periods = listOf("Semana", "Mes", "3 Meses", "Todo")
                        periods.forEach { period ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { timePeriod = period }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(period)
                                RadioButton(
                                    selected = timePeriod == period,
                                    onClick = { timePeriod = period }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showTimePeriodDialog = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePeriodDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    ProviderIconsRow(
                        providers = favoriteProviders,
                        viewedProviderIds = viewedFavorites,
                        onProviderClick = {
                            // Al hacer click, solo abrimos el dialogo. NO marcamos como visto aun.
                            providerForDialog = it
                        }
                    )
                }
                items(filteredListItems, key = {
                    when (it) {
                        is PromoListItem.AdItem -> "ad_${it.id}"
                        is PromoListItem.ProviderPromoItem -> "provider_${it.providerPromotions.provider.id}"
                    }
                }) { item ->
                    when (item) {
                        is PromoListItem.AdItem -> AdCard()
                        is PromoListItem.ProviderPromoItem -> {
                            val providerPromo = item.providerPromotions
                            if (providerPromo.promotions.size > 1) {
                                ProviderPromotionPager(
                                    providerPromotions = providerPromo,
                                    promosState = promosState,
                                    onLikeClick = ::handleLikeClick,
                                    onMessageClick = { navController.navigate("chat?providerId=$it") },
                                    onProfileClick = { navController.navigate("perfil_prestador/$it") },
                                    onImageClick = { selectedPromotion = it }
                                )
                            } else {
                                val promotion = providerPromo.promotions.first()
                                val updatedPromotion = promosState[promotion.id] ?: promotion
                                val onLike = { handleLikeClick(updatedPromotion.id) }

                                if (promotion.id % 2 != 0) {
                                    PromotionCardVertical(
                                        promotion = updatedPromotion,
                                        onMessageClick = { navController.navigate("chat?providerId=$it") },
                                        onProfileClick = { navController.navigate("perfil_prestador/$it") },
                                        onImageClick = { selectedPromotion = it },
                                        onLikeClick = onLike
                                    )
                                } else {
                                    PromotionCard(
                                        promotion = updatedPromotion,
                                        onMessageClick = { navController.navigate("chat?providerId=$it") },
                                        onProfileClick = { navController.navigate("perfil_prestador/$it") },
                                        onImageClick = { selectedPromotion = it },
                                        onLikeClick = onLike
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } // Cierre de LazyColumn y Scaffold paddingValues
        
        // Dialogo del prestador favorito
        providerForDialog?.let { currentProvider ->
            Dialog(onDismissRequest = { 
                // Al cerrar el dialogo, marcamos como visto y enviamos al final de la lista
                viewedFavorites = viewedFavorites + currentProvider.provider.id
                providerForDialog = null 
            }) {
                ProviderPromotionPager(
                    providerPromotions = currentProvider,
                    promosState = promosState,
                    onLikeClick = ::handleLikeClick,
                    onMessageClick = { navController.navigate("chat?providerId=$it") },
                    onProfileClick = { navController.navigate("perfil_prestador/$it") },
                    onImageClick = { selectedPromotion = it }
                )
            }
        }
        
        selectedPromotion?.let {
            FullScreenPromotionView(
                promotion = it,
                onDismiss = { selectedPromotion = null },
                onMessageClick = { navController.navigate("chat?providerId=$it") },
                onProfileClick = { navController.navigate("perfil_prestador/$it") },
                onLikeClick = { handleLikeClick(it.id) }
            )
        }

        // Bottom Sheet para filtros
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Filtrar por Categorías",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(allCategories) { category ->
                            FilterChip(
                                selected = category in tempSelectedCategories,
                                onClick = {
                                    tempSelectedCategories = if (category in tempSelectedCategories) {
                                        tempSelectedCategories - category
                                    } else {
                                        tempSelectedCategories + category
                                    }
                                },
                                label = { Text(category) },
                                leadingIcon = if (category in tempSelectedCategories) {
                                    { Icon(Icons.Default.Done, contentDescription = "Selected") }
                                } else null
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            selectedCategories = tempSelectedCategories
                            showFilterSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aplicar Filtros")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // Barra de búsqueda flotante
        if (isSearchActive) {
            val rainbowBrush = geminiGradientEffect()
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp)
                    .zIndex(10f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.surface,
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                        shadowElevation = 12.dp,
                        border = BorderStroke(2.5.dp, rainbowBrush)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                null,
                                tint = colors.onSurface.copy(0.8f),
                                modifier = Modifier.padding(start = 24.dp).size(20.dp)
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                                    .focusRequester(focusRequester),
                                textStyle = TextStyle(color = colors.onSurface, fontSize = 17.sp),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (searchQuery.isEmpty()) {
                                            Text("Buscar promociones...", color = colors.onSurfaceVariant, fontSize = 16.sp)
                                        }
                                        inner()
                                    }
                                }
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.size(56.dp),
                    onClick = {
                        isSearchActive = false
                        searchQuery = ""
                        keyboardController?.hide()
                    },
                    shape = CircleShape,
                    color = colors.surface,
                    border = BorderStroke(2.5.dp, rainbowBrush),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, "Cerrar", tint = colors.onSurface, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }
        } // Cierre del Box externo
    } // Cierre de MyApplicationTheme
}

@Composable
fun ProviderIconsRow(
    providers: List<ProviderPromotions>,
    viewedProviderIds: Set<String>,
    onProviderClick: (ProviderPromotions) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "Descuentos de Mis Favoritos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(providers, key = { it.provider.id }) { providerPromo ->
                Box(modifier = Modifier.clickable { onProviderClick(providerPromo) }) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = providerPromo.provider.profileImageUrl,
                            placeholder = painterResource(id = R.drawable.logo_app)
                        ),
                        contentDescription = providerPromo.provider.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    // Usamos if en lugar de AnimatedVisibility para evitar problemas de scope en items
                    if (providerPromo.provider.id !in viewedProviderIds) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFiltersRow(
    allCategories: List<String>,
    selectedCategories: Set<String>,
    onOpenSheet: () -> Unit,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, end = 100.dp), // Espacio extra para botones
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allCategories) { category ->
                FilterChip(
                    selected = category in selectedCategories,
                    onClick = { /* Solo visualización aquí, seleccion en sheet */ },
                    label = { Text(category) },
                    leadingIcon = if (category in selectedCategories) {
                        { Icon(Icons.Default.Done, contentDescription = "Selected") }
                    } else null
                )
            }
        }
        // Botones flotantes alineados a la derecha
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                        startX = 0f,
                        endX = 20f
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedCategories.isNotEmpty()) {
                Surface(
                    shape = CircleShape,
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    IconButton(onClick = onClearFilters) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar filtros",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            Surface(
                shape = CircleShape,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                IconButton(onClick = onOpenSheet) {
                    Icon(
                        imageVector = Icons.Default.Tune, // Icono de filtros/ajustes
                        contentDescription = "Filtrar categorías",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ProviderPromotionPager(
    providerPromotions: ProviderPromotions,
    promosState: Map<Int, Promotion>,
    onLikeClick: (Int) -> Unit,
    onMessageClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onImageClick: (Promotion) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { providerPromotions.promotions.size })

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(0.dp),
            pageSpacing = 0.dp,
        ) { pageIndex ->
            val promotion = providerPromotions.promotions[pageIndex]
            val updatedPromotion = promosState[promotion.id] ?: promotion

            PromotionCard(
                promotion = updatedPromotion,
                onMessageClick = onMessageClick,
                onProfileClick = onProfileClick,
                onImageClick = onImageClick,
                onLikeClick = { onLikeClick(updatedPromotion.id) }
            )
        }

        if (pagerState.pageCount > 1) {
            Row(
                Modifier
                    .height(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
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
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = promotion.imageUrls.firstOrNull(),
                    placeholder = painterResource(id = R.drawable.ic_launcher_background)
                ),
                contentDescription = "Imagen de promoción a pantalla completa",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 72.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    promotion.providerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    promotion.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
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
                                if (promotion.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                "Like",
                                tint = if (promotion.isLiked) Color(0xFFFFD700) else Color.White
                            )
                        }
                        Text(
                            promotion.likes.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FloatingActionButton(
                            onClick = { onProfileClick(promotion.providerId) },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ) {
                            Icon(Icons.Default.Person, "Ver perfil", tint = MaterialTheme.colorScheme.onSecondary)
                        }
                        FloatingActionButton(
                            onClick = { onMessageClick(promotion.providerId) },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, "Enviar mensaje", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
fun PromoScreenPreview() {
    MyApplicationTheme {
        PromoScreen(onBack = {}, navController = rememberNavController())
    }
}

