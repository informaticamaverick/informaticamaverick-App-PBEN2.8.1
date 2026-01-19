package com.example.myapplication.Data.Repository

import com.example.myapplication.Data.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()

            val firebaseUser = authResult.user ?: return Result.failure(Exception("Usuario no encontrado"))

            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )

            // NO guardar automáticamente - solo se guardará cuando complete el perfil

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkUserProfileExists(uid: String): Boolean {
        return try {
            android.util.Log.d("AuthRepository", "Verificando perfil para uid: $uid")
            
            val userDoc = firestore.collection("users").document(uid).get().await()
            
            android.util.Log.d("AuthRepository", "Documento existe: ${userDoc.exists()}")
            
            if (!userDoc.exists()) {
                return false
            }
            
            // Verificar si el perfil está completo
            val isProfileComplete = userDoc.getBoolean("isProfileComplete") ?: false
            
            android.util.Log.d("AuthRepository", "isProfileComplete: $isProfileComplete")
            
            // Si no tiene el campo pero sí tiene datos importantes, considerarlo completo
            if (!isProfileComplete) {
                val phoneNumber = userDoc.getString("phoneNumber")
                val address = userDoc.getString("address")
                
                android.util.Log.d("AuthRepository", "phoneNumber: $phoneNumber, address: $address")
                
                // Si tiene teléfono y dirección, el perfil ya está completo
                if (!phoneNumber.isNullOrEmpty() && !address.isNullOrEmpty()) {
                    android.util.Log.d("AuthRepository", "Actualizando isProfileComplete a true")
                    // Actualizar el campo isProfileComplete en Firebase
                    firestore.collection("users").document(uid)
                        .update("isProfileComplete", true)
                        .await()
                    return true
                }
            }
            
            isProfileComplete
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error verificando perfil: ${e.message}")
            false
        }
    }

    // --- CORRECCIÓN AQUÍ ---
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            // Configurar idioma español para el email
            auth.setLanguageCode("es")
            
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString() ?: ""
        )
    }

    fun signOut() {
        auth.signOut()
    }


    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<User>{
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser =
                authResult.user ?: return Result.failure(Exception("Usuario no encontrado"))

            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )

            Result.success(user)
        } catch (e: Exception) {

            Result.failure(e)
        }
    }

}