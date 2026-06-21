package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.ClientEquipment
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

// ============================================================================
// ClientEquipmentRepository — Equipos registrados por cliente.
//
// Tabla: client_equipment
// Cols:  id, client_id, catalog_id, serial_num, location, created_at
//
// Operaciones:
//   • getEquipmentByClient:  SELECT WHERE client_id = ?
// ============================================================================

class ClientEquipmentRepository {

    private val client = SupabaseClient.client

    // ── Obtener equipos de un cliente ────────────────────────────────────────
    suspend fun getEquipmentByClient(clientId: String): Result<List<ClientEquipment>> {
        return try {
            val list = client.postgrest
                .from("client_equipment")
                .select {
                    filter { eq("client_id", clientId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ClientEquipment>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
