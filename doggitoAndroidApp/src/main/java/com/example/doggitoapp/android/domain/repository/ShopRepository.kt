package com.example.doggitoapp.android.domain.repository

import com.example.doggitoapp.android.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    fun getAvailableProducts(): Flow<List<Product>>
    fun getProductById(productId: String): Flow<Product?>
    fun getProductsByCategory(category: String): Flow<List<Product>>
    suspend fun refreshProducts()
}
