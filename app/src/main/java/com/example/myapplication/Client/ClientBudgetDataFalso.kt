package com.example.myapplication.Client

import androidx.compose.ui.graphics.Color

// 1. ESTRUCTURA DE DATOS PARA EL PRESUPUESTO
data class PresupuestoFalso(
    val id: String,
    val nombre: String,
    val categoria: String, // "Presupuestos de Licitaciones" o "Presupuestos Generales"
    val servicioCategoria: String, // "Informatica", "Electricidad", "Diseño"
    val precioTotal: Double,
    val fechaRecepcion: String, // "dd/MM/yyyy"

    // Datos del Prestador
    val prestadorId: String,
    val prestadorNombre: String,

    // Datos específicos de Licitaciones
    val esLicitacion: Boolean = false,
    val fechaInicioLicitacion: String? = null, // "dd/MM/yyyy"
    val fechaFinLicitacion: String? = null, // "dd/MM/yyyy"
    val estadoLicitacion: EstadoLicitacion,

    // Datos adicionales
    val imageUrl: String? = null, // URL o URI de la imagen
    val isNew: Boolean = false
)

// 2. ENUM PARA LOS ESTADOS DE LICITACIÓN CON SU COLOR ASOCIADO
enum class EstadoLicitacion(val displayName: String, val color: Color) {
    ACTIVA("Activa", Color(0xFF10B981)),       // Verde
    TERMINADA("Terminada", Color(0xFFEF4444)), // Rojo
    ADJUDICADA("Adjudicada", Color(0xFF3B82F6)),// Celeste
    CANCELADA("Cancelada", Color(0xFF78350F))  // Marrón
}

// 3. BASE DE DATOS FALSA CON DATOS DE EJEMPLO
object ClientBudgetDataFalso {
    // FIX: Se eliminó toda la lógica de fechas dinámicas ('LocalDate.now()', 'DateTimeFormatter').
    // COMENTARIO: Para asegurar la consistencia y previsibilidad de los datos de prueba,
    // todas las fechas ahora son valores de cadena estáticos (hardcoded). Esto elimina
    // cualquier error relacionado con la inicialización de objetos de fecha y hace que
    // el comportamiento del ordenamiento y los filtros sea más fácil de depurar.
    val presupuestos: List<PresupuestoFalso> = listOf(
        // === LICITACIONES (esLicitacion = true) ===
        PresupuestoFalso(
            id = "LIC001",
            nombre = "Renovación Eléctrica Edificio Central",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Electricidad",
            precioTotal = 15000.00,
            fechaRecepcion = "15/08/2024",
            prestadorId = "1",
            prestadorNombre = "Maxi Nanterne",
            esLicitacion = true,
            fechaInicioLicitacion = "05/08/2024",
            fechaFinLicitacion = "25/08/2024",
            estadoLicitacion = EstadoLicitacion.ACTIVA,
            imageUrl = "https://picsum.photos/seed/LIC001/400/300"
        ),
        PresupuestoFalso(
            id = "LIC003",
            nombre = "Diseño de Interiores Oficina Corporativa",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Diseño",
            precioTotal = 25000.00,
            fechaRecepcion = "30/07/2024",
            prestadorId = "4",
            prestadorNombre = "Ana Martinez",
            esLicitacion = true,
            fechaInicioLicitacion = "01/07/2024",
            fechaFinLicitacion = "31/07/2024",
            estadoLicitacion = EstadoLicitacion.TERMINADA,
            imageUrl = "https://picsum.photos/seed/LIC003/400/300"
        ),
        PresupuestoFalso(
            id = "LIC006",
            nombre = "Rediseño Arquitectónico Local Comercial",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Diseño",
            precioTotal = 35000.00,
            fechaRecepcion = "08/08/2024",
            prestadorId = "17",
            prestadorNombre = "Alberto Castillo",
            esLicitacion = true,
            fechaInicioLicitacion = "10/08/2024",
            fechaFinLicitacion = "10/09/2024",
            estadoLicitacion = EstadoLicitacion.ACTIVA,
            imageUrl = "https://picsum.photos/seed/LIC006/400/300"
        ),
        PresupuestoFalso(
            id = "LIC004",
            nombre = "Auditoría de Seguridad Informática",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Informatica",
            precioTotal = 18000.00,
            fechaRecepcion = "02/08/2024",
            prestadorId = "1",
            prestadorNombre = "Maxi Nanterne",
            esLicitacion = true,
            fechaInicioLicitacion = "15/08/2024",
            fechaFinLicitacion = "15/09/2024",
            estadoLicitacion = EstadoLicitacion.ADJUDICADA,
            imageUrl = "https://picsum.photos/seed/LIC004/400/300"
        ),
        PresupuestoFalso(
            id = "LIC005",
            nombre = "Desarrollo e Implementación de E-commerce",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Informatica",
            precioTotal = 50000.00,
            fechaRecepcion = "05/08/2024",
            prestadorId = "9",
            prestadorNombre = "David Moreno",
            esLicitacion = true,
            fechaInicioLicitacion = "10/08/2024",
            fechaFinLicitacion = "30/10/2024",
            estadoLicitacion = EstadoLicitacion.CANCELADA,
            imageUrl = "https://picsum.photos/seed/LIC005/400/300"
        ),
        PresupuestoFalso(
            id = "LICIT-INF-001",
            nombre = "Instalación de Sistema de Videovigilancia",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Informatica",
            precioTotal = 7200.00,
            fechaRecepcion = "19/08/2024",
            prestadorId = "1",
            prestadorNombre = "Maxi Nanterne",
            esLicitacion = true,
            fechaInicioLicitacion = "01/09/2024",
            fechaFinLicitacion = "20/09/2024",
            estadoLicitacion = EstadoLicitacion.ACTIVA,
            imageUrl = "https://picsum.photos/seed/LICIT-INF-001/400/300",
            isNew = true
        ),
        PresupuestoFalso(
            id = "LICIT-INF-002",
            nombre = "Instalación de Sistema de Videovigilancia",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Informatica",
            precioTotal = 6850.50,
            fechaRecepcion = "20/08/2024",
            prestadorId = "9",
            prestadorNombre = "David Moreno",
            esLicitacion = true,
            fechaInicioLicitacion = "01/09/2024",
            fechaFinLicitacion = "20/09/2024",
            estadoLicitacion = EstadoLicitacion.ACTIVA,
            imageUrl = "https://picsum.photos/seed/LICIT-INF-002/400/300",
            isNew = true
        ),
        PresupuestoFalso(
            id = "LICIT-INF-003",
            nombre = "Instalación de Sistema de Videovigilancia",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Informatica",
            precioTotal = 7500.00,
            fechaRecepcion = "18/08/2024",
            prestadorId = "19",
            prestadorNombre = "Sergio Soto",
            esLicitacion = true,
            fechaInicioLicitacion = "01/09/2024",
            fechaFinLicitacion = "20/09/2024",
            estadoLicitacion = EstadoLicitacion.ACTIVA,
            imageUrl = "https://picsum.photos/seed/LICIT-INF-003/400/300"
        ),
        PresupuestoFalso(
            id = "LICIT-ELEC-001",
            nombre = "Actualización de Cableado Eléctrico Oficina",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Electricidad",
            precioTotal = 9500.00,
            fechaRecepcion = "10/07/2024",
            prestadorId = "1",
            prestadorNombre = "Maxi Nanterne",
            esLicitacion = true,
            fechaInicioLicitacion = "20/07/2024",
            fechaFinLicitacion = "10/08/2024",
            estadoLicitacion = EstadoLicitacion.ADJUDICADA,
            imageUrl = "https://picsum.photos/seed/LICIT-ELEC-001/400/300"
        ),
        PresupuestoFalso(
            id = "LICIT-ELEC-002",
            nombre = "Actualización de Cableado Eléctrico Oficina",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Electricidad",
            precioTotal = 9200.00,
            fechaRecepcion = "11/07/2024",
            prestadorId = "15",
            prestadorNombre = "Miguel Ramirez",
            esLicitacion = true,
            fechaInicioLicitacion = "20/07/2024",
            fechaFinLicitacion = "10/08/2024",
            estadoLicitacion = EstadoLicitacion.ADJUDICADA,
            imageUrl = "https://picsum.photos/seed/LICIT-ELEC-002/400/300"
        ),
        PresupuestoFalso(
            id = "LICIT-ELEC-003",
            nombre = "Actualización de Cableado Eléctrico Oficina",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Electricidad",
            precioTotal = 9800.00,
            fechaRecepcion = "12/07/2024",
            prestadorId = "19",
            prestadorNombre = "Sergio Soto",
            esLicitacion = true,
            fechaInicioLicitacion = "20/07/2024",
            fechaFinLicitacion = "10/08/2024",
            estadoLicitacion = EstadoLicitacion.ADJUDICADA,
            imageUrl = "https://picsum.photos/seed/LICIT-ELEC-003/400/300"
        ),
        PresupuestoFalso(
            id = "LICIT-DIS-001",
            nombre = "Branding y Manual de Marca para Nuevo Producto",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Diseño",
            precioTotal = 12000.00,
            fechaRecepcion = "01/06/2024",
            prestadorId = "4",
            prestadorNombre = "Ana Martinez",
            esLicitacion = true,
            fechaInicioLicitacion = "05/06/2024",
            fechaFinLicitacion = "25/06/2024",
            estadoLicitacion = EstadoLicitacion.TERMINADA,
            imageUrl = "https://picsum.photos/seed/LICIT-DIS-001/400/300"
        ),
        PresupuestoFalso(
            id = "LICIT-DIS-002",
            nombre = "Branding y Manual de Marca para Nuevo Producto",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Diseño",
            precioTotal = 11500.00,
            fechaRecepcion = "02/06/2024",
            prestadorId = "13",
            prestadorNombre = "Francisco Santos",
            esLicitacion = true,
            fechaInicioLicitacion = "05/06/2024",
            fechaFinLicitacion = "25/06/2024",
            estadoLicitacion = EstadoLicitacion.TERMINADA,
            imageUrl = "https://picsum.photos/seed/LICIT-DIS-002/400/300"
        ),
        PresupuestoFalso(
            id = "LICIT-DIS-003",
            nombre = "Branding y Manual de Marca para Nuevo Producto",
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Diseño",
            precioTotal = 12800.00,
            fechaRecepcion = "03/06/2024",
            prestadorId = "17",
            prestadorNombre = "Alberto Castillo",
            esLicitacion = true,
            fechaInicioLicitacion = "05/06/2024",
            fechaFinLicitacion = "25/06/2024",
            estadoLicitacion = EstadoLicitacion.TERMINADA,
            imageUrl = "https://picsum.photos/seed/LICIT-DIS-003/400/300"
        ),
        PresupuestoFalso(
            id = "GEN001",
            nombre = "Instalación de Tablero Eléctrico",
            categoria = "Presupuestos Generales",
            servicioCategoria = "Electricidad",
            precioTotal = 950.00,
            fechaRecepcion = "12/08/2024",
            prestadorId = "1",
            prestadorNombre = "Maxi Nanterne",
            esLicitacion = false,
            estadoLicitacion = EstadoLicitacion.ADJUDICADA,
            imageUrl = "https://picsum.photos/seed/GEN001/400/300"
        ),
        PresupuestoFalso(
            id = "GEN002",
            nombre = "Diseño de Logo y Branding para Startup",
            categoria = "Presupuestos Generales",
            servicioCategoria = "Diseño",
            precioTotal = 2500.00,
            fechaRecepcion = "13/08/2024",
            prestadorId = "4",
            prestadorNombre = "Ana Martinez",
            esLicitacion = false,
            estadoLicitacion = EstadoLicitacion.ADJUDICADA,
            imageUrl = "https://picsum.photos/seed/GEN002/400/300"
        ),
        PresupuestoFalso(
            id = "GEN003",
            nombre = "Consultoría de Diseño de Interiores",
            categoria = "Presupuestos Generales",
            servicioCategoria = "Diseño",
            precioTotal = 800.00,
            fechaRecepcion = "14/08/2024",
            prestadorId = "13",
            prestadorNombre = "Francisco Santos",
            esLicitacion = false,
            estadoLicitacion = EstadoLicitacion.ADJUDICADA,
            imageUrl = "https://picsum.photos/seed/GEN003/400/300"
        ),
        PresupuestoFalso(
            id = "GEN004",
            nombre = "Soporte Técnico y Mantenimiento de PCs",
            categoria = "Presupuestos Generales",
            servicioCategoria = "Informatica",
            precioTotal = 1800.00,
            fechaRecepcion = "11/08/2024",
            prestadorId = "1",
            prestadorNombre = "Maxi Nanterne",
            esLicitacion = false,
            estadoLicitacion = EstadoLicitacion.ADJUDICADA,
            imageUrl = "https://picsum.photos/seed/GEN004/400/300"
        ),
        PresupuestoFalso(
            id = "GEN005",
            nombre = "Reparación de Smartphone",
            categoria = "Presupuestos Generales",
            servicioCategoria = "Informatica",
            precioTotal = 400.00,
            fechaRecepcion = "16/08/2024",
            prestadorId = "19",
            prestadorNombre = "Sergio Soto",
            esLicitacion = false,
            estadoLicitacion = EstadoLicitacion.ADJUDICADA,
            imageUrl = "https://picsum.photos/seed/GEN005/400/300"
        )
    )
}
