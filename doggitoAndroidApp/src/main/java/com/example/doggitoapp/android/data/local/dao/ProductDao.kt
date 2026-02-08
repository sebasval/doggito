package com.example.doggitoapp.android.data.local.dao

import androidx.room.*
import com.example.doggitoapp.android.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isAvailable = 1 ORDER BY priceCoins ASC")
    fun getAvailableProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE category = :category AND isAvailable = 1")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(products: List<ProductEntity>) {
        clearAll()
        insertProducts(products)
    }
}
