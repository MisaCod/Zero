package com.example.zero.ui.screens.reports

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.repository.MaintenanceReportRow
import com.example.zero.data.repository.TechnicianPerformanceRow
import com.example.zero.data.repository.PartsInventoryRow
import com.example.zero.ui.viewmodel.AuthViewModel
import com.example.zero.ui.viewmodel.ReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    authViewModel: AuthViewModel = viewModel(),
    vm: ReportsViewModel = viewModel(),
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mantenimiento", "Rendimiento", "Inventario")

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> vm.loadMaintenanceReport()
            1 -> vm.loadPerformanceReport()
            2 -> vm.loadInventoryReport()
        }
    }

    // Compartir CSV cuando esté listo
    LaunchedEffect(vm.exportedCsv) {
        vm.exportedCsv?.let { csv ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_TEXT, csv)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte 0 Grados — ${tabs[selectedTab]}")
            }
            context.startActivity(Intent.createChooser(intent, "Compartir reporte CSV"))
            vm.clearExportedCsv()
        }
    }

    // Filtros de estado para mantenimiento
    val statusFilters = listOf("TODOS", "SIN INICIAR", "EN PROGRESO", "TERMINADO")
    val statusColors = mapOf("SIN INICIAR" to Color(0xFF6B7280), "EN PROGRESO" to Color(0xFFF59E0B), "TERMINADO" to Color(0xFF10B981))

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(darkBg),
        contentPadding = PaddingValues(bottom = 20.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BarChart, null, tint = cyan, modifier = Modifier.size(22.dp))
                    Text("Reportes", color = textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Text("Módulo 5.0 — Reportes del Sistema", color = textSecondary, fontSize = 12.sp)
            }
        }

        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = cardBg,
                contentColor = cyan,
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title, fontSize = 12.sp, color = if (selectedTab == i) cyan else textSecondary) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }


        // Acciones top (filtros + exportar)
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                when (selectedTab) {
                    0 -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        statusFilters.forEach { f ->
                            FilterChip(selected = vm.maintenanceStatusFilter == f, onClick = { vm.updateMaintenanceStatusFilter(f) },
                                label = { Text(f, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = blue.copy(alpha = 0.4f), selectedLabelColor = cyan, containerColor = cardBg, labelColor = textSecondary))
                        }
                    }
                    1 -> Text("${vm.filteredPerformance.size} técnicos", color = textSecondary, fontSize = 12.sp)
                    2 -> OutlinedTextField(
                        value = vm.inventorySearchQuery, onValueChange = vm::updateInventorySearch,
                        placeholder = { Text("Buscar...", color = textSecondary.copy(alpha = 0.4f), fontSize = 12.sp) },
                        modifier = Modifier.width(180.dp).height(46.dp),
                        shape = RoundedCornerShape(10.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.6f), unfocusedContainerColor = cardBg, focusedContainerColor = cardBg, unfocusedTextColor = textPrimary, focusedTextColor = textPrimary),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                    )
                }
                if (vm.isExporting) CircularProgressIndicator(color = cyan, modifier = Modifier.size(24.dp))
                else IconButton(onClick = { vm.exportCurrentTabCsv(selectedTab) }) {
                    Icon(Icons.Filled.Share, null, tint = cyan)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Contenido por tab
        when (selectedTab) {
            0 -> {
                if (vm.isLoadingMaintenance) { item { LoadingBox(cyan) } }
                else if (vm.filteredMaintenance.isEmpty()) { item { EmptyBox(textSecondary) } }
                else items(vm.filteredMaintenance) { row -> MaintenanceRow(row, statusColors, cardBg, textPrimary, textSecondary) }
            }
            1 -> {
                if (vm.isLoadingPerformance) { item { LoadingBox(cyan) } }
                else if (vm.filteredPerformance.isEmpty()) { item { EmptyBox(textSecondary) } }
                else items(vm.filteredPerformance) { row -> PerformanceRow(row, cardBg, textPrimary, textSecondary) }
            }
            2 -> {
                if (vm.isLoadingInventory) { item { LoadingBox(cyan) } }
                else if (vm.filteredInventory.isEmpty()) { item { EmptyBox(textSecondary) } }
                else items(vm.filteredInventory) { row -> InventoryPartRow(row, cardBg, textPrimary, textSecondary) }
            }
        }
    }
}

@Composable private fun LoadingBox(cyan: Color) = Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cyan) }
@Composable private fun EmptyBox(ts: Color) = Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { Text("Sin datos para mostrar", color = ts) }

@Composable
private fun MaintenanceRow(row: MaintenanceReportRow, statusColors: Map<String, Color>, cardBg: Color, tp: Color, ts: Color) {
    val sc = statusColors[row.status] ?: Color(0xFF6B7280)
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Serie: ${row.serialNum}", color = tp, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Box(modifier = Modifier.background(sc.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) { Text(row.status, color = sc, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
            }
            Text(row.failureDesc, color = ts, fontSize = 12.sp, maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Técnico: ${row.technicianName}", color = ts.copy(alpha = 0.7f), fontSize = 11.sp)
                Text(row.createdAt?.take(10) ?: "", color = ts.copy(alpha = 0.5f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PerformanceRow(row: TechnicianPerformanceRow, cardBg: Color, tp: Color, ts: Color) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(Brush.linearGradient(listOf(Color(0xFF0B4F7A), Color(0xFF10B981).copy(alpha = 0.6f))), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                Text(row.name.firstOrNull()?.uppercase() ?: "T", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(row.name, color = tp, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${row.completedAssignments} trabajos completados", color = ts, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    (1..5).forEach { i -> Icon(if (i <= row.score) Icons.Filled.Star else Icons.Outlined.StarOutline, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp)) }
                    Text(String.format("%.1f", row.score), color = Color(0xFFF59E0B), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun InventoryPartRow(row: PartsInventoryRow, cardBg: Color, tp: Color, ts: Color) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.partName, color = tp, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Marca: ${row.brandName}", color = ts, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Cliente: ${row.clientName}", color = ts.copy(alpha = 0.7f), fontSize = 11.sp)
                Text("Serie: ${row.serialNum}", color = ts.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
    }
}

