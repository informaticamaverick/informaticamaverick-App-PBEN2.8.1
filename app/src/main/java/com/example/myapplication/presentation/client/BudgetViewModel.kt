package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.*
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ==========================================================
// --- MODELOS DE ESTADO PARA LA ANALÍTICA ---
// ==========================================================
// NOTA: ChartBudgetItem ya está declarado en BudgetAnalyticsScreen.kt,
// por lo que no lo volvemos a declarar aquí para evitar el error "Redeclaration".

data class AnalyticsState(
    val items: List<ChartBudgetItem> = emptyList(),
    val avgTotal: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val validCount: Int = 0,
    val isAnalyzing: Boolean = true
)

/**
 * --- VIEWMODEL DE PRESUPUESTOS ---
 * Integra ChatRepository para notificar decisiones al prestador
 * y un motor matemático para Analíticas en 2do plano.
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val chatRepository: ChatRepository // 🔥 Inyectado para notificar decisiones
) : ViewModel() {

    // ==========================================================
    // 1. ESTADOS (DATOS OBSERVABLES)
    // ==========================================================

    val tenders: StateFlow<List<TenderEntity>> = repository.allTenders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val directBudgets: StateFlow<List<BudgetEntity>> = repository.directBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 🔥 [NUEVO] Todos los presupuestos para lógica de filtrado global y notificaciones.
     */
    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado exclusivo para la pantalla de Análisis de Mercado
    private val _analyticsState = MutableStateFlow(AnalyticsState())
    val analyticsState: StateFlow<AnalyticsState> = _analyticsState.asStateFlow()

    // ==========================================================
    // 2. ACCIONES
    // ==========================================================

    fun createTender(title: String, description: String, category: String, endDate: Long) {
        viewModelScope.launch {
            val newTender = TenderEntity(
                tenderId = UUID.randomUUID().toString(),
                title = title,
                description = description,
                category = category,
                endDate = endDate
            )
            repository.createNewTender(newTender)
        }
    }

    /**
     * Acepta el presupuesto y envía un mensaje automático al chat del prestador.
     */
    fun acceptBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            // 1. Actualizar estado en Room
            repository.updateBudgetStatus(budget.budgetId, BudgetStatus.ACEPTADO)

            // 2. Notificar al prestador por chat
            sendDecisionMessage(
                budget = budget,
                text = "✅ ¡Hola! He ACEPTADO el presupuesto #${budget.budgetId.takeLast(4)}. Por favor, contactame para agendar la visita técnica."
            )
        }
    }

    /**
     * Rechaza el presupuesto y notifica al prestador.
     */
    fun rejectBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            // 1. Actualizar estado en Room
            repository.updateBudgetStatus(budget.budgetId, BudgetStatus.RECHAZADO)

            // 2. Notificar al prestador por chat
            sendDecisionMessage(
                budget = budget,
                text = "❌ Hola. He decidido RECHAZAR el presupuesto #${budget.budgetId.takeLast(4)} por el momento. Gracias por tu propuesta."
            )
        }
    }

    /**
     * 🔥 [NUEVO] Marca un presupuesto como leído en la base de datos persistente.
     */
    fun markAsRead(budgetId: String) {
        viewModelScope.launch {
            repository.markBudgetAsRead(budgetId)
        }
    }

    /**
     * 🔥 Elimina una lista de licitaciones.
     */
    fun deleteTenders(ids: Set<String>) {
        viewModelScope.launch {
            ids.forEach { repository.removeTender(it) }

        }
    }

    /**
     * 🔥 Elimina una lista de presupuestos.
     */
    fun deleteBudgets(ids: Set<String>) {
        viewModelScope.launch {
            ids.forEach { repository.removeBudget(it) }
        }
    }

    /**
     * 🔥 Cancela una licitación activa.
     */
    fun cancelTender(tender: TenderEntity) {
        viewModelScope.launch {
            val updated = tender.copy(
                status = "CANCELADA",
                cancellationDate = System.currentTimeMillis()
            )
            repository.createNewTender(updated) // El DAO usa OnConflict.REPLACE
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

    fun getBudgetsForTender(tenderId: String): StateFlow<List<BudgetEntity>> {
        return repository.getBudgetsForTender(tenderId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // ==========================================================
    // 3. MOTOR MATEMÁTICO (CÁLCULOS PESADOS EN BACKGROUND)
    // ==========================================================

    /**
     * Ejecuta sumatorias y detecciones de anomalías en Dispatchers.Default
     * para NO bloquear la UI de Jetpack Compose.
     */
    fun analyzeBudgets(budgets: List<BudgetEntity>) {
        viewModelScope.launch(Dispatchers.Default) {
            _analyticsState.value = _analyticsState.value.copy(isAnalyzing = true)

            if (budgets.isEmpty()) {
                _analyticsState.value = AnalyticsState(isAnalyzing = false)
                return@launch
            }

            // 1. Mapeo y sumatorias
            val mapped = budgets.map { budget ->
                // Las funciones itemsTotal() y servicesTotal() ya fueron agregadas
                // como extensiones en BudgetAnalyticsScreen.kt, por lo que podemos usarlas aquí.
                // Sin embargo, para mayor limpieza, hacemos el cálculo directo.
                val mat = budget.items.sumOf { it.unitPrice * it.quantity }
                val lab = budget.services.sumOf { it.total }
                val tax = budget.taxAmount
                val total = mat + lab + tax

                // Filtro de Anomalías (Range Check idiomático)
                val isIrr = total !in 15000.0..200000.0
                ChartBudgetItem(budget, total, mat, lab, tax, isIrr, false)
            }

            // 2. Cálculo de Promedios sobre datos VÁLIDOS (que no son irrisorios)
            val validItems = mapped.filter { !it.isIrrisory }
            val avg = if (validItems.isNotEmpty()) validItems.map { it.total }.average() else 0.0

            // 3. Zona de Valor Óptimo (+/- 15%)
            val optMin = avg * 0.85
            val optMax = avg * 1.15

            val finalItems = mapped.map {
                it.copy(isOptimal = !it.isIrrisory && it.total in optMin..optMax)
            }.sortedBy { it.total } // Se entrega pre-ordenado para formar la Curva Visual

            val minPrice = validItems.minOfOrNull { it.total } ?: 0.0
            val maxPrice = validItems.maxOfOrNull { it.total } ?: 0.0

            // 4. Emitir el nuevo estado procesado a la UI
            _analyticsState.value = AnalyticsState(
                items = finalItems,
                avgTotal = avg,
                minPrice = minPrice,
                maxPrice = maxPrice,
                validCount = validItems.size,
                isAnalyzing = false
            )
        }
    }
}
