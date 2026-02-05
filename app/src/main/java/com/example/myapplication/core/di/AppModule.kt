package com.example.myapplication.core.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * MÓDULO DE INYECCIÓN DE DEPENDENCIAS (AppModule)
 *
 * Configura cómo Hilt debe crear las instancias de la Base de Datos y los DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provee la instancia de la base de datos de Room.
     * Se usa .fallbackToDestructiveMigration() para evitar errores al cambiar el esquema en desarrollo.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
        .fallbackToDestructiveMigration() // 🔥 Sincronización: Borra y recrea la BD si el esquema cambia
        .build()
    }

    /**
     * Provee el DAO de Usuario para realizar operaciones en la base de datos.
     */
    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao {
        return db.userDao()
    }
}