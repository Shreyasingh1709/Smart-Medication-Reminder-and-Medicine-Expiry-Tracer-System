package com.mediease.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
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

        // Wake up the screen
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE, "MediEase:AlarmWakeLock")
        wakeLock.acquire(10 * 1000L /*10 seconds*/)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicine_id", medicineId)
        }
        val pendingIntent = PendingIntent.getActivity(context, requestCode, tapIntent,
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
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setColor(0xF4A58D.or(0xFF shl 24))
            .setFullScreenIntent(pendingIntent, true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(requestCode, notification)

        // Reschedule for next day
        val nextTrigger = System.currentTimeMillis() + 86400000L
        NotificationUtils.scheduleAlarm(context, requestCode, nextTrigger, medicineId, medicineName)
    }
}
