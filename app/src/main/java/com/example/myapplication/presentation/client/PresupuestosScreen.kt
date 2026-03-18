package com.example.myapplication.presentation.client

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.preference.isNotEmpty
import coil.compose.AsyncImage
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.myapplication.presentation.client.prepareForSearch
import com.example.myapplication.presentation.client.wordStartsWith

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
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    
    // Estados reactivos de Be para filtrado dinámico
    val activeFilters by beBrainViewModel.activeFilters.collectAsStateWithLifecycle()
    val dynamicCategories by beBrainViewModel.dynamicCategories.collectAsStateWithLifecycle()
    val availableFilters by beBrainViewModel.availableFilters.collectAsStateWithLifecycle()
    val availableSortOptions by beBrainViewModel.availableSortOptions.collectAsStateWithLifecycle()
    val isSearchActive by beBrainViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchResults by beBrainViewModel.searchResults.collectAsStateWithLifecycle()

    // Captura el texto de búsqueda actual de Be
    val searchQuery by beBrainViewModel.searchQuery.collectAsStateWithLifecycle()

    // 🔥 CONEXIÓN DE CABLES: Enviamos los datos a Be para que filtre categorías por contexto
    LaunchedEffect(categories, tenders, directBudgets) {
        beBrainViewModel.updateAllCategories(categories)
        beBrainViewModel.updateTenders(tenders)
        beBrainViewModel.updateBudgets(directBudgets)
    }

    PresupuestosScreenContent(
        tenders = tenders,
        directBudgets = directBudgets,
        categories = categories,
        activeFilters = activeFilters,
        dynamicCategories = dynamicCategories,
        refinementFilters = availableFilters,
        sortOptions = availableSortOptions,
        onFilterToggle = { beBrainViewModel.toggleFilter(it) },
        // 🔥 Corregido: Limpieza específica para cada menú
        onClearFilters = { beBrainViewModel.clearSpecificFilters(listOf("filter_", "cat_")) },
        onClearSort = { beBrainViewModel.clearSpecificFilters(listOf("sort_", "view_")) },
        onSetContext = { beBrainViewModel.setHUDContext(it) },
        getBudgetsForTender = { tenderId -> viewModel.getBudgetsForTender(tenderId) },
        onChatClick = onChatClick,
        onBack = onBack,
        onAcceptBudget = { budget -> viewModel.acceptBudget(budget) },
        onRejectBudget = { budget -> viewModel.rejectBudget(budget) },
        bottomPadding = bottomPadding,
        isSearchActive = isSearchActive,
        searchResults = searchResults,
        onCloseBeAssistant = { beBrainViewModel.cerrarBeAssistantCompleto() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreenContent(
    tenders: List<TenderEntity>,
    directBudgets: List<BudgetEntity>,
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
    bottomPadding: PaddingValues,
    isSearchActive: Boolean = false,
    searchResults: BeBrainViewModel.SearchResult = BeBrainViewModel.SearchResult.Empty,
    onCloseBeAssistant: () -> Unit = {},
    searchQuery: String = ""
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(BudgetTabMode.LICITACIONES) }

    // Sincronización de Contexto HUD según la solapa activa
    LaunchedEffect(pagerState.currentPage) {
        currentTab = if (pagerState.currentPage == 0) BudgetTabMode.LICITACIONES else BudgetTabMode.DIRECTOS
        onSetContext(if (currentTab == BudgetTabMode.LICITACIONES) HUDContext.BUDGETS_TENDERS else HUDContext.BUDGETS_DIRECT)
    }

    // Estados de UI
    var selectedTenderForSheet by remember { mutableStateOf<TenderEntity?>(null) }
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var providerProfileToShow by remember { mutableStateOf<BudgetEntity?>(null) }
    val readBudgetIds = remember { mutableStateListOf<String>() }

    // Backup para mantener la UI estable durante la animación de salida
    var lastSelectedTenderForExit by remember { mutableStateOf<TenderEntity?>(null) }
    if (selectedTenderForSheet != null) {
        lastSelectedTenderForExit = selectedTenderForSheet
    }

    // Sincronización de contexto al abrir/cerrar el overlay de resultados
    LaunchedEffect(selectedTenderForSheet) {
        if (selectedTenderForSheet != null) {
            onSetContext(HUDContext.BUDGETS_DIRECT) 
        } else {
            onSetContext(if (currentTab == BudgetTabMode.LICITACIONES) HUDContext.BUDGETS_TENDERS else HUDContext.BUDGETS_DIRECT)
        }
    }

    // =================================================================================
    // 🧠 LÓGICA DE FILTRADO REAL (CONECTADA A LAS CATEGORÍAS DE BE)
    // =================================================================================

    // Filtrado de Licitaciones
    val filteredTenders = remember(tenders, activeFilters, isSearchActive, searchResults) {
        var list = if (isSearchActive && searchResults is BeBrainViewModel.SearchResult.TenderMatch) {
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
        // Aplicar Ordenamiento
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
                val budgetCatMatches = budget.category?.lowercase() in catFilters
                
                // Prioridad 2: Fallback por providerId en las categorías del sistema
                val providerMatches = categories.filter { it.name.lowercase() in catFilters }
                                                .any { it.providerIds.contains(budget.providerId) }
                
                budgetCatMatches || providerMatches
            }
        }
        // Aplicar Ordenamiento
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
                                Text("Panel Comercial", fontWeight = FontWeight.Black, color = Color.White, fontSize = 17.sp)
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
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {

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
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
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
                                Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.15f)))
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
                                    val unreadCount = budgets.count { !readBudgetIds.contains(it.budgetId) }

                                    val categoryInfo = categories.find { it.name.equals(tender.category, ignoreCase = true) }

                                    LicitacionFolderPremium(
                                        title = tender.title,
                                        tenderId = tender.tenderId,
                                        status = tender.status,
                                        startDate = tender.dateTimestamp,
                                        endDate = tender.endDate,
                                        budgetCount = budgets.size,
                                        unreadCount = unreadCount,
                                        isSelected = false,
                                        category = tender.category,
                                        categoryIcon = categoryInfo?.icon ?: "📋",
                                        categoryColor = categoryInfo?.color?.let { Color(it) } ?: Color.Gray,
                                        onClick = { 
                                            onCloseBeAssistant()
                                            selectedTenderForSheet = tender 
                                        }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        } else {
                            // Contenido de Directos Filtrado
                            BudgetGridContent(
                                budgets = filteredDirectBudgets,
                                readBudgetIds = readBudgetIds,
                                onBudgetClick = { budget ->
                                    readBudgetIds.add(budget.budgetId)
                                    budgetForA4Preview = budget
                                },
                                onChatClick = onChatClick,
                                onAvatarClick = { budget -> providerProfileToShow = budget }
                            )
                        }
                    }
                }
            }
        }

        // --- OVERLAY: COMPARATIVA ---
        AnimatedVisibility(
            visible = selectedTenderForSheet != null,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeOut(),
            modifier = Modifier.zIndex(50f)
        ) {
            lastSelectedTenderForExit?.let { tender ->
                val tenderBudgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
                val budgets by tenderBudgetsFlow.collectAsStateWithLifecycle(emptyList())

                // 🔥 LÓGICA UNIFICADA: Filtrado por búsqueda de Be + Ordenamiento
                val sortedAndFilteredBudgets = remember(budgets, activeFilters, isSearchActive, searchResults, searchQuery) {
                    // 1. Primero filtramos por búsqueda de Be (si hay texto escrito)
                    var list = if (searchQuery.isNotEmpty()) {
                        val normalized = searchQuery.prepareForSearch()
                        budgets.filter { budget ->
                            budget.providerName.wordStartsWith(normalized) ||
                                    (budget.providerCompanyName?.wordStartsWith(normalized) ?: false)
                        }
                    } else if (isSearchActive && searchResults is BeBrainViewModel.SearchResult.BudgetMatch) {
                        // Si Be devuelve un match específico de presupuestos
                        val searchedIds = searchResults.budgets.map { it.budgetId }.toSet()
                        budgets.filter { it.budgetId in searchedIds }
                    } else {
                        budgets
                    }

                    // 2. Luego aplicamos los ordenamientos tácticos
                    if (activeFilters.contains("sort_alpha")) list = list.sortedBy { it.providerName }
                    if (activeFilters.contains("sort_date")) list = list.sortedByDescending { it.dateTimestamp }
                    if (activeFilters.contains("sort_price")) list = list.sortedBy { it.grandTotal }

                    list
                }

                // --- LLAMADA ÚNICA AL COMPONENTE ---
                ComparisonSheetEdgeToEdge(
                    tender = tender,
                    budgets = sortedAndFilteredBudgets, // <--- Usamos la lista procesada arriba
                    readBudgetIds = readBudgetIds,
                    activeFilters = activeFilters,
                    dynamicCategories = dynamicCategories,
                    refinementFilters = refinementFilters,
                    sortOptions = sortOptions,
                    onFilterToggle = onFilterToggle,
                    onClearFilters = onClearFilters,
                    onClearSort = onClearSort,
                    onBack = { selectedTenderForSheet = null },
                    onBudgetClick = { budget ->
                        readBudgetIds.add(budget.budgetId)
                        budgetForA4Preview = budget
                    },
                    onChatClick = onChatClick,
                    onAvatarClick = { budget -> providerProfileToShow = budget }
                )
            }
        }



  /**
        // --- OVERLAY: COMPARATIVA ---
        AnimatedVisibility(
            visible = selectedTenderForSheet != null,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeOut(),
            modifier = Modifier.zIndex(50f)
        ) {
            lastSelectedTenderForExit?.let { tender ->
                val tenderBudgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
                val budgets by tenderBudgetsFlow.collectAsStateWithLifecycle(emptyList())

                // Lógica de ordenamiento para el detalle de la licitación (incluyendo búsqueda de Be)
                val sortedTenderBudgets = remember(budgets, activeFilters, isSearchActive, searchResults) {
                    var list = if (isSearchActive && searchResults is BeBrainViewModel.SearchResult.BudgetMatch) {
                        val searchedIds = searchResults.budgets.map { it.budgetId }.toSet()
                        budgets.filter { it.budgetId in searchedIds }
                    } else {
                        budgets
                    }

                    if (activeFilters.contains("sort_alpha")) list = list.sortedBy { it.providerName }
                    if (activeFilters.contains("sort_date")) list = list.sortedByDescending { it.dateTimestamp }
                    if (activeFilters.contains("sort_price")) list = list.sortedBy { it.grandTotal }
                    list
                }

                ComparisonSheetEdgeToEdge(
                    tender = tender,
                    budgets = sortedTenderBudgets,
                    readBudgetIds = readBudgetIds,
                    activeFilters = activeFilters,
                    dynamicCategories = dynamicCategories,
                    refinementFilters = refinementFilters,
                    sortOptions = sortOptions,
                    onFilterToggle = onFilterToggle,
                    onClearFilters = onClearFilters,
                    onClearSort = onClearSort,
                    onBack = { selectedTenderForSheet = null },
                    onBudgetClick = { budget ->
                        readBudgetIds.add(budget.budgetId)
                        budgetForA4Preview = budget
                    },
                    onChatClick = onChatClick,
                    onAvatarClick = { budget -> providerProfileToShow = budget }
                )
            }
        }
**/
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
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = providerProfileToShow?.providerPhotoUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, MaverickBlue, CircleShape),
                        contentScale = ContentScale.Crop,
                        fallback = rememberVectorPainter(Icons.Default.Person)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(providerProfileToShow?.providerName ?: "", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(providerProfileToShow?.providerCompanyName ?: "Profesional", color = MaverickBlue, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaverickBlue), shape = RoundedCornerShape(12.dp)) {
                        Text("VER PERFIL COMPLETO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =================================================================================
// --- COMPONENTES DE UI ---
// =================================================================================

/**
 * Divider Premium con degradado horizontal para separar secciones visualmente.
 */
@Composable
fun DividerPremium(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Header de estadísticas simplificado.
 */
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

/**
 * Tabs con comportamiento dinámico. El activo se centra, el inactivo se asoma un 50% en el borde.
 */
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
        modifier = Modifier.fillMaxWidth().height(95.dp).background(DarkBackground).zIndex(10f),
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
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
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

@Composable
fun BudgetGridContent(
    budgets: List<BudgetEntity>,
    readBudgetIds: List<String>,
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String) -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit
) {
    if (budgets.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin ofertas registradas", color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(budgets, key = { it.budgetId }) { budget ->
                TarjetaPresupuestoPremium(
                    providerName = budget.providerName,
                    companyName = budget.providerCompanyName ?: "Independiente",
                    amount = budget.grandTotal,
                    budgetId = budget.budgetId,
                    photoUrl = budget.providerPhotoUrl,
                    isOnline = true,
                    isSubscribed = true,
                    isSelected = readBudgetIds.contains(budget.budgetId),
                    onViewClick = { onBudgetClick(budget) },
                    onChatClick = { onChatClick(budget.providerId) }
                )
            }
        }
    }
}

@Composable
fun ComparisonSheetEdgeToEdge(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    readBudgetIds: List<String>,
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    refinementFilters: List<ControlItem>,
    sortOptions: List<ControlItem>,
    onFilterToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClearSort: () -> Unit,
    onBack: () -> Unit,
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String) -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit
) {
    Column(Modifier.fillMaxSize().background(DarkBackground)) {
        Surface(color = DarkBackground, modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Column(Modifier.weight(1f)) {
                    Text("Ofertas Recibidas", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text(tender.title, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MenuFiltros(
                        activeFilters = activeFilters,
                        dynamicCategories = dynamicCategories,
                        refinementFilters = refinementFilters,
                        onAction = onFilterToggle,
                        onApply = {},
                        onClearFilters = onClearFilters
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.15f)))
                    Spacer(Modifier.width(6.dp))
                    MenuOrdenamiento(
                        activeFilters = activeFilters,
                        sortOptions = sortOptions,
                        onAction = onFilterToggle,
                        onApply = {},
                        onClearFilters = onClearSort
                    )
                }
            }
        }
        DividerPremium()
        BudgetGridContent(budgets, readBudgetIds, onBudgetClick, onChatClick, onAvatarClick)
    }
}

@Preview(showBackground = true)
@Composable
fun PresupuestosScreenPreview() {
    val sampleTenders = listOf(
        TenderEntity(
            tenderId = "T-001",
            title = "Reparación de Aire Acondicionado",
            description = "El aire no enfría y hace un ruido extraño.",
            category = "Climatización",
            status = "ABIERTA",
            dateTimestamp = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 86400000 * 7,
            budgetCount = 2
        ),
        TenderEntity(
            tenderId = "T-002",
            title = "Instalación Eléctrica Cocina",
            description = "Instalación de tomacorrientes y luminarias.",
            category = "Electricidad",
            status = "CERRADA",
            dateTimestamp = System.currentTimeMillis() - 86400000,
            endDate = System.currentTimeMillis() + 86400000 * 2,
            budgetCount = 5
        )
    )

    val sampleBudgets = listOf(
        BudgetEntity(
            budgetId = "B-001",
            clientId = "user123",
            providerId = "prov1",
            providerName = "Juan Pérez",
            providerCompanyName = "ClimaCool S.A.",
            grandTotal = 1500.0,
            status = BudgetStatus.PENDIENTE
        ),
        BudgetEntity(
            budgetId = "B-002",
            clientId = "user123",
            providerId = "prov2",
            providerName = "Marta Gómez",
            providerCompanyName = "Electricidad Gómez",
            grandTotal = 2500.0,
            status = BudgetStatus.PENDIENTE
        )
    )

    val sampleCategories = listOf(
        CategoryEntity(name = "Climatización", icon = "❄️", color = 0xFF2196F3, superCategory = "Hogar", imageUrl = null, isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Electricidad", icon = "⚡", color = 0xFFFFEB3B, superCategory = "Hogar", imageUrl = null, isNew = false, isNewPrestador = false, isAd = false)
    )

    MyApplicationTheme(darkTheme = true) {
        PresupuestosScreenContent(
            tenders = sampleTenders,
            directBudgets = sampleBudgets,
            categories = sampleCategories,
            activeFilters = emptySet(),
            dynamicCategories = emptyList(),
            refinementFilters = emptyList(),
            sortOptions = emptyList(),
            onFilterToggle = {},
            onClearFilters = {},
            onClearSort = {},
            onSetContext = {},
            getBudgetsForTender = { _ -> MutableStateFlow(sampleBudgets) },
            onChatClick = {},
            onBack = {},
            onAcceptBudget = {},
            onRejectBudget = {},
            bottomPadding = PaddingValues(0.dp)
        )
    }
}
