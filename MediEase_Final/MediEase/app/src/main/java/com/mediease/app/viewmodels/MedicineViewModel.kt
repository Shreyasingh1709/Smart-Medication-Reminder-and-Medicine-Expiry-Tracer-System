package com.mediease.app.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.mediease.app.models.*
import com.mediease.app.repository.MedicineRepository
import com.mediease.app.utils.PrefsManager
import com.mediease.app.utils.ReminderScheduler
import kotlinx.coroutines.launch

class MedicineViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MedicineRepository(application)
    private val prefs = PrefsManager(application)

    val medicines: LiveData<List<Medicine>> by lazy {
        repo.getMedicinesForUser(prefs.userId)
    }

    val medicinesWithExpiry: LiveData<List<Medicine>> by lazy {
        repo.getMedicinesWithExpiry(prefs.userId)
    }

    private val _medicineCount = MutableLiveData<Int>()
    val medicineCount: LiveData<Int> get() = _medicineCount

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> get() = _saveResult

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> get() = _deleteResult

    fun getMedicineById(id: Long): LiveData<Medicine?> = repo.getMedicineByIdLive(id)

    fun saveMedicine(medicine: Medicine) {
        viewModelScope.launch {
            try {
                if (medicine.id == 0L) {
                    val newId = repo.insertMedicine(medicine.copy(userId = prefs.userId))
                    val savedMed = repo.getMedicineById(newId)
                    if (savedMed != null) {
                        ReminderScheduler.scheduleForMedicine(getApplication(), savedMed)
                    }
                } else {
                    val oldMed = repo.getMedicineById(medicine.id)
                    oldMed?.let {
                        ReminderScheduler.cancelForMedicine(getApplication(), it.id)
                    }
                    
                    repo.updateMedicine(medicine.copy(updatedAt = System.currentTimeMillis()))
                    ReminderScheduler.scheduleForMedicine(getApplication(), medicine)
                }
                _saveResult.postValue(true)
            } catch (e: Exception) {
                _saveResult.postValue(false)
            }
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            try {
                ReminderScheduler.cancelForMedicine(getApplication(), medicine.id)
                repo.deleteMedicine(medicine.id)
                _deleteResult.postValue(true)
            } catch (e: Exception) {
                _deleteResult.postValue(false)
            }
        }
    }

    fun deleteAllMedicines() {
        viewModelScope.launch {
            try {
                repo.deleteAllMedicines(prefs.userId)
                _deleteResult.postValue(true)
            } catch (e: Exception) {
                _deleteResult.postValue(false)
            }
        }
    }

    fun getExpiredAndExpiring(): LiveData<List<Medicine>> {
        val result = MutableLiveData<List<Medicine>>()
        medicinesWithExpiry.observeForever { medicines ->
            val filtered = medicines.filter { it.isExpired() || it.isExpiringSoon() }
            result.postValue(filtered)
        }
        return result
    }
}
