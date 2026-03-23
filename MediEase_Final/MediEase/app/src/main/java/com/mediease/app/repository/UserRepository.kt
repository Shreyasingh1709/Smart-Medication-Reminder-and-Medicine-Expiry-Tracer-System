package com.mediease.app.repository

import com.mediease.app.models.User
import com.mediease.app.network.ApiClient
import com.mediease.app.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val api = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun login(username: String, password: String): String? = withContext(Dispatchers.IO) {
        val response = api.login(username, password)
        if (response.isSuccessful) response.body()?.access_token else null
    }

    suspend fun getMe(): User? = withContext(Dispatchers.IO) {
        val response = api.getMe()
        if (response.isSuccessful) response.body() else null
    }

    suspend fun sendOtp(email: String): Boolean = withContext(Dispatchers.IO) {
        val response = api.sendOtp(email)
        response.isSuccessful
    }

    suspend fun verifyOtp(email: String, otp: String): Boolean = withContext(Dispatchers.IO) {
        val response = api.verifyOtp(email, otp)
        response.isSuccessful
    }
}
