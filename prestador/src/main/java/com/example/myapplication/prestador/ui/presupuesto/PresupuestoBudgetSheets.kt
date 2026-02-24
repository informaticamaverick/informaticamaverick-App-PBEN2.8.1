package com.example.myapplication.prestador.ui.presupuesto

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import java.util.UUID

// --- SHEET CONTENT COMPOSABLES ---

@Composable
fun AddArticleSheetContent(
    itemToEdit: BudgetItem?,
    suggestionItems: List<BudgetItem> = emptyList(),
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
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        Text(
            if (isEditMode) "Editar Artículo" else "Agregar Nuevo Artículo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primaryOrange,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BudgetItemRow(item = currentItem, suggestionItems = suggestionItems, onUpdate = { currentItem = it })

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
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        Text(
            if (isEditMode) "Editar Servicio" else "Agregar Nuevo Servicio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primaryOrange,
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
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        Text(
            if (isEditMode) "Editar Honorario" else "Agregar Honorario Profesional",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primaryOrange,
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
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (isEditMode) "Editar Gasto" else "Agregar Gastos Varios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primaryOrange
            )
            if (!isEditMode) {
                IconButton(
                    onClick = { expenseRows.add(TempMiscExpense()) },
                    modifier = Modifier.background(colors.primaryOrange, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar fila", tint = Color.White)
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
        // Handle pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFCBD5E1))
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                if (isEditMode) "Editar Impuesto" else "Agregar Impuestos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primaryOrange
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
