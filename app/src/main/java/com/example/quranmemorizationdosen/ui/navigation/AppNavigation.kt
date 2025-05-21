package com.example.quranmemorizationdosen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quranmemorizationdosen.ui.screens.DashboardScreen
import com.example.quranmemorizationdosen.ui.screens.KelolaSetoranScreen
import com.example.quranmemorizationdosen.ui.screens.LihatSetoranScreen
import com.example.quranmemorizationdosen.ui.screens.LoginScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = { navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } })
        }
        composable("dashboard") {
            DashboardScreen(navController = navController, onLogout = { navController.navigate("login") { popUpTo("dashboard") { inclusive = true } } })
        }
        composable("lihat_setoran/{nim}") { backStackEntry ->
            val nim = backStackEntry.arguments?.getString("nim") ?: ""
            LihatSetoranScreen(navController = navController, nim = nim)
        }
        composable("kelola_setoran") {
            KelolaSetoranScreen(navController = navController)
        }
    }
}