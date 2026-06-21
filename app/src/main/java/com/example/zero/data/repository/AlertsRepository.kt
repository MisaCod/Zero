package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

// ============================================================================
// AlertsRepository — CRUD de alertas del sistema.
//
// Operaciones:
//   • obtenerAlertas:           SELECT de alertas activas
//   • marcarComoAtendida:       UPDATE del campo atendida
//   • suscribirAlertas:         Realtime subscription para nuevas alertas
// ============================================================================

@Serializable
data class AlertaSupabase(
    val id: String? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val severidad: String = "warning",   // "critical", "warning", "info"
    val equipo: String? = null,
    val ubicacion: String? = null,
    val atendida: Boolean = false,
    val creadaEn: String? = null,
)

class AlertsRepository {

    private val client = SupabaseClient.client
    private val tableName = "alerts"

    // ── Obtener alertas activas (no atendidas) ───────────────────────────
    suspend fun obtenerAlertasActivas(): Result<List<AlertaSupabase>> {
        return try {
            val alertas = client.postgrest
                .from(tableName)
                .select {
                    filter { eq("atendida", false) }
                    order("creada_en", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<AlertaSupabase>()
            Result.success(alertas)
        } catch (e: Exception) {
            // Si la tabla no existe aún, devolver lista vacía en lugar de error
            Result.success(emptyList())
        }
    }

    // ── Marcar alerta como atendida ──────────────────────────────────────
    suspend fun marcarComoAtendida(alertaId: String): Result<Unit> {
        return try {
            client.postgrest
                .from(tableName)
                .update({ set("atendida", true) }) {
                    filter { eq("id", alertaId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Suscripción Realtime a nuevas alertas ────────────────────────────
    suspend fun suscribirAlertas(): Flow<PostgresAction.Insert> {
        val channel = client.realtime.channel("alertas-nuevas")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = tableName
        }
        channel.subscribe()
        return flow
    }
}
