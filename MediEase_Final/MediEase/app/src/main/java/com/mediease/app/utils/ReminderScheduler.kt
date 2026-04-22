package com.mediease.app.utils

import android.content.Context
import com.mediease.app.models.Medicine
import com.mediease.app.models.Reminder
import com.mediease.app.repository.MedicineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Schedules system alarms and manages the Reminders database entries.
 */
object ReminderScheduler {

    suspend fun scheduleForMedicine(context: Context, medicine: Medicine) {
        withContext(Dispatchers.IO) {
            val repo = MedicineRepository(context)
            val times = medicine.getReminderTimesList()
            val name = medicine.name
            val medicineId = medicine.id
            val imagePath = medicine.imagePath

            // 1. Clear existing reminders for this medicine in DB
            repo.deleteRemindersForMedicine(medicineId)

            times.forEachIndexed { idx, timeStr ->
                val triggerMillis = NotificationUtils.getNextAlarmMillis(timeStr)
                val requestCode = (medicineId * 100 + idx).toInt()

                // 2. Save to database so it shows up in "Reminders" tab
                val reminder = Reminder(
                    medicineId = medicineId,
                    medicineName = name,
                    time = timeStr,
                    repeatDays = medicine.repeatDays,
                    alarmRequestCode = requestCode,
                    isEnabled = true,
                    frequency = medicine.frequency,
                    medicineImagePath = imagePath // Store medicine image in reminder
                )
                repo.insertReminder(reminder)

                // 3. Schedule system alarm using the harmonized NotificationUtils
                NotificationUtils.scheduleAlarm(
                    context,
                    requestCode,
                    triggerMillis,
                    medicineId,
                    name
                )
            }

            // 4. Schedule expiry reminder if set
            medicine.expiryDate?.let { expiryMillis ->
                val oneDayBefore = expiryMillis - 24 * 60 * 60 * 1000
                if (oneDayBefore > System.currentTimeMillis()) {
                    // Using existing Utils for consistency
                    NotificationUtils.scheduleAlarm(
                        context,
                        (medicineId * 100 + 99).toInt(), // unique code for expiry
                        oneDayBefore,
                        medicineId,
                        "Expiry Alert: $name"
                    )
                }
            }
        }
    }

    suspend fun cancelForMedicine(context: Context, medicineId: Long, timeCount: Int) {
        withContext(Dispatchers.IO) {
            val repo = MedicineRepository(context)
            repo.deleteRemindersForMedicine(medicineId)
            for (idx in 0 until (timeCount + 1)) { // +1 to cover potential expiry alarm
                val requestCode = (medicineId * 100 + idx).toInt()
                NotificationUtils.cancelAlarm(context, requestCode)
            }
            // Explicitly cancel expiry alarm just in case
            NotificationUtils.cancelAlarm(context, (medicineId * 100 + 99).toInt())
        }
    }
}
