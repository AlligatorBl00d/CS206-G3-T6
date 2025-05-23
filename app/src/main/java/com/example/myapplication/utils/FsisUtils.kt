package com.example.myapplication.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myapplication.models.FsisFoodItem
import org.json.JSONArray
import java.io.BufferedReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FsisUtils {
    fun loadFsisData(context: Context): List<FsisFoodItem> {
        val jsonString = context.assets.open("clean_fsis_data.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val items = mutableListOf<FsisFoodItem>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val name = obj.optString("name") ?: continue
            val keywords = obj.optString("keywords")?.split(",")?.map { it.trim() } ?: emptyList()
            val refrigerateMin = obj.optDouble("refrigeration_min", -1.0).takeIf { it >= 0 }?.toInt()
            val refrigerateMax = obj.optDouble("refrigeration_max", -1.0).takeIf { it >= 0 }?.toInt()
            val refrigerateMetric = obj.optString("refrigeration_unit")
            val freezeMin = obj.optDouble("freezing_min", -1.0).takeIf { it >= 0 }?.toInt()
            val freezeMax = obj.optDouble("freezing_max", -1.0).takeIf { it >= 0 }?.toInt()
            val freezeMetric = obj.optString("freezing_unit")

            items.add(
                FsisFoodItem(
                    name = name,
                    keywords = keywords,
                    refrigerateMin = refrigerateMin,
                    refrigerateMax = refrigerateMax,
                    refrigerateMetric = refrigerateMetric,
                    freezeMin = freezeMin,
                    freezeMax = freezeMax,
                    freezeMetric = freezeMetric
                )
            )
        }

        return items
    }

    fun findMatch(name: String, data: List<FsisFoodItem>): FsisFoodItem? {
        val lowerName = name.lowercase()

        val candidates = data.flatMap { item ->
            listOf(item.name.lowercase()) + item.keywords.map { it.lowercase() }
        }

        val bestMatch = fuzzyMatch(lowerName, candidates)

        return data.find { item ->
            item.name.equals(bestMatch, ignoreCase = true) ||
                    item.keywords.any { it.equals(bestMatch, ignoreCase = true) }
        }
    }

    fun fuzzyMatch(input: String, candidates: List<String>): String? {
        return candidates.minByOrNull { candidate ->
            levenshteinDistance(input.lowercase(), candidate.lowercase())
        }
    }

    // Simple Levenshtein Distance function (for small-scale use)
    fun levenshteinDistance(lhs: String, rhs: String): Int {
        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1

        val cost = Array(lhsLength) { IntArray(rhsLength) }

        for (i in 0 until lhsLength) cost[i][0] = i
        for (j in 0 until rhsLength) cost[0][j] = j

        for (i in 1 until lhsLength) {
            for (j in 1 until rhsLength) {
                val editCost = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                cost[i][j] = listOf(
                    cost[i - 1][j] + 1,
                    cost[i][j - 1] + 1,
                    cost[i - 1][j - 1] + editCost
                ).minOrNull() ?: 0
            }
        }

        return cost[lhsLength - 1][rhsLength - 1]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun estimateExpiryDate(fsisFoodItem: FsisFoodItem, storageLocation: String, purchaseDate: String): String? {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        val parsedPurchaseDate = try {
            LocalDate.parse(purchaseDate, formatter)
        } catch (e: Exception) {
            return null
        }

        val daysToAdd = when (storageLocation.lowercase()) {
            "fridge", "refrigerator" -> fsisFoodItem.refrigerateMax
            "freezer", "freeze" -> fsisFoodItem.freezeMax
            else -> null
        }

        return daysToAdd?.let {
            parsedPurchaseDate.plusDays(it.toLong()).format(DateTimeFormatter.ISO_DATE)
        }
    }
}
