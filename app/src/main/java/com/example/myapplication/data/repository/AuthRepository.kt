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
            Log.d("AuthRepository", "Verificando perfil para uid: $uid")

            val userDoc = firestore.collection("usuarios").document(uid).get().await()

            Log.d("AuthRepository", "Documento existe: ${userDoc.exists()}")

            if (!userDoc.exists()) {
                return false
            }

            // Verificar si tiene el rol de cliente

            val roles = userDoc.get("roles")as? List<*>
            val  hasClientRole = roles?.contains("cliente") == true

            Log.d("AuthRepository", "Roles encontrados: $roles")
            Log.d("AuthRepository", "¿Tiene rol cliente?: $hasClientRole")

            if (!hasClientRole) {
                Log.d("AuthRepository", "No tiene rol de cliente")
                return false
            }

            //Verifica si el perfil de cliente esta completo
            val isProfileComplete = userDoc.getBoolean("isProfileComplete") ?: false

            Log.d("AuthRepository", "isProfileComplete: $isProfileComplete")

            //Si no tiene el campo pero si tiene datos importante, considerarlo completo

            if (!isProfileComplete) {
                val phoneNumber = userDoc.getString("phoneNumber")
                val address = userDoc.getString("address")

                Log.d("AuthRepository", "phoneNumber: $phoneNumber, address: $address")

                //si tiene telefono y direccion, el perfil esta completo

                if (!phoneNumber.isNullOrEmpty() && !address.isNullOrEmpty()) {

                    Log.d("AuthRepository", "Actualizado isProfileComplete a true")

                    //Actualizar el campo en Firebase

                    firestore.collection("usuarios").document(uid)
                        .update("isProfileComplete",
                            true)
                        .await()
                    return true
                }
            }


            isProfileComplete
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error verificando perfil: ${e.message}")
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