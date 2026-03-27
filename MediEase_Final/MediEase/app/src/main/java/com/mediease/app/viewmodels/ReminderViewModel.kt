package com.mediease.app.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.mediease.app.models.Reminder
import com.mediease.app.repository.MedicineRepository
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MedicineRepository(application)

    // Link directly to the LiveData from DAO
    val allReminders: LiveData<List<Reminder>> = repo.getAllRemindersLive()
    
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
