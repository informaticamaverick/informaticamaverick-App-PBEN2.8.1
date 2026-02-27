package com.example.myapplication.core.di

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.BudgetDao
import com.example.myapplication.data.local.CalendarDao
import com.example.myapplication.data.local.CategoryDao
import com.example.myapplication.data.local.ChatDao
import com.example.myapplication.data.local.ProviderDao
import com.example.myapplication.data.local.UserDao
import com.example.myapplication.data.repository.CategoryRepository
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.ProviderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * MÓDULO DE INYECCIÓN DE DEPENDENCIAS (AppModule)
 * * Este archivo le dice a Hilt cómo crear las instancias de la base de datos y los DAOs.
 * [CORREGIDO] Se actualizó provideChatRepository para incluir BudgetDao.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return AppDatabase.getDatabase(context, dbScope)
    }

    // --- PROVEEDORES DE DAOs ---

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideProviderDao(db: AppDatabase): ProviderDao = db.providerDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    // 🔥 AGREGUÉ @Singleton AQUÍ PARA MEJOR RENDIMIENTO
    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    fun provideCalendarDao(database: AppDatabase): CalendarDao {
        return database.calendarDao()
    }

    // --- PROVEEDORES DE REPOSITORIOS ---

    @Provides
    @Singleton
    fun provideProviderRepository(dao: ProviderDao): ProviderRepository {
        return ProviderRepository(dao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(dao: CategoryDao): CategoryRepository {
        return CategoryRepository(dao)
    }

    // 🔥🔥 AQUÍ ESTABA EL ERROR 🔥🔥
    // Ahora le pedimos a Hilt que nos traiga AMBOS Daos
    @Provides
    @Singleton
    fun provideChatRepository(
        chatDao: ChatDao,
        budgetDao: BudgetDao // <--- NUEVO PARÁMETRO
    ): ChatRepository {
        // Y se los pasamos al constructor del repositorio
        return ChatRepository(chatDao, budgetDao)
    }


}

