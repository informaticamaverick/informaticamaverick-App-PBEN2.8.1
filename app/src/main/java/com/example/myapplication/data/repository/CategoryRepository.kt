package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.example.myapplication.data.local.CategoryDao
import com.example.myapplication.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow


// Asumo que usas Hilt para la inyección de dependencias, por eso el @Inject
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    // La UI observará este Flow. La fuente de verdad ÚNICA es Room.
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    // Esta función se llamará desde un ViewModel o Worker para sincronizar datos.
    suspend fun syncWithFirebase() {
        // 🔥 AQUÍ ESCRIBIRÁS EL CÓDIGO DE FIREBASE 🔥
        // Ejemplo conceptual:
        // 1. val firebaseData = firestore.collection("categories").get().await()
        // 2. firebaseData.documents.forEach { document ->
        //      val categoryEntity = document.toObject(CategoryEntity::class.java)
        //      if (categoryEntity != null) {
        //          // Esto insertará si es nuevo, o actualizará si el nombre ya existe.
        //          categoryDao.insertOrUpdate(categoryEntity)
        //      }
        //    }
    }

    // Nota: No hay métodos para borrar. Se cumple el requisito.
}





/**
@Singleton
class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val categoriesCollection = firestore.collection("categories")

    // Agregar una categoría
    suspend fun addCategory(category: Category): Result<String> {
        return try {
            val docRef = categoriesCollection.add(category).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Agregar múltiples categorías
    suspend fun addCategories(categories: List<Category>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            categories.forEach { category ->
                val docRef = categoriesCollection.document()
                batch.set(docRef, category.copy(id = docRef.id))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener todas las categorías
    suspend fun getAllCategories(): Result<List<Category>> {
        return try {
            val snapshot = categoriesCollection
                .orderBy("order")
                .get()
                .await()
            val categories = snapshot.documents.mapNotNull { it.toObject(Category::class.java) }
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buscar categorías por nombre
    suspend fun searchCategories(query: String): Result<List<Category>> {
        return try {
            val snapshot = categoriesCollection.get().await()
            val categories = snapshot.documents
                .mapNotNull { it.toObject(Category::class.java) }
                .filter { it.name.contains(query, ignoreCase = true) }
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar una categoría
    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            categoriesCollection.document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}**/