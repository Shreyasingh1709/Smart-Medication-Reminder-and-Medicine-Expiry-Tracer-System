package com.mediease.app.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.mediease.app.models.AdherenceStats
import com.mediease.app.repository.AdherenceRepository
import com.mediease.app.utils.PrefsManager
import kotlinx.coroutines.launch


class AdherenceViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AdherenceRepository()
    private val prefs = PrefsManager(application)

    val weeklyStats = MutableLiveData<AdherenceStats>()
    val monthlyStats = MutableLiveData<AdherenceStats>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val userId = prefs.userId
                if (userId.isNotEmpty()) {
                    val result = repo.getAdherenceStats(userId)
                    if (result != null && result.isNotEmpty()) {
                        weeklyStats.value = result[0]
                        if (result.size > 1) {
                            monthlyStats.value = result[1]
                        } else {
                            monthlyStats.value = result[0]
                        }
                    }
                }
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun getAdherenceTip(adherencePercent: Float): String = when {
        adherencePercent >= 90 -> "🎉 Excellent! Keep up the great work!"
        adherencePercent >= 75 -> "👍 Good job! Try to be more consistent."
        adherencePercent >= 50 -> "💡 Set multiple reminders to improve adherence."
        else -> "⚠️ Please try to take your medicines as scheduled."
    }
}
