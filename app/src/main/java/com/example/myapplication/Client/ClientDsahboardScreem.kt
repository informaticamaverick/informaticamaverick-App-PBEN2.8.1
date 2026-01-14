package com.example.myapplication.Client

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
    onNavigateToChat: () -> Unit = {}
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
    val categoryViewModel: com.example.myapplication.ViewModel.CategoryViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    
    // ViewModel de subcategorías para Firebase
    val subCategoryViewModel: com.example.myapplication.ViewModel.SubCategoryViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    
    // ViewModel de perfil para obtener la foto del usuario
    val profileViewModel: com.example.myapplication.Profile.ProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()
    
    // Scope para coroutines
    val coroutineScope = rememberCoroutineScope()
    
    // Estado para la búsqueda
    var searchText by remember { mutableStateOf("") }
    
    // Estado para navegación a resultados
    var showSearchResults by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    
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
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // --- HEADER FIJO ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = surfaceColor,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Fila: Ubicación y botón Salir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ubicación
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
                            Text(
                                text = "▼",
                                fontSize = 10.sp,
                                color = textSecondaryColor
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Indicador del clima
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        surfaceColor,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = weatherEmoji,
                                    fontSize = 50.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = temperature,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimaryColor
                                )
                            }
                        }




                    }
                    
                    // Notificaciones, mensajes y menú de perfil
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de mensajes
                        IconButton(
                            onClick = onNavigateToChat,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Mensajes",
                                tint = textSecondaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Botón de notificaciones con badge
                        Box {
                            IconButton(
                                onClick = { /* TODO: Abrir notificaciones */ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // Badge rojo
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .offset(x = 26.dp, y = 6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                        }
                        
                        // Menú de perfil desplegable
                        var showMenu by remember { mutableStateOf(false) }
                        
                        // Obtener el nombre para mostrar
                        val displayName = when {
                            userName.isNotEmpty() -> userName
                            profileUiState.displayName.isNotEmpty() -> profileUiState.displayName
                            else -> "U"
                        }
                        
                        Box {
                            // Avatar con foto de perfil o inicial
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6))
                                    .clickable { showMenu = !showMenu },
                                contentAlignment = Alignment.Center
                            ) {
                                // Texto siempre presente (inicial)
                                Text(
                                    text = displayName.firstOrNull()?.uppercase()?.toString() ?: "U",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                // Foto encima si existe
                                if (profileUiState.photoUrl.isNotEmpty()) {
                                    androidx.compose.foundation.Image(
                                        painter = rememberAsyncImagePainter(profileUiState.photoUrl),
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }

                        // Menú desplegable
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .width(200.dp)
                                .background(surfaceColor, RoundedCornerShape(12.dp))
                        ) {
                            // Encabezado del menú con nombre
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = userName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimaryColor
                                )
                                Text(
                                    text = "Cliente",
                                    fontSize = 12.sp,
                                    color = textSecondaryColor
                                )
                            }
                            
                            Divider(color = textSecondaryColor.copy(alpha = 0.2f))
                            
                            // Opción: Mis Datos
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Mis Datos",
                                            tint = textSecondaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Mis Datos",
                                            fontSize = 14.sp,
                                            color = textPrimaryColor
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToProfile()
                                }
                            )
                            
                            // Opción: Configuración
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Configuración",
                                            tint = textSecondaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Configuración",
                                            fontSize = 14.sp,
                                            color = textPrimaryColor
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    // TODO: Navegar a Configuración
                                }
                            )
                            
                            Divider(color = textSecondaryColor.copy(alpha = 0.2f))
                            
                            // Opción: Cerrar Sesión
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Cerrar Sesión",
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
                
                // Saludo
                Text(
                    text = GreetingUtils.getGreetingMessage(
                        userName = userName,
                        location = locationName,
                        greetingType = greetingType
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Buscador estilo Google
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = surfaceColor,
                            shadowElevation = 4.dp,
                            tonalElevation = 0.dp
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
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                androidx.compose.foundation.text.BasicTextField(
                                    value = searchText,
                                    onValueChange = { searchText = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 16.sp,
                                        color = textPrimaryColor,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { innerTextField ->
                                        if (searchText.isEmpty()) {
                                            Text(
                                                text = "Buscar servicios",
                                                fontSize = 16.sp,
                                                color = textSecondaryColor
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = { searchText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar búsqueda",
                                            tint = textSecondaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Dropdown de resultados
                        if (searchText.isNotEmpty()) {
                            // Preparar categorías para búsqueda
                            val searchCategories = listOf(
                                Triple(R.drawable.ic_electricista, "Electricista", Color(0xFFF59E0B)),
                                Triple(R.drawable.ic_plomero, "Plomería", Color(0xFF3B82F6)),
                                Triple(R.drawable.ic_pintura, "Pintura", Color(0xFFEC4899)),
                                Triple(R.drawable.ic_mudanza, "Mudanzas", Color(0xFF10B981)),
                                Triple(R.drawable.ic_limpieza, "Limpieza", Color(0xFF8B5CF6)),
                                Triple(R.drawable.ic_jardin, "Paisajismo", Color(0xFF16A34A)),
                                Triple(R.drawable.ic_mecanico, "Reparaciones", Color(0xFF0369A1)),
                                Triple(R.drawable.ic_mudanza, "Transporte", Color(0xFF059669)),
                                Triple(R.drawable.ic_albanil, "Construcción", Color(0xFFEA580C)),
                                Triple(R.drawable.ic_electricista, "Refrigeración", Color(0xFF0284C7)),
                                Triple(R.drawable.ic_otros, "Otros", Color(0xFF9CA3AF))
                            )
                            
                            val categoriesResults = searchCategories.filter { 
                                it.second.contains(searchText, ignoreCase = true) 
                            }
                            
                            val professionalsResults = listOf(
                                Quadruple("Carlos Ruiz", "Electricista Master", Color(0xFFF59E0B), "4.9", "120", 8),
                                Quadruple("Ana López", "Limpieza Profunda", Color(0xFF8B5CF6), "5.0", "85", 5),
                                Quadruple("Mario Bross", "Plomero Certificado", Color(0xFFEF4444), "4.8", "210", 3),
                                Quadruple("Luis García", "Pintor Profesional", Color(0xFFEC4899), "4.7", "95", 4),
                                Quadruple("Pedro Martínez", "Jardinero Experto", Color(0xFF84CC16), "4.9", "150", 6)
                            ).filter { 
                                it.first.contains(searchText, ignoreCase = true) || 
                                it.second.contains(searchText, ignoreCase = true) 
                            }
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = surfaceColor,
                                shadowElevation = 8.dp,
                                tonalElevation = 2.dp
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    // Mostrar categorías
                                    if (categoriesResults.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Categorías",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textSecondaryColor,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                        }
                                        
                                        items(categoriesResults.size) { index ->
                                            val category = categoriesResults[index]
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedCategory = category.second
                                                        showSearchResults = true
                                                        searchText = ""
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = category.first),
                                                    contentDescription = category.second,
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = category.second,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = textPrimaryColor
                                                )
                                            }
                                            if (index < categoriesResults.size - 1 || professionalsResults.isNotEmpty()) {
                                                HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.2f), thickness = 1.dp)
                                            }
                                        }
                                    }
                                    
                                    // Mostrar profesionales
                                    if (professionalsResults.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Profesionales",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textSecondaryColor,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                        }
                                        
                                        items(professionalsResults.size) { index ->
                                            val professional = professionalsResults[index]
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    modifier = Modifier.size(40.dp),
                                                    shape = CircleShape,
                                                    color = professional.color.copy(alpha = 0.1f)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = professional.first.first().toString(),
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = professional.color
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = professional.first,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = textPrimaryColor
                                                    )
                                                    Text(
                                                        text = professional.second,
                                                        fontSize = 12.sp,
                                                        color = textSecondaryColor
                                                    )
                                                }
                                                Spacer(modifier = Modifier.weight(1f))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFBBF24),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = professional.third.toString(),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = textPrimaryColor
                                                    )
                                                }
                                            }
                                            if (index < professionalsResults.size - 1) {
                                                HorizontalDivider(color = textSecondaryColor.copy(alpha = 0.2f), thickness = 1.dp)
                                            }
                                        }
                                    }
                                    
                                    // Sin resultados
                                    if (categoriesResults.isEmpty() && professionalsResults.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = null,
                                                        tint = textSecondaryColor,
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "No se encontraron resultados",
                                                        fontSize = 14.sp,
                                                        color = textSecondaryColor,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        


        
        // Contenido scrollable con scrollbar
        val scrollState = rememberScrollState()
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
            // Cargar categorías desde Firebase al iniciar
            val firebaseCategories by categoryViewModel.categories.collectAsState()
            val isLoadingCategories by categoryViewModel.isLoading.collectAsState()
            
            // Título de Categorías
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
                    Text(
                        text = "Ver todas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3B82F6)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mapeo de iconos desde Firebase
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
            
            // Usar categorías de Firebase si existen, sino usar las locales
            val allCategories = if (firebaseCategories.isNotEmpty()) {
                // Convertir categorías de Firebase al formato local
                firebaseCategories.map { category ->
                    val iconRes = iconMap[category.iconName] ?: R.drawable.ic_otros
                    val color = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        Color(0xFF9CA3AF)
                    }
                    Triple(iconRes, category.name, color)
                }
            } else {
                // Categorías locales como fallback
                listOf(
                    Triple(R.drawable.ic_electricista, "Electricista", Color(0xFFFBBF24)),
                    Triple(R.drawable.ic_plomero, "Plomero", Color(0xFF06B6D4)),
                    Triple(R.drawable.ic_pintura, "Pintura", Color(0xFFEC4899)),
                    Triple(R.drawable.ic_mudanza, "Mudanza", Color(0xFF10B981)),
                    Triple(R.drawable.ic_limpieza, "Limpieza", Color(0xFF8B5CF6)),
                    Triple(R.drawable.ic_jardin, "Jardín", Color(0xFF84CC16)),
                    Triple(R.drawable.ic_mecanico, "Mecánico", Color(0xFF475569)),
                    Triple(R.drawable.ic_albanil, "Albañilería", Color(0xFFF97316)),
                    Triple(R.drawable.ic_electricista, "Carpintería", Color(0xFF92400E)),
                    Triple(R.drawable.ic_plomero, "Cerrajería", Color(0xFF78350F)),
                    Triple(R.drawable.ic_pintura, "Decoración", Color(0xFFDB2777)),
                    Triple(R.drawable.ic_limpieza, "Lavandería", Color(0xFF0891B2)),
                    Triple(R.drawable.ic_jardin, "Paisajismo", Color(0xFF16A34A)),
                    Triple(R.drawable.ic_mecanico, "Reparaciones", Color(0xFF0369A1)),
                    Triple(R.drawable.ic_mudanza, "Transporte", Color(0xFF059669)),
                    Triple(R.drawable.ic_albanil, "Construcción", Color(0xFFEA580C)),
                    Triple(R.drawable.ic_electricista, "Refrigeración", Color(0xFF0284C7)),
                    Triple(R.drawable.ic_otros, "Otros", Color(0xFF9CA3AF))
                )
            }
            
            // Mostrar indicador de carga o grid
            if (isLoadingCategories && firebaseCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            } else {
                // Grid de Categorías con scroll vertical
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(allCategories.size) { index ->
                        val category = allCategories[index]
                        CategoryItem(
                            iconRes = category.first,
                            label = category.second,
                            color = category.third,
                            onClick = {
                                selectedCategory = category.second
                                showSearchResults = true
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Título de Profesionales más usados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tus favoritos ⭐",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor
                )
                
                TextButton(onClick = { }) {
                    Text(
                        text = "Ver todos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3B82F6)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Definir todos los profesionales
            val allProfessionals = listOf(
                Quadruple("Carlos Ruiz", "Electricista Master", Color(0xFFF59E0B), "4.9", "120", 8),
                Quadruple("Ana López", "Limpieza Profunda", Color(0xFF8B5CF6), "5.0", "85", 5),
                Quadruple("Mario Bross", "Plomero Certificado", Color(0xFFEF4444), "4.8", "210", 3),
                Quadruple("Luis García", "Pintor Profesional", Color(0xFFEC4899), "4.7", "95", 4),
                Quadruple("Pedro Martínez", "Jardinero Experto", Color(0xFF84CC16), "4.9", "150", 6)
            )
            
            // Grid de profesionales favoritos en 2 columnas
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(allProfessionals.size) { index ->
                    val pro = allProfessionals[index]
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
            }
            }
        }
        }
        
        // Bottom Navigation Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Inicio",
                    isSelected = true
                )
                
                // Presupuesto con ícono personalizado de papel y lápiz
                BottomNavItemCustom(
                    painter = painterResource(id = R.drawable.ic_presupuesto),
                    label = "Presupuesto",
                    isSelected = false
                )
                
                BottomNavItem(
                    icon = Icons.Default.DateRange,
                    label = "Pedidos"
                )
                
                BottomNavItem(
                    icon = Icons.Default.Email,
                    label = "Chat",
                    onClick = onNavigateToChat
                )
                
                // Promociones con ícono personalizado de porcentaje
                BottomNavItemCustom(
                    painter = painterResource(id = R.drawable.ic_percent),
                    label = "Promociones",
                    isSelected = false
                )
            }
        }
    }
    
    // Mostrar pantalla de resultados
    if (showSearchResults) {
        SearchResultsScreen(
            category = selectedCategory,
            onBack = { showSearchResults = false }
        )
    }
}