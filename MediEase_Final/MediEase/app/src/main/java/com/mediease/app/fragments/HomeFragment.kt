package com.mediease.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mediease.app.activities.AddMedicineActivity
import com.mediease.app.adapters.MedicineCardAdapter
import com.mediease.app.adapters.ExpiryWarningAdapter
import com.mediease.app.databinding.FragmentHomeBinding
import com.mediease.app.utils.DateUtils
import com.mediease.app.utils.PrefsManager
import com.mediease.app.viewmodels.HomeViewModel
import com.mediease.app.viewmodels.MedicineViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeVM: HomeViewModel by viewModels()
    private val medicineVM: MedicineViewModel by viewModels()
    private lateinit var prefs: PrefsManager
    private lateinit var medicineAdapter: MedicineCardAdapter
    private lateinit var expiryAdapter: ExpiryWarningAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PrefsManager(requireContext())

        setupGreeting()
        setupRecyclerViews()
        setupFab()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        homeVM.markOverdueAsMissed()
        homeVM.loadTodayMedicines()
    }

    private fun setupGreeting() {
        val userName = prefs.userName
        val name = if (userName.isNotEmpty()) userName.split(" ").first() else "Friend"
        binding.tvGreeting.text = "${DateUtils.getGreeting()} $name 👋"
        binding.tvDate.text = java.text.SimpleDateFormat("EEEE, dd MMM yyyy",
            java.util.Locale.getDefault()).format(java.util.Date())
    }

    private fun setupRecyclerViews() {
        medicineAdapter = MedicineCardAdapter(
            onMarkTaken = { medicine -> homeVM.markMedicineAsTaken(medicine) },
            onEdit = { _ ->
                // Navigate to edit/details if needed
            }
        )
        binding.rvMedicines.adapter = medicineAdapter

        expiryAdapter = ExpiryWarningAdapter()
        binding.rvExpiryWarnings.adapter = expiryAdapter
    }

    private fun setupFab() {
        binding.fabAddMedicine.setOnClickListener {
            startActivity(Intent(requireContext(), AddMedicineActivity::class.java))
        }
    }

    private fun observeData() {
        homeVM.todayMedicines.observe(viewLifecycleOwner) { medicines ->
            val isEmpty = medicines.isEmpty()
            binding.rvMedicines.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.tvNoMedicines.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.tvMedicineCount.text = "${medicines.size} medicines today"
            medicineAdapter.submitList(medicines)
        }

        homeVM.takenCount.observe(viewLifecycleOwner) { count ->
            binding.tvTakenCount.text = "$count Taken"
        }

        homeVM.missedCount.observe(viewLifecycleOwner) { count ->
            binding.tvMissedCount.text = "$count Missed"
            binding.cardMissedAlert.visibility = if (count > 0) View.VISIBLE else View.GONE
        }

        homeVM.totalToday.observe(viewLifecycleOwner) { total ->
            binding.tvTotalCount.text = "$total Total"
        }

        medicineVM.getExpiredAndExpiring().observe(viewLifecycleOwner) { medicines ->
            val hasWarnings = medicines.isNotEmpty()
            binding.cardExpirySection.visibility = if (hasWarnings) View.VISIBLE else View.GONE
            expiryAdapter.submitList(medicines)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
