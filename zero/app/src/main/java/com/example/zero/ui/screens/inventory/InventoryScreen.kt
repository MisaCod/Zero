package com.example.zero.ui.screens.inventory

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zero.ui.components.CriticalFailureChip
import com.example.zero.ui.components.OperativeChip
import com.example.zero.ui.components.RepairChip
import com.example.zero.ui.theme.Dimens
import com.example.zero.ui.theme.StatusOperative
import com.example.zero.ui.theme.StatusRepair
import com.example.zero.ui.theme.TechFlowTheme

// ============================================================================
// InventoryScreen — Translated from 3.html and user screenshot
// Equipment listing with search, category filters, status filters, and cards.
// ============================================================================

// region Mock Data
private enum class EquipmentStatus { OPERATIVE, REPAIR, CRITICAL }

private data class Equipment(
    val name: String,
    val location: String,
    val status: EquipmentStatus,
    val maintenanceLabel: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
)

private val mockEquipment = listOf(
    Equipment(
        name = "Compresor Industrial C-40",
        location = "Planta Baja - Sector B",
        status = EquipmentStatus.OPERATIVE,
        maintenanceLabel = "Hace 15 días",
        icon = Icons.Filled.AcUnit,
        gradientColors = listOf(Color(0xFF37474F), Color(0xFF546E7A)),
    ),
    Equipment(
        name = "Chiller Carrier 30XA",
        location = "Azotea - Bloque A",
        status = EquipmentStatus.REPAIR,
        maintenanceLabel = "Hoy (En curso)",
        icon = Icons.Filled.Kitchen,
        gradientColors = listOf(Color(0xFF455A64), Color(0xFF607D8B)),
    ),
    Equipment(
        name = "Motor Trifásico WEG",
        location = "Línea de Montaje 4",
        status = EquipmentStatus.CRITICAL,
        maintenanceLabel = "Vencido (hace 45d)",
        icon = Icons.Filled.ElectricalServices,
        gradientColors = listOf(Color(0xFF4E342E), Color(0xFF6D4C41)),
    ),
    Equipment(
        name = "Tablero Eléctrico T-105",
        location = "Cuarto Técnico G",
        status = EquipmentStatus.OPERATIVE,
        maintenanceLabel = "Hace 10 días",
        icon = Icons.Filled.ElectricalServices,
        gradientColors = listOf(Color(0xFF263238), Color(0xFF37474F)),
    ),
)

private val categories = listOf("Todas", "Frío", "Clima", "Motor", "Eléctrico")
// endregion

@Composable
fun InventoryScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onScannerClick: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableIntStateOf(0) }
    var selectedStatus by remember { mutableStateOf<EquipmentStatus?>(null) }
    
    val equipmentList = remember { mutableStateListOf(*mockEquipment.toTypedArray()) }
    
    val filteredEquipment = equipmentList.filter { eq ->
        val matchesSearch = eq.name.contains(searchQuery, ignoreCase = true) || 
                            eq.location.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == 0 || categories[selectedCategory] in eq.name // Simulating category mapping
        val matchesStatus = selectedStatus == null || eq.status == selectedStatus
        matchesSearch && matchesCategory && matchesStatus
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 100.dp, // Space for FABs
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            // Title
            item {
                Text(
                    text = "Inventario de Equipos",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        horizontal = Dimens.marginMobile,
                        vertical = Dimens.md,
                    ),
                )
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.marginMobile)
                        .height(56.dp),
                    placeholder = {
                        Text(
                            "Buscar equipo, serie o ubicación...",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(Dimens.xl),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                )
            }

            // Category chips
            item {
                Column(
                    modifier = Modifier.padding(horizontal = Dimens.marginMobile),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    Text(
                        text = "Categorías",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                    ) {
                        items(categories.size) { index ->
                            FilterChip(
                                selected = selectedCategory == index,
                                onClick = { selectedCategory = index },
                                label = {
                                    Text(
                                        text = categories[index],
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    enabled = true,
                                    selected = selectedCategory == index,
                                ),
                            )
                        }
                    }
                }
            }

            // Status filter chips
            item {
                Column(
                    modifier = Modifier.padding(horizontal = Dimens.marginMobile),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    Text(
                        text = "Estado de Operación",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                        StatusFilterChip(
                            dotColor = StatusOperative,
                            label = "Operativo",
                            selected = selectedStatus == EquipmentStatus.OPERATIVE,
                            onClick = { 
                                selectedStatus = if (selectedStatus == EquipmentStatus.OPERATIVE) null else EquipmentStatus.OPERATIVE 
                            },
                        )
                        StatusFilterChip(
                            dotColor = StatusRepair,
                            label = "En Reparación",
                            selected = selectedStatus == EquipmentStatus.REPAIR,
                            onClick = { 
                                selectedStatus = if (selectedStatus == EquipmentStatus.REPAIR) null else EquipmentStatus.REPAIR 
                            },
                        )
                        StatusFilterChip(
                            dotColor = MaterialTheme.colorScheme.error,
                            label = "Falla",
                            selected = selectedStatus == EquipmentStatus.CRITICAL,
                            onClick = { 
                                selectedStatus = if (selectedStatus == EquipmentStatus.CRITICAL) null else EquipmentStatus.CRITICAL 
                            },
                        )
                    }
                }
            }

            // Equipment cards
            items(filteredEquipment) { equipment ->
                EquipmentCard(
                    equipment = equipment,
                    modifier = Modifier.padding(horizontal = Dimens.marginMobile),
                )
            }
        }

        // FABs
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = Dimens.md,
                    bottom = contentPadding.calculateBottomPadding() + Dimens.lg,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            ExtendedFloatingActionButton(
                onClick = onScannerClick,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR")
                Spacer(Modifier.width(Dimens.sm))
                Text("Escanear QR", style = MaterialTheme.typography.labelLarge)
            }
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Agregar equipo",
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

// ============================================================================
// Internal Composables
// ============================================================================

@Composable
private fun StatusFilterChip(
    dotColor: Color,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun EquipmentCard(
    equipment: Equipment,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.xl),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column {
            // Image area with gradient background + equipment icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = Brush.linearGradient(equipment.gradientColors),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Large equipment icon as visual placeholder
                Icon(
                    imageVector = equipment.icon,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color.White.copy(alpha = 0.25f),
                )

                // Status chip overlay — top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                ) {
                    when (equipment.status) {
                        EquipmentStatus.OPERATIVE -> OperativeChip()
                        EquipmentStatus.REPAIR -> RepairChip()
                        EquipmentStatus.CRITICAL -> CriticalFailureChip()
                    }
                }
            }

            // Card body
            Column(
                modifier = Modifier.padding(Dimens.md),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = equipment.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = equipment.location,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(Dimens.sm))

                // Maintenance info footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(0.dp),
                        )
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "MANTENIMIENTO",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text = equipment.maintenanceLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (equipment.status != EquipmentStatus.OPERATIVE) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            color = when (equipment.status) {
                                EquipmentStatus.OPERATIVE -> MaterialTheme.colorScheme.onSurface
                                EquipmentStatus.REPAIR -> StatusRepair
                                EquipmentStatus.CRITICAL -> MaterialTheme.colorScheme.error
                            },
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configurar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InventoryScreenPreview() {
    TechFlowTheme(darkTheme = false) {
        InventoryScreen()
    }
}
