package com.example.myapplication.models

data class ScannedItem(
    var name: String,
    var quantity: Int = 1,
    var unitSize: String = "" // ← new field
)
