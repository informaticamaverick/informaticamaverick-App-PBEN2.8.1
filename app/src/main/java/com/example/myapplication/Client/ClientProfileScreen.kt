package com.example.myapplication.Client

import android.R
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.Profile.ProfileViewModel
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

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
    
    // Cargar perfil al iniciar
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }
    
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
                // Header con foto de perfil
                ProfileHeader(
                    userName = uiState.displayName.ifEmpty { "Usuario" },
                    userEmail = uiState.email,
                    photoUrl = uiState.photoUrl,
                    onBackClick = onNavigateBack,
                    onCameraClick = { imagePickerLauncher.launch("image/*") },
                    onDeletePhoto = { profileViewModel.deleteProfilePhoto()},
                    surfaceColor = surfaceColor,
                    textPrimaryColor = textPrimaryColor,
                    textSecondaryColor = textSecondaryColor
                )
                
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
    
    // Diálogo de edición
    if (showEditDialog) {
        EditProfileDialog(
            currentName = uiState.displayName,
            currentPhone = uiState.phoneNumber,
            currentAddress = uiState.address,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone, address ->
                profileViewModel.updateProfile(
                    displayName = name,
                    phoneNumber = phone,
                    address = address
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
                profileViewModel.updateEmail(newEmail, password)
                showEditEmailDialog = false
            }
        )
    }
    
    // Diálogo para cambiar contraseña
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { currentPassword, newPassword ->
                profileViewModel.updatePassword(currentPassword, newPassword)
                showChangePasswordDialog = false
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
            onSaveCurrentAddress = { address ->
                profileViewModel.onAddressChange(address)
                profileViewModel.saveProfile()
            },
            onSaveHomeAddress = { address ->
                profileViewModel.onAddressHomeChange(address)
                profileViewModel.saveProfile()
            },
            onSaveWorkAddress = { address ->
                profileViewModel.onAddressWorkChange(address)
                profileViewModel.saveProfile()
            },
            surfaceColor = surfaceColor,
            textPrimaryColor = textPrimaryColor,
            textSecondaryColor = textSecondaryColor
        )
    }
}

// ===== COMPONENTE: Header con foto de perfil =====
@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    photoUrl: String = "",
    onBackClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDeletePhoto: () -> Unit = {},
    surfaceColor: Color = Color.White,
    textPrimaryColor: Color = Color(0xFF1E293B),
    textSecondaryColor: Color = Color(0xFF64748B)
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Botón de regresar
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = textPrimaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Avatar y nombre centrados
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Obtener el nombre para mostrar
                val displayName = userName.ifEmpty { "Usuario" }
                
                // Avatar circular con botón de cámara
                Box {
                    // Avatar principal
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6)), // Azul
                        contentAlignment = Alignment.Center
                    ) {
                        // Texto siempre presente (inicial)
                        Text(
                            text = displayName.firstOrNull()?.uppercase()?.toString() ?: "U",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        // Foto encima si existe
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
                    
                    // Botón de cámara flotante (derecha inferior)
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-8).dp, y = (-8).dp)
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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Botón de eliminar foto (izquierda inferior - solo si hay foto)
                    if (photoUrl.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.BottomStart)
                                .offset(x = 8.dp, y = (-8).dp)
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
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nombre
                Text(
                    text = userName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Email
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = textSecondaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
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
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimaryColor
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = textSecondaryColor
                )
            }
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
    currentAddress: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }
    var address by remember { mutableStateOf(currentAddress) }
    
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
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección", color = appColors.textSecondaryColor) },
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
                onClick = { onSave(name, phone, address) }
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
    onSaveCurrentAddress: (String) -> Unit,
    onSaveHomeAddress: (String) -> Unit,
    onSaveWorkAddress: (String) -> Unit,
    surfaceColor: Color = Color.White,
    textPrimaryColor: Color = Color(0xFF1E293B),
    textSecondaryColor: Color = Color(0xFF64748B)
) {
    var currentAddressText by remember { mutableStateOf(currentAddress) }
    var homeAddressText by remember { mutableStateOf(homeAddress) }
    var workAddressText by remember { mutableStateOf(workAddress) }
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
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Dirección de Casa
                if (showHomeField) {
                    Text(
                        text = "Dirección de Casa",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = homeAddressText,
                        onValueChange = { homeAddressText = it },
                        placeholder = { Text("Ingresa dirección de casa", color = textSecondaryColor) },
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
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Dirección de Trabajo
                if (showWorkField) {
                    Text(
                        text = "Dirección de Trabajo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = workAddressText,
                        onValueChange = { workAddressText = it },
                        placeholder = { Text("Ingresa dirección de trabajo", color = textSecondaryColor) },
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