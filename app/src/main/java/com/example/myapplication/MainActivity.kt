package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.data.repository.InventoryRepository
import com.example.myapplication.ui.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.example.myapplication.utils.WorkScheduler
class MainActivity : ComponentActivity() {

    private val inventoryRepository = InventoryRepository()  // Firestore repo instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WorkScheduler.scheduleDailyExpiryCheck(applicationContext)
        // 🔹 Firestore Operations in Background
        CoroutineScope(Dispatchers.IO).launch {
            testFirestoreOperations()
        }

        // 🔹 Jetpack Compose UI
        setContent {
            val navController = rememberNavController() // ✅ Define NavController
            MyApplicationTheme {
                AppNavigation(navController) // ✅ Use AppNavigation for navigation
            }
        }
    }

    // 🔹 TEST Firestore Read/Write/Delete
    private suspend fun testFirestoreOperations() {
        try {
            // 1️⃣ Create a test inventory item
            val testItem = InventoryItem(
                id = "test_001",
                name = "Test Chicken",
                category = "Meat",
                purchaseDate = "2025-03-10",
                estimatedExpiryDate = "2025-03-15",
                quantity = 2,
                storageLocation = "Freezer"
            )

            // 2️⃣ Add item to Firestore
            val addSuccess = inventoryRepository.addItem(testItem)
            Log.d("FirestoreTest", "✅ Add Item Success: $addSuccess")

            if (!addSuccess) {
                Log.e("FirestoreTest", "❌ Failed to add item!")
                return
            }

            // ⏳ **Increase delay to 4 seconds for Firestore sync**
            delay(4000)

            // 3️⃣ Fetch all items (LOG SNAPSHOT SIZE BEFORE PARSING)
            val snapshot = inventoryRepository.getAllItemsSnapshot()
            Log.d("FirestoreTest", "📂 Firestore Snapshot Size: ${snapshot.size()}")

            val items = snapshot.toObjects(InventoryItem::class.java)
            Log.d("FirestoreTest", "📦 Retrieved ${items.size} items: $items")

            if (items.isNotEmpty()) {
                // 4️⃣ Delete the test item **only if found**
                val deleteSuccess = inventoryRepository.deleteItem("test_001")
                Log.d("FirestoreTest", "🗑 Delete Item Success: $deleteSuccess")
            } else {
                Log.e("FirestoreTest", "⚠️ Item not found in Firestore!")
            }

        } catch (e: Exception) {
            Log.e("FirestoreTest", "🔥 Error in Firestore test", e)
        }
    }
}