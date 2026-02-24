package com.example.myapplication.prestador.ui.calendar

import android.R
import android.app.DatePickerDialog
import android.app.Service
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.appointments.CreateAppointmentDialog
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.utils.getServiceTypeConfig
import com.example.myapplication.prestador.viewmodel.RentalSpacesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*



// Modelo de datos para las citas del prestador
data class Appointment(
    val id: String,
    val clientId: String, // ID del cliente para navegación
    val date: String, // Formato "yyyy-MM-dd"
    val time: String, // Ej: "10:30"
    val service: String,
    val clientName: String,
    val status: AppointmentStatus,
    val avatarColor: Color
)

enum class AppointmentStatus {
    CONFIRMED,    // Confirmada
    PENDING,      // Pendiente
    CANCELLED,    // Cancelada
    COMPLETED     // Completada
}

// Datos de ejemplo (luego los conectaremos con Firebase)
val SAMPLE_APPOINTMENTS = listOf(
    Appointment("apt_maria_001", "cliente_001", "2026-02-21", "15:00",
        "Sesión de corte y color", "María González",
        AppointmentStatus.PENDING,
        Color(0xFF6366F1)),
    Appointment("apt_carlos_001", "cliente_002", "2026-02-24", "10:00",
        "Corte de cabello", "Carlos Rodríguez",
        AppointmentStatus.CONFIRMED, Color(0xFFEC4899)),
    Appointment("apt_ana_001", "cliente_003", "2026-02-25", "14:00",
        "Peinado para evento", "Ana López",
        AppointmentStatus.PENDING,
        Color(0xFF10B981)),
    Appointment("apt_juan_001", "cliente_004", "2026-02-27", "11:30",
        "Corte y barba", "Juan Pérez",
        AppointmentStatus.CONFIRMED, Color(0xFFF59E0B))
)


//pantalla principal del calendario para el prestador

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PrestadorCalendarScreen(
    onNavigateToChat: (clientId: String, clientName: String, newDate: String, newTime: String, appointmentId: String) -> Unit,
    onNavigateToPresupuesto: (appointmentId: String) -> Unit = {},
    onBack: () -> Unit = {},
    appointmentViewModel: com.example.myapplication.prestador.viewmodel.AppointmentViewModel = hiltViewModel(),
    editProfileViewModel: com.example.myapplication.prestador.viewmodel.EditProfileViewModel = hiltViewModel(),
    rentalSpacesViewModel: RentalSpacesViewModel = hiltViewModel(),
    empleadosViewModel: com.example.myapplication.prestador.viewmodel.EmpleadosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // Obtener estado del perfil
    val profileState by editProfileViewModel.profileState.collectAsState()
    
    // Obtener configuración según tipo de servicio del provider (REACTIVO)
    val serviceTypeConfig by remember {
        derivedStateOf {
            when (profileState) {
                is com.example.myapplication.prestador.viewmodel.ProfileState.Success -> {
                    val provider = (profileState as com.example.myapplication.prestador.viewmodel.ProfileState.Success).provider
                    println("🔥 CALENDAR: ServiceType cambió a ${provider.serviceType}")
                    getServiceTypeConfig(ServiceType.fromString(provider.serviceType))
                }
                else -> {
                    println("⚠️ CALENDAR: ProfileState = $profileState")
                    getServiceTypeConfig(ServiceType.PROFESSIONAL) // Default mientras carga
                }
            }
        }
    }
    
    // Paginación infinita para deslizar entre meses
    val indiceInicial = 5000
    val pagerState = rememberPagerState(
        initialPage = indiceInicial,
        pageCount = { 10000 }
    )
    val coroutineScope = rememberCoroutineScope()
    
    // Estado para la fecha actual mostrada (sincronizada con el pager)
    var currentDate by remember {
        mutableStateOf(Calendar.getInstance())
    }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    // Sincronizar currentDate con la página del pager
    LaunchedEffect(pagerState.currentPage) {
        val mesesDiferencia = pagerState.currentPage - indiceInicial
        val newDate = Calendar.getInstance()
        newDate.add(Calendar.MONTH, mesesDiferencia)
        currentDate = newDate
    }
    
    // 🎯 CARGAR CITAS DESDE ROOM DATABASE
    val appointmentsFromDb by appointmentViewModel.appointments.collectAsState()
    
    //Formato de fecha para la comparacion
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    //Filtrar citas del dia seleccionado
    val selectedDateStr = dateFormat.format(selectedDate.time)
    
    // Estado para controlar si la lista de citas está expandida (calendario minimizado)
    var isExpanded by remember { mutableStateOf(true) }
    
    // Estados para el modal de cancelación
    var showCancelDialog by remember { mutableStateOf(false) }
    var appointmentToCancel by remember { mutableStateOf<String?>(null) }

    // Estados para el modal de reprogramación
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var appointmentToReschedule by remember { mutableStateOf<Appointment?>(null) }

    // Estado para el modal de edición
    var showEditDialog by remember { mutableStateOf(false) }
    var appointmentEntityToEdit by remember { mutableStateOf<com.example.myapplication.prestador.data.local.entity.AppointmentEntity?>(null) }

    // Estado para el modal de crear cita
    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScopeSnackbar = rememberCoroutineScope()

    // Obtener providerId del perfil
    val providerId = remember(profileState) {
        when (profileState) {
            is com.example.myapplication.prestador.viewmodel.ProfileState.Success ->
                (profileState as com.example.myapplication.prestador.viewmodel.ProfileState.Success).provider.id
            else -> ""
        }
    }

    // Obtener serviceType actual
    val currentServiceType = remember(profileState) {
        when (profileState) {
            is com.example.myapplication.prestador.viewmodel.ProfileState.Success ->
                ServiceType.fromString((profileState as com.example.myapplication.prestador.viewmodel.ProfileState.Success).provider.serviceType)
            else -> ServiceType.PROFESSIONAL
        }
    }

    // Transformar AppointmentEntity a Appointment (modelo local)
    val appointments = remember(appointmentsFromDb, currentServiceType) {
        appointmentsFromDb
            .filter { it.serviceType == currentServiceType.name }
            .map { entity ->
            Appointment(
                id = entity.id,
                clientId = entity.clientId,
                date = entity.date,
                time = entity.time,
                service = entity.service,
                clientName = entity.clientName,
                status = when (entity.status) {
                    "confirmed" -> AppointmentStatus.CONFIRMED
                    "pending" -> AppointmentStatus.PENDING
                    "cancelled" -> AppointmentStatus.CANCELLED
                    "completed" -> AppointmentStatus.COMPLETED
                    else -> AppointmentStatus.PENDING
                },
                avatarColor = generateColorFromId(entity.clientId)
            )
        }
    }

    //Filtrar citas del dia seleccionado
    val appointmentsForSelectedDay = appointments.filter { it.date == selectedDateStr }
    //dias que tienen citas
    val daysWithAppointments = appointments.filter { it.status != AppointmentStatus.CANCELLED }.map { it.date }.toSet()

    // Cargar espacios si es RENTAL
    LaunchedEffect(providerId, currentServiceType) {
        if (currentServiceType == ServiceType.RENTAL && providerId.isNotBlank()) {
            rentalSpacesViewModel.setProviderId(providerId)
        }
    }

    val rentalSpaces by rentalSpacesViewModel.rentalSpaces.collectAsState()
    val availableSpaces = remember(rentalSpaces) {
        rentalSpaces.filter { it.isActive }.map { it.id to it.name }
    }

    //Cargar slots cuando se abre el dialogo y es professional
    val availableSlots by appointmentViewModel.availabilitySlots.collectAsState()
    LaunchedEffect(showCreateDialog,
        currentServiceType, selectedDateStr) {
        if (showCreateDialog && currentServiceType == ServiceType.PROFESSIONAL && providerId.isNotBlank()){
            appointmentViewModel.loadAvailabilitySlots(providerId, selectedDateStr)

    }
    }

    //Cargar empleados para TECHNICAL
    val empleadosState by empleadosViewModel.uiState.collectAsState()
    val availableEmployees = remember(empleadosState) {
        when (val state = empleadosState) {
            is com.example.myapplication.prestador.viewmodel.EmpleadosUiState.Success ->
                state.empleados.filter { it.activo }.map { it.id to it.nombreCompleto() }
            else -> emptyList()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFFFF6B35),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = serviceTypeConfig.createAction
                )
            }
        },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF6B35),
                                Color(0xFFFF9F66)
                            )
                        )
                    )
            ) {
                TopAppBar(
                    title = {
                        Text(
                            serviceTypeConfig.calendarTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF8F3))
        ) {
            // Header del calendario con navegación por flechas y mes/año
            CalendarHeader(
                currentDate = currentDate,
                onPreviousMonth = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNextMonth = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            )
            
            // Contenido del calendario que se puede minimizar
            AnimatedVisibility(visible = !isExpanded) {
                Column {
                    // HorizontalPager para deslizar entre meses
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) { page ->
                        // Calcular el mes basado en la página
                        val mesesDiferencia = page - indiceInicial
                        val fechaMes = Calendar.getInstance()
                        fechaMes.add(Calendar.MONTH, mesesDiferencia)
                        
                        // Renderizar el grid del mes
                        CalendarGrid(
                            currentDate = fechaMes,
                            selectedDate = selectedDate,
                            daysWithAppointments = daysWithAppointments,
                            onDateSelected = { newDate ->
                                selectedDate = newDate
                                isExpanded = true // Minimizar calendario automáticamente
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Lista de citas (todas del tipo, el componente filtra internamente)
            AppointmentsList(
                appointments = appointments,
                selectedDate = selectedDate,
                isExpanded = isExpanded,
                serviceTypeConfig = serviceTypeConfig,
                onExpandClick = { isExpanded = !isExpanded },
                onReschedule = { appointmentId ->
                    val entity = appointmentsFromDb.find { it.id == appointmentId }
                    if (entity != null) {
                        appointmentEntityToEdit = entity
                        showEditDialog = true
                    }
                },

                onCancel = { appointmentId ->
                    appointmentToCancel = appointmentId
                    showCancelDialog = true
                },
                onConfirm = { appointmentId ->
                    coroutineScope.launch {
                        appointmentViewModel.confirmAppointment(appointmentId)
                        // Programar recordatorios al confirmar
                        appointmentsFromDb.find { it.id == appointmentId }?.let {
                            com.example.myapplication.prestador.utils.AppointmentReminderScheduler.schedule(context, it)
                        }
                    }
                },
                onComplete = { appointmentId ->
                    coroutineScope.launch {
                        appointmentViewModel.completeAppointment(appointmentId)
                        com.example.myapplication.prestador.utils.AppointmentReminderScheduler.cancel(context, appointmentId)
                    }
                },
                onGenerarPresupuesto = { appointmentId, _ ->
                    onNavigateToPresupuesto(appointmentId)
                }
            )
        }
    }
    
    // Diálogo de confirmación de cancelación
    if (showCancelDialog) {
        CancelAppointmentDialog(
            serviceTypeConfig = serviceTypeConfig,
            onConfirm = {
                appointmentToCancel?.let { id ->
                    coroutineScope.launch {
                        appointmentViewModel.cancelAppointment(id)
                        com.example.myapplication.prestador.utils.AppointmentReminderScheduler.cancel(context, id)
                    }
                }
                showCancelDialog = false
                appointmentToCancel = null
            },
            onDismiss = {
                showCancelDialog = false
                appointmentToCancel = null
            }
        )
    }

    // Diálogo de reprogramación
    if (showRescheduleDialog && appointmentToReschedule != null) {
        RescheduleAppointmentDialog(
            appointment = appointmentToReschedule!!,
            onDismiss = {
                showRescheduleDialog = false
                appointmentToReschedule = null
            },
            onConfirm = { newDate, newTime ->
                println("🟣 Reprogramación confirmada - Nueva fecha: $newDate, hora: $newTime")
                println("🟣 Navegando al chat con nueva fecha/hora")
                println("🟣 ClientId: ${appointmentToReschedule?.clientId}, Nombre: ${appointmentToReschedule?.clientName}")
                println("🟣 AppointmentId ORIGINAL: ${appointmentToReschedule?.id}")
                
                // Navegar al chat con los datos de la reprogramación
                onNavigateToChat(
                    appointmentToReschedule!!.clientId,
                    appointmentToReschedule!!.clientName,
                    newDate,
                    newTime,
                    appointmentToReschedule!!.id  // ✅ Pasar ID original de la cita
                )
                
                showRescheduleDialog = false
                appointmentToReschedule = null
            }
        )
    }

    // Diálogo de editar cita existente
    if (showEditDialog && appointmentEntityToEdit != null) {
        val entity = appointmentEntityToEdit!!
        CreateAppointmentDialog(
            serviceType = currentServiceType,
            onDismiss = { showEditDialog = false; appointmentEntityToEdit = null },
            onConfirm = { clientName, service, date, time, duration, rentalSpaceId, scheduleId, notes, assignedEmployeeIds, peopleCount ->
                val updated = entity.copy(
                    clientName = clientName,
                    service = service,
                    date = date,
                    time = time,
                    duration = duration,
                    rentalSpaceId = rentalSpaceId,
                    scheduleId = scheduleId,
                    notes = notes,
                    assignedEmployeeIds = assignedEmployeeIds,
                    peopleCount = peopleCount,
                    status = "pending",
                    updatedAt = System.currentTimeMillis()
                )
                appointmentViewModel.updateAppointment(updated)

                // Notificar al cliente por chat
                val rescheduleMessage = com.example.myapplication.prestador.data.model.Message(
                    id = "msg_reschedule_${System.currentTimeMillis()}_${entity.clientId}",
                    text = "Propuesta de cambio de cita",
                    timestamp = System.currentTimeMillis(),
                    isFromCurrentUser = true,
                    type = com.example.myapplication.prestador.data.model.Message.MessageType.APPOINTMENT,
                    appointmentDate = date,
                    appointmentTime = time,
                    appointmentId = entity.id,
                    appointmentStatus = com.example.myapplication.prestador.data.model.Message.AppointmentProposalStatus.PENDING,
                    appointmentTitle = service
                )
                com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.addMessage(entity.clientId, rescheduleMessage)

                showEditDialog = false
                appointmentEntityToEdit = null
            },
            colors = com.example.myapplication.prestador.ui.theme.getPrestadorColors(),
            availableSpaces = availableSpaces,
            availableSlots = availableSlots,
            availableEmployees = if (currentServiceType == ServiceType.TECHNICAL) availableEmployees else emptyList(),
            initialClientName = entity.clientName,
            initialService = entity.service,
            initialDate = entity.date,
            initialTime = entity.time,
            initialDuration = entity.duration,
            initialNotes = entity.notes,
            initialPeopleCount = entity.peopleCount ?: 1,
            isEditMode = true
        )
    }

    // Diálogo de crear nueva cita/servicio/reserva
    if (showCreateDialog) {
        CreateAppointmentDialog(
            serviceType = currentServiceType,
            onDismiss = { showCreateDialog = false },
            onConfirm = { clientName, service, date, time, duration, rentalSpaceId, scheduleId, notes, assignedEmployeeIds, peopleCount ->
                val newAppointment = AppointmentEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    clientId = "",
                    clientName = clientName,
                    providerId = providerId,
                    service = service,
                    date = date,
                    time = time,
                    duration = duration,
                    status = "pending",
                    notes = notes,
                    serviceType = currentServiceType.name,
                    rentalSpaceId = rentalSpaceId,
                    scheduleId = scheduleId,
                    assignedEmployeeIds = assignedEmployeeIds,
                    peopleCount = peopleCount
                )
                appointmentViewModel.validateAndSave(
                    appointment = newAppointment,
                    serviceType = currentServiceType,
                    onSuccess = { showCreateDialog = false },
                    onError = { msg ->
                        coroutineScopeSnackbar.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                )
            },
            colors = getPrestadorColors(),
            availableSpaces = availableSpaces,
            availableSlots = availableSlots,
            availableEmployees = if (currentServiceType == ServiceType.TECHNICAL)
            availableEmployees else emptyList()
        )
    }
}

//Header del calendario con navegacion de mes

@Composable
fun CalendarHeader(
    currentDate: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Mes anterior",
                tint = Color(0xFFFF6B35)
            )
        }

        Text(
            text = monthFormat.format(currentDate.time).capitalize(Locale.getDefault()),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Mes siguiente",
                tint = Color(0xFFFF6B35)
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentDate: Calendar,
    selectedDate: Calendar,
    daysWithAppointments: Set<String>,
    onDateSelected: (Calendar) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    //DIAS DE LA SEMANA
    val daysOfWeek = listOf("D", "L", "M", "M", "J", "V", "S")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        //Header de dias de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid de días
        val daysInMonth = getDaysInMonth(currentDate)
        val rows = daysInMonth.chunked(7)

        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { dayInfo ->
                    DayCell(
                        dayInfo = dayInfo,
                        selectedDate = selectedDate,
                        daysWithAppointments = daysWithAppointments,
                        dateFormat = dateFormat,
                        onDateSelected = onDateSelected
                    )
                }
                // Rellenar espacios vacíos al final
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/**
 * Data class para información del día
 */
data class DayInfo(
    val date: Calendar,
    val dayNumber: Int,
    val isCurrentMonth: Boolean
)

/**
 * Obtiene todos los días del mes incluyendo días del mes anterior/siguiente
 */
fun getDaysInMonth(date: Calendar): List<DayInfo> {
    val days = mutableListOf<DayInfo>()
    val calendar = date.clone() as Calendar
    
    // Ir al primer día del mes
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    
    // Agregar días del mes anterior
    val prevMonth = calendar.clone() as Calendar
    prevMonth.add(Calendar.MONTH, -1)
    val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    for (i in (daysInPrevMonth - firstDayOfWeek + 1)..daysInPrevMonth) {
        val day = prevMonth.clone() as Calendar
        day.set(Calendar.DAY_OF_MONTH, i)
        days.add(DayInfo(day, i, false))
    }
    
    // Agregar días del mes actual
    val daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 1..daysInCurrentMonth) {
        val day = calendar.clone() as Calendar
        day.set(Calendar.DAY_OF_MONTH, i)
        days.add(DayInfo(day, i, true))
    }
    
    // Agregar días del mes siguiente
    val nextMonth = calendar.clone() as Calendar
    nextMonth.add(Calendar.MONTH, 1)
    val remainingDays = 42 - days.size // 6 semanas completas
    for (i in 1..remainingDays) {
        val day = nextMonth.clone() as Calendar
        day.set(Calendar.DAY_OF_MONTH, i)
        days.add(DayInfo(day, i, false))
    }
    
    return days
}

/**
 * Celda individual del día en el calendario
 */
@Composable
fun RowScope.DayCell(
    dayInfo: DayInfo,
    selectedDate: Calendar,
    daysWithAppointments: Set<String>,
    dateFormat: SimpleDateFormat,
    onDateSelected: (Calendar) -> Unit
) {
    val isSelected = dateFormat.format(dayInfo.date.time) == dateFormat.format(selectedDate.time)
    val hasAppointments = daysWithAppointments.contains(dateFormat.format(dayInfo.date.time))
    val isToday = dateFormat.format(dayInfo.date.time) == dateFormat.format(Calendar.getInstance().time)
    
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> Color(0xFFFF6B35)
                    isToday -> Color(0xFFFFE4DB)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = dayInfo.isCurrentMonth) {
                onDateSelected(dayInfo.date)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayInfo.dayNumber.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    !dayInfo.isCurrentMonth -> Color(0xFFD1D5DB)
                    isToday -> Color(0xFFFF6B35)
                    else -> Color(0xFF1F2937)
                }
            )
            
            if (hasAppointments && dayInfo.isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color(0xFFFF6B35))
                )
            }
        }
    }
}

//lista de citas del dia seleccionado

@Composable
fun AppointmentsList(
    appointments: List<Appointment>,
    selectedDate: Calendar,
    isExpanded: Boolean,
    serviceTypeConfig: com.example.myapplication.prestador.utils.ServiceTypeConfig,
    onExpandClick: () -> Unit,
    onReschedule: (String) -> Unit,
    onCancel: (String) -> Unit,
    onConfirm: (String) -> Unit = {},
    onComplete: (String) -> Unit = {},
    onGenerarPresupuesto: (appointmentId: String, clientName: String) -> Unit = { _, _ -> }
) {
    val dateFormat = SimpleDateFormat("d 'de' MMMM", Locale.getDefault())
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

    // Separar en próximas (pending/confirmed con fecha >= hoy) e historial (completed/cancelled o pasadas)
    val upcoming = appointments.filter {
        (it.status == AppointmentStatus.PENDING || it.status == AppointmentStatus.CONFIRMED) && it.date >= today
    }.sortedBy { it.date + it.time }

    val history = appointments.filter {
        it.status == AppointmentStatus.COMPLETED || it.status == AppointmentStatus.CANCELLED ||
        ((it.status == AppointmentStatus.PENDING || it.status == AppointmentStatus.CONFIRMED) && it.date < today)
    }.sortedByDescending { it.date + it.time }

    var selectedTab by remember { mutableStateOf(0) }
    val displayList = if (selectedTab == 0) upcoming else history

    // Estado para controlar qué cita está expandida (solo una a la vez)
    var expandedAppointmentId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header con fecha del día seleccionado y toggle expandir
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${selectedDate.get(Calendar.DAY_OF_MONTH)} de ${monthNames[selectedDate.get(Calendar.MONTH)]}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = if (appointments.isEmpty()) {
                        "Sin ${serviceTypeConfig.appointmentsName}"
                    } else {
                        "${appointments.size} ${if (appointments.size == 1) serviceTypeConfig.appointmentName else serviceTypeConfig.appointmentsName}"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280)
                )
            }
            Text(
                text = if (isExpanded) "Minimizar" else "Expandir",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B35),
                modifier = Modifier.clickable { onExpandClick() }
            )
        }

        // Tabs Próximas / Historial
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Próximas" to upcoming.size, "Historial" to history.size).forEachIndexed { index, (label, count) ->
                val isSelected = selectedTab == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = index; expandedAppointmentId = null },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFFF6B35) else Color(0xFFF3F4F6)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(alpha = 0.3f) else Color(0xFFE5E7EB)
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * direction } + fadeIn(tween(250))) togetherWith
                (slideOutHorizontally { -it * direction } + fadeOut(tween(200)))
            },
            label = "tab_content"
        ) { tab ->
            val list = if (tab == 0) upcoming else history
            if (list.isEmpty()) {
                // sin citas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(2.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (tab == 0) "📅" else "🗂️", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (tab == 0)
                                "No hay ${serviceTypeConfig.appointmentsName} próximas"
                            else
                                "Sin historial de ${serviceTypeConfig.appointmentsName}",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(list) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            isExpanded = expandedAppointmentId == appointment.id,
                            onToggleExpand = {
                                expandedAppointmentId = if (expandedAppointmentId == appointment.id) null else appointment.id
                            },
                            serviceTypeConfig = serviceTypeConfig,
                            onReschedule = onReschedule,
                            onCancel = onCancel,
                            onConfirm = onConfirm,
                            onComplete = onComplete,
                            onGenerarPresupuesto = onGenerarPresupuesto
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta individual de cita
 */
@Composable
fun AppointmentCard(
    appointment: Appointment,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    serviceTypeConfig: com.example.myapplication.prestador.utils.ServiceTypeConfig,
    onReschedule: (String) -> Unit = {},
    onCancel: (String) -> Unit = {},
    onConfirm: (String) -> Unit = {},
    onComplete: (String) -> Unit = {},
    onGenerarPresupuesto: (appointmentId: String, clientName: String) -> Unit = { _, _ -> }
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del cliente
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(appointment.avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appointment.clientName.first().uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Información de la cita
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.clientName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = appointment.service,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🕐",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.time,
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                
                // Badge de estado
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (appointment.status) {
                        AppointmentStatus.CONFIRMED -> Color(0xFF10B981).copy(alpha = 0.1f)
                        AppointmentStatus.PENDING -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                        AppointmentStatus.CANCELLED -> Color(0xFFEF4444).copy(alpha = 0.1f)
                        AppointmentStatus.COMPLETED -> Color(0xFF6366F1).copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = when (appointment.status) {
                            AppointmentStatus.CONFIRMED -> serviceTypeConfig.confirmedStatus
                            AppointmentStatus.PENDING -> serviceTypeConfig.pendingStatus
                            AppointmentStatus.CANCELLED -> serviceTypeConfig.cancelledStatus
                            AppointmentStatus.COMPLETED -> "Completada"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (appointment.status) {
                            AppointmentStatus.CONFIRMED -> Color(0xFF10B981)
                            AppointmentStatus.PENDING -> Color(0xFFF59E0B)
                            AppointmentStatus.CANCELLED -> Color(0xFFEF4444)
                            AppointmentStatus.COMPLETED -> Color(0xFF6366F1)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Botones de acción (solo si está expandida y no está cancelada ni completada)
            AnimatedVisibility(
                visible = isExpanded && appointment.status != AppointmentStatus.CANCELLED && appointment.status != AppointmentStatus.COMPLETED,
                enter = expandVertically(
                    animationSpec = tween(300),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    animationSpec = tween(300),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Column {
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val btnPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        val btnModifier = Modifier.weight(1f).height(36.dp)

                        // Botón Confirmar (solo si está pendiente)
                        if (appointment.status == AppointmentStatus.PENDING) {
                            Button(
                                onClick = { onConfirm(appointment.id) },
                                modifier = btnModifier,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = btnPadding
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Confirmar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Botón Completar (solo si está confirmada)
                        if (appointment.status == AppointmentStatus.CONFIRMED) {
                            Button(
                                onClick = { onComplete(appointment.id) },
                                modifier = btnModifier,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = btnPadding
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Completar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Botón Presupuesto (pending o confirmed)
                        Button(
                            onClick = { onGenerarPresupuesto(appointment.id, appointment.clientName) },
                            modifier = btnModifier,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = btnPadding
                        ) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "Presupuesto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Botón Reprogramar
                        Button(
                            onClick = { onReschedule(appointment.id) },
                            modifier = btnModifier,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = btnPadding
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = serviceTypeConfig.rescheduleAction, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Botón Cancelar
                        OutlinedButton(
                            onClick = { onCancel(appointment.id) },
                            modifier = btnModifier,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = btnPadding
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = serviceTypeConfig.cancelAction, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo de confirmación para cancelar cita
 */
@Composable
fun CancelAppointmentDialog(
    serviceTypeConfig: com.example.myapplication.prestador.utils.ServiceTypeConfig,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
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
                    text = "¿${serviceTypeConfig.cancelAction}?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = "Esta acción eliminará la ${serviceTypeConfig.appointmentName} programada. ¿Estás seguro de que quieres continuar?",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
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
                            text = "Sí, ${serviceTypeConfig.cancelAction.lowercase()}",
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
                            containerColor = Color(0xFFF3F4F6),
                            contentColor = Color(0xFF6B7280)
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


// --- COLORES DEL TEMA (App Prestador) ---
val OrangePrimary = Color(0xFFFF6B35)  // Color principal naranja
val OrangeLight = Color(0xFFFF9F66)    // Naranja claro
val OrangeBackground = Color(0xFFFFF8F3)  // Fondo claro
val Gray800 = Color(0xFF1F2937)
val Gray500 = Color(0xFF6B7280)
val Gray400 = Color(0xFF9CA3AF)
val Green100 = Color(0xFFDCFCE7)
val Green600 = Color(0xFF16A34A)

// Componente de animación de éxito
@Composable
fun PropuestaEnviadaView(
    onDismiss: () -> Unit
) {
    // Temporizador de 2 segundos
    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    // Animación de rebote
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.95f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Icono animado
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .size(80.dp)
                    .background(Green100, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Éxito",
                    tint = Green600,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "Cita Reprogramada",
                color = Gray800,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "La cita ha sido actualizada correctamente.\nVolviendo al calendario...",
                color = Gray500,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// Diálogo para reprogramar una cita
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleAppointmentDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // Estados para fecha y hora
    var newDate by remember { mutableStateOf(appointment.date) }
    var newTime by remember { mutableStateOf(appointment.time) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Estados para mostrar pickers personalizados
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Función para confirmar
    fun confirmReschedule() {
        isSaving = true
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            isSaving = false
            showSuccess = true
        }, 500)
    }
    
    // Solo mostrar Dialog si no está mostrando éxito
    if (!showSuccess) {
        // Dialog popup centrado
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reprogramar Cita",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Gray500)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Información del cliente
                Text(
                    text = "Cliente: ${appointment.clientName}",
                    fontSize = 14.sp,
                    color = Gray500
                )
                Text(
                    text = "Servicio: ${appointment.service}",
                    fontSize = 14.sp,
                    color = Gray500
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Input: Nueva Fecha
                Text(
                    text = "NUEVA FECHA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newDate,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, null, tint = OrangePrimary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color(0xFFE2E8F0),
                        disabledContainerColor = OrangeBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = "Seleccionado: ${newDate.split("-").reversed().joinToString("/")}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Input: Nueva Hora
                Text(
                    text = "NUEVA HORA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newTime,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    trailingIcon = {
                        Text("🕐", fontSize = 20.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color(0xFFE2E8F0),
                        disabledContainerColor = OrangeBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón Confirmar
                Button(
                    onClick = { confirmReschedule() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving,
                    elevation = ButtonDefaults.buttonElevation(10.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Confirmar Cambio",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    }
    
    // Picker de Fecha Personalizado
    if (showDatePicker && !showSuccess) {
        CustomDatePickerDialog(
            initialDate = newDate,
            onDateSelected = { selectedDate ->
                newDate = selectedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Picker de Hora Personalizado
    if (showTimePicker && !showSuccess) {
        CustomTimePickerDialog(
            initialTime = newTime,
            onTimeSelected = { selectedTime ->
                newTime = selectedTime
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
    
    // Animación de éxito (se muestra encima de todo)
    if (showSuccess) {
        PropuestaEnviadaView(
            onDismiss = {
                showSuccess = false
                onConfirm(newDate, newTime)
            }
        )
    }
}

// Picker de Fecha Personalizado con diseño moderno
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            val parts = initialDate.split("-")
            val calendar = Calendar.getInstance()
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            calendar.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH) + 1
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        val dateStr = String.format("%04d-%02d-%02d", year, month, day)
                        onDateSelected(dateStr)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                )
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Gray500)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White,
            selectedDayContainerColor = OrangePrimary,
            todayDateBorderColor = OrangePrimary,
            todayContentColor = OrangePrimary
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = OrangePrimary,
                todayDateBorderColor = OrangePrimary,
                todayContentColor = OrangePrimary,
                selectedDayContentColor = Color.White
            )
        )
    }
}

// Picker de Hora Personalizado con diseño moderno
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timeParts = initialTime.split(":")
    val initialHour = if (timeParts.isNotEmpty()) timeParts[0].toIntOrNull() ?: 9 else 9
    val initialMinute = if (timeParts.size > 1) timeParts[1].toIntOrNull() ?: 0 else 0
    
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar Hora",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = OrangeBackground,
                        selectorColor = OrangePrimary,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Gray800,
                        timeSelectorSelectedContainerColor = OrangePrimary,
                        timeSelectorUnselectedContainerColor = OrangeBackground,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = Gray800
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", color = Gray500)
                    }
                    
                    Button(
                        onClick = {
                            val hour = timePickerState.hour.toString().padStart(2, '0')
                            val minute = timePickerState.minute.toString().padStart(2, '0')
                            onTimeSelected("$hour:$minute")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary
                        )
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}


// Función helper para generar color único basado en clientId
private fun generateColorFromId(clientId: String): Color {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFFEC4899), // Pink
        Color(0xFF10B981), // Green
        Color(0xFFF59E0B), // Amber
        Color(0xFF8B5CF6), // Purple
        Color(0xFF06B6D4), // Cyan
        Color(0xFFEF4444), // Red
        Color(0xFF14B8A6)  // Teal
    )
    val hash = clientId.hashCode()
    val index = kotlin.math.abs(hash) % colors.size
    return colors[index]
}
