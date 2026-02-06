package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories_table")
data class CategoryEntity(
    @PrimaryKey
    val name: String,

    val icon: String,

    // [ARREGLO 1] Usamos Long (número) porque borramos el converter de Color.
    // Esto elimina el error "Cannot figure out how to save this field".
    val color: Long,

    val superCategory: String,

    // [ARREGLO 2] Usamos List normal para que coincida con tu archivo Converters.kt
    val providerIds: List<String> = emptyList(),

    val imageUrl: String?,
    val isNew: Boolean,
    val isNewPrestador: Boolean,
    val isAd: Boolean
)


/**
package com.example.myapplication.data.local


// Ubicación: com.example.myapplication.data.local.CategoryEntity.kt
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories_table")
data class CategoryEntity(
    // El nombre es la clave principal. Si Firebase envía un nombre que ya existe,
    // se actualizará esa categoría en lugar de crear una duplicada.
    @PrimaryKey
    val name: String,
    val icon: String,
    val color: Long, // Se usará el Converter para esto
    val superCategory: String,
    val providerIds: MutableList<String>, // Se usará el Converter para esto
    val imageUrl: String?,
    val isNew: Boolean,
    val isNewPrestador: Boolean,
    val isAd: Boolean
)**/