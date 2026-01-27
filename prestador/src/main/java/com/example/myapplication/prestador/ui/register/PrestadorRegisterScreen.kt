package com.example.myapplication.prestador.ui.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrestadorRegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: PrestadorRegisterViewModel = hiltViewModel()
) {
    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("Argentina") }
    var provincia by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Servicios
    var serviciosSeleccionados by remember { mutableStateOf(listOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showServiceModal by remember { mutableStateOf(false) }
    var tempSelectedServices by remember { mutableStateOf(setOf<String>()) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    // Lista de servicios disponibles
    val serviciosDisponibles = listOf(
        "Aire Acondicionado", "Albañilería", "Carpintería", "Cerrajería",
        "Computación", "Decoración", "Electricidad", "Electrónica",
        "Fletes", "Gasista", "Herrería", "Jardinería", "Limpieza",
        "Mecánica Ligera", "Pintura", "Plomería", "Refrigeración", "Soldadura"
    )
    
    // Servicios filtrados según búsqueda
    val serviciosFiltrados = remember(searchQuery, serviciosSeleccionados) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            serviciosDisponibles.filter { servicio ->
                servicio.contains(searchQuery, ignoreCase = true) && 
                !serviciosSeleccionados.contains(servicio)
            }
        }
    }
    
    // Toggles
    var atencionUrgencias by remember { mutableStateOf(false) }
    var vaDomicilio by remember { mutableStateOf(false) }
    var turnosEnLocal by remember { mutableStateOf(false) }
    var tieneEmpresa by remember { mutableStateOf(false) }
    
    // Datos empresa
    var nombreEmpresa by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var cuit by remember { mutableStateOf("") }
    var sucursales by remember { mutableStateOf(listOf(Sucursal("", ""))) }
    
    // UI States
    var personalSectionExpanded by remember { mutableStateOf(false) }
    var showMatriculaTooltip by remember { mutableStateOf(false) }
    
    val registerState by viewModel.registerState.collectAsState()
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }
    
    // Manejar estado de registro
    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> onRegisterSuccess()
            is RegisterState.Error -> {
                // Mostrar error
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear perfil", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrestadorOrange
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = SurfaceWhite
            ) {
                Button(
                    onClick = {
                        // Validar y registrar
                        // viewModel.register(...)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrestadorOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (registerState is RegisterState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Guardar Perfil",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Foto de perfil
            item {
                ProfilePhotoSection(
                    imageUri = profileImageUri,
                    onCameraClick = { imagePickerLauncher.launch("image/*") }
                )
            }
            
            // Sección Datos Personales
            item {
                CollapsibleSection(
                    title = "DATOS PERSONALES",
                    isExpanded = personalSectionExpanded,
                    onToggle = { personalSectionExpanded = !personalSectionExpanded }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        FloatingLabelTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = "Nombre Completo",
                            leadingIcon = Icons.Default.Person,
                            enabled = personalSectionExpanded
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        AnimatedVisibility(visible = personalSectionExpanded) {
                            Column {
                                FloatingLabelTextField(
                                    value = matricula,
                                    onValueChange = { matricula = it },
                                    label = "N° de Matrícula",
                                    leadingIcon = Icons.Default.CardMembership,
                                    trailingIcon = Icons.Default.Info,
                                    onTrailingIconClick = {
                                        showMatriculaTooltip = !showMatriculaTooltip
                                    }
                                )
                                
                                // Tooltip
                                if (showMatriculaTooltip) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        TooltipBubble(
                                            text = "Este campo es opcional si aún no tienes matrícula.",
                                            onDismiss = { showMatriculaTooltip = false }
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                FloatingLabelTextField(
                                    value = pais,
                                    onValueChange = { pais = it },
                                    label = "País",
                                    leadingIcon = Icons.Default.Public
                                )
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                FloatingLabelTextField(
                                    value = provincia,
                                    onValueChange = { provincia = it },
                                    label = "Provincia / Estado",
                                    leadingIcon = Icons.Default.LocationOn
                                )
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                FloatingLabelTextField(
                                    value = codigoPostal,
                                    onValueChange = { codigoPostal = it },
                                    label = "Código Postal",
                                    leadingIcon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Number
                                )
                                
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                        
                        FloatingLabelTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = "Dirección (Calle y N°)",
                            leadingIcon = Icons.Default.Home,
                            enabled = personalSectionExpanded
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            // DividerLight
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFFF0F2F5))
                )
            }
            
            // Sección Servicios
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        "¿QUÉ SERVICIO PRESTAS?",
                        color = OrangePrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Buscador con icono de lista
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FloatingLabelTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                showSuggestions = query.isNotEmpty()
                            },
                            label = "Buscar o seleccionar de lista",
                            leadingIcon = Icons.Default.Search,
                            trailingIcon = Icons.Default.List,
                            onTrailingIconClick = {
                                tempSelectedServices = serviciosSeleccionados.toSet()
                                showServiceModal = true
                                showSuggestions = false
                            }
                        )
                        
                        // Lista de sugerencias
                        AnimatedVisibility(
                            visible = showSuggestions && serviciosFiltrados.isNotEmpty()
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(
                                    bottomStart = 8.dp,
                                    bottomEnd = 8.dp
                                ),
                                shadowElevation = 4.dp,
                                color = Color.White
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                ) {
                                    items(serviciosFiltrados) { servicio ->
                                        SuggestionItem(
                                            text = servicio,
                                            onClick = {
                                                if (serviciosSeleccionados.size < 5) {
                                                    serviciosSeleccionados = serviciosSeleccionados + servicio
                                                    searchQuery = ""
                                                    showSuggestions = false
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Servicios seleccionados:",
                        color = TextSecondaryGray,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Chips de servicios
                    ServiceChipsList(
                        services = serviciosSeleccionados,
                        onRemove = { service ->
                            serviciosSeleccionados = serviciosSeleccionados - service
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Switches
                    SwitchRow(
                        title = "Atención urgencias",
                        subtitle = "Disponible las 24 horas",
                        checked = atencionUrgencias,
                        onCheckedChange = { atencionUrgencias = it }
                    )
                    
                    SwitchRow(
                        title = "¿Vas a domicilio?",
                        subtitle = "Realizas trabajos en el hogar del cliente",
                        checked = vaDomicilio,
                        onCheckedChange = { vaDomicilio = it }
                    )
                    
                    SwitchRow(
                        title = "¿Turnos en local?",
                        subtitle = "Se requiere agendar cita previa",
                        checked = turnosEnLocal,
                        onCheckedChange = { turnosEnLocal = it }
                    )
                    
                    SwitchRow(
                        title = "¿Tienes empresa?",
                        subtitle = "Registrar datos de tu negocio",
                        checked = tieneEmpresa,
                        onCheckedChange = { tieneEmpresa = it }
                    )
                    
                    // Formulario de empresa
                    AnimatedVisibility(
                        visible = tieneEmpresa,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        BusinessDetailsForm(
                            nombreEmpresa = nombreEmpresa,
                            onNombreEmpresaChange = { nombreEmpresa = it },
                            razonSocial = razonSocial,
                            onRazonSocialChange = { razonSocial = it },
                            cuit = cuit,
                            onCuitChange = { cuit = it },
                            sucursales = sucursales,
                            onSucursalesChange = { sucursales = it }
                        )
                    }
                }
            }
        }
    }
    
    // Modal de servicios
    if (showServiceModal) {
        ServiceSelectionModal(
            availableServices = listOf(
                "Aire Acondicionado", "Albañilería", "Carpintería", "Cerrajería",
                "Computación", "Decoración", "Electricidad", "Electrónica",
                "Fletes", "Gasista", "Herrería", "Jardinería", "Limpieza",
                "Mecánica Ligera", "Pintura", "Plomería", "Refrigeración", "Soldadura"
            ),
            selectedServices = tempSelectedServices,
            onServiceToggle = { service ->
                tempSelectedServices = if (tempSelectedServices.contains(service)) {
                    tempSelectedServices - service
                } else {
                    if (tempSelectedServices.size < 5) {
                        tempSelectedServices + service
                    } else {
                        tempSelectedServices
                    }
                }
            },
            onDismiss = { showServiceModal = false },
            onConfirm = {
                serviciosSeleccionados = tempSelectedServices.toList()
                showServiceModal = false
            }
        )
    }
}

// =========================
// COMPONENTES REUTILIZABLES
// =========================

@Composable
fun ProfilePhotoSection(
    imageUri: Uri?,
    onCameraClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = Color(0xFFE9EDEF),
                border = BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF8696A0)
                    )
                }
            }
            
            // Botón cámara flotante
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .clickable { onCameraClick() },
                shape = CircleShape,
                shadowElevation = 4.dp,
                color = PrestadorOrange
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = OrangePrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            
            Surface(
                shape = CircleShape,
                color = if (isExpanded) OrangePrimary.copy(alpha = 0.1f) else Color.Transparent,
                modifier = Modifier.clickable { onToggle() }
            ) {
                Icon(
                    if (isExpanded) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isExpanded) "Guardar" else "Editar",
                    tint = if (isExpanded) OrangePrimary else TextSecondaryGray,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val hasText = value.isNotEmpty()
    
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = TextSecondaryGray
                )
            },
            trailingIcon = trailingIcon?.let {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            it,
                            contentDescription = null,
                            tint = TextSecondaryGray
                        )
                    }
                }
            },
            label = {
                Text(
                    label,
                    color = if (isFocused) OrangePrimary else TextSecondaryGray,
                    fontSize = if (isFocused || hasText) 12.sp else 16.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = if (enabled) Color(0xFF8696A0) else Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedLabelColor = OrangePrimary,
                unfocusedLabelColor = TextSecondaryGray,
                focusedTextColor = TextPrimaryDark,
                unfocusedTextColor = TextPrimaryDark,
                disabledTextColor = TextPrimaryDark
            ),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            singleLine = true
        )
    }
}

@Composable
fun TooltipBubble(
    text: String,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.width(200.dp),
        color = PrestadorOrange
    ) {
        Box(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceChipsList(
    services: List<String>,
    onRemove: (String) -> Unit
) {
    if (services.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "No hay servicios seleccionados",
                color = TextSecondaryGray,
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = Int.MAX_VALUE
        ) {
            services.forEach { service ->
                ServiceChip(
                    text = service,
                    onRemove = { onRemove(service) }
                )
            }
        }
    }
}

@Composable
fun ServiceChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.wrapContentSize(),
        shape = RoundedCornerShape(10.dp),
        color = ChipBackground,
        border = BorderStroke(1.dp, Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text,
                color = OrangeDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            Surface(
                shape = CircleShape,
                color = Color(0x0D000000),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = OrangePrimary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(
            color = DividerLight,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimaryDark
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = TextSecondaryGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = OrangePrimary,
                    uncheckedColor = Color(0xFF8696A0)
                )
            )
        }
    }
}

@Composable
fun BusinessDetailsForm(
    nombreEmpresa: String,
    onNombreEmpresaChange: (String) -> Unit,
    razonSocial: String,
    onRazonSocialChange: (String) -> Unit,
    cuit: String,
    onCuitChange: (String) -> Unit,
    sucursales: List<Sucursal>,
    onSucursalesChange: (List<Sucursal>) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAFAFA),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            FloatingLabelTextField(
                value = nombreEmpresa,
                onValueChange = onNombreEmpresaChange,
                label = "Nombre de Fantasía",
                leadingIcon = Icons.Default.Business
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            FloatingLabelTextField(
                value = razonSocial,
                onValueChange = onRazonSocialChange,
                label = "Razón Social",
                leadingIcon = Icons.Default.Description
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            FloatingLabelTextField(
                value = cuit,
                onValueChange = onCuitChange,
                label = "CUIT",
                leadingIcon = Icons.Default.Receipt,
                keyboardType = KeyboardType.Number
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sucursales
            sucursales.forEachIndexed { index, sucursal ->
                BranchBlock(
                    title = if (index == 0) "Sucursal Principal" else "Sucursal #${index + 1}",
                    direccion = sucursal.direccion,
                    onDireccionChange = { newDir ->
                        val updated = sucursales.toMutableList()
                        updated[index] = sucursal.copy(direccion = newDir)
                        onSucursalesChange(updated)
                    },
                    codigoPostal = sucursal.codigoPostal,
                    onCodigoPostalChange = { newCp ->
                        val updated = sucursales.toMutableList()
                        updated[index] = sucursal.copy(codigoPostal = newCp)
                        onSucursalesChange(updated)
                    },
                    showDelete = index > 0,
                    onDelete = {
                        val updated = sucursales.toMutableList()
                        updated.removeAt(index)
                        onSucursalesChange(updated)
                    }
                )
                
                if (index < sucursales.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón agregar sucursal
            OutlinedButton(
                onClick = {
                    onSucursalesChange(sucursales + Sucursal("", ""))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangePrimary
                ),
                border = BorderStroke(1.dp, OrangePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Agregar otra sucursal",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun BranchBlock(
    title: String,
    direccion: String,
    onDireccionChange: (String) -> Unit,
    codigoPostal: String,
    onCodigoPostalChange: (String) -> Unit,
    showDelete: Boolean,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA))
            .drawBehind {
                // Dibujar línea izquierda naranja
                drawRect(
                    color = androidx.compose.ui.graphics.Color(0xFFF97316), // PrestadorOrange
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(12f, size.height)
                )
            }
            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = PrestadorOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            if (showDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar sucursal",
                        tint = ErrorRed
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FloatingLabelTextField(
            value = direccion,
            onValueChange = onDireccionChange,
            label = "Dirección",
            leadingIcon = Icons.Default.LocationOn
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = codigoPostal,
            onValueChange = onCodigoPostalChange,
            label = "Código Postal",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Number
        )
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ServiceSelectionModal(
    availableServices: List<String>,
    selectedServices: Set<String>,
    onServiceToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrestadorOrange)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Servicios",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "${selectedServices.size}/5",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Lista de servicios
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(availableServices) { service ->
                        val isSelected = selectedServices.contains(service)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onServiceToggle(service) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = OrangePrimary,
                                    uncheckedColor = Color(0xFF8696A0)
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                service,
                                fontSize = 16.sp,
                                color = TextPrimaryDark
                            )
                        }
                        
                        Divider(color = Color(0xFFF0F2F5))
                    }
                }
                
                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = OrangePrimary)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrestadorOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Aceptar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Componente para item de sugerencia
@Composable
fun SuggestionItem(
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = TextPrimaryDark,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                tint = PrestadorOrange,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Divider(
            color = Color(0xFFF0F2F5),
            thickness = 1.dp
        )
    }
}

// Data class
data class Sucursal(
    val direccion: String,
    val codigoPostal: String
)




