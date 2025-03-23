package com.example.myapplication.models

data class FsisFoodItem(
    val name: String,
    val keywords: List<String>,
    val refrigerateMin: Int? = null,
    val refrigerateMax: Int? = null,
    val refrigerateMetric: String? = null,
    val freezeMin: Int? = null,
    val freezeMax: Int? = null,
    val freezeMetric: String? = null
)
