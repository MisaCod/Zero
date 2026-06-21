package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.model.ClientEquipment
import com.example.zero.data.model.ServiceRequest
import com.example.zero.data.repository.ClientEquipmentRepository
import com.example.zero.data.repository.ServiceRequestRepository
import kotlinx.coroutines.launch

// ============================================================================
// NewServiceRequestViewModel — Gestiona la creación de nuevas solicitudes
// de servicio por parte del cliente.
//
// Flujo:
//   1. Al entrar: carga lista de equipos del cliente (client_equipment)
//   2. Cliente selecciona equipo + escribe descripción del fallo
//   3. Al enviar: INSERT en service_request con status='SIN INICIAR'
// ============================================================================

class NewServiceRequestViewModel : ViewModel() {

    private val equipmentRepo = ClientEquipmentRepository()
    private val requestRepo   = ServiceRequestRepository()

    // ── Lista de equipos disponibles para seleccionar ────────────────────────
    val equipmentList = mutableStateListOf<ClientEquipment>()

    // ── Campos del formulario ────────────────────────────────────────────────
    var selectedEquipment by mutableStateOf<ClientEquipment?>(null)
        private set

    var failureDesc by mutableStateOf("")
        private set

    // ── Estados de UI ────────────────────────────────────────────────────────
    var isLoadingEquipment by mutableStateOf(false)
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var submitSuccess by mutableStateOf(false)
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    // ── Cargar equipos del cliente ───────────────────────────────────────────
    fun loadEquipment(clientId: String) {
        isLoadingEquipment = true
        errorMsg = null
        viewModelScope.launch {
            equipmentRepo.getEquipmentByClient(clientId).fold(
                onSuccess = { list ->
                    equipmentList.clear()
                    equipmentList.addAll(list)
                    isLoadingEquipment = false
                },
                onFailure = { e ->
                    errorMsg = "Error al cargar equipos: ${e.localizedMessage}"
                    isLoadingEquipment = false
                }
            )
        }
    }

    // ── Actualizar campos ────────────────────────────────────────────────────
    fun selectEquipment(equipment: ClientEquipment?) {
        selectedEquipment = equipment
    }

    fun updateFailureDesc(value: String) {
        failureDesc = value
    }

    // ── Enviar solicitud ─────────────────────────────────────────────────────
    fun submit(clientId: String) {
        if (selectedEquipment == null) {
            errorMsg = "Por favor selecciona un equipo"
            return
        }
        if (failureDesc.isBlank()) {
            errorMsg = "Por favor describe el fallo"
            return
        }

        isSubmitting = true
        errorMsg = null
        viewModelScope.launch {
            val solicitud = ServiceRequest(
                clientId    = clientId,
                equipmentId = selectedEquipment!!.id,
                status      = "SIN INICIAR",
                failureDesc = failureDesc.trim(),
            )
            requestRepo.crearSolicitud(solicitud).fold(
                onSuccess = {
                    isSubmitting = false
                    submitSuccess = true
                },
                onFailure = { e ->
                    isSubmitting = false
                    errorMsg = "Error al enviar solicitud: ${e.localizedMessage}"
                }
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    fun clearError()   { errorMsg = null }
    fun resetSuccess() { submitSuccess = false }
}
