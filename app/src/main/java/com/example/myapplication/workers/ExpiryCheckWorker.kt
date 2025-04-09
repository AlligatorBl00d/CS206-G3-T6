package com.example.myapplication.workers

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.repository.InventoryRepository
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.notifications.NotificationHelper
import com.google.firebase.firestore.ktx.toObjects
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpiryCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = InventoryRepository()

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        Log.d("ExpiryCheck", "🚀 ExpiryCheckWorker started")

        try {
            val snapshot = repository.getAllItemsSnapshot()
            val items = snapshot.toObjects(InventoryItem::class.java)
            val today = LocalDate.of(2025, 3, 23) // You can change this back to LocalDate.now() after testing

            Log.d("ExpiryCheck", "📦 Inventory Snapshot Size: ${items.size}")
            val formatter = DateTimeFormatter.ISO_DATE

            items.forEach { item ->
                Log.d("ExpiryCheck", "Item: ${item.name}, Expiry: ${item.estimatedExpiryDate}")

                val expiryDate = item.estimatedExpiryDate
                if (expiryDate.isNotBlank()) {
                    try {
                        val parsedExpiry = LocalDate.parse(expiryDate, formatter)

                        if (!parsedExpiry.isBefore(today) &&
                            parsedExpiry.minusDays(3).isBefore(today.plusDays(1))
                        ) {
                            val daysLeft = parsedExpiry.toEpochDay() - today.toEpochDay()
                            Log.d("ExpiryCheck", "🔔 Expiring Soon: ${item.name} in $daysLeft days")
                            NotificationHelper.sendExpiryReminder(applicationContext, item.name, daysLeft)
                        }
                    } catch (e: Exception) {
                        Log.e("ExpiryCheck", "⚠️ Failed to parse expiry date for ${item.name}: $expiryDate", e)
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("ExpiryCheck", "🔥 Failed to fetch items or process expiry", e)
            return Result.failure()
        }
    }
}
