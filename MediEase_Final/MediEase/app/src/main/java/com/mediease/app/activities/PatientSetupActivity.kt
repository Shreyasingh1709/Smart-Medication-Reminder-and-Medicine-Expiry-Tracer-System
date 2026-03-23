package com.mediease.app.activities

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mediease.app.databinding.ActivityPatientSetupBinding
import com.mediease.app.models.User
import com.mediease.app.utils.PrefsManager
import com.mediease.app.viewmodels.UserViewModel
import java.util.*

class PatientSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientSetupBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var prefs: PrefsManager
    private var currentStep = 1
    private val totalSteps = 4

    // Collected data
    private var name = ""
    private var age: Int? = null
    private var ageRange: String? = null
    private var wakeTime = "07:00"
    private var bedTime = "22:00"
    private var breakfastTime = "08:00"
    private var lunchTime = "13:00"
    private var dinnerTime = "19:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        setupStepNavigation()
        updateStepUI()
        setupPickers()
        setupAgeSpinner()

        viewModel.saveResult.observe(this) { success ->
            if (success) {
                prefs.isLoggedIn = true // Mark as logged in
                prefs.isSetupComplete = true
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                val errorMsg = viewModel.error.value ?: "Unknown error"
                Toast.makeText(this, "Setup failed: $errorMsg", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupStepNavigation() {
        binding.btnNext.setOnClickListener {
            if (validateCurrentStep()) {
                if (currentStep < totalSteps) {
                    currentStep++
                    updateStepUI()
                } else {
                    completeSetup()
                }
            }
        }
        binding.btnBack.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateStepUI()
            }
        }
    }

    private fun validateCurrentStep(): Boolean {
        return when (currentStep) {
            1 -> {
                name = binding.etName.text.toString().trim()
                if (name.isEmpty()) {
                    binding.etName.error = "Please enter your name"
                    false
                } else true
            }
            2 -> {
                val ageText = binding.etAge.text.toString().trim()
                val selectedRange = binding.spinnerAgeRange.selectedItem?.toString()
                if (ageText.isEmpty() && (selectedRange == null || selectedRange.isEmpty() || selectedRange == "Select")) {
                    Toast.makeText(this, "Please enter your age or select a range", Toast.LENGTH_SHORT).show()
                    false
                } else {
                    age = ageText.toIntOrNull()
                    ageRange = if (selectedRange != null && selectedRange != "Select") selectedRange else null
                    true
                }
            }
            else -> true
        }
    }

    private fun updateStepUI() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundColor(if (index + 1 == currentStep) 0xFFF4A58D.toInt() else 0xFFD1D1D1.toInt())
        }

        binding.tvStepLabel.text = "Step $currentStep of $totalSteps"
        binding.btnBack.visibility = if (currentStep > 1) View.VISIBLE else View.INVISIBLE
        binding.btnNext.text = if (currentStep == totalSteps) "All Done! ✓" else "Next →"

        val (title, subtitle, icon) = when (currentStep) {
            1 -> Triple("What's your name?", "This helps us personalise your experience", "🙋")
            2 -> Triple("How old are you?", "Enter your age or select a range", "🎂")
            3 -> Triple("When do you sleep & wake up?", "We'll avoid reminders during sleep hours", "🌙")
            else -> Triple("What are your meal times?", "We'll sync reminders to your meals", "🍽️")
        }
        binding.tvTitle.text = title
        binding.tvSubtitle.text = subtitle
        binding.tvStepIcon.text = icon

        binding.panelStep1.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
        binding.panelStep2.visibility = if (currentStep == 2) View.VISIBLE else View.GONE
        binding.panelStep3.visibility = if (currentStep == 3) View.VISIBLE else View.GONE
        binding.panelStep4.visibility = if (currentStep == 4) View.VISIBLE else View.GONE
    }

    private fun setupPickers() {
        binding.cardWakeTime.setOnClickListener { showTimePicker { h, m ->
            wakeTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            binding.tvWakeTime.text = formatTime(h, m)
        }}
        binding.cardBedTime.setOnClickListener { showTimePicker { h, m ->
            bedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            binding.tvBedTime.text = formatTime(h, m)
        }}
        binding.cardBreakfastTime.setOnClickListener { showTimePicker { h, m ->
            breakfastTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            binding.tvBreakfastTime.text = formatTime(h, m)
        }}
        binding.cardLunchTime.setOnClickListener { showTimePicker { h, m ->
            lunchTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            binding.tvLunchTime.text = formatTime(h, m)
        }}
        binding.cardDinnerTime.setOnClickListener { showTimePicker { h, m ->
            dinnerTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            binding.tvDinnerTime.text = formatTime(h, m)
        }}
    }

    private fun showTimePicker(onTime: (Int, Int) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m -> onTime(h, m) },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
    }

    private fun formatTime(h: Int, m: Int): String {
        val amPm = if (h >= 12) "PM" else "AM"
        val hour = if (h > 12) h - 12 else if (h == 0) 12 else h
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour, m, amPm)
    }

    private fun completeSetup() {
        val userId = prefs.userId.ifEmpty { prefs.generateUserId() }
        val email = prefs.userEmail.ifEmpty { "user@mediease.app" }

        val user = User(
            id = userId,
            name = name,
            email = email,
            role = "PATIENT",
            age = age ?: 0,
            ageRange = ageRange ?: "",
            wakeUpTime = wakeTime,
            bedTime = bedTime,
            breakfastTime = breakfastTime,
            lunchTime = lunchTime,
            dinnerTime = dinnerTime,
            profileCode = prefs.generateProfileCode()
        )
        prefs.userId = user.id
        prefs.userName = user.name
        prefs.userEmail = user.email
        prefs.profileCode = user.profileCode
        viewModel.saveUser(user)
    }

    private fun setupAgeSpinner() {
        // Optionally, add any spinner setup logic here if needed
    }
}
