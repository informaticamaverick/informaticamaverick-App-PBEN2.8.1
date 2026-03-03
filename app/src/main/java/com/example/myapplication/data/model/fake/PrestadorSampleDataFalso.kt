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
        val tenders = generateTenders(realCategories)

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

    private fun generateTenders(realCategories: List<CategoryEntity>): List<TenderEntity> {
        val categoryNames = realCategories.map { it.name }.ifEmpty { listOf("Informatica", "Hogar", "Electricidad") }

        val sampleTenders = listOf(
            TenderEntity(
                tenderId = "T-001",
                title = "Reparación de Techo",
                description = "Filtraciones en el techo del garaje. Requiere sellado urgente.",
                category = categoryNames.random(),
                status = "ABIERTA",
                dateTimestamp = System.currentTimeMillis() - 86400000 * 1
            ),
            TenderEntity(
                tenderId = "T-002",
                title = "Instalación Eléctrica Local",
                description = "Instalación completa de luminarias en local comercial nuevo.",
                category = categoryNames.random(),
                status = "ADJUDICADA",
                dateTimestamp = System.currentTimeMillis() - 86400000 * 10
            )
        )
        return sampleTenders.shuffled().take(Random.nextInt(1, 3))
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
                            doesShipping = false,
                            acceptsAppointments = true,
                            workingHours = "Lunes a Viernes: 10:00 a 18:00 hs",
                            galleryImages = listOf("https://images.unsplash.com/photo-1497215728101-856f4ea42174"),
                            employees = listOf(
                                EmployeeProvider(name = "Carlos", lastName = "López", position = "Gerente de Sucursal", detail = "Referente comercial local.", photoUrl = "https://picsum.photos/seed/carlos/200/200")
                            )
                        )
                    )
                )
            ),
            photoUrl = "https://picsum.photos/seed/maverick/200/200",
            bannerImageUrl = "https://picsum.photos/seed/maverick_banner/800/400",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1550751827-4bd374c3f58b",
                "https://images.unsplash.com/photo-1518770660439-4636190af475"
            ),
            favoriteProviderIds = emptyList(),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun generateRandomProvider(categoryName: String, index: Int): ProviderEntity {
        val id = "auto_${categoryName.lowercase()}_$index"
        val firstName = listOf("Carlos", "Luis", "Ana", "Marta", "Silvia", "Andrés", "Pedro", "Juan", "Bautista", "Emma", "Diego").random()
        val lastName = listOf("Díaz", "Sosa", "Rodríguez", "Fernández", "Guzmán", "López", "Pérez", "García", "Martínez").random()
        val isPremium = Random.nextDouble() < 0.4

        val companyCount = if (Random.nextDouble() < 0.3) Random.nextInt(2, 4) else 1
        val companies = (1..companyCount).map { generateRandomCompany(categoryName, lastName, it) }

        return ProviderEntity(
            id = id,
            email = "pro_$index@maverick.com",
            alternateEmail = null, 
            displayName = "$firstName $lastName",
            name = firstName,
            lastName = lastName,
            phoneNumber = "381-${Random.nextInt(4000000, 7000000)}",
            additionalPhones = emptyList(),
            matricula = if (isPremium) "REG-${Random.nextInt(1000, 9999)}" else null,
            titulo = if (isPremium) "Especialista en $categoryName" else "Profesional $categoryName",
            cuilCuit = "20-${Random.nextInt(10000000, 40000000)}-${Random.nextInt(0, 9)}", 

            address = AddressProvider(calle = "Calle Falsa", numero = "123", localidad = "Tucumán"), 

            works24h = Random.nextBoolean(),
            hasPhysicalLocation = Random.nextBoolean(),
            doesHomeVisits = true,
            doesShipping = Random.nextBoolean(),
            acceptsAppointments = true,
            isSubscribed = isPremium,
            isVerified = Random.nextBoolean(),
            isOnline = Random.nextBoolean(),
            isFavorite = false,

            rating = (37 + Random.nextInt(13)).toFloat() / 10f,
            workingHours = "09:00 a 18:00 hs", 
            categories = listOf(categoryName), 
            description = "Atención profesional en $categoryName para todo Tucumán.",

            companies = companies,
            hasCompanyProfile = true,

            photoUrl = "https://i.pravatar.cc/150?u=$id",
            bannerImageUrl = "https://picsum.photos/seed/$id/800/400",
            galleryImages = listOf("https://picsum.photos/seed/${id}g1/400/300", "https://picsum.photos/seed/${id}g2/400/300"),
            favoriteProviderIds = emptyList(),
            createdAt = System.currentTimeMillis() - Random.nextLong(0, 31536000000L)
        )
    }

    private fun generateRandomCompany(categoryName: String, ownerLastName: String, index: Int): CompanyProvider {
        val branchCount = Random.nextInt(1, 4)
        val allBranches = (1..branchCount).map { generateRandomBranch(it) }
        val companyId = UUID.randomUUID().toString()

        return CompanyProvider(
            id = companyId,
            name = "$ownerLastName $categoryName S.A. #$index",
            razonSocial = "$ownerLastName Servicios Integrales S.R.L.",
            cuit = "30-${Random.nextInt(20000000, 35000000)}-${Random.nextInt(0, 9)}",
            description = "Brindamos servicios de $categoryName con seriedad y confianza.",
            rating = 4.5f,
            categories = listOf(categoryName, "Atención Personalizada", "Turnos"), 
            productImages = listOf("https://picsum.photos/seed/${companyId}p1/400/300", "https://picsum.photos/seed/${companyId}p2/400/300"),
            photoUrl = "https://picsum.photos/seed/${companyId}logo/200/200",
            bannerImageUrl = "https://picsum.photos/seed/${companyId}banner/800/400",
            workingHours = "08:00 a 20:00 hs", 
            works24h = Random.nextBoolean(),
            hasPhysicalLocation = true,
            doesHomeVisits = true,
            doesShipping = Random.nextBoolean(),
            acceptsAppointments = true,
            isVerified = Random.nextBoolean(),

            mainBranch = allBranches.first(), 
            branches = if (allBranches.size > 1) allBranches.drop(1) else emptyList()
        )
    }

    private fun generateRandomBranch(index: Int): BranchProvider {
        val branchId = UUID.randomUUID().toString()
        val employees = (1..Random.nextInt(1, 4)).map {
            EmployeeProvider(
                name = listOf("Juan", "Pedro", "Santiago", "Agustín").random(),
                lastName = listOf("Páez", "López", "García", "Martínez").random(),
                position = listOf("Profesional", "Supervisor", "Asistente").random(),
                detail = "Miembro calificado del equipo.",
                photoUrl = "https://i.pravatar.cc/150?u=${UUID.randomUUID()}"
            )
        }

        return BranchProvider(
            id = branchId,
            name = "Sucursal ${listOf("Centro", "Norte", "Sur", "Yerba Buena").random()} #$index",
            address = AddressProvider(
                calle = listOf("Av. Aconquija", "San Martín", "Belgrano").random(),
                numero = Random.nextInt(100, 3000).toString(),
                localidad = "Tucumán"
            ),
            works24h = false,
            hasPhysicalLocation = true,
            doesHomeVisits = true,
            doesShipping = false,
            acceptsAppointments = true,
            isVerified = true,
            rating = 4.2f,
            workingHours = "09:00 a 13:00 y 17:00 a 21:00 hs",
            galleryImages = listOf("https://picsum.photos/seed/${branchId}g1/400/300", "https://picsum.photos/seed/${branchId}g2/400/300"),
            employees = employees
        )
    }

    data class DataSeedBundle(
        val providers: List<ProviderEntity>,
        val tenders: List<TenderEntity>,
        val budgets: List<BudgetEntity>,
        val messages: List<MessageEntity>,
        val calendarEvents: List<CalendarEventEntity>
    )
}
