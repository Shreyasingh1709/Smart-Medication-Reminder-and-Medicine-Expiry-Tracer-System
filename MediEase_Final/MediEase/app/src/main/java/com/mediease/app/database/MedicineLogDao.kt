package com.mediease.app.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mediease.app.models.MedicineLog

@Dao
interface MedicineLogDao {
    @Query("SELECT * FROM medicine_logs WHERE medicineId = :medicineId ORDER BY scheduledTime DESC")
    fun getLogsForMedicine(medicineId: Long): LiveData<List<MedicineLog>>

    @Query("SELECT * FROM medicine_logs WHERE date = :date ORDER BY scheduledTime ASC")
    fun getLogsForDate(date: String): LiveData<List<MedicineLog>>

    @Query("SELECT * FROM medicine_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY scheduledTime ASC")
    suspend fun getLogsForDateRange(startDate: String, endDate: String): List<MedicineLog>

    @Query("SELECT * FROM medicine_logs WHERE medicineId = :medicineId AND date = :date")
    suspend fun getLogForMedicineOnDate(medicineId: Long, date: String): List<MedicineLog>

    @Query("SELECT COUNT(*) FROM medicine_logs WHERE status = 'TAKEN' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTakenCountForRange(startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM medicine_logs WHERE status = 'MISSED' AND date BETWEEN :startDate AND :endDate")
    suspend fun getMissedCountForRange(startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM medicine_logs WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalScheduledForRange(startDate: String, endDate: String): Int

    @Query("SELECT * FROM medicine_logs WHERE status = 'MISSED' ORDER BY scheduledTime DESC LIMIT 20")
    fun getRecentMissedLogs(): LiveData<List<MedicineLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicineLog): Long

    @Update
    suspend fun updateLog(log: MedicineLog)

    @Query("UPDATE medicine_logs SET status = 'TAKEN', takenTime = :takenTime WHERE id = :logId")
    suspend fun markAsTaken(logId: Long, takenTime: Long = System.currentTimeMillis())

    @Query("UPDATE medicine_logs SET status = 'MISSED' WHERE scheduledTime < :threshold AND status = 'PENDING'")
    suspend fun markOverdueAsMissed(threshold: Long = System.currentTimeMillis())

    @Query("SELECT * FROM medicine_logs WHERE medicineId = :medicineId AND status = 'MISSED' ORDER BY scheduledTime DESC")
    suspend fun getMissedLogsForMedicine(medicineId: Long): List<MedicineLog>
}
