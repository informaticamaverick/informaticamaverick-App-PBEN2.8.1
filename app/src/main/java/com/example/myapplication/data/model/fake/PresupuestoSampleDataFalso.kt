/**
package com.example.myapplication.data.model.fake

import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.data.local.Provider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

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

    // Lista mutable de presupuestos de ejemplo
    val presupuestos = mutableStateListOf<PresupuestoFalso>().apply {
        // Generar datos basados en los prestadores existentes
        val prestadores = Provider
        
        prestadores.forEachIndexed { index, prestador ->
            val isLicitacion = index % 2 == 0
            val category = if (isLicitacion) "Presupuestos de Licitaciones" else "Presupuestos Generales"
            val service = prestador.services.firstOrNull() ?: "General"
            
            val daysAgo = Random.nextLong(0, 30)
            val fecha = LocalDate.now().minusDays(daysAgo).format(formatter)
            val precio = "$${Random.nextInt(1000, 50000)}"
            
            val nombrePresupuesto = if (isLicitacion) "Licitación para $service" else "Presupuesto de $service"
            
            var inicio: String? = null
            var fin: String? = null
            var status = "Pendiente"

            if (isLicitacion) {
                val daysStart = Random.nextLong(0, 5)
                val daysEnd = Random.nextLong(5, 20)
                inicio = LocalDate.now().minusDays(daysStart).format(formatter)
                fin = LocalDate.now().plusDays(daysEnd).format(formatter)
                
                // Lógica simple para determinar estado
                try {
                    val fechaInicioDate = LocalDate.parse(inicio, formatter)
                    val fechaFinDate = LocalDate.parse(fin, formatter)
                    val ahora = LocalDate.now()
                    status = if (!ahora.isBefore(fechaInicioDate) && !ahora.isAfter(fechaFinDate)) "Activa" else "Finalizada"
                } catch (e: Exception) {
                    status = "Desconocido"
                }
            }

            add(PresupuestoFalso(
                id = "presupuesto_${prestador.id}_$index",
                nombre = nombrePresupuesto,
                fecha = fecha,
                categoria = category,
                servicioCategoria = service,
                empresaId = prestador.id,
                empresaNombre = prestador.companyName ?: "${prestador.name} ${prestador.lastName}",
                empresaImagenUrl = prestador.profileImageUrl,
                precio = precio,
                fechaInicioLicitacion = inicio,
                fechaFinLicitacion = fin,
                status = status
            ))
        }
        
        // Agregar algunos datos manuales específicos para variedad
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
**/