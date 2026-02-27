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
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * VISOR DE PRESUPUESTO A4 (MAVERICK FAST) - VERSIÓN REPARADA
 * Incluye soporte para ZOOM (gesto de 2 dedos), botones de decisión y legibilidad mejorada.
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

    // Dividimos los ítems dinámicamente para simular las hojas físicas
    val pagedItems = remember(budget.items) {
        val pages = mutableListOf<List<BudgetItem>>()
        val items = budget.items.toMutableList()
        
        if (items.isNotEmpty()) {
            // La primera hoja (8 ítems max)
            pages.add(items.take(8))
            val remaining = items.drop(8).toMutableList()
            
            while (remaining.isNotEmpty()) {
                pages.add(remaining.take(15))
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
    pageItems: List<BudgetItem>,
    pageNumber: Int,
    totalPoints: Int,
    isFirstPage: Boolean,
    isLastPage: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(if (isFirstPage || isLastPage) 1f else 0.95f) // Pequeño ajuste visual
            .widthIn(max = 500.dp) // Evita que en pantallas gigantes se vea deforme
            .aspectRatio(0.707f),
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
                        Text("Servicios Profesionales Maverick", fontSize = 10.sp, color = Color.Gray)
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
                
                // Info del cliente (Recuperado de budget o placeholder si no está)
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

            // --- TABLA DE DESGLOSE (LISTA) ---
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A)).padding(8.dp)) {
                Text("CANT", modifier = Modifier.width(40.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text("DESCRIPCIÓN DEL SERVICIO", modifier = Modifier.weight(1f), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text("SUBTOTAL", modifier = Modifier.width(80.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End)
            }

            Column(modifier = Modifier.weight(1f)) {
                pageItems.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp)) {
                        Text(item.quantity.toString(), modifier = Modifier.width(40.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(item.description, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.Black)
                        Text(
                            text = "$ ${String.format("%,.0f", item.unitPrice * item.quantity)}", 
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

            if (isLastPage) {
                // Bloque de totales al final de todo
                Column(modifier = Modifier.align(Alignment.End).width(220.dp).padding(top = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SUBTOTAL NETO", fontSize = 11.sp, color = Color.Gray)
                        Text("$ ${String.format("%,.2f", budget.subtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
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
                text = "MAVERICK FAST SERVICE • VALIDEZ DE OFERTA: ${budget.validityDays} DÍAS", 
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
    val sampleItems = (1..10).map { i ->
        BudgetItem(
            code = "SVR-$i",
            description = "Servicio Técnico Nivel $i - Análisis de hardware y software",
            quantity = 1,
            unitPrice = 12000.0 * i
        )
    }

    val sampleBudget = BudgetEntity(
        budgetId = "PRE-2024-TEST-01",
        clientId = "user_123",
        providerId = "prov_maverick",
        providerName = "Maximiliano Nanterne",
        providerCompanyName = "Maverick Informática S.A.",
        items = sampleItems,
        subtotal = sampleItems.sumOf { it.unitPrice * it.quantity },
        grandTotal = sampleItems.sumOf { it.unitPrice * it.quantity },
        notes = "Garantía de 6 meses incluida.",
        paymentMethods = "Efectivo / Transferencia"
    )

    MyApplicationTheme {
        BudgetMultiPageScreen(
            budget = sampleBudget,
            onBack = {}
        )
    }
}
