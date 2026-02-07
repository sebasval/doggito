package com.example.doggitoapp.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.doggitoapp.android.domain.model.Store

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val email: String,
    val openingHours: String,
    val imageUrl: String? = null
) {
    fun toDomain() = Store(
        id = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        phone = phone,
        email = email,
        openingHours = openingHours,
        imageUrl = imageUrl
    )

    companion object {
        fun fromDomain(store: Store) = StoreEntity(
            id = store.id,
            name = store.name,
            address = store.address,
            latitude = store.latitude,
            longitude = store.longitude,
            phone = store.phone,
            email = store.email,
            openingHours = store.openingHours,
            imageUrl = store.imageUrl
        )
    }
}
