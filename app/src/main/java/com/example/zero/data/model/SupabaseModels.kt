package com.example.zero.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================================================
// Modelos serializables que mapean las tablas de Supabase.
//
// IMPORTANTE: Los nombres de campo con @SerialName deben coincidir
// exactamente con los nombres de columna en tu base de datos.
// Si algún nombre no coincide, ajústalo aquí.
// ============================================================================

// region ── Perfil de usuario (tabla: profile) ───────────────────────────────

/**
 * Mapea la tabla `profile` de Supabase.
 * Contiene el rol del usuario (cliente, tecnico, supervisor).
 */
@Serializable
data class Profile(
    val id: String = "",
    @SerialName("full_name") val fullName: String = "",
    val role: String = "",               // "cliente", "tecnico", "supervisor"
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// endregion

// region ── Solicitud de servicio (tabla: service_request) ───────────────────

/**
 * Mapea la tabla `service_request` de Supabase.
 * Representa una solicitud de mantenimiento creada por un cliente.
 */
@Serializable
data class ServiceRequest(
    val id: String? = null,
    @SerialName("client_id") val clientId: String = "",
    val title: String = "",
    val description: String? = null,
    val location: String = "",
    @SerialName("equipment_id") val equipmentId: String? = null,
    val priority: String = "media",      // "alta", "media", "baja"
    val status: String = "SIN INICIAR",  // "SIN INICIAR", "EN PROGRESO", "TERMINADO"
    @SerialName("technician_id") val technicianId: String? = null,
    @SerialName("scheduled_date") val scheduledDate: String? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

// endregion

// region ── Equipo del cliente (tabla: client_equipment) ─────────────────────

/**
 * Mapea la tabla `client_equipment` de Supabase.
 * Equipos registrados a nombre de un cliente.
 */
@Serializable
data class ClientEquipment(
    val id: String = "",
    @SerialName("client_id") val clientId: String = "",
    val name: String = "",
    val brand: String? = null,
    val model: String? = null,
    @SerialName("serial_number") val serialNumber: String? = null,
    val location: String? = null,
    val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// endregion

// region ── Asignación de técnico (tabla: assignments) ───────────────────────

/**
 * Mapea la tabla `assignments` de Supabase.
 * Vincula un técnico a una solicitud de servicio.
 */
@Serializable
data class Assignment(
    val id: String? = null,
    @SerialName("service_request_id") val serviceRequestId: String = "",
    @SerialName("technician_id") val technicianId: String = "",
    val status: String = "asignado",
    @SerialName("assigned_at") val assignedAt: String? = null,
    val notes: String? = null,
)

// endregion

// region ── Roles (enum utilitario) ──────────────────────────────────────────

/**
 * Roles soportados en la aplicación.
 */
enum class UserRole(val value: String) {
    CLIENTE("CLIENTE"),
    TECNICO("TÉCNICO"),
    SUPERVISOR("SUPERVISOR");

    companion object {
        fun fromString(value: String): UserRole {
            val normalized = value.trim().uppercase()
            return entries.firstOrNull { it.value == normalized } ?: CLIENTE
        }
    }
}

// endregion
