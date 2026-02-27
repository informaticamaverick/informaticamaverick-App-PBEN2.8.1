package com.example.myapplication.data.model.fake

import com.example.myapplication.data.local.*
import com.example.myapplication.data.model.*
import java.util.UUID
import kotlin.random.Random

/**
 * --- GENERADOR DE DATOS DE PRUEBA MAESTRO (VERSIÓN FINAL INTERCONECTADA) ---
 * Propósito: Poblar Room con un ecosistema completo y funcional de Prestadores y Licitaciones.
 * 
 * Cambios realizados:
 * - Se añadió la siembra de Licitaciones (Tenders) de ejemplo (2 a 5 con diferentes estados).
 * - Se desactivó (comentó) la siembra automática de presupuestos y chats.
 */
object PrestadorSampleDataFalso {

    private const val CLIENT_ID = "user_demo_66"

    /**
     * Función principal llamada desde AppDatabase.
     * @param realCategories Lista de categorías obtenidas de la base de datos para sembrar prestadores.
     */
    fun generateAll(realCategories: List<CategoryEntity>): DataSeedBundle {

        // PASO 1: Generar Prestadores
        val providers = generateProviders(realCategories)

        // PASO 2: Generar Licitaciones de ejemplo (2 a 5 con diferentes estados)
        val tenders = generateTenders(realCategories)

        // PASO 3: [DESACTIVADO] Generar la interacción compleja (Presupuestos + Historial de Mensajes)
        // La siembra de presupuestos y chats ha sido comentada por pedido del usuario para realizarse manualmente.
        // val interactionData = generateBudgetsAndChatHistory(providers, tenders)

        return DataSeedBundle(
            providers = providers,
            tenders = tenders,
            budgets = emptyList(), 
            messages = emptyList()
        )
    }

    private fun generateProviders(realCategories: List<CategoryEntity>): List<ProviderEntity> {
        val providers = mutableListOf<ProviderEntity>()

        // El prestador Maverick (ID 1001) es nuestra referencia de calidad absoluta
        providers.add(generateMaverickProvider()) 

        realCategories.forEach { category ->
            // Generamos entre 5 y 20 prestadores para CADA categoría
            val countPerCategory = Random.nextInt(5, 21)

            repeat(countPerCategory) { index ->
                if (!(category.name == "Informatica" && index == 0)) {
                    // CORREGIDO: Se pasan solo 2 argumentos según la definición esperada
                    providers.add(generateRandomProvider(category.name, index))
                }
            }
        }
        return providers
    }

    /**
     * Genera de 2 a 5 licitaciones de ejemplo con diferentes estados y categorías.
     */
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
            ),
            TenderEntity(
                tenderId = "T-003",
                title = "Pintura de Fachada",
                description = "Pintar frente de casa de 2 plantas.",
                category = categoryNames.random(),
                status = "CANCELADA",
                dateTimestamp = System.currentTimeMillis() - 86400000 * 5
            ),
            TenderEntity(
                tenderId = "T-004",
                title = "Mantenimiento Aire Acondicionado",
                description = "Limpieza de filtros y carga de gas para 3 equipos split.",
                category = categoryNames.random(),
                status = "CERRADA",
                dateTimestamp = System.currentTimeMillis() - 86400000 * 20
            ),
            TenderEntity(
                tenderId = "T-005",
                title = "Servicio de Plomería",
                description = "Cambio de grifería y reparación de mochila en baño.",
                category = categoryNames.random(),
                status = "ABIERTA",
                dateTimestamp = System.currentTimeMillis()
            )
        )

        return sampleTenders.shuffled().take(Random.nextInt(8, 15))
    }

    /**
     * [COMENTADO] Lógica de interacción automática desactivada por pedido del usuario.
     * Los presupuestos se cargarán manualmente vía simulación.
     */
    /*
    private fun generateBudgetsAndChatHistory(
        providers: List<ProviderEntity>,
        tenders: List<TenderEntity>
    ): Pair<List<BudgetEntity>, List<MessageEntity>> {
        return Pair(emptyList(), emptyList())
    }
    */

    private fun generateMaverickProvider(): ProviderEntity {
        return ProviderEntity(
            id = "1001",
            email = "contacto@maverick.com",
            displayName = "Maverick Informática",
            name = "Maximiliano",
            lastName = "Nanterne",
            phoneNumber = "343-1234567",
            category = "Informatica",
            additionalEmails = emptyList(),
            additionalPhones = emptyList(),
            matricula = "MP-9922",
            titulo = "Ingeniero de Software",
            photoUrl = "https://picsum.photos/seed/maverick/200/200",
            bannerImageUrl = "https://picsum.photos/seed/maverick_banner/800/400",
            galleryImages = emptyList(),
            personalAddresses = emptyList(),
            companies = listOf(
                CompanyProvider(
                    name = "Maverick Tech S.A.",
                    razonSocial = "Maverick Soluciones Digitales S.R.L.",
                    cuit = "20-12345678-9",
                    services = listOf("Informatica", "Seguridad", "Redes"),
                    works24h = true,
                    doesHomeVisits = true,
                    hasPhysicalLocation = true,
                    description = "Expertos en soluciones tecnológicas de alta complejidad.",
                    branches = listOf(
                        BranchProvider(
                            name = "Casa Central",
                            address = AddressProvider(calle = "Av. Siempre Viva", numero = "742", localidad = "Springfield"),
                            employees = listOf(
                                EmployeeProvider(name = "Lisa", lastName = "Simpson", position = "Gerente de Proyectos"),
                                EmployeeProvider(name = "Bart", lastName = "Simpson", position = "Técnico de Campo")
                            )
                        )
                    )
                )
            ),
            hasCompanyProfile = true,
            isSubscribed = true,
            isVerified = true,
            isOnline = true,
            isFavorite = false,
            rating = 5.0f,
            favoriteProviderIds = emptyList(),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun generateRandomProvider(categoryName: String, index: Int): ProviderEntity {
        val id = "auto_${categoryName.lowercase()}_$index"
        val firstName = listOf("Carlos", "Luis", "Ana", "Marta", "Silvia", "Andrés", "Pedro", "Juan", "Bautista", "Emma", "Gillermina", "Nazarena").random()
        val lastName = listOf("Díaz", "Sosa", "Rodríguez", "Fernández", "Guzmán", "López", "Pérez", "García", "Martínez", "González").random()
        val isPremium = Random.nextDouble() < 0.4
        val multipleCompanies = Random.nextDouble() < 0.7

        val companies = if (multipleCompanies) {
            listOf(generateRandomCompany(categoryName, lastName, 1))
        } else {
            emptyList()
        }

        return ProviderEntity(
            id = id,
            email = "pro_$index@maverick.com",
            displayName = "$firstName $lastName",
            name = firstName,
            lastName = lastName,
            phoneNumber = "381-${Random.nextInt(4000000, 7000000)}",
            category = categoryName,
            additionalEmails = emptyList(),
            additionalPhones = emptyList(),
            matricula = if (isPremium) "REG-${Random.nextInt(1000, 9999)}" else null,
            titulo = if (isPremium) "Técnico en $categoryName" else "Profesional $categoryName",
            photoUrl = "https://i.pravatar.cc/150?u=$id",
            bannerImageUrl = "https://picsum.photos/seed/$id/800/400",
            galleryImages = emptyList(),
            personalAddresses = emptyList(),
            companies = companies,
            hasCompanyProfile = true,
            isSubscribed = isPremium,
            isVerified = Random.nextBoolean(),
            isOnline = Random.nextBoolean(),
            isFavorite = false,
            rating = (37 + Random.nextInt(13)).toFloat() / 10f,
            favoriteProviderIds = emptyList(),
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * [NUEVO] Genera una empresa aleatoria con sucursales y empleados.
     */
    private fun generateRandomCompany(categoryName: String, ownerLastName: String, index: Int): CompanyProvider {
        val branchCount = Random.nextInt(1, 4)
        val branches = (1..branchCount).map { branchIndex ->
            generateRandomBranch(branchIndex)
        }

        return CompanyProvider(
            name = "$ownerLastName $categoryName S.A. #$index",
            razonSocial = "$ownerLastName Servicios Integrales S.R.L.",
            cuit = "20-${Random.nextInt(20000000, 35000000)}-${Random.nextInt(0, 9)}",
            services = listOf(categoryName, "Reparaciones", "Urgencias"),
            works24h = Random.nextBoolean(),
            doesHomeVisits = true,
            hasPhysicalLocation = Random.nextBoolean(),
            description = "Brindamos servicios de $categoryName con seriedad y confianza.",
            branches = branches
        )
    }

    /**
     * [NUEVO] Genera una sucursal aleatoria con empleados.
     */
    private fun generateRandomBranch(index: Int): BranchProvider {
        val employeeCount = Random.nextInt(1, 5)
        val employees = (1..employeeCount).map {
            generateRandomEmployee()
        }

        return BranchProvider(
            name = "Sucursal ${listOf("Centro", "Norte", "Sur", "Oeste").random()} #$index",
            address = AddressProvider(
                calle = listOf("Av. Aconquija", "Calle San Martín", "Av. Belgrano", "Av. Saen Peña", "Cordoba", "San Juan", "Salta").random(),
                numero = Random.nextInt(100, 3000).toString(),
                localidad = listOf("Yerba Buena", "Tafí Viejo", "San Miguel de Tucumán", "San Martín", "Alderete").random()
            ),
            employees = employees
        )
    }

    /**
     * [NUEVO] Genera un empleado aleatorio.
     */
    private fun generateRandomEmployee(): EmployeeProvider {
        val firstName = listOf("Juan", "Pedro", "Santiago", "Agustín", "Mateo").random()
        val lastName = listOf("Páez", "López", "García", "Martínez").random()
        return EmployeeProvider(
            name = firstName,
            lastName = lastName,
            position = listOf("Técnico", "Supervisor", "Atención al Cliente").random(),
            detail = "Miembro del equipo desde ${Random.nextInt(2018, 2023)}",
            photoUrl = "https://i.pravatar.cc/150?u=${firstName}_${lastName}"
        )
    }

    data class DataSeedBundle(
        val providers: List<ProviderEntity>,
        val tenders: List<TenderEntity>,
        val budgets: List<BudgetEntity>,
        val messages: List<MessageEntity>
    )
}
