package com.mediease.app.utils

import android.content.Context
import android.content.Intent
import com.mediease.app.models.Medicine
import com.mediease.app.models.Reminder
import com.mediease.app.notifications.MedicineAlarmReceiver
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
                    medicineImagePath = imagePath
                )
                repo.insertReminder(reminder)

                // 3. Schedule system alarm
                NotificationUtils.scheduleAlarm(
                    context,
                    requestCode,
                    triggerMillis,
                    medicineId,
                    name
                )
            }

            // 4. Schedule multiple expiry reminders
            medicine.expiryDate?.let { expiryMillis ->
                val alertDays = listOf(7, 3, 1, 0) // Alerts 7, 3, 1 day before, and on the day
                alertDays.forEach { daysBefore ->
                    val triggerTime = expiryMillis - (daysBefore * 24 * 60 * 60 * 1000L)
                    // Set alert time to 9:00 AM on that day
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = triggerTime
                        set(Calendar.HOUR_OF_DAY, 9)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }
                    
                    if (cal.timeInMillis > System.currentTimeMillis()) {
                        scheduleExpiryAlarm(
                            context,
                            (medicineId * 1000 + 900 + daysBefore).toInt(),
                            cal.timeInMillis,
                            medicineId,
                            name,
                            daysBefore
                        )
                    }
                }
            }
        }
    }

    private fun scheduleExpiryAlarm(context: Context, requestCode: Int, triggerMillis: Long, 
                                    medicineId: Long, name: String, daysBefore: Int) {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", name)
            putExtra("request_code", requestCode)
            putExtra("is_expiry", true)
            putExtra("days_before", daysBefore)
        }
        NotificationUtils.scheduleAlarmWithIntent(context, requestCode, triggerMillis, intent)
    }

    suspend fun cancelForMedicine(context: Context, medicineId: Long) {
        withContext(Dispatchers.IO) {
            val repo = MedicineRepository(context)
            repo.deleteRemindersForMedicine(medicineId)
            
            // Cancel intake alarms (cancel a reasonable range)
            for (idx in 0 until 10) {
                NotificationUtils.cancelAlarm(context, (medicineId * 100 + idx).toInt())
            }
            
            // Cancel expiry alarms
            val alertDays = listOf(7, 3, 1, 0)
            alertDays.forEach { daysBefore ->
                NotificationUtils.cancelAlarm(context, (medicineId * 1000 + 900 + daysBefore).toInt())
            }
        }
    }
}
