package com.mediease.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mediease.app.databinding.ActivityRegisterBinding
import com.mediease.app.models.User
import com.mediease.app.utils.PrefsManager
import com.mediease.app.viewmodels.UserViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Enter a valid email"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            val newUser = User(
                id = prefs.generateUserId(),
                email = email,
                password = password
            )

            viewModel.saveUser(newUser)
        }

        viewModel.saveResult.observe(this) { success ->
            if (success) {
                prefs.isLoggedIn = true
                prefs.userId = viewModel.currentUser.value?.id ?: ""
                prefs.userEmail = binding.etEmail.text.toString().trim()
                
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, RoleSelectActivity::class.java))
                finish()
            } else {
                val errorMsg = viewModel.error.value ?: "Registration failed"
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
}
