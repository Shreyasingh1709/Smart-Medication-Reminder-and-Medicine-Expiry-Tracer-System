package com.mediease.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mediease.app.repository.MedicineRepository
import com.mediease.app.utils.NotificationUtils
import com.mediease.app.utils.PrefsManager
import kotlinx.coroutines.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = PrefsManager(context)
            if (!prefs.isLoggedIn) return
            // Reschedule all alarms after reboot
            CoroutineScope(Dispatchers.IO).launch {
                val repo = MedicineRepository(context)
                val reminders = repo.getAllReminders()
                reminders.filter { it.isEnabled }.forEach { reminder ->
                    val triggerMillis = NotificationUtils.getNextAlarmMillis(reminder.time)
                    NotificationUtils.scheduleAlarm(
                        context, reminder.alarmRequestCode, triggerMillis,
                        reminder.medicineId, reminder.medicineName
                    )
                }
            }
        }
    }
}
