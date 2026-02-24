package com.example.myapplication.data.model.fake

import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.R
import java.util.UUID

// --- NUEVOS MODELOS DE DATOS ---

// Representa una dirección física completa
data class Address(
    val id: String = UUID.randomUUID().toString(),
    var calle: String,      // Calle y altura
    var localidad: String,  // Ciudad
    var provincia: String,  // Provincia o Estado
    var pais: String,       // País
    var zipCode: String = "" // Opcional pero útil
) {
    // Retorna una cadena formateada para mostrar en UI
    fun fullString(): String = listOf(calle, localidad, provincia, pais).filter { it.isNotBlank() }.joinToString(", ")
}

// Representa un empleado asociado a una sede de la empresa
data class Employee(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var lastName: String,
    var photoUrl: Any?,
    var position: String,   // Cargo o puesto (ej. Vendedor)
    var detail: String      // Descripción breve
)

// Representa una sede (Casa Central o Sucursal) con su dirección y equipo
data class CompanyBranch(
    val id: String = UUID.randomUUID().toString(),
    var name: String, // "Casa Central", "Sucursal 1", etc.
    var address: Address,
    var employees: MutableList<Employee> = mutableListOf()
)

// Representa una empresa propiedad del usuario
data class Company(
    val id: String = UUID.randomUUID().toString(),
    var name: String,          // Nombre comercial
    var razonSocial: String,   // Razón social legal
    var cuit: String,          // Identificación tributaria
    var profileImageUrl: Any? = null, // Imagen de perfil de la empresa
    
    // Lista de sedes (La primera se asume Casa Central por convención o se etiqueta en 'name')
    var branches: MutableList<CompanyBranch> = mutableListOf(),
    
    // Lista de servicios o categorías que realiza la empresa
    var services: MutableList<String> = mutableListOf(),

    // Propiedades operativas de la empresa
    var doesHomeVisits: Boolean = false,
    var hasPhysicalLocation: Boolean = true,
    var works24h: Boolean = false,
    var acceptsAppointments: Boolean = false, // Nuevo: Da turnos en local
    
    // Descripción y Multimedia
    var description: String = "Empresa líder en el sector con años de experiencia brindando soluciones de calidad.",
    var productImages: List<String> = emptyList() // Nuevo: Album de productos
) {
    val casaCentral: Address
        get() = branches.firstOrNull()?.address ?: Address(calle = "", localidad = "", provincia = "", pais = "")
}

// Modelo de Usuario Falso (Mutable para permitir edición en tiempo de ejecución simulada)
// Ahora unificado para actuar como Cliente y Proveedor
data class UserFalso(
    val id: String,
    val username: String,
    var name: String,
    var lastName: String,
    
    // Nuevos campos profesionales
    var matricula: String? = null,
    var titulo: String? = null,
    
    // Listas para soportar múltiples contactos
    val emails: MutableList<String> = mutableListOf(),
    val phones: MutableList<String> = mutableListOf(),
    
    val profileImageUrl: Any?,
    val bannerImageUrl: Any? = null,
    
    // Lista de direcciones personales (MutableList para agregar/borrar)
    val personalAddresses: MutableList<Address> = mutableListOf(),
    
    // Configuración de modo empresa
    var hasCompanyProfile: Boolean = false, // Indica si tiene el perfil empresa habilitado
    var isSubscribed: Boolean = false, // Indica si es usuario Premium
    var isVerified: Boolean = false,   // Indica si el perfil está verificado
    var isOnline: Boolean = false,     
    var isFavorite: Boolean = false,   
    var rating: Float = 0f,            
    
    val companies: MutableList<Company> = mutableListOf(),
    val galleryImages: List<String> = emptyList(),
    
    // Compatibilidad
    val favoriteProviderIds: List<String> = emptyList()
) {
    val ciudad: String get() = personalAddresses.firstOrNull()?.localidad ?: ""
    val direccionCasa: String get() = personalAddresses.firstOrNull()?.fullString() ?: ""
    val direccionTrabajo: String get() = if (personalAddresses.size > 1) personalAddresses[1].fullString() else ""
    
    // Added for compatibility with older code accessing these directly
    val services: List<String> get() = companies.firstOrNull()?.services ?: emptyList()
    val companyName: String? get() = companies.firstOrNull()?.name
    val works24h: Boolean get() = companies.firstOrNull()?.works24h ?: false
    val doesHomeVisits: Boolean get() = companies.firstOrNull()?.doesHomeVisits ?: false
    val hasPhysicalLocation: Boolean get() = companies.firstOrNull()?.hasPhysicalLocation ?: false
}

object UserSampleDataFalso {
    
    // Usuario principal simulado (El que usa la app)
    val currentUser = UserFalso(
        id = "user1",
        username = "maxinanterne",
        name = "Maximiliano",
        lastName = "Nanterne",
        titulo = "Ingeniero de Software",
        matricula = "MP-12345",
        emails = mutableStateListOf("maxi.nanterne@example.com", "contacto@maverick.com"),
        phones = mutableStateListOf("343-1234567", "343-9998888"),
        profileImageUrl = R.drawable.maverickprofile,
        bannerImageUrl = R.drawable.myeasteregg,
        personalAddresses = mutableStateListOf(
            Address(calle = "Av. Siempre Viva 742", localidad = "Paraná", provincia = "Entre Ríos", pais = "Argentina", zipCode = "3100"),
            Address(calle = "Calle Falsa 123", localidad = "Santa Fe", provincia = "Santa Fe", pais = "Argentina", zipCode = "3000")
        ),
        hasCompanyProfile = true,
        isSubscribed = true, 
        isVerified = true,
        isOnline = true,
        rating = 5.0f,
        companies = mutableStateListOf(
            Company(
                name = "Maverick Informatica",
                razonSocial = "Maverick Tech S.A.",
                cuit = "20-12345678-9",
                profileImageUrl = null, 
                doesHomeVisits = true,
                hasPhysicalLocation = true,
                works24h = false,
                acceptsAppointments = true,
                description = "Especialistas en reparación de hardware y redes corporativas.",
                productImages = listOf("https://picsum.photos/seed/prod1/200/200", "https://picsum.photos/seed/prod2/200/200", "https://picsum.photos/seed/prod3/200/200"),
                branches = mutableListOf(
                    CompanyBranch(
                        name = "Casa Central",
                        address = Address(calle = "B. Matienzo 1339", localidad = "San Miguel de Tucumán", provincia = "Tucumán", pais = "Argentina", zipCode = "4000"),
                        employees = mutableListOf(
                            Employee(name = "Juan", lastName = "Perez", photoUrl = "https://picsum.photos/seed/emp1/100/100", position = "Técnico Senior", detail = "Especialista en Hardware")
                        )
                    ),
                    CompanyBranch(
                        name = "Sucursal Centro",
                        address = Address(calle = "Peatonal San Martín 500", localidad = "San Miguel de Tucumán", provincia = "Tucumán", pais = "Argentina", zipCode = "4000"),
                        employees = mutableListOf(
                            Employee(name = "Maria", lastName = "Gomez", photoUrl = "https://picsum.photos/seed/emp2/100/100", position = "Ventas", detail = "Atención al cliente")
                        )
                    )
                ),
                services = mutableListOf("Reparación de PC", "Redes", "Venta de Insumos")
            ),
            Company(
                name = "Maverick Developer",
                razonSocial = "Maverick Devs S.R.L.",
                cuit = "30-87654321-0",
                profileImageUrl = null,
                doesHomeVisits = false,
                hasPhysicalLocation = true,
                acceptsAppointments = true,
                description = "Desarrollo de software a medida y aplicaciones móviles.",
                productImages = listOf("https://picsum.photos/seed/dev1/200/200", "https://picsum.photos/seed/dev2/200/200"),
                branches = mutableListOf(
                    CompanyBranch(
                        name = "Oficina Principal",
                        address = Address(calle = "San Martin 100", localidad = "San Miguel de Tucumán", provincia = "Tucumán", pais = "Argentina", zipCode = "4000"),
                        employees = mutableListOf(
                            Employee(name = "Carlos", lastName = "Dev", photoUrl = "https://picsum.photos/seed/emp3/100/100", position = "Lead Dev", detail = "Full Stack"),
                            Employee(name = "Ana", lastName = "UI", photoUrl = "https://picsum.photos/seed/emp4/100/100", position = "Designer", detail = "UX/UI")
                        )
                    )
                ),
                services = mutableListOf("Desarrollo Web", "Apps Móviles")
            )
        ),
        galleryImages = listOf("https://picsum.photos/seed/gal1/400/300", "https://picsum.photos/seed/gal2/400/300"),
        favoriteProviderIds = listOf("2", "4")
    )

    fun findUserByUsername(username: String): UserFalso? {
        return if (currentUser.username == username) currentUser else null
    }
}
