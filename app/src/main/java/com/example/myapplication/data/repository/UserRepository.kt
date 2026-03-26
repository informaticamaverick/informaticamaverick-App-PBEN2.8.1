package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.UserDao
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.local.toEntity
import com.example.myapplication.data.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE USUARIO (UserRepository) ---
 * 
 * Este repositorio actúa como el mediador entre la base de datos local (Room)
 * y la base de datos remota (Firestore). Sincroniza el perfil del dueño de la app.
 * Implementa la lógica para la nueva estructura de UserEntity.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Flujo reactivo del perfil de usuario desde Room.
     */
    val userProfile: Flow<UserEntity?> = userDao.getUser()

    /**
     * Sincroniza una actualización del usuario.
     * Guarda localmente en Room y envía el modelo de dominio completo a Firestore.
     */
    suspend fun updateUser(userEntity: UserEntity) {
        userDao.insertOrUpdateUser(userEntity)
        try {
            val userDomain = userEntity.toDomain()
            firestore.collection("usuarios")
                .document(userEntity.id)
                .set(userDomain, SetOptions.merge())
                .await()
            Log.d("UserRepository", "✓ Sincronización exitosa: ${userEntity.id}")
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error en sincronización: ${e.message}")
        }
    }

    /**
     * Guarda el perfil completo tras edición y asegura la vinculación de credenciales.
     */
    suspend fun saveCompleteProfile(profile: UserEntity, password: String? = null) {
        val currentUser = auth.currentUser ?: throw Exception("Usuario no autenticado")

        // 1. Vinculación opcional de credenciales
        if (!password.isNullOrBlank()) {
            try {
                val email = currentUser.email ?: ""
                val credential = EmailAuthProvider.getCredential(email, password)
                currentUser.linkWithCredential(credential).await()
            } catch (e: Exception) {
                Log.w("UserRepository", "Vinculación omitida: ${e.message}")
            }
        }

        // 2. Persistencia en la nube (Firestore)
        val userDomain = profile.toDomain()
        firestore.collection("usuarios")
            .document(currentUser.uid)
            .set(userDomain, SetOptions.merge())
            .await()

        // 3. Persistencia local inmediata (Room)
        userDao.insertOrUpdateUser(profile)
        
        Log.d("UserRepository", "✓ Perfil del dueño guardado y sincronizado")
    }

    /**
     * Sincroniza los datos desde Firestore hacia Room.
     * Si faltan datos en la nube, utiliza los de Google Auth como respaldo.
     */
    suspend fun refreshUserFromRemote() {
        val currentUser = auth.currentUser ?: return

        try {
            val uid = currentUser.uid
            val doc = firestore.collection("usuarios").document(uid).get().await()

            if (doc.exists()) {
                val remoteUser = doc.toObject(User::class.java)
                if (remoteUser != null) {
                    val entity = remoteUser.toEntity()
                    
                    // --- FALLBACK GOOGLE ---
                    val photoUrl = if (entity.photoUrl.isNullOrBlank()) currentUser.photoUrl?.toString() else entity.photoUrl
                    val displayName = if (entity.displayName.isBlank()) currentUser.displayName ?: "Usuario" else entity.displayName
                    val email = if (entity.email.isBlank()) currentUser.email ?: "" else entity.email

                    val finalEntity = entity.copy(
                        photoUrl = photoUrl,
                        displayName = displayName,
                        email = email
                    )
                    
                    userDao.insertOrUpdateUser(finalEntity)
                    Log.d("UserRepository", "✓ Perfil refrescado desde remoto")
                    return
                }
            }

            // Inicializar si el documento no existe
            initializeNewUserFromGoogle(currentUser)

        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error refrescando perfil: ${e.message}")
        }
    }

    /**
     * Crea un perfil de cliente inicial usando datos de Google.
     */
    private suspend fun initializeNewUserFromGoogle(currentUser: com.google.firebase.auth.FirebaseUser) {
        val uid = currentUser.uid
        val nameFull = currentUser.displayName ?: "Usuario"
        
        val newEntity = UserEntity(
            id = uid,
            email = currentUser.email ?: "",
            displayName = nameFull,
            name = nameFull.split(" ").firstOrNull() ?: "",
            lastName = nameFull.split(" ").drop(1).joinToString(" "),
            photoUrl = currentUser.photoUrl?.toString(),
            isOnline = true,
            createdAt = System.currentTimeMillis()
        )
        
        userDao.insertOrUpdateUser(newEntity)
        
        firestore.collection("usuarios")
            .document(uid)
            .set(newEntity.toDomain(), SetOptions.merge())
            .await()
            
        Log.d("UserRepository", "✓ Nuevo usuario inicializado con Google")
    }

    suspend fun clearLocalUser() {
        userDao.deleteUser()
        auth.signOut()
        Log.d("UserRepository", "✓ Sesión cerrada y caché limpia")
    }
}
