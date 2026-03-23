# MediEase Android App

> Elderly-friendly medication reminder and expiry tracking app for Patients and Caregivers.

## 📱 Features
- **Patient Role**: Add medicines, set reminders, mark doses as taken, view adherence stats
- **Caregiver Role**: Monitor linked patient's medicines, receive missed-dose alerts
- **Expiry Tracking**: Visual warnings for expiring/expired medicines
- **Adherence Analysis**: Bar charts showing weekly/monthly dose adherence with tips
- **Notifications**: Exact alarm reminders, auto-reschedule after reboot

## 🏗 Architecture
- **MVVM** with LiveData + Room Database
- **Kotlin** for all logic; **XML** for layouts
- Clean separation: `models` → `database` → `repository` → `viewmodels` → `ui`

## 📂 Project Structure
```
app/src/main/java/com/mediease/app/
├── activities/         # All Activities (Splash, Login, RoleSelect, Setup, Main, AddMedicine…)
├── fragments/          # Home, Reminders, Calendar, Profile, Adherence, CaregiverPatients
├── adapters/           # RecyclerView adapters
├── viewmodels/         # MedicineVM, HomeVM, UserVM, AdherenceVM, ReminderVM
├── models/             # Medicine, MedicineLog, User, Reminder, AdherenceStats
├── database/           # AppDatabase + DAOs
├── repository/         # MedicineRepository (single source of truth)
├── notifications/      # MedicineAlarmReceiver, BootReceiver
└── utils/              # PrefsManager, DateUtils, NotificationUtils
```

## 🚀 Setup
1. Open in **Android Studio Hedgehog** or newer
2. Download **Nunito** font from Google Fonts → place TTF files in `app/src/main/res/font/`
3. Add `jitpack.io` to `settings.gradle` repositories (for MPAndroidChart):
   ```groovy
   maven { url 'https://jitpack.io' }
   ```
4. Sync Gradle → Run on device/emulator (min SDK 26)

## 🔌 Backend Integration Points

### API Client Setup (Retrofit)
```kotlin
// In MedicineRepository, replace mock calls with:
interface MediEaseApi {
    @GET("medicines/{userId}") suspend fun getMedicines(@Path("userId") id: String): List<Medicine>
    @POST("medicines") suspend fun saveMedicine(@Body medicine: Medicine): Medicine
    @POST("logs/{id}/taken") suspend fun markTaken(@Path("id") logId: Long): MedicineLog
    @GET("analysis/{userId}") suspend fun getAdherence(@Path("userId") id: String): AdherenceStats
}
```

### CNN / Image Recognition Hook
```kotlin
// In AddMedicineActivity, after image capture:
// Send image to Python backend for medicine name recognition
suspend fun recogniseMedicineFromImage(imagePath: String): String {
    // POST to /api/recognize with base64 image
    // Returns predicted medicine name
}
```

### Sample Mock Data
Call `MedicineRepository.seedSampleData(userId)` on first launch to populate 4 sample medicines.

## 🎨 UI Kit Colors
| Token | Hex | Usage |
|-------|-----|-------|
| Lavender | `#D8C8E8` | Headers, accents |
| Coral | `#F4A58D` | Buttons, highlights |
| Oat | `#F0EDE4` | Backgrounds |
| Charcoal | `#333333` | Primary text |
| Green | `#A3D9A5` | Taken status |

## ♿ Accessibility
- All touch targets ≥ 48dp
- `contentDescription` on all interactive elements
- High contrast text (4.5:1+ ratio)
- Large text sizes: body 16sp, buttons 17-18sp, titles 22-26sp
