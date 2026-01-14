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
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                return false
            }
            // Verificar si el perfil está completo
            val isProfileComplete = userDoc.getBoolean("isProfileComplete") ?: false
            isProfileComplete
        } catch (e: Exception) {
            false
        }
    }

    // --- CORRECCIÓN AQUÍ ---
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) { // Se corrigió el tipo de excepción
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