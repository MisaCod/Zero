package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.Profile
import com.example.zero.data.model.UserRole
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

// ============================================================================
// AuthRepository — Maneja autenticación y perfiles de usuario.
//
// Operaciones:
//   • signIn:        Inicia sesión con email y contraseña via Supabase Auth
//   • signOut:       Cierra la sesión activa
//   • getCurrentUserId: Obtiene el UUID del usuario autenticado
//   • getUserProfile: Consulta la tabla `profile` para obtener el rol
// ============================================================================

class AuthRepository {

    private val client = SupabaseClient.client

    // ── Iniciar sesión ──────────────────────────────────────────────────
    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Cerrar sesión ───────────────────────────────────────────────────
    suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener el ID del usuario actual ─────────────────────────────────
    fun getCurrentUserId(): String? {
        return client.auth.currentUserOrNull()?.id
    }

    // ── Verificar si hay sesión activa ────────────────────────────────────
    fun isLoggedIn(): Boolean {
        return client.auth.currentUserOrNull() != null
    }

    // ── Obtener perfil completo del usuario (incluye el rol) ─────────────
    suspend fun getUserProfile(): Result<Profile> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.failure(Exception("No hay usuario autenticado"))

            val profile = client.postgrest
                .from("profile")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<Profile>()

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener solo el rol del usuario ───────────────────────────────────
    suspend fun getUserRole(): UserRole {
        val profileResult = getUserProfile()
        return profileResult.getOrNull()?.let {
            UserRole.fromString(it.role)
        } ?: UserRole.CLIENTE
    }
}
