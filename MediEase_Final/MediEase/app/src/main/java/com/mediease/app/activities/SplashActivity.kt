package com.mediease.app.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.mediease.app.R
import com.mediease.app.utils.PrefsManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = PrefsManager(this)
            val intent = when {
                // Temporary Change: Logged out users go directly to Role Selection
                !prefs.isLoggedIn -> Intent(this, RoleSelectActivity::class.java)
                !prefs.isSetupComplete -> Intent(this, RoleSelectActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2000)
    }
}
