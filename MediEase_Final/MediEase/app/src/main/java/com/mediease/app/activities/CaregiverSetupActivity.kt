package com.mediease.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mediease.app.databinding.ActivityCaregiverSetupBinding
import com.mediease.app.models.User
import com.mediease.app.utils.PrefsManager
import com.mediease.app.viewmodels.UserViewModel

class CaregiverSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCaregiverSetupBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaregiverSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        binding.btnConnect.setOnClickListener {
            val patientName = binding.etPatientName.text.toString().trim()
            val code = binding.etPatientCode.text.toString().trim().uppercase()

            if (patientName.isEmpty()) {
                binding.etPatientName.error = "Please enter patient name"
                return@setOnClickListener
            }
            if (code.isEmpty()) {
                binding.etPatientCode.error = "Please enter patient code"
                return@setOnClickListener
            }

            binding.btnConnect.isEnabled = false
            binding.btnConnect.text = "Connecting…"

            // Save caregiver user first then connect
            saveCaregiver(patientName, code)
        }

        viewModel.saveResult.observe(this) { success ->
            if (success) {
                prefs.isSetupComplete = true
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        viewModel.connectResult.observe(this) { patientId ->
            if (patientId != null) {
                prefs.linkedUserId = patientId
                Toast.makeText(this, "Connected to $patientId! ✓", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Patient not found. Check the code.", Toast.LENGTH_SHORT).show()
                binding.btnConnect.isEnabled = true
                binding.btnConnect.text = "Connect & Start ✓"
            }
        }
    }

    private fun saveCaregiver(patientName: String, patientCode: String) {
        val user = User(
            id = prefs.userId.ifEmpty { prefs.generateUserId() },
            name = prefs.userName.ifEmpty { "Caregiver" }, // Use existing name or default
            email = prefs.userEmail,
            role = "CAREGIVER",
            profileCode = prefs.generateProfileCode()
        )
        prefs.userId = user.id
        viewModel.saveUser(user)
        if (patientCode.isNotEmpty()) {
            viewModel.connectToPatient(patientCode)
        }
    }
}
