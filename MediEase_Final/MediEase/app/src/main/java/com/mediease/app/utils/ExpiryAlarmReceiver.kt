package com.mediease.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ExpiryAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra("med_name") ?: return
        val medicineId = intent.getLongExtra("medicine_id", -1L)
        NotificationHelper.showExpiryReminder(context, medName, medicineId)
    }
}