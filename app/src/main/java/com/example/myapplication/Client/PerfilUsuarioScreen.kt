package com.example.myapplication.Client

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.Profile.ProfileViewModel
import com.example.myapplication.Profile.ProfileUiState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- ESTADOS PARA EDICIÓN ---
    var showEditSheet by remember { mutableStateOf(false) }
    var editType by remember { mutableStateOf("personal") }

    // --- Estados para diálogos ---
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showPasswordManagerSheet by remember { mutableStateOf(false) }
    var pendingNewEmail by remember { mutableStateOf("") }
    var pendingPassword by remember { mutableStateOf("") }
    var pendingCurrentPassword by remember { mutableStateOf("") }
    var pendingNewPassword by remember { mutableStateOf("") }
    var showConfirmEmailChangeDialog by remember { mutableStateOf(false) }
    var showConfirmPasswordChangeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            profileViewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            profileViewModel.clearMessages()
        }
    }

    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val headerSizePx = with(density) { 250.dp.toPx() }

    val scrollProgress by remember {
        derivedStateOf {
            if (lazyListState.layoutInfo.visibleItemsInfo.isEmpty() || lazyListState.firstVisibleItemIndex > 0) {
                1f
            } else {
                val firstItem = lazyListState.layoutInfo.visibleItemsInfo.first()
                (firstItem.offset.toFloat() / -headerSizePx).coerceIn(0f, 1f)
            }
        }
    }

    // --- VARIABLES DE ESTILO ---
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF9F9F9)
    val surfaceColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textPrimaryColor = if (isDarkTheme) Color.White.copy(alpha = 0.87f) else Color.Black.copy(alpha = 0.87f)
    val textSecondaryColor = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    val pagerState = rememberPagerState(pageCount = { 2 })

    // Valores dinámicos según el carrusel
    val currentTopName = if (pagerState.currentPage == 0) {
        uiState.displayName.ifEmpty { "Usuario" }
    } else {
        uiState.nameComercialEmpresa.ifEmpty { "Empresa" }
    }
    val currentTopPhoto = if (pagerState.currentPage == 0) uiState.photoUrl else null
    val currentTopIcon = if (pagerState.currentPage == 1) Icons.Default.Business else null

    // --- ESTRUCTURA PRINCIPAL DE LA PANTALLA ---
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            AnimatedTopBar(
                userName = currentTopName,
                photoUrl = currentTopPhoto,
                icon = currentTopIcon,
                scrollProgress = scrollProgress,
                onBackClick = onNavigateBack,
                surfaceColor = surfaceColor,
                textPrimaryColor = textPrimaryColor
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    // --- CAMBIO: Banner del TopBar con fondo MD3 ---
                    UserInfoHeader(
                        userName = currentTopName,
                        userEmail = if (pagerState.currentPage == 0) uiState.email else uiState.emailEmpresa,
                        photoUrl = currentTopPhoto,
                        icon = currentTopIcon,
                        surfaceColor = surfaceColor,
                        textPrimaryColor = textPrimaryColor,
                        textSecondaryColor = textSecondaryColor,
                        isDark = isDarkTheme
                    )
                }

                item { // -- CUERPO CON CARRUSEL Y ETIQUETAS --
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // --- CAMBIO: Carrusel de tarjetas con HorizontalPager ---
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth().height(260.dp),
                                contentPadding = PaddingValues(horizontal = 32.dp),
                                pageSpacing = 16.dp
                            ) { page ->
                                if (page == 0) {
                                    InfoCard(
                                        title = "Detalles Personales",
                                        surfaceColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE3F2FD),
                                        textPrimaryColor = textPrimaryColor
                                    ) {
                                        Column {
                                            // --- CAMBIO: Se esconden nombre y correo (ya en cabecera) ---
                                            DisplayItem(Icons.Default.Phone, "Teléfono", uiState.phoneNumber, textPrimaryColor, textSecondaryColor)
                                            DisplayItem(Icons.Default.LocationOn, "Dirección", uiState.address, textPrimaryColor, textSecondaryColor)
                                        }
                                    }
                                } else {
                                    InfoCard(
                                        title = "Detalles de Empresa",
                                        surfaceColor = if (isDarkTheme) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF1F8E9),
                                        textPrimaryColor = textPrimaryColor
                                    ) {
                                        Column {
                                            // --- CAMBIO: Se esconden nombre comercial y email empresa (ya en cabecera) ---
                                            DisplayItem(Icons.Default.Domain, "Razón Social", uiState.nameRazonSocialEmpresa, textPrimaryColor, textSecondaryColor)
                                            DisplayItem(Icons.Default.Badge, "CUIT", uiState.numberCuitEmpresa, textPrimaryColor, textSecondaryColor)
                                        }
                                    }
                                }
                            }

                            // --- CAMBIO: FAB movido al extremo derecho de las tarjetas (solo icono) ---
                            FloatingActionButton(
                                onClick = {
                                    editType = if (pagerState.currentPage == 0) "personal" else "empresa"
                                    showEditSheet = true
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                                    .size(48.dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                            }
                        }

                        // Indicador de páginas
                        Row(
                            Modifier.height(32.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(pagerState.pageCount) { iteration ->
                                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                Box(modifier = Modifier.padding(3.dp).clip(CircleShape).background(color).size(6.dp))
                            }
                        }

                        // --- CAMBIO: Divisor "Datos de Cuenta" y Etiquetas Booleanas ---
                        SectionDivider("Datos de Cuenta")

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (pagerState.currentPage == 0) {
                                BooleanTag(label = "Notificaciones", isActive = uiState.notificationsEnabled)
                                BooleanTag(label = "Público", isActive = uiState.isPublicProfile)
                            } else {
                                BooleanTag(label = "Verificada", isActive = uiState.isEmpresa)
                                BooleanTag(label = "Activa", isActive = uiState.isComplete)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ProfileSection(
                                title = "Configuración",
                                surfaceColor = surfaceColor,
                                textPrimaryColor = textPrimaryColor
                            ) {
                                Column {
                                    ProfileMenuItem(
                                        icon = Icons.Default.Password,
                                        title = "Seguridad",
                                        subtitle = "Cambiar contraseñas",
                                        iconColor = Color(0xFFF59E0B),
                                        onClick = { showPasswordManagerSheet = true },
                                        textPrimaryColor = textPrimaryColor,
                                        textSecondaryColor = textSecondaryColor
                                    )
                                    HorizontalDivider(color = dividerColor)
                                    ProfileMenuItem(
                                        icon = Icons.Default.Notifications,
                                        title = "Notificaciones",
                                        subtitle = "Preferencias",
                                        iconColor = Color(0xFFF59E0B),
                                        onClick = { /* TODO */ },
                                        textPrimaryColor = textPrimaryColor,
                                        textSecondaryColor = textSecondaryColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            LogoutButton(onClick = onLogout)
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            )
        }
    }

    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            if (editType == "personal") {
                EditPersonalContent(
                    uiState = uiState,
                    onSave = { name, phone, address ->
                        profileViewModel.updateProfile(name, phone, address)
                        showEditSheet = false
                    }
                )
            } else {
                EditBusinessContent(
                    uiState = uiState,
                    onSave = { nameCom, razon ->
                        profileViewModel.onNameComercialEmpresaChange(nameCom)
                        profileViewModel.onNameRazonSocialEmpresaChange(razon)
                        profileViewModel.updateProfile(uiState.displayName, uiState.phoneNumber, uiState.address)
                        showEditSheet = false
                    }
                )
            }
        }
    }

    if (showPasswordManagerSheet) {
        ModalBottomSheet(onDismissRequest = { showPasswordManagerSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Seguridad", style = MaterialTheme.typography.titleLarge)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Cambiar contraseña",
                    subtitle = "Actualiza tu seguridad",
                    iconColor = Color(0xFF8B5CF6),
                    onClick = {
                        showPasswordManagerSheet = false
                        showChangePasswordDialog = true
                    },
                    textPrimaryColor = textPrimaryColor,
                    textSecondaryColor = textSecondaryColor
                )
            }
        }
    }

    if (showEditEmailDialog) {
        EditEmailDialog(
            currentEmail = uiState.email,
            onDismiss = { showEditEmailDialog = false },
            onConfirm = { newEmail, password ->
                pendingNewEmail = newEmail
                pendingPassword = password
                showEditEmailDialog = false
                showConfirmEmailChangeDialog = true
            }
        )
    }

    if (showConfirmEmailChangeDialog) {
        ConfirmEmailChangeDialog(
            newEmail = pendingNewEmail,
            onDismiss = { showConfirmEmailChangeDialog = false },
            onConfirm = {
                profileViewModel.updateEmail(pendingNewEmail, pendingPassword)
                showConfirmEmailChangeDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { current, new ->
                pendingCurrentPassword = current
                pendingNewPassword = new
                showChangePasswordDialog = false
                showConfirmPasswordChangeDialog = true
            }
        )
    }

    if (showConfirmPasswordChangeDialog) {
        ConfirmPasswordChangeDialog(
            onDismiss = { showConfirmPasswordChangeDialog = false },
            onConfirm = {
                profileViewModel.updatePassword(pendingCurrentPassword, pendingNewPassword)
                showConfirmPasswordChangeDialog = false
            }
        )
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun SectionDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    }
}

@Composable
fun BooleanTag(label: String, isActive: Boolean) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    surfaceColor: Color,
    textPrimaryColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = textPrimaryColor)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DisplayItem(icon: ImageVector, label: String, value: String, textPrimary: Color, textSecondary: Color) {
    Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = textSecondary)
            Text(value.ifEmpty { "No especificado" }, style = MaterialTheme.typography.bodyLarge, color = textPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EditPersonalContent(uiState: ProfileUiState, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf(uiState.displayName) }
    var phone by remember { mutableStateOf(uiState.phoneNumber) }
    var address by remember { mutableStateOf(uiState.address) }

    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text("Editar Información Personal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Phone, null) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.LocationOn, null) }
        )

        Button(
            onClick = { onSave(name, phone, address) },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Guardar Cambios")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EditBusinessContent(uiState: ProfileUiState, onSave: (String, String) -> Unit) {
    var nameCom by remember { mutableStateOf(uiState.nameComercialEmpresa) }
    var razonSocial by remember { mutableStateOf(uiState.nameRazonSocialEmpresa) }

    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text("Editar Información de Empresa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nameCom,
            onValueChange = { nameCom = it },
            label = { Text("Nombre Comercial") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Business, null) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = razonSocial,
            onValueChange = { razonSocial = it },
            label = { Text("Razón Social") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Domain, null) }
        )

        Button(
            onClick = { onSave(nameCom, razonSocial) },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Actualizar Empresa")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun UserInfoHeader(
    userName: String,
    userEmail: String,
    photoUrl: String?,
    icon: ImageVector?,
    surfaceColor: Color,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    isDark: Boolean
) {
    // --- CAMBIO: Header con gradiente MD3 ---
    val gradient = if (isDark) {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant))
    } else {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, Color.White))
    }

    Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(gradient), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (photoUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = photoUrl, placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)),
                    contentDescription = "Foto",
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else if (icon != null) {
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(userName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = textPrimaryColor)
            Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = textSecondaryColor)
        }
    }
}

@Composable
fun ProfileSection(title: String, surfaceColor: Color, textPrimaryColor: Color, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textPrimaryColor, modifier = Modifier.padding(bottom = 8.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = surfaceColor, tonalElevation = 1.dp, shadowElevation = 1.dp) { content() }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, iconColor: Color = LocalContentColor.current, onClick: () -> Unit, textPrimaryColor: Color, textSecondaryColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = textPrimaryColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = textSecondaryColor)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = textSecondaryColor)
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Cerrar Sesión")
    }
}

// --- CAMBIO: AnimatedTopBar dinámico ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedTopBar(
    userName: String,
    photoUrl: String?,
    icon: ImageVector?,
    scrollProgress: Float,
    onBackClick: () -> Unit,
    surfaceColor: Color,
    textPrimaryColor: Color
) {
    val alpha = animateFloatAsState(targetValue = scrollProgress, label = "").value
    if (alpha > 0.01f) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.graphicsLayer { this.alpha = alpha }) {
                    if (photoUrl != null) {
                        Image(painter = rememberAsyncImagePainter(model = photoUrl), contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    } else if (icon != null) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(userName, color = textPrimaryColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            },
            navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = textPrimaryColor) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor.copy(alpha = alpha), scrolledContainerColor = surfaceColor),
            modifier = Modifier.graphicsLayer { this.alpha = alpha }
        )
    }
}

// Otros diálogos se mantienen iguales...
@Composable
fun EditEmailDialog(currentEmail: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var email by remember { mutableStateOf(currentEmail) }
    var password by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Cambiar Email") }, text = { Column { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Nuevo Email") }, modifier = Modifier.fillMaxWidth()); Spacer(modifier = Modifier.height(8.dp)); OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña actual") }, modifier = Modifier.fillMaxWidth()) } }, confirmButton = { TextButton(onClick = { onConfirm(email, password) }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
fun ConfirmEmailChangeDialog(newEmail: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Confirmar Cambio") }, text = { Text("¿Deseas cambiar tu email a $newEmail?") }, confirmButton = { TextButton(onClick = onConfirm) { Text("Sí, cambiar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Cambiar Contraseña") }, text = { Column { OutlinedTextField(value = current, onValueChange = { current = it }, label = { Text("Contraseña Actual") }, modifier = Modifier.fillMaxWidth()); Spacer(modifier = Modifier.height(8.dp)); OutlinedTextField(value = new, onValueChange = { new = it }, label = { Text("Nueva Contraseña") }, modifier = Modifier.fillMaxWidth()) } }, confirmButton = { TextButton(onClick = { onConfirm(current, new) }) { Text("Cambiar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
fun ConfirmPasswordChangeDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Confirmar Cambio") }, text = { Text("¿Estás seguro de que deseas cambiar tu contraseña?") }, confirmButton = { TextButton(onClick = onConfirm) { Text("Sí, cambiar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}
