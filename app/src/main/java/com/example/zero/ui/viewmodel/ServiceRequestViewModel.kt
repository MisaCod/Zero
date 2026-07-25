package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.model.ServiceRequest
import com.example.zero.data.model.ServiceRequestWithEquipment
import com.example.zero.data.repository.ServiceRequestRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

// ============================================================================
// ServiceRequestViewModel — Gestiona solicitudes y notificaciones Realtime.
//
// Esquema REAL:
//   tabla: service_request
//   cols:  id, client_id, equipment_id, status, failure_desc, created_at
//
// Funcionalidades:
//   • Crear solicitudes (INSERT con failure_desc)
//   • Listar solicitudes (SELECT con join a client_equipment)
//   • Suscripción Realtime INSERT → notificación en app para técnicos
//   • Suscripción Realtime UPDATE → actualizar estado en tiempo real
//   • Actualizar estado (UPDATE status)
// ============================================================================

class ServiceRequestViewModel : ViewModel() {

    private val repository = ServiceRequestRepository()

    // ── Lista principal de solicitudes (con datos de equipo) ─────────────
    val solicitudes = mutableStateListOf<ServiceRequestWithEquipment>()

    // ── Estados de UI ────────────────────────────────────────────────────
    var isLoading by mutableStateOf(false)
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    var operacionExitosa by mutableStateOf(false)
        private set

    // ── Notificaciones Realtime ───────────────────────────────────────────
    var nuevaSolicitudNotificacion by mutableStateOf<ServiceRequestWithEquipment?>(null)
        private set

    var mostrarBannerNotificacion by mutableStateOf(false)
        private set

    private var realtimeJob: Job? = null

    // ── Cargar solicitudes ────────────────────────────────────────────────
    fun cargarSolicitudes() {
        isLoading = true
        errorMsg = null
        viewModelScope.launch {
            repository.obtenerSolicitudes().fold(
                onSuccess = { lista ->
                    solicitudes.clear()
                    solicitudes.addAll(lista)
                    isLoading = false
                },
                onFailure = { e ->
                    errorMsg = "Error al cargar: ${e.localizedMessage}"
                    isLoading = false
                },
            )
        }
    }

    // ── Crear solicitud (para Clientes) ───────────────────────────────────
    /**
     * Crea una nueva solicitud usando el esquema real de la BD.
     * El campo `titulo` se mapea a `failure_desc`.
     * El campo `equipo` debe ser un UUID de client_equipment o null.
     */
    fun crearSolicitud(
        titulo: String,
        descripcion: String,
        ubicacion: String,
        equipo: String?,
        prioridad: String,
        fechaProgramada: String?,
        clientId: String,
        onSuccess: () -> Unit = {},
    ) {
        if (titulo.isBlank()) {
            errorMsg = "La descripción del problema es obligatoria"
            return
        }

        isLoading = true
        errorMsg = null
        operacionExitosa = false

        viewModelScope.launch {
            // failure_desc combina toda la informacion
            val failureDescFull = """
                Título: $titulo
                Ubicación: $ubicacion
                Equipo: ${equipo?.ifBlank { "No especificado" } ?: "No especificado"}
                Prioridad: $prioridad
                Fecha deseada: ${fechaProgramada?.ifBlank { "No especificada" } ?: "No especificada"}
                
                Descripción: ${descripcion.ifBlank { "Sin detalles adicionales" }}
            """.trimIndent()

            val solicitud = ServiceRequest(
                clientId = clientId,
                equipmentId = null, // Se envía null porque no es un UUID seleccionado
                status = "SIN INICIAR",
                failureDesc = failureDescFull,
            )

            repository.crearSolicitud(solicitud).fold(
                onSuccess = { creada ->
                    val enriquecida = ServiceRequestWithEquipment(
                        id = creada.id,
                        clientId = creada.clientId,
                        equipmentId = creada.equipmentId,
                        status = creada.status,
                        failureDesc = creada.failureDesc,
                        createdAt = creada.createdAt,
                    )
                    solicitudes.add(0, enriquecida)
                    isLoading = false
                    operacionExitosa = true
                    onSuccess()
                },
                onFailure = { e ->
                    isLoading = false
                    errorMsg = "Error al crear solicitud: ${e.localizedMessage}"
                },
            )
        }
    }

    // ── Suscripción Realtime (para Técnicos/Supervisores) ─────────────────
    /**
     * Escucha nuevas solicitudes en tiempo real.
     * Muestra banner de notificación en la app cuando llega una nueva.
     */
    fun iniciarRealtimeTecnico() {
        if (realtimeJob?.isActive == true) return

        realtimeJob = viewModelScope.launch {
            try {
                repository.suscribirInserciones()
                    .catch { /* ignorar errores de red */ }
                    .collect { action ->
                        val rec = action.record
                        val nueva = ServiceRequestWithEquipment(
                            id = rec["id"]?.jsonPrimitive?.content,
                            clientId = rec["client_id"]?.jsonPrimitive?.content ?: "",
                            equipmentId = rec["equipment_id"]?.jsonPrimitive?.content,
                            status = rec["status"]?.jsonPrimitive?.content ?: "SIN INICIAR",
                            failureDesc = rec["failure_desc"]?.jsonPrimitive?.content ?: "Nueva solicitud",
                            createdAt = rec["created_at"]?.jsonPrimitive?.content,
                        )
                        nuevaSolicitudNotificacion = nueva
                        mostrarBannerNotificacion = true
                        solicitudes.add(0, nueva)
                    }
            } catch (_: Exception) { }
        }
    }

    // ── Actualizar estado (UPDATE) ────────────────────────────────────────
    fun actualizarEstado(solicitudId: String, nuevoEstado: String) {
        viewModelScope.launch {
            // Actualizar localmente primero (optimistic update)
            val idx = solicitudes.indexOfFirst { it.id == solicitudId }
            if (idx >= 0) {
                val actual = solicitudes[idx]
                solicitudes[idx] = actual.copy(status = nuevoEstado)
            }
            // Luego persistir en BD
            repository.actualizarEstado(solicitudId, nuevoEstado).onFailure { e ->
                errorMsg = "Error al actualizar: ${e.localizedMessage}"
                // Revertir si falla
                if (idx >= 0) cargarSolicitudes()
            }
        }
    }

    // ── Helpers UI ────────────────────────────────────────────────────────
    fun ocultarBannerNotificacion() {
        mostrarBannerNotificacion = false
        nuevaSolicitudNotificacion = null
    }

    fun clearError() { errorMsg = null }
    fun resetOperacionExitosa() { operacionExitosa = false }

    // ── Resolver assignmentId a partir del requestId ───────────────────────
    fun buscarAssignmentIdPorRequest(requestId: String, technicianId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val repo = com.example.zero.data.repository.TechnicianRepository()
            repo.getAssignmentByRequestId(requestId).fold(
                onSuccess = { assignment -> 
                    if (assignment != null && assignment.id != null) {
                        onResult(assignment.id)
                    } else if (technicianId.isNotEmpty()) {
                        // Si no existe asignación pero está en progreso y tenemos técnico, la creamos (auto-asignación)
                        viewModelScope.launch {
                            val supRepo = com.example.zero.data.repository.SupervisorRepository()
                            supRepo.assignTechnician(requestId, technicianId).onSuccess {
                                // Buscar de nuevo
                                repo.getAssignmentByRequestId(requestId).onSuccess { newAssign ->
                                    onResult(newAssign?.id)
                                }.onFailure { 
                                    errorMsg = "Error al obtener ID de asignación creada"
                                    onResult(null) 
                                }
                            }.onFailure {
                                errorMsg = "Error al auto-asignar técnico: ${it.localizedMessage}"
                                onResult(null)
                            }
                        }
                    } else {
                        // Cliente buscando asignación que no existe aún
                        errorMsg = "No se encontró un técnico asignado a este servicio para calificar."
                        onResult(null)
                    }
                },
                onFailure = { 
                    errorMsg = "Error al buscar asignación: ${it.localizedMessage}"
                    onResult(null) 
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
        viewModelScope.launch { repository.desconectarRealtime() }
    }
}
