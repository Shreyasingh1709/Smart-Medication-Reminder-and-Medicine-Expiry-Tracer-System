package com.mediease.app.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.mediease.app.models.Reminder
import com.mediease.app.repository.MedicineRepository
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MedicineRepository(application)

    // Properly expose LiveData from repository
    val activeReminders: LiveData<List<Reminder>> = repo.getActiveReminders()
    
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    fun loadActiveReminders() {
        // No longer needed to manually update activeReminders as it's directly linked to the DB LiveData
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                repo.insertReminder(reminder)
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
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
