package com.mediease.app.repository

import com.mediease.app.models.AdherenceStats
import com.mediease.app.network.ApiClient
import com.mediease.app.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdherenceRepository {
    private val api = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun getAdherenceStats(patientId: String): List<AdherenceStats>? = withContext(Dispatchers.IO) {
        try {
            val response = api.getAdherenceStats(patientId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
}
