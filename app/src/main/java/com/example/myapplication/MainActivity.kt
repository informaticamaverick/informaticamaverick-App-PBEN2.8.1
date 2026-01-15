package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.myapplication.Client.CalendarScreen
import com.example.myapplication.Client.ChatScreen
import com.example.myapplication.Client.ClientDashboardScreen
import com.example.myapplication.Client.ClientProfileScreen
import com.example.myapplication.Login.LoginScreen
import com.example.myapplication.Profile.CompleteProfileScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import com.example.myapplication.Client.CalendarScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita soporte para edge-to-edge y gestos
        setContent {
            MyApplicationTheme {
                // Surface usa el color 'background' definido en tu tema automáticamente
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentUser = Firebase.auth.currentUser
    val startDestination = if (currentUser != null) "dashboard/${currentUser.displayName ?: "Usuario"}" else "login"

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
                        // Si ya tiene perfil, ir directamente al dashboard
                        navController.navigate("dashboard/$userName") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        // Si no tiene perfil, ir a completar perfil
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
                    navController.navigate("dashboard/$userName") {
                        popUpTo("complete_profile/$userName") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "dashboard/{userName}",
            arguments = listOf(
                navArgument("userName") {
                    type = NavType.StringType
                    defaultValue = "Usuario"
                }
            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "Usuario"
            
            ClientDashboardScreen(
                userName = userName,
                location = "Buenos Aires, AR",
                onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate("admin_init")
                },
                onNavigateToProfile = {
                    navController.navigate("client_profile")
                },
                onNavigateToChat = {
                    navController.navigate("chat")
                },
                onNavigateToCalendar = {
                    navController.navigate("calendar")
                }
            )
        }
        
        composable("calendar") {
            CalendarScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("chat") {
            ChatScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("client_profile") {
            ClientProfileScreen(
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