package com.example.myapplication.presentation.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.components.*
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// ==========================================================================================
// --- PANTALLA FAST (STATEFUL - CONECTADA AL VIEWMODEL) ---
// ==========================================================================================

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FastScreen(
    navController: NavHostController,
    profileViewModel: ProfileSharedViewModel = hiltViewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    fastViewModel: FastViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel() // Inyectamos el clima para la alerta contextual
) {
    val userState by profileViewModel.userState.collectAsState()
    val allProviders by providerViewModel.providers.collectAsStateWithLifecycle()
    val allCategories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val weatherDesc by weatherViewModel.weatherDescription.collectAsState()

    val isSearching by fastViewModel.isSearching.collectAsStateWithLifecycle()
    val searchFinished by fastViewModel.searchFinished.collectAsStateWithLifecycle()
    val searchResults by fastViewModel.searchResults.collectAsStateWithLifecycle()

    FastScreenContent(
        navController = navController,
        userState = userState,
        allProviders = allProviders,
        allCategories = allCategories,
        weatherDescription = weatherDesc,
        isSearching = isSearching,
        searchFinished = searchFinished,
        searchResults = searchResults,
        onStartSearch = { category, lat, lon -> fastViewModel.startEmergencySearch(category, allProviders, lat, lon) },
        onResetSearch = { fastViewModel.resetSearch() }
    )
}

// ==========================================================================================
// --- PANTALLA FAST (STATELESS - PURA UI) ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastScreenContent(
    navController: NavHostController,
    userState: UserEntity?,
    allProviders: List<Provider>,
    allCategories: List<CategoryEntity>,
    weatherDescription: String,
    isSearching: Boolean,
    searchFinished: Boolean,
    searchResults: List<ProviderWithDistance>,
    onStartSearch: (CategoryEntity?, Double, Double) -> Unit,
    onResetSearch: () -> Unit
) {
    var showManualSearchSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    // Estado para el popup interactivo del radar
    var selectedProviderOnRadar by remember { mutableStateOf<ProviderWithDistance?>(null) }

    // Estado del panel inferior (Colapsado por defecto)
    var isBottomSheetExpanded by remember { mutableStateOf(false) }

    // --- ALERTA CONTEXTUAL (CLIMA Y HORARIO) ---
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val isNightTime = currentHour >= 21 || currentHour < 6
    val isRaining = remember(weatherDescription) {
        weatherDescription.contains("lluvia", true) ||
                weatherDescription.contains("tormenta", true) ||
                weatherDescription.contains("rain", true) ||
                weatherDescription.contains("storm", true)
    }

    // Mostramos la alerta si es de noche o está lloviendo
    var showContextAlert by remember { mutableStateOf(isNightTime || isRaining) }

    // Mock Coords (Plaza Independencia Tucumán aprox) para alimentar el ViewModel por ahora
    val mockUserLat = -26.8310
    val mockUserLon = -65.2045

    var currentLocationState by remember {
        mutableStateOf<LocationOption>(LocationOption.Gps(address = "Buscando...", locality = ""))
    }

    val topCategories = remember(allCategories) {
        allCategories.filter { it.name in listOf("Electricidad", "Plomería", "Fletes", "Cerrajería") }.take(4)
    }

    LaunchedEffect(topCategories) {
        if (selectedCategory == null && topCategories.isNotEmpty()) {
            selectedCategory = topCategories.first()
        }
    }

    // Auto-colapsar la lista de resultados al buscar nuevamente o resetear
    LaunchedEffect(isSearching, searchFinished) {
        if (!searchFinished) isBottomSheetExpanded = false
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF05070A))) {

        // 1. FONDO: MAPA TÁCTICO CON RESULTADOS INTERACTIVOS
        TacticalMapBackground(
            isSearching = isSearching,
            searchFinished = searchFinished,
            results = searchResults,
            onProviderClick = { selectedProviderOnRadar = it } // Evento de interacción
        )

        // 2. HUD SUPERIOR: UBICACIÓN Y ALERTA CONTEXTUAL
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).statusBarsPadding().padding(horizontal = 16.dp)) {
            val cardGradientBrush = Brush.verticalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.03f)))

            LocationSelector(
                user = userState,
                currentLocation = currentLocationState,
                onRefresh = {
                    currentLocationState = LocationOption.Gps("Actualizando GPS...", "")
                    // RE-BÚSQUEDA AUTOMÁTICA AL CAMBIAR UBICACIÓN
                    if (isSearching || searchFinished) onStartSearch(selectedCategory, mockUserLat, mockUserLon)
                },
                onLocationSelected = {
                    currentLocationState = it
                    // RE-BÚSQUEDA AUTOMÁTICA AL CAMBIAR UBICACIÓN
                    if (isSearching || searchFinished) onStartSearch(selectedCategory, mockUserLat, mockUserLon)
                },
                brush = cardGradientBrush
            )

            // ALERTA DE CLIMA / NOCHE
            AnimatedVisibility(
                visible = showContextAlert && (isNightTime || isRaining),
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ContextualWarningBanner(
                    isNight = isNightTime,
                    isRaining = isRaining,
                    weatherDesc = weatherDescription,
                    onDismiss = { showContextAlert = false }
                )
            }
        }

        // 3. CAPA PUBLICITARIA (POPUP DURANTE BÚSQUEDA)
        if (isSearching) {
            GoogleAdPopup()
        }

        // 4. POPUP DE INTERACCIÓN DEL RADAR (ESTRELLA DE LA PANTALLA)
        AnimatedVisibility(
            visible = selectedProviderOnRadar != null,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut(tween(200)) + fadeOut(),
            modifier = Modifier.align(Alignment.Center).zIndex(50f)
        ) {
            selectedProviderOnRadar?.let { providerData ->
                InteractiveRadarPopup(
                    data = providerData,
                    onClose = { selectedProviderOnRadar = null },
                    onChatClick = {
                        val providerId = providerData.provider.id
                        selectedProviderOnRadar = null
                        // Bloque try-catch para evitar crash de navegación si se interrumpe el contexto
                        try {
                            navController.navigate("chat/$providerId") {
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onProfileClick = {
                        val providerId = providerData.provider.id
                        selectedProviderOnRadar = null
                        try {
                            navController.navigate("perfil_prestador/$providerId") {
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )
            }
        }

        // 5. BOTTOM SHEET (RESULTADOS O INICIO DE BÚSQUEDA)
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).navigationBarsPadding()
        ) {
            if (searchFinished) {
                // MUESTRA RESULTADOS DE LA BÚSQUEDA (Colapsable)
                FastResultsPanel(
                    results = searchResults,
                    selectedCategory = selectedCategory,
                    isExpanded = isBottomSheetExpanded,
                    onToggleExpand = { isBottomSheetExpanded = !isBottomSheetExpanded },
                    onReset = {
                        onResetSearch()
                        selectedProviderOnRadar = null
                    },
                    onChatClick = { providerId ->
                        try {
                            navController.navigate("chat/$providerId") { launchSingleTop = true }
                        } catch (e: Exception) { e.printStackTrace() }
                    },
                    onNavigateToNormalSearch = { catName ->
                        try {
                            navController.navigate("result_busqueda/$catName") { launchSingleTop = true }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                )
            } else if (!isSearching) {
                // MUESTRA PANEL DE CONFIGURACIÓN DE EMERGENCIA
                FastConfigBottomSheet(
                    selectedCategory = selectedCategory,
                    topCategories = topCategories,
                    onCategorySelect = { selectedCategory = it },
                    onOpenManualSearch = { showManualSearchSheet = true },
                    onStartSearch = { onStartSearch(selectedCategory, mockUserLat, mockUserLon) }
                )
            }
        }

        // 6. MODAL DE BÚSQUEDA MANUAL DE CATEGORÍAS
        if (showManualSearchSheet) {
            ManualCategorySearchSheet(
                allCategories = allCategories,
                onDismiss = { showManualSearchSheet = false },
                onCategorySelected = {
                    selectedCategory = it
                    showManualSearchSheet = false
                }
            )
        }
    }
}

// ==========================================================================================
// --- ALERTAS Y POPUPS CONTEXTUALES ---
// ==========================================================================================

@Composable
fun ContextualWarningBanner(
    isNight: Boolean,
    isRaining: Boolean,
    weatherDesc: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1F26).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.5f)),
        shadowElevation = 10.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFFACC15), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("AVISO DEL SISTEMA FAST", color = Color(0xFFFACC15), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (isNight) {
                    Text("🌙 Horario Nocturno: Solo buscaremos prestadores activos que cuenten con servicio de urgencias 24hs.", color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                }
                if (isRaining) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🌧️ Clima Adverso ($weatherDesc): Es posible que las reparaciones externas se vean demoradas o deban ser reprogramadas por seguridad.", color = Color.LightGray, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp).offset(x = 8.dp, y = (-8).dp)) {
                Icon(Icons.Default.Close, null, tint = Color.Gray)
            }
        }
    }
}

@Composable
fun GoogleAdPopup() {
    Dialog(onDismissRequest = { }, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Texto Tranquilizador
            Text(
                text = "Relájate y mira estas ofertas mientras nosotros buscamos al mejor profesional para ti ☕",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 20.dp, start = 8.dp, end = 8.dp)
            )

            // Tarjeta de Anuncio
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF7F7F7),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        AsyncImage(
                            model = "https://picsum.photos/seed/ad/600/300",
                            contentDescription = "Ad",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(color = Color(0xFFFFC107), shape = RoundedCornerShape(bottomEnd = 12.dp)) {
                            Text("Anuncio", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Surface(color = Color.Black.copy(0.7f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Google Ads", color = Color.White, fontSize = 8.sp)
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("YouTube Premium", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        Text("Disfruta de música y videos sin interrupciones publicitarias. Prueba 1 mes gratis.", fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { /* Abre Link */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1967D2))) {
                            Text("OBTENER OFERTA", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Procesando radar en segundo plano...", color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- MAPA RADAR INTERACTIVO ---
// ==========================================================================================

@Composable
fun TacticalMapBackground(
    isSearching: Boolean,
    searchFinished: Boolean,
    results: List<ProviderWithDistance>,
    onProviderClick: (ProviderWithDistance) -> Unit
) {
    val gridColor = if (isSearching) Color(0xFF22D3EE).copy(0.1f) else Color(0xFF1A1F26)

    Box(modifier = Modifier.fillMaxSize().drawBehind {
        val step = 40.dp.toPx()
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 1f)
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(gridColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f)
        }
    }) {
        // Centro del Radar
        Box(modifier = Modifier.align(Alignment.Center).offset(y = (-50).dp)) {

            if (isSearching) {
                RadarPulse(delay = 0)
                RadarPulse(delay = 1000)
                RadarPulse(delay = 2000)
            } else if (searchFinished) {
                // Anillos estáticos
                Box(modifier = Modifier.size(150.dp).border(1.dp, Color(0xFF22D3EE).copy(0.2f), CircleShape).align(Alignment.Center))
                Box(modifier = Modifier.size(280.dp).border(1.dp, Color(0xFF22D3EE).copy(0.1f), CircleShape).align(Alignment.Center))

                // Pintar Prestadores Interactivos
                results.forEachIndexed { index, data ->
                    // Distribuir en círculo (angulos) y radio según la distancia
                    val angle = (index * (360 / results.size.coerceAtLeast(1))) * (Math.PI / 180)
                    // Radio base 70, escala con distancia (max ~140 para no salirse de pantalla)
                    val radius = 70f + (data.distanceKm * 8).toFloat().coerceAtMost(70f)
                    val offsetX = (cos(angle) * radius).dp
                    val offsetY = (sin(angle) * radius).dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = offsetX, y = offsetY)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onProviderClick(data) }
                            .zIndex(10f) // Asegura que atrape clicks sobre el radar
                    ) {
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(2.dp, Color(0xFF00FFC2)),
                            modifier = Modifier.size(54.dp), // Avatar más grande y visible
                            shadowElevation = 10.dp
                        ) {
                            AsyncImage(
                                model = data.provider.photoUrl,
                                contentDescription = "Avatar ${data.provider.displayName}",
                                contentScale = ContentScale.Crop,
                                fallback = rememberVectorPainter(Icons.Default.Person)
                            )
                        }
                        Surface(
                            color = Color.Black.copy(0.8f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White.copy(0.2f)),
                            modifier = Modifier.padding(top = 4.dp).offset(y = (-8).dp)
                        ) {
                            Text(
                                "${String.format(Locale.getDefault(), "%.1f", data.distanceKm)}km",
                                color = Color(0xFF22D3EE),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Centro: Ubicación del Cliente
            Surface(
                modifier = Modifier.size(40.dp).align(Alignment.Center),
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

// ==========================================================================================
// --- POPUP INTERACTIVO DEL RADAR (DARK GRADIENT) ---
// ==========================================================================================

@Composable
fun InteractiveRadarPopup(
    data: ProviderWithDistance,
    onClose: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    // Fondo invisible que captura clics afuera para cerrar
    Box(
        modifier = Modifier.fillMaxSize().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        // Tarjeta con Gradiente Oscuro estilo Maverick
        Surface(
            modifier = Modifier.width(300.dp).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Evita que clicks internos cierren el modal */ },
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent, // Dejamos el fondo transparente para que se vea el Box inferior
            border = BorderStroke(1.5.dp, Color(0xFF22D3EE).copy(alpha = 0.5f)),
            shadowElevation = 24.dp
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1F26), Color(0xFF05070A))))
            ) {
                // Brillo interno sutil cyan
                Box(modifier = Modifier.matchParentSize().blur(20.dp).background(Color(0xFF22D3EE).copy(alpha = 0.05f)))

                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Header (X + Distancia)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFF10B981).copy(0.2f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFF10B981).copy(0.5f))) {
                            Text("A ${data.estimatedMinutes} min", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        IconButton(onClick = onClose, modifier = Modifier.size(24.dp).background(Color.White.copy(0.1f), CircleShape)) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Avatar central
                    AsyncImage(
                        model = data.provider.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, Color.White.copy(0.2f), CircleShape),
                        contentScale = ContentScale.Crop,
                        fallback = rememberVectorPainter(Icons.Default.Person)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Info del prestador
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(data.provider.displayName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        if (data.provider.isVerified) {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Filled.Verified, null, tint = Color(0xFF9B51E0), modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(data.provider.companies.firstOrNull()?.name ?: "Profesional Independiente", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(data.provider.rating.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(24.dp))

                    // Botones de Acción Táctica
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onProfileClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f))
                        ) {
                            Text("PERFIL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onChatClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2197F5))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("CHATEAR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- PANELES BOTTOM SHEET ---
// ==========================================================================================

@Composable
fun FastResultsPanel(
    results: List<ProviderWithDistance>,
    selectedCategory: CategoryEntity?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onReset: () -> Unit,
    onChatClick: (String) -> Unit,
    onNavigateToNormalSearch: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF111827).copy(alpha = 0.95f), // Ligeramente transparente para no tapar todo el mapa
        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
        shadowElevation = 24.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            // Cabecera clickeable que controla la expansión
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleExpand() }
            ) {
                Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.Gray, CircleShape).align(Alignment.CenterHorizontally))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("RESULTADOS FAST", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("${results.size} prestadores en alerta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        // Feedback visual de expansión
                        Text(if (isExpanded) "Ocultar lista" else "Toca para ver lista", color = Color(0xFF22D3EE), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, null, tint = Color.Gray, modifier = Modifier.padding(end = 16.dp))
                        IconButton(onClick = onReset, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                }
            }

            // Contenido Expandido
            if (isExpanded) {
                HorizontalDivider(color = Color.White.copy(0.05f))

                if (results.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFFACC15), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No hay unidades de emergencia", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Ningún prestador de '${selectedCategory?.name ?: "esta categoría"}' cumple con los requisitos de urgencia en este momento.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { onNavigateToNormalSearch(selectedCategory?.name ?: "Hogar") },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("IR A BÚSQUEDA ESTÁNDAR", fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(0.6f), // Limita altura
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results) { item ->
                            Box {
                                PrestadorCard(
                                    provider = item.provider,
                                    onClick = { onChatClick(item.provider.id) },
                                    onChat = { onChatClick(item.provider.id) },
                                    showPreviews = false
                                )
                                // Superponemos la distancia
                                Surface(
                                    color = Color(0xFF05070A).copy(0.9f),
                                    shape = RoundedCornerShape(bottomStart = 16.dp, topEnd = 16.dp),
                                    border = BorderStroke(1.dp, Color(0xFF22D3EE)),
                                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 2.dp)
                                ) {
                                    Text("A ${String.format(Locale.getDefault(), "%.1f", item.distanceKm)}km", color = Color(0xFF22D3EE), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadarPulse(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val scale by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 4f, animationSpec = infiniteRepeatable(tween(3000, delayMillis = delay, easing = LinearEasing)), label = "scale")
    val alpha by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(3000, delayMillis = delay, easing = LinearEasing)), label = "alpha")
    Box(modifier = Modifier.size(150.dp).graphicsLayer { scaleX = scale; scaleY = scale }.alpha(alpha).border(2.dp, Color(0xFF22D3EE).copy(0.4f), CircleShape))
}

@Composable
fun FastConfigBottomSheet(
    selectedCategory: CategoryEntity?,
    topCategories: List<CategoryEntity>,
    onCategorySelect: (CategoryEntity) -> Unit,
    onOpenManualSearch: () -> Unit,
    onStartSearch: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
        shadowElevation = 24.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Maverick FAST", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF22D3EE))
                    Text("Busca el servicio de emergencia", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    onClick = onOpenManualSearch,
                    shape = CircleShape,
                    color = Color.White.copy(0.05f),
                    border = BorderStroke(1.dp, Color(0xFF2197F5))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.Search, null, tint = Color(0xFF2197F5), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Búsqueda Manual", color = Color(0xFF2197F5), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("MÁS UTILIZADOS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val displayList = if (selectedCategory != null && !topCategories.contains(selectedCategory)) {
                    listOf(selectedCategory) + topCategories.take(3)
                } else {
                    topCategories
                }

                items(displayList) { cat ->
                    val isSelected = cat.name == selectedCategory?.name
                    Surface(
                        onClick = { onCategorySelect(cat) },
                        modifier = Modifier.size(width = 80.dp, height = 90.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(cat.color).copy(alpha = 0.2f) else Color.White.copy(0.03f),
                        border = BorderStroke(1.dp, if (isSelected) Color(cat.color) else Color.White.copy(0.1f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(cat.icon, fontSize = 28.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(cat.name.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color.White else Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
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
                Text("SOLICITAR ASISTENCIA AHORA", color = Color(0xFF05070A), fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualCategorySearchSheet(
    allCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onCategorySelected: (CategoryEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val prefixMatches = remember(searchQuery, allCategories) { allCategories.filter { it.name.startsWith(searchQuery, ignoreCase = true) } }
    val approximateMatches = remember(searchQuery, allCategories) { allCategories.filter { it.name.contains(searchQuery, ignoreCase = true) && !it.name.startsWith(searchQuery, ignoreCase = true) } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0E14),
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Text("Selecciona una Categoría", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 16.dp))

            GeminiTopSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                placeholderText = "Escribe el oficio o servicio..."
            )

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (prefixMatches.isEmpty() && approximateMatches.isEmpty() && searchQuery.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { Text("No se encontraron resultados", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, color = Color.Gray) }
                } else if (searchQuery.isEmpty()) {
                    items(allCategories) { category ->
                        CompactCategoryCard(item = category, onClick = { onCategorySelected(category) })
                    }
                } else {
                    if (prefixMatches.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) { Text("Coincidencia Exacta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF22D3EE), modifier = Modifier.padding(bottom = 8.dp)) }
                        items(prefixMatches) { category -> CompactCategoryCard(item = category, onClick = { onCategorySelected(category) }) }
                    }
                    if (approximateMatches.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) { Text("Resultados relacionados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF22D3EE), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                        items(approximateMatches) { category -> CompactCategoryCard(item = category, onClick = { onCategorySelected(category) }) }
                    }
                }
            }
        }
    }
}