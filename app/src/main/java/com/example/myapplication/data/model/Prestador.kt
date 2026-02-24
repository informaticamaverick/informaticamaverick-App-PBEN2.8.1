package com.example.myapplication.Data.Model

import androidx.compose.ui.graphics.Color

enum class SubscriptionType {
    PREMIUM,
    BASIC
}

data class Prestador(
    val id: Int,
    val name: String,
    val job: String,
    val rating: Double,
    val reviews: Int,
    val distance: Double,
    val avatarColor: Color,
    val subscription: SubscriptionType,
    val verified: Boolean = false,
    val atiendeDomicilio: Boolean = false,
    val tieneMatricula: Boolean = false
)

object PrestadoresData {
    val prestadores = listOf(
        // ========== ELECTRICISTAS ==========
        // PREMIUM
        Prestador(
            id = 1,
            name = "Carlos Ruiz",
            job = "Electricista",
            rating = 4.9,
            reviews = 120,
            distance = 1.2,
            avatarColor = Color(0xFFF59E0B),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 2,
            name = "José Electric",
            job = "Electricista",
            rating = 5.0,
            reviews = 40,
            distance = 0.5,
            avatarColor = Color(0xFFEAB308),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 9,
            name = "Miguel Voltios",
            job = "Electricista",
            rating = 4.7,
            reviews = 88,
            distance = 2.1,
            avatarColor = Color(0xFFFBBF24),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = false,
            tieneMatricula = true
        ),
        // BÁSICO
        Prestador(
            id = 6,
            name = "Pedro Cables",
            job = "Electricista",
            rating = 4.5,
            reviews = 15,
            distance = 5.0,
            avatarColor = Color(0xFFF97316),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = false,
            tieneMatricula = false
        ),
        Prestador(
            id = 10,
            name = "Ricardo Luz",
            job = "Electricista",
            rating = 4.2,
            reviews = 32,
            distance = 3.8,
            avatarColor = Color(0xFFFB923C),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        Prestador(
            id = 11,
            name = "Fernando Ampere",
            job = "Electricista",
            rating = 4.4,
            reviews = 27,
            distance = 4.5,
            avatarColor = Color(0xFFF97316),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        
        // ========== PLOMEROS ==========
        // PREMIUM
        Prestador(
            id = 3,
            name = "Mario Bross",
            job = "Plomero",
            rating = 5.0,
            reviews = 210,
            distance = 2.5,
            avatarColor = Color(0xFFEF4444),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = false,
            tieneMatricula = true
        ),
        Prestador(
            id = 12,
            name = "Alberto Tuberías",
            job = "Plomero",
            rating = 4.8,
            reviews = 145,
            distance = 1.8,
            avatarColor = Color(0xFF3B82F6),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 13,
            name = "Sandra Cañerías",
            job = "Plomero",
            rating = 4.9,
            reviews = 98,
            distance = 2.0,
            avatarColor = Color(0xFF0EA5E9),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        // BÁSICO
        Prestador(
            id = 8,
            name = "Roberto Plomería",
            job = "Plomero",
            rating = 4.6,
            reviews = 30,
            distance = 3.5,
            avatarColor = Color(0xFF3B82F6),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = false,
            tieneMatricula = true
        ),
        Prestador(
            id = 14,
            name = "Daniel Agua",
            job = "Plomero",
            rating = 4.1,
            reviews = 19,
            distance = 4.2,
            avatarColor = Color(0xFF06B6D4),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        Prestador(
            id = 15,
            name = "Patricia Llaves",
            job = "Plomero",
            rating = 4.3,
            reviews = 41,
            distance = 5.3,
            avatarColor = Color(0xFF0284C7),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = false,
            tieneMatricula = false
        ),
        
        // ========== PINTORES ==========
        // PREMIUM
        Prestador(
            id = 4,
            name = "Luisa Pintora",
            job = "Pintura",
            rating = 4.9,
            reviews = 55,
            distance = 3.2,
            avatarColor = Color(0xFFEC4899),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        Prestador(
            id = 16,
            name = "Rodrigo Colores",
            job = "Pintura",
            rating = 4.8,
            reviews = 76,
            distance = 1.5,
            avatarColor = Color(0xFFA855F7),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 17,
            name = "Elena Brocha",
            job = "Pintura",
            rating = 5.0,
            reviews = 112,
            distance = 2.2,
            avatarColor = Color(0xFFD946EF),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = false,
            tieneMatricula = true
        ),
        // BÁSICO
        Prestador(
            id = 7,
            name = "Juan Pintor",
            job = "Pintura",
            rating = 4.3,
            reviews = 22,
            distance = 4.0,
            avatarColor = Color(0xFFF43F5E),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 18,
            name = "Marta Rodillo",
            job = "Pintura",
            rating = 4.0,
            reviews = 14,
            distance = 6.1,
            avatarColor = Color(0xFFE879F9),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = false,
            tieneMatricula = false
        ),
        Prestador(
            id = 19,
            name = "Antonio Pincel",
            job = "Pintura",
            rating = 4.4,
            reviews = 38,
            distance = 3.9,
            avatarColor = Color(0xFFC026D3),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        
        // ========== LIMPIEZA ==========
        // PREMIUM
        Prestador(
            id = 20,
            name = "Carmen Pulcritud",
            job = "Limpieza",
            rating = 4.9,
            reviews = 167,
            distance = 0.9,
            avatarColor = Color(0xFF10B981),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 21,
            name = "Laura Brillante",
            job = "Limpieza",
            rating = 4.7,
            reviews = 93,
            distance = 1.3,
            avatarColor = Color(0xFF059669),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        Prestador(
            id = 22,
            name = "Sofía Limpiezas",
            job = "Limpieza",
            rating = 5.0,
            reviews = 201,
            distance = 2.4,
            avatarColor = Color(0xFF14B8A6),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = false,
            tieneMatricula = true
        ),
        // BÁSICO
        Prestador(
            id = 5,
            name = "Ana López",
            job = "Limpieza",
            rating = 4.8,
            reviews = 85,
            distance = 0.8,
            avatarColor = Color(0xFF8B5CF6),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        Prestador(
            id = 23,
            name = "Rosa Ordenada",
            job = "Limpieza",
            rating = 4.2,
            reviews = 28,
            distance = 4.7,
            avatarColor = Color(0xFF0D9488),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 24,
            name = "Beatriz Espuma",
            job = "Limpieza",
            rating = 4.5,
            reviews = 51,
            distance = 3.3,
            avatarColor = Color(0xFF06B6D4),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = false,
            tieneMatricula = false
        ),
        
        // ========== CARPINTERÍA ==========
        // PREMIUM
        Prestador(
            id = 25,
            name = "Javier Madera",
            job = "Carpintería",
            rating = 4.9,
            reviews = 134,
            distance = 1.7,
            avatarColor = Color(0xFF92400E),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = false,
            tieneMatricula = true
        ),
        Prestador(
            id = 26,
            name = "Tomás Carpintero",
            job = "Carpintería",
            rating = 4.8,
            reviews = 89,
            distance = 2.8,
            avatarColor = Color(0xFFA16207),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 27,
            name = "Gloria Muebles",
            job = "Carpintería",
            rating = 5.0,
            reviews = 156,
            distance = 3.1,
            avatarColor = Color(0xFF78350F),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        // BÁSICO
        Prestador(
            id = 28,
            name = "Diego Tablones",
            job = "Carpintería",
            rating = 4.3,
            reviews = 25,
            distance = 5.2,
            avatarColor = Color(0xFFB45309),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = false,
            tieneMatricula = true
        ),
        Prestador(
            id = 29,
            name = "Silvia Barniz",
            job = "Carpintería",
            rating = 4.1,
            reviews = 18,
            distance = 6.0,
            avatarColor = Color(0xFF92400E),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        Prestador(
            id = 30,
            name = "Héctor Sierra",
            job = "Carpintería",
            rating = 4.4,
            reviews = 33,
            distance = 4.4,
            avatarColor = Color(0xFFA16207),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = false,
            tieneMatricula = false
        ),
        
        // ========== JARDINERÍA ==========
        // PREMIUM
        Prestador(
            id = 31,
            name = "Marcos Verde",
            job = "Jardinería",
            rating = 4.8,
            reviews = 102,
            distance = 2.3,
            avatarColor = Color(0xFF16A34A),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 32,
            name = "Claudia Jardines",
            job = "Jardinería",
            rating = 4.9,
            reviews = 127,
            distance = 1.9,
            avatarColor = Color(0xFF15803D),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        Prestador(
            id = 33,
            name = "Pablo Césped",
            job = "Jardinería",
            rating = 5.0,
            reviews = 178,
            distance = 3.4,
            avatarColor = Color(0xFF22C55E),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = false,
            tieneMatricula = true
        ),
        // BÁSICO
        Prestador(
            id = 34,
            name = "Isabel Plantas",
            job = "Jardinería",
            rating = 4.2,
            reviews = 21,
            distance = 5.8,
            avatarColor = Color(0xFF16A34A),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 35,
            name = "Raúl Podador",
            job = "Jardinería",
            rating = 4.0,
            reviews = 12,
            distance = 7.2,
            avatarColor = Color(0xFF65A30D),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = false,
            tieneMatricula = false
        ),
        Prestador(
            id = 36,
            name = "Natalia Flores",
            job = "Jardinería",
            rating = 4.5,
            reviews = 44,
            distance = 4.1,
            avatarColor = Color(0xFF84CC16),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = false
        ),
        
        // ========== CERRAJERÍA ==========
        // PREMIUM
        Prestador(
            id = 37,
            name = "Gustavo Llaves",
            job = "Cerrajería",
            rating = 4.9,
            reviews = 143,
            distance = 1.1,
            avatarColor = Color(0xFF64748B),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 38,
            name = "Adriana Cerrajera",
            job = "Cerrajería",
            rating = 4.7,
            reviews = 91,
            distance = 2.6,
            avatarColor = Color(0xFF475569),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 39,
            name = "Ramiro Seguridad",
            job = "Cerrajería",
            rating = 5.0,
            reviews = 189,
            distance = 3.0,
            avatarColor = Color(0xFF334155),
            subscription = SubscriptionType.PREMIUM,
            verified = true,
            atiendeDomicilio = false,
            tieneMatricula = true
        ),
        // BÁSICO
        Prestador(
            id = 40,
            name = "Víctor Candados",
            job = "Cerrajería",
            rating = 4.4,
            reviews = 29,
            distance = 4.9,
            avatarColor = Color(0xFF71717A),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = false,
            tieneMatricula = false
        ),
        Prestador(
            id = 41,
            name = "Mónica Cerraduras",
            job = "Cerrajería",
            rating = 4.1,
            reviews = 16,
            distance = 6.5,
            avatarColor = Color(0xFF52525B),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = true
        ),
        Prestador(
            id = 42,
            name = "Ernesto Aperturas",
            job = "Cerrajería",
            rating = 4.3,
            reviews = 23,
            distance = 5.5,
            avatarColor = Color(0xFF3F3F46),
            subscription = SubscriptionType.BASIC,
            verified = false,
            atiendeDomicilio = true,
            tieneMatricula = false
        )
    )
    
    // Función para filtrar por categoría
    fun filterByCategory(category: String): List<Prestador> {
        return prestadores.filter { 
            it.job.contains(category, ignoreCase = true)
        }
    }
    
    // Función para separar por suscripción y ordenar
    fun filterBySubscription(
        prestadores: List<Prestador>, 
        type: SubscriptionType
    ): List<Prestador> {
        return prestadores.filter { it.subscription == type }
            .sortedWith(compareByDescending<Prestador> { it.rating }
                .thenBy { it.distance })
    }
}
