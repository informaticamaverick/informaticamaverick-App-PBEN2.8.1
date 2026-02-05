package com.example.myapplication.data.model.fake

import androidx.compose.ui.graphics.Color

// =========================================
// MODELO DE DATOS DE CATEGORÍA
// =========================================
data class CategoryItem(
    val name: String,    val icon: String, // Emoji
    val color: Color,
    val superCategory: String,
    // [MODIFICADO] Ahora es una lista mutable para poder añadir IDs dinámicamente
    val providerIds: MutableList<String> = mutableListOf(),
    val imageUrl: String? = "https://picsum.photos/200",
    val isNew: Boolean = Math.random() < 0.2,
    val isNewPrestador: Boolean = Math.random() < 0.15,
    val isAd: Boolean = false
)

// =========================================
// BASE DE DATOS FALSA (STATIC DATA)
// =========================================
object CategorySampleDataFalso {
    // [MODIFICADO] Limpiamos los providerIds, se llenarán automáticamente
    val categories = listOf(
        // SuperCategoría: Hogar
        CategoryItem("Limpieza", "🧹", Color(0xFFFAD2E1), "Hogar"),
        CategoryItem("Jardinería", "🌿", Color(0xFFE2F0CB), "Hogar"),
        CategoryItem("Mudanzas", "🚚", Color(0xFFB5EAD7), "Hogar"),
        CategoryItem("Reparación", "🛠️", Color(0xFFFFB7B2), "Hogar"),
        CategoryItem("Plomería", "🪠", Color(0xFFD4A5A5), "Hogar"),
        CategoryItem("Electricidad", "⚡", Color(0xFFFAD2E1), "Hogar"),
        CategoryItem("Carpintería", "🪚", Color(0xFFE2F0CB), "Hogar"),
        CategoryItem("Pintura de Casas", "🏘️", Color(0xFFC7CEEA), "Hogar"),
        CategoryItem("Diseño de Interiores", "🛋️", Color(0xFFA2D2FF), "Hogar"),
        CategoryItem("Fumigación", "💨", Color(0xFFB5EAD7), "Hogar"),
        CategoryItem("Cerrajería", "🔑", Color(0xFFFFB7B2), "Hogar"),
        CategoryItem("Ensamblaje de Muebles", "🔩", Color(0xFFD4A5A5), "Hogar"),

        // SuperCategoría: Clases y Tutorías
        CategoryItem("Tutorías", "📚", Color(0xFFFFDAC1), "Clases y Tutorías"),
        CategoryItem("Clases de Baile", "💃", Color(0xFFFFDAC1), "Clases y Tutorías"),
        CategoryItem("Clases de Yoga", "🧘‍♀️", Color(0xFFC7CEEA), "Clases y Tutorías"),
        CategoryItem("Música", "🎵", Color(0xFFFAD2E1), "Clases y Tutorías"),

        // SuperCategoría: Cuidado Personal y Salud
        CategoryItem("Mascotas", "🐾", Color(0xFFC7CEEA), "Cuidado Personal y Salud"),
        CategoryItem("Belleza", "💅", Color(0xFFF1CBFF), "Cuidado Personal y Salud"),
        CategoryItem("Cuidado", "❤️‍🩹", Color(0xFFC7CEEA), "Cuidado Personal y Salud"),
        CategoryItem("Cuidado de Niños", "👶", Color(0xFFE2F0CB), "Cuidado Personal y Salud"),
        CategoryItem("Cuidado de Ancianos", "🧑‍🦳", Color(0xFFB5EAD7), "Cuidado Personal y Salud"),
        CategoryItem("Entrenamiento Personal", "💪", Color(0xFFFFB7B2), "Cuidado Personal y Salud"),
        CategoryItem("Nutrición", "🍎", Color(0xFFFFDAC1), "Cuidado Personal y Salud"),
        CategoryItem("Fisioterapia", "🏃", Color(0xFFC7CEEA), "Cuidado Personal y Salud"),
        CategoryItem("Psicología", "🧠", Color(0xFFF1CBFF), "Cuidado Personal y Salud"),
        CategoryItem("Coaching de Vida", "🧘", Color(0xFFA2D2FF), "Cuidado Personal y Salud"),
        CategoryItem("Peluqueria", "✂️", Color(0xFFF8BBD0), "Cuidado Personal y Salud"),
        CategoryItem("Sastrería", "🧵", Color(0xFFFAD2E1), "Cuidado Personal y Salud"),
        CategoryItem("Masajes Terapéuticos", "💆", Color(0xFFFFDAC1), "Cuidado Personal y Salud"),
        CategoryItem("Acupuntura", "📍", Color(0xFFC7CEEA), "Cuidado Personal y Salud"),

        // SuperCategoría: Tecnología y Reparaciones
        CategoryItem("Tecnología", "💻", Color(0xFFFFB7B2), "Tecnología y Reparaciones"),
        CategoryItem("Desarrollo Web", "👨‍💻", Color(0xFFD4F0F0), "Tecnología y Reparaciones"),
        CategoryItem("Reparación de Móviles", "📱", Color(0xFFA2D2FF), "Tecnología y Reparaciones"),
        CategoryItem("Informatica", "💻", Color(0xFFB2EBF2), "Tecnología y Reparaciones"),
        CategoryItem("Tecnico", "🛠️", Color(0xFF80DEEA), "Tecnología y Reparaciones"),
        CategoryItem("Redes", "🌐", Color(0xFF00BCD4), "Tecnología y Reparaciones"),
        CategoryItem("Programador", "👨‍💻", Color(0xFFD1C4E9), "Tecnología y Reparaciones"),

        // SuperCategoría: Eventos y Entretenimiento
        CategoryItem("Fotografía", "📷", Color(0xFFA2D2FF), "Eventos y Entretenimiento"),
        CategoryItem("Eventos", "🎉", Color(0xFFFFDAC1), "Eventos y Entretenimiento"),
        CategoryItem("Edición de Video", "🎬", Color(0xFFD1E8E2), "Eventos y Entretenimiento"),
        CategoryItem("Animación", "🎞️", Color(0xFFFFD6E0), "Eventos y Entretenimiento"),
        CategoryItem("Locución", "🎤", Color(0xFFE8D4F1), "Eventos y Entretenimiento"),
        CategoryItem("DJ", "🎧", Color(0xFFD4A5A5), "Eventos y Entretenimiento"),
        CategoryItem("Bandas en Vivo", "🎸", Color(0xFFFAD2E1), "Eventos y Entretenimiento"),
        CategoryItem("Magia para Fiestas", "✨", Color(0xFFE2F0CB), "Eventos y Entretenimiento"),
        CategoryItem("Stand-up Comedy", "😂", Color(0xFFB5EAD7), "Eventos y Entretenimiento"),
        CategoryItem("Sonido para Fiestas", "🔊", Color(0xFFE2F0CB), "Eventos y Entretenimiento"),
        CategoryItem("Iluminación para Fiestas", "💡", Color(0xFFB5EAD7), "Eventos y Entretenimiento"),
        CategoryItem("Bartender", "🍸", Color(0xFFFFB7B2), "Eventos y Entretenimiento"),
        CategoryItem("Planificación de Bodas", "💒", Color(0xFFE2F0CB), "Eventos y Entretenimiento"),
        CategoryItem("Floristería", "💐", Color(0xFFB5EAD7), "Eventos y Entretenimiento"),

        // SuperCategoría: Servicios Profesionales
        CategoryItem("Consultoría", "💼", Color(0xFFF1CBFF), "Servicios Profesionales"),
        CategoryItem("Diseño", "🎨", Color(0xFFA2D2FF), "Servicios Profesionales"),
        CategoryItem("Traducción", "🌐", Color(0xFFD4A5A5), "Servicios Profesionales"),
        CategoryItem("Asesoría Legal", "⚖️", Color(0xFFC4E0F9), "Servicios Profesionales"),
        CategoryItem("Contabilidad", "🧾", Color(0xFFFFF6E5), "Servicios Profesionales"),
        CategoryItem("Marketing Digital", "📈", Color(0xFFF9E2D2), "Servicios Profesionales"),
        CategoryItem("Redacción", "✍️", Color(0xFFE2E2E2), "Servicios Profesionales"),
        CategoryItem("Arquitectura", "🏛️", Color(0xFFF1CBFF), "Servicios Profesionales"),
        CategoryItem("Investigador Privado", "🕵️", Color(0xFFFAD2E1), "Servicios Profesionales"),
        CategoryItem("Abogado", "⚖️", Color(0xFFB2EBF2), "Servicios Profesionales"),
        CategoryItem("Contador", "🧾", Color(0xFFF0F4C3), "Servicios Profesionales"),

        // SuperCategoría: Gastronomía
        CategoryItem("Cocina", "🍳", Color(0xFFB5EAD7), "Gastronomía"),
        CategoryItem("Catering", "🍲", Color(0xFFD7F9F1), "Gastronomía"),
        CategoryItem("Repostería", "🍰", Color(0xFFFDEFD2), "Gastronomía"),

        // SuperCategoría: Vehículos
        CategoryItem("Lavado de Autos", "🚗", Color(0xFFFFB7B2), "Vehículos"),
        CategoryItem("Mecánica", "🔧", Color(0xFFFFDAC1), "Vehículos"),
        CategoryItem("Reparación de Bicicletas", "🚲", Color(0xFFD4A5A5), "Vehículos"),

        // SuperCategoría: Seguridad
        CategoryItem("Seguridad", "🛡️", Color(0xFFD4A5A5), "Seguridad"),
        CategoryItem("Alarmas", "🚨", Color(0xFF26C6DA), "Seguridad"),
        CategoryItem("Camaras de Seguridad", "📹", Color(0xFF4DD0E1), "Seguridad"),

        // SuperCategoría: Esotérico
        CategoryItem("Astrología", "✨", Color(0xFFF1CBFF), "Esotérico"),
        CategoryItem("Lectura de Tarot", "🔮", Color(0xFFA2D2FF), "Esotérico"),

        // SuperCategoría: Deportes y Recreación
        CategoryItem("Cancha Futbol 5", "⚽", Color(0xFFC8E6C9), "Deportes y Recreación"),
        CategoryItem("Cancha Futbol", "🏟️", Color(0xFFB9F6CA), "Deportes y Recreación"),
        CategoryItem("Cancha de Padel", "🎾", Color(0xFFD7CCC8), "Deportes y Recreación"),
        CategoryItem("Guía Turístico", "🗺️", Color(0xFFFFB7B2), "Deportes y Recreación"),

        // SuperCategoría: Otros
        CategoryItem("Zapatería", "👟", Color(0xFFE2F0CB), "Otros"),
        CategoryItem("Alquiler de Equipos", "🪑", Color(0xFFFAD2E1), "Otros")
    )
}


/**
package com.example.myapplication.Client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ==========================================
// MODELO DE DATOS DE CATEGORÍA
// ==========================================
data class CategoryItem(
    val name: String,
    val icon: String, // Emoji
    val color: Color,
    val superCategory: String,
    val providerIds: List<String> = emptyList(),
    val imageUrl: String? = "https://picsum.photos/200",
    
    // [NUEVO] Campos aleatorios para indicar estado "Nuevo" y "Nuevo Prestador"
    // Se generan aleatoriamente al iniciar la app (Math.random())
    val isNew: Boolean = Math.random() < 0.2, // 20% de probabilidad de ser Nuevo
    val isNewPrestador: Boolean = Math.random() < 0.15, // 15% de probabilidad de tener aviso
    
    // [NUEVO] Campo para identificar si es un anuncio
    val isAd: Boolean = false
)

// ==========================================
// BASE DE DATOS FALSA (STATIC DATA)
// ==========================================
object CategorySampleDataFalso {
    val categories = listOf(
        // SuperCategoría: Hogar
        CategoryItem("Limpieza", "🧹", Color(0xFFFAD2E1), "Hogar", providerIds = listOf("2")),
        CategoryItem("Jardinería", "🌿", Color(0xFFE2F0CB), "Hogar", providerIds = listOf("2", "13")),
        CategoryItem("Mudanzas", "🚚", Color(0xFFB5EAD7), "Hogar", providerIds = listOf("11")),
        CategoryItem("Reparación", "🛠️", Color(0xFFFFB7B2), "Hogar", providerIds = listOf("1", "3")),
        CategoryItem("Plomería", "🪠", Color(0xFFD4A5A5), "Hogar", providerIds = listOf("1")),
        CategoryItem("Electricidad", "⚡", Color(0xFFFAD2E1), "Hogar", providerIds = listOf("1")),
        CategoryItem("Carpintería", "🪚", Color(0xFFE2F0CB), "Hogar", providerIds = listOf("3")),
        CategoryItem("Pintura de Casas", "🏘️", Color(0xFFC7CEEA), "Hogar", providerIds = listOf("17")),
        CategoryItem("Diseño de Interiores", "🛋️", Color(0xFFA2D2FF), "Hogar", providerIds = listOf("13", "17")),
        CategoryItem("Fumigación", "💨", Color(0xFFB5EAD7), "Hogar", providerIds = listOf()),
        CategoryItem("Cerrajería", "🔑", Color(0xFFFFB7B2), "Hogar", providerIds = listOf("15")),
        CategoryItem("Ensamblaje de Muebles", "🔩", Color(0xFFD4A5A5), "Hogar", providerIds = listOf("3", "11")),

        // SuperCategoría: Clases y Tutorías
        CategoryItem("Tutorías", "📚", Color(0xFFFFDAC1), "Clases y Tutorías", providerIds = listOf("8")),
        CategoryItem("Clases de Baile", "💃", Color(0xFFFFDAC1), "Clases y Tutorías", providerIds = listOf("16")),
        CategoryItem("Clases de Yoga", "🧘‍♀️", Color(0xFFC7CEEA), "Clases y Tutorías", providerIds = listOf("10")),
        CategoryItem("Música", "🎵", Color(0xFFFAD2E1), "Clases y Tutorías", providerIds = listOf("16")),

        // SuperCategoría: Cuidado Personal y Salud
        CategoryItem("Mascotas", "🐾", Color(0xFFC7CEEA), "Cuidado Personal y Salud", providerIds = listOf("18")),
        CategoryItem("Belleza", "💅", Color(0xFFF1CBFF), "Cuidado Personal y Salud", providerIds = listOf("12")),
        CategoryItem("Cuidado", "❤️‍🩹", Color(0xFFC7CEEA), "Cuidado Personal y Salud", providerIds = listOf("8", "22")),
        CategoryItem("Cuidado de Niños", "👶", Color(0xFFE2F0CB), "Cuidado Personal y Salud", providerIds = listOf("8")),
        CategoryItem("Cuidado de Ancianos", "🧑‍🦳", Color(0xFFB5EAD7), "Cuidado Personal y Salud", providerIds = listOf("8")),
        CategoryItem("Entrenamiento Personal", "💪", Color(0xFFFFB7B2), "Cuidado Personal y Salud", providerIds = listOf("10")),
        CategoryItem("Nutrición", "🍎", Color(0xFFFFDAC1), "Cuidado Personal y Salud", providerIds = listOf("10")),
        CategoryItem("Fisioterapia", "🏃", Color(0xFFC7CEEA), "Cuidado Personal y Salud", providerIds = listOf("22")),
        CategoryItem("Psicología", "🧠", Color(0xFFF1CBFF), "Cuidado Personal y Salud", providerIds = listOf()),
        CategoryItem("Coaching de Vida", "🧘", Color(0xFFA2D2FF), "Cuidado Personal y Salud", providerIds = listOf()),
        CategoryItem("Peluqueria", "✂️", Color(0xFFF8BBD0), "Cuidado Personal y Salud", providerIds = listOf("12")),
        CategoryItem("Sastrería", "🧵", Color(0xFFFAD2E1), "Cuidado Personal y Salud", providerIds = listOf("12")),
        CategoryItem("Masajes Terapéuticos", "💆", Color(0xFFFFDAC1), "Cuidado Personal y Salud", providerIds = listOf("22")),
        CategoryItem("Acupuntura", "📍", Color(0xFFC7CEEA), "Cuidado Personal y Salud", providerIds = listOf("22")),

        // SuperCategoría: Tecnología y Reparaciones
        CategoryItem("Tecnología", "💻", Color(0xFFFFB7B2), "Tecnología y Reparaciones", providerIds = listOf("9", "19")),
        CategoryItem("Desarrollo Web", "👨‍💻", Color(0xFFD4F0F0), "Tecnología y Reparaciones", providerIds = listOf("9", "1")),
        CategoryItem("Reparación de Móviles", "📱", Color(0xFFA2D2FF), "Tecnología y Reparaciones", providerIds = listOf("19")),
        CategoryItem("Informatica", "💻", Color(0xFFB2EBF2), "Tecnología y Reparaciones", providerIds = listOf("", "9", "19","18","17","16","15","14","13","12","11","10","9","8","7","6","5","4","3","2","1")),
        CategoryItem("Tecnico", "🛠️", Color(0xFF80DEEA), "Tecnología y Reparaciones", providerIds = listOf("1", "19")),
        CategoryItem("Redes", "🌐", Color(0xFF00BCD4), "Tecnología y Reparaciones", providerIds = listOf("9", "19")),
        CategoryItem("Programador", "👨‍💻", Color(0xFFD1C4E9), "Tecnología y Reparaciones", providerIds = listOf("4", "9", "1")),

        // SuperCategoría: Eventos y Entretenimiento
        CategoryItem("Fotografía", "📷", Color(0xFFA2D2FF), "Eventos y Entretenimiento", providerIds = listOf("7")),
        CategoryItem("Eventos", "🎉", Color(0xFFFFDAC1), "Eventos y Entretenimiento", providerIds = listOf("21")),
        CategoryItem("Edición de Video", "🎬", Color(0xFFD1E8E2), "Eventos y Entretenimiento", providerIds = listOf("7")),
        CategoryItem("Animación", "🎞️", Color(0xFFFFD6E0), "Eventos y Entretenimiento", providerIds = listOf("7")),
        CategoryItem("Locución", "🎤", Color(0xFFE8D4F1), "Eventos y Entretenimiento", providerIds = listOf()),
        CategoryItem("DJ", "🎧", Color(0xFFD4A5A5), "Eventos y Entretenimiento", providerIds = listOf("21")),
        CategoryItem("Bandas en Vivo", "🎸", Color(0xFFFAD2E1), "Eventos y Entretenimiento", providerIds = listOf("16")),
        CategoryItem("Magia para Fiestas", "✨", Color(0xFFE2F0CB), "Eventos y Entretenimiento", providerIds = listOf()),
        CategoryItem("Stand-up Comedy", "😂", Color(0xFFB5EAD7), "Eventos y Entretenimiento", providerIds = listOf()),
        CategoryItem("Sonido para Fiestas", "🔊", Color(0xFFE2F0CB), "Eventos y Entretenimiento", providerIds = listOf("21")),
        CategoryItem("Iluminación para Fiestas", "💡", Color(0xFFB5EAD7), "Eventos y Entretenimiento", providerIds = listOf("21")),
        CategoryItem("Bartender", "🍸", Color(0xFFFFB7B2), "Eventos y Entretenimiento", providerIds = listOf("21")),
        CategoryItem("Planificación de Bodas", "💒", Color(0xFFE2F0CB), "Eventos y Entretenimiento", providerIds = listOf()),
        CategoryItem("Floristería", "💐", Color(0xFFB5EAD7), "Eventos y Entretenimiento", providerIds = listOf()),

        // SuperCategoría: Servicios Profesionales
        CategoryItem("Consultoría", "💼", Color(0xFFF1CBFF), "Servicios Profesionales", providerIds = listOf("20")),
        CategoryItem("Diseño", "🎨", Color(0xFFA2D2FF), "Servicios Profesionales", providerIds = listOf("4", "13", "17")),
        CategoryItem("Traducción", "🌐", Color(0xFFD4A5A5), "Servicios Profesionales", providerIds = listOf("20")),
        CategoryItem("Asesoría Legal", "⚖️", Color(0xFFC4E0F9), "Servicios Profesionales", providerIds = listOf("14")),
        CategoryItem("Contabilidad", "🧾", Color(0xFFFFF6E5), "Servicios Profesionales", providerIds = listOf("14")),
        CategoryItem("Marketing Digital", "📈", Color(0xFFF9E2D2), "Servicios Profesionales", providerIds = listOf("4")),
        CategoryItem("Redacción", "✍️", Color(0xFFE2E2E2), "Servicios Profesionales", providerIds = listOf("4", "20")),
        CategoryItem("Arquitectura", "🏛️", Color(0xFFF1CBFF), "Servicios Profesionales", providerIds = listOf("17")),
        CategoryItem("Investigador Privado", "🕵️", Color(0xFFFAD2E1), "Servicios Profesionales", providerIds = listOf()),
        CategoryItem("Abogado", "⚖️", Color(0xFFB2EBF2), "Servicios Profesionales", providerIds = listOf("14")),
        CategoryItem("Contador", "🧾", Color(0xFFF0F4C3), "Servicios Profesionales", providerIds = listOf("14")),
        
        // SuperCategoría: Gastronomía
        CategoryItem("Cocina", "🍳", Color(0xFFB5EAD7), "Gastronomía", providerIds = listOf("6")),
        CategoryItem("Catering", "🍲", Color(0xFFD7F9F1), "Gastronomía", providerIds = listOf("6")),
        CategoryItem("Repostería", "🍰", Color(0xFFFDEFD2), "Gastronomía", providerIds = listOf("6")),

        // SuperCategoría: Vehículos
        CategoryItem("Lavado de Autos", "🚗", Color(0xFFFFB7B2), "Vehículos", providerIds = listOf("5")),
        CategoryItem("Mecánica", "🔧", Color(0xFFFFDAC1), "Vehículos", providerIds = listOf("5")),
        CategoryItem("Reparación de Bicicletas", "🚲", Color(0xFFD4A5A5), "Vehículos", providerIds = listOf()),

        // SuperCategoría: Seguridad
        CategoryItem("Seguridad", "🛡️", Color(0xFFD4A5A5), "Seguridad", providerIds = listOf("15")),
        CategoryItem("Alarmas", "🚨", Color(0xFF26C6DA), "Seguridad", providerIds = listOf("15")),
        CategoryItem("Camaras de Seguridad", "📹", Color(0xFF4DD0E1), "Seguridad", providerIds = listOf("1","15")),

        // SuperCategoría: Esotérico
        CategoryItem("Astrología", "✨", Color(0xFFF1CBFF), "Esotérico", providerIds = listOf()),
        CategoryItem("Lectura de Tarot", "🔮", Color(0xFFA2D2FF), "Esotérico", providerIds = listOf()),

        // SuperCategoría: Deportes y Recreación
        CategoryItem("Cancha Futbol 5", "⚽", Color(0xFFC8E6C9), "Deportes y Recreación", providerIds = listOf()),
        CategoryItem("Cancha Futbol", "🏟️", Color(0xFFB9F6CA), "Deportes y Recreación", providerIds = listOf("9", "9", "9", "9", "9", "2")),
        CategoryItem("Cancha de Padel", "🎾", Color(0xFFD7CCC8), "Deportes y Recreación", providerIds = listOf()),
        CategoryItem("Guía Turístico", "🗺️", Color(0xFFFFB7B2), "Deportes y Recreación", providerIds = listOf()),

        // SuperCategoría: Otros
        CategoryItem("Zapatería", "👟", Color(0xFFE2F0CB), "Otros", providerIds = listOf()),
        CategoryItem("Alquiler de Equipos", "🪑", Color(0xFFFAD2E1), "Otros", providerIds = listOf())
    )
}
**/