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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
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
    private var isCurrentlyPrescription = false

    private val TAG = "AddMedicineActivity"

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (!uris.isNullOrEmpty()) handleSelectedImages(uris)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                handleSelectedImages(listOf(uri))
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
        val uid = prefs.userId
        if (uid.isNotEmpty()) {
            userVM.loadCurrentUserById(uid)
        } else {
            currentUser = User()
            updateBaseTimeChips(currentUser!!)
        }
    }

    private fun loadExistingMedicine(id: Long) {
        medicineVM.getMedicineById(id).observe(this) { medicine ->
            medicine?.let {
                binding.etMedicineName.setText(it.name)
                binding.etDosage.setText(it.dosage)
                binding.etNotes.setText(it.notes)
                
                expiryDateMillis = it.expiryDate
                if (expiryDateMillis != null) {
                    binding.tvExpiryDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expiryDateMillis!!))
                }

                reminderTimes.clear()
                if (it.reminderTimes.isNotEmpty()) {
                    reminderTimes.addAll(it.reminderTimes.split(",").filter { t -> t.isNotEmpty() })
                }
                updateTimeChips()

                when (it.type) {
                    "CAPSULE" -> binding.chipCapsule.isChecked = true
                    "SYRUP" -> binding.chipSyrup.isChecked = true
                    "DROPS" -> binding.chipDrops.isChecked = true
                    else -> binding.chipTablet.isChecked = true
                }

                when (it.mealTiming) {
                    "BEFORE_MEAL" -> binding.chipBefore.isChecked = true
                    "AFTER_MEAL" -> binding.chipAfter.isChecked = true
                    "WITH_MEAL" -> binding.chipWith.isChecked = true
                    else -> binding.chipAnytime.isChecked = true
                }

                val days = it.repeatDays.split(",")
                binding.chipMon.isChecked = days.contains("1")
                binding.chipTue.isChecked = days.contains("2")
                binding.chipWed.isChecked = days.contains("3")
                binding.chipThu.isChecked = days.contains("4")
                binding.chipFri.isChecked = days.contains("5")
                binding.chipSat.isChecked = days.contains("6")
                binding.chipSun.isChecked = days.contains("7")

                if (!it.imagePath.isNullOrEmpty()) {
                    loadImage(it.imagePath)
                }
            }
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        binding.cardExpiry.setOnClickListener { showDatePicker() }
        binding.btnAddTime.setOnClickListener { showTimePicker() }
        binding.btnCamera.setOnClickListener { launchCamera() }
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnSave.setOnClickListener { saveMedicine(true) }
        binding.btnSaveAndNext.setOnClickListener { saveMedicine(false) }
        
        binding.chipMon.isChecked = true
        binding.chipTue.isChecked = true
        binding.chipWed.isChecked = true
        binding.chipThu.isChecked = true
        binding.chipFri.isChecked = true
        binding.chipSat.isChecked = true
        binding.chipSun.isChecked = true
    }

    private fun handleSelectedImages(uris: List<Uri>) {
        loadImage(uris[0].toString())
        uploadAndExtract(uris)
    }

    private fun uploadAndExtract(uris: List<Uri>) {
        lifecycleScope.launch {
            try {
                binding.etMedicineName.error = null
                binding.btnSave.isEnabled = false
                
                val parts = withContext(Dispatchers.IO) {
                    uris.mapNotNull { uri -> processAndGetImagePart(uri) }
                }
                
                if (parts.isNotEmpty()) {
                    Toast.makeText(this@AddMedicineActivity, "Uploading to Server...", Toast.LENGTH_SHORT).show()
                    val response = ApiClient.apiService.uploadAndExtract(parts)
                    
                    if (response.isSuccessful) {
                        var extractedList = response.body()?.medicines ?: emptyList()
                        
                        val allText = (extractedList.joinToString(" ") { 
                            (it.name ?: "") + " " + (it.instructions ?: "") + " " + (it.dosage ?: "")
                        }).uppercase()

                        isCurrentlyPrescription = allText.contains("CONSULTANT") || 
                                                allText.contains("CONSULTATION") || 
                                                allText.contains("PATIENT") ||
                                                allText.contains("HOSPITAL") ||
                                                allText.contains("DR.") ||
                                                allText.contains("OPD")

                        if (isCurrentlyPrescription) {
                            Log.d(TAG, "Detected as PRESCRIPTION.")
                            if (extractedList.size <= 1) {
                                val rawText = extractedList.firstOrNull()?.instructions ?: ""
                                val trimmed = trimPrescriptionText(rawText)
                                if (trimmed.length > 10) {
                                    val splitList = splitPrescriptionIntoMedicines(trimmed)
                                    if (splitList.isNotEmpty()) extractedList = splitList
                                }
                            }
                        }

                        if (extractedList.isNotEmpty()) {
                            extractedMedicinesQueue = extractedList.toMutableList()
                            currentQueueIndex = 0
                            showExtractedMedicine(extractedMedicinesQueue[0])
                        } else {
                            Toast.makeText(this@AddMedicineActivity, "No data extracted.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e(TAG, "Server Error: ${response.code()}")
                        Toast.makeText(this@AddMedicineActivity, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Extraction Error", e)
                Toast.makeText(this@AddMedicineActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnSave.isEnabled = true
            }
        }
    }

    private fun showExtractedMedicine(extracted: ExtractedMedicine) {
        if (extractedMedicinesQueue.size > 1) {
            binding.tvQueueStatus.visibility = View.VISIBLE
            binding.tvQueueStatus.text = "${currentQueueIndex + 1} of ${extractedMedicinesQueue.size}"
            binding.btnSaveAndNext.visibility = if (currentQueueIndex < extractedMedicinesQueue.size - 1) View.VISIBLE else View.GONE
            binding.btnSave.text = if (currentQueueIndex == extractedMedicinesQueue.size - 1) "💾 Save & Finish" else "💾 Save Current"
        } else {
            binding.tvQueueStatus.visibility = View.GONE
            binding.btnSaveAndNext.visibility = View.GONE
            binding.btnSave.text = "💾 Save & Finish"
        }
        autoFillForm(extracted)
    }

    private fun autoFillForm(extracted: ExtractedMedicine) {
        val rawName = extracted.name?.trim() ?: ""
        val cleanName = if (isCurrentlyPrescription) {
            rawName.replace(Regex("(?i)Cream|Lotion|Tablets|Tablet|Solution|Ointment"), "").trim()
        } else { rawName }
        
        val cleanDosage = cleanDosageOcr(extracted.dosage ?: "")
        binding.etMedicineName.setText(cleanName)
        binding.etDosage.setText(cleanDosage)
        
        val rawInstructions = extracted.instructions ?: ""
        val notesText = if (isCurrentlyPrescription) trimPrescriptionText(rawInstructions) else rawInstructions
        binding.etNotes.setText(notesText)

        // Reset expiry date before detection
        expiryDateMillis = null
        binding.tvExpiryDate.text = "Select Date"

        // 1. Check server-provided expiry date first
        if (!extracted.expiryDate.isNullOrEmpty()) {
            parseAndSetExpiryDate(extracted.expiryDate)
        }

        val combinedRawText = "$rawName $cleanDosage $rawInstructions".uppercase()

        // 2. Improved Universal Expiry Detection (for Strip OCR)
        if (expiryDateMillis == null) {
            // Regex that matches EXP labels followed by a date, allowing for optional "DATE" word and various separators
            val expiryKeywordsRegex = Regex("(?i)\\b(EXP|EXPIRY|E\\.?D\\.?|EXP\\.?\\s*DATE|VALID\\s*TILL|VAL|EXPIRATION)\\b\\D{0,10}(\\d{1,2}[-/.]\\d{2,4})")
            val keywordMatch = expiryKeywordsRegex.find(combinedRawText)
            
            if (keywordMatch != null) {
                parseAndSetExpiryDate(keywordMatch.groupValues[2])
            } else {
                // 3. Fallback: Find all dates and pick the one that isn't the manufacturing date
                val fallbackDateRegex = Regex("(\\d{1,2}[-/.]\\d{2,4})")
                val allDates = fallbackDateRegex.findAll(combinedRawText).map { it.value }.toList()
                
                if (allDates.isNotEmpty()) {
                    val mfgRegex = Regex("(?i)MFG\\.?\\s*(?:DATE)?\\D{0,5}(\\d{1,2}[-/.]\\d{2,4})")
                    val mfgMatch = mfgRegex.find(combinedRawText)
                    val mfgDate = mfgMatch?.groupValues?.get(1)
                    
                    // Expiry is usually the latest date or the one NOT labeled as MFG
                    val candidateDate = allDates.find { it != mfgDate } ?: allDates.last()
                    parseAndSetExpiryDate(candidateDate)
                }
            }
        }

        // Type Detection
        when {
            combinedRawText.contains("CREAM") || combinedRawText.contains("OINTMENT") || 
            combinedRawText.contains("GEL") || combinedRawText.contains("LOTION") -> {
                binding.chipSyrup.isChecked = true
                binding.chipAnytime.isChecked = true
            }
            combinedRawText.contains("BEFORE MEAL") || combinedRawText.contains("EMPTY STOMACH") -> binding.chipBefore.isChecked = true
            combinedRawText.contains("AFTER MEAL") || combinedRawText.contains("AFTER FOOD") -> binding.chipAfter.isChecked = true
            combinedRawText.contains("WITH MEAL") -> binding.chipWith.isChecked = true
            else -> binding.chipAnytime.isChecked = true
        }

        if (!binding.chipSyrup.isChecked) {
            when {
                combinedRawText.contains("TAB") || combinedRawText.contains("MG") -> binding.chipTablet.isChecked = true
                combinedRawText.contains("CAP") -> binding.chipCapsule.isChecked = true
                combinedRawText.contains("SYR") -> binding.chipSyrup.isChecked = true
                combinedRawText.contains("DROP") -> binding.chipDrops.isChecked = true
                else -> binding.chipTablet.isChecked = true
            }
        }
        
        suggestReminderTimes(rawInstructions)
    }

    private fun trimPrescriptionText(text: String): String {
        if (text.isBlank()) return ""
        val upperText = text.uppercase()
        val startMarkers = listOf("PRESCRIPTION", "PRESCRIPTIONS", "RX", "R/X")
        var startIndex = 0
        for (marker in startMarkers) {
            val idx = upperText.indexOf(marker)
            if (idx != -1) { startIndex = idx + marker.length; break }
        }
        val endMarkers = listOf("SPECIAL ADVICE", "NEXT FOLLOW", "OPD TIMINGS", "SIGNATURE")
        var endIndex = text.length
        val remainingText = upperText.substring(startIndex)
        for (marker in endMarkers) {
            val idx = remainingText.indexOf(marker)
            if (idx != -1) { endIndex = startIndex + idx; break }
        }
        return text.substring(startIndex, endIndex).trim()
    }

    private fun splitPrescriptionIntoMedicines(trimmedText: String): List<ExtractedMedicine> {
        val medicines = mutableListOf<ExtractedMedicine>()
        val blocks = trimmedText.split(Regex("(?i)(?:\\()?(?:Dispense|Dk|ddpentr|Dispensc|Dknanse|Dipense|Dipensc|Disp|Dispen)[^\\d)]*\\d+(?:\\))?"))
        for (block in blocks) {
            val content = block.trim()
            if (content.length < 5) continue
            val lines = content.split("\n").filter { it.trim().isNotEmpty() }
            if (lines.isEmpty()) continue
            val firstLine = lines[0].trim()
            val regex = Regex("^([^\\d(]+)(?:\\s+([\\dzOImg\\-\\[\\]\\/\\.]+))?", RegexOption.IGNORE_CASE)
            val match = regex.find(firstLine)
            var name = match?.groupValues?.get(1)?.trim() ?: firstLine.split(" ")[0]
            val dosage = match?.groupValues?.get(2)?.trim() ?: ""
            name = name.replace(Regex("[^a-zA-Z\\s\\-]"), "").trim()
            if (name.length > 2) medicines.add(ExtractedMedicine(name = name, dosage = cleanDosageOcr(dosage), instructions = content))
        }
        return medicines
    }

    private fun cleanDosageOcr(dosage: String): String {
        if (dosage.isEmpty()) return ""
        return dosage.uppercase().replace("SMG", "5MG").replace("S MG", "5 MG").replace("Z", "2").replace("O", "0").replace("I", "1").trim()
    }

    private fun suggestReminderTimes(instructions: String) {
        val user = currentUser ?: User()
        val text = instructions.uppercase(Locale.getDefault()).replace("O", "0").replace("I", "1")
        val suggested = mutableListOf<String>()
        val pattern = Regex("(\\d)\\s*[-/ ]\\s*(\\d)\\s*[-/ ]\\s*(\\d)")
        val match = pattern.find(text)
        if (match != null) {
            val (m, a, n) = match.destructured
            if (m != "0") suggested.add(calculateReminderTime(user.breakfastTime, "MORNING"))
            if (a != "0") suggested.add(calculateReminderTime(user.lunchTime, "AFTERNOON"))
            if (n != "0") suggested.add(calculateReminderTime(user.dinnerTime, "NIGHT"))
        } else {
            if (text.contains("MORNING") || text.contains("BREAKFAST")) suggested.add(calculateReminderTime(user.breakfastTime, "MORNING"))
            if (text.contains("AFTERNOON") || text.contains("LUNCH")) suggested.add(calculateReminderTime(user.lunchTime, "AFTERNOON"))
            if (text.contains("NIGHT") || text.contains("DINNER") || text.contains("EVENING")) suggested.add(calculateReminderTime(user.dinnerTime, "NIGHT"))
        }
        if (text.contains("BD") || text.contains("B.I.D.") || text.contains("TWICE DAILY")) {
            if (!suggested.contains(calculateReminderTime(user.breakfastTime, "MORNING"))) suggested.add(calculateReminderTime(user.breakfastTime, "MORNING"))
            if (!suggested.contains(calculateReminderTime(user.dinnerTime, "NIGHT"))) suggested.add(calculateReminderTime(user.dinnerTime, "NIGHT"))
        }
        if (text.contains("BEDTIME") || text.contains("HS")) {
            val bedtimeReminder = addMinutes(user.bedTime, -30)
            if (!suggested.contains(bedtimeReminder)) suggested.add(bedtimeReminder)
        }
        if (suggested.isNotEmpty()) {
            reminderTimes.clear(); reminderTimes.addAll(suggested.distinct()); updateTimeChips()
        }
    }

    private fun calculateReminderTime(baseTime: String, period: String): String = when (period) {
        "MORNING" -> addMinutes(baseTime, 60); "NIGHT", "DINNER" -> addMinutes(baseTime, 30); "AFTERNOON" -> addMinutes(baseTime, 30); else -> baseTime
    }

    private fun addMinutes(time: String, minutes: Int): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()); val date = sdf.parse(time) ?: return time
            val cal = Calendar.getInstance(); cal.time = date; cal.add(Calendar.MINUTE, minutes); sdf.format(cal.time)
        } catch (e: Exception) { time }
    }

    private fun saveMedicine(finishActivity: Boolean) {
        val name = binding.etMedicineName.text.toString().trim()
        val dosage = binding.etDosage.text.toString().trim()
        if (name.isEmpty() || reminderTimes.isEmpty()) {
            if (name.isEmpty()) binding.etMedicineName.error = "Required"; return
        }
        val medicine = Medicine(id = if (editMedicineId > 0) editMedicineId else 0, userId = PrefsManager(this).userId, name = name, type = getSelectedType(), dosage = dosage, mealTiming = getSelectedMealTiming(), expiryDate = expiryDateMillis, repeatDays = getSelectedDays(), reminderTimes = reminderTimes.joinToString(","), frequency = getSelectedFrequency(), notes = binding.etNotes.text.toString().trim(), imagePath = selectedImagePath, isActive = true)
        medicineVM.saveMedicine(medicine)
        if (!finishActivity && currentQueueIndex < extractedMedicinesQueue.size - 1) {
            Toast.makeText(this, "Saved $name! Loading next...", Toast.LENGTH_SHORT).show()
            clearFormForNext(); currentQueueIndex++; showExtractedMedicine(extractedMedicinesQueue[currentQueueIndex])
        } else { finish() }
    }

    private fun clearFormForNext() {
        binding.etMedicineName.text.clear(); binding.etDosage.text.clear(); binding.etNotes.text.clear(); reminderTimes.clear(); updateTimeChips(); binding.chipSyrup.isChecked = false; binding.chipTablet.isChecked = true
    }

    private fun observeViewModel() {
        userVM.currentUser.observe(this) { user -> if (user != null) { currentUser = user; updateBaseTimeChips(user) } else { currentUser = User(); updateBaseTimeChips(currentUser!!) } }
    }

    private fun updateBaseTimeChips(user: User) {
        binding.chipBaseBreakfast.text = "Breakfast: ${user.breakfastTime}"; binding.chipBaseLunch.text = "Lunch: ${user.lunchTime}"; binding.chipBaseDinner.text = "Dinner: ${user.dinnerTime}"
    }

    private fun parseAndSetExpiryDate(dateStr: String) {
        val normalized = dateStr.replace(".", "/").replace("-", "/").trim()
        val formats = listOf("MM/yy", "MM/yyyy", "dd/MM/yyyy", "yyyy/MM/dd", "MMM yyyy", "MM-yyyy")
        for (f in formats) {
            try {
                val sdf = SimpleDateFormat(f, Locale.getDefault())
                sdf.isLenient = false
                val date = sdf.parse(normalized)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date
                    // Handle 2-digit years by making them 20xx
                    if (cal.get(Calendar.YEAR) < 100) {
                        cal.set(Calendar.YEAR, 2000 + cal.get(Calendar.YEAR))
                    }
                    
                    expiryDateMillis = cal.timeInMillis
                    binding.tvExpiryDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
                    return
                }
            } catch (e: Exception) { }
        }
    }

    private suspend fun processAndGetImagePart(uri: Uri): MultipartBody.Part? = withContext(Dispatchers.IO) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBytes = inputStream.use { it.readBytes() }
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size, options)
            var inSampleSize = 1; val maxDim = 1200 
            if (options.outHeight > maxDim || options.outWidth > maxDim) { inSampleSize = 2 }
            val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size, BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }) ?: return@withContext null
            val exif = ExifInterface(originalBytes.inputStream())
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val matrix = Matrix()
            when (orientation) { ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f); ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f); ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f) }
            val rotated = if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != 0) { Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true) } else { bitmap }
            val out = ByteArrayOutputStream()
            rotated.compress(Bitmap.CompressFormat.JPEG, 80, out)
            if (rotated != bitmap) rotated.recycle(); bitmap.recycle()
            val byteArray = out.toByteArray(); val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", "image_${System.currentTimeMillis()}.jpg", requestBody)
        } catch (e: Exception) { Log.e(TAG, "Failed to process image part", e); null }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker().setTitleText("Expiry Date").setSelection(expiryDateMillis ?: MaterialDatePicker.todayInUtcMilliseconds()).build()
        picker.addOnPositiveButtonClickListener { expiryDateMillis = it; binding.tvExpiryDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) }
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
        binding.timeChipsContainer.removeAllViews(); reminderTimes.sort()
        reminderTimes.forEach { time ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = formatTime(time); chip.isCloseIconVisible = true; chip.setOnCloseIconClickListener { reminderTimes.remove(time); updateTimeChips() }; binding.timeChipsContainer.addView(chip)
        }
    }

    private fun formatTime(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return time
        val h = parts[0].toIntOrNull() ?: 0; val m = parts[1].toIntOrNull() ?: 0; val amPm = if (h >= 12) "PM" else "AM"; val hour = if (h > 12) h - 12 else if (h == 0) 12 else h
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour, m, amPm)
    }

    private fun launchCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100); return }
        val imgFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "med_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", imgFile)
        cameraLauncher.launch(cameraImageUri)
    }

    private fun loadImage(path: String) {
        selectedImagePath = path; Glide.with(this).load(path).centerInside().into(binding.ivMedicineImage)
        binding.ivMedicineImage.visibility = View.VISIBLE; binding.layoutImagePlaceholder.visibility = View.GONE
    }

    private fun getSelectedType(): String = when { binding.chipCapsule.isChecked -> "CAPSULE"; binding.chipSyrup.isChecked -> "SYRUP"; binding.chipDrops.isChecked -> "DROPS"; else -> "TABLET" }
    private fun getSelectedMealTiming(): String = when { binding.chipBefore.isChecked -> "BEFORE_MEAL"; binding.chipAfter.isChecked -> "AFTER_MEAL"; binding.chipWith.isChecked -> "WITH_MEAL"; else -> "ANYTIME" }
    private fun getSelectedDays(): String {
        val days = mutableListOf<String>()
        if (binding.chipMon.isChecked) days.add("1"); if (binding.chipTue.isChecked) days.add("2"); if (binding.chipWed.isChecked) days.add("3"); if (binding.chipThu.isChecked) days.add("4"); if (binding.chipFri.isChecked) days.add("5"); if (binding.chipSat.isChecked) days.add("6"); if (binding.chipSun.isChecked) days.add("7")
        return days.joinToString(",")
    }
    private fun getSelectedFrequency(): String { val count = reminderTimes.size; return when { count == 1 -> "DAILY"; count == 2 -> "TWICE_DAILY"; count == 3 -> "THREE_TIMES"; else -> "CUSTOM" } }
}
