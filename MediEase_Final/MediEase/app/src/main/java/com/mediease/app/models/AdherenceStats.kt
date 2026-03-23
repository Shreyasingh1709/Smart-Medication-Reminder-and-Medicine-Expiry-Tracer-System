package com.mediease.app.models

data class AdherenceStats(
    val totalScheduled: Int = 0,
    val totalTaken: Int = 0,
    val totalMissed: Int = 0,
    val adherencePercent: Float = 0f,
    val weeklyData: List<DayAdherence> = emptyList(),
    val monthlyData: List<WeekAdherence> = emptyList(),
    val perMedicineStats: List<MedicineAdherence> = emptyList()
)

data class DayAdherence(
    val dayLabel: String = "",
    val date: String = "",
    val scheduled: Int = 0,
    val taken: Int = 0,
    val missed: Int = 0
)

data class WeekAdherence(
    val weekLabel: String = "",
    val scheduled: Int = 0,
    val taken: Int = 0,
    val missed: Int = 0,
    val adherencePercent: Float = 0f
)

data class MedicineAdherence(
    val medicineId: Long = 0,
    val medicineName: String = "",
    val totalScheduled: Int = 0,
    val totalTaken: Int = 0,
    val missedCount: Int = 0,
    val adherencePercent: Float = 0f
)
