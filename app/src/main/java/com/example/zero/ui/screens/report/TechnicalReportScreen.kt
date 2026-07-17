package com.example.zero.ui.screens.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
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
import com.example.zero.ui.viewmodel.TechnicalReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalReportScreen(
    assigmentId: String,
    vm: TechnicalReportViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {},
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236); val surfaceBg = Color(0xFF0F2D47)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)
    val green = Color(0xFF10B981)

    LaunchedEffect(vm.submitSuccess) { if (vm.submitSuccess) { vm.resetSuccess(); onSuccess() } }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let {
                        val instant = java.time.Instant.ofEpochMilli(it)
                        vm.updateManualEndTime(java.time.format.DateTimeFormatter.ISO_INSTANT.format(instant))
                    }
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte Técnico", color = textPrimary, fontWeight = FontWeight.Bold) },
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
            // Info asignación
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f))) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Assignment, null, tint = green, modifier = Modifier.size(20.dp))
                    Column {
                        Text("Asignación: #${assigmentId.take(8).uppercase()}", color = green, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Inicio registrado: ${vm.startTime.take(16).replace("T", " ")}", color = textSecondary, fontSize = 11.sp)
                    }
                }
            }

            // Diagnóstico
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Engineering, null, tint = cyan, modifier = Modifier.size(16.dp))
                    Text("DIAGNÓSTICO TÉCNICO", color = cyan.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                }
                OutlinedTextField(
                    value = vm.diagnosis,
                    onValueChange = vm::updateDiagnosis,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                    placeholder = { Text("Describe el diagnóstico técnico detallado...\n\nEjemplo: Se detectó fuga en la válvula de expansión. Compresor con presión baja en descarga...", color = textSecondary.copy(alpha = 0.5f), fontSize = 13.sp) },
                    maxLines = 10,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.7f),
                        unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg,
                        unfocusedTextColor = textPrimary, focusedTextColor = textPrimary, cursorColor = cyan,
                    ),
                )
            }

            // Fecha de finalización
            Card(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Fecha de Finalización", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            vm.manualEndTime?.take(10) ?: "Toca para seleccionar fecha",
                            color = textSecondary, fontSize = 12.sp,
                        )
                    }
                    Icon(Icons.Filled.CalendarToday, null, tint = cyan)
                }
            }

            // Trabajo completado
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Trabajo Completado", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            if (vm.workDone) "✓ Solicitud pasará a TERMINADO" else "Solicitud permanece EN PROGRESO",
                            color = if (vm.workDone) green else textSecondary, fontSize = 12.sp,
                        )
                    }
                    Switch(
                        checked = vm.workDone,
                        onCheckedChange = vm::toggleWorkDone,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = green),
                    )
                }
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

            if (vm.isSubmitting) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cyan, modifier = Modifier.size(40.dp)) }
            } else {
                Button(
                    onClick = { vm.submitReport(assigmentId, null) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(blue, if (vm.workDone) green else cyan.copy(alpha = 0.85f))), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text(if (vm.workDone) "Finalizar Trabajo" else "Guardar Reporte", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
