package com.example.zero.ui.screens.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.model.ServiceRequestWithEquipment
import com.example.zero.data.model.UserRole
import com.example.zero.ui.theme.Dimens
import com.example.zero.ui.theme.StatusCompletedBg
import com.example.zero.ui.theme.StatusCompletedText
import com.example.zero.ui.viewmodel.AuthViewModel
import com.example.zero.ui.viewmodel.ServiceRequestViewModel

// ============================================================================
// ServiceRequestDetailScreen — Vista completa de una solicitud de mantenimiento.
//
// Funcionalidades:
//   • Técnicos/Supervisores: Cambiar estado (SIN INICIAR → EN PROGRESO → TERMINADO)
//   • Todos: Ver todos los detalles de la solicitud
//   • Indicador visual de progreso del estado
//   • Información de equipo, ubicación, prioridad, fecha
// ============================================================================

// Estados posibles de una solicitud
private val ESTADOS = listOf("SIN INICIAR", "EN PROGRESO", "TERMINADO")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRequestDetailScreen(
    solicitud: ServiceRequestWithEquipment,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    serviceRequestViewModel: ServiceRequestViewModel = viewModel(),
    onAssignTechnician: (String) -> Unit = {},
    onFillReport: (String) -> Unit = {},
    onRateService: (String) -> Unit = {},
) {
    var estadoActual by remember { mutableStateOf(solicitud.status ?: "SIN INICIAR") }
    var isUpdating by remember { mutableStateOf(false) }
    val canEdit = authViewModel.userRole == UserRole.TECNICO ||
                  authViewModel.userRole == UserRole.SUPERVISOR

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle de Solicitud",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "#SR-${solicitud.id?.take(8)?.uppercase() ?: "---"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = Dimens.marginMobile)
                .padding(bottom = Dimens.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg),
        ) {
            Spacer(Modifier.height(Dimens.sm))

            // ── Indicador de Progreso de Estado ──────────────────────────────
            EstadoProgressCard(
                estadoActual = estadoActual,
                canEdit = canEdit,
                isUpdating = isUpdating,
                onCambiarEstado = { nuevoEstado ->
                    val id = solicitud.id ?: return@EstadoProgressCard
                    isUpdating = true
                    serviceRequestViewModel.actualizarEstado(id, nuevoEstado)
                    estadoActual = nuevoEstado
                    isUpdating = false
                },
            )

            // ── Información Principal ─────────────────────────────────────────
            InfoCard(
                icon = Icons.Filled.Build,
                titulo = "Solicitud",
                contenido = {
                    InfoRow(
                        label = "Descripcion del Fallo",
                        value = solicitud.failureDesc,
                    )
                    InfoRow(
                        label = "Estado",
                        value = estadoActual,
                    )
                },
            )

            // ── Ubicación y Equipo ────────────────────────────────────────────
            InfoCard(
                icon = Icons.Outlined.LocationOn,
                titulo = "Ubicación y Equipo",
                contenido = {
                    InfoRow(label = "Ubicación", value = solicitud.location.ifBlank { "Sin ubicación registrada" })
                    solicitud.serialNumber.takeIf { it.isNotBlank() }?.let {
                        InfoRow(label = "Serial del Equipo", value = it)
                    }
                    solicitud.equipmentId?.let {
                        InfoRow(label = "ID Equipo", value = "#${it.take(8).uppercase()}")
                    }
                },
            )

            // ── Fechas ────────────────────────────────────────────────────────
            InfoCard(
                icon = Icons.Filled.CalendarMonth,
                titulo = "Fechas",
                contenido = {
                    solicitud.createdAt?.let {
                        InfoRow(label = "Creada el", value = it.take(10))
                    }
                },
            )

            // ── Botones de acción según rol ──────────────────────────────────
            val userRole = authViewModel.userRole
            val solId = solicitud.id ?: ""

            // SUPERVISOR: Asignar técnico (si aún sin iniciar)
            if (userRole == UserRole.SUPERVISOR && estadoActual.uppercase() == "SIN INICIAR") {
                Button(
                    onClick = { onAssignTechnician(solId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4F7A)),
                ) {
                    Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Dimens.sm))
                    Text("Asignar Técnico", fontWeight = FontWeight.Bold)
                }
            }

            // TÉCNICO: Llenar reporte (si en progreso)
            if (userRole == UserRole.TECNICO && estadoActual.uppercase() == "EN PROGRESO") {
                // Necesitamos el assigment_id — usamos el requestId como fallback
                Button(
                    onClick = { onFillReport(solId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                ) {
                    Icon(Icons.Filled.Assignment, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Dimens.sm))
                    Text("Llenar Reporte Técnico", fontWeight = FontWeight.Bold)
                }
            }

            // CLIENTE: Calificar (si terminado)
            if (userRole == UserRole.CLIENTE && estadoActual.uppercase() == "TERMINADO") {
                Button(
                    onClick = { onRateService(solId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                ) {
                    Icon(Icons.Filled.Star, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Dimens.sm))
                    Text("Calificar Servicio", fontWeight = FontWeight.Bold)
                }
            }

            // ── Estado "Terminado" visual ─────────────────────────────────────
            if (estadoActual.uppercase() == "TERMINADO") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = StatusCompletedBg,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.md),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = StatusCompletedText,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(Dimens.sm))
                        Text(
                            text = "Solicitud completada exitosamente ✅",
                            color = StatusCompletedText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// Componentes internos
// ============================================================================

@Composable
private fun EstadoProgressCard(
    estadoActual: String,
    canEdit: Boolean,
    isUpdating: Boolean,
    onCambiarEstado: (String) -> Unit,
) {
    val indexActual = ESTADOS.indexOfFirst { it.equals(estadoActual, ignoreCase = true) }
        .coerceAtLeast(0)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Text(
                text = "Estado de la Orden",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                letterSpacing = 1.sp,
            )

            // Indicador de pasos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ESTADOS.forEachIndexed { index, estado ->
                    val isCompleted = index <= indexActual
                    val isCurrent = index == indexActual

                    val bgColor by animateColorAsState(
                        targetValue = if (isCompleted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        animationSpec = tween(400),
                        label = "stepColor",
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 40.dp else 32.dp)
                                .clip(CircleShape)
                                .background(bgColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            AnimatedContent(targetState = isCompleted, label = "icon") { done ->
                                if (done) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = when (estado) {
                                "SIN INICIAR" -> "Sin iniciar"
                                "EN PROGRESO" -> "En progreso"
                                "TERMINADO" -> "Terminado"
                                else -> estado
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        )
                    }

                    // Línea conectora entre pasos
                    if (index < ESTADOS.size - 1) {
                        val lineColor by animateColorAsState(
                            targetValue = if (index < indexActual)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                            animationSpec = tween(400),
                            label = "lineColor",
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(bottom = 20.dp),
                            color = lineColor,
                            thickness = 2.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            // Header de la tarjeta
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Contenido dinámico
            contenido()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f),
        )
    }
}
