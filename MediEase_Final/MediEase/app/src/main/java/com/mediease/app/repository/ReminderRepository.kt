package com.mediease.app.repository

import com.mediease.app.models.Reminder
import com.mediease.app.network.ApiClient
import com.mediease.app.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderRepository {
    private val api = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun getReminders(medicineId: Long): List<Reminder>? = withContext(Dispatchers.IO) {
        val response = api.getReminders(medicineId)
        if (response.isSuccessful) response.body() else null
    }

    suspend fun addReminder(reminder: Reminder): Reminder? = withContext(Dispatchers.IO) {
        val response = api.addReminder(reminder)
        if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteReminder(id: Long): Boolean = withContext(Dispatchers.IO) {
        val response = api.deleteReminder(id)
        response.isSuccessful
    }
}
