package com.mediease.app.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mediease.app.models.Medicine

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getMedicinesForUser(userId: String): LiveData<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getMedicinesForUserSync(userId: String): List<Medicine>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): Medicine?

    @Query("SELECT * FROM medicines WHERE id = :id")
    fun getMedicineByIdLive(id: Long): LiveData<Medicine?>

    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1 AND expiryDate IS NOT NULL ORDER BY expiryDate ASC")
    fun getMedicinesWithExpiry(userId: String): LiveData<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1 AND expiryDate < :now")
    suspend fun getExpiredMedicines(userId: String, now: Long): List<Medicine>

    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1 AND expiryDate BETWEEN :now AND :threshold")
    suspend fun getExpiringSoonMedicines(userId: String, now: Long, threshold: Long): List<Medicine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("SELECT COUNT(*) FROM medicines WHERE userId = :userId AND isActive = 1")
    fun getMedicineCount(userId: String): LiveData<Int>

    @Query("UPDATE medicines SET isActive = 0 WHERE id = :id")
    suspend fun deactivateMedicine(id: Long)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: Long)
}
