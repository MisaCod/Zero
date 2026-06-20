package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.model.TechnicalReport
import com.example.zero.data.repository.ServiceRequestRepository
import com.example.zero.data.repository.TechnicalReportRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter

// ============================================================================
// TechnicalReportViewModel — Gestiona el reporte técnico de una asignación.
//
// Flujo:
//   1. Al entrar: registra start_time = ahora
//   2. Técnico escribe diagnóstico + marca si trabajo fue completado
//   3. Al finalizar: INSERT en technical_report con end_time = ahora
//   4. UPDATE service_request.status = 'TERMINADO' si work_done = true
// ============================================================================

class TechnicalReportViewModel : ViewModel() {

    private val reportRepo  = TechnicalReportRepository()
    private val requestRepo = ServiceRequestRepository()

    // ── Campos del formulario ────────────────────────────────────────────────
    var diagnosis by mutableStateOf("")
        private set

    var workDone by mutableStateOf(false)
        private set

    // start_time fijo al momento de abrir la pantalla
    val startTime: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

    // ── Estados de UI ────────────────────────────────────────────────────────
    var isSubmitting by mutableStateOf(false)
        private set

    var submitSuccess by mutableStateOf(false)
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    // ── Actualizar campos ────────────────────────────────────────────────────
    fun updateDiagnosis(value: String) {
        diagnosis = value
    }

    fun toggleWorkDone(value: Boolean) {
        workDone = value
    }

    // ── Finalizar y enviar reporte ───────────────────────────────────────────
    fun submitReport(assigmentId: String, requestId: String?) {
        if (diagnosis.isBlank()) {
            errorMsg = "El diagnóstico técnico es obligatorio"
            return
        }

        isSubmitting = true
        errorMsg = null
        viewModelScope.launch {
            val endTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            val report = TechnicalReport(
                assigmentId = assigmentId,
                diagnosis   = diagnosis.trim(),
                workDone    = workDone,
                startTime   = startTime,
                endTime     = endTime,
            )

            reportRepo.insertReport(report).fold(
                onSuccess = {
                    // Si el trabajo está marcado como completado, actualizar estado
                    if (workDone && requestId != null) {
                        requestRepo.actualizarEstado(requestId, "TERMINADO")
                    }
                    isSubmitting = false
                    submitSuccess = true
                },
                onFailure = { e ->
                    isSubmitting = false
                    errorMsg = "Error al enviar reporte: ${e.localizedMessage}"
                }
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    fun clearError()   { errorMsg = null }
    fun resetSuccess() { submitSuccess = false }
}
