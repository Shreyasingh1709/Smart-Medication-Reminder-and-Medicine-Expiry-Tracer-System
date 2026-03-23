package com.mediease.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val name: String = "",
    val dosage: String = "",
    val type: String = "TABLET",
    val frequency: String = "DAILY",
    val repeatDays: String = "1,2,3,4,5,6,7",
    val reminderTimes: String = "",
    val mealTiming: String = "ANYTIME",
    val startDate: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null,
    val imagePath: String? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dosageInfo: String = "", // Added field
    val detectedTime: String = "" // Added field
) {
    fun isExpired(): Boolean = expiryDate != null && expiryDate < System.currentTimeMillis()

    fun isExpiringSoon(daysThreshold: Int = 30): Boolean {
        if (expiryDate == null) return false
        val now = System.currentTimeMillis()
        val thresholdMs = daysThreshold * 86400000L
        return expiryDate > now && (expiryDate - now) <= thresholdMs
    }

    fun daysUntilExpiry(): Int? {
        if (expiryDate == null) return null
        return ((expiryDate - System.currentTimeMillis()) / 86400000L).toInt()
    }

    fun getReminderTimesList(): List<String> =
        if (reminderTimes.isBlank()) emptyList()
        else reminderTimes.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun getRepeatDaysList(): List<Int> =
        if (repeatDays.isBlank()) emptyList()
        else repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }

    fun getTypeEmoji(): String = when (type) {
        "TABLET" -> "💊"; "CAPSULE" -> "💉"; "SYRUP" -> "🍶"
        "INJECTION" -> "💉"; "DROPS" -> "💧"; else -> "🔵"
    }

    fun getMealTimingLabel(): String = when (mealTiming) {
        "BEFORE_MEAL" -> "Before Meal"; "AFTER_MEAL" -> "After Meal"
        "WITH_MEAL" -> "With Meal"; else -> "Any Time"
    }
}
