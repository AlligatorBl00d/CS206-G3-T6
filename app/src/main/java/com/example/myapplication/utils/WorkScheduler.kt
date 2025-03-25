package com.example.myapplication.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.workers.ExpiryCheckWorker
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun scheduleDailyExpiryCheck(context: Context) {
        val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
            1, TimeUnit.DAYS // ⏱️ Set to 1 day; change to Minutes for testing
        )
            .setInitialDelay(1, TimeUnit.MINUTES) // 🧪 Optional for testing
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_expiry_check",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing worker if already scheduled
            request
        )
    }
}