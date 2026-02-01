package com.example.myapplication.Client

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.getAppColors
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLicScreen(
    onBack: () -> Unit,
) {
    // We will now use the sample data directly
    val categories = CategorySampleDataFalso.categories
    val user = UserSampleDataFalso.currentUser // Get a sample user

    CrearLicScreenContent(
        onBack = onBack,
        categories = categories,
        user = user
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLicScreenContent(
    onBack: () -> Unit,
    categories: List<CategoryItem>,
    user: UserFalso
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFakeAd by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showHeadsUpNotification by remember { mutableStateOf(false) }

    LaunchedEffect(showHeadsUpNotification) {
        if (showHeadsUpNotification) {
            delay(3000)
            showHeadsUpNotification = false
            showFakeAd = true
        }
    }


    //Manejar  boton atras del sistema
    BackHandler {
        if (showFakeAd) {
            // If the ad is showing, navigate back home.
            onBack()
        } else {
            onBack()
        }
    }

    // Estados del formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showInfoBanner by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val durationInDays = remember(startDate, endDate) {
        if (startDate != null && endDate != null) {
            val diff = endDate!! - startDate!!
            TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        } else {
            0
        }
    }

    // Estados para categoría
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var categorySearchQuery by remember { mutableStateOf("") }
    var isCategorySearchFocused by remember { mutableStateOf(false) }

    // Estados para dirección
    var address by remember { mutableStateOf(user.personalAddresses.firstOrNull()?.fullString() ?: "") }
    var locationExpanded by remember { mutableStateOf(false) }


    // Colores adaptativos
    val colors = getAppColors()

    // Filtrar categorías según búsqueda
    val filteredCategories = remember(categorySearchQuery, categories) {
        if (categorySearchQuery.isBlank()) {
            emptyList()
        } else {
            categories.filter { it.name.startsWith(categorySearchQuery, ignoreCase = true) }
        }
    }

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        selectedImages = uris
    }

    if (showFakeAd) {
        FakeGoogleAdScreen(onClose = onBack)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (title.isBlank() || description.isBlank() || selectedCategory == null || startDate == null || endDate == null || address.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Por favor, completa todos los campos obligatorios.")
                                }
                            } else {
                                showBottomSheet = true
                            }
                        },
                        icon = { Icon(Icons.Default.Add, "Publicar Licitación") },
                        text = { Text("Publicar Licitación") },
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    )
                },
                floatingActionButtonPosition = FabPosition.End,
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.backgroundColor)
                        .padding(paddingValues)
                ) {
                    // SECCIÓN 1: HEADER
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gavel, contentDescription = "Licitación")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nueva Licitación",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimaryColor
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = colors.textSecondaryColor
                                )
                            }
                        },
                        actions = {
                            Box {
                                IconButton(onClick = { showInfoBanner = true }) {
                                    Icon(Icons.Default.Info, contentDescription = "Información")
                                }
                                DropdownMenu(
                                    expanded = showInfoBanner,
                                    onDismissRequest = { showInfoBanner = false },
                                    modifier = Modifier.width(300.dp)
                                ) {
                                    InfoBannerContent()
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.surfaceColor
                        )
                    )

                    // SECCIÓN 2: CONTENIDO SCROLLABLE
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(24.dp)
                    ) {
                        // SECCIÓN DATOS PERSONALES
                        Text(
                            text = "Datos Personales",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = user.name,
                                onValueChange = {},
                                label = { Text("Nombre") },
                                readOnly = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = user.lastName,
                                onValueChange = {},
                                label = { Text("Apellido") },
                                readOnly = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Dirección") },
                            trailingIcon = {
                                Box {
                                    IconButton(onClick = { locationExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Cambiar dirección")
                                    }
                                    DropdownMenu(
                                        expanded = locationExpanded,
                                        onDismissRequest = { locationExpanded = false }
                                    ) {
                                        user.personalAddresses.forEach { addr ->
                                            DropdownMenuItem(
                                                text = { Text("Casa: ${addr.fullString()}") },
                                                onClick = {
                                                    address = addr.fullString()
                                                    locationExpanded = false
                                                }
                                            )
                                        }
                                        user.companies.forEach { company ->
                                            DropdownMenuItem(
                                                text = { Text("Trabajo: ${company.casaCentral.fullString()}") },
                                                onClick = {
                                                    address = company.casaCentral.fullString()
                                                    locationExpanded = false
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("Ubicación actual") },
                                            onClick = {
                                                address = "Ubicación en tiempo real" // Placeholder
                                                locationExpanded = false
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                       // HorizontalDivider()
                        Spacer(modifier = Modifier.height(5.dp))

                        // SECCIÓN LICITACIÓN
                        Text(
                            text = "Licitación",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column {
                            OutlinedTextField(
                                value = selectedCategory?.name ?: categorySearchQuery,
                                onValueChange = { newValue ->
                                    categorySearchQuery = newValue
                                    isCategorySearchFocused = true
                                    selectedCategory = null
                                },
                                label = { Text("Categoría del Proyecto") },
                                leadingIcon = {
                                    if (selectedCategory != null) {
                                        Text(selectedCategory!!.icon, fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (selectedCategory != null || categorySearchQuery.isNotEmpty()) {
                                        IconButton(onClick = {
                                            selectedCategory = null
                                            categorySearchQuery = ""
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar categoría"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        isCategorySearchFocused = focusState.isFocused
                                    },
                                singleLine = true
                            )
                            if (isCategorySearchFocused && categorySearchQuery.isNotEmpty() && filteredCategories.isNotEmpty() && selectedCategory == null) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                ) {
                                    items(filteredCategories) { category ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCategory = category
                                                    categorySearchQuery = ""
                                                    isCategorySearchFocused = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(category.icon, fontSize = 20.sp)
                                            Text(
                                                text = category.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textPrimaryColor
                                            )
                                        }
                                        if (category != filteredCategories.last()) {
                                            HorizontalDivider(
                                                color = colors.dividerColor,
                                                thickness = 0.5.dp,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo: Título del Proyecto
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Título del Proyecto") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo: Descripción Detallada
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción Detallada") },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fechas de Inicio y Fin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Fecha de Inicio
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = startDate?.let { dateFormatter.format(Date(it)) } ?: "",
                                    onValueChange = {},
                                    label = { Text("Fecha de Inicio") },
                                    readOnly = true,
                                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                            }

                            // Fecha de Fin
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = endDate?.let { dateFormatter.format(Date(it)) } ?: "",
                                    onValueChange = {},
                                    label = { Text("Fecha de Fin") },
                                    readOnly = true,
                                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true })
                            }
                        }
                        if (durationInDays > 0) {
                            Text(
                                text = "Duración: $durationInDays días",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Subir fotos
                        Text(
                            text = "Fotos de referencia (opcional)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón para agregar fotos
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surfaceColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, colors.dividerColor, RoundedCornerShape(16.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.textSecondaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Subir fotos (máx. 5)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondaryColor
                                )
                            }
                        }

                        // Preview de imágenes seleccionadas
                        if (selectedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(selectedImages) { uri ->
                                    Box {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Foto seleccionada",
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, colors.dividerColor, RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Botón para eliminar
                                        IconButton(
                                            onClick = {
                                                selectedImages = selectedImages.filter { it != uri }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(colors.accentRed, shape = RoundedCornerShape(12.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar foto",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            HeadsUpNotification(
                visible = showHeadsUpNotification,
                title = "¡Licitación Publicada!",
                description = "Tu proyecto ya está visible para los profesionales."
            )
        }
        
        // DatePickers con tema adaptativo
        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedDate = datePickerState.selectedDateMillis
                            val today = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis

                            if (selectedDate != null && selectedDate < today) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("La fecha de inicio no puede ser anterior a hoy.")
                                }
                            } else {
                                startDate = selectedDate
                                showStartDatePicker = false
                            }
                        }
                    ) {
                        Text("Aceptar", color = colors.accentBlue)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) {
                        Text("Cancelar", color = colors.textSecondaryColor)
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = colors.surfaceColor
                )
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = colors.surfaceColor,
                        titleContentColor = colors.textPrimaryColor,
                        headlineContentColor = colors.textPrimaryColor,
                        weekdayContentColor = colors.textSecondaryColor,
                        subheadContentColor = colors.textPrimaryColor,
                        yearContentColor = colors.textPrimaryColor,
                        currentYearContentColor = colors.accentBlue,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = colors.accentBlue,
                        dayContentColor = colors.textPrimaryColor,
                        selectedDayContentColor = Color.White,
                        selectedDayContainerColor = colors.accentBlue,
                        todayContentColor = colors.accentBlue,
                        todayDateBorderColor = colors.accentBlue
                    )
                )
            }
        }

        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedDate = datePickerState.selectedDateMillis
                            if (startDate != null && selectedDate != null) {
                                val diff = selectedDate - startDate!!
                                val duration = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                                if (duration < 1 || duration > 31) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("La duración debe ser entre 1 y 31 días.")
                                    }
                                } else {
                                    endDate = selectedDate
                                    showEndDatePicker = false
                                }
                            } else {
                                endDate = selectedDate
                                showEndDatePicker = false
                            }
                        }
                    ) {
                        Text("Aceptar", color = colors.accentBlue)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("Cancelar", color = colors.textSecondaryColor)
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = colors.surfaceColor
                )
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = colors.surfaceColor,
                        titleContentColor = colors.textPrimaryColor,
                        headlineContentColor = colors.textPrimaryColor,
                        weekdayContentColor = colors.textSecondaryColor,
                        subheadContentColor = colors.textPrimaryColor,
                        yearContentColor = colors.textPrimaryColor,
                        currentYearContentColor = colors.accentBlue,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = colors.accentBlue,
                        dayContentColor = colors.textPrimaryColor,
                        selectedDayContentColor = Color.White,
                        selectedDayContainerColor = colors.accentBlue,
                        todayContentColor = colors.accentBlue,
                        todayDateBorderColor = colors.accentBlue
                    )
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Confirmar Licitación", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¿Estás seguro de que quieres publicar esta licitación?")
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    sheetState.hide()
                                }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                        showHeadsUpNotification = true
                                    }
                                }
                            }
                        ) {
                            Text("Aceptar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FakeGoogleAdScreen(onClose: () -> Unit) {
    var timer by remember { mutableIntStateOf(5) }
    var isButtonEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timer > 0) {
            delay(1000)
            timer--
        }
        isButtonEnabled = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = isButtonEnabled, onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "¡Felicidades!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tu licitación ha sido publicada. Para llegar a más profesionales, mira este anuncio.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Gray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aquí iría el anuncio de Google Ads")
                }
                Button(onClick = onClose, enabled = isButtonEnabled) {
                    Text(if (isButtonEnabled) "Cerrar" else "Cerrar en $timer...")
                }
            }
        }
    }
}

@Composable
fun HeadsUpNotification(
    visible: Boolean,
    title: String,
    description: String
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBannerContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEEF2FF), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cómo crear una licitación",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3730A3)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "1. Datos Personales:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF4338CA)
        )
        Text(
            text = "Asegúrate de que tu nombre y dirección sean correctos. Estos datos se compartirán con el profesional que elijas.",
            fontSize = 12.sp,
            color = Color(0xFF4338CA),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "2. Licitación:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF4338CA)
        )
        Text(
            text = "Selecciona la categoría, un título claro y una descripción detallada para que los profesionales entiendan qué necesitas.",
            fontSize = 12.sp,
            color = Color(0xFF4338CA),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "3. Fechas y Fotos:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF4338CA)
        )
        Text(
            text = "Elige un rango de fechas, en el cual el proyecto estara activo para RECIBIR Preupuestos del trabajo que intentas Realizar. Sube fotos de referencia. ¡Esto ayuda a recibir presupuestos más precisos!",
            fontSize = 12.sp,
            color = Color(0xFF4338CA),
            lineHeight = 16.sp
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CrearLicScreenPreview() {
    MyApplicationTheme {
        CrearLicScreenContent(
            onBack = {},
            categories = CategorySampleDataFalso.categories,
            user = UserSampleDataFalso.currentUser
        )
    }
}
