package com.example.myapplication.Client

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestosScreen(
    onProfileClick: (String) -> Unit = {},
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Presupuestos de Licitaciones", "Presupuestos Generales")
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val allPresupuestos = remember { ClientBudgetDataFalso.presupuestos }
    val licitacionesAgrupadas = allPresupuestos.filter { it.esLicitacion }.groupBy { it.nombre }
    val presupuestosGenerales = allPresupuestos.filter { !it.esLicitacion }

    var selectedLicitacionBudgets by remember { mutableStateOf<List<PresupuestoFalso>>(emptyList()) }
    var showBudgetSheet by remember { mutableStateOf(false) }
    // FIX: Se agrega el estado para el BottomSheet expandible.
    // COMENTARIO: 'skipPartiallyExpanded = false' permite que el sheet tenga un estado intermedio y se pueda expandir completamente.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    if (showBudgetSheet) {
        // FIX: Se utiliza el nuevo 'sheetState' y se ajusta el contenedor.
        // COMENTARIO: Se pasa el 'sheetState' al ModalBottomSheet y se usa 'fillMaxSize()' para que el contenido
        // pueda ocupar toda la pantalla cuando se expande.
        ModalBottomSheet(
            onDismissRequest = { showBudgetSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxSize()
        ) {
            LicitacionDetailSheetContent(
                budgets = selectedLicitacionBudgets,
                onProfileClick = onProfileClick,
                onChatClick = onChatClick,
                onBudgetClick = { imageUrl -> selectedImageUrl = imageUrl }
            )
        }
    }

    if (selectedImageUrl != null) {
        Dialog(onDismissRequest = { selectedImageUrl = null }) {
            Image(
                painter = rememberAsyncImagePainter(model = selectedImageUrl),
                contentDescription = "Imagen del Presupuesto",
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!isSearchActive) Text("Presupuestos Recibidos")
                },
                navigationIcon = {
                    if (!isSearchActive) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
                            placeholder = { Text("Buscar...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    isSearchActive = false
                                    searchQuery = ""
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda")
                                }
                            },
                            singleLine = true,
                            shape = CircleShape
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !isSearchActive) {
                FloatingActionButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar Presupuesto")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> LicitacionesTabContent(
                    licitaciones = licitacionesAgrupadas,
                    searchQuery = searchQuery,
                    onLicitacionClick = { budgets ->
                        selectedLicitacionBudgets = budgets
                        showBudgetSheet = true
                    }
                )
                1 -> GeneralesTabContent(
                    presupuestos = presupuestosGenerales,
                    searchQuery = searchQuery,
                    onPresupuestoClick = { imageUrl -> selectedImageUrl = imageUrl },
                    onProfileClick = onProfileClick,
                    onChatClick = onChatClick
                )
            }
        }
    }
}

@Composable
fun LicitacionesTabContent(
    licitaciones: Map<String, List<PresupuestoFalso>>,
    searchQuery: String,
    onLicitacionClick: (List<PresupuestoFalso>) -> Unit
) {
    var selectedStatus by remember { mutableStateOf<EstadoLicitacion?>(null) }
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var sortAscending by remember { mutableStateOf(true) }

    val allCategories = licitaciones.values.flatten().map { it.servicioCategoria }.distinct()

    // FIX: Se implementó un comparador personalizado para el ordenamiento por defecto.
    // COMENTARIO: Este comparador define un orden de prioridad para los estados de licitación.
    // También prioriza las licitaciones activas que tienen nuevos presupuestos ('isNew').
    // El ordenamiento por fecha ('sortAscending') se aplica como un criterio secundario.
    val statusOrder = mapOf(
        EstadoLicitacion.ACTIVA to 1,
        EstadoLicitacion.ADJUDICADA to 2,
        EstadoLicitacion.TERMINADA to 3,
        EstadoLicitacion.CANCELADA to 4
    )

    val filteredAndSortedLicitaciones = licitaciones.filter { (nombre, presupuestos) ->
        val first = presupuestos.first()
        val statusMatch = selectedStatus == null || first.estadoLicitacion == selectedStatus
        val categoryMatch = selectedCategories.isEmpty() || selectedCategories.contains(first.servicioCategoria)
        val searchMatch = nombre.contains(searchQuery, ignoreCase = true) ||
                presupuestos.any { it.servicioCategoria.contains(searchQuery, ignoreCase = true) }
        statusMatch && categoryMatch && searchMatch
    }.entries.sortedWith(
        compareBy<Map.Entry<String, List<PresupuestoFalso>>> { (_, budgets) ->
            val hasNew = budgets.any { it.isNew }
            val status = budgets.first().estadoLicitacion
            if (status == EstadoLicitacion.ACTIVA && hasNew) 0 else 1
        }.thenBy { (_, budgets) ->
            statusOrder[budgets.first().estadoLicitacion] ?: 5
        }
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryFilterSplitButton(
                allCategories = allCategories,
                selectedCategories = selectedCategories,
                onCategoryToggle = { category ->
                    val newSelection = selectedCategories.toMutableSet()
                    if (newSelection.contains(category)) {
                        newSelection.remove(category)
                    } else {
                        newSelection.add(category)
                    }
                    selectedCategories = newSelection
                },
                onClear = { selectedCategories = emptySet() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            SortFilterChip(
                isAscending = sortAscending,
                onClick = { sortAscending = !sortAscending }
            )
            Spacer(modifier = Modifier.weight(1f))
            StatusFilterSplitButton(
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it }
            )
        }

        if (filteredAndSortedLicitaciones.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredAndSortedLicitaciones, key = { it.key }) { (nombre, presupuestos) ->
                    val licitacionInfo = presupuestos.first()
                    val hasNew = presupuestos.any { it.isNew }
                    LicitacionArchiveroCard(
                        categoriaNombre = licitacionInfo.servicioCategoria,
                        licitacionNombre = nombre,
                        fechaInicio = licitacionInfo.fechaInicioLicitacion ?: "-",
                        fechaFin = licitacionInfo.fechaFinLicitacion ?: "-",
                        status = licitacionInfo.estadoLicitacion.displayName,
                        statusColor = licitacionInfo.estadoLicitacion.color,
                        presupuestosCount = presupuestos.size,
                        hasNewBudgets = hasNew,
                        onClick = { onLicitacionClick(presupuestos) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryFilterSplitButton(
    allCategories: List<String>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isSelected = selectedCategories.isNotEmpty()
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val elevation by animateDpAsState(targetValue = if (expanded) 8.dp else 2.dp, label = "elevation")

    Box {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = elevation,
            shadowElevation = elevation,
            modifier = Modifier.height(44.dp).animateContentSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Parte Izquierda
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = { expanded = true },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        )
                        .padding(start = 12.dp, end = 8.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Category, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSelected) "${selectedCategories.size} Categorías" else "Categorías",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Divisor
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(contentColor.copy(alpha = 0.3f)))

                // Parte Derecha
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = { if (isSelected) onClear() else expanded = true },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        )
                        .padding(horizontal = 10.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isSelected,
                        transitionSpec = {
                            (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                        },
                        label = "icon"
                    ) { selected ->
                        Icon(
                            imageVector = if (selected) Icons.Default.Close else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(220.dp)
        ) {
            Text(
                "Filtrar por categoría",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Gray
            )
            allCategories.forEach { category ->
                val isCategorySelected = selectedCategories.contains(category)
                DropdownMenuItem(
                    modifier = if (isCategorySelected) {
                        Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    } else Modifier,
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCategorySelected) Icons.Default.CheckCircle else Icons.Default.Circle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isCategorySelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(category, fontWeight = if (isCategorySelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    onClick = { onCategoryToggle(category) }
                )
            }
        }
    }
}

@Composable
fun StatusFilterSplitButton(
    selectedStatus: EstadoLicitacion?,
    onStatusSelected: (EstadoLicitacion?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isSelected = selectedStatus != null
    val containerColor = if (isSelected) selectedStatus.color else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val elevation by animateDpAsState(targetValue = if (expanded) 8.dp else 2.dp, label = "elevation")

    Box {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = elevation,
            shadowElevation = elevation,
            modifier = Modifier.height(44.dp).animateContentSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Parte Izquierda
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = { expanded = true },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        )
                        .padding(start = 12.dp, end = 8.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSelected) {
                                when (selectedStatus) {
                                    EstadoLicitacion.ACTIVA -> Icons.Default.PlayCircle
                                    EstadoLicitacion.TERMINADA -> Icons.Default.CheckCircle
                                    EstadoLicitacion.ADJUDICADA -> Icons.Default.WorkspacePremium
                                    EstadoLicitacion.CANCELADA -> Icons.Default.Cancel
                                }
                            } else Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedStatus?.displayName ?: "Estado",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Divisor
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(contentColor.copy(alpha = 0.3f)))

                // Parte Derecha
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = { if (isSelected) onStatusSelected(null) else expanded = true },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        )
                        .padding(horizontal = 10.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isSelected,
                        transitionSpec = {
                            (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                        },
                        label = "icon"
                    ) { selected ->
                        Icon(
                            imageVector = if (selected) Icons.Default.Close else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(180.dp)
        ) {
            Text(
                "Filtrar por estado",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Gray
            )
            EstadoLicitacion.entries.forEach { status ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(status.color))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(status.displayName)
                        }
                    },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun GeneralesTabContent(
    presupuestos: List<PresupuestoFalso>,
    searchQuery: String,
    onPresupuestoClick: (String?) -> Unit,
    onProfileClick: (String) -> Unit,
    onChatClick: (String) -> Unit
) {
    val filteredPresupuestos = presupuestos.filter {
        it.nombre.contains(searchQuery, ignoreCase = true) ||
                it.prestadorNombre.contains(searchQuery, ignoreCase = true)
    }

    if (filteredPresupuestos.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredPresupuestos, key = { it.id }) { presupuesto ->
                PresupuestoGeneralCard(
                    presupuesto = presupuesto,
                    onClick = { onPresupuestoClick(presupuesto.imageUrl) },
                    onProfileClick = { onProfileClick(presupuesto.prestadorId) },
                    onChatClick = { onChatClick(presupuesto.prestadorId) }
                )
            }
        }
    }
}

@Composable
fun LicitacionArchiveroCard(
    categoriaNombre: String,
    licitacionNombre: String,
    fechaInicio: String,
    fechaFin: String,
    status: String,
    statusColor: Color,
    presupuestosCount: Int,
    hasNewBudgets: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, statusColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Text(
                        text = categoriaNombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    if (hasNewBudgets) {
                        Spacer(Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("NUEVO", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = licitacionNombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = "Fechas", modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "$fechaInicio - $fechaFin", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onClick) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Mostrar Presupuestos")
                    }
                    Icon(Icons.Default.Archive, contentDescription = "Presupuestos recibidos", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "$presupuestosCount Recibidos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, statusColor)
            ) {
                Text(
                    text = status.uppercase(),
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PresupuestoGeneralCard(
    presupuesto: PresupuestoFalso,
    onClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val provider = remember { SampleDataFalso.getPrestadorById(presupuesto.prestadorId) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(presupuesto.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("De: ${presupuesto.prestadorNombre}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("S/ ${presupuesto.precioTotal}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(model = provider?.profileImageUrl),
                    contentDescription = "Logo de ${presupuesto.prestadorNombre}",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onProfileClick),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onChatClick) {
                    Icon(Icons.AutoMirrored.Filled.Message, "Chat", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No se encontraron resultados",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun LicitacionDetailSheetContent(
    budgets: List<PresupuestoFalso>,
    onProfileClick: (String) -> Unit,
    onChatClick: (String) -> Unit,
    onBudgetClick: (String?) -> Unit
) {
    var sortAscending by remember { mutableStateOf(true) }

    val sortedBudgets = if (sortAscending) {
        budgets.sortedBy { it.precioTotal }
    } else {
        budgets.sortedByDescending { it.precioTotal }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = budgets.firstOrNull()?.nombre ?: "Detalle de Licitación",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sortedBudgets) { budget ->
                PresupuestoGeneralCard(
                    presupuesto = budget,
                    onClick = { onBudgetClick(budget.imageUrl) },
                    onProfileClick = { onProfileClick(budget.prestadorId) },
                    onChatClick = { onChatClick(budget.prestadorId) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { sortAscending = !sortAscending }) {
                Text(if (sortAscending) "Menor a Mayor" else "Mayor a Menor")
                Icon(
                    if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = "Ordenar por precio",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SortFilterChip(isAscending: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(
            if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            contentDescription = "Ordenar por fecha",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Fecha")
    }
}

@Preview(showBackground = true)
@Composable
fun PresupuestosScreenPreview() {
    MaterialTheme {
        PresupuestosScreen(onBack = {})
    }
}
