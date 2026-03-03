

/**package com.example.myapplication.presentation.client

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
 * --- MOTOR DE SIMULACIÓN PROFESIONAL MAVERICK ULTRA ---
 * Centraliza la generación de datos falsos realistas para probar Comparativas,
 * Chats y la Pantalla de Promociones.
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

    // ==========================================================================================
    // --- PARÁMETROS AJUSTABLES (Modifica esto según tu necesidad) ---
    // ==========================================================================================
    private val TENDER_MASSIVE_COUNT = 20      // Cantidad de presupuestos para licitaciones
    private val CHAT_SIM_DELAY = 2500L         // Delay para realismo en chat (ms)
    private val PRICE_RIDICULOUS_CHANCE = 0.2  // 20% de probabilidad de precios "irrisorios"
    // ==========================================================================================

    /**
     * SIMULACIÓN A: CHAT INDIVIDUAL
     * Simula que un prestador te saluda y te envía un presupuesto desglosado por chat.
     */
    fun simulateProviderWelcomeAndBudget(specificClientId: String? = null) {
        viewModelScope.launch {
            val currentUserId = specificClientId ?: auth.currentUser?.uid ?: "user_demo_66"

            // Seleccionamos un prestador (Maverick o uno con chat previo)
            val provider = selectProviderForSimulation(currentUserId)
            if (provider == null) {
                Toast.makeText(application, "Sin prestadores en la BD.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val chatId = "chat_${currentUserId}_${provider.id}"

            // 1. Saludo inicial
            val welcomeText = "¡Hola! Soy ${provider.name}. Analicé tu solicitud y aquí te adjunto el presupuesto detallado con materiales y mano de obra."
            simulateMessage(chatId, currentUserId, provider, welcomeText, MessageType.TEXT)

            delay(CHAT_SIM_DELAY)

            // 2. Crear presupuesto con precios variados
            val newBudget = createProfessionalDesglosadoBudget(currentUserId, provider, null)
            budgetRepository.receiveBudgetFromChat(newBudget)

            // 3. Enviar el objeto presupuesto al chat
            simulateMessage(
                chatId = chatId,
                currentUserId = currentUserId,
                provider = provider,
                text = "Propuesta Técnica: ${newBudget.providerCompanyName}",
                type = MessageType.BUDGET,
                relatedId = newBudget.budgetId
            )

            notificationHelper.showNotification("Nuevo Mensaje", "${provider.displayName} te envió un presupuesto.")
            Toast.makeText(application, "Simulación de Chat enviada.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * SIMULACIÓN B: GENERACIÓN MASIVA PARA LICITACIONES
     * Crea 20 presupuestos automáticos para las licitaciones abiertas.
     * Útil para probar la "Comparativa de Columnas".
     */
    fun simulateMassiveTenderResponses() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val openTenders = budgetRepository.getOpenTenders()
            val allProviders = providerRepository.allProviders.first()

            if (openTenders.isEmpty()) {
                Toast.makeText(application, "Primero crea una Licitación ABIERTA.", Toast.LENGTH_LONG).show()
                return@launch
            }

            if (allProviders.isEmpty()) {
                Toast.makeText(application, "No hay prestadores en la base de datos.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            Toast.makeText(application, "Generando $TENDER_MASSIVE_COUNT presupuestos comparativos...", Toast.LENGTH_SHORT).show()

            // Tomamos una licitación al azar para llenarla de ofertas
            val targetTender = openTenders.random()

            repeat(TENDER_MASSIVE_COUNT) {
                // Elegimos un prestador al azar de los existentes
                val provider = allProviders.random()

                // Creamos un presupuesto desglosado con precios que varían mucho entre sí
                val budget = createProfessionalDesglosadoBudget(currentUserId, provider, targetTender.tenderId)
                budgetRepository.receiveBudgetFromChat(budget)
            }

            notificationHelper.showNotification("Licitación Completa", "Recibiste 20 nuevas ofertas para comparar.")
            Toast.makeText(application, "¡20 Presupuestos generados con éxito!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * SIMULACIÓN C: NUEVAS PROMOCIONES
     * "Activa" promociones simuladas para que aparezcan en PromoScreen.
     */
    fun simulateNewPromotions() {
        viewModelScope.launch {
            val allProviders = providerRepository.allProviders.first()
            if (allProviders.isEmpty()) return@launch

            // Simulamos que 5 prestadores lanzaron ofertas flash
            val luckyProviders = allProviders.shuffled().take(5)
            luckyProviders.forEach {
                notificationHelper.showNotification("Oferta Flash", "¡${it.displayName} publicó un nuevo descuento!")
            }
            Toast.makeText(application, "5 Nuevas Promociones detectadas.", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================================================================
    // --- LÓGICA INTERNA DE CONSTRUCCIÓN ---
    // ==========================================================================================

    private suspend fun selectProviderForSimulation(myId: String): Provider? {
        val all = providerRepository.allProviders.first()
        if (all.isEmpty()) return null
        return all.find { it.id == "1001" } ?: all.random()
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
            isRead = false // Nuevo campo obligatorio en la DB de mensajes
        )
        chatRepository.sendMessage(message)
    }

    /**
     * ESTA ES LA FUNCIÓN CLAVE PARA LA COMPARATIVA
     * Genera un BudgetEntity usando PARÁMETROS NOMBRADOS para evitar cualquier error de compilación.
     */
    private fun createProfessionalDesglosadoBudget(clientId: String, provider: Provider, tenderId: String?): BudgetEntity {

        // --- LÓGICA DE PRECIOS IRRISORIOS / REALISTAS ---
        val priceMultiplier = when {
            Random.nextDouble() < PRICE_RIDICULOUS_CHANCE -> Random.nextDouble(2.5, 5.0) // Muy caro
            Random.nextDouble() < 0.15 -> Random.nextDouble(0.3, 0.6) // Muy barato/oferta
            else -> Random.nextDouble(0.8, 1.3) // Precio Normal Mercado
        }

        // 1. MATERIALES (Items) - Uso de parámetros nombrados estrictos
        val items = listOf(
            BudgetItem(
                id = UUID.randomUUID().toString(),
                description = "Kit de Insumos Técnicos Cat.A",
                quantity = 1,
                unitPrice = 15000.0 * priceMultiplier,
                total = (15000.0 * priceMultiplier) * 1
            ),
            BudgetItem(
                id = UUID.randomUUID().toString(),
                description = "Componentes de Repuesto Original",
                quantity = 2,
                unitPrice = 4500.0 * priceMultiplier,
                total = (4500.0 * priceMultiplier) * 2
            )
        )

        // 2. MANO DE OBRA / SERVICIOS
        val services = listOf(
            BudgetService(
                id = UUID.randomUUID().toString(),
                description = "Mano de Obra Especializada",
                total = 25000.0 * priceMultiplier
            ),
            BudgetService(
                id = UUID.randomUUID().toString(),
                description = "Configuración y Testing",
                total = 12000.0 * priceMultiplier
            )
        )

        // 3. HONORARIOS PROFESIONALES
        val fees = listOf(
            BudgetProfessionalFee(
                id = UUID.randomUUID().toString(),
                description = "Dirección Técnica y Certificación",
                total = 10000.0 * priceMultiplier
            )
        )

        // 4. IMPUESTOS (Calculamos el 21% sobre el total)
        val subtotalValue = items.sumOf { it.total } + services.sumOf { it.total } + fees.sumOf { it.total }
        val taxes = listOf(
            BudgetTax(
                description = "IVA Inscrito (21%)",
                amount = subtotalValue * 0.21
            )
        )

        val totalValue = subtotalValue + taxes.sumOf { it.amount }

        return BudgetEntity(
            budgetId = "SIM-${UUID.randomUUID().toString().take(6).uppercase()}",
            clientId = clientId,
            providerId = provider.id,
            tenderId = tenderId,
            providerName = provider.displayName,
            providerCompanyName = provider.companies.firstOrNull()?.name ?: "${provider.lastName} Soluciones",
            providerPhotoUrl = provider.photoUrl,

            // 🔥 MAPEO DE LISTAS COMPLETAS PARA COMPARAR
            items = items,
            services = services,
            professionalFees = fees, // CORRECCIÓN: La variable en Room se llama professionalFees, no fees
            taxes = taxes,
            miscExpenses = emptyList(),

            subtotal = subtotalValue,
            grandTotal = totalValue,
            status = BudgetStatus.PENDIENTE,
            notes = if(priceMultiplier > 2.0) "Precio Premium por urgencia y repuestos importados." else "Presupuesto válido por 5 días.",
            dateTimestamp = System.currentTimeMillis()
        )
    }
}

**/


/**
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
**/