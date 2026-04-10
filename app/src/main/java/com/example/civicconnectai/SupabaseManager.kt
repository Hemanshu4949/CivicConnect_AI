package com.example.civicconnectai // Make sure this matches your package

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseManager {

    // By putting it in an 'object', this client is now globally accessible
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Storage)
        install(Auth)
    }
}