package com.example.myapplication.presentation.client

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.repository.ProviderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * --- FÁBRICA PARA CREAR INSTANCIAS DE PROVIDERVIEWMODEL ---
 */
class ProviderViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProviderViewModel::class.java)) {
            
            // 🔥 [CORRECCIÓN] Se crea un scope para la base de datos
            // Este scope se usa principalmente para la precarga de categorías en AppDatabase.kt
            val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            // 1. Obtener la instancia del DAO desde la base de datos (Pasamos el scope faltante)
            val dao = AppDatabase.getDatabase(context, dbScope).providerDao()

            // 2. Crear el repositorio con el DAO.
            val repository = ProviderRepository(dao)

            // 3. Crear y devolver el ViewModel con el repositorio.
            @Suppress("UNCHECKED_CAST")
            return ProviderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
