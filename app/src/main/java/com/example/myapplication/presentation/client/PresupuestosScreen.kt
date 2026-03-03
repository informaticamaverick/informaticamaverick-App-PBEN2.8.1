package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
private val ErrorRed = Color(0xFFF43F5E)

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
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("LICITACIONES", "DIRECTOS")
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isFabExpanded by remember { mutableStateOf(false) }

    // Panel Táctico & Multiselección
    var activeFilters by remember { mutableStateOf(setOf<String>()) }
    var multiSelectEnabled by remember { mutableStateOf(false) }
    val selectedTenderIds = remember { mutableStateListOf<String>() }
    val selectedBudgetIds = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Estados de navegación interna
    var selectedTenderForSheet by remember { mutableStateOf<TenderEntity?>(null) }
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var selectedAnalyticsData by remember { mutableStateOf<Pair<TenderEntity, List<BudgetEntity>>?>(null) }
    var providerProfileToShow by remember { mutableStateOf<BudgetEntity?>(null) }

    // Registro local de presupuestos vistos
    val readBudgetIds = remember { mutableStateListOf<String>() }

    // Obtenemos los presupuestos de la licitación seleccionada EN VIVO para el Análisis y la Vista
    val tenderBudgetsFlow = remember(selectedTenderForSheet) {
        selectedTenderForSheet?.let { getBudgetsForTender(it.tenderId) } ?: MutableStateFlow(emptyList())
    }
    val rawTenderBudgets by tenderBudgetsFlow.collectAsStateWithLifecycle(emptyList())

    // 🔥 LÓGICA DE CATEGORÍAS CONTEXTUALES PARA EL PANEL TÁCTICO
    val dynamicCategoriesForPanel = remember(tenders, directBudgets, selectedTabIndex) {
        val extractedNames = mutableSetOf<String>()
        if (selectedTabIndex == 0) extractedNames.addAll(tenders.map { it.category })
        else if (directBudgets.isNotEmpty()) extractedNames.addAll(listOf("Informática", "Electricidad", "Plomería"))

        extractedNames.map { catName ->
            ControlItem(label = catName, icon = null, emoji = getCategoryEmoji(catName), color = getCategoryColor(catName), id = "cat_${catName.lowercase()}")
        }
    }

    // --- LÓGICA DE FILTRADO Y ORDENAMIENTO (APLICANDO LOS BOTONES DEL PANEL TÁCTICO) ---
    val filteredTenders = remember(tenders, activeFilters, searchQuery) {
        val selectedCats = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
        var result = tenders.filter { tender ->
            val matchesCategory = selectedCats.isEmpty() || selectedCats.contains(tender.category.lowercase())
            val matchesSearch = searchQuery.isEmpty() || tender.title.contains(searchQuery, ignoreCase = true) || tender.tenderId.contains(searchQuery)
            matchesCategory && matchesSearch
        }

        // Filtros especiales
        if (activeFilters.contains("filter_verif")) result = result.filter { it.status == "ABIERTA" }

        // Ordenamiento
        result = when {
            activeFilters.contains("sort_fecha_asc") -> result.sortedBy { it.dateTimestamp }
            activeFilters.contains("sort_nombre_asc") -> result.sortedBy { it.title }
            activeFilters.contains("sort_nombre_desc") -> result.sortedByDescending { it.title }
            else -> result.sortedByDescending { it.dateTimestamp } // Por defecto los más recientes
        }
        result
    }

    // Filtro inteligente para presupuestos (Directos y de Licitación)
    val budgetFilterLambda: (List<BudgetEntity>) -> List<BudgetEntity> = remember(activeFilters, searchQuery) {
        { list ->
            var result = list.filter { budget ->
                searchQuery.isEmpty() || budget.providerName.contains(searchQuery, ignoreCase = true)
            }

            // Ordenamientos combinados
            result = when {
                activeFilters.contains("sort_precio_asc") -> result.sortedBy { it.grandTotal }
                activeFilters.contains("sort_precio_desc") -> result.sortedByDescending { it.grandTotal }
                activeFilters.contains("sort_fecha_asc") -> result.sortedBy { it.dateTimestamp }
                activeFilters.contains("sort_nombre_asc") -> result.sortedBy { it.providerName }
                activeFilters.contains("sort_nombre_desc") -> result.sortedByDescending { it.providerName }
                else -> result.sortedByDescending { it.dateTimestamp }
            }
            result
        }
    }

    val displayedDirectBudgets = remember(directBudgets, budgetFilterLambda) { budgetFilterLambda(directBudgets) }
    val filteredTenderBudgets = remember(rawTenderBudgets, budgetFilterLambda) { budgetFilterLambda(rawTenderBudgets) }

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
                                onClick = { selectedTabIndex = index; cancelSelection(); selectedTenderForSheet = null },
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
                                readBudgetIds = readBudgetIds,
                                onTenderClick = { tender ->
                                    if (multiSelectEnabled) {
                                        if (selectedTenderIds.contains(tender.tenderId)) selectedTenderIds.remove(tender.tenderId)
                                        else selectedTenderIds.add(tender.tenderId)
                                        if (selectedTenderIds.isEmpty()) cancelSelection()
                                    } else {
                                        selectedTenderForSheet = tender
                                    }
                                },
                                onTenderLongClick = { tender ->
                                    multiSelectEnabled = true
                                    if (!selectedTenderIds.contains(tender.tenderId)) selectedTenderIds.add(tender.tenderId)
                                }
                            )
                            1 -> DirectBudgetsTabContent(
                                budgets = displayedDirectBudgets,
                                selectedIds = selectedBudgetIds,
                                readBudgetIds = readBudgetIds,
                                onBudgetClick = { budget ->
                                    if (multiSelectEnabled) {
                                        if (selectedBudgetIds.contains(budget.budgetId)) selectedBudgetIds.remove(budget.budgetId)
                                        else selectedBudgetIds.add(budget.budgetId)
                                        if (selectedBudgetIds.isEmpty()) cancelSelection()
                                    } else {
                                        readBudgetIds.add(budget.budgetId) // Marcar como leído
                                        budgetForA4Preview = budget
                                    }
                                },
                                onChatClick = onChatClick,
                                onAvatarClick = { budget -> providerProfileToShow = budget },
                                onBudgetLongClick = { budget ->
                                    multiSelectEnabled = true
                                    if (!selectedBudgetIds.contains(budget.budgetId)) selectedBudgetIds.add(budget.budgetId)
                                }
                            )
                        }
                    }
                }

                // --- BÚSQUEDA OVERLAY ---
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
                    }
                }
            }
        }

        // --- OVERLAY: COMPARATIVA EDGE-TO-EDGE (Ofertas de Licitación) ---
        AnimatedVisibility(
            visible = selectedTenderForSheet != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.zIndex(40f)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
                if (selectedTenderForSheet != null) {
                    ComparisonSheetEdgeToEdge(
                        tender = selectedTenderForSheet!!,
                        budgets = filteredTenderBudgets, // Aplicamos los filtros de ordenamiento
                        readBudgetIds = readBudgetIds,
                        onBack = { selectedTenderForSheet = null },
                        onBudgetClick = { budget ->
                            readBudgetIds.add(budget.budgetId)
                            budgetForA4Preview = budget
                        },
                        onChatClick = onChatClick,
                        onAvatarClick = { budget -> providerProfileToShow = budget },
                        onBudgetLongClick = { budget ->
                            multiSelectEnabled = true
                            if (!selectedBudgetIds.contains(budget.budgetId)) selectedBudgetIds.add(budget.budgetId)
                        },
                        selectedBudgetIds = selectedBudgetIds,
                        isMultiSelectMode = multiSelectEnabled
                    )
                }
            }
        }

        // --- FAB GEMINI TÁCTICO V2 (Z-INDEX 100) ---
        Box(modifier = Modifier.fillMaxSize().zIndex(100f).padding(bottom = bottomPadding.calculateBottomPadding())) {
            GeminiFABWithScrim(bottomPadding = PaddingValues(0.dp), showScrim = isFabExpanded) {
                GeminiSplitFAB(
                    isExpanded = isFabExpanded,
                    isSearchActive = isSearchActive,
                    isMultiSelectionActive = multiSelectEnabled,
                    onToggleExpand = { isFabExpanded = !isFabExpanded },
                    onActivateSearch = { isSearchActive = true; isFabExpanded = false },
                    onCloseSearch = { isSearchActive = false; searchQuery = "" },
                    activeFilters = activeFilters,
                    dynamicCategories = dynamicCategoriesForPanel,
                    onCompareClick = {
                        // Multi-selección manual
                        if (selectedTabIndex == 1 && selectedBudgetIds.size > 1) {
                            val budgetsToCompare = directBudgets.filter { it.budgetId in selectedBudgetIds }
                            val fakeTender = TenderEntity("comp_directa", "Comparativa Directa", "", "Varios")
                            selectedAnalyticsData = Pair(fakeTender, budgetsToCompare)
                            cancelSelection()
                        }
                    },
                    onDeleteClick = {
                        showDeleteDialog = true
                        isFabExpanded = false
                    },
                    onAction = { actionId ->
                        when (actionId) {
                            "toggle_multiselect" -> if (multiSelectEnabled) cancelSelection() else multiSelectEnabled = true
                            "apply_filters" -> isFabExpanded = false
                            else -> activeFilters = if (activeFilters.contains(actionId)) activeFilters - actionId else activeFilters + actionId
                        }
                    },
                    onResetAll = { activeFilters = emptySet() },
                    secondaryActions = {
                        // 🔥 BOTÓN ANALIZAR: Aparece si estamos dentro de una Licitación
                        if (selectedTenderForSheet != null && filteredTenderBudgets.isNotEmpty() && !multiSelectEnabled) {
                            SmallActionFab(
                                icon = Icons.Default.Analytics,
                                label = "Analizar",
                                iconColor = NeonCyber,
                                onClick = {
                                    // Pasa la licitación actual y sus presupuestos
                                    selectedAnalyticsData = Pair(selectedTenderForSheet!!, filteredTenderBudgets)
                                }
                            )
                        }
                    }
                )
            }
        }

        // --- OVERLAY: ANALÍTICAS (Z-INDEX SUPERIOR) ---
        if (selectedAnalyticsData != null) {
            Box(modifier = Modifier.fillMaxSize().zIndex(200f)) {
                BudgetComparisonAnalytics(
                    tender = selectedAnalyticsData!!.first,
                    budgets = selectedAnalyticsData!!.second,
                    onBack = { selectedAnalyticsData = null },
                    onViewBudgetDetail = { budgetId ->
                        val budget = selectedAnalyticsData?.second?.find { it.budgetId == budgetId }
                        if (budget != null) {
                            budgetForA4Preview = budget
                        }
                    }
                )
            }
        }

        // --- DIÁLOGO DE CONFIRMACIÓN DE BORRADO ---
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = CardSurface,
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed) },
                title = { Text("Confirmar Eliminación") },
                text = {
                    val count = if (selectedTabIndex == 0) selectedTenderIds.size else selectedBudgetIds.size
                    Text("Estás a punto de eliminar $count elemento(s) seleccionado(s). Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Aquí conectarías con tu ViewModel
                        cancelSelection()
                        showDeleteDialog = false
                    }) {
                        Text("Eliminar Definitivamente", color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar", color = Color.White)
                    }
                }
            )
        }
    }

    // --- MODAL BOTTOM SHEET: PERFIL DEL PRESTADOR ---
    if (providerProfileToShow != null) {
        val provider = providerProfileToShow!!
        ModalBottomSheet(
            onDismissRequest = { providerProfileToShow = null },
            containerColor = CardSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = provider.providerPhotoUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(100.dp).clip(CircleShape).border(3.dp, MaverickBlue, CircleShape),
                    contentScale = ContentScale.Crop,
                    fallback = rememberVectorPainter(Icons.Default.Person)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(provider.providerName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Filled.Verified, null, tint = MaverickPurple, modifier = Modifier.size(20.dp))
                }

                Text(provider.providerCompanyName ?: "Profesional Independiente", color = MaverickBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { /* TODO: Navegar a pantalla Perfil Prestador real */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaverickBlue)
                ) {
                    Text("VER PERFIL COMPLETO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // --- VISTA DETALLE A4 ---
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
    readBudgetIds: List<String>,
    onTenderClick: (TenderEntity) -> Unit,
    onTenderLongClick: (TenderEntity) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        items(tenders, key = { it.tenderId }) { tender ->
            val budgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
            val budgets by budgetsFlow.collectAsStateWithLifecycle()

            val unreadCount = budgets.count { it.status == BudgetStatus.PENDIENTE && !readBudgetIds.contains(it.budgetId) }

            LicitacionFolderCard(
                tender = tender,
                budgetCount = budgets.size,
                unreadCount = unreadCount,
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
    readBudgetIds: List<String>,
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String) -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit,
    onBudgetLongClick: (BudgetEntity) -> Unit
) {
    if (budgets.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay presupuestos.", color = Color.Gray) }
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
            items(budgets, key = { it.budgetId }) { budget ->
                val isNew = budget.status == BudgetStatus.PENDIENTE && !readBudgetIds.contains(budget.budgetId)

                BudgetComparisonItemEdge(
                    budget = budget,
                    isBestPrice = false,
                    isNew = isNew,
                    isSelected = selectedIds.contains(budget.budgetId),
                    onView = { onBudgetClick(budget) },
                    onChat = { onChatClick(budget.providerId) },
                    onAvatarClick = { onAvatarClick(budget) },
                    onLongClick = { onBudgetLongClick(budget) }
                )
                HorizontalDivider(color = Color.White.copy(0.05f))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LicitacionFolderCard(
    tender: TenderEntity,
    budgetCount: Int,
    unreadCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val endDate = if (tender.endDate > 0) tender.endDate else tender.dateTimestamp + (86400000L * 7)
    val remainingDays = TimeUnit.MILLISECONDS.toDays(endDate - System.currentTimeMillis()).coerceAtLeast(0)

    val df = SimpleDateFormat("dd MMM", Locale.getDefault())
    val startDateStr = df.format(Date(tender.dateTimestamp))
    val endDateStr = df.format(Date(endDate))

    val borderColor = if (isSelected) MaverickBlue else if (unreadCount > 0) NeonCyber else Color.White.copy(0.1f)

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .padding(top = 40.dp) // 🔥 Separación mayor solicitada
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        // Pestaña de la carpeta
        Surface(
            modifier = Modifier.offset(x = 0.dp, y = (-26).dp).width(130.dp).height(32.dp),
            color = Color.White.copy(0.06f),
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("#${tender.tenderId.takeLast(6).uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if(isSelected) MaverickBlue else if(unreadCount > 0) NeonCyber else MaverickBlue)
            }
        }

        // Cuerpo de la tarjeta
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardSurface,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 28.dp, bottomEnd = 28.dp, bottomStart = 28.dp),
            border = BorderStroke(if(isSelected) 2.dp else 1.dp, borderColor),
            shadowElevation = if (unreadCount > 0 || isSelected) 20.dp else 6.dp
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(tender.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.weight(1f), lineHeight = 24.sp)
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaverickBlue, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(Modifier.height(16.dp))

                // Fechas e Info
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("INICIO", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text(startDateStr, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESTADO", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        StatusPill(tender.status)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CIERRE", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text(endDateStr, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Surface(color = MaverickBlue.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Icon(Icons.Default.Description, null, tint = MaverickBlue, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("$budgetCount RECIBIDOS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaverickBlue)
                        }
                    }

                    if (tender.status.uppercase() == "ACTIVO" || tender.status.uppercase() == "ABIERTA") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = if(remainingDays < 3) StatusWarning else Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Faltan $remainingDays días", fontSize = 11.sp, color = if(remainingDays < 3) StatusWarning else Color.Gray, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // 🔥 Badge de Nuevas Ofertas Mejorado
        if (unreadCount > 0 && !isSelected) {
            Surface(
                color = NeonCyber,
                shape = RoundedCornerShape(percent = 50),
                shadowElevation = 8.dp,
                border = BorderStroke(2.2.dp, DarkBackground),
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp).zIndex(5f)
            ) {
                Text(
                    text = "$unreadCount NUEVOS",
                    color = DarkBackground,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    letterSpacing = 1.sp
                )
            }
        }
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
        Text(status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 9.sp, fontWeight = FontWeight.Black, color = color)
    }
}

// =================================================================================
// --- COMPARISON SHEET EDGE-TO-EDGE ---
// =================================================================================

@Composable
fun ComparisonSheetEdgeToEdge(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    readBudgetIds: List<String>,
    onBack: () -> Unit,
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String) -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit,
    onBudgetLongClick: (BudgetEntity) -> Unit,
    selectedBudgetIds: List<String>,
    isMultiSelectMode: Boolean
) {
    val minPrice = if (budgets.isNotEmpty()) budgets.minByOrNull { it.grandTotal }?.grandTotal ?: 0.0 else 0.0
    val unreadCount = budgets.count { it.status == BudgetStatus.PENDIENTE && !readBudgetIds.contains(it.budgetId) }

    // Lógica para ordenar por no leídos
    var sortByUnread by remember { mutableStateOf(false) }
    val displayBudgets = remember(budgets, sortByUnread, readBudgetIds) {
        if (sortByUnread) {
            budgets.sortedByDescending { it.status == BudgetStatus.PENDIENTE && !readBudgetIds.contains(it.budgetId) }
        } else {
            budgets
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Cabecera Fija
        Surface(color = DarkBackground, modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text("Ofertas Recibidas", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.weight(1f))

                    // 🔥 CONTADOR INTERACTIVO DE NO LEÍDOS
                    if (unreadCount > 0) {
                        Surface(
                            color = if(sortByUnread) ErrorRed else ErrorRed.copy(0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ErrorRed),
                            onClick = { sortByUnread = !sortByUnread }
                        ) {
                            Text(
                                "$unreadCount No Leídos",
                                color = if(sortByUnread) Color.White else ErrorRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Text(tender.title.uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(start = 36.dp, top = 4.dp))
            }
        }

        HorizontalDivider(color = Color.White.copy(0.1f))

        // Lista a Ancho Completo
        if (displayBudgets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay propuestas aún.", color = Color.DarkGray) }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) { // Espacio para el FAB
                items(displayBudgets, key = { it.budgetId }) { budget ->
                    val isNew = budget.status == BudgetStatus.PENDIENTE && !readBudgetIds.contains(budget.budgetId)

                    BudgetComparisonItemEdge(
                        budget = budget,
                        isBestPrice = budget.grandTotal == minPrice && budgets.size > 1,
                        isNew = isNew,
                        isSelected = selectedBudgetIds.contains(budget.budgetId),
                        onView = { onBudgetClick(budget) },
                        onChat = { onChatClick(budget.providerId) },
                        onAvatarClick = { onAvatarClick(budget) },
                        onLongClick = { onBudgetLongClick(budget) }
                    )
                    HorizontalDivider(color = Color.White.copy(0.05f))
                }
            }
        }
    }
}

/**
 * TARJETA DE PRESUPUESTO A ANCHO COMPLETO (EDGE-TO-EDGE)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetComparisonItemEdge(
    budget: BudgetEntity,
    isBestPrice: Boolean,
    isNew: Boolean,
    isSelected: Boolean = false,
    onView: () -> Unit,
    onChat: () -> Unit,
    onAvatarClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaverickBlue.copy(0.15f) else Color.Transparent

    Box(modifier = Modifier
        .fillMaxWidth()
        .background(backgroundColor)
        .combinedClickable(
            onClick = onView, // Toda la tarjeta abre el presupuesto
            onLongClick = onLongClick
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {

            // Avatar clickeable
            Box(contentAlignment = Alignment.TopEnd) {
                AsyncImage(
                    model = budget.providerPhotoUrl,
                    contentDescription = null,
                    fallback = rememberVectorPainter(Icons.Default.Person),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(0.1f), CircleShape)
                        .clickable { onAvatarClick() },
                    contentScale = ContentScale.Crop
                )
                if (isNew) {
                    Box(modifier = Modifier.size(14.dp).background(NeonCyber, CircleShape).border(2.dp, DarkBackground, CircleShape))
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(budget.providerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (isBestPrice && !isSelected) {
                        Spacer(Modifier.width(8.dp))
                        Surface(color = StatusFinished.copy(0.2f), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, StatusFinished.copy(0.5f))) {
                            Text("MEJOR PRECIO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 7.sp, fontWeight = FontWeight.Black, color = StatusFinished)
                        }
                    }
                }

                Text(
                    text = "$ ${String.format(Locale.getDefault(), "%,.0f", budget.grandTotal)}",
                    color = if (isBestPrice) StatusFinished else MaverickBlue,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                if (budget.providerCompanyName != null) {
                    Text(budget.providerCompanyName, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = MaverickBlue, modifier = Modifier.size(28.dp))
            } else {
                // SOLO EL ICONO DE MENSAJE (El de documento se quitó por requerimiento)
                IconButton(
                    onClick = onChat,
                    modifier = Modifier.size(48.dp).background(Color.White.copy(0.05f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = MaverickBlue, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

// --- FUNCIONES DE APOYO ---

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
