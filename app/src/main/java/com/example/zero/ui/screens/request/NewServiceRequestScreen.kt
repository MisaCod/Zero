package com.example.zero.ui.screens.request

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.model.ClientEquipment
import com.example.zero.ui.viewmodel.AuthViewModel
import com.example.zero.ui.viewmodel.NewServiceRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewServiceRequestScreen(
    authViewModel: AuthViewModel = viewModel(),
    vm: NewServiceRequestViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {},
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236); val surfaceBg = Color(0xFF0F2D47)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)

    val clientId = authViewModel.getCurrentUserId() ?: ""
    var showEquipmentDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(clientId) { if (clientId.isNotBlank()) vm.loadEquipment(clientId) }

    LaunchedEffect(vm.submitSuccess) { if (vm.submitSuccess) { vm.resetSuccess(); onSuccess() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Solicitud", color = textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = textPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardBg),
            )
        },
        containerColor = darkBg,
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header info
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0B4F7A).copy(alpha = 0.3f))) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, null, tint = cyan, modifier = Modifier.size(18.dp))
                    Text("Selecciona el equipo con falla y describe el problema.", color = textSecondary, fontSize = 13.sp)
                }
            }

            // Selección de equipo
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Memory, null, tint = cyan, modifier = Modifier.size(16.dp))
                    Text("EQUIPO CON FALLA", color = cyan.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                }

                if (vm.isLoadingEquipment) {
                    Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(surfaceBg, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = cyan, modifier = Modifier.size(24.dp))
                    }
                } else {
                    ExposedDropdownMenuBox(expanded = showEquipmentDropdown, onExpandedChange = { showEquipmentDropdown = it }) {
                        OutlinedTextField(
                            value = vm.selectedEquipment?.let { "Equipo: ${it.serialNum} — ${it.location ?: "Sin ubicación"}" } ?: "Seleccionar equipo...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            leadingIcon = { Icon(Icons.Outlined.Memory, null, tint = cyan.copy(alpha = 0.6f)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEquipmentDropdown) },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedBorderColor = cyan.copy(alpha = 0.7f),
                                unfocusedContainerColor = surfaceBg,
                                focusedContainerColor = surfaceBg,
                                unfocusedTextColor = if (vm.selectedEquipment != null) textPrimary else textSecondary,
                                focusedTextColor = textPrimary,
                            ),
                        )
                        ExposedDropdownMenu(expanded = showEquipmentDropdown, onDismissRequest = { showEquipmentDropdown = false }, modifier = Modifier.background(cardBg)) {
                            if (vm.equipmentList.isEmpty()) {
                                DropdownMenuItem(text = { Text("No tienes equipos registrados", color = textSecondary) }, onClick = { showEquipmentDropdown = false })
                            } else {
                                vm.equipmentList.forEach { equip ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(equip.serialNum, color = textPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                                Text(equip.location ?: "Sin ubicación", color = textSecondary, fontSize = 12.sp)
                                            }
                                        },
                                        onClick = { vm.selectEquipment(equip); showEquipmentDropdown = false },
                                        modifier = Modifier.background(if (vm.selectedEquipment?.id == equip.id) blue.copy(alpha = 0.3f) else Color.Transparent),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Descripción del fallo
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Description, null, tint = cyan, modifier = Modifier.size(16.dp))
                    Text("DESCRIPCIÓN DEL FALLO", color = cyan.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                }
                OutlinedTextField(
                    value = vm.failureDesc,
                    onValueChange = vm::updateFailureDesc,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                    placeholder = { Text("Describe detalladamente el problema...\n\nEjemplo: El compresor hace ruido excesivo al arrancar y no enfría correctamente...", color = textSecondary.copy(alpha = 0.5f), fontSize = 13.sp) },
                    maxLines = 8,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedBorderColor = cyan.copy(alpha = 0.7f),
                        unfocusedContainerColor = surfaceBg,
                        focusedContainerColor = surfaceBg,
                        unfocusedTextColor = textPrimary,
                        focusedTextColor = textPrimary,
                        cursorColor = cyan,
                    ),
                )
                Text("${vm.failureDesc.length} caracteres", color = textSecondary.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.align(Alignment.End))
            }

            // Error
            AnimatedVisibility(visible = vm.errorMsg != null, enter = expandVertically() + fadeIn()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(12.dp)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Error, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Text(vm.errorMsg ?: "", color = Color(0xFFEF4444), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Botón enviar
            if (vm.isSubmitting) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cyan, modifier = Modifier.size(40.dp))
                }
            } else {
                Button(
                    onClick = { vm.submit(clientId) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(blue, cyan.copy(alpha = 0.85f))), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("Enviar Solicitud", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
