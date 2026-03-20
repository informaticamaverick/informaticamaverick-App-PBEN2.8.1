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
     * 🔥 [NUEVO] Simulación de 5 presupuestos directos de distintos prestadores al chat.
     * Garantiza el envío de al menos 5 mensajes de distintos prestadores con presupuestos.
     */
    fun simulateFiveDirectBudgetsToChat() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val allProviders = providerRepository.allProviders.first().shuffled()
            
            if (allProviders.size < 5) {
                Toast.makeText(application, "Necesitas al menos 5 prestadores para esta simulación.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val selectedProviders = allProviders.take(5)

            selectedProviders.forEach { provider ->
                val chatId = "chat_${currentUserId}_${provider.id}"
                
                // Mensaje de saludo realista
                val greetings = listOf(
                    "¡Hola! Analicé lo que necesitabas y te armé este presupuesto.",
                    "Buenas tardes, un gusto. Aquí te envío mi propuesta detallada.",
                    "Hola, vi tu pedido. Te adjunto el presupuesto para que lo revises.",
                    "¿Cómo estás? Te paso la cotización por el servicio solicitado.",
                    "¡Hola! Te envío el presupuesto técnico con el desglose de materiales."
                ).random()
                
                simulateMessage(chatId, currentUserId, provider, greetings, MessageType.TEXT)
                
                delay(Random.nextLong(1500, 3000)) // Delay para simular escritura

                // Crear presupuesto con precio variado
                val budget = createProfessionalDesglosadoBudget(currentUserId, provider, null)
                budgetRepository.receiveBudgetFromChat(budget)

                // Enviar mensaje del presupuesto al chat
                simulateMessage(
                    chatId = chatId,
                    currentUserId = currentUserId,
                    provider = provider,
                    text = "📄 Presupuesto Directo #${budget.budgetId.takeLast(4)}",
                    type = MessageType.BUDGET,
                    relatedId = budget.budgetId
                )
                
                delay(1000)
            }
            notificationHelper.showNotification("Simulación", "Has recibido 5 nuevos presupuestos en tus chats.")
        }
    }

    /**
     * 🔥 [NUEVO] Simulación de respuestas para TODAS las licitaciones activas.
     * Envía por lo menos 5 presupuestos de distintos prestadores por cada licitación abierta.
     */
    fun simulateTenderResponsesForEachActive() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: "user_demo_66"
            val openTenders = budgetRepository.getOpenTenders()
            
            if (openTenders.isEmpty()) {
                Toast.makeText(application, "No tienes licitaciones ABIERTAS para simular respuestas.", Toast.LENGTH_LONG).show()
                return@launch
            }

            openTenders.forEach { tender ->
                // Buscamos prestadores de la misma categoría de la licitación
                val matchingProviders = providerRepository.getProvidersByCategory(tender.category).shuffled()
                
                // Si no hay suficientes en la categoría, completamos con generales para llegar a 5
                val selectedProviders = if (matchingProviders.size >= 5) {
                    matchingProviders.take(5)
                } else {
                    val all = providerRepository.allProviders.first().shuffled()
                    (matchingProviders + all.filter { it.id !in matchingProviders.map { p -> p.id } }).take(5)
                }

                selectedProviders.forEach { provider ->
                    // Crear presupuesto vinculado a la licitación con alta variabilidad de precio
                    val budget = createProfessionalDesglosadoBudget(currentUserId, provider, tender.tenderId, tender.category)
                    budgetRepository.receiveBudgetFromChat(budget)
                    
                    // También enviamos un aviso al chat para mayor realismo
                    val chatId = "chat_${currentUserId}_${provider.id}"
                    simulateMessage(
                        chatId = chatId,
                        currentUserId = currentUserId,
                        provider = provider,
                        text = "¡Hola! He enviado una propuesta para tu licitación de '${tender.title}'. Quedo a tu disposición.",
                        type = MessageType.TEXT
                    )
                }
            }

            notificationHelper.showNotification("Licitaciones", "Se han generado ofertas para todas tus licitaciones activas.")
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
    private fun createProfessionalDesglosadoBudget(clientId: String, provider: Provider, tenderId: String?, forceCategory: String? = null): BudgetEntity {

        val randomValue = Random.nextDouble()
        val priceMultiplier = when {
            randomValue < PRICE_RIDICULOUS_CHANCE -> Random.nextDouble(2.5, 4.5)
            randomValue > (1.0 - PRICE_CHEAP_CHANCE) -> Random.nextDouble(0.4, 0.7)
            else -> Random.nextDouble(0.9, 1.2)
        }

        val category = forceCategory ?: provider.categories.firstOrNull() ?: "General"

        // 1. MATERIALES (BudgetItem usa unitPrice y quantity)
        val items = mutableListOf<BudgetItem>()
        
        when(category) {
            "Electricidad" -> {
                items.add(BudgetItem(description = "Cable Unipolar 2.5mm (Normalizado)", quantity = 1, unitPrice = 45000.0 * priceMultiplier))
                items.add(BudgetItem(description = "Térmica Sica 2x20A", quantity = 2, unitPrice = 8500.0 * priceMultiplier))
            }
            "Plomería" -> {
                items.add(BudgetItem(description = "Kit Termofusión Agua Fría/Caliente", quantity = 1, unitPrice = 15000.0 * priceMultiplier))
                items.add(BudgetItem(description = "Grifería Monocomando Premium", quantity = 1, unitPrice = 85000.0 * priceMultiplier))
            }
            else -> {
                items.add(BudgetItem(description = "Kit de Insumos Técnicos Cat.A", quantity = 1, unitPrice = 15000.0 * priceMultiplier))
                items.add(BudgetItem(description = "Componentes de Repuesto Original", quantity = 2, unitPrice = 4500.0 * priceMultiplier))
            }
        }
        
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
            category = category,
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
