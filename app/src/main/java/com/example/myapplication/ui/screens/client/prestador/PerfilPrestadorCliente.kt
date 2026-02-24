package com.example.myapplication.ui.screens.client.prestador

import android.annotation.SuppressLint
import com.example.myapplication.mock.sample.UserFalso
import com.example.myapplication.mock.sample.SampleDataFalso
import com.example.myapplication.mock.sample.Company
import com.example.myapplication.mock.sample.Employee
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.components.*

// =================================================================================
// --- PANTALLA DE PERFIL DE PRESTADOR (VISTA CLIENTE) ---
// =================================================================================

/**
 * Pantalla que permite al cliente ver el perfil completo de un prestador.
 * Basada en la UI de PerfilUsuarioScreen pero en modo solo lectura.
 */
@Composable
fun PerfilPrestadorCliente(
    providerId: String,
    onBack: () -> Unit
) {
    // Obtenemos el prestador desde los datos falsos (Usando la estructura UserFalso)
    val user = remember { SampleDataFalso.getPrestadorUserById(providerId) }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Prestador no encontrado", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                Text("Volver")
            }
        }
        return
    }
    
    // Por defecto mostramos la vista de empresa ya que es un perfil de prestador
    val isCompanyViewActive = true 
    
    PerfilPrestadorContent(
        user = user,
        isCompanyViewActive = isCompanyViewActive,
        onNavigateBack = onBack
    )
}

/**
 * Contenido principal de la pantalla de perfil del prestador.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PerfilPrestadorContent(
    user: UserFalso,
    isCompanyViewActive: Boolean,
    onNavigateBack: () -> Unit
) {
    // --- ESTADOS DE UI ---
    val lazyListState = rememberLazyListState() // Estado del scroll principal
    
    // Estados del FAB Gemini (Boton flotante interactivo)
    var isFabExpanded by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Estado del Pager de Empresas (Para sincronizar con el Header)
    val companyPagerState = rememberPagerState(pageCount = { user.companies.size })
    
    // Cálculo para la animación de transparencia del TopBar al scrollear
    val density = LocalDensity.current
    val headerSizePx = with(density) { 320.dp.toPx() } // Ajustado a la nueva altura del header
    val scrollProgress by remember {
        derivedStateOf {
            if (lazyListState.layoutInfo.visibleItemsInfo.isEmpty() || lazyListState.firstVisibleItemIndex > 0) 1f
            else (lazyListState.firstVisibleItemScrollOffset.toFloat() / headerSizePx).coerceIn(0f, 1f)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Barra superior animada que aparece al hacer scroll
            AnimatedTopBarProfileProvider(
                title = if (isCompanyViewActive) "Perfil Profesional" else "Perfil",
                scrollProgress = scrollProgress,
                onBackClick = onNavigateBack
            )
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            // --- CONTENIDO SCROLLABLE PRINCIPAL ---
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 1. HEADER (Dinamico segun empresa seleccionada)
                item {
                    val currentCompany = if (user.companies.isNotEmpty()) user.companies[companyPagerState.currentPage] else null
                    ProfileHeaderProvider(
                        user = user, 
                        company = currentCompany,
                        isCompanyMode = isCompanyViewActive
                    )
                }

                // 2. CONTENIDO DE EMPRESA (Principal para el prestador)
                if (isCompanyViewActive && user.hasCompanyProfile) {
                    // --- VISTA EMPRESA (CARRUSEL DE EMPRESAS) ---
                    if (user.companies.isNotEmpty()) {
                        item {
                            // Pager para navegar entre múltiples empresas
                            HorizontalPager(
                                state = companyPagerState,
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                contentPadding = PaddingValues(horizontal = 0.dp), // Full width
                                verticalAlignment = Alignment.Top
                            ) { page ->
                                val company = user.companies[page]
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // --- TARJETA DE INFORMACIÓN DE LA EMPRESA ---
                                    ArchiveroSectionProvider(
                                        title = "Detalles de Empresa", // Título genérico, nombre está en Header
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            
                                            // Info legal básica
                                            InfoRowProvider(Icons.Default.Domain, "Razón Social", company.razonSocial)
                                            InfoRowProvider(Icons.Default.Badge, "CUIT", company.cuit)
                                            
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                            
                                            // Servicios ofrecidos (Etiquetas)
                                            Text("Servicios:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            
                                            // Usamos FlowRow (Layout experimental, disponible en BOM recientes)
                                            OptIn(ExperimentalLayoutApi::class)
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                company.services.forEach { service ->
                                                    SuggestionChip(
                                                        onClick = { },
                                                        label = { Text(service, style = MaterialTheme.typography.bodySmall) },
                                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    )
                                                }
                                            }

                                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                            
                                            // --- CARACTERÍSTICAS (Iconos booleanos) ---
                                            Text("Características:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceAround
                                            ) {
                                                FeatureIcon(icon = Icons.Default.AccessTime, label = "24Hs", isActive = company.works24h)
                                                FeatureIcon(icon = Icons.Default.Store, label = "Local", isActive = company.hasPhysicalLocation)
                                                FeatureIcon(icon = Icons.Default.LocalShipping, label = "Visitas", isActive = company.doesHomeVisits)
                                                FeatureIcon(icon = Icons.Default.EventAvailable, label = "Turnos", isActive = company.acceptsAppointments)
                                            }
                                        }
                                    }
                                    
                                    // Divider separando info de empresa de las sucursales
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                                    
                                    // --- CARRUSEL DE SUCURSALES Y EQUIPOS ---
                                    if (company.branches.isNotEmpty()) {
                                        val branchPagerState = rememberPagerState(pageCount = { company.branches.size })
                                        
                                        HorizontalPager(
                                            state = branchPagerState,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 4.dp)
                                        ) { branchPage ->
                                            val branch = company.branches[branchPage]
                                            
                                            Column {
                                                // Tarjeta de Dirección de la Sucursal
                                                ArchiveroSectionProvider(
                                                    title = branch.name, // "Casa Central", "Sucursal X"
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    InfoRowProvider(Icons.Default.Place, "Dirección", branch.address.fullString())
                                                }
                                                
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                                
                                                // Sección Equipo de Trabajo
                                                Text(
                                                    "Equipo de Trabajo", 
                                                    style = MaterialTheme.typography.titleMedium, 
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(start = 8.dp)
                                                )
                                                
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                                
                                                // Carrusel de Empleados de esta sucursal
                                                if (branch.employees.isNotEmpty()) {
                                                    LazyRow(
                                                        contentPadding = PaddingValues(vertical = 8.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        items(branch.employees) { emp ->
                                                            EmployeeRoundedCardProvider(emp)
                                                        }
                                                    }
                                                } else {
                                                    Text("Sin personal visible.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(8.dp))
                                                }
                                                
                                                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                                            }
                                        }
                                        
                                        // Indicador de Puntos (Dots) para sucursales
                                        if (company.branches.size > 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                repeat(company.branches.size) { iteration ->
                                                    val color = if (branchPagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(4.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                            .size(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // --- ALBUM DE PRODUCTOS ---
                                    if (company.productImages.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            "Nuestros Trabajos / Productos", 
                                            style = MaterialTheme.typography.titleMedium, 
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                        )
                                        
                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(company.productImages) { imgUrl ->
                                                Card(
                                                    shape = RoundedCornerShape(8.dp),
                                                    elevation = CardDefaults.cardElevation(2.dp),
                                                    modifier = Modifier.size(120.dp)
                                                ) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(model = imgUrl),
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                            
                            // Indicador de Puntos (Dots) para Empresas (Si tiene más de una)
                            if (user.companies.size > 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(user.companies.size) { iteration ->
                                        val color = if (companyPagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .size(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No hay información empresarial disponible.", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    // Fallback si no tiene perfil empresa (aunque no debería pasar para un prestador)
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Este perfil no tiene información pública de servicios.", color = Color.Gray)
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // --- FAB GEMINI (Botón Flotante con herramientas) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .zIndex(10f),
                contentAlignment = Alignment.BottomEnd
            ) {
                GeminiSplitFAB(
                    isExpanded = isFabExpanded,
                    isSearchActive = isSearchActive,
                    onToggleExpand = { isFabExpanded = !isFabExpanded },
                    onActivateSearch = { /* No search */ },
                    onCloseSearch = { /* No search */ },
                    secondaryActions = { },
                    expandedTools = {
                        SmallFabTool("Contactar", Icons.Default.Chat, onClick = {})
                        SmallFabTool("Compartir", Icons.Default.Share, onClick = {})
                    }
                )
            }
        }
    }
}

// =================================================================================
// --- COMPONENTES VISUALES ADAPTADOS (SOLO LECTURA) ---
// =================================================================================

/**
 * Cabecera del perfil (Solo Lectura) - Dinámica para Empresa
 */
@Composable
fun ProfileHeaderProvider(
    user: UserFalso, 
    company: Company?,
    isCompanyMode: Boolean
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().height(320.dp) // Un poco más alto para acomodar más info
    ) {
        // Fondo / Portada
        val bannerUrl = company?.productImages?.firstOrNull() ?: user.bannerImageUrl ?: R.drawable.myeasteregg
        Image(
            painter = rememberAsyncImagePainter(model = bannerUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay Gradiente para legibilidad
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
        )))

        // Botón "Sobre Nosotros" en el banner
        if (company != null) {
            IconButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Info, contentDescription = "Sobre Nosotros", tint = Color.White)
            }
        }

        // Etiqueta PREMIUM
        if (user.isSubscribed) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 0.dp),
                color = Color(0xFFFFD700),
                shape = RoundedCornerShape(bottomEnd = 16.dp, topEnd = 16.dp),
                shadowElevation = 6.dp
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PREMIUM", fontWeight = FontWeight.Bold, color = Color.Black, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Contenido Central (Avatar y Nombre de Empresa o Usuario)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp), 
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.Bottom
        ) {
            Box {
                // Avatar (Empresa o Usuario)
                val avatarModel = if (company != null) company.profileImageUrl ?: R.drawable.iconapp else user.profileImageUrl ?: R.drawable.iconapp
                Image(
                    painter = rememberAsyncImagePainter(model = avatarModel),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).clip(CircleShape).border(3.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                // Icono Verificado
                if (user.isVerified) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verificado",
                        tint = Color(0xFF1DA1F2),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(28.dp)
                            .background(Color.White, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Nombre Principal (Empresa si hay, sino Usuario)
            Text(
                text = company?.name ?: "${user.name} ${user.lastName}", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // Subtítulo (Usuario dueño si muestra empresa, o titulo profesional)
            if (company != null) {
                Text(
                    text = "Por ${user.name} ${user.lastName} ${user.titulo?.let { "- $it" } ?: ""}", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = Color.White.copy(alpha = 0.8f)
                )
                if (user.matricula != null) {
                     Text(
                        text = "Matrícula: ${user.matricula}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            } else {
                 user.titulo?.let {
                     Text(text = it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                 }
            }
        }
    }

    // Dialogo "Sobre Nosotros"
    if (showAboutDialog && company != null) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.Business, null) },
            title = { Text(company.name) },
            text = { Text(company.description) },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun FeatureIcon(icon: ImageVector, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun EmployeeRoundedCardProvider(employee: Employee) {
    Card(
        modifier = Modifier.width(200.dp).height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = employee.photoUrl ?: R.drawable.iconapp),
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    "${employee.name} ${employee.lastName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    employee.position,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ArchiveroSectionProvider(
    title: String, 
    color: Color, 
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // Cabecera de la tarjeta
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 18.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Tarjeta de Contenido
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxWidth().background(color.copy(alpha = 0.05f)).padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRowProvider(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedTopBarProfileProvider(title: String, scrollProgress: Float, onBackClick: () -> Unit) {
    val alpha = animateFloatAsState(targetValue = scrollProgress, label = "").value
    if (alpha > 0.1f) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = alpha)),
            modifier = Modifier.graphicsLayer { this.alpha = alpha }
        )
    } else {
        Box(modifier = Modifier.padding(top = 16.dp, start = 16.dp)) {
            IconButton(onClick = onBackClick, modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPrestadorClientePreview() {
    MyApplicationTheme {
        PerfilPrestadorCliente(providerId = "1", onBack = {})
    }
}
