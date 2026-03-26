package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.User
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

    /**
     * Inicia sesión con Google. 
     * La sincronización y creación del perfil se delega a UserRepository 
     * para mantener una única fuente de verdad.
     */
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

            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en signInWithGoogle: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun checkUserProfileExists(uid: String): Boolean {
        return try {
            val userDoc = firestore.collection("usuarios").document(uid).get().await()
            if (!userDoc.exists()) return false

            val roles = userDoc.get("roles") as? List<*>
            val hasClientRole = roles?.contains("cliente") == true
            if (!hasClientRole) return false

            val isProfileComplete = userDoc.getBoolean("isProfileComplete") ?: false
            if (!isProfileComplete) {
                val phoneNumber = userDoc.getString("phoneNumber")
                val address = userDoc.getString("address")
                if (!phoneNumber.isNullOrEmpty() && !address.isNullOrEmpty()) {
                    firestore.collection("usuarios").document(uid).update("isProfileComplete", true).await()
                    return true
                }
            }
            isProfileComplete
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error verificando perfil: ${e.message}")
            false
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
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
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Usuario no encontrado"))
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
