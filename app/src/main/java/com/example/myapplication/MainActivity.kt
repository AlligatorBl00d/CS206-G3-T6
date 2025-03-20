//package com.example.myapplication
//
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.myapplication.ui.theme.MyApplicationTheme
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.messaging.FirebaseMessaging
//
////class MainActivity : ComponentActivity() {
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        enableEdgeToEdge()
////        setContent {
////            MyApplicationTheme {
////                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
////                    Greeting(
////                        name = "Android",
////                        modifier = Modifier.padding(innerPadding)
////                    )
////                }
////            }
////        }
////    }
////}
////
////@Composable
////fun Greeting(name: String, modifier: Modifier = Modifier) {
////    Text(
////        text = "Hello $name!",
////        modifier = modifier
////    )
////}
////
////@Preview(showBackground = true)
////@Composable
////fun GreetingPreview() {
////    MyApplicationTheme {
////        Greeting("Android")
////    }
////}
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            MyApplicationTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//
//        // 🔥 Run Firebase Tests
//        testFirestore()
//        testAuth()
//        testFCM()
//    }
//
//    // 📌 Firestore Test
//    private fun testFirestore() {
//        val db = FirebaseFirestore.getInstance()
//        val testData = hashMapOf("key" to "Hello Firebase!")
//
//        db.collection("testCollection").document("testDoc")
//            .set(testData)
//            .addOnSuccessListener { Log.d("FirebaseTest", "Data successfully written!") }
//            .addOnFailureListener { e -> Log.w("FirebaseTest", "Error writing document", e) }
//    }
//
//    // 📌 Firebase Authentication Test
//    private fun testAuth() {
//        val auth = FirebaseAuth.getInstance()
//
//        auth.createUserWithEmailAndPassword("testuser@email.com", "testpassword123")
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Log.d("FirebaseAuth", "User created: ${task.result?.user?.uid}")
//                } else {
//                    Log.e("FirebaseAuth", "Error: ${task.exception?.message}")
//                }
//            }
//    }
//
//    // 📌 Firebase Cloud Messaging (FCM) Test
//    private fun testFCM() {
//        FirebaseMessaging.getInstance().token
//            .addOnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Log.w("FCM", "Fetching FCM token failed", task.exception)
//                    return@addOnCompleteListener
//                }
//                val token = task.result
//                Log.d("FCM", "FCM Token: $token")
//            }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MyApplicationTheme {
//        Greeting("Android")
//    }
//}

package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.data.repository.InventoryRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {

    private val inventoryRepository = InventoryRepository()  // Firestore repo instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 TESTING FIRESTORE OPERATIONS
        CoroutineScope(Dispatchers.IO).launch {
            testFirestoreOperations()
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
