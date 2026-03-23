package com.mediease.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mediease.app.R
import com.mediease.app.activities.MainActivity
import com.mediease.app.utils.NotificationUtils

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val medicineId = intent.getLongExtra("medicine_id", 0L)
        val requestCode = intent.getIntExtra("request_code", 0)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicine_id", medicineId)
        }
        val pendingIntent = PendingIntent.getActivity(context, requestCode, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_MEDICINE)
            .setSmallIcon(R.drawable.ic_medicine_placeholder)
            .setContentTitle("💊 Medicine Reminder")
            .setContentText("Time to take $medicineName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to take your $medicineName. Tap to mark as taken."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setColor(0xF4A58D.or(0xFF shl 24))
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(requestCode, notification)

        // Reschedule for next day
        val nextTrigger = System.currentTimeMillis() + 86400000L
        NotificationUtils.scheduleAlarm(context, requestCode, nextTrigger, medicineId, medicineName)
    }
}
