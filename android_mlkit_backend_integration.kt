// Add this to your Android app (Kotlin) to send ML Kit OCR text to your FastAPI backend
// Requires OkHttp and org.json dependencies

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

fun sendOcrTextToBackend(ocrText: String) {
    val json = JSONObject().put("ocr_text", ocrText)
    val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())

    val request = Request.Builder()
        .url("http://<YOUR_PC_IP>:8000/parse_medicine") // Replace <YOUR_PC_IP> with your computer's IP address
        .post(requestBody)
        .build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle error (e.g., show a Toast or log)
        }
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            // Parse the JSON and update your UI
        }
    })
}
