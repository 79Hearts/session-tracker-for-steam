package com.steamtimeline.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.steamtimeline.app.ui.gamehistory.GameHistoryScreen
import com.steamtimeline.app.ui.home.HomeScreen
import com.steamtimeline.app.ui.settings.SettingsScreen
import com.steamtimeline.app.ui.summary.SummaryScreen
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Summary : Screen("summary")
    object GameHistory : Screen("game_history/{gameName}") {
        fun createRoute(gameName: String) =
            "game_history/${URLEncoder.encode(gameName, "UTF-8")}"
    }
}

@Composable
fun SteamTimelineNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToSummary = { navController.navigate(Screen.Summary.route) },
                onNavigateToGameHistory = { gameName ->
                    navController.navigate(Screen.GameHistory.createRoute(gameName))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Summary.route) {
            SummaryScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.GameHistory.route,
            arguments = listOf(navArgument("gameName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("gameName") ?: ""
            val gameName = URLDecoder.decode(encoded, "UTF-8")
            GameHistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
