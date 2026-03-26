package com.example.myapplication.presentation.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.presentation.components.BeEmotion
import com.example.myapplication.presentation.components.BeMessage
import com.example.myapplication.presentation.components.BeSmallActionModel
import com.example.myapplication.presentation.components.ControlItem
import com.example.myapplication.presentation.registry.BeMenuRegistry
import com.example.myapplication.presentation.registry.BeDictionary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** * --- ENUM DE CONTEXTO DEL HUD ---
 * Define en qué sección de la app se encuentra el usuario. */
enum class HUDContext {
    HOME, BUDGETS, BUDGETS_TENDERS, BUDGETS_DIRECT, CHAT, CALENDAR, PROMO, TENDER_DETAILS, PROFILE, UNKNOWN
}

/** * --- MODELO DE UBICACIÓN GLOBAL ---
 * Centralizado en el cerebro para que todas las pantallas compartan la misma referencia. */
sealed class LocationOption {
    data class Gps(val address: String, val locality: String) : LocationOption()
    data class Personal(val address: String, val number: String, val locality: String) : LocationOption()
    data class Business(val companyName: String, val branchName: String, val address: String, val number: String, val locality: String) : LocationOption()
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

    // 🔥 CENTRALIZACIÓN DE DATOS DE USUARIO Y UBICACIÓN
    // El cerebro ahora es el guardián de quién es el usuario y dónde está.
    private val _userState = MutableStateFlow<UserEntity?>(null)
    val userState: StateFlow<UserEntity?> = _userState.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LocationOption?>(null)
    val selectedLocation: StateFlow<LocationOption?> = _selectedLocation.asStateFlow()

    // --- ESTADOS DE DATOS RAW (Para búsqueda contextual e inteligencia de filtros) ---
    private val _allCategoriesRaw = MutableStateFlow<List<CategoryEntity>>(emptyList())
    private val _allBudgetsRaw = MutableStateFlow<List<BudgetEntity>>(emptyList())
    private val _allTendersRaw = MutableStateFlow<List<TenderEntity>>(emptyList())

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
    
    // 🔥 Canal de Acciones Personalizadas (FASE 1)
    private val _customActions = MutableStateFlow<List<BeSmallActionModel>>(emptyList())
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

    // --- ESTADO DE MULTISELECCIÓN (Delegado a especialistas para lógica fina) ---
    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive: StateFlow<Boolean> = _isMultiSelectionActive.asStateFlow()
    
    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedItemIds: StateFlow<Set<String>> = _selectedItemIds.asStateFlow()

    // 🔥 GESTIÓN DINÁMICA DE FILTROS SEGÚN CONTEXTO 🔥
    val availableFilters: StateFlow<List<ControlItem>> = _currentContext.map { context ->
        when (context) {
            HUDContext.HOME -> listOf(
                BeMenuRegistry.FILTER_PRODUCTS, BeMenuRegistry.FILTER_SERVICES,
                BeMenuRegistry.FILTER_24H, BeMenuRegistry.FILTER_LOCAL
            )
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> listOf(
                BeMenuRegistry.FILTER_TENDER_ACTIVE, BeMenuRegistry.FILTER_TENDER_CLOSED,
                BeMenuRegistry.FILTER_TENDER_CANCELED, BeMenuRegistry.FILTER_TENDER_AWARDED
            )
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> listOf(
                BeMenuRegistry.FILTER_SUBSCRIBED,
                BeMenuRegistry.FILTER_FAVORITE
            )
            HUDContext.CHAT -> listOf(
                BeMenuRegistry.FILTER_SUBSCRIBED, BeMenuRegistry.FILTER_FAVORITE,
                BeMenuRegistry.FILTER_ONLINE, BeMenuRegistry.FILTER_PRODUCTS,
                BeMenuRegistry.FILTER_SERVICES, BeMenuRegistry.FILTER_24H,
                BeMenuRegistry.FILTER_SHIPPING, BeMenuRegistry.FILTER_VISITS,
                BeMenuRegistry.FILTER_LOCAL, BeMenuRegistry.FILTER_APPOINTMENTS
            )
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val availableSortOptions: StateFlow<List<ControlItem>> = _currentContext.map { context ->
        when (context) {
            HUDContext.HOME -> listOf(BeMenuRegistry.SORT_ALPHA, BeMenuRegistry.VIEW_COMPACT)
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> listOf(
                BeMenuRegistry.SORT_ALPHA, BeMenuRegistry.SORT_DATE,
                BeMenuRegistry.VIEW_COMPACT
            )
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> listOf(
                BeMenuRegistry.SORT_ALPHA, BeMenuRegistry.SORT_DATE,
                BeMenuRegistry.SORT_PRICE
            )
            HUDContext.CHAT -> listOf(
                BeMenuRegistry.SORT_ALPHA, BeMenuRegistry.SORT_DATE,
                BeMenuRegistry.SORT_UNREAD
            )
            else -> listOf(BeMenuRegistry.SORT_ALPHA)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 🔥 NUEVO: Disparador para resetear posición (0 = No reset, aumenta para disparar)
    private val _resetBePositionTrigger = MutableStateFlow(0)
    val resetBePositionTrigger: StateFlow<Int> = _resetBePositionTrigger.asStateFlow()

    private val _beMessages = MutableStateFlow<List<BeMessage>>(emptyList())
    val beMessages: StateFlow<List<BeMessage>> = _beMessages.asStateFlow()

    // 🔥 NUEVO: Identificador de la caja de herramientas actual para animaciones de bloque
    private val _toolboxKey = MutableStateFlow("home_default")
    val toolboxKey: StateFlow<String> = _toolboxKey.asStateFlow()

    private val _requestKeyboard = MutableStateFlow(false)
    val requestKeyboard = _requestKeyboard.asStateFlow()

    // 🔥 FILTRADO INTELIGENTE DE CATEGORÍAS POR CONTEXTO 🔥
    val dynamicCategories: StateFlow<List<ControlItem>> = combine(
        _currentContext,
        _allCategoriesRaw,
        _allTendersRaw,
        _allBudgetsRaw
    ) { context, allCats, tenders, budgets ->
        val filtered = when (context) {
            HUDContext.BUDGETS_TENDERS -> {
                val names = tenders.map { it.category.lowercase() }.toSet()
                allCats.filter { it.name.lowercase() in names }
            }
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> {
                val categoryNamesInBudgets = budgets.mapNotNull { it.category?.lowercase() }.toSet()
                val providerIds = budgets.map { it.providerId }.toSet()
                allCats.filter { cat -> 
                    cat.name.lowercase() in categoryNamesInBudgets || 
                    cat.providerIds.any { it in providerIds } 
                }
            }
            else -> allCats
        }
        filtered.map { cat ->
            ControlItem(label = cat.name, icon = null, emoji = cat.icon, color = Color(cat.color), id = "cat_${cat.name.lowercase()}")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeFilters: StateFlow<Set<String>> = _activeFilters.asStateFlow()


    // ======================================================================================
    // 🔥 LÓGICA DE CENTRALIZACIÓN DE DATOS (Perfil y Ubicación)
    // ======================================================================================

    /** Actualiza el estado del usuario en el cerebro. */
    fun updateProfile(user: UserEntity?) {
        _userState.value = user
    }

    /** Actualiza la ubicación seleccionada globalmente. */
    fun updateLocation(location: LocationOption?) {
        _selectedLocation.value = location
    }

    // ======================================================================================
    // 2. BÚSQUEDA CONTEXTUAL CENTRALIZADA
    // ======================================================================================
    val searchResults = combine(
        _searchQuery,
        _currentContext,
        _selectedSuperCategory,
        _superCategories,
        _allCategoriesRaw,
        _allBudgetsRaw,
        _allTendersRaw
    ) { flows ->
        val query = flows[0] as String
        val context = flows[1] as HUDContext
        val selectedSuper = flows[2] as? SuperCategory
        val allSuper = flows[3] as List<SuperCategory>
        val allCategoriesRaw = flows[4] as List<CategoryEntity>
        val allBudgetsRaw = flows[5] as List<BudgetEntity>
        val allTendersRaw = flows[6] as List<TenderEntity>
        val normalizedQuery = query.prepareForSearch()
        if (normalizedQuery.isEmpty()) return@combine SearchResult.Empty

        when (context) {
            HUDContext.HOME -> {
                if (selectedSuper != null) {
                    val filteredItems = selectedSuper.items.filter { it.matches(normalizedQuery) }
                        .sortedBy { it.name.lowercase() }
                    SearchResult.CategoryMatch(filteredItems)
                } else {
                    val matchedSupers = allSuper
                        .filter { it.title.prepareForSearch().wordStartsWith(normalizedQuery) }
                        .sortedBy { it.title.lowercase() }

                    val matchedCategories = allCategoriesRaw
                        .filter { it.matches(normalizedQuery) }
                        .sortedBy { it.name.lowercase() }
                    SearchResult.GlobalMatch(superCategories = matchedSupers, categories = matchedCategories)
                }
            }
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> {
                val filteredTenders = allTendersRaw.filter { tender ->
                    tender.title.matchesSmart(normalizedQuery)
                }
                SearchResult.TenderMatch(filteredTenders)
            }
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> {
                val filteredBudgets = allBudgetsRaw.filter { budget ->
                    budget.providerName.matchesSmart(normalizedQuery) ||
                            (budget.providerCompanyName?.matchesSmart(normalizedQuery) ?: false) ||
                            budget.grandTotal.toString().startsWith(normalizedQuery) ||
                            budget.budgetId.prepareForSearch().matchesSmart(normalizedQuery)
                              }
                SearchResult.BudgetMatch(filteredBudgets)
            }
            else -> SearchResult.Empty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult.Empty)

    sealed class SearchResult {
        object Empty : SearchResult()
        data class CategoryMatch(val categories: List<CategoryEntity>) : SearchResult()
        data class GlobalMatch(val superCategories: List<SuperCategory>, val categories: List<CategoryEntity>) : SearchResult()
        data class BudgetMatch(val budgets: List<BudgetEntity>) : SearchResult()
        data class TenderMatch(val tenders: List<TenderEntity>) : SearchResult()
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
            .sortedBy { it.title.lowercase() }
        _superCategories.value = sorted
    }
    fun updateBudgets(budgets: List<BudgetEntity>) {
        _allBudgetsRaw.value = budgets
    }
    fun updateAllCategories(categories: List<CategoryEntity>) { _allCategoriesRaw.value = categories }
    fun updateTenders(tenders: List<TenderEntity>) { _allTendersRaw.value = tenders }

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
        _isMultiSelectionActive.value = false
        _selectedItemIds.value = emptySet()
        closeKeyboard()
    }

    fun setShowBeTools(visible: Boolean) {
        _showBeTools.value = visible
        if (visible) {
            updateActionsForContext(_currentContext.value)
        }
        updateToolboxKey()
    }

    private fun updateToolboxKey() {
        val context = _currentContext.value.name.lowercase()
        val mode = if (_showBeTools.value) "tools" else "default"
        _toolboxKey.value = "${context}_${mode}"
    }

    fun onBeLongClick() {
        if (!_isBeDormido.value) {
            val nextToolsState = !_showBeTools.value
            if (nextToolsState) {
                if (!_isMultiSelectionActive.value) {
                    cerrarBeAssistantCompleto()
                } else {
                    _isSearchActive.value = false
                    _searchQuery.value = ""
                    _isResultadoVisible.value = false
                }
                _showBeTools.value = true
                updateActionsForContext(_currentContext.value)
            } else { _showBeTools.value = false }
        }
        updateToolboxKey()
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

        // A. Detectamos el nuevo contexto basado en la ruta de forma limpia
        val newContext = when {
            currentRoute.contains("home") -> HUDContext.HOME
            currentRoute.contains("presupuestos") -> HUDContext.BUDGETS
            currentRoute.contains("chat") -> HUDContext.CHAT
            currentRoute.contains("calendar") -> HUDContext.CALENDAR
            currentRoute.contains("perfil_cliente") -> HUDContext.PROFILE
            else -> HUDContext.UNKNOWN
        }

        // B. Si el contexto cambió, hacemos un reset profundo
        if (_currentContext.value != newContext) {
            _currentContext.value = newContext

            // Al cambiar de pantalla, borramos acciones de especialistas inmediatamente
            _customActions.value = emptyList()

            if (!_isBeDormido.value) {
                // Cerramos cualquier búsqueda o herramienta abierta al navegar
                cerrarBeAssistantCompleto()
                clearFilters()
                _resetBePositionTrigger.value++
            }
        }

        // C. Sincronizamos visibilidad y mensajes
        _showBe.value = !(currentRoute == "login" || currentRoute == "register")
        _isResultadoVisible.value = false
        _showBeTools.value = false // Empezar siempre cerrado al cambiar de pantalla

        // D. IMPORTANTE: Actualizamos las acciones para el nuevo contexto (especialmente para HOME)
        updateActionsForContext(newContext)
        updateBeContextMessages(currentRoute)
        updateToolboxKey()
    }

    // 2. Agrega protección a setHUDContext para evitar sobrescrituras accidentales:
    fun setHUDContext(context: HUDContext) {
        // PROTECCIÓN: Si ya estamos en HOME o en otra pantalla principal definida por el NavHost,
        // no permitimos que un componente interno (como el Pager) cambie el contexto global
        // a uno antiguo durante la transición.
        if (_currentContext.value == HUDContext.HOME ||
            _currentContext.value == HUDContext.CHAT ||
            _currentContext.value == HUDContext.CALENDAR) {
            if (context != _currentContext.value) return
        }

        if (_currentContext.value != context) {
            cerrarBeAssistantCompleto()
            clearFilters()
        }
        _currentContext.value = context
        updateActionsForContext(context)
        updateToolboxKey()
    }

    // 3. Mejora syncMultiSelection para ignorar señales de pantallas muertas:
    fun syncMultiSelection(active: Boolean, selectedIds: Set<String>) {
        // Si estamos en HOME, ignoramos señales de "desactivación" que vengan de Presupuestos
        if (_currentContext.value == HUDContext.HOME && !active) {
            _isMultiSelectionActive.value = false
            _selectedItemIds.value = emptySet()
            return
        }

        val wasActive = _isMultiSelectionActive.value
        _isMultiSelectionActive.value = active
        _selectedItemIds.value = selectedIds

        if (active && !wasActive) {
            _showBeTools.value = true
        } else if (!active && wasActive) {
            _showBeTools.value = false
        }
        updateActionsForContext(_currentContext.value)
        updateToolboxKey()
    }

    // 4. Mejora setCustomActions para no pisar las acciones de la Home:
    fun setCustomActions(actions: List<BeSmallActionModel>) {
        if (_currentContext.value == HUDContext.HOME) return
        _customActions.value = actions
        updateActionsForContext(_currentContext.value)
    }

    private fun updateActionsForContext(context: HUDContext) {
        val actions = mutableListOf<BeSmallActionModel>()

        // 🔥 FASE 1: Solo maneja HOME directamente. Los demás vienen delegados vía setCustomActions.
        if (context == HUDContext.HOME) {
            actions.add(
                BeSmallActionModel(
                    "sim_chat",
                    Icons.AutoMirrored.Filled.Chat,
                    "Sim Chat",
                    emoji = "💬"
                ) { triggerAction("sim_chat") })
            actions.add(
                BeSmallActionModel(
                    "sim_tender",
                    Icons.Default.Gavel,
                    "Sim Licit",
                    emoji = "⚖️"
                ) { triggerAction("sim_tender") })
            actions.add(
                BeSmallActionModel(
                    "fast",
                    Icons.Default.FlashOn,
                    "Fast",
                    emoji = "⚡",
                    isDefault = true
                ) { triggerAction("fast") })
            actions.add(
                BeSmallActionModel(
                    "licit",
                    Icons.Default.Gavel,
                    "Licitación",
                    emoji = "⚖️",
                    isDefault = true
                ) { triggerAction("licit") })
            actions.add(
                BeSmallActionModel(
                    "fav",
                    Icons.Default.Favorite,
                    "Favoritos",
                    emoji = "❤️",
                    isDefault = true
                ) { triggerAction("fav") })
            actions.add(
                BeSmallActionModel(
                    "share",
                    Icons.Default.Share,
                    "Compartir",
                    emoji = "📤"
                ) { })
        }


        else if (context == HUDContext.PROFILE) {
            // --- LÓGICA DINÁMICA DE PERFIL ---
            if (_customActions.value.isNotEmpty()) {
                // Si el ProfileViewModel envió acciones (como GUARDAR/CANCELAR o EDITAR/AJUSTES)
                // las agregamos tal cual vienen delegadas.
                // 🔥 MODIFICACIÓN: Envolvemos las acciones para que disparen triggerAction y respondan al Screen
                actions.addAll(_customActions.value.map { action ->
                    if (action.id.contains("divider_v")) action
                    else action.copy(onClick = { triggerAction(action.id) })
                })
            } else {
                // ACCIONES POR DEFECTO: Solo si por alguna razón la lista está vacía al entrar
                // Marcamos como isDefault = true para que aparezcan por defecto sin long press
                actions.add(BeSmallActionModel("edit_profile", Icons.Default.Edit, "Editar", emoji = "✏️", isDefault = true) {
                    triggerAction("edit_profile")
                })
                actions.add(BeSmallActionModel("settings_profile", Icons.Default.Settings, "Ajustes", emoji = "⚙️", isDefault = true) {
                    triggerAction("settings_profile")
                })
            }
        }

        else {
            actions.addAll(_customActions.value)
        }
        _currentActions.value = actions
    }

    fun toggleMultiSelection() {
        val newState = !_isMultiSelectionActive.value
        _isMultiSelectionActive.value = newState
        if (newState) {
            _showBeTools.value = true
        } else {
            _selectedItemIds.value = emptySet()
            _showBeTools.value = true
        }
        updateActionsForContext(_currentContext.value)
        updateToolboxKey()
    }

    fun toggleItemSelection(id: String) {
        val current = _selectedItemIds.value.toMutableSet()
        if (!current.add(id)) current.remove(id)
        _selectedItemIds.value = current
        updateActionsForContext(_currentContext.value)
    }

    fun selectAllItems(ids: List<String>) {
        _selectedItemIds.value = ids.toSet()
        updateActionsForContext(_currentContext.value)
    }

    private fun updateBeContextMessages(route: String) {
        val finalMessages = mutableListOf<BeMessage>()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour >= 21 || currentHour < 6) {
            finalMessages.add(BeMessage("🌙", "Es tarde. Si tienes una urgencia, usa Maverick FAST.", "PROBAR FAST", Color(0xFFF59E0B), emotion = BeEmotion.SURPRISED))
        }
        // Usar diccionario externo (FASE 1)
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

    fun toggleFilter(filterId: String) {
        val current = _activeFilters.value.toMutableSet()
        if (!current.add(filterId)) current.remove(filterId)
        _activeFilters.value = current
    }

    fun clearFilters() {
        _activeFilters.value = emptySet()
    }

    fun clearSpecificFilters(prefixes: List<String>) {
        _activeFilters.value = _activeFilters.value.filter { filterId ->
            prefixes.none { filterId.startsWith(it) }
        }.toSet()
    }

    fun updateDynamicCategories(categories: List<CategoryEntity>) {
        _allCategoriesRaw.value = categories
    }
}
