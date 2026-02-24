package com.example.myapplication.prestador.ui.presupuesto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager
import com.example.myapplication.prestador.viewmodel.PresupuestoViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetChatSheet(
    userId: String,
    userName: String,
    onDismiss: () -> Unit,
    viewModel: PresupuestoViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- ITEMS STATE ---
    val items = remember { mutableStateListOf<BudgetItem>() }
    val services = remember { mutableStateListOf<BudgetService>() }
    val professionalFees = remember { mutableStateListOf<BudgetProfessionalFee>() }
    val miscExpenses = remember { mutableStateListOf<BudgetMiscExpense>() }
    val taxes = remember { mutableStateListOf<BudgetTax>() }

    // --- SECTION EXPANSION ---
    var isArticlesExpanded by remember { mutableStateOf(true) }
    var isServicesExpanded by remember { mutableStateOf(true) }
    var isProfessionalFeesExpanded by remember { mutableStateOf(true) }
    var isMiscExpanded by remember { mutableStateOf(true) }
    var isTaxesExpanded by remember { mutableStateOf(true) }

    // --- DIALOG STATES ---
    var sheetType by remember { mutableStateOf<SheetType?>(null) }
    var itemToEdit by remember { mutableStateOf<Any?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showSaveCatalogDialog by remember { mutableStateOf(false) }
    var pendingPresupuesto by remember { mutableStateOf<PresupuestoEntity?>(null) }
    val presupuestos by viewModel.presupuestos.collectAsState()

    // --- NOTES / VALIDITY ---
    var notes by remember { mutableStateOf("") }
    var validity by remember { mutableStateOf("7") }

    // --- CALCULATED TOTALS ---
    val itemsBaseSubtotal = items.sumOf { it.unitPrice * it.quantity }
    val itemsTaxTotal = items.sumOf {
        val base = it.unitPrice * it.quantity
        base * (it.taxPercentage / 100)
    }
    val itemsDiscountTotal = items.sumOf {
        val base = it.unitPrice * it.quantity
        val taxAmt = base * (it.taxPercentage / 100)
        (base + taxAmt) * (it.discountPercentage / 100)
    }
    val itemsSubtotal = itemsBaseSubtotal + itemsTaxTotal - itemsDiscountTotal
    val servicesSubtotal = services.sumOf { it.total }
    val feesSubtotal = professionalFees.sumOf { it.total }
    val miscSubtotal = miscExpenses.sumOf { it.amount }
    val taxesSubtotal = taxes.sumOf { it.amount }
    val subtotal = itemsSubtotal + servicesSubtotal + feesSubtotal + miscSubtotal
    val grandTotal = subtotal + taxesSubtotal

    val lazyListState = rememberLazyListState()

    val hasItems = items.isNotEmpty() || services.isNotEmpty() ||
            professionalFees.isNotEmpty() || miscExpenses.isNotEmpty()

    // Suggestion items from saved templates
    val suggestionItems = remember(presupuestos) {
        presupuestos.flatMap { p ->
            if (p.itemsJson.isBlank()) emptyList()
            else p.itemsJson.split("|").mapNotNull { s ->
                val parts = s.split(";")
                if (parts.size >= 4) BudgetItem(
                    id = 0L,
                    code = parts[0],
                    description = parts[1],
                    quantity = parts[2].toIntOrNull() ?: 1,
                    unitPrice = parts[3].toDoubleOrNull() ?: 0.0,
                    taxPercentage = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                    discountPercentage = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                ) else null
            }
        }.distinctBy { it.description }
    }

    // Descripciones ya guardadas en el catálogo (para detectar items nuevos)
    val knownItemDescriptions = remember(presupuestos) {
        presupuestos.flatMap { p ->
            if (p.itemsJson.isBlank()) emptyList()
            else p.itemsJson.split("|").mapNotNull { s -> s.split(";").getOrNull(1) }
        }.toSet()
    }
    val knownServiceDescriptions = remember(presupuestos) {
        presupuestos.flatMap { p ->
            if (p.serviciosJson.isBlank()) emptyList()
            else p.serviciosJson.split("|").mapNotNull { s -> s.split(";").getOrNull(1) }
        }.toSet()
    }
    val knownFeeDescriptions = remember(presupuestos) {
        presupuestos.flatMap { p ->
            if (p.honorariosJson.isBlank()) emptyList()
            else p.honorariosJson.split("|").mapNotNull { s -> s.split(";").getOrNull(1) }
        }.toSet()
    }
    val knownMiscDescriptions = remember(presupuestos) {
        presupuestos.flatMap { p ->
            if (p.gastosJson.isBlank()) emptyList()
            else p.gastosJson.split("|").mapNotNull { s -> s.split(";").getOrNull(0) }
        }.toSet()
    }
    val knownTaxDescriptions = remember(presupuestos) {
        presupuestos.flatMap { p ->
            if (p.impuestosJson.isBlank()) emptyList()
            else p.impuestosJson.split("|").mapNotNull { s -> s.split(";").getOrNull(0) }
        }.toSet()
    }

    fun hasNewItems(): Boolean {
        return items.any { it.description !in knownItemDescriptions } ||
               services.any { it.description !in knownServiceDescriptions } ||
               professionalFees.any { it.description !in knownFeeDescriptions } ||
               miscExpenses.any { it.description !in knownMiscDescriptions } ||
               taxes.any { it.description !in knownTaxDescriptions }
    }

    fun buildPresupuesto(): PresupuestoEntity {
        val clienteId = "cliente_chat_${System.currentTimeMillis()}"
        viewModel.insertCliente(ClienteEntity(
            id = clienteId, nombre = userName,
            email = "", telefono = "", direccion = ""
        ))
        val itemsJson = items.joinToString("|") {
            "${it.code};${it.description};${it.quantity};${it.unitPrice};${it.taxPercentage};${it.discountPercentage}"
        }
        val serviciosJson = services.joinToString("|") {
            "${it.code};${it.description};${it.total}"
        }
        val honorariosJson = professionalFees.joinToString("|") {
            "${it.code};${it.description};${it.total}"
        }
        val gastosJson = miscExpenses.joinToString("|") {
            "${it.description};${it.amount}"
        }
        val impuestosJson = taxes.joinToString("|") {
            "${it.description};${it.amount}"
        }
        return PresupuestoEntity(
            id = "pres_chat_${System.currentTimeMillis()}",
            numeroPresupuesto = "P-${(1000..9999).random()}",
            clienteId = clienteId,
            prestadorId = "prestador_demo",
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
            impuestosJson = impuestosJson
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.93f)) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = colors.primaryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "Presupuesto para",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        Text(
                            userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.border)

            // --- CONTENT ---
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Articles
                item {
                    CollapsibleSection(
                        title = "Artículos",
                        items = items,
                        sectionTotal = itemsSubtotal,
                        isExpanded = isArticlesExpanded,
                        onToggleExpand = { isArticlesExpanded = !isArticlesExpanded },
                        onAddClick = { itemToEdit = null; sheetType = SheetType.Article },
                        quickAddSlot = {
                            ArticleAutoCompleteFields(
                                suggestions = suggestionItems,
                                onAdd = { selected ->
                                    items.add(selected.copy(id = System.currentTimeMillis()))
                                    isArticlesExpanded = true
                                }
                            )
                        }
                    ) { item, index ->
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            ArticleSummaryRow(
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { showMenu = true })
                                },
                                item = item
                            )
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                    itemToEdit = item; sheetType = SheetType.Article; showMenu = false
                                })
                                DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                                    items.removeAt(index); showMenu = false
                                })
                            }
                        }
                    }
                }
                // Services
                item {
                    CollapsibleSection(
                        title = "Mano de Obra / Servicios",
                        items = services,
                        sectionTotal = servicesSubtotal,
                        isExpanded = isServicesExpanded,
                        onToggleExpand = { isServicesExpanded = !isServicesExpanded },
                        onAddClick = { itemToEdit = null; sheetType = SheetType.Service },
                        quickAddSlot = {
                            val suggServices = remember(presupuestos) {
                                presupuestos.flatMap { p ->
                                    if (p.serviciosJson.isBlank()) emptyList()
                                    else p.serviciosJson.split("|").mapNotNull { s ->
                                        val parts = s.split(";")
                                        if (parts.size >= 2) BudgetService(
                                            id = 0L, code = parts[0],
                                            description = parts[1],
                                            total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                                        ) else null
                                    }
                                }.distinctBy { it.description }
                            }
                            ServiceAutoCompleteFields(
                                suggestions = suggServices,
                                onAdd = { selected ->
                                    services.add(selected.copy(id = System.currentTimeMillis()))
                                    isServicesExpanded = true
                                }
                            )
                        }
                    ) { item, index ->
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            ServiceSummaryRow(
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { showMenu = true })
                                },
                                item = item
                            )
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                    itemToEdit = item; sheetType = SheetType.Service; showMenu = false
                                })
                                DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                                    services.removeAt(index); showMenu = false
                                })
                            }
                        }
                    }
                }
                // Professional Fees
                item {
                    CollapsibleSection(
                        title = "Honorarios del Profesional",
                        items = professionalFees,
                        sectionTotal = feesSubtotal,
                        isExpanded = isProfessionalFeesExpanded,
                        onToggleExpand = { isProfessionalFeesExpanded = !isProfessionalFeesExpanded },
                        onAddClick = { itemToEdit = null; sheetType = SheetType.ProfessionalFee },
                        quickAddSlot = {
                            val suggFees = remember(presupuestos) {
                                presupuestos.flatMap { p ->
                                    if (p.honorariosJson.isBlank()) emptyList()
                                    else p.honorariosJson.split("|").mapNotNull { s ->
                                        val parts = s.split(";")
                                        if (parts.size >= 2) BudgetProfessionalFee(
                                            id = 0L, code = parts[0],
                                            description = parts[1],
                                            total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                                        ) else null
                                    }
                                }.distinctBy { it.description }
                            }
                            FeeAutoCompleteFields(
                                suggestions = suggFees,
                                onAdd = { selected ->
                                    professionalFees.add(selected.copy(id = System.currentTimeMillis()))
                                    isProfessionalFeesExpanded = true
                                }
                            )
                        }
                    ) { item, index ->
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            ProfessionalFeeSummaryRow(
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { showMenu = true })
                                },
                                item = item
                            )
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                    itemToEdit = item; sheetType = SheetType.ProfessionalFee; showMenu = false
                                })
                                DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                                    professionalFees.removeAt(index); showMenu = false
                                })
                            }
                        }
                    }
                }
                // Misc
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
                                        if (parts.size >= 1) parts[0] else null
                                    }
                                }.distinct()
                            }
                            DescriptionAutoCompleteField(
                                label = "Descripción de gasto",
                                suggestions = suggMisc,
                                onSelect = { desc ->
                                    val prevAmount = presupuestos.flatMap { p ->
                                        if (p.gastosJson.isBlank()) emptyList()
                                        else p.gastosJson.split("|").mapNotNull { s ->
                                            val parts = s.split(";")
                                            if (parts.size >= 2 && parts[0] == desc) parts[1].toDoubleOrNull() else null
                                        }
                                    }.firstOrNull() ?: 0.0
                                    miscExpenses.add(BudgetMiscExpense(id = System.currentTimeMillis(), description = desc, amount = prevAmount))
                                    isMiscExpanded = true
                                }
                            )
                        }
                    ) { item, index ->
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            MiscExpenseSummaryRow(
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { showMenu = true })
                                },
                                item = item
                            )
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                    itemToEdit = item; sheetType = SheetType.Misc; showMenu = false
                                })
                                DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                                    miscExpenses.removeAt(index); showMenu = false
                                })
                            }
                        }
                    }
                }
                // Taxes
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
                                        if (parts.size >= 1) parts[0] else null
                                    }
                                }.distinct()
                            }
                            DescriptionAutoCompleteField(
                                label = "Descripción de impuesto",
                                suggestions = suggTaxes,
                                onSelect = { desc ->
                                    val prevAmount = presupuestos.flatMap { p ->
                                        if (p.impuestosJson.isBlank()) emptyList()
                                        else p.impuestosJson.split("|").mapNotNull { s ->
                                            val parts = s.split(";")
                                            if (parts.size >= 2 && parts[0] == desc) parts[1].toDoubleOrNull() else null
                                        }
                                    }.firstOrNull() ?: 0.0
                                    taxes.add(BudgetTax(id = System.currentTimeMillis(), description = desc, amount = prevAmount))
                                    isTaxesExpanded = true
                                }
                            )
                        }
                    ) { item, index ->
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            TaxSummaryRow(
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { showMenu = true })
                                },
                                item = item
                            )
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                    itemToEdit = item; sheetType = SheetType.Tax; showMenu = false
                                })
                                DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                                    taxes.removeAt(index); showMenu = false
                                })
                            }
                        }
                    }
                }
            }

            // --- BOTTOM BAR: totals + generate button ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E293B),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            "$ ${String.format("%,.2f", grandTotal)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            pendingPresupuesto = buildPresupuesto()
                            if (hasNewItems()) showSaveCatalogDialog = true
                            else showPreviewDialog = true
                        },
                        enabled = hasItems,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryOrange
                        )
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Vista Previa", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- ITEM FORMS as Dialog (to avoid nested ModalBottomSheet) ---
    if (sheetType != null) {
        Dialog(
            onDismissRequest = { sheetType = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.97f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = colors.backgroundColor
            ) {
                when (sheetType) {
                    SheetType.Article -> AddArticleSheetContent(
                        itemToEdit = itemToEdit as? BudgetItem,
                        suggestionItems = suggestionItems,
                        onAddItem = { items.add(it); sheetType = null; isArticlesExpanded = true },
                        onUpdateItem = { updated ->
                            val i = items.indexOfFirst { it.id == updated.id }
                            if (i != -1) items[i] = updated
                            sheetType = null
                        }
                    )
                    SheetType.Service -> AddServiceSheetContent(
                        itemToEdit = itemToEdit as? BudgetService,
                        onAddItem = { services.add(it); sheetType = null; isServicesExpanded = true },
                        onUpdateItem = { updated ->
                            val i = services.indexOfFirst { it.id == updated.id }
                            if (i != -1) services[i] = updated
                            sheetType = null
                        }
                    )
                    SheetType.ProfessionalFee -> AddProfessionalFeeSheetContent(
                        itemToEdit = itemToEdit as? BudgetProfessionalFee,
                        onAddItem = { professionalFees.add(it); sheetType = null; isProfessionalFeesExpanded = true },
                        onUpdateItem = { updated ->
                            val i = professionalFees.indexOfFirst { it.id == updated.id }
                            if (i != -1) professionalFees[i] = updated
                            sheetType = null
                        }
                    )
                    SheetType.Misc -> AddMiscExpenseSheetContent(
                        itemToEdit = itemToEdit as? BudgetMiscExpense,
                        onAddItem = { list -> miscExpenses.addAll(list); sheetType = null; isMiscExpanded = true },
                        onUpdateItem = { updated ->
                            val i = miscExpenses.indexOfFirst { it.id == updated.id }
                            if (i != -1) miscExpenses[i] = updated
                            sheetType = null
                        }
                    )
                    SheetType.Tax -> AddTaxSheetContent(
                        itemToEdit = itemToEdit as? BudgetTax,
                        onAddItem = { list -> taxes.addAll(list); sheetType = null; isTaxesExpanded = true },
                        onUpdateItem = { updated ->
                            val i = taxes.indexOfFirst { it.id == updated.id }
                            if (i != -1) taxes[i] = updated
                            sheetType = null
                        }
                    )
                    else -> {}
                }
            }
        }
    }

    // --- SAVE CATALOG DIALOG ---
    if (showSaveCatalogDialog) {
        AlertDialog(
            onDismissRequest = { showSaveCatalogDialog = false; showPreviewDialog = true },
            title = { Text("¿Guardar en catálogo?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Querés guardar estos ítems para autocompletar futuros presupuestos?") },
            confirmButton = {
                TextButton(onClick = {
                    pendingPresupuesto?.let { viewModel.insertPresupuesto(it) }
                    showSaveCatalogDialog = false
                    showPreviewDialog = true
                }) { Text("Sí, guardar", color = colors.primaryOrange) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveCatalogDialog = false
                    showPreviewDialog = true
                }) { Text("No") }
            }
        )
    }

    // --- PREVIEW WITH CAPTURE ---
    if (showPreviewDialog) {
        val prestador = remember {
            PPrestadorProfileFalso(
                id = "demo_1", name = "Prestador", lastName = "", profileImageUrl = "",
                bannerImageUrl = null, rating = 5.0f, isVerified = true, isOnline = true,
                services = emptyList(), companyName = "Mi Empresa", address = "",
                email = "", doesHomeVisits = false, hasPhysicalLocation = true,
                works24h = false, galleryImages = emptyList(), isFavorite = false, isSubscribed = true
            )
        }
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
            onDismiss = { showPreviewDialog = false },
            onEnviar = { showPreviewDialog = false; onDismiss() },
            onEnviarBudget = {
                val pres = pendingPresupuesto ?: buildPresupuesto()
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    isFromCurrentUser = true,
                    type = Message.MessageType.BUDGET,
                    text = "📋 Presupuesto",
                    budgetNumero = pres.numeroPresupuesto,
                    budgetTotal = pres.total,
                    budgetSubtotal = pres.subtotal,
                    budgetImpuestos = pres.impuestos,
                    budgetItemsJson = pres.itemsJson,
                    budgetServiciosJson = pres.serviciosJson,
                    budgetHonorariosJson = pres.honorariosJson,
                    budgetGastosJson = pres.gastosJson,
                    budgetImpuestosJson = pres.impuestosJson,
                    budgetNotas = pres.notas,
                    budgetValidezDias = pres.validezDias
                )
                AppointmentRescheduleManager.addMessage(userId, message)
                showPreviewDialog = false
                onDismiss()
            }
        )
    }
}
