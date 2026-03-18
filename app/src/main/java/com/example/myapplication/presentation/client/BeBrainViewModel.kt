package com.example.myapplication.presentation.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.TenderEntity
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** * --- ENUM DE CONTEXTO DEL HUD ---
 * Define en qué sección de la app se encuentra el usuario. */
enum class HUDContext {
    HOME, BUDGETS, BUDGETS_TENDERS, BUDGETS_DIRECT, CHAT, CALENDAR, PROMO, UNKNOWN
}

/** 📝 PASOS PARA AGREGAR NUEVOS FILTROS OR ORDENAMIENTOS:
 * 1. Define el botón en 'BeTacticalRegistry' con un ID único.
 * 2. Agrégalo a la lista correspondiente en 'availableFilters' o 'availableSortOptions' según el HUDContext en el ViewModel.
 * 3. Implementa la lógica de filtrado/ordenado en los flujos de datos del ViewModel (como searchResults).
 */
object BeTacticalRegistry {
    // --- ORDENAMIENTOS (Para MenuOrdenamiento.kt) ---
    val SORT_ALPHA = ControlItem("Nombre", Icons.Default.SortByAlpha, "ABC", Color(0xFF2197F5), "sort_alpha")
    val SORT_DATE = ControlItem("Fecha", Icons.Default.CalendarToday, "📅", Color(0xFF9B51E0), "sort_date")
    val SORT_UNREAD = ControlItem("No Leídos", Icons.Default.MarkChatUnread, "📩", Color(0xFFEF4444), "sort_unread")
    val SORT_PLAZO = ControlItem("Plazo", Icons.Default.HourglassBottom, "⏳", Color(0xFF22D3EE), "sort_plazo")
    val SORT_PRICE = ControlItem("Precio", Icons.Default.AttachMoney, "💰", Color(0xFF10B981), "sort_price")
    val VIEW_COMPACT = ControlItem("Compacta", Icons.Default.ViewStream, "📱", Color(0xFFF59E0B), "view_compact")

    // --- FILTROS (Para MenuFiltros.kt) ---
    val FILTER_SUBSCRIBED = ControlItem("Suscrito", Icons.Default.Verified, "✅", Color(0xFF9B51E0), "filter_sub")
    val FILTER_FAVORITE = ControlItem("Favorito", Icons.Default.Favorite, "❤️", Color(0xFFE91E63), "filter_fav")
    val FILTER_ONLINE = ControlItem("Online", Icons.Default.Circle, "🌐", Color(0xFF10B981), "filter_online")
    val FILTER_PRODUCTS = ControlItem("Productos", Icons.Default.ShoppingBag, "🛍️", Color(0xFF22D3EE), "filter_products")
    val FILTER_SERVICES = ControlItem("Servicios", Icons.Default.Build, "🔧", Color(0xFFF59E0B), "filter_services")
    val FILTER_24H = ControlItem("24hs", Icons.Default.AccessTimeFilled, "⏳", Color(0xFFFF9800), "filter_24h")
    val FILTER_SHIPPING = ControlItem("Envios", Icons.Default.LocalShipping, "🚚", Color(0xFF9B51E0), "filter_shipping")
    val FILTER_VISITS = ControlItem("Visitas", Icons.Default.HomeWork, "🏠", Color(0xFF4CAF50), "filter_visits")
    val FILTER_LOCAL = ControlItem("Local", Icons.Default.Storefront, "🏪", Color(0xFF2197F5), "filter_local")
    val FILTER_APPOINTMENTS = ControlItem("Turnos", Icons.Default.EventAvailable, "📅", Color(0xFF00FFC2), "filter_appointments")
    
    // Filtros de Licitación (PresupuestosScreen)
    val FILTER_TENDER_ACTIVE = ControlItem("Abierta", Icons.Default.LockOpen, "📂", Color(0xFF10B981), "filter_tender_active")
    val FILTER_TENDER_CLOSED = ControlItem("Cerrada", Icons.Default.Lock, "📁", Color(0xFF64748B), "filter_tender_closed")
    val FILTER_TENDER_CANCELED = ControlItem("Cancelada", Icons.Default.Block, "🚫", Color(0xFFEF4444), "filter_tender_canceled")
    val FILTER_TENDER_AWARDED = ControlItem("Adjudicada", Icons.Default.EmojiEvents, "🏆", Color(0xFFFACC15), "filter_tender_awarded")
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

    // 🔥 GESTIÓN DINÁMICA DE FILTROS SEGÚN CONTEXTO 🔥
    val availableFilters: StateFlow<List<ControlItem>> = _currentContext.map { context ->
        when (context) {
            HUDContext.HOME -> listOf(
                BeTacticalRegistry.FILTER_PRODUCTS, BeTacticalRegistry.FILTER_SERVICES,
                BeTacticalRegistry.FILTER_24H, BeTacticalRegistry.FILTER_LOCAL
            )
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> listOf(
                BeTacticalRegistry.FILTER_TENDER_ACTIVE, BeTacticalRegistry.FILTER_TENDER_CLOSED,
                BeTacticalRegistry.FILTER_TENDER_CANCELED, BeTacticalRegistry.FILTER_TENDER_AWARDED
            )
            HUDContext.BUDGETS_DIRECT -> listOf(
                BeTacticalRegistry.FILTER_SUBSCRIBED,
                BeTacticalRegistry.FILTER_FAVORITE
            )
            HUDContext.CHAT -> listOf(
                BeTacticalRegistry.FILTER_SUBSCRIBED, BeTacticalRegistry.FILTER_FAVORITE,
                BeTacticalRegistry.FILTER_ONLINE, BeTacticalRegistry.FILTER_PRODUCTS,
                BeTacticalRegistry.FILTER_SERVICES, BeTacticalRegistry.FILTER_24H,
                BeTacticalRegistry.FILTER_SHIPPING, BeTacticalRegistry.FILTER_VISITS,
                BeTacticalRegistry.FILTER_LOCAL, BeTacticalRegistry.FILTER_APPOINTMENTS
            )
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val availableSortOptions: StateFlow<List<ControlItem>> = _currentContext.map { context ->
        when (context) {
            HUDContext.HOME -> listOf(BeTacticalRegistry.SORT_ALPHA, BeTacticalRegistry.VIEW_COMPACT)
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> listOf(
                BeTacticalRegistry.SORT_ALPHA, BeTacticalRegistry.SORT_DATE,
                BeTacticalRegistry.VIEW_COMPACT
            )
            HUDContext.BUDGETS_DIRECT -> listOf(
                BeTacticalRegistry.SORT_ALPHA, BeTacticalRegistry.SORT_DATE,
                BeTacticalRegistry.SORT_PRICE
            )
            HUDContext.CHAT -> listOf(
                BeTacticalRegistry.SORT_ALPHA, BeTacticalRegistry.SORT_DATE, BeTacticalRegistry.SORT_UNREAD
            )
            else -> listOf(BeTacticalRegistry.SORT_ALPHA)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 🔥 NUEVO: Disparador para resetear posición (0 = No reset, aumenta para disparar)
    private val _resetBePositionTrigger = MutableStateFlow(0)
    val resetBePositionTrigger: StateFlow<Int> = _resetBePositionTrigger.asStateFlow()

    private val _beMessages = MutableStateFlow<List<BeMessage>>(BeDictionary.DefaultMessages)
    val beMessages: StateFlow<List<BeMessage>> = _beMessages.asStateFlow()

    private val _requestKeyboard = MutableStateFlow(false)
    val requestKeyboard = _requestKeyboard.asStateFlow()

    // 🔥 FILTRADO INTELIGENTE DE CATEGORÍAS POR CONTEXTO 🔥
    // Centraliza qué categorías se muestran en el menú Tornado basándose en los datos visibles
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
            HUDContext.BUDGETS_DIRECT -> {
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
                    SearchResult.CategoryMatch(filteredItems)
                } else {
                    val matchedSupers = allSuper.filter { it.title.prepareForSearch().wordStartsWith(normalizedQuery) }
                    val matchedCategories = allCategoriesRaw.filter { it.matches(normalizedQuery) }
                    SearchResult.GlobalMatch(superCategories = matchedSupers, categories = matchedCategories)
                }
            }
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> {
                // Filtro por nombre de licitación (Coincidencia exacta por palabra/prefijo)
                val filteredTenders = allTendersRaw.filter { tender ->
                    tender.title.matchesSmart(normalizedQuery)
                   //tender.title.wordStartsWith(normalizedQuery)
                }
                SearchResult.TenderMatch(filteredTenders)
            }
            HUDContext.BUDGETS_DIRECT -> {
                // Filtro por nombre prestador, empresa, monto y número (ID) con coincidencia por palabra
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

    // Clase sellada para manejar los diferentes tipos de resultados que devuelve Be
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
        _superCategories.value = sorted
    }
    fun updateBudgets(budgets: List<BudgetEntity>) {
        _allBudgetsRaw.value = budgets
    }
    // --- MÉTODOS PARA ALIMENTAR A BE ---
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
        
        // 🔥 AL CAMBIAR DE PANTALLA: Cerramos todo y damos la orden de volver a casa
        if (!_isBeDormido.value) {
            cerrarBeAssistantCompleto()
            _resetBePositionTrigger.value++ // Incrementamos para avisar al Composable
        }

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

    fun setHUDContext(context: HUDContext) {
        // 🔥 AL CAMBIAR EL ESTADO O CONTEXTO: Cerramos el asistente si estaba abierto
        if (_currentContext.value != context) {
            cerrarBeAssistantCompleto()
        }
        _currentContext.value = context
        updateActionsForContext(context)
    }

    private fun updateActionsForContext(context: HUDContext) {
        val actions = mutableListOf<BeSmallActionModel>()
        
        // 🔥 BOTONES DE SIMULACIÓN (Disponibles cuando showBeTools es true)
        // Se disparan a través de triggerAction para ser capturados por la Screen
        actions.add(BeSmallActionModel("sim_chat", Icons.Default.Chat, "Simular Chat", emoji = "💬") { triggerAction("sim_chat") })
        actions.add(BeSmallActionModel("sim_tender", Icons.Default.Gavel, "Simular Licitación", emoji = "⚖️") { triggerAction("sim_tender") })
        actions.add(BeSmallActionModel("sim_promo", Icons.Default.Campaign, "Simular Promos", emoji = "📢") { triggerAction("sim_promo") })

        when (context) {
            HUDContext.HOME -> {
                actions.add(BeSmallActionModel("fast", Icons.Default.FlashOn, "Fast", emoji = "⚡", isDefault = true) { triggerAction("fast") })
                actions.add(BeSmallActionModel("licit", Icons.Default.Gavel, "Licitación", emoji = "⚖️", isDefault = true) { triggerAction("licit") })
                actions.add(BeSmallActionModel("fav", Icons.Default.Favorite, "Favoritos", emoji = "❤️", isDefault = true) { triggerAction("fav") })
                actions.add(BeSmallActionModel("share", Icons.Default.Share, "Compartir", emoji = "📤") { })
            }
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS, HUDContext.BUDGETS_DIRECT -> {
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

    fun clearFilters() {
        _activeFilters.value = emptySet()
    }

    fun clearSpecificFilters(prefixes: List<String>) {
        _activeFilters.value = _activeFilters.value.filter { filterId ->
            prefixes.none { filterId.startsWith(it) }
        }.toSet()
    }

    // 🔥 Este método ahora solo actualiza la lista base para los flujos inteligentes 🔥
    fun updateDynamicCategories(categories: List<CategoryEntity>) {
        _allCategoriesRaw.value = categories
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


// ... Nueva extensión de búsqueda inteligente al final del archivo ...
fun String.matchesSmart(query: String): Boolean {
    if (query.isEmpty()) return false
    val normalizedText = this.prepareForSearch()
    val queryWords = query.prepareForSearch().split(" ").filter { it.isNotEmpty() }
    val textWords = normalizedText.split(" ").filter { it.isNotEmpty() }

    // Cada palabra de la consulta debe ser el inicio de alguna palabra del texto
    return queryWords.all { qw ->
        textWords.any { tw -> tw.startsWith(qw) }
    }
}