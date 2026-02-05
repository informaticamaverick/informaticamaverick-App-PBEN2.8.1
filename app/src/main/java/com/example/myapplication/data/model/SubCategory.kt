package com.example.myapplication.data.model

data class SubCategory(
    val id: String = "",
    val categoryId: String = "", // ID de la categoría padre
    val name: String = "",
    val description: String = "",
    val iconName: String = "",
    val order: Int = 0,
    val isActive: Boolean = true
)