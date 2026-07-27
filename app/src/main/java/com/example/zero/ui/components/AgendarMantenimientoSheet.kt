package com.example.zero.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.zero.ui.theme.Dimens

// ============================================================================
// AgendarMantenimientoSheet — Bottom sheet con el formulario para clientes.
// Permite registrar un nuevo "service_request" en Supabase.
// ============================================================================

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendarMantenimientoSheet(
    isLoading: Boolean,
    errorMsg: String? = null,
    clientId: String,
    onDismiss: () -> Unit,
    onSubmit: (titulo: String, descripcion: String, ubicacion: String, equipo: String?, equipoId: String?, prioridad: String, fecha: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estados del formulario
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var equipo by remember { mutableStateOf("") }
    var equipoId by remember { mutableStateOf<String?>(null) }
    var prioridad by remember { mutableStateOf("media") }
    var fecha by remember { mutableStateOf("") }

    var formError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var equipoExpanded by remember { mutableStateOf(false) }

    val equipmentRepo = remember { com.example.zero.data.repository.EquipmentRepository() }
    var clientEquipments by remember { mutableStateOf<List<com.example.zero.data.model.ClientEquipmentWithDetails>>(emptyList()) }

    LaunchedEffect(clientId) {
        equipmentRepo.getClientEquipment(clientId).onSuccess { list ->
            clientEquipments = list
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.lg)
                .padding(bottom = Dimens.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Text(
                text = "Agendar Mantenimiento",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Completa los datos de tu solicitud",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Dimens.xs))

            // Título
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título / Problema Principal") },
                leadingIcon = { Icon(Icons.Filled.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )

            // Ubicación
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación exacta") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )

            // Equipo (Opcional) Dropdown
            ExposedDropdownMenuBox(
                expanded = equipoExpanded,
                onExpandedChange = { equipoExpanded = it }
            ) {
                OutlinedTextField(
                    value = equipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Equipo (Opcional)") },
                    leadingIcon = { Icon(Icons.Filled.PrecisionManufacturing, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(equipoExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
                ExposedDropdownMenu(
                    expanded = equipoExpanded,
                    onDismissRequest = { equipoExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Ninguno") },
                        onClick = { equipo = ""; equipoExpanded = false }
                    )
                    clientEquipments.forEach { eq ->
                        val displayText = "${eq.serialNum} - ${eq.reference}"
                        DropdownMenuItem(
                            text = { Text(displayText) },
                            onClick = { 
                                equipo = displayText
                                equipoId = eq.equipment.id
                                equipoExpanded = false 
                            }
                        )
                    }
                }
            }

            // Prioridad Chips
            Text(
                text = "Prioridad",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                listOf("baja", "media", "alta").forEach { prio ->
                    FilterChip(
                        selected = prioridad == prio,
                        onClick = { prioridad = prio },
                        label = { Text(prio.replaceFirstChar { it.uppercase() }) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when(prio) {
                                "alta" -> MaterialTheme.colorScheme.errorContainer
                                "baja" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            selectedLabelColor = when(prio) {
                                "alta" -> MaterialTheme.colorScheme.onErrorContainer
                                "baja" -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    )
                }
            }

            // Fecha DatePicker
            OutlinedTextField(
                value = fecha,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha deseada") },
                leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false, // To make the clickable work over the whole field
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Detalles adicionales") },
                leadingIcon = { Icon(Icons.Filled.Description, null) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )

            // Error
            if (errorMsg != null || formError != null) {
                Text(
                    text = formError ?: errorMsg ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Dimens.sm)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.md))

            Button(
                onClick = {
                    if (titulo.isBlank() || ubicacion.isBlank() || equipo.isBlank() || fecha.isBlank()) {
                        formError = "Por favor completa todos los campos obligatorios."
                    } else {
                        formError = null
                        onSubmit(titulo, descripcion, ubicacion, equipo.takeIf { it.isNotBlank() }, equipoId, prioridad, fecha)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Agendar Solicitud")
                }
            }
        }
    }

    if (showDatePicker) {
        val todayMillis = remember {
            LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= todayMillis
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        val instant = Instant.ofEpochMilli(ms)
                        fecha = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC")).format(instant)
                    }
                    showDatePicker = false
                }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
