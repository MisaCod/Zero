package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.repository.AlertaSupabase
import com.example.zero.data.repository.AlertsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

// ============================================================================
// AlertsViewModel — Gestiona alertas en tiempo real desde Supabase.
//
// Funcionalidades:
//   • Cargar alertas activas (SELECT)
//   • Marcar como atendida (UPDATE + animación de salida)
//   • Suscripción Realtime para recibir nuevas alertas al instante
// ============================================================================

class AlertsViewModel : ViewModel() {

    private val repository = AlertsRepository()

    val alertas = mutableStateListOf<AlertaSupabase>()

    var isLoading by mutableStateOf(false)
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    // ── Cargar alertas activas ───────────────────────────────────────────
    fun cargarAlertas() {
        isLoading = true
        viewModelScope.launch {
            val result = repository.obtenerAlertasActivas()
            result.fold(
                onSuccess = { lista ->
                    alertas.clear()
                    alertas.addAll(lista)
                    isLoading = false

                    // Iniciar Realtime después de cargar
                    iniciarRealtime()
                },
                onFailure = { e ->
                    errorMsg = e.localizedMessage
                    isLoading = false
                },
            )
        }
    }

    // ── Marcar alerta como atendida ──────────────────────────────────────
    fun atenderAlerta(alertaId: String) {
        viewModelScope.launch {
            alertas.removeIf { it.id == alertaId }
            repository.marcarComoAtendida(alertaId)
        }
    }

    // ── Suscripción Realtime ─────────────────────────────────────────────
    private fun iniciarRealtime() {
        viewModelScope.launch {
            try {
                repository.suscribirAlertas()
                    .catch { /* ignorar errores de red */ }
                    .collect { insertAction ->
                        val record = insertAction.record
                        val nuevaAlerta = AlertaSupabase(
                            id = record["id"]?.jsonPrimitive?.content,
                            titulo = record["titulo"]?.jsonPrimitive?.content ?: "Nueva alerta",
                            descripcion = record["descripcion"]?.jsonPrimitive?.content ?: "",
                            severidad = record["severidad"]?.jsonPrimitive?.content ?: "warning",
                            equipo = record["equipo"]?.jsonPrimitive?.content,
                            ubicacion = record["ubicacion"]?.jsonPrimitive?.content,
                            atendida = false,
                        )
                        alertas.add(0, nuevaAlerta)
                    }
            } catch (_: Exception) { }
        }
    }

    fun clearError() { errorMsg = null }
}
