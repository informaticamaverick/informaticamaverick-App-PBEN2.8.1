package com.example.myapplication.prestador.ui.presupuesto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.PresupuestoViewModel
import androidx.compose.runtime.collectAsState

enum class SheetType { Article, Service, ProfessionalFee, Misc, Tax, Attachment, Sections, Client }

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
    var showSaveCatalogDialog by remember { mutableStateOf(false) }
    var pendingPresupuesto by remember { mutableStateOf<com.example.myapplication.prestador.data.local.entity.PresupuestoEntity?>(null) }
    val presupuestos by viewModel.presupuestos.collectAsState()
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
                        
                        // Convertir items a JSON
                        val itemsJson = items.joinToString("|") { item ->
                            "${item.code};${item.description};${item.quantity};${item.unitPrice};${item.taxPercentage};${item.discountPercentage}"
                        }
                        val serviciosJson = services.joinToString("|") { service ->
                            "${service.code};${service.description};${service.total}"
                        }
                        val honorariosJson = professionalFees.joinToString("|") { fee ->
                            "${fee.code};${fee.description};${fee.total}"
                        }
                        val gastosJson = miscExpenses.joinToString("|") { misc ->
                            "${misc.description};${misc.amount}"
                        }
                        val impuestosJsonStr = taxes.joinToString("|") { tax ->
                            "${tax.description};${tax.amount}"
                        }

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
                            honorariosJson = honorariosJson,
                            gastosJson = gastosJson,
                            impuestosJson = impuestosJsonStr,
                            appointmentId = appointmentId.ifBlank { null }
                        )

                        pendingSimPresupuestoId = nuevoPresupuesto.id
                        pendingSimClienteName = clienteNombre.ifBlank { "Cliente" }
                        pendingSimTotal = grandTotal
                        pendingPresupuesto = nuevoPresupuesto
                        val hasNewItems = items.isNotEmpty() || services.isNotEmpty() ||
                            professionalFees.isNotEmpty() || miscExpenses.isNotEmpty() || taxes.isNotEmpty()
                        if (hasNewItems) showSaveCatalogDialog = true else showPreviewDialog = true
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
                            },
                            quickAddSlot = {
                                val suggItems = remember(presupuestos) {
                                    presupuestos.flatMap { p ->
                                        if (p.itemsJson.isBlank()) emptyList()
                                        else p.itemsJson.split("|").mapNotNull { s ->
                                            val parts = s.split(";")
                                            if (parts.size >= 4) BudgetItem(
                                                id = 0L, code = parts[0], description = parts[1],
                                                quantity = parts[2].toIntOrNull() ?: 1,
                                                unitPrice = parts[3].toDoubleOrNull() ?: 0.0,
                                                taxPercentage = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                                                discountPercentage = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                                            ) else null
                                        }
                                    }.distinctBy { it.description }
                                }
                                ArticleAutoCompleteFields(
                                    suggestions = suggItems,
                                    onAdd = { items.add(it.copy(id = System.currentTimeMillis())); isArticlesExpanded = true }
                                )
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
                            },
                            quickAddSlot = {
                                val suggServices = remember(presupuestos) {
                                    presupuestos.flatMap { p ->
                                        if (p.serviciosJson.isBlank()) emptyList()
                                        else p.serviciosJson.split("|").mapNotNull { s ->
                                            val parts = s.split(";")
                                            if (parts.size >= 2) BudgetService(id = 0L, code = parts[0], description = parts[1], total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
                                        }
                                    }.distinctBy { it.description }
                                }
                                ServiceAutoCompleteFields(
                                    suggestions = suggServices,
                                    onAdd = { services.add(it.copy(id = System.currentTimeMillis())); isServicesExpanded = true }
                                )
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
                            onAddClick = { itemToEdit = null; sheetType = SheetType.ProfessionalFee },
                            quickAddSlot = {
                                val suggFees = remember(presupuestos) {
                                    presupuestos.flatMap { p ->
                                        if (p.honorariosJson.isBlank()) emptyList()
                                        else p.honorariosJson.split("|").mapNotNull { s ->
                                            val parts = s.split(";")
                                            if (parts.size >= 2) BudgetProfessionalFee(id = 0L, code = parts[0], description = parts[1], total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
                                        }
                                    }.distinctBy { it.description }
                                }
                                FeeAutoCompleteFields(suggestions = suggFees, onAdd = { professionalFees.add(it.copy(id = System.currentTimeMillis())); isProfessionalFeesExpanded = true })
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                ProfessionalFeeSummaryRow(modifier = Modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { showMenu = true }) }, item = item)
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar") }, onClick = { itemToEdit = item; sheetType = SheetType.ProfessionalFee; showMenu = false })
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
                            onAddClick = { itemToEdit = null; sheetType = SheetType.Misc },
                            quickAddSlot = {
                                val suggMisc = remember(presupuestos) {
                                    presupuestos.flatMap { p ->
                                        if (p.gastosJson.isBlank()) emptyList()
                                        else p.gastosJson.split("|").mapNotNull { s ->
                                            val parts = s.split(";")
                                            if (parts.size >= 1 && parts[0].isNotBlank()) parts[0] else null
                                        }
                                    }.distinct()
                                }
                                DescriptionAutoCompleteField(label = "Buscar gasto...", suggestions = suggMisc, onSelect = { desc ->
                                    val prev = presupuestos.flatMap { p -> if (p.gastosJson.isBlank()) emptyList() else p.gastosJson.split("|").mapNotNull { s -> val parts = s.split(";"); if (parts[0] == desc) parts.getOrNull(1)?.toDoubleOrNull() else null } }.firstOrNull() ?: 0.0
                                    miscExpenses.add(BudgetMiscExpense(description = desc, amount = prev)); isMiscExpanded = true
                                })
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                MiscExpenseSummaryRow(modifier = Modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { showMenu = true }) }, item = item)
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar") }, onClick = { itemToEdit = item; sheetType = SheetType.Misc; showMenu = false })
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
                            onAddClick = { itemToEdit = null; sheetType = SheetType.Tax },
                            quickAddSlot = {
                                val suggTaxes = remember(presupuestos) {
                                    presupuestos.flatMap { p ->
                                        if (p.impuestosJson.isBlank()) emptyList()
                                        else p.impuestosJson.split("|").mapNotNull { s ->
                                            val parts = s.split(";")
                                            if (parts.size >= 1 && parts[0].isNotBlank()) parts[0] else null
                                        }
                                    }.distinct()
                                }
                                DescriptionAutoCompleteField(label = "Buscar impuesto...", suggestions = suggTaxes, onSelect = { desc ->
                                    val prev = presupuestos.flatMap { p -> if (p.impuestosJson.isBlank()) emptyList() else p.impuestosJson.split("|").mapNotNull { s -> val parts = s.split(";"); if (parts[0] == desc) parts.getOrNull(1)?.toDoubleOrNull() else null } }.firstOrNull() ?: 0.0
                                    taxes.add(BudgetTax(description = desc, amount = prev)); isTaxesExpanded = true
                                })
                            }
                        ) { item, index ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                TaxSummaryRow(modifier = Modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { showMenu = true }) }, item = item)
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Editar") }, onClick = { itemToEdit = item; sheetType = SheetType.Tax; showMenu = false })
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

            // --- SAVE CATALOG DIALOG ---
            if (showSaveCatalogDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showSaveCatalogDialog = false; showPreviewDialog = true },
                    title = { Text("¿Guardar en catálogo?", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                    text = { Text("¿Querés guardar estos ítems para autocompletar futuros presupuestos?") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            pendingPresupuesto?.let { viewModel.insertPresupuesto(it) }
                            showSaveCatalogDialog = false
                            showPreviewDialog = true
                        }) { Text("Sí, guardar", color = colors.primaryOrange) }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            showSaveCatalogDialog = false
                            showPreviewDialog = true
                        }) { Text("No") }
                    }
                )
            }

            // --- PREVIEW DIALOG ---
            if (showPreviewDialog) {
                val capturedSimId = pendingSimPresupuestoId
                val capturedSimName = pendingSimClienteName
                val capturedSimTotal = pendingSimTotal
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
                    },
                    onEnviar = {
                        showPreviewDialog = false
                        if (capturedSimId.isNotBlank()) {
                            viewModel.simulateClientResponse(
                                context = context,
                                presupuestoId = capturedSimId,
                                clienteName = capturedSimName,
                                total = capturedSimTotal
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun CrearPresupuestoPrestadorScreenPreview() {
    MaterialTheme {
        CrearPresupuestoPrestadorScreen()
    }
}
