package com.example.doggitoapp.android.core.util

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.first

/**
 * Espera a que la sesión de Auth termine de cargar desde el almacenamiento local
 * y devuelve el userId real. Esto es necesario porque currentUserOrNull() puede
 * retornar null si la sesión aún no se ha cargado (operación asíncrona),
 * causando que se use "local_user" y Room no encuentre datos.
 *
 * No requiere conexión a internet — solo espera la lectura de SharedPreferences.
 */
suspend fun SupabaseClient.awaitUserId(): String {
    val status = auth.sessionStatus
        .first { it !is SessionStatus.LoadingFromStorage }
    return when (status) {
        is SessionStatus.Authenticated -> status.session.user?.id ?: "local_user"
        else -> auth.currentUserOrNull()?.id ?: "local_user"
    }
}
