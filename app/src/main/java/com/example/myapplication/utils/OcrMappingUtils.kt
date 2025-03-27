package com.example.myapplication.utils

import android.content.Context
import org.json.JSONObject

object OcrMappingUtils {
    private var mapping: Map<String, String>? = null

    // Loads the mapping from assets (cached after first load)
    fun loadMapping(context: Context): Map<String, String> {
        if (mapping == null) {
            val jsonString = context.assets.open("ocr_mapping.json")
                .bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(jsonString)
            mapping = jsonObject.keys().asSequence().associateWith { jsonObject.getString(it) }
        }
        return mapping!!
    }

    // Applies the mapping to convert raw OCR names to clean names
    fun applyMapping(rawName: String, context: Context): String {
        val map = loadMapping(context)
        return map.entries.firstOrNull { rawName.uppercase().contains(it.key.uppercase()) }?.value ?: rawName
    }
    fun mapToCleanName(context: Context, rawName: String): String {
        val map = loadMapping(context)
        return map.entries.firstOrNull {
            rawName.uppercase().contains(it.key.uppercase())
        }?.value ?: rawName
    }
}
