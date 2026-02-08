package com.example.doggitoapp.android.data.repository

import android.util.Log
import com.example.doggitoapp.android.data.local.dao.ProductDao
import com.example.doggitoapp.android.data.local.entity.ProductEntity
import com.example.doggitoapp.android.domain.model.Product
import com.example.doggitoapp.android.domain.repository.ShopRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val description: String,
    val image_url: String,
    val price_coins: Int,
    val category: String,
    val is_available: Boolean = true
)

class ShopRepositoryImpl(
    private val productDao: ProductDao,
    private val supabaseClient: SupabaseClient
) : ShopRepository {

    companion object {
        private const val TAG = "ShopRepository"
    }

    override fun getAvailableProducts(): Flow<List<Product>> =
        productDao.getAvailableProducts().map { entities -> entities.map { it.toDomain() } }

    override fun getProductById(productId: String): Flow<Product?> =
        productDao.getProductById(productId).map { it?.toDomain() }

    override fun getProductsByCategory(category: String): Flow<List<Product>> =
        productDao.getProductsByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshProducts() {
        try {
            val remoteDtos = supabaseClient.postgrest["products"]
                .select()
                .decodeList<ProductDto>()

            if (remoteDtos.isNotEmpty()) {
                val entities = remoteDtos.map { dto ->
                    ProductEntity(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        imageUrl = dto.image_url,
                        priceCoins = dto.price_coins,
                        category = dto.category,
                        isAvailable = dto.is_available
                    )
                }
                productDao.replaceAll(entities)
                Log.d(TAG, "Fetched ${entities.size} products from Supabase")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch products from Supabase, using cached data: ${e.message}")
            // Offline: Room cache will serve the last fetched products
        }
    }
}
