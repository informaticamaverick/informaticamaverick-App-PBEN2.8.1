package com.example.myapplication.Client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.zIndex
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.getAppColors
import java.text.SimpleDateFormat
import java.util.*

// Modelo de datos para las visitas técnicas
data class TechnicalVisit(
    val id: String,
    val date: String, // Formato "yyyy-MM-dd"
    val time: String, // Ej: "10:30"
    val service: String,
    val provider: String,
    val status: VisitStatus,
    val avatarColor: Color
)

enum class VisitStatus {
    CONFIRMED,
    PENDING,
    CANCELLED
}

// Datos simulados (luego los conectaremos con Firebase)
val SAMPLE_VISITS = listOf(
    TechnicalVisit("1", "2026-01-15", "10:30", "Instalación Eléctrica", "Juan Pérez", VisitStatus.CONFIRMED, Color(0xFF6366F1)),
    TechnicalVisit("2", "2026-01-15", "14:00", "Reparación Plomería", "María García", VisitStatus.PENDING, Color(0xFFEC4899)),
    TechnicalVisit("3", "2026-01-20", "09:00", "Pintura de Fachada", "Carlos López", VisitStatus.CONFIRMED, Color(0xFF10B981)),
    TechnicalVisit("4", "2026-01-20", "16:30", "Revisión HVAC", "Ana Martínez", VisitStatus.PENDING, Color(0xFFF59E0B))
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit
) {
    // Obtener colores adaptables al tema
    val colors = getAppColors()
    val materialColors = MaterialTheme.colorScheme
    val isSystemInDarkMode = isSystemInDarkTheme()

    // Estados para manejar fechas
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    // Estados para el modal de cancelación
    var showCancelModal by remember { mutableStateOf(false) }
    var visitToCancel by remember { mutableStateOf<String?>(null) }

    // Lista mutable de visitas (para poder modificar el estado)
    var visits by remember { mutableStateOf(SAMPLE_VISITS) }

    // Estados para FABs y búsqueda
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Estado cíclico del menú: 0 = Filtros, 1 = Menú config, 2 = Todo oculto
    var menuState by remember { mutableIntStateOf(0) }
    val showSettingsMenu = menuState == 0
    val showVerticalMenu = menuState == 1

    // Estados para filtros
    var filterStatus by remember { mutableStateOf<VisitStatus?>(null) }
    var sortByTime by remember { mutableStateOf(true) } // true = Horario, false = Servicio

    // Estados del menú de configuración
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showDataVisibilityDialog by remember { mutableStateOf(false) }
    var showTimePeriodDialog by remember { mutableStateOf(false) }

    // Preferencias de usuario
    var viewMode by remember { mutableStateOf("Detallada") } // "Compacta", "Detallada", "Tarjetas"
    var timePeriod by remember { mutableStateOf("Todo") } // "Semana", "Mes", "3 Meses", "Todo"
    var showDates by remember { mutableStateOf(true) }
    var showProviderInfo by remember { mutableStateOf(true) }
    var showStatus by remember { mutableStateOf(true) }
    var notifyUpcoming by remember { mutableStateOf(true) }
    var notifyChanges by remember { mutableStateOf(true) }
    var notifyCancellations by remember { mutableStateOf(true) }

    // Formato de fecha para comparación
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Filtrar eventos del día seleccionado con búsqueda y filtros
    val selectedDateStr = dateFormat.format(selectedDate.time)

    // Filtrar visitas por período de tiempo
    val filteredVisitsByPeriod = remember(visits, timePeriod) {
        val currentCalendar = Calendar.getInstance()
        val cutoffDate = Calendar.getInstance().apply {
            when(timePeriod) {
                "Semana" -> add(Calendar.DAY_OF_YEAR, -7)
                "Mes" -> add(Calendar.MONTH, -1)
                "3 Meses" -> add(Calendar.MONTH, -3)
                else -> add(Calendar.YEAR, -100) // "Todo" - mostrar todo
            }
        }
        
        visits.filter { visit ->
            try {
                val visitDate = dateFormat.parse(visit.date)
                visitDate != null && !visitDate.before(cutoffDate.time)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    val eventsForSelectedDay = remember(selectedDateStr, filteredVisitsByPeriod, searchQuery, filterStatus, sortByTime) {
        var filtered = filteredVisitsByPeriod.filter { it.date == selectedDateStr }
        
        // Filtro por búsqueda
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.service.contains(searchQuery, ignoreCase = true) ||
                it.provider.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Filtro por estado
        if (filterStatus != null) {
            filtered = filtered.filter { it.status == filterStatus }
        }
        
        // Ordenar
        if (sortByTime) {
            filtered = filtered.sortedBy { it.time }
        } else {
            filtered = filtered.sortedBy { it.service }
        }
        
        filtered
    }

    // Días que tienen eventos (para mostrar indicador) - usar visitas filtradas
    val daysWithEvents = filteredVisitsByPeriod.filter { it.status != VisitStatus.CANCELLED }.map { it.date }.toSet()

    // Estado para controlar si la lista de eventos está expandida
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()
        .background(colors.backgroundColor)) {
        Scaffold(
            containerColor = colors.backgroundColor,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Eventos / Turnos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = colors.textPrimaryColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.surfaceColor,
                        titleContentColor = colors.textPrimaryColor,
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
                            // Opción: Alertas
                            Surface(
                                modifier = Modifier.size(64.dp),
                                onClick = { 
                                    showNotificationsDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = materialColors.surface,
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
                                        tint = materialColors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Alertas",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = materialColors.onSurface,
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
                                color = materialColors.surface,
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
                                        tint = materialColors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Datos",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = materialColors.onSurface,
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
                                color = materialColors.surface,
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
                                        tint = materialColors.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Período",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = materialColors.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                
                    // Botones de búsqueda y engranaje
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
                                // Botón: Filtrar Estado
                                Surface(
                                    modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                    onClick = { 
                                        filterStatus = when(filterStatus) {
                                            null -> VisitStatus.CONFIRMED
                                            VisitStatus.CONFIRMED -> VisitStatus.PENDING
                                            VisitStatus.PENDING -> null
                                            else -> null
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (filterStatus != null) materialColors.primaryContainer else materialColors.surface,
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
                                            tint = if (filterStatus != null) materialColors.onPrimaryContainer else materialColors.onSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = when(filterStatus) {
                                                VisitStatus.CONFIRMED -> "Confirm."
                                                VisitStatus.PENDING -> "Pend."
                                                else -> "Todos"
                                            },
                                            color = if (filterStatus != null) materialColors.onPrimaryContainer else materialColors.onSurface,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                                
                                // Botón: Ordenar
                                Surface(
                                    modifier = Modifier.size(width = 64.dp, height = 56.dp),
                                    onClick = { 
                                        sortByTime = !sortByTime
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (sortByTime) materialColors.primaryContainer else materialColors.surface,
                                    shadowElevation = 6.dp
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            if (sortByTime) Icons.Default.AccessTime else Icons.Default.SortByAlpha,
                                            "Ordenar",
                                            tint = if (sortByTime) materialColors.onPrimaryContainer else materialColors.onSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (sortByTime) "Horario" else "Servicio",
                                            color = if (sortByTime) materialColors.onPrimaryContainer else materialColors.onSurface,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Botón Dividido (Buscar + Engranaje)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Parte Izquierda: Buscar
                            Surface(
                                onClick = { isSearchActive = true },
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                                color = materialColors.surface,
                                border = BorderStroke(2.5.dp, rainbowBrush),
                                shadowElevation = 12.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Search, null, tint = materialColors.onSurface, modifier = Modifier.size(26.dp))
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
                                color = materialColors.surface,
                                border = BorderStroke(2.5.dp, rainbowBrush),
                                shadowElevation = 12.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Settings,
                                        "Ajustes",
                                        tint = materialColors.onSurface,
                                        modifier = Modifier.size(26.dp).rotate(gearRotation)
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            },
floatingActionButtonPosition = FabPosition.End,
            //containerColor = colors.backgroundColor
        ) { paddingValues ->
        
        // Diálogo: Alertas de Notificaciones
        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                title = { Text("Alertas", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Configurar notificaciones:", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notificar visitas próximas")
                            Switch(
                                checked = notifyUpcoming,
                                onCheckedChange = { notifyUpcoming = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notificar cambios de horario")
                            Switch(
                                checked = notifyChanges,
                                onCheckedChange = { notifyChanges = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notificar cancelaciones")
                            Switch(
                                checked = notifyCancellations,
                                onCheckedChange = { notifyCancellations = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showNotificationsDialog = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationsDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Diálogo: Mostrar Datos
        if (showDataVisibilityDialog) {
            AlertDialog(
                onDismissRequest = { showDataVisibilityDialog = false },
                title = { Text("Mostrar Datos", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Configurar visibilidad de datos:", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar fechas")
                            Switch(
                                checked = showDates,
                                onCheckedChange = { showDates = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar info del prestador")
                            Switch(
                                checked = showProviderInfo,
                                onCheckedChange = { showProviderInfo = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar estado de visita")
                            Switch(
                                checked = showStatus,
                                onCheckedChange = { showStatus = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showDataVisibilityDialog = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDataVisibilityDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Diálogo: Período de Tiempo
        if (showTimePeriodDialog) {
            AlertDialog(
                onDismissRequest = { showTimePeriodDialog = false },
                title = { Text("Período de Tiempo", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Filtrar visitas por período:", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val periods = listOf("Semana", "Mes", "3 Meses", "Todo")
                        periods.forEach { period ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { timePeriod = period }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(period)
                                RadioButton(
                                    selected = timePeriod == period,
                                    onClick = { timePeriod = period }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showTimePeriodDialog = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePeriodDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()) // HACE LA PANTALLA DESPLAZABLE
            ) {
                // Widget del Calendario
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceColor,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, colors.dividerColor) // Borde añadido
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Header con navegación de mes
                        CalendarHeader(
                            currentDate = currentDate,
                            onPreviousMonth = {
                                currentDate = Calendar.getInstance().apply {
                                    time = currentDate.time
                                    add(Calendar.MONTH, -1)
                                }
                            },
                            onNextMonth = {
                                currentDate = Calendar.getInstance().apply {
                                    time = currentDate.time
                                    add(Calendar.MONTH, 1)
                                }
                            },
                            colors = colors
                        )

                        // Contenido del calendario que se puede minimizar
                        AnimatedVisibility(visible = !isExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                // Días de la semana
                                WeekDaysHeader(colors = colors)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Grilla de días del mes
                                CalendarGrid(
                                    currentDate = currentDate,
                                    selectedDate = selectedDate,
                                    daysWithEvents = daysWithEvents,
                                    dateFormat = dateFormat,
                                    onDayClick = { day ->
                                        selectedDate = Calendar.getInstance().apply {
                                            time = currentDate.time
                                            set(Calendar.DAY_OF_MONTH, day)
                                        }
                                    },
                                    colors = colors
                                )
                            }
                        }
                    }
                }

                // Lista de eventos del día
                EventsList(
                    selectedDate = selectedDate,
                    events = eventsForSelectedDay,
                    colors = colors,
                    onCancelClick = { visitId ->
                        visitToCancel = visitId
                        showCancelModal = true
                    },
                    onRescheduleClick = { /* TODO: Implementar reprogramación */ },
                    isExpanded = isExpanded,
                    onExpandClick = { isExpanded = !isExpanded }
                )
            }
        }

        // Modal de cancelación
        if (showCancelModal) {
            CancelVisitModal(
                colors = colors,
                onConfirm = {
                    visitToCancel?.let { id ->
                        visits = visits.map { visit ->
                            if (visit.id == id) {
                                visit.copy(status = VisitStatus.CANCELLED)
                            } else {
                                visit
                            }
                        }
                    }
                    showCancelModal = false
                    visitToCancel = null
                },
                onDismiss = {
                    showCancelModal = false
                    visitToCancel = null
                }
            )
        }
        
        // Barra de búsqueda flotante
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
                        color = materialColors.surface,
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
                                tint = materialColors.onSurface.copy(0.8f),
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
                                textStyle = TextStyle(color = materialColors.onSurface, fontSize = 17.sp),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (searchQuery.isEmpty()) {
                                            Text("Buscar eventos...", color = materialColors.onSurfaceVariant, fontSize = 16.sp)
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
                    color = materialColors.surface,
                    border = BorderStroke(2.5.dp, rainbowBrush),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, "Cerrar", tint = materialColors.onSurface, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }
    }
}

// Composable para el header del calendario con navegación
@Composable
fun CalendarHeader(
    currentDate: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    colors: com.example.myapplication.ui.theme.AppColors
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.accentBlue, shape = RoundedCornerShape(12.dp)) // Fondo azul con bordes redondeados
            .padding(vertical = 4.dp), // Padding vertical
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Mes anterior",
                tint = Color.White // Ícono en blanco
            )
        }

        Text(
            text = "${monthNames[currentDate.get(Calendar.MONTH)]} ${currentDate.get(Calendar.YEAR)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White // Texto en blanco
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Mes siguiente",
                tint = Color.White // Ícono en blanco
            )
        }
    }
}

// Composable para los días de la semana
@Composable
fun WeekDaysHeader(colors: com.example.myapplication.ui.theme.AppColors) {
    val weekDays = listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sa")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondaryColor
            )
        }
    }
}

// Composable para la grilla del calendario
@Composable
fun CalendarGrid(
    currentDate: Calendar,
    selectedDate: Calendar,
    daysWithEvents: Set<String>,
    dateFormat: SimpleDateFormat,
    onDayClick: (Int) -> Unit,
    colors: com.example.myapplication.ui.theme.AppColors
) {
    val daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = Calendar.getInstance().apply {
        time = currentDate.time
        set(Calendar.DAY_OF_MONTH, 1)
    }.get(Calendar.DAY_OF_WEEK) - 1

    val today = Calendar.getInstance()

    Column {
        var dayCounter = 1

        // Calcular número de filas necesarias
        val totalCells = firstDayOfMonth + daysInMonth
        val rows = (totalCells + 6) / 7

        repeat(rows) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = week * 7 + dayOfWeek

                    if (cellIndex < firstDayOfMonth || dayCounter > daysInMonth) {
                        // Celda vacía
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        val day = dayCounter
                        val dateToCheck = Calendar.getInstance().apply {
                            time = currentDate.time
                            set(Calendar.DAY_OF_MONTH, day)
                        }
                        val dateStr = dateFormat.format(dateToCheck.time)

                        val isSelected = isSameDay(dateToCheck, selectedDate)
                        val isToday = isSameDay(dateToCheck, today)
                        val hasEvent = daysWithEvents.contains(dateStr)

                        DayCell(
                            day = day,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasEvent = hasEvent,
                            onClick = { onDayClick(day) },
                            colors = colors
                        )

                        dayCounter++
                    }
                }
            }

            if (week < rows - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// Composable para cada celda del día
@Composable
fun RowScope.DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit,
    colors: com.example.myapplication.ui.theme.AppColors
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> colors.accentBlue
                    isToday -> colors.accentBlue.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.dp else 0.dp,
                color = if (isToday && !isSelected) colors.accentBlue else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    else -> colors.textPrimaryColor
                }
            )

            if (hasEvent) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color(0xFFEF4444))
                )
            }
        }
    }
}

// Composable para la lista de eventos
@Composable
fun EventsList(
    selectedDate: Calendar,
    events: List<TechnicalVisit>,
    colors: com.example.myapplication.ui.theme.AppColors,
    onCancelClick: (String) -> Unit,
    onRescheduleClick: (String) -> Unit,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // --- ENCABEZADO DE LA LISTA DE EVENTOS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Eventos del ${selectedDate.get(Calendar.DAY_OF_MONTH)} de ${monthNames[selectedDate.get(Calendar.MONTH)]}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondaryColor
            )
            Text(
                text = if (isExpanded) "Minimizar" else "Expandir",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accentBlue,
                modifier = Modifier.clickable { onExpandClick() }
            )
        }

        if (events.isNotEmpty()) {
            // --- LISTA DE EVENTOS (NO-LAZY) ---
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                events.forEach { event ->
                    EventCard(
                        event = event,
                        colors = colors,
                        onCancelClick = onCancelClick,
                        onRescheduleClick = onRescheduleClick
                    )
                }
            }
        } else {
            // Estado vacío
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = colors.textSecondaryColor.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No hay visitas programadas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondaryColor
                )
                Text(
                    text = "Acuerda una visita desde el chat",
                    fontSize = 12.sp,
                    color = colors.textSecondaryColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}


// Composable para cada tarjeta de evento
@Composable
fun EventCard(
    event: TechnicalVisit,
    colors: com.example.myapplication.ui.theme.AppColors,
    onCancelClick: (String) -> Unit,
    onRescheduleClick: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, colors.dividerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna de hora
                Column(
                    modifier = Modifier
                        .width(70.dp)
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = event.time,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (event.status == VisitStatus.CANCELLED) {
                            colors.textSecondaryColor
                        } else {
                            colors.textPrimaryColor
                        }
                    )
                    Text(
                        text = "HRS",
                        fontSize = 10.sp,
                        color = colors.textSecondaryColor
                    )
                }

                // Divisor vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(colors.dividerColor)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Información del evento
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event.service,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (event.status == VisitStatus.CANCELLED) {
                                colors.textSecondaryColor
                            } else {
                                colors.textPrimaryColor
                            }
                        )

                        // Badge de estado
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (event.status) {
                                VisitStatus.CONFIRMED -> Color(0xFF10B981).copy(alpha = 0.1f)
                                VisitStatus.PENDING -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                VisitStatus.CANCELLED -> Color(0xFFEF4444).copy(alpha = 0.1f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = when (event.status) {
                                        VisitStatus.CONFIRMED -> Icons.Default.Check
                                        VisitStatus.PENDING -> Icons.Default.DateRange
                                        VisitStatus.CANCELLED -> Icons.Default.Close
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = when (event.status) {
                                        VisitStatus.CONFIRMED -> Color(0xFF10B981)
                                        VisitStatus.PENDING -> Color(0xFFF59E0B)
                                        VisitStatus.CANCELLED -> Color(0xFFEF4444)
                                    }
                                )
                                Text(
                                    text = when (event.status) {
                                        VisitStatus.CONFIRMED -> "Confirmado"
                                        VisitStatus.PENDING -> "Pendiente"
                                        VisitStatus.CANCELLED -> "Cancelado"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (event.status) {
                                        VisitStatus.CONFIRMED -> Color(0xFF10B981)
                                        VisitStatus.PENDING -> Color(0xFFF59E0B)
                                        VisitStatus.CANCELLED -> Color(0xFFEF4444)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Avatar y nombre del proveedor
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(event.avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = event.provider.first().toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = event.provider,
                            fontSize = 12.sp,
                            color = colors.textSecondaryColor
                        )
                    }
                }
                
                // Icono indicador de expandir/colapsar
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = colors.textSecondaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Botones de acción con animación expandible
            AnimatedVisibility(
                visible = isExpanded && event.status != VisitStatus.CANCELLED,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        color = colors.dividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón Reprogramar
                        Button(
                            onClick = { onRescheduleClick(event.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.backgroundColor,
                                contentColor = colors.accentBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reprogramar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Botón Cancelar
                        Button(
                            onClick = { onCancelClick(event.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.backgroundColor,
                                contentColor = Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cancelar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Función auxiliar para comparar días
fun isSameDay(date1: Calendar, date2: Calendar): Boolean {
    return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
            date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
            date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
}

// Modal de confirmación de cancelación
@Composable
fun CancelVisitModal(
    colors: com.example.myapplication.ui.theme.AppColors,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = RoundedCornerShape(24.dp),
            color = colors.surfaceColor,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de alerta
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFEF4444)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Título
                Text(
                    text = "¿Cancelar Visita?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimaryColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = "Esta acción eliminará la visita programada. ¿Estás seguro de que quieres continuar?",
                    fontSize = 14.sp,
                    color = colors.textSecondaryColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Confirmar
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Sí, cancelar visita",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Botón Volver
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.backgroundColor,
                            contentColor = colors.textSecondaryColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Volver atrás",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    CalendarScreen(onBack = {})
}
