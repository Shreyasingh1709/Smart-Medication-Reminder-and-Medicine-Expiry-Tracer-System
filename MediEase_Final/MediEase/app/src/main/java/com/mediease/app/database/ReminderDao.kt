package com.mediease.app.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mediease.app.models.Reminder

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE medicineId = :medicineId ORDER BY time ASC")
    fun getRemindersForMedicineLive(medicineId: Long): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE medicineId = :medicineId")
    suspend fun getRemindersForMedicine(medicineId: Long): List<Reminder>

    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE medicineId = :medicineId")
    suspend fun deleteRemindersForMedicine(medicineId: Long)

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY time ASC")
    fun getActiveReminders(): LiveData<List<Reminder>>
}
