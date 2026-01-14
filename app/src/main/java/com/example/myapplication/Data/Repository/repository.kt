package com.example.myapplication.Data.Repository

import com.example.myapplication.Data.local.TokenManager
import com.example.myapplication.Data.Model.LoginResquest
import com.example.myapplication.Data.Model.LoginResponse
import com.example.myapplication.Data.Model.UserRole
import com.example.myapplication.Data.Remote.ApiService
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    /**
     * Intenta loguear al usuario
     * Si tiene éxito, guarda el token automáticamente.
     * @return Result<LoginResponse> para manejar éxito o error.
    */
    suspend fun login(email: String, pass: String, role: UserRole): Result<LoginResponse> {
        return try {
            // 1- Crea el paquete de datos
            val request = LoginResquest(email, pass, role)

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
}
