package com.example.myapplication.presentation.components

import android.app.Application
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.BudgetItem
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.data.local.MessageEntity
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
 * --- MOTOR DE SIMULACIÓN PROFESIONAL (MAVERICK FAST) ---
 * Propósito: Simular la interacción técnica real entre un prestador y el cliente.
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
     * Función principal que ejecuta el flujo de simulación.
     * [MODIFICADO] Se cambió el nombre para consistencia con HomeScreenCliente3.kt
     */
    fun simulateProviderWelcomeAndBudget(specificClientId: String? = null) {
        viewModelScope.launch {
            // 1. Determinar el ID del cliente (Yo)
            val currentUserId = specificClientId ?: auth.currentUser?.uid ?: "user_demo_66"
            
            // 2. Seleccionar un prestador inteligente (Maverick o uno con chat previo)
            val provider = selectProviderForSimulation(currentUserId)
            if (provider == null) {
                Toast.makeText(application, "Sin prestadores para simular.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val chatId = "chat_${currentUserId}_${provider.id}"

            // 3. Simular saludo técnico
            val welcomeText = "Hola! Soy ${provider.name}. Analicé tu caso y preparé un presupuesto detallado para la solución técnica."
            simulateMessage(chatId, currentUserId, provider, welcomeText, MessageType.TEXT)

            delay(2500) // Pausa para realismo

            // 4. Crear el presupuesto profesional (Apto para BudgetA4View)
            val newBudget = createProfessionalA4Budget(currentUserId, provider)
            
            // 5. Persistencia Dual: Guardar en Room (PresupuestosScreen) y enviar mensaje (ChatScreen)
            budgetRepository.receiveBudgetFromChat(newBudget)
            
            simulateMessage(
                chatId = chatId,
                currentUserId = currentUserId,
                provider = provider,
                text = "Presupuesto Técnico #${newBudget.budgetId.takeLast(4)}",
                type = MessageType.BUDGET,
                relatedId = newBudget.budgetId
            )

            notificationHelper.showNotification("Maverick Fast", "Nueva propuesta de ${provider.displayName}")
            Toast.makeText(application, "Simulación completada con éxito.", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun selectProviderForSimulation(myId: String): Provider? {
        val all = providerRepository.allProviders.first()
        if (all.isEmpty()) return null
        
        // 1. Intentar con Maverick (Referencia)
        val maverick = all.find { it.id == "1001" }
        if (maverick != null) return maverick

        // 2. Intentar con alguien con quien ya hablé
        val activeIds = chatRepository.getActiveChatIds(myId).first()
        val existing = all.find { activeIds.contains(it.id) }
        
        return existing ?: all.random()
    }

    private suspend fun simulateMessage(
        chatId: String, currentUserId: String, provider: Provider, 
        text: String, type: MessageType, relatedId: String? = null
    ) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = provider.id,
            receiverId = currentUserId,
            type = type,
            content = text,
            timestamp = System.currentTimeMillis(),
            status = "SENT",
            relatedId = relatedId
        )
        chatRepository.sendMessage(message)
    }

    private fun createProfessionalA4Budget(clientId: String, provider: Provider): BudgetEntity {
        // Items diseñados para demostrar la visualización A4 (Paginación y Subtotales)
        val items = listOf(
            BudgetItem(description = "Mantenimiento Preventivo Especializado", quantity = 1, unitPrice = 18500.0),
            BudgetItem(description = "Repuesto: Ventilador Silencioso 120mm", quantity = 2, unitPrice = 4500.0),
            BudgetItem(description = "Pasta Térmica de Alto Rendimiento (Arctic MX-4)", quantity = 1, unitPrice = 3200.0),
            BudgetItem(description = "Limpieza de Ductos y Filtros de Aire", quantity = 1, unitPrice = 6000.0)
        )
        
        val subtotal = items.sumOf { it.unitPrice * it.quantity }

        return BudgetEntity(
            budgetId = "SIM-${Random.nextInt(1000, 9999)}",
            clientId = clientId,
            providerId = provider.id,
            providerName = provider.displayName,
            providerCompanyName = provider.companies.firstOrNull()?.name ?: "Servicios Maverick Fast",
            providerPhotoUrl = provider.photoUrl,
            items = items,
            subtotal = subtotal,
            grandTotal = subtotal,
            status = BudgetStatus.PENDIENTE,
            notes = "Presupuesto generado para simulación de flujo completo.",
            dateTimestamp = System.currentTimeMillis()
        )
    }
}

/**
 * COMPONENTE VISUAL: Botón de ejecución manual.
 */
@Composable
fun SimulateProviderBudgetButton(
    viewModel: SimulationViewModel = hiltViewModel(),
    clientId: String? = null
) {
    Button(onClick = { viewModel.simulateProviderWelcomeAndBudget(clientId) }) {
        Icon(Icons.Default.Psychology, null)
        Text("Iniciar Simulación Profesional")
    }
}
