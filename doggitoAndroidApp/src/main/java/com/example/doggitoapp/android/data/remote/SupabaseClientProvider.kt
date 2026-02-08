package com.example.doggitoapp.android.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClientProvider {

    private const val SUPABASE_URL = "https://xhuppyipswdywqqdzthm.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhodXBweWlwc3dkeXdxcWR6dGhtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzA0ODUzODYsImV4cCI6MjA4NjA2MTM4Nn0.k7zNgu-ZNSxGPD1ThZRk6W9Ye56ZAZ2Ci32BVjH5zBc"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
