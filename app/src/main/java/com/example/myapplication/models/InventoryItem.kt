package com.example.myapplication.models

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val purchaseDate: String = "",
    val estimatedExpiryDate: String = "",
    val quantity: Int = 0,
    val storageLocation: String = "",
    val imageUrl: String = "",
    val daysLeft: Int = 0,
) {
    constructor() : this("", "", "", "", "", 0, "", "", 0)
}