package com.example.myapplication.core.di

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.BudgetDao
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

/**
package com.example.myapplication.core.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.BudgetDao
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
 * [ACTUALIZADO] Se añadieron los proveedores para CategoryDao y CategoryRepository.
 * [ACTUALIZADO] Se añadieron los proveedores para ChatDao y ChatRepository.
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

    @Provides
    @Singleton
    fun provideChatRepository(dao: ChatDao): ChatRepository {
        return ChatRepository(dao)
    }

    //* Le dice a Hilt: "Cuando alguien pida un BudgetDao, sácalo de AppDatabase".
    //*/PRESUPUESTOS
    @Provides

    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }
}
**/