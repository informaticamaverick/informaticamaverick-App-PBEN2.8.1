package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.local.*
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// =================================================================================
// --- CONSTANTES Y ESTILOS VISUALES ---
// =================================================================================

private val DarkBackground = Color(0xFF05070A)
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val MaverickPurple = Color(0xFF9B51E0)
private val StatusActive = Color(0xFF38BDF8)
private val StatusFinished = Color(0xFF34D399)
private val StatusWarning = Color(0xFFF87171)
private val NeonCyber = Color(0xFF00FFC2)

// =================================================================================
// --- PANTALLA PRINCIPAL ---
// =================================================================================

@Composable
fun PresupuestosScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    val tenders by viewModel.tenders.collectAsStateWithLifecycle()
    val directBudgets by viewModel.directBudgets.collectAsStateWithLifecycle()

    PresupuestosScreenContent(
        tenders = tenders,
        directBudgets = directBudgets,
        getBudgetsForTender = { tenderId -> viewModel.getBudgetsForTender(tenderId) },
        onChatClick = onChatClick,
        onBack = onBack,
        onAcceptBudget = { budget -> viewModel.acceptBudget(budget) },
        onRejectBudget = { budget -> viewModel.rejectBudget(budget) },

        //onAcceptBudget = { budget -> viewModel.acceptBudget(budget.budgetId) },
        //onRejectBudget = { budget -> viewModel.rejectBudget(budget.budgetId) },
        bottomPadding = bottomPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreenContent(
    tenders: List<TenderEntity>,
    directBudgets: List<BudgetEntity>,
    getBudgetsForTender: (String) -> StateFlow<List<BudgetEntity>>,
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    onAcceptBudget: (BudgetEntity) -> Unit,
    onRejectBudget: (BudgetEntity) -> Unit,
    bottomPadding: PaddingValues
) {
    // --- ESTADOS NAVEGACIÓN ---
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("LICITACIONES", "DIRECTOS")
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isFabExpanded by remember { mutableStateOf(false) }

    // --- ESTADOS PANEL TÁCTICO (NUEVO) ---
    var activeFilters by remember { mutableStateOf(setOf<String>()) }
    var multiSelectEnabled by remember { mutableStateOf(false) }
    val selectedTenderIds = remember { mutableStateListOf<String>() }
    val selectedBudgetIds = remember { mutableStateListOf<String>() }

    var selectedTenderForSheet by remember { mutableStateOf<TenderEntity?>(null) }
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }

    // 🔥 LÓGICA DE CATEGORÍAS CONTEXTUALES (Lee los datos en pantalla para generar los chips)
    val dynamicCategoriesForPanel = remember(tenders, directBudgets, selectedTabIndex) {
        val extractedNames = mutableSetOf<String>()

        if (selectedTabIndex == 0) {
            // Contexto Licitaciones: Extraemos los rubros de las licitaciones activas
            extractedNames.addAll(tenders.map { it.category })
        } else {
            // Contexto Directos: Simulamos extracción (En la vida real vendría del Provider)
            if (directBudgets.isNotEmpty()) {
                extractedNames.addAll(listOf("Informática", "Electricidad", "Plomería"))
            }
        }

        // Mapeamos a ControlItem para el Panel Táctico
        extractedNames.map { catName ->
            ControlItem(
                label = catName,
                icon = null,
                emoji = getCategoryEmoji(catName),
                color = getCategoryColor(catName),
                id = "cat_${catName.lowercase()}"
            )
        }
    }

    // --- LÓGICA DE FILTRADO UNIFICADA ---
    val filteredTenders = remember(tenders, activeFilters, searchQuery) {
        val selectedCats = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
        tenders.filter { tender ->
            val matchesCategory = selectedCats.isEmpty() || selectedCats.contains(tender.category.lowercase())
            val matchesSearch = searchQuery.isEmpty() || tender.title.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }.sortedWith(
            if (activeFilters.contains("fecha")) compareByDescending { it.dateTimestamp } else compareByDescending { it.dateTimestamp }
        )
    }

    val budgetFilterLambda: (List<BudgetEntity>) -> List<BudgetEntity> = remember(activeFilters, searchQuery) {
        { list ->
            val sortByPrice = activeFilters.contains("precio")
            list.filter { budget ->
                val matchesSearch = searchQuery.isEmpty() || budget.providerName.contains(searchQuery, ignoreCase = true)
                matchesSearch
            }.sortedWith(
                if (sortByPrice) compareBy { it.grandTotal } else compareByDescending { it.dateTimestamp }
            )
        }
    }

    val displayedDirectBudgets = remember(directBudgets, budgetFilterLambda) { budgetFilterLambda(directBudgets) }

    val cancelSelection = {
        selectedTenderIds.clear()
        selectedBudgetIds.clear()
        multiSelectEnabled = false
        isFabExpanded = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                if (!isSearchActive) {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Gestión Comercial", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                                Text("ADMINISTRADOR DE COTIZACIONES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {

                // --- CONTENIDO ---
                Column(modifier = Modifier.fillMaxSize()) {
                    CommercialStatsDashboard(
                        activeCount = if (selectedTabIndex == 0) filteredTenders.size else displayedDirectBudgets.size,
                        offerCount = tenders.sumOf { it.budgetCount },
                        savings = 15,
                        contextLabel = if (selectedTabIndex == 0) "LICITACIONES" else "PRESUPUESTOS"
                    )

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaverickBlue,
                        indicator = { positions ->
                            if (selectedTabIndex < positions.size) {
                                TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[selectedTabIndex]), color = MaverickBlue)
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index; cancelSelection() },
                                text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTabIndex) {
                            0 -> LicitacionesTabContent(
                                tenders = filteredTenders,
                                getBudgetsForTender = getBudgetsForTender,
                                selectedIds = selectedTenderIds,
                                onTenderClick = { tender ->
                                    if (multiSelectEnabled) {
                                        if (selectedTenderIds.contains(tender.tenderId)) selectedTenderIds.remove(tender.tenderId)
                                        else selectedTenderIds.add(tender.tenderId)
                                        if (selectedTenderIds.isEmpty()) multiSelectEnabled = false
                                    } else {
                                        selectedTenderForSheet = tender
                                    }
                                },
                                onTenderLongClick = { tender ->
                                    multiSelectEnabled = true
                                    if (!selectedTenderIds.contains(tender.tenderId)) selectedTenderIds.add(tender.tenderId)
                                    isFabExpanded = true
                                }
                            )
                            1 -> DirectBudgetsTabContent(
                                budgets = displayedDirectBudgets,
                                selectedIds = selectedBudgetIds,
                                onBudgetClick = { budget ->
                                    if (multiSelectEnabled) {
                                        if (selectedBudgetIds.contains(budget.budgetId)) selectedBudgetIds.remove(budget.budgetId)
                                        else selectedBudgetIds.add(budget.budgetId)
                                        if (selectedBudgetIds.isEmpty()) multiSelectEnabled = false
                                    } else {
                                        budgetForA4Preview = budget
                                    }
                                },
                                onChatClick = onChatClick,
                                onBudgetLongClick = { budget ->
                                    multiSelectEnabled = true
                                    if (!selectedBudgetIds.contains(budget.budgetId)) selectedBudgetIds.add(budget.budgetId)
                                    isFabExpanded = true
                                }
                            )
                        }
                    }
                }

                // --- BÚSQUEDA ---
                if (isSearchActive) {
                    Box(modifier = Modifier.fillMaxSize().zIndex(10f).background(Color.Black.copy(alpha = 0.6f)).clickable { isSearchActive = false })
                    Column(modifier = Modifier.fillMaxSize().zIndex(11f)) {
                        AnimatedVisibility(visible = isSearchActive, enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut()) {
                            Row(modifier = Modifier.fillMaxWidth().background(DarkBackground).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    GeminiTopSearchBar(
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { searchQuery = it },
                                        placeholderText = if(selectedTabIndex == 0) "Buscar licitaciones..." else "Buscar presupuestos..."
                                    )
                                }
                                Surface(onClick = { isSearchActive = false; searchQuery = "" }, modifier = Modifier.size(56.dp), shape = CircleShape, color = CardSurface, border = BorderStroke(1.dp, MaverickBlue.copy(alpha = 0.5f))) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, null, tint = Color.White) }
                                }
                            }
                        }
                        if (searchQuery.isNotEmpty()) {
                            PresupuestoSearchResultsPanel(
                                searchQuery = searchQuery,
                                tenders = if(selectedTabIndex == 0) tenders else emptyList(),
                                budgets = if(selectedTabIndex == 1) directBudgets else emptyList(),
                                onTenderClick = { selectedTenderForSheet = it; isSearchActive = false },
                                onBudgetClick = { budgetForA4Preview = it; isSearchActive = false }
                            )
                        }
                    }
                }
            }
        }

        // --- OVERLAY: COMPARATIVA (Ofertas de la Licitación) ---
        AnimatedVisibility(
            visible = selectedTenderForSheet != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.zIndex(40f)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedTenderForSheet = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text("Ofertas Recibidas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    if (selectedTenderForSheet != null) {
                        val budgetsFlow = remember(selectedTenderForSheet) { getBudgetsForTender(selectedTenderForSheet!!.tenderId) }
                        val rawBudgets by budgetsFlow.collectAsStateWithLifecycle()
                        // Aplicamos los filtros activos (precio, etc) a las ofertas
                        val filteredBudgets = remember(rawBudgets, budgetFilterLambda) { budgetFilterLambda(rawBudgets) }

                        ComparisonSheet(
                            tender = selectedTenderForSheet!!,
                            budgets = filteredBudgets,
                            onBudgetClick = { budget -> budgetForA4Preview = budget },
                            onChatClick = onChatClick
                        )
                    }
                }
            }
        }

        // --- FAB GEMINI TÁCTICO V2 (Z-INDEX 100) ---
        Box(modifier = Modifier.fillMaxSize().zIndex(100f).padding(bottom = bottomPadding.calculateBottomPadding())) {
            GeminiFABWithScrim(bottomPadding = PaddingValues(0.dp), showScrim = isFabExpanded) {
                GeminiSplitFAB(
                    isExpanded = isFabExpanded,
                    isSearchActive = isSearchActive,
                    onToggleExpand = { isFabExpanded = !isFabExpanded },
                    onActivateSearch = { isSearchActive = true; isFabExpanded = false },
                    onCloseSearch = { isSearchActive = false; searchQuery = "" },
                    activeFilters = activeFilters,
                    dynamicCategories = dynamicCategoriesForPanel, // ¡Inyectamos las categorías contextuales!
                    onAction = { actionId ->
                        when (actionId) {
                            "sim_chat", "sim_lic", "refresh", "vista", "config" -> {
                                isFabExpanded = false
                            }
                            else -> {
                                // Toggle filtro
                                activeFilters = if (activeFilters.contains(actionId)) activeFilters - actionId else activeFilters + actionId
                            }
                        }
                    },
                    onResetAll = { activeFilters = emptySet() }
                )
            }
        }
    }

    if (budgetForA4Preview != null) {
        Dialog(onDismissRequest = { budgetForA4Preview = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            BudgetMultiPageScreen(
                budget = budgetForA4Preview!!,
                onBack = { budgetForA4Preview = null },
                onAccept = { _ -> onAcceptBudget(budgetForA4Preview!!); budgetForA4Preview = null },
                onReject = { _ -> onRejectBudget(budgetForA4Preview!!); budgetForA4Preview = null }
            )
        }
    }
}

// =================================================================================
// --- COMPONENTES DE UI ---
// =================================================================================

@Composable
fun CommercialStatsDashboard(activeCount: Int, offerCount: Int, savings: Int, contextLabel: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(Modifier.weight(1f), "$contextLabel ACTIVAS", activeCount.toString(), Color.White)
        StatCard(Modifier.weight(1f), "RECIBIDOS", offerCount.toString(), MaverickPurple)
        StatCard(Modifier.weight(1f), "AHORRO EST.", "$savings%", StatusFinished)
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, color: Color) {
    Surface(modifier = modifier, color = Color.White.copy(0.03f), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color.White.copy(0.06f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LicitacionesTabContent(
    tenders: List<TenderEntity>,
    getBudgetsForTender: (String) -> StateFlow<List<BudgetEntity>>,
    selectedIds: List<String>,
    onTenderClick: (TenderEntity) -> Unit,
    onTenderLongClick: (TenderEntity) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        items(tenders, key = { it.tenderId }) { tender ->
            val budgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
            val budgets by budgetsFlow.collectAsStateWithLifecycle()
            val isNew = budgets.any { it.status == BudgetStatus.PENDIENTE }
            LicitacionFolderCard(
                tender = tender,
                budgetCount = budgets.size,
                isNew = isNew,
                isSelected = selectedIds.contains(tender.tenderId),
                onClick = { onTenderClick(tender) },
                onLongClick = { onTenderLongClick(tender) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DirectBudgetsTabContent(
    budgets: List<BudgetEntity>,
    selectedIds: List<String>,
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String) -> Unit,
    onBudgetLongClick: (BudgetEntity) -> Unit
) {
    if (budgets.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay presupuestos que coincidan.", color = Color.Gray) }
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
            items(budgets, key = { it.budgetId }) { budget ->
                val isNew = budget.status == BudgetStatus.PENDIENTE
                BudgetComparisonItem(
                    budget = budget,
                    isBestPrice = false,
                    isNew = isNew,
                    isSelected = selectedIds.contains(budget.budgetId),
                    onView = { onBudgetClick(budget) },
                    onChat = { onChatClick(budget.providerId) },
                    onLongClick = { onBudgetLongClick(budget) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LicitacionFolderCard(
    tender: TenderEntity,
    budgetCount: Int,
    isNew: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val endDate = if (tender.endDate > 0) tender.endDate else tender.dateTimestamp + (86400000L * 7)
    val remainingDays = TimeUnit.MILLISECONDS.toDays(endDate - System.currentTimeMillis()).coerceAtLeast(0)
    val borderColor = if (isSelected) MaverickBlue else if (isNew) NeonCyber else Color.White.copy(0.1f)

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .padding(top = 20.dp)
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Surface(modifier = Modifier.offset(x = 0.dp, y = (-24).dp).width(110.dp).height(30.dp), color = Color.White.copy(0.06f), shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp), border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))) {
            Box(contentAlignment = Alignment.Center) { Text("#${tender.tenderId.takeLast(6).uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = if(isSelected) MaverickBlue else if(isNew) NeonCyber else MaverickBlue) }
        }
        Surface(modifier = Modifier.fillMaxWidth(), color = CardSurface, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp), border = BorderStroke(if(isSelected) 2.dp else 1.dp, borderColor), shadowElevation = if (isNew || isSelected) 15.dp else 4.dp) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tender.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaverickBlue)
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(" ${formatDateShort(tender.dateTimestamp)} - ${formatDateShort(endDate)}", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    if (tender.status.uppercase() == "ACTIVO" || tender.status.uppercase() == "ABIERTA") {
                        Text("• $remainingDays días", fontSize = 10.sp, color = if(remainingDays < 3) StatusWarning else MaverickBlue, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(color = MaverickBlue.copy(0.1f), shape = RoundedCornerShape(6.dp)) { Text("$budgetCount propuestas", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaverickBlue) }
                    StatusPill(tender.status)
                }
            }
        }
        if (isNew && !isSelected) Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-5).dp).size(12.dp).background(NeonCyber, CircleShape).border(2.dp, DarkBackground, CircleShape))
    }
}

@Composable
fun StatusPill(status: String) {
    val color = when(status.uppercase()) {
        "ACTIVO", "ABIERTA" -> StatusActive
        "ADJUDICADO" -> MaverickPurple
        "TERMINADO" -> StatusFinished
        else -> Color.Gray
    }
    Surface(color = color.copy(0.12f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(0.3f))) {
        Text(status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun ComparisonSheet(tender: TenderEntity, budgets: List<BudgetEntity>, onBudgetClick: (BudgetEntity) -> Unit, onChatClick: (String) -> Unit) {
    val minPrice = if (budgets.isNotEmpty()) budgets.minByOrNull { it.grandTotal }?.grandTotal ?: 0.0 else 0.0
    Column(Modifier.fillMaxWidth().padding(24.dp)) {
        Text(tender.title.uppercase(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
        Text("Comparativa técnica de cotizaciones", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(24.dp))
        if (budgets.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("No hay propuestas aún.", color = Color.DarkGray) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(budgets, key = { it.budgetId }) { budget ->
                    BudgetComparisonItem(
                        budget = budget,
                        isBestPrice = budget.grandTotal == minPrice && budgets.size > 1,
                        isNew = budget.status == BudgetStatus.PENDIENTE,
                        onView = { onBudgetClick(budget) },
                        onChat = { onChatClick(budget.providerId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetComparisonItem(
    budget: BudgetEntity,
    isBestPrice: Boolean,
    isNew: Boolean,
    isSelected: Boolean = false,
    onView: () -> Unit,
    onChat: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val rainbowBrush = geminiGradientEffect()
    val borderColor = if (isSelected) MaverickBlue else if (isNew) NeonCyber else if (isBestPrice) StatusFinished.copy(0.4f) else Color.White.copy(0.06f)

    Box(modifier = Modifier
        .padding(horizontal = 16.dp)
        .combinedClickable(
            onClick = onView,
            onLongClick = onLongClick
        )
    ) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(0.03f), shape = RoundedCornerShape(24.dp), border = BorderStroke(if(isSelected) 2.dp else 1.dp, borderColor)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = budget.providerPhotoUrl, contentDescription = null, modifier = Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(budget.providerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text(text = "Total: $ ${String.format(Locale.getDefault(), "%,.0f", budget.grandTotal)}", color = if (isBestPrice) StatusFinished else MaverickBlue, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    if (isNew) Text("NUEVA PROPUESTA", color = NeonCyber, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaverickBlue, modifier = Modifier.size(24.dp))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onChat, modifier = Modifier.size(44.dp)) { Icon(Icons.AutoMirrored.Filled.Chat, null, tint = MaverickBlue) }
                        IconButton(onClick = onView, modifier = Modifier.background(rainbowBrush, RoundedCornerShape(14.dp)).size(44.dp)) { Icon(Icons.Default.Description, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                    }
                }
            }
        }
        if (isBestPrice && !isSelected) Surface(modifier = Modifier.offset(x = 20.dp, y = (-10).dp), color = StatusFinished, shape = RoundedCornerShape(6.dp)) { Text("MEJOR PRECIO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White) }
    }
}

@Composable
fun PresupuestoSearchResultsPanel(
    searchQuery: String,
    tenders: List<TenderEntity>,
    budgets: List<BudgetEntity>,
    onTenderClick: (TenderEntity) -> Unit,
    onBudgetClick: (BudgetEntity) -> Unit
) {
    val matchedTenders = tenders.filter { it.title.contains(searchQuery, ignoreCase = true) || it.tenderId.contains(searchQuery, ignoreCase = true) }
    val matchedBudgets = budgets.filter { it.providerName.contains(searchQuery, ignoreCase = true) || it.budgetId.contains(searchQuery, ignoreCase = true) }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground.copy(alpha = 0.95f)) {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            if (matchedTenders.isNotEmpty()) {
                item { Text("LICITACIONES", color = MaverickBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                items(matchedTenders) { tender ->
                    Row(Modifier.fillMaxWidth().clickable { onTenderClick(tender) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, null, tint = MaverickBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(tender.title, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
            if (matchedBudgets.isNotEmpty()) {
                item { Text("PRESUPUESTOS", color = MaverickPurple, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                items(matchedBudgets) { budget ->
                    Row(Modifier.fillMaxWidth().clickable { onBudgetClick(budget) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, null, tint = MaverickPurple, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(budget.providerName, color = Color.White, fontSize = 14.sp)
                            Text("Total: $ ${budget.grandTotal}", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
            if (matchedTenders.isEmpty() && matchedBudgets.isEmpty()) {
                item { Text("No se encontraron resultados para '$searchQuery'", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center) }
            }
        }
    }
}

// --- FUNCIONES DE APOYO PARA EMOJIS/COLORES ---
private fun getCategoryEmoji(title: String): String {
    return when {
        title.contains("Hogar", ignoreCase = true) -> "🏠"
        title.contains("Tecnología", ignoreCase = true) || title.contains("Informatica", ignoreCase = true) -> "💻"
        title.contains("Vehículos", ignoreCase = true) -> "🚗"
        title.contains("Eventos", ignoreCase = true) -> "🎉"
        title.contains("Salud", ignoreCase = true) -> "⚕️"
        title.contains("Enseñanza", ignoreCase = true) -> "📚"
        title.contains("Construcción", ignoreCase = true) -> "🏗️"
        title.contains("Mascotas", ignoreCase = true) -> "🐾"
        title.contains("Belleza", ignoreCase = true) -> "💅"
        title.contains("Transporte", ignoreCase = true) -> "🚚"
        title.contains("Gastronomía", ignoreCase = true) -> "🍔"
        title.contains("Profesionales", ignoreCase = true) -> "👨‍⚖️"
        title.contains("Electricidad", ignoreCase = true) -> "⚡"
        title.contains("Plomería", ignoreCase = true) -> "🔧"
        else -> "📂"
    }
}

private fun getCategoryColor(title: String): Color {
    return when {
        title.contains("Hogar", ignoreCase = true) -> Color(0xFFFAD2E1)
        title.contains("Tecnología", ignoreCase = true) || title.contains("Informatica", ignoreCase = true) -> Color(0xFF38BDF8)
        title.contains("Electricidad", ignoreCase = true) -> Color(0xFFFACC15)
        title.contains("Construcción", ignoreCase = true) -> Color(0xFF9B51E0)
        else -> Color(0xFF10B981)
    }
}

private fun formatDateShort(timestamp: Long): String = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))

// =================================================================================
// --- PREVIEW ---
// =================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PresupuestosScreenPreview() {
    val currentTime = System.currentTimeMillis()
    val sampleTenders = listOf(
        TenderEntity("T1", "Instalación Cámaras IP", "Sistema de 4 cámaras", "Informatica", "ACTIVO", currentTime, currentTime + 86400000L * 5, 4),
        TenderEntity("T2", "Reparación Tablero", "...", "Electricidad", "ADJUDICADO", currentTime - 86400000L, currentTime - 86400000L, 1)
    )

    MyApplicationTheme {
        PresupuestosScreenContent(
            tenders = sampleTenders,
            directBudgets = emptyList(),
            getBudgetsForTender = { _ -> MutableStateFlow(emptyList()) },
            onChatClick = {},
            onBack = {},
            onAcceptBudget = {},
            onRejectBudget = {},
            bottomPadding = PaddingValues(0.dp)
        )
    }
}
/**
package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.local.*
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// =================================================================================
// --- SECCIÓN 1: CONSTANTES Y ESTILOS VISUALES ---
// =================================================================================

private val DarkBackground = Color(0xFF05070A)
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val MaverickPurple = Color(0xFF9B51E0)
private val StatusActive = Color(0xFF38BDF8)
private val StatusFinished = Color(0xFF34D399)
private val StatusWarning = Color(0xFFF87171)
private val NeonCyber = Color(0xFF00FFC2)

// =================================================================================
// --- SECCIÓN 2: PANTALLA PRINCIPAL (CONTENEDOR) ---
// =================================================================================

/**
 * Pantalla principal de Presupuestos.
 * Maneja la integración con el ViewModel y delega la UI al contenido stateless.
 */
@Composable
fun PresupuestosScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp) // Recibimos el padding del Scaffold externo
) {
    // Suscripción a flujos de datos de Firebase/Room
    val tenders by viewModel.tenders.collectAsStateWithLifecycle()
    val directBudgets by viewModel.directBudgets.collectAsStateWithLifecycle()

    PresupuestosScreenContent(
        tenders = tenders,
        directBudgets = directBudgets,
        getBudgetsForTender = { tenderId -> viewModel.getBudgetsForTender(tenderId) },
        onChatClick = onChatClick,
        onBack = onBack,
        onAcceptBudget = { budget -> viewModel.acceptBudget(budget) },
        onRejectBudget = { budget -> viewModel.rejectBudget(budget) },
        bottomPadding = bottomPadding
    )
}

/**
 * Contenido stateless de la pantalla de Presupuestos.
 * Se ha mejorado la jerarquía de capas para que el FAB sea siempre el elemento superior.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreenContent(
    tenders: List<TenderEntity>,
    directBudgets: List<BudgetEntity>,
    getBudgetsForTender: (String) -> StateFlow<List<BudgetEntity>>,
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    onAcceptBudget: (BudgetEntity) -> Unit,
    onRejectBudget: (BudgetEntity) -> Unit,
    bottomPadding: PaddingValues
) {
    // --- ESTADOS DE UI Y NAVEGACIÓN ---
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("LICITACIONES", "DIRECTOS")
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // --- ESTADOS DEL FAB GEMINI ---
    var isFabExpanded by remember { mutableStateOf(false) }

    // --- ESTADOS DE FILTROS Y ORDENAMIENTO (Barra Horizontal) ---
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var filterFavorites by remember { mutableStateOf(false) }
    var filterVerified by remember { mutableStateOf(true) }
    var sortMethod by remember { mutableStateOf("Fecha") }

    // --- ESTADOS DE HERRAMIENTAS (Barra Vertical) ---
    var viewMode by remember { mutableStateOf("Lista") }
    var multiSelectEnabled by remember { mutableStateOf(false) }
    val selectedTenderIds = remember { mutableStateListOf<String>() }
    val selectedBudgetIds = remember { mutableStateListOf<String>() }

    // Modales y Visores
    var selectedTenderForSheet by remember { mutableStateOf<TenderEntity?>(null) }
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    
    // --- ESTADO PARA ANALÍTICAS ---
    var selectedAnalyticsData by remember { mutableStateOf<Pair<TenderEntity, List<BudgetEntity>>?>(null) }

    // Lógica de filtrado común para presupuestos (REMEMORED para evitar bucles de recomposición)
    val budgetFilterLambda: (List<BudgetEntity>) -> List<BudgetEntity> = remember(selectedCategory, sortMethod, searchQuery, filterVerified, filterFavorites) {
        { list ->
            list.filter { budget ->
                val matchesCategory = selectedCategory == null || budget.items.any { it.description.contains(selectedCategory!!, ignoreCase = true) }
                val matchesSearch = searchQuery.isEmpty() || 
                                    budget.providerName.contains(searchQuery, ignoreCase = true) || 
                                    budget.budgetId.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }.let { filteredList ->
                when(sortMethod) {
                    "Precio" -> filteredList.sortedBy { it.grandTotal }
                    "Alpha" -> filteredList.sortedBy { it.providerName }
                    "Fecha" -> filteredList.sortedByDescending { it.dateTimestamp }
                    else -> filteredList
                }
            }
        }
    }

    // Lista procesada para el Tab de Directos
    val displayedDirectBudgets = remember(directBudgets, budgetFilterLambda) {
        budgetFilterLambda(directBudgets)
    }

    // Lógica para cancelar la multiselección
    val cancelSelection = {
        selectedTenderIds.clear()
        selectedBudgetIds.clear()
        multiSelectEnabled = false
        isFabExpanded = false
    }

    // Root Box para asegurar que el FAB esté siempre arriba de todo
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                if (!isSearchActive) {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Gestión Comercial", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                                Text("ADMINISTRADOR DE COTIZACIONES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                
                // --- ESTRUCTURA BASE DE CONTENIDO ---
                Column(modifier = Modifier.fillMaxSize()) {
                    CommercialStatsDashboard(
                        activeCount = if (selectedTabIndex == 0) tenders.size else directBudgets.size,
                        offerCount = if (selectedTabIndex == 0) tenders.sumOf { it.budgetCount } else directBudgets.size,
                        savings = 15,
                        contextLabel = if (selectedTabIndex == 0) "LICITACIONES" else "PRESUPUESTOS"
                    )

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaverickBlue,
                        indicator = { positions ->
                            if (selectedTabIndex < positions.size) {
                                TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[selectedTabIndex]), color = MaverickBlue)
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { 
                                    selectedTabIndex = index
                                    cancelSelection()
                                },
                                text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTabIndex) {
                            0 -> LicitacionesTabContent(
                                tenders = tenders,
                                getBudgetsForTender = getBudgetsForTender,
                                selectedIds = selectedTenderIds,
                                onTenderClick = { tender -> 
                                    if (multiSelectEnabled) {
                                        if (selectedTenderIds.contains(tender.tenderId)) selectedTenderIds.remove(tender.tenderId)
                                        else selectedTenderIds.add(tender.tenderId)
                                        if (selectedTenderIds.isEmpty()) multiSelectEnabled = false
                                    } else {
                                        selectedTenderForSheet = tender 
                                    }
                                },
                                onTenderLongClick = { tender ->
                                    multiSelectEnabled = true
                                    if (!selectedTenderIds.contains(tender.tenderId)) selectedTenderIds.add(tender.tenderId)
                                    isFabExpanded = true // Abrir menú de herramientas
                                }
                            )
                            1 -> DirectBudgetsTabContent(
                                budgets = displayedDirectBudgets,
                                categoryFilter = selectedCategory,
                                selectedIds = selectedBudgetIds,
                                onBudgetClick = { budget -> 
                                    if (multiSelectEnabled) {
                                        if (selectedBudgetIds.contains(budget.budgetId)) selectedBudgetIds.remove(budget.budgetId)
                                        else selectedBudgetIds.add(budget.budgetId)
                                        if (selectedBudgetIds.isEmpty()) multiSelectEnabled = false
                                    } else {
                                        budgetForA4Preview = budget 
                                    }
                                },
                                onChatClick = onChatClick,
                                onBudgetLongClick = { budget ->
                                    multiSelectEnabled = true
                                    if (!selectedBudgetIds.contains(budget.budgetId)) selectedBudgetIds.add(budget.budgetId)
                                    isFabExpanded = true
                                }
                            )
                        }
                    }
                }

                // --- OVERLAY DE BÚSQUEDA UNIFICADA ---
                if (isSearchActive) {
                    Box(modifier = Modifier.fillMaxSize().zIndex(10f).background(Color.Black.copy(alpha = 0.6f)).clickable { isSearchActive = false })
                    Column(modifier = Modifier.fillMaxSize().zIndex(11f)) {
                        AnimatedVisibility(visible = isSearchActive, enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut()) {
                            Row(modifier = Modifier.fillMaxWidth().background(DarkBackground).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    GeminiTopSearchBar(
                                        searchQuery = searchQuery, 
                                        onSearchQueryChange = { searchQuery = it }, 
                                        placeholderText = if(selectedTabIndex == 0) "Buscar licitaciones..." else "Buscar presupuestos..."
                                    )
                                }
                                Surface(onClick = { isSearchActive = false; searchQuery = "" }, modifier = Modifier.size(56.dp), shape = CircleShape, color = CardSurface, border = BorderStroke(1.dp, MaverickBlue.copy(alpha = 0.5f))) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, null, tint = Color.White) }
                                }
                            }
                        }
                        if (searchQuery.isNotEmpty()) {
                            PresupuestoSearchResultsPanel(
                                searchQuery = searchQuery,
                                tenders = if(selectedTabIndex == 0) tenders else emptyList(),
                                budgets = if(selectedTabIndex == 1) directBudgets else emptyList(),
                                onTenderClick = { selectedTenderForSheet = it; isSearchActive = false },
                                onBudgetClick = { budgetForA4Preview = it; isSearchActive = false }
                            )
                        }
                    }
                }
            }
        }

        // --- OVERLAY: COMPARATIVA (Overlay total para mantener FAB arriba) ---
        AnimatedVisibility(
            visible = selectedTenderForSheet != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.zIndex(40f)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedTenderForSheet = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text("Ofertas Recibidas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    
                    if (selectedTenderForSheet != null) {
                        val budgetsFlow = remember(selectedTenderForSheet) { getBudgetsForTender(selectedTenderForSheet!!.tenderId) }
                        val rawBudgets by budgetsFlow.collectAsStateWithLifecycle()
                        val filteredBudgets = remember(rawBudgets, budgetFilterLambda) { budgetFilterLambda(rawBudgets) }

                        ComparisonSheet(
                            tender = selectedTenderForSheet!!,
                            budgets = filteredBudgets,
                            onBudgetClick = { budget -> budgetForA4Preview = budget },
                            onChatClick = onChatClick
                        )
                    }
                }
            }
        }

        // --- FAB GEMINI SUPERPUESTO (MÁXIMO Z-INDEX 100) ---
        // Aplicamos el bottomPadding recibido para posicionarlo correctamente sobre la NavigationBar
        Box(modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .padding(bottom = bottomPadding.calculateBottomPadding())
        ) {
            GeminiFABWithScrim(
                bottomPadding = PaddingValues(0.dp),
                showScrim = isFabExpanded
            ) {
                GeminiSplitFAB(
                    isExpanded = isFabExpanded,
                    isSearchActive = isSearchActive,
                    onToggleExpand = { isFabExpanded = !isFabExpanded },
                    onActivateSearch = { isSearchActive = true; isFabExpanded = false },
                    onCloseSearch = { isSearchActive = false; searchQuery = "" },


                )
            }
        }
        
        // --- OVERLAY: ANALÍTICAS (Z-INDEX SUPERIOR) ---
        if (selectedAnalyticsData != null) {
            Box(modifier = Modifier.fillMaxSize().zIndex(200f)) {
                BudgetComparisonAnalytics(
                    tender = selectedAnalyticsData!!.first,
                    budgets = selectedAnalyticsData!!.second,
                    onBack = { selectedAnalyticsData = null }
                )
            }
        }
    }

    if (budgetForA4Preview != null) {
        Dialog(onDismissRequest = { budgetForA4Preview = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            BudgetMultiPageScreen(
                budget = budgetForA4Preview!!, 
                onBack = { budgetForA4Preview = null }, 
                onAccept = { _ -> onAcceptBudget(budgetForA4Preview!!); budgetForA4Preview = null }, 
                onReject = { _ -> onRejectBudget(budgetForA4Preview!!); budgetForA4Preview = null }
            )
        }
    }
}

// =================================================================================
// --- COMPONENTES DE UI ---
// =================================================================================

@Composable
fun CommercialStatsDashboard(activeCount: Int, offerCount: Int, savings: Int, contextLabel: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(Modifier.weight(1f), "$contextLabel ACTIVAS", activeCount.toString(), Color.White)
        StatCard(Modifier.weight(1f), "RECIBIDOS", offerCount.toString(), MaverickPurple)
        StatCard(Modifier.weight(1f), "AHORRO EST.", "$savings%", StatusFinished)
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, color: Color) {
    Surface(modifier = modifier, color = Color.White.copy(0.03f), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color.White.copy(0.06f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

// =================================================================================
// --- SECCIÓN 4: LISTADOS ---
// =================================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LicitacionesTabContent(
    tenders: List<TenderEntity>, 
    getBudgetsForTender: (String) -> StateFlow<List<BudgetEntity>>, 
    selectedIds: List<String>,
    onTenderClick: (TenderEntity) -> Unit,
    onTenderLongClick: (TenderEntity) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        items(tenders, key = { it.tenderId }) { tender ->
            val budgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
            val budgets by budgetsFlow.collectAsStateWithLifecycle()
            val isNew = budgets.any { it.status == BudgetStatus.PENDIENTE }
            LicitacionFolderCard(
                tender = tender, 
                budgetCount = budgets.size, 
                isNew = isNew, 
                isSelected = selectedIds.contains(tender.tenderId),
                onClick = { onTenderClick(tender) },
                onLongClick = { onTenderLongClick(tender) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DirectBudgetsTabContent(
    budgets: List<BudgetEntity>, 
    categoryFilter: String?, 
    selectedIds: List<String>,
    onBudgetClick: (BudgetEntity) -> Unit, 
    onChatClick: (String) -> Unit,
    onBudgetLongClick: (BudgetEntity) -> Unit
) {
    if (budgets.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay presupuestos que coincidan.", color = Color.Gray) }
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
            items(budgets, key = { it.budgetId }) { budget ->
                val isNew = budget.status == BudgetStatus.PENDIENTE
                BudgetComparisonItem(
                    budget = budget, 
                    isBestPrice = false, 
                    isNew = isNew, 
                    isSelected = selectedIds.contains(budget.budgetId),
                    onView = { onBudgetClick(budget) }, 
                    onChat = { onChatClick(budget.providerId) },
                    onLongClick = { onBudgetLongClick(budget) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LicitacionFolderCard(
    tender: TenderEntity, 
    budgetCount: Int, 
    isNew: Boolean, 
    isSelected: Boolean,
    onClick: () -> Unit, 
    onLongClick: () -> Unit
) {
    val endDate = if (tender.endDate > 0) tender.endDate else tender.dateTimestamp + (86400000L * 7)
    val remainingDays = TimeUnit.MILLISECONDS.toDays(endDate - System.currentTimeMillis()).coerceAtLeast(0)
    val borderColor = if (isSelected) MaverickBlue else if (isNew) NeonCyber else Color.White.copy(0.1f)

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .padding(top = 20.dp)
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Surface(modifier = Modifier.offset(x = 0.dp, y = (-24).dp).width(110.dp).height(30.dp), color = Color.White.copy(0.06f), shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp), border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))) {
            Box(contentAlignment = Alignment.Center) { Text("#${tender.tenderId.takeLast(6).uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = if(isSelected) MaverickBlue else if(isNew) NeonCyber else MaverickBlue) }
        }
        Surface(modifier = Modifier.fillMaxWidth(), color = CardSurface, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp), border = BorderStroke(if(isSelected) 2.dp else 1.dp, borderColor), shadowElevation = if (isNew || isSelected) 15.dp else 4.dp) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tender.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaverickBlue)
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(" ${formatDateShort(tender.dateTimestamp)} - ${formatDateShort(endDate)}", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    if (tender.status.uppercase() == "ACTIVO" || tender.status.uppercase() == "ABIERTA") {
                        Text("• $remainingDays días", fontSize = 10.sp, color = if(remainingDays < 3) StatusWarning else MaverickBlue, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(color = MaverickBlue.copy(0.1f), shape = RoundedCornerShape(6.dp)) { Text("$budgetCount propuestas", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaverickBlue) }
                    StatusPill(tender.status)
                }
            }
        }
        if (isNew && !isSelected) Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-5).dp).size(12.dp).background(NeonCyber, CircleShape).border(2.dp, DarkBackground, CircleShape))
    }
}

@Composable
fun StatusPill(status: String) {
    val color = when(status.uppercase()) {
        "ACTIVO", "ABIERTA" -> StatusActive
        "ADJUDICADO" -> MaverickPurple
        "TERMINADO" -> StatusFinished
        else -> Color.Gray
    }
    Surface(color = color.copy(0.12f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(0.3f))) {
        Text(status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun ComparisonSheet(tender: TenderEntity, budgets: List<BudgetEntity>, onBudgetClick: (BudgetEntity) -> Unit, onChatClick: (String) -> Unit) {
    val minPrice = if (budgets.isNotEmpty()) budgets.minByOrNull { it.grandTotal }?.grandTotal ?: 0.0 else 0.0
    Column(Modifier.fillMaxWidth().padding(24.dp)) {
        Text(tender.title.uppercase(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
        Text("Comparativa técnica de cotizaciones", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(24.dp))
        if (budgets.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("No hay propuestas aún.", color = Color.DarkGray) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(budgets, key = { it.budgetId }) { budget ->
                    BudgetComparisonItem(
                        budget = budget, 
                        isBestPrice = budget.grandTotal == minPrice && budgets.size > 1, 
                        isNew = budget.status == BudgetStatus.PENDIENTE, 
                        onView = { onBudgetClick(budget) }, 
                        onChat = { onChatClick(budget.providerId) } 
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetComparisonItem(
    budget: BudgetEntity, 
    isBestPrice: Boolean, 
    isNew: Boolean, 
    isSelected: Boolean = false,
    onView: () -> Unit, 
    onChat: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val rainbowBrush = geminiGradientEffect()
    val borderColor = if (isSelected) MaverickBlue else if (isNew) NeonCyber else if (isBestPrice) StatusFinished.copy(0.4f) else Color.White.copy(0.06f)

    Box(modifier = Modifier
        .padding(horizontal = 16.dp)
        .combinedClickable(
            onClick = onView, 
            onLongClick = onLongClick
        )
    ) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(0.03f), shape = RoundedCornerShape(24.dp), border = BorderStroke(if(isSelected) 2.dp else 1.dp, borderColor)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = budget.providerPhotoUrl, contentDescription = null, modifier = Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(budget.providerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text(text = "Total: $ ${String.format(Locale.getDefault(), "%,.0f", budget.grandTotal)}", color = if (isBestPrice) StatusFinished else MaverickBlue, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    if (isNew) Text("NUEVA PROPUESTA", color = NeonCyber, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaverickBlue, modifier = Modifier.size(24.dp))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onChat, modifier = Modifier.size(44.dp)) { Icon(Icons.AutoMirrored.Filled.Chat, null, tint = MaverickBlue) }
                        IconButton(onClick = onView, modifier = Modifier.background(rainbowBrush, RoundedCornerShape(14.dp)).size(44.dp)) { Icon(Icons.Default.Description, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                    }
                }
            }
        }
        if (isBestPrice && !isSelected) Surface(modifier = Modifier.offset(x = 20.dp, y = (-10).dp), color = StatusFinished, shape = RoundedCornerShape(6.dp)) { Text("MEJOR PRECIO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White) }
    }
}

fun formatDateShort(timestamp: Long): String = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))

// =================================================================================
// --- SECCIÓN 6: PANEL DE RESULTADOS DE BÚSQUEDA ---
// =================================================================================

@Composable
fun PresupuestoSearchResultsPanel(
    searchQuery: String,
    tenders: List<TenderEntity>,
    budgets: List<BudgetEntity>,
    onTenderClick: (TenderEntity) -> Unit,
    onBudgetClick: (BudgetEntity) -> Unit
) {
    val matchedTenders = tenders.filter { it.title.contains(searchQuery, ignoreCase = true) || it.tenderId.contains(searchQuery, ignoreCase = true) }
    val matchedBudgets = budgets.filter { it.providerName.contains(searchQuery, ignoreCase = true) || it.budgetId.contains(searchQuery, ignoreCase = true) }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground.copy(alpha = 0.95f)) {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            if (matchedTenders.isNotEmpty()) {
                item { Text("LICITACIONES", color = MaverickBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                items(matchedTenders) { tender ->
                    Row(Modifier.fillMaxWidth().clickable { onTenderClick(tender) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, null, tint = MaverickBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(tender.title, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
            if (matchedBudgets.isNotEmpty()) {
                item { Text("PRESUPUESTOS", color = MaverickPurple, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                items(matchedBudgets) { budget ->
                    Row(Modifier.fillMaxWidth().clickable { onBudgetClick(budget) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, null, tint = MaverickPurple, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(budget.providerName, color = Color.White, fontSize = 14.sp)
                            Text("Total: $ ${budget.grandTotal}", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
            if (matchedTenders.isEmpty() && matchedBudgets.isEmpty()) {
                item { Text("No se encontraron resultados for '$searchQuery'", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center) }
            }
        }
    }
}

// =================================================================================
// --- PREVIEW ---
// =================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PresupuestosScreenPreview() {
    val currentTime = System.currentTimeMillis()
    val sampleTenders = listOf(
        TenderEntity("T1", "Instalación Cámaras IP", "Sistema de 4 cámaras", "Informatica", "ACTIVO", currentTime, currentTime + 86400000L * 5, 4)
    )
    val sampleBudgets = listOf(
        BudgetEntity("B1", "u1", "p1", null, "Maverick Tech", null, null, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 45000.0, 0.0, 0.0, 45000.0, 7, null, null, null, null, BudgetStatus.PENDIENTE, currentTime)
    )

    MyApplicationTheme {
        PresupuestosScreenContent(
            tenders = sampleTenders,
            directBudgets = sampleBudgets,
            getBudgetsForTender = { _ -> MutableStateFlow(sampleBudgets) },
            onChatClick = {},
            onBack = {},
            onAcceptBudget = {},
            onRejectBudget = {},
            bottomPadding = PaddingValues(0.dp)
        )
    }
}
**/