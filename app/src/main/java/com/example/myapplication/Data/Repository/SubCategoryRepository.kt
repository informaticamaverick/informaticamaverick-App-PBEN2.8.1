package com.example.myapplication.Data.Repository

import com.example.myapplication.Data.Model.SubCategory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubCategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val categoriesCollection = firestore.collection("categories")

    // Agregar una subcategoría usando SUBCOLECCIÓN
    suspend fun addSubCategory(categoryId: String, subCategory: SubCategory): Result<String> {
        return try {
            val docRef = categoriesCollection
                .document(categoryId)
                .collection("subcategories")
                .add(subCategory)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Agregar múltiples subcategorías usando SUBCOLECCIÓN
    suspend fun addSubCategories(categoryId: String, subCategories: List<SubCategory>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val subcollectionRef = categoriesCollection
                .document(categoryId)
                .collection("subcategories")
            
            subCategories.forEach { subCategory ->
                val docRef = subcollectionRef.document()
                batch.set(docRef, subCategory.copy(id = docRef.id, categoryId = categoryId))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener subcategorías de una categoría específica usando SUBCOLECCIÓN
    suspend fun getSubCategoriesByCategory(categoryId: String): Result<List<SubCategory>> {
        return try {
            val snapshot = categoriesCollection
                .document(categoryId)
                .collection("subcategories")
                .whereEqualTo("isActive", true)
                .orderBy("order")
                .get()
                .await()
            val subCategories = snapshot.documents.mapNotNull { it.toObject(SubCategory::class.java) }
            Result.success(subCategories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener todas las subcategorías de todas las categorías
    suspend fun getAllSubCategories(): Result<Map<String, List<SubCategory>>> {
        return try {
            val categoriesSnapshot = categoriesCollection.get().await()
            val allSubCategories = mutableMapOf<String, List<SubCategory>>()
            
            for (categoryDoc in categoriesSnapshot.documents) {
                val categoryId = categoryDoc.id
                val subcategoriesSnapshot = categoryDoc.reference
                    .collection("subcategories")
                    .orderBy("order")
                    .get()
                    .await()
                
                val subCategories = subcategoriesSnapshot.documents
                    .mapNotNull { it.toObject(SubCategory::class.java) }
                
                if (subCategories.isNotEmpty()) {
                    allSubCategories[categoryId] = subCategories
                }
            }
            Result.success(allSubCategories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buscar subcategorías por nombre en una categoría específica
    suspend fun searchSubCategories(categoryId: String, query: String): Result<List<SubCategory>> {
        return try {
            val snapshot = categoriesCollection
                .document(categoryId)
                .collection("subcategories")
                .get()
                .await()
            
            val subCategories = snapshot.documents
                .mapNotNull { it.toObject(SubCategory::class.java) }
                .filter { it.name.contains(query, ignoreCase = true) }
            Result.success(subCategories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar una subcategoría
    suspend fun deleteSubCategory(categoryId: String, subCategoryId: String): Result<Unit> {
        return try {
            categoriesCollection
                .document(categoryId)
                .collection("subcategories")
                .document(subCategoryId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Desactivar subcategoría (soft delete)
    suspend fun deactivateSubCategory(categoryId: String, subCategoryId: String): Result<Unit> {
        return try {
            categoriesCollection
                .document(categoryId)
                .collection("subcategories")
                .document(subCategoryId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
