package com.example.myapplication.prestador.di

import com.example.myapplication.prestador.data.local.dao.NotificacionDao
import com.example.myapplication.prestador.data.repository.NotificacionRepository
import com.example.myapplication.prestador.data.repository.RoomNotificacionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificacionModule {

    @Provides
    @Singleton
    fun provideNotificacionRepository(
        notificacionDao: NotificacionDao
    ): NotificacionRepository {
        return RoomNotificacionRepository(notificacionDao)
    }
}