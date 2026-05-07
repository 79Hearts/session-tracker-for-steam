package com.steamtimeline.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.steamtimeline.app.ui.home.HomeScreen
import com.steamtimeline.app.ui.settings.SettingsScreen
import com.steamtimeline.app.ui.summary.SummaryScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Summary : Screen("summary")
}

@Composable
fun SteamTimelineNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToSummary = { navController.navigate(Screen.Summary.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Summary.route) {
            SummaryScreen(onBack = { navController.popBackStack() })
        }
    }
}
