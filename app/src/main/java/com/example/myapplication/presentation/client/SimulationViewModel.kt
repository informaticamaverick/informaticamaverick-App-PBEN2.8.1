package com.example.myapplication.presentation.client

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.BudgetItem
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.ProviderRepository
import com.example.myapplication.presentation.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/**
 * VIEWMODEL DE SIMULACIÓN PROFESIONAL (MAVERICK FAST)
 * Centraliza la lógica de creación de presupuestos y mensajes simulados.
 */
@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val chatRepository: ChatRepository,
    private val budgetRepository: BudgetRepository,
    private val providerRepository: ProviderRepository,
    private val application: Application
) : ViewModel() {

    private val notificationHelper = NotificationHelper(application)

    /**
     * Simulación A: CHAT Y PRESUPUESTO DIRECTO
     * Genera respuestas de TODOS los chats activos actuales.
     */
    fun simulateProviderWelcomeAndBudget(specificClientId: String? = null) {
        viewModelScope.launch {
            val currentUserId = specificClientId ?: auth.currentUser?.uid ?: "user_demo_66"
            
            // Obtenemos IDs de chats donde ya hubo interacción
            val activeChatIds = chatRepository.getActiveChatIds(currentUserId).first()
            val allProviders = providerRepository.allProviders.first()

            val targetProviders = if (activeChatIds.isNotEmpty()) {
                allProviders.filter { activeChatIds.contains(it.id) }
            } else {
                // Si no hay chats, iniciamos con 2 al azar para no estar vacíos
                allProviders.shuffled().take(2)
            }

            if (targetProviders.isEmpty()) {
                Toast.makeText(application, "No hay prestadores para simular.", Toast.LENGTH_LONG).show()
                return@launch
            }

            Toast.makeText(application, "Simulando respuestas en ${targetProviders.size} chats...", Toast.LENGTH_SHORT).show()

            targetProviders.forEachIndexed { index, provider ->
                launch {
                    delay(index * 2000L) // Delay Long corregido
                    executeDirectChatSimulation(currentUserId, provider)
                }
            }
        }
    }

    /**
     * Simulación B: RESPUESTAS A LICITACIONES (TENDERS)
     * Busca licitaciones abiertas y genera presupuestos técnicos. NO genera chats.
     */
    fun simulateTenderResponses() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val openTenders = budgetRepository.getOpenTenders()
            val allProviders = providerRepository.allProviders.first()

            if (openTenders.isEmpty()) {
                Toast.makeText(application, "Crea una Licitación ABIERTA para simular respuestas.", Toast.LENGTH_LONG).show()
                return@launch
            }

            var count = 0
            openTenders.forEach { tender ->
                // Buscamos prestadores del mismo rubro
                val candidates = allProviders.filter { it.category.equals(tender.category, true) }
                
                candidates.shuffled().take(Random.nextInt(1, 3)).forEach { provider ->
                    val budget = createProfessionalA4Budget(currentUserId, provider, tender.tenderId)
                    budgetRepository.receiveBudgetFromChat(budget)
                    count++
                }
            }

            if (count > 0) {
                notificationHelper.showNotification("Gestión Comercial", "Recibiste $count propuestas nuevas en tus licitaciones.")
                Toast.makeText(application, "Se generaron $count presupuestos en tus carpetas.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(application, "No se hallaron prestadores para los rubros licitados.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun executeDirectChatSimulation(currentUserId: String, provider: Provider) {
        val chatId = "chat_${currentUserId}_${provider.id}"
        
        val welcomeText = "¡Hola! Soy ${provider.name}. Estuve viendo tu consulta y te envío mi propuesta formal."
        sendSimulatedMessage(chatId, currentUserId, provider, welcomeText, MessageType.TEXT)
        notificationHelper.showNotification(provider.displayName, welcomeText)

        delay(3000L)

        val newBudget = createProfessionalA4Budget(currentUserId, provider, null)
        budgetRepository.receiveBudgetFromChat(newBudget)
        
        sendSimulatedMessage(
            chatId = chatId,
            currentUserId = currentUserId,
            provider = provider,
            text = "Presupuesto Técnico #${newBudget.budgetId.takeLast(4)}",
            type = MessageType.BUDGET,
            relatedId = newBudget.budgetId
        )
        notificationHelper.showNotification("Nuevo Presupuesto", "De: ${provider.displayName}")
    }

    private suspend fun sendSimulatedMessage(chatId: String, currentUserId: String, provider: Provider, text: String, type: MessageType, relatedId: String? = null) {
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = provider.id,
            receiverId = currentUserId,
            type = type,
            content = text,
            timestamp = System.currentTimeMillis(),
            relatedId = relatedId,
            isRead = false
        )
        chatRepository.sendMessage(msg)
    }

    private fun createProfessionalA4Budget(clientId: String, provider: Provider, tenderId: String?): BudgetEntity {
        val items = listOf(
            BudgetItem(description = "Mantenimiento Técnico Especializado", quantity = 1, unitPrice = 15000.0),
            BudgetItem(description = "Repuestos y Componentes Originales", quantity = 1, unitPrice = 25000.0),
            BudgetItem(description = "Instalación y Configuración Final", quantity = 1, unitPrice = 8000.0)
        )
        val total = items.sumOf { it.unitPrice * it.quantity }
        
        return BudgetEntity(
            budgetId = "SIM-${Random.nextInt(1000, 9999)}",
            clientId = clientId,
            providerId = provider.id,
            tenderId = tenderId,
            providerName = provider.displayName,
            providerCompanyName = provider.companies.firstOrNull()?.name ?: "${provider.lastName} Soluciones",
            providerPhotoUrl = provider.photoUrl,
            items = items,
            subtotal = total,
            grandTotal = total,
            status = BudgetStatus.PENDIENTE,
            notes = if (tenderId != null) "Respuesta a Licitación Ref: $tenderId" else "Presupuesto directo vía chat.",
            dateTimestamp = System.currentTimeMillis()
        )
    }
}
