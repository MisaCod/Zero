package com.example.zero.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.model.Profile
import com.example.zero.data.model.UserRole
import com.example.zero.data.repository.AuthRepository
import kotlinx.coroutines.launch

// ============================================================================
// AuthViewModel — Gestiona el estado de autenticación y el rol del usuario.
//
// Estados:
//   • isLoading:  true mientras se procesa el login
//   • isLoggedIn: true cuando hay sesión activa
//   • errorMsg:   mensaje de error (null si no hay error)
//   • userRole:   rol del usuario autenticado
//   • profile:    perfil completo del usuario
// ============================================================================

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // ── Estado observable ────────────────────────────────────────────────
    var isLoading by mutableStateOf(false)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    var errorMsg by mutableStateOf<String?>(null)
        private set

    var userRole by mutableStateOf(UserRole.CLIENTE)
        private set

    var profile by mutableStateOf<Profile?>(null)
        private set

    // ── Inicializar: verificar si ya hay sesión ──────────────────────────
    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        if (authRepository.isLoggedIn()) {
            isLoggedIn = true
            viewModelScope.launch {
                loadUserProfile()
            }
        }
    }

    // ── Login ────────────────────────────────────────────────────────────
    /**
     * Inicia sesión con email y contraseña.
     * Al completarse, carga el perfil del usuario para obtener el rol.
     *
     * @param onSuccess callback que se ejecuta al autenticarse exitosamente
     */
    fun login(email: String, password: String, onSuccess: () -> Unit = {}) {
        // Validación básica
        if (email.isBlank() || password.isBlank()) {
            errorMsg = "Por favor ingresa email y contraseña"
            return
        }

        errorMsg = null
        isLoading = true

        viewModelScope.launch {
            val result = authRepository.signIn(email.trim(), password)

            result.fold(
                onSuccess = { user ->
                    isLoggedIn = true
                    // Obtener rol directamente del usuario autenticado
                    userRole = UserRole.fromString(user.role)
                    loadUserProfile()
                    isLoading = false
                    onSuccess()
                },
                onFailure = { e ->
                    isLoading = false
                    errorMsg = e.message ?: "Error de conexión. Intenta de nuevo"
                },
            )
        }
    }

    // ── Logout ───────────────────────────────────────────────────────────
    fun logout(onComplete: () -> Unit = {}) {
        authRepository.signOut()
        isLoggedIn = false
        profile = null
        userRole = UserRole.CLIENTE
        errorMsg = null
        onComplete()
    }

    // ── Cargar perfil ────────────────────────────────────────────────────
    private suspend fun loadUserProfile() {
        authRepository.getUserProfile().onSuccess { p ->
            profile = p
            // El rol ya está seteado desde el login (tabla users)
            // Solo actualizamos si no lo teníamos
            if (userRole == UserRole.CLIENTE) {
                userRole = authRepository.getUserRole()
            }
        }
    }

    // ── Limpiar error ────────────────────────────────────────────────────
    fun clearError() {
        errorMsg = null
    }

    // ── Helper: obtener user ID ──────────────────────────────────────────
    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()
}
