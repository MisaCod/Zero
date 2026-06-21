package com.example.zero.ui.screens.equipment

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.model.*
import com.example.zero.data.repository.EquipmentRepository
import com.example.zero.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// ViewModel para inventario de repuestos del cliente
class ClientPartsViewModel : ViewModel() {
    private val repo = EquipmentRepository()
    val items = mutableStateListOf<ClientPartsInventoryWithDetails>()
    val availableParts = mutableStateListOf<SparePart>()
    val clientEquipment = mutableStateListOf<ClientEquipment>()
    var isLoading by mutableStateOf(false); private set
    var isSaving by mutableStateOf(false); private set
    var errorMsg by mutableStateOf<String?>(null); private set
    var successMsg by mutableStateOf<String?>(null); private set

    fun load(clientId: String) {
        isLoading = true
        viewModelScope.launch {
            repo.getClientPartsInventory(clientId).onSuccess { list -> items.clear(); items.addAll(list) }.onFailure { e -> errorMsg = e.localizedMessage }
            if (availableParts.isEmpty()) repo.getSpareParts().onSuccess { availableParts.addAll(it) }
            isLoading = false
        }
    }

    fun add(clientId: String, partsId: String, serialNum: String, equipmentId: String?) {
        if (partsId.isBlank() || serialNum.isBlank()) { errorMsg = "Repuesto y serial son obligatorios"; return }
        isSaving = true
        viewModelScope.launch {
            repo.addClientPartsInventory(clientId, partsId, equipmentId, serialNum).fold(
                onSuccess = { load(clientId); successMsg = "Repuesto registrado" },
                onFailure = { e -> errorMsg = e.localizedMessage }
            )
            isSaving = false
        }
    }

    fun delete(id: String, clientId: String) {
        viewModelScope.launch {
            repo.deleteClientPartsInventory(id).fold(
                onSuccess = { items.removeAll { it.inventory.id == id }; successMsg = "Repuesto eliminado" },
                onFailure = { e -> errorMsg = e.localizedMessage }
            )
        }
    }
    fun clearMessages() { errorMsg = null; successMsg = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SparePartsScreen(
    authViewModel: AuthViewModel = viewModel(),
    vm: ClientPartsViewModel = viewModel(),
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236); val surfaceBg = Color(0xFF0F2D47)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)

    val clientId = authViewModel.getCurrentUserId() ?: ""
    var showAdd by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(clientId) { if (clientId.isNotBlank()) vm.load(clientId) }
    LaunchedEffect(vm.successMsg) { vm.successMsg?.let { snackbar.showSnackbar(it); vm.clearMessages() } }
    LaunchedEffect(vm.errorMsg) { vm.errorMsg?.let { snackbar.showSnackbar("Error: $it"); vm.clearMessages() } }

    Scaffold(
        containerColor = darkBg,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = blue, contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Filled.Add, null)
            }
        },
    ) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Build, null, tint = cyan, modifier = Modifier.size(22.dp))
                    Text("Inventario de Repuestos", color = textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Text("${vm.items.size} repuestos registrados · Presiona + para agregar", color = textSecondary, fontSize = 12.sp)
            }

            if (vm.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cyan) } }
            } else if (vm.items.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Build, null, tint = textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Sin repuestos en inventario", color = textSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(vm.items, key = { it.inventory.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(46.dp).background(Brush.linearGradient(listOf(blue, Color(0xFFF59E0B).copy(alpha = 0.4f))), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Build, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.partName, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Marca: ${item.partBrand}", color = textSecondary, fontSize = 12.sp)
                                Text("Serial: ${item.inventory.serialNum}", color = textSecondary.copy(alpha = 0.7f), fontSize = 11.sp)
                                if (item.equipmentSerial != "N/A") Text("Equipo: ${item.equipmentSerial}", color = cyan.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                            IconButton(onClick = { vm.delete(item.inventory.id, clientId) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444).copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddPartDialog(vm = vm, clientId = clientId, cardBg = cardBg, surfaceBg = surfaceBg, textPrimary = textPrimary, textSecondary = textSecondary, cyan = cyan, onDismiss = { showAdd = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPartDialog(vm: ClientPartsViewModel, clientId: String, cardBg: Color, surfaceBg: Color, textPrimary: Color, textSecondary: Color, cyan: Color, onDismiss: () -> Unit) {
    var selectedPart by remember { mutableStateOf<SparePart?>(null) }
    var serial by remember { mutableStateOf("") }
    var partExpanded by remember { mutableStateOf(false) }
    val fieldColors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.7f), unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg, unfocusedTextColor = textPrimary, focusedTextColor = textPrimary)

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = cardBg,
        title = { Text("Agregar Repuesto", color = textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = partExpanded, onExpandedChange = { partExpanded = it }) {
                    OutlinedTextField(value = selectedPart?.let { "${it.name} (${it.brandName})" } ?: "Seleccionar repuesto...", onValueChange = {}, readOnly = true, label = { Text("Repuesto", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(partExpanded) }, shape = RoundedCornerShape(10.dp), colors = fieldColors)
                    ExposedDropdownMenu(expanded = partExpanded, onDismissRequest = { partExpanded = false }, modifier = Modifier.background(cardBg)) {
                        vm.availableParts.forEach { p -> DropdownMenuItem(text = { Column { Text(p.name, color = textPrimary, fontSize = 13.sp); Text(p.brandName, color = textSecondary, fontSize = 11.sp) } }, onClick = { selectedPart = p; partExpanded = false }) }
                    }
                }
                OutlinedTextField(value = serial, onValueChange = { serial = it }, label = { Text("Número de Serie del repuesto", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors)
            }
        },
        confirmButton = {
            if (vm.isSaving) CircularProgressIndicator(color = cyan, modifier = Modifier.size(24.dp))
            else TextButton(onClick = { vm.add(clientId, selectedPart?.id ?: "", serial, null); onDismiss() }, colors = ButtonDefaults.textButtonColors(contentColor = cyan)) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = textSecondary) } },
    )
}
