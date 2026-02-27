



package com.example.myapplication.presentation.client

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.local.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.util.Locale

// --- PALETA ANALÍTICA ---
private val DarkBg = Color(0xFF020408)
private val GlassPanel = Color(0xFF161C24)
private val ColorMaterial = Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
private val ColorLabor = Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF7E22CE)))
private val ColorTax = Brush.verticalGradient(listOf(Color(0xFFF43F5E), Color(0xFFBE123C)))
private val SuccessGreen = Color(0xFF10B981)

/** Extensiones de ayuda visual */
private fun BudgetEntity.itemsTotal() = items.sumOf { it.unitPrice * it.quantity }
private fun BudgetEntity.servicesTotal() = services.sumOf { it.total }

/** Buscador de Activity para forzar rotación de pantalla */
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// =================================================================================
// --- MODELO DE DATOS VISUAL ---
// =================================================================================
data class ChartBudgetItem(
    val budget: BudgetEntity,
    val total: Double,
    val mat: Double,
    val lab: Double,
    val tax: Double,
    val isIrrisory: Boolean,
    val isOptimal: Boolean
)

// =================================================================================
// --- PANTALLA PRINCIPAL CON INYECCIÓN DE DEPENDENCIAS (MVVM) ---
// =================================================================================

@Composable
fun BudgetComparisonAnalytics(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    viewModel: BudgetViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val analyticsState by viewModel.analyticsState.collectAsStateWithLifecycle()

    LaunchedEffect(budgets) {
        viewModel.analyzeBudgets(budgets)
    }

    BudgetComparisonAnalyticsContent(
        tender = tender,
        budgets = budgets,
        state = analyticsState,
        onBack = onBack
    )
}

// =================================================================================
// --- CONTENIDO STATELESS (UI PURA Y RÁPIDA) ---
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetComparisonAnalyticsContent(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    state: AnalyticsState,
    onBack: () -> Unit
) {
    var showFullscreenLandscape by rememberSaveable { mutableStateOf(false) }

    // =============================================================================
    // SOLUCIÓN DEFINITIVA A LA BARRA DE NAVEGACIÓN Y AL CUADRADO DE MOTOROLA
    // =============================================================================
    // Al usar un Dialog, se dibuja POR ENCIMA de la barra inferior de AppNavigation.
    // Con los hacks de WindowManager, destruimos las limitaciones de tamaño de Motorola.
    if (showFullscreenLandscape) {
        Dialog(
            onDismissRequest = { showFullscreenLandscape = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = true
            )
        ) {
            val context = LocalContext.current
            val dialogView = LocalView.current

            DisposableEffect(Unit) {
                val activity = context.findActivity()
                val dialogWindow = (dialogView.parent as? DialogWindowProvider)?.window

                dialogWindow?.let { window ->
                    // 1. FORZAR TAMAÑO COMPLETO (Mata el cuadrado flotante de Motorola/Samsung)
                    window.setLayout(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // 2. Fondo transparente para evitar parpadeos blancos
                    window.setBackgroundDrawableResource(android.R.color.transparent)

                    // 3. Ocultar Barra de Estado (Top) y Barra de Navegación del Sistema (Botones Abajo)
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    val insetsController = WindowCompat.getInsetsController(window, dialogView)
                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                }

                // 4. Forzar rotación
                val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                onDispose {
                    activity?.requestedOrientation = originalOrientation
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                FullscreenLandscapeChart(
                    state = state,
                    onClose = { showFullscreenLandscape = false },
                    onViewBudget = { _ ->
                        // TODO: Navegar a vista de presupuesto detallado
                    }
                )
            }
        }
    } else {
        // MODO VERTICAL NORMAL
        Scaffold(
            containerColor = DarkBg,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ANÁLISIS DE MERCADO", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
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
            if (state.isAnalyzing) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2197F5))
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AnalyticsKpiCard(Modifier.weight(1f), "PROMEDIO MERCADO", state.avgTotal, Color.White)
                        AnalyticsKpiCard(Modifier.weight(1f), "MEJOR OFERTA", state.minPrice, SuccessGreen)
                    }

                    MassiveStackedBarChartCard(
                        budgetCount = state.items.size,
                        budgets = budgets,
                        onMaximizeClick = { showFullscreenLandscape = true }
                    )

                    MatrixTableSection(budgets)
                    MarketBenchmarkCard(budgets)
                    AiInsightCard(budgets)
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
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

@Composable
fun MassiveStackedBarChartCard(budgetCount: Int, budgets: List<BudgetEntity>, onMaximizeClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp), color = GlassPanel,
        shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Insights, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CURVA DE PRECIOS ($budgetCount)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
                }
                IconButton(onClick = onMaximizeClick, modifier = Modifier.size(32.dp).background(Color.White.copy(0.1f), RoundedCornerShape(8.dp))) {
                    Icon(Icons.Default.Fullscreen, "Pantalla Completa", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                if (budgets.isEmpty()) {
                    Text("Sin datos", color = Color.Gray)
                } else {
                    MiniatureStackedChart(budgets)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Toca el icono de maximizar para análisis interactivo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MiniatureStackedChart(budgets: List<BudgetEntity>) {
    val rawMaxTotal = budgets.maxOfOrNull { it.grandTotal }?.toFloat() ?: 0f
    val maxTotal = if (rawMaxTotal > 0f) rawMaxTotal else 1f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = size.width / budgets.size.coerceAtLeast(1)
        val gap = barWidth * 0.2f
        val actualWidth = barWidth - gap

        budgets.forEachIndexed { index, budget ->
            val total = budget.grandTotal.toFloat()
            val mat = budget.itemsTotal().toFloat()
            val lab = budget.servicesTotal().toFloat()
            val tax = budget.taxAmount.toFloat()

            val alpha = if (total !in 15000f..200000f) 0.3f else 1f

            val hTotal = (total / maxTotal) * size.height
            val hMat = if (total > 0f) (mat / total) * hTotal else 0f
            val hLab = if (total > 0f) (lab / total) * hTotal else 0f
            val hTax = if (total > 0f) (tax / total) * hTotal else 0f

            val x = index * barWidth + gap / 2

            val yTax = size.height - hTotal
            drawRect(Color(0xFFF43F5E), Offset(x, yTax), Size(actualWidth, hTax), alpha = alpha)

            val yLab = yTax + hTax
            drawRect(Color(0xFFA855F7), Offset(x, yLab), Size(actualWidth, hLab), alpha = alpha)

            val yMat = yLab + hLab
            drawRect(Color(0xFF3B82F6), Offset(x, yMat), Size(actualWidth, hMat), alpha = alpha)
        }
    }
}

// =================================================================================
// --- PANTALLA COMPLETA APAISADA (MODO INMERSIVO TÁCTIL V5) ---
// =================================================================================

@Composable
fun FullscreenLandscapeChart(
    state: AnalyticsState,
    onClose: () -> Unit,
    onViewBudget: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isolationMode by remember { mutableStateOf("total") }
    var isOptimalSorted by remember { mutableStateOf(false) }
    var selectedBudgetId by remember { mutableStateOf<String?>(null) }

    val displayItems = remember(state.items, isOptimalSorted) {
        if (isOptimalSorted) {
            state.items.sortedWith(compareBy({ it.isIrrisory }, { !it.isOptimal }, { it.total }))
        } else {
            state.items
        }
    }

    val validDisplayVals = displayItems.filter { !it.isIrrisory }.map {
        when(isolationMode) {
            "mat" -> it.mat
            "lab" -> it.lab
            "tax" -> it.tax
            else -> it.total
        }
    }

    val rawMax = validDisplayVals.maxOrNull() ?: 0.0
    val vMax = if (rawMax > 0.0) rawMax else 1.0
    val vMin = validDisplayVals.minOrNull() ?: 0.0
    val vAvg = if(validDisplayVals.isNotEmpty()) validDisplayVals.average() else 0.0
    val visualMax = vMax * 1.15

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { selectedBudgetId = null },
        color = DarkBg
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF2197F5).copy(0.08f), Color.Transparent), center = Offset(500f, 1500f), radius = 1500f)))

        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.displayCutout)) {

            // --- TOP HUD COMPACTO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF05070A).copy(0.85f))
                    .border(1.dp, Color.White.copy(0.08f))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("ANALIZANDO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${state.items.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(0.1f)))
                    Column {
                        Text("PROMEDIO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF2197F5), letterSpacing = 1.sp)
                        Text("$ ${(state.avgTotal/1000).toInt()}k", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF2197F5))
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(0.1f)))
                    Column {
                        Text("IRRISORIOS", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFFF43F5E), letterSpacing = 1.sp)
                        Text("${state.items.size - state.validCount}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFF43F5E))
                    }

                    Surface(
                        onClick = {
                            isOptimalSorted = !isOptimalSorted
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOptimalSorted) SuccessGreen else SuccessGreen.copy(0.1f),
                        border = BorderStroke(1.dp, SuccessGreen.copy(0.4f)),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrackChanges, null, tint = if (isOptimalSorted) DarkBg else SuccessGreen, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isOptimalSorted) "VIENDO ÓPTIMOS" else "AGRUPAR ÓPTIMOS", fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (isOptimalSorted) DarkBg else SuccessGreen)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("VISTA:", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Gray)

                    IsolationButton("Total", Icons.Default.Layers, Color.White, isolationMode == "total") { isolationMode = "total" }
                    IsolationButton("Mat", Icons.Default.Square, Color(0xFF3B82F6), isolationMode == "mat") { isolationMode = "mat" }
                    IsolationButton("Obra", Icons.Default.Square, Color(0xFFA855F7), isolationMode == "lab") { isolationMode = "lab" }
                    IsolationButton("Tasa", Icons.Default.Square, Color(0xFFF43F5E), isolationMode == "tax") { isolationMode = "tax" }

                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))) {
                        Icon(Icons.Default.CloseFullscreen, null, tint = Color.Gray)
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                val hPx = constraints.maxHeight.toFloat()

                val animMax by animateFloatAsState(targetValue = (vMax / visualMax).toFloat(), animationSpec = tween(600), label = "max")
                val animAvg by animateFloatAsState(targetValue = (vAvg / visualMax).toFloat(), animationSpec = tween(600), label = "avg")
                val animMin by animateFloatAsState(targetValue = (vMin / visualMax).toFloat(), animationSpec = tween(600), label = "min")

                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, top = 20.dp)) {
                    val w = size.width
                    val h = size.height
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    val yMax = h - (h * animMax)
                    drawLine(Color(0xFFF43F5E).copy(0.5f), Offset(0f, yMax), Offset(w, yMax), strokeWidth = 2f, pathEffect = pathEffect)

                    val yAvg = h - (h * animAvg)
                    drawLine(Color(0xFF2197F5).copy(0.5f), Offset(0f, yAvg), Offset(w, yAvg), strokeWidth = 2f, pathEffect = pathEffect)

                    val yMin = h - (h * animMin)
                    drawLine(Color(0xFF10B981).copy(0.5f), Offset(0f, yMin), Offset(w, yMin), strokeWidth = 2f, pathEffect = pathEffect)
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, top = 20.dp)) {
                    PeakLabel("Pico Máx: $${(vMax/1000).toInt()}k", Color(0xFFF43F5E), animMax, this.maxHeight)
                    PeakLabel("Promedio: $${(vAvg/1000).toInt()}k", Color(0xFF2197F5), animAvg, this.maxHeight)
                    PeakLabel("Pico Mín: $${(vMin/1000).toInt()}k", Color(0xFF10B981), animMin, this.maxHeight)
                }

                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(bottom = 10.dp, top = 20.dp),
                    contentPadding = PaddingValues(horizontal = 60.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    items(displayItems, key = { it.budget.budgetId }) { item ->
                        SuperimposedBarItem(
                            item = item,
                            mode = isolationMode,
                            visualMax = visualMax,
                            isSelected = selectedBudgetId == item.budget.budgetId,
                            onSelect = { selectedBudgetId = item.budget.budgetId },
                            onViewBudget = { onViewBudget(item.budget.budgetId) },
                            maxHeightPx = hPx
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IsolationButton(label: String, icon: ImageVector, color: Color, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isActive) color.copy(alpha = 0.15f) else Color.White.copy(0.05f),
        border = BorderStroke(1.dp, if (isActive) color else Color.White.copy(0.1f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text(label.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun BoxScope.PeakLabel(text: String, color: Color, percentage: Float, boxHeight: Dp) {
    val alphaAnim by animateFloatAsState(targetValue = 1f, animationSpec = tween(600), label = "alpha")
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset {
                IntOffset(
                    x = 10.dp.roundToPx(),
                    y = -((percentage * boxHeight.toPx()).toInt())
                )
            }
            .graphicsLayer { alpha = alphaAnim }
            .offset(y = (-4).dp)
            .background(Color(0xFF020408).copy(0.8f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

/**
 * BARRA SUPERPUESTA TIPO MATRYOSHKA Y POPUP FLOTANTE
 */
@Composable
fun SuperimposedBarItem(
    item: ChartBudgetItem,
    mode: String,
    visualMax: Double,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onViewBudget: () -> Unit,
    maxHeightPx: Float
) {
    val totalHeightRatio = (item.total / visualMax).toFloat().coerceIn(0f, 1f)
    val matHeightRatio = (item.mat / visualMax).toFloat().coerceIn(0f, 1f)
    val labHeightRatio = (item.lab / visualMax).toFloat().coerceIn(0f, 1f)
    val taxHeightRatio = (item.tax / visualMax).toFloat().coerceIn(0f, 1f)

    val hTotal by animateFloatAsState(if(mode == "total") totalHeightRatio else 0f, tween(500), label = "hTotal")
    val hMat by animateFloatAsState(if(mode == "total" || mode == "mat") matHeightRatio else 0f, tween(500), label = "hMat")
    val hLab by animateFloatAsState(if(mode == "total" || mode == "lab") labHeightRatio else 0f, tween(500), label = "hLab")
    val hTax by animateFloatAsState(if(mode == "total" || mode == "tax") taxHeightRatio else 0f, tween(500), label = "hTax")

    val wMat by animateDpAsState(if(mode == "mat") 32.dp else 22.dp, label = "wMat")
    val alphaMat by animateFloatAsState(if(mode == "total" || mode == "mat") 1f else 0f, label = "aMat")

    val wLab by animateDpAsState(if(mode == "lab") 32.dp else 14.dp, label = "wLab")
    val alphaLab by animateFloatAsState(if(mode == "total" || mode == "lab") 1f else 0f, label = "aLab")

    val wTax by animateDpAsState(if(mode == "tax") 32.dp else 6.dp, label = "wTax")
    val alphaTax by animateFloatAsState(if(mode == "total" || mode == "tax") 1f else 0f, label = "aTax")

    Box(
        modifier = Modifier
            .width(52.dp)
            .fillMaxHeight()
            .background(if (item.isOptimal) SuccessGreen.copy(alpha = 0.08f) else Color.Transparent)
            .border(width = if (item.isOptimal) 1.dp else 0.dp, color = if (item.isOptimal) SuccessGreen.copy(0.2f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSelect() }
            .zIndex(if (isSelected) 100f else 0f),
        contentAlignment = Alignment.BottomCenter
    ) {
        val fallbackAvatar = rememberVectorPainter(Icons.Default.Person)
        val avatarScale by animateFloatAsState(if(isSelected) 1.3f else 1f, label = "avatarScale")
        val avatarColor = if(isSelected) Color(0xFF2197F5) else Color.White.copy(0.1f)

        Box(modifier = Modifier.padding(bottom = 5.dp).zIndex(20f)) {
            AsyncImage(
                model = item.budget.providerPhotoUrl,
                contentDescription = null,
                fallback = fallbackAvatar,
                error = fallbackAvatar,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { scaleX = avatarScale; scaleY = avatarScale }
                    .clip(CircleShape)
                    .border(2.dp, avatarColor, CircleShape)
                    .background(Color(0xFF161C24)),
                contentScale = ContentScale.Crop
            )
            if (item.isIrrisory) {
                Text("⚠️", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-18).dp))
            }
        }

        val barAlpha = if (item.isIrrisory) 0.3f else if (isSelected) 1f else 0.8f

        // ZONA DE BARRAS
        Box(modifier = Modifier.padding(bottom = 35.dp).fillMaxSize().alpha(barAlpha), contentAlignment = Alignment.BottomCenter) {
            Box(modifier = Modifier.width(32.dp).fillMaxHeight(hTotal).background(if(isSelected) Color.White.copy(0.15f) else Color.White.copy(0.03f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).zIndex(1f))
            Box(modifier = Modifier.width(wMat).fillMaxHeight(hMat).alpha(alphaMat).background(ColorMaterial, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).shadow(10.dp).zIndex(if(mode == "mat") 10f else 2f))
            Box(modifier = Modifier.width(wLab).fillMaxHeight(hLab).alpha(alphaLab).background(ColorLabor, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).shadow(10.dp).zIndex(if(mode == "lab") 10f else 3f))
            Box(modifier = Modifier.width(wTax).fillMaxHeight(hTax).alpha(alphaTax).background(ColorTax, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).shadow(10.dp).zIndex(if(mode == "tax") 10f else 4f))
        }

        // SOLUCIÓN A POPUP CORTADO: Control Matemático de Límites
        if (isSelected) {
            val density = LocalDensity.current

            val bottomPaddingPx = with(density) { 35.dp.toPx() } // Espacio donde está el avatar
            val extraHoverPx = with(density) { 20.dp.toPx() } // Distancia de flotación sobre la barra
            val popupEstimatedHeightPx = with(density) { 200.dp.toPx() } // Alto estimado del popup

            // Calculamos la posición natural del popup (justo arriba de la barra)
            val projectedY = -((hTotal * (maxHeightPx - bottomPaddingPx)) + bottomPaddingPx + extraHoverPx)

            // Límite de seguridad superior de la pantalla.
            val maxSafeY = -(maxHeightPx - popupEstimatedHeightPx)

            // coerceAtLeast evita que el popup se escape por el borde superior de la pantalla.
            val topOfBarYOffset = projectedY.toFloat().coerceAtLeast(maxSafeY).toInt()

            Popup(
                alignment = Alignment.BottomCenter,
                offset = IntOffset(0, topOfBarYOffset),
                properties = PopupProperties(
                    clippingEnabled = false,
                    excludeFromSystemGesture = true
                )
            ) {
                var animateIn by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { animateIn = true }

                AnimatedVisibility(
                    visible = animateIn,
                    enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(0.5f, 1f)),
                    exit = fadeOut()
                ) {
                    TouchPopupDetail(item, onViewBudget)
                }
            }
        }
    }
}

@Composable
fun TouchPopupDetail(item: ChartBudgetItem, onViewBudget: () -> Unit) {
    Surface(
        modifier = Modifier.width(170.dp),
        color = Color(0xFF0A0E14).copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF2197F5).copy(0.4f)),
        shadowElevation = 25.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Color.White.copy(0.1f)), RoundedCornerShape(6.dp)).padding(bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Final:", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("$${(item.total/1000).toInt()}k", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(4.dp))
            PopupRow("Material:", "$${(item.mat/1000).toInt()}k", Color(0xFF3B82F6))
            PopupRow("M. Obra:", "$${(item.lab/1000).toInt()}k", Color(0xFFA855F7))
            PopupRow("Tasas:", "$${(item.tax/1000).toInt()}k", Color(0xFFF43F5E))

            Spacer(Modifier.height(6.dp))
            if (item.isOptimal) {
                Text("ZONA ÓPTIMA", modifier = Modifier.fillMaxWidth().background(SuccessGreen.copy(0.1f), RoundedCornerShape(4.dp)).border(1.dp, SuccessGreen.copy(0.2f), RoundedCornerShape(4.dp)).padding(vertical = 2.dp), textAlign = TextAlign.Center, color = SuccessGreen, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            if (item.isIrrisory) {
                Text("⚠️ ANOMALÍA", modifier = Modifier.fillMaxWidth().background(Color.Red.copy(0.1f), RoundedCornerShape(4.dp)).border(1.dp, Color.Red.copy(0.2f), RoundedCornerShape(4.dp)).padding(vertical = 2.dp), textAlign = TextAlign.Center, color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(0.1f))
            Spacer(Modifier.height(6.dp))
            Text(item.budget.providerName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(item.budget.providerCompanyName ?: "Profesional Independiente", color = Color(0xFF22D3EE), fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onViewBudget,
                modifier = Modifier.fillMaxWidth().height(32.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("VER PRESUPUESTO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 0.5.sp)
            }
        }
    }
}

@Composable
fun PopupRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = color, fontSize = 8.sp)
        Text(value, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

// =================================================================================
// --- COMPONENTES TABULARES ---
// =================================================================================

@Composable
fun MatrixTableSection(budgets: List<BudgetEntity>) {
    Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), color = Color.White.copy(0.02f), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color.White.copy(0.05f))) {
        Column(Modifier.padding(20.dp)) {
            Text("COMPARATIVA TÉCNICA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF2197F5))
            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CONCEPTO", Modifier.width(110.dp), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        budgets.forEach { budget ->
                            Text(budget.providerName.split(" ").first().uppercase(), modifier = Modifier.width(90.dp), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color.White.copy(0.1f))

                    val minItems = budgets.minOfOrNull { it.itemsTotal() } ?: 0.0
                    val minServices = budgets.minOfOrNull { it.servicesTotal() } ?: 0.0
                    val minTotal = budgets.minOfOrNull { it.grandTotal } ?: 0.0

                    MatrixPriceRow("Materiales", budgets, minItems) { it.itemsTotal() }
                    MatrixPriceRow("Mano de Obra", budgets, minServices) { it.servicesTotal() }

                    Spacer(Modifier.height(12.dp))
                    Surface(color = Color(0xFF2197F5).copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                        MatrixPriceRow("TOTAL FINAL", budgets, minTotal, isTotal = true) { it.grandTotal }
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixPriceRow(label: String, budgets: List<BudgetEntity>, bestValue: Double, isTotal: Boolean = false, valueExtractor: (BudgetEntity) -> Double) {
    Row(modifier = Modifier.padding(vertical = 6.dp).then(if(isTotal) Modifier.padding(vertical = 8.dp) else Modifier), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(110.dp).padding(start = if (isTotal) 8.dp else 0.dp), fontSize = if(isTotal) 10.sp else 11.sp, color = if(isTotal) Color.White else Color.LightGray, fontWeight = if(isTotal) FontWeight.ExtraBold else FontWeight.Normal)
        budgets.forEach { budget ->
            val value = valueExtractor(budget)
            val isBest = value == bestValue && value > 0
            Surface(modifier = Modifier.width(90.dp), color = if (isBest && !isTotal) SuccessGreen.copy(0.1f) else Color.Transparent, shape = RoundedCornerShape(6.dp)) {
                Text("$ ${String.format(Locale.getDefault(), "%,.0f", value)}", modifier = Modifier.padding(vertical = 4.dp), fontSize = if(isTotal) 12.sp else 11.sp, color = if (isBest) SuccessGreen else if(isTotal) Color.White else Color.Gray, fontWeight = if(isBest || isTotal) FontWeight.Black else FontWeight.Normal, textAlign = TextAlign.Center)
            }
        }
    }
    if (!isTotal) HorizontalDivider(color = Color.White.copy(0.03f))
}

@Composable
fun MarketBenchmarkCard(budgets: List<BudgetEntity>) {
    if (budgets.isEmpty()) return
    val avgItems = budgets.map { it.itemsTotal() }.average()
    val avgServices = budgets.map { it.servicesTotal() }.average()
    val bestInItems = budgets.minByOrNull { it.itemsTotal() }
    val bestInServices = budgets.minByOrNull { it.servicesTotal() }

    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), color = GlassPanel, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color.White.copy(0.08f))) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, null, tint = Color(0xFF22D3EE), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("LÍDERES DE COSTOS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(20.dp))
            bestInItems?.let { BenchmarkRow("Materiales", it, avgItems, Color(0xFF3B82F6)) { b -> b.itemsTotal() } }
            bestInServices?.let { BenchmarkRow("Mano de Obra", it, avgServices, Color(0xFFA855F7)) { b -> b.servicesTotal() } }
        }
    }
}

@Composable
fun BenchmarkRow(label: String, winner: BudgetEntity, marketAvg: Double, accentColor: Color, valueExtractor: (BudgetEntity) -> Double) {
    val winVal = valueExtractor(winner)
    val savingsPercent = if (marketAvg > 0) (((marketAvg - winVal) / marketAvg) * 100).toInt().coerceAtLeast(0) else 0

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                AsyncImage(model = winner.providerPhotoUrl, contentDescription = null, fallback = rememberVectorPainter(Icons.Default.Person), error = rememberVectorPainter(Icons.Default.Person), modifier = Modifier.size(16.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(6.dp))
                Text(winner.providerName, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("VS PROMEDIO", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text("-$savingsPercent%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = accentColor)
        }
    }
    HorizontalDivider(color = Color.White.copy(0.03f))
}

@Composable
fun AiInsightCard(budgets: List<BudgetEntity>) {
    val bestPrice = budgets.minByOrNull { it.grandTotal }
    if (bestPrice != null) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), color = Color.Transparent, border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF581C87))))) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("MAVERICK AI INSIGHT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF22D3EE), letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("La oferta de ${bestPrice.providerName} lidera el mercado con un ahorro del ${String.format(Locale.getDefault(), "%.1f", (budgets.maxOf { it.grandTotal } - bestPrice.grandTotal) / budgets.maxOf { it.grandTotal } * 100)}%.", fontSize = 11.sp, color = Color.LightGray, lineHeight = 18.sp)
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

    val mockState = AnalyticsState(
        items = emptyList(),
        avgTotal = 50000.0,
        minPrice = 45000.0,
        maxPrice = 87000.0,
        validCount = 6,
        isAnalyzing = false
    )

    MyApplicationTheme {
        BudgetComparisonAnalyticsContent(tender, budgets, mockState) {}
    }
}

