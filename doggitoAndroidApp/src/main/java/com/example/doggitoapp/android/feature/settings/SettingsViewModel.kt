package com.example.doggitoapp.android.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
            } catch (_: Exception) {
                // Sign out locally even if network fails
            }
            onComplete()
        }
    }
}
