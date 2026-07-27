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
import com.example.zero.data.model.ServiceRequestWithEquipment
import com.example.zero.ui.components.CriticalAlertChip
import com.example.zero.ui.components.TechFlowPrimaryButton
import com.example.zero.ui.components.TechFlowSecondaryButton
import com.example.zero.ui.theme.CodeTextStyle
import com.example.zero.ui.theme.Dimens
import com.example.zero.ui.viewmodel.ServiceRequestViewModel

@Composable
fun AlertsScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    serviceRequestViewModel: ServiceRequestViewModel = viewModel(),
) {
    LaunchedEffect(Unit) {
        serviceRequestViewModel.cargarSolicitudes()
    }

    val liveSolicitudes = serviceRequestViewModel.solicitudes
    val isLoading = serviceRequestViewModel.isLoading
    
    val alertasAlta = liveSolicitudes.filter { it.failureDesc.contains("Prioridad: ALTA", ignoreCase = true) && it.status?.uppercase() != "CANCELADO" }
    val criticalCount = alertasAlta.size

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
                        text = "🔴 $criticalCount solicitud${if (criticalCount > 1) "es" else ""} con prioridad ALTA requiere${if (criticalCount == 1) "" else "n"} atención",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                    else -> Text(
                        text = "✅ Sin alertas críticas. Todos los equipos operan normalmente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // ── Lista de alertas con animación ────────────────────────────────
        items(alertasAlta, key = { it.id ?: it.title }) { alerta ->
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
                        // TODO: Implementar lógica de navegación si es necesario
                    },
                )
            }
        }

        // ── Estado vacío ──────────────────────────────────────────────────
        if (!isLoading && alertasAlta.isEmpty()) {
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
                            "No hay solicitudes con prioridad ALTA.",
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
// AlertCard — Tarjeta de alerta con datos de ServiceRequest
// ============================================================================

@Composable
private fun AlertCard(
    alerta: ServiceRequestWithEquipment,
    onAtender: () -> Unit = {},
) {
    val borderColor = MaterialTheme.colorScheme.error
    val alertIcon: ImageVector = Icons.Filled.Error

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
                    CriticalAlertChip()
                    Text("SR-${alerta.id?.take(4)?.uppercase() ?: "NEW"}", style = CodeTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Título y descripción
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm), verticalAlignment = Alignment.Top) {
                    Icon(alertIcon, null, tint = borderColor, modifier = Modifier.size(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            alerta.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            alerta.failureDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Text(alerta.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
