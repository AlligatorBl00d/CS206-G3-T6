package com.example.myapplication.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getCurrentDate(): String {
        return sdf.format(Date())
    }

    fun addDaysToDate(date: String, days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(date) ?: Date()
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return sdf.format(calendar.time)
    }
}
