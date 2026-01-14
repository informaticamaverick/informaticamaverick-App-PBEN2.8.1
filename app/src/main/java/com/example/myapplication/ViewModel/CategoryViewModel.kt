package com.example.myapplication.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.Data.Model.Category
import com.example.myapplication.Data.Repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            categoryRepository.getAllCategories()
                .onSuccess { _categories.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun searchCategories(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            categoryRepository.searchCategories(query)
                .onSuccess { _categories.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Función para poblar Firebase con categorías iniciales
    fun initializeCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val initialCategories = listOf(
                Category(name = "Electricista", iconName = "ic_electricista", colorHex = "#FBBF24", order = 1),
                Category(name = "Plomero", iconName = "ic_plomero", colorHex = "#06B6D4", order = 2),
                Category(name = "Pintura", iconName = "ic_pintura", colorHex = "#EC4899", order = 3),
                Category(name = "Mudanza", iconName = "ic_mudanza", colorHex = "#10B981", order = 4),
                Category(name = "Limpieza", iconName = "ic_limpieza", colorHex = "#8B5CF6", order = 5),
                Category(name = "Jardín", iconName = "ic_jardin", colorHex = "#84CC16", order = 6),
                Category(name = "Mecánico", iconName = "ic_mecanico", colorHex = "#475569", order = 7),
                Category(name = "Albañilería", iconName = "ic_albanil", colorHex = "#F97316", order = 8),
                Category(name = "Carpintería", iconName = "ic_electricista", colorHex = "#92400E", order = 9),
                Category(name = "Cerrajería", iconName = "ic_plomero", colorHex = "#78350F", order = 10),
                Category(name = "Decoración", iconName = "ic_pintura", colorHex = "#DB2777", order = 11),
                Category(name = "Lavandería", iconName = "ic_limpieza", colorHex = "#0891B2", order = 12),
                Category(name = "Paisajismo", iconName = "ic_jardin", colorHex = "#16A34A", order = 13),
                Category(name = "Reparaciones", iconName = "ic_mecanico", colorHex = "#0369A1", order = 14),
                Category(name = "Transporte", iconName = "ic_mudanza", colorHex = "#059669", order = 15),
                Category(name = "Construcción", iconName = "ic_albanil", colorHex = "#EA580C", order = 16),
                Category(name = "Refrigeración", iconName = "ic_electricista", colorHex = "#0284C7", order = 17),
                Category(name = "Otros", iconName = "ic_otros", colorHex = "#9CA3AF", order = 18)
            )

            categoryRepository.addCategories(initialCategories)
                .onSuccess { 
                    _error.value = "Categorías inicializadas exitosamente"
                    loadCategories()
                }
                .onFailure { _error.value = "Error al inicializar: ${it.message}" }
            
            _isLoading.value = false
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.addCategory(category)
                .onSuccess { loadCategories() }
                .onFailure { _error.value = it.message }
        }
    }

    // Obtener ID de una categoría por nombre
    suspend fun getCategoryIdByName(name: String): String? {
        return categoryRepository.getAllCategories()
            .getOrNull()
            ?.find { it.name.equals(name, ignoreCase = true) }
            ?.id
    }
}
