package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.repository.MaintenanceReportRow
import com.example.zero.data.repository.PartsInventoryRow
import com.example.zero.data.repository.SupervisorRepository
import com.example.zero.data.repository.TechnicianPerformanceRow
import kotlinx.coroutines.launch

// ============================================================================
// ReportsViewModel — Estado para ReportsScreen (3 tabs).
//
// Tabs:
//   1. Mantenimiento → MaintenanceReportRow list
//   2. Rendimiento   → TechnicianPerformanceRow list
//   3. Inventario    → PartsInventoryRow list
//
// Filtros en memoria para performance.
// CSV generation delegated to repository.
// ============================================================================

class ReportsViewModel : ViewModel() {

    private val repository = SupervisorRepository()

    // ── Tab 1: Mantenimiento ──────────────────────────────────────────────
    val maintenanceRows = mutableStateListOf<MaintenanceReportRow>()
    var maintenanceStatusFilter by mutableStateOf("TODOS")
        private set

    val filteredMaintenance: List<MaintenanceReportRow>
        get() = if (maintenanceStatusFilter == "TODOS") maintenanceRows.toList()
                else maintenanceRows.filter { it.status == maintenanceStatusFilter }

    // ── Tab 2: Rendimiento ────────────────────────────────────────────────
    val performanceRows = mutableStateListOf<TechnicianPerformanceRow>()
    var minScoreFilter by mutableStateOf(0.0)
        private set
    var maxScoreFilter by mutableStateOf(5.0)
        private set

    val filteredPerformance: List<TechnicianPerformanceRow>
        get() = performanceRows.filter { it.score in minScoreFilter..maxScoreFilter }

    // ── Tab 3: Inventario ─────────────────────────────────────────────────
    val inventoryRows = mutableStateListOf<PartsInventoryRow>()
    var inventorySearchQuery by mutableStateOf("")
        private set

    val filteredInventory: List<PartsInventoryRow>
        get() = if (inventorySearchQuery.isBlank()) inventoryRows.toList()
                else inventoryRows.filter {
                    it.partName.contains(inventorySearchQuery, ignoreCase = true) ||
                    it.clientName.contains(inventorySearchQuery, ignoreCase = true) ||
                    it.serialNum.contains(inventorySearchQuery, ignoreCase = true)
                }

    // ── UI States ─────────────────────────────────────────────────────────
    var isLoadingMaintenance by mutableStateOf(false)
        private set

    var isLoadingPerformance by mutableStateOf(false)
        private set

    var isLoadingInventory by mutableStateOf(false)
        private set

    var isExporting by mutableStateOf(false)
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    var exportedCsv by mutableStateOf<String?>(null)
        private set

    // ── Carga de tabs ─────────────────────────────────────────────────────
    fun loadMaintenanceReport() {
        if (maintenanceRows.isNotEmpty()) return
        isLoadingMaintenance = true
        viewModelScope.launch {
            repository.getMaintenanceReport().fold(
                onSuccess = { list ->
                    maintenanceRows.clear()
                    maintenanceRows.addAll(list)
                },
                onFailure = { e -> errorMsg = "Error mantenimiento: ${e.localizedMessage}" }
            )
            isLoadingMaintenance = false
        }
    }

    fun loadPerformanceReport() {
        if (performanceRows.isNotEmpty()) return
        isLoadingPerformance = true
        viewModelScope.launch {
            repository.getTechnicianPerformance().fold(
                onSuccess = { list ->
                    performanceRows.clear()
                    performanceRows.addAll(list)
                },
                onFailure = { e -> errorMsg = "Error rendimiento: ${e.localizedMessage}" }
            )
            isLoadingPerformance = false
        }
    }

    fun loadInventoryReport() {
        if (inventoryRows.isNotEmpty()) return
        isLoadingInventory = true
        viewModelScope.launch {
            repository.getPartsInventoryReport().fold(
                onSuccess = { list ->
                    inventoryRows.clear()
                    inventoryRows.addAll(list)
                },
                onFailure = { e -> errorMsg = "Error inventario: ${e.localizedMessage}" }
            )
            isLoadingInventory = false
        }
    }

    // ── Filtros ──────────────────────────────────────────────────────────────────────────────
    fun updateMaintenanceStatusFilter(status: String) { maintenanceStatusFilter = status }
    fun updateScoreFilter(min: Double, max: Double) { minScoreFilter = min; maxScoreFilter = max }
    fun updateInventorySearch(query: String) { inventorySearchQuery = query }

    // ── Exportar CSV del tab activo ───────────────────────────────────────
    fun exportCurrentTabCsv(tabIndex: Int) {
        isExporting = true
        viewModelScope.launch {
            val result = when (tabIndex) {
                0 -> {
                    val sb = StringBuilder()
                    sb.appendLine("ID,Serial,Descripción,Estado,Técnico,Inicio,Fin,Ubicación,Fecha")
                    filteredMaintenance.forEach { r ->
                        sb.appendLine("${r.requestId},${r.serialNum},\"${r.failureDesc.replace("\"","'")}\",${r.status},\"${r.technicianName}\",${r.startTime ?: ""},${r.endTime ?: ""},\"${r.location}\",${r.createdAt ?: ""}")
                    }
                    Result.success(sb.toString())
                }
                1 -> {
                    val sb = StringBuilder()
                    sb.appendLine("Nombre,Email,Puntuación,Asignaciones Completadas,Calificación Promedio")
                    filteredPerformance.forEach { r ->
                        sb.appendLine("\"${r.name}\",${r.email},${r.score},${r.completedAssignments},${String.format("%.2f", r.averageRating)}")
                    }
                    Result.success(sb.toString())
                }
                2 -> {
                    val sb = StringBuilder()
                    sb.appendLine("ID,Repuesto,Marca,Cliente,Serial,Ubicación,Fecha")
                    filteredInventory.forEach { r ->
                        sb.appendLine("${r.id},\"${r.partName}\",\"${r.brandName}\",\"${r.clientName}\",${r.serialNum},\"${r.location}\",${r.createdAt ?: ""}")
                    }
                    Result.success(sb.toString())
                }
                else -> Result.failure(Exception("Tab desconocido"))
            }
            result.fold(
                onSuccess = { csv -> exportedCsv = csv },
                onFailure = { e -> errorMsg = "Error al exportar: ${e.localizedMessage}" }
            )
            isExporting = false
        }
    }

    fun clearExportedCsv() { exportedCsv = null }
    fun clearError() { errorMsg = null }

    // Force refresh all tabs
    fun refreshAll() {
        maintenanceRows.clear()
        performanceRows.clear()
        inventoryRows.clear()
        loadMaintenanceReport()
    }
}
