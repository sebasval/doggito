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
import com.example.doggitoapp.android.data.sync.DataPullManager
import com.example.doggitoapp.android.core.util.awaitUserId
import io.github.jan.supabase.SupabaseClient
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
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

class HomeViewModel(
    private val petRepository: PetRepository,
    private val coinRepository: CoinRepository,
    private val taskRepository: TaskRepository,
    private val shopRepository: ShopRepository,
    private val streakDao: StreakDao,
    private val networkMonitor: NetworkMonitor,
    private val supabaseClient: SupabaseClient,
    private val dataPullManager: DataPullManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var userId: String = "local_user"

    init {
        viewModelScope.launch {
            userId = supabaseClient.awaitUserId()
            loadData()
        }
    }

    /**
     * Pull-to-refresh: fuerza la descarga de datos desde Supabase.
     * Los Flows reactivos de Room actualizan la UI automáticamente.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            dataPullManager.forcePull(userId)
            shopRepository.refreshProducts()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private fun loadData() {
        val today = DateUtils.todayStartMillis()

        // Pull centralizado: primero descarga datos de Supabase a Room,
        // luego inicia las colecciones reactivas de Room (en paralelo entre si)
        viewModelScope.launch {
            // PASO 1: Pull de datos remotos (si no hay cache local)
            dataPullManager.pullIfNeeded(userId)

            // PASO 2: Generar tareas diarias (despues del pull por si se bajaron datos)
            taskRepository.generateDailyTasks(userId, today)

            // PASO 3: Iniciar colecciones reactivas de Room (en paralelo entre si)
            launch {
                petRepository.getFirstPetByUser(userId).collect { pet ->
                    _uiState.value = _uiState.value.copy(pet = pet)
                }
            }

            launch {
                coinRepository.getBalance(userId).collect { balance ->
                    _uiState.value = _uiState.value.copy(balance = balance)
                }
            }

            launch {
                taskRepository.getCompletedCountByDate(userId, today).collect { count ->
                    _uiState.value = _uiState.value.copy(completedTasks = count)
                }
            }

            launch {
                taskRepository.getTotalCountByDate(userId, today).collect { count ->
                    _uiState.value = _uiState.value.copy(totalTasks = count, isLoading = false)
                }
            }

            launch {
                coinRepository.getRecentTransactions(userId, 5).collect { txs ->
                    _uiState.value = _uiState.value.copy(recentTransactions = txs)
                }
            }

            launch {
                streakDao.getStreak(userId).collect { streak ->
                    _uiState.value = _uiState.value.copy(streak = streak?.toDomain())
                }
            }
        }

        // Productos y network monitor corren independientes del pull
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.value = _uiState.value.copy(isOnline = online)
            }
        }

        viewModelScope.launch {
            shopRepository.getAvailableProducts()
                .map { it.take(7) }
                .collect { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        isLoading = products.isEmpty()
                    )
                }
        }
        viewModelScope.launch {
            shopRepository.refreshProducts()
        }
    }
}
