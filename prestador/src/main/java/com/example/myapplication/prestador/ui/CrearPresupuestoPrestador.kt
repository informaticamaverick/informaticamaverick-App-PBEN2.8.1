package com.example.myapplication.prestador.ui

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.data.PPrestadorSampleDataFalso
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.PresupuestoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// --- DATA MODELS ---
data class BudgetItem(
    val id: Long = System.currentTimeMillis(),
    val code: String = "",
    val description: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1,
    val taxPercentage: Double = 0.0,
    val discountPercentage: Double = 0.0
)

data class BudgetService(
    val id: Long = System.currentTimeMillis(),
    var code: String = "",
    var description: String = "",
    var total: Double = 0.0
)

data class BudgetProfessionalFee(
    val id: Long = System.currentTimeMillis(),
    var code: String = "",
    var description: String = "",
    var total: Double = 0.0
)

data class BudgetMiscExpense(
    val id: Long = System.currentTimeMillis(),
    var description: String = "",
    var amount: Double = 0.0
)

data class BudgetTax(
    val id: Long = System.currentTimeMillis(),
    var description: String = "",
    var amount: Double = 0.0
)

data class BudgetAttachment(
    val id: Long = System.currentTimeMillis(),
    val uri: Uri? = null,
    var description: String = "",
    val type: AttachmentType = AttachmentType.IMAGE
)

enum class AttachmentType { IMAGE, PDF }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoPrestadorScreen(
    appointmentId: String = "",
    onBack: () -> Unit = {},
    viewModel: PresupuestoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val colors = getPrestadorColors()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prestador = remember {
        PPrestadorProfileFalso(
            id = "demo_1",
            name = "Maxi",
            lastName = "Nanterne",
            profileImageUrl = "https://example.com/demo.png",
            bannerImageUrl = null,
            rating = 5.0f,
            isVerified = true,
            isOnline = true,
            services = listOf("Informatica"),
            companyName = "Maverick Informatica (DEMO)",
            address = "B. Matienzo 1339",
            email = "demo@maverick.com",
            doesHomeVisits = true,
            hasPhysicalLocation = true,
            works24h = false,
            galleryImages = emptyList(),
            isFavorite = true,
            isSubscribed = true
        )
    }

    val items = remember { mutableStateListOf<BudgetItem>() }
    val services = remember { mutableStateListOf<BudgetService>() }
    val professionalFees = remember { mutableStateListOf<BudgetProfessionalFee>() }
    val miscExpenses = remember { mutableStateListOf<BudgetMiscExpense>() }
    val taxes = remember { mutableStateListOf<BudgetTax>() }
    val attachments = remember { mutableStateListOf<BudgetAttachment>() }

    val lazyListState = rememberLazyListState()

    var showPreviewDialog by remember { mutableStateOf(false) }
    var pendingSimPresupuestoId by remember { mutableStateOf("") }
    var pendingSimClienteName by remember { mutableStateOf("") }
    var pendingSimTotal by remember { mutableStateOf(0.0) }

    // --- BOTTOM SHEET STATES ---
    var sheetType by remember { mutableStateOf<SheetType?>(null) }
    var itemToEdit by remember { mutableStateOf<Any?>(null) }


    // --- SECTION EXPANSION STATES ---
    var isArticlesExpanded by remember { mutableStateOf(true) }
    var isServicesExpanded by remember { mutableStateOf(true) }
    var isProfessionalFeesExpanded by remember { mutableStateOf(true) }
    var isMiscExpanded by remember { mutableStateOf(true) }
    var isTaxesExpanded by remember { mutableStateOf(true) }
    var isAttachmentsExpanded by remember { mutableStateOf(true) }

    // --- SECTION VISIBILITY STATES ---
    var showArticlesSection by remember { mutableStateOf(true) }
    var showServicesSection by remember { mutableStateOf(true) }
    var showProfessionalFeesSection by remember { mutableStateOf(true) }
    var showMiscSection by remember { mutableStateOf(true) }
    var showTaxesSection by remember { mutableStateOf(true) }
    var showAttachmentsSection by remember { mutableStateOf(true) }


    // --- NEW FIELDS STATE ---
    var validity by remember { mutableStateOf("7") }
    var notes by remember { mutableStateOf("") }
    
    // --- CLIENTE STATE ---
    var clienteNombre by remember { mutableStateOf("") }
    var clienteEmail by remember { mutableStateOf("") }
    var clienteTelefono by remember { mutableStateOf("") }
    var clienteDireccion by remember { mutableStateOf("") }

    // --- CALCULATED TOTALS ---
    // Calcular subtotal base (sin impuestos ni descuentos)
    val itemsBaseSubtotal = items.sumOf { it.unitPrice * it.quantity }
    
    // Calcular impuestos totales
    val itemsTaxTotal = items.sumOf {
        val base = it.unitPrice * it.quantity
        base * (it.taxPercentage / 100)
    }
    
    // Calcular descuentos totales
    val itemsDiscountTotal = items.sumOf {
        val base = it.unitPrice * it.quantity
        val taxAmount = base * (it.taxPercentage / 100)
        val withTax = base + taxAmount
        withTax * (it.discountPercentage / 100)
    }
    
    // Subtotal de items después de impuestos y descuentos
    val itemsSubtotal = itemsBaseSubtotal + itemsTaxTotal - itemsDiscountTotal

    val servicesSubtotal = services.sumOf { it.total }
    val professionalFeesSubtotal = professionalFees.sumOf { it.total }
    val miscSubtotal = miscExpenses.sumOf { it.amount }
    val taxesSubtotal = taxes.sumOf { it.amount }

    val subtotal = itemsSubtotal + servicesSubtotal + professionalFeesSubtotal + miscSubtotal
    val grandTotal = subtotal + taxesSubtotal


    // --- DERIVED STATE FOR SCROLL ---
    val isScrolledToEnd by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                true
            } else {
                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index == layoutInfo.totalItemsCount - 1
            }
        }
    }

    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Crear Presupuesto",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.primaryOrange
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { sheetType = SheetType.Client },
                    containerColor = Color(0xFFFF6B35),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Cliente")
                }
                FloatingActionButton(
                    onClick = { 
                        // Guardar presupuesto en la BD
                        val clienteId = if (clienteNombre.isNotBlank()) {
                            val nuevoClienteId = "cliente_${System.currentTimeMillis()}"
                            viewModel.insertCliente(
                                com.example.myapplication.prestador.data.local.entity.ClienteEntity(
                                    id = nuevoClienteId,
                                    nombre = clienteNombre,
                                    email = clienteEmail,
                                    telefono = clienteTelefono,
                                    direccion = clienteDireccion
                                )
                            )
                            nuevoClienteId
                        } else {
                            // Cliente por defecto si no se ingresó información
                            val defaultClienteId = "cliente_default_${System.currentTimeMillis()}"
                            viewModel.insertCliente(
                                com.example.myapplication.prestador.data.local.entity.ClienteEntity(
                                    id = defaultClienteId,
                                    nombre = "Cliente Sin Nombre",
                                    email = "",
                                    telefono = "",
                                    direccion = ""
                                )
                            )
                            defaultClienteId
                        }
                        
                        // Usar el ID del prestador actual (o crear uno demo si no existe)
                        val prestadorId = "prestador_${System.currentTimeMillis()}"
                        
                        // Convertir items a JSON simple
                        val itemsJson = items.joinToString("|") { item ->
                            "${item.description};${item.quantity};${item.unitPrice};${item.taxPercentage};${item.discountPercentage}"
                        }
                        
                        // Convertir servicios a JSON simple
                        val serviciosJson = services.joinToString("|") { service ->
                            "${service.code};${service.description};${service.total}"
                        }
                        
                        // Crear presupuesto SIN Foreign Keys para evitar crash
                        // (Las FK se validarán cuando implementemos el login real)
                        val nuevoPresupuesto = com.example.myapplication.prestador.data.local.entity.PresupuestoEntity(
                            id = "pres_${System.currentTimeMillis()}",
                            numeroPresupuesto = "P-${(1000..9999).random()}",
                            clienteId = clienteId,
                            prestadorId = prestadorId,
                            fecha = java.time.LocalDate.now().toString(),
                            validezDias = validity.toIntOrNull() ?: 7,
                            subtotal = subtotal,
                            impuestos = itemsTaxTotal + taxesSubtotal,
                            total = grandTotal,
                            estado = "Enviado",
                            notas = notes,
                            itemsJson = itemsJson,
                            serviciosJson = serviciosJson,
                            appointmentId = appointmentId.ifBlank { null }
                        )
                        
                        viewModel.insertPresupuesto(nuevoPresupuesto)
                        pendingSimPresupuestoId = nuevoPresupuesto.id
                        pendingSimClienteName = clienteNombre.ifBlank { "Cliente" }
                        pendingSimTotal = grandTotal
                        
                        // Mostrar preview y luego volver
                        showPreviewDialog = true 
                    },
                    containerColor = colors.primaryOrange,
                    contentColor = Color.White
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item { PrestadorHeader(prestador, onFilterClick = { sheetType = SheetType.Sections }) }

                // --- SECTIONS ---
                if (showArticlesSection) {
                    item {
                        CollapsibleSection(
                            title = "Artículos",
                            items = items,
                            sectionTotal = itemsSubtotal,
                            isExpanded = isArticlesExpanded,
                            onToggleExpand = { isArticlesExpanded = !isArticlesExpanded },
                            onAddClick = {
                                itemToEdit = null
                                sheetType = SheetType.Article
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                ArticleSummaryRow(
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(onLongPress = { showMenu = true })
                                    },
                                    item = item,
                                )
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                        itemToEdit = item; sheetType = SheetType.Article; showMenu = false
                                    })
                                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = { items.removeAt(index); showMenu = false })
                                }
                            }
                        }
                    }
                }

                if (showServicesSection) {
                    item {
                        CollapsibleSection(
                            title = "Mano de Obra / Servicios",
                            items = services,
                            sectionTotal = servicesSubtotal,
                            isExpanded = isServicesExpanded,
                            onToggleExpand = { isServicesExpanded = !isServicesExpanded },
                            onAddClick = {
                                itemToEdit = null
                                sheetType = SheetType.Service
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                ServiceSummaryRow(
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(onLongPress = { showMenu = true })
                                    },
                                    item = item,
                                )
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                        itemToEdit = item; sheetType = SheetType.Service; showMenu = false
                                    })
                                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = { services.removeAt(index); showMenu = false })
                                }
                            }
                        }
                    }
                }

                if (showProfessionalFeesSection) {
                    item {
                        CollapsibleSection(
                            title = "Honorarios del Profesional",
                            items = professionalFees,
                            sectionTotal = professionalFeesSubtotal,
                            isExpanded = isProfessionalFeesExpanded,
                            onToggleExpand = { isProfessionalFeesExpanded = !isProfessionalFeesExpanded },
                            onAddClick = {
                                itemToEdit = null
                                sheetType = SheetType.ProfessionalFee
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                ProfessionalFeeSummaryRow(
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(onLongPress = { showMenu = true })
                                    },
                                    item = item,
                                )
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                        itemToEdit = item; sheetType = SheetType.ProfessionalFee; showMenu = false
                                    })
                                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = { professionalFees.removeAt(index); showMenu = false })
                                }
                            }
                        }
                    }
                }

                if (showMiscSection) {
                    item {
                        CollapsibleSection(
                            title = "Gastos Varios",
                            items = miscExpenses,
                            sectionTotal = miscSubtotal,
                            isExpanded = isMiscExpanded,
                            onToggleExpand = { isMiscExpanded = !isMiscExpanded },
                            onAddClick = {
                                itemToEdit = null
                                sheetType = SheetType.Misc
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                MiscExpenseSummaryRow(
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(onLongPress = { showMenu = true })
                                    },
                                    item = item,
                                )
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                        itemToEdit = item; sheetType = SheetType.Misc; showMenu = false
                                    })
                                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = { miscExpenses.removeAt(index); showMenu = false })
                                }
                            }
                        }
                    }
                }

                if (showTaxesSection) {
                    item {
                        CollapsibleSection(
                            title = "Impuestos",
                            items = taxes,
                            sectionTotal = taxesSubtotal,
                            isExpanded = isTaxesExpanded,
                            onToggleExpand = { isTaxesExpanded = !isTaxesExpanded },
                            onAddClick = {
                                itemToEdit = null
                                sheetType = SheetType.Tax
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                TaxSummaryRow(
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(onLongPress = { showMenu = true })
                                    },
                                    item = item,
                                )
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                        itemToEdit = item; sheetType = SheetType.Tax; showMenu = false
                                    })
                                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = { taxes.removeAt(index); showMenu = false })
                                }
                            }
                        }
                    }
                }

                if (showAttachmentsSection) {
                    item {
                        CollapsibleSection(
                            title = "Archivos Adjuntos",
                            items = attachments,
                            sectionTotal = 0.0,
                            isExpanded = isAttachmentsExpanded,
                            onToggleExpand = { isAttachmentsExpanded = !isAttachmentsExpanded },
                            onAddClick = {
                                itemToEdit = null
                                sheetType = SheetType.Attachment
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                AttachmentSummaryRow(
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(onLongPress = { showMenu = true })
                                    },
                                    item = item,
                                )
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar Descripción") }, onClick = {
                                        itemToEdit = item; sheetType = SheetType.Attachment; showMenu = false
                                    })
                                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = { attachments.removeAt(index); showMenu = false })
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surfaceColor
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Observaciones", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Escriba aquí sus observaciones...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primaryOrange,
                                    unfocusedBorderColor = colors.border,
                                    focusedLabelColor = colors.primaryOrange,
                                    unfocusedLabelColor = colors.textSecondary,
                                    cursorColor = colors.primaryOrange,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically, 
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Presupuesto válido por", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary
                                )
                                CompactTextField(
                                    value = validity,
                                    onValueChange = { validity = it.filter { char -> char.isDigit() } },
                                    modifier = Modifier.width(60.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    textStyle = TextStyle(textAlign = TextAlign.Center)
                                )
                                Text(
                                    "días.", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(150.dp)) }
            }

            // --- TOTALS SUMMARY (STICKY) ---
            TotalsSummary(
                modifier = Modifier.align(Alignment.BottomCenter),
                isExpanded = isScrolledToEnd,
                grandTotal = grandTotal
            )

            // --- PREVIEW DIALOG ---
            if (showPreviewDialog) {
                BudgetPreviewPDFDialog(
                    prestador = prestador,
                    items = items,
                    services = services,
                    professionalFees = professionalFees,
                    miscExpenses = miscExpenses,
                    taxes = taxes,
                    grandTotal = grandTotal,
                    subtotal = subtotal,
                    taxAmount = itemsTaxTotal + taxesSubtotal,
                    discountAmount = itemsDiscountTotal,
                    onDismiss = { 
                        showPreviewDialog = false
                        // Lanzar simulación de respuesta del cliente
                        if (pendingSimPresupuestoId.isNotBlank()) {
                            viewModel.simulateClientResponse(
                                context = context,
                                presupuestoId = pendingSimPresupuestoId,
                                clienteName = pendingSimClienteName,
                                total = pendingSimTotal
                            )
                        }
                        onBack()
                    }
                )
            }

            // --- BOTTOM SHEETS ---
            if (sheetType != null) {
                ModalBottomSheet(
                    onDismissRequest = { sheetType = null },
                    containerColor = colors.backgroundColor
                ) {
                    when (sheetType) {
                        SheetType.Article -> AddArticleSheetContent(
                            itemToEdit = itemToEdit as? BudgetItem,
                            onAddItem = { newItem -> items.add(newItem); sheetType = null; isArticlesExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = items.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) items[index] = updatedItem
                                sheetType = null
                            }
                        )
                        SheetType.Service -> AddServiceSheetContent(
                            itemToEdit = itemToEdit as? BudgetService,
                            onAddItem = { newItem -> services.add(newItem); sheetType = null; isServicesExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = services.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) services[index] = updatedItem
                                sheetType = null
                            }
                        )
                        SheetType.ProfessionalFee -> AddProfessionalFeeSheetContent(
                            itemToEdit = itemToEdit as? BudgetProfessionalFee,
                            onAddItem = { newItem -> professionalFees.add(newItem); sheetType = null; isProfessionalFeesExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = professionalFees.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) professionalFees[index] = updatedItem
                                sheetType = null
                            }
                        )
                        SheetType.Misc -> AddMiscExpenseSheetContent(
                            itemToEdit = itemToEdit as? BudgetMiscExpense,
                            onAddItem = { newItems -> miscExpenses.addAll(newItems); sheetType = null; isMiscExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = miscExpenses.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) miscExpenses[index] = updatedItem
                                sheetType = null
                            }
                        )
                        SheetType.Tax -> AddTaxSheetContent(
                            itemToEdit = itemToEdit as? BudgetTax,
                            onAddItem = { newItems -> taxes.addAll(newItems); sheetType = null; isTaxesExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = taxes.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) taxes[index] = updatedItem
                                sheetType = null
                            }
                        )
                        SheetType.Attachment -> AddAttachmentSheetContent(
                            itemToEdit = itemToEdit as? BudgetAttachment,
                            onAddItem = { newItem -> attachments.add(newItem); sheetType = null; isAttachmentsExpanded = true },
                            onUpdateItem = { updatedItem ->
                                val index = attachments.indexOfFirst { it.id == updatedItem.id }
                                if (index != -1) attachments[index] = updatedItem
                                sheetType = null
                            }
                        )
                        SheetType.Sections -> SectionsSheetContent(
                            showArticles = showArticlesSection,
                            showServices = showServicesSection,
                            showMisc = showMiscSection,
                            showTaxes = showTaxesSection,
                            showAttachments = showAttachmentsSection,
                            showProfessionalFees = showProfessionalFeesSection,
                            onShowArticlesChange = { showArticlesSection = it },
                            onShowServicesChange = { showServicesSection = it },
                            onShowProfessionalFeesChange = { showProfessionalFeesSection = it },
                            onShowMiscChange = { showMiscSection = it },
                            onShowTaxesChange = { showTaxesSection = it },
                            onShowAttachmentsChange = { showAttachmentsSection = it }
                        )
                        SheetType.Client -> ClientDetailsSheetContent()
                        null -> {}
                    }
                }
            }
        }
    }
}

enum class SheetType { Article, Service, ProfessionalFee, Misc, Tax, Attachment, Sections, Client }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
) {
    val colors = getPrestadorColors()
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle.copy(color = colors.textPrimary),
        keyboardOptions = keyboardOptions,
        singleLine = true,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                label = label,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.primaryOrange,
                    unfocusedLabelColor = colors.textSecondary,
                    cursorColor = colors.primaryOrange,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledBorderColor = colors.border,
                    disabledLabelColor = Color(0xFF9CA3AF), // Gris
                    disabledTextColor = Color(0xFF9CA3AF), // Gris
                    errorBorderColor = Color(0xFFEF4444), // Rojo para errores
                    errorLabelColor = Color(0xFFEF4444),
                    errorCursorColor = Color(0xFFEF4444)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), // Padding compacto
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B35),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            )
        }
    )
}

// --- SHEET CONTENT COMPOSABLES ---

@Composable
fun AddArticleSheetContent(
    itemToEdit: BudgetItem?,
    onAddItem: (BudgetItem) -> Unit,
    onUpdateItem: (BudgetItem) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetItem()) }

    val baseAmount = currentItem.quantity * currentItem.unitPrice
    val taxAmountValue = baseAmount * (currentItem.taxPercentage / 100)
    val baseWithTax = baseAmount + taxAmountValue

    var taxPercentStr by remember { mutableStateOf(if (currentItem.taxPercentage > 0) currentItem.taxPercentage.toString() else "") }
    var taxAmountStr by remember { mutableStateOf(if (currentItem.taxPercentage > 0) "%.2f".format(taxAmountValue) else "") }
    var discountPercentStr by remember { mutableStateOf(if (currentItem.discountPercentage > 0) currentItem.discountPercentage.toString() else "") }
    var discountAmountStr by remember { mutableStateOf(if (currentItem.discountPercentage > 0) "%.2f".format(baseWithTax * currentItem.discountPercentage / 100) else "") }

    LaunchedEffect(baseAmount) {
         if (baseAmount > 0) {
            val taxP = taxPercentStr.toDoubleOrNull() ?: 0.0
            if (taxP > 0) taxAmountStr = "%.2f".format(baseAmount * taxP / 100)

            val discP = discountPercentStr.toDoubleOrNull() ?: 0.0
            val currentTaxAmt = baseAmount * (taxP / 100)
            val currentBaseWithTax = baseAmount + currentTaxAmt
            if (discP > 0) discountAmountStr = "%.2f".format(currentBaseWithTax * discP / 100)
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
        .imePadding()
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        Text(
            if (isEditMode) "Editar Artículo" else "Agregar Nuevo Artículo", 
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BudgetItemRow(item = currentItem, onUpdate = { currentItem = it })

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.border)
        Text(
            "Impuestos y Descuentos", 
            style = MaterialTheme.typography.titleSmall, 
            color = colors.primaryOrange,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(
                value = taxPercentStr,
                onValueChange = {
                    taxPercentStr = it
                    val p = it.toDoubleOrNull()
                    if (p != null && baseAmount > 0) {
                         val tAmount = baseAmount * p / 100
                         taxAmountStr = "%.2f".format(tAmount)
                         currentItem = currentItem.copy(taxPercentage = p)
                         val newBaseWithTax = baseAmount + tAmount
                         val currentDiscP = discountPercentStr.toDoubleOrNull() ?: 0.0
                         if (currentDiscP > 0) discountAmountStr = "%.2f".format(newBaseWithTax * currentDiscP / 100)
                    } else if (it.isEmpty()) {
                        taxAmountStr = ""
                        currentItem = currentItem.copy(taxPercentage = 0.0)
                    }
                },
                label = { Text("Imp. (%)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            CompactTextField(
                value = taxAmountStr,
                onValueChange = {
                    taxAmountStr = it
                    val a = it.toDoubleOrNull()
                    if (a != null && baseAmount > 0) {
                        val p = (a / baseAmount) * 100
                        taxPercentStr = "%.2f".format(p)
                        currentItem = currentItem.copy(taxPercentage = p)
                         val newBaseWithTax = baseAmount + a
                         val currentDiscP = discountPercentStr.toDoubleOrNull() ?: 0.0
                         if (currentDiscP > 0) discountAmountStr = "%.2f".format(newBaseWithTax * currentDiscP / 100)
                    } else if (it.isEmpty()) {
                        taxPercentStr = ""
                        currentItem = currentItem.copy(taxPercentage = 0.0)
                    }
                },
                label = { Text("Imp. ($)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(
                value = discountPercentStr,
                onValueChange = {
                    discountPercentStr = it
                    val p = it.toDoubleOrNull()
                    if (p != null && baseWithTax > 0) {
                         discountAmountStr = "%.2f".format(baseWithTax * p / 100)
                         currentItem = currentItem.copy(discountPercentage = p)
                    } else if (it.isEmpty()) {
                        discountAmountStr = ""
                        currentItem = currentItem.copy(discountPercentage = 0.0)
                    }
                },
                label = { Text("Desc. (%)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )
            CompactTextField(
                value = discountAmountStr,
                onValueChange = {
                    discountAmountStr = it
                    val a = it.toDoubleOrNull()
                    if (a != null && baseWithTax > 0) {
                        val p = (a / baseWithTax) * 100
                        discountPercentStr = "%.2f".format(p)
                        currentItem = currentItem.copy(discountPercentage = p)
                    } else if (it.isEmpty()) {
                        discountPercentStr = ""
                        currentItem = currentItem.copy(discountPercentage = 0.0)
                    }
                },
                label = { Text("Desc. ($)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { if (isEditMode) onUpdateItem(currentItem) else onAddItem(currentItem) },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentItem.description.isNotBlank() && currentItem.unitPrice > 0 && currentItem.quantity > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text(
                if (isEditMode) "Guardar Cambios" else "Agregar Artículo",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddServiceSheetContent(
    itemToEdit: BudgetService?,
    onAddItem: (BudgetService) -> Unit,
    onUpdateItem: (BudgetService) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetService()) }

    Column(modifier = Modifier
        .padding(16.dp)
        .imePadding()
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        Text(
            if (isEditMode) "Editar Servicio" else "Agregar Nuevo Servicio", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        BudgetServiceRow(service = currentItem, onUpdate = { currentItem = it })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (isEditMode) onUpdateItem(currentItem) else onAddItem(currentItem) },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentItem.description.isNotBlank() && currentItem.total > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text(
                if (isEditMode) "Guardar Cambios" else "Agregar Servicio",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddProfessionalFeeSheetContent(
    itemToEdit: BudgetProfessionalFee?,
    onAddItem: (BudgetProfessionalFee) -> Unit,
    onUpdateItem: (BudgetProfessionalFee) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetProfessionalFee()) }

    Column(modifier = Modifier
        .padding(16.dp)
        .imePadding()
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        Text(
            if (isEditMode) "Editar Honorario" else "Agregar Honorario Profesional", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        BudgetProfessionalFeeRow(fee = currentItem, onUpdate = { updatedItem -> currentItem = updatedItem })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (isEditMode) onUpdateItem(currentItem) else onAddItem(currentItem) },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentItem.description.isNotBlank() && currentItem.total > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text(
                if (isEditMode) "Guardar Cambios" else "Agregar Honorario",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class TempMiscExpense(
    val id: String = java.util.UUID.randomUUID().toString(),
    var description: String = "",
    var amount: String = ""
)

@Composable
fun AddMiscExpenseSheetContent(
    itemToEdit: BudgetMiscExpense?,
    onAddItem: (List<BudgetMiscExpense>) -> Unit,
    onUpdateItem: (BudgetMiscExpense) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    val initialList = if (itemToEdit != null) {
        listOf(TempMiscExpense(description = itemToEdit.description, amount = if(itemToEdit.amount > 0) itemToEdit.amount.toString() else ""))
    } else {
        listOf(TempMiscExpense())
    }

    val expenseRows = remember { mutableStateListOf<TempMiscExpense>().apply { addAll(initialList) } }

    val commonRows = remember {
        mutableStateListOf(
            TempMiscExpense(description = "Gastos de envios"),
            TempMiscExpense(description = "Viáticos"),
            TempMiscExpense(description = "Logística"),
            TempMiscExpense(description = "Movilidad")
        )
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .background(colors.backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (isEditMode) "Editar Gasto" else "Agregar Gastos Varios", 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            if (!isEditMode) {
                IconButton(
                    onClick = { expenseRows.add(TempMiscExpense()) }, 
                    modifier = Modifier.background(colors.primaryOrange, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Agregar fila", 
                        tint = Color.White
                    )
                }
            }
        }

        expenseRows.forEachIndexed { index, row ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactTextField(
                    value = row.description,
                    onValueChange = { expenseRows[index] = row.copy(description = it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                CompactTextField(
                    value = row.amount,
                    onValueChange = { expenseRows[index] = row.copy(amount = it) },
                    label = { Text("Importe ($)") },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                )
                if (!isEditMode && expenseRows.size > 1) {
                    IconButton(onClick = { expenseRows.removeAt(index) }) { 
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Eliminar", 
                            tint = Color(0xFFEF4444)
                        ) 
                    }
                }
            }
        }

        if (!isEditMode) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = colors.border
            )
            Text(
                "Gastos Comunes", 
                style = MaterialTheme.typography.titleMedium, 
                modifier = Modifier.padding(bottom = 8.dp), 
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            commonRows.forEachIndexed { index, row ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactTextField(value = row.description, onValueChange = { commonRows[index] = row.copy(description = it) }, label = { Text("Descripción") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                    CompactTextField(
                        value = row.amount,
                        onValueChange = { commonRows[index] = row.copy(amount = it) },
                        label = { Text("Importe ($)") },
                        modifier = Modifier.width(100.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                    )
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        val totalAmount = (expenseRows + commonRows).sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        Text(
            text = "Total Gasto: \$${"%.2f".format(totalAmount)}", 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            color = colors.primaryOrange,
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (itemToEdit != null) {
                    val row = expenseRows.first()
                    onUpdateItem(itemToEdit.copy(description = row.description, amount = row.amount.toDoubleOrNull() ?: 0.0))
                } else {
                    val newExpenses = (expenseRows + commonRows).mapNotNull { row ->
                        val amt = row.amount.toDoubleOrNull() ?: 0.0
                        if (row.description.isNotBlank() && amt > 0) {
                            BudgetMiscExpense(description = row.description, amount = amt)
                        } else null
                    }
                    if (newExpenses.isNotEmpty()) onAddItem(newExpenses)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = totalAmount > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text(
                if (isEditMode) "Guardar Cambios" else "Agregar Gastos",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddTaxSheetContent(
    itemToEdit: BudgetTax?,
    onAddItem: (List<BudgetTax>) -> Unit,
    onUpdateItem: (BudgetTax) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    val initialList = if (itemToEdit != null) {
        listOf(BudgetTax(description = itemToEdit.description, amount = itemToEdit.amount))
    } else {
        listOf(BudgetTax())
    }
    val taxRows = remember { mutableStateListOf<BudgetTax>().apply { addAll(initialList) } }
    val commonTaxes = remember { mutableStateListOf(BudgetTax(description = "IVA 21%"), BudgetTax(description = "IVA 10.5%"), BudgetTax(description = "Retenciones"), BudgetTax(description = "Ingresos Brutos")) }

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
        .imePadding()
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                if (isEditMode) "Editar Impuesto" else "Agregar Impuestos", 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            if (!isEditMode) {
                IconButton(
                    onClick = { taxRows.add(BudgetTax()) }, 
                    modifier = Modifier.background(colors.primaryOrange, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Agregar fila", 
                        tint = Color.White
                    )
                }
            }
        }

        taxRows.forEachIndexed { index, row ->
            var amountStr by remember { mutableStateOf("") }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactTextField(value = row.description, onValueChange = { taxRows[index] = row.copy(description = it) }, label = { Text("Descripción") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                CompactTextField(value = amountStr, onValueChange = { amountStr = it; taxRows[index] = row.copy(amount = it.toDoubleOrNull() ?: 0.0) }, label = { Text("Importe ($)") }, modifier = Modifier.width(100.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
                if (!isEditMode && taxRows.size > 1) {
                    IconButton(onClick = { taxRows.removeAt(index) }) { 
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Eliminar", 
                            tint = Color(0xFFEF4444)
                        ) 
                    }
                }
            }
        }

        if (!isEditMode) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = colors.border
            )
            Text(
                "Impuestos Comunes", 
                style = MaterialTheme.typography.titleMedium, 
                modifier = Modifier.padding(bottom = 8.dp), 
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            commonTaxes.forEachIndexed { index, row ->
                var amountStr by remember { mutableStateOf("") }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactTextField(value = row.description, onValueChange = { commonTaxes[index] = row.copy(description = it) }, label = { Text("Descripción") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                    CompactTextField(value = amountStr, onValueChange = { amountStr = it; commonTaxes[index] = row.copy(amount = it.toDoubleOrNull() ?: 0.0) }, label = { Text("Importe ($)") }, modifier = Modifier.width(100.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (isEditMode) {
                    onUpdateItem(taxRows.first())
                } else {
                    val newTaxes = (taxRows + commonTaxes).filter { it.amount > 0 }
                    if (newTaxes.isNotEmpty()) onAddItem(newTaxes)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text(
                if (isEditMode) "Guardar Cambios" else "Agregar Impuestos",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddAttachmentSheetContent(
    itemToEdit: BudgetAttachment?,
    onAddItem: (BudgetAttachment) -> Unit,
    onUpdateItem: (BudgetAttachment) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetAttachment()) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            currentItem = currentItem.copy(uri = uri, type = if (uri.toString().endsWith("pdf")) AttachmentType.PDF else AttachmentType.IMAGE)
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .imePadding()
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        Text(
            "Adjuntar Archivo", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFFFFE5D9), RoundedCornerShape(12.dp))
                .clickable { launcher.launch("*/*") },
            contentAlignment = Alignment.Center
        ) {
            if (currentItem.uri != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (currentItem.type == AttachmentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image, 
                        contentDescription = null, 
                        modifier = Modifier.size(48.dp), 
                        tint = colors.primaryOrange
                    )
                    Text(
                        "Archivo seleccionado", 
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AttachFile, 
                        contentDescription = null, 
                        modifier = Modifier.size(48.dp), 
                        tint = colors.primaryOrange
                    )
                    Text(
                        "Toque para seleccionar (Imagen/PDF)", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = colors.textSecondary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CompactTextField(value = currentItem.description, onValueChange = { currentItem = currentItem.copy(description = it) }, label = { Text("Descripción / Detalle") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (isEditMode) onUpdateItem(currentItem) else onAddItem(currentItem) }, 
            modifier = Modifier.fillMaxWidth(), 
            enabled = currentItem.uri != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text("Adjuntar", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionsSheetContent(
    showArticles: Boolean,
    showServices: Boolean,
    showProfessionalFees: Boolean,
    showMisc: Boolean,
    showTaxes: Boolean,
    showAttachments: Boolean,
    onShowArticlesChange: (Boolean) -> Unit,
    onShowServicesChange: (Boolean) -> Unit,
    onShowProfessionalFeesChange: (Boolean) -> Unit,
    onShowMiscChange: (Boolean) -> Unit,
    onShowTaxesChange: (Boolean) -> Unit,
    onShowAttachmentsChange: (Boolean) -> Unit
) {
    val colors = getPrestadorColors()
    Column(modifier = Modifier
        .padding(16.dp)
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        Text(
            "Mostrar/Ocultar Secciones", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        SectionSwitch(title = "Artículos", checked = showArticles, onCheckedChange = onShowArticlesChange)
        SectionSwitch(title = "Mano de Obra / Servicios", checked = showServices, onCheckedChange = onShowServicesChange)
        SectionSwitch(title = "Honorarios del Profesional", checked = showProfessionalFees, onCheckedChange = onShowProfessionalFeesChange)
        SectionSwitch(title = "Gastos Varios", checked = showMisc, onCheckedChange = onShowMiscChange)
        SectionSwitch(title = "Impuestos", checked = showTaxes, onCheckedChange = onShowTaxesChange)
        SectionSwitch(title = "Archivos Adjuntos", checked = showAttachments, onCheckedChange = onShowAttachmentsChange)
    }
}

@Composable
fun SectionSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = getPrestadorColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, 
            modifier = Modifier.weight(1f),
            color = colors.textPrimary
        )
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primaryOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.border
            )
        )
    }
}

@Composable
fun ClientDetailsSheetContent() {
    val colors = getPrestadorColors()
    Column(modifier = Modifier
        .padding(16.dp)
        .navigationBarsPadding()
        .background(colors.backgroundColor)) {
        Text(
            "Datos del Cliente", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            "Funcionalidad en desarrollo", 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

@Composable
fun BudgetItemRow(item: BudgetItem, onUpdate: (BudgetItem) -> Unit) {
    val colors = getPrestadorColors()
    val base = item.unitPrice * item.quantity
    val taxAmount = base * (item.taxPercentage / 100)
    val withTax = base + taxAmount
    val discountAmount = withTax * (item.discountPercentage / 100)
    val total = withTax - discountAmount

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactTextField(value = item.code, onValueChange = { onUpdate(item.copy(code = it)) }, label = { Text("Cód.") }, modifier = Modifier.weight(0.25f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            CompactTextField(value = item.description, onValueChange = { onUpdate(item.copy(description = it)) }, label = { Text("Descripción") }, modifier = Modifier.weight(0.75f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(value = if (item.quantity == 0) "" else item.quantity.toString(), onValueChange = { onUpdate(item.copy(quantity = it.toIntOrNull() ?: 0)) }, label = { Text("Cant.") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
            CompactTextField(value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(), onValueChange = { onUpdate(item.copy(unitPrice = it.toDoubleOrNull() ?: 0.0)) }, label = { Text("P. Unit.") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
            Text(
                "Total: \$${"%.2f".format(total)}", 
                modifier = Modifier.weight(1f), 
                textAlign = TextAlign.Center, 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primaryOrange
            )
        }
    }
}

@Composable
fun BudgetServiceRow(service: BudgetService, onUpdate: (BudgetService) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
         Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactTextField(value = service.code, onValueChange = { onUpdate(service.copy(code = it)) }, label = { Text("Cód.") }, modifier = Modifier.weight(0.25f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            CompactTextField(value = service.description, onValueChange = { onUpdate(service.copy(description = it)) }, label = { Text("Descripción") }, modifier = Modifier.weight(0.75f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(value = if (service.total == 0.0) "" else service.total.toString(), onValueChange = { onUpdate(service.copy(total = it.toDoubleOrNull() ?: 0.0)) }, label = { Text("Total") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
        }
    }
}

@Composable
fun BudgetProfessionalFeeRow(fee: BudgetProfessionalFee, onUpdate: (BudgetProfessionalFee) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
         Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactTextField(value = fee.code, onValueChange = { onUpdate(fee.copy(code = it)) }, label = { Text("Cód.") }, modifier = Modifier.weight(0.25f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            CompactTextField(value = fee.description, onValueChange = { onUpdate(fee.copy(description = it)) }, label = { Text("Descripción") }, modifier = Modifier.weight(0.75f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactTextField(value = if (fee.total == 0.0) "" else fee.total.toString(), onValueChange = { onUpdate(fee.copy(total = it.toDoubleOrNull() ?: 0.0)) }, label = { Text("Total") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
        }
    }
}

@Composable
fun ArticleSummaryRow(modifier: Modifier = Modifier, item: BudgetItem) {
    val colors = getPrestadorColors()
    val base = item.unitPrice * item.quantity
    val taxAmount = base * (item.taxPercentage / 100)
    val withTax = base + taxAmount
    val discountAmount = withTax * (item.discountPercentage / 100)
    val total = withTax - discountAmount

    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.description.ifBlank { "(Sin descripción)" }, 
                fontWeight = FontWeight.SemiBold, 
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary
            )
            Text(
                "${item.quantity} x \$${"%.2f".format(item.unitPrice)}", 
                style = MaterialTheme.typography.bodySmall, 
                color = colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "\$${"%.2f".format(total)}", 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primaryOrange
        )
    }
}

@Composable
fun ServiceSummaryRow(modifier: Modifier = Modifier, item: BudgetService) {
    val colors = getPrestadorColors()
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            item.description.ifBlank { "(Sin descripción)" }, 
            modifier = Modifier.weight(1f), 
            fontWeight = FontWeight.SemiBold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "\$${"%.2f".format(item.total)}", 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primaryOrange
        )
    }
}

@Composable
fun ProfessionalFeeSummaryRow(modifier: Modifier = Modifier, item: BudgetProfessionalFee) {
    val colors = getPrestadorColors()
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            item.description.ifBlank { "(Sin descripción)" }, 
            modifier = Modifier.weight(1f), 
            fontWeight = FontWeight.SemiBold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "\$${"%.2f".format(item.total)}", 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primaryOrange
        )
    }
}

@Composable
fun TaxSummaryRow(modifier: Modifier = Modifier, item: BudgetTax) {
    val colors = getPrestadorColors()
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            item.description.ifBlank { "(Sin descripción)" }, 
            modifier = Modifier.weight(1f), 
            fontWeight = FontWeight.SemiBold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "\$${"%.2f".format(item.amount)}", 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primaryOrange
        )
    }
}

@Composable
fun AttachmentSummaryRow(modifier: Modifier = Modifier, item: BudgetAttachment) {
    val colors = getPrestadorColors()
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (item.type == AttachmentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image, 
            contentDescription = null, 
            tint = colors.primaryOrange,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            item.description.ifBlank { "(Sin descripción)" }, 
            modifier = Modifier.weight(1f), 
            fontWeight = FontWeight.SemiBold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Icon(
            Icons.Default.CheckCircle, 
            contentDescription = "Adjunto", 
            tint = Color(0xFF10B981),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun MiscExpenseSummaryRow(modifier: Modifier = Modifier, item: BudgetMiscExpense) {
    val colors = getPrestadorColors()
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            item.description.ifBlank { "(Sin descripción)" }, 
            modifier = Modifier.weight(1f), 
            fontWeight = FontWeight.SemiBold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "\$${"%.2f".format(item.amount)}", 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primaryOrange
        )
    }
}

@Composable
fun TotalsSummary(modifier: Modifier = Modifier, isExpanded: Boolean, grandTotal: Double) {
    val colors = getPrestadorColors()
    val priceTextStyle = if (isExpanded) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge
    val verticalPadding = if (isExpanded) 12.dp else 4.dp
    Surface(
        modifier = modifier.fillMaxWidth(), 
        shadowElevation = 8.dp,
        color = colors.surfaceColor
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = 16.dp, vertical = verticalPadding),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "TOTAL GENERAL", 
                style = MaterialTheme.typography.labelLarge, 
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "\$${"%.2f".format(grandTotal)}", 
                style = priceTextStyle, 
                fontWeight = FontWeight.ExtraBold,
                color = colors.primaryOrange
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)

@Composable
fun BudgetMetaBox(title: String, value: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color.Black, RoundedCornerShape(4.dp))) {
        Text(text = title, modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1976D2))
            .padding(2.dp), color = Color.White, fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Text(text = value, modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(4.dp), fontSize = 10.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SectionHeader(text: String) {
    Surface(color = Color(0xFF1976D2), modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun <T> CollapsibleSection(
    title: String,
    items: List<T>,
    sectionTotal: Double,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddClick: () -> Unit,
    itemContent: @Composable (item: T, index: Int) -> Unit
) {
    val colors = getPrestadorColors()
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Box(modifier = Modifier.padding(top = 18.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleExpand)
                ) {
                    Badge(
                        containerColor = colors.primaryOrange,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "${items.size}",
                            modifier = Modifier.padding(horizontal = 6.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        modifier = Modifier.rotate(rotationAngle),
                        tint = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = colors.border
                        )
                        if (items.isEmpty()) {
                            Text(
                                "No hay ítems agregados.",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = colors.textSecondary
                            )
                        } else {
                            Column {
                                items.forEachIndexed { index, item ->
                                    itemContent(item, index)
                                    if (index < items.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = colors.border
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        SmallFloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-18).dp),
            shape = CircleShape,
            containerColor = colors.primaryOrange,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir ${title.substringBefore(' ')}")
        }

        if (items.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 12.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF3E0),
                border = BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "Subtotal: \$${"%.2f".format(sectionTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryOrange,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PrestadorHeader(prestador: PPrestadorProfileFalso, onFilterClick: () -> Unit) {
    val colors = getPrestadorColors()
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = prestador.profileImageUrl, 
                    contentDescription = "Foto de perfil", 
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape), 
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        prestador.companyName ?: "${prestador.name} ${prestador.lastName}", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        prestador.address, 
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                    Text(
                        "Email: ${prestador.email}", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
                IconButton(onClick = onFilterClick) { 
                    Icon(
                        Icons.Filled.FilterList, 
                        contentDescription = "Filtrar Secciones",
                        tint = colors.primaryOrange
                    ) 
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = colors.border)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Presupuesto Nº: 0001", 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    "Fecha: $currentDate", 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        }
    }
}

fun Modifier.drawDottedLineBottom() = this.drawBehind {
    val strokeWidth = 1.dp.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(color = Color.Gray, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
}


private val Slate50 = Color(0xFFF8FAFC)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate600 = Color(0xFF475569)
private val Slate700 = Color(0xFF334155)
private val Slate800 = Color(0xFF1E293B)
private val MaverickBlueEnd = Color(0xFF2563EB)
private val MaverickBlueStart = Color(0xFF1E40AF)
private val MaverickGradient = Brush.linearGradient(colors = listOf(MaverickBlueStart, MaverickBlueEnd))
data class PresupuestoItemDisplay(
    val cantidad: String,
    val descripcion: String,
    val unitario: String,
    val total: String,
    val isSpecial: Boolean = false
)

//Dimensiones A4
val A4_WIDTH = 450.dp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetPreviewPDFDialog(
    prestador: PPrestadorProfileFalso,
    items: List<BudgetItem>,
    services: List<BudgetService>,
    professionalFees: List<BudgetProfessionalFee>,
    miscExpenses: List<BudgetMiscExpense>,
    taxes: List<BudgetTax>,
    grandTotal: Double,
    subtotal: Double,
    taxAmount: Double,
    discountAmount: Double,
    onDismiss: () -> Unit
) {
    //CONVERTIR ITEMS A FORMATO DE DISPLAY

    val displayItems = mutableListOf<PresupuestoItemDisplay>()

    //Agregar articulos
    items.forEach { item ->
        val base = item.unitPrice * item.quantity
        val taxAmount = base * (item.taxPercentage / 100)
        val withTax = base + taxAmount
        val discountAmount = withTax * (item.discountPercentage / 100)
        val total = withTax - discountAmount

        displayItems.add(
            PresupuestoItemDisplay(
                cantidad = item.quantity.toString(),
                descripcion = item.description,
                unitario = "$ ${String.format("%,.2f", item.unitPrice)}",
                total = "$ ${String.format("%,.2f", total)}"
            )
        )
    }

    // Agregar servicios
    services.forEach { service ->
        displayItems.add(
            PresupuestoItemDisplay(
                cantidad = "-",
                descripcion = service.description,
                unitario = "-",
                total = "$ ${String.format("%,.2f", service.total)}",
                isSpecial = true
            )
        )
    }
    
    // Agregar honorarios profesionales
    professionalFees.forEach { fee ->
        displayItems.add(
            PresupuestoItemDisplay(
                cantidad = "-",
                descripcion = fee.description,
                unitario = "-",
                total = "$ ${String.format("%,.2f", fee.total)}",
                isSpecial = true
            )
        )
    }

    // Agregar gastos varios
    miscExpenses.forEach { expense ->
        displayItems.add(
            PresupuestoItemDisplay(
                cantidad = "-",
                descripcion = expense.description,
                unitario = "-",
                total = "$ ${String.format("%,.2f", expense.amount)}"
            )
        )
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenWidth = maxWidth
            val initialFitScale = remember (screenWidth) {
                ((screenWidth - 32.dp) / A4_WIDTH).coerceAtMost(1f)
            }
            // Estados de zoom y paneo
            var scale by remember { mutableFloatStateOf(initialFitScale) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            //Contenedor principal (visor)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF202020))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(initialFitScale, 4f)
                            offset += pan
                        }
                    }
            ) {
                // La hoja de papel A4
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .width(A4_WIDTH)
                        .wrapContentHeight()
                        .shadow(elevation = 12.dp)
                        .background(Color.White)

                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate300)
                    ) {
                        //Franja decorativa
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(MaverickGradient)
                        )

                        // Encabezado
                        A4HeaderSection(prestador)
                        HorizontalDivider(color = Slate200)

                        // Datos Cliente
                        A4ClientInfoSection(prestador)

                        // Tabla de items
                        A4ItemsTable(displayItems)
                        //Footer
                        A4FooterSection(
                            subtotal = subtotal,
                            taxAmount = taxAmount,
                            discountAmount = discountAmount,
                            total = grandTotal
                        )
                    }
                }

                //Boton cerrar
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .size(48.dp)
                        .zIndex(10f)
                ) {
                    Icon(Icons.Default.Close, "Cerrar", tint = Slate800)
                }

                //Controles de zoom
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Slate800.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .zIndex(10f), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scale = (scale * 0.8f).coerceAtLeast(initialFitScale)
                            offset = Offset.Zero
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, "Alejar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = "${(scale / initialFitScale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 50.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = {
                            scale = (scale * 1.25f).coerceAtMost(4f)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, "Acercar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            scale = initialFitScale
                            offset = Offset.Zero
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, "Resetear", tint = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun A4HeaderSection(prestador: PPrestadorProfileFalso) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Logo y dirección
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaverickGradient)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "${prestador.name} ${prestador.lastName}".uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate800,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 16.sp
                )
                Text(
                    prestador.services.firstOrNull() ?: "SERVICIOS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.5.sp,
                    lineHeight = 11.sp
                )
            }
        }

        // La "X" con PRESUPUESTO debajo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(2.dp, Slate800, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("X", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Slate800)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("PRESUPUESTO", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Slate600, letterSpacing = 0.5.sp)
        }

        // Datos
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .background(Slate50)
                    .border(1.dp, Slate300, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("N° ${prestador.id.takeLast(8)}", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Slate800)
            }
            Text(currentDate, fontSize = 10.sp, color = Slate600, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun A4ClientInfoSection(prestador: PPrestadorProfileFalso) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        // Emisor
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("DE:", fontSize = 10.sp,
                fontWeight = FontWeight.Bold, color = Slate400)
            Text(
                prestador.companyName ?: "${prestador.name} ${prestador.lastName}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = Slate600
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        //Receptor

        Column {
            Text("PARA:", fontSize = 10.sp,
                fontWeight = FontWeight.Bold, color = Slate400)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp).width(20.dp),
                color = Slate300
            )

            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text("CLIENTE / EMPRESA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cliente", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800, lineHeight = 13.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(1.dp)
                            .height(13.dp)
                            .background(Slate400)
                    )
                    Text(prestador.companyName ?: "", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800, lineHeight = 13.sp)
                }
                HorizontalDivider(color = Slate400, thickness = 1.dp)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DIRECCIÓN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                    Text("A definir", fontSize = 11.sp, color = Slate800, lineHeight = 14.sp)
                    HorizontalDivider(color = Slate300, thickness = 1.dp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("MÉTODO DE PAGO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                    Text("A definir", fontSize = 11.sp, color = Slate800, lineHeight = 14.sp)
                    HorizontalDivider(color = Slate300, thickness = 1.dp)
                }
            }
            Column {
                Text("TRABAJO / PROYECTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Text("Proyecto de servicio", fontSize = 11.sp, color = Slate800, lineHeight = 14.sp)
                HorizontalDivider(color = Slate300, thickness = 1.dp)
            }
        }
    }
}

@Composable
fun A4ItemsTable(items: List<PresupuestoItemDisplay>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.border(1.dp, Slate300)) {
            // Header
            Row(modifier = Modifier.background(Slate100).height(IntrinsicSize.Min)) {
                A4TableCell("Cant", 0.15f, true)
                A4TableCell("Descripción", 0.55f, true)
                A4TableCell("Unitario", 0.3f, true, alignRight = true)
                A4TableCell("Total", 0.3f, true, alignRight = true, isLast = true)
            }
            HorizontalDivider(color = Slate300)

            // Items
            items.forEach { item ->
                val bg = if (item.isSpecial) Color(0xFFEFF6FF) else Color.White
                val color = if (item.isSpecial) MaverickBlueEnd else Slate800
                val weight = if (item.isSpecial) FontWeight.Bold else FontWeight.Normal

                Row(modifier = Modifier.background(bg).height(IntrinsicSize.Min)) {
                    A4TableCell(if(item.isSpecial) "-" else item.cantidad, 0.15f, color = Slate600)
                    A4TableCell(item.descripcion, 0.55f, color = color, fontWeight = weight)
                    A4TableCell(item.unitario, 0.3f, alignRight = true, color = Slate600)
                    A4TableCell(item.total, 0.3f, alignRight = true, fontWeight = FontWeight.Bold, color = color, isLast = true)
                }
                HorizontalDivider(color = Slate300)
            }
        }
    }
}

@Composable
fun RowScope.A4TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    alignRight: Boolean = false,
    isLast: Boolean = false,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    val finalColor = if (color == Color.Unspecified) (if (isHeader) Slate600 else Slate800) else color
    val finalWeight = fontWeight ?: (if (isHeader) FontWeight.Bold else FontWeight.Normal)

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .then(if (!isLast) Modifier.border(width = 0.5.dp, color = Slate300.copy(alpha = 0.5f)) else Modifier)
            .padding(6.dp),
        contentAlignment = if (alignRight) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 9.sp else 10.sp,
            fontWeight = finalWeight,
            color = finalColor,
            textAlign = if (alignRight) TextAlign.End else TextAlign.Start,
            lineHeight = if (isHeader) 11.sp else 12.sp
        )
    }
}

@Composable
fun A4FooterSection(
    subtotal: Double,
    taxAmount: Double,
    discountAmount: Double,
    total: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate50)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Nota Legal
            Text(
                text = "Nota: Los precios están expresados en Pesos Argentinos.\nVálido por 15 días.",
                fontSize = 10.sp,
                color = Slate400,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 14.sp,
                modifier = Modifier.width(180.dp)
            )

            // Cuadro de Totales
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .shadow(2.dp, RoundedCornerShape(4.dp))
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, Slate300, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", fontSize = 11.sp, color = Slate600)
                    Text("$ ${String.format("%,.2f", subtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                }

                if (taxAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Impuestos:", fontSize = 11.sp, color = Slate600)
                        Text("$ ${String.format("%,.2f", taxAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                    }
                }

                if (discountAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Descuento:", fontSize = 11.sp, color = Slate600)
                        Text("- $ ${String.format("%,.2f", discountAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Slate200)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Slate800)
                    Text("$ ${String.format("%,.2f", total)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaverickBlueEnd)
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun CrearPresupuestoPrestadorScreenPreview() {
    MaterialTheme {
        CrearPresupuestoPrestadorScreen()
    }
}



