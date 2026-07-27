package com.example.zero.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zero.model.Alert
import com.example.zero.model.AlertSeverity
import com.example.zero.model.Priority
import com.example.zero.model.WorkOrder
import com.example.zero.data.model.ServiceRequest
import com.example.zero.ui.screens.detail.ServiceRequestDetailScreen
import com.example.zero.ui.components.ActiveChip
import com.example.zero.ui.components.HighPriorityChip
import com.example.zero.ui.components.MediumPriorityChip
import com.example.zero.ui.components.StatusChip
import com.example.zero.ui.components.ChipShape
import com.example.zero.ui.components.TechFlowPrimaryButton
import com.example.zero.ui.components.UrgentChip
import com.example.zero.ui.components.scaleOnPress
import com.example.zero.ui.theme.CodeTextStyle
import com.example.zero.ui.theme.Dimens
import com.example.zero.ui.theme.TechFlowTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.ui.viewmodel.AuthViewModel
import com.example.zero.ui.viewmodel.ServiceRequestViewModel
import com.example.zero.data.model.UserRole
import com.example.zero.ui.components.AgendarMantenimientoSheet
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold

// ============================================================================
// DashboardScreen — Phase 4: Upgraded with state, animations, and interactions
//
// Key upgrades:
//   1. State management via mutableStateListOf for live list mutation
//   2. AnimatedVisibility with spring transitions for item removal
//   3. animateContentSize on stat cards for dynamic counter updates
//   4. scaleOnPress on interactive elements (scanner banner, cards)
//   5. MutableTransitionState pattern for clean animated dismissals
// ============================================================================

// region Mock Data Initialization
private val initialWorkOrders = listOf(
    WorkOrder(
        id = "wo-1",
        code = "#WO-8821",
        title = "Mantenimiento Preventivo Aire Acondicionado",
        location = "Sector A",
        time = "09:00 AM",
        priority = Priority.HIGH,
        icon = Icons.Filled.AcUnit,
        imageUrl = "https://images.unsplash.com/photo-1504328345606-18bbc8c9d7d1?w=400&fit=crop",
    ),
    WorkOrder(
        id = "wo-2",
        code = "#WO-8845",
        title = "Reparación de Nevera Industrial",
        location = "Comedor",
        time = "11:30 AM",
        priority = Priority.MEDIUM,
        icon = Icons.Filled.Kitchen,
        imageUrl = "https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=400&fit=crop",
    ),
    WorkOrder(
        id = "wo-3",
        code = "#WO-8892",
        title = "Revisión de Motor Eléctrico Trifásico",
        location = "Línea de Montaje 4",
        time = "03:00 PM",
        priority = Priority.HIGH,
        icon = Icons.Filled.ElectricalServices,
        imageUrl = "https://images.unsplash.com/photo-1581092160562-40aa08e78837?w=400&fit=crop",
    ),
)

private val initialAlerts = listOf(
    Alert(
        id = "alert-1",
        title = "Baja presión en Compresor 4",
        subtitle = "Hace 15 min • Planta Sótano",
        severity = AlertSeverity.CRITICAL,
        icon = Icons.Filled.Error,
    ),
    Alert(
        id = "alert-2",
        title = "Falla de sensor en Sala de Máquinas",
        subtitle = "Hace 1 hora • Torre B",
        severity = AlertSeverity.WARNING,
        icon = Icons.Filled.SensorsOff,
    ),
    Alert(
        id = "alert-3",
        title = "Temperatura elevada en zona de carga",
        subtitle = "Hace 2 horas • Muelle C",
        severity = AlertSeverity.WARNING,
        icon = Icons.Filled.Warning,
    ),
)
// endregion

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onScannerClick: () -> Unit = {},
    onSolicitudClick: (com.example.zero.data.model.ServiceRequestWithEquipment) -> Unit = {},
    onFillReport: (String) -> Unit = {},
    onRateService: (String) -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    serviceRequestViewModel: ServiceRequestViewModel = viewModel(),
    isMisTrabajos: Boolean = false,
) {
    // ── State management ─────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        serviceRequestViewModel.cargarSolicitudes()
    }

    val liveSolicitudes = serviceRequestViewModel.solicitudes

    // Mapear ServiceRequestWithEquipment a WorkOrder para la UI existente
    val workOrders = remember(liveSolicitudes.size, isMisTrabajos, authViewModel.userRole) { 
        val filtered = if (isMisTrabajos) {
            liveSolicitudes.filter { it.status == "EN PROGRESO" || it.status == "TERMINADO" }
        } else {
            if (authViewModel.userRole == UserRole.CLIENTE) {
                liveSolicitudes
            } else {
                liveSolicitudes.filter { it.status == "SIN INICIAR" }
            }
        }
        mutableStateListOf(*filtered.map { req ->
            WorkOrder(
                id = req.id ?: "",
                code = "#SR-${req.id?.take(4)?.uppercase() ?: "NEW"}",
                title = req.title,
                location = req.location,
                time = req.createdAt?.take(10) ?: "Reciente",
                priority = Priority.MEDIUM,
                icon = if (req.status == "TERMINADO") Icons.Filled.CheckCircle else Icons.Filled.AcUnit,
                imageUrl = ""
            )
        }.toTypedArray()) 
    }
    
    var selectedTab by remember { mutableStateOf(0) } // 0: En Progreso, 1: Finalizadas
    
    val alerts = remember { mutableStateListOf(*initialAlerts.toTypedArray()) }
    
    var showAgendaSheet by remember { mutableStateOf(false) }

    // Dynamic statistics — automatically update when items are removed
    val ordersToday = workOrders.size
    val criticalCount = alerts.count { it.severity == AlertSeverity.CRITICAL }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = Dimens.marginMobile)
                .padding(top = Dimens.lg, bottom = Dimens.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.xl),
        ) {
            // ── Greeting ─────────────────────────────────────────────────────
            GreetingSection(authViewModel.profile?.fullName ?: "Usuario")

            // ── Bento Stat Cards ─────────────────────────────────────────────
            BentoStatsRow(
                ordersToday = ordersToday,
                criticalCount = criticalCount,
            )

            // ── Scanner Banner (Solo para Técnicos/Supervisores) ──────────────
            if (authViewModel.userRole != UserRole.CLIENTE && !isMisTrabajos) {
                ScannerBanner(onClick = onScannerClick)
            }

            // ── Work Orders (animated list) ──────────────────────────────────
            if (isMisTrabajos) {
                androidx.compose.material3.TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    androidx.compose.material3.Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("En Progreso") }
                    )
                    androidx.compose.material3.Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Finalizadas") }
                    )
                }
                
                val displayedOrders = workOrders.filter { order ->
                    val req = liveSolicitudes.find { it.id == order.id }
                    if (selectedTab == 0) req?.status == "EN PROGRESO" else req?.status == "TERMINADO"
                }.toMutableList()

                WorkOrdersSection(
                    workOrders = displayedOrders,
                    onOrderClick = { orderId ->
                        val req = liveSolicitudes.find { it.id == orderId }
                        req?.let { onSolicitudClick(it) }
                    },
                    title = if (selectedTab == 0) "Solicitudes en Progreso" else "Solicitudes Finalizadas"
                )
            } else {
                WorkOrdersSection(
                    workOrders = workOrders,
                    onOrderClick = { orderId ->
                        val req = liveSolicitudes.find { it.id == orderId }
                        req?.let { onSolicitudClick(it) }
                    },
                    title = "Órdenes de Trabajo Próximas"
                )
            }

            // ── Alerts (animated list) ───────────────────────────────────────
            if (authViewModel.userRole != UserRole.CLIENTE && !isMisTrabajos) {
                AlertsSection(alerts = alerts)
            }
        }

        // ── FAB para Clientes (Agendar Mantenimiento) ─────────────────────
        if (authViewModel.userRole == UserRole.CLIENTE) {
            ExtendedFloatingActionButton(
                onClick = { showAgendaSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Dimens.marginMobile, bottom = contentPadding.calculateBottomPadding() + Dimens.md)
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agendar")
            }
        }
    }

    if (showAgendaSheet) {
        AgendarMantenimientoSheet(
            isLoading = serviceRequestViewModel.isLoading,
            errorMsg = serviceRequestViewModel.errorMsg,
            onDismiss = { 
                showAgendaSheet = false
                serviceRequestViewModel.clearError()
            },
            onSubmit = { titulo, descripcion, ubicacion, equipo, prioridad, fecha ->
                serviceRequestViewModel.crearSolicitud(
                    titulo = titulo,
                    descripcion = descripcion,
                    ubicacion = ubicacion,
                    equipo = equipo,
                    prioridad = prioridad,
                    fechaProgramada = fecha,
                    clientId = authViewModel.getCurrentUserId() ?: "",
                    onSuccess = {
                        showAgendaSheet = false
                        serviceRequestViewModel.resetOperacionExitosa()
                    }
                )
            }
        )
    }

}

// ============================================================================
// Section Composables
// ============================================================================

@Composable
private fun GreetingSection(name: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "¡Hola, $name!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Jueves, 21 de mayo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BentoStatsRow(
    ordersToday: Int,
    criticalCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        // Active orders card
        BentoStatCard(
            modifier = Modifier
                .weight(1f)
                .height(128.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, null) },
            chip = { ActiveChip() },
            value = "$ordersToday",
            label = "Órdenes de hoy",
        )

        // Critical equipment card
        BentoStatCard(
            modifier = Modifier
                .weight(1f)
                .height(128.dp),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = { Icon(Icons.Filled.Warning, null) },
            chip = {
                if (criticalCount > 0) UrgentChip()
                else StatusChip(
                    text = "OK",
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    chipShape = ChipShape.Pill,
                )
            },
            value = "$criticalCount",
            label = if (criticalCount > 0) "Equipos críticos" else "Sin alertas",
        )
    }
}

@Composable
private fun ScannerBanner(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scaleOnPress(interactionSource, targetScale = 0.97f)
            .clip(RoundedCornerShape(Dimens.xl))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(Dimens.xl),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(Dimens.xl),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Escanear Equipo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                )
                Text(
                    text = "Identificación rápida vía QR o Barcode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f),
                )
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(Dimens.xl),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Escanear",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun WorkOrdersSection(
    workOrders: MutableList<WorkOrder>,
    onOrderClick: (String) -> Unit = {},
    title: String = "Órdenes de Trabajo Próximas",
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { },
            ) {
                Text(
                    text = "Ver todas",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Animated work order items
        workOrders.forEach { order ->
            key(order.id) {
                val dismissState = remember { MutableTransitionState(true) }

                AnimatedVisibility(
                    visibleState = dismissState,
                    enter = expandVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ) + fadeIn(),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300),
                    ) + shrinkVertically(
                        animationSpec = tween(200, delayMillis = 100),
                    ) + fadeOut(animationSpec = tween(200)),
                ) {
                    WorkOrderCard(
                        workOrder = order,
                        onAction = { onOrderClick(order.id) },
                        onDismiss = {
                            // Trigger the exit animation
                            dismissState.targetState = false
                        },
                    )
                }

                // Remove from state list after exit animation completes
                if (dismissState.isIdle && !dismissState.currentState) {
                    LaunchedEffect(Unit) {
                        workOrders.remove(order)
                    }
                }
            }
        }

        // Empty state — appears when all orders are completed
        AnimatedVisibility(
            visible = workOrders.isEmpty(),
            enter = expandVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) + fadeIn(animationSpec = tween(400)),
        ) {
            Card(
                shape = RoundedCornerShape(Dimens.xl),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.lg),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(Dimens.sm))
                    Text(
                        text = "¡Todas las órdenes completadas! 🎉",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsSection(
    alerts: MutableList<Alert>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        Text(
            text = "Alertas Recientes",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Animated alert items
        alerts.forEach { alert ->
            key(alert.id) {
                val dismissState = remember { MutableTransitionState(true) }

                AnimatedVisibility(
                    visibleState = dismissState,
                    enter = expandVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ) + fadeIn(),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it }, // Slide right on dismiss
                        animationSpec = tween(300),
                    ) + shrinkVertically(
                        animationSpec = tween(200, delayMillis = 100),
                    ) + fadeOut(animationSpec = tween(200)),
                ) {
                    AlertItem(
                        alert = alert,
                        onAttend = {
                            // Trigger the exit animation
                            dismissState.targetState = false
                        },
                    )
                }

                // Clean up after animation
                if (dismissState.isIdle && !dismissState.currentState) {
                    LaunchedEffect(Unit) {
                        alerts.remove(alert)
                    }
                }
            }
        }

        // Empty state
        AnimatedVisibility(
            visible = alerts.isEmpty(),
            enter = expandVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) + fadeIn(animationSpec = tween(400)),
        ) {
            Card(
                shape = RoundedCornerShape(Dimens.xl),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.lg),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(Dimens.sm))
                    Text(
                        text = "Sin alertas pendientes ✅",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ============================================================================
// Card Composables
// ============================================================================

@Composable
private fun BentoStatCard(
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    icon: @Composable () -> Unit,
    chip: @Composable () -> Unit,
    value: String,
    label: String,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .scaleOnPress(interactionSource, targetScale = 0.97f)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
        onClick = {},
        interactionSource = interactionSource,
        shape = RoundedCornerShape(Dimens.xl),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.md),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                icon()
                chip()
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun WorkOrderCard(
    workOrder: WorkOrder,
    onAction: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }

    val priorityChip: @Composable () -> Unit = when (workOrder.priority) {
        Priority.HIGH -> { { HighPriorityChip() } }
        Priority.MEDIUM -> { { MediumPriorityChip() } }
        Priority.LOW -> { { MediumPriorityChip() } } // reuse for now
    }

    val trailingIcon = when (workOrder.priority) {
        Priority.HIGH -> Icons.Filled.PlayCircle
        else -> Icons.Outlined.MoreVert
    }

    val trailingTint = when (workOrder.priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onAction,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .scaleOnPress(interactionSource)
            .animateContentSize(),
        shape = RoundedCornerShape(Dimens.xl),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = workOrder.icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    priorityChip()
                    Text(
                        text = workOrder.code,
                        style = CodeTextStyle.copy(
                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = workOrder.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${workOrder.location} • ${workOrder.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Action button
            IconButton(
                onClick = onAction,
                modifier = Modifier.size(Dimens.touchTarget),
            ) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = "Completar orden",
                    tint = trailingTint,
                )
            }
        }
    }
}

@Composable
private fun AlertItem(
    alert: Alert,
    onAttend: () -> Unit,
) {
    val borderColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.error
        AlertSeverity.WARNING -> MaterialTheme.colorScheme.tertiary
        AlertSeverity.INFO -> MaterialTheme.colorScheme.primary
    }

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        onClick = onAttend,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .scaleOnPress(interactionSource),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = Dimens.xl,
            bottomEnd = Dimens.xl,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row {
            // Left accent border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(borderColor),
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(Dimens.md),
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = alert.icon,
                    contentDescription = null,
                    tint = borderColor,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
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

                // Attend action
                TechFlowPrimaryButton(
                    text = "Atender",
                    onClick = onAttend,
                )
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DashboardScreenPreview() {
    TechFlowTheme(darkTheme = false) {
        DashboardScreen()
    }
}
