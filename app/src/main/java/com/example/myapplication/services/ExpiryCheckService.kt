package com.example.myapplication.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.myapplication.models.InventoryItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpiryCheckService : Service() {

    private val db = FirebaseFirestore.getInstance()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkForExpiringItems()
        return START_NOT_STICKY
    }

    private fun checkForExpiringItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val today = Calendar.getInstance()
            today.add(Calendar.DAY_OF_MONTH, 3) // Check items expiring in 3 days
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val alertDate = sdf.format(today.time)

            db.collection("inventory")
                .get()
                .addOnSuccessListener { snapshot ->
                    for (doc in snapshot.documents) {
                        val item = doc.toObject(InventoryItem::class.java)
                        if (item != null && item.estimatedExpiryDate == alertDate) {
                            sendExpiryNotification(item.name)
                        }
                    }
                }
                .addOnFailureListener { e -> Log.e("ExpiryCheck", "Error checking expiry", e) }
        }
    }

    private fun sendExpiryNotification(foodName: String) {
        val notificationIntent = Intent("com.example.myapplication.EXPIRY_ALERT")
        notificationIntent.putExtra("foodName", foodName)
        sendBroadcast(notificationIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
