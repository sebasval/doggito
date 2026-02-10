package com.example.doggitoapp.android.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doggitoapp.android.domain.model.Product
import com.example.doggitoapp.android.domain.repository.CoinRepository
import com.example.doggitoapp.android.domain.repository.ShopRepository
import com.example.doggitoapp.android.core.util.awaitUserId
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ShopUiState(
    val products: List<Product> = emptyList(),
    val balance: Int = 0,
    val isLoading: Boolean = true,
    val selectedProduct: Product? = null
)

class ShopViewModel(
    private val shopRepository: ShopRepository,
    private val coinRepository: CoinRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    private var userId: String = "local_user"

    init {
        viewModelScope.launch {
            userId = supabaseClient.awaitUserId()
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            shopRepository.refreshProducts()
        }
        viewModelScope.launch {
            shopRepository.getAvailableProducts().collect { products ->
                _uiState.value = _uiState.value.copy(products = products, isLoading = false)
            }
        }
        viewModelScope.launch {
            coinRepository.getBalance(userId).collect { balance ->
                _uiState.value = _uiState.value.copy(balance = balance)
            }
        }
    }

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            shopRepository.getProductById(productId).collect { product ->
                _uiState.value = _uiState.value.copy(selectedProduct = product)
            }
        }
    }
}
