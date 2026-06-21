package com.example.zero.ui.screens.technician

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.model.Assignment
import com.example.zero.data.model.TechnicianDetails
import com.example.zero.data.repository.TechnicianRepository
import com.example.zero.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class TechnicianPanelViewModel : ViewModel() {
    private val repo = TechnicianRepository()
    var details by mutableStateOf<TechnicianDetails?>(null)
        private set
    val assignments = mutableStateListOf<Assignment>()
    var isLoading by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMsg by mutableStateOf<String?>(null)
        private set

    fun load(userId: String) {
        isLoading = true
        viewModelScope.launch {
            repo.getDetails(userId).onSuccess { details = it }
            repo.getAssignmentsByTech(userId).onSuccess { list -> assignments.clear(); assignments.addAll(list) }
            isLoading = false
        }
    }

    fun toggleAvailability(userId: String) {
        val current = details?.availability ?: false
        isSaving = true
        viewModelScope.launch {
            repo.updateAvailability(userId, !current).onSuccess {
                details = details?.copy(availability = !current)
            }.onFailure { e -> errorMsg = "Error: ${e.localizedMessage}" }
            isSaving = false
        }
    }
    fun clearError() { errorMsg = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianAvailabilityScreen(
    authViewModel: AuthViewModel = viewModel(),
    vm: TechnicianPanelViewModel = viewModel(),
    onSolicitudClick: (String) -> Unit = {},
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val green = Color(0xFF10B981); val red = Color(0xFFEF4444)
    val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)
    val userId = authViewModel.getCurrentUserId() ?: ""

    LaunchedEffect(userId) { if (userId.isNotBlank()) vm.load(userId) }

    val available = vm.details?.availability ?: false
    val score = vm.details?.score ?: 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(darkBg),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Mi Panel Técnico", color = textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Gestiona tu disponibilidad y trabajos", color = textSecondary, fontSize = 13.sp)
        }

        // Disponibilidad
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(52.dp).background(
                                if (available) green.copy(alpha = 0.15f) else red.copy(alpha = 0.15f), CircleShape
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(if (available) Icons.Filled.CheckCircle else Icons.Filled.Cancel, null, tint = if (available) green else red, modifier = Modifier.size(28.dp))
                        }
                        Column {
                            Text("Disponibilidad", color = textSecondary, fontSize = 12.sp)
                            Text(if (available) "Disponible para trabajos" else "No disponible", color = if (available) green else red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    if (vm.isSaving) CircularProgressIndicator(color = cyan, modifier = Modifier.size(28.dp))
                    else Switch(
                        checked = available,
                        onCheckedChange = { vm.toggleAvailability(userId) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = green, uncheckedTrackColor = red.copy(alpha = 0.4f)),
                    )
                }
            }
        }

        // Score
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(52.dp).background(Brush.linearGradient(listOf(Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFF59E0B).copy(alpha = 0.05f))), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Text("Calificación Promedio", color = textSecondary, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(String.format("%.1f", score), color = textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("/ 5.0", color = textSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                        }
                        Row {
                            (1..5).forEach { i -> Icon(if (i <= score) Icons.Filled.Star else Icons.Outlined.StarOutline, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp)) }
                        }
                    }
                }
            }
        }

        // Asignaciones
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Assignment, null, tint = cyan, modifier = Modifier.size(16.dp))
                Text("MIS ASIGNACIONES (${vm.assignments.size})", color = cyan.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            }
        }

        if (vm.isLoading) {
            item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cyan) } }
        } else if (vm.assignments.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.AssignmentTurnedIn, null, tint = textSecondary.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Sin asignaciones activas", color = textSecondary, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(vm.assignments) { assignment ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { assignment.requestId.let { onSolicitudClick(it) } },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2D47)),
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(blue.copy(alpha = 0.3f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Build, null, tint = cyan, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Asignación #${assignment.id?.take(8)?.uppercase() ?: "—"}", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Solicitud: #${assignment.requestId.take(8).uppercase()}", color = textSecondary, fontSize = 12.sp)
                            Text(assignment.createdAt?.take(10) ?: "", color = textSecondary.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = textSecondary.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}
