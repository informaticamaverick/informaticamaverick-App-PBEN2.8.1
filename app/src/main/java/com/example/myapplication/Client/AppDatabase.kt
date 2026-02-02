package com.example.myapplication.Client

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * BASE DE DATOS DE LA APLICACIÓN
 * 
 * Se añade @TypeConverters para que Room sepa cómo guardar objetos complejos.
 */
@Database(entities = [UserEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona una instancia del DAO para la entidad de usuario.
     * Room generará automáticamente el cuerpo de esta función.
     */
    abstract fun userDao(): UserDao
}
