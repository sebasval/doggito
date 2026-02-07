package com.example.doggitoapp.android.data.repository

import com.example.doggitoapp.android.data.local.dao.PetDao
import com.example.doggitoapp.android.data.local.entity.PetEntity
import com.example.doggitoapp.android.domain.model.Pet
import com.example.doggitoapp.android.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PetRepositoryImpl(
    private val petDao: PetDao
) : PetRepository {

    override fun getPetsByUser(userId: String): Flow<List<Pet>> =
        petDao.getPetsByUser(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getFirstPetByUser(userId: String): Flow<Pet?> =
        petDao.getFirstPetByUser(userId).map { it?.toDomain() }

    override fun getPetById(petId: String): Flow<Pet?> =
        petDao.getPetById(petId).map { it?.toDomain() }

    override suspend fun savePet(pet: Pet) {
        petDao.insertPet(PetEntity.fromDomain(pet))
    }

    override suspend fun updatePet(pet: Pet) {
        petDao.updatePet(PetEntity.fromDomain(pet))
    }

    override suspend fun deletePet(pet: Pet) {
        petDao.deletePet(PetEntity.fromDomain(pet))
    }
}
