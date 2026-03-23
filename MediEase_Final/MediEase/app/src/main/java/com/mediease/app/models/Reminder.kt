package com.mediease.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long = 0,
    val medicineName: String = "",
    val time: String = "",           // HH:mm
    val repeatDays: String = "1,2,3,4,5,6,7",
    val isEnabled: Boolean = true,
    val alarmRequestCode: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val frequency: String = "", // Added field
    val status: String = "" // Added field
)
