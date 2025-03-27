package com.example.myapplication.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import com.example.myapplication.models.InventoryItem
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


class InventoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val inventoryCollection = db.collection("inventoryItems")  // Firestore collection

    // 🔹 Add item to Firestore
    suspend fun addItem(item: InventoryItem): Boolean {
        return try {
            val document = if (item.id.isBlank()) inventoryCollection.document() else inventoryCollection.document(item.id)
            val itemWithId = item.copy(id = document.id)  // Ensure it has an ID before saving
            document.set(itemWithId).await()
            Log.d("Firestore", "✅ Item Added: ${itemWithId.name}")
            true
        } catch (e: Exception) {
            Log.e("Firestore", "❌ Error adding item: ${item.name} - ${e.message}", e)
            false
        }
    }

    // 🔹 Get all inventory items (Real-time)
    fun getAllItems(): Flow<List<InventoryItem>> = callbackFlow {
        val listener = inventoryCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("Firestore", "❌ Error fetching inventory items", e)
                trySend(emptyList())
                return@addSnapshotListener
            }

            snapshot?.let {
                val items = it.toObjects(InventoryItem::class.java)

                // ✅ TEMPORARY LOGS FOR DEBUGGING
                Log.d("ExpiryCheck", "📦 Inventory Snapshot Size: ${items.size}")
                items.forEach { item ->
                    Log.d("ExpiryCheck", "Item: ${item.name}, Expiry: ${item.estimatedExpiryDate}")
                }

                trySend(items)
            }
        }

        awaitClose { listener.remove() }
    }


    // 🔹 Get all inventory items (One-time snapshot)
    suspend fun getAllItemsSnapshot(): QuerySnapshot {
        return inventoryCollection.get().await()
    }

    // 🔹 Fetch Expiring Items (Next 3 Days)
    suspend fun getExpiringItems(): List<InventoryItem> {
        val snapshot = inventoryCollection
            .whereLessThanOrEqualTo("estimatedExpiryDate", getThreeDaysLaterDate())
            .get().await()
        return snapshot.toObjects(InventoryItem::class.java)
    }

    private fun getThreeDaysLaterDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 3)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    // 🔹 Delete an inventory item
    suspend fun deleteItem(itemId: String): Boolean {
        return try {
            inventoryCollection.document(itemId).delete().await()
            Log.d("Firestore", "Item Deleted: $itemId")
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Error deleting item: $itemId", e)
            false
        }
    }

    // 🔹 Update an existing inventory item
    suspend fun updateItem(item: InventoryItem): Boolean {
        return try {
            inventoryCollection.document(item.id).set(item).await()
            Log.d("Firestore", "Item Updated: ${item.id}")
            true
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating item: ${item.id}", e)
            false
        }
    }
}
