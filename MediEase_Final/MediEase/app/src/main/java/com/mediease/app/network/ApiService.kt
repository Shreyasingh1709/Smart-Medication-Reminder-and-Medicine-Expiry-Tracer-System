package com.mediease.app.network

import com.mediease.app.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // User
    @POST("users/login")
    suspend fun login(@Query("username") username: String, @Query("password") password: String): Response<Token>

    @GET("users/me")
    suspend fun getMe(): Response<User>

    @POST("auth/send_otp")
    suspend fun sendOtp(@Query("email") email: String): Response<Map<String, String>>

    @POST("auth/verify_otp")
    suspend fun verifyOtp(@Query("email") email: String, @Query("otp") otp: String): Response<Map<String, String>>

    // Medicines
    @GET("medicines")
    suspend fun getMedicines(@Query("patient_id") patientId: Long): Response<List<Medicine>>

    @POST("medicines")
    suspend fun addMedicine(@Body medicine: Medicine): Response<Medicine>

    @DELETE("medicines/{id}")
    suspend fun deleteMedicine(@Path("id") id: Long): Response<Unit>

    // Reminders
    @GET("reminders")
    suspend fun getReminders(@Query("medicine_id") medicineId: Long): Response<List<Reminder>>

    @POST("reminders")
    suspend fun addReminder(@Body reminder: Reminder): Response<Reminder>

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: Long): Response<Unit>

    // Adherence
    @GET("adherence")
    suspend fun getAdherenceStats(@Query("patient_id") patientId: Long): Response<List<AdherenceStats>>

    // Image upload
    @Multipart
    @POST("images/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<ImageUploadResponse>
}

// Add this data class for token response
 data class Token(val access_token: String, val token_type: String)

// Add this data class for image upload response
 data class ImageUploadResponse(val image_url: String)
