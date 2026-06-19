package com.example.zero.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zero.ui.components.CriticalAlertChip
import com.example.zero.ui.components.StatusChip
import com.example.zero.ui.components.ChipShape
import com.example.zero.ui.components.TechFlowPrimaryButton
import com.example.zero.ui.components.TechFlowSecondaryButton
import com.example.zero.ui.components.WarningChip
import com.example.zero.ui.theme.CodeTextStyle
import com.example.zero.ui.components.AlertDetailBottomSheet
import com.example.zero.model.Alert
import com.example.zero.model.AlertSeverity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.zero.ui.theme.Dimens
import com.example.zero.ui.theme.TechFlowTheme

// ============================================================================
// AlertsScreen — Translated from 5.html
// ============================================================================

private val mockAlerts = listOf(
    Alert(
        id = "TK-882",
        severity = AlertSeverity.CRITICAL,
        title = "Sobrecalentamiento en Compresor C-40",
        subtitle = "Temperatura excede 95°C. Riesgo de daño permanente al equipo.",
        icon = Icons.Filled.Thermostat,
    ),
    Alert(
        id = "MT-045",
        severity = AlertSeverity.WARNING,
        title = "Vibración anómala en Motor WEG",
        subtitle = "Nivel de vibración fuera de rango normal. Requiere inspección.",
        icon = Icons.Filled.Warning,
    ),
    Alert(
        id = "EL-103",
        severity = AlertSeverity.WARNING,
        title = "Voltaje inestable en Tablero T-105",
        subtitle = "Fluctuaciones de voltaje detectadas en circuito principal.",
        icon = Icons.Filled.Bolt,
    ),
    Alert(
        id = "RF-221",
        severity = AlertSeverity.CRITICAL,
        title = "Fuga de refrigerante en Chiller 30XA",
        subtitle = "Pérdida de presión detectada. Se requiere intervención inmediata.",
        icon = Icons.Filled.ErrorOutline,
    ),
)

@Composable
fun AlertsScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var selectedAlert by remember { mutableStateOf<Alert?>(null) }
    
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
        item {
            Text(
                text = "Alertas del Sistema",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Dimens.xs))
            Text(
                text = "${mockAlerts.count { it.severity == AlertSeverity.CRITICAL }} alertas críticas requieren atención",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
            )
        }

        items(mockAlerts) { alert ->
            AlertCard(
                alert = alert,
                onDetailsClick = { selectedAlert = alert }
            )
        }
    }
    
    selectedAlert?.let { alert ->
        AlertDetailBottomSheet(
            alert = alert,
            onDismiss = { selectedAlert = null },
            onAttend = { selectedAlert = null }
        )
    }
}

@Composable
private fun AlertCard(
    alert: Alert,
    onDetailsClick: () -> Unit = {}
) {
    val borderColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.error
        AlertSeverity.WARNING -> MaterialTheme.colorScheme.tertiary
        AlertSeverity.INFO -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = Dimens.xl,
            bottomEnd = Dimens.xl,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row {
            // Left accent border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(200.dp)
                    .background(borderColor),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                // Header: severity chip + ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        when (alert.severity) {
                            AlertSeverity.CRITICAL -> CriticalAlertChip()
                            AlertSeverity.WARNING -> WarningChip()
                            AlertSeverity.INFO -> StatusChip(
                                text = "INFO",
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                chipShape = ChipShape.Rounded,
                            )
                        }
                        Text(
                            text = "ID: ${alert.id}",
                            style = CodeTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "Hace 15 min", // Mocking timeAgo
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Title + description
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = alert.icon,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(24.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = alert.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    TechFlowPrimaryButton(
                        text = "ATENDER",
                        leadingIcon = Icons.Filled.Bolt,
                        onClick = {},
                        modifier = Modifier.weight(1f),
                    )
                    TechFlowSecondaryButton(
                        text = "VER DETALLES",
                        onClick = onDetailsClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AlertsScreenPreview() {
    TechFlowTheme(darkTheme = false) {
        AlertsScreen()
    }
}
