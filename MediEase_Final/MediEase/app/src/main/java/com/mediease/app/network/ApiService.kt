package com.mediease.app.network

import com.google.gson.annotations.SerializedName
import com.mediease.app.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("users/login")
    suspend fun login(@Query("username") username: String, @Query("password") password: String): Response<Token>

    @GET("users/me")
    suspend fun getMe(): Response<User>

    @Multipart
    @POST("images/upload_and_extract")
    suspend fun uploadAndExtract(
        @Part files: List<MultipartBody.Part>
    ): Response<ExtractionResponse>

    @POST("ai/chat")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>

    @GET("adherence/stats/{patient_id}")
    suspend fun getAdherenceStats(@Path("patient_id") patientId: String): Response<List<AdherenceStats>>

    // Sync Endpoints
    @GET("medicines/sync")
    suspend fun pullMedicines(): Response<List<Medicine>>

    @POST("medicines/sync")
    suspend fun pushMedicines(@Body medicines: List<Medicine>): Response<Unit>

    @GET("logs/sync")
    suspend fun pullLogs(): Response<List<MedicineLog>>

    @POST("logs/sync")
    suspend fun pushLogs(@Body logs: List<MedicineLog>): Response<Unit>
}

data class ChatRequest(
    @SerializedName("message")
    val message: String,
    @SerializedName("user_id")
    val userId: String
)

data class ChatResponse(
    @SerializedName("reply")
    val reply: String
)

data class ExtractionResponse(
    @SerializedName("medicines")
    val medicines: List<ExtractedMedicine>? = emptyList()
)

data class ExtractedMedicine(
    @SerializedName("name", alternate = ["medicine_name", "med_name"])
    val name: String? = null,
    
    @SerializedName("dosage", alternate = ["strength", "amount"])
    val dosage: String? = null,
    
    @SerializedName("instructions", alternate = ["notes", "usage"])
    val instructions: String? = null,

    @SerializedName("expiry_date", alternate = ["exp_date", "expiry"])
    val expiryDate: String? = null,
    
    @SerializedName("missing_fields")
    val missing_fields: List<String>? = emptyList()
)

data class Token(val access_token: String, val token_type: String)
data class ImageUploadResponse(val image_url: String)
