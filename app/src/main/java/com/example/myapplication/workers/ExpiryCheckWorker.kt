package com.example.myapplication.workers

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.repository.InventoryRepository
import com.example.myapplication.notifications.NotificationHelper
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ExpiryCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = InventoryRepository() // Adjust if you're using DI

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val items = repository.getAllItems().first()
        val today = LocalDate.now()

        items.forEach { item ->
            val expiryDate = item.estimatedExpiryDate
            if (expiryDate.isNotBlank()) {
                try {
                    val parsedExpiry = LocalDate.parse(expiryDate)
                    if (!parsedExpiry.isBefore(today) &&
                        parsedExpiry.minusDays(3).isBefore(today.plusDays(1))
                    ) {
                        val daysLeft = parsedExpiry.toEpochDay() - today.toEpochDay()
                        NotificationHelper.sendExpiryReminder(applicationContext, item.name, daysLeft)
                    }
                } catch (_: Exception) { }
            }
        }

        return Result.success()
    }
}
