package com.mediease.app.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.mediease.app.models.MedicineLog
import com.mediease.app.repository.MedicineRepository
import com.mediease.app.models.Medicine
import com.mediease.app.utils.DateUtils
import com.mediease.app.utils.PrefsManager
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MedicineRepository(application)
    private val prefs = PrefsManager(application)

    val todayMedicines = MutableLiveData<List<Medicine>>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    val takenCount = MutableLiveData<Int>()
    val missedCount = MutableLiveData<Int>()
    val totalToday = MutableLiveData<Int>()

    fun loadTodayMedicines() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                // Using the local DB repository instead of the API one which was wrongly referenced
                val userId = prefs.userId
                val result = repo.getMedicinesForUserSync(userId)
                todayMedicines.value = result
                
                // Mocking counts for now
                totalToday.value = result.size
                takenCount.value = 0
                missedCount.value = 0
                
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun markAsTaken(logId: Long) {
        viewModelScope.launch {
            repo.markAsTaken(logId)
            loadTodayMedicines()
        }
    }

    fun markOverdueAsMissed() {
        viewModelScope.launch {
            repo.markOverdueAsMissed()
            loadTodayMedicines()
        }
    }
}
