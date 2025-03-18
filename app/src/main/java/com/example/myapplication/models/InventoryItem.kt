package com.example.myapplication.models
data class InventoryItem(
    val id: String,  // Primary constructor parameter
    val name: String,
    val category: String,
    val purchaseDate: String,
    val estimatedExpiryDate: String,
    val quantity: Int
)