package com.example.doggitoapp.android.data.repository

import com.example.doggitoapp.android.data.local.dao.ProductDao
import com.example.doggitoapp.android.data.local.entity.ProductEntity
import com.example.doggitoapp.android.domain.model.Product
import com.example.doggitoapp.android.domain.model.ProductCategory
import com.example.doggitoapp.android.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ShopRepositoryImpl(
    private val productDao: ProductDao
) : ShopRepository {

    override fun getAvailableProducts(): Flow<List<Product>> =
        productDao.getAvailableProducts().map { entities -> entities.map { it.toDomain() } }

    override fun getProductById(productId: String): Flow<Product?> =
        productDao.getProductById(productId).map { it?.toDomain() }

    override fun getProductsByCategory(category: String): Flow<List<Product>> =
        productDao.getProductsByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshProducts() {
        // TODO: Fetch from Supabase when online
        // For now, seed with sample products if empty
        val sampleProducts = listOf(
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "Snack Natural Pollo",
                description = "Deliciosos snacks de pollo deshidratado, 100% natural",
                imageUrl = "https://placehold.co/400x300/FF8C00/white?text=Snack+Pollo",
                priceCoins = 50,
                category = ProductCategory.SNACKS.name
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "Collar Reflectante",
                description = "Collar ajustable con bandas reflectantes para paseos nocturnos",
                imageUrl = "https://placehold.co/400x300/4169E1/white?text=Collar",
                priceCoins = 150,
                category = ProductCategory.COLLARS.name
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "Cama Ortopédica M",
                description = "Cama con espuma viscoelástica para perros medianos",
                imageUrl = "https://placehold.co/400x300/2E8B57/white?text=Cama",
                priceCoins = 500,
                category = ProductCategory.BEDS.name
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "Pelota Indestructible",
                description = "Pelota de caucho natural resistente a mordidas",
                imageUrl = "https://placehold.co/400x300/DC143C/white?text=Pelota",
                priceCoins = 80,
                category = ProductCategory.TOYS.name
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "Snack Dental Menta",
                description = "Barritas dentales que limpian dientes y refrescan el aliento",
                imageUrl = "https://placehold.co/400x300/FF6347/white?text=Dental",
                priceCoins = 60,
                category = ProductCategory.SNACKS.name
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "Arnés Deportivo",
                description = "Arnés acolchado ideal para running y senderismo",
                imageUrl = "https://placehold.co/400x300/8A2BE2/white?text=Arnes",
                priceCoins = 200,
                category = ProductCategory.ACCESSORIES.name
            )
        )
        productDao.insertProducts(sampleProducts)
    }
}
