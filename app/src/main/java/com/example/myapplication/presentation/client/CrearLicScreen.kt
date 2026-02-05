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
