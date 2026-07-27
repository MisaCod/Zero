package com.example.zero.ui.screens.supervisor

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.data.repository.UserWithProfile
import com.example.zero.ui.viewmodel.AuthViewModel
import com.example.zero.ui.viewmodel.UsersManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersManagementScreen(
    authViewModel: AuthViewModel = viewModel(),
    vm: UsersManagementViewModel = viewModel(),
) {
    val darkBg = Color(0xFF050E17); val cardBg = Color(0xFF0A2236); val surfaceBg = Color(0xFF0F2D47)
    val cyan = Color(0xFF00C8F0); val blue = Color(0xFF0B4F7A)
    val textPrimary = Color(0xFFF0F8FF); val textSecondary = Color(0xFF7BA9C4)

    var showAddDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserWithProfile?>(null) }
    var deleteConfirm by remember { mutableStateOf<UserWithProfile?>(null) }

    LaunchedEffect(Unit) { vm.loadUsers() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(vm.successMsg) { vm.successMsg?.let { snackbarHostState.showSnackbar(it); vm.clearSuccess() } }
    LaunchedEffect(vm.errorMsg) { vm.errorMsg?.let { snackbarHostState.showSnackbar("Error: $it"); vm.clearError() } }

    val filters = listOf("TODOS", "CLIENTE", "TÉCNICO", "SUPERVISOR")
    val roleColors = mapOf("CLIENTE" to cyan, "TÉCNICO" to Color(0xFF10B981), "SUPERVISOR" to Color(0xFFF59E0B))

    Scaffold(
        containerColor = darkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = blue, contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Filled.PersonAdd, null)
            }
        },
    ) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Group, null, tint = cyan, modifier = Modifier.size(22.dp))
                    Text("Gestión de Usuarios", color = textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Text("${vm.filteredUsers.size} usuarios · Toca para editar", color = textSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.searchCedula,
                    onValueChange = { vm.updateSearchCedula(it) },
                    placeholder = { Text("Buscar por cédula...", color = textSecondary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar", tint = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
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
            }

            // Filtros por rol
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEach { f ->
                        FilterChip(
                            selected = vm.activeFilter == f,
                            onClick = { vm.setFilter(f) },
                            label = { Text(f, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = blue.copy(alpha = 0.4f),
                                selectedLabelColor = cyan,
                                containerColor = cardBg,
                                labelColor = textSecondary,
                            ),
                        )
                    }
                }
            }

            if (vm.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cyan) } }
            } else if (vm.filteredUsers.isEmpty() && vm.searchCedula.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                        Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.SearchOff, null, tint = textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("La cédula no existe", color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Verifica la cédula e intenta de nuevo.", color = textSecondary, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(vm.filteredUsers, key = { it.user.id ?: it.user.email }) { uwp ->
                    val roleColor = roleColors[uwp.user.role] ?: textSecondary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        onClick = { editingUser = uwp },
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(46.dp).background(Brush.linearGradient(listOf(blue, roleColor.copy(alpha = 0.5f))), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) { Text(uwp.fullName.firstOrNull()?.uppercase() ?: "U", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(uwp.fullName.ifBlank { uwp.user.email }, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(uwp.user.email, color = textSecondary, fontSize = 12.sp)
                                Text("CI: ${uwp.identityCard}", color = textSecondary.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.background(roleColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text(uwp.user.role, color = roleColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                                IconButton(onClick = { deleteConfirm = uwp }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444).copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo agregar usuario
    if (showAddDialog) AddUserDialog(
        cardBg = cardBg, surfaceBg = surfaceBg, textPrimary = textPrimary, textSecondary = textSecondary, cyan = cyan,
        isSaving = vm.isSaving,
        onDismiss = { showAddDialog = false },
        onSave = { email, pw, role, name, lastName, ci, phone ->
            vm.createUser(email, pw, role, name, lastName, ci, phone)
            showAddDialog = false
        },
    )

    // Diálogo editar usuario
    editingUser?.let { uwp ->
        EditUserDialog(
            uwp = uwp, cardBg = cardBg, surfaceBg = surfaceBg, textPrimary = textPrimary, textSecondary = textSecondary, cyan = cyan,
            isSaving = vm.isSaving,
            onDismiss = { editingUser = null },
            onSave = { name, lastName, phone, role ->
                vm.updateUser(uwp.user.id ?: "", name, lastName, phone, role)
                editingUser = null
            },
        )
    }

    // Confirmar eliminar
    deleteConfirm?.let { uwp ->
        AlertDialog(
            onDismissRequest = { deleteConfirm = null },
            containerColor = cardBg,
            title = { Text("Eliminar Usuario", color = Color(0xFFF0F8FF), fontWeight = FontWeight.Bold) },
            text = { Text("¿Eliminar a ${uwp.fullName.ifBlank { uwp.user.email }}? Esta acción no se puede deshacer.", color = Color(0xFF7BA9C4)) },
            confirmButton = { TextButton(onClick = { vm.deleteUser(uwp.user.id ?: ""); deleteConfirm = null }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { deleteConfirm = null }) { Text("Cancelar", color = Color(0xFF7BA9C4)) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUserDialog(cardBg: Color, surfaceBg: Color, textPrimary: Color, textSecondary: Color, cyan: Color, isSaving: Boolean, onDismiss: () -> Unit, onSave: (String, String, String, String, String, String, String) -> Unit) {
    var email by remember { mutableStateOf("") }; var pw by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }; var lastName by remember { mutableStateOf("") }
    var ci by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("CLIENTE") }
    val roles = listOf("CLIENTE", "TÉCNICO", "SUPERVISOR"); var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = cardBg,
        title = { Text("Nuevo Usuario", color = textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.7f),
                    unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg,
                    unfocusedTextColor = textPrimary, focusedTextColor = textPrimary,
                )
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email *", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors, singleLine = true)
                OutlinedTextField(value = pw, onValueChange = { pw = it }, label = { Text("Contraseña *", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors, singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors, singleLine = true)
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellido", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors, singleLine = true)
                OutlinedTextField(value = ci, onValueChange = { ci = it.filter { c -> c.isDigit() } }, label = { Text("Cédula", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors, singleLine = true)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 11) phone = it.filter { c -> c.isDigit() } },
                    label = { Text("Teléfono (04121234567)", color = textSecondary, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = fieldColors, singleLine = true,
                    supportingText = { Text("${phone.length}/11 dígitos", color = textSecondary.copy(alpha = 0.6f), fontSize = 10.sp) },
                )
                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                    OutlinedTextField(value = role, onValueChange = {}, readOnly = true, label = { Text("Rol", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) }, shape = RoundedCornerShape(10.dp), colors = fieldColors)
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }, modifier = Modifier.background(cardBg)) {
                        roles.forEach { r -> DropdownMenuItem(text = { Text(r, color = textPrimary) }, onClick = { role = r; roleExpanded = false }) }
                    }
                }
            }
        },
        confirmButton = {
            if (isSaving) CircularProgressIndicator(color = cyan, modifier = Modifier.size(24.dp))
            else TextButton(onClick = { onSave(email, pw, role, name, lastName, ci, phone) }, colors = ButtonDefaults.textButtonColors(contentColor = cyan)) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = textSecondary) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditUserDialog(uwp: UserWithProfile, cardBg: Color, surfaceBg: Color, textPrimary: Color, textSecondary: Color, cyan: Color, isSaving: Boolean, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf(uwp.profile?.name ?: "") }
    var lastName by remember { mutableStateOf(uwp.profile?.lastName ?: "") }
    var phone by remember { mutableStateOf(uwp.profile?.phoneNumber ?: "") }
    var role by remember { mutableStateOf(uwp.user.role) }
    val roles = listOf("CLIENTE", "TÉCNICO", "SUPERVISOR"); var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = cardBg,
        title = { Text("Editar Usuario", color = textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(uwp.user.email, color = textSecondary, fontSize = 12.sp)
                listOf(name to "Nombre", lastName to "Apellido", phone to "Teléfono").forEachIndexed { i, (v, label) ->
                    val isPhone = i == 2
                    OutlinedTextField(
                        value = v,
                        onValueChange = { nv -> 
                            when(i) {
                                0 -> name = nv
                                1 -> lastName = nv
                                2 -> if (nv.length <= 11) phone = nv.filter { c -> c.isDigit() }
                            }
                        },
                        label = { Text(if (isPhone) "Teléfono (04121234567)" else label, color = textSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.7f), unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg, unfocusedTextColor = textPrimary, focusedTextColor = textPrimary),
                        supportingText = if (isPhone) { { Text("${phone.length}/11 dígitos", color = textSecondary.copy(alpha = 0.6f), fontSize = 10.sp) } } else null
                    )
                }
                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                    OutlinedTextField(value = role, onValueChange = {}, readOnly = true, label = { Text("Rol", color = textSecondary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) }, shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.12f), focusedBorderColor = cyan.copy(alpha = 0.7f), unfocusedContainerColor = surfaceBg, focusedContainerColor = surfaceBg, unfocusedTextColor = textPrimary, focusedTextColor = textPrimary))
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }, modifier = Modifier.background(cardBg)) {
                        roles.forEach { r -> DropdownMenuItem(text = { Text(r, color = textPrimary) }, onClick = { role = r; roleExpanded = false }) }
                    }
                }
            }
        },
        confirmButton = {
            if (isSaving) CircularProgressIndicator(color = cyan, modifier = Modifier.size(24.dp))
            else TextButton(onClick = { onSave(name, lastName, phone, role) }, colors = ButtonDefaults.textButtonColors(contentColor = cyan)) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = textSecondary) } },
    )
}
