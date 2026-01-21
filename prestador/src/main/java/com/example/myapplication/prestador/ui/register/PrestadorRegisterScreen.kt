package com.example.myapplication.prestador.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.theme.PrestadorOrange
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import com.example.myapplication.prestador.R

@Composable
fun PrestadorRegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: PrestadorRegisterViewModel = hiltViewModel()
) {
    // Verificar si el usuario ya está autenticado
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val isGoogleUser = currentUser != null
    
    // Estados del formulario
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }
    var provinciaSearch by remember { mutableStateOf("") }
    var showProvinciaDropdown by remember { mutableStateOf(false) }
    var serviciosSeleccionados by remember { mutableStateOf(listOf<String>()) }
    var isHomeService by remember { mutableStateOf(false) }
    var is24Hours by remember { mutableStateOf(false) }
    var serviceSearch by remember { mutableStateOf("") }
    var showServiceDropdown by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showProvinciaMenu by remember { mutableStateOf(false) }
    var showServiciosDialog by remember { mutableStateOf(false) }
    
    val registerState by viewModel.registerState.collectAsState()
    
    // Listas de opciones
    val provincias = listOf("Tucumán")
    
    // Filtrar provincias según búsqueda
    val filteredProvincias = remember(provinciaSearch, provincia) {
        provincias.filter {
            it.contains(provinciaSearch, ignoreCase = true) && it != provincia
        }
    }
    
    val serviciosDisponibles = listOf(
        "Plomería", "Electricidad", "Carpintería", "Pintura",
        "Albañilería", "Jardinería", "Limpieza", "Cerrajería",
        "Herrería", "Vidriería", "Climatización", "Mudanzas"
    )
    
    // Filtrar servicios según búsqueda
    val filteredServices = remember(serviceSearch, serviciosSeleccionados) {
        serviciosDisponibles.filter {
            it.contains(serviceSearch, ignoreCase = true) && !serviciosSeleccionados.contains(it)
        }
    }
    
    // Manejar el estado del registro
    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                onRegisterSuccess()
            }
            is RegisterState.Error -> {
                errorMessage = (registerState as RegisterState.Error).message
            }
            else -> {}
        }
    }
    
    // Animación de pulso para el círculo superior izquierdo
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5ED))
    ) {
        // --- EFECTO DE DIFUMINADO ---
        
        // 1. Círculo Superior Izquierda (Con animación de pulso)
        Box(
            modifier = Modifier
                .offset(x = (-96).dp, y = (-96).dp)
                .size(384.dp)
                .background(
                    color = Color(0xFFFB923C).copy(alpha = pulseAlpha),
                    shape = CircleShape
                )
                .blur(radius = 80.dp)
        )
        
        // 2. Círculo Inferior Derecha
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 0.dp)
                .size(320.dp)
                .background(
                    color = Color(0xFFFBBF24).copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .blur(radius = 80.dp)
        )
        
        // 3. Círculo Central/Inferior
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = 200.dp)
                .size(256.dp)
                .background(
                    color = Color(0xFFFCA5A5).copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .blur(radius = 100.dp)
        )
        
        // Contenido principal con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Título
            Text(
                text = "Registro de Prestador",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Tarjeta de registro
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Completa tus datos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Campo Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Nombre",
                                tint = Color.Gray
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = PrestadorOrange,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = PrestadorOrange,
                            cursorColor = PrestadorOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo Apellido
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Apellido",
                                tint = Color.Gray
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = PrestadorOrange,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = PrestadorOrange,
                            cursorColor = PrestadorOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo Email (solo si NO es usuario de Google)
                    if (!isGoogleUser) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo electrónico") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color.Gray
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = PrestadorOrange,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedLabelColor = PrestadorOrange,
                                cursorColor = PrestadorOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Campo Contraseña
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = Color.Gray
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                                        ),
                                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = if (passwordVisible) PrestadorOrange else Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = PrestadorOrange,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedLabelColor = PrestadorOrange,
                                cursorColor = PrestadorOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Campo Confirmar Contraseña
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirmar Contraseña") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Confirm Password",
                                    tint = Color.Gray
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (confirmPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                                        ),
                                        contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = if (confirmPasswordVisible) PrestadorOrange else Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = PrestadorOrange,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedLabelColor = PrestadorOrange,
                                cursorColor = PrestadorOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        // Mostrar email del usuario de Google (solo lectura)
                        OutlinedTextField(
                            value = email,
                            onValueChange = {},
                            label = { Text("Correo electrónico") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color.Gray
                                )
                            },
                            enabled = false,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color(0xFFF1F5F9),
                                disabledBorderColor = Color(0xFFE2E8F0),
                                disabledLabelColor = Color.Gray,
                                disabledTextColor = Color(0xFF64748B)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Campo Dirección
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Dirección",
                                tint = Color.Gray
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = PrestadorOrange,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = PrestadorOrange,
                            cursorColor = PrestadorOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo Código Postal
                    OutlinedTextField(
                        value = codigoPostal,
                        onValueChange = { codigoPostal = it },
                        label = { Text("Código Postal") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Código Postal",
                                tint = Color.Gray
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = PrestadorOrange,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = PrestadorOrange,
                            cursorColor = PrestadorOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo Ciudad
                    OutlinedTextField(
                        value = ciudad,
                        onValueChange = { ciudad = it },
                        label = { Text("Ciudad") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ciudad",
                                tint = Color.Gray
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = PrestadorOrange,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedLabelColor = PrestadorOrange,
                            cursorColor = PrestadorOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // --- CAMPO PROVINCIA CON BUSCADOR INTERACTIVO ---
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Provincia",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Campo de búsqueda
                        Box {
                            OutlinedTextField(
                                value = if (provincia.isNotEmpty() && provinciaSearch.isEmpty()) provincia else provinciaSearch,
                                onValueChange = {
                                    provinciaSearch = it
                                    showProvinciaDropdown = it.isNotEmpty()
                                    if (it.isEmpty()) {
                                        provincia = ""
                                    }
                                },
                                placeholder = { Text("Buscar provincia", fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = if (showProvinciaDropdown || provincia.isNotEmpty()) PrestadorOrange else Color.Gray
                                    )
                                },
                                trailingIcon = {
                                    if (provincia.isNotEmpty() && provinciaSearch.isEmpty()) {
                                        IconButton(onClick = {
                                            provincia = ""
                                            provinciaSearch = ""
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Limpiar",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = PrestadorOrange,
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    cursorColor = PrestadorOrange
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            
                            // Dropdown de resultados
                            if (showProvinciaDropdown && filteredProvincias.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 70.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 150.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        filteredProvincias.forEach { prov ->
                                            TextButton(
                                                onClick = {
                                                    provincia = prov
                                                    provinciaSearch = ""
                                                    showProvinciaDropdown = false
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp),
                                                colors = ButtonDefaults.textButtonColors(
                                                    containerColor = Color.Transparent
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = prov,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF475569)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Seleccionar",
                                                        tint = PrestadorOrange,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            if (prov != filteredProvincias.last()) {
                                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // --- BOTONES DE DISPONIBILIDAD (DOMICILIO / 24HS) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ¿Vas a domicilio?
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF8FAFC)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "¿Vas a\ndomicilio?",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    lineHeight = 14.sp
                                )
                                Switch(
                                    checked = isHomeService,
                                    onCheckedChange = { isHomeService = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrestadorOrange,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFCBD5E1)
                                    )
                                )
                            }
                        }
                        
                        // Servicio 24hs
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF8FAFC)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Servicio 24hs",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "Activa FAST",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrestadorOrange
                                    )
                                }
                                Switch(
                                    checked = is24Hours,
                                    onCheckedChange = { is24Hours = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrestadorOrange,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFCBD5E1)
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // --- BUSCADOR DE SERVICIOS INTERACTIVO ---
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "¿Qué servicios vas a prestar?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Chips de servicios seleccionados
                        if (serviciosSeleccionados.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                items(serviciosSeleccionados) { servicio ->
                                    AssistChip(
                                        onClick = {
                                            serviciosSeleccionados = serviciosSeleccionados - servicio
                                        },
                                        label = {
                                            Text(
                                                servicio,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = Color(0xFFFED7AA),
                                            labelColor = Color(0xFFC2410C),
                                            leadingIconContentColor = Color(0xFFC2410C)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFFEBF8C))
                                    )
                                }
                            }
                        }
                        
                        // Campo de búsqueda
                        Box {
                            OutlinedTextField(
                                value = serviceSearch,
                                onValueChange = {
                                    serviceSearch = it
                                    showServiceDropdown = it.isNotEmpty()
                                },
                                placeholder = { Text("Buscar servicio (ej. Electricista)", fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = if (showServiceDropdown) PrestadorOrange else Color.Gray
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = PrestadorOrange,
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    cursorColor = PrestadorOrange
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            
                            // Dropdown de resultados
                            if (showServiceDropdown && filteredServices.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 70.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        filteredServices.forEach { servicio ->
                                            TextButton(
                                                onClick = {
                                                    serviciosSeleccionados = serviciosSeleccionados + servicio
                                                    serviceSearch = ""
                                                    showServiceDropdown = false
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp),
                                                colors = ButtonDefaults.textButtonColors(
                                                    containerColor = Color.Transparent
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = servicio,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF475569)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Agregar",
                                                        tint = PrestadorOrange,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            if (servicio != filteredServices.last()) {
                                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo Servicios (Multi-selección) - COMENTADO PARA REEMPLAZAR
                    /*
                    OutlinedButton(
                        onClick = { showServiciosDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFF8FAFC)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (serviciosSeleccionados.isEmpty()) 
                                    "Seleccionar servicios" 
                                else 
                                    "${serviciosSeleccionados.size} servicio(s) seleccionado(s)",
                                color = if (serviciosSeleccionados.isEmpty()) Color.Gray else Color(0xFF1E293B)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Seleccionar",
                                tint = PrestadorOrange
                            )
                        }
                    }
                    
                    // Mostrar servicios seleccionados
                    if (serviciosSeleccionados.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = serviciosSeleccionados.joinToString(", "),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    */
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Mensaje de error
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Botón de Registro
                    Button(
                        onClick = {
                            when {
                                nombre.isBlank() || apellido.isBlank() || direccion.isBlank() ||
                                codigoPostal.isBlank() || ciudad.isBlank() || provincia.isBlank() -> {
                                    errorMessage = "Por favor completa todos los campos"
                                }
                                !isGoogleUser && (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) -> {
                                    errorMessage = "Por favor completa todos los campos"
                                }
                                !isGoogleUser && password != confirmPassword -> {
                                    errorMessage = "Las contraseñas no coinciden"
                                }
                                !isGoogleUser && password.length < 6 -> {
                                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                }
                                serviciosSeleccionados.isEmpty() -> {
                                    errorMessage = "Selecciona al menos un servicio"
                                }
                                else -> {
                                    viewModel.register(
                                        email, password, nombre, apellido, direccion,
                                        codigoPostal, ciudad, provincia, serviciosSeleccionados,
                                        isHomeService, is24Hours
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrestadorOrange
                        ),
                        enabled = registerState !is RegisterState.Loading
                    ) {
                        if (registerState is RegisterState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Registrarse",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón para volver al login
                    TextButton(onClick = onBackToLogin) {
                        Text(
                            text = "¿Ya tienes cuenta? Inicia sesión",
                            color = PrestadorOrange,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    
    // Diálogo de selección de servicios
    if (showServiciosDialog) {
        AlertDialog(
            onDismissRequest = { showServiciosDialog = false },
            title = {
                Text(
                    text = "Seleccionar Servicios",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    serviciosDisponibles.forEach { servicio ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = serviciosSeleccionados.contains(servicio),
                                onCheckedChange = { isChecked ->
                                    serviciosSeleccionados = if (isChecked) {
                                        serviciosSeleccionados + servicio
                                    } else {
                                        serviciosSeleccionados - servicio
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrestadorOrange
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = servicio)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showServiciosDialog = false }
                ) {
                    Text(
                        "Aceptar",
                        color = PrestadorOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}
