package com.example.zero.data

import com.example.zero.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

// ============================================================================
// SupabaseClient — Singleton con dos clientes Supabase.
//
// • client       → Usa ANON KEY  → Para datos normales con RLS
// • authClient   → Usa SERVICE ROLE → Para autenticación custom (bypassa RLS)
//
// NOTA DE SEGURIDAD: El service_role key bypassa todas las políticas RLS.
// Solo se usa para la autenticación custom (login). Para producción real
// se debería usar Supabase Auth o una Edge Function como proxy.
// ============================================================================

object SupabaseClient {

    // ── Cliente principal (anon key + RLS) ───────────────────────────────
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }

    // ── Cliente de autenticación (service_role, bypassa RLS) ──────────────
    // Solo usado en AuthRepository.signIn() para buscar usuarios por email+password
    val authClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_SERVICE_ROLE_KEY,
    ) {
        install(Postgrest)
    }
}
