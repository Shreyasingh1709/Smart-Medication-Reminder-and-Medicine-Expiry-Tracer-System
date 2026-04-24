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

    suspend fun deleteMedicine(medicineId: Long) = withContext(Dispatchers.IO) {
        medicineDao.deleteMedicineById(medicineId)
        logDao.deleteLogsForMedicine(medicineId)
        reminderDao.deleteRemindersForMedicine(medicineId)
    }

    suspend fun deleteAllMedicines(userId: String) = withContext(Dispatchers.IO) {
        val medicines = medicineDao.getMedicinesForUserSync(userId)
        medicines.forEach { 
            logDao.deleteLogsForMedicine(it.id)
            reminderDao.deleteRemindersForMedicine(it.id)
        }
    }

    suspend fun getMedicinesForUserSync(userId: String): List<Medicine> =
        medicineDao.getMedicinesForUserSync(userId)

    // Logs
    fun getLogsForDate(date: String): LiveData<List<MedicineLog>> =
        logDao.getLogsForDate(date)

    suspend fun getLogsForDateSync(date: String): List<MedicineLog> =
        logDao.getLogsForDateSync(date)

    suspend fun insertLog(log: MedicineLog): Long = logDao.insertLog(log)

    suspend fun markAsTaken(logId: Long) = logDao.markAsTaken(logId)

    suspend fun markOverdueAsMissed() =
        logDao.markOverdueAsMissed(System.currentTimeMillis() - 3600000)

    // User
    fun getUserById(userId: String): LiveData<User?> = userDao.getUserById(userId)
    suspend fun getUserByIdSync(userId: String): User? = userDao.getUserByIdSync(userId)
    suspend fun insertUser(user: User) = userDao.insertUser(user)

    // Reminders
    fun getAllRemindersLive(): LiveData<List<Reminder>> = reminderDao.getAllRemindersLive()
    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)
    suspend fun updateReminder(reminder: Reminder) = reminderDao.updateReminder(reminder)
    suspend fun deleteRemindersForMedicine(medicineId: Long) = reminderDao.deleteRemindersForMedicine(medicineId)
}
