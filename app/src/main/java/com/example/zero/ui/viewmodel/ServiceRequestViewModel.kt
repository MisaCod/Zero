package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.model.ServiceRequest
import com.example.zero.data.model.UserRole
import com.example.zero.data.repository.ServiceRequestRepository
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

// ============================================================================
// ServiceRequestViewModel — Gestiona solicitudes y notificaciones Realtime.
//
// Funcionalidades:
//   • Crear solicitudes de mantenimiento (INSERT)
//   • Listar solicitudes existentes (SELECT)
//   • Suscripción Realtime para notificar nuevas solicitudes a técnicos
//   • Estado de notificación visual (banner en la app)
// ============================================================================

class ServiceRequestViewModel : ViewModel() {

    private val repository = ServiceRequestRepository()

    // ── Estado de solicitudes ────────────────────────────────────────────
    val solicitudes = mutableStateListOf<ServiceRequest>()

    var isLoading by mutableStateOf(false)
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    var operacionExitosa by mutableStateOf(false)
        private set

    // ── Estado de notificaciones Realtime ─────────────────────────────────
    var nuevaSolicitudNotificacion by mutableStateOf<ServiceRequest?>(null)
        private set

    var mostrarBannerNotificacion by mutableStateOf(false)
        private set

    private var realtimeJob: Job? = null

    // ── Cargar solicitudes ───────────────────────────────────────────────
    /**
     * Obtiene todas las solicitudes visibles para el usuario actual.
     * Las políticas RLS filtran automáticamente según el rol.
     */
    fun cargarSolicitudes() {
        isLoading = true
        errorMsg = null

        viewModelScope.launch {
            val result = repository.obtenerSolicitudes()
            result.fold(
                onSuccess = { lista ->
                    solicitudes.clear()
                    solicitudes.addAll(lista)
                    isLoading = false
                },
                onFailure = { e ->
                    errorMsg = "Error al cargar solicitudes: ${e.localizedMessage}"
                    isLoading = false
                },
            )
        }
    }

    // ── Crear solicitud (para clientes) ──────────────────────────────────
    /**
     * Inserta una nueva solicitud de mantenimiento en la base de datos.
     *
     * @param titulo       Título descriptivo del servicio requerido
     * @param descripcion  Detalles del problema o mantenimiento
     * @param ubicacion    Ubicación del equipo
     * @param equipo       ID o nombre del equipo (opcional)
     * @param prioridad    "alta", "media" o "baja"
     * @param fechaProgramada Fecha deseada para el servicio (ISO 8601)
     * @param clientId     UUID del cliente autenticado
     * @param onSuccess    Callback al completarse exitosamente
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
        if (titulo.isBlank() || ubicacion.isBlank()) {
            errorMsg = "Título y ubicación son obligatorios"
            return
        }

        isLoading = true
        errorMsg = null
        operacionExitosa = false

        viewModelScope.launch {
            val solicitud = ServiceRequest(
                clientId = clientId,
                title = titulo,
                description = descripcion.ifBlank { null },
                location = ubicacion,
                equipmentId = equipo?.ifBlank { null },
                priority = prioridad,
                scheduledDate = fechaProgramada,
            )

            val result = repository.crearSolicitud(solicitud)
            result.fold(
                onSuccess = { creada ->
                    solicitudes.add(0, creada) // Agregar al inicio de la lista
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

    // ── Suscripción Realtime (para técnicos) ─────────────────────────────
    /**
     * Inicia la suscripción Realtime a la tabla service_request.
     * Cada vez que un cliente crea una nueva solicitud, el técnico
     * recibirá una notificación visual dentro de la app.
     *
     * Solo debe llamarse si el usuario tiene rol de TECNICO o SUPERVISOR.
     */
    fun iniciarRealtimeTecnico() {
        // Evitar suscripciones duplicadas
        if (realtimeJob?.isActive == true) return

        realtimeJob = viewModelScope.launch {
            try {
                val insertFlow = repository.suscribirInserciones()

                insertFlow
                    .catch { e ->
                        errorMsg = "Error en Realtime: ${e.localizedMessage}"
                    }
                    .collect { insertAction ->
                        // Extraer datos de la nueva solicitud del evento
                        val record = insertAction.record
                        val nuevaSolicitud = ServiceRequest(
                            id = record["id"]?.jsonPrimitive?.content,
                            clientId = record["client_id"]?.jsonPrimitive?.content ?: "",
                            title = record["title"]?.jsonPrimitive?.content ?: "Nueva Solicitud",
                            description = record["description"]?.jsonPrimitive?.content,
                            location = record["location"]?.jsonPrimitive?.content ?: "",
                            priority = record["priority"]?.jsonPrimitive?.content ?: "media",
                            status = record["status"]?.jsonPrimitive?.content ?: "SIN INICIAR",
                            createdAt = record["created_at"]?.jsonPrimitive?.content,
                        )

                        // Actualizar estado para mostrar banner
                        nuevaSolicitudNotificacion = nuevaSolicitud
                        mostrarBannerNotificacion = true

                        // Agregar a la lista local
                        solicitudes.add(0, nuevaSolicitud)
                    }
            } catch (e: Exception) {
                errorMsg = "Error al conectar Realtime: ${e.localizedMessage}"
            }
        }
    }

    // ── Ocultar banner de notificación ───────────────────────────────────
    fun ocultarBannerNotificacion() {
        mostrarBannerNotificacion = false
        nuevaSolicitudNotificacion = null
    }

    // ── Actualizar estado de solicitud ───────────────────────────────────
    fun actualizarEstado(solicitudId: String, nuevoEstado: String) {
        viewModelScope.launch {
            val result = repository.actualizarEstado(solicitudId, nuevoEstado)
            result.fold(
                onSuccess = { cargarSolicitudes() },
                onFailure = { e ->
                    errorMsg = "Error al actualizar: ${e.localizedMessage}"
                },
            )
        }
    }

    // ── Limpiar estado ───────────────────────────────────────────────────
    fun clearError() {
        errorMsg = null
    }

    fun resetOperacionExitosa() {
        operacionExitosa = false
    }

    // ── Cleanup ──────────────────────────────────────────────────────────
    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
        viewModelScope.launch {
            repository.desconectarRealtime()
        }
    }
}
