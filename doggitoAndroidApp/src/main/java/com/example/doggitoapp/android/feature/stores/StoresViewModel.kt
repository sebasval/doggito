package com.example.doggitoapp.android.feature.stores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doggitoapp.android.domain.model.Store
import com.example.doggitoapp.android.domain.repository.StoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StoresUiState(
    val stores: List<Store> = emptyList(),
    val selectedStore: Store? = null,
    val isLoading: Boolean = true
)

class StoresViewModel(
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoresUiState())
    val uiState: StateFlow<StoresUiState> = _uiState.asStateFlow()

    init {
        loadStores()
    }

    private fun loadStores() {
        viewModelScope.launch {
            storeRepository.refreshStores()
            storeRepository.getAllStores().collect { stores ->
                _uiState.value = _uiState.value.copy(stores = stores, isLoading = false)
            }
        }
    }

    fun loadStore(storeId: String) {
        viewModelScope.launch {
            storeRepository.getStoreById(storeId).collect { store ->
                _uiState.value = _uiState.value.copy(selectedStore = store)
            }
        }
    }
}
