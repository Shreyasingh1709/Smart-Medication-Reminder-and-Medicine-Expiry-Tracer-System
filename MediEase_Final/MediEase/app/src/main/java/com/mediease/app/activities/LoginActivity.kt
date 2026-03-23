package com.mediease.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mediease.app.databinding.ActivityLoginBinding
import com.mediease.app.utils.PrefsManager
import com.mediease.app.viewmodels.UserViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.loginWithEmailAndPassword(email, password)
        }

        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                prefs.isLoggedIn = true
                prefs.userId = user.id
                prefs.userEmail = user.email
                prefs.userRole = user.role
                prefs.userName = user.name
                
                // If user exists and setup was previously done, go to Main
                if (user.name.isNotEmpty()) {
                    prefs.isSetupComplete = true
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, RoleSelectActivity::class.java))
                }
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password. Please try again or create an account.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
