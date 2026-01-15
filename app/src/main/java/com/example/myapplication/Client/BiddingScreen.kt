package com.example.myapplication.Client

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.Data.Model.Category
import com.example.myapplication.ViewModel.CategoryViewModel
import com.example.myapplication.ui.theme.getAppColors
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiddingScreen(
    onBack: () -> Unit,
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    // Estados del formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showInfoBanner by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    // Estados para categoría
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categorySearchQuery by remember { mutableStateOf("") }
    var isCategorySearchFocused by remember { mutableStateOf(false) }
    
    // Colores adaptativos
    val colors = getAppColors()
    
    // Obtener categorías
    val categories by categoryViewModel.categories.collectAsState()
    
    // Filtrar categorías según búsqueda
    val filteredCategories = remember(categorySearchQuery, categories) {
        if (categorySearchQuery.isEmpty()) {
            categories
        } else {
            categories.filter { it.name.contains(categorySearchQuery, ignoreCase = true) }
        }
    }
    
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        selectedImages = uris
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        // SECCIÓN 1: HEADER
        TopAppBar(
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nueva Licitación",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box {
                            IconButton(
                                onClick = { showInfoBanner = !showInfoBanner },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Información",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Viñeta emergente
                            DropdownMenu(
                                expanded = showInfoBanner,
                                onDismissRequest = { showInfoBanner = false },
                                modifier = Modifier
                                    .width(300.dp)
                                    .background(Color.Transparent)
                            ) {
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
                                            text = "Proyectos Grandes",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3730A3)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ideal para reformas, instalaciones completas o pintura general. Recibe varios presupuestos y elige el mejor.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4338CA),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Publica tu proyecto y recibe ofertas",
                        fontSize = 12.sp,
                        color = colors.textSecondaryColor
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = colors.textSecondaryColor
                    )
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
            // Campo: Categoría del Proyecto
            Text(
                text = "Categoría del Proyecto",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Column {
                Box {
                    OutlinedTextField(
                        value = if (selectedCategory != null) selectedCategory!!.name else categorySearchQuery,
                        onValueChange = { newValue ->
                            if (selectedCategory != null) {
                                // Si hay categoría seleccionada, no permitir edición directa
                                // Solo permitir borrar
                                return@OutlinedTextField
                            }
                            categorySearchQuery = newValue
                            isCategorySearchFocused = true
                        },
                        placeholder = { 
                            Text(
                                "Escribe el oficio (ej. Electricista)", 
                                fontSize = 14.sp, 
                                color = colors.textSecondaryColor
                            ) 
                        },
                        leadingIcon = {
                            if (selectedCategory != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(android.graphics.Color.parseColor(selectedCategory!!.colorHex)),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = selectedCategory!!.name.first().toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = colors.textSecondaryColor
                                )
                            }
                        },
                        trailingIcon = {
                            if (selectedCategory != null) {
                                IconButton(onClick = { 
                                    selectedCategory = null
                                    categorySearchQuery = ""
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar categoría",
                                        tint = colors.textSecondaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else if (categorySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { categorySearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = colors.textSecondaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = if (selectedCategory != null) Color(0xFFEEF2FF) else colors.surfaceColor,
                            focusedContainerColor = if (selectedCategory != null) Color(0xFFEEF2FF) else Color.White,
                            unfocusedBorderColor = if (selectedCategory != null) colors.accentBlue else colors.dividerColor,
                            focusedBorderColor = colors.accentBlue,
                            focusedTextColor = colors.textPrimaryColor,
                            unfocusedTextColor = colors.textPrimaryColor,
                            disabledTextColor = colors.textPrimaryColor,
                            disabledBorderColor = colors.accentBlue,
                            disabledContainerColor = Color(0xFFEEF2FF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused && selectedCategory == null) {
                                    isCategorySearchFocused = true
                                }
                            },
                        singleLine = true,
                        readOnly = selectedCategory != null
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resto del contenido continúa aquí...
            
            // Campo: Título del Proyecto
            Text(
                text = "Título del Proyecto",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Ej. Remodelación de cocina completa", fontSize = 14.sp, color = colors.textSecondaryColor) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = colors.textSecondaryColor
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colors.surfaceColor,
                    focusedContainerColor = colors.surfaceColor,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedBorderColor = colors.accentBlue,
                    focusedTextColor = colors.textPrimaryColor,
                    unfocusedTextColor = colors.textPrimaryColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Campo: Descripción Detallada
            Text(
                text = "Descripción Detallada",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { 
                    Text(
                        "Describe las medidas, materiales necesarios y detalles del trabajo a realizar...",
                        fontSize = 14.sp,
                        color = colors.textSecondaryColor
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colors.surfaceColor,
                    focusedContainerColor = colors.surfaceColor,
                    unfocusedBorderColor = colors.dividerColor,
                    focusedBorderColor = colors.accentBlue,
                    focusedTextColor = colors.textPrimaryColor,
                    unfocusedTextColor = colors.textPrimaryColor
                ),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fecha de Inicio",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.dividerColor, RoundedCornerShape(16.dp))
                            .clickable { showStartDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = colors.textSecondaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = startDate?.let { dateFormatter.format(Date(it)) } ?: "Seleccionar",
                                fontSize = 14.sp,
                                color = if (startDate != null) colors.textPrimaryColor else colors.textSecondaryColor
                            )
                        }
                    }
                }
                
                // Fecha de Fin
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fecha de Fin",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.dividerColor, RoundedCornerShape(16.dp))
                            .clickable { showEndDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = colors.textSecondaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = endDate?.let { dateFormatter.format(Date(it)) } ?: "Seleccionar",
                                fontSize = 14.sp,
                                color = if (endDate != null) colors.textPrimaryColor else colors.textSecondaryColor
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subir fotos
            Text(
                text = "Fotos de referencia",
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
        
        // SECCIÓN 3: BOTÓN DE ENVÍO (FIJO ABAJO)
        Surface(
            color = colors.surfaceColor,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { /* TODO: Publicar licitación */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Publicar Licitación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    
    // Overlay de categorías (fuera del flujo principal)
    if (isCategorySearchFocused && filteredCategories.isNotEmpty() && selectedCategory == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(horizontal = 24.dp)
                    .offset(x = 0.dp, y = 180.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceColor,
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                LazyColumn {
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
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(android.graphics.Color.parseColor(category.colorHex)),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = category.name.first().toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
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
    }
    
    // DatePickers con tema adaptativo
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDate = datePickerState.selectedDateMillis
                        showStartDatePicker = false
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
                        endDate = datePickerState.selectedDateMillis
                        showEndDatePicker = false
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
}