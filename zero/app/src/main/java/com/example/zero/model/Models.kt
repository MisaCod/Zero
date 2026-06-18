package com.example.zero.model

import androidx.compose.ui.graphics.vector.ImageVector

// ============================================================================
// Shared data models for TechFlow
// Used across Dashboard, Inventory, History, and Alerts screens.
// ============================================================================

// region Work Orders
data class WorkOrder(
    val id: String,
    val code: String,
    val title: String,
    val location: String,
    val time: String,
    val priority: Priority,
    val icon: ImageVector,
    val imageUrl: String = "",
)

enum class Priority { HIGH, MEDIUM, LOW }
// endregion

// region Alerts
data class Alert(
    val id: String,
    val title: String,
    val subtitle: String,
    val severity: AlertSeverity,
    val icon: ImageVector,
)

enum class AlertSeverity { CRITICAL, WARNING, INFO }
// endregion

// region Equipment
data class Equipment(
    val id: String,
    val name: String,
    val location: String,
    val status: EquipmentStatus,
    val maintenanceLabel: String,
    val imageUrl: String,
    val icon: ImageVector,
)

enum class EquipmentStatus { OPERATIVE, REPAIR, CRITICAL }
// endregion
