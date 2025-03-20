package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
}
