package com.mediease.app.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mediease.app.databinding.ActivityAdherenceAnalysisBinding
import com.mediease.app.viewmodels.AdherenceViewModel

class AdherenceAnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdherenceAnalysisBinding
    private val viewModel: AdherenceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdherenceAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        viewModel.weeklyStats.observe(this) { stats ->
            binding.tvAdherencePercent.text = "${stats.adherencePercent.toInt()}%"
            binding.tvTakenCount.text = stats.totalTaken.toString()
            binding.tvMissedCount.text = stats.totalMissed.toString()
            binding.tvTip.text = viewModel.getAdherenceTip(stats.adherencePercent)
            binding.progressAdherence.progress = stats.adherencePercent.toInt()
        }
    }
}
