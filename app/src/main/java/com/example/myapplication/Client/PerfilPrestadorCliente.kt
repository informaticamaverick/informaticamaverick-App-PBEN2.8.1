package com.example.myapplication.Client

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// ============================================
// PANTALLA PRINCIPAL
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilPrestadorCliente(providerId: String, onBack: () -> Unit) {
    // LOS DATOS AHORA SE OBTIENEN BASADO EN EL ID PROPORCIONADO
    val profile = SampleDataFalso.getPrestadorById(providerId)

    // SI EL PERFIL NO SE ENCUENTRA, MOSTRAMOS UN MENSAJE
    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Proveedor no encontrado")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del Prestador") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { /* TODO: Lógica para navegar al chat */ },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Enviar Mensaje",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            ProfileHeader(profile = profile)
            ProviderDetailsPager(profile = profile)
            Spacer(modifier = Modifier.height(16.dp))
            ProviderGallerySection(galleryImages = profile.galleryImages)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ============================================
// COMPONENTES DE LA PANTALLA
// ============================================

@Composable
fun ProfileHeader(profile: PrestadorProfileFalso) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AsyncImage(
            model = profile.bannerImageUrl,
            contentDescription = "Banner del prestador",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                // Esta llamada ahora usa la versión de FunComunesIUClienteFalso.kt
                //RatingBar(rating = profile.rating)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 60.dp)
                .size(120.dp)
        ) {
            AsyncImage(
                model = profile.profileImageUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentScale = ContentScale.Crop
            )
            if (profile.isVerified) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Perfil Verificado",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProviderDetailsPager(profile: PrestadorProfileFalso) {
    val tabTitles = listOf("Datos Personales", "Servicios y Empresa")
    val pagerState = rememberPagerState { tabTitles.size }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${profile.name} ${profile.lastName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (profile.isVerified) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Perfil Verificado",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                when (page) {
                    0 -> PersonalInfoContent(profile = profile)
                    1 -> ServicesInfoContent(profile = profile)
                }
            }
        }
    }
}

@Composable
fun PersonalInfoContent(profile: PrestadorProfileFalso) {
    Column(modifier = Modifier.padding(16.dp)) {
        InfoRow(icon = Icons.Filled.LocationOn, text = profile.address)
        InfoRow(icon = Icons.Filled.Email, text = profile.email)
        InfoRow(icon = if (profile.doesHomeVisits) Icons.Filled.CheckCircle else Icons.Filled.Cancel, text = "Realiza visitas a domicilio", tint = if (profile.doesHomeVisits) Color(0xFF10B981) else Color.Red)
        InfoRow(icon = if (profile.hasPhysicalLocation) Icons.Filled.CheckCircle else Icons.Filled.Cancel, text = "Tiene local físico", tint = if (profile.hasPhysicalLocation) Color(0xFF10B981) else Color.Red)
        InfoRow(icon = if (profile.works24h) Icons.Filled.CheckCircle else Icons.Filled.Cancel, text = "Trabaja 24hs", tint = if (profile.works24h) Color(0xFF10B981) else Color.Red)
    }
}

@Composable
fun ServicesInfoContent(profile: PrestadorProfileFalso) {
    Column(modifier = Modifier.padding(16.dp)) {
        profile.companyName?.let {
            InfoRow(icon = Icons.Filled.Business, text = it)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        Text(
            text = "Servicios Ofrecidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        profile.services.forEach { service ->
            InfoRow(icon = Icons.Filled.Construction, text = service)
        }
    }
}


@Composable
fun InfoRow(icon: ImageVector, text: String, tint: Color = LocalContentColor.current) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ProviderGallerySection(galleryImages: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Galería de Trabajos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(300.dp), 
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(galleryImages) { imageUrl ->
                Card(shape = RoundedCornerShape(12.dp)) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Imagen de trabajo",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

// ============================================
// 4. VISTA PREVIA
// ============================================

@Preview(showBackground = true)
@Composable
fun PerfilPrestadorScreenPreview() {
    MaterialTheme {
        // AHORA LA VISTA PREVIA USA DATOS DE MUESTRA
        PerfilPrestadorCliente(providerId = "1", onBack = {})
    }
}
