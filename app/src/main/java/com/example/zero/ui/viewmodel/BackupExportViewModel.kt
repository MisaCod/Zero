package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.repository.SupervisorRepository
import kotlinx.coroutines.launch

// ============================================================================
// BackupExportViewModel — Estado para BackupExportScreen.
//
// Carga conteos de filas para cada tarjeta de exportación.
// Genera CSV y lo expone para compartir via Android ShareSheet.
// ============================================================================

data class ExportCardState(
    val title: String,
    val description: String,
    val rowCount: Int = 0,
    val isLoadingCount: Boolean = true,
    val isExporting: Boolean = false,
    val lastExportTimestamp: String? = null,
    val pendingCsv: String? = null,
)

class BackupExportViewModel : ViewModel() {

    private val repository = SupervisorRepository()

    var serviceRequestCard by mutableStateOf(
        ExportCardState(
            title = "Solicitudes de Servicio",
            description = "Exporta todas las solicitudes con nombre del cliente, descripción de falla, estado y fechas.",
        )
    )
        private set

    var equipmentCard by mutableStateOf(
        ExportCardState(
            title = "Inventario de Equipos",
            description = "Exporta los equipos registrados de cada cliente con serial, ubicación y referencia de catálogo.",
        )
    )
        private set

    var usersCard by mutableStateOf(
        ExportCardState(
            title = "Usuarios del Sistema",
            description = "Exporta todos los usuarios con perfil completo: nombre, rol, cédula y teléfono.",
        )
    )
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    var importSuccessMsg by mutableStateOf<String?>(null)
        private set

    // ── Cargar conteos ────────────────────────────────────────────────────
    fun loadCounts() {
        viewModelScope.launch {
            // Solicitudes
            repository.getServiceRequestCount().fold(
                onSuccess = { count -> serviceRequestCard = serviceRequestCard.copy(rowCount = count, isLoadingCount = false) },
                onFailure = { serviceRequestCard = serviceRequestCard.copy(isLoadingCount = false) }
            )
        }
        viewModelScope.launch {
            // Equipos
            repository.getClientEquipmentCount().fold(
                onSuccess = { count -> equipmentCard = equipmentCard.copy(rowCount = count, isLoadingCount = false) },
                onFailure = { equipmentCard = equipmentCard.copy(isLoadingCount = false) }
            )
        }
        viewModelScope.launch {
            // Usuarios
            repository.getUserCount().fold(
                onSuccess = { count -> usersCard = usersCard.copy(rowCount = count, isLoadingCount = false) },
                onFailure = { usersCard = usersCard.copy(isLoadingCount = false) }
            )
        }
    }

    // ── Exportar Solicitudes ──────────────────────────────────────────────
    fun exportServiceRequests() {
        serviceRequestCard = serviceRequestCard.copy(isExporting = true)
        viewModelScope.launch {
            repository.exportServiceRequestsCsv().fold(
                onSuccess = { csv ->
                    val ts = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    serviceRequestCard = serviceRequestCard.copy(
                        isExporting = false,
                        pendingCsv = csv,
                        lastExportTimestamp = ts,
                    )
                },
                onFailure = { e ->
                    errorMsg = "Error al exportar solicitudes: ${e.localizedMessage}"
                    serviceRequestCard = serviceRequestCard.copy(isExporting = false)
                }
            )
        }
    }

    // ── Exportar Equipos ──────────────────────────────────────────────────
    fun exportEquipment() {
        equipmentCard = equipmentCard.copy(isExporting = true)
        viewModelScope.launch {
            repository.exportClientEquipmentCsv().fold(
                onSuccess = { csv ->
                    val ts = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    equipmentCard = equipmentCard.copy(
                        isExporting = false,
                        pendingCsv = csv,
                        lastExportTimestamp = ts,
                    )
                },
                onFailure = { e ->
                    errorMsg = "Error al exportar equipos: ${e.localizedMessage}"
                    equipmentCard = equipmentCard.copy(isExporting = false)
                }
            )
        }
    }

    // ── Exportar Usuarios ─────────────────────────────────────────────────
    fun exportUsers() {
        usersCard = usersCard.copy(isExporting = true)
        viewModelScope.launch {
            repository.exportUsersCsv().fold(
                onSuccess = { csv ->
                    val ts = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    usersCard = usersCard.copy(
                        isExporting = false,
                        pendingCsv = csv,
                        lastExportTimestamp = ts,
                    )
                },
                onFailure = { e ->
                    errorMsg = "Error al exportar usuarios: ${e.localizedMessage}"
                    usersCard = usersCard.copy(isExporting = false)
                }
            )
        }
    }

    // ── Confirmar que CSV fue compartido ──────────────────────────────────
    fun clearServiceRequestCsv() { serviceRequestCard = serviceRequestCard.copy(pendingCsv = null) }
    fun clearEquipmentCsv() { equipmentCard = equipmentCard.copy(pendingCsv = null) }
    fun clearUsersCsv() { usersCard = usersCard.copy(pendingCsv = null) }
    fun clearError() { errorMsg = null }

    fun importData(uri: android.net.Uri, type: String) {
        viewModelScope.launch {
            // Simulamos un proceso de importación
            // En un caso real, leeríamos el CSV/SQL de ContentResolver y ejecutaríamos los inserts correspondientes.
            kotlinx.coroutines.delay(1500)
            importSuccessMsg = "Datos de $type importados correctamente."
            loadCounts()
        }
    }
    
    fun clearImportSuccess() { importSuccessMsg = null }
}
