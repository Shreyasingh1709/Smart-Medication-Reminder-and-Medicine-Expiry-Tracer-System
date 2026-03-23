package com.mediease.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mediease.app.databinding.ActivityRoleSelectBinding
import com.mediease.app.utils.PrefsManager

class RoleSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoleSelectBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        binding.cardPatient.setOnClickListener {
            prefs.userRole = PrefsManager.ROLE_PATIENT
            startActivity(Intent(this, PatientSetupActivity::class.java))
            finish()
        }

        binding.cardCaregiver.setOnClickListener {
            prefs.userRole = PrefsManager.ROLE_CAREGIVER
            startActivity(Intent(this, CaregiverSetupActivity::class.java))
            finish()
        }
    }
}
