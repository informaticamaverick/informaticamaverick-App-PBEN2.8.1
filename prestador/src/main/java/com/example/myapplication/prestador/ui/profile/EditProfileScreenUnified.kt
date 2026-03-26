package com.example.myapplication.prestador.ui.profile

import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.register.FloatingLabelTextField
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.DireccionUiState
import com.example.myapplication.prestador.viewmodel.DireccionViewModel
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState
import com.example.myapplication.prestador.viewmodel.UpdateState
import com.example.myapplication.prestador.viewmodel.ReferenteViewModel
import com.example.myapplication.prestador.viewmodel.ReferentesUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.viewmodel.PhotoUploadState
import com.example.myapplication.prestador.viewmodel.BusinessViewModel
import okhttp3.internal.http2.Header

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreenUnified(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
    direccionViewModel: DireccionViewModel = hiltViewModel(),
    referenteViewModel: ReferenteViewModel = hiltViewModel(),
    businessViewModel: BusinessViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val scope = rememberCoroutineScope()
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val photoUploadState by viewModel.photoUploadState.collectAsState()
    var isUploadingPhoto by remember { mutableStateOf(false)}
    val direccionUiState by direccionViewModel.uiState.collectAsState()
    val consultorioUiState by direccionViewModel.consultorioState.collectAsState()
    val referentesUiState by referenteViewModel.uiState.collectAsState()
    val direccionActual = (direccionUiState as? DireccionUiState.Success)?.direccion
    val referentesActuales = (referentesUiState as? ReferentesUiState.Success)?.referentes ?: emptyList()
    val businessEntity by viewModel.businessEntity.collectAsState()
    val allBusinesses by businessViewModel.businesses.collectAsState()
    var verificado by remember { mutableStateOf(false)}
    var imagenesProductos by remember { mutableStateOf("[]") }
    var categorias by remember { mutableStateOf("[]") }
    
    // Estados del formulario
    var name by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
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
    var envios by remember { mutableStateOf(false) }
    var turnosEnLocal by remember { mutableStateOf(false) }
    var direccionLocal by remember { mutableStateOf("") }
    var provinciaLocal by remember { mutableStateOf("") }
    var codigoPostalLocal by remember { mutableStateOf("") }
    var tieneEmpresa by remember { mutableStateOf(false) }
    var tieneSucursales by remember { mutableStateOf(false) }
    var atiendeVirtual by remember { mutableStateOf(false) }
    var trabajaConOtros by remember { mutableStateOf(false) }
    var nombreEmpresa by remember { mutableStateOf("") }
    var cuitEmpresa by remember { mutableStateOf("") }
    var direccionEmpresa by remember { mutableStateOf("") }
    var empresaGuardadaTrigger by remember { mutableStateOf(0) }
    var pendingEmpresaRefresh by remember { mutableStateOf(false) }
    var serviceType by remember { mutableStateOf(ServiceType.TECHNICAL) }
    var providerId by remember { mutableStateOf<String?>(null) }
    var horarioLocal by remember { mutableStateOf("") }
    var horarioCasaCentral by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

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
        uri?.let {
            selectedImageUri = it
            viewModel.uploadProfilePhoto(it)
        }
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
            apellido = provider.apellido ?: ""
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
            envios = provider.envios
            turnosEnLocal = provider.turnosEnLocal
            direccionLocal = provider.direccionLocal ?: ""
            provinciaLocal = provider.provinciaLocal ?: ""
            codigoPostalLocal = provider.codigoPostalLocal ?: ""
            horarioLocal = provider.horarioLocal?: ""
            tieneEmpresa = provider.tieneEmpresa
            atiendeVirtual = provider.atiendeVirtual
            trabajaConOtros = provider.trabajaConOtros
            nombreEmpresa = provider.nombreEmpresa ?: ""
            cuitEmpresa = provider.cuitEmpresa ?: ""
            direccionEmpresa = provider.direccionEmpresa ?: ""
            serviceType = ServiceType.fromString(provider.serviceType)
            imagenesProductos = businessEntity?.imagenesProductos ?: "[]"
            categorias = businessEntity?.categorias ?: provider.categories.ifBlank { "[]" }

            //Carga dirección y referentes
            direccionViewModel.loadDireccion(provider.id, "PRESTADOR")
            direccionViewModel.loadConsultorioDireccion(provider.id)
            referenteViewModel.loadReferentesByProvider()
            verificado = provider.verificado
        }
    }

    LaunchedEffect(photoUploadState) {
        when (val state = photoUploadState) {
            is PhotoUploadState.Loading -> {
                isUploadingPhoto = true
            }
            is PhotoUploadState.Success -> {
                isUploadingPhoto = false
                imageUrl = state.url
                snackbarHostState.showSnackbar("\u0005' Foto actualizada correctamente")
            }
            is PhotoUploadState.Error -> {
                isUploadingPhoto = false
                snackbarHostState.showSnackbar("L' Error: ${state.message}")
            }
            else -> isUploadingPhoto = false
        }
    }

    LaunchedEffect(updateState) {
        horarioCasaCentral = businessEntity?.horario ?: ""
    }
    
    // Mostrar mensaje de éxito
    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Success) {
            if (pendingEmpresaRefresh) {
                pendingEmpresaRefresh = false
                empresaGuardadaTrigger++
            }
            kotlinx.coroutines.delay(1500)
            viewModel.resetUpdateState()
        }
    }
    
    Scaffold(
        containerColor = colors.backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            Box {
                                HeaderSection(
                                    name = name,
                                    apellido = apellido,
                                    profesion = profesion,
                                    imageUrl = imageUrl,
                                    selectedImageUri = selectedImageUri,
                                    tieneEmpresa = tieneEmpresa,
                                    colors = colors,
                                    paddingValues = paddingValues,
                                    onBack = onBack,
                                    onImageClick = {
                                        if (!isUploadingPhoto) galleryLauncher.launch("image/*")
                                    }
                                )
                                if (isUploadingPhoto) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(start = 90.dp, bottom = 16.dp)
                                            .size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                        item {
                            if (verificado) {
                                VerificadoBadge(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
                            }
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        
                        item {
                                PersonalDataSection(
                                    name = name,
                                    onNameChange = { name = it },
                                    apellido = apellido,
                                    onApellidoChange = { apellido = it },
                                    email = email,
                                    onEmailChange = { email = it },
                                    phone = phone,
                                    onPhoneChange = { phone = it },
                                    dniCuit = dniCuit,
                                    onDniCuitChange = { dniCuit = it },
                                    expanded = expandedSection == "personal",
                                    onExpandChange = { expandedSection = if (expandedSection == "personal") null else "personal" },
                                    colors = colors
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            item {
                                DireccionSection(
                                    titulo = "Dirección personal",
                                    direccion = direccionActual,
                                    expanded = expandedSection == "direccion",
                                    onExpandChange = { expandedSection = if (expandedSection == "direccion") null else "direccion" },
                                    onGuardar = { pais, provincia, localidad, codigoPostal, calle, numero ->
                                        providerId?.let { id -> isDebugInspectorInfoEnabled
                                            direccionViewModel.guardarDireccion(
                                                referenciaId = id,
                                                referenciaTipo = "PRESTADOR",
                                                pais = pais,
                                                provincia = provincia,
                                                localidad = localidad,
                                                codigoPostal = codigoPostal,
                                                calle = calle,
                                                numero = numero
                                            )
                                        }
                                    }
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
                                    expanded = expandedSection == "professional",
                                    onExpandChange = { expandedSection = if (expandedSection == "professional") null else "professional" },
                                    colors = colors
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            item {
                                ArchiveroSection(
                                    title = "Categorías",
                                    sectionId = "categorias",
                                    icon = Icons.Default.Category,
                                    color = Color(0xFF00897B),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    expanded = expandedSection == "categorias",
                                    onExpandChange = { expandedSection = if (expandedSection == "categorias") null else "categorias" }
                                ) {
                                    CategoriasSelector(
                                        categoriasJson = categorias,
                                        onCategoriasActualizadas = { json ->
                                            categorias = json
                                            viewModel.updateCategorias(json)
                                        },
                                        serviceType = serviceType
                                    )
                                }
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            item {
                                GaleriaSection(
                                    imagenesJson = imagenesProductos,
                                    empresaId = businessEntity?.id ?: (providerId ?: ""),
                                    onImagenesActualizadas = { json ->
                                        imagenesProductos = json
                                        viewModel.updateImagenesProductos(json)
                                    },
                                    expanded = expandedSection == "galeria",
                                    onExpandChange = { expandedSection = if (expandedSection == "galeria") null else "galeria" },
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
                                        envios = envios,
                                        onEnviosChange = { envios = it },
                                        turnosEnLocal = turnosEnLocal,
                                        onTurnosEnLocalChange = { turnosEnLocal = it },
                                        direccionLocal = direccionLocal,
                                        onDireccionLocalChange = { direccionLocal = it },
                                        provinciaLocal = provinciaLocal,
                                        onProvinciaLocalChange = { provinciaLocal = it },
                                        codigoPostalLocal = codigoPostalLocal,
                                        onCodigoPostalLocalChange = { codigoPostalLocal = it },
                                        horarioLocal = horarioLocal,
                                        onHorarioLocalChange = { horarioLocal = it },
                                        serviceType = serviceType,
                                        onServiceTypeClick = { showServiceTypeDialog = true },
                                        providerId = providerId,
                                        isEmpresaMode = false,
                                        direccionEmpresa = "",
                                        expanded = expandedSection == "services",
                                        onExpandChange = { expandedSection = if (expandedSection == "services") null else "services" },
                                        colors = colors
                                    )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        if (serviceType == ServiceType.PROFESSIONAL) {
                            item {
                                ProfessionalModalidadBlock(
                                    turnosEnLocal = turnosEnLocal,
                                    onTurnosEnLocalChange = { value ->
                                        turnosEnLocal = value
                                        viewModel.updateProfile(turnosEnLocal = value)
                                    },
                                    vaDomicilio = vaDomicilio,
                                    onVaDomicilioChange = { value ->
                                        vaDomicilio = value
                                        viewModel.updateProfile(vaDomicilio = value)
                                    },
                                    atiendeVirtual = atiendeVirtual,
                                    onAtiendeVirtualChange = { value ->
                                        atiendeVirtual = value
                                        viewModel.updateProfile(atiendeVirtual = value)
                                    },
                                    provinciaLocal = provinciaLocal,
                                    onProvinciaLocalChange = { provinciaLocal = it },
                                    direccionLocal = direccionLocal,
                                    onDireccionLocalChange = { direccionLocal = it },
                                    codigoPostalLocal = codigoPostalLocal,
                                    onCodigoPostalLocalChange = { codigoPostalLocal = it },
                                    consultorioDireccion = (consultorioUiState as? DireccionUiState.Success)?.direccion,
                                    onGuardarConsultorio = { pais, provincia, localidad, cp, calle, numero ->
                                        val uid = providerId ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                        uid?.let { id ->
                                            direccionViewModel.guardarDireccion(
                                                referenciaId = id,
                                                referenciaTipo = "CONSULTORIO",
                                                pais = pais,
                                                provincia = provincia,
                                                localidad = localidad,
                                                codigoPostal = cp,
                                                calle = calle,
                                                numero = numero
                                            )
                                        }
                                    },
                                    colors = colors
                                )
                            }
                        } else {

                            // Toggle: ¿Tiene empresa registrada?
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                                    border = BorderStroke(1.5.dp, Color(0xFF9C27B0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { tieneEmpresa = !tieneEmpresa }
                                            .padding(horizontal = 20.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF9C27B0).copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Business,
                                                contentDescription = null,
                                                tint = Color(0xFF9C27B0),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Empresa registrada",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = if (tieneEmpresa) "Activo — mostrás datos de empresa" else "Activar para cargar datos de empresa",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                        Switch(
                                            checked = tieneEmpresa,
                                            onCheckedChange = { tieneEmpresa = it },
                                            modifier = Modifier.scale(0.85f),
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF9C27B0),
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = colors.textSecondary.copy(
                                                    alpha = 0.3f
                                                )
                                            )
                                        )
                                    }
                                }
                            }

                            item {
                                AnimatedVisibility(
                                    visible = tieneEmpresa,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 0.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Datos empresa

                                        CompanyDataSection(
                                            nombreEmpresa = nombreEmpresa,
                                            onNombreEmpresaChange = { nombreEmpresa = it },
                                            razonSocial = direccionEmpresa,
                                            onRazonSocialChange = { direccionEmpresa = it },
                                            cuitEmpresa = cuitEmpresa,
                                            onCuitEmpresaChange = { cuitEmpresa = it },
                                            expanded = expandedSection == "company",
                                            onExpandChange = {
                                                expandedSection =
                                                    if (expandedSection == "company") null else "company"
                                            },
                                            onGuardar = { nombre, razon, cuit ->
                                                pendingEmpresaRefresh = true
                                                viewModel.updateProfile(
                                                    nombreEmpresa = nombre,
                                                    direccionEmpresa = razon,
                                                    cuitEmpresa = cuit,
                                                    tieneEmpresa = true
                                                )
                                            },
                                            colors = colors
                                        )

                                        // Dirección empresa + referentes casa central
                                        DireccionSection(
                                            titulo = "Dirección Casa Central",
                                            direccion = direccionActual,
                                            expanded = expandedSection == "direccion_empresa",
                                            onExpandChange = {
                                                expandedSection =
                                                    if (expandedSection == "direccion_empresa") null else "direccion_empresa"
                                            },
                                            extraContent = {
                                                HorarioSelectorField(
                                                    horario = horarioCasaCentral,
                                                    onHorarioChange = { horarioCasaCentral = it },
                                                    label = "Horario de atención (Casa Central)"
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                ReferentesSection(
                                                    referentes = referentesActuales,
                                                    onAgregar = { nombre, apellido, cargo, imageUri ->
                                                        scope.launch {
                                                            val imageUrl: String? =
                                                                imageUri?.let { uri ->
                                                                    try {
                                                                        val ref =
                                                                            com.google.firebase.storage.FirebaseStorage.getInstance()
                                                                                .reference
                                                                                .child("referentes/$providerId/${java.util.UUID.randomUUID()}.jpg")
                                                                        ref.putFile(uri).await()
                                                                        ref.downloadUrl.await()
                                                                            .toString()
                                                                    } catch (e: Exception) {
                                                                        null
                                                                    }
                                                                }
                                                            referenteViewModel.addReferente(
                                                                nombre = nombre,
                                                                apellido = apellido.ifBlank { null },
                                                                cargo = cargo.ifBlank { null },
                                                                imageUrl = imageUrl
                                                            )
                                                        }
                                                    },
                                                    onDesactivar = { referente ->
                                                        referenteViewModel.desactivarReferente(
                                                            referente.id
                                                        )
                                                    }
                                                )
                                            },
                                            onGuardar = { pais, provincia, localidad, codigoPostal, calle, numero ->
                                                providerId?.let { id ->
                                                    direccionViewModel.guardarDireccion(
                                                        referenciaId = id,
                                                        referenciaTipo = "EMPRESA",
                                                        pais = pais,
                                                        provincia = provincia,
                                                        localidad = localidad,
                                                        codigoPostal = codigoPostal,
                                                        calle = calle,
                                                        numero = numero
                                                    )
                                                }
                                            }
                                        )

                                        // Sucursales
                                        ArchiveroSection(
                                            title = "Sucursales",
                                            sectionId = "sucursales",
                                            icon = Icons.Default.Store,
                                            color = colors.primaryOrange,
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            expanded = expandedSection == "sucursales",
                                            onExpandChange = {
                                                expandedSection =
                                                    if (expandedSection == "sucursales") null else "sucursales"
                                            }
                                        ) {
                                            SucursalesSection(
                                                colors = colors,
                                                refreshTrigger = empresaGuardadaTrigger,
                                                onUploadImage = { uri ->
                                                    try {
                                                        val uid =
                                                            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                                                ?: "unknown"
                                                        val ref =
                                                            com.google.firebase.storage.FirebaseStorage.getInstance()
                                                                .reference
                                                                .child("sucursales/$uid/${java.util.UUID.randomUUID()}.jpg")
                                                        ref.putFile(uri).await()
                                                        ref.downloadUrl.await().toString()
                                                    } catch (e: Exception) {
                                                        null
                                                    }
                                                },
                                                onSucursalAgregada = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("✅ Sucursal guardada correctamente")
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                            }
                        }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            item {
                                EmpleadosSection(
                                    trabajaConOtros = trabajaConOtros,
                                    onTrabajaConOtrosChange = { trabajaConOtros = it },
                                    expanded = expandedSection == "team",
                                    onExpandChange = { expandedSection = if (expandedSection == "team") null else "team" },
                                    isProfessional = serviceType == ServiceType.PROFESSIONAL
                                )
                            }
                    }
                    
                    // FAB GUARDAR
                    FloatingActionButton(
                        onClick = {
                            // Validar datos antes de guardar
                            if (turnosEnLocal) {
                                if (serviceType == ServiceType.PROFESSIONAL) {
                                    val consultorioDireccion = (consultorioUiState as? DireccionUiState.Success)?.direccion
                                    val consultorioOk = consultorioDireccion != null &&
                                        !consultorioDireccion.provincia.isNullOrBlank() &&
                                        !consultorioDireccion.codigoPostal.isNullOrBlank() &&
                                        !consultorioDireccion.calle.isNullOrBlank()

                                    if (!consultorioOk) {
                                        validationErrorMessage = "Guardá la dirección del consultorio/oficina para activar atención en local"
                                        showValidationError = true
                                        return@FloatingActionButton
                                    }

                                    // Mantener los campos del Provider sincronizados (se usan para guardar en 'usuarios')
                                    if (direccionLocal.isBlank()) {
                                        direccionLocal = listOfNotNull(
                                            consultorioDireccion!!.calle,
                                            consultorioDireccion.numero
                                        ).joinToString(" ").trim()
                                    }
                                    if (provinciaLocal.isBlank()) {
                                        provinciaLocal = consultorioDireccion!!.provincia ?: ""
                                    }
                                    if (codigoPostalLocal.isBlank()) {
                                        codigoPostalLocal = consultorioDireccion!!.codigoPostal ?: ""
                                    }
                                } else {
                                    if (direccionLocal.isBlank() || provinciaLocal.isBlank() || codigoPostalLocal.isBlank()) {
                                        validationErrorMessage = "Completá la dirección del local para activar atención en local"
                                        showValidationError = true
                                        return@FloatingActionButton
                                    }
                                }
                            }
                            
                            // Si pasa validación, guardar
                            viewModel.updateProfile(
                            name = name,
                            apellido = apellido,
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
                            envios = envios,
                            turnosEnLocal = turnosEnLocal,
                            direccionLocal = direccionLocal.takeIf { turnosEnLocal },
                            provinciaLocal = provinciaLocal.takeIf { turnosEnLocal },
                            codigoPostalLocal = codigoPostalLocal.takeIf { turnosEnLocal },
                            tieneEmpresa = tieneEmpresa,
                                atiendeVirtual = atiendeVirtual,
                            trabajaConOtros = trabajaConOtros,
                            nombreEmpresa = nombreEmpresa.takeIf { tieneEmpresa },
                            cuitEmpresa = cuitEmpresa.takeIf { tieneEmpresa },
                            direccionEmpresa = direccionEmpresa.takeIf { tieneEmpresa },
                            serviceType = serviceType.name,
                            horarioLocal = horarioLocal.takeIf { turnosEnLocal }
                        )
                            if (tieneEmpresa && horarioCasaCentral.isNotBlank()) {
                                viewModel.updateHorarioCasaCentral(horarioCasaCentral)
                            }
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
    apellido: String,
    profesion: String,
    imageUrl: String?,
    selectedImageUri: Uri?,
    tieneEmpresa: Boolean,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onImageClick: () -> Unit
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
                text = "$name ${apellido}".trim().ifEmpty { "Prestador" },
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
    }
}

// SECCIÓN: Datos Personales
@Composable
fun PersonalDataSection(
    name: String,
    onNameChange: (String) -> Unit,
    apellido: String,
    onApellidoChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    dniCuit: String,
    onDniCuitChange: (String) -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
){
    ArchiveroSection(
        title = "Datos Personales",
        sectionId = "personal",
        icon = Icons.Default.Person,
        color = colors.primaryOrange,
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        // Nombre + Apellido en la misma fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FloatingLabelTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Nombre",
                    leadingIcon = Icons.Default.Person
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FloatingLabelTextField(
                    value = apellido,
                    onValueChange = onApellidoChange,
                    label = "Apellido",
                    leadingIcon = Icons.Default.Person
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Email — con opción de cambio vía reautenticación + verificación Firebase
        var showEmailDialog by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = colors.surfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Email", fontSize = 11.sp, color = colors.textSecondary)
                    Text(
                        text = email.ifBlank { "—" },
                        fontSize = 14.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(
                    onClick = { showEmailDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Cambiar", fontSize = 12.sp, color = colors.primaryOrange)
                }
            }
        }

        if (showEmailDialog) {
            CambiarEmailDialog(
                onDismiss = { showEmailDialog = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FloatingLabelTextField(
                value = phone,
                onValueChange = { if (it.all { c -> c.isDigit() || c == ' ' || c == '-' }) onPhoneChange(it) },
                label = "Telefono",
                leadingIcon = null,
                leadingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null,
                            tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                        Text("+54", color = colors.textSecondary, fontSize = 12.sp)
                    }
                },
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.weight(1f)
            )

            FloatingLabelTextField(
                value = dniCuit,
                onValueChange = { if (it.all { c -> c.isDigit() }) onDniCuitChange(it) },
                label = "DNI / CUIT",
                leadingIcon = Icons.Default.Badge,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)

            )
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
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    // Mapeo profesión → label + hint de matrícula
    val matriculaInfo = remember(profesion) {
        when {
            profesion.contains("gasista", ignoreCase = true) ||
            profesion.contains("gas", ignoreCase = true) ->
                Pair("Habilitaci\u00f3n ENARGAS", "Ej: MAT-12345 \u2014 emitida por ENARGAS")
            profesion.contains("m\u00e9dico", ignoreCase = true) ||
            profesion.contains("medico", ignoreCase = true) ||
            profesion.contains("doctor", ignoreCase = true) ->
                Pair("Matr\u00edcula M\u00e9dica (MP/MN)", "Ej: MP 12345 \u2014 Matr\u00edcula Provincial o Nacional")
            profesion.contains("abogado", ignoreCase = true) ->
                Pair("Tomo y Folio", "Ej: T.XV F.123 \u2014 Colegio de Abogados")
            profesion.contains("contador", ignoreCase = true) ||
            profesion.contains("contable", ignoreCase = true) ->
                Pair("Matr\u00edcula FACPCE", "Ej: 12345 \u2014 Consejo Profesional de Cs. Econ\u00f3micas")
            profesion.contains("arquitecto", ignoreCase = true) ||
            profesion.contains("ingeniero", ignoreCase = true) ->
                Pair("Matr\u00edcula Profesional", "Ej: 12345 \u2014 Consejo Profesional de Ing./Arq.")
            profesion.contains("electricista", ignoreCase = true) ->
                Pair("Habilitaci\u00f3n El\u00e9ctrica", "Ej: HAB-12345 \u2014 ENRE o habilitaci\u00f3n municipal")
            profesion.contains("plomero", ignoreCase = true) ||
            profesion.contains("plomer", ignoreCase = true) ->
                Pair("Habilitaci\u00f3n Municipal", "Ej: 12345 \u2014 habilitaci\u00f3n municipal")
            profesion.contains("psic\u00f3logo", ignoreCase = true) ||
            profesion.contains("psicologo", ignoreCase = true) ->
                Pair("Matr\u00edcula Profesional", "Ej: 12345 \u2014 Colegio de Psic\u00f3logos")
            profesion.contains("farmac\u00e9utico", ignoreCase = true) ||
            profesion.contains("farmaceutico", ignoreCase = true) ->
                Pair("Matr\u00edcula Farmac\u00e9utica", "Ej: 12345 \u2014 Colegio de Farm.")
            else ->
                Pair("N\u00famero de Matr\u00edcula", "Ingres\u00e1 el n\u00famero de tu matr\u00edcula o habilitaci\u00f3n")
        }
    }

    ArchiveroSection(
        title = "Datos Profesionales",
        sectionId = "professional",
        icon = Icons.Default.Work,
        color = Color(0xFF4CAF50),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        FloatingLabelTextField(
            value = profesion,
            onValueChange = onProfesionChange,
            label = "Profesi\u00f3n / Oficio",
            leadingIcon = Icons.Default.Work
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Switch matrícula
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTieneMatriculaChange(!tieneMatricula) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CardMembership, contentDescription = null,
                tint = colors.textSecondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "\u00bfTiene matr\u00edcula profesional?",
                color = colors.textPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
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

        // Campo matrícula + mensajes
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
                    label = matriculaInfo.first,
                    leadingIcon = Icons.Default.CardMembership
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Hint del formato
                Text(
                    text = "\uD83D\uDCA1 ${matriculaInfo.second}",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Banner verificación pendiente
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null,
                            tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                        Column {
                            Text("\u23F3 Verificaci\u00f3n pendiente",
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE65100))
                            Text("Pr\u00f3ximamente podr\u00e1s verificar tu matr\u00edcula para obtener el badge \u2705 en tu perfil.",
                                fontSize = 11.sp, color = Color(0xFFBF360C),
                                lineHeight = 15.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FloatingLabelTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Descripci\u00f3n / Sobre m\u00ed",
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
razonSocial: String,
onRazonSocialChange: (String) -> Unit,
cuitEmpresa: String,
onCuitEmpresaChange: (String) -> Unit,
expanded: Boolean,
onExpandChange: () -> Unit,
onGuardar: (nombre: String, razonSocial: String, cuit: String) -> Unit,
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
        var editando by remember { mutableStateOf(nombreEmpresa.isBlank()) }
        var cuitError by remember { mutableStateOf("") }
        var localNombre by remember(nombreEmpresa) { mutableStateOf(nombreEmpresa) }
        var localRazon by remember(razonSocial) { mutableStateOf(razonSocial) }
        var cuitValue by remember(cuitEmpresa) {
            mutableStateOf(TextFieldValue(cuitEmpresa, TextRange(cuitEmpresa.length)))
        }

        if (!editando && nombreEmpresa.isNotBlank()) {
            // ── MODO LECTURA ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                            Icon(Icons.Default.Business, contentDescription = null,
                                tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Nombre", fontSize = 11.sp, color = colors.textSecondary)
                            Text(nombreEmpresa, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (razonSocial.isNotBlank()) {
                        HorizontalDivider(color = colors.border)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                                Icon(Icons.Default.BusinessCenter, contentDescription = null,
                                    tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Razón social", fontSize = 11.sp, color = colors.textSecondary)
                                Text(razonSocial, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    if (cuitEmpresa.isNotBlank()) {
                        HorizontalDivider(color = colors.border)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = colors.primaryOrange.copy(alpha = 0.12f)) {
                                Icon(Icons.Default.Numbers, contentDescription = null,
                                    tint = colors.primaryOrange, modifier = Modifier.padding(6.dp).size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("CUIT", fontSize = 11.sp, color = colors.textSecondary)
                                Text(cuitEmpresa, fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { editando = true },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Editar", fontSize = 13.sp)
            }
        } else {
            // ── MODO EDICIÓN ──
            FloatingLabelTextField(
                value = localNombre,
                onValueChange = { localNombre = it },
                label = "Nombre de la empresa",
                leadingIcon = Icons.Default.Business
            )
            Spacer(modifier = Modifier.height(12.dp))

            FloatingLabelTextField(
                value = localRazon,
                onValueChange = { localRazon = it },
                label = "Razón social",
                leadingIcon = Icons.Default.BusinessCenter
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cuitValue,
                onValueChange = { tvInput ->
                    val soloDigitos = tvInput.text.filter { c -> c.isDigit() }.take(11)
                    val formateado = when {
                        soloDigitos.length <= 2 -> soloDigitos
                        soloDigitos.length <= 10 -> "${soloDigitos.take(2)}-${soloDigitos.drop(2)}"
                        else -> "${soloDigitos.take(2)}-${soloDigitos.drop(2).take(8)}-${soloDigitos[10]}"
                    }
                    cuitValue = TextFieldValue(formateado, TextRange(formateado.length))
                    cuitError = if (soloDigitos.length == 11 && !validarCuit(soloDigitos)) "CUIT inválido" else ""
                },
                label = { Text("CUIT") },
                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = colors.textSecondary) },
                isError = cuitError.isNotEmpty(),
                supportingText = if (cuitError.isNotEmpty()) {
                    { Text(cuitError, color = MaterialTheme.colorScheme.error) }
                } else null,
                placeholder = { Text("XX-XXXXXXXX-X") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    focusedLabelColor = colors.primaryOrange,
                    unfocusedBorderColor = colors.border
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (nombreEmpresa.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            localNombre = nombreEmpresa
                            localRazon = razonSocial
                            cuitValue = TextFieldValue(cuitEmpresa, TextRange(cuitEmpresa.length))
                            cuitError = ""
                            editando = false
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) { Text("Cancelar", fontSize = 13.sp) }
                }
                Button(
                    onClick = {
                        if (localNombre.isBlank()) return@Button
                        if (cuitError.isNotEmpty()) return@Button
                        onNombreEmpresaChange(localNombre)
                        onRazonSocialChange(localRazon)
                        onCuitEmpresaChange(cuitValue.text)
                        onGuardar(localNombre, localRazon, cuitValue.text)
                        editando = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) { Text("Guardar", fontSize = 13.sp) }
            }
        }
    }
}

private fun validarCuit(digits: String): Boolean {
    if (digits.length != 11) return false
    val weight = intArrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)
    val sum = weight.indices.sumOf { digits[it].digitToInt() * weight[it] }
    val resto = sum % 11
    val digitoVerificador = if (resto == 0) 0 else 11 - resto
    if (digitoVerificador == 10) return false
    return digitoVerificador == digits[10].digitToInt()
}

// SECCIÓN: Configuración de Servicios
@Composable
fun ServiceConfigSection(
    atencionUrgencias: Boolean,
    onAtencionUrgenciasChange: (Boolean) -> Unit,
    vaDomicilio: Boolean,
    onVaDomicilioChange: (Boolean) -> Unit,
    envios: Boolean,
    onEnviosChange: (Boolean) -> Unit,
    turnosEnLocal: Boolean,
    onTurnosEnLocalChange: (Boolean) -> Unit,
    direccionLocal: String,
    onDireccionLocalChange: (String) -> Unit,
    provinciaLocal: String,
    onProvinciaLocalChange: (String) -> Unit,
    codigoPostalLocal: String,
    onCodigoPostalLocalChange: (String) -> Unit,
    horarioLocal: String,
    onHorarioLocalChange: (String) -> Unit,
    serviceType: ServiceType,
    onServiceTypeClick: () -> Unit,
    providerId: String? = null,
    isEmpresaMode: Boolean = false,
    direccionEmpresa: String = "",
    expanded: Boolean,
    onExpandChange: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    var mostrarInfoServicio by remember { mutableStateOf(false) }

    ArchiveroSection(
        title = "Configuración de Servicios",
        sectionId = "services",
        icon = Icons.Default.Settings,
        color = Color(0xFF9C27B0),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() },
        headerTrailingContent = {
            Box {
                IconButton(
                    onClick = { mostrarInfoServicio = !mostrarInfoServicio },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = "Información",
                        tint = if (mostrarInfoServicio) Color(0xFF2196F3) else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = mostrarInfoServicio,
                    onDismissRequest = { mostrarInfoServicio = false }
                ) {
                    Column(modifier = Modifier.padding(12.dp).widthIn(max = 260.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (serviceType == ServiceType.PROFESSIONAL) "Configurá tu agenda:" else "Configurá tus servicios:",
                                color = Color(0xFF2196F3),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val bullets = if (serviceType == ServiceType.PROFESSIONAL) listOf(
                            "Agregá los días y horarios en que atendés.",
                            "Podés tener múltiples franjas horarias (mañana y tarde).",
                            "La duración del turno define cada cuánto se reservan citas.",
                            "Tus clientes verán solo los turnos disponibles."
                        ) else listOf(
                            "Podés activar una o más opciones.",
                            "Urgencias 24/7: emergencias fuera de horario.",
                            "A domicilio: te desplazás al cliente.",
                            "Envíos: mandás productos o materiales.",
                            "Turnos en local: el cliente viene a vos."
                        )
                        bullets.forEach { item ->
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•  ", color = Color(0xFF2196F3), fontSize = 13.sp)
                                Text(item, color = colors.textPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    ) {
        // Tipo de servicio — siempre visible arriba
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
                    Text(text = "Tipo de servicio", fontSize = 12.sp, color = colors.textSecondary)
                    Text(
                        text = serviceType.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primaryOrange
                    )
                }
                Icon(Icons.Default.Edit, contentDescription = "Cambiar", tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Configuración según el tipo de servicio
        when (serviceType) {
            ServiceType.PROFESSIONAL -> {
                // Para profesionales con agenda
                AvailabilityScheduleSection(colors = colors)
            }
            ServiceType.TECHNICAL -> {
                TechnicalServiceConfig(
                    atencionUrgencias = atencionUrgencias,
                    onAtencionUrgenciasChange = onAtencionUrgenciasChange,
                    vaDomicilio = vaDomicilio,
                    onVaDomicilioChange = onVaDomicilioChange,
                    envios = envios,
                    onEnviosChange = onEnviosChange,
                    turnosEnLocal = turnosEnLocal,
                    onTurnosEnLocalChange = onTurnosEnLocalChange,
                    direccionLocal = direccionLocal,
                    onDireccionLocalChange = onDireccionLocalChange,
                    provinciaLocal = provinciaLocal,
                    onProvinciaLocalChange = onProvinciaLocalChange,
                    codigoPostalLocal = codigoPostalLocal,
                    onCodigoPostalLocalChange = onCodigoPostalLocalChange,
                    isEmpresaMode = isEmpresaMode,
                    horarioLocal = horarioLocal,
                    onHorarioLocalChange = onHorarioLocalChange,
                    direccionEmpresa = direccionEmpresa,
                    mostrarInfoServicio = mostrarInfoServicio,
                    colors = colors
                )
            }
            ServiceType.RENTAL -> {
                RentalSpacesSection(colors = colors, providerId = providerId)
            }
            ServiceType.OTHER -> {
                TechnicalServiceConfig(
                    atencionUrgencias = atencionUrgencias,
                    onAtencionUrgenciasChange = onAtencionUrgenciasChange,
                    vaDomicilio = vaDomicilio,
                    onVaDomicilioChange = onVaDomicilioChange,
                    envios = envios,
                    onEnviosChange = onEnviosChange,
                    turnosEnLocal = turnosEnLocal,
                    onTurnosEnLocalChange = onTurnosEnLocalChange,
                    direccionLocal = direccionLocal,
                    onDireccionLocalChange = onDireccionLocalChange,
                    provinciaLocal = provinciaLocal,
                    onProvinciaLocalChange = onProvinciaLocalChange,
                    codigoPostalLocal = codigoPostalLocal,
                    onCodigoPostalLocalChange = onCodigoPostalLocalChange,
                    isEmpresaMode = isEmpresaMode,
                    horarioLocal = horarioLocal,
                    onHorarioLocalChange = onHorarioLocalChange,
                    direccionEmpresa = direccionEmpresa,
                    mostrarInfoServicio = mostrarInfoServicio,
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
    envios: Boolean,
    onEnviosChange: (Boolean) -> Unit,
    turnosEnLocal: Boolean,
    onTurnosEnLocalChange: (Boolean) -> Unit,
    direccionLocal: String,
    onDireccionLocalChange: (String) -> Unit,
    provinciaLocal: String,
    onProvinciaLocalChange: (String) -> Unit,
    codigoPostalLocal: String,
    onCodigoPostalLocalChange: (String) -> Unit,
    isEmpresaMode: Boolean,
    horarioLocal: String,
    onHorarioLocalChange: (String) -> Unit,
    direccionEmpresa: String,
    mostrarInfoServicio: Boolean,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Column {
        Spacer(modifier = Modifier.height(4.dp))
        
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
            label = "Envíos",
            icon = Icons.Default.LocalShipping,
            checked = envios,
            onCheckedChange = onEnviosChange,
            colors = colors,
            description = "Realizás envíos de productos o materiales"
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
                    
                } else {
                    // Modo personal: campos editables con autocomplete
                    var mostrarSugsProvLocal by remember { mutableStateOf(false) }
                    var mostrarSugsLocalLocal by remember { mutableStateOf(false) }
                    var localidadLocalTexto by remember { mutableStateOf("") }
                    val provInteraction = remember { MutableInteractionSource() }
                    val locInteraction = remember { MutableInteractionSource() }
                    val provFocused by provInteraction.collectIsFocusedAsState()
                    val locFocused by locInteraction.collectIsFocusedAsState()
                    val provinciasFiltradas = if (provinciaLocal.isBlank()) emptyList()
                        else PROVINCIAS_ARGENTINA.filter { it.contains(provinciaLocal.trim(), ignoreCase = true) }
                    val localidadesDeProv = LOCALIDADES_POR_PROVINCIA.entries
                        .firstOrNull { it.key.equals(provinciaLocal.trim(), ignoreCase = true) }?.value ?: emptyList()
                    val localidadesFiltradas = if (localidadLocalTexto.isBlank()) localidadesDeProv
                        else localidadesDeProv.filter { it.nombre.contains(localidadLocalTexto.trim(), ignoreCase = true) }

                    LaunchedEffect(provFocused) { if (provFocused) mostrarSugsProvLocal = true }
                    LaunchedEffect(locFocused) { if (locFocused) mostrarSugsLocalLocal = true }

                    // 1) Provincia con autocomplete
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FloatingLabelTextField(
                            value = provinciaLocal,
                            onValueChange = { onProvinciaLocalChange(it); mostrarSugsProvLocal = true },
                            label = "Provincia",
                            leadingIcon = Icons.Default.Place,
                            interactionSource = provInteraction
                        )
                        AnimatedVisibility(
                            visible = mostrarSugsProvLocal && provinciasFiltradas.isNotEmpty(),
                            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                        ) {
                            Surface(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    provinciasFiltradas.take(6).forEach { prov ->
                                        Text(
                                            text = prov,
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                onProvinciaLocalChange(prov)
                                                mostrarSugsProvLocal = false
                                            }.padding(horizontal = 16.dp, vertical = 12.dp),
                                            fontSize = 14.sp
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2) Direccion y Localidad en fila
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = rememberCoroutineScope()
                    var geocodingLoading by remember { mutableStateOf(false) }
                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        if (granted) {
                            scope.launch {
                                geocodingLoading = true
                                try {
                                    val fusedClient = com.google.android.gms.location.LocationServices
                                        .getFusedLocationProviderClient(context)
                                    @Suppress("MissingPermission")
                                    val loc = fusedClient.lastLocation.await()
                                    if (loc != null) {
                                        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                        @Suppress("DEPRECATION")
                                        val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                        if (!addresses.isNullOrEmpty()) {
                                            val addr = addresses[0]
                                            val calle = buildString {
                                                if (!addr.thoroughfare.isNullOrBlank()) append(addr.thoroughfare)
                                                if (!addr.subThoroughfare.isNullOrBlank()) append(" ${addr.subThoroughfare}")
                                            }
                                            if (calle.isNotBlank()) onDireccionLocalChange(calle)
                                            if (!addr.locality.isNullOrBlank()) localidadLocalTexto = addr.locality!!
                                            if (!addr.adminArea.isNullOrBlank()) onProvinciaLocalChange(addr.adminArea!!)
                                            if (!addr.postalCode.isNullOrBlank()) onCodigoPostalLocalChange(addr.postalCode!!)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // silently ignore
                                } finally {
                                    geocodingLoading = false
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FloatingLabelTextField(
                            value = direccionLocal,
                            onValueChange = onDireccionLocalChange,
                            label = "Direcci\u00f3n",
                            leadingIcon = Icons.Default.Store,
                            trailingContent = {
                                if (geocodingLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primaryOrange)
                                } else {
                                    IconButton(onClick = {
                                        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                    }) {
                                        Icon(Icons.Default.MyLocation, contentDescription = "Detectar ubicaci\u00f3n", tint = colors.primaryOrange)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            FloatingLabelTextField(
                                value = localidadLocalTexto,
                                onValueChange = { localidadLocalTexto = it; mostrarSugsLocalLocal = true },
                                label = "Localidad",
                                leadingIcon = Icons.Default.LocationCity,
                                interactionSource = locInteraction
                            )
                            AnimatedVisibility(
                                visible = mostrarSugsLocalLocal && localidadesFiltradas.isNotEmpty(),
                                enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                            ) {
                                Surface(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        localidadesFiltradas.take(5).forEach { loc ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    localidadLocalTexto = loc.nombre
                                                    onCodigoPostalLocalChange(loc.codigoPostal)
                                                    mostrarSugsLocalLocal = false
                                                }.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(loc.nombre, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                                Text(loc.codigoPostal, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3) Codigo Postal
                    FloatingLabelTextField(
                        value = codigoPostalLocal,
                        onValueChange = { v ->
                            val filtered = v.filter { it.isDigit() || it.isLetter() }.uppercase()
                            onCodigoPostalLocalChange(filtered)
                        },
                        label = "Codigo Postal",
                        leadingIcon = Icons.Default.PinDrop,
                        keyboardType = KeyboardType.Text
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    HorarioSelectorField(
                        horario = horarioLocal,
                        onHorarioChange = onHorarioLocalChange
                    )
                }
            }
        }
    }
}

// COMPONENTE: Sección estilo Archivero (Colapsable)
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ArchiveroSection(
    title: String,
    sectionId: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandChange: (String) -> Unit = {},
    headerTrailingContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = getPrestadorColors()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

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
                    headerTrailingContent?.invoke()
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
                    LaunchedEffect(true) {
                        kotlinx.coroutines.delay(300)
                        bringIntoViewRequester.bringIntoView()
                    }
                    Column(modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester)) {
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


// Sección independiente: Portafolio de fotos
@Composable
fun GaleriaSection(
    imagenesJson: String,
    empresaId: String,
    onImagenesActualizadas: (String) -> Unit,
    expanded: Boolean,
    onExpandChange: (String) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    ArchiveroSection(
        title = "Portafolio",
        sectionId = "galeria",
        icon = Icons.Default.PhotoLibrary,
        color = Color(0xFF1976D2),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = onExpandChange
    ) {
        GaleriaInlineContent(
            imagenesJson = imagenesJson,
            empresaId = empresaId,
            onImagenesActualizadas = onImagenesActualizadas,
            colors = colors
        )
    }
}

// Galería inline
@Composable
fun GaleriaInlineContent(
    imagenesJson: String,
    empresaId: String,
    onImagenesActualizadas: (String) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    val scope = rememberCoroutineScope()
    var imagenes: List<String> by remember(imagenesJson) {
        mutableStateOf<List<String>>(
            try {
                val arr = org.json.JSONArray(imagenesJson)
                (0 until arr.length()).map { i -> arr.getString(i) }
            } catch (e: Exception) {
                emptyList()
            }
        )
    }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploading = true
                val nuevaUrl: String? = try {
                    val ref = com.google.firebase.storage.FirebaseStorage.getInstance()
                        .reference
                        .child("empresas/$empresaId/galeria/${java.util.UUID.randomUUID()}.jpg")
                    ref.putFile(selectedUri).await()
                    ref.downloadUrl.await().toString()
                } catch (e: Exception) { null }
                if (nuevaUrl != null) {
                    val nuevaLista: List<String> = imagenes + nuevaUrl
                    imagenes = nuevaLista
                    val arr = org.json.JSONArray()
                    nuevaLista.forEach { s -> arr.put(s) }
                    onImagenesActualizadas(arr.toString())
                }
                isUploading = false
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Photo,
                contentDescription = null,
                tint = colors.primaryOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Galeria de imágenes (${imagenes.size}/5)",
                fontSize = 15.sp,
                color = colors.textPrimary
            )
        }
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colors.primaryOrange,
                strokeWidth = 2.dp
            )
        } else {
            val llena = imagenes.size >= 5
            OutlinedButton(
                onClick = { if (!llena) launcher.launch("image/*") },
                enabled = !llena,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                border = BorderStroke(1.dp, if (llena) colors.textSecondary else
                colors.primaryOrange),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (llena) "Limite 5 fotos" else "Agregar", fontSize = 13.sp)
            }

        }
    }

    if (imagenes.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, colors.textSecondary.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Sin fotos a\u00fan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
                Text(
                    text = "Mostr\u00e1 tus trabajos agregando fotos",
                    fontSize = 12.sp,
                    color = colors.textSecondary.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        // Grilla 2 columnas
        val filas = imagenes.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filas.forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    fila.forEach { url ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Botón eliminar sobre la imagen
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .clickable {
                                        val nuevaLista: List<String> = imagenes.filter { it != url }
                                        imagenes = nuevaLista
                                        val arr = org.json.JSONArray()
                                        nuevaLista.forEach { s -> arr.put(s) }
                                        onImagenesActualizadas(arr.toString())
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    // Si la fila tiene solo 1 foto, completar con placeholder vacío
                    if (fila.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarEmailDialog(onDismiss: () -> Unit) {
    val colors = getPrestadorColors()
    var nuevoEmail by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val esUsuarioGoogle = remember {
        com.google.firebase.auth.FirebaseAuth.getInstance()
            .currentUser?.providerData
            ?.any { it.providerId == "google.com" } == true
    }

    AlertDialog(
        onDismissRequest = { if (!cargando) onDismiss() },
        containerColor = colors.surfaceColor,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(Icons.Default.Email, contentDescription = null, tint = colors.primaryOrange,
                modifier = Modifier.size(28.dp))
        },
        title = {
            Text("Cambiar email", fontWeight = FontWeight.Bold, color = colors.textPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                if (esUsuarioGoogle) {
                    // Usuario Google — no puede cambiar email desde aquí
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = colors.primaryOrange,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Text(
                                "Tu cuenta usa Google como m\u00e9todo de inicio de sesi\u00f3n. " +
                                "El email est\u00e1 administrado por Google y no puede cambiarse desde aqu\u00ed.\n\n" +
                                "Para modificarlo, hac\u00e9lo desde tu cuenta de Google.",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Text(
                        "Ingres\u00e1 tu nuevo email y tu contrase\u00f1a actual para confirmar el cambio. " +
                        "Te enviaremos un email de verificaci\u00f3n.",
                        fontSize = 13.sp, color = colors.textSecondary
                    )

                    OutlinedTextField(
                        value = nuevoEmail,
                        onValueChange = { nuevoEmail = it; estado = null },
                        label = { Text("Nuevo email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            focusedLabelColor = colors.primaryOrange,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it; estado = null },
                        label = { Text("Contrase\u00f1a actual") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                                Icon(
                                    if (contrasenaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (contrasenaVisible)
                            androidx.compose.ui.text.input.VisualTransformation.None
                        else
                            androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            focusedLabelColor = colors.primaryOrange,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    estado?.let { msg ->
                        val esError = msg.startsWith("Error") || msg.startsWith("La")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (esError) colors.error.copy(alpha = 0.1f)
                                    else colors.success.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (esError) colors.error else colors.success,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!esUsuarioGoogle) {
                Button(
                    onClick = {
                        if (nuevoEmail.isBlank() || contrasena.isBlank()) {
                            estado = "Complet\u00e1 todos los campos."
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(nuevoEmail).matches()) {
                            estado = "La direcci\u00f3n de email no es v\u00e1lida."
                            return@Button
                        }
                        cargando = true
                        scope.launch {
                            try {
                                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                    ?: throw Exception("Sesi\u00f3n no encontrada.")
                                val credential = com.google.firebase.auth.EmailAuthProvider
                                    .getCredential(user.email ?: "", contrasena)
                                user.reauthenticate(credential).await()
                                user.verifyBeforeUpdateEmail(nuevoEmail).await()
                                estado = "\u2705 Te enviamos un link de verificaci\u00f3n a $nuevoEmail. " +
                                         "El cambio se aplicar\u00e1 cuando confirmes."
                            } catch (e: Exception) {
                                estado = "Error: ${
                                    when {
                                        e.message?.contains("password") == true ||
                                        e.message?.contains("credential") == true ->
                                            "Contrase\u00f1a incorrecta."
                                        e.message?.contains("email") == true ->
                                            "Email inv\u00e1lido o ya en uso."
                                        else -> e.message ?: "Ocurri\u00f3 un error."
                                    }
                                }"
                            } finally {
                                cargando = false
                            }
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(color = Color.White,
                            modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Confirmar", color = Color.White)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!cargando) onDismiss() }) {
                Text("Cancelar", color = colors.textSecondary)
            }
        }
    )
}



@Composable
private fun ProfessionalModalidadBlock(
    turnosEnLocal: Boolean,
    onTurnosEnLocalChange: (Boolean) -> Unit,
    vaDomicilio: Boolean,
    onVaDomicilioChange: (Boolean) -> Unit,
    atiendeVirtual: Boolean,
    onAtiendeVirtualChange: (Boolean) -> Unit,
    provinciaLocal: String,
    onProvinciaLocalChange: (String) -> Unit,
    direccionLocal: String,
    onDireccionLocalChange: (String) -> Unit,
    codigoPostalLocal: String,
    onCodigoPostalLocalChange: (String) -> Unit,
    consultorioDireccion: com.example.myapplication.prestador.data.local.entity.DireccionEntity?,
    onGuardarConsultorio: (pais: String, provincia: String, localidad: String, cp: String, calle: String, numero: String) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    val purple = Color(0xFF9C27B0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModalidadToggleCard(
            icon = Icons.Default.MeetingRoom,
            label = "En consultorio / oficina",
            description = if (turnosEnLocal) "El cliente va a tu consultorio" else "Activar para cargar direccion",
            checked = turnosEnLocal,
            onCheckedChange = onTurnosEnLocalChange,
            accentColor = purple,
            colors = colors
        )
        AnimatedVisibility(
            visible = turnosEnLocal,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            var consultorioExpanded by remember { mutableStateOf(consultorioDireccion == null) }
            DireccionSection(
                titulo = "Direccion del consultorio / oficina",
                direccion = consultorioDireccion,
                expanded = consultorioExpanded,
                onExpandChange = { consultorioExpanded = !consultorioExpanded },
                onGuardar = onGuardarConsultorio
            )
        }
        ModalidadToggleCard(
            icon = Icons.Default.DirectionsCar,
            label = "Visitas a domicilio",
            description = "Te desplazas al domicilio del cliente",
            checked = vaDomicilio,
            onCheckedChange = onVaDomicilioChange,
            accentColor = purple,
            colors = colors
        )
        ModalidadToggleCard(
            icon = Icons.Default.VideoCall,
            label = "Atencion online / virtual",
            description = "Atendes por videollamada u otro medio",
            checked = atiendeVirtual,
            onCheckedChange = onAtiendeVirtualChange,
            accentColor = purple,
            colors = colors
        )
    }
}

@Composable
private fun ModalidadToggleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        border = BorderStroke(1.5.dp, accentColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(description, fontSize = 12.sp, color = colors.textSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.scale(0.85f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
                )
            )
        }
    }
}
