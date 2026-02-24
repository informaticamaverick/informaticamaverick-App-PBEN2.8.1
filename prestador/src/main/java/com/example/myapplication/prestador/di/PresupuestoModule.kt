package com.example.myapplication.prestador.di

import com.example.myapplication.prestador.data.local.dao.ClienteDao
import com.example.myapplication.prestador.data.local.dao.PresupuestoDao
import com.example.myapplication.prestador.data.repository.PresupuestoRepository
import com.example.myapplication.prestador.data.repository.RoomPresupuestoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para inyección de dependencias de Presupuestos
 * 
 * NOTA: La BD y los DAOs se proveen en DatabaseModule
 * Este módulo solo provee el Repository específico de Presupuestos
 */
@Module
@InstallIn(SingletonComponent::class)
object PresupuestoModule {
    
    /**
     * Provee el Repository de Presupuestos
     * Los DAOs son inyectados automáticamente desde DatabaseModule
     */
    @Provides
    @Singleton
    fun providePresupuestoRepository(
        presupuestoDao: PresupuestoDao,
        clienteDao: ClienteDao
    ): PresupuestoRepository {
        return RoomPresupuestoRepository(presupuestoDao, clienteDao)
    }
}
