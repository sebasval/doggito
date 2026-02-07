package com.example.doggitoapp.android.data.repository

import com.example.doggitoapp.android.data.local.dao.VaccineDao
import com.example.doggitoapp.android.data.local.entity.VaccineEntity
import com.example.doggitoapp.android.domain.model.VaccineRecord
import com.example.doggitoapp.android.domain.repository.VaccineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VaccineRepositoryImpl(
    private val vaccineDao: VaccineDao
) : VaccineRepository {

    override fun getVaccinesByPet(petId: String): Flow<List<VaccineRecord>> =
        vaccineDao.getVaccinesByPet(petId).map { entities -> entities.map { it.toDomain() } }

    override fun getUpcomingVaccines(petId: String): Flow<List<VaccineRecord>> =
        vaccineDao.getUpcomingVaccines(petId, System.currentTimeMillis())
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun addVaccine(vaccine: VaccineRecord) {
        vaccineDao.insertVaccine(VaccineEntity.fromDomain(vaccine))
    }

    override suspend fun deleteVaccine(vaccine: VaccineRecord) {
        vaccineDao.deleteVaccine(VaccineEntity.fromDomain(vaccine))
    }
}
