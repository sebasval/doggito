package com.example.doggitoapp.android.data.local.dao

import androidx.room.*
import com.example.doggitoapp.android.data.local.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE userId = :userId")
    fun getPetsByUser(userId: String): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :petId")
    fun getPetById(petId: String): Flow<PetEntity?>

    @Query("SELECT * FROM pets WHERE userId = :userId LIMIT 1")
    fun getFirstPetByUser(userId: String): Flow<PetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Delete
    suspend fun deletePet(pet: PetEntity)

    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetByIdOnce(petId: String): PetEntity?

    @Query("SELECT COUNT(*) FROM pets WHERE userId = :userId")
    suspend fun countPetsByUser(userId: String): Int

    @Query("SELECT * FROM pets WHERE synced = 0")
    suspend fun getUnsyncedPets(): List<PetEntity>

    @Query("UPDATE pets SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
