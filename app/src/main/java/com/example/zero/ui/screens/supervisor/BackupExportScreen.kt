package com.example.zero.ui.screens.supervisor

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.ui.viewmodel.BackupExportViewModel
import com.example.zero.ui.viewmodel.ExportCardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupExportScreen(
    vm: BackupExportViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.loadCounts() }

    // Disparar ShareSheet cuando haya CSV pendiente
    LaunchedEffect(vm.serviceRequestCard.pendingCsv) {
        vm.serviceRequestCard.pendingCsv?.let { csv ->
            shareCSV(context, csv, "solicitudes_servicio")
            vm.clearServiceRequestCsv()
        }
    }
    LaunchedEffect(vm.equipmentCard.pendingCsv) {
        vm.equipmentCard.pendingCsv?.let { csv ->
            shareCSV(context, csv, "inventario_equipos")
            vm.clearEquipmentCsv()
        }
    }
    LaunchedEffect(vm.usersCard.pendingCsv) {
        vm.usersCard.pendingCsv?.let { csv ->
            shareCSV(context, csv, "usuarios_sistema")
            vm.clearUsersCsv()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Exportar", color = textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = textPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardBg),
            )
        },
        containerColor = darkBg,
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B4F7A).copy(alpha = 0.25f)),
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Backup, null, tint = cyan, modifier = Modifier.size(28.dp))
                    Column {
                        Text("Módulo 6.0 — Mantenimiento del Sistema", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Exporta datos del sistema en formato CSV para respaldo o análisis externo.", color = textSecondary, fontSize = 12.sp)
                    }
                }
            }

            Text("EXPORTACIONES DISPONIBLES", color = cyan.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)

            // Tarjeta: Solicitudes
            ExportCard(
                state = vm.serviceRequestCard,
                icon = Icons.Outlined.Build,
                iconTint = cyan,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                onExport = { vm.exportServiceRequests() },
            )

            // Tarjeta: Equipos
            ExportCard(
                state = vm.equipmentCard,
                icon = Icons.Outlined.Memory,
                iconTint = Color(0xFF10B981),
                cardBg = cardBg,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                onExport = { vm.exportEquipment() },
            )

            // Tarjeta: Usuarios
            ExportCard(
                state = vm.usersCard,
                icon = Icons.Outlined.Group,
                iconTint = Color(0xFFF59E0B),
                cardBg = cardBg,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                onExport = { vm.exportUsers() },
            )

            vm.errorMsg?.let {
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(12.dp)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Error, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Text(it, color = Color(0xFFEF4444), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ExportCard(
    state: ExportCardState,
    icon: ImageVector,
    iconTint: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    onExport: () -> Unit,
) {
    val blue = Color(0xFF0B4F7A); val cyan = Color(0xFF00C8F0)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(state.title, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    if (state.isLoadingCount) {
                        Text("Calculando filas...", color = textSecondary.copy(alpha = 0.5f), fontSize = 12.sp)
                    } else {
                        Text("${state.rowCount} registros disponibles", color = textSecondary, fontSize = 12.sp)
                    }
                }
            }
            Text(state.description, color = textSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            state.lastExportTimestamp?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                    Text("Último export: $it", color = Color(0xFF10B981), fontSize = 11.sp)
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            if (state.isExporting) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = iconTint, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Generando CSV...", color = textSecondary, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(blue, iconTint.copy(alpha = 0.8f))), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FileDownload, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("Exportar CSV", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

private fun shareCSV(context: android.content.Context, csv: String, fileName: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_TEXT, csv)
        putExtra(Intent.EXTRA_SUBJECT, "Backup 0 Grados — $fileName.csv")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir $fileName.csv"))
}
