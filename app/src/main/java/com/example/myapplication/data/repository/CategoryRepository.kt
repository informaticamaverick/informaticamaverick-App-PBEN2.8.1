package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

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
}