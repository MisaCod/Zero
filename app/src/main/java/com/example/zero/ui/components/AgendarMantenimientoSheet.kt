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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendarMantenimientoSheet(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (titulo: String, descripcion: String, ubicacion: String, equipo: String?, prioridad: String, fecha: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estados del formulario
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var equipo by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf("media") }
    var fecha by remember { mutableStateOf("") } // Idealmente usar un DatePicker real

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

            // Equipo (Opcional)
            OutlinedTextField(
                value = equipo,
                onValueChange = { equipo = it },
                label = { Text("Equipo (Opcional)") },
                leadingIcon = { Icon(Icons.Filled.PrecisionManufacturing, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )

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

            // Fecha
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha deseada (ej. 2026-06-20)") },
                leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
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

            Spacer(modifier = Modifier.height(Dimens.md))

            TechFlowPrimaryButton(
                text = if (isLoading) "Enviando..." else "Enviar Solicitud",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (!isLoading && titulo.isNotBlank() && ubicacion.isNotBlank()) {
                        onSubmit(titulo, descripcion, ubicacion, equipo, prioridad, fecha)
                    }
                },
            )
        }
    }
}
