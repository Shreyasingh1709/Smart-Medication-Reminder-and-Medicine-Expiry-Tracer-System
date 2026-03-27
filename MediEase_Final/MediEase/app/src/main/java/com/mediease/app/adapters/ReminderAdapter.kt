package com.mediease.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mediease.app.databinding.ItemReminderBinding
import com.mediease.app.models.Reminder

class ReminderAdapter(
    private val onStatusChanged: (Reminder, Boolean) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reminder: Reminder) {
            binding.tvMedicineName.text = reminder.medicineName
            binding.tvTime.text = formatTime(reminder.time)
            
            // Remove listener before setting state to avoid infinite loop
            binding.switchEnabled.setOnCheckedChangeListener(null)
            binding.switchEnabled.isChecked = reminder.isEnabled
            
            binding.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                onStatusChanged(reminder, isChecked)
            }
        }

        private fun formatTime(time: String): String {
            val parts = time.split(":")
            if (parts.size != 2) return time
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            val amPm = if (h >= 12) "PM" else "AM"
            val hour = if (h > 12) h - 12 else if (h == 0) 12 else h
            return String.format("%02d:%02d %s", hour, m, amPm)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(old: Reminder, new: Reminder) = old.id == new.id
        override fun areContentsTheSame(old: Reminder, new: Reminder) = old == new
    }
}
