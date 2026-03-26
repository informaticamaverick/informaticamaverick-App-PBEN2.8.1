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
    val superCategoryIcon: String = "📂",
    // [ARREGLO 2] Usamos List normal para que coincida con tu archivo Converters.kt
    val providerIds: List<String> = emptyList(),

    val imageUrl: String?,
    val isNew: Boolean,
    val isNewPrestador: Boolean,
    val isAd: Boolean
    //val isNewProduct: Boolean
   // val isNewService: Boolean


)