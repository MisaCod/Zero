package com.example.zero.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.zero.model.Alert
import com.example.zero.model.AlertSeverity
import com.example.zero.model.WorkOrder
import com.example.zero.ui.theme.Dimens

// ============================================================================
// Modals.kt
// Bottom Sheets for Profile, Scanner, Alert Details, and History Details.
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBottomSheet(
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg)
                .padding(bottom = Dimens.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                tint = MaterialTheme.colorScheme.primary,
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Roberto Gómez",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Técnico Especialista HVAC",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Dimens.sm))
                StatusChip(
                    text = "En turno",
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(Dimens.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(Dimens.sm))

            TechFlowSecondaryButton(
                text = "Configuración",
                leadingIcon = Icons.Outlined.Settings,
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss,
            )
            TechFlowSecondaryButton(
                text = "Cerrar Sesión",
                leadingIcon = Icons.AutoMirrored.Outlined.ExitToApp,
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerBottomSheet(
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Simulating scanning laser line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "laser",
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg)
                .padding(bottom = Dimens.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Escanear Equipo",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Alinee el código QR en el recuadro",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            
            Spacer(modifier = Modifier.height(Dimens.xl))
            
            // Scanner Viewfinder Mock
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(Dimens.lg))
                    .background(Color.Black)
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Dimens.lg)),
                contentAlignment = Alignment.Center,
            ) {
                // Laser line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .graphicsLayer { translationY = laserOffset }
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.Red, Color.Transparent)
                            )
                        )
                )
                // Viewfinder corners overlay could be added here
            }

            Spacer(modifier = Modifier.height(Dimens.xl))

            TechFlowSecondaryButton(
                text = "Ingresar código manual",
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailBottomSheet(
    alert: Alert,
    onDismiss: () -> Unit,
    onAttend: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tintColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.error
        AlertSeverity.WARNING -> MaterialTheme.colorScheme.tertiary
        AlertSeverity.INFO -> MaterialTheme.colorScheme.primary
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg)
                .padding(bottom = Dimens.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(tintColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = alert.icon, contentDescription = null, tint = tintColor)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "ID: ${alert.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Details
            Text(
                text = "Detalles del Incidente",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Se ha detectado una anomalía que coincide con la alerta ${alert.id}. El sistema reporta: ${alert.subtitle}. Por favor, inspeccione la zona y siga el protocolo de seguridad establecido.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Recommended Action
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.md),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(
                            text = "Acción Recomendada",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Aislar componente eléctrico y verificar voltaje.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.md))
            
            TechFlowPrimaryButton(
                text = "Marcar como Atendida",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onAttend()
                    onDismiss()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailBottomSheet(
    workOrder: WorkOrder,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg)
                .padding(bottom = Dimens.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Text(
                text = "Detalle de Orden",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = workOrder.code,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                StatusChip(
                    text = "Completada",
                    backgroundColor = Color(0xFF16A34A).copy(alpha = 0.1f),
                    textColor = Color(0xFF16A34A),
                )
            }
            
            Text(
                text = workOrder.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Stats grid mock
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ubicación", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(workOrder.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fecha / Hora", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(workOrder.time, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.md),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                ) {
                    Text("Notas del Técnico", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "Se reemplazaron filtros primarios y se ajustó tensión en correas de transmisión. Pruebas de encendido OK.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.md))
            
            TechFlowSecondaryButton(
                text = "Cerrar",
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss,
            )
        }
    }
}
