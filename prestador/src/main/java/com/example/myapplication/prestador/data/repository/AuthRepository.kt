package com.example.myapplication.prestador.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
    // Obtener usuario actual
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Login con email y contraseña
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login con google
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cerrar sesión
    fun signOut() {
        auth.signOut()
    }

    // Enviar email de recuperacion de contraseña
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si existe perfil del prestador en firestore

    suspend fun checkUserProfileExists(uid: String): Boolean {
        return try {
            android.util.Log.d("PrestadorAuthRepo", "Verificando perfil para uid: $uid")

            val doc = firestore.collection("usuarios").document(uid).get().await()

            android.util.Log.d("PrestadorAuthRepo", "Documento existe: ${doc.exists()}")

            if (!doc.exists()) {
                android.util.Log.d("PrestadorAuthRepos", "El documento no existe")
                return false
            }

            // Verificar si tiene el rol de prestador
            val roles = doc.get("roles") as? List<*>
            val hasPrestadorRole = roles?.contains("prestador") == true

            android.util.Log.d("PrestadorAuthRepo", "Roles encontrados: $roles")
            android.util.Log.d("PrestadorAuthRepo", "¿Tiene rol prestador?: $hasPrestadorRole")

            if (!hasPrestadorRole) {
                android.util.Log.d("PrestadorAuthRepo", "No tiene rol de prestador")
                return false
            }

            // Verificar que tenga los campos específicos del prestador completos
            val servicios = doc.get("servicios") as? List<*>
            val provincia = doc.getString("provincia")

            android.util.Log.d("PrestadorAuthRepo", "Servicios: $servicios, Provincia: $provincia")

            val hasCompleteProfile = !servicios.isNullOrEmpty() && !provincia.isNullOrEmpty()

            android.util.Log.d("PrestadorAuthRepo", "¿Perfil prestador completo?: $hasCompleteProfile")

            hasCompleteProfile
        } catch (e: Exception) {
            android.util.Log.e("PrestadorAuthRepo", "Error verificando perfil: ${e.message}")
            false
        }
    }
}
