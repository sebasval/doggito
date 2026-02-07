package com.example.doggitoapp.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.doggitoapp.android.domain.model.Product
import com.example.doggitoapp.android.domain.model.ProductCategory

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val priceCoins: Int,
    val category: String,
    val isAvailable: Boolean = true
) {
    fun toDomain() = Product(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        priceCoins = priceCoins,
        category = ProductCategory.valueOf(category),
        isAvailable = isAvailable
    )

    companion object {
        fun fromDomain(product: Product) = ProductEntity(
            id = product.id,
            name = product.name,
            description = product.description,
            imageUrl = product.imageUrl,
            priceCoins = product.priceCoins,
            category = product.category.name,
            isAvailable = product.isAvailable
        )
    }
}
