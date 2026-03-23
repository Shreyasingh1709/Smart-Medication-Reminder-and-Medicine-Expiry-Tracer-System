package com.mediease.app.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.mediease.app.databinding.FragmentAdherenceBinding
import com.mediease.app.models.AdherenceStats
import com.mediease.app.viewmodels.AdherenceViewModel

class AdherenceFragment : Fragment() {
    private var _binding: FragmentAdherenceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdherenceViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdherenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tab switching
        binding.btnWeekly.setOnClickListener {
            binding.btnWeekly.isSelected = true
            binding.btnMonthly.isSelected = false
            viewModel.weeklyStats.value?.let { updateUI(it) }
        }
        binding.btnMonthly.setOnClickListener {
            binding.btnMonthly.isSelected = true
            binding.btnWeekly.isSelected = false
            viewModel.monthlyStats.value?.let { updateUI(it) }
        }
        binding.btnWeekly.isSelected = true

        viewModel.weeklyStats.observe(viewLifecycleOwner) { stats -> updateUI(stats) }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun updateUI(stats: AdherenceStats) {
        val pct = stats.adherencePercent.toInt()
        binding.tvAdherencePercent.text = "$pct%"
        binding.tvTakenCount.text = "${stats.totalTaken}"
        binding.tvMissedCount.text = "${stats.totalMissed}"
        binding.tvTotalScheduled.text = "${stats.totalScheduled}"
        binding.progressAdherence.progress = pct
        binding.tvTip.text = viewModel.getAdherenceTip(stats.adherencePercent)

        setupBarChart(stats)
    }

    private fun setupBarChart(stats: AdherenceStats) {
        val takenEntries = stats.weeklyData.mapIndexed { i, d -> BarEntry(i.toFloat(), d.taken.toFloat()) }
        val missedEntries = stats.weeklyData.mapIndexed { i, d -> BarEntry(i.toFloat(), d.missed.toFloat()) }
        val labels = stats.weeklyData.map { it.dayLabel }

        val takenSet = BarDataSet(takenEntries, "Taken").apply { color = 0xFFA3D9A5.toInt() }
        val missedSet = BarDataSet(missedEntries, "Missed").apply { color = 0xFFF4A58D.toInt() }

        val barData = BarData(takenSet, missedSet).apply {
            barWidth = 0.35f
        }

        binding.barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            description.isEnabled = false
            legend.isEnabled = true
            animateY(500)
            groupBars(0f, 0.1f, 0.05f)
            invalidate()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
