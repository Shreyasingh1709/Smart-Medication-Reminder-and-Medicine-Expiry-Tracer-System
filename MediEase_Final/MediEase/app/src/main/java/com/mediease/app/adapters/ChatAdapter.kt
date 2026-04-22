package com.mediease.app.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mediease.app.R
import com.mediease.app.databinding.ItemChatMessageBinding
import com.mediease.app.models.ChatMessage

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.message
            
            val params = binding.cvMessageBubble.layoutParams as LinearLayout.LayoutParams
            if (message.isUser) {
                params.gravity = Gravity.END
                binding.cvMessageBubble.setCardBackgroundColor(binding.root.context.getColor(R.color.lavender))
                binding.tvMessage.setTextColor(binding.root.context.getColor(R.color.charcoal))
            } else {
                params.gravity = Gravity.START
                binding.cvMessageBubble.setCardBackgroundColor(binding.root.context.getColor(R.color.white))
                binding.tvMessage.setTextColor(binding.root.context.getColor(R.color.charcoal))
            }
            binding.cvMessageBubble.layoutParams = params
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(old: ChatMessage, new: ChatMessage) = 
            old.timestamp == new.timestamp && old.message == new.message
        override fun areContentsTheSame(old: ChatMessage, new: ChatMessage) = old == new
    }
}
