package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.UserDao
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.local.toEntity
import com.example.myapplication.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * REPOSITORIO DE USUARIO (UserRepository)
 *
 * Gestiona la sincronización de datos entre Room y Firestore.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IUserRepository {

    override val userProfile: Flow<UserEntity?> = userDao.getUser()

    override suspend fun updateUser(userEntity: UserEntity) {
        userDao.insertOrUpdateUser(userEntity)
        try {
            val userDomain = userEntity.toDomain()
            firestore.collection("users")
                .document(userEntity.id)
                .set(userDomain, SetOptions.merge())
                .await()
            Log.d("UserRepository", "Usuario sincronizado con Firebase: ${userEntity.id}")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error sincronizando con Firebase: ${e.message}")
        }
    }

    override suspend fun refreshUserFromRemote() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("UserRepository", "No hay usuario autenticado al refrescar. Omitiendo actualización.")
            return
        }

        try {
            val uid = currentUser.uid
            Log.d("UserRepository", "Buscando perfil en Firestore para: $uid")

            val documentSnapshot = firestore.collection("users").document(uid).get().await()

            if (documentSnapshot.exists()) {
                val remoteUser = documentSnapshot.toObject(User::class.java)
                if (remoteUser != null) {
                    Log.d("UserRepository", "Perfil descargado de Firebase. Actualizando Room.")
                    userDao.insertOrUpdateUser(remoteUser.toEntity())
                }
            } else {
                 Log.d("UserRepository", "Usuario nuevo. Creando perfil inicial en Firestore.")
                val newUser = User(
                    uid = uid,
                    email = currentUser.email ?: "",
                    displayName = currentUser.displayName ?: "Usuario",
                    name = currentUser.displayName?.split(" ")?.first() ?: "",
                    lastName = currentUser.displayName?.split(" ")?.lastOrNull() ?: "",
                    photoUrl = currentUser.photoUrl?.toString(),
                    createdAt = System.currentTimeMillis()
                )
                firestore.collection("users").document(uid).set(newUser).await()
                userDao.insertOrUpdateUser(newUser.toEntity())
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error en refreshUserFromRemote: ${e.message}")
        }
    }

    override suspend fun clearLocalUser() {
        userDao.deleteUser()
        auth.signOut()
    }
}
