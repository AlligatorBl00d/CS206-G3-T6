package com.example.myapplication.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.viewmodel.InventoryViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


class InventoryScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InventoryScreenContent()
        }
    }
}

@Composable
fun InventoryScreenContent(viewModel: InventoryViewModel = viewModel()) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("📦 Inventory Items", style = MaterialTheme.typography.titleLarge)

        inventoryItems.forEach { item ->
            Text("🛒 ${item.name} - ${item.quantity} pcs - Exp: ${item.estimatedExpiryDate}")
        }

        Button(onClick = {
            val testItem = InventoryItem(
                id = "123",
                name = "Milk",
                category = "Dairy",
                purchaseDate = "2025-03-10",
                estimatedExpiryDate = "2025-03-20",
                quantity = 1,
                storageLocation = "Fridge" // Added missing field

            )
            viewModel.addItem(testItem,
                onSuccess = { println("Item added successfully!") },  // ✅ Success callback
                onFailure = { e -> println("Error adding item: ${e.message}") }  // ✅ Failure callback
            )
        }) {
            Text("➕ Add Test Item")
        }
    }
}
