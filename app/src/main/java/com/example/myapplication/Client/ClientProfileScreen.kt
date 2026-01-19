package com.example.myapplication.Client

import android.R
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.Profile.ProfileViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import kotlin.contracts.contract

@Composable
fun ClientProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember {mutableStateOf(false)}
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showAddressManagerDialog by remember { mutableStateOf(false) }
    
    // Estados para confirmación de cambios
    var showConfirmEmailChangeDialog by remember { mutableStateOf(false) }
    var showConfirmPasswordChangeDialog by remember { mutableStateOf(false) }
    var pendingNewEmail by remember { mutableStateOf("") }
    var pendingPassword by remember { mutableStateOf("") }
    var pendingCurrentPassword by remember { mutableStateOf("") }
    var pendingNewPassword by remember { mutableStateOf("") }
    
    // Manejar el botón "Atrás" del sistema (hardware o gestos)
    BackHandler {
        onNavigateBack()
    }
    
    // Colores adaptativos para modo oscuro
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val surfaceColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimaryColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val textSecondaryColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val dividerColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    
    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            profileViewModel.updateProfilePhoto(it)
        }
    }


    // Launcher para seleccionar foto de portada
    val  coverPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            profileViewModel.updateCoverPhoto(it)
        }
    }

    // Launcher para permisos de ubicación
    val context = androidx.compose.ui.platform.LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            profileViewModel.getCurrentLocation(context)
        }
    }
    
    // Snackbar para mensajes de éxito y error
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Observar mensajes de éxito y error
    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            profileViewModel.clearMessages()
        }
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            profileViewModel.clearMessages()
        }
    }
    
    // Cargar perfil al iniciar
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
        // Verificar si el email fue actualizado después de verificación
        profileViewModel.checkAndCompleteEmailChange()
    }
    
    // Detectar cuando la app vuelve al primer plano para recargar datos
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Cuando la app vuelve del navegador (después de verificar email)
                profileViewModel.checkAndCompleteEmailChange()
                profileViewModel.loadUserProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Calcular la opacidad y progreso del scroll
    val scrollOffset = scrollState.value.toFloat()
    val headerHeight = 180f // Altura de la foto de portada
    val scrollProgress = (scrollOffset / headerHeight).coerceIn(0f, 1f)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF3B82F6)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header con foto de perfil y portada (con parallax)
                ProfileHeader(
                    userName = uiState.displayName.ifEmpty { "Usuario" },
                    userEmail = uiState.email,
                    photoUrl = uiState.photoUrl,
                    coverPhotoUrl = uiState.coverPhotoUrl,
                    onBackClick = onNavigateBack,
                    onCameraClick = { imagePickerLauncher.launch("image/*") },
                    onDeletePhoto = { profileViewModel.deleteProfilePhoto() },
                    onCoverPhotoClick = { coverPhotoPickerLauncher.launch("image/*") },
                    onDeleteCoverPhoto = { profileViewModel.deleteCoverPhoto() },
                    surfaceColor = surfaceColor,
                    textPrimaryColor = textPrimaryColor,
                    textSecondaryColor = textSecondaryColor,
                    scrollOffset = scrollOffset // Pasar el offset para parallax
                )
                
                // Contenido principal con bordes redondeados superiores
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-30).dp), // Deslizar sobre la portada
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                    color = backgroundColor,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Información personal
                        ProfileSection(
                            title = "Información Personal",
                            surfaceColor = surfaceColor,
                            textPrimaryColor = textPrimaryColor,
                    content = {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.Person,
                                title = "Nombre completo",
                                subtitle = uiState.displayName,
                                onClick = { showEditDialog = true },
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor
                            )
                            Divider(color = dividerColor)
                            ProfileMenuItem(
                                icon = Icons.Default.Email,
                                title = "Correo electrónico",
                                subtitle = uiState.email,
                                onClick = { showEditEmailDialog = true },
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor
                            )
                            Divider(color = dividerColor)

                            ProfileMenuItem(
                                icon = Icons.Default.Lock,
                                title = "Cambiar contraseña",
                                subtitle = "Actualizar contraseña",
                                iconColor = Color(0xFF8B5CF6),
                                onClick = { showChangePasswordDialog = true},
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor
                            )
                            Divider(color = dividerColor)


                            ProfileMenuItem(
                                icon = Icons.Default.Phone,
                                title = "Teléfono",
                                subtitle = uiState.phoneNumber.ifEmpty { "Agregar teléfono" },
                                onClick = { showEditDialog = true },
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor
                            )
                            Divider(color = dividerColor)
                            ProfileMenuItem(
                                icon = Icons.Default.LocationOn,
                                title = "Dirección",
                                subtitle = uiState.address.ifEmpty { "Agregar dirección" },
                                onClick = { showAddressManagerDialog = true },
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Configuraciones
                ProfileSection(
                    title = "Configuración",
                    surfaceColor = surfaceColor,
                    textPrimaryColor = textPrimaryColor,
                    content = {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.Notifications,
                                title = "Notificaciones",
                                subtitle = "Configurar preferencias",
                                iconColor = Color(0xFFF59E0B),
                                onClick = { /* TODO */ },
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor
                            )
                            Divider(color = dividerColor)
                            ProfileMenuItem(
                                icon = Icons.Default.Lock,
                                title = "Privacidad y seguridad",
                                subtitle = "Gestionar permisos",
                                iconColor = Color(0xFF8B5CF6),
                                onClick = { /* TODO */ },
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor
                            )
                            Divider(color = dividerColor)
                            ProfileMenuItem(
                                icon = Icons.Default.Info,
                                title = "Ayuda y soporte",
                                subtitle = "Centro de ayuda",
                                iconColor = Color(0xFF10B981),
                                onClick = { /* TODO */ },
                                textPrimaryColor = textPrimaryColor,
                                textSecondaryColor = textSecondaryColor
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón de cerrar sesión
                LogoutButton(onClick = onLogout)
                
                Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            
            // Header dinámico que aparece al hacer scroll
            AnimatedTopBar(
                userName = uiState.displayName.ifEmpty { "Usuario" },
                photoUrl = uiState.photoUrl,
                scrollProgress = scrollProgress,
                onBackClick = onNavigateBack,
                surfaceColor = surfaceColor,
                textPrimaryColor = textPrimaryColor
            )
            
            // Snackbar Host
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (uiState.error != null) Color(0xFFEF4444) else Color(0xFF10B981),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
    
    // Diálogo de edición
    if (showEditDialog) {
        EditProfileDialog(
            currentName = uiState.displayName,
            currentPhone = uiState.phoneNumber,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone ->
                profileViewModel.updateProfile(
                    displayName = name,
                    phoneNumber = phone,
                    address = uiState.address
                )
                showEditDialog = false
            }
        )
    }
    
    // Diálogo para editar email
    if (showEditEmailDialog) {
        EditEmailDialog(
            currentEmail = uiState.email,
            onDismiss = { showEditEmailDialog = false },
            onConfirm = { newEmail, password ->
                // Mostrar confirmación antes de cambiar
                showEditEmailDialog = false
                // Aquí mostramos el diálogo de confirmación
                showConfirmEmailChangeDialog = true
                pendingNewEmail = newEmail
                pendingPassword = password
            }
        )
    }
    
    // Diálogo para cambiar contraseña
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { currentPassword, newPassword ->
                // Mostrar confirmación antes de cambiar
                showChangePasswordDialog = false
                showConfirmPasswordChangeDialog = true
                pendingCurrentPassword = currentPassword
                pendingNewPassword = newPassword
            }
        )
    }
    
    // Diálogo para gestionar direcciones
    if (showAddressManagerDialog) {
        AddressManagerDialog(
            currentAddress = uiState.address,
            homeAddress = uiState.addressHome,
            workAddress = uiState.addressWork,
            onDismiss = { showAddressManagerDialog = false },
            onGetLocation = {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            },
            onSaveCurrentAddress = { address ->
                profileViewModel.onAddressChange(address)
                profileViewModel.updateAddresses()
            },
            onSaveHomeAddress = { address ->
                profileViewModel.onAddressHomeChange(address)
                profileViewModel.updateAddresses()
            },
            onSaveWorkAddress = { address ->
                profileViewModel.onAddressWorkChange(address)
                profileViewModel.updateAddresses()
            },
            surfaceColor = surfaceColor,
            textPrimaryColor = textPrimaryColor,
            textSecondaryColor = textSecondaryColor
        )
    }
    
    // Diálogo de confirmación para cambio de email
    if (showConfirmEmailChangeDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmEmailChangeDialog = false },
            containerColor = surfaceColor,
            title = {
                Text(
                    text = "⚠️ Confirmar cambio de email",
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Estás a punto de cambiar tu email a:",
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                    Text(
                        text = pendingNewEmail,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )
                    Text(
                        text = "Se enviará un correo de verificación al nuevo email. Tu email actual seguirá activo hasta que confirmes el cambio.",
                        fontSize = 13.sp,
                        color = textSecondaryColor
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        profileViewModel.updateEmail(pendingNewEmail, pendingPassword)
                        showConfirmEmailChangeDialog = false
                    }
                ) {
                    Text(
                        "Confirmar",
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmEmailChangeDialog = false }
                ) {
                    Text(
                        "Cancelar",
                        color = textSecondaryColor
                    )
                }
            }
        )
    }
    
    // Diálogo de confirmación para cambio de contraseña
    if (showConfirmPasswordChangeDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmPasswordChangeDialog = false },
            containerColor = surfaceColor,
            title = {
                Text(
                    text = "⚠️ Confirmar cambio de contraseña",
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "¿Estás seguro de que deseas cambiar tu contraseña?",
                        fontSize = 14.sp,
                        color = textSecondaryColor
                    )
                    Text(
                        text = "Tu sesión actual seguirá activa, pero necesitarás la nueva contraseña en futuros inicios de sesión.",
                        fontSize = 13.sp,
                        color = textSecondaryColor
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        profileViewModel.updatePassword(pendingCurrentPassword, pendingNewPassword)
                        showConfirmPasswordChangeDialog = false
                    }
                ) {
                    Text(
                        "Confirmar",
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmPasswordChangeDialog = false }
                ) {
                    Text(
                        "Cancelar",
                        color = textSecondaryColor
                    )
                }
            }
        )
    }
}

// ===== COMPONENTE: Barra superior animada que aparece al hacer scroll =====
@Composable
fun AnimatedTopBar(
    userName: String,
    photoUrl: String,
    scrollProgress: Float,
    onBackClick: () -> Unit,
    surfaceColor: Color,
    textPrimaryColor: Color
) {
    // Animar la opacidad del fondo
    val backgroundAlpha by animateFloatAsState(
        targetValue = scrollProgress,
        label = "backgroundAlpha"
    )
    
    // Animar la opacidad del texto y avatar
    val contentAlpha by animateFloatAsState(
        targetValue = if (scrollProgress > 0.7f) 1f else 0f,
        label = "contentAlpha"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = surfaceColor.copy(alpha = backgroundAlpha),
        shadowElevation = if (backgroundAlpha > 0.5f) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón de volver
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = if (backgroundAlpha > 0.5f) textPrimaryColor else Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Avatar pequeño y nombre (aparecen gradualmente)
            if (contentAlpha > 0f) {
                Row(
                    modifier = Modifier
                        .graphicsLayer { alpha = contentAlpha },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mini avatar
                    if (photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color(0xFF3B82F6)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userName.firstOrNull()?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Nombre
                    Text(
                        text = userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimaryColor
                    )
                }
            } else {
                // Mostrar "Mi Perfil" cuando está arriba
                Text(
                    text = "Mi Perfil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (backgroundAlpha > 0.5f) textPrimaryColor else Color.White,
                    modifier = Modifier.graphicsLayer { 
                        alpha = 1f - contentAlpha 
                    }
                )
            }
        }
    }
}

// ===== COMPONENTE: Header con foto de perfil y portada =====
@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    photoUrl: String = "",
    coverPhotoUrl: String = "",
    onBackClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDeletePhoto: () -> Unit = {},
    onCoverPhotoClick: () -> Unit = {},
    onDeleteCoverPhoto: () -> Unit = {},
    surfaceColor: Color = Color.White,
    textPrimaryColor: Color = Color(0xFF1E293B),
    textSecondaryColor: Color = Color(0xFF64748B),
    scrollOffset: Float = 0f
) {
    val parallaxEffect = scrollOffset * 0.5f // Efecto parallax (se mueve más lento)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Foto de portada con efecto parallax
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .graphicsLayer {
                    translationY = -parallaxEffect // Se mueve más lento que el scroll
                }
        ) {
            if (coverPhotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = coverPhotoUrl,
                    contentDescription = "Foto de portada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6),
                                    Color(0xFF2563EB)
                                )
                            )
                        )
                )
            }
            
            IconButton(
                onClick = onCoverPhotoClick,
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp)
                    .align(Alignment.TopEnd)
                    .offset(y = 150.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar portada",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            if (coverPhotoUrl.isNotEmpty()) {
                IconButton(
                    onClick = onDeleteCoverPhoto,
                    modifier = Modifier
                        .padding(top = 8.dp, end = 56.dp)
                        .size(40.dp)
                        .align(Alignment.TopEnd)
                        .offset(y = 150.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEF4444).copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar portada",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .offset(y = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.offset(y = (-50).dp)
                ) {
                    val displayName = userName.ifEmpty { "Usuario" }
                    
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(surfaceColor)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.firstOrNull()?.uppercase()?.toString() ?: "U",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            if (photoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .clickable { onCameraClick() },
                        shape = CircleShape,
                        color = Color(0xFF3B82F6),
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    if (photoUrl.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomStart)
                                .offset(x = 4.dp, y = (-4).dp)
                                .clickable { onDeletePhoto() },
                            shape = CircleShape,
                            color = Color(0xFFEF4444),
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = textSecondaryColor,
                    modifier = Modifier.offset(y = (-40).dp)
                )
                
                Text(
                    text = userName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor,
                    modifier = Modifier.offset(y = (-36).dp)
                )
                
                Spacer(modifier = Modifier.height((-36).dp))
            }
        }
    }
}

// ===== COMPONENTE: Sección del perfil =====
@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit,
    surfaceColor: Color = Color.White,
    textPrimaryColor: Color = Color(0xFF1E293B)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimaryColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = surfaceColor,
            shadowElevation = 1.dp
        ) {
            content()
        }
    }
}

// ===== COMPONENTE: Item de menú =====
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    iconColor: Color = Color(0xFF3B82F6),
    onClick: () -> Unit = {},
    textPrimaryColor: Color = Color(0xFF1E293B),
    textSecondaryColor: Color = Color(0xFF94A3B8)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono con fondo
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Textos
        Column(modifier = Modifier.weight(1f)) {
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor
                )
            }
            Text(
                text = title,
                fontSize = 12.sp,
                color = textSecondaryColor
            )
        }
        
        // Flecha
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = textSecondaryColor
        )
    }
}

// ===== COMPONENTE: Botón de cerrar sesión =====
@Composable
fun LogoutButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFEE2E2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Cerrar sesión",
                tint = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cerrar sesión",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
        }
    }
}

// ===== COMPONENTE: Diálogo de edición =====
@Composable
fun EditProfileDialog(
    currentName: String,
    currentPhone: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }
    
    // Colores adaptativos
    val appColors = com.example.myapplication.ui.theme.getAppColors()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.surfaceColor,
        title = {
            Text(
                text = "Editar Perfil",
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimaryColor
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo", color = appColors.textSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimaryColor,
                        unfocusedTextColor = appColors.textPrimaryColor,
                        focusedBorderColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor,
                        cursorColor = appColors.accentBlue
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono", color = appColors.textSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimaryColor,
                        unfocusedTextColor = appColors.textPrimaryColor,
                        focusedBorderColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor,
                        cursorColor = appColors.accentBlue
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, phone) }
            ) {
                Text("Guardar", color = appColors.accentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = appColors.textSecondaryColor)
            }
        }
    )
}

// ===== DIÁLOGOS =====

@Composable
fun EditEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var newEmail by remember { mutableStateOf(currentEmail) }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    // Colores adaptativos
    val appColors = com.example.myapplication.ui.theme.getAppColors()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.surfaceColor,
        title = {
            Text(
                text = "Cambiar correo electrónico",
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimaryColor
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Por seguridad, ingresa tu contraseña actual para cambiar tu email.",
                    fontSize = 14.sp,
                    color = appColors.textSecondaryColor
                )
                
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { 
                        newEmail = it
                        showError = false
                    },
                    label = { Text("Nuevo correo electrónico", color = appColors.textSecondaryColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = appColors.textSecondaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimaryColor,
                        unfocusedTextColor = appColors.textPrimaryColor,
                        focusedBorderColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor,
                        focusedLabelColor = appColors.accentBlue,
                        cursorColor = appColors.accentBlue
                    )
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        showError = false
                    },
                    label = { Text("Contraseña actual", color = appColors.textSecondaryColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = appColors.textSecondaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimaryColor,
                        unfocusedTextColor = appColors.textPrimaryColor,
                        focusedBorderColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor,
                        focusedLabelColor = appColors.accentBlue,
                        cursorColor = appColors.accentBlue
                    )
                )
                
                if (showError) {
                    Text(
                        text = "El email debe ser válido y la contraseña correcta",
                        color = appColors.accentRed,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newEmail.contains("@") && password.isNotEmpty()) {
                        onConfirm(newEmail, password)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text(
                    "Cambiar",
                    color = appColors.accentBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = appColors.textSecondaryColor)
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Colores adaptativos
    val appColors = com.example.myapplication.ui.theme.getAppColors()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.surfaceColor,
        title = {
            Text(
                text = "Cambiar contraseña",
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimaryColor
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { 
                        currentPassword = it
                        showError = false
                    },
                    label = { Text("Contraseña actual", color = appColors.textSecondaryColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = appColors.textSecondaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimaryColor,
                        unfocusedTextColor = appColors.textPrimaryColor,
                        focusedBorderColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor,
                        focusedLabelColor = appColors.accentBlue,
                        cursorColor = appColors.accentBlue
                    )
                )
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        showError = false
                    },
                    label = { Text("Nueva contraseña", color = appColors.textSecondaryColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = appColors.textSecondaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    isError = showError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimaryColor,
                        unfocusedTextColor = appColors.textPrimaryColor,
                        focusedBorderColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor,
                        focusedLabelColor = appColors.accentBlue,
                        cursorColor = appColors.accentBlue
                    )
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        showError = false
                    },
                    label = { Text("Confirmar nueva contraseña", color = appColors.textSecondaryColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = appColors.textSecondaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    isError = showError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimaryColor,
                        unfocusedTextColor = appColors.textPrimaryColor,
                        focusedBorderColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor,
                        focusedLabelColor = appColors.accentBlue,
                        cursorColor = appColors.accentBlue
                    )
                )
                
                if (showError) {
                    Text(
                        text = errorMessage,
                        color = appColors.accentRed,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        currentPassword.isEmpty() -> {
                            showError = true
                            errorMessage = "Ingresa tu contraseña actual"
                        }
                        newPassword.length < 6 -> {
                            showError = true
                            errorMessage = "La nueva contraseña debe tener al menos 6 caracteres"
                        }
                        newPassword != confirmPassword -> {
                            showError = true
                            errorMessage = "Las contraseñas no coinciden"
                        }
                        else -> {
                            onConfirm(currentPassword, newPassword)
                        }
                    }
                }
            ) {
                Text(
                    "Cambiar",
                    color = appColors.accentBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = appColors.textSecondaryColor)
            }
        }
    )
}

// ===== DIÁLOGO: Gestionar Direcciones con botón + =====
@Composable
fun AddressManagerDialog(
    currentAddress: String,
    homeAddress: String,
    workAddress: String,
    onDismiss: () -> Unit,
    onGetLocation: () -> Unit,
    onSaveCurrentAddress: (String) -> Unit,
    onSaveHomeAddress: (String) -> Unit,
    onSaveWorkAddress: (String) -> Unit,
    surfaceColor: Color = Color.White,
    textPrimaryColor: Color = Color(0xFF1E293B),
    textSecondaryColor: Color = Color(0xFF64748B)
) {
    var currentAddressText by remember { mutableStateOf(currentAddress) }
    var homeAddressText by remember { mutableStateOf(homeAddress) }
    var homeCityText by remember { mutableStateOf("") }
    var homeStateText by remember { mutableStateOf("") }
    var homeZipCodeText by remember { mutableStateOf("") }
    var workAddressText by remember { mutableStateOf(workAddress) }
    var workCityText by remember { mutableStateOf("") }
    var workStateText by remember { mutableStateOf("") }
    var workZipCodeText by remember { mutableStateOf("") }
    var showHomeField by remember { mutableStateOf(homeAddress.isNotEmpty()) }
    var showWorkField by remember { mutableStateOf(workAddress.isNotEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor,
        title = {
            Text(
                text = "Gestionar Direcciones",
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Dirección Actual
                Text(
                    text = "Dirección Actual",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textSecondaryColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = currentAddressText,
                    onValueChange = { currentAddressText = it },
                    placeholder = { Text("Ingresa tu dirección", color = textSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onGetLocation) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Obtener ubicación GPS",
                                tint = Color(0xFF10B981)
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Dirección de Casa
                if (showHomeField) {
                    Text(
                        text = "Dirección de Casa",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Dirección completa
                    OutlinedTextField(
                        value = homeAddressText,
                        onValueChange = { homeAddressText = it },
                        placeholder = { Text("Calle y número", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = Color(0xFF10B981)
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Ciudad
                    OutlinedTextField(
                        value = homeCityText,
                        onValueChange = { homeCityText = it },
                        placeholder = { Text("Ciudad", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6)
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Provincia/Estado
                    OutlinedTextField(
                        value = homeStateText,
                        onValueChange = { homeStateText = it },
                        placeholder = { Text("Provincia/Estado", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6)
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Código Postal
                    OutlinedTextField(
                        value = homeZipCodeText,
                        onValueChange = { homeZipCodeText = it },
                        placeholder = { Text("Código Postal", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6)
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Dirección de Trabajo
                if (showWorkField) {
                    Text(
                        text = "Dirección de Trabajo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Dirección completa
                    OutlinedTextField(
                        value = workAddressText,
                        onValueChange = { workAddressText = it },
                        placeholder = { Text("Calle y número", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B)
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Ciudad
                    OutlinedTextField(
                        value = workCityText,
                        onValueChange = { workCityText = it },
                        placeholder = { Text("Ciudad", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6)
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Provincia/Estado
                    OutlinedTextField(
                        value = workStateText,
                        onValueChange = { workStateText = it },
                        placeholder = { Text("Provincia/Estado", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6)
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Código Postal
                    OutlinedTextField(
                        value = workZipCodeText,
                        onValueChange = { workZipCodeText = it },
                        placeholder = { Text("Código Postal", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6)
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Botones para agregar direcciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!showHomeField) {
                        Button(
                            onClick = { showHomeField = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDCFCE7)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Casa", color = Color(0xFF10B981), fontSize = 12.sp)
                        }
                    }
                    
                    if (!showWorkField) {
                        Button(
                            onClick = { showWorkField = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFEF3C7)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trabajo", color = Color(0xFFF59E0B), fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveCurrentAddress(currentAddressText)
                    if (showHomeField) onSaveHomeAddress(homeAddressText)
                    if (showWorkField) onSaveWorkAddress(workAddressText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = textSecondaryColor)
            }
        }
    )
}