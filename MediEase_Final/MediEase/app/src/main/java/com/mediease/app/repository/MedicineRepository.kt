package com.mediease.app.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.mediease.app.database.AppDatabase
import com.mediease.app.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MedicineRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val medicineDao = db.medicineDao()
    private val logDao = db.medicineLogDao()
    private val userDao = db.userDao()
    private val reminderDao = db.reminderDao()

    // ── Medicine CRUD ────────────────────────────────────
    fun getMedicinesForUser(userId: String): LiveData<List<Medicine>> =
        medicineDao.getMedicinesForUser(userId)

    fun getMedicinesWithExpiry(userId: String): LiveData<List<Medicine>> =
        medicineDao.getMedicinesWithExpiry(userId)

    fun getMedicineByIdLive(id: Long): LiveData<Medicine?> =
        medicineDao.getMedicineByIdLive(id)

    suspend fun getMedicineById(id: Long): Medicine? =
        medicineDao.getMedicineById(id)

    suspend fun insertMedicine(medicine: Medicine): Long =
        medicineDao.insertMedicine(medicine)

    suspend fun updateMedicine(medicine: Medicine) =
        medicineDao.updateMedicine(medicine)

    suspend fun deleteMedicine(medicine: Medicine) =
        medicineDao.deactivateMedicine(medicine.id)

    suspend fun getMedicinesForUserSync(userId: String): List<Medicine> =
        medicineDao.getMedicinesForUserSync(userId)

    // ── Logs ─────────────────────────────────────────────
    fun getLogsForDate(date: String): LiveData<List<MedicineLog>> =
        logDao.getLogsForDate(date)

    fun getLogsForMedicine(medicineId: Long): LiveData<List<MedicineLog>> =
        logDao.getLogsForMedicine(medicineId)

    fun getRecentMissedLogs(): LiveData<List<MedicineLog>> =
        logDao.getRecentMissedLogs()

    suspend fun insertLog(log: MedicineLog): Long = logDao.insertLog(log)

    suspend fun markAsTaken(logId: Long) = logDao.markAsTaken(logId)

    suspend fun markOverdueAsMissed() =
        logDao.markOverdueAsMissed(System.currentTimeMillis() - 3600000) // 1hr grace

    suspend fun getLogsForDateRange(startDate: String, endDate: String): List<MedicineLog> =
        logDao.getLogsForDateRange(startDate, endDate)

    // ── Adherence Stats ───────────────────────────────────
    suspend fun getAdherenceStats(userId: String, daysBack: Int = 7): AdherenceStats {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endDate = sdf.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysBack)
        val startDate = sdf.format(cal.time)

        val total = logDao.getTotalScheduledForRange(startDate, endDate)
        val taken = logDao.getTakenCountForRange(startDate, endDate)
        val missed = logDao.getMissedCountForRange(startDate, endDate)
        val adherence = if (total > 0) (taken.toFloat() / total) * 100f else 0f

        // Build per-day data for last 7 days
        val weeklyData = mutableListOf<DayAdherence>()
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        for (i in daysBack - 1 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(dayCal.time)
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)
            val label = when(dayOfWeek) {
                Calendar.MONDAY -> "Mon"; Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"; Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"; Calendar.SATURDAY -> "Sat"
                else -> "Sun"
            }
            val dayLogs = logDao.getLogsForDateRange(dateStr, dateStr)
            val dayTaken = dayLogs.count { it.status == "TAKEN" }
            val dayMissed = dayLogs.count { it.status == "MISSED" }
            weeklyData.add(DayAdherence(label, dateStr, dayLogs.size, dayTaken, dayMissed))
        }

        // Per-medicine stats
        val medicines = medicineDao.getMedicinesForUserSync(userId)
        val perMedicineStats = medicines.map { med ->
            val medLogs = logDao.getLogsForDateRange(startDate, endDate)
                .filter { it.medicineId == med.id }
            val medTaken = medLogs.count { it.status == "TAKEN" }
            val medMissed = medLogs.count { it.status == "MISSED" }
            val medAdh = if (medLogs.isNotEmpty()) (medTaken.toFloat() / medLogs.size) * 100f else 0f
            MedicineAdherence(med.id, med.name, medLogs.size, medTaken, medMissed, medAdh)
        }

        return AdherenceStats(total, taken, missed, adherence, weeklyData, emptyList(), perMedicineStats)
    }

    // ── User ─────────────────────────────────────────────
    fun getUserById(userId: String): LiveData<User?> = userDao.getUserById(userId)

    suspend fun getUserByIdSync(userId: String): User? = userDao.getUserByIdSync(userId)

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun getUserByProfileCode(code: String): User? = userDao.getUserByProfileCode(code)

    // ── Reminders ─────────────────────────────────────────
    fun getActiveReminders(): LiveData<List<Reminder>> = reminderDao.getActiveReminders()

    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)

    suspend fun deleteRemindersForMedicine(medicineId: Long) =
        reminderDao.deleteRemindersForMedicine(medicineId)

    suspend fun getAllReminders(): List<Reminder> = reminderDao.getAllReminders()

    // ── Seed Data ─────────────────────────────────────────
    suspend fun seedSampleData(userId: String) {
        val now = System.currentTimeMillis()
        val medicines = listOf(
            Medicine(userId = userId, name = "Metformin", dosage = "500mg", type = "TABLET",
                frequency = "TWICE_DAILY", reminderTimes = "08:00,20:00",
                mealTiming = "AFTER_MEAL", expiryDate = now + 90L * 86400000),
            Medicine(userId = userId, name = "Amlodipine", dosage = "5mg", type = "TABLET",
                frequency = "DAILY", reminderTimes = "09:00", mealTiming = "ANYTIME",
                expiryDate = now + 15L * 86400000),  // expiring soon
            Medicine(userId = userId, name = "Vitamin D3", dosage = "1000 IU", type = "CAPSULE",
                frequency = "DAILY", reminderTimes = "13:00", mealTiming = "WITH_MEAL",
                expiryDate = now + 180L * 86400000),
            Medicine(userId = userId, name = "Aspirin", dosage = "75mg", type = "TABLET",
                frequency = "DAILY", reminderTimes = "08:00", mealTiming = "AFTER_MEAL",
                expiryDate = now - 5L * 86400000)  // expired
        )
        medicines.forEach { medicineDao.insertMedicine(it) }
    }
}
