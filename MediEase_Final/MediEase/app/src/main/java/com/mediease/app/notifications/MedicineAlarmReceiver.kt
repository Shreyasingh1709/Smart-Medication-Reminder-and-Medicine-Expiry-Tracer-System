package com.mediease.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.mediease.app.R
import com.mediease.app.activities.MainActivity
import com.mediease.app.utils.NotificationUtils

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val medicineId = intent.getLongExtra("medicine_id", 0L)
        val requestCode = intent.getIntExtra("request_code", 0)
        val isExpiryAlert = intent.getBooleanExtra("is_expiry", false)

        // Wake up the screen using modern flag if possible
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLockTag = "MediEase:AlarmWakeLock"
        
        // Using a less deprecated way for wake lock if possible, or just keeping it scoped
        val wakeLock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag)
        } else {
            @Suppress("DEPRECATION")
            pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, wakeLockTag)
        }
        
        wakeLock.acquire(10 * 1000L /*10 seconds*/)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicine_id", medicineId)
        }
        val pendingIntent = PendingIntent.getActivity(context, requestCode, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (isExpiryAlert) {
            val daysBefore = intent.getIntExtra("days_before", 0)
            val expiryNotification = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_EXPIRY)
                .setSmallIcon(R.drawable.ic_medicine_placeholder)
                .setContentTitle("⚠️ Medicine Expiring Soon")
                .setContentText("$medicineName will expire in $daysBefore days!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            nm.notify(requestCode, expiryNotification)
        } else {
            // "Mark as Taken" action
            val takenIntent = Intent(context, MedicineActionReceiver::class.java).apply {
                action = "ACTION_MARK_TAKEN"
                putExtra("medicine_id", medicineId)
                putExtra("notification_id", requestCode)
            }
            val takenPendingIntent = PendingIntent.getBroadcast(context, requestCode, takenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val notification = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_MEDICINE)
                .setSmallIcon(R.drawable.ic_medicine_placeholder)
                .setContentTitle("💊 Medicine Alarm")
                .setContentText("Urgent: Time to take $medicineName")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("It's time to take your $medicineName. Tap to mark as taken."))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(alarmSound)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_medicine_placeholder, "Mark as Taken", takenPendingIntent) // Action button
                .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
                .setColor(0xF4A58D.or(0xFF shl 24))
                .setFullScreenIntent(pendingIntent, true)
                .build()

            nm.notify(requestCode, notification)

            // Repeat logic: reschedule every 5 minutes up to 10 times if not marked as taken
            val repeatCount = intent.getIntExtra("repeat_count", 0)
            if (repeatCount < 10) {
                val nextIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                    putExtra("medicine_id", medicineId)
                    putExtra("medicine_name", medicineName)
                    putExtra("request_code", requestCode)
                    putExtra("repeat_count", repeatCount + 1)
                }
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                val pi = PendingIntent.getBroadcast(context, requestCode, nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val triggerAtMillis = System.currentTimeMillis() + 5 * 60 * 1000 // 5 minutes
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    val info = android.app.AlarmManager.AlarmClockInfo(triggerAtMillis, pi)
                    alarmManager.setAlarmClock(info, pi)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
                }
            }
        }
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
