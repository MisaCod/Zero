package com.example.zero.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.model.UserRole
import com.example.zero.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)
    val profile = authViewModel.profile
    val userRole = authViewModel.userRole
    val email = authViewModel.getCurrentUserId()?.let { "" } ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = textPrimary, fontWeight = FontWeight.Bold) },
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
            // Avatar + nombre
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(80.dp).background(Brush.linearGradient(listOf(blue, cyan)), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text(profile?.name?.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(12.dp))
                    Text(profile?.fullName?.ifBlank { "Sin nombre" } ?: "Sin nombre", color = textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    val (rc, rt) = when (userRole) {
                        UserRole.SUPERVISOR -> Color(0xFFF59E0B) to "SUPERVISOR"
                        UserRole.TECNICO -> Color(0xFF10B981) to "TÉCNICO"
                        UserRole.CLIENTE -> cyan to "CLIENTE"
                    }
                    Box(modifier = Modifier.background(rc.copy(alpha = 0.15f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text(rt, color = rc, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    }
                }
            }
            // Info personal
            ProfileCard("Información Personal", Icons.Outlined.Person, cyan, cardBg, textPrimary, textSecondary) {
                PRow("Cédula", profile?.identityCard ?: "No registrada", Icons.Outlined.Badge, cyan, textPrimary, textSecondary)
                PRow("Teléfono", profile?.phoneNumber ?: "No registrado", Icons.Outlined.Phone, cyan, textPrimary, textSecondary)
                PRow("Dirección", profile?.address ?: "No registrada", Icons.Outlined.LocationOn, cyan, textPrimary, textSecondary)
            }
            // Info app
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AcUnit, null, tint = cyan, modifier = Modifier.size(20.dp))
                    Column {
                        Text("0 Grados", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Sistema de Gestión de Mantenimiento Industrial", color = textSecondary, fontSize = 11.sp)
                        Text("v1.0.0 Beta · Supabase Realtime", color = textSecondary.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable private fun ProfileCard(title: String, icon: ImageVector, cyan: Color, cardBg: Color, tp: Color, ts: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = cyan, modifier = Modifier.size(18.dp))
                Text(title, color = tp, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            content()
        }
    }
}

@Composable private fun PRow(label: String, value: String, icon: ImageVector, cyan: Color, tp: Color, ts: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = cyan.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        Column { Text(label, color = ts, fontSize = 11.sp); Text(value, color = tp, fontSize = 14.sp) }
    }
}
