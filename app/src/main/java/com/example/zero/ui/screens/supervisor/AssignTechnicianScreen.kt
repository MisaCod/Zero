package com.example.zero.ui.screens.supervisor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.repository.AvailableTechnician
import com.example.zero.ui.viewmodel.AssignTechnicianViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTechnicianScreen(
    serviceRequestId: String,
    vm: AssignTechnicianViewModel = viewModel(),
    onBack: () -> Unit = {},
    onAssigned: () -> Unit = {},
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val green = Color(0xFF10B981); val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)

    LaunchedEffect(serviceRequestId) { vm.loadData(serviceRequestId) }
    LaunchedEffect(vm.assignmentSuccess) { if (vm.assignmentSuccess) { vm.resetSuccess(); onAssigned() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asignar Técnico", color = textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = textPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardBg),
            )
        },
        containerColor = darkBg,
    ) { pad ->
        if (vm.isLoading) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cyan) }
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Detalle de la solicitud
            item {
                vm.serviceRequest?.let { req ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Build, null, tint = cyan, modifier = Modifier.size(18.dp))
                                Text("SOLICITUD A ASIGNAR", color = cyan.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Text(req.failureDesc.take(120), color = textPrimary, fontSize = 14.sp)
                            if (vm.equipmentSerialNum.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Memory, null, tint = textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                    Text("Serie: ${vm.equipmentSerialNum}", color = textSecondary, fontSize = 12.sp)
                                }
                            }
                            if (vm.equipmentLocation.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.LocationOn, null, tint = textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                    Text(vm.equipmentLocation, color = textSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Header técnicos
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Group, null, tint = cyan, modifier = Modifier.size(16.dp))
                    Text("TÉCNICOS DISPONIBLES (${vm.technicians.size})", color = cyan.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                }
            }

            if (vm.technicians.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.PersonOff, null, tint = textSecondary.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No hay técnicos disponibles en este momento", color = textSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(vm.technicians) { tech ->
                    TechnicianCard(tech = tech, isAssigning = vm.isAssigning, cyan = cyan, blue = blue, green = green, cardBg = cardBg, textPrimary = textPrimary, textSecondary = textSecondary) {
                        vm.assignTechnician(serviceRequestId, tech)
                    }
                }
            }

            // Error
            vm.errorMsg?.let {
                item {
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(12.dp)).padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Error, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Text(it, color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicianCard(
    tech: AvailableTechnician, isAssigning: Boolean, cyan: Color, blue: Color, green: Color,
    cardBg: Color, textPrimary: Color, textSecondary: Color, onAssign: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(Brush.linearGradient(listOf(blue, cyan.copy(alpha = 0.6f))), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text(tech.name.firstOrNull()?.uppercase() ?: "T", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text("${tech.name} ${tech.lastName}".trim().ifBlank { "Técnico" }, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text("CI: ${tech.identityCard}", color = textSecondary, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                    Text(String.format("%.1f", tech.score), color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            if (isAssigning) CircularProgressIndicator(color = cyan, modifier = Modifier.size(28.dp))
            else Button(
                onClick = onAssign,
                colors = ButtonDefaults.buttonColors(containerColor = green),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) { Text("Asignar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        }
    }
}
