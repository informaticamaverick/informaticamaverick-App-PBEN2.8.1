package com.example.myapplication.prestador.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.Address

/**
 * ENTIDAD DE BASE DE DATOS: PROVIDER (PRESTADOR)
 * Esta clase representa una tabla en la base de datos SQLite.
 * Room voncierte automaticamente esta clase kotlin en una tabla SQL
 * @Entity = Anotacion que Indica que esta clase es una tabla
 * tablesName =  "providers" → El nombre de la tabla en la BD será "providers"
 * cada propiedad de la clase = Una COLUMNA en la tabla
 * Los TIPOS DE DATOS Kotlin se mapean automaticamente a tipos SQL
 * Esta tabla guarda la informacion del perfil del prestador
 */

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val address: String? = null,
    val rating: Float = 0f,
    val categories: String = "", //JSON string de lista
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    
    // NUEVOS CAMPOS - Datos personales y profesionales
    val dniCuit: String? = null,
    val profesion: String? = null,
    val tieneMatricula: Boolean = false,
    val matricula: String? = null,
    
    // Ubicación detallada
    val provincia: String? = null,
    val codigoPostal: String? = null,
    val pais: String = "Argentina",
    
    // Configuración de servicios
    val atencionUrgencias: Boolean = false,
    val vaDomicilio: Boolean = false,
    val turnosEnLocal: Boolean = false,
    val tieneEmpresa: Boolean = false,
    val trabajaConOtros: Boolean = false,
    
    // Dirección del local (si turnosEnLocal = true)
    val direccionLocal: String? = null,
    val provinciaLocal: String? = null,
    val codigoPostalLocal: String? = null,
    
    // Datos de empresa (si tieneEmpresa = true)
    val nombreEmpresa: String? = null,
    val cuitEmpresa: String? = null,
    val direccionEmpresa: String? = null,
    
    // Tipo de servicio
    val serviceType: String = "TECHNICAL"  // ServiceType enum as String
)