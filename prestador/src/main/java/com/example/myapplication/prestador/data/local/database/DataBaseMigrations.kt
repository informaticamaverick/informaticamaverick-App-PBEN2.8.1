package com.example.myapplication.prestador.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MIGRACIONES DE LA BASE DE DATOS
 * Cada migración actualiza la BD de una versión a otra sin perder datos
 */
object DatabaseMigrations {

    /**
     * Migración de versión 6 a 7
     * Cambios:
     * - Agregar Foreign Keys a appointments, presupuestos y promotions
     * - Agregar índices para mejorar rendimiento
     */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Como Room no puede alterar tablas con FK fácilmente,
            // necesitamos recrear las tablas afectadas
            
            // 1. RECREAR TABLA APPOINTMENTS con Foreign Keys
            database.execSQL("""
                CREATE TABLE appointments_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    clientId TEXT NOT NULL,
                    clientName TEXT NOT NULL,
                    providerId TEXT NOT NULL,
                    service TEXT NOT NULL,
                    date TEXT NOT NULL,
                    time TEXT NOT NULL,
                    status TEXT NOT NULL,
                    notes TEXT NOT NULL DEFAULT '',
                    proposedBy TEXT NOT NULL DEFAULT 'provider',
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(clientId) REFERENCES clientes(id) ON DELETE CASCADE,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Crear índices para appointments
            database.execSQL("CREATE INDEX index_appointments_clientId ON appointments_new(clientId)")
            database.execSQL("CREATE INDEX index_appointments_providerId ON appointments_new(providerId)")
            database.execSQL("CREATE INDEX index_appointments_providerId_date_time ON appointments_new(providerId, date, time)")
            
            // Copiar datos antiguos
            database.execSQL("""
                INSERT INTO appointments_new 
                SELECT id, clientId, clientName, providerId, service, date, time, status, 
                       notes, proposedBy, createdAt, updatedAt
                FROM appointments
            """.trimIndent())
            
            // Eliminar tabla antigua y renombrar
            database.execSQL("DROP TABLE appointments")
            database.execSQL("ALTER TABLE appointments_new RENAME TO appointments")
            
            
            // 2. RECREAR TABLA PRESUPUESTOS con Foreign Keys
            database.execSQL("""
                CREATE TABLE presupuestos_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    numeroPresupuesto TEXT NOT NULL,
                    clienteId TEXT NOT NULL,
                    prestadorId TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    validezDias INTEGER NOT NULL,
                    subtotal REAL NOT NULL,
                    impuestos REAL NOT NULL,
                    total REAL NOT NULL,
                    estado TEXT NOT NULL,
                    notas TEXT NOT NULL DEFAULT '',
                    itemsJson TEXT NOT NULL DEFAULT '',
                    serviciosJson TEXT NOT NULL DEFAULT '',
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(clienteId) REFERENCES clientes(id) ON DELETE CASCADE,
                    FOREIGN KEY(prestadorId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Crear índices para presupuestos
            database.execSQL("CREATE INDEX index_presupuestos_clienteId ON presupuestos_new(clienteId)")
            database.execSQL("CREATE INDEX index_presupuestos_prestadorId ON presupuestos_new(prestadorId)")
            database.execSQL("CREATE INDEX index_presupuestos_clienteId_estado ON presupuestos_new(clienteId, estado)")
            
            // Copiar datos antiguos
            database.execSQL("""
                INSERT INTO presupuestos_new 
                SELECT id, numeroPresupuesto, clienteId, prestadorId, fecha, validezDias,
                       subtotal, impuestos, total, estado, notas, itemsJson, serviciosJson,
                       createdAt, updatedAt
                FROM presupuestos
            """.trimIndent())
            
            // Eliminar tabla antigua y renombrar
            database.execSQL("DROP TABLE presupuestos")
            database.execSQL("ALTER TABLE presupuestos_new RENAME TO presupuestos")
            
            
            // 3. RECREAR TABLA PROMOTIONS con Foreign Keys
            database.execSQL("""
                CREATE TABLE promotions_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    providerId TEXT NOT NULL,
                    providerName TEXT NOT NULL,
                    providerImageUrl TEXT,
                    type TEXT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    imageUrls TEXT NOT NULL,
                    discount INTEGER,
                    categories TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    expiresAt INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    likes INTEGER NOT NULL DEFAULT 0,
                    views INTEGER NOT NULL DEFAULT 0,
                    rating REAL NOT NULL DEFAULT 0.0,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Crear índices para promotions
            database.execSQL("CREATE INDEX index_promotions_providerId ON promotions_new(providerId)")
            database.execSQL("CREATE INDEX index_promotions_providerId_createdAt ON promotions_new(providerId, createdAt)")
            
            // Copiar datos antiguos
            database.execSQL("""
                INSERT INTO promotions_new 
                SELECT id, providerId, providerName, providerImageUrl, type, title, 
                       description, imageUrls, discount, categories, createdAt, expiresAt,
                       status, likes, views, rating
                FROM promotions
            """.trimIndent())
            
            // Eliminar tabla antigua y renombrar
            database.execSQL("DROP TABLE promotions")
            database.execSQL("ALTER TABLE promotions_new RENAME TO promotions")
        }
    }
    
    /**
     * Migración de versión 7 a 8
     * Cambios:
     * - Remover Foreign Keys de presupuestos (temporalmente)
     * - Esto permite crear presupuestos sin necesidad de tener providerId existente
     */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Recrear tabla presupuestos SIN Foreign Keys
            database.execSQL("""
                CREATE TABLE presupuestos_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    numeroPresupuesto TEXT NOT NULL,
                    clienteId TEXT NOT NULL,
                    prestadorId TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    validezDias INTEGER NOT NULL,
                    subtotal REAL NOT NULL,
                    impuestos REAL NOT NULL,
                    total REAL NOT NULL,
                    estado TEXT NOT NULL,
                    notas TEXT NOT NULL DEFAULT '',
                    itemsJson TEXT NOT NULL DEFAULT '',
                    serviciosJson TEXT NOT NULL DEFAULT '',
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            
            // Crear solo índices (sin FK)
            database.execSQL("CREATE INDEX index_presupuestos_clienteId ON presupuestos_new(clienteId)")
            database.execSQL("CREATE INDEX index_presupuestos_prestadorId ON presupuestos_new(prestadorId)")
            database.execSQL("CREATE INDEX index_presupuestos_clienteId_estado ON presupuestos_new(clienteId, estado)")
            
            // Copiar datos antiguos
            database.execSQL("""
                INSERT INTO presupuestos_new 
                SELECT id, numeroPresupuesto, clienteId, prestadorId, fecha, validezDias,
                       subtotal, impuestos, total, estado, notas, itemsJson, serviciosJson,
                       createdAt, updatedAt
                FROM presupuestos
            """.trimIndent())
            
            // Eliminar tabla antigua y renombrar
            database.execSQL("DROP TABLE presupuestos")
            database.execSQL("ALTER TABLE presupuestos_new RENAME TO presupuestos")
        }
    }
    
    /**
     * Migración de versión 9 a 10
     * Cambios:
     * - Agregar campos profesionales y de configuración a providers
     */
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columnas nuevas a la tabla providers
            database.execSQL("ALTER TABLE providers ADD COLUMN dniCuit TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN profesion TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN matricula TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN provincia TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN codigoPostal TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN pais TEXT NOT NULL DEFAULT 'Argentina'")
            database.execSQL("ALTER TABLE providers ADD COLUMN atencionUrgencias INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN vaDomicilio INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN turnosEnLocal INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN tieneEmpresa INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE providers ADD COLUMN nombreEmpresa TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN cuitEmpresa TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN direccionEmpresa TEXT DEFAULT NULL")
        }
    }
    
    /**
     * Migración de versión 10 a 11
     * Cambios:
     * - Agregar campo tieneMatricula a providers
     */
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columna tieneMatricula
            database.execSQL("ALTER TABLE providers ADD COLUMN tieneMatricula INTEGER NOT NULL DEFAULT 0")
        }
    }
    
    /**
     * Migración de versión 11 a 12
     * Cambios:
     * - Agregar campo trabajaConOtros a providers
     * - Crear tabla empleados para gestionar equipo de trabajo
     */
    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columna trabajaConOtros a providers
            database.execSQL("ALTER TABLE providers ADD COLUMN trabajaConOtros INTEGER NOT NULL DEFAULT 0")
            
            // Crear tabla empleados con el orden exacto de columnas
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS empleados (
                    id TEXT PRIMARY KEY NOT NULL,
                    prestadorId TEXT NOT NULL,
                    nombre TEXT NOT NULL,
                    apellido TEXT NOT NULL,
                    dni TEXT NOT NULL,
                    activo INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            
            // Crear índice para búsquedas por prestadorId
            database.execSQL("CREATE INDEX IF NOT EXISTS index_empleados_prestadorId ON empleados(prestadorId)")
        }
    }
    
    /**
     * Migración de versión 12 a 13
     * Cambios:
     * - Agregar campos para dirección del local (direccionLocal, provinciaLocal, codigoPostalLocal)
     */
    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columnas para dirección del local
            database.execSQL("ALTER TABLE providers ADD COLUMN direccionLocal TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN provinciaLocal TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE providers ADD COLUMN codigoPostalLocal TEXT DEFAULT NULL")
        }
    }
    
    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columna serviceType para tipos de servicio
            database.execSQL("ALTER TABLE providers ADD COLUMN serviceType TEXT NOT NULL DEFAULT 'TECHNICAL'")
        }
    }
    
    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Crear tabla de horarios de disponibilidad para profesionales
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS availability_schedules (
                    id TEXT PRIMARY KEY NOT NULL,
                    providerId TEXT NOT NULL,
                    dayOfWeek INTEGER NOT NULL,
                    startTime TEXT NOT NULL,
                    endTime TEXT NOT NULL,
                    appointmentDuration INTEGER NOT NULL,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """)
            
            // Crear índice para providerId
            database.execSQL("CREATE INDEX IF NOT EXISTS index_availability_schedules_providerId ON availability_schedules(providerId)")
        }
    }
    
    /**
     * Migración 15 → 16: Agregar tabla rental_spaces para alquiler de espacios
     */
    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Crear tabla rental_spaces
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS rental_spaces (
                    id TEXT PRIMARY KEY NOT NULL,
                    providerId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    pricePerHour REAL NOT NULL,
                    blockDuration INTEGER NOT NULL,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Crear índice para providerId
            database.execSQL("CREATE INDEX IF NOT EXISTS index_rental_spaces_providerId ON rental_spaces(providerId)")
        }
    }
    
    /**
     * Migración 16 → 17: Agregar campos a appointments para multi-tipo de servicio
     */
    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Recrear tabla appointments con nuevos campos
            // Es más seguro recrear que usar ALTER TABLE para campos nullable
            database.execSQL("""
                CREATE TABLE appointments_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    clientId TEXT NOT NULL,
                    clientName TEXT NOT NULL,
                    providerId TEXT NOT NULL,
                    service TEXT NOT NULL,
                    date TEXT NOT NULL,
                    time TEXT NOT NULL,
                    duration INTEGER NOT NULL DEFAULT 60,
                    status TEXT NOT NULL,
                    notes TEXT NOT NULL DEFAULT '',
                    proposedBy TEXT NOT NULL DEFAULT 'provider',
                    serviceType TEXT NOT NULL DEFAULT 'TECHNICAL',
                    rentalSpaceId TEXT DEFAULT NULL,
                    scheduleId TEXT DEFAULT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(clientId) REFERENCES clientes(id) ON DELETE CASCADE,
                    FOREIGN KEY(providerId) REFERENCES providers(id) ON DELETE CASCADE,
                    FOREIGN KEY(rentalSpaceId) REFERENCES rental_spaces(id) ON DELETE SET NULL,
                    FOREIGN KEY(scheduleId) REFERENCES availability_schedules(id) ON DELETE SET NULL
                )
            """.trimIndent())
            
            // Crear índices
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_clientId ON appointments_new(clientId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_providerId ON appointments_new(providerId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_providerId_date_time ON appointments_new(providerId, date, time)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_rentalSpaceId ON appointments_new(rentalSpaceId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_scheduleId ON appointments_new(scheduleId)")
            
            // Copiar datos existentes (sin los campos nuevos que tienen defaults)
            database.execSQL("""
                INSERT INTO appointments_new (
                    id, clientId, clientName, providerId, service, date, time,
                    status, notes, proposedBy, createdAt, updatedAt
                )
                SELECT 
                    id, clientId, clientName, providerId, service, date, time,
                    status, notes, proposedBy, createdAt, updatedAt
                FROM appointments
            """.trimIndent())
            
            // Eliminar tabla vieja y renombrar
            database.execSQL("DROP TABLE appointments")
            database.execSQL("ALTER TABLE appointments_new RENAME TO appointments")
        }
    }
    
    /**
     * Migración 17 → 18: Arreglar esquema de sucursales (prestadorId -> businessId)
     */
    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Verificar si la tabla sucursales tiene la columna incorrecta
            // Room puede haber generado un esquema incorrecto en versiones anteriores
            
            // Eliminar índice viejo si existe
            try {
                database.execSQL("DROP INDEX IF EXISTS index_sucursales_prestadorId")
            } catch (e: Exception) {
                // Ignorar si no existe
            }
            
            // Crear índice correcto si no existe
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sucursales_businessId ON sucursales(businessId)")
        }
    }
    
    /**
     * Migración 20 → 21: Agregar campos específicos por tipo de servicio
     */
    val MIGRATION_20_21 = object : Migration(20, 21) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columnas nuevas con valores por defecto
            database.execSQL(
                "ALTER TABLE appointments ADD COLUMN urgencyLevel TEXT DEFAULT NULL"
            )
            database.execSQL(
                "ALTER TABLE appointments ADD COLUMN peopleCount INTEGER DEFAULT NULL"
            )
        }
    }

    /**
     * Lista de todas las migraciones disponibles
     *
     */
    val MIGRATION_21_22 = object : Migration(21, 22) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE appointments ADD COLUMN assignedEmployeeIds TEXT DEFAULT NULL"
            )

        }
    }


    val MIGRATION_22_23 = object : Migration(22, 23) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE presupuestos ADD COLUMN appointmentId TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE presupuestos ADD COLUMN firestoreId TEXT DEFAULT NULL")
            database.execSQL("ALTER TABLE presupuestos ADD COLUMN syncedAt INTEGER DEFAULT NULL")
        }
    }

    /**
     * Lista de todas las migraciones disponibles
     */
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_13_14,
        MIGRATION_14_15,
        MIGRATION_15_16,
        MIGRATION_16_17,
        MIGRATION_17_18,
        MIGRATION_20_21,
        MIGRATION_21_22,
        MIGRATION_22_23
    )
}