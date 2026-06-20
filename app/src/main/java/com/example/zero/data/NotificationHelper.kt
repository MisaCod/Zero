package com.example.zero.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.zero.MainActivity

// ============================================================================
// NotificationHelper — Gestiona canales y creación de notificaciones Android.
//
// Canales:
//   • CANAL_SOLICITUDES: Para nuevas solicitudes de mantenimiento
//   • CANAL_ESTADOS:     Para cambios de estado en solicitudes
//   • CANAL_ALERTAS:     Para alertas críticas del sistema
// ============================================================================

object NotificationHelper {

    const val CANAL_SOLICITUDES = "canal_solicitudes"
    const val CANAL_ESTADOS = "canal_estados"
    const val CANAL_ALERTAS = "canal_alertas"
    const val CANAL_SERVICIO = "canal_servicio_foreground"

    // ── Crear todos los canales de notificación ──────────────────────────
    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val canalSolicitudes = NotificationChannel(
                CANAL_SOLICITUDES,
                "Nuevas Solicitudes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando se crea una nueva solicitud de mantenimiento"
                enableVibration(true)
            }

            val canalEstados = NotificationChannel(
                CANAL_ESTADOS,
                "Actualización de Estados",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando cambia el estado de tu solicitud"
            }

            val canalAlertas = NotificationChannel(
                CANAL_ALERTAS,
                "Alertas Críticas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas críticas de equipos de refrigeración"
                enableVibration(true)
                enableLights(true)
            }

            val canalServicio = NotificationChannel(
                CANAL_SERVICIO,
                "Servicio en segundo plano",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Mantiene la conexión en tiempo real activa"
            }

            manager.createNotificationChannels(
                listOf(canalSolicitudes, canalEstados, canalAlertas, canalServicio)
            )
        }
    }

    // ── Notificar nueva solicitud (para Técnicos y Supervisores) ─────────
    fun notificarNuevaSolicitud(
        context: Context,
        titulo: String,
        ubicacion: String,
        prioridad: String,
        solicitudId: String,
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("solicitud_id", solicitudId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prioridadEmoji = when (prioridad.lowercase()) {
            "alta" -> "🔴"
            "media" -> "🟡"
            else -> "🟢"
        }

        val notification = NotificationCompat.Builder(context, CANAL_SOLICITUDES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$prioridadEmoji Nueva solicitud de mantenimiento")
            .setContentText(titulo)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$titulo\n📍 $ubicacion")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(solicitudId.hashCode(), notification)
    }

    // ── Notificar cambio de estado (para Clientes) ───────────────────────
    fun notificarCambioEstado(
        context: Context,
        tituloSolicitud: String,
        nuevoEstado: String,
        solicitudId: String,
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("solicitud_id", solicitudId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (emoji, mensaje) = when (nuevoEstado.uppercase()) {
            "EN PROGRESO" -> Pair("🔧", "Tu solicitud está siendo atendida")
            "TERMINADO" -> Pair("✅", "Tu solicitud ha sido completada")
            else -> Pair("📋", "El estado de tu solicitud ha cambiado")
        }

        val notification = NotificationCompat.Builder(context, CANAL_ESTADOS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$emoji Estado actualizado")
            .setContentText(tituloSolicitud)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$tituloSolicitud\n$mensaje")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify((solicitudId + nuevoEstado).hashCode(), notification)
    }

    // ── Notificación del servicio en primer plano ────────────────────────
    fun construirNotificacionServicio(context: Context): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CANAL_SERVICIO)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("0 Grados")
            .setContentText("Conectado — Monitoreo en tiempo real activo")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
