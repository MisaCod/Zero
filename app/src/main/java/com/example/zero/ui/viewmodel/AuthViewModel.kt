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
                onSuccess = {
                    isLoggedIn = true
                    loadUserProfile()
                    isLoading = false
                    onSuccess()
                },
                onFailure = { e ->
                    isLoading = false
                    errorMsg = when {
                        e.message?.contains("Invalid login", ignoreCase = true) == true ->
                            "Email o contraseña incorrectos"
                        e.message?.contains("Email not confirmed", ignoreCase = true) == true ->
                            "Email no confirmado. Revisa tu bandeja de entrada"
                        else ->
                            "Error de conexión: ${e.localizedMessage ?: "Intenta de nuevo"}"
                    }
                },
            )
        }
    }

    // ── Logout ───────────────────────────────────────────────────────────
    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.signOut()
            isLoggedIn = false
            profile = null
            userRole = UserRole.CLIENTE
            errorMsg = null
            onComplete()
        }
    }

    // ── Cargar perfil ────────────────────────────────────────────────────
    private suspend fun loadUserProfile() {
        val result = authRepository.getUserProfile()
        result.fold(
            onSuccess = { p ->
                profile = p
                userRole = UserRole.fromString(p.role)
            },
            onFailure = {
                // Si falla cargar el perfil, mantenemos rol por defecto
                profile = null
                userRole = UserRole.CLIENTE
            },
        )
    }

    // ── Limpiar error ────────────────────────────────────────────────────
    fun clearError() {
        errorMsg = null
    }

    // ── Helper: obtener user ID ──────────────────────────────────────────
    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()
}
