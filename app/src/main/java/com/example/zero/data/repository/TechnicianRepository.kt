package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.Assignment
import com.example.zero.data.model.TechnicianDetails
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

// ============================================================================
// TechnicianRepository — Detalles del técnico y sus asignaciones.
//
// Tablas:
//   • technician_details: user_id, availability, score
//   • assigments:         id, request_id, technician_id, created_at
//
// Operaciones:
//   • getDetails:            SELECT WHERE user_id = ?
//   • updateAvailability:    UPDATE availability WHERE user_id = ?
//   • getAssignmentsByTech:  SELECT WHERE technician_id = ?
// ============================================================================

class TechnicianRepository {

    // Usar authClient para ver todos los datos del técnico sin restricciones RLS
    private val client = SupabaseClient.authClient

    // ── Obtener detalles del técnico ─────────────────────────────────────────
    suspend fun getDetails(userId: String): Result<TechnicianDetails?> {
        return try {
            val list = client.postgrest
                .from("technician_details")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<TechnicianDetails>()
            Result.success(list.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Actualizar disponibilidad ────────────────────────────────────────────
    suspend fun updateAvailability(userId: String, available: Boolean): Result<Unit> {
        return try {
            client.postgrest
                .from("technician_details")
                .update({ set("availability", available) }) {
                    filter { eq("user_id", userId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener asignaciones del técnico ─────────────────────────────────────
    suspend fun getAssignmentsByTech(technicianId: String): Result<List<Assignment>> {
        return try {
            val list = client.postgrest
                .from("assigments")
                .select {
                    filter { eq("technician_id", technicianId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Assignment>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
