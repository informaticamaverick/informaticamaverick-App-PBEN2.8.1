package com.example.myapplication.data.repository

import com.example.myapplication.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para UserRepository
 * Esto ayuda a KSP a procesar mejor las dependencias
 */
interface IUserRepository {
    val userProfile: Flow<UserEntity?>
    suspend fun updateUser(userEntity: UserEntity)
    suspend fun refreshUserFromRemote()
    suspend fun clearLocalUser()
}

/**
 * IUserRepository: Interfaz que define contrato del UserRepository para facilitar testing
 * y ayudar a KSP a procesar dependencias correctamente.
 */
