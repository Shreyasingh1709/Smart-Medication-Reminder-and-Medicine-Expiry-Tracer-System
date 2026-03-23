package com.mediease.app.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mediease.app.databinding.ActivityHelpSupportBinding

class HelpSupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpSupportBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnEmailSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@mediease.app"))
                putExtra(Intent.EXTRA_SUBJECT, "MediEase Support Request")
            }
            try {
                startActivity(Intent.createChooser(intent, "Send email..."))
            } catch (e: Exception) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
