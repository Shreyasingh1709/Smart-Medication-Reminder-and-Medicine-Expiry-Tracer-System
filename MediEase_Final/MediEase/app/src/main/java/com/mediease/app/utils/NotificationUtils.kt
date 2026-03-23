package com.mediease.app.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mediease.app.notifications.MedicineAlarmReceiver
import java.util.*

object NotificationUtils {
    const val CHANNEL_MEDICINE = "medicine_reminders"
    const val CHANNEL_EXPIRY = "expiry_alerts"
    const val CHANNEL_CAREGIVER = "caregiver_alerts"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            listOf(
                NotificationChannel(CHANNEL_MEDICINE, "Medicine Reminders",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Reminders to take your medicine"
                    enableVibration(true)
                },
                NotificationChannel(CHANNEL_EXPIRY, "Expiry Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Alerts for medicines expiring soon"
                },
                NotificationChannel(CHANNEL_CAREGIVER, "Caregiver Alerts",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alerts for caregiver activity"
                }
            ).forEach { nm.createNotificationChannel(it) }
        }
    }

    fun scheduleAlarm(context: Context, requestCode: Int, triggerMillis: Long,
                      medicineId: Long, medicineName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("request_code", requestCode)
        }
        val pi = PendingIntent.getBroadcast(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
        }
    }

    fun cancelAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicineAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        pi?.let { alarmManager.cancel(it) }
    }

    fun getNextAlarmMillis(timeStr: String): Long {
        val cal = Calendar.getInstance()
        val parts = timeStr.split(":")
        if (parts.size == 2) {
            cal.set(Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: 8)
            cal.set(Calendar.MINUTE, parts[1].toIntOrNull() ?: 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }
}
