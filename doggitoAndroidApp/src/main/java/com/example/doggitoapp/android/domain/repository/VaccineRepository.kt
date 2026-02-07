package com.example.doggitoapp.android.domain.repository

import com.example.doggitoapp.android.domain.model.VaccineRecord
import kotlinx.coroutines.flow.Flow

interface VaccineRepository {
    fun getVaccinesByPet(petId: String): Flow<List<VaccineRecord>>
    fun getUpcomingVaccines(petId: String): Flow<List<VaccineRecord>>
    suspend fun addVaccine(vaccine: VaccineRecord)
    suspend fun deleteVaccine(vaccine: VaccineRecord)
}
