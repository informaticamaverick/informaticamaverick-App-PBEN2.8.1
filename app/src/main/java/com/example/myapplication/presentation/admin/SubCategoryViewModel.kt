package com.example.myapplication.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.SubCategory
import com.example.myapplication.data.repository.SubCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubCategoryViewModel @Inject constructor(
    private val subCategoryRepository: SubCategoryRepository
) : ViewModel() {

    private val _subCategories = MutableStateFlow<List<SubCategory>>(emptyList())
    val subCategories: StateFlow<List<SubCategory>> = _subCategories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadSubCategoriesByCategory(categoryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            subCategoryRepository.getSubCategoriesByCategory(categoryId)
                .onSuccess { _subCategories.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadAllSubCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            subCategoryRepository.getAllSubCategories()
                .onSuccess {
                    // Convertir el Map a List plana para mostrar todas
                    val allSubCats = it.values.flatten()
                    _subCategories.value = allSubCats
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Función para inicializar subcategorías de ejemplo
    fun initializeSubCategories(mechanicCategoryId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val initialSubCategories = listOf(
                // Subcategorías para Mecánico
                SubCategory(
                    categoryId = mechanicCategoryId,
                    name = "Mecánico de Motor",
                    description = "Especialista en reparación y mantenimiento de motores",
                    iconName = "ic_motor",
                    order = 1
                ),
                SubCategory(
                    categoryId = mechanicCategoryId,
                    name = "Mecánico de Tren Delantero",
                    description = "Especialista en suspensión, dirección y frenos",
                    iconName = "ic_suspension",
                    order = 2
                ),
                SubCategory(
                    categoryId = mechanicCategoryId,
                    name = "Mecánico de Transmisión",
                    description = "Especialista en cajas de cambio y embragues",
                    iconName = "ic_transmission",
                    order = 3
                ),
                SubCategory(
                    categoryId = mechanicCategoryId,
                    name = "Mecánico Eléctrico Automotriz",
                    description = "Especialista en sistemas eléctricos del vehículo",
                    iconName = "ic_electric_car",
                    order = 4
                ),
                SubCategory(
                    categoryId = mechanicCategoryId,
                    name = "Mecánico de Aire Acondicionado",
                    description = "Especialista en climatización automotriz",
                    iconName = "ic_ac",
                    order = 5
                ),
                SubCategory(
                    categoryId = mechanicCategoryId,
                    name = "Mecánico Diesel",
                    description = "Especialista en motores diesel",
                    iconName = "ic_diesel",
                    order = 6
                )
            )

            subCategoryRepository.addSubCategories(mechanicCategoryId, initialSubCategories)
                .onSuccess {
                    _error.value = "Subcategorías de Mecánico inicializadas exitosamente"
                    loadSubCategoriesByCategory(mechanicCategoryId)
                }
                .onFailure { _error.value = "Error al inicializar subcategorías: ${it.message}" }

            _isLoading.value = false
        }
    }

    // Función genérica para agregar subcategorías a cualquier categoría
    fun addSubCategoriesForCategory(
        categoryId: String,
        categoryName: String,
        subCategoryNames: List<String>
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val subCategories = subCategoryNames.mapIndexed { index, name ->
                SubCategory(
                    categoryId = categoryId,
                    name = name,
                    description = "Especialista en $name",
                    iconName = "ic_subcategory",
                    order = index + 1
                )
            }

            subCategoryRepository.addSubCategories(categoryId, subCategories)
                .onSuccess {
                    _error.value = "Subcategorías de $categoryName agregadas exitosamente"
                    loadSubCategoriesByCategory(categoryId)
                }
                .onFailure { _error.value = "Error: ${it.message}" }

            _isLoading.value = false
        }
    }

    fun addSubCategory(categoryId: String, subCategory: SubCategory) {
        viewModelScope.launch {
            subCategoryRepository.addSubCategory(categoryId, subCategory)
                .onSuccess { loadSubCategoriesByCategory(categoryId) }
                .onFailure { _error.value = it.message }
        }
    }
}