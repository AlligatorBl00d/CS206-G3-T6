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

fun extractFoodNameFromFuzzy(raw: String): String {
    val ignoreWords = setOf(
        "F", "FRD", "S", "PREM", "KR", "SG", "B", "FT", "FRZ", "PK", "S/", "BBQ"
    )

    val tokens = raw.uppercase()
        .replace(Regex("[^A-Z0-9\\s]"), " ")
        .split(" ")
        .map { it.trim() }
        .filter { it.isNotBlank() && it !in ignoreWords }

    val cleaned = tokens.joinToString(" ") { token ->
        token.lowercase().replaceFirstChar { it.uppercase() }
    }

    return cleaned.trim()
}

