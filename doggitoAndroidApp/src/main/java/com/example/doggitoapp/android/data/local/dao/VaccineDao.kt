package com.example.doggitoapp.android.data.local.dao

import androidx.room.*
import com.example.doggitoapp.android.data.local.entity.VaccineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccineDao {
    @Query("SELECT * FROM vaccine_records WHERE petId = :petId ORDER BY dateAdministered DESC")
    fun getVaccinesByPet(petId: String): Flow<List<VaccineEntity>>

    @Query("SELECT * FROM vaccine_records WHERE petId = :petId AND nextDueDate IS NOT NULL AND nextDueDate > :now ORDER BY nextDueDate ASC")
    fun getUpcomingVaccines(petId: String, now: Long): Flow<List<VaccineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccine(vaccine: VaccineEntity)

    @Delete
    suspend fun deleteVaccine(vaccine: VaccineEntity)

    @Query("SELECT * FROM vaccine_records WHERE synced = 0")
    suspend fun getUnsyncedVaccines(): List<VaccineEntity>

    @Query("UPDATE vaccine_records SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
