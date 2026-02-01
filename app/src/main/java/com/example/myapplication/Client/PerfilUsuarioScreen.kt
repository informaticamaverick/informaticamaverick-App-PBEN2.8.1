package com.example.myapplication.Client

import android.annotation.SuppressLint
import androidx.compose.animation.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.R 
import kotlinx.coroutines.launch

// =================================================================================
// --- PANTALLA DE PERFIL DE USUARIO ---
// =================================================================================

/**
 * Pantalla principal del perfil de usuario.
 * Maneja la navegación y el estado global del modo (Cliente/Empresa).
 */
@Composable
fun PerfilUsuarioScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    // Obtenemos el usuario actual desde los datos falsos
    val user = remember { UserSampleDataFalso.currentUser }
    
    // Estado para controlar qué vista se muestra: Cliente (false) o Empresa (true)
    // Por defecto inicia en modo cliente (false) según requerimiento.
    var isCompanyViewActive by remember { mutableStateOf(false) }
    
    // Trigger para refrescar la UI al guardar cambios en listas mutables durante la edición
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    PerfilUsuarioContent(
        user = user,
        isCompanyViewActive = isCompanyViewActive,
        onToggleViewMode = { 
            // Solo permite cambiar si tiene perfil de empresa habilitado
            if (user.hasCompanyProfile) isCompanyViewActive = !isCompanyViewActive 
        },
        onNavigateBack = onNavigateBack,
        onLogout = onLogout,
        onRefresh = { refreshTrigger++ }
    )
}

/**
 * Contenido principal de la pantalla de perfil.
 * Implementa Scaffold, Animación de Header y Lógica de Pagers.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PerfilUsuarioContent(
    user: UserFalso,
    isCompanyViewActive: Boolean,
    onToggleViewMode: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit
) {
    // --- ESTADOS DE UI ---
    val lazyListState = rememberLazyListState() // Estado del scroll principal
    var showEditSheet by remember { mutableStateOf(false) } // Controla la visibilidad del sheet de edición
    
    // Estados del FAB Gemini (Boton flotante interactivo)
    var isFabExpanded by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Cálculo para la animación de transparencia del TopBar al scrollear
    val density = LocalDensity.current
    val headerSizePx = with(density) { 280.dp.toPx() }
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
            AnimatedTopBarProfile(
                title = if (isCompanyViewActive) "Perfil Empresarial" else "Perfil Personal",
                scrollProgress = scrollProgress,
                onBackClick = onNavigateBack,
                isCompanyMode = isCompanyViewActive
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
                // 1. HEADER (Común para ambos modos, pero con comportamiento distinto en botones)
                item {
                    ProfileHeader(
                        user = user, 
                        isCompanyMode = isCompanyViewActive,
                        onToggleMode = onToggleViewMode,
                        onEditPhoto = { /* Acción editar foto perfil */ },
                        onEditCover = { /* Acción editar portada */ }
                    )
                }

                // 2. CONTENIDO SEGÚN EL MODO (EXCLUSIVO)
                if (isCompanyViewActive) {
                    // --- VISTA EMPRESA (CARRUSEL DE EMPRESAS) ---
                    // Si el usuario tiene empresas, mostramos el carrusel
                    if (user.companies.isNotEmpty()) {
                        item {
                            // Pager para navegar entre múltiples empresas
                            val companyPagerState = rememberPagerState(pageCount = { user.companies.size })
                            
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
                                    // Título usando el nombre de la empresa
                                    ArchiveroSection(
                                        title = company.name, // Requisito 3: Nombre de la empresa
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        isProviderMode = true
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            // Imagen de perfil de la empresa (si existe)
                                            if (company.profileImageUrl != null) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(model = company.profileImageUrl),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(50.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(company.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                            } else {
                                                Text(company.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                            }

                                            InfoRow(Icons.Default.Domain, "Razón Social", company.razonSocial)
                                            InfoRow(Icons.Default.Badge, "CUIT", company.cuit)
                                            
                                            // Nota: Servicios quitados de la tarjeta (Requisito 4)
                                        }
                                    }
                                    
                                    // Divider separando info de empresa de las sucursales
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                                    
                                    // --- CARRUSEL DE SUCURSALES Y EQUIPOS ---
                                    // Pager anidado para Casas Centrales / Sucursales
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
                                                ArchiveroSection(
                                                    title = branch.name, // "Casa Central", "Sucursal X"
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    isProviderMode = true
                                                ) {
                                                    InfoRow(Icons.Default.Place, "Dirección", branch.address.fullString())
                                                }
                                                
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                                
                                                // Sección Equipo de Trabajo
                                                Text(
                                                    "Equipo de Trabajo", 
                                                    style = MaterialTheme.typography.titleMedium, 
                                                    color = Color.Black, // Requisito: Negro
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
                                                            EmployeeRoundedCard(emp)
                                                        }
                                                    }
                                                } else {
                                                    Text("Sin personal asignado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(8.dp))
                                                }
                                                
                                                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                                            }
                                        }
                                        
                                        // Indicador de Puntos (Dots) para saber que hay varias sucursales
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
                                }
                            }
                        }
                    } else {
                        // Estado vacío empresa
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No hay datos de empresa cargados.", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    // --- VISTA CLIENTE ---
                    // Datos Personales (Mails y Teléfonos)
                    if (user.emails.isNotEmpty() || user.phones.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            // Títulos arriba a la izquierda
                            ArchiveroSection(
                                title = "Contacto", 
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                isProviderMode = false
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    user.emails.forEach { email -> InfoRow(Icons.Default.Email, "Email", email) }
                                    user.phones.forEach { phone -> InfoRow(Icons.Default.Phone, "Teléfono", phone) }
                                }
                            }
                        }
                    }

                    // Direcciones Personales
                    if (user.personalAddresses.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            ArchiveroSection(
                                title = "Mis Direcciones", 
                                color = MaterialTheme.colorScheme.primaryContainer,
                                isProviderMode = false
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    user.personalAddresses.forEach { addr ->
                                        InfoRow(Icons.Default.Home, "Domicilio", addr.fullString())
                                    }
                                }
                            }
                        }
                    }
                    
                    // Si no hay nada
                    if (user.emails.isEmpty() && user.phones.isEmpty() && user.personalAddresses.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Completa tu perfil para ver tu información aquí.", color = Color.Gray)
                            }
                        }
                    }
                }

                // Botón Cerrar Sesión (Siempre al final)
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer, 
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar Sesión")
                        }
                    }
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
                    secondaryActions = {
                         SmallActionFab(
                             icon = Icons.Default.Edit,
                             onClick = { showEditSheet = true }
                         )
                    },
                    expandedTools = {
                        // Botón para cambiar Modo (Cliente/Empresa)
                        if (user.hasCompanyProfile) {
                            SmallFabTool(
                                label = if (isCompanyViewActive) "Modo Cliente" else "Modo Empresa",
                                icon = if (isCompanyViewActive) Icons.Default.Person else Icons.Default.Business,
                                onClick = onToggleViewMode
                            )
                        }
                        SmallFabTool("Ajustes", Icons.Default.Settings, onClick = {})
                    }
                )
            }
        }
    }

    // --- SHEET DE EDICIÓN (Modal Inferior) ---
    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            // Mostramos el editor correspondiente al modo activo
            if (isCompanyViewActive) {
                EditCompanyModeContent(user = user, onSave = { onRefresh(); showEditSheet = false })
            } else {
                EditClientModeContent(user = user, onSave = { onRefresh(); showEditSheet = false })
            }
        }
    }
}

// =================================================================================
// --- COMPONENTES VISUALES ---
// =================================================================================

/**
 * Cabecera del perfil con Foto de Portada, Avatar, Nombre y Botón de cambio de modo.
 */
@Composable
fun ProfileHeader(
    user: UserFalso, 
    isCompanyMode: Boolean, 
    onToggleMode: () -> Unit,
    onEditPhoto: () -> Unit,
    onEditCover: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(280.dp)
    ) {
        // Fondo / Portada
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background))))

        // Etiqueta PREMIUM (Parte Superior Izquierda del Banner)
        if (user.isSubscribed) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 0.dp),
                color = Color(0xFFFFD700), // Dorado
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

        // Botón Editar Portada (Parte Superior Derecha)
        IconButton(
            onClick = onEditCover,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Editar Portada", tint = Color.White)
        }

        // Contenido Central (Avatar y Nombre)
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box {
                // Avatar (Imagen del usuario, o de la empresa actual podría ser una mejora futura)
                Image(
                    painter = rememberAsyncImagePainter(model = user.profileImageUrl ?: R.drawable.iconapp),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp).clip(CircleShape).border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                // Botón Editar Foto Perfil (Abajo Derecha del Avatar)
                IconButton(
                    onClick = onEditPhoto,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }

                // Icono Verificado (Si corresponde)
                if (user.isVerified) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verificado",
                        tint = Color(0xFF1DA1F2), // Azul estilo verificado
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-4).dp, y = 4.dp)
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("${user.name} ${user.lastName}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            
            // Subtítulo
            if (!isCompanyMode && user.emails.isNotEmpty()) {
                Text(user.emails.first(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (isCompanyMode && user.companies.isNotEmpty()) {
                Text(user.companies.first().name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Botón Switch Modo (Abajo Derecha de la Portada)
        if (user.hasCompanyProfile) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .clickable { onToggleMode() },
                color = if (isCompanyMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 6.dp
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isCompanyMode) "Modo Empresa" else "Modo Cliente", 
                        color = Color.White, 
                        style = MaterialTheme.typography.labelMedium, 
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        if (isCompanyMode) Icons.Default.Business else Icons.Default.Person, 
                        null, 
                        tint = Color.White, 
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta rectangular con bordes redondeados para el carrusel de empleados.
 */
@Composable
fun EmployeeRoundedCard(employee: Employee) {
    Card(
        modifier = Modifier.width(200.dp).height(80.dp), // Rectangular horizontal
        shape = RoundedCornerShape(12.dp), // Bordes redondeados
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
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), // Foto cuadrada redondeada
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

/**
 * Sección de tarjeta con título en la parte superior izquierda.
 * Usada para agrupar información.
 */
@Composable
fun ArchiveroSection(
    title: String, 
    color: Color, 
    isProviderMode: Boolean = false,
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
fun InfoRow(icon: ImageVector, label: String, value: String) {
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
fun AnimatedTopBarProfile(title: String, scrollProgress: Float, onBackClick: () -> Unit, isCompanyMode: Boolean) {
    val alpha = animateFloatAsState(targetValue = scrollProgress, label = "").value
    if (alpha > 0.1f) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = alpha)),
            actions = {
                if(isCompanyMode) Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(end = 16.dp))
            },
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

// =================================================================================
// --- PANTALLAS DE EDICIÓN SEPARADAS ---
// =================================================================================

// --- EDITOR MODO CLIENTE ---
@Composable
fun EditClientModeContent(user: UserFalso, onSave: () -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var lastName by remember { mutableStateOf(user.lastName) }
    // Estado para "Activar" el perfil empresa si no lo tiene
    var hasCompanyProfile by remember { mutableStateOf(user.hasCompanyProfile) }
    var refresh by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Editar Datos Personales", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // Nombre y Apellido
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it; user.name = it }, label = { Text("Nombre") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = lastName, onValueChange = { lastName = it; user.lastName = it }, label = { Text("Apellido") }, modifier = Modifier.weight(1f))
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Listas Dinámicas (Emails y Teléfonos)
        DynamicStringListEditor(
            title = "Correos Electrónicos",
            items = user.emails,
            icon = Icons.Default.Email,
            onUpdate = { refresh++ }
        )
        Spacer(modifier = Modifier.height(16.dp))
        DynamicStringListEditor(
            title = "Teléfonos",
            items = user.phones,
            icon = Icons.Default.Phone,
            onUpdate = { refresh++ }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Direcciones Personales
        Text("Mis Direcciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Button(
            onClick = { user.personalAddresses.add(Address(calle="", localidad="", provincia="", pais="")); refresh++ },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Agregar Dirección")
        }
        user.personalAddresses.forEachIndexed { index, addr ->
            key(addr.id, refresh) {
                AddressFullEditor(addr, onDelete = { user.personalAddresses.removeAt(index); refresh++ })
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        // Switch para Habilitar Modo Empresa
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("¿Tienes un Negocio?", fontWeight = FontWeight.Bold)
                    Text("Habilita el perfil empresarial.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = hasCompanyProfile, onCheckedChange = { hasCompanyProfile = it; user.hasCompanyProfile = it })
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
    SaveButton(onSave)
}

// --- EDITOR MODO EMPRESA ---
@Composable
fun EditCompanyModeContent(user: UserFalso, onSave: () -> Unit) {
    var refresh by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Editar Datos Empresariales", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // Lista de Empresas
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mis Empresas", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            // Botón para agregar una nueva empresa (Lógica simplificada)
            IconButton(onClick = { 
                user.companies.add(Company(name="Nueva", razonSocial="", cuit="", branches = mutableListOf(CompanyBranch(name="Central", address=Address(calle="", localidad="", provincia="", pais=""))))); 
                refresh++ 
            }) {
                Icon(Icons.Default.AddBusiness, null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (user.companies.isEmpty()) {
            Text("No tienes empresas registradas.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
        }

        user.companies.forEachIndexed { _, company ->
            key(company.id, refresh) {
                CompanyFullEditor(company)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
    SaveButton(onSave)
}

// =================================================================================
// --- COMPONENTES DE EDICIÓN REUTILIZABLES ---
// =================================================================================

@Composable
fun SaveButton(onSave: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(onClick = onSave) { Icon(Icons.Default.Save, null) }
    }
}

@Composable
fun DynamicStringListEditor(title: String, items: MutableList<String>, icon: ImageVector, onUpdate: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { items.add(""); onUpdate() }) {
                Icon(Icons.Default.Add, null)
            }
        }
        items.forEachIndexed { index, item ->
            var text by remember { mutableStateOf(item) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; items[index] = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = { items.removeAt(index); onUpdate() }) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddressFullEditor(address: Address, onDelete: (() -> Unit)? = null) {
    var calle by remember { mutableStateOf(address.calle) }
    var localidad by remember { mutableStateOf(address.localidad) }
    var provincia by remember { mutableStateOf(address.provincia) }
    var pais by remember { mutableStateOf(address.pais) }

    fun update() { address.calle = calle; address.localidad = localidad; address.provincia = provincia; address.pais = pais }

    Card(border = BorderStroke(1.dp, Color.Gray.copy(0.3f)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dirección", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                if(onDelete != null) IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            }
            OutlinedTextField(value = calle, onValueChange = { calle = it; update() }, label = { Text("Calle y Número") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = localidad, onValueChange = { localidad = it; update() }, label = { Text("Ciudad") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = provincia, onValueChange = { provincia = it; update() }, label = { Text("Provincia") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = pais, onValueChange = { pais = it; update() }, label = { Text("País") }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun CompanyFullEditor(company: Company) {
    var name by remember { mutableStateOf(company.name) }
    var razon by remember { mutableStateOf(company.razonSocial) }
    var cuit by remember { mutableStateOf(company.cuit) }
    var refresh by remember { mutableIntStateOf(0) }

    fun update() { company.name = name; company.razonSocial = razon; company.cuit = cuit }

    Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Datos Fiscales", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = name, onValueChange = { name = it; update() }, label = { Text("Nombre Comercial") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = razon, onValueChange = { razon = it; update() }, label = { Text("Razón Social") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = cuit, onValueChange = { cuit = it; update() }, label = { Text("CUIT") }, modifier = Modifier.weight(1f))
            }
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            
            // Servicios
            DynamicStringListEditor("Categorías / Servicios", company.services, Icons.Default.Category, { refresh++ })
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            
            // Sedes / Sucursales (Ahora Branches)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sedes / Sucursales", style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = { 
                    company.branches.add(CompanyBranch(name="Nueva Sucursal", address=Address(calle="", localidad="", provincia="", pais=""))); 
                    refresh++ 
                }) { Icon(Icons.Default.Add, null) }
            }
            company.branches.forEachIndexed { i, branch ->
                key(branch.id, refresh) {
                    var branchName by remember { mutableStateOf(branch.name) }
                    OutlinedTextField(
                        value = branchName, 
                        onValueChange = { branchName = it; branch.name = it }, 
                        label = { Text("Nombre Sucursal") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    AddressFullEditor(branch.address, onDelete = { company.branches.removeAt(i); refresh++ })
                    Spacer(Modifier.height(16.dp))
                }
            }
            
            // Empleados (Placeholder)
            Text("Gestión de empleados disponible en detalle.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilUsuarioScreenPreview() {
    MyApplicationTheme {
        PerfilUsuarioScreen(onNavigateBack = {}, onLogout = {})
    }
}
