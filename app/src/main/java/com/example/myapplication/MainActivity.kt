package com.example.myapplication

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.Admin.AdminInitScreen
// Se importa la navegación del cliente y se le da un alias para evitar conflictos.
import com.example.myapplication.Client.AppNavigation as ClientAppNavigation
import com.example.myapplication.Client.PerfilUsuarioScreen
import com.example.myapplication.Login.LoginScreen
import com.example.myapplication.Profile.CompleteProfileScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Se llama a la navegación raíz de la aplicación.
                    RootNavigation()
                }
            }
        }
    }
}

/**
 * RootNavigation maneja la navegación principal de la aplicación.
 * Decide si mostrar la pantalla de login, la de completar perfil o la navegación principal del cliente.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootNavigation() {
    val navController = rememberNavController()
    val startDestination = "login"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { hasProfile, userName ->
                    if (hasProfile) {
                        // Si el usuario tiene perfil, navega a la pantalla principal del cliente.
                        navController.navigate("main_screen") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        // Si no, navega a la pantalla para completar el perfil.
                        navController.navigate("complete_profile/$userName") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = "complete_profile/{userName}",
            arguments = listOf(
                navArgument("userName") {
                    type = NavType.StringType
                    defaultValue = "Usuario"
                }
            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "Usuario"

            CompleteProfileScreen(
                userName = userName,
                onProfileComplete = {
                    // Cuando se completa el perfil, navega a la pantalla principal.
                    navController.navigate("main_screen") {
                        popUpTo("complete_profile/$userName") { inclusive = true }
                    }
                }
            )
        }

        // La ruta "main_screen" ahora carga toda la navegación del cliente.
        composable("main_screen") {
            ClientAppNavigation()
        }

        composable("client_profile") {
            PerfilUsuarioScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("admin_init") {
            AdminInitScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
