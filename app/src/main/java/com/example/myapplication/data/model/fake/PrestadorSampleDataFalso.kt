package com.example.myapplication.data.model.fake

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.data.local.*
import com.example.myapplication.data.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

/**
 * --- GENERADOR DE DATOS DE PRUEBA MAESTRO (VERSIÓN FINAL INTERCONECTADA) ---
 * Propósito: Poblar Room con un ecosistema completo y funcional de Prestadores, Licitaciones y Eventos de Agenda.
 */
object PrestadorSampleDataFalso {

    const val CLIENT_ID = "user_demo_66" // Hecho público para que CalendarScreen y ChatViewModel lo puedan leer

    /**
     * Función principal llamada desde AppDatabase.
     * @param realCategories Lista de categorías obtenidas de la base de datos para sembrar prestadores.
     */
    fun generateAll(realCategories: List<CategoryEntity>): DataSeedBundle {

        // PASO 1: Generar Prestadores (Ahora todos con empresa y sucursal y la NUEVA ESTRUCTURA)
        val providers = generateProviders(realCategories)

        // PASO 2: Generar Licitaciones de ejemplo
        val tenders = generateTenders(realCategories, providers)

        // PASO 3: Generar Agenda Técnica (Visitas, Turnos, Envíos)
        val calendarEvents = generateCalendarEvents(providers)

        return DataSeedBundle(
            providers = providers,
            tenders = tenders,
            budgets = emptyList(),
            messages = emptyList(),
            calendarEvents = calendarEvents
        )
    }

    private fun generateTenders(realCategories: List<CategoryEntity>, providers: List<ProviderEntity>): List<TenderEntity> {
        val categoryNames = realCategories.map { it.name }.ifEmpty { listOf("Informatica", "Hogar", "Electricidad") }
        val tenders = mutableListOf<TenderEntity>()

        val titlesMap = mapOf(
            "ABIERTA" to listOf("Arreglo de Techo", "Pintura de Fachada", "Instalación de AC"),
            "CERRADA" to listOf("Mantenimiento de Jardín", "Limpieza de Tanque", "Recableado Eléctrico"),
            "ADJUDICADA" to listOf("Reparación de Cañería", "Instalación de Cámaras", "Servicio de Flete"),
            "CANCELADA" to listOf("Construcción de Quincho", "Pulido de Pisos", "Cambio de Aberturas")
        )

        // Generamos al menos 2 por cada estado solicitado
        listOf("ABIERTA", "CERRADA", "ADJUDICADA", "CANCELADA").forEach { status ->
            val titles = titlesMap[status] ?: listOf("Tarea de $status")

            repeat(Random.nextInt(2, 4)) { i ->
                val id = "T-${status.take(1)}-$i-${UUID.randomUUID().toString().take(4)}"
                val provider = if (status == "ADJUDICADA") providers.random() else null

                tenders.add(
                    TenderEntity(
                        tenderId = id,
                        title = titles.random() + " #$i",
                        isActive = status == "ABIERTA",
                        clientId = CLIENT_ID,
                        description = "Requerimiento para el hogar/negocio. Se solicita presupuesto detallado para $status.",
                        category = categoryNames.random(),
                        status = status,
                        dateTimestamp = System.currentTimeMillis() - (86400000 * Random.nextInt(1, 16)),
                        startDate = System.currentTimeMillis() - (86400000 * Random.nextInt(1, 5)),
                        endDate = System.currentTimeMillis() + (86400000 * Random.nextInt(5, 20)),
                        cancellationDate = if (status == "CANCELADA") System.currentTimeMillis() - 7200000 else null,
                        awardedProviderId = provider?.id,
                        awardedProviderName = provider?.displayName,
                        budgetCount = Random.nextInt(1, 12),
                        requiresVisit = Random.nextBoolean(),
                        requiresPaymentMethod = Random.nextBoolean(),
                        requiresWorkGuarantee = Random.nextBoolean(),
                        requiresProviderDoc = Random.nextBoolean(),
                        locationAddress = "Calle Falsa",
                        locationNumber = "123",
                        locationLocality = "San Miguel de Tucumán",
                        locationType = "PERSONAL"
                    )
                )
            }
        }
        return tenders.shuffled()
    }

    private fun generateProviders(realCategories: List<CategoryEntity>): List<ProviderEntity> {
        val providers = mutableListOf<ProviderEntity>()

        // El prestador Maverick (ID 1001) es nuestra referencia de calidad absoluta. Carga COMPLETA.
        providers.add(generateMaverickProvider())

        realCategories.forEach { category ->
            // Generamos entre 8 y 20 prestadores para CADA categoría
            val countPerCategory = Random.nextInt(8, 21)

            repeat(countPerCategory) { index ->
                if (!(category.name == "Informatica" && index == 0)) {
                    providers.add(generateRandomProvider(category.name, index))
                }
            }
        }
        return providers
    }

    /**
     * Genera una agenda activa con turnos en locales, visitas técnicas y envíos.
     */
    private fun generateCalendarEvents(providers: List<ProviderEntity>): List<CalendarEventEntity> {
        val events = mutableListOf<CalendarEventEntity>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val baseTime = System.currentTimeMillis()
        val dayInMillis = 86400000L

        val activeProvidersForCalendar = providers.shuffled().take(25)

        activeProvidersForCalendar.forEachIndexed { index, provider ->

            // Extraemos la primera categoría (Como ahora es lista, usamos firstOrNull)
            val mainCategory = provider.categories.firstOrNull() ?: "Servicios"
            val catLower = mainCategory.lowercase()

            val eventType = when {
                catLower.contains("flete") || catLower.contains("transporte") || catLower.contains("envío") -> EventType.SHIPPING
                catLower.contains("peluquería") || catLower.contains("cancha") || catLower.contains("salud") || catLower.contains("estética") -> EventType.APPOINTMENT
                else -> listOf(EventType.VISIT, EventType.APPOINTMENT).random()
            }

            // Usamos la Casa Central si existe, si no, una dirección de prueba
            val firstCompany = provider.companies.firstOrNull()
            val branchAddress = firstCompany?.mainBranch?.address ?: firstCompany?.branches?.firstOrNull()?.address

            val address = if (eventType == ImageVector.Companion.hashCode().let { EventType.APPOINTMENT } && branchAddress != null) {
                "${branchAddress.calle} ${branchAddress.numero}, ${branchAddress.localidad}"
            } else {
                "Barrio Matienzo 1339, San Miguel de Tucumán"
            }

            val daysOffset = Random.nextInt(-2, 8)
            val eventTimeMillis = baseTime + (daysOffset * dayInMillis)
            val dateStr = dateFormat.format(Date(eventTimeMillis))

            val hour = Random.nextInt(9, 18)
            val minute = listOf("00", "15", "30", "45").random()
            val timeStr = "${hour.toString().padStart(2, '0')}:$minute"

            val title = when (eventType) {
                EventType.VISIT -> "Revisión en domicilio - $mainCategory"
                EventType.APPOINTMENT -> "Reserva de turno en local"
                EventType.SHIPPING -> "Entrega de pedido"
            }

            val status = when {
                daysOffset < 0 -> listOf(VisitStatus.CONFIRMED, VisitStatus.CANCELLED).random()
                daysOffset == 0 -> VisitStatus.CONFIRMED
                else -> listOf(VisitStatus.CONFIRMED, VisitStatus.PENDING).random()
            }

            events.add(
                CalendarEventEntity(
                    id = UUID.randomUUID().toString(),
                    date = dateStr,
                    time = timeStr,
                    type = eventType,
                    title = title,
                    provider = provider.displayName,
                    providerId = provider.id,
                    address = address,
                    status = status,
                    providerPhotoUrl = provider.photoUrl
                )
            )
        }

        return events
    }

    /**
     * 🔥 MAVERICK: EL GOLD STANDARD (Estructura COMPLETAMENTE cargada)
     */
    private fun generateMaverickProvider(): ProviderEntity {
        return ProviderEntity(
            id = "1001",
            email = "MAVERICKINFORMATICA@maverick.com",
            alternateEmail = "maximiliano.nanterne@gmail.com",
            displayName = "Maverick Informática",
            name = "Maximiliano",
            lastName = "Nanterne",
            phoneNumber = "381-1234567",
            additionalPhones = listOf("381-7654321"),
            matricula = "MP-9922",
            titulo = "Ingeniero de Software & Tech Lead",
            cuilCuit = "20-30405060-7", 

            address = AddressProvider(
                calle = "San Martín", numero = "450",
                localidad = "San Miguel de Tucumán", provincia = "Tucumán",
                pais = "Argentina", codigoPostal = "4000",
                latitude = -26.830, longitude = -65.202
            ),

            works24h = true,
            hasPhysicalLocation = true,
            doesHomeVisits = true,
            doesShipping = true,
            acceptsAppointments = true,
            isSubscribed = true, // Premium
            isVerified = true,
            isFavorite = true,
            isOnline = true,

            rating = 5.0f,
            workingHours = "Lunes a Sábado: 09:00 a 20:00 hs", 
            categories = listOf("Informatica", "Desarrollo Móvil", "Seguridad", "Redes"), 
            description = "Especialistas en soluciones tecnológicas de alta complejidad. Desarrollo de software nativo y multiplataforma.",

            hasCompanyProfile = true,
            companies = listOf(
                CompanyProvider(
                    name = "Maverick Tech S.A.",
                    razonSocial = "Maverick Soluciones Digitales S.R.L.",
                    cuit = "30-12345678-9",
                    description = "Nuestra misión es llevar la tecnología a cada negocio de Tucumán.",
                    rating = 4.9f,
                    categories = listOf("Consultoría IT", "Hardware", "Software"), 
                    productImages = listOf(
                        "https://images.unsplash.com/photo-1517694712202-14dd9538aa97",
                        "https://images.unsplash.com/photo-1498050108023-c5249f4df085"
                    ),
                    photoUrl = "https://picsum.photos/seed/maverick_logo/200/200",
                    bannerImageUrl = "https://picsum.photos/seed/maverick_corp_banner/800/400",
                    workingHours = "Lunes a Viernes: 08:00 a 18:00 hs", 
                    works24h = false,
                    hasPhysicalLocation = true,
                    doesHomeVisits = true,
                    doesShipping = true,
                    acceptsAppointments = true,
                    isVerified = true,

                    mainBranch = BranchProvider(
                        name = "Sede Central Barrio Sur",
                        address = AddressProvider(calle = "Lavalle", numero = "1500", localidad = "San Miguel de Tucumán"),
                        works24h = true,
                        hasPhysicalLocation = true,
                        doesShipping = true,
                        acceptsAppointments = true,
                        workingHours = "Lunes a Sábado: 09:00 a 20:00 hs",
                        galleryImages = listOf("https://images.unsplash.com/photo-1497366216548-37526070297c"),
                        employees = listOf(
                            EmployeeProvider(name = "Maximiliano", lastName = "Nanterne", position = "CEO & Founder", detail = "Fundador y líder técnico del proyecto.", photoUrl = "https://picsum.photos/seed/max/200/200"),
                            EmployeeProvider(name = "Ana", lastName = "Gómez", position = "Líder de Soporte", detail = "Encargada de la experiencia del cliente.", photoUrl = "https://picsum.photos/seed/ana/200/200")
                        )
                    ),

                    branches = listOf(
                        BranchProvider(
                            name = "Sucursal Yerba Buena",
                            address = AddressProvider(calle = "Av. Aconquija", numero = "2000", localidad = "Yerba Buena"),
                            works24h = false,
                            hasPhysicalLocation = true,
                            doesShipping = true,
                            acceptsAppointments = true,
                            workingHours = "Lunes a Viernes: 10:00 a 19:00 hs",
                            galleryImages = listOf("https://images.unsplash.com/photo-1497215728101-856f4ea42174"),
                            employees = emptyList()
                        )
                    )
                )
            ),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun generateRandomProvider(category: String, index: Int): ProviderEntity {
        val names = listOf("Juan", "Pedro", "Carlos", "Luis", "Miguel", "Jorge", "Andrés", "Marcos")
        val lastNames = listOf("Pérez", "García", "López", "Rodríguez", "Sánchez", "Martínez", "Gómez", "Díaz")
        val name = names.random()
        val lastName = lastNames.random()
        val isPremium = Random.nextInt(0, 10) > 7

        return ProviderEntity(
            id = "P-${category.take(3)}-$index",
            email = "${name.lowercase()}.${lastName.lowercase()}@example.com",
            displayName = "$name $lastName",
            name = name,
            lastName = lastName,
            phoneNumber = "381-${Random.nextInt(1000000, 9999999)}",
            rating = 3.5f + (Random.nextFloat() * 1.5f),
            categories = listOf(category),
            description = "Servicio profesional de $category. Amplia experiencia y garantía.",
            isSubscribed = isPremium,
            isVerified = Random.nextBoolean(),
            isOnline = Random.nextBoolean(),
            hasCompanyProfile = Random.nextBoolean(),
            address = AddressProvider(
                calle = "Calle ${Random.nextInt(1, 100)}",
                numero = "${Random.nextInt(100, 2000)}",
                localidad = "San Miguel de Tucumán"
            ),
            createdAt = System.currentTimeMillis()
        )
    }
}

data class DataSeedBundle(
    val providers: List<ProviderEntity>,
    val tenders: List<TenderEntity>,
    val budgets: List<BudgetEntity>,
    val messages: List<MessageEntity>,
    val calendarEvents: List<CalendarEventEntity>
)
