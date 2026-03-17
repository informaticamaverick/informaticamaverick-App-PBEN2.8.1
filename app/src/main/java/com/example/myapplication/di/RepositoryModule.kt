package com.example.myapplication.di

import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.IAuthRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.data.repository.IUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepository: AuthRepository
    ): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepository: UserRepository
    ): IUserRepository
}

/**
 * RepositoryModule: Módulo Hilt abstracto que vincula implementaciones de repositorios
 * (AuthRepository, UserRepository) con sus interfaces para facilitar testing.
 */
