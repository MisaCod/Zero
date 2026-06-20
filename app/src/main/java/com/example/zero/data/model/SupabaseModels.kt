package com.example.zero.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================================================
// SupabaseModels.kt — Modelos que mapean EXACTAMENTE las tablas de Supabase.
//
// Esquema verificado el 2026-06-20 contra la BD real de 0 Grados.
// Tablas: users, profile, service_request, client_equipment,
//         equipment_catalog, equipment_type, assigments,
//         technical_report, service_rating, technician_details
// ============================================================================

// region ── Usuario (tabla: users) ────────────────────────────────────────────

/**
 * Mapea la tabla `users`.
 * Columnas: id, email, password_hash, role, created_at
 */
@Serializable
data class User(
    val id: String? = null,
    val email: String = "",
    @SerialName("password_hash") val passwordHash: String = "",
    val role: String = "CLIENTE",     // "CLIENTE", "TÉCNICO", "SUPERVISOR"
    @SerialName("created_at") val createdAt: String? = null,
)

// endregion

// region ── Perfil (tabla: profile) ───────────────────────────────────────────

/**
 * Mapea la tabla `profile`.
 * Columnas: user_id, identity_card, name, last_name, phone_number, address
 */
@Serializable
data class Profile(
    @SerialName("user_id") val userId: String = "",
    @SerialName("identity_card") val identityCard: String? = null,
    val name: String = "",
    @SerialName("last_name") val lastName: String = "",
    @SerialName("phone_number") val phoneNumber: String? = null,
    val address: String? = null,
) {
    val fullName get() = "$name $lastName".trim()
}

// endregion

// region ── Solicitud de Servicio (tabla: service_request) ────────────────────

/**
 * Mapea la tabla `service_request`.
 * Columnas: id, client_id, equipment_id, status, failure_desc, created_at
 *
 * NOTA: El campo principal de descripción es `failure_desc` (no description/title).
 * Se usa `failure_desc` como título principal en la UI.
 */
@Serializable
data class ServiceRequest(
    val id: String? = null,
    @SerialName("client_id") val clientId: String = "",
    @SerialName("equipment_id") val equipmentId: String? = null,
    val status: String = "SIN INICIAR",      // "SIN INICIAR", "EN PROGRESO", "TERMINADO"
    @SerialName("failure_desc") val failureDesc: String = "",
    @SerialName("created_at") val createdAt: String? = null,
) {
    // Propiedades derivadas para compatibilidad con la UI existente
    val title: String get() = failureDesc.take(60).let { if (failureDesc.length > 60) "$it..." else it }
    val description: String? get() = failureDesc
    val location: String get() = ""            // Se obtiene join con client_equipment
    val priority: String get() = "media"       // Pendiente: agregar columna a BD
    val scheduledDate: String? get() = null    // Pendiente: agregar columna a BD
}

// endregion

// region ── Solicitud con Join (vista enriquecida) ────────────────────────────

/**
 * ServiceRequest con datos de equipo y ubicación (resultado de join).
 * Usado cuando se hace SELECT con embed de client_equipment.
 */
@Serializable
data class ServiceRequestWithEquipment(
    val id: String? = null,
    @SerialName("client_id") val clientId: String = "",
    @SerialName("equipment_id") val equipmentId: String? = null,
    val status: String = "SIN INICIAR",
    @SerialName("failure_desc") val failureDesc: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("client_equipment") val equipment: ClientEquipment? = null,
) {
    val title: String get() = failureDesc.take(60).let { if (failureDesc.length > 60) "$it..." else it }
    val location: String get() = equipment?.location ?: "Sin ubicación"
    val serialNumber: String get() = equipment?.serialNum ?: ""
}

// endregion

// region ── Equipo del cliente (tabla: client_equipment) ──────────────────────

/**
 * Mapea la tabla `client_equipment`.
 * Columnas: id, client_id, catalog_id, serial_num, location, created_at
 */
@Serializable
data class ClientEquipment(
    val id: String = "",
    @SerialName("client_id") val clientId: String = "",
    @SerialName("catalog_id") val catalogId: String? = null,
    @SerialName("serial_num") val serialNum: String = "",
    val location: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// endregion

// region ── Catálogo de equipos (tabla: equipment_catalog) ────────────────────

/**
 * Mapea la tabla `equipment_catalog`.
 * Columnas: id, type_id, brand_id, reference
 */
@Serializable
data class EquipmentCatalog(
    val id: String = "",
    @SerialName("type_id") val typeId: String? = null,
    @SerialName("brand_id") val brandId: String? = null,
    val reference: String = "",
)

// endregion

// region ── Tipo de equipo (tabla: equipment_type) ────────────────────────────

/**
 * Mapea la tabla `equipment_type`.
 * Columnas: id, name
 */
@Serializable
data class EquipmentType(
    val id: String = "",
    val name: String = "",
)

// endregion

// region ── Marcas (tabla: brands) ─────────────────────────────────────────────

/**
 * Mapea la tabla `brands`.
 * Columnas: id, name
 */
@Serializable
data class Brand(
    val id: String = "",
    val name: String = "",
)

// endregion

// region ── Repuestos (tabla: spare_parts) ─────────────────────────────────────

/**
 * Mapea la tabla `spare_parts`.
 * Columnas: id, name, brand_name
 */
@Serializable
data class SparePart(
    val id: String = "",
    val name: String = "",
    @SerialName("brand_name") val brandName: String = "",
)

// endregion

// region ── Inventario de repuestos del cliente (tabla: client_parts_inventory) ──

/**
 * Mapea la tabla `client_parts_inventory`.
 * Columnas: id, client_id, parts_id, equipment_id, serial_num, created_at
 */
@Serializable
data class ClientPartsInventory(
    val id: String = "",
    @SerialName("client_id") val clientId: String = "",
    @SerialName("parts_id") val partsId: String = "",
    @SerialName("equipment_id") val equipmentId: String? = null,
    @SerialName("serial_num") val serialNum: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)

// endregion

// region ── Modelos enriquecidos (joins para UI) ────────────────────────────────

/**
 * ClientEquipment con datos del catalogo (tipo y marca).
 */
data class ClientEquipmentWithDetails(
    val equipment: ClientEquipment,
    val catalog: EquipmentCatalog?,
    val type: EquipmentType?,
    val brand: Brand?,
    val clientName: String = "",
) {
    val reference: String get() = catalog?.reference ?: "Sin referencia"
    val typeName: String get() = type?.name ?: "Tipo desconocido"
    val brandName: String get() = brand?.name ?: "Marca desconocida"
    val serialNum: String get() = equipment.serialNum
    val location: String get() = equipment.location ?: "Sin ubicacion"
}

/**
 * EquipmentCatalog con nombres de tipo y marca resueltos.
 */
data class EquipmentCatalogWithDetails(
    val catalog: EquipmentCatalog,
    val typeName: String,
    val brandName: String,
) {
    val id: String get() = catalog.id
    val reference: String get() = catalog.reference
}

/**
 * ClientPartsInventory con nombre del repuesto y serial del equipo.
 */
data class ClientPartsInventoryWithDetails(
    val inventory: ClientPartsInventory,
    val partName: String,
    val partBrand: String,
    val equipmentSerial: String,
    val clientName: String = "",
)

// endregion

// region ── Asignación de técnico (tabla: assigments) ─────────────────────────

/**
 * Mapea la tabla `assigments` (nota: typo en nombre de tabla es intencional — así está en BD).
 * Columnas: id, request_id, technician_id, created_at
 */
@Serializable
data class Assignment(
    val id: String? = null,
    @SerialName("request_id") val requestId: String = "",
    @SerialName("technician_id") val technicianId: String = "",
    @SerialName("created_at") val createdAt: String? = null,
)

// endregion

// region ── Reporte técnico (tabla: technical_report) ─────────────────────────

/**
 * Mapea la tabla `technical_report`.
 * Columnas: id, assigment_id, diagnosis, work_done, start_time, end_time
 */
@Serializable
data class TechnicalReport(
    val id: String? = null,
    @SerialName("assigment_id") val assigmentId: String = "",
    val diagnosis: String = "",
    @SerialName("work_done") val workDone: Boolean = false,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
)

// endregion

// region ── Calificación (tabla: service_rating) ───────────────────────────────

/**
 * Mapea la tabla `service_rating`.
 * Columnas: assigment_id, score, comments
 */
@Serializable
data class ServiceRating(
    @SerialName("assigment_id") val assigmentId: String = "",
    val score: Int = 5,
    val comments: String? = null,
)

// endregion

// region ── Detalles del técnico (tabla: technician_details) ──────────────────

/**
 * Mapea la tabla `technician_details`.
 * Columnas: user_id, availability, score
 */
@Serializable
data class TechnicianDetails(
    @SerialName("user_id") val userId: String = "",
    val availability: Boolean? = null,
    val score: Double? = null,
)

// endregion

// region ── Roles (enum) ───────────────────────────────────────────────────────

/**
 * Roles soportados en la aplicación "0 Grados".
 * Valores exactos que usa la columna `role` en la tabla `users`.
 */
enum class UserRole(val value: String) {
    CLIENTE("CLIENTE"),
    TECNICO("TÉCNICO"),
    SUPERVISOR("SUPERVISOR");

    companion object {
        fun fromString(value: String): UserRole {
            val v = value.trim()
            return when {
                v.equals("CLIENTE", ignoreCase = true) -> CLIENTE
                v.contains("CNICO", ignoreCase = true) -> TECNICO   // TÉCNICO / TECNICO
                v.equals("SUPERVISOR", ignoreCase = true) -> SUPERVISOR
                else -> CLIENTE
            }
        }
    }
}

// endregion
