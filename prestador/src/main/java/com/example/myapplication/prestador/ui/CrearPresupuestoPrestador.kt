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
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.data.PPrestadorSampleDataFalso
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
fun CrearPresupuestoPrestadorScreen() {
    // --- STATE MANAGEMENT ---
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

    // --- BOTTOM SHEET STATES ---
    var sheetType by remember { mutableStateOf<SheetType?>(null) }
    var itemToEdit by remember { mutableStateOf<Any?>(null) }


    // --- SECTION EXPANSION STATES ---
    var isArticlesExpanded by remember { mutableStateOf(false) }
    var isServicesExpanded by remember { mutableStateOf(false) }
    var isProfessionalFeesExpanded by remember { mutableStateOf(false) }
    var isMiscExpanded by remember { mutableStateOf(false) }
    var isTaxesExpanded by remember { mutableStateOf(false) }
    var isAttachmentsExpanded by remember { mutableStateOf(false) }

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

    // --- CALCULATED TOTALS ---
    val itemsSubtotal = items.sumOf {
        val base = it.unitPrice * it.quantity
        val taxAmount = base * (it.taxPercentage / 100)
        val withTax = base + taxAmount
        val discountAmount = withTax * (it.discountPercentage / 100)
        withTax - discountAmount
    }

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
        topBar = {
            TopAppBar(
                title = { Text("Crear Presupuesto") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle back */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(onClick = { sheetType = SheetType.Client }) {
                    Icon(Icons.Default.Person, contentDescription = "Cliente")
                }
                FloatingActionButton(onClick = { showPreviewDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item { PrestadorHeader(prestador, onFilterClick = { sheetType = SheetType.Sections }) }
                item { HorizontalDivider() }

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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Observaciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Escriba aquí sus observaciones...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Presupuesto válido por", style = MaterialTheme.typography.bodyMedium)
                            CompactTextField(
                                value = validity,
                                onValueChange = { validity = it.filter { char -> char.isDigit() } },
                                modifier = Modifier.width(60.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                textStyle = TextStyle(textAlign = TextAlign.Center)
                            )
                            Text("días.", style = MaterialTheme.typography.bodyMedium)
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
                BudgetPreviewDialog(
                    onDismiss = { showPreviewDialog = false },
                    onConfirm = { showPreviewDialog = false },
                    prestador = prestador,
                    items = items,
                    services = services,
                    professionalFees = professionalFees,
                    miscExpenses = miscExpenses,
                    taxes = taxes,
                    grandTotal = grandTotal
                )
            }

            // --- BOTTOM SHEETS ---
            if (sheetType != null) {
                ModalBottomSheet(onDismissRequest = { sheetType = null }) {
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
    // FIX: Se rediseñó el Composable para evitar la superposición de texto.
    // COMENTARIO: El error ocurría porque se forzaba una altura fija de '26.dp' en un 'OutlinedTextField',
    // lo cual es insuficiente para renderizar el texto, el label y el borde sin que se superpongan.
    // La solución es usar 'BasicTextField' con 'OutlinedTextFieldDefaults.DecorationBox'.
    // Esto nos da control total sobre el 'contentPadding' para crear un campo de texto verdaderamente compacto
    // sin sacrificar la legibilidad y manteniendo el estilo de Material Design.
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
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
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp) // Padding compacto
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

    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding()) {
        Text(if (isEditMode) "Editar Artículo" else "Agregar Nuevo Artículo", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        BudgetItemRow(item = currentItem, onUpdate = { currentItem = it })

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Text("Impuestos y Descuentos", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))

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
            enabled = currentItem.description.isNotBlank() && currentItem.unitPrice > 0 && currentItem.quantity > 0
        ) {
            Text(if (isEditMode) "Guardar Cambios" else "Agregar Artículo")
        }
    }
}

@Composable
fun AddServiceSheetContent(
    itemToEdit: BudgetService?,
    onAddItem: (BudgetService) -> Unit,
    onUpdateItem: (BudgetService) -> Unit
) {
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetService()) }

    Column(modifier = Modifier.padding(16.dp).imePadding().navigationBarsPadding()) {
        Text(if (isEditMode) "Editar Servicio" else "Agregar Nuevo Servicio", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        BudgetServiceRow(service = currentItem, onUpdate = { currentItem = it })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (isEditMode) onUpdateItem(currentItem) else onAddItem(currentItem) },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentItem.description.isNotBlank() && currentItem.total > 0
        ) {
            Text(if (isEditMode) "Guardar Cambios" else "Agregar Servicio")
        }
    }
}

@Composable
fun AddProfessionalFeeSheetContent(
    itemToEdit: BudgetProfessionalFee?,
    onAddItem: (BudgetProfessionalFee) -> Unit,
    onUpdateItem: (BudgetProfessionalFee) -> Unit
) {
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetProfessionalFee()) }

    Column(modifier = Modifier.padding(16.dp).imePadding().navigationBarsPadding()) {
        Text(if (isEditMode) "Editar Honorario" else "Agregar Honorario Profesional", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        BudgetProfessionalFeeRow(fee = currentItem, onUpdate = { updatedItem -> currentItem = updatedItem })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (isEditMode) onUpdateItem(currentItem) else onAddItem(currentItem) },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentItem.description.isNotBlank() && currentItem.total > 0
        ) {
            Text(if (isEditMode) "Guardar Cambios" else "Agregar Honorario")
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
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(if (isEditMode) "Editar Gasto" else "Agregar Gastos Varios", style = MaterialTheme.typography.titleLarge)
            if (!isEditMode) {
                IconButton(onClick = { expenseRows.add(TempMiscExpense()) }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar fila", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        expenseRows.forEachIndexed { index, row ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    IconButton(onClick = { expenseRows.removeAt(index) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }

        if (!isEditMode) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Gastos Comunes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp), fontWeight = FontWeight.Bold)
            commonRows.forEachIndexed { index, row ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Text(text = "Total Gasto: \$${"%.2f".format(totalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End).padding(vertical = 8.dp))
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
            enabled = totalAmount > 0
        ) {
            Text(if (isEditMode) "Guardar Cambios" else "Agregar Gastos")
        }
    }
}

@Composable
fun AddTaxSheetContent(
    itemToEdit: BudgetTax?,
    onAddItem: (List<BudgetTax>) -> Unit,
    onUpdateItem: (BudgetTax) -> Unit
) {
    val isEditMode = itemToEdit != null
    val initialList = if (itemToEdit != null) {
        listOf(BudgetTax(description = itemToEdit.description, amount = itemToEdit.amount))
    } else {
        listOf(BudgetTax())
    }
    val taxRows = remember { mutableStateListOf<BudgetTax>().apply { addAll(initialList) } }
    val commonTaxes = remember { mutableStateListOf(BudgetTax(description = "IVA 21%"), BudgetTax(description = "IVA 10.5%"), BudgetTax(description = "Retenciones"), BudgetTax(description = "Ingresos Brutos")) }

    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (isEditMode) "Editar Impuesto" else "Agregar Impuestos", style = MaterialTheme.typography.titleLarge)
            if (!isEditMode) {
                IconButton(onClick = { taxRows.add(BudgetTax()) }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar fila", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        taxRows.forEachIndexed { index, row ->
            var amountStr by remember { mutableStateOf("") }
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactTextField(value = row.description, onValueChange = { taxRows[index] = row.copy(description = it) }, label = { Text("Descripción") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                CompactTextField(value = amountStr, onValueChange = { amountStr = it; taxRows[index] = row.copy(amount = it.toDoubleOrNull() ?: 0.0) }, label = { Text("Importe ($)") }, modifier = Modifier.width(100.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
                if (!isEditMode && taxRows.size > 1) {
                    IconButton(onClick = { taxRows.removeAt(index) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }

        if (!isEditMode) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Impuestos Comunes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp), fontWeight = FontWeight.Bold)
            commonTaxes.forEachIndexed { index, row ->
                var amountStr by remember { mutableStateOf("") }
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEditMode) "Guardar Cambios" else "Agregar Impuestos")
        }
    }
}

@Composable
fun AddAttachmentSheetContent(
    itemToEdit: BudgetAttachment?,
    onAddItem: (BudgetAttachment) -> Unit,
    onUpdateItem: (BudgetAttachment) -> Unit
) {
    val isEditMode = itemToEdit != null
    var currentItem by remember { mutableStateOf(itemToEdit ?: BudgetAttachment()) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            currentItem = currentItem.copy(uri = uri, type = if (uri.toString().endsWith("pdf")) AttachmentType.PDF else AttachmentType.IMAGE)
        }
    }

    Column(modifier = Modifier.padding(16.dp).imePadding().navigationBarsPadding()) {
        Text("Adjuntar Archivo", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).clickable { launcher.launch("*/*") },
            contentAlignment = Alignment.Center
        ) {
            if (currentItem.uri != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = if (currentItem.type == AttachmentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Archivo seleccionado", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Toque para seleccionar (Imagen/PDF)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CompactTextField(value = currentItem.description, onValueChange = { currentItem = currentItem.copy(description = it) }, label = { Text("Descripción / Detalle") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { if (isEditMode) onUpdateItem(currentItem) else onAddItem(currentItem) }, modifier = Modifier.fillMaxWidth(), enabled = currentItem.uri != null) {
            Text("Adjuntar")
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
    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
        Text("Mostrar/Ocultar Secciones", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ClientDetailsSheetContent() {
    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
        Text("Datos del Cliente", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        Text("Funcionalidad en desarrollo", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun BudgetItemRow(item: BudgetItem, onUpdate: (BudgetItem) -> Unit) {
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
            Text("Total: \$${"%.2f".format(total)}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
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
    val base = item.unitPrice * item.quantity
    val taxAmount = base * (item.taxPercentage / 100)
    val withTax = base + taxAmount
    val discountAmount = withTax * (item.discountPercentage / 100)
    val total = withTax - discountAmount

    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.description.ifBlank { "(Sin descripción)" }, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text("${item.quantity} x \$${"%.2f".format(item.unitPrice)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("\$${"%.2f".format(total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ServiceSummaryRow(modifier: Modifier = Modifier, item: BudgetService) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(item.description.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text("\$${"%.2f".format(item.total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ProfessionalFeeSummaryRow(modifier: Modifier = Modifier, item: BudgetProfessionalFee) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(item.description.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text("\$${"%.2f".format(item.total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TaxSummaryRow(modifier: Modifier = Modifier, item: BudgetTax) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(item.description.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text("\$${"%.2f".format(item.amount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AttachmentSummaryRow(modifier: Modifier = Modifier, item: BudgetAttachment) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = if (item.type == AttachmentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(item.description.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Icon(Icons.Default.CheckCircle, contentDescription = "Adjunto", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun MiscExpenseSummaryRow(modifier: Modifier = Modifier, item: BudgetMiscExpense) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(item.description.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text("\$${"%.2f".format(item.amount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TotalsSummary(modifier: Modifier = Modifier, isExpanded: Boolean, grandTotal: Double) {
    val priceTextStyle = if (isExpanded) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge
    val verticalPadding = if (isExpanded) 12.dp else 4.dp
    Surface(modifier = modifier.fillMaxWidth(), shadowElevation = 8.dp, tonalElevation = 8.dp) {
        Column(
            modifier = Modifier.animateContentSize().padding(horizontal = 16.dp, vertical = verticalPadding),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("TOTAL GENERAL", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "\$${"%.2f".format(grandTotal)}", style = priceTextStyle, fontWeight = FontWeight.Bold)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetPreviewDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    prestador: PPrestadorProfileFalso,
    items: List<BudgetItem>,
    services: List<BudgetService>,
    professionalFees: List<BudgetProfessionalFee>,
    miscExpenses: List<BudgetMiscExpense>,
    taxes: List<BudgetTax>,
    grandTotal: Double
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            containerColor = Color.White,
            floatingActionButton = {
                ExtendedFloatingActionButton(onClick = onConfirm, containerColor = Color(0xFF1976D2), contentColor = Color.White) {
                    Text("GUARDAR Y ENVIAR", fontWeight = FontWeight.Bold)
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).background(Color.White)) {
                // Header, Client Data, Detail, Footer Sections
            }
        }
    }
}

@Composable
fun BudgetMetaBox(title: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(4.dp))) {
        Text(text = title, modifier = Modifier.fillMaxWidth().background(Color(0xFF1976D2)).padding(2.dp), color = Color.White, fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Text(text = value, modifier = Modifier.fillMaxWidth().background(Color.White).padding(4.dp), fontSize = 10.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SectionHeader(text: String) {
    Surface(color = Color(0xFF1976D2), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))) {
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
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Box(modifier = Modifier.padding(top = 18.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "${items.size}",
                        modifier = Modifier.padding(horizontal = 6.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    modifier = Modifier.rotate(rotationAngle)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    if (items.isEmpty()) {
                        Text(
                            "No hay ítems agregados.",
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column {
                            items.forEachIndexed { index, item ->
                                itemContent(item, index)
                                if (index < items.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir ${title.substringBefore(' ')}")
        }

        if (items.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 12.dp),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                tonalElevation = 3.dp
            ) {
                Text(
                    text = "Subtotal: \$${"%.2f".format(sectionTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PrestadorHeader(prestador: PPrestadorProfileFalso, onFilterClick: () -> Unit) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AsyncImage(model = prestador.profileImageUrl, contentDescription = "Foto de perfil", modifier = Modifier.size(56.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.weight(1f)) {
                Text(prestador.companyName ?: "${prestador.name} ${prestador.lastName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(prestador.address, style = MaterialTheme.typography.bodyMedium)
                Text("Email: ${prestador.email}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onFilterClick) { Icon(Icons.Filled.FilterList, contentDescription = "Filtrar Secciones") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Presupuesto Nº: 0001", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("Fecha: $currentDate", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun Modifier.drawDottedLineBottom() = this.drawBehind {
    val strokeWidth = 1.dp.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(color = Color.Gray, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun CrearPresupuestoPrestadorScreenPreview() {
    MaterialTheme {
        CrearPresupuestoPrestadorScreen()
    }
}
