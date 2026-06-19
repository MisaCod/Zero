package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.ServiceRequest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow

// ============================================================================
// ServiceRequestRepository — CRUD de solicitudes de mantenimiento.
//
// Operaciones:
//   • crearSolicitud:       INSERT en service_request
//   • obtenerSolicitudes:   SELECT de solicitudes (filtrado por RLS)
//   • actualizarEstado:     UPDATE del campo status
//   • suscribirInserciones: Realtime subscription para nuevas solicitudes
// ============================================================================

class ServiceRequestRepository {

    private val client = SupabaseClient.client
    private val tableName = "service_request"

    // ── Crear solicitud (INSERT) ─────────────────────────────────────────
    /**
     * Inserta una nueva solicitud de mantenimiento.
     * El campo client_id debe ser el UUID del usuario autenticado.
     */
    suspend fun crearSolicitud(solicitud: ServiceRequest): Result<ServiceRequest> {
        return try {
            val result = client.postgrest
                .from(tableName)
                .insert(solicitud) {
                    select()
                }
                .decodeSingle<ServiceRequest>()

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener solicitudes (SELECT) ─────────────────────────────────────
    /**
     * Obtiene las solicitudes visibles para el usuario actual.
     * El filtrado por rol se maneja automáticamente por las políticas RLS.
     */
    suspend fun obtenerSolicitudes(): Result<List<ServiceRequest>> {
        return try {
            val solicitudes = client.postgrest
                .from(tableName)
                .select {
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<ServiceRequest>()

            Result.success(solicitudes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener solicitudes de un cliente específico ──────────────────────
    suspend fun obtenerSolicitudesPorCliente(clientId: String): Result<List<ServiceRequest>> {
        return try {
            val solicitudes = client.postgrest
                .from(tableName)
                .select {
                    filter { eq("client_id", clientId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<ServiceRequest>()

            Result.success(solicitudes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Actualizar estado de solicitud (UPDATE) ──────────────────────────
    suspend fun actualizarEstado(
        solicitudId: String,
        nuevoEstado: String,
    ): Result<Unit> {
        return try {
            client.postgrest
                .from(tableName)
                .update({
                    set("status", nuevoEstado)
                    set("updated_at", "now()")
                }) {
                    filter { eq("id", solicitudId) }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Suscripción Realtime a nuevas solicitudes ────────────────────────
    /**
     * Crea un canal Realtime que emite un Flow cada vez que se inserta
     * una nueva solicitud en la tabla service_request.
     *
     * REQUISITO: En el panel de Supabase, ir a Database → Replication
     * y activar Realtime para la tabla "service_request".
     *
     * Uso típico:
     *   val flow = repo.suscribirInserciones()
     *   flow.collect { nuevaSolicitud -> mostrarNotificacion(nuevaSolicitud) }
     */
    suspend fun suscribirInserciones(): Flow<PostgresAction.Insert> {
        val channel = client.realtime.channel("solicitudes-nuevas")

        val insertFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = tableName
        }

        channel.subscribe()

        return insertFlow
    }

    // ── Desconectar Realtime ─────────────────────────────────────────────
    suspend fun desconectarRealtime() {
        try {
            client.realtime.removeAllChannels()
        } catch (_: Exception) {
            // Silenciar errores de desconexión
        }
    }
}
