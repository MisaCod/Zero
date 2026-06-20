package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.TechnicalReport
import io.github.jan.supabase.postgrest.postgrest

// ============================================================================
// TechnicalReportRepository — Reportes técnicos de asignaciones.
//
// Tabla: technical_report
// Cols:  id, assigment_id, diagnosis, work_done, start_time, end_time
//
// Operaciones:
//   • insertReport:    INSERT nuevo reporte técnico
//   • getByAssignment: SELECT WHERE assigment_id = ?
// ============================================================================

class TechnicalReportRepository {

    private val client = SupabaseClient.client

    // ── Insertar reporte técnico ─────────────────────────────────────────────
    suspend fun insertReport(report: TechnicalReport): Result<TechnicalReport> {
        return try {
            val result = client.postgrest
                .from("technical_report")
                .insert(report) { select() }
                .decodeSingle<TechnicalReport>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener reporte por asignación ───────────────────────────────────────
    suspend fun getByAssignment(assigmentId: String): Result<TechnicalReport?> {
        return try {
            val list = client.postgrest
                .from("technical_report")
                .select {
                    filter { eq("assigment_id", assigmentId) }
                }
                .decodeList<TechnicalReport>()
            Result.success(list.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
