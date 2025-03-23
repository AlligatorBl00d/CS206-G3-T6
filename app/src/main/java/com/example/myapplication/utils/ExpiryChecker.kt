package com.example.myapplication.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.notifications.NotificationHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object ExpiryChecker {
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkAndNotify(context: Context, items: List<InventoryItem>) {
        val today = LocalDate.now()

        for (item in items) {
            val expiryDate = try {
                LocalDate.parse(item.estimatedExpiryDate, DateTimeFormatter.ISO_DATE)
            } catch (e: Exception) {
                continue
            }

            val daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate)
            if (daysUntilExpiry in 0..3) {
                NotificationHelper.sendExpiryReminder(
                    context,
                    item.name,
                    daysUntilExpiry
                )
            }
        }
    }
}
