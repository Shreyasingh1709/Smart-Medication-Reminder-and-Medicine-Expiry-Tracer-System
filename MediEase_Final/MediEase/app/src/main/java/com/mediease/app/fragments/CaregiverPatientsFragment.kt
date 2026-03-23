package com.mediease.app.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.mediease.app.databinding.FragmentCaregiverPatientsBinding
import com.mediease.app.utils.PrefsManager

class CaregiverPatientsFragment : Fragment() {
    private var _binding: FragmentCaregiverPatientsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCaregiverPatientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = PrefsManager(requireContext())

        if (prefs.linkedUserId.isEmpty()) {
            binding.tvNoPatients.visibility = View.VISIBLE
            binding.rvPatients.visibility = View.GONE
        } else {
            binding.tvNoPatients.visibility = View.GONE
            binding.rvPatients.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
