package com.mediease.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "medicine_logs",
    foreignKeys = [ForeignKey(
        entity = Medicine::class,
        parentColumns = ["id"],
        childColumns = ["medicineId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medicineId")]
)
data class MedicineLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long = 0,
    val scheduledTime: Long = 0,
    val takenTime: Long? = null,
    val status: String = "PENDING",  // PENDING, TAKEN, MISSED, SKIPPED
    val date: String = "",           // yyyy-MM-dd
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
