package com.mediease.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mediease.app.adapters.ChatAdapter
import com.mediease.app.databinding.FragmentChatBinding
import com.mediease.app.models.ChatMessage
import com.mediease.app.network.ApiClient
import com.mediease.app.network.ApiService
import com.mediease.app.network.ChatRequest
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = ChatAdapter()
        binding.rvChat.adapter = adapter
        
        // Initial welcome message
        addMessage(ChatMessage("Hello! I'm MediEase AI. How can I help you with your medications or health today?", false))

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun sendMessage(text: String) {
        addMessage(ChatMessage(text, true))
        binding.etMessage.setText("")
        
        binding.pbLoading.visibility = View.VISIBLE
        
        val api = ApiClient.retrofit.create(ApiService::class.java)
        lifecycleScope.launch {
            try {
                val response = api.chat(ChatRequest(text))
                binding.pbLoading.visibility = View.GONE
                
                if (response.isSuccessful) {
                    response.body()?.reply?.let {
                        addMessage(ChatMessage(it, false))
                    }
                } else {
                    addMessage(ChatMessage("Sorry, I'm having trouble connecting to the brain. Please try again later.", false))
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
