package com.example.myapplication.presentation.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.presentation.components.BeEmotion
import com.example.myapplication.presentation.components.BeMessage
import com.example.myapplication.presentation.components.BeSmallActionModel
import com.example.myapplication.presentation.components.ControlItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * --- ENUM DE CONTEXTO DEL HUD ---
 * Define en qué sección de la app se encuentra el usuario.
 */
enum class HUDContext {
    HOME, BUDGETS, CHAT, CALENDAR, PROMO, UNKNOWN
}

/**
 * --- DICCIONARIO CENTRALIZADO DE BE ---
 */
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

/**
 * --- BE BRAIN VIEWMODEL ---
 * El "Cerebro" de Be. Gestiona estados, contexto HUD y lógica de interacción.
 */
@HiltViewModel
class BeBrainViewModel @Inject constructor() : ViewModel() {
    // ======================================================================================
    // 1. ESTADOS DE BE (VISIBILIDAD Y MODOS)
    // ======================================================================================
    private val _showBe = MutableStateFlow(true)
    val showBe: StateFlow<Boolean> = _showBe.asStateFlow()
    
    var isBeDormido by mutableStateOf(false)
        private set
    
    var showBeTools by mutableStateOf(false)
        private set
        
    private val _currentActions = MutableStateFlow<List<BeSmallActionModel>>(emptyList())
    val currentActions: StateFlow<List<BeSmallActionModel>> = _currentActions.asStateFlow()
    
    private val _isBottomBarVisible = MutableStateFlow(true)
    val isBottomBarVisible: StateFlow<Boolean> = _isBottomBarVisible.asStateFlow()

    private val _actionEvent = MutableSharedFlow<String>()
    val actionEvent = _actionEvent.asSharedFlow()

    // 🔥 Estado para controlar la visibilidad de BeResultadoScreen
    private val _isResultadoVisible = MutableStateFlow(false)
    val isResultadoVisible: StateFlow<Boolean> = _isResultadoVisible.asStateFlow()

    // 🔥 Estado para rastrear si hay otras capas de UI abiertas (Sheets, Diálogos, etc.)
    private val _isUIBlocked = MutableStateFlow(false)
    val isUIBlocked: StateFlow<Boolean> = _isUIBlocked.asStateFlow()

    fun setBottomBarVisible(visible: Boolean) {
        _isBottomBarVisible.value = visible
    }

    fun setResultadoVisible(visible: Boolean) {
        // 🔥 MODIFICACIÓN: Si la UI está bloqueada (ej: SuperCategory Panel), no permitimos mostrar resultados globales
        if (visible && _isUIBlocked.value) return
        _isResultadoVisible.value = visible
    }

    fun hideResultado() {
        _isResultadoVisible.value = false
        // 🔥 MODIFICACIÓN: Al ocultar resultados manualmente, cerramos también el modo búsqueda
        setSearchActive(false)
    }

    // 🔥 Permite que la UI notifique si hay otros paneles abiertos
    fun setUIBlocked(blocked: Boolean) {
        _isUIBlocked.value = blocked
        // Si se bloquea la UI mientras la búsqueda está activa, cerramos los resultados globales
        if (blocked && _isSearchActive.value) {
            _isResultadoVisible.value = false
        }
    }

    fun triggerAction(actionId: String) {
        viewModelScope.launch {
            _actionEvent.emit(actionId)
        }
    }

    // ======================================================================================
    // 2. BUSQUEDA, FILTROS Y CATEGORÍAS
    // ======================================================================================
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _activeFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeFilters: StateFlow<Set<String>> = _activeFilters.asStateFlow()
    
    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive: StateFlow<Boolean> = _isMultiSelectionActive.asStateFlow()
    
    private val _dynamicCategories = MutableStateFlow<List<ControlItem>>(emptyList())
    val dynamicCategories: StateFlow<List<ControlItem>> = _dynamicCategories.asStateFlow()

    // ======================================================================================
    // 3. LOGICA DE GESTOS DE BE
    // ======================================================================================
    /**
     * 🔥 GESTO: CLICK SIMPLE
     * - Si está dormido: Se despierta.
     * - Si está despierto: Alterna modo búsqueda.
     */
    fun onBeClick() {
        if (isBeDormido) {
            isBeDormido = false
        } else {
            val nextSearchState = !_isSearchActive.value
            setSearchActive(nextSearchState)
        }
    }

    /**
     * 🔥 MODIFICACIÓN: Permite establecer el estado de búsqueda explícitamente.
     * Centralizamos aquí la lógica de visibilidad de resultados y barra de navegación.
     */
    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (active) {
            showBeTools = false
            // Abrir resultados si se activa búsqueda en HOME y NO hay bloqueos (ej: Panel de Supercategorías)
            if (_currentContext.value == HUDContext.HOME && !_isUIBlocked.value) {
                _isResultadoVisible.value = true
                _isBottomBarVisible.value = false // 🔥 Ocultar NavigationBar al buscar
            } else {
                _isResultadoVisible.value = false
            }
        } else {
            _isResultadoVisible.value = false
            _isBottomBarVisible.value = true // 🔥 Restaurar NavigationBar al cerrar búsqueda
        }
    }

    fun onBeLongClick() {
        if (!isBeDormido) {
            showBeTools = !showBeTools
            if (showBeTools) {
                _isSearchActive.value = false
                _isResultadoVisible.value = false
                _isBottomBarVisible.value = true
            }
        }
    }

    fun onBeDoubleClick() {
        isBeDormido = !isBeDormido
        if (isBeDormido) {
            showBeTools = false
            _isSearchActive.value = false
            _isResultadoVisible.value = false
            _isBottomBarVisible.value = true
        }
    }

    // ======================================================================================
    // 4. CONTEXTO Y NAVEGACIÓN
    // ======================================================================================
    private val _currentContext = MutableStateFlow(HUDContext.HOME)
    val currentContext: StateFlow<HUDContext> = _currentContext.asStateFlow()

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
        showBeTools = false
        _isResultadoVisible.value = false // Reseteamos al navegar
        updateActionsForContext(_currentContext.value)
        updateBeContextMessages(currentRoute)
    }

    private fun updateActionsForContext(context: HUDContext) {
        val actions = mutableListOf<BeSmallActionModel>()
        when (context) {
            HUDContext.HOME -> {
                actions.add(BeSmallActionModel("fast", Icons.Default.FlashOn, "Fast", isDefault = true) {triggerAction("fast") })
                actions.add(BeSmallActionModel("licit", Icons.Default.Gavel, "Licitación", isDefault = true) {triggerAction("licit") })
                actions.add(BeSmallActionModel("fav", Icons.Default.Favorite, "Favoritos", isDefault = true) {triggerAction("fav") })
                actions.add(BeSmallActionModel("share", Icons.Default.Share, "Compartir") { })
            }
            HUDContext.CHAT -> {
                if (_isMultiSelectionActive.value) {
                    actions.add(BeSmallActionModel("delete", Icons.Default.Delete, "Borrar", tint = Color.Red) { })
                }
                actions.add(BeSmallActionModel("share", Icons.Default.Share, "Compartir") { })
            }
            else -> {}
        }
        _currentActions.value = actions
    }

    // ======================================================================================
    // 5. GESTIÓN DEL DICCIONARIO DE MENSAJES
    // ======================================================================================
    private val _beMessages = MutableStateFlow<List<BeMessage>>(BeDictionary.DefaultMessages)
    val beMessages: StateFlow<List<BeMessage>> = _beMessages.asStateFlow()

    private fun updateBeContextMessages(route: String) {
        _beMessages.value = emptyList()
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
    // 6. UTILIDADES
    // ======================================================================================
    fun updateSearchQuery(query: String) = _searchQuery.run { value = query }
    
    fun toggleFilter(filterId: String) {
        val current = _activeFilters.value.toMutableSet()
        if (!current.add(filterId)) current.remove(filterId)
        _activeFilters.value = current
    }

    fun updateDynamicCategories(categories: List<CategoryEntity>) {
        val controlItems = categories.map { cat ->
            ControlItem(
                label = cat.name,
                icon = null,
                emoji = cat.icon,
                color = Color(cat.color),
                id = "cat_${cat.name.lowercase()}"
            )
        }
        _dynamicCategories.value = controlItems
    }
}
