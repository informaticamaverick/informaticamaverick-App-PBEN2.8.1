package com.example.myapplication.ui.screens.client.promo

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getAppColors
import androidx.compose.ui.graphics.Brush
import com.example.myapplication.ui.components.*

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PromotionsScreen(onBack: () -> Unit = {}) {
    // Obtener colores
    val colors = getAppColors()
    val materialColors = MaterialTheme.colorScheme
    
    // Estados para búsqueda y menú cíclico
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Estado cíclico del menú: 0 = Filtros, 1 = Menú config, 2 = Todo oculto
    var menuState by remember { mutableIntStateOf(0) }
    val showSettingsMenu = menuState == 0
    val showVerticalMenu = menuState == 1
    
    // Estados para filtros
    var filterByNew by remember { mutableStateOf(false) }
    var filterByExpiring by remember { mutableStateOf(false) }
    
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
    
    // Filtrar promociones
    val filteredPromotions = remember(searchQuery, filterByNew, filterByExpiring) {
        var result = samplePromotions
        
        if (searchQuery.isNotEmpty()) {
            result = result.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
        
        if (filterByNew) {
            result = result.filter { it.isNew }
        }
        
        if (filterByExpiring) {
            result = result.filter { it.expiresInHours != null }
        }
        
        result
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promociones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceColor
                )
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
                            color = materialColors.surface,
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
                                    tint = materialColors.onSurface,
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
                                    color = materialColors.onSurface,
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
                            color = materialColors.surface,
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
                                    tint = materialColors.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Alertas",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = materialColors.onSurface,
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
                            color = materialColors.surface,
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
                                    tint = materialColors.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Datos",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = materialColors.onSurface,
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
                            color = materialColors.surface,
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
                                    tint = materialColors.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Período",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = materialColors.onSurface,
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
                            // Botón: Nuevas
                            Surface(
                                modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                onClick = { 
                                    filterByNew = !filterByNew
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (filterByNew) materialColors.primaryContainer else materialColors.surface,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.NewReleases,
                                        "Nuevas",
                                        tint = if (filterByNew) materialColors.onPrimaryContainer else materialColors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (filterByNew) "Activo" else "Nuevas",
                                        color = if (filterByNew) materialColors.onPrimaryContainer else materialColors.onSurface,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            // Botón: Por Vencer
                            Surface(
                                modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                onClick = { 
                                    filterByExpiring = !filterByExpiring
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (filterByExpiring) materialColors.primaryContainer else materialColors.surface,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        "Por Vencer",
                                        tint = if (filterByExpiring) materialColors.onPrimaryContainer else materialColors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (filterByExpiring) "Activo" else "Por Vencer",
                                        color = if (filterByExpiring) materialColors.onPrimaryContainer else materialColors.onSurface,
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
                            color = materialColors.surface,
                            border = BorderStroke(2.5.dp, rainbowBrush),
                            shadowElevation = 12.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Search, null, tint = materialColors.onSurface, modifier = Modifier.size(26.dp))
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
                            color = materialColors.surface,
                            border = BorderStroke(2.5.dp, rainbowBrush),
                            shadowElevation = 12.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Settings,
                                    "Ajustes",
                                    tint = materialColors.onSurface,
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
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
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

        items(filteredPromotions.filter { it.expiresInHours == null }) { promotion ->
            PromotionCard(promotion = promotion)
        }
    }
    
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
