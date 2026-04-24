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
                val allMedicines = repo.getMedicinesForUserSync(userId)
                
                // Get today's day of week (1=Mon, 7=Sun)
                val calendar = Calendar.getInstance()
                // java.util.Calendar uses 1=Sun, 2=Mon...
                // Our repeatDays uses 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
                val dayOfWeek = when(calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7
                    else -> 1
                }
                
                // Filter medicines that should be taken today
                val filtered = allMedicines.filter { medicine ->
                    val repeatDays = medicine.getRepeatDaysList()
                    repeatDays.contains(dayOfWeek) && medicine.isActive
                }
                
                todayMedicines.value = filtered
                
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val logs = repo.getLogsForDateSync(today)
                
                // Calculate counts based on today's logs
                totalToday.value = filtered.size
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
