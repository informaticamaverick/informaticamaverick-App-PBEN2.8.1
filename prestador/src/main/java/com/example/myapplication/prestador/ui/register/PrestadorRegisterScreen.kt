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
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.data.model.ServiceType
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrestadorRegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    isGoogleUser: Boolean = false,
    viewModel: PrestadorRegisterViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    // Email de la cuenta de Google (solo presente cuando isGoogleUser = true)
    val googleEmail = remember {
        if (isGoogleUser) FirebaseAuth.getInstance().currentUser?.email ?: "" else ""
    }

    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf(ServiceType.TECHNICAL) }
    var expandedTipoServicio by remember { mutableStateOf(false) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    // Categorías organizadas por tipo de servicio
    val categoriasPorTipo = mapOf(
        ServiceType.TECHNICAL to listOf(
            "Aire Acondicionado", "Albañilería", "Antenas y Cableado",
            "Carpintería", "Cerrajería", "Computación / IT",
            "Electricidad", "Electrónica", "Fumigación",
            "Gasista", "Herrería", "Impermeabilización",
            "Instalaciones Sanitarias", "Jardinería", "Limpieza",
            "Mecánica Ligera", "Mudanzas / Fletes", "Pintura",
            "Plomería", "Refrigeración", "Soldadura",
            "Techos y Cubiertas", "Vidriería", "Zinguería"
        ),
        ServiceType.PROFESSIONAL to listOf(
            "Abogacía", "Arquitectura", "Asesoría Contable",
            "Asesoría Impositiva", "Coaching", "Diseño Gráfico",
            "Diseño Web", "Fonoaudiología", "Kinesiología",
            "Marketing Digital", "Medicina General", "Nutrición",
            "Odontología", "Psicología", "Psicopedagogía",
            "Recursos Humanos", "Terapia Ocupacional", "Veterinaria"
        ),
        ServiceType.RENTAL to listOf(
            "Cancha de Fútbol", "Cancha de Pádel", "Cancha de Tenis",
            "Cochera / Estacionamiento", "Estudio de Música",
            "Estudio Fotográfico", "Oficina Compartida",
            "Quincho / Salón de Eventos", "Sala de Reuniones",
            "Sala de Yoga / Pilates", "SUM / Salón Múltiple"
        ),
        ServiceType.OTHER to listOf(
            "Clases Particulares", "Cuidado de Mascotas", "Cuidado de Personas Mayores",
            "Delivery / Mensajería", "Estética y Belleza", "Fotografía / Video",
            "Gastronomía / Catering", "Hotelería / Alojamiento",
            "Música / Entretenimiento", "Organización de Eventos",
            "Peluquería", "Seguridad Privada", "Tatuajes y Piercings",
            "Turismo / Excursiones"
        )
    )

    // Categorías filtradas según el tipo de servicio seleccionado
    val categoriasDisponibles = categoriasPorTipo[serviceType] ?: emptyList()

    // Variables requeridas por componentes reusables (no usadas en registro inicial)
    var serviciosSeleccionados by remember { mutableStateOf(listOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showServiceModal by remember { mutableStateOf(false) }
    var tempSelectedServices by remember { mutableStateOf(setOf<String>()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var atencionUrgencias by remember { mutableStateOf(false) }
    var vaDomicilio by remember { mutableStateOf(false) }
    var turnosEnLocal by remember { mutableStateOf(false) }
    var tieneEmpresa by remember { mutableStateOf(false) }
    var nombreEmpresa by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var cuit by remember { mutableStateOf("") }
    var sucursales by remember { mutableStateOf(listOf(Sucursal("", ""))) }
    var showMatriculaTooltip by remember { mutableStateOf(false) }

    // Sección expandida (acordeón: solo una a la vez)
    // Google users: arranca en "personal" (no necesitan datos de acceso)
    var expandedSection by remember { mutableStateOf<String?>(if (isGoogleUser) "personal" else "acceso") }

    val registerState by viewModel.registerState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> profileImageUri = uri }

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> onRegisterSuccess()
            else -> {}
        }
    }

    Scaffold(
        containerColor = colors.backgroundColor,
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = colors.surfaceColor
            ) {
                Button(
                    onClick = {
                        viewModel.register(
                            email = email,
                            password = password,
                            nombre = nombre,
                            apellido = apellido,
                            categoria = categoriaSeleccionada,
                            mensaje = mensaje,
                            serviceType = serviceType.name,
                            isGoogleUser = isGoogleUser
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (registerState is RegisterState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Crear cuenta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── HERO HEADER ──────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    colors.primaryOrange.copy(alpha = 0.25f),
                                    colors.backgroundColor
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Barra superior con botón volver
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackToLogin) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = colors.primaryOrange
                                )
                            }
                            Text(
                                text = "Crear perfil",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Foto de perfil circular
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(3.dp, colors.primaryOrange, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri != null) {
                                AsyncImage(
                                    model = profileImageUri,
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
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = colors.primaryOrange.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            // Botón cámara
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp),
                                shape = CircleShape,
                                color = colors.primaryOrange,
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Cambiar foto",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "$nombre $apellido".trim().ifEmpty { "Nuevo prestador" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (categoriaSeleccionada.isNotEmpty()) categoriaSeleccionada else serviceType.displayName,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ── SECCIÓN: Datos de acceso (solo para registro manual, no Google) ──
            if (!isGoogleUser) {
                item {
                    RegisterSectionCard(
                        title = "Datos de acceso",
                        icon = Icons.Default.Lock,
                        color = colors.primaryOrange,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        expanded = expandedSection == "acceso",
                        onExpandChange = {
                            expandedSection = if (expandedSection == "acceso") null else "acceso"
                        }
                    ) {
                        FloatingLabelTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Correo electrónico",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FloatingLabelTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Contraseña",
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            onTrailingIconClick = { passwordVisible = !passwordVisible }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            // ── SECCIÓN: Información personal ─────────────────────────────
            item {
                RegisterSectionCard(
                    title = "Información personal",
                    icon = Icons.Default.Person,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    expanded = expandedSection == "personal",
                    onExpandChange = {
                        expandedSection = if (expandedSection == "personal") null else "personal"
                    }
                ) {
                    // Banner cuenta de Google (solo si vino de Google)
                    if (isGoogleUser && googleEmail.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1976D2).copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Cuenta de Google vinculada",
                                        color = Color(0xFF1976D2),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = googleEmail,
                                        color = colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    // Banner informativo
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = colors.primaryOrange.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Completá lo esencial ahora. Podés agregar más datos desde Editar perfil.",
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FloatingLabelTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = "Nombre",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FloatingLabelTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = "Apellido",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // ── SECCIÓN: Tu servicio ───────────────────────────────────────
            item {
                RegisterSectionCard(
                    title = "Tu servicio",
                    icon = Icons.Default.Build,
                    color = Color(0xFF00897B),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    expanded = expandedSection == "servicio",
                    onExpandChange = {
                        expandedSection = if (expandedSection == "servicio") null else "servicio"
                    }
                ) {
                    // Dropdown Tipo de servicio
                    ExposedDropdownMenuBox(
                        expanded = expandedTipoServicio,
                        onExpandedChange = { expandedTipoServicio = !expandedTipoServicio }
                    ) {
                        OutlinedTextField(
                            value = serviceType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de servicio", color = colors.textSecondary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoServicio) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00897B),
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTipoServicio,
                            onDismissRequest = { expandedTipoServicio = false }
                        ) {
                            ServiceType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        serviceType = type
                                        categoriaSeleccionada = "" // resetear al cambiar tipo
                                        expandedTipoServicio = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = serviceType.description,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dropdown Categoría
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoria,
                        onExpandedChange = { expandedCategoria = !expandedCategoria }
                    ) {
                        OutlinedTextField(
                            value = categoriaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría", color = colors.textSecondary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00897B),
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategoria,
                            onDismissRequest = { expandedCategoria = false }
                        ) {
                            categoriasDisponibles.forEach { categoria ->
                                DropdownMenuItem(
                                    text = { Text(categoria) },
                                    onClick = { categoriaSeleccionada = categoria; expandedCategoria = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = { mensaje = it },
                        label = { Text("Mensaje de presentación", color = colors.textSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00897B),
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Más adelante podés completar teléfono, dirección, empresa y horarios desde Editar perfil.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Mostrar error si lo hay
    if (registerState is RegisterState.Error) {
        val errorMsg = (registerState as RegisterState.Error).message
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {},
            title = { Text("Error al registrar") },
            text = { Text(errorMsg) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) { Text("OK") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Tarjeta de sección estilo Archivero (igual que EditProfileScreenUnified)
// ─────────────────────────────────────────────────────────────────
@Composable
fun RegisterSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandChange: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = getPrestadorColors()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Borde izquierdo de color
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
                // Cabecera clickeable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandChange() }
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
                        fontSize = 17.sp,
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

                // Contenido animado
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column { content() }
                }
            }
        }
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
    val colors = getPrestadorColors()
    
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
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = colors.textSecondary
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
                color = colors.primaryOrange
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
    val colors = getPrestadorColors()
    
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
                color = colors.primaryOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            
            Surface(
                shape = CircleShape,
                color = if (isExpanded) colors.primaryOrange.copy(alpha = 0.1f) else Color.Transparent,
                modifier = Modifier.clickable { onToggle() }
            ) {
                Icon(
                    if (isExpanded) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isExpanded) "Guardar" else "Editar",
                    tint = if (isExpanded) colors.primaryOrange else colors.textSecondary,
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
    leadingIcon: ImageVector?,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefixText: String? = null,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val colors = getPrestadorColors()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val hasText = value.isNotEmpty()
    
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            leadingIcon = {
                if (leadingContent != null) {
                    leadingContent()
                } else if (leadingIcon != null) {
                    Icon(
                        leadingIcon,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            },
            trailingIcon = when {
                trailingContent != null -> trailingContent
                trailingIcon != null -> ({
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(trailingIcon, contentDescription = null, tint = colors.textSecondary)
                    }
                })
                else -> null
            },
            label = {
                Text(
                    label,
                    color = if (isFocused) colors.primaryOrange else colors.textSecondary,
                    fontSize = if (isFocused || hasText) 12.sp else 16.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryOrange,
                unfocusedBorderColor = if (enabled) colors.border else Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedLabelColor = colors.primaryOrange,
                unfocusedLabelColor = colors.textSecondary,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                disabledTextColor = colors.textPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            singleLine = true,
            prefix = prefixText?.let { { Text(it, color = colors.textSecondary)}},
        )
    }
}

@Composable
fun TooltipBubble(
    text: String,
    onDismiss: () -> Unit
) {
    val colors = getPrestadorColors()
    
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.width(200.dp),
        color = colors.primaryOrange
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
    val colors = getPrestadorColors()
    
    if (services.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "No hay servicios seleccionados",
                color = colors.textSecondary,
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
    val colors = getPrestadorColors()
    
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
                        tint = colors.primaryOrange,
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
    val colors = getPrestadorColors()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(
            color = colors.divider,
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
                    color = colors.textPrimary
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.primaryOrange,
                    uncheckedColor = colors.border
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
    val colors = getPrestadorColors()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.border)
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
                    contentColor = colors.primaryOrange
                ),
                border = BorderStroke(1.dp, colors.primaryOrange),
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
    val colors = getPrestadorColors()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated)
            .drawBehind {
                // Dibujar línea izquierda naranja
                drawRect(
                    color = androidx.compose.ui.graphics.Color(0xFFF97316), // Keep orange for accent
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
                color = colors.primaryOrange,
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
                        tint = colors.error
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
    val colors = getPrestadorColors()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.surfaceColor,
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
                        .background(colors.primaryOrange)
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
                                    checkedColor = colors.primaryOrange,
                                    uncheckedColor = colors.border
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                service,
                                fontSize = 16.sp,
                                color = colors.textPrimary
                            )
                        }
                        
                        Divider(color = colors.divider)
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
                        Text("Cancelar", color = colors.primaryOrange)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryOrange
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
    val colors = getPrestadorColors()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(colors.surfaceColor)
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
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                tint = colors.primaryOrange,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Divider(
            color = colors.divider,
            thickness = 1.dp
        )
    }
}

// Data class
data class Sucursal(
    val direccion: String,
    val codigoPostal: String
)




