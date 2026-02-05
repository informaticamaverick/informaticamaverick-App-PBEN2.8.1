package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.local.Converters
import com.example.myapplication.data.local.UserDao
import com.example.myapplication.data.local.UserEntity

/**
 * BASE DE DATOS DE LA APLICACIÓN
 *
 * Se añade @TypeConverters para que Room sepa cómo guardar objetos complejos.
 * 🔥 IMPORTANTE: Si cambias el esquema (UserEntity), incrementa la versión aquí.
 */
@Database(entities = [UserEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona una instancia del DAO para la entidad de usuario.
     * Room generará automáticamente el cuerpo de esta función.
     */
    abstract fun userDao(): UserDao
}
