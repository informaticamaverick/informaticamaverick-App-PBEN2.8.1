package com.example.myapplication.Client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import com.example.myapplication.Data.Model.Prestador
import com.example.myapplication.Data.Model.PrestadoresData
import com.example.myapplication.Data.Model.SubscriptionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    category: String,
    onBack: () -> Unit
) {
    // Manejar el botón "Atrás" del sistema (hardware o gestos)
    BackHandler {
        onBack()
    }
    
    // Colores adaptativos
    val appColors = com.example.myapplication.ui.theme.getAppColors()
    
    // Filtrar prestadores por categoría
    val filteredPrestadores = remember(category) {
        PrestadoresData.filterByCategory(category)
    }
    
    // Estado para la pestaña seleccionada (0 = Premium, 1 = Básico)
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Estado para el filtro de ordenamiento
    var sortOrder by remember { mutableStateOf("rating") } // "rating", "distance", "alphabetic"
    var showFilterDialog by remember { mutableStateOf(false) }
    
    // Estado para filtros adicionales
    var showAdvancedFilters by remember { mutableStateOf(false) }
    var tempFilterDomicilio by remember { mutableStateOf(false) }
    var tempFilterMatricula by remember { mutableStateOf(false) }
    var filterDomicilio by remember { mutableStateOf(false) }
    var filterMatricula by remember { mutableStateOf(false) }
    
    // Separar prestadores por suscripción
    val premiumPrestadores = remember(filteredPrestadores) {
        PrestadoresData.filterBySubscription(filteredPrestadores, SubscriptionType.PREMIUM)
    }
    
    val basicPrestadores = remember(filteredPrestadores) {
        PrestadoresData.filterBySubscription(filteredPrestadores, SubscriptionType.BASIC)
    }
    
    // Aplicar ordenamiento
    val sortedPremiumPrestadores = remember(premiumPrestadores, sortOrder) {
        when (sortOrder) {
            "alphabetic" -> premiumPrestadores.sortedBy { it.name }
            "distance" -> premiumPrestadores.sortedBy { it.distance }
            else -> premiumPrestadores.sortedByDescending { it.rating }
        }
    }
    
    val sortedBasicPrestadores = remember(basicPrestadores, sortOrder) {
        when (sortOrder) {
            "alphabetic" -> basicPrestadores.sortedBy { it.name }
            "distance" -> basicPrestadores.sortedBy { it.distance }
            else -> basicPrestadores.sortedByDescending { it.rating }
        }
    }
    
    // Aplicar filtros avanzados
    val filteredPremiumPrestadores = remember(sortedPremiumPrestadores, filterDomicilio, filterMatricula) {
        var result = sortedPremiumPrestadores
        if (filterDomicilio) {
            result = result.filter { it.atiendeDomicilio }
        }
        if (filterMatricula) {
            result = result.filter { it.tieneMatricula }
        }
        result
    }
    
    val filteredBasicPrestadores = remember(sortedBasicPrestadores, filterDomicilio, filterMatricula) {
        var result = sortedBasicPrestadores
        if (filterDomicilio) {
            result = result.filter { it.atiendeDomicilio }
        }
        if (filterMatricula) {
            result = result.filter { it.tieneMatricula }
        }
        result
    }
    
    // Prestadores a mostrar según la pestaña
    val displayPrestadores = if (selectedTab == 0) filteredPremiumPrestadores else filteredBasicPrestadores
    
    // Obtener icono drawable según categoría
    val categoryIconRes = when {
        category.contains("Electric", ignoreCase = true) -> R.drawable.ic_electricista
        category.contains("Plom", ignoreCase = true) -> R.drawable.ic_plomero
        category.contains("Pintur", ignoreCase = true) -> R.drawable.ic_pintura
        category.contains("Mudanz", ignoreCase = true) -> R.drawable.ic_mudanza
        category.contains("Limpie", ignoreCase = true) -> R.drawable.ic_limpieza
        category.contains("Jardín", ignoreCase = true) || category.contains("Jardin", ignoreCase = true) -> R.drawable.ic_jardin
        category.contains("Mecán", ignoreCase = true) -> R.drawable.ic_mecanico
        category.contains("Albañil", ignoreCase = true) -> R.drawable.ic_albanil
        else -> R.drawable.ic_otros
    }
    
    Scaffold(
        containerColor = appColors.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = categoryIconRes),
                            contentDescription = category,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = category,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appColors.surfaceColor,
                    navigationIconContentColor = appColors.textPrimaryColor,
                    actionIconContentColor = appColors.textPrimaryColor
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Botón de filtros avanzados
                    Box {
                        IconButton(onClick = { 
                            showAdvancedFilters = true
                            // Sincronizar valores temporales con los actuales
                            tempFilterDomicilio = filterDomicilio
                            tempFilterMatricula = filterMatricula
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_trophy),
                                contentDescription = "Filtros avanzados",
                                tint = if (filterDomicilio || filterMatricula) appColors.accentYellow else appColors.accentBlue
                            )
                        }
                        
                        // Dropdown de filtros avanzados
                        DropdownMenu(
                            expanded = showAdvancedFilters,
                            onDismissRequest = { 
                                showAdvancedFilters = false
                                // Restaurar valores al cerrar sin aplicar
                                tempFilterDomicilio = filterDomicilio
                                tempFilterMatricula = filterMatricula
                            },
                            modifier = Modifier
                                .width(280.dp)
                                .background(appColors.surfaceColor, RoundedCornerShape(12.dp))
                        ) {
                            // Título
                            Text(
                                text = "Filtros",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimaryColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                            
                            HorizontalDivider(color = appColors.dividerColor, thickness = 1.dp)
                            
                            // Opción: Domicilio
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🏠 Atiende a domicilio",
                                                fontSize = 14.sp,
                                                color = appColors.textPrimaryColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Va a tu ubicación",
                                                fontSize = 11.sp,
                                                color = appColors.textSecondaryColor
                                            )
                                        }
                                        Switch(
                                            checked = tempFilterDomicilio,
                                            onCheckedChange = { tempFilterDomicilio = it },
                                            modifier = Modifier.scale(0.8f),
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = appColors.accentBlue,
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = appColors.dividerColor
                                            )
                                        )
                                    }
                                },
                                onClick = {
                                    tempFilterDomicilio = !tempFilterDomicilio
                                }
                            )
                            
                            // Opción: Matrícula
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "📜 Tiene matrícula",
                                                fontSize = 14.sp,
                                                color = appColors.textPrimaryColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Profesional habilitado",
                                                fontSize = 11.sp,
                                                color = appColors.textSecondaryColor
                                            )
                                        }
                                        Switch(
                                            checked = tempFilterMatricula,
                                            onCheckedChange = { tempFilterMatricula = it },
                                            modifier = Modifier.scale(0.8f),
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = appColors.accentBlue,
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = appColors.dividerColor
                                            )
                                        )
                                    }
                                },
                                onClick = {
                                    tempFilterMatricula = !tempFilterMatricula
                                }
                            )
                            
                            HorizontalDivider(color = appColors.dividerColor, thickness = 1.dp)
                            
                            // Botón Aplicar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        filterDomicilio = tempFilterDomicilio
                                        filterMatricula = tempFilterMatricula
                                        showAdvancedFilters = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = appColors.accentBlue
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Aplicar filtros",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    
                    // Botón de ordenamiento
                    Box {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Filtros",
                                tint = appColors.accentBlue
                            )
                        }
                        
                        // Dropdown justo debajo del botón
                        DropdownMenu(
                            expanded = showFilterDialog,
                            onDismissRequest = { showFilterDialog = false },
                            modifier = Modifier
                                .width(250.dp)
                                .background(appColors.surfaceColor, RoundedCornerShape(12.dp))
                        ) {
                            // Título
                            Text(
                                text = "Ordenar por",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimaryColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                            
                            HorizontalDivider(color = appColors.dividerColor, thickness = 1.dp)
                            
                            // Opción: Calificación
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "⭐ Calificación",
                                                fontSize = 14.sp,
                                                color = if (sortOrder == "rating") appColors.accentBlue else appColors.textPrimaryColor,
                                                fontWeight = if (sortOrder == "rating") FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "Mayor a menor",
                                                fontSize = 11.sp,
                                                color = appColors.textSecondaryColor
                                            )
                                        }
                                        if (sortOrder == "rating") {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = appColors.accentBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    sortOrder = "rating"
                                    showFilterDialog = false
                                }
                            )
                            
                            // Opción: Distancia
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "📍 Distancia",
                                                fontSize = 14.sp,
                                                color = if (sortOrder == "distance") appColors.accentBlue else appColors.textPrimaryColor,
                                                fontWeight = if (sortOrder == "distance") FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "Más cercano primero",
                                                fontSize = 11.sp,
                                                color = appColors.textSecondaryColor
                                            )
                                        }
                                        if (sortOrder == "distance") {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = appColors.accentBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    sortOrder = "distance"
                                    showFilterDialog = false
                                }
                            )
                            
                            // Opción: Alfabético
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🔤 Nombre",
                                                fontSize = 14.sp,
                                                color = if (sortOrder == "alphabetic") appColors.accentBlue else appColors.textPrimaryColor,
                                                fontWeight = if (sortOrder == "alphabetic") FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "Orden alfabético A-Z",
                                                fontSize = 11.sp,
                                                color = appColors.textSecondaryColor
                                            )
                                        }
                                        if (sortOrder == "alphabetic") {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = appColors.accentBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    sortOrder = "alphabetic"
                                    showFilterDialog = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(appColors.backgroundColor)
        ) {
            // Pestañas Premium / Básico
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = appColors.surfaceColor,
                contentColor = appColors.accentBlue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) appColors.accentYellow else appColors.textSecondaryColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Premium (${premiumPrestadores.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Básico (${basicPrestadores.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
            
            // Lista de prestadores
            if (displayPrestadores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No hay prestadores disponibles",
                            fontSize = 16.sp,
                            color = appColors.textSecondaryColor,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == 0) "Prueba en la pestaña Básico" else "Prueba en la pestaña Premium",
                            fontSize = 12.sp,
                            color = appColors.textSecondaryColor.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayPrestadores) { prestador ->
                        PrestadorCard(
                            prestador = prestador,
                            isPremium = selectedTab == 0
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrestadorCard(
    prestador: Prestador,
    isPremium: Boolean,
    appColors: com.example.myapplication.ui.theme.AppColors = com.example.myapplication.ui.theme.getAppColors()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = prestador.avatarColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = prestador.name.first().toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = prestador.avatarColor
                    )
                }
            }
            
            // Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = prestador.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimaryColor
                    )
                    if (isPremium && prestador.verified) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verificado",
                            tint = appColors.accentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Text(
                    text = prestador.job,
                    fontSize = 13.sp,
                    color = appColors.textSecondaryColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = appColors.accentYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = prestador.rating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.textPrimaryColor
                        )
                        Text(
                            text = "(${prestador.reviews})",
                            fontSize = 11.sp,
                            color = appColors.textSecondaryColor
                        )
                    }
                    
                    // Distancia
                    Text(
                        text = "• ${prestador.distance} km",
                        fontSize = 12.sp,
                        color = appColors.textSecondaryColor
                    )
                }
                
                // Badges de atributos
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prestador.atiendeDomicilio) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = appColors.accentGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "A domicilio",
                                fontSize = 10.sp,
                                color = appColors.accentGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (prestador.tieneMatricula) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = appColors.accentBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Habilitado",
                                fontSize = 10.sp,
                                color = appColors.accentBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Badge Premium
            if (isPremium) {
                Surface(
                    color = appColors.accentYellow,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "PREMIUM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
