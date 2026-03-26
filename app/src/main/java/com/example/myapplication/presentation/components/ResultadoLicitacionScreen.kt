package com.example.myapplication.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.presentation.client.BeBrainViewModel
import com.example.myapplication.presentation.client.HUDContext
import com.example.myapplication.presentation.client.prepareForSearch
import com.example.myapplication.presentation.client.wordStartsWith
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

private val DarkBackground = Color(0xFF05070A)

@Composable
fun ResultadoLicitacionOverlay(
    selectedTender: TenderEntity?,
    onClose: () -> Unit,
    beBrainViewModel: BeBrainViewModel,
    getBudgetsForTender: (String) -> StateFlow<List<BudgetEntity>>,
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    refinementFilters: List<ControlItem>,
    sortOptions: List<ControlItem>,
    onFilterToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClearSort: () -> Unit,
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String) -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit,
    isMultiSelectionActive: Boolean,
    selectedItemIds: Set<String>,
    onToggleItemSelection: (String) -> Unit,
    onToggleMultiSelection: () -> Unit,
    onAnalyticsClick: (TenderEntity, List<BudgetEntity>) -> Unit,
    onDeleteBudgets: (Set<String>) -> Unit,
    onMarkAsReadMulti: (Set<String>) -> Unit = {},
    onSetContext: (HUDContext) -> Unit,
    showDeleteConfirmDialog: (String, () -> Unit) -> Unit
) {
    // Backup para mantener la UI estable durante la animación de salida
    var lastSelectedTenderForExit by remember { mutableStateOf<TenderEntity?>(null) }
    if (selectedTender != null) {
        lastSelectedTenderForExit = selectedTender
    }

    LaunchedEffect(selectedTender) {
        if (selectedTender != null) {
            beBrainViewModel.setHUDContext(HUDContext.TENDER_DETAILS)
            onSetContext(HUDContext.TENDER_DETAILS)
        }
    }

    // Al cerrar, debemos restaurar el contexto (usando DisposableEffect)
    DisposableEffect(selectedTender) {
        onDispose {
            if (selectedTender == null) {
                // Solo si realmente se cerró, volvemos al estado de lista
                onSetContext(HUDContext.BUDGETS_TENDERS)
            }
        }
    }

    AnimatedVisibility(
        visible = selectedTender != null,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeOut(),
        modifier = Modifier.zIndex(50f)
    ) {
        lastSelectedTenderForExit?.let { tender ->
            BackHandler(enabled = isMultiSelectionActive || selectedTender != null) {
                if (isMultiSelectionActive) {
                    onToggleMultiSelection()
                } else {
                    onClose()
                }
            }

            val tenderBudgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
            val budgets by tenderBudgetsFlow.collectAsStateWithLifecycle(emptyList())

            LaunchedEffect(tender, budgets, selectedItemIds) {
                beBrainViewModel.actionEvent.collect { actionId: String ->
                    when (actionId) {
                        "compare_all" -> {
                            onAnalyticsClick(tender, budgets.sortedBy { it.providerName.lowercase(Locale.getDefault()) })

                            //***************** 🔥 REQUERIMIENTO: Al tocar el botón de Be, comparamos TODOS los presupuestos
                            // Usamos la lista 'budgets' que ya viene del Flow de esta licitación
                          //  if (budgets.isNotEmpty()) {
                           //     onAnalyticsClick(tender, budgets.sortedBy { it.providerName.lowercase() })
                           // }
                        //****************************************************************************

                        }
                        "compare_selected" -> {
                            val selectedBudgets = budgets.filter { it.budgetId in selectedItemIds }
                            if (selectedBudgets.isNotEmpty()) {
                                onAnalyticsClick(tender, selectedBudgets.sortedBy { it.providerName.lowercase(Locale.getDefault()) })
                            }
                        }
                        "delete_selected" -> {
                            showDeleteConfirmDialog("¿Deseas eliminar las ofertas seleccionadas de esta licitación?") {
                                onDeleteBudgets(selectedItemIds)
                            }
                        }
                        "select_all_budgets" -> {
                            // Este ID es legado, ahora se usa select_all desde PresupuestosScreen
                        }
                        "mark_as_read_multi" -> {
                            if (selectedItemIds.isNotEmpty()) {
                                onMarkAsReadMulti(selectedItemIds)
                            }
                        }
                    }
                }
            }

            // Lógica de búsqueda y ordenamiento centralizada (A-Z por defecto)
            val searchQuery by beBrainViewModel.searchQuery.collectAsStateWithLifecycle()
            val isSearchActive by beBrainViewModel.isSearchActive.collectAsStateWithLifecycle()
            val searchResults by beBrainViewModel.searchResults.collectAsStateWithLifecycle()

            val sortedAndFilteredBudgets = remember(budgets, activeFilters, isSearchActive, searchResults, searchQuery) {
                var list = if (searchQuery.isNotEmpty()) {
                    val normalized = searchQuery.prepareForSearch()
                    budgets.filter { budget ->
                        budget.providerName.prepareForSearch().wordStartsWith(normalized) ||
                                (budget.providerCompanyName?.prepareForSearch()?.wordStartsWith(normalized) ?: false)
                    }
                } else if (isSearchActive && searchResults is BeBrainViewModel.SearchResult.BudgetMatch) {
                    val searchedIds = (searchResults as BeBrainViewModel.SearchResult.BudgetMatch).budgets.map { it.budgetId }.toSet()
                    budgets.filter { it.budgetId in searchedIds }
                } else {
                    budgets
                }
                // Ordenamiento alfabético por defecto o según filtros tácticos
                list = when {
                    activeFilters.contains("sort_date") -> list.sortedByDescending { it.dateTimestamp }
                    activeFilters.contains("sort_price") -> list.sortedBy { it.grandTotal }
                    else -> list.sortedBy { it.providerName.lowercase(Locale.getDefault()) } // A-Z por defecto
                }
                list
            }

            ComparisonSheetEdgeToEdge(
                tender = tender,
                budgets = sortedAndFilteredBudgets,
                activeFilters = activeFilters,
                dynamicCategories = dynamicCategories,
                refinementFilters = refinementFilters,
                sortOptions = sortOptions,
                onFilterToggle = onFilterToggle,
                onClearFilters = onClearFilters,
                onClearSort = onClearSort,
                onBack = {
                    if (isMultiSelectionActive) {
                        onToggleMultiSelection()
                    } else {
                        onClose()
                    }
                },
                onBudgetClick = onBudgetClick,
                onChatClick = onChatClick,
                onAvatarClick = { onAvatarClick(it) },
                isMultiSelectionActive = isMultiSelectionActive,
                selectedItemIds = selectedItemIds,
                onToggleItemSelection = onToggleItemSelection,
                onToggleMultiSelection = onToggleMultiSelection,
                onAnalyticsClick = {
                    onAnalyticsClick(tender, budgets.sortedBy { it.providerName.lowercase(Locale.getDefault()) })
                }
            )
        }
    }
}

@Composable
fun ComparisonSheetEdgeToEdge(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
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
    onAvatarClick: (BudgetEntity) -> Unit,
    isMultiSelectionActive: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onToggleItemSelection: (String) -> Unit = {},
    onToggleMultiSelection: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {}
) {
    Column(Modifier
        .fillMaxSize()
        .background(DarkBackground)) {
        Surface(color = DarkBackground, modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()) {
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
                    Box(modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(Color.White.copy(alpha = 0.15f)))
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
        BudgetGridContent(
            budgets = budgets,
            isMultiSelectionActive = isMultiSelectionActive,
            selectedItemIds = selectedItemIds,
            onToggleItemSelection = onToggleItemSelection,
            onBudgetClick = onBudgetClick,
            onChatClick = onChatClick,
            onToggleMultiSelection = onToggleMultiSelection,
            onAvatarClick = onAvatarClick
        )
    }
}

@Composable
fun BudgetGridContent(
    budgets: List<BudgetEntity>,
    isMultiSelectionActive: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onToggleItemSelection: (String) -> Unit = {},
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String) -> Unit,
    onToggleMultiSelection: () -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit
) {
    if (budgets.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin ofertas registradas", color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    } else {
        val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES")) }
        
        // Agrupar por fecha y ordenar dentro de cada grupo (no leídos primero)
        val groupedBudgets = remember(budgets) {
            budgets.groupBy {
                dateFormatter.format(Date(it.dateTimestamp))
            }.mapValues { entry ->
                // Ordenar: isRead = false primero, luego por timestamp descendente
                entry.value.sortedWith(compareBy<BudgetEntity> { it.isRead }.thenByDescending { it.dateTimestamp })
            }.toList().sortedByDescending { it.second.first().dateTimestamp }
        }

        val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            groupedBudgets.forEach { (dateText, budgetsInDate) ->
                val isExpanded = expandedStates[dateText] ?: true
                
                // Header de Fecha con Divider Premium y Flecha
                item(span = { GridItemSpan(maxLineSpan) }, key = "header_$dateText") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { 
                                expandedStates[dateText] = !isExpanded 
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DividerPremium(modifier = Modifier.weight(1f))
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateText.uppercase(),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        DividerPremium(modifier = Modifier.weight(1f))
                    }
                }

                if (isExpanded) {
                    items(budgetsInDate, key = { it.budgetId }) { budget ->
                        TarjetaPresupuestoPremium(
                            providerName = budget.providerName,
                            companyName = budget.providerCompanyName ?: "Independiente",
                            amount = budget.grandTotal,
                            budgetId = budget.budgetId,
                            photoUrl = budget.providerPhotoUrl,
                            isOnline = true,
                            isSubscribed = true,
                            isSelected = selectedItemIds.contains(budget.budgetId),
                            isRead = budget.isRead,
                            isMultiSelectionActive = isMultiSelectionActive,
                            onViewClick = { onBudgetClick(budget) },
                            onChatClick = { onChatClick(budget.providerId) },
                            onAvatarClick = { onAvatarClick(budget) },
                            onLongClick = {
                                if (!isMultiSelectionActive) {
                                    onToggleMultiSelection()
                                }
                                onToggleItemSelection(budget.budgetId)
                            }
                        )
                    }
                }
            }
        }
    }
}

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
