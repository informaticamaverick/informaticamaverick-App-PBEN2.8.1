package com.example.myapplication.prestador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.prestador.ui.navigation.PrestadorNavGraph
import com.example.myapplication.prestador.ui.theme.PrestadorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrestadorTheme {
                val navController = rememberNavController()
                PrestadorNavGraph(navController = navController)
            }
        }
    }
}