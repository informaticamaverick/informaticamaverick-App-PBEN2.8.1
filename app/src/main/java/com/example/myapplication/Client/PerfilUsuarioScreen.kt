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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.ui.screens.ProfileMode
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// =================================================================================
// --- SECCIÓN 1: PANTALLA DE PERFIL DE USUARIO (PRINCIPAL) ---
// =================================================================================

/**
 * Pantalla principal del perfil de usuario.
 * Centraliza la lógica de obtención de datos desde Room y la gestión de modos.
 */
@Composable
fun PerfilUsuarioScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileSharedViewModel = hiltViewModel()
) {
    // Suscripción al estado global del perfil (Room es la Fuente Única de Verdad)
    val profileMode by viewModel.profileMode.collectAsState()
    val userState by viewModel.userState.collectAsState()
    val isCompanyViewActive = profileMode == ProfileMode.EMPRESA

    val currentUser = userState
    if (currentUser == null) {
        // Estado de carga inicial mientras se inicializa la base de datos local
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        // Renderizado del contenido una vez que tenemos datos reales de Room
        PerfilUsuarioContent(
            user = currentUser,
            isCompanyViewActive = isCompanyViewActive,
            onToggleViewMode = viewModel::toggleProfileMode,
            onNavigateBack = onNavigateBack,
            onLogout = onLogout,
            onSave = { updatedUser ->
                // Guardado persistente en Room
                // 🔥 Sincronización remota: El ViewModel se encargará de enviar 'updatedUser' a Firestore
                viewModel.saveUserProfile(updatedUser)
            }
        )
    }
}

/**
 * Estructura visual del perfil (Scaffold, Barra superior animada y Contenido).
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PerfilUsuarioContent(
    user: UserEntity,
    isCompanyViewActive: Boolean,
    onToggleViewMode: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    val lazyListState = rememberLazyListState()
    var showEditSheet by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }
    
    // Configuración para la animación del TopBar basada en el scroll
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
            
            // --- LISTA PRINCIPAL DE CONTENIDO ---
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 1. CABECERA (Portada, Foto y Nombre)
                item {
                    ProfileHeader(
                        user = user, 
                        isCompanyMode = isCompanyViewActive,
                        onToggleMode = onToggleViewMode,
                        onEditPhoto = { /* 🔥 Futuro: Cambiar foto perfil en Firestore/Storage */ },
                        onEditCover = { /* 🔥 Futuro: Cambiar portada en Firestore/Storage */ }
                    )
                }

                // 2. BLOQUES DE INFORMACIÓN DINÁMICA
                if (isCompanyViewActive) {
                    // --- VISTA MODO EMPRESA ---
                    if (user.companies.isNotEmpty()) {
                        item {
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
                                    ArchiveroSection(title = company.name, color = MaterialTheme.colorScheme.tertiaryContainer) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            InfoRow(Icons.Default.Domain, "Razón Social", company.razonSocial)
                                            InfoRow(Icons.Default.Badge, "CUIT", company.cuit)
                                        }
                                    }
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
                                                ArchiveroSection(title = branch.name, color = MaterialTheme.colorScheme.primaryContainer) {
                                                    InfoRow(Icons.Default.Place, "Dirección", branch.address.fullString())
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text("Equipo de Trabajo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                                LazyRow(contentPadding = PaddingValues(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    items(branch.employees) { emp -> EmployeeRoundedCard(emp) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No tienes empresas vinculadas.", color = Color.Gray) } }
                    }
                } else {
                    // --- VISTA MODO CLIENTE ---
                    if (user.emails.isNotEmpty() || user.phones.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            ArchiveroSection(title = "Contacto", color = MaterialTheme.colorScheme.secondaryContainer) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    user.emails.forEach { InfoRow(Icons.Default.Email, "Email", it) }
                                    user.phones.forEach { InfoRow(Icons.Default.Phone, "Teléfono", it) }
                                }
                            }
                        }
                    }

                    // Direcciones Personales
                    if (user.personalAddresses.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            ArchiveroSection(title = "Mis Direcciones", color = MaterialTheme.colorScheme.primaryContainer) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    user.personalAddresses.forEach { addr -> InfoRow(Icons.Default.Home, "Domicilio", addr.fullString()) }
                                }
                            }
                        }
                    }
                }

                // 3. BOTÓN DE CERRAR SESIÓN
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                        Button(
                            onClick = {
                                // 🔥 Futuro: Firebase Auth Logout
                                onLogout()
                            }, 
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer), 
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, null); Spacer(modifier = Modifier.width(8.dp)); Text("Cerrar Sesión")
                        }
                    }
                }
            }

            // --- 4. FAB GEMINI (BOTÓN FLOTANTE INTERACTIVO) ---
            Box(modifier = Modifier.fillMaxSize().padding(16.dp).zIndex(10f), contentAlignment = Alignment.BottomEnd) {
                GeminiSplitFAB(
                    isExpanded = isFabExpanded,
                    isSearchActive = false,
                    onToggleExpand = { isFabExpanded = !isFabExpanded },
                    onActivateSearch = {},
                    onCloseSearch = {},
                    secondaryActions = {
                         SmallActionFab(icon = Icons.Default.Edit, label = "Editar", iconColor = MaterialTheme.colorScheme.primary, onClick = { showEditSheet = true })
                    },
                    expandedTools = {
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

    // Modal de edición - Maneja ambos modos y guarda en Room
    if (showEditSheet) {
        ModalBottomSheet(onDismissRequest = { showEditSheet = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            if (isCompanyViewActive) {
                EditCompanyModeContent(
                    user = user, 
                    onSave = { updatedUser ->
                        onSave(updatedUser)
                        showEditSheet = false
                    }
                )
            } else {
                EditClientModeContent(
                    user = user, 
                    onSave = { updatedUser ->
                        onSave(updatedUser)
                        showEditSheet = false 
                    }
                )
            }
        }
    }
}

// =================================================================================
// --- SECCIÓN 3: COMPONENTES VISUALES ---
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedTopBarProfile(title: String, scrollProgress: Float, onBackClick: () -> Unit, isCompanyMode: Boolean) {
    val alpha = animateFloatAsState(targetValue = scrollProgress, label = "alpha").value
    if (alpha > 0.1f) {
        TopAppBar(
            title = { Text(title, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha)),
            actions = { if(isCompanyMode) Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(end = 16.dp)) },
            modifier = Modifier.graphicsLayer { this.alpha = alpha }
        )
    } else {
        Box(modifier = Modifier.statusBarsPadding().padding(top = 16.dp, start = 16.dp)) {
            IconButton(onClick = onBackClick, modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ProfileHeader(user: UserEntity, isCompanyMode: Boolean, onToggleMode: () -> Unit, onEditPhoto: () -> Unit, onEditCover: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background))))
        if (user.isSubscribed) {
            Surface(modifier = Modifier.align(Alignment.TopStart).padding(top = 40.dp), color = Color(0xFFFFD700), shape = RoundedCornerShape(bottomEnd = 16.dp, topEnd = 16.dp), shadowElevation = 6.dp) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PREMIUM", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                }
            }
        }
        IconButton(onClick = onEditCover, modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Editar Portada", tint = Color.White)
        }
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box {
                Image(painter = rememberAsyncImagePainter(model = user.profileImageUrl ?: R.drawable.iconapp), contentDescription = null, modifier = Modifier.size(120.dp).clip(CircleShape).border(4.dp, MaterialTheme.colorScheme.surface, CircleShape), contentScale = ContentScale.Crop)
                IconButton(onClick = onEditPhoto, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 8.dp, y = 8.dp).size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape).border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                if (user.isVerified) {
                    Icon(imageVector = Icons.Filled.Verified, contentDescription = "Verificado", tint = Color(0xFF1DA1F2), modifier = Modifier.align(Alignment.BottomStart).offset(x = (-4).dp, y = 4.dp).size(32.dp).background(Color.White, CircleShape).border(2.dp, Color.White, CircleShape))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("${user.name} ${user.lastName}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Usuario Verificado", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (user.hasCompanyProfile) {
            Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).clickable { onToggleMode() }, color = if (isCompanyMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(24.dp), shadowElevation = 6.dp) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isCompanyMode) "Modo Empresa" else "Modo Cliente", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(if (isCompanyMode) Icons.Default.Business else Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun EmployeeRoundedCard(employee: Employee) {
    Card(modifier = Modifier.width(200.dp).height(80.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = rememberAsyncImagePainter(model = employee.photoUrl ?: R.drawable.iconapp), contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(12.dp)); Column {
                Text("${employee.name} ${employee.lastName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(employee.position, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            }
        }
    }
}

/**
 * Sección de tarjeta con título en la parte superior izquierda.
 * Usada para agrupar información.
 */
@Composable
fun ArchiveroSection(title: String, color: Color, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
            Box(modifier = Modifier.size(width = 4.dp, height = 18.dp).background(color, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(8.dp)); Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.5f)), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Box(modifier = Modifier.fillMaxWidth().background(color.copy(alpha = 0.05f)).padding(16.dp)) { content() }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp)); Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

// =================================================================================
// --- SECCIÓN 4: PANTALLAS DE EDICIÓN CON PERSISTENCIA ---
// =================================================================================

@Composable
fun EditClientModeContent(user: UserEntity, onSave: (UserEntity) -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var hasCompanyProfile by remember { mutableStateOf(user.hasCompanyProfile) }
    val emails = remember { mutableStateListOf(*user.emails.toTypedArray()) }
    val phones = remember { mutableStateListOf(*user.phones.toTypedArray()) }
    val addresses = remember { mutableStateListOf(*user.personalAddresses.toTypedArray()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Editar Datos Personales", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellido") }, modifier = Modifier.weight(1f))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Editor de Emails
            DynamicStringListEditor(title = "Correos Electrónicos", items = emails, icon = Icons.Default.Email, onUpdate = {})
            Spacer(modifier = Modifier.height(16.dp))

            // Editor de Teléfonos
            DynamicStringListEditor(title = "Teléfonos", items = phones, icon = Icons.Default.Phone, onUpdate = {})
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Editor de Direcciones
            Text("Mis Direcciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { addresses.add(Address(calle="", localidad="", provincia="", pais="")) }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = ButtonDefaults.outlinedButtonColors()) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Agregar Dirección")
            }
            addresses.forEachIndexed { index, addr -> AddressFullEditor(addr, onDelete = { addresses.removeAt(index) }) }
            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            // Perfil de Empresa
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¿Tienes un Negocio?", fontWeight = FontWeight.Bold); Text("Habilita el perfil empresarial.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = hasCompanyProfile, onCheckedChange = { hasCompanyProfile = it })
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
        SaveButton {
            onSave(user.copy(name = name, lastName = lastName, hasCompanyProfile = hasCompanyProfile, emails = emails.toList(), phones = phones.toList(), personalAddresses = addresses.toList()))
        }
    }
}

@Composable
fun EditCompanyModeContent(user: UserEntity, onSave: (UserEntity) -> Unit) {
    // Para simplificar, editamos la primera empresa si existe
    val companies = remember { mutableStateListOf(*user.companies.toTypedArray()) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Editar Datos Empresariales", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            if (companies.isEmpty()) {
                Text("No tienes empresas registradas.", modifier = Modifier.padding(16.dp), color = Color.Gray)
            } else {
                companies.forEachIndexed { index, company ->
                    CompanyFullEditor(
                        company = company,
                        onUpdate = { updatedCompany -> companies[index] = updatedCompany }
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
        SaveButton {
            onSave(user.copy(companies = companies.toList()))
        }
    }
}

// =================================================================================
// --- COMPONENTES DE EDICIÓN REUTILIZABLES ---
// =================================================================================

@Composable
fun SaveButton(onSave: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(onClick = onSave, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Save, null, tint = Color.White) }
    }
}

@Composable
fun DynamicStringListEditor(title: String, items: MutableList<String>, icon: ImageVector, onUpdate: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp)); Text(title, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.weight(1f))
            IconButton(onClick = { items.add(""); onUpdate() }) { Icon(Icons.Default.Add, null) }
        }
        items.forEachIndexed { index, item ->
            var text by remember { mutableStateOf(item) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                OutlinedTextField(value = text, onValueChange = { text = it; items[index] = it }, modifier = Modifier.weight(1f), singleLine = true)
                IconButton(onClick = { items.removeAt(index); onUpdate() }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
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
    Card(border = BorderStroke(1.dp, Color.Gray.copy(0.3f)), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dirección", fontWeight = FontWeight.Bold, fontSize = 12.sp); Spacer(Modifier.weight(1f))
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
fun CompanyFullEditor(company: Company, onUpdate: (Company) -> Unit) {
    var name by remember { mutableStateOf(company.name) }
    var razon by remember { mutableStateOf(company.razonSocial) }
    var cuit by remember { mutableStateOf(company.cuit) }
    
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Datos Fiscales", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = name, onValueChange = { name = it; onUpdate(company.copy(name = it)) }, label = { Text("Nombre Comercial") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = razon, onValueChange = { razon = it; onUpdate(company.copy(razonSocial = it)) }, label = { Text("Razón Social") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cuit, onValueChange = { cuit = it; onUpdate(company.copy(cuit = it)) }, label = { Text("CUIT") }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilUsuarioScreenPreview() {
    MyApplicationTheme {
        // PerfilUsuarioScreen(onNavigateBack = {}, onLogout = {})
    }
}
