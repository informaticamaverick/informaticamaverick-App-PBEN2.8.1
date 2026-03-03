package com.example.myapplication.presentation.client

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.BudgetItem
import com.example.myapplication.data.local.BudgetService
import com.example.myapplication.data.local.BudgetProfessionalFee
import com.example.myapplication.data.local.BudgetTax
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.ui.theme.MyApplicationTheme

// --- ESTRUCTURA UNIFICADA PARA LA TABLA ---
data class PrintableRow(
    val qty: String,
    val description: String,
    val total: Double
)

/**
 * VISOR DE PRESUPUESTO A4 (MAVERICK FAST) - VERSIÓN REPARADA
 * Se corrigió el scroll de múltiples páginas y se añadieron las condiciones comerciales.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetMultiPageScreen(
    budget: BudgetEntity,
    onAccept: (String) -> Unit = {},
    onReject: (String) -> Unit = {},
    onBack: () -> Unit
) {
    // --- LÓGICA DE ZOOM (Controlado por botones para no romper el scroll) ---
    var scale by remember { mutableFloatStateOf(1f) }

    // --- CONSOLIDACIÓN DE DATOS ---
    val pagedItems = remember(budget) {
        val rows = mutableListOf<PrintableRow>()

        budget.items.forEach { rows.add(PrintableRow(it.quantity.toString(), it.description, it.unitPrice * it.quantity)) }
        budget.services.forEach { rows.add(PrintableRow("-", it.description, it.total)) }
        budget.professionalFees.forEach { rows.add(PrintableRow("-", it.description, it.total)) }
        budget.miscExpenses.forEach { rows.add(PrintableRow("-", it.description, it.amount)) }

        val pages = mutableListOf<List<PrintableRow>>()

        if (rows.isNotEmpty()) {
            // Primera hoja (6 ítems max para dar espacio al encabezado)
            pages.add(rows.take(6))
            val remaining = rows.drop(6).toMutableList()

            while (remaining.isNotEmpty()) {
                // Hojas siguientes aguantan hasta 12 filas para no desbordar
                pages.add(remaining.take(12))
                val nextChunk = remaining.drop(12)
                remaining.clear()
                remaining.addAll(nextChunk)
            }
        } else {
            pages.add(emptyList())
        }
        pages
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("DETALLE DE OFERTA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("De: ${budget.providerName}", color = Color.Cyan.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                actions = {
                    // Controles de Zoom seguros
                    IconButton(onClick = { scale = (scale + 0.25f).coerceAtMost(2.5f) }) {
                        Icon(Icons.Default.ZoomIn, "Acercar", tint = Color.White)
                    }
                    IconButton(onClick = { scale = (scale - 0.25f).coerceAtLeast(1f) }) {
                        Icon(Icons.Default.ZoomOut, "Alejar", tint = Color.White)
                    }
                    IconButton(onClick = { /* Exportar a PDF real */ }) {
                        Icon(Icons.Default.PictureAsPdf, "PDF", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
            if (budget.status == BudgetStatus.PENDIENTE) {
                Surface(
                    color = Color(0xFF1E293B),
                    tonalElevation = 8.dp,
                    shadowElevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onReject(budget.budgetId) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("RECHAZAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onAccept(budget.budgetId) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))
                        ) {
                            Text("ACEPTAR ESTE", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        // --- LAZY COLUMN CON SCROLL NATIVO Y ZOOM APLICADO AL LAYER ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(pagedItems) { index, pageItems ->
                BudgetA4Page(
                    budget = budget,
                    pageItems = pageItems,
                    pageNumber = index + 1,
                    totalPoints = pagedItems.size,
                    isFirstPage = index == 0,
                    isLastPage = index == pagedItems.size - 1
                )
            }
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun BudgetA4Page(
    budget: BudgetEntity,
    pageItems: List<PrintableRow>,
    pageNumber: Int,
    totalPoints: Int,
    isFirstPage: Boolean,
    isLastPage: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
            .aspectRatio(0.707f), // Proporción A4 estándar
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // --- ENCABEZADO DE PÁGINA ---
            if (isFirstPage) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = budget.providerCompanyName ?: budget.providerName,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF1E3A8A),
                            maxLines = 2
                        )
                        Text("Servicios Profesionales", fontSize = 10.sp, color = Color.Gray)
                    }

                    Surface(
                        color = when(budget.status) {
                            BudgetStatus.ACEPTADO -> Color(0xFFDCFCE7)
                            BudgetStatus.RECHAZADO -> Color(0xFFFEE2E2)
                            else -> Color(0xFFF1F5F9)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = budget.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = when(budget.status) {
                                BudgetStatus.ACEPTADO -> Color(0xFF166534)
                                BudgetStatus.RECHAZADO -> Color(0xFF991B1B)
                                else -> Color(0xFF475569)
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 3.dp, color = Color(0xFF1E3A8A))

                Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                    Text("CLIENTE DESTINATARIO:", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))
                    Text("Maximiliano Nanterne", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ID: ${budget.budgetId.takeLast(8).uppercase()}", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("PÁGINA $pageNumber DE $totalPoints", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TABLA DE DESGLOSE ---
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A)).padding(8.dp)) {
                Text("CANT", modifier = Modifier.width(40.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text("CONCEPTO / DESCRIPCIÓN", modifier = Modifier.weight(1f), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text("SUBTOTAL", modifier = Modifier.width(80.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End)
            }

            Column(modifier = Modifier.weight(1f)) {
                pageItems.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp)) {
                        Text(row.qty, modifier = Modifier.width(40.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(row.description, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.Black)
                        Text(
                            text = "$ ${String.format("%,.2f", row.total)}",
                            modifier = Modifier.width(80.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.End,
                            color = Color.Black
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }

            // --- RESUMEN, TOTALES Y CONDICIONES (SÓLO ÚLTIMA PÁGINA) ---
            if (isLastPage) {

                // CONDICIONES COMERCIALES (Los datos que faltaban)
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).background(Color(0xFFF8FAFC)).padding(12.dp)) {
                    Text("CONDICIONES COMERCIALES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))
                    Spacer(modifier = Modifier.height(4.dp))

                    budget.executionTime?.let { Text("• Ejecución: $it", fontSize = 9.sp, color = Color.DarkGray) }
                    budget.warrantyInfo?.let { Text("• Garantía: $it", fontSize = 9.sp, color = Color.DarkGray) }
                    budget.paymentMethods?.let { Text("• Forma de pago: $it", fontSize = 9.sp, color = Color.DarkGray) }
                    Text("• Validez de oferta: ${budget.validityDays} días", fontSize = 9.sp, color = Color.DarkGray)

                    if (!budget.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("NOTAS ADICIONALES:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(budget.notes, fontSize = 9.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 2.dp))
                    }
                }

                Column(modifier = Modifier.align(Alignment.End).width(240.dp).padding(top = 16.dp)) {
                    // Subtotal
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SUBTOTAL NETO", fontSize = 11.sp, color = Color.Gray)
                        Text("$ ${String.format("%,.2f", budget.subtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    // Descuentos
                    if (budget.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("DESCUENTOS", fontSize = 11.sp, color = Color(0xFF166534))
                            Text("- $ ${String.format("%,.2f", budget.discountAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                    }

                    // Impuestos
                    if (budget.taxAmount > 0) {
                        budget.taxes.forEach { tax ->
                            Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(tax.description, fontSize = 11.sp, color = Color.Gray)
                                Text("$ ${String.format("%,.2f", tax.amount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // TOTAL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E3A8A), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL FINAL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text(
                            text = "$ ${String.format("%,.2f", budget.grandTotal)}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("--- DOCUMENTO CONTINÚA EN LA SIGUIENTE PÁGINA ---", textAlign = TextAlign.Center, fontSize = 9.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("MAVERICK FAST SERVICE • DOCUMENTO GENERADO AUTOMÁTICAMENTE", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 7.sp, color = Color.LightGray)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun BudgetMultiPageScreenPreview() {
    // Generamos suficientes items para forzar múltiples páginas
    val sampleItems = (1..15).map {
        BudgetItem(description = "Insumo Técnico Nivel $it", quantity = 2, unitPrice = 1500.0)
    }
    val sampleServices = listOf(BudgetService(description = "Instalación de Nodos", total = 15000.0))
    val sampleFees = listOf(BudgetProfessionalFee(description = "Certificación de Red", total = 8000.0))

    val subtotal = (1500.0 * 2 * 15) + 15000.0 + 8000.0
    val sampleTaxes = listOf(BudgetTax(description = "IVA (21%)", amount = subtotal * 0.21))

    val sampleBudget = BudgetEntity(
        budgetId = "PRE-2024-TEST-01",
        clientId = "user_123",
        providerId = "prov_maverick",
        providerName = "Maximiliano Nanterne",
        providerCompanyName = "Maverick Informática S.A.",
        items = sampleItems,
        services = sampleServices,
        professionalFees = sampleFees,
        taxes = sampleTaxes,
        subtotal = subtotal,
        taxAmount = sampleTaxes.sumOf { it.amount },
        grandTotal = subtotal + sampleTaxes.sumOf { it.amount },
        notes = "Requiere pago del 50% por adelantado para congelar precio de materiales.",
        paymentMethods = "Transferencia Bancaria / MercadoPago",
        warrantyInfo = "12 meses de garantía escrita.",
        executionTime = "4 a 5 días hábiles.",
        validityDays = 15
    )

    MyApplicationTheme {
        BudgetMultiPageScreen(
            budget = sampleBudget,
            onBack = {}
        )
    }
}








/**
package com.example.myapplication.presentation.client

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.BudgetItem
import com.example.myapplication.data.local.BudgetService
import com.example.myapplication.data.local.BudgetProfessionalFee
import com.example.myapplication.data.local.BudgetTax
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.ui.theme.MyApplicationTheme

// --- ESTRUCTURA UNIFICADA PARA LA TABLA ---
data class PrintableRow(
    val qty: String,
    val description: String,
    val total: Double
)

/**
 * VISOR DE PRESUPUESTO A4 (MAVERICK FAST) - VERSIÓN REPARADA Y COMPLETA
 * Agrupa materiales, servicios y honorarios en la tabla. Muestra impuestos en el subtotal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetMultiPageScreen(
    budget: BudgetEntity,
    onAccept: (String) -> Unit = {},
    onReject: (String) -> Unit = {},
    onBack: () -> Unit
) {
    // --- LÓGICA DE ZOOM ---
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // --- CONSOLIDACIÓN DE DATOS ---
    // Agrupamos items, servicios y honorarios en una sola lista para la tabla
    val pagedItems = remember(budget) {
        val rows = mutableListOf<PrintableRow>()

        budget.items.forEach {
            // Los items tienen cantidad y precio unitario (mostramos el total calculado)
            rows.add(PrintableRow(it.quantity.toString(), it.description, it.unitPrice * it.quantity))
        }
        budget.services.forEach {
            // Los servicios van por total global
            rows.add(PrintableRow("-", it.description, it.total))
        }
        budget.professionalFees.forEach {
            // Los honorarios van por total global
            rows.add(PrintableRow("-", it.description, it.total))
        }

        val pages = mutableListOf<List<PrintableRow>>()

        if (rows.isNotEmpty()) {
            // La primera hoja (8 ítems max porque tiene el cabezal)
            pages.add(rows.take(8))
            val remaining = rows.drop(8).toMutableList()

            while (remaining.isNotEmpty()) {
                pages.add(remaining.take(15)) // Hojas siguientes aguantan más filas
                val nextChunk = remaining.drop(15)
                remaining.clear()
                remaining.addAll(nextChunk)
            }
        } else {
            pages.add(emptyList())
        }
        pages
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "DETALLE DE OFERTA",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            "Viendo presupuesto de: ${budget.providerName}",
                            color = Color.Cyan.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Exportar a PDF real */ }) {
                        Icon(Icons.Default.PictureAsPdf, null, tint = Color.White)
                    }
                    // Botón para resetear Zoom
                    if (scale != 1f) {
                        IconButton(onClick = { scale = 1f; offset = Offset.Zero }) {
                            Icon(Icons.Default.ZoomOutMap, null, tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
            // SOLO mostramos los botones si el presupuesto está PENDIENTE.
            if (budget.status == BudgetStatus.PENDIENTE) {
                Surface(
                    color = Color(0xFF1E293B),
                    tonalElevation = 8.dp,
                    shadowElevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onReject(budget.budgetId) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("RECHAZAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onAccept(budget.budgetId) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))
                        ) {
                            Text("ACEPTAR ESTE", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        // --- CONTENEDOR TRANSFORMABLE (ZOOM) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        scale = (scale * zoom).coerceIn(1f, 3f)
                        if (scale > 1f) {
                            offset += pan
                        } else {
                            offset = Offset.Zero
                        }
                    }
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(pagedItems) { index, pageItems ->
                    BudgetA4Page(
                        budget = budget,
                        pageItems = pageItems,
                        pageNumber = index + 1,
                        totalPoints = pagedItems.size,
                        isFirstPage = index == 0,
                        isLastPage = index == pagedItems.size - 1
                    )
                }
                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
fun BudgetA4Page(
    budget: BudgetEntity,
    pageItems: List<PrintableRow>,
    pageNumber: Int,
    totalPoints: Int,
    isFirstPage: Boolean,
    isLastPage: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(if (isFirstPage || isLastPage) 1f else 0.95f)
            .widthIn(max = 500.dp)
            .aspectRatio(0.707f), // Proporción A4 estándar
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            if (isFirstPage) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            text = budget.providerCompanyName ?: budget.providerName,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF1E3A8A)
                        )
                        Text("Servicios Profesionales", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Válido por: ${budget.validityDays} días", fontSize = 9.sp, color = Color.DarkGray)
                    }

                    // ESTADO ACTUAL DEL DOCUMENTO
                    Surface(
                        color = when(budget.status) {
                            BudgetStatus.ACEPTADO -> Color(0xFFDCFCE7)
                            BudgetStatus.RECHAZADO -> Color(0xFFFEE2E2)
                            else -> Color(0xFFF1F5F9)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = budget.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = when(budget.status) {
                                BudgetStatus.ACEPTADO -> Color(0xFF166534)
                                BudgetStatus.RECHAZADO -> Color(0xFF991B1B)
                                else -> Color(0xFF475569)
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 3.dp, color = Color(0xFF1E3A8A))

                // Info del cliente
                Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                    Text("CLIENTE DESTINATARIO:", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))
                    Text("Maximiliano Nanterne", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ID: ${budget.budgetId.takeLast(8).uppercase()}", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("PÁGINA $pageNumber DE $totalPoints", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TABLA DE DESGLOSE (LISTA CONSOLIDADA) ---
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A)).padding(8.dp)) {
                Text("CANT", modifier = Modifier.width(40.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text("CONCEPTO / DESCRIPCIÓN", modifier = Modifier.weight(1f), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text("SUBTOTAL", modifier = Modifier.width(80.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End)
            }

            Column(modifier = Modifier.weight(1f)) {
                pageItems.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp)) {
                        Text(row.qty, modifier = Modifier.width(40.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(row.description, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.Black)
                        Text(
                            text = "$ ${String.format("%,.2f", row.total)}",
                            modifier = Modifier.width(80.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.End,
                            color = Color.Black
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }

            // --- RESUMEN Y TOTALES (SÓLO ÚLTIMA PÁGINA) ---
            if (isLastPage) {
                Column(modifier = Modifier.align(Alignment.End).width(240.dp).padding(top = 16.dp)) {

                    // Subtotal Neto
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SUBTOTAL NETO", fontSize = 11.sp, color = Color.Gray)
                        Text("$ ${String.format("%,.2f", budget.subtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    // Descuentos (Si los hay)
                    if (budget.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("DESCUENTOS", fontSize = 11.sp, color = Color(0xFF166534))
                            Text("- $ ${String.format("%,.2f", budget.discountAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                    }

                    // Impuestos (Desglosamos o mostramos el total de impuestos)
                    if (budget.taxAmount > 0) {
                        budget.taxes.forEach { tax ->
                            Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(tax.description, fontSize = 11.sp, color = Color.Gray)
                                Text("$ ${String.format("%,.2f", tax.amount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // TOTAL FINAL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E3A8A), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL FINAL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text(
                            text = "$ ${String.format("%,.2f", budget.grandTotal)}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }

                // Notas extra
                if (!budget.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("NOTAS Y CONDICIONES:", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                    Text(budget.notes, fontSize = 9.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                }

            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "--- DOCUMENTO CONTINÚA EN LA SIGUIENTE PÁGINA ---",
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "MAVERICK FAST SERVICE • DOCUMENTO GENERADO AUTOMÁTICAMENTE",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 7.sp,
                color = Color.LightGray
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun BudgetMultiPageScreenPreview() {
    val sampleItems = listOf(
        BudgetItem(description = "Kit de Cables de Red", quantity = 2, unitPrice = 4500.0)
    )
    val sampleServices = listOf(
        BudgetService(description = "Instalación de Nodos", total = 15000.0)
    )
    val sampleFees = listOf(
        BudgetProfessionalFee(description = "Certificación de Red", total = 8000.0)
    )
    val subtotal = 9000.0 + 15000.0 + 8000.0
    val sampleTaxes = listOf(
        BudgetTax(description = "IVA (21%)", amount = subtotal * 0.21)
    )

    val sampleBudget = BudgetEntity(
        budgetId = "PRE-2024-TEST-01",
        clientId = "user_123",
        providerId = "prov_maverick",
        providerName = "Maximiliano Nanterne",
        providerCompanyName = "Maverick Informática S.A.",
        items = sampleItems,
        services = sampleServices,
        professionalFees = sampleFees,
        taxes = sampleTaxes,
        subtotal = subtotal,
        taxAmount = sampleTaxes.sumOf { it.amount },
        grandTotal = subtotal + sampleTaxes.sumOf { it.amount },
        notes = "Garantía de 6 meses incluida en mano de obra.",
        paymentMethods = "Efectivo / Transferencia",
        validityDays = 15
    )

    MyApplicationTheme {
        BudgetMultiPageScreen(
            budget = sampleBudget,
            onBack = {}
        )
    }
}

**/