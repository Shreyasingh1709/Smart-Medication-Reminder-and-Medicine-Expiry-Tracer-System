package com.mediease.app.database

import androidx.room.*
import com.mediease.app.models.Patient

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient): Long

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Long): Patient?

    @Query("SELECT * FROM patients WHERE profileCode = :profileCode")
    suspend fun getPatientByProfileCode(profileCode: String): Patient?
}
