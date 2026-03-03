package com.example.myapplication.presentation.client

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
 * Genera datos falsos realistas (Presupuestos desglosados, Chats, Promociones)
 * para probar el UI, basándose estrictamente en los modelos de datos locales.
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
    // --- PARÁMETROS AJUSTABLES PARA EL DESARROLLADOR ---
    // ==========================================================================================
    private val TENDER_MASSIVE_COUNT = 20      // Cantidad de presupuestos a generar en Licitaciones
    private val CHAT_SIM_DELAY = 2500L         // Tiempo de "escribiendo..." en el chat (ms)
    private val PRICE_RIDICULOUS_CHANCE = 0.20 // 20% probabilidad de precio carísimo
    private val PRICE_CHEAP_CHANCE = 0.15      // 15% probabilidad de muy barato/oferta
    // ==========================================================================================

    /**
     * SIMULACIÓN A: CHAT INDIVIDUAL
     * Simula que un prestador te saluda y te envía un presupuesto desglosado por chat.
     */
    fun simulateProviderWelcomeAndBudget(specificClientId: String? = null) {
        viewModelScope.launch {
            val currentUserId = specificClientId ?: auth.currentUser?.uid ?: "user_demo_66"

            val provider = selectProviderForSimulation(currentUserId)
            if (provider == null) {
                Toast.makeText(application, "Sin prestadores en la BD para simular.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val chatId = "chat_${currentUserId}_${provider.id}"

            // 1. Mensaje de bienvenida
            val welcomeText = "¡Hola! Soy ${provider.name}. Analicé tu solicitud y aquí te adjunto el presupuesto detallado con materiales, mano de obra e impuestos."
            simulateMessage(chatId, currentUserId, provider, welcomeText, MessageType.TEXT)

            delay(CHAT_SIM_DELAY)

            // 2. Crear presupuesto técnico respetando las nuevas Data Classes
            val newBudget = createProfessionalDesglosadoBudget(currentUserId, provider, null)
            budgetRepository.receiveBudgetFromChat(newBudget)

            // 3. Enviar la tarjeta del presupuesto al chat
            simulateMessage(
                chatId = chatId,
                currentUserId = currentUserId,
                provider = provider,
                text = "Propuesta Técnica: ${newBudget.providerCompanyName ?: provider.displayName}",
                type = MessageType.BUDGET,
                relatedId = newBudget.budgetId
            )

            notificationHelper.showNotification("Nuevo Mensaje", "${provider.displayName} te envió un presupuesto.")
            Toast.makeText(application, "Simulación de Chat enviada.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * SIMULACIÓN B: GENERACIÓN MASIVA PARA LICITACIONES (20 Presupuestos)
     * Crea respuestas automáticas para probar la tabla comparativa de columnas.
     * 🔥 Nombre restaurado a simulateTenderResponses para mantener compatibilidad con HomeScreenCliente3
     */
    fun simulateTenderResponses() {
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

            val targetTender = openTenders.random()

            // Filtramos proveedores que coincidan con la categoría de la licitación
            val validProviders = allProviders.filter { provider ->
                provider.categories.any { it.equals(targetTender.category, ignoreCase = true) }
            }.ifEmpty { allProviders }

            repeat(TENDER_MASSIVE_COUNT) {
                val provider = validProviders.random()
                val budget = createProfessionalDesglosadoBudget(currentUserId, provider, targetTender.tenderId)
                budgetRepository.receiveBudgetFromChat(budget)
            }

            notificationHelper.showNotification("Licitación Completa", "Recibiste $TENDER_MASSIVE_COUNT nuevas ofertas para comparar.")
            Toast.makeText(application, "¡Presupuestos generados con éxito!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * SIMULACIÓN C: NUEVAS PROMOCIONES
     */
    fun simulateNewPromotions() {
        viewModelScope.launch {
            val allProviders = providerRepository.allProviders.first()
            if (allProviders.isEmpty()) return@launch

            val luckyProviders = allProviders.shuffled().take(3)
            luckyProviders.forEach {
                notificationHelper.showNotification("Oferta Flash 🔥", "¡${it.displayName} publicó un nuevo descuento en sus servicios!")
            }
            Toast.makeText(application, "Nuevas Promociones simuladas.", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================================================================
    // --- LÓGICA INTERNA DE CONSTRUCCIÓN ---
    // ==========================================================================================

    private suspend fun selectProviderForSimulation(myId: String): Provider? {
        val all = providerRepository.allProviders.first()
        if (all.isEmpty()) return null

        val maverick = all.find { it.id == "1001" }
        if (maverick != null) return maverick

        val activeIds = chatRepository.getActiveChatIds(myId).first()
        return all.find { activeIds.contains(it.id) } ?: all.random()
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
            isRead = false
        )
        chatRepository.sendMessage(message)
    }

    /**
     * 🔥 CREADOR DE PRESUPUESTOS (ADAPTADO EXACTAMENTE A TUS DATA CLASSES)
     */
    private fun createProfessionalDesglosadoBudget(clientId: String, provider: Provider, tenderId: String?): BudgetEntity {

        val randomValue = Random.nextDouble()
        val priceMultiplier = when {
            randomValue < PRICE_RIDICULOUS_CHANCE -> Random.nextDouble(2.5, 4.5)
            randomValue > (1.0 - PRICE_CHEAP_CHANCE) -> Random.nextDouble(0.4, 0.7)
            else -> Random.nextDouble(0.9, 1.2)
        }

        // 1. MATERIALES (BudgetItem usa unitPrice y quantity)
        val items = listOf(
            BudgetItem(
                code = "MAT-01",
                description = "Kit de Insumos Técnicos Cat.A",
                quantity = 1,
                unitPrice = 15000.0 * priceMultiplier
            ),
            BudgetItem(
                code = "MAT-02",
                description = "Componentes de Repuesto Original",
                quantity = 2,
                unitPrice = 4500.0 * priceMultiplier
            )
        )
        val itemsTotal = items.sumOf { it.unitPrice * it.quantity }

        // 2. SERVICIOS (BudgetService usa solo total)
        val services = listOf(
            BudgetService(
                code = "SRV-01",
                description = "Mano de Obra Especializada",
                total = 25000.0 * priceMultiplier
            ),
            BudgetService(
                code = "SRV-02",
                description = "Configuración y Testing de Sistemas",
                total = 12000.0 * priceMultiplier
            )
        )
        val servicesTotal = services.sumOf { it.total }

        // 3. HONORARIOS (BudgetProfessionalFee usa solo total)
        val fees = listOf(
            BudgetProfessionalFee(
                code = "FEE-01",
                description = "Dirección Técnica y Certificación",
                total = 10000.0 * priceMultiplier
            )
        )
        val feesTotal = fees.sumOf { it.total }

        // 4. IMPUESTOS (BudgetTax usa amount)
        val subtotalValue = itemsTotal + servicesTotal + feesTotal
        val calculatedTax = subtotalValue * 0.21
        val taxes = listOf(
            BudgetTax(
                description = "IVA Inscrito (21%)",
                amount = calculatedTax
            )
        )
        val taxesTotal = taxes.sumOf { it.amount }

        val grandTotalValue = subtotalValue + taxesTotal

        // Configuramos características adicionales aleatorias
        val paymentMethod = listOf("Transferencia / Efectivo", "Tarjetas (3 Cuotas sin interés)", "Efectivo 10% OFF").random()
        val warranty = listOf("Garantía de 3 meses", "Garantía oficial de 1 año", "Sin garantía extendida").random()
        val execution = listOf("Aproximadamente 2 días", "Ejecución inmediata", "Requiere 1 semana de planificación").random()

        return BudgetEntity(
            budgetId = "SIM-${UUID.randomUUID().toString().take(6).uppercase()}",
            clientId = clientId,
            providerId = provider.id,
            tenderId = tenderId,
            providerName = provider.displayName,
            providerCompanyName = provider.companies.firstOrNull()?.name,
            providerPhotoUrl = provider.photoUrl,

            // Asignación a las listas
            items = items,
            services = services,
            professionalFees = fees,
            miscExpenses = emptyList(), // Queda vacío en esta simulación
            taxes = taxes,
            imageUrls = emptyList(),

            // Totales
            subtotal = subtotalValue,
            taxAmount = taxesTotal,
            discountAmount = 0.0,
            grandTotal = grandTotalValue,

            // Características comerciales
            validityDays = if (priceMultiplier < 0.7) 3 else 15,
            notes = if(priceMultiplier > 2.0) "Precio Premium VIP. Incluye repuestos importados y prioridad de urgencia." else "Presupuesto estándar. Sujeto a disponibilidad de agenda.",
            paymentMethods = paymentMethod,
            warrantyInfo = warranty,
            executionTime = execution,

            status = BudgetStatus.PENDIENTE,
            dateTimestamp = System.currentTimeMillis()
        )
    }
}

/**
package com.example.myapplication.presentation.client

import android.app.Application
import android.widget.Toast
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
 * VIEWMODEL DE SIMULACIÓN PROFESIONAL (MAVERICK FAST)
 * [ACTUALIZADO] Adaptado a la nueva estructura de categorías (List<String>) y campos de Provider.
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
                    delay(index * 2000L)
                    executeDirectChatSimulation(currentUserId, provider)
                }
            }
        }
    }

    /**
     * Simulación B: RESPUESTAS A LICITACIONES (TENDERS)
     * Busca licitaciones abiertas y genera presupuestos técnicos.
     */
    fun simulateTenderResponses() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val openTenders = budgetRepository.getOpenTenders()
            val allProviders = providerRepository.allProviders.first()

            if (openTenders.isEmpty()) {
                Toast.makeText(application, "Crea una Licitación ABIERTA para simular.", Toast.LENGTH_LONG).show()
                return@launch
            }

            var count = 0
            openTenders.forEach { tender ->
                // 🔥 CORRECCIÓN: Buscamos prestadores donde su LISTA de categorías contenga la categoría de la licitación
                val candidates = allProviders.filter { provider ->
                    provider.categories.any { it.equals(tender.category, ignoreCase = true) }
                }

                candidates.shuffled().take(Random.nextInt(1, 3)).forEach { provider ->
                    val budget = createProfessionalA4Budget(currentUserId, provider, tender.tenderId)
                    budgetRepository.receiveBudgetFromChat(budget)
                    count++
                }
            }

            if (count > 0) {
                notificationHelper.showNotification("Gestión Comercial", "Recibiste $count propuestas nuevas.")
                Toast.makeText(application, "Se generaron $count presupuestos.", Toast.LENGTH_SHORT).show()
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
            BudgetItem(description = "Insumos y Materiales", quantity = 1, unitPrice = 12000.0),
            BudgetItem(description = "Mano de Obra Calificada", quantity = 1, unitPrice = 8000.0)
        )
        val total = items.sumOf { it.unitPrice * it.quantity }

        return BudgetEntity(
            budgetId = "SIM-${Random.nextInt(1000, 9999)}",
            clientId = clientId,
            providerId = provider.id,
            tenderId = tenderId,
            providerName = provider.displayName,
            // 🔥 Usamos la información real de la empresa si existe
            providerCompanyName = provider.companies.firstOrNull()?.name ?: "${provider.lastName} Soluciones",
            providerPhotoUrl = provider.photoUrl,
            items = items,
            subtotal = total,
            grandTotal = total,
            status = BudgetStatus.PENDIENTE,
            notes = if (tenderId != null) "Respuesta a Licitación Ref: $tenderId" else "Presupuesto directo Maverick.",
            dateTimestamp = System.currentTimeMillis()
        )
    }
}



**/



/**package com.example.myapplication.presentation.client

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
                val candidates = allProviders.filter { it.categories.equals(tender.categories, true) }
                
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
**/