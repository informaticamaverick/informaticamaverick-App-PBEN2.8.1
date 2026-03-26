package com.example.myapplication.presentation.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.presentation.components.BeEmotion
import com.example.myapplication.presentation.components.BeMessage
import com.example.myapplication.presentation.components.BeSmallActionModel
import com.example.myapplication.presentation.components.ControlItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** * --- ENUM DE CONTEXTO DEL HUD ---
 * Define en qué sección de la app se encuentra el usuario. */
enum class HUDContext {
    HOME, BUDGETS, CHAT, CALENDAR, PROMO, UNKNOWN
}
/** * --- DICCIONARIO CENTRALIZADO DE BE --- */
object BeDictionary {
    val HomeMessages = listOf(
        BeMessage("💡", "Usa el Menú Táctico inferior para filtrar prestadores verificados.", null, Color(0xFF22D3EE), emotion = BeEmotion.NORMAL),
        BeMessage("🚀", "¡Nuevas categorías disponibles! Explora los servicios destacados hoy.", null, Color(0xFF10B981), emotion = BeEmotion.HAPPY)
    )
    val BudgetMessages = listOf(
        BeMessage("⚖️", "Selecciona múltiples ofertas para que yo pueda ayudarte a analizarlas y compararlas.", "ANALIZAR", Color(0xFF9B51E0), Color.White, BeEmotion.HAPPY),
        BeMessage("📋", "Recuerda revisar los detalles de cada presupuesto antes de aceptar.", null, Color(0xFFFACC15), emotion = BeEmotion.NORMAL)
    )
    val ChatMessages = listOf(
        BeMessage("💬", "Nunca compartas datos de tarjetas de crédito o contraseñas a través del chat.", null, Color(0xFFF43F5E), Color.White, BeEmotion.ANGRY),
        BeMessage("👀", "Si el prestador no responde, puedo ayudarte a buscar alternativas rápidas.", "BUSCAR", Color(0xFF22D3EE), emotion = BeEmotion.NORMAL)
    )
    val CalendarMessages = listOf(
        BeMessage("📅", "Recuerda que si cancelas un turno, el sistema le avisará automáticamente.", null, Color(0xFF10B981), emotion = BeEmotion.NORMAL),
        BeMessage("⏰", "Tienes turnos pendientes de confirmación. ¡No los pierdas!", "VER TURNOS", Color(0xFFF59E0B), emotion = BeEmotion.SURPRISED)
    )
    val DefaultMessages = listOf(
        BeMessage("🤖", "Hola, soy Be. Estoy aquí para asistirte en todo lo que necesites.", null, Color(0xFF22D3EE), emotion = BeEmotion.NORMAL)
    )
}

/** * --- MODELO DE DATOS ESTABLE PARA SUPER CATEGORÍAS ---
 * Movido fuera para evitar recomposiciones innecesarias. */
data class SuperCategory(
    val title: String,
    val icon: String,
    val items: List<CategoryEntity>,
    val color: Long = 0xFF1A1F26
)

/** * --- BE BRAIN VIEWMODEL ---
 * El "Cerebro" de Be. Gestiona estados, contexto HUD y lógica de interacción. */
@HiltViewModel
class BeBrainViewModel @Inject constructor() : ViewModel() {
    // --- ESTADOS DE DATOS RAW (Para búsqueda contextual) ---
    private val _allCategoriesRaw = MutableStateFlow<List<CategoryEntity>>(emptyList())
    private val _allBudgetsRaw = MutableStateFlow<List<BudgetEntity>>(emptyList())
    // --- ESTADO DE SUPER CATEGORÍAS ---
    private val _superCategories = MutableStateFlow<List<SuperCategory>>(emptyList())
    val superCategories: StateFlow<List<SuperCategory>> = _superCategories.asStateFlow()
    // Estado de filtros global
    private val _activeSortFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeSortFilters: StateFlow<Set<String>> = _activeSortFilters.asStateFlow()
    // --- ESTADOS DE BE Y VISIBILIDAD ---
    private val _showBe = MutableStateFlow(true)
    val showBe: StateFlow<Boolean> = _showBe.asStateFlow()
    private val _isBeDormido = MutableStateFlow(false)
    val isBeDormido: StateFlow<Boolean> = _isBeDormido.asStateFlow()
    private val _showBeTools = MutableStateFlow(false)
    val showBeTools: StateFlow<Boolean> = _showBeTools.asStateFlow()
    private val _currentActions = MutableStateFlow<List<BeSmallActionModel>>(emptyList())
    val currentActions: StateFlow<List<BeSmallActionModel>> = _currentActions.asStateFlow()
    private val _isBottomBarVisible = MutableStateFlow(true)
    val isBottomBarVisible: StateFlow<Boolean> = _isBottomBarVisible.asStateFlow()
    private val _actionEvent = MutableSharedFlow<String>()
    val actionEvent = _actionEvent.asSharedFlow()
    private val _isResultadoVisible = MutableStateFlow(false)
    val isResultadoVisible: StateFlow<Boolean> = _isResultadoVisible.asStateFlow()
    private val _isUIBlocked = MutableStateFlow(false)
    val isUIBlocked: StateFlow<Boolean> = _isUIBlocked.asStateFlow()
    // 🔥 Estado para manejar la Supercategoría seleccionada globalmente
    private val _selectedSuperCategory = MutableStateFlow<SuperCategory?>(null)
    val selectedSuperCategory: StateFlow<SuperCategory?> = _selectedSuperCategory.asStateFlow()
    private val _currentContext = MutableStateFlow(HUDContext.HOME)
    val currentContext: StateFlow<HUDContext> = _currentContext.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
/**
    // Variable necesaria para que el buscador tenga de donde filtrar
    private val _allCategoriesRaw = MutableStateFlow<List<CategoryEntity>>(emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
**/

    private val _beMessages = MutableStateFlow<List<BeMessage>>(BeDictionary.DefaultMessages)
    val beMessages: StateFlow<List<BeMessage>> = _beMessages.asStateFlow()

    private val _requestKeyboard = MutableStateFlow(false)
    val requestKeyboard = _requestKeyboard.asStateFlow()

    private val _dynamicCategories = MutableStateFlow<List<ControlItem>>(emptyList())
    val dynamicCategories: StateFlow<List<ControlItem>> = _dynamicCategories.asStateFlow()

    private val _activeFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeFilters: StateFlow<Set<String>> = _activeFilters.asStateFlow()

    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive: StateFlow<Boolean> = _isMultiSelectionActive.asStateFlow()

    // ======================================================================================
    // 2. BÚSQUEDA CONTEXTUAL CENTRALIZADA
    // ======================================================================================
/** Corazón del filtrado dinámico. Reacciona a la query y al contexto de la pantalla. */
    val searchResults = combine(
        _searchQuery,
        _currentContext,
        _selectedSuperCategory,
        _superCategories,
        _allCategoriesRaw,
        _allBudgetsRaw
    ) { flows ->
        val query = flows[0] as String
        val context = flows[1] as HUDContext
        val selectedSuper = flows[2] as? SuperCategory
        val allSuper = flows[3] as List<SuperCategory>
        val allCategoriesRaw = flows[4] as List<CategoryEntity>
        val allBudgetsRaw = flows[5] as List<BudgetEntity>
        val normalizedQuery = query.prepareForSearch()
        if (normalizedQuery.isEmpty()) return@combine SearchResult.Empty

        when (context) {
            HUDContext.HOME -> {
                if (selectedSuper != null) {
                    val filteredItems = selectedSuper.items.filter { it.matches(normalizedQuery) }
                    SearchResult.CategoryMatch(filteredItems)
                } else {
                    val matchedSupers = allSuper.filter { it.title.prepareForSearch().wordStartsWith(normalizedQuery) }
                    val matchedCategories = allCategoriesRaw.filter { it.matches(normalizedQuery) }
                    SearchResult.GlobalMatch(superCategories = matchedSupers, categories = matchedCategories)
                }
            }
            HUDContext.BUDGETS -> {
                val filteredBudgets = allBudgetsRaw.filter { budget ->
                    budget.providerName.prepareForSearch().wordStartsWith(normalizedQuery) ||
                    (budget.providerCompanyName?.prepareForSearch()?.wordStartsWith(normalizedQuery) ?: false) ||
                    (budget.notes?.prepareForSearch()?.contains(normalizedQuery) ?: false)
                }
                SearchResult.BudgetMatch(filteredBudgets)
            }
            else -> SearchResult.Empty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult.Empty)

    // Clase sellada para manejar los diferentes tipos de resultados que devuelve Be
    sealed class SearchResult {
        object Empty : SearchResult()
        data class CategoryMatch(val categories: List<CategoryEntity>) : SearchResult()
        data class GlobalMatch(val superCategories: List<SuperCategory>, val categories: List<CategoryEntity>) : SearchResult()
        data class BudgetMatch(val budgets: List<BudgetEntity>) : SearchResult()
    }

    // ======================================================================================
    // 1. GESTIÓN DE CATEGORÍAS Y DATOS
    // ======================================================================================
    fun updateSortFilters(newFilters: Set<String>) {
        _activeSortFilters.value = newFilters
    }
    fun updateSuperCategories(allCategories: List<CategoryEntity>) {
        if (allCategories.isEmpty()) return
        _allCategoriesRaw.value = allCategories
        val filters = _activeSortFilters.value
        val grouped = allCategories.groupBy { it.superCategory }
            .map { entry ->
                SuperCategory(
                    title = entry.key ?: "Otros",
                    icon = entry.value.firstOrNull()?.superCategoryIcon ?: "📂",
                    items = entry.value
                )
            }
        val sorted = when {
            filters.contains("sort_nombre_asc") -> grouped.sortedBy { it.title }
            filters.contains("sort_nombre_desc") -> grouped.sortedByDescending { it.title }
            else -> grouped
        }
        _superCategories.value = sorted
    }
    fun updateBudgets(budgets: List<BudgetEntity>) {
        _allBudgetsRaw.value = budgets
    }

    // ======================================================================================
    // 3. LÓGICA DE GESTOS Y VISIBILIDAD DE BE
    // ======================================================================================
    fun selectSuperCategory(superCategory: SuperCategory?) {
        _selectedSuperCategory.value = superCategory
    }
    fun setBottomBarVisible(visible: Boolean) {
        _isBottomBarVisible.value = visible
    }
    fun setResultadoVisible(visible: Boolean) {
        if (visible && _isUIBlocked.value) return
        _isResultadoVisible.value = visible
    }
    fun setUIBlocked(blocked: Boolean) {
        _isUIBlocked.value = blocked
        if (blocked && _isSearchActive.value) {
            _isResultadoVisible.value = false
        }
    }
    fun openKeyboard() { _requestKeyboard.value = true }
    fun closeKeyboard() { _requestKeyboard.value = false }
    fun triggerAction(actionId: String) {
        viewModelScope.launch { _actionEvent.emit(actionId) }
    }
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    fun onBeClick() {
        if (_isBeDormido.value) { _isBeDormido.value = false } 
        else { setSearchActive(!_isSearchActive.value) }
    }
    fun setSearchActive(active: Boolean) {
        if (active) {
            _isSearchActive.value = true
            _showBeTools.value = false
            if (_currentContext.value == HUDContext.HOME && !_isUIBlocked.value) {
                _isResultadoVisible.value = true
                _isBottomBarVisible.value = false
            } else { _isResultadoVisible.value = false }
        } else { cerrarBeAssistantCompleto() }
    }
    fun cerrarBeAssistantCompleto() {
        _isSearchActive.value = false
        _searchQuery.value = ""
        _isResultadoVisible.value = false
        _isBottomBarVisible.value = true
        _showBeTools.value = false
        closeKeyboard()
    }
    fun onBeLongClick() {
        if (!_isBeDormido.value) {
            val nextToolsState = !_showBeTools.value
            if (nextToolsState) {
                cerrarBeAssistantCompleto()
                _showBeTools.value = true
            } else { _showBeTools.value = false }
        }
    }
    fun onBeDoubleClick() {
        _isBeDormido.value = !_isBeDormido.value
        if (_isBeDormido.value) cerrarBeAssistantCompleto()
    }
    // ======================================================================================
    // 4. SENSOR DE CONTEXTO Y NAVEGACIÓN
    // ======================================================================================
    fun onRouteChanged(route: String?) {
        val currentRoute = route ?: return
        _currentContext.value = when {
            currentRoute.contains("home") -> HUDContext.HOME
            currentRoute.contains("presupuestos") -> HUDContext.BUDGETS
            currentRoute.contains("chat") -> HUDContext.CHAT
            currentRoute.contains("calendar") -> HUDContext.CALENDAR
            else -> HUDContext.UNKNOWN
        }
        _showBe.value = !(currentRoute == "login" || currentRoute == "register")
        _isResultadoVisible.value = false
        _showBeTools.value = false
        updateActionsForContext(_currentContext.value)
        updateBeContextMessages(currentRoute)
    }
    private fun updateActionsForContext(context: HUDContext) {
        val actions = mutableListOf<BeSmallActionModel>()
        when (context) {
            HUDContext.HOME -> {
                actions.add(BeSmallActionModel("fast", Icons.Default.FlashOn, "Fast", emoji = "⚡", isDefault = true) { triggerAction("fast") })
                actions.add(BeSmallActionModel("licit", Icons.Default.Gavel, "Licitación", emoji = "⚖️", isDefault = true) { triggerAction("licit") })
                actions.add(BeSmallActionModel("fav", Icons.Default.Favorite, "Favoritos", emoji = "❤️", isDefault = true) { triggerAction("fav") })
                actions.add(BeSmallActionModel("share", Icons.Default.Share, "Compartir", emoji = "📤") { })
            }
            HUDContext.BUDGETS -> {
                actions.add(BeSmallActionModel("comparar", Icons.AutoMirrored.Filled.CompareArrows, "Comparar", emoji = "⚖️", isDefault = true) { triggerAction("comparar") })
                actions.add(BeSmallActionModel("historial", Icons.Default.History, "Historial", emoji = "📜", isDefault = true) { triggerAction("historial") })
            }
            HUDContext.CHAT -> {
                actions.add(BeSmallActionModel("share", Icons.Default.Share, "Compartir", emoji = "📤") { })
            }
            else -> {}
        }
        _currentActions.value = actions
    }
    private fun updateBeContextMessages(route: String) {
        val finalMessages = mutableListOf<BeMessage>()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour >= 21 || currentHour < 6) {
            finalMessages.add(BeMessage("🌙", "Es tarde. Si tienes una urgencia, usa Maverick FAST.", "PROBAR FAST", Color(0xFFF59E0B), emotion = BeEmotion.SURPRISED))
        }
        val baseFromDictionary = when {
            route.contains("home") -> BeDictionary.HomeMessages
            route.contains("presupuestos") -> BeDictionary.BudgetMessages
            route.contains("chat") -> BeDictionary.ChatMessages
            route.contains("calendar") -> BeDictionary.CalendarMessages
            else -> BeDictionary.DefaultMessages
        }
        finalMessages.addAll(baseFromDictionary)
        _beMessages.value = finalMessages
    }
// ======================================================================================
// 5. UTILIDADES DE FILTRO
// ======================================================================================
    fun toggleFilter(filterId: String) {
        val current = _activeFilters.value.toMutableSet()
        if (!current.add(filterId)) current.remove(filterId)
        _activeFilters.value = current
    }

    fun updateDynamicCategories(categories: List<CategoryEntity>) {
        _dynamicCategories.value = categories.map { cat ->
            ControlItem(label = cat.name, icon = null, emoji = cat.icon, color = Color(cat.color), id = "cat_${cat.name.lowercase()}")
        }
    }
}
// ======================================================================================
// UTILIDADES DE BÚSQUEDA (EXTENSIONES) PARA FILTRADO DE TEXTO MAYUS, MINUS, ASENTO, ETC
// ======================================================================================
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
fun CategoryEntity.matches(normalizedQuery: String): Boolean = this.name.wordStartsWith(normalizedQuery)
