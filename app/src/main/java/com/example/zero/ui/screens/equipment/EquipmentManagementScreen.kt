package com.example.zero.ui.screens.equipment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed

// ViewModel combinado para el módulo 3.0 — Equipo
class EquipmentManagementViewModel : ViewModel() {
    private val repo = EquipmentRepository()

    // Equipos registrados
    val equipmentList = mutableStateListOf<ClientEquipmentWithDetails>()
    // Catalogo (para selector)
    val catalog = mutableStateListOf<EquipmentCatalogWithDetails>()
    // Clientes (para asignar equipo — Supervisor)
    val clients = mutableStateListOf<User>()

    var isLoading by mutableStateOf(false); private set
    var isSaving by mutableStateOf(false); private set
    var errorMsg by mutableStateOf<String?>(null); private set
    var successMsg by mutableStateOf<String?>(null); private set

    var searchQuery by mutableStateOf("")
    var searchFilter by mutableStateOf("Número de Serie") // Opciones: "Marca", "Tipo", "Número de Serie"

    val filteredEquipmentList: List<ClientEquipmentWithDetails>
        get() {
            if (searchQuery.isBlank()) return equipmentList.toList()
            return equipmentList.filter {
                when (searchFilter) {
                    "Marca" -> it.brandName.contains(searchQuery, ignoreCase = true)
                    "Tipo" -> it.typeName.contains(searchQuery, ignoreCase = true)
                    "Número de Serie" -> it.serialNum.contains(searchQuery, ignoreCase = true)
                    else -> true
                }
            }
        }

    fun loadForClient(clientId: String) {
        isLoading = true
        viewModelScope.launch {
            repo.getClientEquipment(clientId).onSuccess { list -> equipmentList.clear(); equipmentList.addAll(list) }.onFailure { e -> errorMsg = e.localizedMessage }
            if (catalog.isEmpty()) repo.getEquipmentCatalogWithDetails().onSuccess { catalog.addAll(it) }
            isLoading = false
        }
    }

    fun loadAll() {
        isLoading = true
        viewModelScope.launch {
            repo.getAllClientEquipment().onSuccess { list -> equipmentList.clear(); equipmentList.addAll(list) }.onFailure { e -> errorMsg = e.localizedMessage }
            if (catalog.isEmpty()) repo.getEquipmentCatalogWithDetails().onSuccess { catalog.addAll(it) }
            if (clients.isEmpty()) repo.getClients().onSuccess { clients.addAll(it) }
            isLoading = false
        }
    }

    fun add(clientId: String, catalogId: String, serialNum: String, location: String, isClient: Boolean) {
        if (serialNum.isBlank() || catalogId.isBlank()) { errorMsg = "Serial y catálogo son obligatorios"; return }
        isSaving = true
        viewModelScope.launch {
            repo.addClientEquipment(clientId, catalogId, serialNum, location).fold(
                onSuccess = { if (isClient) loadForClient(clientId) else loadAll(); successMsg = "Equipo registrado correctamente" },
                onFailure = { e -> errorMsg = "Error: ${e.localizedMessage}" }
            )
            isSaving = false
        }
    }

    fun update(id: String, catalogId: String, location: String, isClient: Boolean, clientId: String) {
        if (catalogId.isBlank()) { errorMsg = "Catálogo es obligatorio"; return }
        isSaving = true
        viewModelScope.launch {
            repo.updateClientEquipment(id, catalogId, location).fold(
                onSuccess = { if (isClient) loadForClient(clientId) else loadAll(); successMsg = "Equipo actualizado correctamente" },
                onFailure = { e -> errorMsg = "Error: ${e.localizedMessage}" }
            )
            isSaving = false
        }
    }

    fun delete(id: String, isClient: Boolean, clientId: String = "") {
        viewModelScope.launch {
            repo.deleteClientEquipment(id).fold(
                onSuccess = { equipmentList.removeAll { it.equipment.id == id }; successMsg = "Equipo eliminado" },
                onFailure = { e -> errorMsg = "Error: ${e.localizedMessage}" }
            )
        }
    }
    fun clearMessages() { errorMsg = null; successMsg = null }

    // ── Tipos de equipo ──────────────────────────────────────────────────
    val equipmentTypes = mutableStateListOf<EquipmentType>()
    var isLoadingTypes by mutableStateOf(false); private set

    fun loadEquipmentTypes() {
        isLoadingTypes = true
        viewModelScope.launch {
            repo.getEquipmentTypes().fold(
                onSuccess = { list -> equipmentTypes.clear(); equipmentTypes.addAll(list) },
                onFailure = { e -> errorMsg = "Error al cargar tipos: ${e.localizedMessage}" }
            )
            isLoadingTypes = false
        }
    }

    fun addEquipmentType(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) { errorMsg = "El nombre del tipo es obligatorio"; return }
        if (equipmentTypes.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            errorMsg = "Ya existe un tipo de equipo con ese nombre."
            return
        }
        isSaving = true
        viewModelScope.launch {
            repo.addEquipmentType(trimmedName).fold(
                onSuccess = { created ->
                    equipmentTypes.add(created)
                    successMsg = "Tipo de equipo '${created.name}' agregado"
                },
                onFailure = { e -> errorMsg = "Error: ${e.localizedMessage}" }
            )
            isSaving = false
        }
    }

    fun deleteEquipmentType(id: String) {
        viewModelScope.launch {
            repo.deleteEquipmentType(id).fold(
                onSuccess = { equipmentTypes.removeAll { it.id == id }; successMsg = "Tipo eliminado" },
                onFailure = { e -> errorMsg = "Error: ${e.localizedMessage}" }
            )
        }
    }

    // ── Marcas y Catálogo ────────────────────────────────────────────────
    val brands = mutableStateListOf<Brand>()

    fun loadBrands() {
        viewModelScope.launch {
            repo.getBrands().fold(
                onSuccess = { list -> brands.clear(); brands.addAll(list) },
                onFailure = { e -> errorMsg = "Error al cargar marcas: ${e.localizedMessage}" }
            )
        }
    }

    fun addBrand(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) { errorMsg = "El nombre de la marca es obligatorio"; return }
        if (brands.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            errorMsg = "Ya existe una marca con ese nombre."
            return
        }
        isSaving = true
        viewModelScope.launch {
            repo.addBrand(trimmedName).fold(
                onSuccess = { created ->
                    brands.add(created)
                    successMsg = "Marca '${created.name}' agregada"
                },
                onFailure = { e -> errorMsg = "Error: ${e.localizedMessage}" }
            )
            isSaving = false
        }
    }

    fun addCatalog(typeId: String, brandId: String, reference: String) {
        val trimmedRef = reference.trim()
        if (typeId.isBlank() || brandId.isBlank() || trimmedRef.isBlank()) {
            errorMsg = "Tipo, marca y referencia son obligatorios"
            return
        }
        isSaving = true
        viewModelScope.launch {
            repo.addEquipmentCatalog(typeId, brandId, trimmedRef).fold(
                onSuccess = {
                    repo.getEquipmentCatalogWithDetails().onSuccess { cat -> catalog.clear(); catalog.addAll(cat) }
                    successMsg = "Modelo agregado al catálogo exitosamente"
                },
                onFailure = { e -> errorMsg = "Error al agregar modelo: ${e.localizedMessage}" }
            )
            isSaving = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentManagementScreen(
    authViewModel: AuthViewModel = viewModel(),
    vm: EquipmentManagementViewModel = viewModel(),
    isSupervisor: Boolean = false,
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236); val surfaceBg = Color(0xFF0F2D47)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)
    val green = Color(0xFF10B981)

    val clientId = authViewModel.getCurrentUserId() ?: ""
    var showAdd by remember { mutableStateOf(false) }
    var showCatalogDialog by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf<String?>(null) }
    var editingEquipment by remember { mutableStateOf<ClientEquipmentWithDetails?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(clientId) { if (isSupervisor) vm.loadAll() else if (clientId.isNotBlank()) vm.loadForClient(clientId) }
    LaunchedEffect(vm.successMsg) { vm.successMsg?.let { snackbar.showSnackbar(it); vm.clearMessages() } }
    LaunchedEffect(vm.errorMsg) { vm.errorMsg?.let { snackbar.showSnackbar("Error: $it"); vm.clearMessages() } }

    Scaffold(
        containerColor = darkBg,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (isSupervisor) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Botón modelos/catálogo
                    FloatingActionButton(
                        onClick = { vm.loadEquipmentTypes(); vm.loadBrands(); showCatalogDialog = true },
                        containerColor = Color(0xFF0D6E3E),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.Category, null, modifier = Modifier.size(22.dp))
                    }
                    // Botón agregar equipo
                    FloatingActionButton(onClick = { showAdd = true }, containerColor = blue, contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Filled.Add, null)
                    }
                }
            } else {
                FloatingActionButton(onClick = { showAdd = true }, containerColor = blue, contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Filled.Add, null)
                }
            }
        },
    ) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Memory, null, tint = cyan, modifier = Modifier.size(22.dp))
                    Text(if (isSupervisor) "Equipos del Sistema" else "Mis Equipos", color = textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Text("Módulo 3.0 — ${vm.equipmentList.size} equipos registrados", color = textSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = vm.searchQuery,
                        onValueChange = { vm.searchQuery = it },
                        placeholder = { Text("Buscar...", color = textSecondary, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar", tint = textSecondary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedBorderColor = cyan.copy(alpha = 0.7f),
                            unfocusedContainerColor = cardBg,
                            focusedContainerColor = cardBg,
                            unfocusedTextColor = textPrimary,
                            focusedTextColor = textPrimary,
                        ),
                        singleLine = true
                    )
                    var filterExpanded by remember { mutableStateOf(false) }
                    val filters = listOf("Marca", "Tipo", "Número de Serie")
                    Box {
                        IconButton(onClick = { filterExpanded = true }, modifier = Modifier.background(cardBg, RoundedCornerShape(12.dp)).size(56.dp)) {
                            Icon(Icons.Outlined.FilterList, contentDescription = "Filtrar", tint = cyan)
                        }
                        DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }, modifier = Modifier.background(cardBg)) {
                            filters.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f, color = if (vm.searchFilter == f) cyan else textPrimary) },
                                    onClick = { vm.searchFilter = f; filterExpanded = false }
                                )
                            }
                        }
                    }
                }
                Text("Buscando por: ${vm.searchFilter}", color = textSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
            }

            if (vm.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cyan) } }
            } else if (vm.filteredEquipmentList.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Memory, null, tint = textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No hay equipos registrados", color = textSecondary, fontSize = 14.sp)
                            Text("Presiona + para agregar", color = textSecondary.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(vm.filteredEquipmentList, key = { it.equipment.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        onClick = { if (isSupervisor) editingEquipment = item }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(46.dp).background(Brush.linearGradient(listOf(blue, cyan.copy(alpha = 0.5f))), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Memory, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.reference.ifBlank { "Sin referencia" }, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Serie: ${item.serialNum}", color = textSecondary, fontSize = 12.sp)
                                Text("${item.typeName} · ${item.brandName}", color = textSecondary.copy(alpha = 0.7f), fontSize = 11.sp)
                                if (item.location.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(Icons.Outlined.LocationOn, null, tint = textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                                        Text(item.location, color = textSecondary.copy(alpha = 0.6f), fontSize = 11.sp)
                                    }
                                }
                                if (isSupervisor && item.clientName.isNotBlank()) {
                                    Text("Cliente: ${item.clientName}", color = cyan.copy(alpha = 0.6f), fontSize = 11.sp)
                                }
                            }
                            if (isSupervisor) IconButton(onClick = { deleteConfirm = item.equipment.id }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444).copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddEquipmentDialog(
        vm = vm, isSupervisor = isSupervisor, clientId = clientId,
        cardBg = cardBg, surfaceBg = surfaceBg, textPrimary = textPrimary, textSecondary = textSecondary, cyan = cyan,
        onDismiss = { showAdd = false },
    )

    deleteConfirm?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirm = null }, containerColor = cardBg,
            title = { Text("Eliminar Equipo", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("¿Confirmas la eliminación de este equipo?", color = textSecondary) },
            confirmButton = { TextButton(onClick = { vm.delete(id, !isSupervisor, clientId); deleteConfirm = null }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { deleteConfirm = null }) { Text("Cancelar", color = textSecondary) } },
        )
    }

    editingEquipment?.let { eq ->
        EditEquipmentDialog(
            vm = vm, eq = eq, isSupervisor = isSupervisor, clientId = clientId,
            cardBg = cardBg, surfaceBg = surfaceBg, textPrimary = textPrimary, textSecondary = textSecondary, cyan = cyan,
            onDismiss = { editingEquipment = null }
        )
    }

    if (showCatalogDialog) {
        ManageCatalogDialog(
            vm = vm,
            cardBg = cardBg, surfaceBg = surfaceBg, textPrimary = textPrimary, textSecondary = textSecondary, cyan = cyan,
            onDismiss = { showCatalogDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEquipmentDialog(
    vm: EquipmentManagementViewModel, isSupervisor: Boolean, clientId: String,
    cardBg: Color, surfaceBg: Color, textPrimary: Color, textSecondary: Color, cyan: Color, onDismiss: () -> Unit,
) {
    var serial by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedCatalog by remember { mutableStateOf<EquipmentCatalogWithDetails?>(null) }
    var selectedClientId by remember { mutableStateOf(clientId) }
    var catalogExpanded by remember { mutableStateOf(false) }
    var clientExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.7f),
        unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg, unfocusedTextColor = textPrimary, focusedTextColor = textPrimary,
    )

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = cardBg,
        title = { Text("Registrar Equipo", color = textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Selector de catálogo
                ExposedDropdownMenuBox(expanded = catalogExpanded, onExpandedChange = { catalogExpanded = it }) {
                    OutlinedTextField(
                        value = selectedCatalog?.let { "${it.typeName} ${it.brandName} - ${it.reference}" } ?: "Seleccionar modelo...",
                        onValueChange = {}, readOnly = true, label = { Text("Modelo (Tipo y Marca)", color = textSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catalogExpanded) },
                        shape = RoundedCornerShape(10.dp), colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = catalogExpanded, onDismissRequest = { catalogExpanded = false }, modifier = Modifier.background(cardBg)) {
                        vm.catalog.forEach { cat -> DropdownMenuItem(text = { Column { Text(cat.reference, color = textPrimary, fontSize = 13.sp); Text("${cat.typeName} · ${cat.brandName}", color = textSecondary, fontSize = 11.sp) } }, onClick = { selectedCatalog = cat; catalogExpanded = false }) }
                    }
                }
                OutlinedTextField(value = serial, onValueChange = { serial = it.replace(" ", "") }, label = { Text("Número de Serie", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors)
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Ubicación", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors)
            }
        },
        confirmButton = {
            if (vm.isSaving) CircularProgressIndicator(color = cyan, modifier = Modifier.size(24.dp))
            else TextButton(onClick = { vm.add(selectedClientId, selectedCatalog?.id ?: "", serial, location, !isSupervisor); onDismiss() }, colors = ButtonDefaults.textButtonColors(contentColor = cyan)) { Text("Registrar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = textSecondary) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEquipmentDialog(
    vm: EquipmentManagementViewModel, eq: ClientEquipmentWithDetails, isSupervisor: Boolean, clientId: String,
    cardBg: Color, surfaceBg: Color, textPrimary: Color, textSecondary: Color, cyan: Color, onDismiss: () -> Unit,
) {
    var location by remember { mutableStateOf(eq.equipment.location ?: "") }
    var selectedCatalog by remember { mutableStateOf<EquipmentCatalogWithDetails?>(eq.catalog?.let { c -> EquipmentCatalogWithDetails(c, eq.typeName ?: "", eq.brandName ?: "") }) }
    var catalogExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.7f),
        unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg, unfocusedTextColor = textPrimary, focusedTextColor = textPrimary,
    )

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = cardBg,
        title = { Text("Modificar Equipo", color = textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = eq.equipment.serialNum, onValueChange = {}, readOnly = true, label = { Text("Número de Serie (Solo lectura)", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors)
                OutlinedTextField(value = eq.clientName, onValueChange = {}, readOnly = true, label = { Text("Cliente (Solo lectura)", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors)
                
                // Selector de catálogo
                ExposedDropdownMenuBox(expanded = catalogExpanded, onExpandedChange = { catalogExpanded = it }) {
                    OutlinedTextField(
                        value = selectedCatalog?.let { "${it.typeName} ${it.brandName} - ${it.reference}" } ?: "Seleccionar modelo...",
                        onValueChange = {}, readOnly = true, label = { Text("Modelo (Tipo y Marca)", color = textSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catalogExpanded) },
                        shape = RoundedCornerShape(10.dp), colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = catalogExpanded, onDismissRequest = { catalogExpanded = false }, modifier = Modifier.background(cardBg)) {
                        vm.catalog.forEach { cat -> DropdownMenuItem(text = { Column { Text(cat.reference, color = textPrimary, fontSize = 13.sp); Text("${cat.typeName} · ${cat.brandName}", color = textSecondary, fontSize = 11.sp) } }, onClick = { selectedCatalog = cat; catalogExpanded = false }) }
                    }
                }
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Ubicación", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors)
            }
        },
        confirmButton = {
            if (vm.isSaving) CircularProgressIndicator(color = cyan, modifier = Modifier.size(24.dp))
            else TextButton(onClick = { vm.update(eq.equipment.id, selectedCatalog?.id ?: "", location, !isSupervisor, clientId); onDismiss() }, colors = ButtonDefaults.textButtonColors(contentColor = cyan)) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = textSecondary) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageCatalogDialog(
    vm: EquipmentManagementViewModel,
    cardBg: Color, surfaceBg: Color, textPrimary: Color, textSecondary: Color, cyan: Color,
    onDismiss: () -> Unit,
) {
    var selectedType by remember { mutableStateOf<EquipmentType?>(null) }
    var selectedBrand by remember { mutableStateOf<Brand?>(null) }
    var reference by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }
    var brandExpanded by remember { mutableStateOf(false) }

    var showNewTypeDialog by remember { mutableStateOf(false) }
    var showNewBrandDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.7f),
        unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg,
        unfocusedTextColor = textPrimary, focusedTextColor = textPrimary,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Category, null, tint = cyan, modifier = Modifier.size(22.dp))
                Text("Registrar Modelo en Catálogo", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Crea un nuevo modelo especificando su tipo, marca y referencia.", color = textSecondary, fontSize = 13.sp)

                // Dropdown Tipo
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedType?.name ?: "Seleccionar Tipo...",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Tipo de Equipo", color = textSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        shape = RoundedCornerShape(10.dp), colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }, modifier = Modifier.background(cardBg)) {
                        DropdownMenuItem(
                            text = { Text("+ Agregar Nuevo Tipo", color = cyan, fontWeight = FontWeight.Bold) },
                            onClick = { typeExpanded = false; newName = ""; showNewTypeDialog = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        vm.equipmentTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name, color = textPrimary) },
                                onClick = { selectedType = type; typeExpanded = false }
                            )
                        }
                    }
                }

                // Dropdown Marca
                ExposedDropdownMenuBox(expanded = brandExpanded, onExpandedChange = { brandExpanded = it }) {
                    OutlinedTextField(
                        value = selectedBrand?.name ?: "Seleccionar Marca...",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Marca", color = textSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(brandExpanded) },
                        shape = RoundedCornerShape(10.dp), colors = fieldColors
                    )
                    ExposedDropdownMenu(expanded = brandExpanded, onDismissRequest = { brandExpanded = false }, modifier = Modifier.background(cardBg)) {
                        DropdownMenuItem(
                            text = { Text("+ Agregar Nueva Marca", color = cyan, fontWeight = FontWeight.Bold) },
                            onClick = { brandExpanded = false; newName = ""; showNewBrandDialog = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        vm.brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand.name, color = textPrimary) },
                                onClick = { selectedBrand = brand; brandExpanded = false }
                            )
                        }
                    }
                }

                // Referencia
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it; localError = null },
                    label = { Text("Referencia / Modelo", color = textSecondary, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors,
                    singleLine = true
                )

                if (localError != null) {
                    Text(localError!!, color = Color(0xFFEF4444), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            if (vm.isSaving || vm.isLoadingTypes) {
                CircularProgressIndicator(color = cyan, modifier = Modifier.size(24.dp))
            } else {
                TextButton(
                    onClick = {
                        if (selectedType == null || selectedBrand == null || reference.trim().isBlank()) {
                            localError = "Debes seleccionar tipo, marca y escribir una referencia."
                        } else {
                            localError = null
                            vm.addCatalog(selectedType!!.id, selectedBrand!!.id, reference)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = cyan)
                ) {
                    Text("Guardar Modelo", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = textSecondary) }
        },
    )

    // Sub-diálogos para crear Tipo o Marca
    if (showNewTypeDialog) {
        NewItemDialog(
            title = "Nuevo Tipo de Equipo",
            placeholder = "Ej: Aire Acondicionado",
            initialName = newName,
            onDismiss = { showNewTypeDialog = false },
            onConfirm = { name -> vm.addEquipmentType(name); showNewTypeDialog = false },
            cardBg = cardBg, surfaceBg = surfaceBg, textPrimary = textPrimary, textSecondary = textSecondary, cyan = cyan
        )
    }

    if (showNewBrandDialog) {
        NewItemDialog(
            title = "Nueva Marca",
            placeholder = "Ej: Samsung",
            initialName = newName,
            onDismiss = { showNewBrandDialog = false },
            onConfirm = { name -> vm.addBrand(name); showNewBrandDialog = false },
            cardBg = cardBg, surfaceBg = surfaceBg, textPrimary = textPrimary, textSecondary = textSecondary, cyan = cyan
        )
    }
}

@Composable
private fun NewItemDialog(
    title: String, placeholder: String, initialName: String,
    onDismiss: () -> Unit, onConfirm: (String) -> Unit,
    cardBg: Color, surfaceBg: Color, textPrimary: Color, textSecondary: Color, cyan: Color
) {
    var name by remember { mutableStateOf(initialName) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        title = { Text(title, color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    placeholder = { Text(placeholder, color = textSecondary.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan,
                        unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg,
                        unfocusedTextColor = textPrimary, focusedTextColor = textPrimary
                    )
                )
                if (error != null) Text(error!!, color = Color(0xFFEF4444), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.trim().length < 2) error = "Mínimo 2 caracteres"
                    else onConfirm(name.trim())
                },
                colors = ButtonDefaults.textButtonColors(contentColor = cyan)
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = textSecondary) }
        }
    )
}
