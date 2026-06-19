package com.example.zero.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zero.ui.theme.TechFlowTheme

// ============================================================================
// TechFlowBottomBar
// Bottom navigation bar with 4 destinations matching the HTML mockups.
//
// Tabs observed across 1.html, 3.html, 4.html, 5.html:
//   1. Work Orders / Órdenes  → assignment
//   2. Equipment / Equipos    → precision_manufacturing
//   3. History / Historial    → history / manage_history
//   4. Alerts / Alertas       → notifications / notifications_active
//
// The selected item uses the primary indicator style from MD3 NavigationBar.
// ============================================================================

/**
 * Represents a single navigation destination in the bottom bar.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasBadge: Boolean = false,
)

/** Default navigation items matching the HTML mockup nav elements. */
val TechFlowNavItems = listOf(
    BottomNavItem(
        route = "work_orders",
        label = "Órdenes",
        selectedIcon = Icons.AutoMirrored.Filled.Assignment,
        unselectedIcon = Icons.AutoMirrored.Outlined.Assignment,
    ),
    BottomNavItem(
        route = "equipment",
        label = "Equipos",
        selectedIcon = Icons.Filled.PrecisionManufacturing,
        unselectedIcon = Icons.Outlined.PrecisionManufacturing,
    ),
    BottomNavItem(
        route = "history",
        label = "Historial",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
    ),
    BottomNavItem(
        route = "alerts",
        label = "Alertas",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications,
        hasBadge = true,
    ),
)

/**
 * Bottom navigation bar for the TechFlow application.
 *
 * @param currentRoute The currently selected route (matches [BottomNavItem.route]).
 * @param onNavigate Callback invoked with the destination route when a tab is tapped.
 * @param items The list of navigation items. Defaults to [TechFlowNavItems].
 */
@Composable
fun TechFlowBottomBar(
    currentRoute: String = "work_orders",
    onNavigate: (String) -> Unit = {},
    items: List<BottomNavItem> = TechFlowNavItems,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            val isSelected = item.route == currentRoute

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    if (item.hasBadge) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(8.dp),
                                    content = {},
                                )
                            },
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

// region Previews
@Preview(showBackground = true)
@Composable
private fun TechFlowBottomBarPreview_WorkOrders() {
    TechFlowTheme(darkTheme = false) {
        TechFlowBottomBar(currentRoute = "work_orders")
    }
}

@Preview(showBackground = true)
@Composable
private fun TechFlowBottomBarPreview_Equipment() {
    TechFlowTheme(darkTheme = false) {
        TechFlowBottomBar(currentRoute = "equipment")
    }
}

@Preview(showBackground = true)
@Composable
private fun TechFlowBottomBarPreview_Alerts() {
    TechFlowTheme(darkTheme = false) {
        TechFlowBottomBar(currentRoute = "alerts")
    }
}
// endregion
