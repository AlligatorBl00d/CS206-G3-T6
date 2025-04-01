package com.example.myapplication
import android.os.Bundle
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.data.repository.InventoryRepository
import com.example.myapplication.notifications.NotificationHelper
import com.example.myapplication.ui.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.WorkScheduler
import com.example.myapplication.utils.populateDummyFridgeData
import com.example.myapplication.viewmodel.InventoryViewModel
import com.example.myapplication.viewmodel.InventoryViewModelFactory
import com.example.myapplication.workers.ExpiryCheckWorker
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Schedule daily expiry checks
        WorkScheduler.scheduleDailyExpiryCheck(applicationContext)

        // 2. One-time check on launch for testing
        val request = OneTimeWorkRequestBuilder<ExpiryCheckWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(request)
        // 3. Compose UI
        setContent {
            val context = this
            val navController = rememberNavController()
            val repository = InventoryRepository()
            val viewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(repository))

            // ✅ Request Notification Permission (Android 13+)
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    android.util.Log.d("PermissionCheck", "✅ POST_NOTIFICATIONS permission granted")
                } else {
                    android.util.Log.e("PermissionCheck", "❌ POST_NOTIFICATIONS permission denied")
                    Toast.makeText(
                        context,
                        "Please enable notifications in settings to receive expiry alerts",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            // ✅ Ask for permission on launch
            LaunchedEffect(Unit) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                // ✅ 1. Wait for Firestore to emit inventory items (even empty)
                viewModel.inventoryItems.first { true }

                // ✅ 2. CLEAR database
                viewModel.clearInventory {
                    // ✅ 3. THEN populate dummy data after deletion
                    populateDummyFridgeData(viewModel, context)
                }
                // 2️⃣ Wait for inventory items to load (even if it's empty)
                //snapshotFlow { viewModel.inventoryItems.value }
//                    .first { it.isNotEmpty() }
//                    .let {
//                        populateDummyFridgeData(viewModel, context)
//                    }
                // ✅ Check if this launch came from notification
                val route = intent.getStringExtra("navigateTo")
                if (route == "expiringPage") {
                    navController.navigate("expiringPage")
                }
            }

            // ✅ Always start at Home
            MyApplicationTheme {
                AppNavigation(navController)
            }
        }

    }
}
