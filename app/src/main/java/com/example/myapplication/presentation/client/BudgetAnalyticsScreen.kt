package com.example.myapplication.presentation.client

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.data.local.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.Locale

// --- PALETA ANALÍTICA ---
private val DarkBg = Color(0xFF020408)
private val GlassPanel = Color(0xFF161C24)
private val ColorMaterial = Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))) // Azul
private val ColorLabor = Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF7E22CE)))    // Violeta
private val ColorTax = Brush.verticalGradient(listOf(Color(0xFFF43F5E), Color(0xFFBE123C)))      // Rojo/Rosa
private val SuccessGreen = Color(0xFF10B981)

/**
 * Extensiones de ayuda para cálculos sobre BudgetEntity
 */
private fun BudgetEntity.itemsTotal() = items.sumOf { it.unitPrice * it.quantity }
private fun BudgetEntity.servicesTotal() = services.sumOf { it.total }

/**
 * PANTALLA: ANÁLISIS COMPARATIVO DE PRESUPUESTOS (VISTA MEJORADA PRO)
 * Soporta múltiples prestadores con desplazamiento horizontal, escalas de precios
 * y efectos visuales de enfoque al seleccionar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetComparisonAnalytics(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    onBack: () -> Unit
) {
    // Filtro interactivo para resaltar un proveedor específico en todo el panel
    var selectedProviderId by remember { mutableStateOf<String?>(null) }

    // Cálculos globales para métricas y escalado
    val average = if (budgets.isNotEmpty()) budgets.map { it.grandTotal }.average() else 0.0
    val minPrice = budgets.minByOrNull { it.grandTotal }?.grandTotal ?: 0.0
    val maxPrice = budgets.maxByOrNull { it.grandTotal }?.grandTotal ?: 0.0

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ANÁLISIS DE COSTOS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
                        Text(tender.title, fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. KPIs RESUMEN
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsKpiCard(Modifier.weight(1f), "PRECIO PROMEDIO", average, Color.White)
                AnalyticsKpiCard(Modifier.weight(1f), "AHORRO MÁXIMO", maxPrice - minPrice, SuccessGreen)
            }

            // 2. GRÁFICO DE COLUMNAS CON ESCALA Y ENFOQUE DINÁMICO
            EnhancedGroupedBarChart(
                budgets = budgets,
                selectedProviderId = selectedProviderId,
                onSelectProvider = { id ->
                    selectedProviderId = if (selectedProviderId == id) null else id
                }
            )

            // 3. MATRIZ DE COMPARACIÓN DETALLADA (Scrollable Horizontal)
            MatrixTableSection(budgets)

            // 4. NUEVA SECCIÓN: BENCHMARK DE MERCADO (Promedios vs Líderes)
            MarketBenchmarkCard(budgets)

            // 5. INSIGHT IA
            AiInsightCard(budgets)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AnalyticsKpiCard(modifier: Modifier, label: String, value: Double, color: Color) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
            Text("$ ${String.format(Locale.getDefault(), "%,.0f", value)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

// =================================================================================
// --- COMPONENTE DE GRÁFICO MEJORADO CON ESCALAS ---
// =================================================================================

@Composable
fun EnhancedGroupedBarChart(
    budgets: List<BudgetEntity>,
    selectedProviderId: String?,
    onSelectProvider: (String) -> Unit
) {
    // Calculamos el valor máximo para escalar el eje Y basado en los totales individuales de categorías
    val maxItemVal = budgets.maxOfOrNull { it.itemsTotal() } ?: 0.0
    val maxServiceVal = budgets.maxOfOrNull { it.servicesTotal() } ?: 0.0
    val maxTaxVal = budgets.maxOfOrNull { it.taxAmount } ?: 0.0
    val globalMax = listOf(maxItemVal, maxServiceVal, maxTaxVal).maxOrNull() ?: 1.0

    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        color = GlassPanel,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("DISTRIBUCIÓN TÉCNICA", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(30.dp))

            // ÁREA DEL GRÁFICO CON ESCALA LATERAL IZQUIERDA
            Row(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                
                // ESCALA DE PRECIOS IZQUIERDA (Y-Axis)
                Column(
                    modifier = Modifier.fillMaxHeight().padding(end = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    val steps = 5
                    for (i in steps downTo 0) {
                        Text(
                            text = "$ ${String.format(Locale.getDefault(), "%,.0f", (globalMax / steps) * i / 1000)}k",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // CONTENEDOR DE BARRAS (Scrollable Horizontal si hay muchos proveedores)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // Líneas de rejilla horizontales de fondo
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        repeat(6) { HorizontalDivider(color = Color.White.copy(0.03f)) }
                    }

                    Row(
                        modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        ChartGroupEnhanced("Materiales", Icons.Default.Inventory2, Color(0xFF3B82F6), budgets, globalMax, selectedProviderId, { it.itemsTotal() }, ColorMaterial)
                        ChartGroupEnhanced("Mano Obra", Icons.Default.Engineering, Color(0xFFA855F7), budgets, globalMax, selectedProviderId, { it.servicesTotal() }, ColorLabor)
                        ChartGroupEnhanced("Impuestos", Icons.Default.Receipt, Color(0xFFF43F5E), budgets, globalMax, selectedProviderId, { it.taxAmount }, ColorTax)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            
            // LEYENDA DE PRESTADORES (Interactiva para enfoque y desvanecimiento)
            Text("SELECCIONAR PARA ENFOCAR:", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                budgets.forEach { budget ->
                    val isSelected = selectedProviderId == budget.providerId
                    val activeColor by animateColorAsState(if (isSelected) Color(0xFF2197F5) else Color.White.copy(0.05f))
                    
                    Surface(
                        onClick = { onSelectProvider(budget.providerId) },
                        color = activeColor,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if(isSelected) Color.White.copy(0.4f) else Color.Transparent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            AsyncImage(
                                model = budget.providerPhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = budget.providerName.split(" ").first().uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if(isSelected) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChartGroupEnhanced(
    label: String,
    icon: ImageVector,
    color: Color,
    budgets: List<BudgetEntity>,
    maxValue: Double,
    selectedId: String?,
    valueExtractor: (BudgetEntity) -> Double,
    brush: Brush
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(200.dp)
        ) {
            budgets.forEach { budget ->
                val value = valueExtractor(budget)
                val heightRatio = (value / maxValue).toFloat().coerceIn(0.01f, 1f)
                
                val isSelected = selectedId == budget.providerId
                val isAnySelected = selectedId != null
                
                // --- EFECTO DE ENFOQUE Y DESVANECIMIENTO ---
                // Si hay alguien seleccionado y no soy yo -> Me desvanezco (0.2 alpha)
                val alphaValue by animateFloatAsState(if (isAnySelected && !isSelected) 0.2f else 1f)
                // Si soy el seleccionado -> Me agrando y me pongo al frente
                val scaleValue by animateFloatAsState(if (isSelected) 1.15f else 1f)
                val zIdx = if (isSelected) 10f else 0f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.zIndex(zIdx).graphicsLayer {
                        alpha = alphaValue
                        scaleX = scaleValue
                        scaleY = scaleValue
                    }
                ) {
                    if (isSelected || !isAnySelected) {
                        Text(
                            text = "${(value/1000).toInt()}k",
                            fontSize = 8.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .fillMaxHeight(heightRatio)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(brush)
                            .then(if(isSelected) Modifier.border(1.dp, Color.White.copy(0.5f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)) else Modifier)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(label.uppercase(), fontSize = 9.sp, color = color, fontWeight = FontWeight.Black)
        }
    }
}

// =================================================================================
// --- TABLA MATRIZ CON SOPORTE PARA MÚLTIPLES PRESTADORES ---
// =================================================================================

@Composable
fun MatrixTableSection(budgets: List<BudgetEntity>) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        color = Color.White.copy(0.02f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("COMPARATIVA DE COSTOS COMPLETA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF2197F5))
            Spacer(Modifier.height(20.dp))

            // Contenedor scrollable para que entren todos los prestadores sin amontonarse
            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column {
                    // Cabecera de la Tabla
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CONCEPTO", Modifier.width(110.dp), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        budgets.forEach { budget ->
                            Text(
                                text = budget.providerName.split(" ").first().uppercase(),
                                modifier = Modifier.width(100.dp),
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color.White.copy(0.1f))

                    // Filas de Datos
                    val minItems = budgets.minOfOrNull { it.itemsTotal() } ?: 0.0
                    val minServices = budgets.minOfOrNull { it.servicesTotal() } ?: 0.0
                    val minTotal = budgets.minOfOrNull { it.grandTotal } ?: 0.0

                    MatrixPriceRow("Materiales", budgets, minItems) { it.itemsTotal() }
                    MatrixPriceRow("Mano de Obra", budgets, minServices) { it.servicesTotal() }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Fila de Total Destacada
                    MatrixPriceRow("TOTAL FINAL", budgets, minTotal, isTotal = true) { it.grandTotal }
                }
            }
        }
    }
}

@Composable
fun MatrixPriceRow(
    label: String,
    budgets: List<BudgetEntity>,
    bestValue: Double,
    isTotal: Boolean = false,
    valueExtractor: (BudgetEntity) -> Double
) {
    Row(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .then(if(isTotal) Modifier.background(Color.White.copy(0.05f), RoundedCornerShape(8.dp)).padding(vertical = 8.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            Modifier.width(110.dp),
            fontSize = if(isTotal) 10.sp else 11.sp,
            color = if(isTotal) Color.White else Color.LightGray,
            fontWeight = if(isTotal) FontWeight.ExtraBold else FontWeight.Normal
        )

        budgets.forEach { budget ->
            val value = valueExtractor(budget)
            val isBest = value == bestValue && value > 0
            
            Surface(
                modifier = Modifier.width(100.dp),
                color = if (isBest && !isTotal) SuccessGreen.copy(0.1f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "$ ${String.format(Locale.getDefault(), "%,.0f", value)}",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontSize = if(isTotal) 12.sp else 11.sp,
                    color = if (isBest) SuccessGreen else if(isTotal) Color.White else Color.Gray,
                    fontWeight = if(isBest || isTotal) FontWeight.Black else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    if (!isTotal) HorizontalDivider(color = Color.White.copy(0.03f))
}

// =================================================================================
// --- NUEVA SECCIÓN: BENCHMARK DE MERCADO (Promedios vs Líderes) ---
// =================================================================================

@Composable
fun MarketBenchmarkCard(budgets: List<BudgetEntity>) {
    if (budgets.isEmpty()) return

    // Cálculo de promedios de mercado para referencia
    val avgItems = budgets.map { it.itemsTotal() }.average()
    val avgServices = budgets.map { it.servicesTotal() }.average()
    val avgTotal = budgets.map { it.grandTotal }.average()

    // Identificación de líderes (quien ofrece el mejor valor en cada rubro)
    val bestInItems = budgets.minByOrNull { it.itemsTotal() }
    val bestInServices = budgets.minByOrNull { it.servicesTotal() }
    val bestOverall = budgets.minByOrNull { it.grandTotal }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        color = GlassPanel,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, null, tint = Color(0xFF22D3EE), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("BENCHMARK DE MERCADO", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(20.dp))

            // Mostramos los mejores candidatos comparados contra el promedio del grupo
            bestInItems?.let { BenchmarkRow("Materiales", it, avgItems, Color(0xFF3B82F6)) { b -> b.itemsTotal() } }
            bestInServices?.let { BenchmarkRow("Mano de Obra", it, avgServices, Color(0xFFA855F7)) { b -> b.servicesTotal() } }
            bestOverall?.let { BenchmarkRow("Total Final", it, avgTotal, SuccessGreen) { b -> b.grandTotal } }
        }
    }
}

@Composable
fun BenchmarkRow(
    label: String,
    winner: BudgetEntity,
    marketAvg: Double,
    accentColor: Color,
    valueExtractor: (BudgetEntity) -> Double
) {
    val winVal = valueExtractor(winner)
    val savingsPercent = if (marketAvg > 0) (((marketAvg - winVal) / marketAvg) * 100).toInt().coerceAtLeast(0) else 0

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = winner.providerPhotoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(6.dp))
                Text(winner.providerName, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("AHORRO VS PROM.", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(
                text = "-$savingsPercent%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }
    }
    HorizontalDivider(color = Color.White.copy(0.03f))
}

@Composable
fun AiInsightCard(budgets: List<BudgetEntity>) {
    val bestPrice = budgets.minByOrNull { it.grandTotal }
    if (bestPrice != null) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF581C87))))
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("ANÁLISIS MAVERICK", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF22D3EE), letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "La oferta de ${bestPrice.providerName} destaca como la más eficiente. " +
                        "Ahorras un ${String.format(Locale.getDefault(), "%.1f", (budgets.maxOf { it.grandTotal } - bestPrice.grandTotal) / budgets.maxOf { it.grandTotal } * 100)}% " +
                        "respecto a la opción más elevada.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// =================================================================================
// --- PREVIEW ---
// =================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun BudgetAnalyticsScreenPreview() {
    val tender = TenderEntity(tenderId = "T1", title = "Mantenimiento Preventivo Servidores", description = "", category = "Sistemas")
    val budgets = (1..6).map { i ->
        BudgetEntity(
            budgetId = "B$i",
            clientId = "C1",
            providerId = "P$i",
            providerName = "Tech Solutions $i",
            grandTotal = 45000.0 + (i * 7000),
            taxAmount = 4000.0,
            items = listOf(BudgetItem(description = "Insumos", quantity = 1, unitPrice = 20000.0)),
            services = listOf(BudgetService(description = "Mano de Obra", total = 21000.0 + (i * 2000)))
        )
    }

    MyApplicationTheme {
        BudgetComparisonAnalytics(tender, budgets, {})
    }
}
