package com.example.myapplication.data.model.fake

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Modelo de datos para representar un presupuesto
data class PresupuestoFalso(
    val id: String,
    val nombre: String,
    val fecha: String,
    val categoria: String, // "Presupuestos Generales" o "Presupuestos de Licitaciones"
    val servicioCategoria: String, // E.g., "Albañilería", "Electricidad"
    val empresaId: String,
    val empresaNombre: String,
    val empresaImagenUrl: Any?,
    val precio: String,
    val fechaInicioLicitacion: String? = null,
    var fechaFinLicitacion: String? = null,
    val status: String = "Pendiente" // Pendiente, Aceptado, Rechazado, Finalizado
)

object PresupuestoSampleDataFalso {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val presupuestos = mutableStateListOf<PresupuestoFalso>().apply {
        add(PresupuestoFalso(
            id = "p_manual_1",
            nombre = "Reparación de Techo",
            fecha = LocalDate.now().minusDays(2).format(formatter),
            categoria = "Presupuestos Generales",
            servicioCategoria = "Albañilería",
            empresaId = "emp_99",
            empresaNombre = "Techos Seguros S.A.",
            empresaImagenUrl = null,
            precio = "$1,200.00"
        ))
        
        add(PresupuestoFalso(
            id = "p_manual_2",
            nombre = "Instalación Eléctrica Completa",
            fecha = LocalDate.now().minusDays(5).format(formatter),
            categoria = "Presupuestos de Licitaciones",
            servicioCategoria = "Electricidad",
            empresaId = "emp_100",
            empresaNombre = "Electro-Max",
            empresaImagenUrl = null,
            precio = "$7,800.00",
            fechaInicioLicitacion = LocalDate.now().minusDays(10).format(formatter),
            fechaFinLicitacion = LocalDate.now().plusDays(5).format(formatter),
            status = "Activa"
        ))
    }
}
