
package com.mediease.app.fragments
import com.mediease.app.R

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mediease.app.activities.LoginActivity
import com.mediease.app.activities.PatientSetupActivity
import com.mediease.app.databinding.FragmentProfileBinding
import com.mediease.app.utils.PrefsManager
import com.mediease.app.viewmodels.UserViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()
    private lateinit var prefs: PrefsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PrefsManager(requireContext())

        setupUI()
        observeUser()
        
        // Load the current user data from the database/repository
        viewModel.loadCurrentUserById(prefs.userId)
    }

    private fun setupUI() {
        val isPatient = prefs.userRole == PrefsManager.ROLE_PATIENT

        binding.tvUserBadge.text = if (isPatient) "Patient" else "Caregiver"
        binding.cardConnectCaregiver.visibility = if (isPatient) View.VISIBLE else View.GONE

        binding.btnEditSchedule.setOnClickListener {
            startActivity(Intent(requireContext(), PatientSetupActivity::class.java))
        }

        binding.btnCopyCode.setOnClickListener {
            val code = binding.tvProfileCode.text.toString()
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                    as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Profile Code", code))
            Toast.makeText(requireContext(), "Code copied! 📋", Toast.LENGTH_SHORT).show()
        }

        // Open Notification Settings
        binding.root.findViewById<android.widget.LinearLayout>(R.id.notification_settings_row)?.setOnClickListener {
            startActivity(Intent(requireContext(), com.mediease.app.activities.NotificationSettingsActivity::class.java))
        }

        // Open Help & Support
        binding.root.findViewById<android.widget.LinearLayout>(R.id.help_support_row)?.setOnClickListener {
            startActivity(Intent(requireContext(), com.mediease.app.activities.HelpSupportActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            // Only set logged in to false, don't clear everything so we "remember" the account
            prefs.isLoggedIn = false
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun observeUser() {
        // Initial set from preferences
        binding.tvUserName.text = if (prefs.userName.isNotEmpty()) prefs.userName else "Your Name"
        binding.tvUserEmail.text = if (prefs.userEmail.isNotEmpty()) prefs.userEmail else ""
        binding.tvProfileCode.text = if (prefs.profileCode.isNotEmpty()) prefs.profileCode else "MED-XXXXX"

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.name.ifEmpty { prefs.userName }.ifEmpty { "Your Name" }
                binding.tvUserEmail.text = user.email.ifEmpty { prefs.userEmail }
                binding.tvProfileCode.text = user.profileCode.ifEmpty { prefs.profileCode }
                binding.tvWakeTime.text = user.wakeUpTime
                binding.tvBedTime.text = user.bedTime
                binding.tvBreakfastTime.text = user.breakfastTime
                binding.tvDinnerTime.text = user.dinnerTime
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
