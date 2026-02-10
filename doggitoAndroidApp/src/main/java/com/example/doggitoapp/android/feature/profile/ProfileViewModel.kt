package com.example.doggitoapp.android.feature.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doggitoapp.android.data.sync.SyncManager
import com.example.doggitoapp.android.domain.model.Pet
import com.example.doggitoapp.android.domain.model.VaccineRecord
import com.example.doggitoapp.android.domain.repository.PetRepository
import com.example.doggitoapp.android.domain.repository.VaccineRepository
import com.example.doggitoapp.android.core.util.awaitUserId
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ProfileUiState(
    val pet: Pet? = null,
    val vaccines: List<VaccineRecord> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val petRepository: PetRepository,
    private val vaccineRepository: VaccineRepository,
    private val supabaseClient: SupabaseClient,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var userId: String = "local_user"

    init {
        viewModelScope.launch {
            userId = supabaseClient.awaitUserId()
            loadPet()
        }
    }

    private fun loadPet() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            petRepository.getFirstPetByUser(userId).collect { pet ->
                _uiState.value = _uiState.value.copy(pet = pet, isLoading = false)
                pet?.let { loadVaccines(it.id) }
            }
        }
    }

    private fun loadVaccines(petId: String) {
        viewModelScope.launch {
            vaccineRepository.getVaccinesByPet(petId).collect { vaccines ->
                _uiState.value = _uiState.value.copy(vaccines = vaccines)
            }
        }
    }

    fun savePet(name: String, breed: String, birthDate: Long, weight: Float, photoUri: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val existing = _uiState.value.pet
                val pet = Pet(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    userId = userId,
                    name = name,
                    breed = breed,
                    birthDate = birthDate,
                    weight = weight,
                    photoUri = photoUri
                )
                if (existing != null) {
                    petRepository.updatePet(pet)
                } else {
                    petRepository.savePet(pet)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, pet = pet)
                SyncManager.triggerImmediateSync(application)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Error al guardar"
                )
            }
        }
    }

    fun addVaccine(vaccineName: String, dateAdministered: Long, nextDueDate: Long?, vetName: String?) {
        val petId = _uiState.value.pet?.id ?: return
        viewModelScope.launch {
            vaccineRepository.addVaccine(
                VaccineRecord(
                    id = UUID.randomUUID().toString(),
                    petId = petId,
                    vaccineName = vaccineName,
                    dateAdministered = dateAdministered,
                    nextDueDate = nextDueDate,
                    vetName = vetName
                )
            )
            SyncManager.triggerImmediateSync(application)
        }
    }

    fun deleteVaccine(vaccine: VaccineRecord) {
        viewModelScope.launch {
            vaccineRepository.deleteVaccine(vaccine)
            SyncManager.triggerImmediateSync(application)
        }
    }
}
