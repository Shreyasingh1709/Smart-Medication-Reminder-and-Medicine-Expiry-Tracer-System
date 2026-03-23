package com.mediease.app.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mediease.app.databinding.ActivityForgotPasswordBinding
import com.mediease.app.viewmodels.UserViewModel

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Enter a valid email"
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                binding.etNewPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            viewModel.resetPassword(email, newPassword)
        }

        viewModel.resetPasswordResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Password updated successfully! Please login.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                val errorMsg = viewModel.error.value ?: "Email not found"
                Toast.makeText(this, "Failed: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
