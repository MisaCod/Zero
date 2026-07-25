package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.model.ServiceRequest
import com.example.zero.data.repository.AvailableTechnician
import com.example.zero.data.repository.SupervisorRepository
import kotlinx.coroutines.launch

// ============================================================================
// AssignTechnicianViewModel — Estado para AsignTechnicianScreen.
//
// Carga:
//   • Detalles de la solicitud de servicio (service_request)
//   • Equipo relacionado (client_equipment)
//   • Lista de técnicos disponibles (technician_details WHERE availability=true)
//
// Acciones:
//   • assignTechnician: INSERT en assignments + UPDATE technician availability
// ============================================================================

class AssignTechnicianViewModel : ViewModel() {

    private val repository = SupervisorRepository()

    // ── Estado de la solicitud ────────────────────────────────────────────
    var serviceRequest by mutableStateOf<ServiceRequest?>(null)
        private set

    var equipmentLocation by mutableStateOf("")
        private set

    var equipmentSerialNum by mutableStateOf("")
        private set

    // ── Lista de técnicos disponibles ─────────────────────────────────────
    val technicians = mutableStateListOf<AvailableTechnician>()

    // ── UI States ─────────────────────────────────────────────────────────
    var isLoading by mutableStateOf(false)
        private set

    var isAssigning by mutableStateOf(false)
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    var assignmentSuccess by mutableStateOf(false)
        private set

    var assignedTechnicianName by mutableStateOf("")
        private set

    // ── Cargar datos de la solicitud ──────────────────────────────────────
    fun loadData(requestId: String) {
        isLoading = true
        errorMsg = null
        viewModelScope.launch {
            // Cargar solicitud
            repository.getServiceRequest(requestId).fold(
                onSuccess = { req ->
                    serviceRequest = req
                    // Cargar equipo si tiene ID
                    req.equipmentId?.let { eqId ->
                        repository.getEquipmentForRequest(eqId).onSuccess { (location, serialNum) ->
                            equipmentLocation = location.ifBlank { "Sin ubicación" }
                            equipmentSerialNum = serialNum
                        }
                    }
                },
                onFailure = { e ->
                    errorMsg = "Error al cargar solicitud: ${e.localizedMessage}"
                }
            )
            // Cargar técnicos disponibles
            repository.getAvailableTechnicians().fold(
                onSuccess = { list ->
                    technicians.clear()
                    technicians.addAll(list)
                },
                onFailure = { e ->
                    errorMsg = "Error al cargar técnicos: ${e.localizedMessage}"
                }
            )
            isLoading = false
        }
    }

    // ── Asignar técnico ───────────────────────────────────────────────────
    fun assignTechnician(requestId: String, technician: AvailableTechnician) {
        isAssigning = true
        errorMsg = null
        viewModelScope.launch {
            // 1. INSERT en assignments
            repository.assignTechnician(requestId, technician.userId).fold(
                onSuccess = {
                    // 2. Marcar técnico como no disponible
                    repository.setTechnicianAvailability(technician.userId, false)
                    // 3. Actualizar lista local
                    technicians.remove(technician)
                    assignedTechnicianName = "${technician.name} ${technician.lastName}".trim()
                    assignmentSuccess = true
                },
                onFailure = { e ->
                    errorMsg = "Error al asignar: ${e.localizedMessage}"
                }
            )
            isAssigning = false
        }
    }

    fun clearError() { errorMsg = null }
    fun resetSuccess() { assignmentSuccess = false }
}
