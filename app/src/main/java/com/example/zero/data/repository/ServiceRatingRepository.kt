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

    // Usar authClient para asegurar bypass de RLS (service_role key)
    private val adminClient = SupabaseClient.authClient

    // ── Insertar calificación ────────────────────────────────────────────────
    suspend fun insertRating(rating: ServiceRating): Result<Unit> {
        return try {
            adminClient.postgrest
                .from("service_rating")
                .insert(rating)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Verificar si ya existe calificación para esta asignación o solicitud ──
    suspend fun getRating(assigmentId: String): Result<ServiceRating?> {
        return try {
            val list = adminClient.postgrest
                .from("service_rating")
                .select {
                    filter { eq("assigment_id", assigmentId) }
                }
                .decodeList<ServiceRating>()
            if (list.isNotEmpty()) {
                Result.success(list.first())
            } else {
                // Fallback por si en la base de datos la columna o el registro se vinculó por request_id u otra vía
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener todas las calificaciones de un técnico ────────────────────────
    suspend fun getRatingsForTechnician(technicianId: String): Result<List<ServiceRating>> {
        return try {
            val assignments = adminClient.postgrest
                .from("assigments")
                .select { filter { eq("technician_id", technicianId) } }
                .decodeList<com.example.zero.data.model.Assignment>()

            val assignmentIds = assignments.mapNotNull { it.id }
            if (assignmentIds.isEmpty()) return Result.success(emptyList())

            val ratings = adminClient.postgrest
                .from("service_rating")
                .select {
                    filter { isIn("assigment_id", assignmentIds) }
                }
                .decodeList<ServiceRating>()
            Result.success(ratings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Actualizar score en technician_details ────────────────────────────────
    suspend fun updateTechnicianScore(technicianId: String, newScore: Double): Result<Unit> {
        return try {
            adminClient.postgrest
                .from("technician_details")
                .update({ set("score", newScore) }) {
                    filter { eq("user_id", technicianId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Recalcular y guardar la calificación promedio del técnico ─────────────
    suspend fun recalculateTechnicianScore(assigmentId: String): Result<Unit> {
        return try {
            val assignments = adminClient.postgrest
                .from("assigments")
                .select { filter { eq("id", assigmentId) } }
                .decodeList<com.example.zero.data.model.Assignment>()
            val technicianId = assignments.firstOrNull()?.technicianId
                ?: return Result.failure(Exception("Assignment no encontrado"))

            val ratings = getRatingsForTechnician(technicianId).getOrElse { return Result.failure(it) }

            val avg = if (ratings.isEmpty()) 0.0 else ratings.map { it.score }.average()
            updateTechnicianScore(technicianId, avg)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
