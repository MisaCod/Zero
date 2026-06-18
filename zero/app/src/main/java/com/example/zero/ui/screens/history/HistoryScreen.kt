package com.example.zero.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zero.ui.components.ArchivedChip
import com.example.zero.ui.components.CompletedChip
import com.example.zero.ui.components.PreventiveChip
import com.example.zero.ui.components.TechFlowPrimaryButton
import com.example.zero.ui.components.TechFlowSecondaryButton
import com.example.zero.ui.theme.CodeTextStyle
import com.example.zero.ui.theme.Dimens
import com.example.zero.model.WorkOrder
import com.example.zero.model.Priority
import com.example.zero.ui.components.HistoryDetailBottomSheet
import com.example.zero.ui.theme.TechFlowTheme
import androidx.compose.material.icons.filled.AcUnit

// ============================================================================
// HistoryScreen — Translated from 4.html
// ============================================================================

private val periodFilters = listOf("Hoy", "Esta Semana", "Este Mes")
private val statusFilters = listOf("Completado" to Color(0xFF16A34A), "Archivado" to Color(0xFF94A3B8))

private val mockHistory = listOf(
    WorkOrder(
        id = "wo-92834",
        code = "#WO-92834",
        title = "Compresor Industrial C-40",
        location = "Planta Baja - Sector B",
        time = "14 Oct, 2023 • 09:30 AM",
        priority = Priority.HIGH,
        icon = Icons.Filled.AcUnit
    ),
    WorkOrder(
        id = "wo-92711",
        code = "#WO-92711",
        title = "Bomba Hidráulica H-12",
        location = "Sector de Bombas",
        time = "10 Oct, 2023 • 14:00 PM",
        priority = Priority.MEDIUM,
        icon = Icons.Filled.AcUnit
    )
)

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableIntStateOf(0) }
    var selectedStatusIndex by remember { mutableStateOf<Int?>(null) }
    
    var selectedWorkOrder by remember { mutableStateOf<WorkOrder?>(null) }

    val filteredHistory = mockHistory.filter { order ->
        val matchesSearch = order.title.contains(searchQuery, ignoreCase = true) ||
                            order.code.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatusIndex == null || 
            (selectedStatusIndex == 0 && order.priority == Priority.HIGH) || // Simplistic mapping for mock
            (selectedStatusIndex == 1 && order.priority != Priority.HIGH)
        matchesSearch && matchesStatus
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + Dimens.md,
            bottom = contentPadding.calculateBottomPadding() + Dimens.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        // Search
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.marginMobile)
                    .height(52.dp),
                placeholder = {
                    Text(
                        "Buscar activos o IDs de servicio (ej. C-40)...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(Dimens.xl),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        }

        // Period filters
        item {
            Column(
                modifier = Modifier.padding(horizontal = Dimens.marginMobile),
                verticalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    Text(
                        text = "PERIODO:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                        items(periodFilters.size) { index ->
                            FilterChip(
                                selected = selectedPeriod == index,
                                onClick = { selectedPeriod = index },
                                label = { Text(periodFilters[index]) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    enabled = true,
                                    selected = selectedPeriod == index,
                                ),
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    Text(
                        text = "ESTADO:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    statusFilters.forEachIndexed { index, (label, dotColor) ->
                        FilterChip(
                            selected = selectedStatusIndex == index,
                            onClick = { 
                                selectedStatusIndex = if (selectedStatusIndex == index) null else index 
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                                ) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    Text(label)
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                enabled = true,
                                selected = selectedStatusIndex == index,
                            ),
                        )
                    }
                }
            }
        }

        // Section header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.marginMobile),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recientes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Ver todo",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        Icons.Outlined.ChevronRight,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // Filtered History list
        items(filteredHistory.size) { index ->
            val workOrder = filteredHistory[index]
            val isCompleted = index % 2 == 0 // Mock logic for status
            
            HistoryCard(
                workOrderId = workOrder.code,
                statusChip = { if (isCompleted) CompletedChip() else ArchivedChip() },
                equipmentName = workOrder.title,
                date = workOrder.time,
                technicianName = if (isCompleted) "Carlos Méndez" else "Ana García",
                typeChip = { PreventiveChip() },
                isPrimary = isCompleted,
                modifier = Modifier.padding(horizontal = Dimens.marginMobile),
                onDetailsClick = { selectedWorkOrder = workOrder }
            )
        }
    }
    
    selectedWorkOrder?.let { workOrder ->
        HistoryDetailBottomSheet(
            workOrder = workOrder,
            onDismiss = { selectedWorkOrder = null }
        )
    }
}

@Composable
private fun HistoryCard(
    workOrderId: String,
    statusChip: @Composable () -> Unit,
    equipmentName: String,
    date: String,
    technicianName: String,
    typeChip: @Composable () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    onDetailsClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.xl),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = Dimens.md, vertical = Dimens.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "ID: $workOrderId",
                style = CodeTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            statusChip()
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Body
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            Text(
                text = equipmentName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                Icon(
                    Icons.Filled.CalendarToday,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = technicianName.first().toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = technicianName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                typeChip()
            }

            Spacer(Modifier.height(Dimens.xs))

            if (isPrimary) {
                TechFlowPrimaryButton(
                    text = "Detalles",
                    trailingIcon = Icons.Outlined.Visibility,
                    onClick = onDetailsClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                TechFlowSecondaryButton(
                    text = "Detalles",
                    trailingIcon = Icons.Outlined.Visibility,
                    onClick = onDetailsClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HistoryScreenPreview() {
    TechFlowTheme(darkTheme = false) {
        HistoryScreen()
    }
}
