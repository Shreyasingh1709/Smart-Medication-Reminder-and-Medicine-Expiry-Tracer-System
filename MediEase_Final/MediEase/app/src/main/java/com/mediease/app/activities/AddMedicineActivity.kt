package com.mediease.app.activities

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.mediease.app.R
import com.mediease.app.databinding.ActivityAddMedicineBinding
import com.mediease.app.models.Medicine
import com.mediease.app.models.User
import com.mediease.app.network.ApiClient
import com.mediease.app.network.ExtractedMedicine
import com.mediease.app.utils.PrefsManager
import com.mediease.app.viewmodels.MedicineViewModel
import com.mediease.app.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddMedicineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicineBinding
    private val medicineVM: MedicineViewModel by viewModels()
    private val userVM: UserViewModel by viewModels()

    private var expiryDateMillis: Long? = null
    private var reminderTimes = mutableListOf<String>()
    private var selectedImagePath: String? = null
    private var cameraImageUri: Uri? = null
    private var editMedicineId: Long = -1L
    private var currentUser: User? = null
    
    private var extractedMedicinesQueue = mutableListOf<ExtractedMedicine>()
    private var currentQueueIndex = 0

    private val TAG = "AddMedicineActivity"

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (!uris.isNullOrEmpty()) handleSelectedImages(uris)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                lifecycleScope.launch { delay(1000); handleSelectedImages(listOf(uri)) }
            }
        }
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
        val prefs = PrefsManager(this)
        userVM.loadCurrentUserById(prefs.userId)
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        binding.cardExpiry.setOnClickListener { showDatePicker() }
        binding.btnAddTime.setOnClickListener { showTimePicker() }
        binding.btnCamera.setOnClickListener { launchCamera() }
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnSave.setOnClickListener { saveMedicine(true) }
        binding.btnSaveAndNext.setOnClickListener { saveMedicine(false) }
    }

    private fun handleSelectedImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        loadImage(uris[0].toString())
        uploadAndExtract(uris)
    }

    private fun uploadAndExtract(uris: List<Uri>) {
        lifecycleScope.launch {
            try {
                binding.etMedicineName.error = null
                val parts = withContext(Dispatchers.IO) {
                    uris.mapNotNull { uri -> processAndGetImagePart(uri) }
                }
                if (parts.isNotEmpty()) {
                    Toast.makeText(this@AddMedicineActivity, "Processing Image...", Toast.LENGTH_SHORT).show()
                    val response = ApiClient.apiService.uploadAndExtract(parts)
                    if (response.isSuccessful) {
                        val extractedList = response.body()?.medicines
                        if (!extractedList.isNullOrEmpty()) {
                            extractedMedicinesQueue = extractedList.toMutableList()
                            currentQueueIndex = 0
                            showExtractedMedicine(extractedMedicinesQueue[0])
                        } else {
                            Toast.makeText(this@AddMedicineActivity, "No data extracted. Please enter manually.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@AddMedicineActivity, "Extraction failed: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Extraction Error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddMedicineActivity, "Server connection failed. Check IP & Server status.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showExtractedMedicine(extracted: ExtractedMedicine) {
        if (extractedMedicinesQueue.size > 1) {
            binding.tvQueueStatus.visibility = View.VISIBLE
            binding.tvQueueStatus.text = "${currentQueueIndex + 1} of ${extractedMedicinesQueue.size}"
            binding.btnSaveAndNext.visibility = if (currentQueueIndex < extractedMedicinesQueue.size - 1) View.VISIBLE else View.GONE
        } else {
            binding.tvQueueStatus.visibility = View.GONE
            binding.btnSaveAndNext.visibility = View.GONE
        }
        autoFillForm(extracted)
    }

    private fun autoFillForm(extracted: ExtractedMedicine) {
        // Fix for "default name" bug: Explicitly clear or set current name
        binding.etMedicineName.setText(extracted.name ?: "")
        binding.etDosage.setText(extracted.dosage ?: "")
        
        var instructions = extracted.instructions ?: ""
        
        // Handle Durations (e.g., 15 days, 20 duyi, etc)
        val durationRegex = Regex("(\\d+)\\s*(DAYS|DAYT|DUYI|DARS|WEEKS)", RegexOption.IGNORE_CASE)
        val durationMatch = durationRegex.find(instructions)
        if (durationMatch != null) {
            val durationText = durationMatch.value
            if (!instructions.contains(durationText, ignoreCase = true)) {
                instructions += " ($durationText)"
            }
        }
        binding.etNotes.setText(instructions)

        val upperText = instructions.uppercase()
        val nameAndInstr = "${extracted.name} ${extracted.dosage} $instructions".uppercase()

        // 1. Medicine Type Detection (Expanded Abbreviations)
        when {
            nameAndInstr.contains("TAB") || nameAndInstr.contains("MG") || nameAndInstr.contains("MCG") -> binding.chipTablet.isChecked = true
            nameAndInstr.contains("CAP") || nameAndInstr.contains("CAPSULA") -> binding.chipCapsule.isChecked = true
            nameAndInstr.contains("SYR") || nameAndInstr.contains("ML") || nameAndInstr.contains("ELIX") || 
                    nameAndInstr.contains("SOL") || nameAndInstr.contains("SUSP") || nameAndInstr.contains("LIQUID") -> binding.chipSyrup.isChecked = true
            nameAndInstr.contains("DROP") || nameAndInstr.contains("GUTT") -> binding.chipDrops.isChecked = true
            else -> binding.chipTablet.isChecked = true
        }

        // 2. Meal Timing Detection (ac/pc/af/after food)
        when {
            upperText.contains("BEFORE MEAL") || upperText.contains("EMPTY STOMACH") || upperText.contains("AC ") -> binding.chipBefore.isChecked = true
            upperText.contains("AFTER MEAL") || upperText.contains("AFTER FOOD") || upperText.contains("PC ") || upperText.contains("AF ") -> binding.chipAfter.isChecked = true
            upperText.contains("WITH MEAL") || upperText.contains("WITH FOOD") -> binding.chipWith.isChecked = true
            else -> binding.chipAnytime.isChecked = true
        }
        
        // Handle Expiry: Use word boundaries and prioritize the last match (often EXP is after MFG)
        val fullTextForExpiry = "$instructions ${extracted.expiryDate ?: ""}".uppercase()
        val expiryKeywordsRegex = Regex("\\b(?:EXP|EXPIRY|E\\.D\\.|ED|EXP\\.|VALID\\s*TILL|VAL)\\b\\D*(\\d{1,2}[-/.]\\d{2,4})")
        val keywordMatch = expiryKeywordsRegex.findAll(fullTextForExpiry).lastOrNull()
        
        if (keywordMatch != null) {
            parseAndSetExpiryDate(keywordMatch.groupValues[1])
        } else if (!extracted.expiryDate.isNullOrBlank()) {
            parseAndSetExpiryDate(extracted.expiryDate)
        }

        // 3. Auto-calculate Reminders (Personalized)
        suggestReminderTimes(instructions)
    }

    private fun suggestReminderTimes(instructions: String) {
        val user = currentUser ?: return
        val text = instructions.uppercase(Locale.getDefault())
            // Correct garbled OCR dosage symbols
            .replace("O", "0")
            .replace(Regex("[Nn]-"), "1-")
            .replace(Regex("[Ff]-"), "1-")
            .replace("I", "1")
            .replace("J", "")

        val suggested = mutableListOf<String>()
        val mealTiming = getSelectedMealTiming()

        // Handle Patterns like 1-0-1 or 1-1-1 or 1-0-0
        val pattern = Regex("(\\d)\\s*[-/ ]\\s*(\\d)\\s*[-/ ]\\s*(\\d)")
        val match = pattern.find(text)
        
        if (match != null) {
            val (m, a, n) = match.destructured
            if (m != "0") suggested.add(calculateReminderTime(user.breakfastTime, mealTiming))
            if (a != "0") suggested.add(calculateReminderTime(user.lunchTime, mealTiming))
            if (n != "0") suggested.add(calculateReminderTime(user.dinnerTime, mealTiming))
        } else {
            // Backup keywords
            if (text.contains("MORNING") || text.contains("BREAKFAST")) 
                suggested.add(calculateReminderTime(user.breakfastTime, mealTiming))
            if (text.contains("AFTERNOON") || text.contains("LUNCH") || text.contains("NOON")) 
                suggested.add(calculateReminderTime(user.lunchTime, mealTiming))
            if (text.contains("NIGHT") || text.contains("DINNER") || text.contains("EVENING")) 
                suggested.add(calculateReminderTime(user.dinnerTime, mealTiming))
        }

        // Specific rules requested: Bedtime (30 mins before), Wake up (30 mins after)
        if (text.contains("BEDTIME") || text.contains("BEFORE SLEEP") || text.contains("HS") || text.contains("NIGHT")) {
            val bedtimeReminder = addMinutes(user.bedTime, -30)
            if (!suggested.contains(bedtimeReminder)) suggested.add(bedtimeReminder)
        }
        
        if (text.contains("WAKE UP") || text.contains("EARLY MORNING") || text.contains("FASTING")) {
            val wakeUpReminder = addMinutes(user.wakeUpTime, 30)
            if (!suggested.contains(wakeUpReminder)) suggested.add(wakeUpReminder)
        }

        if (suggested.isNotEmpty()) {
            reminderTimes.clear()
            reminderTimes.addAll(suggested.distinct())
            updateTimeChips()
        }
    }

    private fun calculateReminderTime(baseTime: String, mealTiming: String): String {
        return when (mealTiming) {
            "AFTER_MEAL" -> addMinutes(baseTime, 30)
            "BEFORE_MEAL" -> addMinutes(baseTime, -30)
            else -> baseTime
        }
    }

    private fun addMinutes(time: String, minutes: Int): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf.parse(time) ?: return time
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MINUTE, minutes)
            sdf.format(cal.time)
        } catch (e: Exception) { time }
    }

    private fun saveMedicine(finishActivity: Boolean) {
        val name = binding.etMedicineName.text.toString().trim()
        val dosage = binding.etDosage.text.toString().trim()
        if (name.isEmpty() || reminderTimes.isEmpty() || (expiryDateMillis == null && !finishActivity)) {
            // Note: Allow save without expiry only if it's the final save or we'll handle it
            if (name.isEmpty()) {
                binding.etMedicineName.error = "Required"
                return
            }
        }

        val medicine = Medicine(
            id = if (editMedicineId > 0) editMedicineId else 0,
            userId = PrefsManager(this).userId,
            name = name,
            type = getSelectedType(),
            dosage = dosage,
            mealTiming = getSelectedMealTiming(),
            expiryDate = expiryDateMillis,
            repeatDays = getSelectedDays(),
            reminderTimes = reminderTimes.joinToString(","),
            frequency = getSelectedFrequency(),
            notes = binding.etNotes.text.toString().trim(),
            imagePath = selectedImagePath,
            isActive = true
        )

        medicineVM.saveMedicine(medicine)
        
        if (!finishActivity && currentQueueIndex < extractedMedicinesQueue.size - 1) {
            Toast.makeText(this, "Saved $name! Loading next...", Toast.LENGTH_SHORT).show()
            clearFormForNext()
            currentQueueIndex++
            showExtractedMedicine(extractedMedicinesQueue[currentQueueIndex])
        } else {
            finish()
        }
    }

    private fun clearFormForNext() {
        binding.etMedicineName.text.clear()
        binding.etDosage.text.clear()
        binding.etNotes.text.clear()
        reminderTimes.clear()
        updateTimeChips()
        // Keep image path and base schedule
    }

    private fun observeViewModel() {
        userVM.currentUser.observe(this) { user ->
            currentUser = user
            user?.let {
                binding.chipBaseBreakfast.text = "Breakfast: ${it.breakfastTime}"
                binding.chipBaseLunch.text = "Lunch: ${it.lunchTime}"
                binding.chipBaseDinner.text = "Dinner: ${it.dinnerTime}"
            }
        }
    }

    private fun parseAndSetExpiryDate(dateStr: String) {
        // OCR often misreads separators or uses dots/dashes
        val normalized = dateStr.replace(".", "/").replace("-", "/").trim()
        val formats = listOf("MM/yy", "MM/yyyy", "dd/MM/yyyy", "yyyy/MM/dd", "MMM yyyy", "MM-yyyy")
        
        for (f in formats) {
            try {
                val sdf = SimpleDateFormat(f, Locale.getDefault())
                sdf.isLenient = false
                val date = sdf.parse(normalized)
                if (date != null) {
                    expiryDateMillis = date.time
                    binding.tvExpiryDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
                    return
                }
            } catch (e: Exception) { }
        }
    }

    private suspend fun processAndGetImagePart(uri: Uri): MultipartBody.Part? = withContext(Dispatchers.IO) {
        try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            val exif = ExifInterface(bytes.inputStream())
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            val maxDimension = 1500 
            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }
            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return@withContext null
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            val rotatedBitmap = if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != 0) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else { bitmap }
            val outputStream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val finalBytes = outputStream.toByteArray()
            val requestFile = finalBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", "image_${System.currentTimeMillis()}.jpg", requestFile)
        } catch (e: Exception) { null }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker().setTitleText("Expiry Date").setSelection(expiryDateMillis ?: MaterialDatePicker.todayInUtcMilliseconds()).build()
        picker.addOnPositiveButtonClickListener {
            expiryDateMillis = it
            binding.tvExpiryDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
        }
        picker.show(supportFragmentManager, "date_picker")
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            val time = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            if (!reminderTimes.contains(time)) { reminderTimes.add(time); updateTimeChips() }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }

    private fun updateTimeChips() {
        binding.timeChipsContainer.removeAllViews()
        reminderTimes.sort()
        reminderTimes.forEach { time ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = formatTime(time)
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener { reminderTimes.remove(time); updateTimeChips() }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100); return
        }
        val imgFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "med_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", imgFile)
        cameraLauncher.launch(cameraImageUri)
    }

    private fun loadImage(path: String) {
        selectedImagePath = path
        Glide.with(this).load(path).centerInside().into(binding.ivMedicineImage)
        binding.ivMedicineImage.visibility = View.VISIBLE
        binding.layoutImagePlaceholder.visibility = View.GONE
    }

    private fun getSelectedType(): String = when {
        binding.chipCapsule.isChecked -> "CAPSULE"; binding.chipSyrup.isChecked -> "SYRUP"
        binding.chipDrops.isChecked -> "DROPS"; else -> "TABLET"
    }

    private fun getSelectedMealTiming(): String = when {
        binding.chipBefore.isChecked -> "BEFORE_MEAL"; binding.chipAfter.isChecked -> "AFTER_MEAL"
        binding.chipWith.isChecked -> "WITH_MEAL"; else -> "ANYTIME"
    }

    private fun getSelectedDays(): String {
        return "1,2,3,4,5,6,7" // Default to all days since chips were removed from layout
    }

    private fun getSelectedFrequency(): String {
        val count = reminderTimes.size
        return when { count == 1 -> "DAILY"; count == 2 -> "TWICE_DAILY"; count == 3 -> "THREE_TIMES"; else -> "CUSTOM" }
    }

    private fun loadExistingMedicine(id: Long) {
        medicineVM.getMedicineById(id).observe(this) { med ->
            if (med != null) {
                binding.etMedicineName.setText(med.name); binding.etDosage.setText(med.dosage); binding.etNotes.setText(med.notes)
                when (med.type) { "TABLET" -> binding.chipTablet.isChecked = true; "CAPSULE" -> binding.chipCapsule.isChecked = true; "SYRUP" -> binding.chipSyrup.isChecked = true; "DROPS" -> binding.chipDrops.isChecked = true }
                when (med.mealTiming) { "BEFORE_MEAL" -> binding.chipBefore.isChecked = true; "AFTER_MEAL" -> binding.chipAfter.isChecked = true; "WITH_MEAL" -> binding.chipWith.isChecked = true; "ANYTIME" -> binding.chipAnytime.isChecked = true }
                med.expiryDate?.let { expiryDateMillis = it; binding.tvExpiryDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) }
                reminderTimes.clear(); reminderTimes.addAll(med.getReminderTimesList()); updateTimeChips()
                if (!med.imagePath.isNullOrEmpty()) loadImage(med.imagePath)
            }
        }
    }
}
