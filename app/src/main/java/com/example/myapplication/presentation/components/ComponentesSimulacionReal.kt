package com.example.myapplication.presentation.components

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.*
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
 * --- MOTOR DE SIMULACIÓN MAVERICK ULTRA 2.0 ---
 * Genera flujos de trabajo hiper-realistas:
 * 1. Mensajería con delays humanos.
 * 2. Presupuestos con desgloses técnicos por categoría.
 * 3. Variabilidad económica (Perfiles: Económico, Normal, Premium).
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

    fun simulateFullChatFlow() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val provider = selectRealisticProvider(currentUserId) ?: return@launch
            val chatId = "chat_${currentUserId}_${provider.id}"

            val greetings = listOf(
                "¡Hola! Vi tu consulta. Preparé un presupuesto detallado para la solución.",
                "¿Qué tal? Analicé tu caso y aquí tienes la cotización completa.",
                "Buenas tardes. Te envío el presupuesto con materiales y mano de obra incluidos."
            ).random()
            
            simulateMessage(chatId, currentUserId, provider, greetings, MessageType.TEXT)
            
            delay(3000) // Simula realismo

            val newBudget = createUltraRealisticBudget(currentUserId, provider, null)
            budgetRepository.receiveBudgetFromChat(newBudget)

            simulateMessage(
                chatId = chatId,
                currentUserId = currentUserId,
                provider = provider,
                text = "Presupuesto Técnico #${newBudget.budgetId.takeLast(4)}",
                type = MessageType.BUDGET,
                relatedId = newBudget.budgetId
            )

            notificationHelper.showNotification("Maverick", "${provider.displayName} te envió una propuesta.")
        }
    }

    fun simulateMassiveTenderResponses() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val openTenders = budgetRepository.getOpenTenders()
            val allProviders = providerRepository.allProviders.first()

            if (openTenders.isEmpty()) {
                Toast.makeText(application, "Crea una licitación ABIERTA primero.", Toast.LENGTH_LONG).show()
                return@launch
            }

            val targetTender = openTenders.random()
            repeat(15) {
                val provider = allProviders.random()
                val budget = createUltraRealisticBudget(currentUserId, provider, targetTender.tenderId, targetTender.category)
                budgetRepository.receiveBudgetFromChat(budget)
            }

            notificationHelper.showNotification("Licitación", "Recibiste 15 nuevas ofertas reales.")
        }
    }

    fun simulateNewPromotions() {
        viewModelScope.launch {
            notificationHelper.showNotification("Promo Flash", "¡Nuevos descuentos del 30% en Climatización detectados!")
        }
    }

    private suspend fun selectRealisticProvider(myId: String): Provider? {
        val all = providerRepository.allProviders.first()
        return if (all.isEmpty()) null else all.random()
    }

    private suspend fun simulateMessage(chatId: String, currentUserId: String, provider: Provider, text: String, type: MessageType, relatedId: String? = null) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = provider.id,
            receiverId = currentUserId,
            type = type,
            content = text,
            timestamp = System.currentTimeMillis(),
            relatedId = relatedId,
            status = "SENT"
        )
        chatRepository.sendMessage(message)
    }

    private fun createUltraRealisticBudget(clientId: String, provider: Provider, tenderId: String?, forceCategory: String? = null): BudgetEntity {
        val profileFactor = listOf(0.8, 1.0, 1.5, 2.5).random()
        val category = forceCategory ?: listOf("Electricidad", "Plomería", "Climatización").random()
        
        val items = mutableListOf<BudgetItem>()
        val services = mutableListOf<BudgetService>()
        val professionalFees = mutableListOf<BudgetProfessionalFee>()

        when(category) {
            "Electricidad" -> {
                items.add(BudgetItem(description = "Cable Unipolar 2.5mm (Normalizado)", quantity = 1, unitPrice = 45000.0 * profileFactor))
                items.add(BudgetItem(description = "Térmica Sica 2x20A", quantity = 2, unitPrice = 8500.0 * profileFactor))
                services.add(BudgetService(description = "Instalación de Tablero y Cableado Técnico", total = 30000.0 * profileFactor))
                if (profileFactor > 1.5) professionalFees.add(BudgetProfessionalFee(description = "Certificado de Aptitud Eléctrica", total = 12000.0))
            }
            "Plomería" -> {
                items.add(BudgetItem(description = "Kit Termofusión Agua Fría/Caliente", quantity = 1, unitPrice = 15000.0 * profileFactor))
                items.add(BudgetItem(description = "Grifería Monocomando Premium", quantity = 1, unitPrice = 85000.0 * profileFactor))
                services.add(BudgetService(description = "Mano de Obra: Instalación Sanitaria", total = 45000.0 * profileFactor))
            }
            "Climatización" -> {
                items.add(BudgetItem(description = "Carga Gas Refrigerante R410", quantity = 1, unitPrice = 28000.0 * profileFactor))
                services.add(BudgetService(description = "Mantenimiento y Limpieza de Filtros", total = 18000.0 * profileFactor))
            }
            else -> {
                items.add(BudgetItem(description = "Insumos Varios de Obra", quantity = 1, unitPrice = 15000.0 * profileFactor))
                services.add(BudgetService(description = "Servicio Técnico General", total = 25000.0 * profileFactor))
            }
        }

        val subtotal = items.sumOf { it.unitPrice * it.quantity } + services.sumOf { it.total } + professionalFees.sumOf { it.total }
        val taxes = subtotal * 0.21

        return BudgetEntity(
            budgetId = "SIM-${UUID.randomUUID().toString().take(5).uppercase()}",
            clientId = clientId,
            providerId = provider.id,
            tenderId = tenderId,
            category = category, // 🔥 Fundamental para el filtrado inteligente
            providerName = provider.displayName,
            providerCompanyName = provider.companies.firstOrNull()?.name ?: "${provider.lastName} Soluciones Técnicas",
            providerPhotoUrl = provider.photoUrl,
            items = items,
            services = services,
            professionalFees = professionalFees,
            subtotal = subtotal,
            taxAmount = taxes,
            grandTotal = subtotal + taxes,
            notes = if(profileFactor > 2.0) "Presupuesto Premium con garantía extendida de 24 meses." else "Válido por 48 horas.",
            status = BudgetStatus.PENDIENTE,
            dateTimestamp = System.currentTimeMillis()
        )
    }
}
