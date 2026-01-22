package com.example.myapplication.Client

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultSerchCategoryScreen(
    categoryName: String, 
    onBack: () -> Unit, 
    navController: NavHostController
) {
    val category = CategorySampleDataFalso.categories.find { it.name == categoryName }
    val professionals = category?.providerIds?.mapNotNull { SampleDataFalso.getPrestadorById(it) } ?: emptyList()

    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = category?.name ?: "Categoría no encontrada")
                        Spacer(modifier = Modifier.width(8.dp))
                        category?.icon?.let {
                            Icon(imageVector = it, contentDescription = null, tint = category.color)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Recomendados") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Todos") }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> RecommendedTab(professionals = professionals.filter { it.isSubscribed }, navController = navController)
                    1 -> AllTab(professionals = professionals.filter { !it.isSubscribed }, navController = navController)
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = "Lo sentimos mucho, pero en estos momentos, no hay Prestadores de Servicios registrados en esta Categoría 😥",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Estamos seguro de que muy pronto estará llena 😊",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedTab(professionals: List<PrestadorProfileFalso>, navController: NavHostController) {
    var verifiedOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("Rating") }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredAndSortedProfessionals = remember(professionals, verifiedOnly, works24hOnly, sortOrder) {
        professionals
            .filter { if (verifiedOnly) it.isVerified else true }
            .filter { if (works24hOnly) it.works24h else true }
            .sortedByDescending {
                when (sortOrder) {
                    "Rating" -> it.rating
                    else -> it.rating // Default sort
                }
            }
    }

    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            // Filters and Sorting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = verifiedOnly,
                    onClick = { verifiedOnly = !verifiedOnly },
                    label = { Text("Verificado") },
                    leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                FilterChip(
                    selected = works24hOnly,
                    onClick = { works24hOnly = !works24hOnly },
                    label = { Text("24hs") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    OutlinedButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sortOrder)
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Rating") },
                            onClick = { sortOrder = "Rating"; showSortMenu = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredAndSortedProfessionals.isEmpty()) {
                EmptyStateMessage()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredAndSortedProfessionals) { professional ->
                        ProfessionalCard(professional = professional, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun AllTab(professionals: List<PrestadorProfileFalso>, navController: NavHostController) {
    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(professionals) { professional ->
                ProfessionalCard(professional = professional, navController = navController)
            }
        }
    }
}

@Composable
fun ProfessionalCard(professional: PrestadorProfileFalso, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.ProviderProfile.createRoute(professional.id)) },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Icon
            AsyncImage(
                model = professional.profileImageUrl,
                contentDescription = "Foto de perfil de ${professional.name}",
                fallback = painterResource(id = R.drawable.iconapp),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            // Name, Company, and attributes
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${professional.name} ${professional.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (professional.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Perfil Verificado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                professional.companyName?.let {
                    if (it.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Rating and Attribute Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attribute Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Servicio 24hs",
                            modifier = Modifier.size(20.dp),
                            tint = if (professional.works24h) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Servicio a domicilio",
                            modifier = Modifier.size(20.dp),
                            tint = if (professional.doesHomeVisits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Tiene local físico",
                            modifier = Modifier.size(20.dp),
                            tint = if (professional.hasPhysicalLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorito",
                            modifier = Modifier.size(20.dp),
                            tint = if (professional.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFFFC107) // Yellow star
                        )
                        Text(
                            text = professional.rating.toString(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Message Button
            FilledTonalIconButton(
                onClick = { /* TODO: Handle message action */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar mensaje",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultSerchCategoryScreenPreview() {
    MyApplicationTheme {
        ResultSerchCategoryScreen(categoryName = "Electricidad", onBack = {}, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun EmptyResultSerchCategoryScreenPreview() {
    MyApplicationTheme {
        ResultSerchCategoryScreen(categoryName = "Astrología", onBack = {}, navController = rememberNavController())
    }
}
