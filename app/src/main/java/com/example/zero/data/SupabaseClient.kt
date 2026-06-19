package com.example.zero.data

import com.example.zero.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

// ============================================================================
// SupabaseClient — Singleton que inicializa la conexión con Supabase.
//
// Las credenciales se leen desde BuildConfig (configuradas en local.properties).
// Para configurar:
//   1. Abre el archivo local.properties en la raíz del proyecto
//   2. Agrega:  SUPABASE_URL=https://tu-proyecto.supabase.co
//   3. Agrega:  SUPABASE_ANON_KEY=tu-anon-key-aqui
//   4. Rebuild el proyecto
// ============================================================================

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        // Módulo de autenticación (login, signup, sesión)
        install(Auth)

        // Módulo de base de datos (SELECT, INSERT, UPDATE, DELETE)
        install(Postgrest)

        // Módulo de tiempo real (suscripciones a cambios en tablas)
        install(Realtime)
    }
}
