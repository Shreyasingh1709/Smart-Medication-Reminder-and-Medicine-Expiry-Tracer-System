package com.mediease.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mediease.app.adapters.ReminderAdapter
import com.mediease.app.databinding.FragmentRemindersBinding
import com.mediease.app.viewmodels.ReminderViewModel

class RemindersFragment : Fragment() {
    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReminderViewModel by viewModels()
    private lateinit var adapter: ReminderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = ReminderAdapter { reminder, isEnabled ->
            viewModel.updateReminderStatus(reminder, isEnabled)
        }
        
        binding.rvReminders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RemindersFragment.adapter
        }

        // Show only DAILY reminders as requested
        viewModel.dailyReminders.observe(viewLifecycleOwner) { reminders ->
            Log.d("RemindersFragment", "Loaded ${reminders.size} daily reminders from DB")
            adapter.submitList(reminders)
            
            if (reminders.isEmpty()) {
                binding.tvNoReminders.visibility = View.VISIBLE
                binding.rvReminders.visibility = View.GONE
            } else {
                binding.tvNoReminders.visibility = View.GONE
                binding.rvReminders.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
