package com.example.myapplication.prestador

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.prestador.ui.navigation.PrestadorNavGraph
import com.example.myapplication.prestador.ui.theme.PrestadorTheme
import com.example.myapplication.prestador.viewmodel.ChatSimulationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // 🎯 ViewModel a nivel de Activity - UNA SOLA instancia para toda la app
    private val chatSimulationViewModel: ChatSimulationViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        println("🏠 MainActivity: ChatSimulationViewModel creado (${chatSimulationViewModel.hashCode()})")
        
        setContent {
            PrestadorTheme {
                val navController = rememberNavController()
                PrestadorNavGraph(
                    navController = navController,
                    chatSimulationViewModel = chatSimulationViewModel  // Pasar a NavGraph
                )
            }
        }
        
        // Manejar intent inicial (cuando la app se abre desde notificación)
        handleNotificationIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Manejar nuevo intent cuando la app ya está abierta
        handleNotificationIntent(intent)
    }
    
    private fun handleNotificationIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra("open_chat", false)) {
                val userId = it.getStringExtra("user_id")
                val userName = it.getStringExtra("user_name")
                println("🔔 Notificación clickeada: userId=$userId, userName=$userName")
                // TODO: Navegar al chat usando navController
                // Por ahora solo logging - necesitaremos pasar el navController aquí
            }
        }
    }
}