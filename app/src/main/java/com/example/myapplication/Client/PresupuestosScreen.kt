package com.example.myapplication.Client

import android.hardware.lights.Light
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.Description
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle

// =================================================================================
// --- SECCIÓN: DIALOGS DE FILTROS ---
// =================================================================================

@Composable
fun CategoryFilterDialog(
    allCategories: List<String>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(2.dp, geminiGradientEffect())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Filtrar por Categoría",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(allCategories) { category ->
                        val isSelected = selectedCategories.contains(category)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategoryToggle(category) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onCategoryToggle(category) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category, color = colors.onSurface)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            onClear()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Limpiar")
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusFilterDialog(
    selectedStatus: EstadoLicitacion?,
    onStatusSelected: (EstadoLicitacion?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        val colors = MaterialTheme.colorScheme
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(2.dp, geminiGradientEffect())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Filtrar por Estado",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Opción "Todos"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStatusSelected(null) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedStatus == null,
                        onClick = { onStatusSelected(null) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Todos", color = colors.onSurface)
                }
                
                // Opciones de estado
                EstadoLicitacion.values().forEach { estado ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatusSelected(estado) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStatus == estado,
                            onClick = { onStatusSelected(estado) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(estado.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(estado.displayName, color = colors.onSurface)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aplicar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreen(
    onProfileClick: (String) -> Unit = {},
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isSystemInDarkMode = isSystemInDarkTheme()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Presupuestos de Licitaciones", "Presupuestos Generales")
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Estado cíclico del menú: 0 = Filtros, 1 = Menú config, 2 = Todo oculto
    var menuState by remember { mutableIntStateOf(0) }
    val showSettingsMenu = menuState == 0
    val showVerticalMenu = menuState == 1
    
    // Estados de filtros
    var selectedStatus by remember { mutableStateOf<EstadoLicitacion?>(null) }
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var sortAscending by remember { mutableStateOf(true) }
    
    // Estados para mostrar menús de filtros
    var showCategoryFilter by remember { mutableStateOf(false) }
    var showStatusFilter by remember { mutableStateOf(false) }
    
    // Estados del menú de configuración
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showDataVisibilityDialog by remember { mutableStateOf(false) }
    var showTimePeriodDialog by remember { mutableStateOf(false) }
    
    // Preferencias de usuario
    var viewMode by remember { mutableStateOf("Detallada") } // "Compacta", "Detallada", "Tarjetas"
    var timePeriod by remember { mutableStateOf("Todo") } // "Semana", "Mes", "3 Meses", "Todo"
    var showDates by remember { mutableStateOf(true) }
    var showPrices by remember { mutableStateOf(true) }
    var showOfferCount by remember { mutableStateOf(true) }
    var showBadges by remember { mutableStateOf(true) }
    var notifyNewQuotes by remember { mutableStateOf(true) }
    var notifyStatusChanges by remember { mutableStateOf(true) }
    var notifyDeadlines by remember { mutableStateOf(true) }

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
    
    // Diálogos de filtros
    val allCategories = licitacionesAgrupadas.values.flatten().map { it.servicioCategoria }.distinct()
    
    if (showCategoryFilter) {
        CategoryFilterDialog(
            allCategories = allCategories,
            selectedCategories = selectedCategories,
            onCategoryToggle = { category ->
                selectedCategories = if (selectedCategories.contains(category)) {
                    selectedCategories - category
                } else {
                    selectedCategories + category
                }
            },
            onClear = { selectedCategories = emptySet() },
            onDismiss = { showCategoryFilter = false }
        )
    }
    
    if (showStatusFilter) {
        StatusFilterDialog(
            selectedStatus = selectedStatus,
            onStatusSelected = { 
                selectedStatus = it
                showStatusFilter = false
            },
            onDismiss = { showStatusFilter = false }
        )
    }
    
    // Diálogo: Período de Tiempo
    if (showTimePeriodDialog) {
        Dialog(onDismissRequest = { showTimePeriodDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Período de Tiempo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    listOf(
                        "Semana" to "Última semana",
                        "Mes" to "Último mes",
                        "3 Meses" to "Últimos 3 meses",
                        "Todo" to "Todos los períodos"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { timePeriod = value }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = timePeriod == value,
                                onClick = { timePeriod = value }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = colors.onSurface)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showTimePeriodDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
    
    // Diálogo: Mostrar/Ocultar Datos
    if (showDataVisibilityDialog) {
        Dialog(onDismissRequest = { showDataVisibilityDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Mostrar u Ocultar Datos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDates = !showDates }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mostrar fechas", color = colors.onSurface)
                        Switch(checked = showDates, onCheckedChange = { showDates = it })
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrices = !showPrices }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mostrar precios", color = colors.onSurface)
                        Switch(checked = showPrices, onCheckedChange = { showPrices = it })
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOfferCount = !showOfferCount }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mostrar cantidad de ofertas", color = colors.onSurface)
                        Switch(checked = showOfferCount, onCheckedChange = { showOfferCount = it })
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBadges = !showBadges }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mostrar insignias", color = colors.onSurface)
                        Switch(checked = showBadges, onCheckedChange = { showBadges = it })
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showDataVisibilityDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
    
    // Diálogo: Alertas y Notificaciones
    if (showNotificationsDialog) {
        Dialog(onDismissRequest = { showNotificationsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Alertas y Notificaciones",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notifyNewQuotes = !notifyNewQuotes }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nuevos presupuestos", color = colors.onSurface)
                        Switch(checked = notifyNewQuotes, onCheckedChange = { notifyNewQuotes = it })
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notifyStatusChanges = !notifyStatusChanges }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cambios de estado", color = colors.onSurface)
                        Switch(checked = notifyStatusChanges, onCheckedChange = { notifyStatusChanges = it })
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notifyDeadlines = !notifyDeadlines }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Próximas fechas límite", color = colors.onSurface)
                        Switch(checked = notifyDeadlines, onCheckedChange = { notifyDeadlines = it })
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showNotificationsDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Presupuestos") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
        floatingActionButton = {
            val rainbowBrush = geminiGradientEffect()
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 70.dp)
            ) {
                // Menú vertical de configuración (aparece arriba del engranaje)
                AnimatedVisibility(
                    visible = showVerticalMenu && !isSearchActive,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Opción: Modo de Vista
                        Surface(
                            modifier = Modifier.size(64.dp),
                            onClick = { 
                                viewMode = when(viewMode) {
                                    "Compacta" -> "Detallada"
                                    "Detallada" -> "Tarjetas"
                                    else -> "Compacta"
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surface,
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.ViewModule,
                                    "Modo Vista",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when(viewMode) {
                                        "Compacta" -> "Comp"
                                        "Detallada" -> "Det"
                                        "Tarjetas" -> "Card"
                                        else -> "Vista"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        // Opción: Alertas
                        Surface(
                            modifier = Modifier.size(64.dp),
                            onClick = { 
                                showNotificationsDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surface,
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    "Alertas",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Alertas",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        // Opción: Mostrar Datos
                        Surface(
                            modifier = Modifier.size(64.dp),
                            onClick = { 
                                showDataVisibilityDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surface,
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    "Mostrar Datos",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Datos",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        // Opción: Período
                        Surface(
                            modifier = Modifier.size(64.dp),
                            onClick = { 
                                showTimePeriodDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surface,
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    "Período",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Período",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            
                // Botones de búsqueda y engranaje (separados)
                AnimatedVisibility(
                    visible = !isSearchActive,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    val gearRotation by animateFloatAsState(
                        targetValue = if (menuState == 2) 0f else 45f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    // Botones de filtros expandibles (aparecen a la izquierda)
                    AnimatedVisibility(
                        visible = showSettingsMenu && !isSearchActive,
                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 32.dp, end = 8.dp)
                        ) {
                            // Botón Filtrar por Categoría
                            Surface(
                                modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                onClick = { 
                                    showCategoryFilter = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedCategories.isNotEmpty()) colors.primaryContainer else colors.surface,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Category, 
                                        "Categoría", 
                                        tint = if (selectedCategories.isNotEmpty()) colors.onPrimaryContainer else colors.onSurface, 
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (selectedCategories.isNotEmpty()) "Activo" else "Categoría",
                                        color = if (selectedCategories.isNotEmpty()) colors.onPrimaryContainer else colors.onSurface,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            // Botón Filtrar por Estado
                            Surface(
                                modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                onClick = { 
                                    showStatusFilter = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedStatus != null) colors.primaryContainer else colors.surface,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.FilterList, 
                                        "Estado", 
                                        tint = if (selectedStatus != null) colors.onPrimaryContainer else colors.onSurface, 
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (selectedStatus != null) "Activo" else "Estado",
                                        color = if (selectedStatus != null) colors.onPrimaryContainer else colors.onSurface,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            // Botón Ordenar
                            Surface(
                                modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                onClick = { 
                                    sortAscending = !sortAscending
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surface,
                                shadowElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        "Ordenar",
                                        tint = colors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (sortAscending) "A-Z" else "Z-A",
                                        color = colors.onSurface,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // El Botón Dividido (Buscar + Engranaje)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Parte Izquierda: Buscar
                        Surface(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                            color = colors.surface,
                            border = BorderStroke(2.5.dp, rainbowBrush),
                            shadowElevation = 12.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Search, null, tint = colors.onSurface, modifier = Modifier.size(26.dp))
                            }
                        }
                        
                        // Parte Derecha: Ajustes / Cerrar
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .combinedClickable(
                                    onClick = { 
                                        menuState = (menuState + 1) % 3
                                    },
                                    onLongClick = { }
                                ),
                            shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 28.dp, bottomEnd = 28.dp),
                            color = colors.surface,
                            border = BorderStroke(2.5.dp, rainbowBrush),
                            shadowElevation = 12.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Settings,
                                    "Ajustes",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(26.dp).rotate(gearRotation)
                                )
                            }
                        }
                    }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        // Contenido principal (deja espacio abajo para los FABs)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Contenido de los tabs
            when (selectedTabIndex) {
                0 -> LicitacionesTabContent(
                    licitaciones = licitacionesAgrupadas,
                    searchQuery = searchQuery,
                    selectedStatus = selectedStatus,
                    selectedCategories = selectedCategories,
                    sortAscending = sortAscending,
                    viewMode = viewMode,
                    timePeriod = timePeriod,
                    showDates = showDates,
                    showPrices = showPrices,
                    showOfferCount = showOfferCount,
                    showBadges = showBadges,
                    onLicitacionClick = { budgets ->
                        selectedLicitacionBudgets = budgets
                        showBudgetSheet = true
                    },
                    onPresupuestoClick = { presupuesto -> selectedPresupuesto = presupuesto }
                )
                1 -> GeneralesTabContent(
                    presupuestos = presupuestosGenerales,
                    searchQuery = searchQuery,
                    viewMode = viewMode,
                    timePeriod = timePeriod,
                    showDates = showDates,
                    showPrices = showPrices,
                    showOfferCount = showOfferCount,
                    showBadges = showBadges,
                    onPresupuestoClick = { presupuesto -> selectedPresupuesto = presupuesto },
                    onProfileClick = onProfileClick,
                    onChatClick = onChatClick
                )
            }
        }
        } // Cierre del Box interno del Scaffold
    } // Cierre del Scaffold
        
    // Barra de búsqueda flotante que tapa el TopAppBar
    if (isSearchActive) {
        val rainbowBrush = geminiGradientEffect()
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp)
                .zIndex(10f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                    shadowElevation = 12.dp,
                    border = BorderStroke(2.5.dp, rainbowBrush)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = colors.onSurface.copy(0.8f),
                            modifier = Modifier.padding(start = 24.dp).size(20.dp)
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(color = colors.onSurface, fontSize = 17.sp),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Buscar presupuestos...", color = colors.onSurfaceVariant, fontSize = 16.sp)
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.size(56.dp),
                onClick = {
                    isSearchActive = false
                    searchQuery = ""
                    keyboardController?.hide()
                },
                shape = CircleShape,
                color = colors.surface,
                border = BorderStroke(2.5.dp, rainbowBrush),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, "Cerrar", tint = colors.onSurface, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
    } // Cierre del Box externo
}

@Composable
fun LicitacionesTabContent(
    licitaciones: Map<String, List<PresupuestoFalso>>,
    searchQuery: String,
    selectedStatus: EstadoLicitacion?,
    selectedCategories: Set<String>,
    sortAscending: Boolean,
    viewMode: String,
    timePeriod: String,
    showDates: Boolean,
    showPrices: Boolean,
    showOfferCount: Boolean,
    showBadges: Boolean,
    onLicitacionClick: (List<PresupuestoFalso>) -> Unit,
    onPresupuestoClick: (PresupuestoFalso) -> Unit
) {
    val allCategories = licitaciones.values.flatten().map { it.servicioCategoria }.distinct()
    
    // Filtro por período de tiempo basado en fechaRecepcion
    val periodMatch: (PresupuestoFalso) -> Boolean = { presupuesto ->
        when (timePeriod) {
            "Semana" -> {
                try {
                    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val fecha = format.parse(presupuesto.fechaRecepcion)
                    val weekAgo = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, -7)
                    }
                    fecha != null && fecha.after(weekAgo.time)
                } catch (e: Exception) {
                    true
                }
            }
            "Mes" -> {
                try {
                    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val fecha = format.parse(presupuesto.fechaRecepcion)
                    val monthAgo = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.MONTH, -1)
                    }
                    fecha != null && fecha.after(monthAgo.time)
                } catch (e: Exception) {
                    true
                }
            }
            "3 Meses" -> {
                try {
                    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val fecha = format.parse(presupuesto.fechaRecepcion)
                    val threeMonthsAgo = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.MONTH, -3)
                    }
                    fecha != null && fecha.after(threeMonthsAgo.time)
                } catch (e: Exception) {
                    true
                }
            }
            else -> true // "Todo" - mostrar todos
        }
    }

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
        val periodFilterMatch = presupuestos.any { periodMatch(it) }
        statusMatch && categoryMatch && searchMatch && periodFilterMatch
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
        if (filteredAndSortedLicitaciones.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
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
                        viewMode = viewMode,
                        showDates = showDates,
                        showOfferCount = showOfferCount,
                        showBadges = showBadges,
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
            shape = RoundedCornerShape(16.dp),
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
            shape = RoundedCornerShape(16.dp),
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
    viewMode: String,
    timePeriod: String,
    showDates: Boolean,
    showPrices: Boolean,
    showOfferCount: Boolean,
    showBadges: Boolean,
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
    viewMode: String = "Compacta",
    showDates: Boolean = true,
    showOfferCount: Boolean = true,
    showBadges: Boolean = true,
    onClick: () -> Unit,
    presupuestoAdjudicado: PresupuestoFalso? = null,
    onAdjudicadoClick: () -> Unit = {}
) {
    // Mapear el status a FolderTheme
    val theme = when {
        status.contains("ACTIVA", ignoreCase = true) -> FolderTheme.Active
        status.contains("ADJUDICADA", ignoreCase = true) -> FolderTheme.Adjudicated
        status.contains("TERMINADA", ignoreCase = true) -> FolderTheme.Finished
        status.contains("CANCELADA", ignoreCase = true) -> FolderTheme.Cancelled
        else -> FolderTheme.Active
    }

    // Mostrar diseño según viewMode
    when (viewMode) {
        "Compacta" -> LicitacionCompactaView(
            categoriaNombre = categoriaNombre,
            licitacionNombre = licitacionNombre,
            status = status,
            theme = theme,
            presupuestosCount = presupuestosCount,
            hasNewBudgets = hasNewBudgets,
            showBadges = showBadges,
            showOfferCount = showOfferCount,
            onClick = onClick
        )
        "Tarjetas" -> LicitacionTarjetaView(
            categoriaNombre = categoriaNombre,
            licitacionNombre = licitacionNombre,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            status = status,
            theme = theme,
            presupuestosCount = presupuestosCount,
            hasNewBudgets = hasNewBudgets,
            showDates = showDates,
            showBadges = showBadges,
            showOfferCount = showOfferCount,
            onClick = onClick,
            presupuestoAdjudicado = presupuestoAdjudicado,
            onAdjudicadoClick = onAdjudicadoClick
        )
        else -> LicitacionArchiveroViewDetallada(
            categoriaNombre = categoriaNombre,
            licitacionNombre = licitacionNombre,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            status = status,
            theme = theme,
            presupuestosCount = presupuestosCount,
            hasNewBudgets = hasNewBudgets,
            showDates = showDates,
            showBadges = showBadges,
            showOfferCount = showOfferCount,
            onClick = onClick,
            presupuestoAdjudicado = presupuestoAdjudicado,
            onAdjudicadoClick = onAdjudicadoClick
        )
    }
}

// VISTA DETALLADA (Diseño de Carpeta/Archivero Original)
@Composable
fun LicitacionArchiveroViewDetallada(
    categoriaNombre: String,
    licitacionNombre: String,
    fechaInicio: String,
    fechaFin: String,
    status: String,
    theme: FolderTheme,
    presupuestosCount: Int,
    hasNewBudgets: Boolean,
    showDates: Boolean,
    showBadges: Boolean,
    showOfferCount: Boolean,
    onClick: () -> Unit,
    presupuestoAdjudicado: PresupuestoFalso?,
    onAdjudicadoClick: () -> Unit
) {
    // Estado para controlar si la tarjeta está expandida
    var isExpanded by remember { mutableStateOf(true) }

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
        if (showBadges) {
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

                        // Indicador de expandir/colapsar - Lado derecho (siempre expandido en vista detallada)
                        Column(horizontalAlignment = Alignment.End) {

                            // Indicador de nuevos ingresos cuando está contraída
                            if (!isExpanded && hasNewBudgets && showBadges && !theme.isCancelled) {
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

                    // Contenido expandible - Fechas (siempre visible en vista detallada)
                    if (showDates) {
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

                    // Divider y Footer (siempre visible en vista detallada)
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
                                if (showOfferCount) {
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
                                        if (hasNewBudgets && showBadges && !theme.isCancelled) {
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
                                            } // Cierre Row "Nuevos Ingresos"
                                        } // Cierre if hasNewBudgets
                                    } // Cierre Column (línea 2018)
                                } // Cierre if showOfferCount (línea 2017)
                            } // Cierre Row Footer principal (línea 1918)
                } // Cierre Column principal (línea 1787)
            } // Cierre Box interno gradiente (línea 1778)
        } // Cierre Surface CAPA 4 (línea 1771)
    } // Cierre Box principal (línea 1682)
} // Cierre función LicitacionArchiveroViewDetallada

// VISTA COMPACTA (Lista simple y minimalista)
@Composable
fun LicitacionCompactaView(
    categoriaNombre: String,
    licitacionNombre: String,
    status: String,
    theme: FolderTheme,
    presupuestosCount: Int,
    hasNewBudgets: Boolean,
    showBadges: Boolean,
    showOfferCount: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna izquierda - Info principal
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getCategoryEmoji(categoriaNombre),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = licitacionNombre,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Columna derecha - Badge y contador
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge de estado
                if (showBadges) {
                    Surface(
                        color = theme.badgeTextColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = status,
                            color = theme.badgeTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Contador de ofertas
                if (showOfferCount) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasNewBudgets && showBadges) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFFE11D48), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "$presupuestosCount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = theme.primaryColor
                        )
                    }
                }
            }
        }
    }
}

// VISTA TARJETAS (Diseño moderno tipo Material Design)
@Composable
fun LicitacionTarjetaView(
    categoriaNombre: String,
    licitacionNombre: String,
    fechaInicio: String,
    fechaFin: String,
    status: String,
    theme: FolderTheme,
    presupuestosCount: Int,
    hasNewBudgets: Boolean,
    showDates: Boolean,
    showBadges: Boolean,
    showOfferCount: Boolean,
    onClick: () -> Unit,
    presupuestoAdjudicado: PresupuestoFalso?,
    onAdjudicadoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header con gradiente y emoji
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(theme.secondaryColor, theme.tertiaryColor)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = getCategoryEmoji(categoriaNombre),
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = categoriaNombre.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor,
                        letterSpacing = 1.sp
                    )
                }

                // Badge de estado en esquina superior derecha
                if (showBadges) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(bottomStart = 12.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = status,
                            color = theme.badgeTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Contenido de la tarjeta
            Column(modifier = Modifier.padding(20.dp)) {
                // Nombre del proyecto
                Text(
                    text = licitacionNombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )

                // Fechas
                if (showDates) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "INICIO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = fechaInicio,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.primaryColor
                            )
                        }
                        Column {
                            Text(
                                text = "FIN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = fechaFin,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.primaryColor
                            )
                        }
                    }
                }

                // Footer con contador
                if (showOfferCount) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasNewBudgets && showBadges) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFE11D48), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nuevas ofertas",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBE123C)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Surface(
                            color = theme.primaryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$presupuestosCount ofertas",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.primaryColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = theme.primaryColor
                                )
                            }
                        }
                    }
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

// --- DIMENSIONES A4 ---
// Ratio A4 = 1 : 1.414
val A4_WIDTH = 450.dp
val A4_HEIGHT = (450 * 1.414).dp // ~636.dp

@Composable
fun BudgetPreviewPDFDialog(
    presupuesto: PresupuestoFalso,
    onDismiss: () -> Unit
) {
    val provider = remember { SampleDataFalso.getPrestadorById(presupuesto.prestadorId) }

    val items = listOf(
        PresupuestoItemDisplay("1", "Fuente 12v", "$ 18.000,00", "$ 18.000,00"),
        PresupuestoItemDisplay("1", "Balun TVI", "$ 3.000,00", "$ 3.000,00"),
        PresupuestoItemDisplay("1", "Ficha dc", "$ 480,00", "$ 480,00"),
        PresupuestoItemDisplay("-", "Mano de obra Instalación Cableada", "$ 130.000,00", "$ 130.000,00", isSpecial = true),
        PresupuestoItemDisplay("-", "Movilidad", "$ 45.000,00", "$ 45.000,00")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenWidth = maxWidth
            val initialFitScale = remember(screenWidth) {
                ((screenWidth - 32.dp) / A4_WIDTH).coerceAtMost(1f)
            }

            // ESTADOS DE ZOOM Y PANEO
            var scale by remember { mutableStateOf(initialFitScale) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            // --- CONTENEDOR PRINCIPAL (VISOR) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF202020)) // Fondo Gris Oscuro (Estilo Acrobat Reader)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(initialFitScale, 4f) // Zoom máx 4x
                            offset += pan
                        }
                    }
            ) {

                // --- LA HOJA DE PAPEL A4 ---
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .width(A4_WIDTH)
                        .wrapContentHeight()
                        .shadow(elevation = 12.dp)
                        .background(Color.White)
                ) {
                    // Contenido de la hoja
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate300)
                    ) {
                        // Franja Decorativa
                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaverickGradient))

                        // Encabezado
                        A4HeaderSection(provider, presupuesto)
                        HorizontalDivider(color = Slate200)

                        // Datos Cliente
                        A4ClientInfoSection(provider, presupuesto)

                        // Tabla
                        A4ItemsTable(items)

                        // Footer (sin espacio adicional)
                        A4FooterSection(presupuesto.precioTotal)
                    }
                }

                // --- BOTÓN CERRAR ---
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .size(48.dp)
                        .zIndex(10f)
                ) {
                    Icon(Icons.Default.Close, "Cerrar", tint = Slate800)
                }

                // --- CONTROLES DE ZOOM ---
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Slate800.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .zIndex(10f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scale = (scale * 0.8f).coerceAtLeast(initialFitScale)
                            offset = Offset.Zero
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, "Alejar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = "${(scale / initialFitScale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 50.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = {
                            scale = (scale * 1.25f).coerceAtMost(4f)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, "Acercar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            scale = initialFitScale
                            offset = Offset.Zero
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, "Resetear", tint = Color.White)
                    }
                }

                // --- BOTÓN DESCARGAR PDF ---
                IconButton(
                    onClick = {
                        // TODO: Implementar descarga de PDF
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(MaverickBlueEnd.copy(alpha = 0.9f), CircleShape)
                        .size(48.dp)
                        .zIndex(10f)
                ) {
                    Icon(Icons.Default.Download, "Descargar PDF", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun A4HeaderSection(provider: UserFalso?, presupuesto: PresupuestoFalso) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Logo y dirección
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaverickGradient)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                provider?.let {
                    Text(
                        "${it.name} ${it.lastName}".uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Slate800,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        it.services.firstOrNull() ?: "INFORMÁTICA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.5.sp,
                        lineHeight = 11.sp
                    )
                   // Text(
                        //it.address,
                     //   fontSize = 9.sp,
                       // fontWeight = FontWeight.Normal,
                       // color = Slate600,
                       /// lineHeight = 11.sp
                    //)
                } ?: run {
                    Text("MAVERICK", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Slate800, letterSpacing = (-0.5).sp, lineHeight = 16.sp)
                    Text("INFORMÁTICA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 1.5.sp, lineHeight = 11.sp)
                    Text("B. Matienzo 1339", fontSize = 9.sp, fontWeight = FontWeight.Normal, color = Slate600, lineHeight = 11.sp)
                }
            }
        }

        // La "X" con PRESUPUESTO debajo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(2.dp, Slate800, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("X", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Slate800)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("PRESUPUESTO", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Slate600, letterSpacing = 0.5.sp)
        }

        // Datos
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .background(Slate50)
                    .border(1.dp, Slate300, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("N° ${presupuesto.id.takeLast(8)}", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Slate800)
            }
            Text(presupuesto.fechaRecepcion, fontSize = 10.sp, color = Slate600, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun A4ClientInfoSection(provider: UserFalso?, presupuesto: PresupuestoFalso) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        // Emisor
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("DE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400)
            provider?.let {
                Text(
                    it.companyName ?: "${it.name} ${it.lastName}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = Slate600
                )
            } ?: run {
                Text("Maverick Informática", fontSize = 10.sp, fontWeight = FontWeight.Normal, color = Slate600)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Receptor
        Column {
            Text("PARA:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400)
            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp).width(20.dp), color = Slate300)

            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text("CLIENTE / EMPRESA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cliente", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800, lineHeight = 13.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(1.dp)
                            .height(13.dp)
                            .background(Slate400)
                    )
                    Text(provider?.companyName ?: "", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800, lineHeight = 13.sp)
                }
                HorizontalDivider(color = Slate400, thickness = 1.dp)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DIRECCIÓN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                    Text("A definir", fontSize = 11.sp, color = Slate800, lineHeight = 14.sp)
                    HorizontalDivider(color = Slate300, thickness = 1.dp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("MÉTODO DE PAGO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                    Text("A definir", fontSize = 11.sp, color = Slate800, lineHeight = 14.sp)
                    HorizontalDivider(color = Slate300, thickness = 1.dp)
                }
            }
            Column {
                Text("TRABAJO / PROYECTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Text(presupuesto.nombre, fontSize = 11.sp, color = Slate800, lineHeight = 14.sp)
                HorizontalDivider(color = Slate300, thickness = 1.dp)
            }
        }
    }
}

@Composable
fun A4ItemsTable(items: List<PresupuestoItemDisplay>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.border(1.dp, Slate300)) {
            // Header
            Row(modifier = Modifier.background(Slate100).height(IntrinsicSize.Min)) {
                A4TableCell("Cant", 0.15f, true)
                A4TableCell("Descripción", 0.55f, true)
                A4TableCell("Unitario", 0.3f, true, alignRight = true)
                A4TableCell("Total", 0.3f, true, alignRight = true, isLast = true)
            }
            HorizontalDivider(color = Slate300)

            // Items
            items.forEach { item ->
                val bg = if (item.isSpecial) Color(0xFFEFF6FF) else Color.White
                val color = if (item.isSpecial) MaverickBlueEnd else Slate800
                val weight = if (item.isSpecial) FontWeight.Bold else FontWeight.Normal

                Row(modifier = Modifier.background(bg).height(IntrinsicSize.Min)) {
                    A4TableCell(if(item.isSpecial) "-" else item.cantidad, 0.15f, color = Slate600)
                    A4TableCell(item.descripcion, 0.55f, color = color, fontWeight = weight)
                    A4TableCell(item.unitario, 0.3f, alignRight = true, color = Slate600)
                    A4TableCell(item.total, 0.3f, alignRight = true, fontWeight = FontWeight.Bold, color = color, isLast = true)
                }
                HorizontalDivider(color = Slate300)
            }

            // Filas de relleno ELIMINADAS para evitar espacio en blanco forzado
        }
    }
}

@Composable
fun RowScope.A4TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    alignRight: Boolean = false,
    isLast: Boolean = false,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    val finalColor = if (color == Color.Unspecified) (if (isHeader) Slate600 else Slate800) else color
    val finalWeight = fontWeight ?: (if (isHeader) FontWeight.Bold else FontWeight.Normal)

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .then(if (!isLast) Modifier.border(width = 0.5.dp, color = Slate300.copy(alpha = 0.5f)) else Modifier)
            .padding(6.dp),
        contentAlignment = if (alignRight) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 9.sp else 10.sp,
            fontWeight = finalWeight,
            color = finalColor,
            textAlign = if (alignRight) TextAlign.End else TextAlign.Start,
            lineHeight = if (isHeader) 11.sp else 12.sp
        )
    }
}

@Composable
fun A4FooterSection(total: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate50)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Nota Legal (Izquierda)
            Text(
                text = "Nota: Los precios están expresados en Pesos Argentinos.\nVálido por 15 días.",
                fontSize = 10.sp,
                color = Slate400,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 14.sp,
                modifier = Modifier.width(180.dp)
            )

            // Cuadro de Totales (Derecha)
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .shadow(2.dp, RoundedCornerShape(4.dp))
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, Slate300, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", fontSize = 11.sp, color = Slate600)
                    Text("$ ${total.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Descuento:", fontSize = 11.sp, color = Slate600)
                    Text("-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Slate200)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Slate800)
                    Text("$ ${total.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaverickBlueEnd)
                }
            }
        }
    }
}

@Composable
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

/**package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.theme.MyApplicationTheme

// =================================================================================
// --- PANTALLA PRINCIPAL DE PRESUPUESTOS ---
// =================================================================================
// Esta pantalla muestra dos tipos de presupuestos:
// 1. Presupuestos de Licitaciones (Agrupados por licitación)
// 2. Presupuestos Generales (Solicitudes directas)
// Utiliza un sistema de capas (Box) para manejar búsquedas y menús flotantes.

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PresupuestosScreen(
    onProfileClick: (String) -> Unit = {},
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit
) {
    // --- ESTADOS DE LA UI ---
    // Control de pestañas (0: Licitaciones, 1: Generales)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Presupuestos de Licitaciones", "Presupuestos Generales")

    // Control de búsqueda y menú flotante
    var isSearchActive by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- CARGA DE DATOS ---
    // Se cargan los presupuestos desde la fuente de datos falsa
    val allPresupuestos = remember { ClientBudgetDataFalso.presupuestos }
    // Filtro 1: Agrupar licitaciones por nombre
    val licitacionesAgrupadas = allPresupuestos.filter { it.esLicitacion }.groupBy { it.nombre }
    // Filtro 2: Presupuestos directos
    val presupuestosGenerales = allPresupuestos.filter { !it.esLicitacion }

    // --- ESTADOS DE DIÁLOGOS Y SHEETS ---
    var selectedLicitacionBudgets by remember { mutableStateOf<List<PresupuestoFalso>>(emptyList()) }
    var showBudgetSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedPresupuesto by remember { mutableStateOf<PresupuestoFalso?>(null) }

    // Función para limpiar y cerrar la búsqueda
    val closeSearch = {
        isSearchActive = false
        searchQuery = ""
        keyboardController?.hide()
        Unit
    }

    // --- MANEJO DE VENTANAS MODALES ---

    // BottomSheet: Muestra la lista de presupuestos dentro de una licitación específica
    if (showBudgetSheet) {
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

    // Diálogo: Vista previa del PDF (Simulada)
    if (selectedPresupuesto != null) {
        BudgetPreviewPDFDialog(
            presupuesto = selectedPresupuesto!!,
            onDismiss = { selectedPresupuesto = null }
        )
    }

    // --- ESTRUCTURA PRINCIPAL (SCAFFOLD) ---
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // La barra superior desaparece si la búsqueda está activa para dar paso a la barra de búsqueda animada
            if (!isSearchActive) {
                TopAppBar(
                    title = { Text("Presupuestos Recibidos") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        }
    ) { paddingValues ->
        // Contenedor Box para manejar capas superpuestas (Search, Scrim, FAB)
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // CAPA 1: CONTENIDO PRINCIPAL (PESTAÑAS Y LISTAS)
            Column(modifier = Modifier.fillMaxSize()) {
                // Selector de Pestañas
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Contenido según pestaña seleccionada
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

            // CAPA 2: SCRIM (FONDO OSCURO AL BUSCAR/ABRIR MENÚ)
            if (isSearchActive || isFabMenuExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .zIndex(5f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isFabMenuExpanded = false
                            if (isSearchActive) closeSearch()
                        }
                )
            }

            // CAPA 3: BARRA DE BÚSQUEDA ANIMADA (PARTE SUPERIOR)
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .zIndex(10f)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        GeminiTopSearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            placeholderText = "Buscar presupuestos..."
                        )
                    }

                    // Botón cerrar búsqueda
                    val rainbowBrush = geminiGradientEffect()
                    Surface(
                        modifier = Modifier.size(56.dp).clickable(onClick = closeSearch),
                        shape = CircleShape,
                        color = Color(0xFF121212),
                        border = BorderStroke(2.5.dp, rainbowBrush),
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, "Cerrar", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }
            }

            // CAPA 4: MENÚ FLOTANTE DIVIDIDO (FAB GEMINI)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .zIndex(15f),
                contentAlignment = Alignment.BottomEnd
            ) {
                GeminiSplitFAB(
                    isExpanded = isFabMenuExpanded,
                    isSearchActive = isSearchActive,
                    onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                    onActivateSearch = { isSearchActive = true },
                    onCloseSearch = closeSearch,
                    expandedTools = {
                        SmallFabTool(label = "Filtros", icon = Icons.Default.FilterList, onClick = {})
                        SmallFabTool(label = "Recientes", icon = Icons.Default.History, onClick = {})
                    }
                )
            }
        }
    }
}

// =================================================================================
// --- SECCIÓN: CONTENIDO DE PESTAÑA LICITACIONES ---
// =================================================================================

@Composable
fun LicitacionesTabContent(
    licitaciones: Map<String, List<PresupuestoFalso>>,
    searchQuery: String,
    onLicitacionClick: (List<PresupuestoFalso>) -> Unit,
    onPresupuestoClick: (PresupuestoFalso) -> Unit
) {
    // --- ESTADOS DE FILTRADO ---
    var selectedStatus by remember { mutableStateOf<EstadoLicitacion?>(null) }
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var sortAscending by remember { mutableStateOf(true) }

    // Obtención dinámica de categorías disponibles
    val allCategories = licitaciones.values.flatten().map { it.servicioCategoria }.distinct()

    // Lógica de ordenamiento por prioridad de estado
    val statusOrder = mapOf(
        EstadoLicitacion.ACTIVA to 1,
        EstadoLicitacion.ADJUDICADA to 2,
        EstadoLicitacion.TERMINADA to 3,
        EstadoLicitacion.CANCELADA to 4
    )

    // Filtrado y ordenamiento de la lista
    val filteredAndSortedLicitaciones = licitaciones.filter { (nombre, presupuestos) ->
        val first = presupuestos.first()
        val statusMatch = selectedStatus == null || first.estadoLicitacion == selectedStatus
        val categoryMatch = selectedCategories.isEmpty() || selectedCategories.contains(first.servicioCategoria)
        val searchMatch = nombre.contains(searchQuery, ignoreCase = true) ||
                presupuestos.any { it.servicioCategoria.contains(searchQuery, ignoreCase = true) }
        statusMatch && categoryMatch && searchMatch
    }.entries.sortedWith(
        compareBy<Map.Entry<String, List<PresupuestoFalso>>> { (_, budgets) ->
            // Prioridad 1: Activas con nuevos presupuestos
            val hasNew = budgets.any { it.isNew }
            val status = budgets.first().estadoLicitacion
            if (status == EstadoLicitacion.ACTIVA && hasNew) 0 else 1
        }.thenBy { (_, budgets) ->
            // Prioridad 2: Estado de la licitación
            statusOrder[budgets.first().estadoLicitacion] ?: 5
        }
    )

    Column {
        // Barra de Filtros (Horizontal)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryFilterSplitButton(
                allCategories = allCategories,
                selectedCategories = selectedCategories,
                onCategoryToggle = { category ->
                    val newSelection = selectedCategories.toMutableSet()
                    if (newSelection.contains(category)) newSelection.remove(category) else newSelection.add(category)
                    selectedCategories = newSelection
                },
                onClear = { selectedCategories = emptySet() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            SortFilterChip(isAscending = sortAscending, onClick = { sortAscending = !sortAscending })
            Spacer(modifier = Modifier.weight(1f))
            StatusFilterSplitButton(selectedStatus = selectedStatus, onStatusSelected = { selectedStatus = it })
        }

        // Lista de resultados
        if (filteredAndSortedLicitaciones.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = filteredAndSortedLicitaciones,
                    key = { _, item -> item.key } // Clave única: Nombre de licitación
                ) { index, (nombre, presupuestos) ->
                    val licitacionInfo = presupuestos.first()
                    val hasNew = presupuestos.any { it.isNew }
                    val presupuestoAdjudicado = if (licitacionInfo.estadoLicitacion == EstadoLicitacion.ADJUDICADA) presupuestos.firstOrNull() else null

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
                }
            }
        }
    }
}

// =================================================================================
// --- SECCIÓN: CONTENIDO DE PESTAÑA GENERALES ---
// =================================================================================

@Composable
fun GeneralesTabContent(
    presupuestos: List<PresupuestoFalso>,
    searchQuery: String,
    onPresupuestoClick: (PresupuestoFalso) -> Unit,
    onProfileClick: (String) -> Unit,
    onChatClick: (String) -> Unit
) {
    // Filtrado simple por nombre o prestador
    val filtered = presupuestos.filter {
        it.nombre.contains(searchQuery, ignoreCase = true) ||
        it.prestadorNombre.contains(searchQuery, ignoreCase = true)
    }

    if (filtered.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { p ->
                PresupuestoGeneralCard(
                    presupuesto = p,
                    onClick = { onPresupuestoClick(p) },
                    onProfileClick = { onProfileClick(p.prestadorId) },
                    onChatClick = { onChatClick(p.prestadorId) }, // Corrección aplicada aquí
                    onPreviewClick = { onPresupuestoClick(p) }
                )
            }
        }
    }
}

// =================================================================================
// --- SECCIÓN: COMPONENTES DE UI (BOTONES DE FILTRO) ---
// =================================================================================

@Composable
fun CategoryFilterSplitButton(allCategories: List<String>, selectedCategories: Set<String>, onCategoryToggle: (String) -> Unit, onClear: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val isSelected = selectedCategories.isNotEmpty()
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box {
        Surface(shape = RoundedCornerShape(16.dp), color = containerColor, contentColor = contentColor, modifier = Modifier.height(44.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clickable { expanded = true }.padding(start = 12.dp, end = 8.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Category, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isSelected) "${selectedCategories.size}" else "Categorías", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(contentColor.copy(alpha = 0.3f)))
                Box(modifier = Modifier.clickable { if (isSelected) onClear() else expanded = true }.padding(horizontal = 10.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Icon(imageVector = if (isSelected) Icons.Default.Close else Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            allCategories.forEach { category ->
                DropdownMenuItem(text = { Text(category) }, onClick = { onCategoryToggle(category) })
            }
        }
    }
}

@Composable
fun StatusFilterSplitButton(selectedStatus: EstadoLicitacion?, onStatusSelected: (EstadoLicitacion?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val isSelected = selectedStatus != null
    val containerColor = if (isSelected) selectedStatus.color else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box {
        Surface(shape = RoundedCornerShape(16.dp), color = containerColor, contentColor = contentColor, modifier = Modifier.height(44.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clickable { expanded = true }.padding(start = 12.dp, end = 8.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text(text = selectedStatus?.displayName ?: "Estado", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(contentColor.copy(alpha = 0.3f)))
                Box(modifier = Modifier.clickable { if (isSelected) onStatusSelected(null) else expanded = true }.padding(horizontal = 10.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Icon(imageVector = if (isSelected) Icons.Default.Close else Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            EstadoLicitacion.entries.forEach { status ->
                DropdownMenuItem(text = { Text(status.displayName) }, onClick = { onStatusSelected(status); expanded = false })
            }
        }
    }
}

// =================================================================================
// --- SECCIÓN: TARJETAS Y ELEMENTOS VISUALES ---
// =================================================================================

/**
 * Obtiene un emoji representativo basado en el nombre de la categoría.
 */
fun getCategoryEmoji(categoria: String): String = when {
    categoria.contains("Informatica", ignoreCase = true) -> "💻"
    categoria.contains("Electricidad", ignoreCase = true) -> "⚡"
    else -> "📋"
}

/**
 * Indicador visual animado (punto pulsante) para notificaciones nuevas.
 */
@Composable
fun PulsingDotIndicator(dotSize: Dp = 8.dp, color: Color = Color(0xFFE11D48)) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 2.5f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart), label = "scale")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.7f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart), label = "alpha")
    Box(modifier = Modifier.size(dotSize * 2), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(dotSize).graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }.background(color, CircleShape))
        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
    }
}

/**
 * Tarjeta expandible que agrupa presupuestos de una misma licitación.
 * Muestra resumen, estado y alertas de nuevos presupuestos.
 */
@Composable
fun LicitacionArchiveroCard(categoriaNombre: String, licitacionNombre: String, fechaInicio: String, fechaFin: String, status: String, statusColor: Color, presupuestosCount: Int, hasNewBudgets: Boolean, onClick: () -> Unit, presupuestoAdjudicado: PresupuestoFalso?, onAdjudicadoClick: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Icono, Nombre y Estado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(getCategoryEmoji(categoriaNombre), fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(licitacionNombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(status.uppercase(), color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
                Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
            }
            // Detalles expandibles
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text("Desde: $fechaInicio - Hasta: $fechaFin", fontSize = 12.sp, color = Color.Gray)
                    if (presupuestoAdjudicado != null) Text("Adjudicado a: ${presupuestoAdjudicado.prestadorNombre}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp).clickable { onAdjudicadoClick() })
                    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Ver $presupuestosCount Presupuestos") }
                }
            }
            // Indicador de "Nuevos"
            if (!isExpanded && hasNewBudgets) Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) { PulsingDotIndicator(dotSize = 6.dp); Spacer(Modifier.width(4.dp)); Text("Nuevos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE123C)) }
        }
    }
}

/**
 * Tarjeta simple para un presupuesto individual (fuera de licitaciones).
 */
@Composable
fun PresupuestoGeneralCard(presupuesto: PresupuestoFalso, onClick: () -> Unit, onProfileClick: () -> Unit, onChatClick: () -> Unit, onPreviewClick: () -> Unit) {
    val provider = remember { SampleDataFalso.getPrestadorById(presupuesto.prestadorId) }
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = rememberAsyncImagePainter(model = provider?.profileImageUrl), contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onProfileClick), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(presupuesto.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(presupuesto.prestadorNombre, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("S/ ${presupuesto.precioTotal}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row {
                    IconButton(onClick = onPreviewClick) { Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onChatClick) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
            Text("No se encontraron resultados", color = Color.Gray)
        }
    }
}

@Composable
fun LicitacionDetailSheetContent(budgets: List<PresupuestoFalso>, onProfileClick: (String) -> Unit, onChatClick: (String) -> Unit, onBudgetClick: (PresupuestoFalso) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Detalle de Presupuestos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(budgets) { b -> PresupuestoGeneralCard(presupuesto = b, onClick = { onBudgetClick(b) }, onProfileClick = { onProfileClick(b.prestadorId) }, onChatClick = { onChatClick(b.prestadorId) }, onPreviewClick = { onBudgetClick(b) }) }
        }
    }
}

@Composable
fun SortFilterChip(isAscending: Boolean, onClick: () -> Unit) {
    FilterChip(selected = true, onClick = onClick, label = { Text("Fecha") }, leadingIcon = { Icon(if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null, modifier = Modifier.size(18.dp)) })
}

@Composable
fun BudgetPreviewPDFDialog(presupuesto: PresupuestoFalso, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }, title = { Text("Vista Previa PDF") }, text = { Text("Aquí se mostraría el documento para ${presupuesto.nombre}") })
}

@Composable
fun GradientDivider(color: Color, thickness: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(thickness).background(Brush.horizontalGradient(listOf(Color.Transparent, color, Color.Transparent))))
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PresupuestosScreenPreview() { MyApplicationTheme { PresupuestosScreen(onBack = {}) } }
**/