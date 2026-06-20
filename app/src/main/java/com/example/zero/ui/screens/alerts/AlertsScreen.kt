package com.example.zero.ui.screens.alerts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.repository.AlertaSupabase
import com.example.zero.ui.components.CriticalAlertChip
import com.example.zero.ui.components.TechFlowPrimaryButton
import com.example.zero.ui.components.TechFlowSecondaryButton
import com.example.zero.ui.components.WarningChip
import com.example.zero.ui.components.StatusChip
import com.example.zero.ui.components.ChipShape
import com.example.zero.ui.theme.CodeTextStyle
import com.example.zero.ui.theme.Dimens
import com.example.zero.ui.viewmodel.AlertsViewModel

// ============================================================================
// AlertsScreen — Conectada a Supabase Realtime.
//
// • Carga alertas activas desde la tabla `alerts`
// • Recibe nuevas alertas en tiempo real vía Supabase Realtime
// • Permite atender/descartar alertas (UPDATE en BD)
// • Fallback elegante si la tabla aún no existe (muestra vacío en lugar de error)
// ============================================================================

@Composable
fun AlertsScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    alertsViewModel: AlertsViewModel = viewModel(),
) {
    LaunchedEffect(Unit) {
        alertsViewModel.cargarAlertas()
    }

    val alertas = alertsViewModel.alertas
    val isLoading = alertsViewModel.isLoading
    val criticalCount = alertas.count { it.severidad == "critical" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + Dimens.md,
            bottom = contentPadding.calculateBottomPadding() + Dimens.xl,
            start = Dimens.marginMobile,
            end = Dimens.marginMobile,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        // ── Header ───────────────────────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Alertas del Sistema",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                when {
                    isLoading -> LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(top = Dimens.sm),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    criticalCount > 0 -> Text(
                        text = "🔴 $criticalCount alerta${if (criticalCount > 1) "s" else ""} crítica${if (criticalCount > 1) "s" else ""} requiere${if (criticalCount == 1) "" else "n"} atención",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                    alertas.isNotEmpty() -> Text(
                        text = "✅ Sin alertas críticas — ${alertas.size} advertencia${if (alertas.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // ── Lista de alertas con animación ────────────────────────────────
        items(alertas, key = { it.id ?: it.titulo }) { alerta ->
            val dismissState = remember { MutableTransitionState(true) }

            AnimatedVisibility(
                visibleState = dismissState,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) +
                        shrinkVertically(animationSpec = tween(200, delayMillis = 100)) +
                        fadeOut(animationSpec = tween(200)),
            ) {
                AlertCard(
                    alerta = alerta,
                    onAtender = {
                        dismissState.targetState = false
                        alerta.id?.let { alertsViewModel.atenderAlerta(it) }
                    },
                )
            }
            if (dismissState.isIdle && !dismissState.currentState) {
                LaunchedEffect(Unit) { alertas.remove(alerta) }
            }
        }

        // ── Estado vacío ──────────────────────────────────────────────────
        if (!isLoading && alertas.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(Dimens.xl),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(Dimens.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            "¡Sin alertas activas!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Todos los equipos operan dentro de parámetros normales",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// AlertCard — Tarjeta de alerta con datos de Supabase
// ============================================================================

@Composable
private fun AlertCard(
    alerta: AlertaSupabase,
    onAtender: () -> Unit = {},
) {
    val borderColor = when (alerta.severidad) {
        "critical" -> MaterialTheme.colorScheme.error
        "warning"  -> MaterialTheme.colorScheme.tertiary
        else       -> MaterialTheme.colorScheme.primary
    }
    val alertIcon: ImageVector = when (alerta.severidad) {
        "critical" -> Icons.Filled.Error
        "warning"  -> Icons.Filled.Warning
        else       -> Icons.Filled.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = Dimens.xl, bottomEnd = Dimens.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(IntrinsicSize.Max)
                    .background(borderColor),
            )
            Column(
                modifier = Modifier.weight(1f).padding(Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (alerta.severidad) {
                        "critical" -> CriticalAlertChip()
                        "warning"  -> WarningChip()
                        else -> StatusChip(
                            text = "INFO",
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onPrimary,
                            chipShape = ChipShape.Rounded,
                        )
                    }
                    alerta.equipo?.let {
                        Text(it, style = CodeTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Título y descripción
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm), verticalAlignment = Alignment.Top) {
                    Icon(alertIcon, null, tint = borderColor, modifier = Modifier.size(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            alerta.titulo,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            alerta.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        alerta.ubicacion?.let { loc ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                Text(loc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Botones
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                    TechFlowPrimaryButton("ATENDER", leadingIcon = Icons.Filled.Check, onClick = onAtender, modifier = Modifier.weight(1f))
                    TechFlowSecondaryButton("IGNORAR", onClick = onAtender, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
