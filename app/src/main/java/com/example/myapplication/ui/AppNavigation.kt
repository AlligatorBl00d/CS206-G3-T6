package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.foodinventory.ui.theme.ExpiringPageScreen
import com.example.foodinventory.ui.theme.StomachScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("fridgePage") { FridgeScreen(navController) } // ✅ Ensure FridgeScreen is mapped
        composable("receiptScanner") { ReceiptScannerScreen(navController) }
        composable("expiringPage") { ExpiringPageScreen(navController) }
        composable("stomachPage") { StomachScreen(navController) }
    }
}