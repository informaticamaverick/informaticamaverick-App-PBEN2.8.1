package com.example.myapplication.prestador

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PrestadorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}