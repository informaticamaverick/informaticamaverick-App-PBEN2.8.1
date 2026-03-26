package com.example.myapplication.presentation.client

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * PANTALLA DE CREACIÓN DE LICITACIÓN
 * Esta pantalla permite al usuario crear una nueva licitación completando datos de rubro,
 * ubicación, fechas, descripción e imágenes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLicScreen(
    onBack: () -> Unit,
    profileViewModel: ProfileSharedViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel()
) {
    // Observamos el estado del usuario desde el ViewModel compartido
    val userState by profileViewModel.userState.collectAsState()
    // Observamos las categorías para el autocompletado
    val allCategories by categoryViewModel.categories.collectAsState()

    CrearLicContent(
        onBack = onBack,
        userState = userState,
        allCategories = allCategories,
        onCreateTender = { title, description, category, endDate ->
            budgetViewModel.createTender(
                title = title,
                description = description,
                category = category,
                endDate = endDate
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLicContent(
    onBack: () -> Unit,
    userState: UserEntity?,
    allCategories: List<CategoryEntity>,
    onCreateTender: (title: String, description: String, category: String, endDate: Long) -> Unit
) {
    // --- ESTADOS DEL FORMULARIO ---
    var categoryInput by remember { mutableStateOf("") }
    var showCategorySuggestions by remember { mutableStateOf(false) }
    
    // Estado para la ubicación seleccionada (Usa el modelo LocationOption de HomeScreenCliente3)
    var selectedLocation by remember { mutableStateOf<LocationOption?>(null) }
    
    // Configuración de fechas (Calendarios)
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    var startDate by remember { mutableStateOf(calendar.time) }
    var endDate by remember { mutableStateOf(Date(calendar.timeInMillis + TimeUnit.DAYS.toMillis(1))) }
    
    var description by remember { mutableStateOf("") }
    var requiresVisit by remember { mutableStateOf(false) }
    
    // Imágenes seleccionadas
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // Launcher para seleccionar imágenes de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = selectedImages + uris
    }
    
    // Launcher para tomar foto con la cámara (Simplificado para este ejemplo)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Aquí se procesaría el bitmap si se requiere
    }

    // Inicializar la ubicación por defecto cuando el usuario cargue
    LaunchedEffect(userState) {
        if (selectedLocation == null && userState != null) {
            val user = userState!!
            if (user.personalAddresses.isNotEmpty()) {
                val addr = user.personalAddresses.first()
                selectedLocation = LocationOption.Personal(addr.calle, addr.numero, addr.localidad)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0A0E14), // Fondo oscuro coherente con el App
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("NUEVA LICITACIÓN", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. ENCABEZADO COLAPSABLE CON DATOS DEL USUARIO/EMPRESA
            userState?.let { user ->
                CollapsibleHeaderReal(
                    user = user,
                    selectedLocation = selectedLocation,
                    onLocationChange = { selectedLocation = it }
                )
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                
                // 2. REQUIERE VISITA TÉCNICA (Debajo de la tarjeta del usuario)
                ToggleAviso(
                    active = requiresVisit,
                    onToggle = { requiresVisit = !requiresVisit },
                    title = "¿Requiere visita técnica?",
                    subtitle = "El profesional debe inspeccionar el lugar antes de cotizar."
                )

                // 3. RUBRO DEL SERVICIO (Búsqueda en CategoryEntity con Autocomplete)
                Column {
                    LabelWithIcon(Icons.Default.Search, "RUBRO DEL SERVICIO")
                    Box {
                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { 
                                categoryInput = it
                                showCategorySuggestions = it.isNotEmpty()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: Electricidad, Pintura...", color = Color.Gray) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF22D3EE),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                        
                        // Panel de sugerencias
                        if (showCategorySuggestions) {
                            val filtered = allCategories.filter { it.name.contains(categoryInput, ignoreCase = true) }
                            if (filtered.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 60.dp)
                                        .zIndex(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                                ) {
                                    Column {
                                        filtered.take(5).forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat.name, color = Color.White) },
                                                leadingIcon = { Text(cat.icon) },
                                                onClick = {
                                                    categoryInput = cat.name
                                                    showCategorySuggestions = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. CRONOGRAMA (Calendarios Inicio y Fin)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        LabelWithIcon(Icons.Default.DateRange, "PLAZOS ESTIMADOS")
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Fecha Inicio
                            DatePickerField(
                                label = "INICIO",
                                date = startDate,
                                onClick = {
                                    showDatePicker(context, startDate, minDate = System.currentTimeMillis()) { newDate ->
                                        startDate = newDate
                                        if (endDate.before(newDate)) {
                                            endDate = Date(newDate.time + TimeUnit.DAYS.toMillis(1))
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Fecha Fin
                            DatePickerField(
                                label = "FIN",
                                date = endDate,
                                onClick = {
                                    val maxDate = startDate.time + TimeUnit.DAYS.toMillis(15)
                                    showDatePicker(context, endDate, minDate = startDate.time, maxDate = maxDate) { newDate ->
                                        endDate = newDate
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Cálculo de diferencia de días
                        val diffMillis = endDate.time - startDate.time
                        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
                        Text(
                            text = "Duración estimada: $diffDays días (Máx 15)",
                            color = if (diffDays > 15) Color.Red else Color(0xFF22D3EE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // 5. MEMORIA DESCRIPTIVA
                Column {
                    LabelWithIcon(Icons.Default.Edit, "MEMORIA DESCRIPTIVA")
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("Detalle técnico del trabajo...", color = Color.Gray) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF22D3EE),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }

                // 6. VISOR Y CARGA DE FOTOS
                Column {
                    LabelWithIcon(Icons.Default.PhotoLibrary, "FOTOS DEL PROYECTO")
                    
                    if (selectedImages.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(selectedImages) { uri ->
                                Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp))) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { selectedImages = selectedImages - uri },
                                        modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(0.5f), CircleShape).size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Botón Galería
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF22D3EE))
                        ) {
                            Icon(Icons.Default.Collections, null, tint = Color(0xFF22D3EE))
                            Spacer(Modifier.width(8.dp))
                            Text("GALERÍA", color = Color(0xFF22D3EE))
                        }
                        
                        // Botón Cámara
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE91E63))
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, tint = Color(0xFFE91E63))
                            Spacer(Modifier.width(8.dp))
                            Text("CÁMARA", color = Color(0xFFE91E63))
                        }
                    }
                }

                // BOTÓN PUBLICAR: Guarda la licitación en la base de datos y vuelve atrás
                Button(
                    onClick = { 
                        // Usamos el ViewModel para crear la nueva licitación (TenderEntity)
                        onCreateTender(categoryInput, description, categoryInput, endDate.time)
                        // Navegamos hacia atrás después de guardar
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE)),
                    // El botón se activa solo si la categoría y descripción tienen texto
                    enabled = categoryInput.isNotBlank() && description.isNotBlank()
                ) {
                    Text("PUBLICAR LICITACIÓN", fontWeight = FontWeight.Black, color = Color.Black)
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

/**
 * Componente de Encabezado Colapsable.
 * Muestra los datos del perfil seleccionado (Usuario o Empresa/Sucursal).
 */
@Composable
fun CollapsibleHeaderReal(
    user: UserEntity,
    selectedLocation: LocationOption?,
    onLocationChange: (LocationOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddressSelector by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    // Gradiente al estilo HomeScreenCliente3
    val cardGradient = Brush.verticalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.03f)))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.background(cardGradient).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icono de perfil tipo HomeScreenCliente3
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF22D3EE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.photoUrl != null) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(Modifier.weight(1f)) {
                    Text(
                        text = user.displayName.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "TITULAR DE LICITACIÓN",
                        color = Color(0xFF22D3EE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.rotate(rotation))
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(Modifier.height(16.dp))

                    // Información Detallada basada en la selección
                    when (val loc = selectedLocation) {
                        is LocationOption.Business -> {
                            val company = user.companies.find { it.name == loc.companyName }
                            InfoLine("Empresa", loc.companyName)
                            InfoLine("Sucursal", loc.branchName)
                            InfoLine("CUIT", company?.cuit ?: "N/A")
                            InfoLine("Dirección", "${loc.address} ${loc.number}")
                            InfoLine("Localidad", loc.locality)
                        }
                        is LocationOption.Personal -> {
                             val addr = user.personalAddresses.find { it.calle == loc.address }
                            InfoLine("Usuario", "${user.name} ${user.lastName}")
                            InfoLine("CUIL/DNI", user.id) // Asumiendo id como CUIL o similar
                            InfoLine("Dirección", "${loc.address} ${loc.number}")
                             InfoLine("C.P.", addr?.codigoPostal ?: "-")
                            InfoLine("Localidad", loc.locality)
                        }
                        else -> {
                            InfoLine("Ubicación", "No seleccionada")
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Botón para cambiar datos (Seleccionar otra empresa/sucursal)
                    Button(
                        onClick = { showAddressSelector = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.SwapHoriz, null, tint = Color(0xFF22D3EE))
                        Spacer(Modifier.width(8.dp))
                        Text("CAMBIAR ORIGEN / DIRECCIÓN", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Selector de dirección (Popup simplificado)
    if (showAddressSelector) {
        AlertDialog(
            onDismissRequest = { showAddressSelector = false },
            containerColor = Color(0xFF1A1C1E),
            title = { Text("Seleccionar Ubicación", color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Direcciones Personales
                    Text("Personales", color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    user.personalAddresses.forEach { addr ->
                        ListItem(
                            headlineContent = { Text("${addr.calle} ${addr.numero}", color = Color.White) },
                            supportingContent = { Text(addr.localidad, color = Color.Gray) },
                            modifier = Modifier.clickable {
                                onLocationChange(LocationOption.Personal(addr.calle, addr.numero, addr.localidad))
                                showAddressSelector = false
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    
                    // Empresas y Sucursales
                    if (user.companies.isNotEmpty()) {
                        Text("Empresas", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        user.companies.forEach { company ->
                            company.branches.forEach { branch ->
                                ListItem(
                                    headlineContent = { Text("${company.name} - ${branch.name}", color = Color.White) },
                                    supportingContent = { Text("${branch.address.calle} ${branch.address.numero}", color = Color.Gray) },
                                    modifier = Modifier.clickable {
                                        onLocationChange(LocationOption.Business(
                                            company.name, branch.name, branch.address.calle, branch.address.numero, branch.address.localidad
                                        ))
                                        showAddressSelector = false
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddressSelector = false }) {
                    Text("CERRAR", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun DatePickerField(label: String, date: Date, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Column(modifier = modifier) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF22D3EE), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(formatter.format(date), color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

private fun showDatePicker(
    context: android.content.Context, 
    currentDate: Date, 
    minDate: Long? = null,
    maxDate: Long? = null,
    onDateSelected: (Date) -> Unit
) {
    val cal = Calendar.getInstance()
    cal.time = currentDate
    val dialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val result = Calendar.getInstance()
            result.set(year, month, day)
            onDateSelected(result.time)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )
    minDate?.let { dialog.datePicker.minDate = it }
    maxDate?.let { dialog.datePicker.maxDate = it }
    dialog.show()
}

@Composable
fun LabelWithIcon(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, null, Modifier.size(16.dp), tint = Color(0xFF22D3EE))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
    }
}

@Composable
fun ToggleAviso(active: Boolean, onToggle: () -> Unit, title: String, subtitle: String) {
    val borderColor = if (active) Color(0xFF22D3EE) else Color.White.copy(alpha = 0.1f)
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Warning, 
                null, 
                tint = if (active) Color(0xFF22D3EE) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
            Switch(
                checked = active, 
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF22D3EE))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrearLicScreenPreview() {
    val sampleUser = UserEntity(
        id = "1",
        email = "test@example.com",
        displayName = "Juan Perez",
        name = "Juan",
        lastName = "Perez",
        phoneNumber = "123456789",
        matricula = null,
        titulo = null,
        photoUrl = null,
        bannerImageUrl = null,
        hasCompanyProfile = false,
        isSubscribed = false,
        isVerified = true,
        isOnline = true,
        isFavorite = false,
        rating = 4.5f,
        createdAt = System.currentTimeMillis()
    )
    
    val sampleCategories = listOf(
        CategoryEntity(name = "Electricidad", icon = "⚡", color = 0xFFFAD2E1, superCategory = "Hogar", imageUrl = null, isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Plomería", icon = "🪠", color = 0xFFD4A5A5, superCategory = "Hogar", imageUrl = null, isNew = false, isNewPrestador = false, isAd = false)
    )

    MyApplicationTheme {
        CrearLicContent(
            onBack = {},
            userState = sampleUser,
            allCategories = sampleCategories,
            onCreateTender = { _, _, _, _ -> }
        )
    }
}
