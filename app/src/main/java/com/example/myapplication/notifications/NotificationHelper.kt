package com.example.myapplication.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R

object NotificationHelper {
    private const val CHANNEL_ID = "expiry_channel"
    private const val CHANNEL_NAME = "Expiry Notifications"
    private const val CHANNEL_DESC = "Notifies about expiring food items"
    fun sendExpiryReminder(context: Context, foodName: String, daysLeft: Long) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }
            manager.createNotificationChannel(channel)
        }

        // Intent to open MainActivity and route to expiry page
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigateTo", "expiringPage")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 🛎️ Customize the notification message
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alert Items are Expiring!")
            .setContentText("Tap to check")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()


        manager.notify(foodName.hashCode(), notification)
    }

//    fun sendAddSuccessNotification(context: Context, itemCount: Int) {
//        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        val channelId = "success_channel"
//        val channelName = "Success Notifications"
//
//        // 🚨 MUST BE IMPORTANCE_HIGH for heads-up + banner to show
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                channelName,
//                NotificationManager.IMPORTANCE_HIGH // ✅ Was DEFAULT, now HIGH
//            ).apply {
//                description = "Notifies when items are added"
//            }
//            manager.createNotificationChannel(channel)
//        }
//
//        // ✅ Show debug log
//        Log.d("NotificationDebug", "🎉 Sending add success notification for $itemCount items")
//
//        val notification = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(android.R.drawable.ic_dialog_info) // ✅ System icon for guaranteed visibility
//            .setContentTitle("✅ Items Added")
//            .setContentText("$itemCount item(s) successfully added to your inventory")
//            .setPriority(NotificationCompat.PRIORITY_HIGH) // ✅ Make sure this is here!
//            .setAutoCancel(true)
//            .build()
//
//        manager.notify(1001, notification)
//    }

    fun sendAddSuccessNotification(context: Context, itemCount: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "expiry_channel" // 🔁 use the same working channel temporarily

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Expiry Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 Item Upload")
            .setContentText("Your inventory has been updated.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("✅ You’ve successfully added $itemCount items to your inventory."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        Log.d("NotificationDebug", "🎉 Sending success notification")

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }



    fun sendDeleteSuccessNotification(context: Context, itemName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "delete_channel",
                "Delete Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "delete_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Item Deleted")
            .setContentText("Deleted: $itemName")
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

}
