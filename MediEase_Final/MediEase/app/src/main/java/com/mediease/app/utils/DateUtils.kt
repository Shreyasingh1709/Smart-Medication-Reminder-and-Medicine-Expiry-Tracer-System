package com.mediease.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun today(): String = dateFormat.format(Date())

    fun formatDate(millis: Long): String = displayDateFormat.format(Date(millis))

    fun formatDateShort(millis: Long): String = dateFormat.format(Date(millis))

    fun formatFullDate(millis: Long): String = fullDateFormat.format(Date(millis))

    fun formatTime(timeStr: String): String {
        return try {
            val date = timeFormat.parse(timeStr)
            if (date != null) displayTimeFormat.format(date) else timeStr
        } catch (e: Exception) { timeStr }
    }

    fun formatTime(millis: Long): String = displayTimeFormat.format(Date(millis))

    fun parseDate(dateStr: String): Long? {
        return try {
            displayDateFormat.parse(dateStr)?.time
        } catch (e: Exception) {
            try { dateFormat.parse(dateStr)?.time } catch (e2: Exception) { null }
        }
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    fun getDaysAgoDate(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return dateFormat.format(cal.time)
    }

    fun getDatePlusDays(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return dateFormat.format(cal.time)
    }

    fun getScheduledTimeMillis(timeStr: String): Long {
        val cal = Calendar.getInstance()
        val parts = timeStr.split(":")
        if (parts.size == 2) {
            cal.set(Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: 8)
            cal.set(Calendar.MINUTE, parts[1].toIntOrNull() ?: 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getStartOfDay(epochMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(epochMillis: Long): Long = getStartOfDay(epochMillis) + 24 * 60 * 60 * 1000L - 1
}
