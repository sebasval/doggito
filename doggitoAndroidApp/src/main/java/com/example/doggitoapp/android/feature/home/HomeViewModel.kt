package com.example.doggitoapp.android.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doggitoapp.android.core.util.DateUtils
import com.example.doggitoapp.android.core.util.NetworkMonitor
import com.example.doggitoapp.android.domain.model.CoinTransaction
import com.example.doggitoapp.android.domain.model.Pet
import com.example.doggitoapp.android.domain.model.Product
import com.example.doggitoapp.android.domain.model.Streak
import com.example.doggitoapp.android.domain.repository.CoinRepository
import com.example.doggitoapp.android.domain.repository.PetRepository
import com.example.doggitoapp.android.domain.repository.ShopRepository
import com.example.doggitoapp.android.domain.repository.TaskRepository
import com.example.doggitoapp.android.data.local.dao.StreakDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val pet: Pet? = null,
    val balance: Int = 0,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val streak: Streak? = null,
    val products: List<Product> = emptyList(),
    val recentTransactions: List<CoinTransaction> = emptyList(),
    val isOnline: Boolean = true,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val petRepository: PetRepository,
    private val coinRepository: CoinRepository,
    private val taskRepository: TaskRepository,
    private val shopRepository: ShopRepository,
    private val streakDao: StreakDao,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val userId = "local_user"

    init {
        loadData()
    }

    private fun loadData() {
        val today = DateUtils.todayStartMillis()

        viewModelScope.launch {
            taskRepository.generateDailyTasks(userId, today)
        }

        viewModelScope.launch {
            petRepository.getFirstPetByUser(userId).collect { pet ->
                _uiState.value = _uiState.value.copy(pet = pet)
            }
        }

        viewModelScope.launch {
            coinRepository.getBalance(userId).collect { balance ->
                _uiState.value = _uiState.value.copy(balance = balance)
            }
        }

        viewModelScope.launch {
            taskRepository.getCompletedCountByDate(userId, today).collect { count ->
                _uiState.value = _uiState.value.copy(completedTasks = count)
            }
        }

        viewModelScope.launch {
            taskRepository.getTotalCountByDate(userId, today).collect { count ->
                _uiState.value = _uiState.value.copy(totalTasks = count, isLoading = false)
            }
        }

        viewModelScope.launch {
            coinRepository.getRecentTransactions(userId, 5).collect { txs ->
                _uiState.value = _uiState.value.copy(recentTransactions = txs)
            }
        }

        viewModelScope.launch {
            streakDao.getStreak(userId).collect { streak ->
                _uiState.value = _uiState.value.copy(streak = streak?.toDomain())
            }
        }

        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.value = _uiState.value.copy(isOnline = online)
            }
        }

        // Load products for home card stack (max 7)
        viewModelScope.launch {
            shopRepository.refreshProducts()
        }
        viewModelScope.launch {
            shopRepository.getAvailableProducts()
                .map { it.take(7) }
                .collect { products ->
                    _uiState.value = _uiState.value.copy(products = products)
                }
        }
    }
}
