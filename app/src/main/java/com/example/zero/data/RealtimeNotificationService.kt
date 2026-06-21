package com.example.zero.data

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.zero.data.model.ServiceRequest
import com.example.zero.data.repository.ServiceRequestRepository
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

// ============================================================================
// RealtimeNotificationService — Foreground Service que mantiene la conexión
// Supabase Realtime activa cuando la app está en segundo plano.
//
// Escucha cambios en service_request y muestra notificaciones del sistema
// según el rol del usuario (técnico/supervisor vs cliente).
// ============================================================================

class RealtimeNotificationService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val repository = ServiceRequestRepository()

    companion object {
        const val NOTIFICATION_ID = 1001
        const val EXTRA_USER_ROLE = "user_role"
        const val EXTRA_USER_ID = "user_id"
        const val ROL_TECNICO = "TÉCNICO"
        const val ROL_SUPERVISOR = "SUPERVISOR"
        const val ROL_CLIENTE = "CLIENTE"
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearCanales(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userRole = intent?.getStringExtra(EXTRA_USER_ROLE) ?: ROL_CLIENTE
        val userId = intent?.getStringExtra(EXTRA_USER_ID) ?: ""

        // Iniciar como foreground service con notificación persistente
        startForeground(
            NOTIFICATION_ID,
            NotificationHelper.construirNotificacionServicio(this)
        )

        // Suscribir a cambios según el rol
        iniciarSuscripcion(userRole, userId)

        return START_STICKY
    }

    private fun iniciarSuscripcion(userRole: String, userId: String) {
        serviceScope.launch {
            try {
                val insertFlow = repository.suscribirInserciones()

                insertFlow
                    .catch { /* Silenciar errores de red */ }
                    .collect { action ->
                        procesarEvento(action, userRole, userId)
                    }
            } catch (_: Exception) { }
        }
    }

    private fun procesarEvento(
        action: PostgresAction.Insert,
        userRole: String,
        userId: String,
    ) {
        val record = action.record
        val solicitudId = record["id"]?.jsonPrimitive?.content ?: return
        val titulo = record["title"]?.jsonPrimitive?.content ?: "Nueva solicitud"
        val ubicacion = record["location"]?.jsonPrimitive?.content ?: ""
        val prioridad = record["priority"]?.jsonPrimitive?.content ?: "media"
        val clientId = record["client_id"]?.jsonPrimitive?.content ?: ""

        when (userRole.uppercase()) {
            ROL_TECNICO, ROL_SUPERVISOR -> {
                // Técnicos y supervisores reciben notificación de nueva solicitud
                NotificationHelper.notificarNuevaSolicitud(
                    context = this,
                    titulo = titulo,
                    ubicacion = ubicacion,
                    prioridad = prioridad,
                    solicitudId = solicitudId,
                )
            }
            else -> {
                // No notificar si la solicitud no pertenece al cliente actual
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            repository.desconectarRealtime()
        }
        serviceScope.cancel()
    }
}
