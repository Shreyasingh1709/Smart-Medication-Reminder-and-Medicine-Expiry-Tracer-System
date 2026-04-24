package com.mediease.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mediease.app.adapters.ChatAdapter
import com.mediease.app.databinding.FragmentChatBinding
import com.mediease.app.models.ChatMessage
import com.mediease.app.network.ApiClient
import com.mediease.app.network.ApiService
import com.mediease.app.network.ChatRequest
import com.mediease.app.utils.PrefsManager
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var prefs: PrefsManager

    private val faqAnswers = mapOf(
        "How to add a medicine?" to "You can add a medicine by clicking the '+' button on the Home screen or navigating to the 'Medicines' tab and selecting 'Add New'.",
        "What is adherence rate?" to "Adherence rate is the percentage of doses you took on time compared to the total scheduled doses. A high rate means you are following your prescription well!",
        "How to set meal timing?" to "When adding or editing a medicine, look for the 'Meal Timing' section. You can choose Before Meal, After Meal, With Meal, or Any Time.",
        "Explain 1-0-1 pattern" to "The 1-0-1 pattern means taking one dose in the morning, none in the afternoon, and one at night.",
        "How to edit profile?" to "Go to the 'Profile' tab or click on your user icon to update your personal details and base reminder times."
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefs = PrefsManager(requireContext())
        adapter = ChatAdapter()
        binding.rvChat.adapter = adapter
        
        setupSuggestions()
        
        // Initial welcome message
        if (messages.isEmpty()) {
            addMessage(ChatMessage("Hello! I'm MediEase AI. How can I help you with your medications or health today?", false))
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun setupSuggestions() {
        binding.layoutSuggestions.removeAllViews()
        faqAnswers.keys.forEach { suggestion ->
            val chip = Chip(requireContext()).apply {
                text = suggestion
                isClickable = true
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    sendMessage(suggestion)
                }
            }
            binding.layoutSuggestions.addView(chip)
        }
    }

    private fun sendMessage(text: String) {
        addMessage(ChatMessage(text, true))
        binding.etMessage.setText("")
        
        // Check for local FAQ first
        val localAnswer = faqAnswers[text]
        if (localAnswer != null) {
            addMessage(ChatMessage(localAnswer, false))
            return
        }

        binding.pbLoading.visibility = View.VISIBLE
        
        val api = ApiClient.retrofit.create(ApiService::class.java)
        lifecycleScope.launch {
            try {
                val response = api.chat(ChatRequest(text, prefs.userId))
                binding.pbLoading.visibility = View.GONE
                
                if (response.isSuccessful) {
                    response.body()?.reply?.let {
                        addMessage(ChatMessage(it, false))
                    }
                } else {
                    addMessage(ChatMessage("Sorry, I'm having trouble connecting to the brain (Error ${response.code()}). Please try again later.", false))
                }
            } catch (e: Exception) {
                binding.pbLoading.visibility = View.GONE
                addMessage(ChatMessage("Error: ${e.message}", false))
            }
        }
    }

    private fun addMessage(chatMessage: ChatMessage) {
        messages.add(chatMessage)
        adapter.submitList(messages.toList()) {
            binding.rvChat.scrollToPosition(messages.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
