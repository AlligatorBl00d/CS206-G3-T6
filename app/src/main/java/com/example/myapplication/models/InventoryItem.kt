package com.example.myapplication.models

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val purchaseDate: String = "",
    val estimatedExpiryDate: String = "",
    val quantity: Int = 0,
    val unitSize: String = "", // e.g., "230g"
    val storageLocation: String = "",
    val imageUrl: String = "",

) {
    constructor() : this("", "", "", "", "", 0, "", "")
}