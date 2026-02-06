package com.example.myapplication.presentation.client

import android.annotation.SuppressLint
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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
// --- [COMENTADO] IMPORTACIONES DE DATOS FALSOS ---
// import com.example.myapplication.data.model.fake.Company
// import com.example.myapplication.data.model.fake.Employee
// import com.example.myapplication.data.model.fake.SampleDataFalso
// import com.example.myapplication.data.model.fake.UserFalso

// --- [SECCIÓN: MODELOS DE DATOS REALES] ---
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.EmployeeProvider
import com.example.myapplication.presentation.components.GeminiSplitFAB
import com.example.myapplication.presentation.components.SmallFabTool
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// =================================================================================
// --- PANTALLA DE PERFIL DE PRESTADOR (VISTA CLIENTE) ---
// =================================================================================

/**
 * Pantalla que permite al cliente ver el perfil completo de un prestador.
 * [ACTUALIZADO] Usa ProviderViewModel para obtener datos reales de Room.
 */
@Composable
fun PerfilPrestadorCliente(
    providerId: String,
    onBack: () -> Unit,
    providerViewModel: ProviderViewModel = hiltViewModel() 
) {
    // 🔥 [FLUJO DE DATOS REAL] - Escucha cambios en Room
    // A futuro: Sincronizar desde Firebase para actualizar Room.
    val providerState by providerViewModel.getProviderById(providerId).collectAsStateWithLifecycle(initialValue = null)

    if (providerState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    
    PerfilPrestadorContent(
        provider = providerState!!,
        isCompanyViewActive = true, // Siempre empresa por ser perfil profesional
        onNavigateBack = onBack
    )
}

/**
 * Contenido principal de la pantalla de perfil del prestador.
 * [ACTUALIZADO] Mapeo completo a modelos reales.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PerfilPrestadorContent(
    provider: Provider,
    isCompanyViewActive: Boolean,
    onNavigateBack: () -> Unit
) {
    val lazyListState = rememberLazyListState() 
    var isFabExpanded by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Estado del Pager de Empresas (Datos reales de Room)
    val companyPagerState = rememberPagerState(pageCount = { provider.companies.size })
    
    val density = LocalDensity.current
    val headerSizePx = with(density) { 320.dp.toPx() }
    val scrollProgress by remember {
        derivedStateOf {
            if (lazyListState.layoutInfo.visibleItemsInfo.isEmpty() || lazyListState.firstVisibleItemIndex > 0) 1f
            else (lazyListState.firstVisibleItemScrollOffset.toFloat() / headerSizePx).coerceIn(0f, 1f)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AnimatedTopBarProfileProvider(
                title = if (isCompanyViewActive) "Perfil Profesional" else "Perfil",
                scrollProgress = scrollProgress,
                onBackClick = onNavigateBack
            )
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 1. HEADER (Dinamico segun empresa seleccionada)
                item {
                    val currentCompany = if (provider.companies.isNotEmpty()) provider.companies[companyPagerState.currentPage] else null
                    ProfileHeaderProvider(
                        provider = provider, 
                        company = currentCompany
                    )
                }

                // 2. CONTENIDO DE EMPRESA (Principal para el prestador)
                if (isCompanyViewActive && provider.hasCompanyProfile) {
                    if (provider.companies.isNotEmpty()) {
                        item {
                            HorizontalPager(
                                state = companyPagerState,
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                verticalAlignment = Alignment.Top
                            ) { page ->
                                val company = provider.companies[page]
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // --- [DETALLES DE EMPRESA] ---
                                    ArchiveroSectionProvider(
                                        title = "Detalles de Empresa",
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            
                                            // 🔥 Datos reales de Room
                                            InfoRowProvider(Icons.Default.Domain, "Razón Social", company.razonSocial)
                                            InfoRowProvider(Icons.Default.Badge, "CUIT", company.cuit)
                                            
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                            
                                            // Servicios ofrecidos (Lista real)
                                            Text("Servicios:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            
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
                                            
                                            // --- CARACTERÍSTICAS (Booleanos de BD) ---
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
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                                    
                                    // --- [SUCURSALES Y EQUIPOS] ---
                                    if (company.branches.isNotEmpty()) {
                                        val branchPagerState = rememberPagerState(pageCount = { company.branches.size })
                                        
                                        HorizontalPager(
                                            state = branchPagerState,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 4.dp)
                                        ) { branchPage ->
                                            val branch = company.branches[branchPage]
                                            
                                            Column {
                                                ArchiveroSectionProvider(
                                                    title = branch.name, 
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    InfoRowProvider(Icons.Default.Place, "Dirección", branch.address.fullString())
                                                }
                                                
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                                
                                                Text(
                                                    "Equipo de Trabajo", 
                                                    style = MaterialTheme.typography.titleMedium, 
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(start = 8.dp)
                                                )
                                                
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                                
                                                // Equipo real mapeado de BD
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
                                        
                                        if (company.branches.size > 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                                repeat(company.branches.size) { iteration ->
                                                    val color = if (branchPagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                                                    Box(modifier = Modifier.padding(4.dp).clip(CircleShape).background(color).size(8.dp))
                                                }
                                            }
                                        }
                                    }

                                    // --- [ALBUM DE TRABAJOS] ---
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
                                                    AsyncImage(
                                                        model = imgUrl,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize(),
                                                        error = painterResource(id = R.drawable.ic_image)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                            
                            if (provider.companies.size > 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    repeat(provider.companies.size) { iteration ->
                                        val color = if (companyPagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                                        Box(modifier = Modifier.padding(4.dp).clip(CircleShape).background(color).size(8.dp))
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
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Este perfil no tiene información pública de servicios.", color = Color.Gray)
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            // --- FAB GEMINI ---
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp).zIndex(10f),
                contentAlignment = Alignment.BottomEnd
            ) {
                GeminiSplitFAB(
                    isExpanded = isFabExpanded,
                    isSearchActive = isSearchActive,
                    onToggleExpand = { isFabExpanded = !isFabExpanded },
                    onActivateSearch = { },
                    onCloseSearch = { },
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
// --- COMPONENTES VISUALES REFACTORIZADOS (DATOS REALES) ---
// =================================================================================

/**
 * Cabecera del perfil (Consumiendo Provider y CompanyProvider reales).
 * 🔥 A futuro: Firebase Storage para imágenes.
 */
@Composable
fun ProfileHeaderProvider(
    provider: Provider,
    company: CompanyProvider?
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        // Banner (🔥 Firebase Storage a futuro)
        val bannerUrl = company?.productImages?.firstOrNull() ?: provider.bannerImageUrl
        AsyncImage(
            model = bannerUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.myeasteregg)
        )
        
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
        )))

        if (company != null) {
            IconButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Info, contentDescription = "Sobre Nosotros", tint = Color.White)
            }
        }

        if (provider.isSubscribed) {
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(top = 40.dp, start = 0.dp),
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

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 20.dp), 
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.Bottom
        ) {
            Box {
                // 🔥 Avatar real de BD
                val avatarModel = if (company != null) company.photoUrl ?: provider.photoUrl else provider.photoUrl
                AsyncImage(
                    model = avatarModel,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).clip(CircleShape).border(3.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.iconapp)
                )
                
                if (provider.isVerified) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verificado",
                        tint = Color(0xFF1DA1F2),
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(28.dp).background(Color.White, CircleShape).border(2.dp, Color.White, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Nombre real de la empresa o persona
            Text(
                text = company?.name ?: "${provider.name} ${provider.lastName}", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            if (company != null) {
                Text(
                    text = "Por ${provider.name} ${provider.lastName} ${provider.titulo?.let { "- $it" } ?: ""}", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = Color.White.copy(alpha = 0.8f)
                )
                provider.matricula?.let {
                     Text(text = "Matrícula: $it", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
            } else {
                 provider.titulo?.let {
                     Text(text = it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                 }
            }
        }
    }

    if (showAboutDialog && company != null) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.Business, null) },
            title = { Text(company.name) },
            text = { Text(company.description) }, // 🔥 Descripción real de BD
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("Cerrar") } }
        )
    }
}

/**
 * Tarjeta de empleado real (Consumiendo EmployeeProvider).
 * 🔥 A futuro: Firebase Auth/Database para perfiles de equipo.
 */
@Composable
fun EmployeeRoundedCardProvider(employee: EmployeeProvider) {
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
            AsyncImage(
                model = employee.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.iconapp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text("${employee.name} ${employee.lastName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(employee.position, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            }
        }
    }
}

// --- [COMPONENTES AUXILIARES INTACTOS] ---

@Composable
fun FeatureIcon(icon: ImageVector, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, if (isActive) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun ArchiveroSectionProvider(title: String, color: Color, content: @Composable () -> Unit) {
    // 🔥 A futuro: Sincronizar secciones dinámicas con Firebase.
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
            Box(modifier = Modifier.size(width = 4.dp, height = 18.dp).background(color, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxWidth().background(color.copy(alpha = 0.05f)).padding(16.dp)) { content() }
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
