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
import com.mediease.app.network.ApiService
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

    private val TAG = "AddMedicineActivity"

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (!uris.isNullOrEmpty()) {
            handleSelectedImages(uris)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                // Ensure camera image is processed with exactly the same logic as gallery selection
                lifecycleScope.launch {
                    delay(1000) // Ensure OS has finished file flushing
                    handleSelectedImages(listOf(uri))
                }
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

        val typeChips = listOf(
            binding.chipTablet, binding.chipCapsule, binding.chipSyrup,
            binding.chipDrops, binding.chipOther
        )
        typeChips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) typeChips.filter { it != chip }.forEach { it.isChecked = false }
            }
        }

        val mealChips = listOf(binding.chipBefore, binding.chipAfter, binding.chipWith, binding.chipAnytime)
        mealChips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) mealChips.filter { it != chip }.forEach { it.isChecked = false }
            }
        }
        binding.chipAnytime.isChecked = true

        binding.cardExpiry.setOnClickListener { showDatePicker() }
        binding.btnAddTime.setOnClickListener { showTimePicker() }
        binding.btnCamera.setOnClickListener { launchCamera() }
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }

        binding.btnSave.setOnClickListener { saveMedicine() }
    }

    private fun handleSelectedImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        loadImage(uris[0].toString())
        uploadAndExtract(uris)
    }

    private fun uploadAndExtract(uris: List<Uri>) {
        val api = ApiClient.retrofit.create(ApiService::class.java)
        
        lifecycleScope.launch {
            try {
                binding.etMedicineName.error = null
                binding.etDosage.error = null
                binding.etNotes.error = null

                val parts = withContext(Dispatchers.IO) {
                    uris.mapNotNull { uri ->
                        processAndGetImagePart(uri)
                    }
                }

                if (parts.isNotEmpty()) {
                    Toast.makeText(this@AddMedicineActivity, "Extracting info...", Toast.LENGTH_SHORT).show()
                    val response = api.uploadAndExtract(parts)
                    
                    if (response.isSuccessful) {
                        val extractedList = response.body()?.medicines
                        if (!extractedList.isNullOrEmpty()) {
                            autoFillForm(extractedList[0])
                        } else {
                            showManualEntryFlags()
                            Toast.makeText(this@AddMedicineActivity, "AI couldn't read the image. Please enter manually.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@AddMedicineActivity, "Server returned error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Extraction Exception", e)
                Toast.makeText(this@AddMedicineActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun processAndGetImagePart(uri: Uri): MultipartBody.Part? = withContext(Dispatchers.IO) {
        try {
            // 1. Get original orientation using ExifInterface (important for Camera images)
            var orientation = ExifInterface.ORIENTATION_NORMAL
            contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }

            // 2. Use inSampleSize to decode bitmap efficiently
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            // Downscale to ~1800px max dimension. This is ideal for OCR.
            // Full 12MP camera images are often too noisy and cause EasyOCR to fail or misread.
            val maxDimension = 1800 
            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            } ?: return@withContext null

            // 3. Manually apply rotation based on EXIF
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            
            val rotatedBitmap = if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            // 85-90% quality is the sweet spot for OCR (less noise than 100%, sharper than 70%)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val bytes = outputStream.toByteArray()
            val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            
            MultipartBody.Part.createFormData("files", "image_${System.currentTimeMillis()}.jpg", requestFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
            null
        }
    }

    private fun showManualEntryFlags() {
        val msg = "We can't able to find this info please enter the details"
        binding.etMedicineName.error = msg
        binding.tvExpiryDate.text = msg
        binding.tvExpiryDate.setTextColor(resources.getColor(R.color.coral, null))
    }

    private fun autoFillForm(extracted: ExtractedMedicine) {
        if (!extracted.name.isNullOrBlank()) {
            binding.etMedicineName.setText(extracted.name)
        }
        if (!extracted.dosage.isNullOrBlank()) {
            binding.etDosage.setText(extracted.dosage)
        }
        if (!extracted.instructions.isNullOrBlank()) {
            binding.etNotes.setText(extracted.instructions)
            suggestReminderTimes(extracted.instructions)
        }
        
        if (!extracted.expiryDate.isNullOrBlank()) {
            // Aggressive cleaning to handle OCR misreads like "3XP" instead of "EXP"
            val cleanedDate = extracted.expiryDate
                .replace(Regex("(?i)[38B]XP[\\.\\s]*[0D]A?T?E?[:\\s]*"), "") // Handles 3XP.DATE, EXP DATE, etc.
                .replace(Regex("(?i)(exp|expiry|exp\\.?|date|valid|until|use\\s*by|lot|batch):?"), "")
                .trim()
            
            parseAndSetExpiryDate(cleanedDate)
            
            // Normalization display (Feature: 11/25 -> 01/11/2025)
            expiryDateMillis?.let {
                val displaySdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.tvExpiryDate.text = displaySdf.format(Date(it))
            } ?: run {
                binding.tvExpiryDate.text = cleanedDate
            }
            
            binding.tvExpiryDate.setTextColor(resources.getColor(R.color.charcoal, null))
        }
        
        val missingFlagMessage = "We can't able to find this info please enter the details"
        extracted.missing_fields?.forEach { field ->
            when (field.lowercase(Locale.getDefault())) {
                "name" -> if (extracted.name.isNullOrBlank()) binding.etMedicineName.error = missingFlagMessage
                "instructions", "notes" -> if (extracted.instructions.isNullOrBlank()) binding.etNotes.error = missingFlagMessage
                "expiry_date", "expiry" -> {
                    if (extracted.expiryDate.isNullOrBlank()) {
                        binding.tvExpiryDate.text = missingFlagMessage
                        binding.tvExpiryDate.setTextColor(resources.getColor(R.color.coral, null))
                    }
                }
            }
        }
    }

    private fun suggestReminderTimes(instructions: String) {
        val user = currentUser ?: return
        val text = instructions.uppercase(Locale.getDefault())
        val suggested = mutableListOf<String>()

        val patternMatch = Regex("(\\d)\\s*[-/]\\s*(\\d)\\s*[-/]\\s*(\\d)").find(text)
        if (patternMatch != null) {
            val (m, a, n) = patternMatch.destructured
            if (m != "0") suggested.add(user.breakfastTime)
            if (a != "0") suggested.add(user.lunchTime)
            if (n != "0") suggested.add(user.dinnerTime)
        } else {
            if (text.contains("MORNING") || text.contains("BREAKFAST") || text.contains("OD")) {
                suggested.add(user.breakfastTime)
            }
            if (text.contains("AFTERNOON") || text.contains("LUNCH") || (text.contains("BD") && suggested.isEmpty())) {
                suggested.add(user.lunchTime)
            }
            if (text.contains("NIGHT") || text.contains("DINNER") || (text.contains("BD") && suggested.size == 1)) {
                suggested.add(user.dinnerTime)
            }
            if (text.contains("BEDTIME") || text.contains("BEFORE BED") || text.contains("HS")) {
                suggested.add(user.bedTime)
            }
            if (text.contains("EARLY MORNING")) {
                suggested.add(user.wakeUpTime)
            }
        }

        if (suggested.isNotEmpty()) {
            reminderTimes.clear()
            reminderTimes.addAll(suggested.distinct())
            updateTimeChips()
        }
    }

    private fun parseAndSetExpiryDate(dateStr: String) {
        val formats = listOf(
            "MM/yyyy", "MM/yy", "dd/MM/yyyy", "yyyy-MM-dd", 
            "MMM yyyy", "MMMM yyyy", "MMM yy", "MMMM yy",
            "MM-yyyy", "MM.yyyy", "MM / yyyy", "MM / yy",
            "MMyy", "MMyyyy"
        )
        for (f in formats) {
            try {
                val sdf = SimpleDateFormat(f, Locale.getDefault())
                val date = sdf.parse(dateStr)
                if (date != null) {
                    expiryDateMillis = date.time
                    break
                }
            } catch (e: Exception) {}
        }
    }

    private fun loadExistingMedicine(id: Long) {
        medicineVM.getMedicineById(id).observe(this) { med ->
            if (med != null) {
                binding.etMedicineName.setText(med.name)
                binding.etDosage.setText(med.dosage)
                binding.etNotes.setText(med.notes)
                
                when (med.type) {
                    "TABLET" -> binding.chipTablet.isChecked = true
                    "CAPSULE" -> binding.chipCapsule.isChecked = true
                    "SYRUP" -> binding.chipSyrup.isChecked = true
                    "DROPS" -> binding.chipDrops.isChecked = true
                    "OTHER" -> binding.chipOther.isChecked = true
                }

                when (med.mealTiming) {
                    "BEFORE_MEAL" -> binding.chipBefore.isChecked = true
                    "AFTER_MEAL" -> binding.chipAfter.isChecked = true
                    "WITH_MEAL" -> binding.chipWith.isChecked = true
                    "ANYTIME" -> binding.chipAnytime.isChecked = true
                }

                med.expiryDate?.let {
                    if (it > 0) {
                        expiryDateMillis = it
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        binding.tvExpiryDate.text = sdf.format(Date(it))
                    }
                }

                val repeatDays = med.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (repeatDays.contains(1)) binding.chipMon.isChecked = true
                if (repeatDays.contains(2)) binding.chipTue.isChecked = true
                if (repeatDays.contains(3)) binding.chipWed.isChecked = true
                if (repeatDays.contains(4)) binding.chipThu.isChecked = true
                if (repeatDays.contains(5)) binding.chipFri.isChecked = true
                if (repeatDays.contains(6)) binding.chipSat.isChecked = true
                if (repeatDays.contains(7)) binding.chipSun.isChecked = true

                reminderTimes.clear()
                reminderTimes.addAll(med.getReminderTimesList())
                updateTimeChips()

                if (!med.imagePath.isNullOrEmpty()) {
                    loadImage(med.imagePath)
                }
            }
        }
    }

    private fun observeViewModel() {
        userVM.currentUser.observe(this) { user ->
            currentUser = user
        }
        
        medicineVM.saveResult.observe(this) { success ->
            if (success == true) {
                Toast.makeText(this, "Medicine Saved Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Expiry Date")
            .setSelection(expiryDateMillis ?: MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener {
            expiryDateMillis = it
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvExpiryDate.text = sdf.format(Date(it))
            binding.tvExpiryDate.setTextColor(resources.getColor(R.color.charcoal, null))
        }
        picker.show(supportFragmentManager, "date_picker")
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            val time = String.format("%02d:%02d", h, m)
            if (!reminderTimes.contains(time)) {
                reminderTimes.add(time)
                updateTimeChips()
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }

    private fun updateTimeChips() {
        binding.timeChipsContainer.removeAllViews()
        reminderTimes.sort()
        reminderTimes.forEach { time ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = formatTime(time)
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                reminderTimes.remove(time)
                updateTimeChips()
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            return
        }
        val imgFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "med_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", imgFile)
        cameraLauncher.launch(cameraImageUri)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        }
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

    private fun getSelectedFrequency(): String {
        val timesCount = reminderTimes.size
        return when {
            timesCount == 1 -> "DAILY"
            timesCount == 2 -> "TWICE_DAILY"
            timesCount == 3 -> "THREE_TIMES"
            else -> "CUSTOM"
        }
    }

    private fun saveMedicine() {
        val name = binding.etMedicineName.text.toString().trim()
        val dosage = binding.etDosage.text.toString().trim()
        if (name.isEmpty() || reminderTimes.isEmpty()) {
            Toast.makeText(this, "Please fill required fields (Name and Time)", Toast.LENGTH_SHORT).show()
            return
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
    }
}
