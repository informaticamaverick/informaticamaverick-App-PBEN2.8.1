package com.example.myapplication.prestador.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.model.PrestadorProfileMode
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.register.FloatingLabelTextField
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState
import com.example.myapplication.prestador.viewmodel.UpdateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreenUnified(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val profileMode by viewModel.profileMode.collectAsState()
    val isEmpresaMode = profileMode == PrestadorProfileMode.EMPRESA
    
    // Estados del formulario
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dniCuit by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profesion by remember { mutableStateOf("") }
    var tieneMatricula by remember { mutableStateOf(false) }
    var matricula by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("Argentina") }
    var provincia by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    var atencionUrgencias by remember { mutableStateOf(false) }
    var vaDomicilio by remember { mutableStateOf(false) }
    var turnosEnLocal by remember { mutableStateOf(false) }
    var direccionLocal by remember { mutableStateOf("") }
    var provinciaLocal by remember { mutableStateOf("") }
    var codigoPostalLocal by remember { mutableStateOf("") }
    var tieneEmpresa by remember { mutableStateOf(false) }
    var trabajaConOtros by remember { mutableStateOf(false) }
    var nombreEmpresa by remember { mutableStateOf("") }
    var cuitEmpresa by remember { mutableStateOf("") }
    var direccionEmpresa by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf(ServiceType.TECHNICAL) }
    var providerId by remember { mutableStateOf<String?>(null) }
    
    // Estado del acordeón: qué sección está expandida
    var expandedSection by remember { mutableStateOf<String?>("personal") }
    
    // Dialog states
    var showServiceTypeDialog by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }
    
    // Launcher para seleccionar imagen
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }
    
    // Estado de scroll para TopBar animada
    val listState = rememberLazyListState()
    val scrollProgress = remember {
        derivedStateOf {
            val maxScroll = 200f
            val currentScroll = listState.firstVisibleItemScrollOffset.toFloat()
            (currentScroll / maxScroll).coerceIn(0f, 1f)
        }
    }
    val topBarAlpha by animateFloatAsState(
        targetValue = scrollProgress.value,
        label = "topBarAlpha"
    )
    
    // Cargar datos cuando se obtiene el perfil
    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val provider = (profileState as ProfileState.Success).provider
            providerId = provider.id
            name = provider.name
            email = provider.email
            phone = provider.phone
            dniCuit = provider.dniCuit ?: ""
            imageUrl = provider.imageUrl
            description = provider.description ?: ""
            address = provider.address ?: ""
            profesion = provider.profesion ?: ""
            tieneMatricula = provider.tieneMatricula
            matricula = provider.matricula ?: ""
            provincia = provider.provincia ?: ""
            codigoPostal = provider.codigoPostal ?: ""
            pais = provider.pais
            atencionUrgencias = provider.atencionUrgencias
            vaDomicilio = provider.vaDomicilio
            turnosEnLocal = provider.turnosEnLocal
            direccionLocal = provider.direccionLocal ?: ""
            provinciaLocal = provider.provinciaLocal ?: ""
            codigoPostalLocal = provider.codigoPostalLocal ?: ""
            tieneEmpresa = provider.tieneEmpresa
            trabajaConOtros = provider.trabajaConOtros
            nombreEmpresa = provider.nombreEmpresa ?: ""
            cuitEmpresa = provider.cuitEmpresa ?: ""
            direccionEmpresa = provider.direccionEmpresa ?: ""
            serviceType = ServiceType.fromString(provider.serviceType)
        }
    }
    
    // Mostrar mensaje de éxito
    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Success) {
            kotlinx.coroutines.delay(1500)
            viewModel.resetUpdateState()
        }
    }
    
    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = topBarAlpha },
                color = colors.surfaceColor,
                shadowElevation = if (topBarAlpha > 0.01f) 4.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.primaryOrange
                        )
                    }
                    Text(
                        text = "Mi Perfil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val state = profileState) {
            is ProfileState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primaryOrange)
                }
            }
            
            is ProfileState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = colors.primaryOrange,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = colors.textPrimary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryOrange
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            
            is ProfileState.Success -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        // HERO HEADER
                        item {
                            HeaderSection(
                                name = name,
                                profesion = profesion,
                                imageUrl = imageUrl,
                                selectedImageUri = selectedImageUri,
                                isEmpresaMode = isEmpresaMode,
                                tieneEmpresa = tieneEmpresa,
                                colors = colors,
                                paddingValues = paddingValues,
                                onBack = onBack,
                                onImageClick = { galleryLauncher.launch("image/*") },
                                onToggleMode = { viewModel.toggleProfileMode() }
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        
                        // MODO PERSONAL
                        if (!isEmpresaMode) {
                            item {
                                PersonalDataSection(
                                    name = name,
                                    onNameChange = { name = it },
                                    email = email,
                                    onEmailChange = { email = it },
                                    phone = phone,
                                    onPhoneChange = { phone = it },
                                    dniCuit = dniCuit,
                                    onDniCuitChange = { dniCuit = it },
                                    tieneEmpresa = tieneEmpresa,
                                    onTieneEmpresaChange = { newValue ->
                                        tieneEmpresa = newValue
                                        // Si activa empresa, cambiar automáticamente a modo Empresa
                                        if (newValue && !isEmpresaMode) {
                                            viewModel.toggleProfileMode()
                                        }
                                        // Si desactiva empresa y está en modo empresa, volver a personal
                                        if (!newValue && isEmpresaMode) {
                                            viewModel.toggleProfileMode()
                                        }
                                    },
                                    expanded = expandedSection == "personal",
                                    onExpandChange = { expandedSection = if (expandedSection == "personal") null else "personal" },
                                    colors = colors
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            
                            item {
                                ProfessionalDataSection(
                                    profesion = profesion,
                                    onProfesionChange = { profesion = it },
                                    tieneMatricula = tieneMatricula,
                                    onTieneMatriculaChange = { tieneMatricula = it },
                                    matricula = matricula,
                                    onMatriculaChange = { matricula = it },
                                    description = description,
                                    onDescriptionChange = { description = it },
                                    serviceType = serviceType,
                                    onServiceTypeClick = { showServiceTypeDialog = true },
                                    expanded = expandedSection == "professional",
                                    onExpandChange = { expandedSection = if (expandedSection == "professional") null else "professional" },
                                    colors = colors
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            
                            item {
                                LocationSection(
                                    address = address,
                                    onAddressChange = { address = it },
                                    provincia = provincia,
                                    onProvinciaChange = { provincia = it },
                                    codigoPostal = codigoPostal,
                                    onCodigoPostalChange = { codigoPostal = it },
                                    pais = pais,
                                    onPaisChange = { pais = it },
                                    expanded = expandedSection == "location",
                                    onExpandChange = { expandedSection = if (expandedSection == "location") null else "location" },
                                    colors = colors
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            
                            item {
                                ServiceConfigSection(
                                    atencionUrgencias = atencionUrgencias,
                                    onAtencionUrgenciasChange = { atencionUrgencias = it },
                                    vaDomicilio = vaDomicilio,
                                    onVaDomicilioChange = { vaDomicilio = it },
                                    turnosEnLocal = turnosEnLocal,
                                    onTurnosEnLocalChange = { turnosEnLocal = it },
                                    direccionLocal = direccionLocal,
                                    onDireccionLocalChange = { direccionLocal = it },
                                    provinciaLocal = provinciaLocal,
                                    onProvinciaLocalChange = { provinciaLocal = it },
                                    codigoPostalLocal = codigoPostalLocal,
                                    onCodigoPostalLocalChange = { codigoPostalLocal = it },
                                    serviceType = serviceType,
                                    providerId = providerId,
                                    isEmpresaMode = false,
                                    direccionEmpresa = "",
                                    expanded = expandedSection == "services",
                                    onExpandChange = { expandedSection = if (expandedSection == "services") null else "services" },
                                    colors = colors
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            
                            item {
                                EmpleadosSection(
                                    trabajaConOtros = trabajaConOtros,
                                    onTrabajaConOtrosChange = { trabajaConOtros = it },
                                    expanded = expandedSection == "team",
                                    onExpandChange = { expandedSection = if (expandedSection == "team") null else "team" }
                                )
                            }
                        }
                        
                        // MODO EMPRESA
                        if (isEmpresaMode) {
                            item {
                                CompanyDataSection(
                                    nombreEmpresa = nombreEmpresa,
                                    onNombreEmpresaChange = { nombreEmpresa = it },
                                    cuitEmpresa = cuitEmpresa,
                                    onCuitEmpresaChange = { cuitEmpresa = it },
                                    direccionEmpresa = direccionEmpresa,
                                    onDireccionEmpresaChange = { direccionEmpresa = it },
                                    expanded = expandedSection == "company",
                                    onExpandChange = { expandedSection = if (expandedSection == "company") null else "company" },
                                    colors = colors
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            
                            item {
                                ServiceConfigSection(
                                    atencionUrgencias = atencionUrgencias,
                                    onAtencionUrgenciasChange = { atencionUrgencias = it },
                                    vaDomicilio = vaDomicilio,
                                    onVaDomicilioChange = { vaDomicilio = it },
                                    turnosEnLocal = turnosEnLocal,
                                    onTurnosEnLocalChange = { turnosEnLocal = it },
                                    direccionLocal = direccionLocal,
                                    onDireccionLocalChange = { direccionLocal = it },
                                    provinciaLocal = provinciaLocal,
                                    onProvinciaLocalChange = { provinciaLocal = it },
                                    codigoPostalLocal = codigoPostalLocal,
                                    onCodigoPostalLocalChange = { codigoPostalLocal = it },
                                    serviceType = serviceType,
                                    providerId = providerId,
                                    isEmpresaMode = true,
                                    direccionEmpresa = direccionEmpresa,
                                    expanded = expandedSection == "services",
                                    onExpandChange = { expandedSection = if (expandedSection == "services") null else "services" },
                                    colors = colors
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            
                            item {
                                EmpleadosSection(
                                    trabajaConOtros = trabajaConOtros,
                                    onTrabajaConOtrosChange = { trabajaConOtros = it },
                                    expanded = expandedSection == "team",
                                    onExpandChange = { expandedSection = if (expandedSection == "team") null else "team" }
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                    
                    // FAB GUARDAR
                    FloatingActionButton(
                        onClick = {
                            // Validar datos antes de guardar
                            if (profileMode == PrestadorProfileMode.PERSONAL && turnosEnLocal) {
                                if (direccionLocal.isBlank() || provinciaLocal.isBlank() || codigoPostalLocal.isBlank()) {
                                    validationErrorMessage = "Completá la dirección del local para activar atención en local"
                                    showValidationError = true
                                    return@FloatingActionButton
                                }
                            }
                            
                            // Si pasa validación, guardar
                            viewModel.updateProfile(
                            name = name,
                            phone = phone,
                            dniCuit = dniCuit,
                            description = description,
                            address = address,
                            profesion = profesion,
                            tieneMatricula = tieneMatricula,
                            matricula = matricula.takeIf { tieneMatricula },
                            provincia = provincia,
                            codigoPostal = codigoPostal,
                            pais = pais,
                            atencionUrgencias = atencionUrgencias,
                            vaDomicilio = vaDomicilio,
                            turnosEnLocal = turnosEnLocal,
                            direccionLocal = direccionLocal.takeIf { turnosEnLocal },
                            provinciaLocal = provinciaLocal.takeIf { turnosEnLocal },
                            codigoPostalLocal = codigoPostalLocal.takeIf { turnosEnLocal },
                            tieneEmpresa = tieneEmpresa,
                            trabajaConOtros = trabajaConOtros,
                            nombreEmpresa = nombreEmpresa.takeIf { tieneEmpresa },
                            cuitEmpresa = cuitEmpresa.takeIf { tieneEmpresa },
                            direccionEmpresa = direccionEmpresa.takeIf { tieneEmpresa },
                            serviceType = serviceType.name
                        )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        containerColor = colors.primaryOrange,
                        contentColor = Color.White
                    ) {
                        when (updateState) {
                            is UpdateState.Loading -> {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            is UpdateState.Success -> {
                                Icon(Icons.Default.Check, contentDescription = "Guardado")
                            }
                            else -> {
                                Icon(Icons.Default.Save, contentDescription = "Guardar")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialog de selección de tipo de servicio
    if (showServiceTypeDialog) {
        ServiceTypeSelectorDialog(
            currentServiceType = serviceType,
            onDismiss = { showServiceTypeDialog = false },
            onServiceTypeSelected = { newType ->
                serviceType = newType
            },
            colors = colors
        )
    }
    
    // Dialog de error de validación
    if (showValidationError) {
        AlertDialog(
            onDismissRequest = { showValidationError = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.primaryOrange
                )
            },
            title = {
                Text(
                    text = "Datos incompletos",
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = validationErrorMessage,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showValidationError = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryOrange
                    )
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

// COMPONENTE: Header con foto y toggle
@Composable
fun HeaderSection(
    name: String,
    profesion: String,
    imageUrl: String?,
    selectedImageUri: Uri?,
    isEmpresaMode: Boolean,
    tieneEmpresa: Boolean,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onImageClick: () -> Unit,
    onToggleMode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.primaryOrange.copy(alpha = 0.2f),
                        colors.backgroundColor
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = paddingValues.calculateTopPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón back
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = colors.primaryOrange
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Foto de perfil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(4.dp, colors.primaryOrange, CircleShape)
                    .clickable { onImageClick() }
            ) {
                if (selectedImageUri != null || imageUrl != null) {
                    AsyncImage(
                        model = selectedImageUri ?: imageUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.primaryOrange.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = colors.primaryOrange.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // Botón de cámara
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp),
                    shape = CircleShape,
                    color = colors.primaryOrange,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = name.ifEmpty { "Prestador" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            
            if (profesion.isNotEmpty()) {
                Text(
                    text = profesion,
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }
        }
        
        // Toggle button
        if (tieneEmpresa) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .clickable { onToggleMode() },
                color = if (isEmpresaMode) Color(0xFF9C27B0) else colors.primaryOrange,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEmpresaMode) "Modo Empresa" else "Modo Personal",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isEmpresaMode) Icons.Default.Business else Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// SECCIÓN: Datos Personales
@Composable
fun PersonalDataSection(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    dniCuit: String,
    onDniCuitChange: (String) -> Unit,
    tieneEmpresa: Boolean,
    onTieneEmpresaChange: (Boolean) -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    ArchiveroSection(
        title = "Datos Personales",
        sectionId = "personal",
        icon = Icons.Default.Person,
        color = colors.primaryOrange,
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        FloatingLabelTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Nombre completo",
            leadingIcon = Icons.Default.Person
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            enabled = false
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = "Teléfono",
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = dniCuit,
            onValueChange = onDniCuitChange,
            label = "DNI / CUIT",
            leadingIcon = Icons.Default.Badge,
            keyboardType = KeyboardType.Number
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Switch: ¿Tiene empresa?
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTieneEmpresaChange(!tieneEmpresa) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Business,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "¿Tiene empresa registrada?",
                color = colors.textPrimary,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = tieneEmpresa,
                onCheckedChange = onTieneEmpresaChange,
                modifier = Modifier.scale(0.85f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primaryOrange,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
                )
            )
        }
        
        // Mensaje informativo si activa el switch
        AnimatedVisibility(
            visible = tieneEmpresa,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = Color(0xFF9C27B0).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF9C27B0),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cambia a 'Modo Empresa' para completar los datos de tu empresa",
                        color = Color(0xFF9C27B0),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// SECCIÓN: Datos Profesionales
@Composable
fun ProfessionalDataSection(
    profesion: String,
    onProfesionChange: (String) -> Unit,
    tieneMatricula: Boolean,
    onTieneMatriculaChange: (Boolean) -> Unit,
    matricula: String,
    onMatriculaChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    serviceType: ServiceType,
    onServiceTypeClick: () -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    ArchiveroSection(
        title = "Datos Profesionales",
        sectionId = "professional",
        icon = Icons.Default.Work,
        color = Color(0xFF4CAF50),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        // Tipo de servicio
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = colors.primaryOrange.copy(alpha = 0.1f),
            onClick = onServiceTypeClick
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (serviceType) {
                        ServiceType.TECHNICAL -> Icons.Default.Build
                        ServiceType.PROFESSIONAL -> Icons.Default.CalendarMonth
                        ServiceType.RENTAL -> Icons.Default.Stadium
                        ServiceType.OTHER -> Icons.Default.MoreHoriz
                    },
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tipo de servicio",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = serviceType.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primaryOrange
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambiar",
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = profesion,
            onValueChange = onProfesionChange,
            label = "Profesión / Oficio",
            leadingIcon = Icons.Default.Work
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Switch para matrícula profesional
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTieneMatriculaChange(!tieneMatricula) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CardMembership,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "¿Tiene matrícula profesional?",
                color = colors.textPrimary,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            androidx.compose.material3.Switch(
                checked = tieneMatricula,
                onCheckedChange = onTieneMatriculaChange,
                modifier = Modifier.scale(0.85f),
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primaryOrange,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
                )
            )
        }
        
        // Campo de matrícula solo si tiene matrícula
        AnimatedVisibility(
            visible = tieneMatricula,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                FloatingLabelTextField(
                    value = matricula,
                    onValueChange = onMatriculaChange,
                    label = "Número de Matrícula",
                    leadingIcon = Icons.Default.CardMembership
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Descripción / Sobre mí",
            leadingIcon = Icons.Default.Info
        )
    }
}

// SECCIÓN: Ubicación
@Composable
fun LocationSection(
    address: String,
    onAddressChange: (String) -> Unit,
    provincia: String,
    onProvinciaChange: (String) -> Unit,
    codigoPostal: String,
    onCodigoPostalChange: (String) -> Unit,
    pais: String,
    onPaisChange: (String) -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    ArchiveroSection(
        title = "Ubicación",
        sectionId = "location",
        icon = Icons.Default.LocationOn,
        color = Color(0xFF2196F3),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        FloatingLabelTextField(
            value = address,
            onValueChange = onAddressChange,
            label = "Dirección",
            leadingIcon = Icons.Default.LocationOn
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = provincia,
            onValueChange = onProvinciaChange,
            label = "Provincia",
            leadingIcon = Icons.Default.Place
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = codigoPostal,
            onValueChange = onCodigoPostalChange,
            label = "Código Postal",
            leadingIcon = Icons.Default.PinDrop,
            keyboardType = KeyboardType.Number
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = pais,
            onValueChange = onPaisChange,
            label = "País",
            leadingIcon = Icons.Default.Public,
            enabled = false
        )
    }
}

// SECCIÓN: Datos de Empresa
@Composable
fun CompanyDataSection(
    nombreEmpresa: String,
    onNombreEmpresaChange: (String) -> Unit,
    cuitEmpresa: String,
    onCuitEmpresaChange: (String) -> Unit,
    direccionEmpresa: String,
    onDireccionEmpresaChange: (String) -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    ArchiveroSection(
        title = "Datos de Empresa",
        sectionId = "company",
        icon = Icons.Default.Business,
        color = Color(0xFFFF9800),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        FloatingLabelTextField(
            value = nombreEmpresa,
            onValueChange = onNombreEmpresaChange,
            label = "Nombre de la empresa",
            leadingIcon = Icons.Default.Business
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = cuitEmpresa,
            onValueChange = onCuitEmpresaChange,
            label = "CUIT de la empresa",
            leadingIcon = Icons.Default.Numbers,
            keyboardType = KeyboardType.Number
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = direccionEmpresa,
            onValueChange = onDireccionEmpresaChange,
            label = "Dirección de la empresa",
            leadingIcon = Icons.Default.LocationOn
        )
    }
}

// SECCIÓN: Configuración de Servicios
@Composable
fun ServiceConfigSection(
    atencionUrgencias: Boolean,
    onAtencionUrgenciasChange: (Boolean) -> Unit,
    vaDomicilio: Boolean,
    onVaDomicilioChange: (Boolean) -> Unit,
    turnosEnLocal: Boolean,
    onTurnosEnLocalChange: (Boolean) -> Unit,
    direccionLocal: String,
    onDireccionLocalChange: (String) -> Unit,
    provinciaLocal: String,
    onProvinciaLocalChange: (String) -> Unit,
    codigoPostalLocal: String,
    onCodigoPostalLocalChange: (String) -> Unit,
    serviceType: ServiceType,
    providerId: String? = null,
    isEmpresaMode: Boolean = false,
    direccionEmpresa: String = "",
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    ArchiveroSection(
        title = "Configuración de Servicios",
        sectionId = "services",
        icon = Icons.Default.Settings,
        color = Color(0xFF9C27B0),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        // Mostrar configuración según el tipo de servicio
        when (serviceType) {
            ServiceType.PROFESSIONAL -> {
                // Para profesionales con agenda
                AvailabilityScheduleSection(colors = colors)
            }
            ServiceType.TECHNICAL -> {
                // Para servicios técnicos (actual)
                TechnicalServiceConfig(
                    atencionUrgencias = atencionUrgencias,
                    onAtencionUrgenciasChange = onAtencionUrgenciasChange,
                    vaDomicilio = vaDomicilio,
                    onVaDomicilioChange = onVaDomicilioChange,
                    turnosEnLocal = turnosEnLocal,
                    onTurnosEnLocalChange = onTurnosEnLocalChange,
                    direccionLocal = direccionLocal,
                    onDireccionLocalChange = onDireccionLocalChange,
                    provinciaLocal = provinciaLocal,
                    onProvinciaLocalChange = onProvinciaLocalChange,
                    codigoPostalLocal = codigoPostalLocal,
                    onCodigoPostalLocalChange = onCodigoPostalLocalChange,
                    isEmpresaMode = isEmpresaMode,
                    direccionEmpresa = direccionEmpresa,
                    colors = colors
                )
            }
            ServiceType.RENTAL -> {
                // Para alquiler de espacios (canchas, salones, etc.)
                RentalSpacesSection(colors = colors, providerId = providerId)
            }
            ServiceType.OTHER -> {
                // Configuración genérica
                TechnicalServiceConfig(
                    atencionUrgencias = atencionUrgencias,
                    onAtencionUrgenciasChange = onAtencionUrgenciasChange,
                    vaDomicilio = vaDomicilio,
                    onVaDomicilioChange = onVaDomicilioChange,
                    turnosEnLocal = turnosEnLocal,
                    onTurnosEnLocalChange = onTurnosEnLocalChange,
                    direccionLocal = direccionLocal,
                    onDireccionLocalChange = onDireccionLocalChange,
                    provinciaLocal = provinciaLocal,
                    onProvinciaLocalChange = onProvinciaLocalChange,
                    codigoPostalLocal = codigoPostalLocal,
                    onCodigoPostalLocalChange = onCodigoPostalLocalChange,
                    isEmpresaMode = isEmpresaMode,
                    direccionEmpresa = direccionEmpresa,
                    colors = colors
                )
            }
        }
    }
}

// Extraer la configuración actual de servicios técnicos en su propia función
@Composable
private fun TechnicalServiceConfig(
    atencionUrgencias: Boolean,
    onAtencionUrgenciasChange: (Boolean) -> Unit,
    vaDomicilio: Boolean,
    onVaDomicilioChange: (Boolean) -> Unit,
    turnosEnLocal: Boolean,
    onTurnosEnLocalChange: (Boolean) -> Unit,
    direccionLocal: String,
    onDireccionLocalChange: (String) -> Unit,
    provinciaLocal: String,
    onProvinciaLocalChange: (String) -> Unit,
    codigoPostalLocal: String,
    onCodigoPostalLocalChange: (String) -> Unit,
    isEmpresaMode: Boolean,
    direccionEmpresa: String,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Column {
        // Mensaje informativo al inicio
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2196F3).copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configurá cómo prestás tus servicios. Podés activar una o más opciones.",
                    color = Color(0xFF2196F3),
                    fontSize = 13.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SwitchOption(
            label = "Atención de urgencias 24/7",
            icon = Icons.Default.Notifications,
            checked = atencionUrgencias,
            onCheckedChange = onAtencionUrgenciasChange,
            colors = colors,
            description = "Para emergencias fuera de horario normal"
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        SwitchOption(
            label = "Servicio a domicilio",
            icon = Icons.Default.DirectionsCar,
            checked = vaDomicilio,
            onCheckedChange = onVaDomicilioChange,
            colors = colors,
            description = "Te desplazás al domicilio del cliente"
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        SwitchOption(
            label = "Atención en local/taller",
            icon = Icons.Default.Store,
            checked = turnosEnLocal,
            onCheckedChange = onTurnosEnLocalChange,
            colors = colors,
            description = "El cliente va a tu local o taller"
        )
        
        // Campos de dirección del local (solo si turnosEnLocal está activo)
        AnimatedVisibility(
            visible = turnosEnLocal,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Si es modo empresa y tiene dirección de empresa, mostrarla
                if (isEmpresaMode && direccionEmpresa.isNotBlank()) {
                    // Mostrar dirección de la empresa
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.surfaceColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = colors.primaryOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dirección principal",
                                    color = colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = direccionEmpresa,
                                color = colors.textPrimary,
                                fontSize = 15.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sección de sucursales adicionales
                    SucursalesSection(colors = colors)
                    
                } else {
                    // Modo personal: campos editables
                    FloatingLabelTextField(
                        value = direccionLocal,
                        onValueChange = onDireccionLocalChange,
                        label = "Dirección del Local",
                        leadingIcon = Icons.Default.Store
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FloatingLabelTextField(
                        value = provinciaLocal,
                        onValueChange = onProvinciaLocalChange,
                        label = "Provincia",
                        leadingIcon = Icons.Default.Place
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FloatingLabelTextField(
                        value = codigoPostalLocal,
                        onValueChange = onCodigoPostalLocalChange,
                        label = "Código Postal",
                        leadingIcon = Icons.Default.PinDrop,
                        keyboardType = KeyboardType.Number
                    )
                }
            }
        }
    }
}

// COMPONENTE: Sección estilo Archivero (Colapsable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveroSection(
    title: String,
    sectionId: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandChange: (String) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = getPrestadorColors()
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Header clickable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandChange(sectionId) }
                        .padding(bottom = if (expanded) 16.dp else 0.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                        tint = color
                    )
                }
                
                // Contenido expandible
                androidx.compose.animation.AnimatedVisibility(
                    visible = expanded,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    Column {
                        content()
                    }
                }
            }
        }
    }
}

// COMPONENTE: Switch con icono
@Composable
fun SwitchOption(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    description: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.backgroundColor.copy(alpha = 0.5f))
            .clickable { onCheckedChange(!checked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) colors.primaryOrange else colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = colors.textPrimary,
                fontWeight = if (checked) FontWeight.Medium else FontWeight.Normal
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = colors.textSecondary.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primaryOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
            )
        )
    }
}
