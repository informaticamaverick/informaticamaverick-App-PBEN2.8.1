package com.example.myapplication.data.model.fake

import androidx.compose.ui.graphics.Color



/************************************************NUEVO HAY QUE REVISAR Y ACTUALIZAR ******************************************
// * MAS INGENIRIAS , MAESTROS PARTICULARES, PASARLE A GEMINI SOLO SUPERCATEGORIAS Y QUE LAS COMPLETE

// =========================================
// 🔥 DICCIONARIO MAESTRO DE SUPERCATEGORÍAS (VERSIÓN DEFINITIVA)
// =========================================**/

val superCategoryIconsMap = mapOf(
"Salud y Medicina" to "⚕️",
"Bienestar y Terapias Alternativas" to "🌿",
"Cuidado Personal y Belleza" to "💅",
"Moda y Textil" to "👗",
"Hogar y Mantenimiento" to "🏠",
"Construcción y Oficios Pesados" to "🏗️",
"Limpieza y Saneamiento" to "🧹",
"Jardinería y Paisajismo" to "🌳",
"Tecnología y Sistemas" to "💻",
"Deporte y Recreación" to "⚽",
"Eventos y Entretenimiento" to "🎉",
"Gastronomía y Bares" to "🍔",
"Transporte y Logística" to "🚛",
"Servicios Automotores" to "🚗",
"Servicios Profesionales y Legales" to "⚖️",
"Finanzas y Negocios" to "📈",
"Marketing, Diseño y Medios" to "📱",
"Ciencias y Humanidades" to "🔬",
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

// Búsqueda automática del icono de supercategoría
val superCategoryIcon: String = superCategoryIconsMap[superCategory] ?: "📂",

val providerIds: MutableList<String> = mutableListOf(),
val imageUrl: String? = "https://picsum.photos/400",
val isNew: Boolean = Math.random() < 0.2,
val isNewPrestador: Boolean = Math.random() < 0.15,
val isAd: Boolean = false
)

// =========================================
// BASE DE DATOS FALSA (ESTRUCTURA MASIVA Y PROFESIONAL ARGENTINA)
// =========================================
object CategorySampleDataFalso {
val categories = listOf(

// -----------------------------------------
// ⚕️ SUPERCATEGORÍA: SALUD Y MEDICINA
// -----------------------------------------
CategoryItem("Médico Clínico", "🩺", Color(0xFFB2EBF2), "Salud y Medicina"),
CategoryItem("Pediatra", "👶", Color(0xFFC5CAE9), "Salud y Medicina"),
CategoryItem("Cardiólogo", "❤️", Color(0xFFFFCDD2), "Salud y Medicina"),
CategoryItem("Odontólogo", "🦷", Color(0xFFE1BEE7), "Salud y Medicina"),
CategoryItem("Ortodoncista", "😁", Color(0xFFE1BEE7), "Salud y Medicina"),
CategoryItem("Odontopediatra", "🧒", Color(0xFFF8BBD0), "Salud y Medicina"),
CategoryItem("Psicólogo", "🧠", Color(0xFFD1C4E9), "Salud y Medicina"),
CategoryItem("Psiquiatra", "🛋️", Color(0xFFD7CCC8), "Salud y Medicina"),
CategoryItem("Psicopedagogo", "🧩", Color(0xFFFFF9C4), "Salud y Medicina"),
CategoryItem("Nutricionista", "🍏", Color(0xFFC8E6C9), "Salud y Medicina"),
CategoryItem("Kinesiólogo", "🏃", Color(0xFFB3E5FC), "Salud y Medicina"),
CategoryItem("Fisioterapeuta", "💆‍♂️", Color(0xFFE0F7FA), "Salud y Medicina"),
CategoryItem("Terapista Ocupacional", "🤲", Color(0xFFFFE0B2), "Salud y Medicina"),
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
// 🌿 SUPERCATEGORÍA: BIENESTAR Y TERAPIAS ALTERNATIVAS
// -----------------------------------------
CategoryItem("Quiropráctico", "🦴", Color(0xFFE0F7FA), "Bienestar y Terapias Alternativas"),
CategoryItem("Osteópata", "👐", Color(0xFFB2EBF2), "Bienestar y Terapias Alternativas"),
CategoryItem("Acupuntura", "📍", Color(0xFFC7CEEA), "Bienestar y Terapias Alternativas"),
CategoryItem("Terapeuta Reiki", "✨", Color(0xFFF1CBFF), "Bienestar y Terapias Alternativas"),
CategoryItem("Homeopatía", "🌿", Color(0xFFC8E6C9), "Bienestar y Terapias Alternativas"),
CategoryItem("Reflexología", "🦶", Color(0xFFFFCCBC), "Bienestar y Terapias Alternativas"),
CategoryItem("Aromaterapia", "🌺", Color(0xFFE1BEE7), "Bienestar y Terapias Alternativas"),
CategoryItem("Flores de Bach", "🌼", Color(0xFFF8BBD0), "Bienestar y Terapias Alternativas"),
CategoryItem("Biodescodificación", "🧬", Color(0xFFD1C4E9), "Bienestar y Terapias Alternativas"),
CategoryItem("Terapia Holística", "☯️", Color(0xFFFFF59D), "Bienestar y Terapias Alternativas"),
CategoryItem("Masajes Descontracturantes", "💆‍♂️", Color(0xFFFFE0B2), "Bienestar y Terapias Alternativas"),

// -----------------------------------------
// 💅 SUPERCATEGORÍA: CUIDADO PERSONAL Y BELLEZA
// -----------------------------------------
CategoryItem("Peluquería", "✂️", Color(0xFFF8BBD0), "Cuidado Personal y Belleza"),
CategoryItem("Barbería", "💈", Color(0xFFBCAAA4), "Cuidado Personal y Belleza"),
CategoryItem("Estilista", "💇‍♀️", Color(0xFFF48FB1), "Cuidado Personal y Belleza"),
CategoryItem("Maquillaje Profesional", "💄", Color(0xFFFCE4EC), "Cuidado Personal y Belleza"),
CategoryItem("Manicura y Uñas Esculpidas", "💅", Color(0xFFF48FB1), "Cuidado Personal y Belleza"),
CategoryItem("Pedicuro", "🦶", Color(0xFFFFCCBC), "Cuidado Personal y Belleza"),
CategoryItem("Cosmetólogo", "🧴", Color(0xFFE1F5FE), "Cuidado Personal y Belleza"),
CategoryItem("Cosmiatría", "💆‍♀️", Color(0xFFFFCDD2), "Cuidado Personal y Belleza"),
CategoryItem("Depilación", "🦵", Color(0xFFE1BEE7), "Cuidado Personal y Belleza"),
CategoryItem("Depilación Definitiva (Láser)", "✨", Color(0xFFFFF9C4), "Cuidado Personal y Belleza"),
CategoryItem("Perfilado de Cejas", "👁️‍🗨️", Color(0xFFD7CCC8), "Cuidado Personal y Belleza"),
CategoryItem("Lifting de Pestañas", "👁️", Color(0xFFE1F5FE), "Cuidado Personal y Belleza"),
CategoryItem("Microblading", "✒️", Color(0xFFCFD8DC), "Cuidado Personal y Belleza"),
CategoryItem("Tatuador", "🖋️", Color(0xFFCFD8DC), "Cuidado Personal y Belleza"),
CategoryItem("Piercer", "🧷", Color(0xFFB0BEC5), "Cuidado Personal y Belleza"),
CategoryItem("Spa y Relax", "🧖‍♀️", Color(0xFFB2DFDB), "Cuidado Personal y Belleza"),
CategoryItem("Cama Solar", "🌟", Color(0xFFF1CBFF), "Cuidado Personal y Belleza"),
CategoryItem("Centro de Estética", "🏥", Color(0xFFC8E6C9), "Cuidado Personal y Belleza"),
// -----------------------------------------
// 👗 SUPERCATEGORÍA: MODA Y TEXTIL
// -----------------------------------------
CategoryItem("Asesor de Imagen", "👗", Color(0xFFE1BEE7), "Moda y Textil"),
CategoryItem("Modista", "👗", Color(0xFFF8BBD0), "Moda y Textil"),
CategoryItem("Sastre", "👔", Color(0xFFBCAAA4), "Moda y Textil"),
CategoryItem("Costurera", "🪡", Color(0xFFFFCCBC), "Moda y Textil"),
CategoryItem("Arreglo de Ropa", "🧵", Color(0xFFE1BEE7), "Moda y Textil"),
CategoryItem("Alta Costura", "✨", Color(0xFFD1C4E9), "Moda y Textil"),
CategoryItem("Zapatero", "👞", Color(0xFFD7CCC8), "Moda y Textil"),
CategoryItem("Marroquinería", "👜", Color(0xFFFFCC80), "Moda y Textil"),
CategoryItem("Joyero", "💎", Color(0xFFFFF59D), "Moda y Textil"),
CategoryItem("Diseñador de Modas", "📐", Color(0xFFCE93D8), "Moda y Textil"),
CategoryItem("Venta de Ropa", "👕", Color(0xFFBBDEFB), "Moda y Textil"),
CategoryItem("Estampado y Bordado", "🖨️", Color(0xFFC8E6C9), "Moda y Textil"),
CategoryItem("Disfraces", "🦸", Color(0xFFD1C4E9), "Moda y Textil"),

// -----------------------------------------
// 🏠 SUPERCATEGORÍA: HOGAR Y MANTENIMIENTO
// -----------------------------------------
CategoryItem("Plomería", "🪠", Color(0xFFBCAAA4), "Hogar y Mantenimiento"),
CategoryItem("Destapa caños y Desagotes", "🚜", Color(0xFF8D6E63), "Hogar y Mantenimiento"),
CategoryItem("Electricista", "⚡", Color(0xFFFFF59D), "Hogar y Mantenimiento"),
CategoryItem("Gasista", "🔥", Color(0xFFFFAB91), "Hogar y Mantenimiento"),
CategoryItem("Carpintería", "🪚", Color(0xFFD7CCC8), "Hogar y Mantenimiento"),
CategoryItem("Ensamblaje de Muebles", "🔩", Color(0xFFD4A5A5), "Hogar y Mantenimiento"),
CategoryItem("Cerrajero", "🔑", Color(0xFFE0E0E0), "Hogar y Mantenimiento"),
CategoryItem("Vidriero", "🪟", Color(0xFFE1F5FE), "Hogar y Mantenimiento"),
CategoryItem("Aire Acondicionado (Técnico)", "❄️", Color(0xFFB3E5FC), "Hogar y Mantenimiento"),
CategoryItem("Refrigeración (Técnico)", "🧊", Color(0xFF81D4FA), "Hogar y Mantenimiento"),
CategoryItem("Estufas y Calefacción", "♨️", Color(0xFFFFCCBC), "Hogar y Mantenimiento"),
CategoryItem("Electrodomésticos (Técnico)", "🔌", Color(0xFFFFCC80), "Hogar y Mantenimiento"),
CategoryItem("Mantenimiento de Piscinas", "🏊‍♂️", Color(0xFF81D4FA), "Hogar y Mantenimiento"),
CategoryItem("Instalador de Paneles Solares", "☀️", Color(0xFFFFF59D), "Hogar y Mantenimiento"),
CategoryItem("Diseño de Interiores", "🛋️", Color(0xFFA2D2FF), "Hogar y Mantenimiento"),
CategoryItem("Tapicero", "🛋️", Color(0xFFD1C4E9), "Hogar y Mantenimiento"),
CategoryItem("Mueblería a Medida", "🪑", Color(0xFFFFCC80), "Hogar y Mantenimiento"),
CategoryItem("Domótica para el Hogar", "🏡", Color(0xFF80DEEA), "Hogar y Mantenimiento"),

// -----------------------------------------
// 🏗️ SUPERCATEGORÍA: CONSTRUCCIÓN Y OFICIOS PESADOS
// -----------------------------------------
CategoryItem("Albañil", "🧱", Color(0xFFFFE0B2), "Construcción y Oficios Pesados"),
CategoryItem("Maestro Mayor de Obra", "🏗️", Color(0xFFFFCC80), "Construcción y Oficios Pesados"),
CategoryItem("Contratista", "📋", Color(0xFFCFD8DC), "Construcción y Oficios Pesados"),
CategoryItem("Obrero", "👷", Color(0xFFFFB7B2), "Construcción y Oficios Pesados"),
CategoryItem("Pintor de Obras", "🖌️", Color(0xFFC5CAE9), "Construcción y Oficios Pesados"),
CategoryItem("Techista", "🏠", Color(0xFFCFD8DC), "Construcción y Oficios Pesados"),
CategoryItem("Herrero de Obra", "⚒️", Color(0xFFB0BEC5), "Construcción y Oficios Pesados"),
CategoryItem("Soldador", "🔥", Color(0xFFFF8A65), "Construcción y Oficios Pesados"),
CategoryItem("Tornero", "⚙️", Color(0xFF9E9E9E), "Construcción y Oficios Pesados"),
CategoryItem("Yesero / Durlock", "🏢", Color(0xFFF5F5F5), "Construcción y Oficios Pesados"),
CategoryItem("Pisos y Revestimientos", "🔲", Color(0xFFCFD8DC), "Construcción y Oficios Pesados"),
CategoryItem("Impermeabilización", "☔", Color(0xFFB2EBF2), "Construcción y Oficios Pesados"),
CategoryItem("Zinguería", "🏗️", Color(0xFF9E9E9E), "Construcción y Oficios Pesados"),
CategoryItem("Marmolería", "🪨", Color(0xFFD7CCC8), "Construcción y Oficios Pesados"),
CategoryItem("Ascensores (Instalación y Mantenimiento)", "🛗", Color(0xFFCFD8DC), "Construcción y Oficios Pesados"),
CategoryItem("Operador de Grúa / Maquinaria", "🏗️", Color(0xFFFFCC80), "Construcción y Oficios Pesados"),
CategoryItem("Agrimensor", "🗺️", Color(0xFFE6EE9C), "Construcción y Oficios Pesados"),
CategoryItem("Topógrafo", "🔭", Color(0xFFBCAAA4), "Construcción y Oficios Pesados"),

// -----------------------------------------
// 🧹 SUPERCATEGORÍA: LIMPIEZA Y SANEAMIENTO
// -----------------------------------------
CategoryItem("Limpieza Doméstica", "🧹", Color(0xFFF0F4C3), "Limpieza y Saneamiento"),
CategoryItem("Limpieza de Obras / Final de Obra", "🏗️", Color(0xFFD7CCC8), "Limpieza y Saneamiento"),
CategoryItem("Limpieza de Vidrios en Altura", "🪟", Color(0xFFE1F5FE), "Limpieza y Saneamiento"),
CategoryItem("Limpieza de Tapizados y Alfombras", "🛋️", Color(0xFFD1C4E9), "Limpieza y Saneamiento"),
CategoryItem("Fumigación y Control de Plagas", "💨", Color(0xFFAED581), "Limpieza y Saneamiento"),
CategoryItem("Desinfección de Ambientes", "🦠", Color(0xFFB2DFDB), "Limpieza y Saneamiento"),
CategoryItem("Lavadero de Ropa / Tintorería", "🧺", Color(0xFFE1F5FE), "Limpieza y Saneamiento"),

// -----------------------------------------
// 🌳 SUPERCATEGORÍA: JARDINERÍA Y PAISAJISMO
// -----------------------------------------
CategoryItem("Jardinería", "🌿", Color(0xFFC8E6C9), "Jardinería y Paisajismo"),
CategoryItem("Paisajista", "🏞️", Color(0xFFAED581), "Jardinería y Paisajismo"),
CategoryItem("Poda de Árboles", "🪓", Color(0xFF8D6E63), "Jardinería y Paisajismo"),
CategoryItem("Diseño de Exteriores", "🏡", Color(0xFFC5E1A5), "Jardinería y Paisajismo"),
CategoryItem("Mantenimiento de Césped", "🌱", Color(0xFFDCEDC8), "Jardinería y Paisajismo"),

// -----------------------------------------
// 💻 SUPERCATEGORÍA: TECNOLOGÍA Y SISTEMAS
// -----------------------------------------
CategoryItem("PC y Notebooks (Técnico)", "💻", Color(0xFF90CAF9), "Tecnología y Sistemas"),
CategoryItem("Celulares (Técnico)", "📱", Color(0xFFB3E5FC), "Tecnología y Sistemas"),
CategoryItem("Redes e Internet (Técnico)", "🌐", Color(0xFF81D4FA), "Tecnología y Sistemas"),
CategoryItem("Control de Accesos - Porteros (Técnico)", "🪪", Color(0xFFB0BEC5), "Tecnología y Sistemas"),
CategoryItem("Seguridad Electronica (Técnico)", "🛡️", Color(0xFFB0BEC5), "Tecnología y Sistemas"),
CategoryItem("Cámaras de Seguridad (Técnico)", "📹", Color(0xFFB0BEC5), "Tecnología y Sistemas"),
CategoryItem("Alarmas de Seguridad (Técnico)", "🚨", Color(0xFFFFCDD2), "Tecnología y Sistemas"),
CategoryItem("Desarrollador de Software", "👨‍💻", Color(0xFFC5CAE9), "Tecnología y Sistemas"),
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
CategoryItem("Scrum Master / Project Manager", "📋", Color(0xFFFFCC80), "Tecnología y Sistemas"),
CategoryItem("Informática (Técnico)", "🖥️", Color(0xFFBBDEFB), "Tecnología y Sistemas"),
CategoryItem("Reparación de Consolas (Técnico)", "🎮", Color(0xFFD1C4E9), "Tecnología y Sistemas"),
CategoryItem("Impresión 3D", "🧊", Color(0xFFFFF59D), "Tecnología y Sistemas"),
CategoryItem("Drones (Técnico)", "🚁", Color(0xFFCFD8DC), "Tecnología y Sistemas"),
CategoryItem("Gammer / Coach E-Sports", "🕹️", Color(0xFFE040FB), "Tecnología y Sistemas"),
CategoryItem("Apple (Técnico)", "🍏", Color(0xFFE0E0E0), "Tecnología y Sistemas"),
CategoryItem("Android (Técnico)", "🤖", Color(0xFFA5D6A7), "Tecnología y Sistemas"),

// -----------------------------------------
// ⚽ SUPERCATEGORÍA: DEPORTE Y RECREACIÓN
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
CategoryItem("Entrenador Personal (Personal Trainer)", "💪", Color(0xFFFFB7B2), "Deporte y Recreación"),
CategoryItem("Paintball", "🔫", Color(0xFF8D6E63), "Deporte y Recreación"),
CategoryItem("Escuela de Baile", "💃", Color(0xFFF48FB1), "Deporte y Recreación"),
CategoryItem("Salsa y Bachata", "🕺", Color(0xFFCE93D8), "Deporte y Recreación"),
CategoryItem("Patinaje", "⛸️", Color(0xFF80DEEA), "Deporte y Recreación"),
CategoryItem("Boxeo", "🥊", Color(0xFFEF9A9A), "Deporte y Recreación"),
CategoryItem("Natación", "🏊", Color(0xFF81D4FA), "Deporte y Recreación"),
CategoryItem("Yoga", "🧘‍♀️", Color(0xFFB2DFDB), "Deporte y Recreación"),
CategoryItem("Pilates", "🧘", Color(0xFFD1C4E9), "Deporte y Recreación"),
CategoryItem("Artes Marciales", "🥋", Color(0xFFFFAB91), "Deporte y Recreación"),
CategoryItem("Entrenamiento Funcional", "⏱️", Color(0xFFFFCC80), "Deporte y Recreación"),
CategoryItem("Ciclismo", "🚴", Color(0xFF80CBC4), "Deporte y Recreación"),
CategoryItem("Trekking y Montañismo", "🧗", Color(0xFFA1887F), "Deporte y Recreación"),
CategoryItem("Skatepark", "🛹", Color(0xFF90CAF9), "Deporte y Recreación"),
CategoryItem("Surf / Kitesurf", "🏄", Color(0xFF4DD0E1), "Deporte y Recreación"),
CategoryItem("Paracaidismo", "🪂", Color(0xFFCFD8DC), "Deporte y Recreación"),
CategoryItem("Buceo", "🤿", Color(0xFF00BCD4), "Deporte y Recreación"),
CategoryItem("Alquiler de Bicicletas", "🚲", Color(0xFFAED581), "Deporte y Recreación"),

// -----------------------------------------
// 🎉 SUPERCATEGORÍA: EVENTOS Y ENTRETIMIENTO
// -----------------------------------------
CategoryItem("Salon de Eventos", "🎪", Color(0xFFE1BEE7), "Eventos y Entretenimiento"),
CategoryItem("Salon de Fiestas", "🎊", Color(0xFFF8BBD0), "Eventos y Entretenimiento"),
CategoryItem("Organizador de Bodas", "💒", Color(0xFFF8BBD0), "Eventos y Entretenimiento"),
CategoryItem("Ambientación de Eventos", "🎀", Color(0xFFFFCDD2), "Eventos y Entretenimiento"),
CategoryItem("Fotografía de Eventos", "📷", Color(0xFFB3E5FC), "Eventos y Entretenimiento"),
CategoryItem("Videografía", "🎥", Color(0xFF90CAF9), "Eventos y Entretenimiento"),
CategoryItem("DJ", "🎧", Color(0xFFB39DDB), "Eventos y Entretenimiento"),
CategoryItem("Sonido e Iluminación", "🎛️", Color(0xFF9E9E9E), "Eventos y Entretenimiento"),
CategoryItem("Animador Infantil", "🎈", Color(0xFFFFCC80), "Eventos y Entretenimiento"),
CategoryItem("Comediante / Stand Up", "🎤", Color(0xFFCE93D8), "Eventos y Entretenimiento"),
CategoryItem("Músico / Banda en Vivo", "🎸", Color(0xFFF48FB1), "Eventos y Entretenimiento"),
CategoryItem("Mago", "🪄", Color(0xFFB39DDB), "Eventos y Entretenimiento"),
CategoryItem("Payaso", "🤡", Color(0xFFFFCDD2), "Eventos y Entretenimiento"),
CategoryItem("Bailarín / Coreógrafo", "🕺", Color(0xFFE1BEE7), "Eventos y Entretenimiento"),
CategoryItem("Maquillador Artístico / FX", "🧟", Color(0xFF8D6E63), "Eventos y Entretenimiento"),
CategoryItem("Alquiler de Vajilla", "🍽️", Color(0xFFF0F4C3), "Eventos y Entretenimiento"),
CategoryItem("Carpas y Toldos", "⛺", Color(0xFFC5CAE9), "Eventos y Entretenimiento"),
CategoryItem("Florista", "💐", Color(0xFFC8E6C9), "Eventos y Entretenimiento"),
CategoryItem("Servicio de Lunch / Catering", "🥪", Color(0xFFFFE082), "Eventos y Entretenimiento"),
CategoryItem("Asador / Parrillero para Eventos", "🥩", Color(0xFFD84315), "Eventos y Entretenimiento"),
CategoryItem("Barras Móviles", "🍸", Color(0xFF80DEEA), "Eventos y Entretenimiento"),
CategoryItem("Fotocabina", "📸", Color(0xFFCFD8DC), "Eventos y Entretenimiento"),
CategoryItem("Castillos Inflables", "🏰", Color(0xFFFFF9C4), "Eventos y Entretenimiento"),
CategoryItem("Pista de LED", "🪩", Color(0xFFE040FB), "Eventos y Entretenimiento"),
CategoryItem("Cotillón", "🥳", Color(0xFFFF8A65), "Eventos y Entretenimiento"),

// -----------------------------------------
// 🍔 SUPERCATEGORÍA: GASTRONOMÍA Y BARES
// -----------------------------------------
CategoryItem("Restaurante", "🍽️", Color(0xFFFFAB91), "Gastronomía y Bares"),
CategoryItem("Bar / Pub", "🍻", Color(0xFFFFCC80), "Gastronomía y Bares"),
CategoryItem("Cervecería Artesanal", "🍺", Color(0xFFFFE082), "Gastronomía y Bares"),
CategoryItem("Coctelería", "🍹", Color(0xFFF48FB1), "Gastronomía y Bares"),
CategoryItem("Bodegón", "🥩", Color(0xFFBCAAA4), "Gastronomía y Bares"),
CategoryItem("Pizzería", "🍕", Color(0xFFFFE082), "Gastronomía y Bares"),
CategoryItem("Hamburguesería", "🍔", Color(0xFFFFCC80), "Gastronomía y Bares"),
CategoryItem("Sushi", "🍣", Color(0xFFF8BBD0), "Gastronomía y Bares"),
CategoryItem("Comida Vegana / Vegetariana", "🥗", Color(0xFFA5D6A7), "Gastronomía y Bares"),
CategoryItem("Dietética", "🥜", Color(0xFFC8E6C9), "Gastronomía y Bares"),
CategoryItem("Cafetería", "☕", Color(0xFFD7CCC8), "Gastronomía y Bares"),
CategoryItem("Pastelería", "🍰", Color(0xFFF48FB1), "Gastronomía y Bares"),
CategoryItem("Panadería", "🥖", Color(0xFFFFE0B2), "Gastronomía y Bares"),
CategoryItem("Food Truck", "🚐", Color(0xFF90CAF9), "Gastronomía y Bares"),
CategoryItem("Heladería", "🍦", Color(0xFFE1BEE7), "Gastronomía y Bares"),
CategoryItem("Vinoteca", "🍾", Color(0xFFEF9A9A), "Gastronomía y Bares"),
CategoryItem("Fiambrería", "🧀", Color(0xFFFFE0B2), "Gastronomía y Bares"),
CategoryItem("Rotisería", "🍗", Color(0xFFFFCCBC), "Gastronomía y Bares"),
CategoryItem("Comida Árabe / Shawarma", "🥙", Color(0xFFD7CCC8), "Gastronomía y Bares"),
CategoryItem("Comida Mexicana", "🌮", Color(0xFFC8E6C9), "Gastronomía y Bares"),
CategoryItem("Comida Peruana", "🥘", Color(0xFFFFF59D), "Gastronomía y Bares"),
CategoryItem("Comida China", "🍣", Color(0xFFC8E6C9), "Gastronomía y Bares"),
CategoryItem("Comida Japonesa", "🍜", Color(0xFFFFF59D), "Gastronomía y Bares"),
CategoryItem("Chocolatería", "🍫", Color(0xFF8D6E63), "Gastronomía y Bares"),
CategoryItem("Chef Privado", "🧑‍🍳", Color(0xFFB0BEC5), "Gastronomía y Bares"),
CategoryItem("Barista", "☕", Color(0xFF8D6E63), "Gastronomía y Bares"),
CategoryItem("Sommelier / Enólogo", "🍷", Color(0xFF9C27B0), "Gastronomía y Bares"),
CategoryItem("Carnicero", "🥩", Color(0xFFEF5350), "Gastronomía y Bares"),
CategoryItem("Pescadero", "🐟", Color(0xFF4FC3F7), "Gastronomía y Bares"),
CategoryItem("Camarero / Mozo", "🤵", Color(0xFFE0E0E0), "Gastronomía y Bares"),
CategoryItem("Bartender", "🍸", Color(0xFF4DD0E1), "Gastronomía y Bares"),

// -----------------------------------------
// 🚛 SUPERCATEGORÍA: TRANSPORTE Y LOGÍSTICA
// -----------------------------------------
CategoryItem("Fletes", "🚛", Color(0xFFB0BEC5), "Transporte y Logística"),
CategoryItem("Mudanzas", "📦", Color(0xFFCFD8DC), "Transporte y Logística"),
CategoryItem("Acarreos", "🚚", Color(0xFFFFCC80), "Transporte y Logística"),
CategoryItem("Mensajería y Envíos", "✉️", Color(0xFFFFF176), "Transporte y Logística"),
CategoryItem("Motoquero / Cadete", "🛵", Color(0xFF81D4FA), "Transporte y Logística"),
CategoryItem("Taxis", "🚕", Color(0xFFFFF59D), "Transporte y Logística"),
CategoryItem("Remisería / Remís", "🚘", Color(0xFFE0E0E0), "Transporte y Logística"),
CategoryItem("Chofer Privado", "🕴️", Color(0xFF90CAF9), "Transporte y Logística"),
CategoryItem("Transporte Escolar", "🚌", Color(0xFFFFE082), "Transporte y Logística"),
CategoryItem("Transporte de Larga Distancia", "🚌", Color(0xFF78909C), "Transporte y Logística"),
CategoryItem("Camionero", "🚛", Color(0xFF90A4AE), "Transporte y Logística"),
CategoryItem("Piloto", "✈️", Color(0xFF81D4FA), "Transporte y Logística"),
CategoryItem("Capitán de Yate / Lancha", "🛥️", Color(0xFF4DD0E1), "Transporte y Logística"),

// -----------------------------------------
// 🚗 SUPERCATEGORÍA: SERVICIOS AUTOMOTORES
// -----------------------------------------
CategoryItem("Auxilio Mecánico", "🆘", Color(0xFFEF9A9A), "Servicios Automotores"),
CategoryItem("Gruero", "🏗️", Color(0xFFFFB7B2), "Servicios Automotores"),
CategoryItem("Gomería", "🛞", Color(0xFF9E9E9E), "Servicios Automotores"),
CategoryItem("Gomería Móvil", "🛞", Color(0xFFBDBDBD), "Servicios Automotores"),
CategoryItem("Taller Mecánico", "⚙️", Color(0xFFB0BEC5), "Servicios Automotores"),
CategoryItem("Mecánico de Autos", "🚗", Color(0xFF90A4AE), "Servicios Automotores"),
CategoryItem("Mecánico de Motos", "🏍️", Color(0xFFFFAB91), "Servicios Automotores"),
CategoryItem("Tren Delantero", "🚙", Color(0xFFBCAAA4), "Servicios Automotores"),
CategoryItem("Alineación y Balanceo", "⚖️", Color(0xFFC5CAE9), "Servicios Automotores"),
CategoryItem("Inyección Electrónica", "🔌", Color(0xFFB2EBF2), "Servicios Automotores"),
CategoryItem("Instalación y Reparación de GNC", "⛽", Color(0xFFA5D6A7), "Servicios Automotores"),
CategoryItem("Especialista en Cajas Automáticas", "⚙️", Color(0xFFD7CCC8), "Servicios Automotores"),
CategoryItem("Especialista en Frenos", "🛑", Color(0xFFEF9A9A), "Servicios Automotores"),
CategoryItem("Lavadero de Autos", "🧽", Color(0xFF81D4FA), "Servicios Automotores"),
CategoryItem("Chapa y Pintura", "🚙", Color(0xFFFFCDD2), "Servicios Automotores"),
CategoryItem("Sacabollos", "🔨", Color(0xFFCFD8DC), "Servicios Automotores"),
CategoryItem("Detailing", "💎", Color(0xFFE0F7FA), "Servicios Automotores"),
CategoryItem("Electricidad del Automóvil", "🔋", Color(0xFFFFF9C4), "Servicios Automotores"),
CategoryItem("Polarizado", "🏴‍☠️🕶️", Color(0xFFCFD8DC), "Servicios Automotores"),
CategoryItem("Plotters (ploteos)", "🏴‍☠️", Color(0xFFCFD8DC), "Servicios Automotores"),
CategoryItem("Repuestos Autos", "🚘", Color(0xFFD7CCC8), "Servicios Automotores"),
CategoryItem("Repuestos Motos", "🏍️", Color(0xFFFFCCBC), "Servicios Automotores"),
CategoryItem("Venta de Neumáticos", "🛞", Color(0xFF90CAF9), "Servicios Automotores"),
CategoryItem("Alquiler de Autos", "🚗", Color(0xFFB2DFDB), "Servicios Automotores"),
CategoryItem("Maquinaria Pesada (Vial)", "🚜", Color(0xFFFFCCBC), "Servicios Automotores"),

// -----------------------------------------
// ⚖️ SUPERCATEGORÍA: SERVICIOS PROFESIONALES Y LEGALES
// -----------------------------------------
CategoryItem("Abogado General", "⚖️", Color(0xFFCFD8DC), "Servicios Profesionales y Legales"),
CategoryItem("Abogado Penalista", "⚖️", Color(0xFFB0BEC5), "Servicios Profesionales y Legales"),
CategoryItem("Abogado Civil y Familia", "⚖️", Color(0xFF90CAF9), "Servicios Profesionales y Legales"),
CategoryItem("Abogado Laboral", "⚖️", Color(0xFFF48FB1), "Servicios Profesionales y Legales"),
CategoryItem("Abogado Comercial", "⚖️", Color(0xFFFFCC80), "Servicios Profesionales y Legales"),
CategoryItem("Escribano Público", "✍️", Color(0xFFF5F5F5), "Servicios Profesionales y Legales"),
CategoryItem("Arquitecto", "📐", Color(0xFFD7CCC8), "Servicios Profesionales y Legales"),
CategoryItem("Ingeniero Civil", "🏗️", Color(0xFFBBDEFB), "Servicios Profesionales y Legales"),
CategoryItem("Gestor del Automotor", "🚗", Color(0xFFFFF3E0), "Servicios Profesionales y Legales"),
CategoryItem("Gestoría General", "📁", Color(0xFFFFE0B2), "Servicios Profesionales y Legales"),
CategoryItem("Traductor Público", "🗣️", Color(0xFFFFF9C4), "Servicios Profesionales y Legales"),
CategoryItem("Despachante de Aduana", "🚢", Color(0xFF90CAF9), "Servicios Profesionales y Legales"),
CategoryItem("Abogado General", "⚖️", Color(0xFFCFD8DC), "Servicios Profesionales y Legales"),
CategoryItem("Abogado Penalista", "⚖️", Color(0xFFB0BEC5), "Servicios Profesionales y Legales"),
CategoryItem("Abogado Civil y Familia", "⚖️", Color(0xFF90CAF9), "Servicios Profesionales y Legales"),
CategoryItem("Abogado Laboral", "⚖️", Color(0xFFF48FB1), "Servicios Profesionales y Legales"),
CategoryItem("Abogado Comercial", "⚖️", Color(0xFFFFCC80), "Servicios Profesionales y Legales"),
CategoryItem("Escribano Público", "✍️", Color(0xFFF5F5F5), "Servicios Profesionales y Legales"),
CategoryItem("Arquitecto", "📐", Color(0xFFD7CCC8), "Servicios Profesionales y Legales"),
CategoryItem("Ingeniero Civil", "🏗️", Color(0xFFBBDEFB), "Servicios Profesionales y Legales"),
// --- Nuevas Ingenierías Agregadas ---
CategoryItem("Ingeniero Mecánico", "⚙️", Color(0xFFE0E0E0), "Servicios Profesionales y Legales"), // Gris claro
CategoryItem("Ingeniero Electrónico", "🔌", Color(0xFF80DEEA), "Servicios Profesionales y Legales"), // Cian pastel
CategoryItem("Ingeniero Eléctrico", "⚡", Color(0xFFFFE082), "Servicios Profesionales y Legales"), // Amarillo pastel
CategoryItem("Ingeniero en Sistemas", "💻", Color(0xFFC5CAE9), "Servicios Profesionales y Legales"), // Índigo pastel
CategoryItem("Ingeniero Agrónomo", "🌱", Color(0xFFA5D6A7), "Servicios Profesionales y Legales"), // Verde pastel
CategoryItem("Ingeniero Químico", "🧪", Color(0xFFC8E6C9), "Servicios Profesionales y Legales"), // Verde claro
CategoryItem("Ingeniero Ambiental", "🌍", Color(0xFFB2DFDB), "Servicios Profesionales y Legales"), // Verde azulado (Teal) pastel
// --- Resto de los servicios ---
CategoryItem("Gestor del Automotor", "🚗", Color(0xFFFFF3E0), "Servicios Profesionales y Legales"),
CategoryItem("Gestoría General", "📁", Color(0xFFFFE0B2), "Servicios Profesionales y Legales"),
CategoryItem("Traductor Público", "🗣️", Color(0xFFFFF9C4), "Servicios Profesionales y Legales"),
CategoryItem("Despachante de Aduana", "🚢", Color(0xFF90CAF9), "Servicios Profesionales y Legales"),

// -----------------------------------------
// 📈 SUPERCATEGORÍA: FINANZAS Y NEGOCIOS
// -----------------------------------------
CategoryItem("Contador Público", "🧾", Color(0xFFE0F7FA), "Finanzas y Negocios"),
CategoryItem("Asesor Financiero", "💹", Color(0xFFC8E6C9), "Finanzas y Negocios"),
CategoryItem("Auditor", "🔎", Color(0xFFB2EBF2), "Finanzas y Negocios"),
CategoryItem("Consultor de Negocios", "💼", Color(0xFFB3E5FC), "Finanzas y Negocios"),
CategoryItem("Analista de Riesgos", "📉", Color(0xFFEF9A9A), "Finanzas y Negocios"),
CategoryItem("Corredor de Bolsa", "📈", Color(0xFF81C784), "Finanzas y Negocios"),
CategoryItem("Actuario", "🧮", Color(0xFFD1C4E9), "Finanzas y Negocios"),
CategoryItem("Recursos Humanos", "👥", Color(0xFFD1C4E9), "Finanzas y Negocios"),
CategoryItem("Corredor de Seguros / Productor", "🛡️", Color(0xFFFFAB91), "Finanzas y Negocios"),
CategoryItem("Agente Inmobiliario / Martillero", "🏢", Color(0xFFBBDEFB), "Finanzas y Negocios"),
CategoryItem("Administrador de Consorcios", "📊", Color(0xFFE0E0E0), "Finanzas y Negocios"),

// -----------------------------------------
// 📱 SUPERCATEGORÍA: MARKETING, DISEÑO Y MEDIOS
// -----------------------------------------
CategoryItem("Marketing Digital", "📱", Color(0xFFE1BEE7), "Marketing, Diseño y Medios"),
CategoryItem("Diseñador Gráfico", "🎨", Color(0xFFF8BBD0), "Marketing, Diseño y Medios"),
CategoryItem("Diseñador UX/UI", "🖥️", Color(0xFF80DEEA), "Marketing, Diseño y Medios"),
CategoryItem("Community Manager", "📱", Color(0xFFCE93D8), "Marketing, Diseño y Medios"),
CategoryItem("Influencer", "🤳", Color(0xFFF48FB1), "Marketing, Diseño y Medios"),
CategoryItem("Streamer", "🎮", Color(0xFFB39DDB), "Marketing, Diseño y Medios"),
CategoryItem("Youtuber / Tiktoker", "▶️", Color(0xFFEF5350), "Marketing, Diseño y Medios"),
CategoryItem("Copywriter / Redactor Freelance", "✍️", Color(0xFFFFF9C4), "Marketing, Diseño y Medios"),
CategoryItem("SEO / SEM Specialist", "🔍", Color(0xFF81D4FA), "Marketing, Diseño y Medios"),
CategoryItem("Trafficker Digital", "🚦", Color(0xFFFFCC80), "Marketing, Diseño y Medios"),
CategoryItem("Relaciones Públicas", "🤝", Color(0xFFBCAAA4), "Marketing, Diseño y Medios"),
CategoryItem("Periodista", "📰", Color(0xFFCFD8DC), "Marketing, Diseño y Medios"),
CategoryItem("Locutor", "🎙️", Color(0xFFB0BEC5), "Marketing, Diseño y Medios"),
CategoryItem("Doblajista", "🗣️", Color(0xFFD7CCC8), "Marketing, Diseño y Medios"),
CategoryItem("Fotógrafo de Productos", "📦", Color(0xFFE0E0E0), "Marketing, Diseño y Medios"),
CategoryItem("Editor de Video", "🎞️", Color(0xFFFFCC80), "Marketing, Diseño y Medios"),

// -----------------------------------------
// 🔬 SUPERCATEGORÍA: CIENCIAS Y HUMANIDADES
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
CategoryItem("Arqueólogo / Antropólogo", "🏺", Color(0xFFA1887F), "Ciencias y Humanidades"),
CategoryItem("Lingüista", "🗣️", Color(0xFFE1BEE7), "Ciencias y Humanidades"),

// -----------------------------------------
// 📚 SUPERCATEGORÍA: EDUCACIÓN Y CLASES
// -----------------------------------------
CategoryItem("Clases Particulares (Apoyo Escolar)", "🎒", Color(0xFFB2EBF2), "Educación y Clases"),
CategoryItem("Profesor de Matemáticas", "➗", Color(0xFFC8E6C9), "Educación y Clases"),
CategoryItem("Profesor de Física / Química", "🧪", Color(0xFFB3E5FC), "Educación y Clases"),
CategoryItem("Profesor de Literatura / Historia", "📚", Color(0xFFD7CCC8), "Educación y Clases"),
CategoryItem("Profesor de Geografía / Biología", "🌍", Color(0xFFC5E1A5), "Educación y Clases"),
CategoryItem("Profesor de Inglés", "🇬🇧", Color(0xFFBBDEFB), "Educación y Clases"),
CategoryItem("Profesor de Francés", "🇫🇷", Color(0xFF90CAF9), "Educación y Clases"),
CategoryItem("Profesor de Alemán", "🇩🇪", Color(0xFFCFD8DC), "Educación y Clases"),
CategoryItem("Profesor de Chino / Japonés", "🌏", Color(0xFFFFCDD2), "Educación y Clases"),
CategoryItem("Profesor de Música", "🎼", Color(0xFFD1C4E9), "Educación y Clases"),
CategoryItem("Clases de Canto", "🎤", Color(0xFFF48FB1), "Educación y Clases"),
CategoryItem("Clases de Guitarra", "🎸", Color(0xFFFFCC80), "Educación y Clases"),
CategoryItem("Clases de Piano", "🎹", Color(0xFFD7CCC8), "Educación y Clases"),
CategoryItem("Clases de Batería", "🥁", Color(0xFFBCAAA4), "Educación y Clases"),
CategoryItem("Clases de Violín", "🎻", Color(0xFFE0E0E0), "Educación y Clases"),
CategoryItem("Taller de Arte / Dibujo", "🎨", Color(0xFFFFF59D), "Educación y Clases"),
CategoryItem("Clases de Cocina", "🧑‍🍳", Color(0xFFFFAB91), "Educación y Clases"),
CategoryItem("Clases de Programación", "💻", Color(0xFFCFD8DC), "Educación y Clases"),
CategoryItem("Cursos de Fotografía", "📸", Color(0xFFB3E5FC), "Educación y Clases"),
CategoryItem("Autoescuela (Manejo)", "🚗", Color(0xFFB0BEC5), "Educación y Clases"),
CategoryItem("Educación Especial", "🧩", Color(0xFFE0F7FA), "Educación y Clases"),

// -----------------------------------------
// 🤝 SUPERCATEGORÍA: CUIDADO Y ASISTENCIA
// -----------------------------------------
CategoryItem("Niñera (Baby Sitter)", "👶", Color(0xFFF8BBD0), "Cuidado y Asistencia"),
CategoryItem("Cuidador de Ancianos", "🧓", Color(0xFFD7CCC8), "Cuidado y Asistencia"),
CategoryItem("Acompañante Terapéutico", "🤝", Color(0xFFC8E6C9), "Cuidado y Asistencia"),

// -----------------------------------------
// 🐾 SUPERCATEGORÍA: MASCOTAS Y VETERINARIA
// -----------------------------------------
CategoryItem("Veterinaria", "🐶", Color(0xFFC8E6C9), "Mascotas y Veterinaria"),
CategoryItem("Peluquería Canina", "🐩", Color(0xFFAED581), "Mascotas y Veterinaria"),
CategoryItem("Paseador de Perros", "🦮", Color(0xFFDCEDC8), "Mascotas y Veterinaria"),
CategoryItem("Guardería de Mascotas", "🏡", Color(0xFFFFF9C4), "Mascotas y Veterinaria"),
CategoryItem("Adiestrador", "🐕‍🦺", Color(0xFFFFE0B2), "Mascotas y Veterinaria"),
CategoryItem("Etólogo Canino", "🧠", Color(0xFFD1C4E9), "Mascotas y Veterinaria"),
CategoryItem("Pet Shop", "🦴", Color(0xFFFFCCBC), "Mascotas y Veterinaria"),
CategoryItem("Acuario / Acuarista", "🐠", Color(0xFF81D4FA), "Mascotas y Veterinaria"),
CategoryItem("Crematorio de Mascotas", "🕊️", Color(0xFFCFD8DC), "Mascotas y Veterinaria"),

// -----------------------------------------
// ✈️ SUPERCATEGORÍA: TURISMO Y HOTELERÍA
// -----------------------------------------
CategoryItem("Guía Turístico", "🗺️", Color(0xFFFFCC80), "Turismo y Hotelería"),
CategoryItem("Agencia de Viajes", "✈️", Color(0xFF81D4FA), "Turismo y Hotelería"),
CategoryItem("Hotel", "🏨", Color(0xFFB39DDB), "Turismo y Hotelería"),
CategoryItem("Hostel", "🛏️", Color(0xFFD7CCC8), "Turismo y Hotelería"),
CategoryItem("Cabañas", "🏕️", Color(0xFFA5D6A7), "Turismo y Hotelería"),
CategoryItem("Traductor Turístico", "🗣️", Color(0xFFFFF59D), "Turismo y Hotelería"),

// -----------------------------------------
// 🚨 SUPERCATEGORÍA: SEGURIDAD Y EMERGENCIAS
// -----------------------------------------
CategoryItem("Seguridad Privada", "🛡️", Color(0xFF9E9E9E), "Seguridad y Emergencias"),
CategoryItem("Seguridad de Eventos", "💂", Color(0xFFB0BEC5), "Seguridad y Emergencias"),
CategoryItem("Bombero", "🚒", Color(0xFFEF5350), "Seguridad y Emergencias"),
CategoryItem("Paramédico", "🚑", Color(0xFFFFCDD2), "Seguridad y Emergencias"),
CategoryItem("Guardaespaldas", "🕴️", Color(0xFF78909C), "Seguridad y Emergencias"),
CategoryItem("Valet Parking", "🚘", Color(0xFFCFD8DC), "Seguridad y Emergencias"),

// -----------------------------------------
// 🌾 SUPERCATEGORÍA: AGRICULTURA Y GANADERÍA
// -----------------------------------------
CategoryItem("Ingeniero Agrónomo", "🌾", Color(0xFFAED581), "Agricultura y Ganadería"),
CategoryItem("Veterinario Rural", "🐄", Color(0xFFC5E1A5), "Agricultura y Ganadería"),
CategoryItem("Operador de Tractor / Cosechadora", "🚜", Color(0xFFFFCC80), "Agricultura y Ganadería"),
CategoryItem("Apicultor", "🐝", Color(0xFFFFF59D), "Agricultura y Ganadería"),
CategoryItem("Vivero", "🪴", Color(0xFF81C784), "Agricultura y Ganadería")
)
}

//**/
