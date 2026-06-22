package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.ServiceRequest
import com.example.zero.data.model.ServiceRequestWithEquipment
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow

// ============================================================================
// ServiceRequestRepository — CRUD de solicitudes.
//
// Esquema REAL verificado 2026-06-20:
//   Tabla: service_request
//   Cols:  id, client_id, equipment_id, status, failure_desc, created_at
//
// Operaciones:
//   • obtenerSolicitudes:           SELECT todas (con join a client_equipment)
//   • obtenerSolicitudesPorCliente: SELECT filtradas por client_id
//   • crearSolicitud:               INSERT
//   • actualizarEstado:             UPDATE status
//   • suscribirInserciones:         Realtime Flow de nuevas inserciones
// ============================================================================

class ServiceRequestRepository {

    private val client = SupabaseClient.client
    private val authClient = SupabaseClient.authClient
    private val tableName = "service_request"

    // ── Obtener todas las solicitudes con join de equipo ─────────────────
    suspend fun obtenerSolicitudes(): Result<List<ServiceRequestWithEquipment>> {
        return try {
            val solicitudes = authClient.postgrest
                .from(tableName)
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ServiceRequest>()
                .map { sr ->
                    ServiceRequestWithEquipment(
                        id = sr.id,
                        clientId = sr.clientId,
                        equipmentId = sr.equipmentId,
                        status = sr.status,
                        failureDesc = sr.failureDesc,
                        createdAt = sr.createdAt,
                    )
                }
            Result.success(solicitudes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Obtener solicitudes de un cliente específico ──────────────────────
    suspend fun obtenerSolicitudesPorCliente(clientId: String): Result<List<ServiceRequest>> {
        return try {
            val solicitudes = authClient.postgrest
                .from(tableName)
                .select {
                    filter { eq("client_id", clientId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ServiceRequest>()
            Result.success(solicitudes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Crear solicitud (INSERT) ──────────────────────────────────────────
    /**
     * Crea una nueva solicitud usando el esquema real de la BD.
     * Los campos requeridos son: client_id, equipment_id, failure_desc, status.
     * equipment_id debe ser un UUID válido de client_equipment.
     */
    suspend fun crearSolicitud(solicitud: ServiceRequest): Result<ServiceRequest> {
        return try {
            val result = authClient.postgrest
                .from(tableName)
                .insert(solicitud) { select() }
                .decodeSingle<ServiceRequest>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Actualizar estado (UPDATE) ────────────────────────────────────────
    suspend fun actualizarEstado(solicitudId: String, nuevoEstado: String): Result<Unit> {
        return try {
            authClient.postgrest
                .from(tableName)
                .update({ set("status", nuevoEstado) }) {
                    filter { eq("id", solicitudId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Suscripción Realtime ──────────────────────────────────────────────
    /**
     * Flow que emite cada INSERT nuevo en service_request.
     * REQUISITO: Activar Realtime para esta tabla en Supabase Dashboard
     *   → Database → Replication → service_request → enable
     */
    suspend fun suscribirInserciones(): Flow<PostgresAction.Insert> {
        val channel = client.realtime.channel("solicitudes-nuevas")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = tableName
        }
        channel.subscribe()
        return flow
    }

    // ── Suscripción a cambios de estado (UPDATE) ──────────────────────────
    suspend fun suscribirCambiosEstado(): Flow<PostgresAction.Update> {
        val channel = client.realtime.channel("solicitudes-updates")
        val flow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = tableName
        }
        channel.subscribe()
        return flow
    }

    // ── Desconectar Realtime ──────────────────────────────────────────────
    suspend fun desconectarRealtime() {
        try {
            client.realtime.removeAllChannels()
        } catch (_: Exception) { }
    }
}
