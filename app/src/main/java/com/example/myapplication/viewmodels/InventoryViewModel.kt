package com.example.myapplication.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.data.repository.InventoryRepository
import com.example.myapplication.utils.FsisUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.myapplication.notifications.NotificationHelper


class InventoryViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems

    init {
        fetchInventoryItems()
    }

    // 🔹 Fetch inventory items from Firestore
    private fun fetchInventoryItems() {
        viewModelScope.launch {
            repository.getAllItems().collect { items ->
                _inventoryItems.value = items
            }
        }
    }

    // 🔹 Add a new item to Firestore
    fun addItem(item: InventoryItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            val success = repository.addItem(item)
            if (success) {
                fetchInventoryItems()
                onSuccess()
            } else {
                onFailure(Exception("Failed to add item"))
            }
        }
    }

    // 🔹 Delete an item
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            val success = repository.deleteItem(itemId)
            if (success) fetchInventoryItems()
        }
    }

    // 🔹 Update an item
    fun updateItem(item: InventoryItem) {
        viewModelScope.launch {
            val success = repository.updateItem(item)
            if (success) fetchInventoryItems()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun checkForExpiringItems(context: Context) {
        viewModelScope.launch {
            val items = inventoryItems.value
            val today = java.time.LocalDate.now()

            items.forEach { item ->
                val expiryDate = item.estimatedExpiryDate
                if (expiryDate.isNotBlank()) {
                    try {
                        val parsedExpiry = java.time.LocalDate.parse(expiryDate)

                        // Check if today is within 3 days before expiry
                        if (!parsedExpiry.isBefore(today) &&
                            parsedExpiry.minusDays(3).isBefore(today.plusDays(1))
                        ) {
                            // 🔔 Send local notification
                            val daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, parsedExpiry)
                            NotificationHelper.sendExpiryReminder(context, item.name, daysLeft)
                        }
                    } catch (e: Exception) {
                        Log.e("ExpiryCheck", "Invalid date for item ${item.name}: $expiryDate")
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addItemWithFsisLookup(
        context: Context,
        itemName: String,
        category: String,
        quantity: Int,
        storageLocation: String,
        purchaseDate: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            val fsisData = FsisUtils.loadFsisData(context)
            val match = FsisUtils.findMatch(itemName, fsisData)

            val estimatedExpiry = match?.let {
                FsisUtils.estimateExpiryDate(it, storageLocation)
            } ?: ""

            val newItem = InventoryItem(
                name = itemName,
                category = category,
                quantity = quantity,
                storageLocation = storageLocation,
                purchaseDate = purchaseDate,
                estimatedExpiryDate = estimatedExpiry
            )

            val success = repository.addItem(newItem)
            if (success) {
                fetchInventoryItems()
                onSuccess()
            } else {
                onFailure(Exception("Failed to add item"))
            }
        }
    }
}
