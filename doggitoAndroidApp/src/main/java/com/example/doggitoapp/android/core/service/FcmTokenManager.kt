package com.example.doggitoapp.android.core.service

import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenDto(
    val user_id: String,
    val fcm_token: String,
    val device_info: String
)

class FcmTokenManager(
    private val supabaseClient: SupabaseClient
) {

    companion object {
        private const val TAG = "FcmTokenManager"
    }

    /**
     * Obtiene el token FCM actual y lo guarda en Supabase.
     * Llamar despues de login exitoso.
     */
    suspend fun registerToken() {
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: run {
                Log.w(TAG, "No hay usuario autenticado, no se puede registrar token")
                return
            }

            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token obtenido: ${token.take(20)}...")

            saveTokenToSupabase(userId, token)
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando FCM token", e)
        }
    }

    /**
     * Guarda un token especifico en Supabase (usado desde onNewToken).
     */
    suspend fun saveToken(token: String) {
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: run {
                Log.w(TAG, "No hay usuario autenticado para guardar token")
                return
            }

            saveTokenToSupabase(userId, token)
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando FCM token", e)
        }
    }

    /**
     * Elimina el token del usuario actual (llamar al hacer logout).
     */
    suspend fun unregisterToken() {
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
            val token = FirebaseMessaging.getInstance().token.await()

            supabaseClient.postgrest["device_tokens"]
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("fcm_token", token)
                    }
                }

            Log.d(TAG, "Token eliminado de Supabase")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando FCM token", e)
        }
    }

    private suspend fun saveTokenToSupabase(userId: String, token: String) {
        val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"

        val dto = DeviceTokenDto(
            user_id = userId,
            fcm_token = token,
            device_info = deviceInfo
        )

        supabaseClient.postgrest["device_tokens"]
            .upsert(dto)

        Log.d(TAG, "Token guardado en Supabase para usuario $userId")
    }
}
