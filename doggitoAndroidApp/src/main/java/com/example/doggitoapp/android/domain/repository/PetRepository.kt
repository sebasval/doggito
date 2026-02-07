package com.example.doggitoapp.android.domain.repository

import com.example.doggitoapp.android.domain.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getPetsByUser(userId: String): Flow<List<Pet>>
    fun getFirstPetByUser(userId: String): Flow<Pet?>
    fun getPetById(petId: String): Flow<Pet?>
    suspend fun savePet(pet: Pet)
    suspend fun updatePet(pet: Pet)
    suspend fun deletePet(pet: Pet)
}
