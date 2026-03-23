package com.mediease.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mediease.app.adapters.MedicineCardAdapter
import com.mediease.app.databinding.FragmentCalendarBinding
import com.mediease.app.viewmodels.MedicineViewModel

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val medicineVM: MedicineViewModel by viewModels()
    private lateinit var medicineAdapter: MedicineCardAdapter

    private var selectedDate: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        medicineAdapter = MedicineCardAdapter()
        binding.rvMedicines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMedicines.adapter = medicineAdapter

        // Set initial date to today
        val calendar = java.util.Calendar.getInstance()
        updateForDate(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            updateForDate(year, month, day)
        }

        medicineVM.medicines.observe(viewLifecycleOwner) { medicines ->
            selectedDate?.let { dateStr ->
                val filtered = medicines.filter { med ->
                    // Compare only date part of startDate
                    val medCal = java.util.Calendar.getInstance().apply { timeInMillis = med.startDate }
                    val medDateStr = String.format("%04d-%02d-%02d", medCal.get(java.util.Calendar.YEAR), medCal.get(java.util.Calendar.MONTH) + 1, medCal.get(java.util.Calendar.DAY_OF_MONTH))
                    medDateStr == dateStr
                }
                medicineAdapter.submitList(filtered)
            }
        }
    }

    private fun updateForDate(year: Int, month: Int, day: Int) {
        selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
        binding.tvScheduleDate.text = "Schedule for $day ${getMonthName(month)} $year"
        // The observer on medicines will update the list
    }

    private fun getMonthName(month: Int) = arrayOf(
        "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")[month]

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
