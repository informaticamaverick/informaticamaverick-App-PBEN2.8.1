package com.example.myapplication.data.model.fake

import androidx.compose.ui.graphics.Color

// =========================================
// 🔥 DICCIONARIO MAESTRO DE SUPERCATEGORÍAS
// =========================================
val superCategoryIconsMap = mapOf(
    "Salud y Medicina" to "⚕️",
    "Medicina Alternativa" to "🌿",
    "Hogar y Construcción" to "🏗️",
    "Tecnología y Sistemas" to "💻",
    "Deporte y Recreación" to "⚽",
    "Eventos y Entretenimiento" to "🎉",
    "Gastronomía y Bares" to "🍔",
    "Transporte y Automotor" to "🚗",
    "Servicios Profesionales" to "👨‍⚖️",
    "Marketing y Medios" to "📱",
    "Ciencias y Humanidades" to "🔬",
    "Cuidado Personal y Moda" to "💅",
    "Educación y Clases" to "📚",
    "Cuidado y Asistencia" to "🤝",
    "Mascotas y Veterinaria" to "🐾",
    "Turismo y Hotelería" to "✈️",
    "Seguridad y Emergencias" to "🚨",
    "Agricultura y Ganadería" to "🌾"
)

// =========================================
// MODELO DE DATOS DE CATEGORÍA
// =========================================
data class CategoryItem(
    val name: String,
    val icon: String, // Emoji
    val color: Color,
    val superCategory: String,

    // 🔥 MAGIA AQUÍ: Busca automáticamente el icono en el mapa, si no está, usa la carpeta.
    val superCategoryIcon: String = superCategoryIconsMap[superCategory] ?: "📂",

    val providerIds: MutableList<String> = mutableListOf(),
    val imageUrl: String? = "https://picsum.photos/400",
    val isNew: Boolean = Math.random() < 0.2,
    val isNewPrestador: Boolean = Math.random() < 0.15,
    val isAd: Boolean = false
)

// ... abajo sigue tu object CategorySampleDataFalso con las 500 categorías ...

// =========================================
// BASE DE DATOS FALSA (ESTRUCTURA MASIVA Y PROFESIONAL - +500 CATEGORÍAS)
// =========================================
object CategorySampleDataFalso {
    val categories = listOf(

        // -----------------------------------------
        // SUPERCATEGORÍA: SALUD Y MEDICINA TRADICIONAL
        // -----------------------------------------
        CategoryItem("Médico Clínico", "🩺", Color(0xFFB2EBF2), "Salud y Medicina"),
        CategoryItem("Pediatra", "👶", Color(0xFFC5CAE9), "Salud y Medicina"),
        CategoryItem("Cardiólogo", "❤️", Color(0xFFFFCDD2), "Salud y Medicina"),
        CategoryItem("Odontólogo", "🦷", Color(0xFFE1BEE7), "Salud y Medicina"),
        CategoryItem("Ortodoncista", "😁", Color(0xFFE1BEE7), "Salud y Medicina"),
        CategoryItem("Psicólogo", "🧠", Color(0xFFD1C4E9), "Salud y Medicina"),
        CategoryItem("Psiquiatra", "🛋️", Color(0xFFD7CCC8), "Salud y Medicina"),
        CategoryItem("Nutricionista", "🍏", Color(0xFFC8E6C9), "Salud y Medicina"),
        CategoryItem("Kinesiólogo", "🏃", Color(0xFFB3E5FC), "Salud y Medicina"),
        CategoryItem("Enfermero", "🩹", Color(0xFFF8BBD0), "Salud y Medicina"),
        CategoryItem("Oncólogo", "🎗️", Color(0xFFF48FB1), "Salud y Medicina"),
        CategoryItem("Fonoaudiólogo", "🗣️", Color(0xFFFFF9C4), "Salud y Medicina"),
        CategoryItem("Oftalmólogo", "👁️", Color(0xFFE0F7FA), "Salud y Medicina"),
        CategoryItem("Traumatólogo", "🦴", Color(0xFFCFD8DC), "Salud y Medicina"),
        CategoryItem("Dermatólogo", "🧴", Color(0xFFFFCCBC), "Salud y Medicina"),
        CategoryItem("Ginecólogo", "⚕️", Color(0xFFF48FB1), "Salud y Medicina"),
        CategoryItem("Obstetra", "🤰", Color(0xFFF8BBD0), "Salud y Medicina"),
        CategoryItem("Urólogo", "🔬", Color(0xFFBBDEFB), "Salud y Medicina"),
        CategoryItem("Neurólogo", "⚡", Color(0xFFB39DDB), "Salud y Medicina"),
        CategoryItem("Endocrinólogo", "🩸", Color(0xFFFFE0B2), "Salud y Medicina"),
        CategoryItem("Otorrinolaringólogo", "👂", Color(0xFFDCEDC8), "Salud y Medicina"),
        CategoryItem("Gastroenterólogo", "🩻", Color(0xFFFFE082), "Salud y Medicina"),
        CategoryItem("Podólogo", "🦶", Color(0xFFBCAAA4), "Salud y Medicina"),
        CategoryItem("Cirujano General", "😷", Color(0xFF80CBC4), "Salud y Medicina"),
        CategoryItem("Cirujano Plástico", "💉", Color(0xFFF48FB1), "Salud y Medicina"),
        CategoryItem("Alergista", "🤧", Color(0xFFFFCC80), "Salud y Medicina"),
        CategoryItem("Óptica", "👓", Color(0xFFE1F5FE), "Salud y Medicina"),
        CategoryItem("Laboratorio Clínico", "🧪", Color(0xFFF3E5F5), "Salud y Medicina"),
        CategoryItem("Farmacia", "💊", Color(0xFFC8E6C9), "Salud y Medicina"),
        CategoryItem("Radiología", "🩻", Color(0xFFCFD8DC), "Salud y Medicina"),
        CategoryItem("Ecografista", "🖥️", Color(0xFFB0BEC5), "Salud y Medicina"),
        CategoryItem("Anestesista", "😴", Color(0xFFE0E0E0), "Salud y Medicina"),
        CategoryItem("Instrumentador Quirúrgico", "🔪", Color(0xFFB2DFDB), "Salud y Medicina"),
        CategoryItem("Hemoterapeuta", "🩸", Color(0xFFFFCDD2), "Salud y Medicina"),
        CategoryItem("Geriatra", "🧓", Color(0xFFD7CCC8), "Salud y Medicina"),

        // -----------------------------------------
        // SUPERCATEGORÍA: MEDICINA ALTERNATIVA Y BIENESTAR
        // -----------------------------------------
        CategoryItem("Quiropráctico", "🦴", Color(0xFFE0F7FA), "Medicina Alternativa"),
        CategoryItem("Osteópata", "👐", Color(0xFFB2EBF2), "Medicina Alternativa"),
        CategoryItem("Acupuntura", "📍", Color(0xFFC7CEEA), "Medicina Alternativa"),
        CategoryItem("Terapeuta Reiki", "✨", Color(0xFFF1CBFF), "Medicina Alternativa"),
        CategoryItem("Homeopatía", "🌿", Color(0xFFC8E6C9), "Medicina Alternativa"),
        CategoryItem("Reflexología", "🦶", Color(0xFFFFCCBC), "Medicina Alternativa"),
        CategoryItem("Aromaterapia", "🌺", Color(0xFFE1BEE7), "Medicina Alternativa"),
        CategoryItem("Flores de Bach", "🌼", Color(0xFFF8BBD0), "Medicina Alternativa"),

        // -----------------------------------------
        // SUPERCATEGORÍA: HOGAR, CONSTRUCCIÓN Y OFICIOS
        // -----------------------------------------
        CategoryItem("Albañil", "🧱", Color(0xFFFFE0B2), "Hogar y Construcción"),
        CategoryItem("Maestro Mayor de Obra", "🏗️", Color(0xFFFFCC80), "Hogar y Construcción"),
        CategoryItem("Obrero", "👷", Color(0xFFFFB7B2), "Hogar y Construcción"),
        CategoryItem("Contratista", "📋", Color(0xFFCFD8DC), "Hogar y Construcción"),
        CategoryItem("Plomería", "🪠", Color(0xFFBCAAA4), "Hogar y Construcción"),
        CategoryItem("Electricidad", "⚡", Color(0xFFFFF59D), "Hogar y Construcción"),
        CategoryItem("Gasista", "🔥", Color(0xFFFFAB91), "Hogar y Construcción"),
        CategoryItem("Carpintería", "🪚", Color(0xFFD7CCC8), "Hogar y Construcción"),
        CategoryItem("Pintor", "🖌️", Color(0xFFC5CAE9), "Hogar y Construcción"),
        CategoryItem("Cerrajero", "🔑", Color(0xFFE0E0E0), "Hogar y Construcción"),
        CategoryItem("Techista", "🏠", Color(0xFFCFD8DC), "Hogar y Construcción"),
        CategoryItem("Herrero", "⚒️", Color(0xFFB0BEC5), "Hogar y Construcción"),
        CategoryItem("Soldador", "🔥", Color(0xFFFF8A65), "Hogar y Construcción"),
        CategoryItem("Tornero", "⚙️", Color(0xFF9E9E9E), "Hogar y Construcción"),
        CategoryItem("Vidriero", "🪟", Color(0xFFE1F5FE), "Hogar y Construcción"),
        CategoryItem("Jardinería", "🌿", Color(0xFFC8E6C9), "Hogar y Construcción"),
        CategoryItem("Paisajista", "🏞️", Color(0xFFAED581), "Hogar y Construcción"),
        CategoryItem("Aire Acondicionado", "❄️", Color(0xFFB3E5FC), "Hogar y Construcción"),
        CategoryItem("Refrigeración", "🧊", Color(0xFF81D4FA), "Hogar y Construcción"),
        CategoryItem("Limpieza", "🧹", Color(0xFFF0F4C3), "Hogar y Construcción"),
        CategoryItem("Reparación Electrodomésticos", "🔌", Color(0xFFFFCCBC), "Hogar y Construcción"),
        CategoryItem("Mantenimiento Piscinas", "🏊‍♂️", Color(0xFF81D4FA), "Hogar y Construcción"),
        CategoryItem("Yesero / Durlock", "🏢", Color(0xFFF5F5F5), "Hogar y Construcción"),
        CategoryItem("Fumigador", "💨", Color(0xFFAED581), "Hogar y Construcción"),
        CategoryItem("Desagotes", "🚜", Color(0xFF8D6E63), "Hogar y Construcción"),
        CategoryItem("Tapicero", "🛋️", Color(0xFFD1C4E9), "Hogar y Construcción"),
        CategoryItem("Mueblería a Medida", "🪑", Color(0xFFFFCC80), "Hogar y Construcción"),
        CategoryItem("Pisos y Revestimientos", "🔲", Color(0xFFCFD8DC), "Hogar y Construcción"),
        CategoryItem("Impermeabilización", "☔", Color(0xFFB2EBF2), "Hogar y Construcción"),
        CategoryItem("Zinguería", "🏗️", Color(0xFF9E9E9E), "Hogar y Construcción"),
        CategoryItem("Topógrafo", "🔭", Color(0xFFBCAAA4), "Hogar y Construcción"),
        CategoryItem("Agrimensor", "🗺️", Color(0xFFE6EE9C), "Hogar y Construcción"),
        CategoryItem("Operador de Grúa", "🏗️", Color(0xFFFFCC80), "Hogar y Construcción"),
        CategoryItem("Ascensores (Mantenimiento)", "🛗", Color(0xFFCFD8DC), "Hogar y Construcción"),
        CategoryItem("Marmolería", "🪨", Color(0xFFD7CCC8), "Hogar y Construcción"),
        CategoryItem("Instalador de Paneles Solares", "☀️", Color(0xFFFFF59D), "Hogar y Construcción"),

        // -----------------------------------------
        // SUPERCATEGORÍA: TECNOLOGÍA Y SISTEMAS
        // -----------------------------------------
        CategoryItem("Reparación de PC", "🖥️", Color(0xFF90CAF9), "Tecnología y Sistemas"),
        CategoryItem("Reparación de Celulares", "📱", Color(0xFFB3E5FC), "Tecnología y Sistemas"),
        CategoryItem("Redes de Internet", "🌐", Color(0xFF81D4FA), "Tecnología y Sistemas"),
        CategoryItem("Camaras de Seguridad", "📹", Color(0xFFB0BEC5), "Tecnología y Sistemas"),
        CategoryItem("Alarmas de Seguridad", "🚨", Color(0xFFFFCDD2), "Tecnología y Sistemas"),
        CategoryItem("Desarrollador de Software", "💻", Color(0xFFC5CAE9), "Tecnología y Sistemas"),
        CategoryItem("Programador Frontend", "🎨", Color(0xFF9FA8DA), "Tecnología y Sistemas"),
        CategoryItem("Programador Backend", "⚙️", Color(0xFF7986CB), "Tecnología y Sistemas"),
        CategoryItem("Programador Fullstack", "🛠️", Color(0xFF5C6BC0), "Tecnología y Sistemas"),
        CategoryItem("Analista de BigData", "📊", Color(0xFF80DEEA), "Tecnología y Sistemas"),
        CategoryItem("Data Scientist", "📉", Color(0xFF4DD0E1), "Tecnología y Sistemas"),
        CategoryItem("Machine Learning Engineer", "🤖", Color(0xFF26C6DA), "Tecnología y Sistemas"),
        CategoryItem("Ciberseguridad", "🛡️", Color(0xFFBCAAA4), "Tecnología y Sistemas"),
        CategoryItem("DevOps", "♾️", Color(0xFF81D4FA), "Tecnología y Sistemas"),
        CategoryItem("QA Tester", "✅", Color(0xFFA5D6A7), "Tecnología y Sistemas"),
        CategoryItem("SysAdmin", "🗄️", Color(0xFFB0BEC5), "Tecnología y Sistemas"),
        CategoryItem("Administrador de Bases de Datos", "💽", Color(0xFF90CAF9), "Tecnología y Sistemas"),
        CategoryItem("Scrum Master", "📋", Color(0xFFFFCC80), "Tecnología y Sistemas"),
        CategoryItem("Diseñador UX/UI", "📱", Color(0xFFCE93D8), "Tecnología y Sistemas"),
        CategoryItem("Domótica", "🏡", Color(0xFF80DEEA), "Tecnología y Sistemas"),
        CategoryItem("Venta de Insumos Informáticos", "🖱️", Color(0xFFBBDEFB), "Tecnología y Sistemas"),
        CategoryItem("Reparación de Consolas", "🎮", Color(0xFFD1C4E9), "Tecnología y Sistemas"),
        CategoryItem("Impresión 3D", "🧊", Color(0xFFFFF59D), "Tecnología y Sistemas"),
        CategoryItem("Reparación de Drones", "🚁", Color(0xFFCFD8DC), "Tecnología y Sistemas"),
        CategoryItem("Gammer / Coach E-Sports", "🕹️", Color(0xFFE040FB), "Tecnología y Sistemas"),

        // -----------------------------------------
        // SUPERCATEGORÍA: DEPORTE Y RECREACIÓN
        // -----------------------------------------
        CategoryItem("Cancha Fútbol 5", "⚽", Color(0xFFA5D6A7), "Deporte y Recreación"),
        CategoryItem("Cancha Fútbol 7", "🥅", Color(0xFF81C784), "Deporte y Recreación"),
        CategoryItem("Cancha Fútbol 11", "🏟️", Color(0xFF66BB6A), "Deporte y Recreación"),
        CategoryItem("Cancha de Pádel", "🎾", Color(0xFFC5E1A5), "Deporte y Recreación"),
        CategoryItem("Tenis", "🏸", Color(0xFFE6EE9C), "Deporte y Recreación"),
        CategoryItem("Golf", "⛳", Color(0xFFAED581), "Deporte y Recreación"),
        CategoryItem("Basquet", "🏀", Color(0xFFFFCC80), "Deporte y Recreación"),
        CategoryItem("Voley", "🏐", Color(0xFFFFB7B2), "Deporte y Recreación"),
        CategoryItem("Gimnasio", "🏋️", Color(0xFFE0E0E0), "Deporte y Recreación"),
        CategoryItem("Crossfit", "🤸", Color(0xFFBDBDBD), "Deporte y Recreación"),
        CategoryItem("Paintball", "🔫", Color(0xFF8D6E63), "Deporte y Recreación"),
        CategoryItem("Escuela de Baile", "💃", Color(0xFFF48FB1), "Deporte y Recreación"),
        CategoryItem("Salsa y Bachata", "🕺", Color(0xFFCE93D8), "Deporte y Recreación"),
        CategoryItem("Patinaje", "⛸️", Color(0xFF80DEEA), "Deporte y Recreación"),
        CategoryItem("Boxeo", "🥊", Color(0xFFEF9A9A), "Deporte y Recreación"),
        CategoryItem("Natación", "🏊", Color(0xFF81D4FA), "Deporte y Recreación"),
        CategoryItem("Yoga", "🧘‍♀️", Color(0xFFB2DFDB), "Deporte y Recreación"),
        CategoryItem("Artes Marciales", "🥋", Color(0xFFFFAB91), "Deporte y Recreación"),
        CategoryItem("Pilates", "🧘", Color(0xFFD1C4E9), "Deporte y Recreación"),
        CategoryItem("Entrenamiento Funcional", "⏱️", Color(0xFFFFCC80), "Deporte y Recreación"),
        CategoryItem("Ciclismo", "🚴", Color(0xFF80CBC4), "Deporte y Recreación"),
        CategoryItem("Trekking y Montañismo", "🧗", Color(0xFFA1887F), "Deporte y Recreación"),
        CategoryItem("Skatepark", "🛹", Color(0xFF90CAF9), "Deporte y Recreación"),
        CategoryItem("Surf / Kitesurf", "🏄", Color(0xFF4DD0E1), "Deporte y Recreación"),
        CategoryItem("Paracaidismo", "🪂", Color(0xFFCFD8DC), "Deporte y Recreación"),
        CategoryItem("Buceo", "🤿", Color(0xFF00BCD4), "Deporte y Recreación"),
        CategoryItem("Alquiler de Bicicletas", "🚲", Color(0xFFAED581), "Deporte y Recreación"),

        // -----------------------------------------
        // SUPERCATEGORÍA: EVENTOS, ARTE Y ENTRETENIMIENTO
        // -----------------------------------------
        CategoryItem("Salon de Eventos", "🎪", Color(0xFFE1BEE7), "Eventos y Entretenimiento"),
        CategoryItem("Salon de Fiestas", "🎊", Color(0xFFF8BBD0), "Eventos y Entretenimiento"),
        CategoryItem("Wedding Planner", "💒", Color(0xFFF8BBD0), "Eventos y Entretenimiento"),
        CategoryItem("Fotografía", "📷", Color(0xFFB3E5FC), "Eventos y Entretenimiento"),
        CategoryItem("Videografía", "🎥", Color(0xFF90CAF9), "Eventos y Entretenimiento"),
        CategoryItem("DJ", "🎧", Color(0xFFB39DDB), "Eventos y Entretenimiento"),
        CategoryItem("Tecnico en Sonido", "🎛️", Color(0xFF9E9E9E), "Eventos y Entretenimiento"),
        CategoryItem("Iluminación", "💡", Color(0xFFFFF59D), "Eventos y Entretenimiento"),
        CategoryItem("Animador Infantil", "🎈", Color(0xFFFFCC80), "Eventos y Entretenimiento"),
        CategoryItem("Comediante", "😂", Color(0xFFFFAB91), "Eventos y Entretenimiento"),
        CategoryItem("Stand Up", "🎤", Color(0xFFCE93D8), "Eventos y Entretenimiento"),
        CategoryItem("Músico", "🎵", Color(0xFFD1C4E9), "Eventos y Entretenimiento"),
        CategoryItem("Guitarrista", "🎸", Color(0xFFBCAAA4), "Eventos y Entretenimiento"),
        CategoryItem("Banda en Vivo", "🥁", Color(0xFFF48FB1), "Eventos y Entretenimiento"),
        CategoryItem("Actor", "🎭", Color(0xFFFFCCBC), "Eventos y Entretenimiento"),
        CategoryItem("Actor de Reparto", "🎬", Color(0xFFD7CCC8), "Eventos y Entretenimiento"),
        CategoryItem("Actor de Propaganda", "📺", Color(0xFFBBDEFB), "Eventos y Entretenimiento"),
        CategoryItem("Mago", "🪄", Color(0xFFB39DDB), "Eventos y Entretenimiento"),
        CategoryItem("Payaso", "🤡", Color(0xFFFFCDD2), "Eventos y Entretenimiento"),
        CategoryItem("Malabarista", "🤹", Color(0xFFFFE082), "Eventos y Entretenimiento"),
        CategoryItem("Mimo", "🤐", Color(0xFFE0E0E0), "Eventos y Entretenimiento"),
        CategoryItem("Bailarín", "🩰", Color(0xFFF8BBD0), "Eventos y Entretenimiento"),
        CategoryItem("Coreógrafo", "🕺", Color(0xFFE1BEE7), "Eventos y Entretenimiento"),
        CategoryItem("Escenógrafo", "🖼️", Color(0xFFCFD8DC), "Eventos y Entretenimiento"),
        CategoryItem("Guionista", "📝", Color(0xFFF5F5F5), "Eventos y Entretenimiento"),
        CategoryItem("Director de Cine", "🎬", Color(0xFF90CAF9), "Eventos y Entretenimiento"),
        CategoryItem("Productor", "🎥", Color(0xFFB0BEC5), "Eventos y Entretenimiento"),
        CategoryItem("Maquillador FX", "🧟", Color(0xFF8D6E63), "Eventos y Entretenimiento"),
        CategoryItem("Sonido e Iluminación", "🔊", Color(0xFFD1C4E9), "Eventos y Entretenimiento"),
        CategoryItem("Alquiler de Vajilla", "🍽️", Color(0xFFF0F4C3), "Eventos y Entretenimiento"),
        CategoryItem("Carpas y Toldos", "⛺", Color(0xFFC5CAE9), "Eventos y Entretenimiento"),
        CategoryItem("Decoración de Eventos", "🎀", Color(0xFFFFCDD2), "Eventos y Entretenimiento"),
        CategoryItem("Florista", "💐", Color(0xFFC8E6C9), "Eventos y Entretenimiento"),
        CategoryItem("Servicio de Lunch", "🥪", Color(0xFFFFE082), "Eventos y Entretenimiento"),
        CategoryItem("Barras Móviles", "🍸", Color(0xFF80DEEA), "Eventos y Entretenimiento"),
        CategoryItem("Fotocabina", "📸", Color(0xFFCFD8DC), "Eventos y Entretenimiento"),
        CategoryItem("Castillos Inflables", "🏰", Color(0xFFFFF9C4), "Eventos y Entretenimiento"),
        CategoryItem("Pista de LED", "🪩", Color(0xFFE040FB), "Eventos y Entretenimiento"),
        CategoryItem("Cotillón", "🥳", Color(0xFFFF8A65), "Eventos y Entretenimiento"),

        // -----------------------------------------
        // SUPERCATEGORÍA: GASTRONOMÍA Y BARES
        // -----------------------------------------
        CategoryItem("Restaurante", "🍽️", Color(0xFFFFAB91), "Gastronomía y Bares"),
        CategoryItem("Bar", "🍻", Color(0xFFFFCC80), "Gastronomía y Bares"),
        CategoryItem("Pub", "🍷", Color(0xFFCE93D8), "Gastronomía y Bares"),
        CategoryItem("Cervecería Artesanal", "🍺", Color(0xFFFFE082), "Gastronomía y Bares"),
        CategoryItem("Coctelería", "🍹", Color(0xFFF48FB1), "Gastronomía y Bares"),
        CategoryItem("Bodegón", "🥩", Color(0xFFBCAAA4), "Gastronomía y Bares"),
        CategoryItem("Pizzería", "🍕", Color(0xFFFFE082), "Gastronomía y Bares"),
        CategoryItem("Hamburguesería", "🍔", Color(0xFFFFCC80), "Gastronomía y Bares"),
        CategoryItem("Sushi", "🍣", Color(0xFFF8BBD0), "Gastronomía y Bares"),
        CategoryItem("Comida Vegana", "🥗", Color(0xFFA5D6A7), "Gastronomía y Bares"),
        CategoryItem("Cafetería", "☕", Color(0xFFD7CCC8), "Gastronomía y Bares"),
        CategoryItem("Pastelería", "🍰", Color(0xFFF48FB1), "Gastronomía y Bares"),
        CategoryItem("Panadería", "🥖", Color(0xFFFFE0B2), "Gastronomía y Bares"),
        CategoryItem("Food Truck", "🚐", Color(0xFF90CAF9), "Gastronomía y Bares"),
        CategoryItem("Catering", "🍱", Color(0xFFCE93D8), "Gastronomía y Bares"),
        CategoryItem("Heladería", "🍦", Color(0xFFE1BEE7), "Gastronomía y Bares"),
        CategoryItem("Vinoteca", "🍾", Color(0xFFEF9A9A), "Gastronomía y Bares"),
        CategoryItem("Fiambrería", "🧀", Color(0xFFFFE0B2), "Gastronomía y Bares"),
        CategoryItem("Rotisería", "🍗", Color(0xFFFFCCBC), "Gastronomía y Bares"),
        CategoryItem("Comida Árabe / Shawarma", "🥙", Color(0xFFD7CCC8), "Gastronomía y Bares"),
        CategoryItem("Comida Mexicana", "🌮", Color(0xFFC8E6C9), "Gastronomía y Bares"),
        CategoryItem("Comida Peruana", "🥘", Color(0xFFFFF59D), "Gastronomía y Bares"),
        CategoryItem("Chocolatería", "🍫", Color(0xFF8D6E63), "Gastronomía y Bares"),
        CategoryItem("Chef Privado", "🧑‍🍳", Color(0xFFB0BEC5), "Gastronomía y Bares"),
        CategoryItem("Barista", "☕", Color(0xFF8D6E63), "Gastronomía y Bares"),
        CategoryItem("Sommelier", "🍷", Color(0xFF9C27B0), "Gastronomía y Bares"),
        CategoryItem("Enólogo", "🍇", Color(0xFF6A1B9A), "Gastronomía y Bares"),
        CategoryItem("Parrillero", "🥩", Color(0xFFD84315), "Gastronomía y Bares"),
        CategoryItem("Maestro Pizzero", "🍕", Color(0xFFFFCA28), "Gastronomía y Bares"),
        CategoryItem("Maestro Sushero", "🔪", Color(0xFFF06292), "Gastronomía y Bares"),
        CategoryItem("Carnicero", "🥩", Color(0xFFEF5350), "Gastronomía y Bares"),
        CategoryItem("Pescadero", "🐟", Color(0xFF4FC3F7), "Gastronomía y Bares"),
        CategoryItem("Camarero", "🤵", Color(0xFFE0E0E0), "Gastronomía y Bares"),
        CategoryItem("Bartender", "🍸", Color(0xFF4DD0E1), "Gastronomía y Bares"),

        // -----------------------------------------
        // SUPERCATEGORÍA: TRANSPORTE Y AUTOMOTOR
        // -----------------------------------------
        CategoryItem("Fletes", "🚛", Color(0xFFB0BEC5), "Transporte y Automotor"),
        CategoryItem("Mudanzas", "📦", Color(0xFFCFD8DC), "Transporte y Automotor"),
        CategoryItem("Acarreos", "🚚", Color(0xFFFFCC80), "Transporte y Automotor"),
        CategoryItem("Auxilio Mecánico", "🆘", Color(0xFFEF9A9A), "Transporte y Automotor"),
        CategoryItem("Gruero", "🏗️", Color(0xFFFFB7B2), "Transporte y Automotor"),
        CategoryItem("Gomería Móvil", "🛞", Color(0xFF9E9E9E), "Transporte y Automotor"),
        CategoryItem("Taxis y Remises", "🚕", Color(0xFFFFF59D), "Transporte y Automotor"),
        CategoryItem("Chofer", "🚘", Color(0xFFE0E0E0), "Transporte y Automotor"),
        CategoryItem("Camionero", "🚛", Color(0xFF90CAF9), "Transporte y Automotor"),
        CategoryItem("Maquinista", "🚂", Color(0xFF78909C), "Transporte y Automotor"),
        CategoryItem("Piloto", "✈️", Color(0xFF81D4FA), "Transporte y Automotor"),
        CategoryItem("Capitán de Yate", "🛥️", Color(0xFF4DD0E1), "Transporte y Automotor"),
        CategoryItem("Mensajero", "✉️", Color(0xFFFFF176), "Transporte y Automotor"),
        CategoryItem("Repartidor", "🛵", Color(0xFF81D4FA), "Transporte y Automotor"),
        CategoryItem("Taller Mecánico", "⚙️", Color(0xFFBDBDBD), "Transporte y Automotor"),
        CategoryItem("Mecánico de Autos", "🚗", Color(0xFF90A4AE), "Transporte y Automotor"),
        CategoryItem("Mecánico de Motos", "🏍️", Color(0xFFFFAB91), "Transporte y Automotor"),
        CategoryItem("Mecánico de Lanchas", "🚤", Color(0xFF4FC3F7), "Transporte y Automotor"),
        CategoryItem("Tren Delantero", "🚙", Color(0xFFBCAAA4), "Transporte y Automotor"),
        CategoryItem("Alineación y Balanceo", "⚖️", Color(0xFFC5CAE9), "Transporte y Automotor"),
        CategoryItem("Especialista en Inyección", "🔌", Color(0xFFB2EBF2), "Transporte y Automotor"),
        CategoryItem("Especialista en Cajas Automáticas", "⚙️", Color(0xFFD7CCC8), "Transporte y Automotor"),
        CategoryItem("Especialista en Frenos", "🛑", Color(0xFFEF9A9A), "Transporte y Automotor"),
        CategoryItem("Lavadero de Autos", "🧽", Color(0xFF81D4FA), "Transporte y Automotor"),
        CategoryItem("Chapa y Pintura", "🚙", Color(0xFFFFCDD2), "Transporte y Automotor"),
        CategoryItem("Pintor Automotriz", "🔫", Color(0xFFCE93D8), "Transporte y Automotor"),
        CategoryItem("Detailing", "💎", Color(0xFFE0F7FA), "Transporte y Automotor"),
        CategoryItem("Electricidad del Automóvil", "🔋", Color(0xFFFFF9C4), "Transporte y Automotor"),
        CategoryItem("Polarizado", "🕶️", Color(0xFFCFD8DC), "Transporte y Automotor"),
        CategoryItem("Repuestos Autos", "🚘", Color(0xFFD7CCC8), "Transporte y Automotor"),
        CategoryItem("Repuestos Motos", "🏍️", Color(0xFFFFCCBC), "Transporte y Automotor"),
        CategoryItem("Venta de Neumáticos", "🛞", Color(0xFF90CAF9), "Transporte y Automotor"),
        CategoryItem("Alquiler de Autos", "🚗", Color(0xFFB2DFDB), "Transporte y Automotor"),
        CategoryItem("Maquinaria Pesada", "🚜", Color(0xFFFFCCBC), "Transporte y Automotor"),

        // -----------------------------------------
        // SUPERCATEGORÍA: SERVICIOS PROFESIONALES, LEGALES Y EMPRESARIALES
        // -----------------------------------------
        CategoryItem("Abogado", "⚖️", Color(0xFFCFD8DC), "Servicios Profesionales"),
        CategoryItem("Abogado Penal", "⚖️", Color(0xFFB0BEC5), "Servicios Profesionales"),
        CategoryItem("Abogado Civil", "⚖️", Color(0xFF90CAF9), "Servicios Profesionales"),
        CategoryItem("Abogado Laboral", "⚖️", Color(0xFFF48FB1), "Servicios Profesionales"),
        CategoryItem("Abogado Comercial", "⚖️", Color(0xFFFFCC80), "Servicios Profesionales"),
        CategoryItem("Contador", "🧾", Color(0xFFE0F7FA), "Servicios Profesionales"),
        CategoryItem("Escribano", "✍️", Color(0xFFF5F5F5), "Servicios Profesionales"),
        CategoryItem("Arquitecto", "📐", Color(0xFFD7CCC8), "Servicios Profesionales"),
        CategoryItem("Gestoría", "📁", Color(0xFFFFF3E0), "Servicios Profesionales"),
        CategoryItem("Auditor", "🔎", Color(0xFFB2EBF2), "Servicios Profesionales"),
        CategoryItem("Traductor", "🗣️", Color(0xFFFFF9C4), "Servicios Profesionales"),
        CategoryItem("Diseñador Gráfico", "🎨", Color(0xFFF8BBD0), "Servicios Profesionales"),
        CategoryItem("Asesor Financiero", "💹", Color(0xFFC8E6C9), "Servicios Profesionales"),
        CategoryItem("Consultor de Negocios", "💼", Color(0xFFB3E5FC), "Servicios Profesionales"),
        CategoryItem("Finanzas", "💰", Color(0xFFA5D6A7), "Servicios Profesionales"),
        CategoryItem("Analista de Riesgos", "📉", Color(0xFFEF9A9A), "Servicios Profesionales"),
        CategoryItem("Corredor de Bolsa", "📈", Color(0xFF81C784), "Servicios Profesionales"),
        CategoryItem("Actuario", "🧮", Color(0xFFD1C4E9), "Servicios Profesionales"),
        CategoryItem("Recursos Humanos", "👥", Color(0xFFD1C4E9), "Servicios Profesionales"),
        CategoryItem("Corredor de Seguros", "🛡️", Color(0xFFFFAB91), "Servicios Profesionales"),
        CategoryItem("Agente Inmobiliario", "🏢", Color(0xFFBBDEFB), "Servicios Profesionales"),
        CategoryItem("Inmobiliaria", "🏢", Color(0xFFBBDEFB), "Servicios Profesionales"),
        CategoryItem("Administrador", "📊", Color(0xFFE0E0E0), "Servicios Profesionales"),
        CategoryItem("Cajero", "💵", Color(0xFFFFF59D), "Servicios Profesionales"),
        CategoryItem("Despachante de Aduana", "🚢", Color(0xFF90CAF9), "Servicios Profesionales"),

        // -----------------------------------------
        // SUPERCATEGORÍA: MARKETING Y MEDIOS DIGITALES
        // -----------------------------------------
        CategoryItem("Marketing Digital", "📱", Color(0xFFE1BEE7), "Marketing y Medios"),
        CategoryItem("Influencer", "🤳", Color(0xFFF48FB1), "Marketing y Medios"),
        CategoryItem("Streamer", "🎮", Color(0xFFB39DDB), "Marketing y Medios"),
        CategoryItem("Youtuber", "▶️", Color(0xFFEF5350), "Marketing y Medios"),
        CategoryItem("Tiktoker", "🎵", Color(0xFF000000), "Marketing y Medios"),
        CategoryItem("Community Manager", "📱", Color(0xFFCE93D8), "Marketing y Medios"),
        CategoryItem("Copywriter", "✍️", Color(0xFFFFF9C4), "Marketing y Medios"),
        CategoryItem("SEO Specialist", "🔍", Color(0xFF81D4FA), "Marketing y Medios"),
        CategoryItem("SEM Specialist", "💰", Color(0xFFA5D6A7), "Marketing y Medios"),
        CategoryItem("Trafficker Digital", "🚦", Color(0xFFFFCC80), "Marketing y Medios"),
        CategoryItem("Relaciones Públicas", "🤝", Color(0xFFBCAAA4), "Marketing y Medios"),
        CategoryItem("Periodista", "📰", Color(0xFFCFD8DC), "Marketing y Medios"),
        CategoryItem("Locutor", "🎙️", Color(0xFFB0BEC5), "Marketing y Medios"),
        CategoryItem("Doblajista", "🗣️", Color(0xFFD7CCC8), "Marketing y Medios"),
        CategoryItem("Fotógrafo", "📸", Color(0xFFF8BBD0), "Marketing y Medios"),
        CategoryItem("Fotógrafo de Productos", "📦", Color(0xFFE0E0E0), "Marketing y Medios"),

        // -----------------------------------------
        // SUPERCATEGORÍA: CIENCIAS Y HUMANIDADES
        // -----------------------------------------
        CategoryItem("Químico", "🧪", Color(0xFFB3E5FC), "Ciencias y Humanidades"),
        CategoryItem("Biólogo", "🧬", Color(0xFFA5D6A7), "Ciencias y Humanidades"),
        CategoryItem("Físico", "⚛️", Color(0xFFD1C4E9), "Ciencias y Humanidades"),
        CategoryItem("Matemático", "📐", Color(0xFFFFF9C4), "Ciencias y Humanidades"),
        CategoryItem("Astrónomo", "🔭", Color(0xFF9FA8DA), "Ciencias y Humanidades"),
        CategoryItem("Geólogo", "🌍", Color(0xFFBCAAA4), "Ciencias y Humanidades"),
        CategoryItem("Botánico", "🪴", Color(0xFFC8E6C9), "Ciencias y Humanidades"),
        CategoryItem("Filósofo", "🤔", Color(0xFFD7CCC8), "Ciencias y Humanidades"),
        CategoryItem("Sociólogo", "📊", Color(0xFFE0E0E0), "Ciencias y Humanidades"),
        CategoryItem("Politólogo", "🏛️", Color(0xFFCFD8DC), "Ciencias y Humanidades"),
        CategoryItem("Historiador", "📜", Color(0xFFFFE0B2), "Ciencias y Humanidades"),
        CategoryItem("Arqueólogo", "🦴", Color(0xFF8D6E63), "Ciencias y Humanidades"),
        CategoryItem("Antropólogo", "🏺", Color(0xFFA1887F), "Ciencias y Humanidades"),
        CategoryItem("Lingüista", "🗣️", Color(0xFFE1BEE7), "Ciencias y Humanidades"),

        // -----------------------------------------
        // SUPERCATEGORÍA: CUIDADO PERSONAL, BELLEZA Y MODA
        // -----------------------------------------
        CategoryItem("Peluquería", "✂️", Color(0xFFF8BBD0), "Cuidado Personal y Moda"),
        CategoryItem("Barbería", "💈", Color(0xFFBCAAA4), "Cuidado Personal y Moda"),
        CategoryItem("Estilista", "💇‍♀️", Color(0xFFF48FB1), "Cuidado Personal y Moda"),
        CategoryItem("Asesor de Imagen", "👗", Color(0xFFE1BEE7), "Cuidado Personal y Moda"),
        CategoryItem("Maquillador", "💄", Color(0xFFFCE4EC), "Cuidado Personal y Moda"),
        CategoryItem("Manicurista", "💅", Color(0xFFF48FB1), "Cuidado Personal y Moda"),
        CategoryItem("Pedicuro", "🦶", Color(0xFFFFCCBC), "Cuidado Personal y Moda"),
        CategoryItem("Cosmetólogo", "🧴", Color(0xFFE1F5FE), "Cuidado Personal y Moda"),
        CategoryItem("Masajista", "💆", Color(0xFFFFE0B2), "Cuidado Personal y Moda"),
        CategoryItem("Tatuador", "🖋️", Color(0xFFCFD8DC), "Cuidado Personal y Moda"),
        CategoryItem("Piercer", "🧷", Color(0xFFB0BEC5), "Cuidado Personal y Moda"),
        CategoryItem("Depilación", "🦵", Color(0xFFE1BEE7), "Cuidado Personal y Moda"),
        CategoryItem("Spa y Relax", "🧖‍♀️", Color(0xFFB2DFDB), "Cuidado Personal y Moda"),
        CategoryItem("Estética Corporal", "✨", Color(0xFFF1CBFF), "Cuidado Personal y Moda"),
        CategoryItem("Sastre", "👔", Color(0xFFBCAAA4), "Cuidado Personal y Moda"),
        CategoryItem("Modista", "👗", Color(0xFFF8BBD0), "Cuidado Personal y Moda"),
        CategoryItem("Zapatero", "👞", Color(0xFFD7CCC8), "Cuidado Personal y Moda"),
        CategoryItem("Joyero", "💎", Color(0xFFFFF59D), "Cuidado Personal y Moda"),
        CategoryItem("Diseñador de Modas", "📐", Color(0xFFCE93D8), "Cuidado Personal y Moda"),
        CategoryItem("Arreglo de Ropa", "🧵", Color(0xFFE1BEE7), "Cuidado Personal y Moda"),
        CategoryItem("Venta de Ropa", "👕", Color(0xFFBBDEFB), "Cuidado Personal y Moda"),
        CategoryItem("Lavadero de Ropa", "🧺", Color(0xFFE1F5FE), "Cuidado Personal y Moda"),
        CategoryItem("Disfraces", "🦸", Color(0xFFD1C4E9), "Cuidado Personal y Moda"),

        // -----------------------------------------
        // SUPERCATEGORÍA: EDUCACIÓN Y CLASES
        // -----------------------------------------
        CategoryItem("Clases Particulares", "📖", Color(0xFFFFE0B2), "Educación y Clases"),
        CategoryItem("Profesor de Matemáticas", "➗", Color(0xFFC8E6C9), "Educación y Clases"),
        CategoryItem("Profesor de Física", "⚛️", Color(0xFFB3E5FC), "Educación y Clases"),
        CategoryItem("Profesor de Química", "🧪", Color(0xFFB2DFDB), "Educación y Clases"),
        CategoryItem("Profesor de Literatura", "📚", Color(0xFFD7CCC8), "Educación y Clases"),
        CategoryItem("Profesor de Historia", "📜", Color(0xFFFFCCBC), "Educación y Clases"),
        CategoryItem("Profesor de Geografía", "🗺️", Color(0xFFC5E1A5), "Educación y Clases"),
        CategoryItem("Profesor de Biología", "🧬", Color(0xFFA5D6A7), "Educación y Clases"),
        CategoryItem("Profesor de Inglés", "🇬🇧", Color(0xFFBBDEFB), "Educación y Clases"),
        CategoryItem("Profesor de Francés", "🇫🇷", Color(0xFF90CAF9), "Educación y Clases"),
        CategoryItem("Profesor de Alemán", "🇩🇪", Color(0xFFCFD8DC), "Educación y Clases"),
        CategoryItem("Profesor de Chino", "🇨🇳", Color(0xFFFFCDD2), "Educación y Clases"),
        CategoryItem("Profesor de Japonés", "🇯🇵", Color(0xFFF8BBD0), "Educación y Clases"),
        CategoryItem("Profesor de Ruso", "🇷🇺", Color(0xFFE1BEE7), "Educación y Clases"),
        CategoryItem("Profesor de Música", "🎼", Color(0xFFD1C4E9), "Educación y Clases"),
        CategoryItem("Clases de Canto", "🎤", Color(0xFFF48FB1), "Educación y Clases"),
        CategoryItem("Clases de Guitarra", "🎸", Color(0xFFFFCC80), "Educación y Clases"),
        CategoryItem("Clases de Piano", "🎹", Color(0xFFD7CCC8), "Educación y Clases"),
        CategoryItem("Clases de Batería", "🥁", Color(0xFFBCAAA4), "Educación y Clases"),
        CategoryItem("Clases de Violín", "🎻", Color(0xFFE0E0E0), "Educación y Clases"),
        CategoryItem("Taller de Arte", "🎨", Color(0xFFFFF59D), "Educación y Clases"),
        CategoryItem("Clases de Dibujo", "✏️", Color(0xFFE1F5FE), "Educación y Clases"),
        CategoryItem("Apoyo Escolar", "🎒", Color(0xFFB2EBF2), "Educación y Clases"),
        CategoryItem("Clases de Cocina", "🧑‍🍳", Color(0xFFFFAB91), "Educación y Clases"),
        CategoryItem("Clases de Programación", "💻", Color(0xFFCFD8DC), "Educación y Clases"),
        CategoryItem("Cursos de Fotografía", "📸", Color(0xFFB3E5FC), "Educación y Clases"),
        CategoryItem("Autoescuela (Manejo)", "🚗", Color(0xFFB0BEC5), "Educación y Clases"),
        CategoryItem("Educación Especial", "🧩", Color(0xFFE0F7FA), "Educación y Clases"),

        // -----------------------------------------
        // SUPERCATEGORÍA: CUIDADO DE NIÑOS Y ADULTOS
        // -----------------------------------------
        CategoryItem("Niñera", "👶", Color(0xFFF8BBD0), "Cuidado y Asistencia"),
        CategoryItem("Empleada Doméstica", "🧹", Color(0xFFE0F7FA), "Cuidado y Asistencia"),
        CategoryItem("Cuidador de Ancianos", "🧓", Color(0xFFD7CCC8), "Cuidado y Asistencia"),
        CategoryItem("Acompañante Terapéutico", "🤝", Color(0xFFC8E6C9), "Cuidado y Asistencia"),

        // -----------------------------------------
        // SUPERCATEGORÍA: MASCOTAS Y VETERINARIA
        // -----------------------------------------
        CategoryItem("Veterinaria", "🐶", Color(0xFFC8E6C9), "Mascotas y Veterinaria"),
        CategoryItem("Peluquería Canina", "🐩", Color(0xFFAED581), "Mascotas y Veterinaria"),
        CategoryItem("Paseador de Perros", "🦮", Color(0xFFDCEDC8), "Mascotas y Veterinaria"),
        CategoryItem("Guardería de Mascotas", "🏡", Color(0xFFFFF9C4), "Mascotas y Veterinaria"),
        CategoryItem("Adiestrador", "🐕‍🦺", Color(0xFFFFE0B2), "Mascotas y Veterinaria"),
        CategoryItem("Etólogo Canino", "🧠", Color(0xFFD1C4E9), "Mascotas y Veterinaria"),
        CategoryItem("Pet Shop", "🦴", Color(0xFFFFCCBC), "Mascotas y Veterinaria"),
        CategoryItem("Acuario", "🐠", Color(0xFF81D4FA), "Mascotas y Veterinaria"),
        CategoryItem("Acuarista", "🐟", Color(0xFF4DD0E1), "Mascotas y Veterinaria"),
        CategoryItem("Veterinaria 24hs", "🚑", Color(0xFFFFCDD2), "Mascotas y Veterinaria"),
        CategoryItem("Crematorio de Mascotas", "🕊️", Color(0xFFCFD8DC), "Mascotas y Veterinaria"),

        // -----------------------------------------
        // SUPERCATEGORÍA: TURISMO Y HOTELERÍA
        // -----------------------------------------
        CategoryItem("Guía Turístico", "🗺️", Color(0xFFFFCC80), "Turismo y Hotelería"),
        CategoryItem("Agencia de Viajes", "✈️", Color(0xFF81D4FA), "Turismo y Hotelería"),
        CategoryItem("Hotel", "🏨", Color(0xFFB39DDB), "Turismo y Hotelería"),
        CategoryItem("Hostel", "🛏️", Color(0xFFD7CCC8), "Turismo y Hotelería"),
        CategoryItem("Cabañas", "🏕️", Color(0xFFA5D6A7), "Turismo y Hotelería"),
        CategoryItem("Traductor Turístico", "🗣️", Color(0xFFFFF59D), "Turismo y Hotelería"),

        // -----------------------------------------
        // SUPERCATEGORÍA: SEGURIDAD Y EMERGENCIAS
        // -----------------------------------------
        CategoryItem("Seguridad Privada", "🛡️", Color(0xFF9E9E9E), "Seguridad y Emergencias"),
        CategoryItem("Seguridad de Eventos", "💂", Color(0xFFB0BEC5), "Seguridad y Emergencias"),
        CategoryItem("Bombero", "🚒", Color(0xFFEF5350), "Seguridad y Emergencias"),
        CategoryItem("Paramédico", "🚑", Color(0xFFFFCDD2), "Seguridad y Emergencias"),
        CategoryItem("Instalador de Alarmas", "🚨", Color(0xFFFF8A65), "Seguridad y Emergencias"),
        CategoryItem("Guardaespaldas", "🕴️", Color(0xFF78909C), "Seguridad y Emergencias"),
        CategoryItem("Valet Parking", "🚘", Color(0xFFCFD8DC), "Seguridad y Emergencias"),

        // -----------------------------------------
        // SUPERCATEGORÍA: AGRICULTURA Y GANADERÍA
        // -----------------------------------------
        CategoryItem("Ingeniero Agrónomo", "🌾", Color(0xFFAED581), "Agricultura y Ganadería"),
        CategoryItem("Veterinario Rural", "🐄", Color(0xFFC5E1A5), "Agricultura y Ganadería"),
        CategoryItem("Operador de Tractor", "🚜", Color(0xFFFFCC80), "Agricultura y Ganadería"),
        CategoryItem("Apicultor", "🐝", Color(0xFFFFF59D), "Agricultura y Ganadería"),
        CategoryItem("Vivero", "🪴", Color(0xFF81C784), "Agricultura y Ganadería")
    )
}




/**
import androidx.compose.ui.graphics.Color

// =========================================
// MODELO DE DATOS DE CATEGORÍA
// =========================================
data class CategoryItem(
    val name: String,
    val icon: String, // Emoji
    val color: Color,
    val superCategory: String,
    // Lista mutable para poder añadir IDs dinámicamente desde el generador de Prestadores
    val providerIds: MutableList<String> = mutableListOf(),
    val imageUrl: String? = "https://picsum.photos/400",
    val isNew: Boolean = Math.random() < 0.2,
    val isNewPrestador: Boolean = Math.random() < 0.15,
    val isAd: Boolean = false
)

// =========================================
// BASE DE DATOS FALSA (ESTRUCTURA MASIVA Y PROFESIONAL)
// =========================================
object CategorySampleDataFalso {
    val categories = listOf(

        // -----------------------------------------
        // SUPERCATEGORÍA: SALUD Y MEDICINA
        // -----------------------------------------
        CategoryItem("Médico Clínico", "🩺", Color(0xFFB2EBF2), "Salud y Medicina"),
        CategoryItem("Pediatra", "👶", Color(0xFFC5CAE9), "Salud y Medicina"),
        CategoryItem("Cardiólogo", "❤️", Color(0xFFFFCDD2), "Salud y Medicina"),
        CategoryItem("Odontólogo", "🦷", Color(0xFFE1BEE7), "Salud y Medicina"),
        CategoryItem("Psicólogo", "🧠", Color(0xFFD1C4E9), "Salud y Medicina"),
        CategoryItem("Nutricionista", "🍏", Color(0xFFC8E6C9), "Salud y Medicina"),
        CategoryItem("Kinesiólogo", "🏃", Color(0xFFB3E5FC), "Salud y Medicina"),
        CategoryItem("Enfermero", "🩹", Color(0xFFF8BBD0), "Salud y Medicina"),
        CategoryItem("Oncólogo", "🎗️", Color(0xFFF48FB1), "Salud y Medicina"),
        CategoryItem("Fonoaudiólogo", "🗣️", Color(0xFFFFF9C4), "Salud y Medicina"),
        CategoryItem("Oftalmólogo", "👁️", Color(0xFFE0F7FA), "Salud y Medicina"),
        CategoryItem("Psiquiatra", "🛋️", Color(0xFFD7CCC8), "Salud y Medicina"),
        CategoryItem("Traumatólogo", "🦴", Color(0xFFCFD8DC), "Salud y Medicina"),
        CategoryItem("Dermatólogo", "🧴", Color(0xFFFFCCBC), "Salud y Medicina"),
        CategoryItem("Ginecólogo", "⚕️", Color(0xFFF48FB1), "Salud y Medicina"),
        CategoryItem("Urólogo", "🔬", Color(0xFFBBDEFB), "Salud y Medicina"),
        CategoryItem("Neurólogo", "⚡", Color(0xFFB39DDB), "Salud y Medicina"),
        CategoryItem("Endocrinólogo", "🩸", Color(0xFFFFE0B2), "Salud y Medicina"),
        CategoryItem("Otorrinolaringólogo", "👂", Color(0xFFDCEDC8), "Salud y Medicina"),
        CategoryItem("Gastroenterólogo", "🩻", Color(0xFFFFE082), "Salud y Medicina"),
        CategoryItem("Podólogo", "🦶", Color(0xFFBCAAA4), "Salud y Medicina"),
        CategoryItem("Cirujano General", "😷", Color(0xFF80CBC4), "Salud y Medicina"),
        CategoryItem("Alergista", "🤧", Color(0xFFFFCC80), "Salud y Medicina"),
        CategoryItem("Óptica", "👓", Color(0xFFE1F5FE), "Salud y Medicina"),
        CategoryItem("Laboratorio Clínico", "🧪", Color(0xFFF3E5F5), "Salud y Medicina"),
        CategoryItem("Farmacia", "💊", Color(0xFFC8E6C9), "Salud y Medicina"),

        // -----------------------------------------
        // SUPERCATEGORÍA: HOGAR Y CONSTRUCCIÓN
        // -----------------------------------------
        CategoryItem("Albañil", "🧱", Color(0xFFFFE0B2), "Hogar y Construcción"),
        CategoryItem("Maestro Mayor de Obra", "🏗️", Color(0xFFFFCC80), "Hogar y Construcción"),
        CategoryItem("Plomería", "🪠", Color(0xFFBCAAA4), "Hogar y Construcción"),
        CategoryItem("Electricidad", "⚡", Color(0xFFFFF59D), "Hogar y Construcción"),
        CategoryItem("Gasista", "🔥", Color(0xFFFFAB91), "Hogar y Construcción"),
        CategoryItem("Carpintería", "🪚", Color(0xFFD7CCC8), "Hogar y Construcción"),
        CategoryItem("Pintor", "🖌️", Color(0xFFC5CAE9), "Hogar y Construcción"),
        CategoryItem("Cerrajero", "🔑", Color(0xFFE0E0E0), "Hogar y Construcción"),
        CategoryItem("Techista", "🏠", Color(0xFFCFD8DC), "Hogar y Construcción"),
        CategoryItem("Herrero", "⚒️", Color(0xFFB0BEC5), "Hogar y Construcción"),
        CategoryItem("Vidriero", "🪟", Color(0xFFE1F5FE), "Hogar y Construcción"),
        CategoryItem("Jardinería", "🌿", Color(0xFFC8E6C9), "Hogar y Construcción"),
        CategoryItem("Aire Acondicionado", "❄️", Color(0xFFB3E5FC), "Hogar y Construcción"),
        CategoryItem("Limpieza", "🧹", Color(0xFFF0F4C3), "Hogar y Construcción"),
        CategoryItem("Reparación Electrodomésticos", "🔌", Color(0xFFFFCCBC), "Hogar y Construcción"),
        CategoryItem("Mantenimiento Piscinas", "🏊‍♂️", Color(0xFF81D4FA), "Hogar y Construcción"),
        CategoryItem("Ingeniero Civil", "📐", Color(0xFFBBDEFB), "Hogar y Construcción"),
        CategoryItem("Yesero / Durlock", "🏢", Color(0xFFF5F5F5), "Hogar y Construcción"),
        CategoryItem("Fumigación y Control", "🐜", Color(0xFFAED581), "Hogar y Construcción"),
        CategoryItem("Desagotes", "🚜", Color(0xFF8D6E63), "Hogar y Construcción"),
        CategoryItem("Tapicero", "🛋️", Color(0xFFD1C4E9), "Hogar y Construcción"),
        CategoryItem("Mueblería a Medida", "🪑", Color(0xFFFFCC80), "Hogar y Construcción"),
        CategoryItem("Pisos y Revestimientos", "🔲", Color(0xFFCFD8DC), "Hogar y Construcción"),
        CategoryItem("Impermeabilización", "☔", Color(0xFFB2EBF2), "Hogar y Construcción"),
        CategoryItem("Zinguería", "🏗️", Color(0xFF9E9E9E), "Hogar y Construcción"),
        CategoryItem("Instalador de Alarmas", "🚨", Color(0xFFFFCDD2), "Hogar y Construcción"),

        // -----------------------------------------
        // SUPERCATEGORÍA: DEPORTE Y RECREACIÓN
        // -----------------------------------------
        CategoryItem("Cancha Fútbol 5", "⚽", Color(0xFFA5D6A7), "Deporte y Recreación"),
        CategoryItem("Cancha Fútbol 7", "🥅", Color(0xFF81C784), "Deporte y Recreación"),
        CategoryItem("Cancha Fútbol 11", "🏟️", Color(0xFF66BB6A), "Deporte y Recreación"),
        CategoryItem("Cancha de Pádel", "🎾", Color(0xFFC5E1A5), "Deporte y Recreación"),
        CategoryItem("Tenis", "🏸", Color(0xFFE6EE9C), "Deporte y Recreación"),
        CategoryItem("Golf", "⛳", Color(0xFFAED581), "Deporte y Recreación"),
        CategoryItem("Gimnasio", "🏋️", Color(0xFFE0E0E0), "Deporte y Recreación"),
        CategoryItem("Crossfit", "🤸", Color(0xFFBDBDBD), "Deporte y Recreación"),
        CategoryItem("Paintball", "🔫", Color(0xFF8D6E63), "Deporte y Recreación"),
        CategoryItem("Escuela de Baile", "💃", Color(0xFFF48FB1), "Deporte y Recreación"),
        CategoryItem("Salsa y Bachata", "🕺", Color(0xFFCE93D8), "Deporte y Recreación"),
        CategoryItem("Patinaje", "⛸️", Color(0xFF80DEEA), "Deporte y Recreación"),
        CategoryItem("Boxeo", "🥊", Color(0xFFEF9A9A), "Deporte y Recreación"),
        CategoryItem("Natación", "🏊", Color(0xFF81D4FA), "Deporte y Recreación"),
        CategoryItem("Yoga", "🧘‍♀️", Color(0xFFB2DFDB), "Deporte y Recreación"),
        CategoryItem("Artes Marciales", "🥋", Color(0xFFFFAB91), "Deporte y Recreación"),
        CategoryItem("Pilates", "🧘", Color(0xFFD1C4E9), "Deporte y Recreación"),
        CategoryItem("Entrenamiento Funcional", "⏱️", Color(0xFFFFCC80), "Deporte y Recreación"),
        CategoryItem("Ciclismo", "🚴", Color(0xFF80CBC4), "Deporte y Recreación"),
        CategoryItem("Trekking y Montañismo", "🧗", Color(0xFFA1887F), "Deporte y Recreación"),
        CategoryItem("Skatepark", "🛹", Color(0xFF90CAF9), "Deporte y Recreación"),

        // -----------------------------------------
        // SUPERCATEGORÍA: GASTRONOMÍA Y BARES
        // -----------------------------------------
        CategoryItem("Restaurante", "🍽️", Color(0xFFFFAB91), "Gastronomía y Bares"),
        CategoryItem("Bar", "🍻", Color(0xFFFFCC80), "Gastronomía y Bares"),
        CategoryItem("Pub", "🍷", Color(0xFFCE93D8), "Gastronomía y Bares"),
        CategoryItem("Cervecería Artesanal", "🍺", Color(0xFFFFE082), "Gastronomía y Bares"),
        CategoryItem("Coctelería", "🍹", Color(0xFFF48FB1), "Gastronomía y Bares"),
        CategoryItem("Bodegón", "🥩", Color(0xFFBCAAA4), "Gastronomía y Bares"),
        CategoryItem("Pizzería", "🍕", Color(0xFFFFE082), "Gastronomía y Bares"),
        CategoryItem("Hamburguesería", "🍔", Color(0xFFFFCC80), "Gastronomía y Bares"),
        CategoryItem("Sushi", "🍣", Color(0xFFF8BBD0), "Gastronomía y Bares"),
        CategoryItem("Comida Vegana", "🥗", Color(0xFFA5D6A7), "Gastronomía y Bares"),
        CategoryItem("Cafetería", "☕", Color(0xFFD7CCC8), "Gastronomía y Bares"),
        CategoryItem("Pastelería", "🍰", Color(0xFFF48FB1), "Gastronomía y Bares"),
        CategoryItem("Food Truck", "🚐", Color(0xFF90CAF9), "Gastronomía y Bares"),
        CategoryItem("Catering", "🍱", Color(0xFFCE93D8), "Gastronomía y Bares"),
        CategoryItem("Heladería", "🍦", Color(0xFFE1BEE7), "Gastronomía y Bares"),
        CategoryItem("Vinoteca", "🍾", Color(0xFFEF9A9A), "Gastronomía y Bares"),
        CategoryItem("Fiambrería", "🧀", Color(0xFFFFE0B2), "Gastronomía y Bares"),
        CategoryItem("Rotisería", "🍗", Color(0xFFFFCCBC), "Gastronomía y Bares"),
        CategoryItem("Shawarma", "🥙", Color(0xFFD7CCC8), "Gastronomía y Bares"),
        CategoryItem("Comida Árabe", "🧆", Color(0xFFBCAAA4), "Gastronomía y Bares"),
        CategoryItem("Comida Mexicana", "🌮", Color(0xFFC8E6C9), "Gastronomía y Bares"),
        CategoryItem("Comida Peruana", "🥘", Color(0xFFFFF59D), "Gastronomía y Bares"),
        CategoryItem("Chocolatería", "🍫", Color(0xFF8D6E63), "Gastronomía y Bares"),

        // -----------------------------------------
        // SUPERCATEGORÍA: EVENTOS Y FIESTAS
        // -----------------------------------------
        CategoryItem("Salón de Eventos", "🎪", Color(0xFFE1BEE7), "Eventos y Fiestas"),
        CategoryItem("Salón de Fiestas", "🎊", Color(0xFFF8BBD0), "Eventos y Fiestas"),
        CategoryItem("Fotografía", "📷", Color(0xFFB3E5FC), "Eventos y Fiestas"),
        CategoryItem("Videografía", "🎥", Color(0xFF90CAF9), "Eventos y Fiestas"),
        CategoryItem("DJ", "🎧", Color(0xFFB39DDB), "Eventos y Fiestas"),
        CategoryItem("Animador Infantil", "🎈", Color(0xFFFFCC80), "Eventos y Fiestas"),
        CategoryItem("Banda en Vivo", "🎸", Color(0xFFF48FB1), "Eventos y Fiestas"),
        CategoryItem("Wedding Planner", "💒", Color(0xFFF8BBD0), "Eventos y Fiestas"),
        CategoryItem("Show de Magia", "✨", Color(0xFFFFF59D), "Eventos y Fiestas"),
        CategoryItem("Sonido e Iluminación", "🔊", Color(0xFFD1C4E9), "Eventos y Fiestas"),
        CategoryItem("Alquiler de Vajilla", "🍽️", Color(0xFFF0F4C3), "Eventos y Fiestas"),
        CategoryItem("Carpas y Toldos", "⛺", Color(0xFFC5CAE9), "Eventos y Fiestas"),
        CategoryItem("Decoración de Eventos", "🎀", Color(0xFFFFCDD2), "Eventos y Fiestas"),
        CategoryItem("Servicio de Lunch", "🥪", Color(0xFFFFE082), "Eventos y Fiestas"),
        CategoryItem("Barras Móviles", "🍸", Color(0xFF80DEEA), "Eventos y Fiestas"),
        CategoryItem("Fotocabina", "📸", Color(0xFFCFD8DC), "Eventos y Fiestas"),
        CategoryItem("Castillos Inflables", "🏰", Color(0xFFFFF9C4), "Eventos y Fiestas"),
        CategoryItem("Pista de LED", "🪩", Color(0xFFE040FB), "Eventos y Fiestas"),
        CategoryItem("Cotillón", "🥳", Color(0xFFFF8A65), "Eventos y Fiestas"),

        // -----------------------------------------
        // SUPERCATEGORÍA: TRANSPORTE Y AUTOMOTOR
        // -----------------------------------------
        CategoryItem("Fletes", "🚛", Color(0xFFB0BEC5), "Transporte y Automotor"),
        CategoryItem("Mudanzas", "📦", Color(0xFFCFD8DC), "Transporte y Automotor"),
        CategoryItem("Acarreos", "🚚", Color(0xFFFFCC80), "Transporte y Automotor"),
        CategoryItem("Auxilio Mecánico", "🆘", Color(0xFFEF9A9A), "Transporte y Automotor"),
        CategoryItem("Gomería Móvil", "🛞", Color(0xFF9E9E9E), "Transporte y Automotor"),
        CategoryItem("Taxis y Remises", "🚕", Color(0xFFFFF59D), "Transporte y Automotor"),
        CategoryItem("Chofer Privado", "🕴️", Color(0xFFE0E0E0), "Transporte y Automotor"),
        CategoryItem("Mensajería y Envíos", "🛵", Color(0xFF81D4FA), "Transporte y Automotor"),
        CategoryItem("Taller Mecánico", "⚙️", Color(0xFFBDBDBD), "Transporte y Automotor"),
        CategoryItem("Lavadero de Autos", "🧽", Color(0xFF81D4FA), "Transporte y Automotor"),
        CategoryItem("Chapa y Pintura", "🚙", Color(0xFFFFCDD2), "Transporte y Automotor"),
        CategoryItem("Detailing", "💎", Color(0xFFE0F7FA), "Transporte y Automotor"),
        CategoryItem("Electricidad del Automóvil", "🔋", Color(0xFFFFF9C4), "Transporte y Automotor"),
        CategoryItem("Polarizado", "🕶️", Color(0xFFCFD8DC), "Transporte y Automotor"),
        CategoryItem("Afinación y Alineación", "🏎️", Color(0xFFC5CAE9), "Transporte y Automotor"),
        CategoryItem("Inyección Electrónica", "🔌", Color(0xFFB2EBF2), "Transporte y Automotor"),
        CategoryItem("Repuestos Autos", "🚘", Color(0xFFD7CCC8), "Transporte y Automotor"),
        CategoryItem("Repuestos Motos", "🏍️", Color(0xFFFFCCBC), "Transporte y Automotor"),
        CategoryItem("Venta de Neumáticos", "🛞", Color(0xFF90CAF9), "Transporte y Automotor"),
        CategoryItem("Taller de Motos", "🛠️", Color(0xFFFFAB91), "Transporte y Automotor"),
        CategoryItem("Alquiler de Autos", "🚗", Color(0xFFB2DFDB), "Transporte y Automotor"),
        CategoryItem("Maquinaria Pesada", "🚜", Color(0xFFFFCCBC), "Transporte y Automotor"),

        // -----------------------------------------
        // SUPERCATEGORÍA: TECNOLOGÍA Y SISTEMAS
        // -----------------------------------------
        CategoryItem("Reparación de PC", "🖥️", Color(0xFF90CAF9), "Tecnología y Sistemas"),
        CategoryItem("Reparación de Celulares", "📱", Color(0xFFB3E5FC), "Tecnología y Sistemas"),
        CategoryItem("Redes e Internet", "🌐", Color(0xFF81D4FA), "Tecnología y Sistemas"),
        CategoryItem("Cámaras de Seguridad", "📹", Color(0xFFB0BEC5), "Tecnología y Sistemas"),
        CategoryItem("Desarrollo Web", "👨‍💻", Color(0xFFC5CAE9), "Tecnología y Sistemas"),
        CategoryItem("Programador", "💻", Color(0xFF9FA8DA), "Tecnología y Sistemas"),
        CategoryItem("Domótica", "🏡", Color(0xFF80DEEA), "Tecnología y Sistemas"),
        CategoryItem("Venta de Insumos", "🖱️", Color(0xFFBBDEFB), "Tecnología y Sistemas"),
        CategoryItem("Reparación de Consolas", "🎮", Color(0xFFD1C4E9), "Tecnología y Sistemas"),
        CategoryItem("Diseño de Software", "⚙️", Color(0xFFB39DDB), "Tecnología y Sistemas"),
        CategoryItem("Servicio Técnico Apple", "🍏", Color(0xFFE0E0E0), "Tecnología y Sistemas"),
        CategoryItem("Servicio Técnico Android", "🤖", Color(0xFFA5D6A7), "Tecnología y Sistemas"),
        CategoryItem("Impresión 3D", "🧊", Color(0xFFFFF59D), "Tecnología y Sistemas"),
        CategoryItem("Reparación de Drones", "🚁", Color(0xFFCFD8DC), "Tecnología y Sistemas"),

        // -----------------------------------------
        // SUPERCATEGORÍA: SERVICIOS PROFESIONALES
        // -----------------------------------------
        CategoryItem("Abogado", "⚖️", Color(0xFFCFD8DC), "Servicios Profesionales"),
        CategoryItem("Contador", "🧾", Color(0xFFE0F7FA), "Servicios Profesionales"),
        CategoryItem("Escribano", "✍️", Color(0xFFF5F5F5), "Servicios Profesionales"),
        CategoryItem("Arquitecto", "📐", Color(0xFFD7CCC8), "Servicios Profesionales"),
        CategoryItem("Traductor", "🗣️", Color(0xFFFFF9C4), "Servicios Profesionales"),
        CategoryItem("Diseñador Gráfico", "🎨", Color(0xFFF8BBD0), "Servicios Profesionales"),
        CategoryItem("Marketing Digital", "📈", Color(0xFFE1BEE7), "Servicios Profesionales"),
        CategoryItem("Asesor Financiero", "💹", Color(0xFFC8E6C9), "Servicios Profesionales"),
        CategoryItem("Gestoría", "📁", Color(0xFFFFF3E0), "Servicios Profesionales"),
        CategoryItem("Consultor de Negocios", "💼", Color(0xFFB3E5FC), "Servicios Profesionales"),
        CategoryItem("Recursos Humanos", "👥", Color(0xFFD1C4E9), "Servicios Profesionales"),
        CategoryItem("Community Manager", "📱", Color(0xFFF48FB1), "Servicios Profesionales"),
        CategoryItem("Diseñador UX/UI", "🖥️", Color(0xFF80DEEA), "Servicios Profesionales"),
        CategoryItem("Editor de Video", "🎞️", Color(0xFFFFCC80), "Servicios Profesionales"),
        CategoryItem("Fotógrafo de Producto", "📸", Color(0xFFBCAAA4), "Servicios Profesionales"),
        CategoryItem("Redactor Freelance", "📝", Color(0xFFE0E0E0), "Servicios Profesionales"),
        CategoryItem("Corredor de Seguros", "🛡️", Color(0xFFFFAB91), "Servicios Profesionales"),
        CategoryItem("Agente Inmobiliario", "🏢", Color(0xFFBBDEFB), "Servicios Profesionales"),

        // -----------------------------------------
        // SUPERCATEGORÍA: CUIDADO PERSONAL Y BELLEZA
        // -----------------------------------------
        CategoryItem("Peluquería", "✂️", Color(0xFFF8BBD0), "Cuidado Personal y Belleza"),
        CategoryItem("Barbería", "💈", Color(0xFFBCAAA4), "Cuidado Personal y Belleza"),
        CategoryItem("Manicura y Pedicura", "💅", Color(0xFFF48FB1), "Cuidado Personal y Belleza"),
        CategoryItem("Maquillaje Profesional", "💄", Color(0xFFFCE4EC), "Cuidado Personal y Belleza"),
        CategoryItem("Masajes", "💆", Color(0xFFFFE0B2), "Cuidado Personal y Belleza"),
        CategoryItem("Depilación", "🦵", Color(0xFFE1BEE7), "Cuidado Personal y Belleza"),
        CategoryItem("Tatuajes y Piercing", "🖋️", Color(0xFFCFD8DC), "Cuidado Personal y Belleza"),
        CategoryItem("Spa y Relax", "🧖‍♀️", Color(0xFFB2DFDB), "Cuidado Personal y Belleza"),
        CategoryItem("Estética Corporal", "✨", Color(0xFFF1CBFF), "Cuidado Personal y Belleza"),
        CategoryItem("Cosmiatría", "💆‍♀️", Color(0xFFFFCDD2), "Cuidado Personal y Belleza"),
        CategoryItem("Perfilado de Cejas", "👁️‍🗨️", Color(0xFFD7CCC8), "Cuidado Personal y Belleza"),
        CategoryItem("Lifting de Pestañas", "👁️", Color(0xFFE1F5FE), "Cuidado Personal y Belleza"),
        CategoryItem("Centro de Estética", "🏥", Color(0xFFC8E6C9), "Cuidado Personal y Belleza"),
        CategoryItem("Solarium", "☀️", Color(0xFFFFE082), "Cuidado Personal y Belleza"),

        // -----------------------------------------
        // SUPERCATEGORÍA: EDUCACIÓN Y CLASES
        // -----------------------------------------
        CategoryItem("Clases Particulares", "📖", Color(0xFFFFE0B2), "Educación y Clases"),
        CategoryItem("Profesor de Inglés", "🇬🇧", Color(0xFFBBDEFB), "Educación y Clases"),
        CategoryItem("Profesor de Matemáticas", "➗", Color(0xFFC8E6C9), "Educación y Clases"),
        CategoryItem("Escuela de Música", "🎼", Color(0xFFE1BEE7), "Educación y Clases"),
        CategoryItem("Clases de Canto", "🎤", Color(0xFFF48FB1), "Educación y Clases"),
        CategoryItem("Guitarra", "🎸", Color(0xFFFFCC80), "Educación y Clases"),
        CategoryItem("Piano", "🎹", Color(0xFFD7CCC8), "Educación y Clases"),
        CategoryItem("Taller de Arte", "🎨", Color(0xFFFFF59D), "Educación y Clases"),
        CategoryItem("Apoyo Escolar", "🎒", Color(0xFFB2EBF2), "Educación y Clases"),
        CategoryItem("Clases de Cocina", "🧑‍🍳", Color(0xFFFFAB91), "Educación y Clases"),
        CategoryItem("Clases de Programación", "💻", Color(0xFFCFD8DC), "Educación y Clases"),
        CategoryItem("Cursos de Fotografía", "📸", Color(0xFFB3E5FC), "Educación y Clases"),
        CategoryItem("Autoescuela (Manejo)", "🚗", Color(0xFFB0BEC5), "Educación y Clases"),
        CategoryItem("Educación Especial", "🧩", Color(0xFFE0F7FA), "Educación y Clases"),

        // -----------------------------------------
        // SUPERCATEGORÍA: MASCOTAS Y VETERINARIA
        // -----------------------------------------
        CategoryItem("Veterinaria", "🐶", Color(0xFFC8E6C9), "Mascotas y Veterinaria"),
        CategoryItem("Peluquería Canina", "🐩", Color(0xFFAED581), "Mascotas y Veterinaria"),
        CategoryItem("Paseador de Perros", "🦮", Color(0xFFDCEDC8), "Mascotas y Veterinaria"),
        CategoryItem("Guardería de Mascotas", "🏡", Color(0xFFFFF9C4), "Mascotas y Veterinaria"),
        CategoryItem("Adiestrador", "🐕‍🦺", Color(0xFFFFE0B2), "Mascotas y Veterinaria"),
        CategoryItem("Pet Shop", "🦴", Color(0xFFFFCCBC), "Mascotas y Veterinaria"),
        CategoryItem("Acuario", "🐠", Color(0xFF81D4FA), "Mascotas y Veterinaria"),
        CategoryItem("Veterinaria 24hs", "🚑", Color(0xFFFFCDD2), "Mascotas y Veterinaria"),
        CategoryItem("Crematorio de Mascotas", "🕊️", Color(0xFFCFD8DC), "Mascotas y Veterinaria"),

        // -----------------------------------------
        // SUPERCATEGORÍA: MODA Y TEXTIL
        // -----------------------------------------
        CategoryItem("Modista", "👗", Color(0xFFF8BBD0), "Moda y Textil"),
        CategoryItem("Sastre", "👔", Color(0xFFBCAAA4), "Moda y Textil"),
        CategoryItem("Arreglo de Ropa", "🧵", Color(0xFFE1BEE7), "Moda y Textil"),
        CategoryItem("Diseño de Indumentaria", "📐", Color(0xFFFFF59D), "Moda y Textil"),
        CategoryItem("Venta de Ropa", "👕", Color(0xFFBBDEFB), "Moda y Textil"),
        CategoryItem("Zapatería", "👞", Color(0xFFD7CCC8), "Moda y Textil"),
        CategoryItem("Marroquinería", "👜", Color(0xFFFFCC80), "Moda y Textil"),
        CategoryItem("Lavadero de Ropa", "🧺", Color(0xFFE1F5FE), "Moda y Textil"),
        CategoryItem("Estampado y Bordado", "🖨️", Color(0xFFC8E6C9), "Moda y Textil"),
        CategoryItem("Disfraces", "🦸", Color(0xFFD1C4E9), "Moda y Textil")
    )
}







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
**/
