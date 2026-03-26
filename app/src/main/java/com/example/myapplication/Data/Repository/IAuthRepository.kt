package com.example.myapplication.data.repository

import com.example.myapplication.data.model.User

/**
 * Interfaz para AuthRepository
 * Esto ayuda a KSP a procesar mejor las dependencias
 */
interface IAuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun checkUserProfileExists(uid: String): Boolean
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun getCurrentUser(): User?
    fun signOut()
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<User>
}

/**
 * IAuthRepository: Interfaz que define operaciones de autenticación (Google, email/password),
 * verificación de perfil, recuperación de contraseña y gestión de sesión.
 */
