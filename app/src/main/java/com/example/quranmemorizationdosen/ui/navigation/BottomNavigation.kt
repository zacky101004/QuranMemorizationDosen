package com.example.quranmemorizationdosen.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(painterResource(id = android.R.drawable.ic_menu_info_details), contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = currentRoute == "dashboard",
            onClick = { navController.navigate("dashboard") { popUpTo(navController.graph.startDestinationId) } }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = android.R.drawable.ic_menu_view), contentDescription = "Lihat Setoran") },
            label = { Text("Lihat Setoran") },
            selected = currentRoute?.startsWith("lihat_setoran") == true,
            onClick = { /* No direct navigation; depends on mahasiswa selection */ }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = android.R.drawable.ic_menu_edit), contentDescription = "Kelola Setoran") },
            label = { Text("Kelola Setoran") },
            selected = currentRoute == "kelola_setoran",
            onClick = { navController.navigate("kelola_setoran") }
        )
    }
}