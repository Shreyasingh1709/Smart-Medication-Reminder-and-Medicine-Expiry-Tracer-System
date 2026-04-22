package com.mediease.app.activities

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mediease.app.R
import com.mediease.app.databinding.ActivityMainBinding
import com.mediease.app.fragments.*
import com.mediease.app.utils.PrefsManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        setupBottomNavigation()
        checkPermissions()

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            binding.bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun checkPermissions() {
        // 1. Post Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // 2. Exact Alarms (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun setupBottomNavigation() {
        val isCaregiver = prefs.userRole == PrefsManager.ROLE_CAREGIVER

        if (isCaregiver) {
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_caregiver)
        } else {
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_menu)
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_patients -> CaregiverPatientsFragment()
                R.id.nav_calendar -> CalendarFragment()
                R.id.nav_ai_chat -> ChatFragment()
                R.id.nav_analysis -> AdherenceFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
