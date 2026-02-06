package com.example.myapplication.presentation.client

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*

// --- ENTIDADES DE DATOS ---
data class UserEntity(
    val name: String,
    val email: String,
    val cuil: String,
    val personalAddresses: List<AddressEntity>,
    val companies: List<CompanyEntity>
)

data class AddressEntity(val id: Int, val alias: String, val street: String, val city: String) {
    fun fullString() = "$street, $city"
}

data class CompanyEntity(val name: String, val cuil: String, val branches: List<BranchEntity>)
data class BranchEntity(val id: Int, val name: String, val address: String)

sealed class LicitacionStep {
    object Form : LicitacionStep()
    object Preview : LicitacionStep()
    object Ad : LicitacionStep()
    object Success : LicitacionStep()
}

// --- MOCK DATA ---
val MockUser = UserEntity(
    name = "Juan Pérez",
    email = "juan.perez@email.com",
    cuil = "20-30444555-9",
    personalAddresses = listOf(
        AddressEntity(1, "Casa Central", "Av. Corrientes 1500", "CABA"),
        AddressEntity(2, "Depto", "Calle Falsa 123", "Rosario")
    ),
    companies = listOf(
        CompanyEntity(
            name = "Construcciones S.A.",
            cuil = "30-71234567-8",
            branches = listOf(
                BranchEntity(101, "Sede Operativa", "Av. Libertador 1234, CABA"),
                BranchEntity(102, "Depósito Norte", "Ruta 9 Km 500, Córdoba")
            )
        )
    )
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CrearLicScreen(
    onBack: () -> Unit
    ) {

    var currentStep by remember { mutableStateOf<LicitacionStep>(LicitacionStep.Form) }

    // Estados del Formulario
    var categoryInput by remember { mutableStateOf("") }
    var address by remember { mutableStateOf(MockUser.personalAddresses.first().fullString()) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requiresVisit by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val requirements = remember { mutableStateMapOf(
        "paymentAnticipo" to false,
        "paymentCertificado" to false,
        "docSeguro" to false,
        "docArt" to false
    ) }
    val requirementLabels = mapOf(
        "paymentAnticipo" to "Requiere Anticipo",
        "paymentCertificado" to "Pago por Certificado",
        "docSeguro" to "Seguro de Obra",
        "docArt" to "ART Personal"
    )
// --- UI PRINCIPAL ---
    Scaffold(
        // 1. CLAVE MODO OSCURO: Usar el color de fondo del tema, no blanco fijo
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Nueva Licitación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            // El color se adapta solo (onSurface)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Para pantallas pequeñas
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Sección: Datos Básicos
            Text(
                text = "Detalles del Trabajo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary // Color de acento
            )

            OutlinedTextField(
                value = categoryInput,
                onValueChange = { categoryInput = it },
                label = { Text("Categoría (ej. Electricidad)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción del problema") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Sección: Requisitos (Lo que faltaba)
            HorizontalDivider()
            Text(
                text = "Requisitos y Documentación",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Renderizamos los checkboxes dinámicamente
            requirements.forEach { (key, isChecked) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requirements[key] = !isChecked }
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { requirements[key] = it }
                    )
                    Text(
                        text = requirementLabels[key] ?: key,
                        style = MaterialTheme.typography.bodyMedium,
                        // Texto adaptable al modo oscuro
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Sección: Visita
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = requiresVisit,
                    onCheckedChange = { requiresVisit = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "¿Requiere visita técnica?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Marca si el profesional debe ir antes de cotizar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { /* Lógica de guardado */ },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Publicar Licitación")
            }
        }
    }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            slideInHorizontally { it } + fadeIn() with slideOutHorizontally { -it } + fadeOut()
        }
    ) { step ->
        when (step) {
            is LicitacionStep.Form -> {
                LicitacionFormScreen(
                    user = MockUser,
                    categoryInput = categoryInput,
                    onCategoryChange = { categoryInput = it },
                    address = address,
                    onAddressChange = { address = it },
                    startDate = startDate,
                    onStartDateChange = { startDate = it },
                    endDate = endDate,
                    onEndDateChange = { endDate = it },
                    budget = budget,
                    onBudgetChange = { budget = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    requiresVisit = requiresVisit,
                    onVisitToggle = { requiresVisit = !requiresVisit },
                    requirements = requirements,
                    images = selectedImages,
                    onAddImage = { /* Lógica de Picker */ },
                    onNext = { currentStep = LicitacionStep.Preview }
                )
            }
            is LicitacionStep.Preview -> {
                LicitacionPreviewScreen(
                    category = categoryInput,
                    address = address,
                    endDate = endDate,
                    budget = budget,
                    description = description,
                    requiresVisit = requiresVisit,
                    requirements = requirements,
                    onBack = { currentStep = LicitacionStep.Form },
                    onConfirm = { currentStep = LicitacionStep.Ad }
                )
            }
            is LicitacionStep.Ad -> {
                LicitacionAdScreen(
                    onFinished = { currentStep = LicitacionStep.Success }
                )
            }
            is LicitacionStep.Success -> {
                LicitacionSuccessScreen(onReset = { currentStep = LicitacionStep.Form })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicitacionFormScreen(
    user: UserEntity,
    categoryInput: String,
    onCategoryChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    budget: String,
    onBudgetChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    requiresVisit: Boolean,
    onVisitToggle: () -> Unit,
    requirements: Map<String, Boolean>,
    images: List<Uri>,
    onAddImage: () -> Unit,
    onNext: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("LANZAR LICITACIÓN", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            CollapsibleHeader(user)

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {

                // Categoría Autocomplete (Simplificado)
                Column {
                    LabelWithIcon(Icons.Default.Search, "Rubro del Servicio")
                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = onCategoryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: Electricidad Industrial") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                // Ubicación (Dropdown logic)
                var expanded by remember { mutableStateOf(false) }
                Column {
                    LabelWithIcon(Icons.Default.LocationOn, "Ubicación del Proyecto")
                    Box {
                        OutlinedTextField(
                            value = address,
                            onValueChange = onAddressChange,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expanded = !expanded }) {
                                    Icon(Icons.Default.KeyboardArrowDown, null)
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            user.personalAddresses.forEach { addr ->
                                DropdownMenuItem(
                                    text = { Text(addr.alias + ": " + addr.street) },
                                    onClick = { onAddressChange(addr.fullString()); expanded = false }
                                )
                            }
                            user.companies.flatMap { it.branches }.forEach { branch ->
                                DropdownMenuItem(
                                    text = { Text(branch.name + ": " + branch.address) },
                                    onClick = { onAddressChange(branch.address); expanded = false }
                                )
                            }
                        }
                    }
                }

                // Cronograma
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        LabelWithIcon(Icons.Default.DateRange, "Plazos")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = onStartDateChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("Inicio", fontSize = 10.sp) },
                                placeholder = { Text("DD/MM/AA") }
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = onEndDateChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("Cierre", fontSize = 10.sp) },
                                placeholder = { Text("DD/MM/AA") }
                            )
                        }
                    }
                }

                // Visita de Obra
                ToggleAviso(
                    active = requiresVisit,
                    onToggle = onVisitToggle,
                    title = "¿Requiere visita previa?",
                    subtitle = "Obligatorio para que coticen los proveedores."
                )

                // Memoria Descriptiva
                Column {
                    LabelWithIcon(Icons.Default.Edit, "Memoria Descriptiva")
                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("Detalle técnico del trabajo...") },
                        shape = RoundedCornerShape(24.dp)
                    )
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun LicitacionAdScreen(onFinished: () -> Unit) {
    var timeLeft by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(280.dp, 160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).background(Color.Black.copy(0.6f), CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Anuncio: ${timeLeft}s", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(40.dp))
            Text("PROCESANDO PUBLICACIÓN", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(60.dp))
            Button(
                onClick = onFinished,
                enabled = timeLeft == 0,
                modifier = Modifier.width(240.dp).height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timeLeft == 0) Color(0xFF22C55E) else Color.DarkGray
                )
            ) {
                Text(if (timeLeft == 0) "PUBLICAR AHORA" else "ESPERE...", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicitacionPreviewScreen(
    category: String,
    address: String,
    endDate: String,
    budget: String,
    description: String,
    requiresVisit: Boolean,
    requirements: Map<String, Boolean>,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VISTA PREVIA", fontWeight = FontWeight.Black, fontSize = 14.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().padding(24.dp).height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(12.dp))
                Text("CONFIRMAR Y PUBLICAR", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            // Bento Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF4F46E5))
                    .padding(24.dp)
            ) {
                Column {
                    Text("RESUMEN DE PROYECTO", color = Color.White.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text(category.ifBlank { "Rubro General" }, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(address, color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("DETALLES TÉCNICOS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyLarge)

            if (requiresVisit) {
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFFEA580C))
                        Spacer(Modifier.width(12.dp))
                        Text("Visita de obra obligatoria", color = Color(0xFFEA580C), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LicitacionSuccessScreen(onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(100.dp))
        Spacer(Modifier.height(24.dp))
        Text("¡EXITOSO!", fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineLarge)
        Text("Tu licitación ha sido lanzada.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(Modifier.height(48.dp))
        Button(onClick = onReset, shape = RoundedCornerShape(16.dp)) {
            Text("VOLVER AL PANEL")
        }
    }
}

// --- SUB-COMPONENTES UI ---

@Composable
fun CollapsibleHeader(user: UserEntity) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).background(Color(0xFF4F46E5), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Build, null, tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(user.companies.firstOrNull()?.name ?: user.name, fontWeight = FontWeight.Black)
                    Text("PERFIL PROFESIONAL", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.KeyboardArrowDown, null, Modifier.rotate(rotation))
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 20.dp).fillMaxWidth()) {
                    Divider(color = Color.LightGray.copy(0.3f))
                    Spacer(Modifier.height(16.dp))
                    InfoRow(Icons.Default.Info, "CUIL: ${user.companies.firstOrNull()?.cuil ?: user.cuil}")
                    InfoRow(Icons.Default.Email, user.email)
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, null, Modifier.size(16.dp), tint = Color(0xFF4F46E5))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 13.sp, color = Color.DarkGray)
    }
}

@Composable
fun LabelWithIcon(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, null, Modifier.size(14.dp), tint = Color(0xFF4F46E5))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray)
    }
}

@Composable
fun ToggleAviso(active: Boolean, onToggle: () -> Unit, title: String, subtitle: String) {
    val bgColor = if (active) Color(0xFFEA580C) else Color.White
    val contentColor = if (active) Color.White else Color.Black

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (!active) BorderStroke(1.dp, Color.LightGray.copy(0.5f)) else null
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = if (active) Color.White else Color(0xFFEA580C))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = contentColor)
                Text(subtitle, fontSize = 10.sp, color = contentColor.copy(0.7f))
            }
            Switch(checked = active, onCheckedChange = { onToggle() })
        }
    }
}













/**
package com.example.myapplication.presentation.client

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.ui.screens.ProfileMode // Se mantiene la importación por si se descomenta el código
import com.example.myapplication.ui.theme.getAppColors
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.fake.CategoryItem
import com.example.myapplication.data.model.fake.CategorySampleDataFalso
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLicScreen(
    onBack: () -> Unit,
    viewModel: ProfileSharedViewModel = hiltViewModel()
) {
    val categories = CategorySampleDataFalso.categories
    val userState by viewModel.userState.collectAsState()

    // --- CÓDIGO COMENTADO: Lógica de ProfileMode restaurada pero inactiva ---
    // val profileMode by viewModel.profileMode.collectAsState()

    if (userState != null) {
        CrearLicScreenContent(
            onBack = onBack,
            categories = categories,
            user = userState!!
            // --- CÓDIGO COMENTADO: Ya no se pasa profileMode ---
            // profileMode = profileMode
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLicScreenContent(
    onBack: () -> Unit,
    categories: List<CategoryItem>,
    user: UserEntity
    // --- CÓDIGO COMENTADO: Parámetro restaurado pero inactivo ---
    // profileMode: ProfileMode 
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFakeAd by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showHeadsUpNotification by remember { mutableStateOf(false) }

    LaunchedEffect(showHeadsUpNotification) {
        if (showHeadsUpNotification) {
            delay(3000)
            showHeadsUpNotification = false
            showFakeAd = true
        }
    }

    BackHandler {
        if (showFakeAd) {
            onBack()
        } else {
            onBack()
        }
    }

    // Estados del formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    // ... (otros estados del formulario sin cambios)
    var address by remember { mutableStateOf("") }
    var locationExpanded by remember { mutableStateOf(false) }

    // Efecto para actualizar la dirección por defecto.
    LaunchedEffect(user) {
        // Lógica activa: Siempre toma la primera dirección personal.
        address = user.personalAddresses.firstOrNull()?.fullString() ?: ""

        // --- CÓDIGO COMENTADO: Lógica anterior que dependía del modo ---
        /*
        address = if (profileMode == ProfileMode.CLIENTE) {
            user.personalAddresses.firstOrNull()?.fullString() ?: ""
        } else {
            user.companies.firstOrNull()?.branches?.firstOrNull()?.address?.fullString() ?: ""
        }
        */
    }
    
    val colors = getAppColors()
/**
    // Filtrar categorías según búsqueda
    val filteredCategories = remember(categorySearchQuery, categories) {
        if (categorySearchQuery.isBlank()) {
            emptyList()
        } else {
            categories.filter { it.name.startsWith(categorySearchQuery, ignoreCase = true) }
        }
    }
**/
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        selectedImages = uris
    }

    if (showFakeAd) {
        FakeGoogleAdScreen(onClose = onBack)
    } else {
        Scaffold(/*...*/) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.backgroundColor)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // ... (Sección Datos Personales, nombre y apellido sin cambios)

                // --- Campo de Dirección ---
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección del Proyecto") },
                    trailingIcon = {
                        Box {
                            IconButton(onClick = { locationExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Cambiar dirección")
                            }
                            DropdownMenu(
                                expanded = locationExpanded,
                                onDismissRequest = { locationExpanded = false }
                            ) {
                                // Lógica activa: Muestra direcciones personales y de empresas siempre.
                                user.personalAddresses.forEach { addr ->
                                    DropdownMenuItem(
                                        text = { Text("Casa: ${addr.fullString()}") },
                                        onClick = {
                                            address = addr.fullString()
                                            locationExpanded = false
                                        }
                                    )
                                }
                                user.companies.forEach { company ->
                                    company.branches.forEach { branch ->
                                        DropdownMenuItem(
                                            text = { Text("${company.name}: ${branch.address.fullString()}") },
                                            onClick = {
                                                address = branch.address.fullString()
                                                locationExpanded = false
                                            }
                                        )
                                    }
                                }

                                // --- CÓDIGO COMENTADO: Lógica anterior que dependía del modo ---
                                /*
                                if (profileMode == ProfileMode.CLIENTE) {
                                    user.personalAddresses.forEach { addr ->
                                        DropdownMenuItem(
                                            text = { Text("Casa: ${addr.fullString()}") },
                                            onClick = {
                                                address = addr.fullString()
                                                locationExpanded = false
                                            }
                                        )
                                    }
                                } else {
                                    user.companies.forEach { company ->
                                        company.branches.forEach { branch ->
                                            DropdownMenuItem(
                                                text = { Text("${company.name}: ${branch.address.fullString()}") },
                                                onClick = {
                                                    address = branch.address.fullString()
                                                    locationExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                */
                                DropdownMenuItem(
                                    text = { Text("Otra ubicación") },
                                    onClick = {
                                        address = "" // Limpiar para que el usuario escriba
                                        locationExpanded = false
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // ... (El resto del formulario de licitación, categorías, fechas, fotos, etc. no necesita cambios)
            }
        }
    }
}


// ... (El resto de componentes como FakeGoogleAdScreen, HeadsUpNotification, DatePickers, etc., no cambian)
// Se incluyen stubs para asegurar la compilación.

@Composable fun FakeGoogleAdScreen(onClose: () -> Unit) { /* ... */ }
@Composable fun HeadsUpNotification(visible: Boolean, title: String, description: String) { /* ... */ }
@Composable fun InfoBannerContent() { /* ... */ }
@Preview(showBackground = true)
@Composable fun CrearLicScreenPreview() { /* ... */ }
**/