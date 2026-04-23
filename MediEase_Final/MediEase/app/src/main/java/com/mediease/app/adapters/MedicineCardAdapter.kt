package com.mediease.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mediease.app.databinding.ItemMedicineCardBinding
import com.mediease.app.models.Medicine

class MedicineCardAdapter(
    private val onMarkTaken: (Medicine) -> Unit = {},
    private val onEdit: (Medicine) -> Unit = {}
) : ListAdapter<Medicine, MedicineCardAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemMedicineCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine) {
            binding.tvMedicineName.text = medicine.name
            binding.tvDosage.text = medicine.dosage
            binding.tvMealTiming.text = medicine.getMealTimingLabel()
            binding.tvMedicineType.text = "${medicine.getTypeEmoji()} ${medicine.type.lowercase().replaceFirstChar { it.uppercase() }}"

            val times = medicine.getReminderTimesList()
            binding.tvReminderTimes.text = times.joinToString(" • ") { formatTime(it) }

            // Expiry status
            when {
                medicine.isExpired() -> {
                    binding.tvExpiryStatus.text = "EXPIRED"
                    binding.tvExpiryStatus.visibility = View.VISIBLE
                    binding.tvExpiryStatus.setBackgroundResource(com.mediease.app.R.drawable.bg_status_expired)
                    binding.tvExpiryStatus.setTextColor(0xFFC62828.toInt())
                }
                medicine.isExpiringSoon() -> {
                    val days = medicine.daysUntilExpiry() ?: 0
                    binding.tvExpiryStatus.text = "Expires in $days days"
                    binding.tvExpiryStatus.visibility = View.VISIBLE
                    binding.tvExpiryStatus.setBackgroundResource(com.mediease.app.R.drawable.bg_status_expiring)
                    binding.tvExpiryStatus.setTextColor(0xFFF57F17.toInt())
                }
                else -> binding.tvExpiryStatus.visibility = View.GONE
            }

            // Image
            if (medicine.imagePath != null) {
                Glide.with(binding.root.context)
                    .load(medicine.imagePath)
                    .centerCrop()
                    .into(binding.ivMedicine)
            } else {
                binding.ivMedicine.setImageResource(com.mediease.app.R.drawable.ic_medicine_placeholder)
            }

            // Mark Taken Button
            binding.btnMarkTaken.setOnClickListener {
                onMarkTaken(medicine)
            }
            
            binding.root.setOnClickListener {
                onEdit(medicine)
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
        ViewHolder(ItemMedicineCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(old: Medicine, new: Medicine) = old.id == new.id
        override fun areContentsTheSame(old: Medicine, new: Medicine) = old == new
    }
}
