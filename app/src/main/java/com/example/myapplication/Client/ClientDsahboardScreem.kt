package com.example.myapplication.Client

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.annotation.DrawableRes
import com.example.myapplication.R
import com.example.myapplication.Utils.GreetingUtils
import com.example.myapplication.ViewModel.WeatherViewModel
import com.example.myapplication.Utils.GreetingType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ViewModel.LocationViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.myapplication.Client.SearchResultsScreen
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll

// Data class para almacenar información de profesionales
data class Quadruple<A, B, C, D, E, F>(
    val first: A,
    val second: B,
    val color: C,
    val third: D,
    val fourth: E,
    val fifth: F
)

@Composable
fun CategoryItem(
    @DrawableRes iconRes: Int,
    label: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) {
        Color(0xFF1E293B)
    } else {
        Color.White
    }
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    // Color del icono adaptado al tema
    val iconTint = if (isDarkTheme) color.copy(alpha = 0.9f) else color

    // Estado para la animación de presión
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.75f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "elevation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .padding(4.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
    ) {
        Surface(
            modifier = Modifier
                .size(60.dp),
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            shadowElevation = elevation,
            tonalElevation = 1.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (isDarkTheme) {
                            color.copy(alpha = 0.15f)
                        } else {
                            color.copy(alpha = 0.1f)
                        }
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 2,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProCard(
    name: String,
    job: String,
    rating: String,
    reviews: String,
    avatarColor: Color,
    timesUsed: Int = 0,
    onClick: () -> Unit = {},
    surfaceColor: Color = Color.White,
    textPrimaryColor: Color = Color(0xFF1E293B),
    textSecondaryColor: Color = Color(0xFF64748B)
) {
    // Determinar el ícono según la categoría
    val categoryIcon = when {
        job.contains("Electricista", ignoreCase = true) -> R.drawable.ic_electricista
        job.contains("Plomero", ignoreCase = true) -> R.drawable.ic_plomero
        job.contains("Pintura", ignoreCase = true) || job.contains("Pintor", ignoreCase = true) -> R.drawable.ic_pintura
        job.contains("Limpieza", ignoreCase = true) -> R.drawable.ic_limpieza
        job.contains("Albañil", ignoreCase = true) -> R.drawable.ic_albanil
        job.contains("Mecánico", ignoreCase = true) -> R.drawable.ic_mecanico
        job.contains("Jardín", ignoreCase = true) -> R.drawable.ic_jardin
        job.contains("Mudanza", ignoreCase = true) -> R.drawable.ic_mudanza
        else -> R.drawable.ic_otros
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp) // Espacio entre cada item de la lista
    ) {
        // --- 1. CAPA INFERIOR: La Tarjeta Grande (Perfil) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp) // Bajamos esta tarjeta para que la de arriba "monte" el borde
                .zIndex(0f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            // Contenido de la tarjeta grande
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 24.dp // Damos espacio interno extra arriba para que el texto no choque con la etiqueta
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar (Círculo)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.first().toString(),
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Textos (Nombre y Rating)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Row de estrellas/rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$rating ($reviews reviews)",
                            fontSize = 12.sp,
                            color = textSecondaryColor
                        )
                    }
                }
            }
        }

        // --- 2. CAPA SUPERIOR: La Tarjeta Pequeña (Categoría) ---
        Card(
            modifier = Modifier
                .fillMaxWidth() // Mismo ancho que la tarjeta del perfil
                .align(Alignment.TopStart) // Pegamos al techo de la Box
                .zIndex(1f)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                    clip = false
                ), // Sombra solo en las puntas de arriba
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono pequeño
                Icon(
                    painter = painterResource(id = categoryIcon),
                    contentDescription = null,
                    tint = avatarColor, // Usamos el color del tema
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = job,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor
                )
            }
        }
    }
}



@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    badge: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Animación de escala al hacer clic
    var clicked by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (clicked) 1.2f else if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Animación de color
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF3B82F6) else Color(0xFF94A3B8),
        animationSpec = tween(300),
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF3B82F6) else Color(0xFF94A3B8),
        animationSpec = tween(300),
        label = "textColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable {
                clicked = true
                onClick()
            }
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )

            // Badge de notificaciones
            if (badge) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(x = 12.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }

    // Reset del estado clicked después de la animación
    LaunchedEffect(clicked) {
        if (clicked) {
            kotlinx.coroutines.delay(150)
            clicked = false
        }
    }
}

@Composable
fun BottomNavItemCustom(
    painter: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Animación de escala al hacer clic
    var clicked by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (clicked) 1.2f else if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Animación de color
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF3B82F6) else Color(0xFF94A3B8),
        animationSpec = tween(300),
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF3B82F6) else Color(0xFF94A3B8),
        animationSpec = tween(300),
        label = "textColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable {
                clicked = true
                onClick()
            }
    ) {
        Icon(
            painter = painter,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }

    // Reset del estado clicked después de la animación
    LaunchedEffect(clicked) {
        if (clicked) {
            kotlinx.coroutines.delay(150)
            clicked = false
        }
    }
}







@Composable
fun ClientDashboardScreen(
    userName: String = "Invitado",
    location: String = "Buenos Aires, AR",
    greetingType: GreetingType = GreetingType.FRIENDLY,
    onLogout: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // Colores adaptados al tema
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val surfaceColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimaryColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val textSecondaryColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    val locationViewModel = remember {
        LocationViewModel(context)
    }
    val locationName by locationViewModel.locationName.collectAsState()
    val latitude by locationViewModel.latitude.collectAsState()
    val longitude by locationViewModel.longitude.collectAsState()

    //ViewModel del clima
    val weatherViewModel: WeatherViewModel = viewModel()
    val temperature by weatherViewModel.temperature.collectAsState()
    val weatherEmoji by weatherViewModel.weatherEmoji.collectAsState()

    // ViewModel de categorías para Firebase
    val categoryViewModel: com.example.myapplication.ViewModel.CategoryViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()

    // ViewModel de subcategorías para Firebase
    val subCategoryViewModel: com.example.myapplication.ViewModel.SubCategoryViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()

    // ViewModel de perfil para obtener la foto del usuario
    val profileViewModel: com.example.myapplication.Profile.ProfileViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()

    // Scope para coroutines
    val coroutineScope = rememberCoroutineScope()

    // Estado para la búsqueda
    var searchText by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }

    // Estado para navegación a resultados
    var showSearchResults by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var showBiddingScreen by remember { mutableStateOf(false) }

    // Determinar si el buscador debe estar expandido
    val shouldExpand = isSearchExpanded || isSearchFocused || searchText.isNotEmpty()

    // Estado para el FAB (botones flotantes)
    var isFabOpen by remember { mutableStateOf(false) }

    // Solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            locationViewModel.fetchLocation()
        }
    }

    // Actualizar clima cuando cambien las coordenadas
    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            weatherViewModel.fetchWeather(lat = latitude!!, lon = longitude!!)
        } else {
            // Coordenadas de Bogotá por defecto
            weatherViewModel.fetchWeather(lat = 4.7110, lon = -74.0721)
        }
    }


    // Manejar el botón "Atrás" del sistema - En el dashboard no permite volver atrás
    // El usuario debe usar el botón de cerrar sesión
    BackHandler {
        // No hacer nada - prevenir que vuelva al login
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (shouldExpand) {
                            isSearchExpanded = false
                            isSearchFocused = false
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Espaciado superior
            Spacer(modifier = Modifier.height(16.dp))

            // HEADER: UBICACIÓN Y PERFIL
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f),
                color = surfaceColor,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Ubicación",
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ubicación actual",
                                    fontSize = 11.sp,
                                    color = textSecondaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = locationName,
                                    fontSize = 13.sp,
                                    color = textPrimaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "▼", fontSize = 10.sp, color = textSecondaryColor)
                                Spacer(modifier = Modifier.width(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(
                                            if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = weatherEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = temperature,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimaryColor
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                IconButton(
                                    onClick = { },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notificaciones",
                                        tint = textSecondaryColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-6).dp, y = 6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                            }

                            var showMenu by remember { mutableStateOf(false) }
                            val displayName = when {
                                userName.isNotEmpty() -> userName
                                profileUiState.displayName.isNotEmpty() -> profileUiState.displayName
                                else -> "U"
                            }

                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B82F6))
                                        .clickable { showMenu = !showMenu },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayName.firstOrNull()?.uppercase()?.toString()
                                            ?: "U",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (profileUiState.photoUrl.isNotEmpty()) {
                                        androidx.compose.foundation.Image(
                                            painter = rememberAsyncImagePainter(profileUiState.photoUrl),
                                            contentDescription = "Foto",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(surfaceColor)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = textPrimaryColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Ver Perfil",
                                                    fontSize = 14.sp,
                                                    color = textPrimaryColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            onNavigateToProfile()
                                        }
                                    )

                                    HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.2f))

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = null,
                                                    tint = textPrimaryColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Administrar",
                                                    fontSize = 14.sp,
                                                    color = textPrimaryColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            onNavigateToAdmin()
                                        }
                                    )

                                    HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.2f))

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ExitToApp,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Cerrar Sesión",
                                                    fontSize = 14.sp,
                                                    color = Color(0xFFEF4444),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            onLogout()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // CONTENIDO PRINCIPAL
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 80.dp),
                ) {
                    val firebaseCategories by categoryViewModel.categories.collectAsState()
                    val subCategoryViewModel: com.example.myapplication.ViewModel.SubCategoryViewModel =
                        hiltViewModel()

                    // BUSCADOR DE SERVICIOS
                    val searchBarWidth by animateFloatAsState(
                        targetValue = if (shouldExpand) 1f else 0.70f,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        label = "searchBarWidth"
                    )
                    val searchBarHeight by animateDpAsState(
                        targetValue = if (shouldExpand) 56.dp else 48.dp,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        label = "searchBarHeight"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .zIndex(10f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // Buscador a la izquierda
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(2f)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(searchBarWidth)
                                    .height(searchBarHeight),
                                shape = RoundedCornerShape(24.dp),
                                color = if (shouldExpand) {
                                    if (isDarkTheme) Color(0xFF1E293B) else Color.White
                                } else {
                                    surfaceColor
                                },
                                shadowElevation = if (shouldExpand) 8.dp else 4.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    if (!shouldExpand) {
                                                        isSearchFocused = true
                                                        isSearchExpanded = true
                                                    }
                                                }
                                            )
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Buscar",
                                            tint = textSecondaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            androidx.compose.foundation.text.BasicTextField(
                                                value = searchText,
                                                onValueChange = { searchText = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                textStyle = androidx.compose.ui.text.TextStyle(
                                                    fontSize = 14.sp,
                                                    color = textPrimaryColor,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                cursorBrush = androidx.compose.ui.graphics.SolidColor(
                                                    Color(0xFF3B82F6)
                                                ),
                                                decorationBox = { innerTextField ->
                                                    Box(modifier = Modifier.fillMaxWidth()) {
                                                        if (searchText.isEmpty()) {
                                                            Text(
                                                                text = "Buscar servicios...",
                                                                fontSize = 14.sp,
                                                                color = textSecondaryColor.copy(
                                                                    alpha = 0.6f
                                                                ),
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                        innerTextField()
                                                    }
                                                }
                                            )
                                        }
                                        if (searchText.isNotEmpty() || shouldExpand) {
                                            IconButton(onClick = {
                                                if (searchText.isNotEmpty()) searchText = ""
                                                else {
                                                    isSearchExpanded = false
                                                    isSearchFocused = false
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Cerrar",
                                                    tint = textSecondaryColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Resultados de búsqueda
                            androidx.compose.animation.AnimatedVisibility(
                                visible = searchText.isNotEmpty(),
                                enter = androidx.compose.animation.fadeIn(
                                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                                ) + androidx.compose.animation.expandVertically(
                                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                                ),
                                exit = androidx.compose.animation.fadeOut(
                                    animationSpec = tween(150)
                                ) + androidx.compose.animation.shrinkVertically(
                                    animationSpec = tween(150)
                                )
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val searchCategories = listOf(
                                        Triple(
                                            R.drawable.ic_electricista,
                                            "Electricista",
                                            Color(0xFFFBBF24)
                                        ),
                                        Triple(R.drawable.ic_plomero, "Plomero", Color(0xFF06B6D4)),
                                        Triple(R.drawable.ic_pintura, "Pintura", Color(0xFFEC4899)),
                                        Triple(
                                            R.drawable.ic_limpieza,
                                            "Limpieza",
                                            Color(0xFF8B5CF6)
                                        ),
                                        Triple(R.drawable.ic_jardin, "Jardín", Color(0xFF84CC16)),
                                        Triple(
                                            R.drawable.ic_mecanico,
                                            "Mecánico",
                                            Color(0xFF475569)
                                        )
                                    )

                                    val filteredResults = searchCategories.filter {
                                        it.second.contains(searchText, ignoreCase = true)
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = surfaceColor,
                                        shadowElevation = 8.dp
                                    ) {
                                        if (filteredResults.isNotEmpty()) {
                                            Column {
                                                filteredResults.forEach { (icon, name, color) ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                selectedCategory = name
                                                                showSearchResults = true
                                                                searchText = ""
                                                            }
                                                            .padding(
                                                                horizontal = 16.dp,
                                                                vertical = 12.dp
                                                            ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .background(
                                                                    color.copy(alpha = 0.15f),
                                                                    CircleShape
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                painter = painterResource(id = icon),
                                                                contentDescription = name,
                                                                tint = color,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text(
                                                            text = name,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = textPrimaryColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // BOTONES DE ACCIÓN RÁPIDA - A la derecha del buscador
                        // Animación para ocultar botones cuando el buscador se expande
                        val buttonsOpacity by animateFloatAsState(
                            targetValue = if (shouldExpand) 0f else 1f,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            ),
                            label = "buttonsOpacity"
                        )
                        val buttonsTranslationX by animateDpAsState(
                            targetValue = if (shouldExpand) 40.dp else 0.dp,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            ),
                            label = "buttonsTranslationX"
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp)
                                .zIndex(1f)
                                .offset(x = buttonsTranslationX)
                                .graphicsLayer {
                                    alpha = buttonsOpacity
                                },
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón Licitaciones
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .clickable { showBiddingScreen = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF6366F1),
                                    shadowElevation = 2.dp
                                ) {
                                    Box(
                                        modifier = Modifier.padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = "Licitaciones",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Licitar",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimaryColor
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Botón Servicio Fast
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .clickable { /* Servicio Fast */ },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFACC15),
                                    shadowElevation = 2.dp
                                ) {
                                    Box(
                                        modifier = Modifier.padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Servicio Fast",
                                            tint = Color(0xFF713F12),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Fast",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimaryColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categorías",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor
                        )
                        TextButton(onClick = { }) {
                            Text("Ver todas", color = Color(0xFF3B82F6))
                        }
                    }

                    val iconMap = mapOf(
                        "ic_electricista" to R.drawable.ic_electricista,
                        "ic_plomero" to R.drawable.ic_plomero,
                        "ic_pintura" to R.drawable.ic_pintura,
                        "ic_mudanza" to R.drawable.ic_mudanza,
                        "ic_limpieza" to R.drawable.ic_limpieza,
                        "ic_jardin" to R.drawable.ic_jardin,
                        "ic_mecanico" to R.drawable.ic_mecanico,
                        "ic_albanil" to R.drawable.ic_albanil,
                        "ic_otros" to R.drawable.ic_otros
                    )

                    val categoriesToShow = if (firebaseCategories.isNotEmpty()) {
                        firebaseCategories.map {
                            val color = try {
                                Color(android.graphics.Color.parseColor(it.colorHex))
                            } catch (e: Exception) {
                                Color(0xFF9CA3AF)
                            }
                            Triple(iconMap[it.iconName] ?: R.drawable.ic_otros, it.name, color)
                        }
                    } else {
                        listOf(
                            Triple(R.drawable.ic_electricista, "Electricista", Color(0xFFFBBF24)),
                            Triple(R.drawable.ic_plomero, "Plomero", Color(0xFF06B6D4)),
                            Triple(R.drawable.ic_pintura, "Pintura", Color(0xFFEC4899)),
                            Triple(R.drawable.ic_limpieza, "Limpieza", Color(0xFF8B5CF6)),
                            Triple(R.drawable.ic_jardin, "Jardín", Color(0xFF84CC16)),
                            Triple(R.drawable.ic_mecanico, "Mecánico", Color(0xFF475569))
                        )
                    }

                    // Grid de categorías con scroll independiente
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .nestedScroll(remember {
                                object : NestedScrollConnection {
                                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                        return Offset.Zero
                                    }
                                }
                            }),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(categoriesToShow.size) { index ->
                            val cat = categoriesToShow[index]
                            CategoryItem(
                                cat.first,
                                cat.second,
                                cat.third
                            ) {
                                selectedCategory = cat.second
                                showSearchResults = true
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Tus favoritos ⭐",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val allProfessionals = listOf(
                        Quadruple(
                            "Carlos Ruiz",
                            "Electricista Master",
                            Color(0xFFF59E0B),
                            "4.9",
                            "120",
                            8
                        ),
                        Quadruple(
                            "Ana López",
                            "Limpieza Profunda",
                            Color(0xFF8B5CF6),
                            "5.0",
                            "85",
                            5
                        ),
                        Quadruple(
                            "Mario Bross",
                            "Plomero Certificado",
                            Color(0xFFEF4444),
                            "4.8",
                            "210",
                            3
                        ),
                        Quadruple(
                            "Luis García",
                            "Pintor Profesional",
                            Color(0xFFEC4899),
                            "4.7",
                            "95",
                            4
                        )
                    )

                    // Grid de favoritos con scroll independiente
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Calcular filas necesarias (2 columnas por fila)
                            val rows = (allProfessionals.size + 1) / 2
                            repeat(rows) { rowIndex ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    repeat(2) { colIndex ->
                                        val index = rowIndex * 2 + colIndex
                                        if (index < allProfessionals.size) {
                                            val pro = allProfessionals[index]
                                            Box(modifier = Modifier.weight(1f)) {
                                                ProCard(
                                                    name = pro.first,
                                                    job = pro.second,
                                                    rating = pro.third,
                                                    reviews = pro.fourth,
                                                    avatarColor = pro.color,
                                                    timesUsed = pro.fifth,
                                                    surfaceColor = surfaceColor,
                                                    textPrimaryColor = textPrimaryColor,
                                                    textSecondaryColor = textSecondaryColor
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }


                }
            }
        }

        // BARRA DE NAVEGACIÓN INFERIOR
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(Icons.Default.Home, "Inicio", true)
                BottomNavItemCustom(
                    painterResource(R.drawable.ic_presupuesto),
                    "Presupuesto",
                    false
                )
                BottomNavItem(
                    Icons.Default.DateRange,
                    "Calendario",
                    false,
                    onClick = onNavigateToCalendar
                )
                BottomNavItem(Icons.Default.Email, "Chat", false, onClick = onNavigateToChat)
                BottomNavItemCustom(painterResource(R.drawable.ic_percent), "Promociones", false)
            }
        }
    }

    // Mostrar pantalla de licitaciones
    if (showBiddingScreen) {
        BiddingScreen(
            onBack = { showBiddingScreen = false }
        )
    }

    // Mostrar pantalla de resultados (fuera del Box principal)
    if (showSearchResults) {
        SearchResultsScreen(
            category = selectedCategory,
            onBack = { showSearchResults = false }
        )
    }
}
