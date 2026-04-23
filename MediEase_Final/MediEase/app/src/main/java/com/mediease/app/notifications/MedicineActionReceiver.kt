package com.mediease.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.mediease.app.models.MedicineLog
import com.mediease.app.repository.MedicineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MedicineActionReceiver : BroadcastReceiver() {
    private val TAG = "MedicineActionReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getLongExtra("medicine_id", -1L)
        val notificationId = intent.getIntExtra("notification_id", -1)
        val action = intent.action

        Log.d(TAG, "Received action: $action for medicine: $medicineId")

        if (medicineId != -1L && action == "ACTION_MARK_TAKEN") {
            val repo = MedicineRepository(context)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            // Using a specific scope instead of GlobalScope for better practice
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val log = MedicineLog(
                        medicineId = medicineId,
                        scheduledTime = System.currentTimeMillis(),
                        takenTime = System.currentTimeMillis(),
                        status = "TAKEN",
                        date = sdf.format(Date())
                    )
                    repo.insertLog(log)
                    Log.d(TAG, "Successfully logged intake for medicine $medicineId")
                    
                    // Dismiss notification
                    NotificationManagerCompat.from(context).cancel(notificationId)

                    // Cancel any further repeats for this notification
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                    val alarmIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                        putExtra("medicine_id", medicineId)
                        putExtra("request_code", notificationId)
                    }
                    val pi = android.app.PendingIntent.getBroadcast(context, notificationId, alarmIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
                    alarmManager.cancel(pi)
                } catch (e: Exception) {
                    Log.e(TAG, "Error logging intake", e)
                }
            }
        }
    }
}
