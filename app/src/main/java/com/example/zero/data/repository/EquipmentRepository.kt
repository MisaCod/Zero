package com.example.zero.data.repository

import com.example.zero.data.SupabaseClient
import com.example.zero.data.model.Brand
import com.example.zero.data.model.ClientEquipment
import com.example.zero.data.model.ClientEquipmentWithDetails
import com.example.zero.data.model.ClientPartsInventory
import com.example.zero.data.model.ClientPartsInventoryWithDetails
import com.example.zero.data.model.EquipmentCatalog
import com.example.zero.data.model.EquipmentCatalogWithDetails
import com.example.zero.data.model.EquipmentType
import com.example.zero.data.model.Profile
import com.example.zero.data.model.SparePart
import com.example.zero.data.model.User
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

// ============================================================================
// EquipmentRepository — CRUD completo para:
//   • equipment_type     (tipos de equipo)
//   • brands             (marcas)
//   • equipment_catalog  (catalogo — tipo + marca + referencia)
//   • client_equipment   (equipos registrados de clientes)
//   • spare_parts        (repuestos)
//   • client_parts_inventory (inventario de repuestos por cliente)
//
// Usa authClient (service_role) para queries del supervisor que omiten RLS.
// Usa client (anon) para queries del cliente con RLS activo.
// ============================================================================

class EquipmentRepository {

    private val client     = SupabaseClient.client
    private val authClient = SupabaseClient.authClient

    // ════════════════════════════════════════════════════════════════════════
    // EQUIPMENT TYPE
    // ════════════════════════════════════════════════════════════════════════

    suspend fun getEquipmentTypes(): Result<List<EquipmentType>> = try {
        val list = authClient.postgrest
            .from("equipment_type")
            .select { order("name", Order.ASCENDING) }
            .decodeList<EquipmentType>()
        Result.success(list)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun addEquipmentType(name: String): Result<EquipmentType> = try {
        val result = authClient.postgrest
            .from("equipment_type")
            .insert(mapOf("name" to name)) { select() }
            .decodeSingle<EquipmentType>()
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteEquipmentType(id: String): Result<Unit> = try {
        authClient.postgrest
            .from("equipment_type")
            .delete { filter { eq("id", id) } }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ════════════════════════════════════════════════════════════════════════
    // BRANDS
    // ════════════════════════════════════════════════════════════════════════

    suspend fun getBrands(): Result<List<Brand>> = try {
        val list = authClient.postgrest
            .from("brands")
            .select { order("name", Order.ASCENDING) }
            .decodeList<Brand>()
        Result.success(list)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun addBrand(name: String): Result<Brand> = try {
        val result = authClient.postgrest
            .from("brands")
            .insert(mapOf("name" to name)) { select() }
            .decodeSingle<Brand>()
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteBrand(id: String): Result<Unit> = try {
        authClient.postgrest
            .from("brands")
            .delete { filter { eq("id", id) } }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ════════════════════════════════════════════════════════════════════════
    // EQUIPMENT CATALOG
    // ════════════════════════════════════════════════════════════════════════

    suspend fun getEquipmentCatalog(): Result<List<EquipmentCatalog>> = try {
        val list = authClient.postgrest
            .from("equipment_catalog")
            .select { order("reference", Order.ASCENDING) }
            .decodeList<EquipmentCatalog>()
        Result.success(list)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * Trae el catalogo completo con nombres de tipo y marca resueltos.
     * Hace joins manuales para evitar problemas con el SDK de Supabase.
     */
    suspend fun getEquipmentCatalogWithDetails(): Result<List<EquipmentCatalogWithDetails>> = try {
        val catalogs = authClient.postgrest
            .from("equipment_catalog")
            .select { order("reference", Order.ASCENDING) }
            .decodeList<EquipmentCatalog>()

        val types = authClient.postgrest.from("equipment_type").select().decodeList<EquipmentType>()
        val brands = authClient.postgrest.from("brands").select().decodeList<Brand>()

        val typeMap  = types.associateBy { it.id }
        val brandMap = brands.associateBy { it.id }

        val result = catalogs.map { cat ->
            EquipmentCatalogWithDetails(
                catalog   = cat,
                typeName  = typeMap[cat.typeId]?.name  ?: "Desconocido",
                brandName = brandMap[cat.brandId]?.name ?: "Desconocido",
            )
        }
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun addEquipmentCatalog(typeId: String, brandId: String, reference: String): Result<EquipmentCatalog> = try {
        val result = authClient.postgrest
            .from("equipment_catalog")
            .insert(mapOf("type_id" to typeId, "brand_id" to brandId, "reference" to reference)) { select() }
            .decodeSingle<EquipmentCatalog>()
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteEquipmentCatalog(id: String): Result<Unit> = try {
        authClient.postgrest
            .from("equipment_catalog")
            .delete { filter { eq("id", id) } }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ════════════════════════════════════════════════════════════════════════
    // CLIENT EQUIPMENT
    // ════════════════════════════════════════════════════════════════════════

    /** Obtiene equipos de un cliente especifico (CLIENTE ve solo los suyos). */
    suspend fun getClientEquipment(clientId: String): Result<List<ClientEquipmentWithDetails>> = try {
        val equipments = authClient.postgrest
            .from("client_equipment")
            .select {
                filter { eq("client_id", clientId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ClientEquipment>()

        val details = resolveEquipmentDetails(equipments, "")
        Result.success(details)
    } catch (e: Exception) { Result.failure(e) }

    /** Obtiene TODOS los equipos con nombre de cliente (SUPERVISOR). */
    suspend fun getAllClientEquipment(): Result<List<ClientEquipmentWithDetails>> = try {
        val equipments = authClient.postgrest
            .from("client_equipment")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<ClientEquipment>()

        // Obtener perfiles para nombres de clientes
        val profiles = authClient.postgrest.from("profile").select().decodeList<Profile>()
        val profileMap = profiles.associateBy { it.userId }

        val details = resolveEquipmentDetails(equipments, "") { equipment ->
            val p = profileMap[equipment.clientId]
            p?.fullName ?: "Cliente desconocido"
        }
        Result.success(details)
    } catch (e: Exception) { Result.failure(e) }

    private suspend fun resolveEquipmentDetails(
        equipments: List<ClientEquipment>,
        defaultClientName: String,
        clientNameResolver: ((ClientEquipment) -> String)? = null,
    ): List<ClientEquipmentWithDetails> {
        if (equipments.isEmpty()) return emptyList()

        val catalogs = authClient.postgrest.from("equipment_catalog").select().decodeList<EquipmentCatalog>()
        val types    = authClient.postgrest.from("equipment_type").select().decodeList<EquipmentType>()
        val brands   = authClient.postgrest.from("brands").select().decodeList<Brand>()

        val catalogMap = catalogs.associateBy { it.id }
        val typeMap    = types.associateBy { it.id }
        val brandMap   = brands.associateBy { it.id }

        return equipments.map { eq ->
            val catalog = catalogMap[eq.catalogId]
            ClientEquipmentWithDetails(
                equipment  = eq,
                catalog    = catalog,
                type       = typeMap[catalog?.typeId],
                brand      = brandMap[catalog?.brandId],
                clientName = clientNameResolver?.invoke(eq) ?: defaultClientName,
            )
        }
    }

    suspend fun addClientEquipment(
        clientId: String,
        catalogId: String,
        serialNum: String,
        location: String,
    ): Result<ClientEquipment> = try {
        val payload = mapOf(
            "client_id"  to clientId,
            "catalog_id" to catalogId,
            "serial_num" to serialNum,
            "location"   to location,
        )
        val result = authClient.postgrest
            .from("client_equipment")
            .insert(payload) { select() }
            .decodeSingle<ClientEquipment>()
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteClientEquipment(id: String): Result<Unit> = try {
        authClient.postgrest
            .from("client_equipment")
            .delete { filter { eq("id", id) } }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    /** Obtiene clientes (rol CLIENTE) para el selector del supervisor. */
    suspend fun getClients(): Result<List<User>> = try {
        val users = authClient.postgrest
            .from("users")
            .select {
                filter { eq("role", "CLIENTE") }
                order("email", Order.ASCENDING)
            }
            .decodeList<User>()
        Result.success(users)
    } catch (e: Exception) { Result.failure(e) }

    /** Obtiene perfiles de clientes (para mostrar nombre en lugar de email). */
    suspend fun getClientProfiles(): Result<List<Profile>> = try {
        val profiles = authClient.postgrest
            .from("profile")
            .select()
            .decodeList<Profile>()
        Result.success(profiles)
    } catch (e: Exception) { Result.failure(e) }

    // ════════════════════════════════════════════════════════════════════════
    // SPARE PARTS
    // ════════════════════════════════════════════════════════════════════════

    suspend fun getSpareParts(): Result<List<SparePart>> = try {
        val list = authClient.postgrest
            .from("spare_parts")
            .select { order("name", Order.ASCENDING) }
            .decodeList<SparePart>()
        Result.success(list)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun addSparePart(name: String, brandName: String): Result<SparePart> = try {
        val result = authClient.postgrest
            .from("spare_parts")
            .insert(mapOf("name" to name, "brand_name" to brandName)) { select() }
            .decodeSingle<SparePart>()
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteSparePart(id: String): Result<Unit> = try {
        authClient.postgrest
            .from("spare_parts")
            .delete { filter { eq("id", id) } }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ════════════════════════════════════════════════════════════════════════
    // CLIENT PARTS INVENTORY
    // ════════════════════════════════════════════════════════════════════════

    /** Inventario de repuestos de un cliente especifico. */
    suspend fun getClientPartsInventory(clientId: String): Result<List<ClientPartsInventoryWithDetails>> = try {
        val items = authClient.postgrest
            .from("client_parts_inventory")
            .select {
                filter { eq("client_id", clientId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ClientPartsInventory>()

        val details = resolveInventoryDetails(items)
        Result.success(details)
    } catch (e: Exception) { Result.failure(e) }

    /** Inventario de repuestos de todos los clientes (SUPERVISOR). */
    suspend fun getAllClientPartsInventory(): Result<List<ClientPartsInventoryWithDetails>> = try {
        val items = authClient.postgrest
            .from("client_parts_inventory")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<ClientPartsInventory>()

        val profiles = authClient.postgrest.from("profile").select().decodeList<Profile>()
        val profileMap = profiles.associateBy { it.userId }

        val details = resolveInventoryDetails(items) { inv ->
            profileMap[inv.clientId]?.fullName ?: "Cliente desconocido"
        }
        Result.success(details)
    } catch (e: Exception) { Result.failure(e) }

    private suspend fun resolveInventoryDetails(
        items: List<ClientPartsInventory>,
        clientNameResolver: ((ClientPartsInventory) -> String)? = null,
    ): List<ClientPartsInventoryWithDetails> {
        if (items.isEmpty()) return emptyList()

        val parts      = authClient.postgrest.from("spare_parts").select().decodeList<SparePart>()
        val equipments = authClient.postgrest.from("client_equipment").select().decodeList<ClientEquipment>()

        val partMap  = parts.associateBy { it.id }
        val equipMap = equipments.associateBy { it.id }

        return items.map { inv ->
            val part  = partMap[inv.partsId]
            val equip = equipMap[inv.equipmentId]
            ClientPartsInventoryWithDetails(
                inventory      = inv,
                partName       = part?.name      ?: "Repuesto desconocido",
                partBrand      = part?.brandName ?: "",
                equipmentSerial= equip?.serialNum ?: "N/A",
                clientName     = clientNameResolver?.invoke(inv) ?: "",
            )
        }
    }

    suspend fun addClientPartsInventory(
        clientId: String,
        partsId: String,
        equipmentId: String?,
        serialNum: String,
    ): Result<ClientPartsInventory> = try {
        val payload = ClientPartsInventory(
            clientId = clientId,
            partsId = partsId,
            equipmentId = equipmentId?.ifBlank { null },
            serialNum = serialNum
        )
        val result = authClient.postgrest
            .from("client_parts_inventory")
            .insert(payload) { select() }
            .decodeSingle<ClientPartsInventory>()
        Result.success(result)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteClientPartsInventory(id: String): Result<Unit> = try {
        authClient.postgrest
            .from("client_parts_inventory")
            .delete { filter { eq("id", id) } }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
