package com.mediease.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "PATIENT",    // PATIENT or CAREGIVER
    val dob: Long = 0L,
    val age: Int = 0,
    val ageRange: String = "",
    val wakeUpTime: String = "07:00",
    val bedTime: String = "22:00",
    val breakfastTime: String = "08:00",
    val lunchTime: String = "13:00",
    val dinnerTime: String = "19:30",
    val profileCode: String = "",
    val linkedUserId: String = "",
    val avatarPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
