package com.example.myapplication.data.model.fake

import com.example.myapplication.data.local.ProviderEntity
import com.example.myapplication.data.model.*
import kotlin.random.Random

/**
 * --- GENERADOR DE DATOS DE PRUEBA PARA PROVEEDORES ---
 * 
 * Propósito: Poblar la base de datos Room con datos realistas para pruebas.
 * 🔥 A futuro: Estos datos vendrán de Firebase Firestore para sincronizarse con Room.
 */
object PrestadorSampleDataFalso {

    /**
     * 🔥 Genera la lista completa de prestadores para la base de datos inicial.
     * Garantiza entre 5 y 20 prestadores por cada categoría existente.
     */
    fun generate(): List<ProviderEntity> {
        val providers = mutableListOf<ProviderEntity>()
        
        // 1. Agregar el Prestador Real: Maverick Informática (ID 1001)
        providers.add(generateMaverickProvider())
        
        // 2. Generar prestadores automáticos para cada categoría
        val allCategories = CategorySampleDataFalso.categories.map { it.name }
        
        allCategories.forEach { categoryName ->
            // Garantizar entre 5 y 20 prestadores por categoría
            val count = Random.nextInt(5, 21)
            repeat(count) { index ->
                providers.add(generateRandomProvider(categoryName, index))
            }
        }
        
        return providers
    }

    /**
     * Crea el prestador manual "Maverick Informática".
     * Usando la estructura real de ProviderEntity.
     */
    private fun generateMaverickProvider(): ProviderEntity {
        return ProviderEntity(
            id = "1001",
            email = "contacto@maverick.com",
            displayName = "Maverick Informática",
            name = "Maximiliano",
            lastName = "Nanterne",
            phoneNumber = "343-1234567",
            category = "Informatica", // Categoría principal
            matricula = "MP-12345",
            titulo = "Ingeniero de Software",
            photoUrl = "https://picsum.photos/seed/maverick_logo/200/200",
            bannerImageUrl = "https://picsum.photos/seed/maverick_banner/800/400",
            hasCompanyProfile = true,
            isSubscribed = true,
            isVerified = true,
            isOnline = true,
            isFavorite = false,
            rating = 5.0f,
            createdAt = System.currentTimeMillis(),
            companies = listOf(
                CompanyProvider(
                    name = "Maverick Tech S.A.",
                    razonSocial = "Maverick Informatica S.R.L.",
                    cuit = "20-12345678-9",
                    services = listOf("Informatica", "Reparación de PC", "Redes", "Desarrollo Web"),
                    works24h = false,
                    doesHomeVisits = true,
                    hasPhysicalLocation = true,
                    acceptsAppointments = true,
                    description = "Expertos en soluciones tecnológicas integrales para empresas y particulares.",
                    productImages = listOf(
                        "https://picsum.photos/seed/m1/400/300",
                        "https://picsum.photos/seed/m2/400/300"
                    ),
                    branches = listOf(
                        BranchProvider(
                            name = "Casa Central",
                            address = AddressProvider(calle = "B. Matienzo 1339", localidad = "San Miguel de Tucumán", provincia = "Tucumán", pais = "Argentina"),
                            employees = listOf(
                                EmployeeProvider(name = "Juan", lastName = "Perez", position = "Técnico Senior", detail = "Especialista en Hardware")
                            )
                        )
                    )
                )
            )
        )
    }

    /**
     * Genera un prestador aleatorio para una categoría específica.
     */
    private fun generateRandomProvider(categoryName: String, index: Int): ProviderEntity {
        val id = "auto_${categoryName}_$index"
        val firstName = listOf("Juan", "Maria", "Carlos", "Ana", "Luis", "Sofia", "Pedro", "Elena").random()
        val lastName = listOf("Gomez", "Rodriguez", "Fernandez", "Lopez", "Diaz", "Perez").random()
        val isPremium = Random.nextDouble() < 0.3 // 30% premium
        
        return ProviderEntity(
            id = id,
            email = "pro_${index}@example.com",
            displayName = "$firstName $lastName",
            name = firstName,
            lastName = lastName,
            phoneNumber = "381-${Random.nextInt(4000000, 5000000)}",
            category = categoryName,
            matricula = if (isPremium) "M-${Random.nextInt(1000, 9999)}" else null,
            titulo = categoryName,
            photoUrl = "https://i.pravatar.cc/150?u=$id",
            bannerImageUrl = "https://picsum.photos/seed/$id/800/400",
            hasCompanyProfile = true,
            isSubscribed = isPremium,
            isVerified = Random.nextBoolean(),
            isOnline = Random.nextBoolean(),
            isFavorite = false,
            rating = (35 + Random.nextInt(15)).toFloat() / 10f, // 3.5 a 5.0
            createdAt = System.currentTimeMillis(),
            companies = listOf(
                CompanyProvider(
                    name = "$lastName $categoryName",
                    razonSocial = "$lastName Servicios Integrales",
                    cuit = "20-${Random.nextInt(10000000, 30000000)}-${Random.nextInt(0, 9)}",
                    services = listOf(categoryName, "Mantenimiento", "Urgencias"),
                    works24h = Random.nextBoolean(),
                    doesHomeVisits = Random.nextBoolean(),
                    hasPhysicalLocation = Random.nextBoolean(),
                    acceptsAppointments = Random.nextBoolean(),
                    description = "Profesionales con amplia experiencia en $categoryName.",
                    productImages = listOf("https://picsum.photos/seed/${id}_work/400/300")
                )
            )
        )
    }
}
