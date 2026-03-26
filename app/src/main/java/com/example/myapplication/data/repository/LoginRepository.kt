package com.example.myapplication.data.repository

import com.example.myapplication.data.local.TokenManager
import com.example.myapplication.data.model.LoginResquest
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.remote.ApiService
import javax.inject.Inject
import com.example.myapplication.data.local.UserDao
import com.example.myapplication.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val userDao: UserDao // 1. Inyectar el DAO
) {
    /**
     * Intenta loguear al usuario
     * Si tiene éxito, guarda el token automáticamente.
     * @return Result<LoginResponse> para manejar éxito o error.
    */
    suspend fun login(email: String, pass: String): Result<LoginResponse> {
        return try {
            // 1- Crea el paquete de datos
            val request = LoginResquest(email, pass)

            // 2- Llama al servidor
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val loginData = response.body()
                if (loginData != null) {
                    // 3- Si tiene éxito, guarda el token y devuelve los datos
                    tokenManager.saveToken(loginData.token)
                    Result.success(loginData)
                } else {
                    Result.failure(Exception("Error: Respuesta vacía del servidor"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()} Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Funciones para interactuar con la base de datos local ---

    /**
     * Obtiene el flujo de datos del usuario actual desde la base de datos local.
     */
    fun getCurrentUserFlow(): Flow<UserEntity?> = userDao.getUser()

    /**
     * Guarda o actualiza el perfil del usuario en la base de datos local.
     */
    suspend fun saveUserProfile(user: UserEntity) {
        userDao.insertOrUpdateUser(user)
    }

    /**
     * Cierra la sesión del usuario, borrando el token y los datos locales.
     */
    suspend fun logout() {
        //tokenManager.clearToken()
        userDao.deleteUser()
    }
}
