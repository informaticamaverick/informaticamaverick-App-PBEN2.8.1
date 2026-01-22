package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
// ¡AHORA SÍ! SE AÑADE LA IMPORTACIÓN QUE FALTABA
import com.example.myapplication.Client.PerfilPrestadorCliente
import com.example.myapplication.Client.ResultSerchCategoryScreen
import com.example.myapplication.Client.SearchResultsScreen

// Define all the navigation routes in your app
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Budget : Screen("budget")
    object Chats : Screen("chats")
    object Promotions : Screen("promotions")
    object ClientProfile : Screen("client_profile")
    object Bidding : Screen("bidding") 
    object ProviderProfile : Screen("provider_profile/{providerId}") {
        fun createRoute(providerId: String) = "provider_profile/$providerId"
    }
    object SearchResults : Screen("search_results/{categoryName}") {
        fun createRoute(categoryName: String) = "search_results/$categoryName"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreenLayout(
                onNavigateToProfile = { navController.navigate(Screen.ClientProfile.route) },
                onNavigateToBidding = { navController.navigate(Screen.Bidding.route) },
                navController = navController
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Budget.route) {
            PresupuestosScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Chats.route) {
            ChatScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Promotions.route) {
            PromoScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ClientProfile.route) {
            ClientProfileScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Bidding.route) { 
            BiddingScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.ProviderProfile.route,
            arguments = listOf(navArgument("providerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")
            if (providerId != null) {
                PerfilPrestadorCliente(
                    providerId = providerId,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.SearchResults.route,
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName")
            if (categoryName != null) {
                ResultSerchCategoryScreen(
                    categoryName = categoryName,
                    onBack = { navController.popBackStack() },
                    navController = navController
                )
            }
        }
    }
}
