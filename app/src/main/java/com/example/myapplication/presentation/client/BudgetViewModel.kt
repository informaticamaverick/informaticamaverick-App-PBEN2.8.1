package com.example.myapplication.presentation.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.*
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.presentation.components.BeSmallActionModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class AnalyticsState(
    val items: List<ChartBudgetItem> = emptyList(),
    val avgTotal: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val validCount: Int = 0,
    val isAnalyzing: Boolean = true
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // ==========================================================
    // 1. ESTADOS DE BUSQUEDA Y SELECCION (Delegados)
    // ==========================================================
    private val _searchQueryFromBe = MutableStateFlow("") 
    val searchQuery = _searchQueryFromBe.asStateFlow()

    private val _activeFiltersFromBe = MutableStateFlow<Set<String>>(emptySet()) 

    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive = _isMultiSelectionActive.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    private val _currentHUDContext = MutableStateFlow(HUDContext.BUDGETS)
    val currentHUDContext = _currentHUDContext.asStateFlow()

    private val _selectedTenderId = MutableStateFlow<String?>(null)
    fun setSelectedTenderId(id: String?) { 
        _selectedTenderId.value = id 
    }

    fun setContext(context: HUDContext) {
        // Si el contexto cambia (por ejemplo de TENDERS a DIRECT), reseteamos la UI
        if (_currentHUDContext.value != context) {
            resetPageState()
        }
        _currentHUDContext.value = context
    }

    /**
     * Resetea filtros, búsqueda y multiselección de esta pantalla específica
     */
    fun resetPageState() {
        _isMultiSelectionActive.value = false
        _selectedIds.value = emptySet()
        _searchQueryFromBe.value = ""
        // Si quieres resetear también los filtros tácticos:
        _activeFiltersFromBe.value = emptySet()
    }


    // ==========================================================
    // 2. DATOS FILTRADOS Y ORDENADOS
    // ==========================================================
    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets 
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTenders: StateFlow<List<TenderEntity>> = repository.allTenders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTenders: StateFlow<List<TenderEntity>> = combine(
        allTenders,
        _searchQueryFromBe,
        _activeFiltersFromBe,
        allBudgets 
    ) { tenders, query, activeFilters, allBudgetsList ->
        var list = if (query.isNotEmpty()) {
            val normalized = query.prepareForSearch()
            tenders.filter { it.title.prepareForSearch().wordStartsWith(normalized) }
        } else {
            tenders
        }

        val catFilters = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
        if (catFilters.isNotEmpty()) {
            list = list.filter { tender -> 
                catFilters.any { it.equals(tender.category, ignoreCase = true) }
            }
        }

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

        list = list.sortedWith(compareBy<TenderEntity> { tender ->
            when (tender.status) {
                "ABIERTA" -> 1
                "ADJUDICADA" -> 2
                "CERRADA" -> 3
                "CANCELADA" -> 4
                else -> 5
            }
        }.thenByDescending { tender ->
            if (tender.status == "ABIERTA") {
                allBudgetsList.any { it.tenderId == tender.tenderId && !it.isRead }
            } else false
        }.thenByDescending { it.dateTimestamp })

        if (activeFilters.contains("sort_alpha")) list = list.sortedBy { it.title }
        if (activeFilters.contains("sort_date")) list = list.sortedByDescending { it.dateTimestamp }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDirectBudgets: StateFlow<List<BudgetEntity>> = combine(
        repository.directBudgets,
        _searchQueryFromBe,
        _activeFiltersFromBe
    ) { budgets, query, activeFilters ->
        applyBudgetFilters(budgets, query, activeFilters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredOverlayBudgets: StateFlow<List<BudgetEntity>> = _selectedTenderId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getBudgetsForTender(id).combine(combine(_searchQueryFromBe, _activeFiltersFromBe) { q, f -> q to f }) { budgets, params ->
                applyBudgetFilters(budgets, params.first, params.second)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getFilteredBudgetsForTender(tenderId: String): StateFlow<List<BudgetEntity>> {
        return combine(repository.getBudgetsForTender(tenderId), _searchQueryFromBe, _activeFiltersFromBe) { budgets, query, activeFilters ->
            applyBudgetFilters(budgets, query, activeFilters)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private fun applyBudgetFilters(budgets: List<BudgetEntity>, query: String, activeFilters: Set<String>): List<BudgetEntity> {
        var list = if (query.isNotEmpty()) {
            val normalized = query.prepareForSearch()
            budgets.filter { 
                it.providerName.prepareForSearch().wordStartsWith(normalized) || 
                (it.providerCompanyName?.prepareForSearch()?.wordStartsWith(normalized) ?: false) || 
                it.grandTotal.toString().startsWith(normalized) || 
                it.budgetId.prepareForSearch().wordStartsWith(normalized)
            }
        } else {
            budgets
        }

        val catFilters = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
        if (catFilters.isNotEmpty()) {
            list = list.filter { budget -> 
                catFilters.any { it.equals(budget.category, ignoreCase = true) }
            }
        }

        list = list.sortedWith(budgetComparator())

        if (activeFilters.contains("sort_alpha")) list = list.sortedBy { it.providerName }
        if (activeFilters.contains("sort_date")) list = list.sortedByDescending { it.dateTimestamp }
        if (activeFilters.contains("sort_price")) list = list.sortedBy { it.grandTotal }
        return list
    }

    private fun budgetComparator() = compareByDescending<BudgetEntity> { !it.isRead && it.status == BudgetStatus.PENDIENTE }
        .thenByDescending { it.status == BudgetStatus.PENDIENTE }
        .thenByDescending { it.status == BudgetStatus.ACEPTADO }
        .thenByDescending { it.status == BudgetStatus.RECHAZADO || it.status == BudgetStatus.VENCIDO }
        .thenByDescending { it.dateTimestamp }

    // ==========================================================
    // 3. FLUJO DE ACCIONES DINÁMICAS
    // ==========================================================
    val beActions: StateFlow<List<BeSmallActionModel>> = combine(
        _isMultiSelectionActive,
        _selectedIds,
        _currentHUDContext
    ) { isMulti, selected, context ->
        val actions = mutableListOf<BeSmallActionModel>()
        val count = selected.size

        if (isMulti) {
            if (context == HUDContext.BUDGETS_TENDERS) {
                // Requerimiento Licitaciones: cerrar, divider vertical, detalles (solo si count == 1), eliminar
                actions.add(BeSmallActionModel("cancel", Icons.Default.Close, "Cerrar") { })
                actions.add(BeSmallActionModel("divider_v_1", Icons.Default.VerticalAlignBottom, "Divider") { })

                // Icono de detalles solo si hay una seleccionada
                if (count == 1) {
                    actions.add(BeSmallActionModel("view_tender_details", Icons.AutoMirrored.Filled.Assignment, "Detalles", emoji = "📋") { })
                    actions.add(BeSmallActionModel("divider_v_2", Icons.Default.VerticalAlignBottom, "Divider") { })
                }

                actions.add(BeSmallActionModel("delete_multi", Icons.Default.Delete, "Eliminar", tint = Color.Red) { })
            } else {
                // Requerimiento Presupuestos Directos: cerrar, divider vertical, comparar(solo si > 1), divider vertical, Todos, leidos, divider vertical, eliminar
                actions.add(BeSmallActionModel("cancel", Icons.Default.Close, "Cerrar") { })
                actions.add(BeSmallActionModel("divider_v_1", Icons.Default.VerticalAlignBottom, "Divider") { }) 
                
                if (count > 1) {
                    actions.add(BeSmallActionModel("compare_selected", Icons.AutoMirrored.Filled.CompareArrows, "Comparar", emoji = "⚖️") { })
                    actions.add(BeSmallActionModel("divider_v_2", Icons.Default.VerticalAlignBottom, "Divider") { })
                }

                // Unificamos IDs para que PresupuestosScreen los capture globalmente
                actions.add(BeSmallActionModel("select_all", Icons.Default.SelectAll, "Todos", emoji = "✅") { })
                actions.add(BeSmallActionModel("mark_as_read", Icons.Default.DoneAll, "Leídos", emoji = "📖") { })
                actions.add(BeSmallActionModel("divider_v_3", Icons.Default.VerticalAlignBottom, "Divider") { })
                actions.add(BeSmallActionModel("delete_multi", Icons.Default.Delete, "Eliminar", tint = Color.Red) { })
            }
        } else {

            when (context) {
                HUDContext.TENDER_DETAILS -> {
                    actions.add(
                        BeSmallActionModel(
                            id = "compare_all",
                            icon = Icons.AutoMirrored.Filled.CompareArrows,
                            label = "Comparar Todo",
                            emoji = "⚖️",
                            isDefault = true
                        ) {
                            // triggerAction se maneja en el LaunchedEffect de la Screen
                        }
                    )
                }
                else -> {}
            }

        }
        actions
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================================
    // 4. MÉTODOS DE DELEGACIÓN (Acciones)
    // ==========================================================
    fun setSearchQuery(query: String) { _searchQueryFromBe.value = query }
    fun setFilters(filters: Set<String>) { _activeFiltersFromBe.value = filters }
    
    fun updateMultiSelection(active: Boolean) {
        _isMultiSelectionActive.value = active
        if (!active) _selectedIds.value = emptySet()
    }

    fun toggleSelection(id: String) {
        val current = _selectedIds.value.toMutableSet()
        if (!current.add(id)) current.remove(id)
        _selectedIds.value = current
    }

    fun selectAll(ids: List<String>) {
        _selectedIds.value = ids.toSet()
    }

    // --- Lógica de Negocio ---
    fun createTender(
        title: String,
        description: String,
        category: String,
        startDate: Long,
        endDate: Long,
        requiresVisit: Boolean,
        requiresPaymentMethod: Boolean,
        requiresWorkGuarantee: Boolean,
        requiresProviderDoc: Boolean,
        location: LocationOption?,
        imageUrls: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"

            val (addr, num, loc, type) = when (location) {
                is LocationOption.Personal -> listOf(location.address, location.number, location.locality, "PERSONAL")
                is LocationOption.Business -> listOf(location.address, location.number, location.locality, "BUSINESS")
                else -> listOf(null, null, null, null)
            }

            val newTender = TenderEntity(
                tenderId = UUID.randomUUID().toString(),
                clientId = currentUserId,
                title = title,
                description = description,
                category = category,
                startDate = startDate,
                endDate = endDate,
                requiresVisit = requiresVisit,
                requiresPaymentMethod = requiresPaymentMethod,
                requiresWorkGuarantee = requiresWorkGuarantee,
                requiresProviderDoc = requiresProviderDoc,
                locationAddress = addr as? String,
                locationNumber = num as? String,
                locationLocality = loc as? String,
                locationType = type as? String,
                imageUrls = imageUrls,
                isActive = true
            )
            repository.createNewTender(newTender)
        }
    }

    /**
     * Actualiza el estado de una licitación.
     */
    fun updateTenderStatus(tenderId: String, newStatus: String) {
        viewModelScope.launch {
            val tender = allTenders.value.find { it.tenderId == tenderId }
            tender?.let {
                val updated = it.copy(
                    status = newStatus,
                    cancellationDate = if (newStatus == "CANCELADA") System.currentTimeMillis() else it.cancellationDate,
                    isActive = when(newStatus) {
                        "ABIERTA" -> it.budgetCount < 100
                        else -> false
                    }
                )
                repository.createNewTender(updated)
            }
        }
    }

    fun cancelTender(tender: TenderEntity) {
        viewModelScope.launch {
            val updated = tender.copy(
                status = "CANCELADA",
                cancellationDate = System.currentTimeMillis(),
                isActive = false
            )
            repository.createNewTender(updated)
        }
    }

    fun markAsRead(ids: Set<String>) {
        viewModelScope.launch {
            ids.forEach { repository.markBudgetAsRead(it) }
        }
    }

    fun deleteBudgets(ids: Set<String>) {
        viewModelScope.launch {
            ids.forEach { repository.removeBudget(it) }
            updateMultiSelection(false)
        }
    }

    fun deleteTenders(ids: Set<String>) {
        viewModelScope.launch {
            ids.forEach { repository.removeTender(it) }
            updateMultiSelection(false)
        }
    }

    fun acceptBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.updateBudgetStatus(budget.budgetId, BudgetStatus.ACEPTADO)
            sendDecisionMessage(budget, "✅ ¡Hola! He ACEPTADO el presupuesto #${budget.budgetId.takeLast(4)}...")
        }
    }

    fun rejectBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.updateBudgetStatus(budget.budgetId, BudgetStatus.RECHAZADO)
            sendDecisionMessage(budget, "❌ Hola. He decidido RECHAZAR el presupuesto #${budget.budgetId.takeLast(4)}...")
        }
    }

    private suspend fun sendDecisionMessage(budget: BudgetEntity, text: String) {
        val chatId = "chat_${budget.clientId}_${budget.providerId}"
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = budget.clientId,
            receiverId = budget.providerId,
            type = MessageType.TEXT,
            content = text,
            timestamp = System.currentTimeMillis(),
            status = "SENT"
        )
        chatRepository.sendMessage(message)
    }

    // ==========================================================
    // 5. ANALÍTICA (Fase 5)
    // ==========================================================
    private val _analyticsState = MutableStateFlow(AnalyticsState())
    val analyticsState: StateFlow<AnalyticsState> = _analyticsState.asStateFlow()

    fun analyzeBudgets(budgets: List<BudgetEntity>) {
        viewModelScope.launch(Dispatchers.Default) {
            _analyticsState.value = _analyticsState.value.copy(isAnalyzing = true)
            if (budgets.isEmpty()) {
                _analyticsState.value = AnalyticsState(isAnalyzing = false)
                return@launch
            }
            val mapped = budgets.map { budget ->
                val mat = budget.items.sumOf { it.unitPrice * it.quantity }
                val lab = budget.services.sumOf { it.total }
                val tax = budget.taxAmount
                val total = mat + lab + tax
                val isIrr = total !in 15000.0..200000.0
                ChartBudgetItem(budget, total, mat, lab, tax, isIrr, false)
            }
            val validItems = mapped.filter { !it.isIrrisory }
            val avg = if (validItems.isNotEmpty()) validItems.map { it.total }.average() else 0.0
            val optMin = avg * 0.85
            val optMax = avg * 1.15
            val finalItems = mapped.map {
                it.copy(isOptimal = !it.isIrrisory && it.total in optMin..optMax)
            }.sortedBy { it.total }
            _analyticsState.value = AnalyticsState(
                items = finalItems, avgTotal = avg,
                minPrice = validItems.minOfOrNull { it.total } ?: 0.0,
                maxPrice = validItems.maxOfOrNull { it.total } ?: 0.0,
                validCount = validItems.size, isAnalyzing = false
            )
        }
    }
}
// Extensiones para la búsqueda inteligente, compartidas con otros ViewModels.
private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
fun String.prepareForSearch(): String {
    val temp = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "").lowercase().trim()
}
fun String.wordStartsWith(query: String): Boolean {
    if (query.isEmpty()) return false
    val normalizedText = this.prepareForSearch()
    return normalizedText.split(" ").any { it.startsWith(query) }
}

fun String.matchesSmart(query: String): Boolean {
    if (query.isEmpty()) return false
    val normalizedText = this.prepareForSearch()
    val queryWords = query.prepareForSearch().split(" ").filter { it.isNotEmpty() }
    val textWords = normalizedText.split(" ").filter { it.isNotEmpty() }
    return queryWords.all { qw ->
        textWords.any { tw -> tw.startsWith(qw) }
    }
}
fun CategoryEntity.matches(normalizedQuery: String): Boolean = this.name.prepareForSearch().wordStartsWith(normalizedQuery)
