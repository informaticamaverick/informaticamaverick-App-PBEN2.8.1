package com.example.myapplication.presentation.client

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.presentation.components.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.emptyList

// =================================================================================
// --- CONSTANTES DE DISEÑO MACO ---
// =================================================================================
private val DarkBackground = Color(0xFF05070A)
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val MaverickPurple = Color(0xFF9B51E0)
private val StatusFinished = Color(0xFF34D399)

enum class BudgetTabMode {
    LICITACIONES, DIRECTOS 
}

// =================================================================================
// --- PANTALLA PRINCIPAL ---
// =================================================================================
@Composable
fun PresupuestosScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(), 
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    val tenders by viewModel.tenders.collectAsStateWithLifecycle()
    val directBudgets by viewModel.directBudgets.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle() // 🔥 Colectamos todos los presupuestos para lógica de ordenamiento
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
     // Estados reactivos de Be para filtrado dinámico
    val activeFilters by beBrainViewModel.activeFilters.collectAsStateWithLifecycle()
    val dynamicCategories by beBrainViewModel.dynamicCategories.collectAsStateWithLifecycle()
    val availableFilters by beBrainViewModel.availableFilters.collectAsStateWithLifecycle()
    val availableSortOptions by beBrainViewModel.availableSortOptions.collectAsStateWithLifecycle()
    val isSearchActive by beBrainViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchResults by beBrainViewModel.searchResults.collectAsStateWithLifecycle()
    val searchQuery by beBrainViewModel.searchQuery.collectAsStateWithLifecycle()
    // 🔥 ESTADOS DE MULTISELECCIÓN 🔥
    val isMultiSelectionActive by beBrainViewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val selectedItemIds by beBrainViewModel.selectedItemIds.collectAsStateWithLifecycle()
    val showBeTools by beBrainViewModel.showBeTools.collectAsStateWithLifecycle()
    val currentActions by beBrainViewModel.currentActions.collectAsStateWithLifecycle()
    // 🔥 CONEXIÓN DE CABLES: Enviamos los datos a Be para que filtre categorías por contexto
    LaunchedEffect(categories, tenders, directBudgets) {
        beBrainViewModel.updateAllCategories(categories)
        beBrainViewModel.updateTenders(tenders)
        beBrainViewModel.updateBudgets(directBudgets)
    }

    PresupuestosScreenContent(
        tenders = tenders,
        directBudgets = directBudgets,
        allBudgets = allBudgets, // 🔥 Pasamos todos los presupuestos
        categories = categories,
        activeFilters = activeFilters,
        dynamicCategories = dynamicCategories,
        refinementFilters = availableFilters,
        sortOptions = availableSortOptions,
        onFilterToggle = { beBrainViewModel.toggleFilter(it) },
        onClearFilters = { beBrainViewModel.clearSpecificFilters(listOf("filter_", "cat_")) },
        onClearSort = { beBrainViewModel.clearSpecificFilters(listOf("sort_", "view_")) },
        onSetContext = { beBrainViewModel.setHUDContext(it) },
        getBudgetsForTender = { tenderId -> viewModel.getBudgetsForTender(tenderId) },
        onChatClick = onChatClick,
        onBack = onBack,
        onAcceptBudget = { budget -> viewModel.acceptBudget(budget) },
        onRejectBudget = { budget -> viewModel.rejectBudget(budget) },
        onDeleteTenders = { ids -> viewModel.deleteTenders(ids) },
        onDeleteBudgets = { ids -> viewModel.deleteBudgets(ids) },
        onCancelTender = { tender -> viewModel.cancelTender(tender) },
        onMarkAsRead = { budgetId -> viewModel.markAsRead(budgetId) },
        bottomPadding = bottomPadding,
        isSearchActive = isSearchActive,
        searchResults = searchResults,
        searchQuery = searchQuery,
        onCloseBeAssistant = { beBrainViewModel.cerrarBeAssistantCompleto() },
        isMultiSelectionActive = isMultiSelectionActive,
        selectedItemIds = selectedItemIds,
        onToggleItemSelection = { beBrainViewModel.toggleItemSelection(it) },
        onSelectAllItems = { ids: List<String> -> beBrainViewModel.selectAllItems(ids) },
        showBeTools = showBeTools,
        currentActions = currentActions,
        onToggleMultiSelection = { beBrainViewModel.toggleMultiSelection() },
        beBrainViewModel = beBrainViewModel,
        onBeLongClick = { beBrainViewModel.onBeLongClick() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreenContent(
    tenders: List<TenderEntity>,
    directBudgets: List<BudgetEntity>,
    allBudgets: List<BudgetEntity>, // 🔥 Nueva lista global de presupuestos
    categories: List<CategoryEntity>,
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    refinementFilters: List<ControlItem>,
    sortOptions: List<ControlItem>,
    onFilterToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClearSort: () -> Unit,
    onSetContext: (HUDContext) -> Unit,
    getBudgetsForTender: (String) -> StateFlow<List<BudgetEntity>>,
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    onAcceptBudget: (BudgetEntity) -> Unit,
    onRejectBudget: (BudgetEntity) -> Unit,
    onDeleteTenders: (Set<String>) -> Unit = {},
    onDeleteBudgets: (Set<String>) -> Unit = {},
    onCancelTender: (TenderEntity) -> Unit = {},
    onMarkAsRead: (String) -> Unit = {},
    isSearchActive: Boolean = false,
    searchResults: BeBrainViewModel.SearchResult = BeBrainViewModel.SearchResult.Empty,
    searchQuery: String = "",
    onCloseBeAssistant: () -> Unit = {},
    isMultiSelectionActive: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onToggleItemSelection: (String) -> Unit = {},
    onLongClick: (String) -> Unit = {},
    onSelectAllItems: (List<String>) -> Unit = {},
    showBeTools: Boolean = false,
    currentActions: List<BeSmallActionModel> = emptyList(),
    bottomPadding: PaddingValues,
    beBrainViewModel: BeBrainViewModel, 
    onToggleMultiSelection: () -> Unit = {},
    onBeLongClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(BudgetTabMode.LICITACIONES) }
    // Sincronización de ContextO HUD según la solapa activa
    LaunchedEffect(pagerState.currentPage) {
        currentTab = if (pagerState.currentPage == 0) BudgetTabMode.LICITACIONES else BudgetTabMode.DIRECTOS
        onSetContext(if (currentTab == BudgetTabMode.LICITACIONES) HUDContext.BUDGETS_TENDERS else HUDContext.BUDGETS_DIRECT) 
    }

    var selectedTenderForSheet by remember { mutableStateOf<TenderEntity?>(null) }
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var providerProfileToShow by remember { mutableStateOf<BudgetEntity?>(null) }
    var tenderForAnalytics by remember { mutableStateOf<Pair<TenderEntity, List<BudgetEntity>>?>(null) }
    // 🔥 ESTADOS DE DIALOGS 🔥
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteContextMessage by remember { mutableStateOf("") }
    var onConfirmDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showTenderDetailPopup by remember { mutableStateOf<TenderEntity?>(null) }

    val currentSelectedIdsState by rememberUpdatedState(selectedItemIds)
    val currentTabState by rememberUpdatedState(currentTab)
    val currentTendersState by rememberUpdatedState(tenders)
    val currentDirectBudgetsState by rememberUpdatedState(directBudgets)
    val currentOnDeleteTenders by rememberUpdatedState(onDeleteTenders)
    val currentOnDeleteBudgets by rememberUpdatedState(onDeleteBudgets)
    val currentOnMarkAsRead by rememberUpdatedState(onMarkAsRead)

// =================================================================================
// 🧠 LÓGICA DE ACCIONES GLOBALES (BE ASSISTANT)
// =================================================================================
    LaunchedEffect(Unit) {
        beBrainViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "delete_multi" -> {
                    if (currentSelectedIdsState.isNotEmpty()) {
                        val isTenderTab = currentTabState == BudgetTabMode.LICITACIONES
                        deleteContextMessage = if (isTenderTab)
                            "¿Estás seguro que deseas eliminar las licitaciones seleccionadas de la base de datos?"
                        else "¿Estás seguro que deseas eliminar los presupuestos seleccionados?"
                        onConfirmDeleteAction = {
                            if (isTenderTab) currentOnDeleteTenders(currentSelectedIdsState)
                            else currentOnDeleteBudgets(currentSelectedIdsState)
                            beBrainViewModel.toggleMultiSelection()
                            showDeleteConfirmDialog = false
                        }
                        showDeleteConfirmDialog = true
                    }
                }
                "view_detail" -> {
                    if (currentSelectedIdsState.size == 1 && currentTabState == BudgetTabMode.LICITACIONES) {
                        val tenderId = currentSelectedIdsState.first()
                        val tender = currentTendersState.find { it.tenderId == tenderId }
                        if (tender != null) {
                            showTenderDetailPopup = tender
                            beBrainViewModel.toggleMultiSelection()
                        }
                    }
                }
                "cancel_select" -> {
                    beBrainViewModel.toggleMultiSelection()
                }
                "comparar_multi" -> {
                    if (currentSelectedIdsState.size >= 2) {
                        val selectedBudgets = currentDirectBudgetsState.filter { it.budgetId in currentSelectedIdsState }
                        if (selectedBudgets.isNotEmpty()) {
                            val mockTender = TenderEntity(
                                tenderId = "direct_comparison",
                                title = "Comparativa de Presupuestos Directos",
                                description = "",
                                category = "Varios"
                            )
                            // 🔥 MARCAR COMO LEÍDOS AL COMPARAR 🔥
                            selectedBudgets.forEach { budget -> currentOnMarkAsRead(budget.budgetId) }
                            tenderForAnalytics = mockTender to selectedBudgets.sortedBy { it.providerName.lowercase(Locale.getDefault()) }
                        }
                    }
                }
                "select_all_budgets" -> {
                    val allIds = currentDirectBudgetsState.map { it.budgetId }
                    beBrainViewModel.selectAllItems(allIds)
                }
                "mark_as_read_multi" -> {
                    if (currentSelectedIdsState.isNotEmpty()) {
                        currentSelectedIdsState.forEach { id -> currentOnMarkAsRead(id) }
                        beBrainViewModel.toggleMultiSelection()
                    }
                }
            }
        }
    }
    // =================================================================================
    // 🧠 LÓGICA DE FILTRADO REAL (CONECTADA A LAS CATEGORÍAS DE BE)
    // =================================================================================
    // Filtrado de Licitaciones
    val filteredTenders = remember(tenders, allBudgets, activeFilters, isSearchActive, searchResults, searchQuery) {
        // 1. Filtrado inicial (Búsqueda de Be o Lista completa)
        var list = if (searchQuery.isNotEmpty()) {
            val normalized = searchQuery.prepareForSearch()
            tenders.filter { it.title.prepareForSearch().wordStartsWith(normalized) }
        } else if (isSearchActive && searchResults is BeBrainViewModel.SearchResult.TenderMatch) {
            searchResults.tenders
        } else {
            tenders
        }

        // 1. Filtrado por Categoría (Dynamic)
        val catFilters = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
        if (catFilters.isNotEmpty()) {
            list = list.filter { tender -> 
                catFilters.any { it.equals(tender.category, ignoreCase = true) }
            }
        }
        // 2. Filtrado por Estado (Específicos de Licitación)
        val stateFilters = activeFilters.filter { it.startsWith("filter_tender_") }
        if (stateFilters.isNotEmpty()) {
            list = list.filter { tender ->
                stateFilters.any { filterId ->
                    when (filterId) {
                        "filter_tender_active" -> tender.status == "ABIERTA"
                        "filter_tender_closed" -> tender.status == "CERRADA"
                        "filter_tender_canceled" -> tender.status == "CANCELADA"
                        "filter_tender_awarded" -> tender.status == "ADJUDICADA"
                        else -> false
                    }
                }
            }
        }
        // 🔥 APLICAR ORDENAMIENTO PERSONALIZADO REQUERIDO 🔥
        // Prioridad: 1. ABIERTA, 2. ADJUDICADA, 3. CERRADA, 4. CANCELADA
        // Dentro de ABIERTA, prioridad a las que tienen presupuestos nuevos (unread)
        list = list.sortedWith(compareBy<TenderEntity> { tender ->
            when (tender.status) {
                "ABIERTA" -> 1
                "ADJUDICADA" -> 2
                "CERRADA" -> 3
                "CANCELADA" -> 4
                else -> 5
            }
        }.thenByDescending { tender ->
            // Si es abierta, verificamos si tiene presupuestos sin leer
            if (tender.status == "ABIERTA") {
                allBudgets.any { it.tenderId == tender.tenderId && !it.isRead }
            } else false
        }.thenByDescending { it.dateTimestamp }) // Luego por fecha más reciente

        // Aplicar Ordenamiento manual desde menú (si el usuario elige uno específico, este pisa el anterior)
        if (activeFilters.contains("sort_alpha")) list = list.sortedBy { it.title }
        if (activeFilters.contains("sort_date")) list = list.sortedByDescending { it.dateTimestamp }
        list
    }
    // Filtrado de Presupuestos Directos
    val filteredDirectBudgets = remember(directBudgets, activeFilters, categories, isSearchActive, searchResults) {
        var list = if (isSearchActive && searchResults is BeBrainViewModel.SearchResult.BudgetMatch) {
            searchResults.budgets
        } else {
            directBudgets
        }
        // 1. Filtrado por Categoría Dinámica (Buscando el providerId en la categoría)
        val catFilters = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
        if (catFilters.isNotEmpty()) {
            list = list.filter { budget ->
                // 🔥 Prioridad 1: Filtrar por la nueva categoría del presupuesto
                val budgetCatMatches = budget.category?.lowercase(Locale.getDefault()) in catFilters
                // Prioridad 2: Fallback por providerId en las categorías del sistema
                val providerMatches = categories.filter { it.name.lowercase(Locale.getDefault()) in catFilters }
                                                .any { it.providerIds.contains(budget.providerId) }
                budgetCatMatches || providerMatches
            }
        }

        // 🔥 Sincronizamos el ordenamiento: No leídos primero, luego por fecha descendente
        list = list.sortedWith(
            compareBy<BudgetEntity> { it.isRead }.thenByDescending { it.dateTimestamp }
        )

        // Aplicar Ordenamiento manual (si existe selección en el menú)
        if (activeFilters.contains("sort_alpha")) list = list.sortedBy { it.providerName }
        if (activeFilters.contains("sort_date")) list = list.sortedByDescending { it.dateTimestamp }
        if (activeFilters.contains("sort_price")) list = list.sortedBy { it.grandTotal }
        list
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💼", fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text("Administrador de Presupuestos", fontWeight = FontWeight.Black, color = Color.White, fontSize = 17.sp)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                    )
                    DividerPremium()
                }
            }
        ) { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                // --- SECCIÓN DE TABS ANIMADOS (LÓGICA DE CENTRADO DINÁMICO) ---
                AnimatedBudgetHeaderTabs(
                    currentTab = currentTab,
                    onTabSelected = {
                        currentTab = it
                        coroutineScope.launch { pagerState.animateScrollToPage(if(it == BudgetTabMode.LICITACIONES) 0 else 1) }
                    }
                )
                // --- PAGER DE CONTENIDO PRINCIPAL ---
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        // --- HEADER DE ESTADÍSTICAS Y MENÚS TÁCTICOS ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Contador alineado a la izquierda según pedido
                            SimpleStatsHeader(
                                label = if (page == 0) "LICITACIONES" else "DIRECTOS",
                                count = if (page == 0) filteredTenders.size else filteredDirectBudgets.size,
                                modifier = Modifier.weight(1f)
                            )
                            // Menús de Filtrado y Ordenamiento agrupados
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MenuFiltros(
                                    activeFilters = activeFilters,
                                    dynamicCategories = dynamicCategories,
                                    refinementFilters = refinementFilters,
                                    onAction = onFilterToggle,
                                    onApply = {},
                                    onClearFilters = onClearFilters
                                )
                                // Divider Vertical sutil entre menús
                                Spacer(Modifier.width(8.dp))
                                Box(modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(Color.White.copy(alpha = 0.15f)))
                                Spacer(Modifier.width(8.dp))

                                MenuOrdenamiento(
                                    activeFilters = activeFilters,
                                    sortOptions = sortOptions,
                                    onAction = onFilterToggle,
                                    onApply = {},
                                    onClearFilters = onClearSort
                                )
                            }
                        }
                        DividerPremium()
                        if (page == 0) {
                            // Contenido de Licitaciones Filtrado
                            LazyColumn(contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp)) {
                                items(filteredTenders, key = { it.tenderId }) { tender ->
                                    val budgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
                                    val budgets by budgetsFlow.collectAsStateWithLifecycle(emptyList())
                                    val unreadCount = budgets.count { !it.isRead }
                                    val isSelected = selectedItemIds.contains(tender.tenderId)
                                    val categoryInfo = categories.find { it.name.equals(tender.category, ignoreCase = true) }
                                    
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        LicitacionFolderPremium(
                                            title = tender.title,
                                            tenderId = tender.tenderId,
                                            status = tender.status,
                                            startDate = tender.dateTimestamp,
                                            endDate = tender.endDate,
                                            budgetCount = budgets.size,
                                            unreadCount = unreadCount,
                                            isSelected = isSelected,
                                            category = tender.category,
                                            categoryIcon = categoryInfo?.icon ?: "📋",
                                            categoryColor = categoryInfo?.color?.let { Color(it) } ?: Color.Gray,
                                            onClick = {
                                                if (isMultiSelectionActive) {
                                                    onToggleItemSelection(tender.tenderId)
                                                } else {
                                                    onCloseBeAssistant()
                                                    selectedTenderForSheet = tender
                                                }
                                            },
                                            onLongClick = {
                                                if (!isMultiSelectionActive) {
                                                    onToggleMultiSelection()
                                                }
                                                onToggleItemSelection(tender.tenderId)
                                            },
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        } else {
                            // Contenido de Directos Filtrado
                            BudgetGridContent(
                                budgets = filteredDirectBudgets,
                                isMultiSelectionActive = isMultiSelectionActive,
                                selectedItemIds = selectedItemIds,
                                onToggleItemSelection = onToggleItemSelection,
                                onBudgetClick = { budget ->
                                    if (isMultiSelectionActive) {
                                        onToggleItemSelection(budget.budgetId)
                                    } else {
                                        onMarkAsRead(budget.budgetId)
                                        budgetForA4Preview = budget
                                    }
                                },
                                onChatClick = onChatClick,
                                onToggleMultiSelection = onToggleMultiSelection,
                                onAvatarClick = { budget -> providerProfileToShow = budget }
                            )
                        }
                    }
                }
            }
        }

        // 🔥 NUEVO OVERLAY EXTERNALIZADO 🔥
        ResultadoLicitacionOverlay(
            selectedTender = selectedTenderForSheet,
            onClose = {
                selectedTenderForSheet = null
                onSetContext(if (currentTab == BudgetTabMode.LICITACIONES) HUDContext.BUDGETS_TENDERS else HUDContext.BUDGETS_DIRECT)
            },
            beBrainViewModel = beBrainViewModel,
            getBudgetsForTender = getBudgetsForTender,
            activeFilters = activeFilters,
            dynamicCategories = dynamicCategories,
            refinementFilters = refinementFilters,
            sortOptions = sortOptions,
            onFilterToggle = onFilterToggle,
            onClearFilters = onClearFilters,
            onClearSort = onClearSort,
            onBudgetClick = { budget ->
                onMarkAsRead(budget.budgetId)
                budgetForA4Preview = budget
            },
            onChatClick = onChatClick,
            onAvatarClick = { budget -> providerProfileToShow = budget },
            isMultiSelectionActive = isMultiSelectionActive,
            selectedItemIds = selectedItemIds,
            onToggleMultiSelection = onToggleMultiSelection,
            onAnalyticsClick = { tender, budgets -> 
                // 🔥 MARCAR COMO LEÍDOS AL COMPARAR 🔥
                budgets.forEach { b -> onMarkAsRead(b.budgetId) }
                tenderForAnalytics = tender to budgets 
            },
            onDeleteBudgets = onDeleteBudgets,
            onMarkAsReadMulti = { ids -> ids.forEach { id -> onMarkAsRead(id) } },
            showDeleteConfirmDialog = { message, action ->
                deleteContextMessage = message
                onConfirmDeleteAction = action
                showDeleteConfirmDialog = true
            }
        )
    }
        // 🔥 DIALOG DE CONFIRMACIÓN DE ELIMINACIÓN 🔥
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Confirmar Eliminación", fontWeight = FontWeight.Bold) },
                text = { Text(deleteContextMessage) },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirmDeleteAction?.invoke()
                        showDeleteConfirmDialog = false
                    }) {
                        Text("ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("CANCELAR", color = Color.Gray)
                    }
                },
                containerColor = CardSurface,
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
        // 🔥 POPUP DE DETALLE DE LICITACIÓN 🔥
        if (showTenderDetailPopup != null) {
            val tender = showTenderDetailPopup!!
            AlertDialog(
                onDismissRequest = { showTenderDetailPopup = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚖️", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(tender.title, fontWeight = FontWeight.Black)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(tender.description, color = Color.LightGray, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("Estado:", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                        val statusColor = when(tender.status) {
                            "ABIERTA" -> MaverickBlue
                            "CERRADA" -> Color.Gray
                            "CANCELADA" -> Color.Red
                            "ADJUDICADA" -> StatusFinished
                            else -> Color.White
                        }
                        Text(tender.status, color = statusColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        when (tender.status) {
                            "CANCELADA" -> {
                                tender.cancellationDate?.let { date ->
                                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    Text("Fecha de cancelación:", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                                    Text(sdf.format(Date(date)), color = Color.White, fontSize = 14.sp)
                                }
                            }
                            "ADJUDICADA" -> {
                                Text("Proveedor Adjudicado:", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Icon(Icons.Default.Verified, null, tint = MaverickBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(tender.awardedProviderName ?: "Desconocido", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = {
                                        tender.awardedProviderId?.let { onChatClick(it) }
                                        showTenderDetailPopup = null
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, "Chat", tint = MaverickBlue)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (tender.status == "ABIERTA") {
                        TextButton(onClick = {
                            onCancelTender(tender)
                            showTenderDetailPopup = null
                        }) {
                            Text("CANCELAR LICITACIÓN", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = { showTenderDetailPopup = null }) {
                            Text("CERRAR", color = MaverickBlue)
                        }
                    }
                },
                dismissButton = {
                    if (tender.status == "ABIERTA") {
                        TextButton(onClick = { showTenderDetailPopup = null }) {
                            Text("SALIR", color = Color.Gray)
                        }
                    }
                },
                containerColor = CardSurface,
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
        // --- VISUALIZADOR DE ANALÍTICA (MODAL) ---
        if (tenderForAnalytics != null) {
            Dialog(
                onDismissRequest = { tenderForAnalytics = null },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                BudgetComparisonAnalytics(
                    tender = tenderForAnalytics!!.first,
                    budgets = tenderForAnalytics!!.second,
                    onBack = { tenderForAnalytics = null },
                    onViewBudgetDetail = { selectedId -> 
                        val currentBudgets = tenderForAnalytics?.second
                        tenderForAnalytics = null
                        val foundBudget = currentBudgets?.find { it.budgetId == selectedId }
                            ?: directBudgets.find { it.budgetId == selectedId }
                        if (foundBudget != null) {
                            onMarkAsRead(foundBudget.budgetId)
                            budgetForA4Preview = foundBudget
                        }
                    }
                )
            }
        }
        // --- VISUALIZADOR DE PRESUPUESTO (A4 / PDF) ---
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
        // --- BOTTOM SHEET PERFIL ---
        if (providerProfileToShow != null) {
            ModalBottomSheet(onDismissRequest = { providerProfileToShow = null }, containerColor = CardSurface) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = providerProfileToShow?.providerPhotoUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaverickBlue, CircleShape),
                        contentScale = ContentScale.Crop,
                        fallback = rememberVectorPainter(Icons.Default.Person)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(providerProfileToShow?.providerName ?: "", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(providerProfileToShow?.providerCompanyName ?: "Profesional", color = MaverickBlue, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { }, modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaverickBlue), shape = RoundedCornerShape(12.dp)) {
                        Text("VER PERFIL COMPLETO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

// =================================================================================
// --- COMPONENTES DE UI ---
// =================================================================================

@Composable
fun SimpleStatsHeader(label: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (label == "LICITACIONES") "Licitaciones" else "Presupuestos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = count.toString(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

/*** Tabs con comportamiento dinámico. El activo se centra, el inactivo se asoma un 50% en el borde.*/
@Composable
fun AnimatedBudgetHeaderTabs(
    currentTab: BudgetTabMode,
    onTabSelected: (BudgetTabMode) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val centerX = screenWidth / 2
    val licTargetCenter = if (currentTab == BudgetTabMode.LICITACIONES) centerX else 0.dp
    val dirTargetCenter = if (currentTab == BudgetTabMode.DIRECTOS) centerX else screenWidth
    val licOffset by animateDpAsState(
        targetValue = licTargetCenter,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "licX"
    )
    val dirOffset by animateDpAsState(
        targetValue = dirTargetCenter,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "dirX"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp)
            .background(DarkBackground)
            .zIndex(10f),
        contentAlignment = Alignment.CenterStart
    ) {
        TabItemMaverick(
            label = "Licitaciones",
            description = "Concursos públicos",
            icon = "⚖️",
            isActive = currentTab == BudgetTabMode.LICITACIONES,
            isLicitacion = true,
            modifier = Modifier.offset(x = licOffset),
            onClick = { onTabSelected(BudgetTabMode.LICITACIONES) }
        )
        TabItemMaverick(
            label = "Presupuesto Directos",
            description = "Presupuestos de Chats",
            icon = "📩",
            isActive = currentTab == BudgetTabMode.DIRECTOS,
            isLicitacion = false,
            modifier = Modifier.offset(x = dirOffset),
            onClick = { onTabSelected(BudgetTabMode.DIRECTOS) }
        )
        DividerPremium(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun TabItemMaverick(
    label: String,
    description: String,
    icon: String,
    isActive: Boolean,
    isLicitacion: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val circleSize by animateDpAsState(if (isActive) 56.dp else 40.dp, label = "size")
    Box(
        modifier = modifier
            .size(0.dp)
            .wrapContentSize(unbounded = true, align = Alignment.Center),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            if (isActive && isLicitacion) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 16.dp)) {
                    Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(description, color = MaverickBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            IconCircleMaverick(icon, isActive, circleSize)
            if (isActive && !isLicitacion) {
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(start = 16.dp)) {
                    Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(description, color = MaverickBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun IconCircleMaverick(icon: String, isActive: Boolean, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isActive) MaverickBlue.copy(0.15f) else Color.White.copy(0.05f))
            .border(1.5.dp, if (isActive) MaverickBlue else Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = (size.value * 0.45f).sp,
            modifier = Modifier.alpha(if (isActive) 1f else 0.3f)
        )
    }
}
