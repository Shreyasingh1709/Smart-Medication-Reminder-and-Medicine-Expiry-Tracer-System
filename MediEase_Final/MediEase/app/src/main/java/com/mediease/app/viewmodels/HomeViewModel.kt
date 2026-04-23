package com.mediease.app.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.mediease.app.models.MedicineLog
import com.mediease.app.repository.MedicineRepository
import com.mediease.app.models.Medicine
import com.mediease.app.utils.PrefsManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
                val userId = prefs.userId
                val medicines = repo.getMedicinesForUserSync(userId)
                todayMedicines.value = medicines
                
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val logs = repo.getLogsForDateSync(today)
                
                // Calculate counts based on today's logs
                totalToday.value = medicines.size
                takenCount.value = logs.count { it.status == "TAKEN" }
                missedCount.value = logs.count { it.status == "MISSED" }
                
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun markMedicineAsTaken(medicine: Medicine) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date())
            
            val log = MedicineLog(
                medicineId = medicine.id,
                scheduledTime = System.currentTimeMillis(),
                takenTime = System.currentTimeMillis(),
                status = "TAKEN",
                date = today
            )
            repo.insertLog(log)
            loadTodayMedicines()
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
