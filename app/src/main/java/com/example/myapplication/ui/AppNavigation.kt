package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.foodinventory.ui.theme.ExpiringPageScreen
import com.example.foodinventory.ui.theme.StomachScreen
import com.google.gson.Gson
import java.net.URLDecoder


@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("fridgePage") { FridgeScreen(navController) } // ✅ Ensure FridgeScreen is mapped
        composable("receiptScanner") { ReceiptScannerScreen(navController) }
        composable("expiringPage") { ExpiringPageScreen(navController) }
        composable("stomachPage") { StomachScreen(navController) }

        composable(
            "confirm_receipt/{date}/{items}",
            arguments = listOf(
                navArgument("date") { type = NavType.StringType },
                navArgument("items") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val itemsJson = backStackEntry.arguments?.getString("items") ?: "[]"

            // Decode URI and parse JSON
            val decodedJson = URLDecoder.decode(itemsJson, "UTF-8")
            val itemsList = Gson().fromJson(decodedJson, Array<String>::class.java).toList()

            ConfirmReceiptPage(navController, date, itemsList)
        }

    }
}