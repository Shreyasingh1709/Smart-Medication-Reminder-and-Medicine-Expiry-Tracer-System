package com.mediease.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mediease.app.databinding.ItemExpiryWarningBinding
import com.mediease.app.models.Medicine

class ExpiryWarningAdapter :
    ListAdapter<Medicine, ExpiryWarningAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemExpiryWarningBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(medicine: Medicine) {
            binding.tvName.text = medicine.name
            binding.tvDosage.text = medicine.dosage
            if (medicine.isExpired()) {
                binding.tvStatus.text = "⚠️ EXPIRED"
                binding.tvStatus.setTextColor(0xFFC62828.toInt())
                binding.tvDaysLeft.text = "Please discard this medicine"
                binding.tvDaysLeft.setTextColor(0xFFC62828.toInt())
            } else {
                val days = medicine.daysUntilExpiry() ?: 0
                binding.tvStatus.text = "⏰ Expires Soon"
                binding.tvStatus.setTextColor(0xFFF57F17.toInt())
                binding.tvDaysLeft.text = "$days days remaining"
                binding.tvDaysLeft.setTextColor(0xFFF9A825.toInt())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemExpiryWarningBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(old: Medicine, new: Medicine) = old.id == new.id
        override fun areContentsTheSame(old: Medicine, new: Medicine) = old == new
    }
}
