package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.Profile
import com.example.zero.data.model.User
import com.example.zero.data.model.UserRole
import io.github.jan.supabase.postgrest.postgrest

// ============================================================================
// AuthRepository — Autenticación usando tabla `users` con password_hash.
//
// IMPORTANTE: Esta app NO usa Supabase Auth (JWT flow).
// Usa autenticación custom con la tabla `users` que tiene:
//   • email, password_hash, role
// Y la tabla `profile` con datos personales:
//   • user_id, name, last_name, identity_card, phone_number, address
//
// Flujo de login:
//   1. SELECT de users WHERE email = ? AND password_hash = ?
//   2. Si encontrado: guardar userId y role en memoria
//   3. Cargar Profile de la tabla profile WHERE user_id = ?
// ============================================================================

class AuthRepository {

    private val client = SupabaseClient.client
    private val authClient = SupabaseClient.authClient  // bypassa RLS para login

    // Estado de sesión en memoria (simple, sin JWT)
    private var currentUserId: String? = null
    private var currentUserRole: String? = null
    private var currentEmail: String? = null

    // ── Iniciar sesión (custom auth con tabla users) ──────────────────────
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            // Usamos authClient (service_role) porque la tabla users tiene RLS
            // que bloquea lecturas anónimas — es intencional para seguridad
            val users = authClient.postgrest
                .from("users")
                .select {
                    filter {
                        eq("email", email.trim())
                        eq("password_hash", password)
                    }
                }
                .decodeList<User>()

            if (users.isEmpty()) {
                return Result.failure(Exception("Email o contraseña incorrectos"))
            }

            val user = users.first()
            currentUserId = user.id
            currentUserRole = user.role
            currentEmail = user.email

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
        }
    }

    // ── Cerrar sesión ─────────────────────────────────────────────────────
    fun signOut() {
        currentUserId = null
        currentUserRole = null
        currentEmail = null
    }

    // ── Obtener ID del usuario actual ─────────────────────────────────────
    fun getCurrentUserId(): String? = currentUserId

    // ── Verificar si hay sesión activa ────────────────────────────────────
    fun isLoggedIn(): Boolean = currentUserId != null

    // ── Obtener rol actual ────────────────────────────────────────────────
    fun getCurrentRole(): String = currentUserRole ?: "CLIENTE"

    // ── Obtener perfil del usuario ────────────────────────────────────────
    suspend fun getUserProfile(): Result<Profile> {
        return try {
            val userId = currentUserId
                ?: return Result.failure(Exception("No hay sesión activa"))

            val profiles = client.postgrest
                .from("profile")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Profile>()

            if (profiles.isEmpty()) {
                // Devolver perfil vacío si no existe
                return Result.success(Profile(userId = userId))
            }

            Result.success(profiles.first())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener rol como UserRole enum ────────────────────────────────────
    suspend fun getUserRole(): UserRole {
        return UserRole.fromString(getCurrentRole())
    }
}
