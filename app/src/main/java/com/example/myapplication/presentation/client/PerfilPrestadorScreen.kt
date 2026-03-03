package com.example.myapplication.presentation.client

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.R

// --- MODELOS ---
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.BranchProvider
import com.example.myapplication.data.model.EmployeeProvider
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.local.CategoryEntity
//import com.example.myapplication.presentation.components.GeminiSplitFAB
import com.example.myapplication.ui.theme.MyApplicationTheme

// =================================================================================
// --- ESTADOS Y ENUMS ---
// =================================================================================
enum class ProfileTabMode {
    INDEPENDENT, COMPANY
}

// =================================================================================
// --- PANTALLA PRINCIPAL ---
// =================================================================================

@Composable
fun PerfilPrestadorCliente(
    providerId: String,
    onBack: () -> Unit,
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val providerState by providerViewModel.getProviderById(providerId).collectAsStateWithLifecycle(initialValue = null)
    val allCategories by categoryViewModel.categories.collectAsStateWithLifecycle()

    if (providerState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    PerfilPrestadorContent(
        provider = providerState!!,
        allCategories = allCategories,
        onNavigateBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PerfilPrestadorContent(
    provider: Provider,
    allCategories: List<CategoryEntity> = emptyList(),
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var isFabExpanded by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    // Estado del Tab (Si tiene empresas arranca ahí, si no, en independiente)
    val hasCompanies = provider.companies.isNotEmpty()
    var currentTab by remember { mutableStateOf(if (hasCompanies) ProfileTabMode.COMPANY else ProfileTabMode.INDEPENDENT) }

    // Estado del Modal de Horarios
    var showHoursModal by remember { mutableStateOf(false) }
    
    // Estado para agrandar imagen
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color(0xFFF3F4F6), // Fondo gris claro (Separador entre tarjetas)
        topBar = {
            // Botón de retroceso flotante
            Box(modifier = Modifier.padding(top = 40.dp, start = 16.dp).zIndex(20f)) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape).size(40.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            }
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 100.dp)
            ) {
                // 1. HEADER IMAGE & INFO DINÁMICO
                val currentCompany = provider.companies.firstOrNull()
                val bannerUrl = if (currentTab == ProfileTabMode.COMPANY && currentCompany != null) {
                    currentCompany.bannerImageUrl ?: provider.bannerImageUrl
                } else {
                    provider.bannerImageUrl
                }
                
                val photoUrl = if (currentTab == ProfileTabMode.COMPANY && currentCompany != null) {
                    currentCompany.photoUrl ?: provider.photoUrl
                } else {
                    provider.photoUrl
                }

                ProfileHeaderFullWidth(
                    provider = provider, 
                    bannerUrl = bannerUrl, 
                    photoUrl = photoUrl,
                    onImageClick = { fullscreenImageUrl = it }
                )

                // 2. TABS ANIMADOS (Solo se muestran si tiene empresas)
                if (hasCompanies) {
                    AnimatedHeaderTabs(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. CONTENIDO DINÁMICO
                if (currentTab == ProfileTabMode.INDEPENDENT) {
                    IndependentFullWidthSection(
                        provider = provider,
                        allCategories = allCategories,
                        onOpenHours = { showHoursModal = true },
                        onImageClick = { fullscreenImageUrl = it }
                    )
                } else {
                    CompanyFullWidthSection(
                        provider = provider,
                        allCategories = allCategories,
                        onOpenHours = { showHoursModal = true },
                        onImageClick = { fullscreenImageUrl = it }
                    )
                }
            }

            /**
            // --- FAB ---
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
                )
            }
            **/
            
            // --- FULLSCREEN IMAGE OVERLAY ---
            if (fullscreenImageUrl != null) {
                FullscreenImageOverlay(
                    imageUrl = fullscreenImageUrl!!,
                    onClose = { fullscreenImageUrl = null }
                )
            }
        }

        // --- MODAL DE HORARIOS ---
        if (showHoursModal) {
            ModalBottomSheet(
                onDismissRequest = { showHoursModal = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                HoursDetailContent(onClose = { showHoursModal = false })
            }
        }
    }
}

@Composable
fun FullscreenImageOverlay(imageUrl: String, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose
            )
            .zIndex(100f),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Imagen agrandada",
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
            contentScale = ContentScale.Fit
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp).background(Color.Black.copy(0.4f), CircleShape)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}

// =================================================================================
// --- ANIMACIÓN DE TABS (HEADER COMPONENT) ---
// =================================================================================
@Composable
fun AnimatedHeaderTabs(
    currentTab: ProfileTabMode,
    onTabSelected: (ProfileTabMode) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Cálculos de animación para desplazar los iconos al centro o a los bordes
    val indepOffset by animateDpAsState(
        targetValue = if (currentTab == ProfileTabMode.INDEPENDENT) 0.dp else (-screenWidth / 2 + 50.dp),
        animationSpec = tween(400)
    )
    val empOffset by animateDpAsState(
        targetValue = if (currentTab == ProfileTabMode.COMPANY) 0.dp else (screenWidth / 2 - 50.dp),
        animationSpec = tween(400)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .border(1.dp, Color(0xFFF3F4F6))
            .zIndex(10f),
        contentAlignment = Alignment.Center
    ) {
        // Tab Independiente
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .offset(x = indepOffset)
                .clickable { onTabSelected(ProfileTabMode.INDEPENDENT) }
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (currentTab == ProfileTabMode.INDEPENDENT) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = if (currentTab == ProfileTabMode.INDEPENDENT) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(20.dp))
            }
            AnimatedVisibility(visible = currentTab == ProfileTabMode.INDEPENDENT) {
                Text("Perfil Personal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
            }
        }

        // Tab Empresa
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .offset(x = empOffset)
                .clickable { onTabSelected(ProfileTabMode.COMPANY) }
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (currentTab == ProfileTabMode.COMPANY) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Domain, null, tint = if (currentTab == ProfileTabMode.COMPANY) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(20.dp))
            }
            AnimatedVisibility(visible = currentTab == ProfileTabMode.COMPANY) {
                Text("Empresas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

// =================================================================================
// --- SECCIONES FULL WIDTH ---
// =================================================================================

data class FeatureInfo(
    val emoji: String,
    val label: String,
    val isActive: Boolean,
    val description: String
)

/**
 * Sección de Perfil Personal (Independiente)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IndependentFullWidthSection(
    provider: Provider, 
    allCategories: List<CategoryEntity> = emptyList(),
    onOpenHours: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedFeatureInfo by remember { mutableStateOf<FeatureInfo?>(null) }

    val features = remember(provider) {
        listOf(
            FeatureInfo("🕒", "24Hs", provider.works24h, "Disponible las 24 horas para urgencias o consultas."),
            FeatureInfo("🏢", "Local", provider.hasPhysicalLocation, "Cuenta con un local físico para atención al público."),
            FeatureInfo("🏠", "Visitas", provider.doesHomeVisits, "Realiza visitas técnicas o servicios a domicilio."),
            FeatureInfo("🚚", "Envíos", provider.doesShipping, "Realiza envíos de productos o materiales."),
            FeatureInfo("📅", "Turnos", provider.acceptsAppointments, "Permite la reserva de turnos programados.")
        ).sortedByDescending { it.isActive }
    }

    Column {
        // --- TARJETA 1: DATOS PERSONALES ---
        Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("DATOS PERSONALES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                PersonalDataRow(emoji = "🎓", label = "PROFESIÓN / TÍTULO", value = provider.titulo ?: "No especificado")
                PersonalDataRow(emoji = "🆔", label = "CUIL / CUIT", value = provider.cuilCuit ?: "No especificado")
                PersonalDataRow(emoji = "📜", label = "MATRÍCULA PROFESIONAL", value = provider.matricula ?: "No posee")

                Spacer(modifier = Modifier.height(16.dp))

                provider.address?.let { addr ->
                    val fullAddr = addr.fullString()
                    Surface(
                        onClick = {
                            try {
                                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(fullAddr)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {}
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("📍", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("DIRECCIÓN DE COBERTURA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(fullAddr.ifEmpty { "A convenir" }, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Toca para abrir en Google Maps", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Botón flotante de Horarios en el corner derecho de la sección con texto debajo
            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SmallFloatingActionButton(
                    onClick = onOpenHours,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text("🕒", fontSize = 20.sp)
                }
                Text("Horarios", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- TARJETA 2: CARACTERÍSTICAS BASE ---
        Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
            Text("CARACTERÍSTICAS BASE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                features.forEach { feature ->
                    FeatureEmojiItem(
                        emoji = feature.emoji,
                        label = feature.label,
                        isActive = feature.isActive,
                        onClick = { selectedFeatureInfo = feature }
                    )
                }
            }
        }

        // Popup informativo de características
        if (selectedFeatureInfo != null) {
            AlertDialog(
                onDismissRequest = { selectedFeatureInfo = null },
                icon = { Text(selectedFeatureInfo!!.emoji, fontSize = 32.sp) },
                title = { Text(selectedFeatureInfo!!.label, fontWeight = FontWeight.Bold) },
                text = { Text(selectedFeatureInfo!!.description, textAlign = TextAlign.Center) },
                confirmButton = {
                    TextButton(onClick = { selectedFeatureInfo = null }) {
                        Text("Cerrar")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- TARJETA 3: ESPECIALIDADES (CON ICONOS Y COLORES DINÁMICOS) ---
        if (provider.categories.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
                Text("ESPECIALIDADES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    provider.categories.forEach { catName ->
                        val categoryEntity = allCategories.find { it.name.equals(catName, ignoreCase = true) }
                        val catColor = categoryEntity?.let { Color(it.color) } ?: MaterialTheme.colorScheme.primary
                        val catIcon = categoryEntity?.icon ?: "🏷️"
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = catColor.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, catColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(catIcon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(catName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = catColor)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- TARJETA 4: GALERÍA (PRODUCTOS O SERVICIOS) ---
        Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
            Text("GALERÍA DE TRABAJOS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp))
            if (provider.galleryImages.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(provider.galleryImages) { imgUrl ->
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                                .clickable { onImageClick(imgUrl) },
                            error = painterResource(id = R.drawable.ic_launcher_background)
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(100.dp).background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text("Todavía no tiene imágenes cargadas", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/**
 * Sección de Empresa y Sucursales
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun CompanyFullWidthSection(
    provider: Provider, 
    allCategories: List<CategoryEntity> = emptyList(),
    onOpenHours: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    val companyPagerState = rememberPagerState(pageCount = { provider.companies.size })
    var selectedFeatureInfo by remember { mutableStateOf<FeatureInfo?>(null) }
    var selectedEmployee by remember { mutableStateOf<EmployeeProvider?>(null) }

    Column {
        // Indicador de Empresas si hay más de 1
        if (provider.companies.size > 1) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                repeat(provider.companies.size) { iteration ->
                    val color = if (companyPagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color(0xFFD1D5DB)
                    Box(modifier = Modifier.padding(3.dp).size(6.dp).clip(CircleShape).background(color))
                }
            }
        }

        HorizontalPager(state = companyPagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val company = provider.companies[page]

            val companyFeatures = remember(company) {
                listOf(
                    FeatureInfo("🕒", "24Hs", company.works24h, "Empresa con servicio 24hs."),
                    FeatureInfo("🏢", "Local", company.hasPhysicalLocation, "La empresa cuenta con local físico."),
                    FeatureInfo("🏠", "Visitas", company.doesHomeVisits, "Realiza visitas a domicilio."),
                    FeatureInfo("🚚", "Envíos", company.doesShipping, "Realiza envíos de productos."),
                    FeatureInfo("📅", "Turnos", company.acceptsAppointments, "Acepta turnos programados.")
                ).sortedByDescending { it.isActive }
            }

            Column {
                // --- TARJETA 1: INFO DE EMPRESA ---
                Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(company.name.ifEmpty { "Nuestra Empresa" }, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            if (company.isVerified) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Filled.Verified, null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        if (company.description.isNotEmpty()) {
                            Text(company.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text("DATOS DE EMPRESA", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                        PersonalDataRow(emoji = "🏢", label = "RAZÓN SOCIAL", value = company.razonSocial.ifEmpty { "No especificada" })
                        PersonalDataRow(emoji = "🆔", label = "CUIT", value = company.cuit.ifEmpty { "No especificado" })
                    }

                    // Botón flotante de Horarios Empresa
                    Column(
                        modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SmallFloatingActionButton(
                            onClick = onOpenHours,
                            containerColor = Color(0xFF1DA1F2),
                            contentColor = Color.White
                        ) {
                            Text("🕒", fontSize = 20.sp)
                        }
                        Text("Horarios", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1DA1F2), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- TARJETA 2: CARACTERÍSTICAS EMPRESA ---
                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
                    Text("CARACTERÍSTICAS GENERALES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        companyFeatures.forEach { feature ->
                            FeatureEmojiItem(
                                emoji = feature.emoji,
                                label = feature.label,
                                isActive = feature.isActive,
                                onClick = { selectedFeatureInfo = feature }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- TARJETA 3: ESPECIALIDADES DE EMPRESA ---
                if (company.categories.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
                        Text("ESPECIALIDADES DE LA EMPRESA", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            company.categories.forEach { catName ->
                                val categoryEntity = allCategories.find { it.name.equals(catName, ignoreCase = true) }
                                val catColor = categoryEntity?.let { Color(it.color) } ?: Color(0xFF1DA1F2)
                                val catIcon = categoryEntity?.icon ?: "🏷️"
                                
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = catColor.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, catColor.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(catIcon, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(catName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = catColor)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- TARJETA 4: SUCURSALES ---
                val allBranches = listOfNotNull(company.mainBranch) + company.branches
                if (allBranches.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(vertical = 16.dp)) {
                        Text("SUCURSALES (${allBranches.size})", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp))

                        val branchPagerState = rememberPagerState(pageCount = { allBranches.size })

                        HorizontalPager(
                            state = branchPagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { branchPage ->
                            val branch = allBranches[branchPage]
                            val isMainBranch = branch == company.mainBranch

                            val branchFeatures = remember(branch) {
                                listOf(
                                    FeatureInfo("🕒", "24Hs", branch.works24h, "Sucursal disponible las 24 horas."),
                                    FeatureInfo("🏢", "Local", branch.hasPhysicalLocation, "Esta sucursal atiende al público en el local."),
                                    FeatureInfo("🏠", "Visitas", branch.doesHomeVisits, "Realiza servicios a domicilio desde esta sede."),
                                    FeatureInfo("🚚", "Envíos", branch.doesShipping, "Realiza envíos desde esta sucursal."),
                                    FeatureInfo("📅", "Turnos", branch.acceptsAppointments, "Permite reserva de turnos en esta sede.")
                                ).sortedByDescending { it.isActive }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White)
                                    .border(if (isMainBranch) 2.dp else 1.dp, if (isMainBranch) Color(0xFF1DA1F2).copy(alpha=0.3f) else Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                                    .padding(20.dp)
                            ) {
                                Column {
                                    if (isMainBranch) {
                                        Surface(color = Color(0xFF1DA1F2), shape = CircleShape, modifier = Modifier.padding(bottom = 12.dp)) {
                                            Text("📍 CASA CENTRAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                        }
                                    }

                                    Text(branch.name.ifEmpty { if(isMainBranch) "Sede Principal" else "Sucursal" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    
                                    // Dirección Clickable Branch
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        onClick = {
                                            try {
                                                val fullAddr = branch.address.fullString()
                                                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(fullAddr)}")
                                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                context.startActivity(mapIntent)
                                            } catch (e: Exception) {}
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF3F4F6),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("📍", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(branch.address.fullString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                Text("Abrir mapas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF3F4F6))

                                    // Branch Features Reordered
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        branchFeatures.forEach { feature ->
                                            FeatureEmojiItem(
                                                emoji = feature.emoji,
                                                label = feature.label,
                                                isActive = feature.isActive,
                                                onClick = { selectedFeatureInfo = feature }
                                            )
                                        }
                                    }

                                    if (branch.employees.isNotEmpty()) {
                                        Text("REFERENTE DE SEDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
                                        val emp = branch.employees.first()
                                        Surface(
                                            onClick = { selectedEmployee = emp },
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color(0xFFF9FAFB),
                                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    model = emp.photoUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
                                                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text("${emp.name} ${emp.lastName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                    Text(emp.position, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1DA1F2))
                                                }
                                                Spacer(modifier = Modifier.weight(1f))
                                                Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    // Galería por sucursal
                                    Text("GALERÍA DE ESTA SEDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
                                    if (branch.galleryImages.isNotEmpty()) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(branch.galleryImages) { imgUrl ->
                                                AsyncImage(
                                                    model = imgUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(100.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                                                        .clickable { onImageClick(imgUrl) },
                                                    error = painterResource(id = R.drawable.ic_launcher_background)
                                                )
                                            }
                                        }
                                    } else {
                                        Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                            Text("Todavía no tiene imágenes cargadas", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                                
                                // Botón flotante horarios sucursal
                                Column(
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    SmallFloatingActionButton(
                                        onClick = onOpenHours,
                                        containerColor = Color(0xFFF3F4F6),
                                        contentColor = Color.Gray
                                    ) {
                                        Text("🕒", fontSize = 16.sp)
                                    }
                                    if (branch.workingHours.isNotEmpty()) {
                                        Text("Horarios", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        // Puntos de Sucursales
                        if (allBranches.size > 1) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                                repeat(allBranches.size) { iteration ->
                                    val color = if (branchPagerState.currentPage == iteration) Color(0xFF1DA1F2) else Color(0xFFD1D5DB)
                                    Box(modifier = Modifier.padding(3.dp).size(6.dp).clip(CircleShape).background(color))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- TARJETA 5: PRODUCTOS DESTACADOS EMPRESA ---
                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
                    Text("PRODUCTOS DESTACADOS EMPRESA", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp))
                    if (company.productImages.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(company.productImages) { imgUrl ->
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                                        .clickable { onImageClick(imgUrl) },
                                    error = painterResource(id = R.drawable.ic_launcher_background)
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(100.dp).background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Text("Todavía no tiene imágenes cargadas", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    // Popup informativo reutilizado
    if (selectedFeatureInfo != null) {
        AlertDialog(
            onDismissRequest = { selectedFeatureInfo = null },
            icon = { Text(selectedFeatureInfo!!.emoji, fontSize = 32.sp) },
            title = { Text(selectedFeatureInfo!!.label, fontWeight = FontWeight.Bold) },
            text = { Text(selectedFeatureInfo!!.description, textAlign = TextAlign.Center) },
            confirmButton = {
                TextButton(onClick = { selectedFeatureInfo = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Popup de detalles del Referente
    if (selectedEmployee != null) {
        ReferenteDetailDialog(
            employee = selectedEmployee!!, 
            onDismiss = { selectedEmployee = null },
            onImageClick = onImageClick
        )
    }
}

// =================================================================================
// --- COMPONENTES VISUALES BASE ---
// =================================================================================

@Composable
fun ReferenteDetailDialog(employee: EmployeeProvider, onDismiss: () -> Unit, onImageClick: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        icon = {
            AsyncImage(
                model = employee.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF1DA1F2), CircleShape)
                    .clickable { employee.photoUrl?.let { onImageClick(it) } },
                error = painterResource(id = R.drawable.ic_launcher_foreground)
            )
        },
        title = {
            Text("${employee.name} ${employee.lastName}", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(color = Color(0xFF1DA1F2).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(employee.position, color = Color(0xFF1DA1F2), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(employee.detail.ifEmpty { "Referente de atención técnica y comercial de esta sucursal." }, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun ProfileHeaderFullWidth(
    provider: Provider, 
    bannerUrl: String?, 
    photoUrl: String?,
    onImageClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        AsyncImage(
            model = bannerUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clickable { bannerUrl?.let { onImageClick(it) } },
            error = painterResource(id = R.drawable.ic_launcher_background)
        )

        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))))

        if (provider.isSubscribed) {
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp),
                color = Color(0xFFFFD700),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 6.dp
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PREMIUM", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 10.sp)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable { photoUrl?.let { onImageClick(it) } },
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
                if (provider.isVerified) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verificado",
                        tint = Color(0xFF1DA1F2),
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).size(24.dp).background(Color.White, CircleShape).border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${provider.name} ${provider.lastName}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Text(text = provider.titulo ?: "Profesional Independiente", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(provider.rating.toString().ifEmpty { "Nuevo" }, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

// --- NUEVOS COMPONENTES PARA PERFIL PERSONAL ---

@Composable
fun PersonalDataRow(emoji: String, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}

@Composable
fun FeatureEmojiItem(emoji: String, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF9FAFB),
            tonalElevation = if (isActive) 2.dp else 0.dp,
            border = if (isActive) null else BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = emoji, 
                    fontSize = 24.sp, 
                    modifier = Modifier.graphicsLayer { alpha = if (isActive) 1f else 0.3f }
                )
                if (!isActive) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawLine(
                            color = Color.Red.copy(alpha = 0.6f),
                            start = Offset(size.width * 0.15f, size.height * 0.85f),
                            end = Offset(size.width * 0.85f, size.height * 0.15f),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label, 
            fontSize = 10.sp, 
            color = if (isActive) Color.DarkGray else Color.Gray, 
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun HoursDetailContent(onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Detalle de Horarios", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onClose, modifier = Modifier.background(Color(0xFFF3F4F6), CircleShape).size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val days = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        days.forEach { day ->
            val isWeekend = day == "Sábado" || day == "Domingo"
            val isClosed = day == "Domingo"

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(12.dp)).background(if(isClosed) Color(0xFFFEF2F2) else Color(0xFFF9FAFB)).border(1.dp, if(isClosed) Color(0xFFFEE2E2) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(day, fontWeight = FontWeight.SemiBold, color = if(isClosed) Color(0xFFB91C1C) else Color.DarkGray)
                Text(if(isClosed) "Cerrado" else if(isWeekend) "09:00 - 13:00" else "09:00 - 18:00", fontWeight = FontWeight.Bold, color = if(isClosed) Color(0xFFEF4444) else if(isWeekend) Color.Gray else MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
            Text("Entendido", fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPrestadorClientePreview() {
    MyApplicationTheme {
        val mockAddress = AddressProvider(
            calle = "San Martín",
            numero = "450",
            localidad = "San Miguel de Tucumán",
            provincia = "Tucumán",
            pais = "Argentina"
        )

        val mockProvider = Provider(
            uid = "1001",
            email = "maverick@tech.com",
            displayName = "Maverick Informática",
            name = "Maximiliano",
            lastName = "Nanterne",
            phoneNumber = "381-1234567",
            matricula = "MP-9922",
            titulo = "Ingeniero de Software",
            cuilCuit = "20-30405060-7",
            address = mockAddress,
            works24h = true,
            hasPhysicalLocation = true,
            doesHomeVisits = true,
            doesShipping = false,
            acceptsAppointments = true,
            isSubscribed = true,
            isVerified = true,
            isOnline = true,
            isFavorite = true,
            rating = 5.0f,
            workingHours = "Lunes a Sábado: 09:00 a 20:00 hs",
            categories = listOf("Informatica", "Desarrollo Móvil", "Redes"),
            description = "Especialistas en soluciones tecnológicas de alta complejidad.",
            hasCompanyProfile = true,
            companies = listOf(
                CompanyProvider(
                    name = "Maverick Tech S.A.",
                    razonSocial = "Maverick Soluciones Digitales S.R.L.",
                    cuit = "30-12345678-9",
                    description = "Nuestra misión es llevar la tecnología a cada negocio de Tucumán.",
                    categories = listOf("Software", "Hardware"),
                    works24h = true,
                    hasPhysicalLocation = true,
                    isVerified = true,
                    mainBranch = BranchProvider(
                        name = "Sede Central",
                        address = mockAddress,
                        employees = listOf(
                            EmployeeProvider(name = "Ana", lastName = "Gómez", position = "Líder de Soporte")
                        ),
                        galleryImages = listOf("https://picsum.photos/seed/b1/200/200")
                    ),
                    productImages = listOf("https://picsum.photos/seed/p1/200/200", "https://picsum.photos/seed/p2/200/200"),
                    photoUrl = "https://picsum.photos/seed/cp1/200/200",
                    bannerImageUrl = "https://picsum.photos/seed/cb1/800/400"
                )
            ),
            photoUrl = "https://picsum.photos/seed/pp1/200/200",
            bannerImageUrl = "https://picsum.photos/seed/pb1/800/400",
            galleryImages = listOf("https://picsum.photos/seed/1/200/200", "https://picsum.photos/seed/2/200/200"),
            createdAt = System.currentTimeMillis()
        )

        val mockCategories = listOf(
            CategoryEntity("Informatica", "💻", 0xFF2197F5, "Tecnologia", "📂", emptyList(), null, false, false, false),
            CategoryEntity("Desarrollo Móvil", "📱", 0xFFE91E63, "Tecnologia", "📂", emptyList(), null, false, false, false),
            CategoryEntity("Redes", "🌐", 0xFF4CAF50, "Tecnologia", "📂", emptyList(), null, false, false, false),
            CategoryEntity("Software", "💾", 0xFF673AB7, "Tecnologia", "📂", emptyList(), null, false, false, false),
            CategoryEntity("Hardware", "🔌", 0xFFFF9800, "Tecnologia", "📂", emptyList(), null, false, false, false)
        )

        PerfilPrestadorContent(
            provider = mockProvider,
            allCategories = mockCategories,
            onNavigateBack = {}
        )
    }
}
