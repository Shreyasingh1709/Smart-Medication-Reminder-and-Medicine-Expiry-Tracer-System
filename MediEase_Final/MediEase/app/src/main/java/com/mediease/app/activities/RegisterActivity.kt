package com.mediease.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mediease.app.databinding.ActivityRegisterBinding
import com.mediease.app.models.User
import com.mediease.app.utils.PrefsManager
import com.mediease.app.viewmodels.UserViewModel
import kotlinx.coroutines.launch

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

            lifecycleScope.launch {
                viewModel.saveUser(newUser)
                prefs.isLoggedIn = true
                prefs.userId = newUser.id
                prefs.userEmail = email
                
                Toast.makeText(this@RegisterActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, RoleSelectActivity::class.java))
                finish()
            }
        }
    }
}
