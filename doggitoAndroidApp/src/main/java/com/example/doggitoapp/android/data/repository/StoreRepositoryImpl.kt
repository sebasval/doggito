package com.example.doggitoapp.android.data.repository

import android.util.Log
import com.example.doggitoapp.android.data.local.dao.StoreDao
import com.example.doggitoapp.android.data.local.entity.StoreEntity
import com.example.doggitoapp.android.domain.model.Store
import com.example.doggitoapp.android.domain.repository.StoreRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlin.math.*

@Serializable
data class StoreDto(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val email: String,
    val opening_hours: String,
    val image_url: String? = null
)

class StoreRepositoryImpl(
    private val storeDao: StoreDao,
    private val supabaseClient: SupabaseClient
) : StoreRepository {

    companion object {
        private const val TAG = "StoreRepository"
    }

    override fun getAllStores(): Flow<List<Store>> =
        storeDao.getAllStores().map { entities -> entities.map { it.toDomain() } }

    override fun getStoreById(storeId: String): Flow<Store?> =
        storeDao.getStoreById(storeId).map { it?.toDomain() }

    override fun getNearestStore(latitude: Double, longitude: Double): Flow<Store?> =
        storeDao.getAllStores().map { stores ->
            stores.minByOrNull { store ->
                haversineDistance(latitude, longitude, store.latitude, store.longitude)
            }?.toDomain()
        }

    override suspend fun refreshStores() {
        try {
            val remoteDtos = supabaseClient.postgrest["stores"]
                .select()
                .decodeList<StoreDto>()

            if (remoteDtos.isNotEmpty()) {
                val entities = remoteDtos.map { dto ->
                    StoreEntity(
                        id = dto.id,
                        name = dto.name,
                        address = dto.address,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        phone = dto.phone,
                        email = dto.email,
                        openingHours = dto.opening_hours,
                        imageUrl = dto.image_url
                    )
                }
                storeDao.clearAll()
                storeDao.insertStores(entities)
                Log.d(TAG, "Fetched ${entities.size} stores from Supabase")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch stores from Supabase, using cached data: ${e.message}")
        }
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }
}
