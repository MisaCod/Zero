package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.repository.SupervisorRepository
import com.example.zero.data.repository.UserWithProfile
import kotlinx.coroutines.launch

// ============================================================================
// UsersManagementViewModel — Estado para UsersManagementScreen.
//
// CRUD completo de usuarios via authClient (service_role).
// Filtra por rol en memoria para performance.
// ============================================================================

class UsersManagementViewModel : ViewModel() {

    private val repository = SupervisorRepository()

    // ── Lista principal (todos los usuarios) ──────────────────────────────
    val allUsers = mutableStateListOf<UserWithProfile>()

    // ── UI States ─────────────────────────────────────────────────────────
    var isLoading by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    var successMsg by mutableStateOf<String?>(null)
        private set

    // ── Filtro activo ─────────────────────────────────────────────────────
    var activeFilter by mutableStateOf("TODOS")
        private set

    val filteredUsers: List<UserWithProfile>
        get() = if (activeFilter == "TODOS") allUsers.toList()
                else allUsers.filter { it.user.role == activeFilter }

    // ── Cargar todos los usuarios ─────────────────────────────────────────
    fun loadUsers() {
        isLoading = true
        errorMsg = null
        viewModelScope.launch {
            repository.getAllUsersWithProfiles().fold(
                onSuccess = { list ->
                    allUsers.clear()
                    allUsers.addAll(list)
                },
                onFailure = { e ->
                    errorMsg = "Error al cargar usuarios: ${e.localizedMessage}"
                }
            )
            isLoading = false
        }
    }

    // ── Cambiar filtro ────────────────────────────────────────────────────
    fun setFilter(filter: String) {
        activeFilter = filter
    }

    // ── Crear usuario completo ────────────────────────────────────────────
    fun createUser(
        email: String,
        password: String,
        role: String,
        name: String,
        lastName: String,
        identityCard: String,
        phoneNumber: String,
    ) {
        if (email.isBlank() || password.isBlank()) {
            errorMsg = "Email y contraseña son obligatorios"
            return
        }
        isSaving = true
        errorMsg = null
        viewModelScope.launch {
            // 1. Crear en users
            repository.createUser(email, password, role).fold(
                onSuccess = { createdUser ->
                    val userId = createdUser.id ?: ""
                    // 2. Crear perfil
                    repository.createProfile(userId, name, lastName, identityCard, phoneNumber)
                    // 3. Si es técnico, crear technician_details
                    if (role == "TÉCNICO") {
                        repository.createTechnicianDetails(userId)
                    }
                    successMsg = "Usuario creado exitosamente"
                    loadUsers()
                },
                onFailure = { e ->
                    errorMsg = "Error al crear usuario: ${e.localizedMessage}"
                }
            )
            isSaving = false
        }
    }

    // ── Editar usuario ────────────────────────────────────────────────────
    fun updateUser(
        userId: String,
        name: String,
        lastName: String,
        phoneNumber: String,
        role: String,
    ) {
        isSaving = true
        errorMsg = null
        viewModelScope.launch {
            repository.updateUserProfile(userId, name, lastName, phoneNumber, role).fold(
                onSuccess = {
                    successMsg = "Usuario actualizado"
                    loadUsers()
                },
                onFailure = { e ->
                    errorMsg = "Error al actualizar: ${e.localizedMessage}"
                }
            )
            isSaving = false
        }
    }

    // ── Eliminar usuario ──────────────────────────────────────────────────
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId).fold(
                onSuccess = {
                    allUsers.removeAll { it.user.id == userId }
                    successMsg = "Usuario eliminado"
                },
                onFailure = { e ->
                    errorMsg = "Error al eliminar: ${e.localizedMessage}"
                }
            )
        }
    }

    fun clearError() { errorMsg = null }
    fun clearSuccess() { successMsg = null }
}
