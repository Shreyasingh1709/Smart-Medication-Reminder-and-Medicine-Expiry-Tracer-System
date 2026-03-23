package com.mediease.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val email: String,
    val password: String,
    val bedTime: String,
    val wakeTime: String,
    val breakfastTime: String,
    val lunchTime: String,
    val dinnerTime: String,
    val profileCode: String
)
