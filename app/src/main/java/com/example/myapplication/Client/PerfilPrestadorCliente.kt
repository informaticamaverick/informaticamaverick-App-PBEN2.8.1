package com.example.myapplication.Client

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// ============================================
// PANTALLA DE PERFIL DE PRESTADOR (Modo Visualización Cliente)
// ============================================

/**
 * Muestra el perfil de un prestador a un cliente.
 * 
 * TODO: INTEGRACIÓN CON FIREBASE / VIEWMODEL
 * Actualmente esta pantalla obtiene los datos de 'SampleDataFalso.getPrestadorById(providerId)'.
 * En el futuro, se debería:
 * 1. Inyectar un ViewModel (ej: PrestadorProfileViewModel).
 * 2. El ViewModel debería llamar a un repositorio que obtenga los datos de Firestore usando el 'providerId'.
 * 3. Observar un StateFlow<PrestadorUiState> en lugar de llamar directamente al objeto estático.
 * 4. Manejar estados de carga (Loading) y error (Error) adecuadamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilPrestadorCliente(providerId: String, onBack: () -> Unit) {
    // [TODO]: Reemplazar con llamada a ViewModel
    val profile = SampleDataFalso.getPrestadorById(providerId)

    // SI EL PERFIL NO SE ENCUENTRA, MOSTRAMOS UN MENSAJE
    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Proveedor no encontrado", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    // Estilos coherentes con PerfilUsuarioScreen
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF9F9F9)
    val textPrimaryColor = if (isDarkTheme) Color.White.copy(alpha = 0.87f) else Color.Black.copy(alpha = 0.87f)
    
    // FAB Expandido y animado (Moderno)
    // Usamos Box para superponer el FAB personalizado
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            // TopBar transparente que se mezcla con el header
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* TODO: Implementar navegación a pantalla de chat con este providerId */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                icon = { Icon(Icons.Filled.Chat, contentDescription = null) },
                text = { Text("Contactar", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        // Contenido principal Scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()) // Respetar FAB
                .verticalScroll(rememberScrollState())
        ) {
            // 1. HEADER (Banner + Foto + Verificación)
            ProviderHeaderModern(profile = profile)

            // 2. CUERPO PRINCIPAL
            Column(modifier = Modifier.padding(top = 16.dp)) {
                
                // Paginador de Información (Similar a PerfilUsuario)
                ProviderDetailsPagerModern(profile = profile, isDarkTheme = isDarkTheme)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 3. GALERÍA DE TRABAJOS
                ProviderGallerySectionModern(galleryImages = profile.galleryImages)
                
                Spacer(modifier = Modifier.height(80.dp)) // Espacio final para el FAB
            }
        }
    }
}

// ============================================
// COMPONENTES MODERNIZADOS
// ============================================

@Composable
fun ProviderHeaderModern(profile: PrestadorProfileFalso) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Más alto para mejor impacto visual
    ) {
        // Banner con gradiente
        AsyncImage(
            model = profile.bannerImageUrl,
            contentDescription = "Banner",
            modifier = Modifier
                .fillMaxSize()
                .height(220.dp), // El banner ocupa parte del box
            contentScale = ContentScale.Crop
        )
        
        // Gradiente oscuro para legibilidad superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(0.6f), Color.Transparent)))
        )

        // Contenedor de la Foto de Perfil (Centrado y superpuesto)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-40).dp) // Subir un poco
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = profile.profileImageUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .border(4.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    // Badge de Verificación Premium
                    if (profile.isVerified) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = "Verificado",
                                tint = Color.White,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Nombre y Rating
                Text(
                    text = "${profile.name} ${profile.lastName}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // Rating Bar Visual
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    Text(
                        text = "${profile.rating} (50 reseñas)", // TODO: Obtener número real de reseñas desde la BD
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProviderDetailsPagerModern(profile: PrestadorProfileFalso, isDarkTheme: Boolean) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    
    // Colores basados en PerfilUsuarioScreen
    val textPrimaryColor = if (isDarkTheme) Color.White.copy(alpha = 0.87f) else Color.Black.copy(alpha = 0.87f)
    val textSecondaryColor = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Indicador de Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val items = listOf("Información", "Servicios")
            items.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                        .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp), // Altura fija ajustada
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp,
            verticalAlignment = Alignment.Top
        ) { page ->
            if (page == 0) {
                // TARJETA DE INFORMACIÓN (Estilo PerfilUsuarioScreen)
                InfoCardModern(
                    title = "Sobre el Profesional",
                    icon = Icons.Filled.Person,
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE3F2FD)
                ) {
                    Column {
                        DisplayItemModern(Icons.Filled.LocationOn, "Ubicación", profile.address, textPrimaryColor, textSecondaryColor)
                        
                        // Lógica para mostrar disponibilidad basada en los datos falsos
                        val availabilityText = if (profile.works24h) "Disponible 24hs" else "Horario Comercial"
                        DisplayItemModern(Icons.Filled.AccessTime, "Disponibilidad", availabilityText, textPrimaryColor, textSecondaryColor)
                        
                        // Lógica para mostrar modalidad
                        DisplayItemModern(Icons.Filled.HomeWork, "Modalidad", buildString {
                            if (profile.doesHomeVisits) append("• A Domicilio\n")
                            if (profile.hasPhysicalLocation) append("• Local Físico")
                            if (!profile.doesHomeVisits && !profile.hasPhysicalLocation) append("• Remoto / Consultar")
                        }.trim(), textPrimaryColor, textSecondaryColor)
                    }
                }
            } else {
                // TARJETA DE SERVICIOS (Estilo PerfilUsuarioScreen)
                InfoCardModern(
                    title = "Servicios y Empresa",
                    icon = Icons.Filled.BusinessCenter,
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF1F8E9)
                ) {
                    Column {
                        profile.companyName?.let {
                            DisplayItemModern(Icons.Filled.Domain, "Empresa", it, textPrimaryColor, textSecondaryColor)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        
                        Text(
                            "Especialidades:",
                            style = MaterialTheme.typography.labelMedium,
                            color = textSecondaryColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // Lista de Servicios como Chips o Items
                        profile.services.forEach { service ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                                Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(service, style = MaterialTheme.typography.bodyMedium, color = textPrimaryColor, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCardModern(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DisplayItemModern(icon: ImageVector, label: String, value: String, textPrimary: Color, textSecondary: Color) {
    Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = textSecondary)
            Text(value.ifEmpty { "No especificado" }, style = MaterialTheme.typography.bodyLarge, color = textPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}


@Composable
fun ProviderGallerySectionModern(galleryImages: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Galería de Trabajos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            // TODO: Implementar navegación a una pantalla de galería completa
            TextButton(onClick = { /* Ver todas */ }) {
                Text("Ver todo")
            }
        }

        // Diseño moderno de galería (Staggered-ish o Grid limpia)
        if (galleryImages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin imágenes disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Usamos un layout manual simple para evitar complejidad de LazyGrid anidado
            // Mostramos hasta 6 imágenes en una cuadrícula
            val displayImages = galleryImages.take(6)
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Filas de 3 imágenes
                displayImages.chunked(3).forEach { rowImages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowImages.forEach { imageUrl ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f), // Cuadrados
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Trabajo realizado",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        // Rellenar espacio si la fila no está completa
                        repeat(3 - rowImages.size) {
                             Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// PREVIEW CON DATOS FALSOS
// ============================================

@Preview(showBackground = true)
@Composable
fun PerfilPrestadorScreenPreview() {
    MaterialTheme {
        // Usamos el ID "1" que corresponde a "Maxi Nanterne" en SampleDataFalso
        PerfilPrestadorCliente(providerId = "1", onBack = {})
    }
}
