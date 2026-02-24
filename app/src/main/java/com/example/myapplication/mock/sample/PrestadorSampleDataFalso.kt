package com.example.myapplication.mock.sample

import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.R
import kotlin.random.Random

object SampleDataFalso {

    val prestadores = mutableStateListOf<UserFalso>()

    // El bloque init se ejecuta una sola vez cuando la app inicia
    init {
        // Limpiamos los IDs de las categorías para evitar duplicados en cada carga
        CategorySampleDataFalso.categories.forEach {
            it.providerIds.clear()
        }

        // 1. Añadimos al prestador principal "Maverick" (ID 1)
        val maverick = UserFalso(
                id = "1",
                username = "Maxi",
                name = "Maximiliano",
                lastName = "Nanterne",
                titulo = "Ingeniero en Sistemas",
                matricula = "MP-327",
                emails = mutableStateListOf("informaticamaverick@gmail.com", "contacto@maverick.com"),
                phones = mutableStateListOf("343-1234567", "343-9998888"),
                profileImageUrl = R.drawable.maverickprofile,
                bannerImageUrl = R.drawable.myeasteregg,
                rating = 100f,
                isVerified = true,
                isOnline = true,
                isSubscribed = true,
                isFavorite = true,
                galleryImages = listOf("https://picsum.photos/seed/1_gal1/400/300", "https://picsum.photos/seed/1_gal2/400/300"),
                personalAddresses = mutableStateListOf(
                    Address(calle = "B. Matienzo 1339", localidad = "San Miguel de Tucuman ", provincia = "Tucuman", pais = "Argentina", zipCode = "4000")
                ),
                hasCompanyProfile = true,
                companies = mutableStateListOf(
                    Company(
                        name = "Maverick Informatica",
                        razonSocial = "Maverick Ingeneri S.A.",
                        cuit = "20-12345678-9",
                        profileImageUrl = R.drawable.maverickprofile,
                        doesHomeVisits = true,
                        hasPhysicalLocation = true,
                        works24h = false,
                        acceptsAppointments = true,
                        description = "Soluciones integrales en hardware y software. Reparación de PC, notebooks y configuración de redes corporativas. Venta de insumos informáticos.",
                        productImages = listOf("https://picsum.photos/seed/prod1/200/200", "https://picsum.photos/seed/prod2/200/200", "https://picsum.photos/seed/prod3/200/200"),
                        branches = mutableListOf(
                            CompanyBranch(
                                name = "Casa Central",
                                address = Address(calle = "B. Matienzo 1339", localidad = "San Miguel de Tucuman", provincia = "Tucuman", pais = "Argentina"),
                                employees = mutableListOf(
                                    Employee(name = "Juan", lastName = "Perez", photoUrl = "https://picsum.photos/seed/emp1/100/100", position = "Técnico Senior", detail = "Especialista en Hardware"),
                                    Employee(name = "Pedro", lastName = "Albornoz", photoUrl = "https://picsum.photos/seed/emp_pedro/100/100", position = "Atención al Cliente", detail = "Ventas")
                                )
                            )
                        ),
                        services = mutableListOf("Informatica", "Electricidad", "Reparación", "Camaras de Seguridad")
                    ),
                    Company(
                        name = "Maverick Developer",
                        razonSocial = "Maverick Devs S.R.L.",
                        cuit = "30-98765432-1",
                        profileImageUrl = null,
                        doesHomeVisits = true,
                        hasPhysicalLocation = true,
                        works24h = true,
                        acceptsAppointments = true,
                        description = "Desarrollo de software a medida, aplicaciones móviles y sitios web corporativos. Consultoría tecnológica.",
                        productImages = listOf("https://picsum.photos/seed/dev1/200/200", "https://picsum.photos/seed/dev2/200/200"),
                        branches = mutableListOf(
                            CompanyBranch(
                                name = "Oficina de Desarrollo",
                                address = Address(calle = "San Martin 100", localidad = "San Miguel de Tucuman", provincia = "Tucuman", pais = "Argentina", zipCode = "4000"),
                                employees = mutableListOf(
                                    Employee(name = "Carlos", lastName = "Dev", photoUrl = "https://picsum.photos/seed/dev_emp1/100/100", position = "Lead Developer", detail = "Full Stack"),
                                    Employee(name = "Ana", lastName = "UI", photoUrl = "https://picsum.photos/seed/dev_emp2/100/100", position = "Diseñadora", detail = "UX/UI")
                                )
                            )
                        ),
                        services = mutableListOf("Desarrollo App", "Páginas Web", "Consultoría IT")
                    )
                )
            )

            /**
            id = "1",
            username = "maxinanterne",
            name = "Maximiliano",
            lastName = "Nanterne",
            titulo = "Ingeniero en Sistemas",
            matricula = "MP-8899",
            emails = mutableStateListOf("informaticamaverick@gmail.com", "contacto@maverick.com"),
            phones = mutableStateListOf("343-1234567", "343-9998888"),
            profileImageUrl = R.drawable.maverickprofile,
            bannerImageUrl = R.drawable.myeasteregg,
            rating = 5.0f,
            isVerified = true, isOnline = true, isSubscribed = true, isFavorite = true,
            hasCompanyProfile = true,
            companies = mutableStateListOf(
                Company(
                    name = "Maverick Informatica",
                    razonSocial = "Maverick Tech S.A.",
                    cuit = "20-12345678-9",
                    services = mutableListOf("Informatica", "Electricidad", "Reparación", "Tecnico", "Camaras de Seguridad"),
                    // ... otros datos de la empresa ...
                ),
                Company(
                    name = "Maverick Developer",
                    razonSocial = "Maverick Devs S.R.L.",
                    cuit = "30-98765432-1",
                    services = mutableListOf("Desarrollo Web", "Programador", "Diseño"),
                    // ... otros datos de la empresa ...
                )
            )
        )
        **/
        prestadores.add(maverick)

        // Sincronizamos manualmente los servicios de Maverick con las categorías
        maverick.companies.flatMap { it.services }.forEach { serviceName ->
            CategorySampleDataFalso.categories.find { it.name == serviceName }?.let {
                if (!it.providerIds.contains(maverick.id)) {
                    it.providerIds.add(maverick.id)
                }
            }
        }

        // 2. Generamos aleatoriamente entre 6 y 19 prestadores para CADA categoría
        var idCounter = 100 // Empezamos desde un ID alto para no chocar con los manuales
        CategorySampleDataFalso.categories.forEach { category ->
            val numberToGenerate = Random.nextInt(6, 20) // Genera un número entre 6 y 19
            repeat(numberToGenerate) {
                val currentId = idCounter++.toString()
                val firstName = listOf("Juan", "Ana", "Pedro", "Maria", "Luis", "Sofia", "Carlos", "Laura", "Diego", "Elena").random()
                val lastName = listOf("Gomez", "Perez", "Rodriguez", "Fernandez", "Lopez", "Martinez", "Gonzalez", "Diaz").random()

                val newUser = UserFalso(
                    id = currentId,
                    username = "${firstName.lowercase()}$currentId",
                    name = firstName,
                    lastName = lastName,
                    titulo = "Especialista en ${category.name}",
                    rating = Random.nextDouble(3.5, 5.0).toFloat(),
                    isVerified = Random.nextBoolean(),
                    isOnline = Random.nextBoolean(),
                    isSubscribed = Random.nextBoolean(),
                    isFavorite = Random.nextBoolean(),
                    galleryImages = listOf("https://picsum.photos/seed/${currentId}_gal1/400/300"),
                    personalAddresses = mutableStateListOf(Address(calle = "Calle Falsa ${Random.nextInt(1, 1000)}", localidad = "Ciudad", provincia = "Provincia", pais = "País",zipCode = "4000")),

                    emails = mutableStateListOf("${firstName.lowercase()}.${lastName.lowercase()}@example.com"),
                    profileImageUrl = "https://picsum.photos/seed/$currentId/200/200",
                    bannerImageUrl = "https://picsum.photos/seed/${currentId}_banner/800/400",
                    hasCompanyProfile = true,
                    companies = mutableStateListOf(
                        Company(
                            name = "${category.name} $lastName",
                            razonSocial = "$lastName Servicios S.R.L.",
                            cuit = "20-${Random.nextLong(10000000, 99999999)}-${Random.nextInt(0, 9)}",
                            profileImageUrl = "https://picsum.photos/seed/${currentId}_comp/200/200",
                            doesHomeVisits = true,
                            hasPhysicalLocation = Random.nextBoolean(),
                            works24h = Random.nextBoolean(),
                            acceptsAppointments = true,
                            description = "Servicio profesional de ${category.name} garantizado. Experiencia y calidad.",
                            productImages = listOf("https://picsum.photos/seed/${currentId}_prod1/200/200"),
                            branches = mutableListOf(
                                CompanyBranch(
                                    name = "Central",
                                    address = Address(calle = "Av. Principal ${Random.nextInt(1, 500)}", localidad = "Ciudad", provincia = "Provincia", pais = "País", zipCode = "4000"),
                                    employees = mutableListOf()
                                )
                            ),
                            services = mutableListOf(category.name) // El servicio principal es la categoría
                        )
                    )
                )
                prestadores.add(newUser)

                // [LÓGICA CLAVE] Sincronizamos el ID del nuevo prestador en la categoría correcta
                category.providerIds.add(currentId)
            }
        }
    }

    // Funciones de utilidad (no cambian)
    fun toggleFavorite(id: String) {
        val index = prestadores.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = prestadores[index]
            prestadores[index] = current.copy(isFavorite = !current.isFavorite)
        }
    }

    fun getPrestadorById(id: String): UserFalso? {
        return prestadores.find { it.id == id }
    }

    fun getPrestadorUserById(id: String): UserFalso? {
        return getPrestadorById(id)
    }
}


/**

package com.example.myapplication.mock.sample

import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.R
import kotlin.random.Random

object SampleDataFalso {

    // LISTA UNIFICADA DE PRESTADORES (UserFalso)
    val prestadores = mutableStateListOf<UserFalso>().apply {
        
        // 1. Prestador Principal "Maxi" (ID 1)
        add(
            UserFalso(
                id = "1",
                username = "maxinanterne",
                name = "Maximiliano",
                lastName = "Nanterne",
                titulo = "Ingeniero en Sistemas",
                matricula = "MP-8899",
                emails = mutableStateListOf("informaticamaverick@gmail.com", "contacto@maverick.com"),
                phones = mutableStateListOf("343-1234567", "343-9998888"),
                profileImageUrl = R.drawable.maverickprofile,
                bannerImageUrl = R.drawable.myeasteregg,
                rating = 5.0f,
                isVerified = true,
                isOnline = true,
                isSubscribed = true,
                isFavorite = true,
                galleryImages = listOf("https://picsum.photos/seed/1_gal1/400/300", "https://picsum.photos/seed/1_gal2/400/300"),
                personalAddresses = mutableStateListOf(
                    Address(calle = "B. Matienzo 1339", localidad = "Paraná", provincia = "Entre Ríos", pais = "Argentina", zipCode = "3100")
                ),
                hasCompanyProfile = true,
                companies = mutableStateListOf(
                    Company(
                        name = "Maverick Informatica",
                        razonSocial = "Maverick Tech S.A.",
                        cuit = "20-12345678-9",
                        profileImageUrl = R.drawable.maverickprofile,
                        doesHomeVisits = true,
                        hasPhysicalLocation = true,
                        works24h = false,
                        acceptsAppointments = true,
                        description = "Soluciones integrales en hardware y software. Reparación de PC, notebooks y configuración de redes corporativas. Venta de insumos informáticos.",
                        productImages = listOf("https://picsum.photos/seed/prod1/200/200", "https://picsum.photos/seed/prod2/200/200", "https://picsum.photos/seed/prod3/200/200"),
                        branches = mutableListOf(
                            CompanyBranch(
                                name = "Casa Central",
                                address = Address(calle = "B. Matienzo 1339", localidad = "Paraná", provincia = "Entre Ríos", pais = "Argentina"),
                                employees = mutableListOf(
                                    Employee(name = "Juan", lastName = "Perez", photoUrl = "https://picsum.photos/seed/emp1/100/100", position = "Técnico Senior", detail = "Especialista en Hardware"),
                                    Employee(name = "Pedro", lastName = "Albornoz", photoUrl = "https://picsum.photos/seed/emp_pedro/100/100", position = "Atención al Cliente", detail = "Ventas")
                                )
                            )
                        ),
                        services = mutableListOf("Informatica", "Electricidad", "Reparación", "Camaras de Seguridad")
                    ),
                    Company(
                        name = "Maverick Developer",
                        razonSocial = "Maverick Devs S.R.L.",
                        cuit = "30-98765432-1",
                        profileImageUrl = null, 
                        doesHomeVisits = false,
                        hasPhysicalLocation = true,
                        works24h = true,
                        acceptsAppointments = true,
                        description = "Desarrollo de software a medida, aplicaciones móviles y sitios web corporativos. Consultoría tecnológica.",
                        productImages = listOf("https://picsum.photos/seed/dev1/200/200", "https://picsum.photos/seed/dev2/200/200"),
                        branches = mutableListOf(
                            CompanyBranch(
                                name = "Oficina de Desarrollo",
                                address = Address(calle = "San Martin 100", localidad = "Paraná", provincia = "Entre Ríos", pais = "Argentina"),
                                employees = mutableListOf(
                                    Employee(name = "Carlos", lastName = "Dev", photoUrl = "https://picsum.photos/seed/dev_emp1/100/100", position = "Lead Developer", detail = "Full Stack"),
                                    Employee(name = "Ana", lastName = "UI", photoUrl = "https://picsum.photos/seed/dev_emp2/100/100", position = "Diseñadora", detail = "UX/UI")
                                )
                            )
                        ),
                        services = mutableListOf("Desarrollo App", "Páginas Web", "Consultoría IT")
                    )
                )
            )
        )

        // 2. GENERACIÓN AUTOMÁTICA DE 5 PROFESIONALES POR CADA CATEGORÍA
        // Obtenemos todas las categorías únicas de CategorySampleDataFalso (asumiendo que podemos acceder a ellas o definirlas aquí)
        // Para asegurar independencia, definimos las categorías clave aquí para generar prestadores.
        val categoriesToGenerate = listOf(
            "Limpieza", "Jardinería", "Mudanzas", "Reparación", "Plomería", "Electricidad", "Carpintería", "Pintura de Casas", 
            "Diseño de Interiores", "Fumigación", "Cerrajería", "Ensamblaje de Muebles", "Tutorías", "Clases de Baile", 
            "Clases de Yoga", "Música", "Mascotas", "Belleza", "Cuidado", "Cuidado de Niños", "Cuidado de Ancianos", 
            "Entrenamiento Personal", "Nutrición", "Fisioterapia", "Psicología", "Coaching de Vida", "Peluqueria", 
            "Sastrería", "Masajes Terapéuticos", "Acupuntura", "Tecnología", "Desarrollo Web", "Reparación de Móviles", 
            "Informatica", "Tecnico", "Redes", "Programador", "Fotografía", "Eventos", "Edición de Video", "Animación", 
            "Locución", "DJ", "Bandas en Vivo", "Magia para Fiestas", "Stand-up Comedy", "Sonido para Fiestas", 
            "Iluminación para Fiestas", "Bartender", "Planificación de Bodas", "Floristería", "Consultoría", "Diseño", 
            "Traducción", "Asesoría Legal", "Contabilidad", "Marketing Digital", "Redacción", "Arquitectura", 
            "Investigador Privado", "Abogado", "Contador", "Cocina", "Catering", "Repostería", "Lavado de Autos", 
            "Mecánica", "Reparación de Bicicletas", "Seguridad", "Alarmas", "Camaras de Seguridad", "Astrología", 
            "Lectura de Tarot", "Cancha Futbol 5", "Cancha Futbol", "Cancha de Padel", "Guía Turístico", "Zapatería", 
            "Alquiler de Equipos"
        )
        
        var idCounter = 100 // IDs generados comenzarán desde 100

        categoriesToGenerate.forEach { categoryName ->
            repeat(5) { i ->
                val currentId = idCounter++.toString()
                val firstName = listOf("Juan", "Ana", "Pedro", "Maria", "Luis", "Sofia", "Carlos", "Laura", "Diego", "Elena").random()
                val lastName = listOf("Gomez", "Perez", "Rodriguez", "Fernandez", "Lopez", "Martinez", "Gonzalez", "Diaz", "Sanchez", "Romero").random()
                
                add(
                    UserFalso(
                        id = currentId,
                        username = "${firstName.lowercase()}${lastName.lowercase()}$currentId",
                        name = firstName,
                        lastName = lastName,
                        titulo = "Profesional en $categoryName",
                        matricula = if(Random.nextBoolean()) "MAT-${Random.nextInt(1000, 9999)}" else null,
                        emails = mutableStateListOf("$firstName.$lastName@example.com".lowercase()),
                        phones = mutableStateListOf("555-${Random.nextInt(100000, 999999)}"),
                        profileImageUrl = "https://picsum.photos/seed/$currentId/200/200",
                        bannerImageUrl = "https://picsum.photos/seed/${currentId}_banner/800/400",
                        rating = Random.nextDouble(3.5, 5.0).toFloat(),
                        isVerified = Random.nextBoolean(),
                        isOnline = Random.nextBoolean(),
                        isSubscribed = Random.nextBoolean(), // Para que algunos salgan en el banner de promos
                        isFavorite = false,
                        galleryImages = listOf("https://picsum.photos/seed/${currentId}_gal1/400/300"),
                        personalAddresses = mutableStateListOf(
                            Address(calle = "Calle Falsa ${Random.nextInt(1, 1000)}", localidad = "Ciudad", provincia = "Provincia", pais = "País")
                        ),
                        hasCompanyProfile = true,
                        companies = mutableStateListOf(
                            Company(
                                name = "$categoryName ${lastName}",
                                razonSocial = "$lastName Servicios S.A.",
                                cuit = "20-${Random.nextLong(10000000, 99999999)}-${Random.nextInt(0, 9)}",
                                profileImageUrl = "https://picsum.photos/seed/${currentId}_comp/200/200",
                                doesHomeVisits = true,
                                hasPhysicalLocation = Random.nextBoolean(),
                                works24h = Random.nextBoolean(),
                                acceptsAppointments = true,
                                description = "Servicio profesional de $categoryName garantizado. Experiencia y calidad.",
                                productImages = listOf("https://picsum.photos/seed/${currentId}_prod1/200/200"),
                                branches = mutableListOf(
                                    CompanyBranch(
                                        name = "Central",
                                        address = Address(calle = "Av. Principal ${Random.nextInt(1, 500)}", localidad = "Ciudad", provincia = "Provincia", pais = "País"),
                                        employees = mutableListOf()
                                    )
                                ),
                                services = mutableListOf(categoryName) // Asignar la categoría actual como servicio principal
                            )
                        )
                    )
                )
            }
        }
    }

    // Funciones de utilidad
    fun toggleFavorite(id: String) {
        val index = prestadores.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = prestadores[index]
            prestadores[index] = current.copy(isFavorite = !current.isFavorite)
        }
    }

    fun getPrestadorById(id: String): UserFalso? {
        return prestadores.find { it.id == id }
    }

    fun getPrestadorUserById(id: String): UserFalso? {
        return getPrestadorById(id)
    }
}
**/

/**
 * PrestadorSampleDataFalso: Datos de muestra de prestadores de servicios con múltiples categorías,
 * genera dinámicamente 100+ prestadores con ratings, servicios y vínculos a categorías.
 */
