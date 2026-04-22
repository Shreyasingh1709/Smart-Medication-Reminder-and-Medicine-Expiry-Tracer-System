package com.mediease.app.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.mediease.app.models.Reminder
import com.mediease.app.repository.MedicineRepository
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MedicineRepository(application)

    // All reminders from the database
    val allReminders: LiveData<List<Reminder>> = repo.getAllRemindersLive()
    
    // Filtered to show only DAILY reminders
    // A daily reminder is one that repeats on all 7 days of the week.
    val dailyReminders: LiveData<List<Reminder>> = allReminders.map { reminders ->
        reminders.filter { isDaily(it.repeatDays) }
    }
    
    private fun isDaily(repeatDays: String): Boolean {
        val days = repeatDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return days.size == 7 && (1..7).all { days.contains(it.toString()) }
    }
    
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    fun updateReminderStatus(reminder: Reminder, isEnabled: Boolean) {
        viewModelScope.launch {
            repo.updateReminder(reminder.copy(isEnabled = isEnabled))
        }
    }

    fun deleteRemindersForMedicine(medicineId: Long) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                repo.deleteRemindersForMedicine(medicineId)
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }
}
