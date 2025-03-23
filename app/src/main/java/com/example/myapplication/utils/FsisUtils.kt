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

//        """Parses a nested JSON array structure (masterArray.getJSONArray(i))
//
//            Flattens each individual item’s key-value pairs into a Map<String, String?>
//
//            Extracts and processes the important fields (name, keywords, expiry durations)
//
//            Builds a List<FsisFoodItem> for easy searching
//
//            Provides a findMatch() utility that looks for a match by:
//
//            Checking if the food name contains the search string
//
//            OR if any of the keywords match the input """
        val jsonString = context.assets.open("fsis_data.json").bufferedReader().use { it.readText() }
        val masterArray = JSONArray(jsonString)
        val items = mutableListOf<FsisFoodItem>()

        for (i in 0 until masterArray.length()) {
            val rawItemArray = masterArray.getJSONArray(i)

            val itemMap = mutableMapOf<String, String?>()

            for (j in 0 until rawItemArray.length()) {
                val fieldObject = rawItemArray.getJSONObject(j)
                val key = fieldObject.keys().next()
                val value = fieldObject.opt(key)?.toString()
                itemMap[key] = value
            }

            val name = itemMap["Name"] ?: continue
            val keywords = itemMap["Keywords"]?.split(",")?.map { it.trim() } ?: emptyList()
            val refrigerateMin = itemMap["Refrigerate_Min"]?.toDoubleOrNull()?.toInt()
            val refrigerateMax = itemMap["Refrigerate_Max"]?.toDoubleOrNull()?.toInt()
            val refrigerateMetric = itemMap["Refrigerate_Metric"]
            val freezeMin = itemMap["Freeze_Min"]?.toDoubleOrNull()?.toInt()
            val freezeMax = itemMap["Freeze_Max"]?.toDoubleOrNull()?.toInt()
            val freezeMetric = itemMap["Freeze_Metric"]

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
        val allNames = data.flatMap { listOf(it.name) + it.keywords }

        val bestMatch = fuzzyMatch(lowerName, allNames)
        return data.find { it.name.equals(bestMatch, true) || it.keywords.contains(bestMatch) }
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
    fun estimateExpiryDate(fsisFoodItem: FsisFoodItem, storageLocation: String): String? {
        val today = LocalDate.now()

        val daysToAdd = when (storageLocation.lowercase()) {
            "fridge", "refrigerator" -> fsisFoodItem.refrigerateMax
            "freezer", "freeze" -> fsisFoodItem.freezeMax
            else -> null
        }

        return daysToAdd?.let {
            today.plusDays(it.toLong()).format(DateTimeFormatter.ISO_DATE)
        }
    }
}
