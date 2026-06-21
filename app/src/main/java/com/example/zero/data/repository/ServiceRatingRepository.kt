package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.ServiceRating
import io.github.jan.supabase.postgrest.postgrest

// ============================================================================
// ServiceRatingRepository — Calificaciones de servicio.
//
// Tabla: service_rating
// Cols:  assigment_id (PK), score, comments
//
// Operaciones:
//   • insertRating:    INSERT una calificación (assigment_id es PK, no duplica)
//   • getRating:       SELECT WHERE assigment_id = ? (verifica si ya calificó)
// ============================================================================

class ServiceRatingRepository {

    private val client = SupabaseClient.client

    // ── Insertar calificación ────────────────────────────────────────────────
    suspend fun insertRating(rating: ServiceRating): Result<ServiceRating> {
        return try {
            val result = client.postgrest
                .from("service_rating")
                .insert(rating) { select() }
                .decodeSingle<ServiceRating>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Verificar si ya existe calificación para esta asignación ─────────────
    suspend fun getRating(assigmentId: String): Result<ServiceRating?> {
        return try {
            val list = client.postgrest
                .from("service_rating")
                .select {
                    filter { eq("assigment_id", assigmentId) }
                }
                .decodeList<ServiceRating>()
            Result.success(list.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
