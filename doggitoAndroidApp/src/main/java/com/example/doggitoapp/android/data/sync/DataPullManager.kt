package com.example.doggitoapp.android.data.sync

import android.app.Application
import android.util.Log
import com.example.doggitoapp.android.data.local.dao.*
import com.example.doggitoapp.android.data.local.entity.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * Centraliza la descarga de datos del usuario desde Supabase a Room.
 * Se ejecuta 1 vez al abrir el Home: si ya hay datos locales, no hace nada.
 * Estrategia offline-first: la app siempre lee de Room.
 */
class DataPullManager(
    private val petDao: PetDao,
    private val coinDao: CoinTransactionDao,
    private val runDao: RunSessionDao,
    private val vaccineDao: VaccineDao,
    private val redeemDao: RedeemCodeDao,
    private val streakDao: StreakDao,
    private val supabaseClient: SupabaseClient,
    private val application: Application
) {

    companion object {
        private const val TAG = "DataPullManager"
    }

    /**
     * Descarga todos los datos del usuario desde Supabase si no hay datos locales.
     * Es fire-and-forget: si falla (sin red, etc.), la app funciona con datos locales.
     */
    suspend fun pullIfNeeded(userId: String) {
        try {
            // Si ya hay mascota local, el usuario ya tiene datos -> no hacer nada
            val localPetCount = petDao.countPetsByUser(userId)
            if (localPetCount > 0) {
                Log.d(TAG, "Local data found for user, skipping pull")
                return
            }

            Log.d(TAG, "No local data for user, pulling from Supabase...")
            pullAllData(userId)
            Log.d(TAG, "Pull completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Pull failed (offline-first, using local data)", e)
        }
    }

    /**
     * Fuerza la descarga de todos los datos del usuario desde Supabase,
     * sin importar si ya hay datos locales. Útil para pull-to-refresh
     * y sincronización bidireccional.
     */
    suspend fun forcePull(userId: String) {
        try {
            Log.d(TAG, "Force pulling all data from Supabase...")
            pullAllData(userId)
            Log.d(TAG, "Force pull completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Force pull failed", e)
        }
    }

    private suspend fun pullAllData(userId: String) {
        pullPets(userId)
        pullCoinTransactions(userId)
        pullRunningSessions(userId)
        pullVaccines(userId)
        pullRedeemCodes(userId)
    }

    private suspend fun pullPets(userId: String) {
        try {
            val remotePets = supabaseClient.postgrest["pets"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<PetDto>()

            remotePets.forEach { dto ->
                // Descargar foto remota a almacenamiento local, o limpiar paths invalidos
                val localPhotoUri = resolvePhotoUri(dto.photo_uri, dto.id)

                petDao.insertPet(
                    PetEntity(
                        id = dto.id,
                        userId = dto.user_id,
                        name = dto.name,
                        breed = dto.breed,
                        birthDate = dto.birth_date,
                        weight = dto.weight,
                        photoUri = localPhotoUri,
                        synced = true
                    )
                )
            }
            Log.d(TAG, "Pulled ${remotePets.size} pets")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull pets", e)
        }
    }

    /**
     * Resuelve la photo_uri del DTO:
     * - Si es URL remota (http): descarga la imagen a filesDir local y retorna ruta local
     * - Si es ruta local que existe: la retorna tal cual
     * - Si es ruta local que no existe o null: retorna null
     */
    private suspend fun resolvePhotoUri(remoteUri: String?, petId: String): String? {
        if (remoteUri == null) return null

        // Es URL remota: descargar a almacenamiento local en hilo IO
        if (remoteUri.startsWith("http")) {
            return withContext(Dispatchers.IO) {
                try {
                    val dir = File(application.filesDir, "pet_photos").apply { mkdirs() }
                    val localFile = File(dir, "$petId.jpg")
                    val bytes = URL(remoteUri).readBytes()
                    localFile.writeBytes(bytes)
                    Log.d(TAG, "Downloaded pet photo to ${localFile.absolutePath}")
                    localFile.absolutePath
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download pet photo from $remoteUri", e)
                    null
                }
            }
        }

        // Es ruta local: verificar que exista
        if (remoteUri.startsWith("/") && File(remoteUri).exists()) {
            return remoteUri
        }

        return null
    }

    private suspend fun pullCoinTransactions(userId: String) {
        try {
            val remoteCoins = supabaseClient.postgrest["coin_transactions"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<CoinDto>()

            remoteCoins.forEach { dto ->
                coinDao.insertTransaction(
                    CoinTransactionEntity(
                        id = dto.id,
                        userId = dto.user_id,
                        amount = dto.amount,
                        type = dto.type,
                        description = dto.description,
                        createdAt = dto.created_at,
                        synced = true
                    )
                )
            }
            Log.d(TAG, "Pulled ${remoteCoins.size} coin transactions")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull coin transactions", e)
        }
    }

    private suspend fun pullRunningSessions(userId: String) {
        try {
            val remoteRuns = supabaseClient.postgrest["running_sessions"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<RunDto>()

            remoteRuns.forEach { dto ->
                runDao.insertSession(
                    RunSessionEntity(
                        id = dto.id,
                        userId = dto.user_id,
                        petId = dto.pet_id,
                        distanceMeters = dto.distance_meters,
                        durationMillis = dto.duration_millis,
                        avgSpeedKmh = dto.avg_speed_kmh,
                        caloriesUser = dto.calories_user,
                        caloriesDog = dto.calories_dog,
                        mode = dto.mode,
                        routeJson = dto.route_json,
                        coinsEarned = dto.coins_earned,
                        startedAt = dto.started_at,
                        endedAt = dto.ended_at,
                        synced = true
                    )
                )
            }
            Log.d(TAG, "Pulled ${remoteRuns.size} running sessions")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull running sessions", e)
        }
    }

    private suspend fun pullVaccines(userId: String) {
        try {
            // Primero obtener los IDs de mascotas del usuario para filtrar vacunas
            val remotePets = supabaseClient.postgrest["pets"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<PetDto>()

            remotePets.forEach { pet ->
                val remoteVaccines = supabaseClient.postgrest["vaccine_records"]
                    .select { filter { eq("pet_id", pet.id) } }
                    .decodeList<VaccineDto>()

                remoteVaccines.forEach { dto ->
                    vaccineDao.insertVaccine(
                        VaccineEntity(
                            id = dto.id,
                            petId = dto.pet_id,
                            vaccineName = dto.vaccine_name,
                            dateAdministered = dto.date_administered,
                            nextDueDate = dto.next_due_date,
                            vetName = dto.vet_name,
                            notes = dto.notes,
                            synced = true
                        )
                    )
                }
                Log.d(TAG, "Pulled ${remoteVaccines.size} vaccines for pet ${pet.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull vaccines", e)
        }
    }

    private suspend fun pullRedeemCodes(userId: String) {
        try {
            val remoteCodes = supabaseClient.postgrest["redeem_codes"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<RedeemDto>()

            remoteCodes.forEach { dto ->
                redeemDao.insertRedeemCode(
                    RedeemCodeEntity(
                        id = dto.id,
                        userId = dto.user_id,
                        productId = dto.product_id,
                        code = dto.code,
                        qrData = dto.qr_data,
                        storeId = dto.store_id,
                        status = dto.status,
                        createdAt = dto.created_at,
                        expiresAt = dto.expires_at,
                        claimedAt = dto.claimed_at,
                        synced = true
                    )
                )
            }
            Log.d(TAG, "Pulled ${remoteCodes.size} redeem codes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull redeem codes", e)
        }
    }
}
