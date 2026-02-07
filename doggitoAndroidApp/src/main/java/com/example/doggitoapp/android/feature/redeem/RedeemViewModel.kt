package com.example.doggitoapp.android.feature.redeem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doggitoapp.android.domain.model.Product
import com.example.doggitoapp.android.domain.model.RedeemCode
import com.example.doggitoapp.android.domain.model.Store
import com.example.doggitoapp.android.domain.repository.CoinRepository
import com.example.doggitoapp.android.domain.repository.RedeemRepository
import com.example.doggitoapp.android.domain.repository.ShopRepository
import com.example.doggitoapp.android.domain.repository.StoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RedeemUiState(
    val product: Product? = null,
    val balance: Int = 0,
    val nearestStore: Store? = null,
    val allStores: List<Store> = emptyList(),
    val redeemCode: RedeemCode? = null,
    val redeemHistory: List<RedeemCode> = emptyList(),
    val isProcessing: Boolean = false,
    val error: String? = null
)

class RedeemViewModel(
    private val redeemRepository: RedeemRepository,
    private val shopRepository: ShopRepository,
    private val coinRepository: CoinRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedeemUiState())
    val uiState: StateFlow<RedeemUiState> = _uiState.asStateFlow()

    private val userId = "local_user"

    init {
        loadHistory()
        loadStores()
        viewModelScope.launch {
            coinRepository.getBalance(userId).collect { balance ->
                _uiState.value = _uiState.value.copy(balance = balance)
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            redeemRepository.expireOldCodes()
            redeemRepository.getRedeemCodesByUser(userId).collect { codes ->
                _uiState.value = _uiState.value.copy(redeemHistory = codes)
            }
        }
    }

    private fun loadStores() {
        viewModelScope.launch {
            storeRepository.refreshStores()
            storeRepository.getAllStores().collect { stores ->
                _uiState.value = _uiState.value.copy(
                    allStores = stores,
                    nearestStore = stores.firstOrNull()
                )
            }
        }
    }

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            shopRepository.getProductById(productId).collect { product ->
                _uiState.value = _uiState.value.copy(product = product)
            }
        }
    }

    fun loadRedeemCode(redeemId: String) {
        viewModelScope.launch {
            redeemRepository.getRedeemCodeById(redeemId).collect { code ->
                _uiState.value = _uiState.value.copy(redeemCode = code)
            }
        }
    }

    fun confirmRedeem(productId: String, onSuccess: (String) -> Unit) {
        val product = _uiState.value.product ?: return
        val store = _uiState.value.nearestStore ?: return

        if (_uiState.value.balance < product.priceCoins) {
            _uiState.value = _uiState.value.copy(error = "Saldo insuficiente")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            try {
                // Deduct coins
                coinRepository.spendCoins(userId, product.priceCoins, "Canje: ${product.name}")

                // Create redeem code
                val code = redeemRepository.createRedeemCode(userId, productId, store.id)

                _uiState.value = _uiState.value.copy(isProcessing = false, redeemCode = code)
                onSuccess(code.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Error al procesar el canje"
                )
            }
        }
    }
}
