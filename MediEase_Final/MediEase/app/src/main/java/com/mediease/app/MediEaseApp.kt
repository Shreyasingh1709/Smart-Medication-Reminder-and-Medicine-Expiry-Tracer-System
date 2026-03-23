package com.mediease.app

import android.app.Application
import com.mediease.app.utils.NotificationUtils

class MediEaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createNotificationChannels(this)
    }
}
