package com.example.zero.ui.screens.technician

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.model.Assignment
import com.example.zero.data.model.TechnicianDetails
import com.example.zero.data.repository.TechnicianRepository
import kotlinx.coroutines.launch

class TechnicianPanelViewModel(
    private val repo: TechnicianRepository = TechnicianRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var errorMsg by mutableStateOf<String?>(null)
    var details by mutableStateOf<TechnicianDetails?>(null)
    var assignments = mutableStateListOf<Assignment>()
    var reports = mutableStateListOf<com.example.zero.data.model.TechnicalReport>()

    fun load(userId: String) {
        isLoading = true
        viewModelScope.launch {
            repo.getDetails(userId).onSuccess { details = it }
            repo.getAssignmentsByTech(userId).onSuccess { list ->
                assignments.clear()
                assignments.addAll(list)

                val reportRepo = com.example.zero.data.repository.TechnicalReportRepository()
                reports.clear()
                list.forEach { assign ->
                    assign.id?.let { assignId ->
                        reportRepo.getByAssignment(assignId).onSuccess { report ->
                            if (report != null) reports.add(report)
                        }
                    }
                }
            }
            isLoading = false
        }
    }

    fun toggleAvailability(userId: String) {
        val current = details?.availability ?: false
        isSaving = true
        viewModelScope.launch {
            repo.updateAvailability(userId, !current).onSuccess {
                details = details?.copy(availability = !current)
            }.onFailure { e ->
                errorMsg = "Error: ${e.localizedMessage}"
            }
            isSaving = false
        }
    }

    fun clearError() { errorMsg = null }
}
