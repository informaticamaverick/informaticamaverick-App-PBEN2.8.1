package com.example.myapplication.Client

import android.hardware.lights.Light
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.Description
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

    var selectedPresupuesto by remember { mutableStateOf<PresupuestoFalso?>(null) }

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
                onBudgetClick = { presupuesto -> selectedPresupuesto = presupuesto }
            )
        }
    }

    if (selectedPresupuesto != null) {
        BudgetPreviewPDFDialog(
            presupuesto = selectedPresupuesto!!,
            onDismiss = { selectedPresupuesto = null }
        )
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
                    },
                    onPresupuestoClick = { presupuesto -> selectedPresupuesto = presupuesto }
                )
                1 -> GeneralesTabContent(
                    presupuestos = presupuestosGenerales,
                    searchQuery = searchQuery,
                    onPresupuestoClick = { presupuesto -> selectedPresupuesto = presupuesto },
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
    onLicitacionClick: (List<PresupuestoFalso>) -> Unit,
    onPresupuestoClick: (PresupuestoFalso) -> Unit
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                itemsIndexed(
                    items = filteredAndSortedLicitaciones,
                    key = { _, item -> item.key }
                ) { index, (nombre, presupuestos) ->
                    val licitacionInfo = presupuestos.first()
                    val hasNew = presupuestos.any { it.isNew }
                    // Para licitaciones adjudicadas, obtener el prestador y su presupuesto
                    val presupuestoAdjudicado = if (licitacionInfo.estadoLicitacion == EstadoLicitacion.ADJUDICADA) {
                        presupuestos.firstOrNull()
                    } else null
                    
                    LicitacionArchiveroCard(
                        categoriaNombre = licitacionInfo.servicioCategoria,
                        licitacionNombre = nombre,
                        fechaInicio = licitacionInfo.fechaInicioLicitacion ?: "-",
                        fechaFin = licitacionInfo.fechaFinLicitacion ?: "-",
                        status = licitacionInfo.estadoLicitacion.displayName,
                        statusColor = licitacionInfo.estadoLicitacion.color,
                        presupuestosCount = presupuestos.size,
                        hasNewBudgets = hasNew,
                        onClick = { onLicitacionClick(presupuestos) },
                        presupuestoAdjudicado = presupuestoAdjudicado,
                        onAdjudicadoClick = { presupuestoAdjudicado?.let { onPresupuestoClick(it) } }
                    )
                    
                    // Agregar divisor gradiente entre items (no después del último)
                    if (index < filteredAndSortedLicitaciones.size - 1) {
                        GradientDivider(
                            color = Color.Black,
                            thickness = 2.dp,
                            modifier = Modifier.padding(vertical = 40.dp, horizontal = 8.dp)
                        )
                    }
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
    onPresupuestoClick: (PresupuestoFalso) -> Unit,
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
                    onClick = { onPresupuestoClick(presupuesto) },
                    onProfileClick = { onProfileClick(presupuesto.prestadorId) },
                    onChatClick = { onChatClick(presupuesto.prestadorId) },
                    onPreviewClick = { onPresupuestoClick(presupuesto) }
                )
            }
        }
    }
}



// Mapeo de estados de licitación a temas de carpeta
fun getThemeForStatus(status: EstadoLicitacion): FolderTheme {
    return when (status) {
        EstadoLicitacion.ACTIVA -> FolderTheme.Active
        EstadoLicitacion.ADJUDICADA -> FolderTheme.Adjudicated
        EstadoLicitacion.TERMINADA -> FolderTheme.Finished
        EstadoLicitacion.CANCELADA -> FolderTheme.Cancelled
    }
}

// Definición de temas según estado
sealed class FolderTheme(
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val tabColor: Color,
    val badgeBgColor: Color,
    val badgeTextColor: Color,
    val icon: ImageVector,
    val isCancelled: Boolean = false
) {
    object Active : FolderTheme(
        primaryColor = Color(0xFF047857),      // Emerald-700 (verde oscuro bordes)
        secondaryColor = Color(0xFFA7F3D0),    // Emerald-200 (verde claro fondo)
        tertiaryColor = Color(0xFFECFDF5),     // Emerald-50 (verde muy suave)
        tabColor = Color(0xFF34D399),          // Emerald-400 (verde brillante tab)
        badgeBgColor = Color(0xFFD1FAE5),      // Emerald-100
        badgeTextColor = Color(0xFF047857),    // Emerald-700
        icon = Icons.Rounded.FolderOpen
    )
    object Adjudicated : FolderTheme(
        primaryColor = Color(0xFF1D4ED8),      // Blue-700 (azul oscuro bordes)
        secondaryColor = Color(0xFFBFDBFE),    // Blue-200 (azul claro fondo)
        tertiaryColor = Color(0xFFEFF6FF),     // Blue-50 (azul muy suave)
        tabColor = Color(0xFF60A5FA),          // Blue-400 (azul brillante tab)
        badgeBgColor = Color(0xFFDBEAFE),      // Blue-100
        badgeTextColor = Color(0xFF1D4ED8),    // Blue-700
        icon = Icons.Default.WorkspacePremium
    )
    object Finished : FolderTheme(
        primaryColor = Color(0xFFDC2626),      // Red-600 (rojo oscuro bordes)
        secondaryColor = Color(0xFFFECACA),    // Red-200 (rojo claro fondo)
        tertiaryColor = Color(0xFFFEF2F2),     // Red-50 (rojo muy suave)
        tabColor = Color(0xFFF87171),          // Red-400 (rojo brillante tab)
        badgeBgColor = Color(0xFFFEE2E2),      // Red-100
        badgeTextColor = Color(0xFFB91C1C),    // Red-700
        icon = Icons.Rounded.LocalShipping
    )
    object Cancelled : FolderTheme(
        primaryColor = Color(0xFFA1A1AA),      // Gray-400
        secondaryColor = Color(0xFFE4E4E7),    // Gray-200
        tertiaryColor = Color(0xFFF4F4F5),     // Gray-100
        tabColor = Color(0xFFD4D4D8),          // Gray-300
        badgeBgColor = Color(0xFFEFEBE9),      // Brown-50
        badgeTextColor = Color(0xFF5D4037),    // Brown-700
        icon = Icons.Rounded.CleaningServices,
        isCancelled = true
    )
}

// Helper function para obtener el emoji de cada categoría
fun getCategoryEmoji(categoria: String): String {
    return when {
        categoria.contains("Informatica", ignoreCase = true) || 
        categoria.contains("Tecnología", ignoreCase = true) -> "💻"
        categoria.contains("Electricidad", ignoreCase = true) || 
        categoria.contains("Electricista", ignoreCase = true) -> "⚡"
        categoria.contains("Diseño", ignoreCase = true) -> "🎨"
        categoria.contains("Plomero", ignoreCase = true) || 
        categoria.contains("Plomería", ignoreCase = true) -> "🪠"
        categoria.contains("Pintura", ignoreCase = true) -> "🏘️"
        categoria.contains("Limpieza", ignoreCase = true) -> "🧹"
        categoria.contains("Albañil", ignoreCase = true) || 
        categoria.contains("Construcción", ignoreCase = true) -> "🔨"
        categoria.contains("Mecánico", ignoreCase = true) || 
        categoria.contains("Mecánica", ignoreCase = true) -> "🔧"
        categoria.contains("Jardín", ignoreCase = true) || 
        categoria.contains("Jardinería", ignoreCase = true) -> "🌿"
        categoria.contains("Mudanza", ignoreCase = true) -> "🚚"
        categoria.contains("Carpintería", ignoreCase = true) -> "🪚"
        categoria.contains("Fotografía", ignoreCase = true) -> "📷"
        categoria.contains("Eventos", ignoreCase = true) -> "🎉"
        else -> "📋"
    }
}


// Funcion para calcular el tiempo restante hasta el cierre de la licitacion
fun calcularTiempoRestante(fechaFin: String): String {
    return try {
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val fechaFinDate = formatter.parse(fechaFin)
        val fechaActual = java.util.Date()

        if (fechaFinDate != null) {
            val diferenciaMilis = fechaFinDate.time - fechaActual.time
            val diasRestantes = (diferenciaMilis / (1000 * 60 * 60 * 24)).toInt()

            when {
                diasRestantes < 0 -> "Cerrada"
                diasRestantes == 0 -> "Cierra hoy"
                diasRestantes == 1 -> "Cierra mañana"
                diasRestantes <= 7 -> "Cierra en $diasRestantes dias"
                diasRestantes <= 30 -> "Cierra en ${diasRestantes / 7} semana${if (diasRestantes / 7 > 1) "s" else ""}"
                else -> "Cierra en ${diasRestantes / 30} mes${if (diasRestantes / 30 > 1) "es" else ""}"
            }
        } else {
            "Fecha invalida"
        }
    } catch (e: Exception) {
        "Error"
    }
}



// Componente de línea divisoria con gradiente
@Composable
fun GradientDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    thickness: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.5f), // Más visible en el centro
                        Color.Transparent
                    )
                )
            )
    )
}

// Componente de punto pulsante animado (estilo Tailwind animate-ping)
@Composable
fun PulsingDotIndicator(
    modifier: Modifier = Modifier,
    dotSize: androidx.compose.ui.unit.Dp = 8.dp,
    color: Color = Color(0xFFE11D48) // Rose-500/600
) {
    // 1. Configurar la transición infinita
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingDotAnimation")

    // 2. Animación de ESCALA: Crece desde 1x hasta 2.5x su tamaño
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scaleAnimation"
    )

    // 3. Animación de OPACIDAD: Se desvanece de 0.7 a 0
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alphaAnimation"
    )

    // Contenedor principal para apilar los puntos
    Box(
        modifier = modifier.size(dotSize * 2),
        contentAlignment = Alignment.Center
    ) {
        // CAPA TRASERA: El círculo que se expande y desvanece
        Box(
            modifier = Modifier
                .size(dotSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .background(color, CircleShape)
        )

        // CAPA DELANTERA: El punto estático central
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(color, CircleShape)
        )
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
    onClick: () -> Unit,
    presupuestoAdjudicado: PresupuestoFalso? = null,
    onAdjudicadoClick: () -> Unit = {}
) {
    // Estado para controlar si la tarjeta está expandida
    var isExpanded by remember { mutableStateOf(false) }
    
    // Mapear el status a FolderTheme
    val theme = when {
        status.contains("ACTIVA", ignoreCase = true) -> FolderTheme.Active
        status.contains("ADJUDICADA", ignoreCase = true) -> FolderTheme.Adjudicated
        status.contains("TERMINADA", ignoreCase = true) -> FolderTheme.Finished
        status.contains("CANCELADA", ignoreCase = true) -> FolderTheme.Cancelled
        else -> FolderTheme.Active
    }
    
    val alpha = if (theme.isCancelled) 0.8f else 1f
    val dateRange = "$fechaInicio - $fechaFin"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded }
            .graphicsLayer { this.alpha = alpha }
    ) {
        
        // CAPA 1: Hoja trasera
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
            shadowElevation = 2.dp
        ) {}

        // CAPA 2: Pestaña superior
        Box(
            modifier = Modifier
                .offset(y = (-32).dp, x = 0.dp)
                .height(40.dp)
                .wrapContentWidth()
                .background(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .border(
                    width = 2.dp,
                    color = theme.primaryColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = getCategoryEmoji(categoriaNombre),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = categoriaNombre.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = theme.primaryColor
                )
            }
        }

        // CAPA 3: Badge flotante de estado
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, theme.badgeTextColor),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-16).dp, x = (-16).dp)
                .zIndex(2f)
                .shadow(4.dp, shape = RoundedCornerShape(8.dp))
        ) {
            Text(
                text = status.uppercase(),
                color = theme.badgeTextColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                letterSpacing = 0.5.sp
            )
        }

        // CAPA 4: Cuerpo principal
        val folderShape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 24.dp,
            bottomEnd = 24.dp,
            bottomStart = 24.dp
        )

        Surface(
            shape = folderShape,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, theme.primaryColor),
            shadowElevation = 8.dp,
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(theme.secondaryColor, theme.tertiaryColor)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    // Header
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Label "NOMBRE DEL PROYECTO:"
                            Text(
                                text = "NOMBRE DEL PROYECTO:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Nombre de la licitación
                            Text(
                                text = licitacionNombre,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (theme.isCancelled) theme.primaryColor else Color(0xFF451A03),
                                lineHeight = 22.sp,
                                textDecoration = if (theme.isCancelled) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                        
                        // Indicador de expandir/colapsar - Lado derecho
                        Column(horizontalAlignment = Alignment.End) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { isExpanded = !isExpanded }
                            ) {
                                Text(
                                    text = if (isExpanded) "Ver menos" else "Ver más detalles",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = theme.primaryColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            // Indicador de nuevos ingresos cuando está contraída
                            if (!isExpanded && hasNewBudgets && !theme.isCancelled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PulsingDotIndicator(
                                        dotSize = 6.dp,
                                        color = Color(0xFFE11D48) // Rose-500
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Nuevos Ingresos",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBE123C)
                                    )
                                }
                            }
                        }
                    }

                    // Contenido expandible - Fechas
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Fechas de inicio y fin en formato horizontal
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                // FECHA DE INICIO
                                Column {
                                    Text(
                                        text = "FECHA DE INICIO:",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black.copy(alpha = 0.6f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Event,
                                            contentDescription = null,
                                            tint = if (theme.isCancelled) theme.primaryColor else theme.primaryColor.copy(alpha = 0.8f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = fechaInicio,
                                            fontSize = 12.sp,
                                            color = if (theme.isCancelled) theme.primaryColor else Color(0xFF713F12),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(24.dp))
                                
                                // FECHA DE FIN
                                Column {
                                    Text(
                                        text = "FECHA DE FIN:",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black.copy(alpha = 0.6f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (theme.isCancelled) Icons.Rounded.EventBusy else Icons.Rounded.Event,
                                            contentDescription = null,
                                            tint = if (theme.isCancelled) theme.primaryColor else theme.primaryColor.copy(alpha = 0.8f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = fechaFin,
                                            fontSize = 12.sp,
                                            color = if (theme.isCancelled) theme.primaryColor else Color(0xFF713F12),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Divider y Footer dentro del AnimatedVisibility
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            // Divider
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(theme.primaryColor.copy(alpha = 0.5f), Color.Transparent)
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Columna izquierda - Tiempo restante o Adjudicado
                                Column {
                            when {
                                // Licitación ACTIVA - Mostrar tiempo restante
                                !theme.isCancelled && status.contains("ACTIVA", ignoreCase = true) -> {
                                    Text(
                                        text = "TIEMPO RESTANTE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.badgeTextColor,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    
                                    Surface(
                                        color = Color.White.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Timer,
                                                contentDescription = null,
                                                tint = theme.badgeTextColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Cierra en 8 días",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.badgeTextColor
                                            )
                                        }
                                    }
                                }
                                // Licitación ADJUDICADA - Mostrar adjudicatario
                                status.contains("ADJUDICADA", ignoreCase = true) -> {
                                    Text(
                                        text = "ADJUDICADO A",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.badgeTextColor,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    
                                    Surface(
                                        color = Color.White.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.2f)),
                                        modifier = Modifier.clickable { 
                                            if (presupuestoAdjudicado != null) onAdjudicadoClick() 
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WorkspacePremium,
                                                contentDescription = null,
                                                tint = theme.badgeTextColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = presupuestoAdjudicado?.prestadorNombre ?: "Por definir",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.badgeTextColor,
                                                maxLines = 1
                                            )
                                            if (presupuestoAdjudicado != null) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Rounded.Visibility,
                                                    contentDescription = "Ver presupuesto",
                                                    tint = theme.badgeTextColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                // Para otros estados (Terminada, Cancelada) no mostrar nada
                                else -> {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }
                            }
                        }
                        
                                // Columna derecha - Contador de archivos
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.5f)),
                                        shadowElevation = 1.dp,
                                        modifier = Modifier.clickable { onClick() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$presupuestosCount Archivo${if (presupuestosCount != 1) "s" else ""}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF451A03)
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowForwardIos,
                                                contentDescription = null,
                                                modifier = Modifier.size(10.dp).padding(start = 4.dp),
                                                tint = theme.primaryColor
                                            )
                                        }
                                    }
                                    
                                    // Indicador de nuevos ingresos
                                    if (hasNewBudgets && !theme.isCancelled) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // COMPONENTE ANIMADO
                                            PulsingDotIndicator(
                                                dotSize = 6.dp,
                                                color = Color(0xFFE11D48) // Rose-500
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Nuevos Ingresos",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFBE123C)
                                            )
                                        }
                                    }
                                }
                            } // Cierre Row Footer
                        } // Cierre Column del AnimatedVisibility
                    } // Cierre AnimatedVisibility
                }
            }
        }
    }
}

@Composable
fun PresupuestoGeneralCard(
    presupuesto: PresupuestoFalso,
    onClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
    onPreviewClick: () -> Unit   // nuevo parametro
) {
    val provider = remember { SampleDataFalso.getPrestadorById(presupuesto.prestadorId) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(model = provider?.profileImageUrl),
                    contentDescription = "Logo de ${presupuesto.prestadorNombre}",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onProfileClick),
                    contentScale = ContentScale.Crop
                )
                // Indicador de conectado
                if (provider?.isOnline == true) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFF10B981), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(presupuesto.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("De: ${presupuesto.prestadorNombre}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (provider?.isVerified == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Verified,
                            "Perfil Verificado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "S/ ${presupuesto.precioTotal}", 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(x = (-18).dp),
                        verticalArrangement = Arrangement.spacedBy((-12).dp)
                    ) {
                        IconButton(onClick = onPreviewClick) {
                            Icon(Icons.Default.Description, "Vista previa",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "Enviado: ${formatDateShort(presupuesto.fechaRecepcion)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                    IconButton(onClick = onChatClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar mensaje",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
    onBudgetClick: (PresupuestoFalso) -> Unit
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
                    onClick = { onBudgetClick(budget) },
                    onProfileClick = { onProfileClick(budget.prestadorId) },
                    onChatClick = { onChatClick(budget.prestadorId) },
                    onPreviewClick = { onBudgetClick(budget) }
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

//@OptIn(ExperimentalMaterial3Api::class)
/*
@Composable
fun BudgetPreviewPDFDialog(
    presupuesto: PresupuestoFalso,
    onDismiss: () -> Unit
) {
    val provider = remember { SampleDataFalso.getPrestadorById(presupuesto.prestadorId) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Vista Previa del Presupuesto") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1976D2),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Header - Información del prestador
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            provider?.let {
                                Image(
                                    painter = rememberAsyncImagePainter(model = it.profileImageUrl),
                                    contentDescription = "Logo del prestador",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color(0xFF1976D2), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "PRESTADOR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    presupuesto.prestadorNombre,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                provider?.let {
                                    Text(
                                        it.email,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    // Información del presupuesto
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "INFORMACIÓN DEL PRESUPUESTO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            InfoRow("Proyecto:", presupuesto.nombre)
                            Spacer(modifier = Modifier.height(8.dp))
                            InfoRow("Categoría:", presupuesto.servicioCategoria)
                            Spacer(modifier = Modifier.height(8.dp))
                            InfoRow("Fecha de Recepción:", presupuesto.fechaRecepcion)
                            
                            if (presupuesto.esLicitacion) {
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Estado:", presupuesto.estadoLicitacion.displayName)
                                presupuesto.fechaInicioLicitacion?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    InfoRow("Fecha de Inicio:", it)
                                }
                                presupuesto.fechaFinLicitacion?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    InfoRow("Fecha de Fin:", it)
                                }
                            }
                        }
                    }
                }
                
                item {
                    // Desglose de costos (simulado)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "DESGLOSE DE COSTOS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Simulación de items (datos de ejemplo)
                            CostItemRow("Materiales", presupuesto.precioTotal * 0.6)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            CostItemRow("Mano de obra", presupuesto.precioTotal * 0.3)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            CostItemRow("Gastos varios", presupuesto.precioTotal * 0.1)
                        }
                    }
                }
                
                item {
                    // Total
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "TOTAL",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "S/ ${String.format("%.2f", presupuesto.precioTotal)}",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            if (presupuesto.isNew) {
                                Surface(
                                    color = Color(0xFFEF4444),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        "NUEVO",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    // Nota al pie
                    Text(
                        "Este presupuesto es válido por 30 días desde su emisión.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
*/

// --- NUEVA VISTA PREVIA DE PRESUPUESTO ---

// Colores Personalizados (Basados en Tailwind Slate)
private val Slate50 = Color(0xFFF8FAFC)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate600 = Color(0xFF475569)
private val Slate700 = Color(0xFF334155)
private val Slate800 = Color(0xFF1E293B)
private val MaverickBlueStart = Color(0xFF2563EB)
private val MaverickBlueEnd = Color(0xFF1E40AF)

private val MaverickGradient = Brush.linearGradient(
    colors = listOf(MaverickBlueStart, MaverickBlueEnd)
)

data class PresupuestoItemDisplay(
    val cantidad: String,
    val descripcion: String,
    val unitario: String,
    val total: String,
    val isHeader: Boolean = false,
    val isSpecial: Boolean = false
)

@Composable
fun BudgetPreviewPDFDialog(
    presupuesto: PresupuestoFalso,
    onDismiss: () -> Unit
) {
    val provider = remember { SampleDataFalso.getPrestadorById(presupuesto.prestadorId) }
    
    // Datos de ejemplo - TEMPORAL
    val items = listOf(
        PresupuestoItemDisplay("1", "Fuente 12v", "$ 18.000,00", "$ 18.000,00"),
        PresupuestoItemDisplay("1", "Balun TVI", "$ 3.000,00", "$ 3.000,00"),
        PresupuestoItemDisplay("1", "Ficha dc", "$ 480,00", "$ 480,00"),
        PresupuestoItemDisplay("-", "Mano de obra Instalación Cableada", "$ 130.000,00", "$ 130.000,00", isSpecial = true),
        PresupuestoItemDisplay("-", "Movilidad", "$ 45.000,00", "$ 45.000,00")
    )
    
    // Estado de zoom
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate200) // Fondo gris claro
        ) {
            // Contenedor con zoom
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val screenWidth = maxWidth
                val a4Width = 800.dp // Ancho máximo del documento
                val initialScale = (screenWidth / a4Width).coerceAtMost(1f)
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 3f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                            }
                        },
                    contentAlignment = Alignment.TopCenter
                ) {
                    // La hoja del presupuesto
                    Card(
                        modifier = Modifier
                            .widthIn(max = a4Width)
                            .fillMaxWidth()
                            .graphicsLayer(
                                scaleX = initialScale * scale,
                                scaleY = initialScale * scale,
                                translationX = offsetX,
                                translationY = offsetY,
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                            ),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Franja decorativa superior
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(MaverickGradient)
                            )
                            
                            // HEADER
                            NewBudgetHeaderSection(provider, presupuesto)
                            
                            HorizontalDivider(color = Slate200)
                            
                            // INFO EMISOR & CLIENTE
                            NewBudgetInfoSection(provider, presupuesto)
                            
                            // TABLA CON BORDES
                            NewBudgetItemsTableSection(items)
                            
                            // FOOTER & TOTALES
                            NewBudgetFooterSection(presupuesto.precioTotal)
                        }
                    }
                }
            }
            
            // Botón cerrar flotante
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .size(48.dp)
            ) {
                Icon(Icons.Default.Close, "Cerrar", tint = Slate800)
            }
            
            // Controles de zoom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Slate800.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { scale = (scale * 0.8f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Remove, "Alejar", tint = Color.White)
                }
                
                Text(
                    text = "${(scale * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 50.dp),
                    textAlign = TextAlign.Center
                )
                
                IconButton(
                    onClick = { scale = (scale * 1.25f).coerceAtMost(3f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, "Acercar", tint = Color.White)
                }
                
                IconButton(
                    onClick = { 
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, "Resetear", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun NewBudgetHeaderSection(provider: PrestadorProfileFalso?, presupuesto: PresupuestoFalso) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .border(0.dp, Color.Transparent, RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Izquierda: Logo y Empresa
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaverickGradient, RoundedCornerShape(4.dp))
                    .shadow(4.dp, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                provider?.let {
                    Text(
                        text = "${it.name} ${it.lastName}".uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Slate800,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = it.services.firstOrNull() ?: "Informática",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Slate500,
                        letterSpacing = 2.sp,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                } ?: run {
                    Text(
                        text = "MAVERICK",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Slate800,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Informática",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Slate500,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // Centro: La "X"
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(2.dp, Slate800, RoundedCornerShape(8.dp))
                .background(Color.White)
                .shadow(2.dp, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "X",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Slate800,
                lineHeight = 40.sp,
                modifier = Modifier.offset(y = (-4).dp)
            )
        }

        // Derecha: Datos del Documento
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "PRESUPUESTO",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Slate700,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .border(1.dp, Slate300, RoundedCornerShape(4.dp))
                    .background(Slate50, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "N° ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.sp
                )
                Text(
                    text = presupuesto.id.takeLast(8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate800,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fecha: ${presupuesto.fechaRecepcion}",
                fontSize = 14.sp,
                color = Slate500,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NewBudgetInfoSection(provider: PrestadorProfileFalso?, presupuesto: PresupuestoFalso) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        // Columna Izquierda - Emisor
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "DE:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate800,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .border(0.dp, Color.Transparent, RoundedCornerShape(0.dp))
                    .drawBehind {
                        drawLine(
                            color = androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                            start = Offset(0f, size.height),
                            end = Offset(100f, size.height),
                            strokeWidth = 2f
                        )
                    }
                    .padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            provider?.let {
                Text(it.address, fontSize = 14.sp, color = Slate600, lineHeight = 20.sp)
                Text("${it.companyName ?: "3815394738"} (Wsp)", fontSize = 14.sp, color = Slate600, lineHeight = 20.sp)
                Text(it.email, fontSize = 14.sp, color = Slate600, lineHeight = 20.sp)
            } ?: run {
                Text("B. Matienzo 1339", fontSize = 14.sp, color = Slate600, lineHeight = 20.sp)
                Text("3815394738 (Wsp)", fontSize = 14.sp, color = Slate600, lineHeight = 20.sp)
                Text("informaticamaverick@gmail.com", fontSize = 14.sp, color = Slate600, lineHeight = 20.sp)
            }
        }

        // Columna Derecha - Cliente
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "PARA:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate800,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .drawBehind {
                        drawLine(
                            color = androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                            start = Offset(0f, size.height),
                            end = Offset(100f, size.height),
                            strokeWidth = 2f
                        )
                    }
                    .padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Campo Cliente / Empresa
            Column {
                Text(
                    "CLIENTE / EMPRESA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 2f
                            )
                        }
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        presupuesto.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Slate800
                    )
                    Text(
                        "| ${provider?.companyName ?: "Empresa"}",
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Slate500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Campo Dirección
            Column {
                Text(
                    "DIRECCIÓN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "A definir",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700,
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 2f
                            )
                        }
                        .padding(bottom = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Campos Forma de Pago y Condición
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "FORMA DE PAGO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "Efectivo",
                        fontSize = 14.sp,
                        color = Slate700,
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 2f
                                )
                            }
                            .padding(bottom = 4.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "CONDICIÓN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "Contado",
                        fontSize = 14.sp,
                        color = Slate700,
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 2f
                                )
                            }
                            .padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NewBudgetItemsTableSection(items: List<PresupuestoItemDisplay>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(bottom = 32.dp)
    ) {
        // Cabecera de la tabla
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate100)
                .border(1.dp, Slate300)
        ) {
            Text(
                "CANT.",
                modifier = Modifier
                    .weight(0.1f)
                    .border(1.dp, Slate300)
                    .padding(8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate600,
                textAlign = TextAlign.Center
            )
            Text(
                "DESCRIPCIÓN / DETALLE",
                modifier = Modifier
                    .weight(0.5f)
                    .border(1.dp, Slate300)
                    .padding(8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate600
            )
            Text(
                "UNITARIO",
                modifier = Modifier
                    .weight(0.2f)
                    .border(1.dp, Slate300)
                    .padding(8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate600,
                textAlign = TextAlign.End
            )
            Text(
                "TOTAL",
                modifier = Modifier
                    .weight(0.2f)
                    .border(1.dp, Slate300)
                    .padding(8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate600,
                textAlign = TextAlign.End
            )
        }

        // Filas de items
        items.forEach { item ->
            val bgColor = if (item.isSpecial) Color(0xFFEFF6FF) else Color.White
            val textColor = if (item.isSpecial) Color(0xFF1E3A8A) else Slate700

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
            ) {
                Text(
                    item.cantidad,
                    modifier = Modifier
                        .weight(0.1f)
                        .border(1.dp, Slate300)
                        .padding(8.dp),
                    fontSize = 14.sp,
                    color = if (item.cantidad == "-") Slate400 else Slate700,
                    textAlign = TextAlign.Center
                )
                Text(
                    item.descripcion,
                    modifier = Modifier
                        .weight(0.5f)
                        .border(1.dp, Slate300)
                        .padding(8.dp),
                    fontSize = 14.sp,
                    fontWeight = if (item.isSpecial) FontWeight.Medium else FontWeight.Normal,
                    color = textColor
                )
                Text(
                    item.unitario,
                    modifier = Modifier
                        .weight(0.2f)
                        .border(1.dp, Slate300)
                        .padding(8.dp),
                    fontSize = 14.sp,
                    color = Slate700,
                    textAlign = TextAlign.End,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    item.total,
                    modifier = Modifier
                        .weight(0.2f)
                        .border(1.dp, Slate300)
                        .padding(8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700,
                    textAlign = TextAlign.End,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        // Filas vacías (relleno de la grilla)
        repeat(2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(Color.White)
            ) {
                Box(modifier = Modifier.weight(0.1f).fillMaxHeight().border(1.dp, Slate300))
                Box(modifier = Modifier.weight(0.5f).fillMaxHeight().border(1.dp, Slate300))
                Box(modifier = Modifier.weight(0.2f).fillMaxHeight().border(1.dp, Slate300))
                Box(modifier = Modifier.weight(0.2f).fillMaxHeight().border(1.dp, Slate300))
            }
        }
    }
}

@Composable
fun NewBudgetFooterSection(total: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate50)
            .border(1.dp, Slate200, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .padding(32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Nota a la izquierda
            Text(
                text = "Nota: Los precios están expresados en Pesos Argentinos. Válido por 15 días.",
                fontSize = 12.sp,
                color = Slate400,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 16.sp,
                modifier = Modifier.weight(0.5f)
            )

            Spacer(modifier = Modifier.width(32.dp))

            // Bloque de Totales con Borde
            Card(
                modifier = Modifier.weight(0.5f).widthIn(min = 280.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate300),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Subtotal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Subtotal:", fontSize = 14.sp, color = Slate600)
                        Text(
                            "$ ${String.format("%,d", total.toInt())}",
                            fontSize = 14.sp,
                            color = Slate600,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    
                    // Descuento
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(0.dp, Color.Transparent, RoundedCornerShape(0.dp)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Descuento:", fontSize = 14.sp, color = Slate600)
                        Text(
                            "-",
                            fontSize = 14.sp,
                            color = Slate600,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    
                    HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TOTAL",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Text(
                            "$ ${String.format("%,d", total.toInt())}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2563EB), // Blue-700
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { /* Descargar PDF */ },
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Slate300),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Slate600
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    "Descargar PDF",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Button(
                onClick = { /* Enviar */ },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaverickBlueStart
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    "Enviar Presupuesto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Función para formatear fecha de dd/MM/yyyy a dd/MM/yy
fun formatDateShort(date: String): String {
    return try {
        if (date.length == 10 && date.contains("/")) {
            val parts = date.split("/")
            "${parts[0]}/${parts[1]}/${parts[2].takeLast(2)}"
        } else {
            date
        }
    } catch (e: Exception) {
        date
    }
}

@Preview(showBackground = true)
@Composable
fun PresupuestosScreenPreview() {
    MaterialTheme {
        PresupuestosScreen(onBack = {})
    }
}
