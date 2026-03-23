package com.mediease.app.utils


import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mediease.app.R
import com.mediease.app.activities.MainActivity
import java.util.*

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

/**
 * Receives scheduled alarm intents and shows medicine reminder notifications.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra(EXTRA_MED_NAME) ?: return
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val logId = intent.getLongExtra(EXTRA_LOG_ID, -1L)
        val repeatCount = intent.getIntExtra("repeat_count", 0)

        if (intent.action == "ACTION_MARK_TAKEN") {
            // Cancel notification and do not repeat
            val notifId = (logId % Int.MAX_VALUE).toInt()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notifId)
            // Mark as taken in DB using coroutine
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val db = com.mediease.app.database.AppDatabase.getDatabase(context)
                    db.medicineLogDao().markAsTaken(logId, System.currentTimeMillis())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return
        }

        NotificationHelper.showMedicineReminder(context, medName, dosage, logId)

        // If not marked as taken, repeat every 5 minutes up to 3 times
        if (repeatCount < 3) {
            val nextIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(EXTRA_MED_NAME, medName)
                putExtra(EXTRA_DOSAGE, dosage)
                putExtra(EXTRA_LOG_ID, logId)
                putExtra("repeat_count", repeatCount + 1)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, (logId % Int.MAX_VALUE).toInt() + 2000, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAt = System.currentTimeMillis() + 5 * 60 * 1000 // 5 minutes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
    }

    companion object {
        const val EXTRA_MED_NAME = "med_name"
        const val EXTRA_DOSAGE = "dosage"
        const val EXTRA_LOG_ID = "log_id"
    }
}

/**
 * Re-schedules alarms after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all active medicine reminders
        }
    }
}

/**
 * Helper for showing notifications.
 */
object NotificationHelper {

    fun showExpiryReminder(context: Context, medName: String, medicineId: Long) {
        val notifId = (medicineId % Int.MAX_VALUE).toInt() + 5000
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_home", true)
        }
        val pendingOpen = PendingIntent.getActivity(
            context, notifId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            context, "expiry_alerts")
            .setSmallIcon(R.drawable.ic_medicine_placeholder)
            .setContentTitle("Medicine Expiry Reminder")
            .setContentText("$medName is expiring soon. Please check your stock.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingOpen)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }

    fun scheduleExpiryAlarm(
        context: Context,
        triggerAt: Long,
        medName: String,
        medicineId: Long
    ) {
        val intent = Intent(context, ExpiryAlarmReceiver::class.java).apply {
            putExtra("med_name", medName)
            putExtra("medicine_id", medicineId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, (medicineId % Int.MAX_VALUE).toInt() + 5000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun showMedicineReminder(context: Context, medName: String, dosage: String, logId: Long) {
        val notifId = (logId % Int.MAX_VALUE).toInt()

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_home", true)
        }
        val pendingOpen = PendingIntent.getActivity(
            context, notifId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark taken action
        val takenIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_MARK_TAKEN"
            putExtra(AlarmReceiver.EXTRA_LOG_ID, logId)
        }
        val pendingTaken = PendingIntent.getBroadcast(
            context, notifId + 1000, takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context, "med_reminders")
            .setSmallIcon(R.drawable.ic_pill_notif)
            .setContentTitle("Medicine Reminder")
            .setContentText("$medName — $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingOpen)
            .addAction(R.drawable.ic_check, "Mark as Taken", pendingTaken)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }

    fun scheduleAlarm(
        context: Context,
        triggerAt: Long,
        medName: String,
        dosage: String,
        logId: Long,
        requestCode: Int
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MED_NAME, medName)
            putExtra(AlarmReceiver.EXTRA_DOSAGE, dosage)
            putExtra(AlarmReceiver.EXTRA_LOG_ID, logId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}
