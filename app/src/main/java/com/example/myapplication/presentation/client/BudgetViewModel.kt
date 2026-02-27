package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.*
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * --- VIEWMODEL DE PRESUPUESTOS ---
 * [ACTUALIZADO] Ahora integra ChatRepository para notificar decisiones al prestador.
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
}
