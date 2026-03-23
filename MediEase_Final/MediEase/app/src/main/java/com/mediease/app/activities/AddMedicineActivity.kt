package com.mediease.app.activities

import android.Manifest
import com.google.android.material.datepicker.MaterialDatePicker
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.mediease.app.R
import com.mediease.app.databinding.ActivityAddMedicineBinding
import com.mediease.app.models.Medicine
import com.mediease.app.viewmodels.MedicineViewModel
import com.mediease.app.viewmodels.ReminderViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddMedicineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicineBinding
    private val medicineVM: MedicineViewModel by viewModels()
    private val reminderVM: ReminderViewModel by viewModels()

    private var expiryDateMillis: Long? = null
    private var tempExpiryDateMillis: Long? = null
    private var reminderTimes = mutableListOf<String>()
    private var tempReminderTime: String? = null
    private var selectedImagePath: String? = null
    private var cameraImageUri: Uri? = null
    private var editMedicineId: Long = -1L

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { loadImage(it.toString()) }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraImageUri?.let { loadImage(it.toString()) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editMedicineId = intent.getLongExtra("medicine_id", -1L)
        if (editMedicineId > 0) {
            binding.tvTitle.text = "Edit Medicine"
            loadExistingMedicine(editMedicineId)
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        // Medicine type chips
        val typeChips = listOf(
            binding.chipTablet, binding.chipCapsule, binding.chipSyrup,
            binding.chipDrops, binding.chipOther
        )
        typeChips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) typeChips.filter { it != chip }.forEach { it.isChecked = false }
            }
        }

        // Meal timing chips
        val mealChips = listOf(binding.chipBefore, binding.chipAfter, binding.chipWith, binding.chipAnytime)
        mealChips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) mealChips.filter { it != chip }.forEach { it.isChecked = false }
            }
        }
        binding.chipAnytime.isChecked = true

        // Expiry date picker
        binding.cardExpiry.setOnClickListener { showDatePicker() }

        // Add time button
        binding.btnAddTime.setOnClickListener { showTimePicker() }

        // Image options
        binding.btnCamera.setOnClickListener { launchCamera() }
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }

        // Repeat days
        setupDayChips()

        // Save
        binding.btnSave.setOnClickListener { saveMedicine() }
    }

    private fun setupDayChips() {
        val dayChips = listOf(
            binding.chipMon, binding.chipTue, binding.chipWed, binding.chipThu,
            binding.chipFri, binding.chipSat, binding.chipSun
        )
        dayChips.forEach { it.isChecked = true }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Expiry Date")
            .setSelection(tempExpiryDateMillis ?: System.currentTimeMillis())
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            tempExpiryDateMillis = selection
            setExpiryDate()
        }
        picker.show(supportFragmentManager, "expiry_picker")
    }

    private fun setExpiryDate() {
        tempExpiryDateMillis?.let {
            expiryDateMillis = it
            binding.tvExpiryDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(expiryDateMillis!!))
            Toast.makeText(this, "Expiry date set!", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "Please pick a date first", Toast.LENGTH_SHORT).show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            val timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            tempReminderTime = timeStr
            setReminderTime()
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
    }

    private fun setReminderTime() {
        tempReminderTime?.let { timeStr ->
            if (!reminderTimes.contains(timeStr)) {
                reminderTimes.add(timeStr)
                updateTimeChips()
                Toast.makeText(this, "Reminder time set!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Time already added", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "Please pick a time first", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimeChips() {
        binding.timeChipsContainer.removeAllViews()
        reminderTimes.forEach { time ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = formatTime(time)
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    reminderTimes.remove(time)
                    updateTimeChips()
                }
            }
            binding.timeChipsContainer.addView(chip)
        }
    }

    private fun formatTime(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return time
        val h = parts[0].toIntOrNull() ?: 0
        val m = parts[1].toIntOrNull() ?: 0
        val amPm = if (h >= 12) "PM" else "AM"
        val hour = if (h > 12) h - 12 else if (h == 0) 12 else h
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour, m, amPm)
    }

    private fun launchCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            return
        }
        val imgFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "med_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", imgFile)
        cameraLauncher.launch(cameraImageUri)
    }

    private fun loadImage(path: String) {
        selectedImagePath = path
        Glide.with(this).load(path).centerCrop().into(binding.ivMedicineImage)
        binding.ivMedicineImage.visibility = View.VISIBLE
        binding.tvImagePlaceholder.visibility = View.GONE
    }

    private fun getSelectedType(): String = when {
        binding.chipCapsule.isChecked -> "CAPSULE"
        binding.chipSyrup.isChecked -> "SYRUP"
        binding.chipDrops.isChecked -> "DROPS"
        binding.chipOther.isChecked -> "OTHER"
        else -> "TABLET"
    }

    private fun getSelectedMealTiming(): String = when {
        binding.chipBefore.isChecked -> "BEFORE_MEAL"
        binding.chipAfter.isChecked -> "AFTER_MEAL"
        binding.chipWith.isChecked -> "WITH_MEAL"
        else -> "ANYTIME"
    }

    private fun getSelectedDays(): String {
        val days = mutableListOf<Int>()
        if (binding.chipMon.isChecked) days.add(1)
        if (binding.chipTue.isChecked) days.add(2)
        if (binding.chipWed.isChecked) days.add(3)
        if (binding.chipThu.isChecked) days.add(4)
        if (binding.chipFri.isChecked) days.add(5)
        if (binding.chipSat.isChecked) days.add(6)
        if (binding.chipSun.isChecked) days.add(7)
        return days.joinToString(",")
    }

    private fun saveMedicine() {
        val name = binding.etMedicineName.text.toString().trim()
        val dosage = binding.etDosage.text.toString().trim()

        if (name.isEmpty()) { binding.etMedicineName.error = "Medicine name is required"; return }
        if (dosage.isEmpty()) { binding.etDosage.error = "Dosage is required"; return }
        if (reminderTimes.isEmpty()) {
            Toast.makeText(this, "Please add at least one reminder time", Toast.LENGTH_SHORT).show()
            return
        }

        val medicine = Medicine(
            id = if (editMedicineId > 0) editMedicineId else 0,
            name = name,
            dosage = dosage,
            type = getSelectedType(),
            frequency = getSelectedFrequency(),
            repeatDays = getSelectedDays(),
            reminderTimes = reminderTimes.joinToString(","),
            mealTiming = getSelectedMealTiming(),
            expiryDate = expiryDateMillis,
            imagePath = selectedImagePath,
            isActive = true,
            notes = binding.etNotes.text.toString().trim()
        )
        medicineVM.saveMedicine(medicine)
    }

    private fun getSelectedFrequency(): String {
        val timesCount = reminderTimes.size
        return when {
            timesCount == 1 -> "DAILY"
            timesCount == 2 -> "TWICE_DAILY"
            timesCount == 3 -> "THREE_TIMES"
            else -> "CUSTOM"
        }
    }

    private fun loadExistingMedicine(id: Long) {
        medicineVM.getMedicineById(id).observe(this) { medicine ->
            medicine ?: return@observe
            binding.etMedicineName.setText(medicine.name)
            binding.etDosage.setText(medicine.dosage)
            binding.etNotes.setText(medicine.notes)
            expiryDateMillis = medicine.expiryDate
            medicine.expiryDate?.let {
                binding.tvExpiryDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
            }
            reminderTimes = medicine.getReminderTimesList().toMutableList()
            updateTimeChips()
            medicine.imagePath?.let { loadImage(it) }
        }
    }

    private fun observeViewModel() {
        medicineVM.saveResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Medicine saved! ✓", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
