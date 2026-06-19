package com.example.zero.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.zero.ui.components.ProfileBottomSheet
import com.example.zero.ui.components.ScannerBottomSheet
import com.example.zero.ui.components.TechFlowBottomBar
import com.example.zero.ui.components.TechFlowBrandTopBar
import com.example.zero.ui.components.TechFlowTopBar
import com.example.zero.ui.screens.alerts.AlertsScreen
import com.example.zero.ui.screens.dashboard.DashboardScreen
import com.example.zero.ui.screens.history.HistoryScreen
import com.example.zero.ui.screens.inventory.InventoryScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.ui.viewmodel.AuthViewModel
import com.example.zero.ui.viewmodel.ServiceRequestViewModel
import com.example.zero.data.model.UserRole
import androidx.compose.runtime.LaunchedEffect
import com.example.zero.ui.components.NotificacionBanner

// ============================================================================
// MainScreen — The primary scaffold with bottom navigation and inner NavHost.
// Hosts Dashboard, Inventory, History, and Alerts as tab destinations.
// ============================================================================

/** Route constants for bottom navigation destinations. */
object MainRoutes {
    const val WORK_ORDERS = "work_orders"
    const val EQUIPMENT = "equipment"
    const val HISTORY = "history"
    const val ALERTS = "alerts"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel = viewModel(),
    serviceRequestViewModel: ServiceRequestViewModel = viewModel()
) {
    val navController = rememberNavController()
    var currentRoute by rememberSaveable { mutableStateOf(MainRoutes.WORK_ORDERS) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showProfileModal by rememberSaveable { mutableStateOf(false) }
    var showScannerModal by rememberSaveable { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "D&S Refrigerantes",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("Configuración") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Outlined.Settings, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Soporte Técnico") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Outlined.HelpOutline, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.weight(1f))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            authViewModel.logout()
                        }
                    },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.ExitToApp,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(bottom = 16.dp)
                )
            }
        }
    ) {
        // Suscripción Realtime si es Técnico o Supervisor
        LaunchedEffect(authViewModel.userRole) {
            if (authViewModel.userRole == UserRole.TECNICO || authViewModel.userRole == UserRole.SUPERVISOR) {
                serviceRequestViewModel.iniciarRealtimeTecnico()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    val onNavClick = { scope.launch { drawerState.open() } }
                    val onProfClick = { showProfileModal = true }
                    when (currentRoute) {
                        MainRoutes.WORK_ORDERS -> TechFlowBrandTopBar(
                            title = "D&S Refrigerantes",
                            onProfileClick = onProfClick
                        )

                        MainRoutes.EQUIPMENT -> TechFlowTopBar(
                            title = "TechFlow",
                            onNavigationClick = { onNavClick() },
                            onProfileClick = onProfClick
                        )

                        MainRoutes.HISTORY -> TechFlowBrandTopBar(
                            title = "D&S Refrigerantes",
                            onProfileClick = onProfClick
                        )

                        MainRoutes.ALERTS -> TechFlowTopBar(
                            title = "D&S Refrigerantes",
                            onNavigationClick = { onNavClick() },
                            onProfileClick = onProfClick
                        )

                        else -> TechFlowBrandTopBar(onProfileClick = onProfClick)
                    }
                },
                bottomBar = {
                    TechFlowBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (route != currentRoute) {
                                currentRoute = route
                                navController.navigate(route) {
                                    // Pop up to the start destination to avoid stacking
                                    popUpTo(MainRoutes.WORK_ORDERS) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    )
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = MainRoutes.WORK_ORDERS,
                ) {
                    composable(MainRoutes.WORK_ORDERS) {
                        DashboardScreen(
                            contentPadding = innerPadding,
                            onScannerClick = { showScannerModal = true },
                            authViewModel = authViewModel,
                            serviceRequestViewModel = serviceRequestViewModel
                        )
                    }
                    composable(MainRoutes.EQUIPMENT) {
                        InventoryScreen(
                            contentPadding = innerPadding,
                            onScannerClick = { showScannerModal = true }
                        )
                    }
                    composable(MainRoutes.HISTORY) {
                        HistoryScreen(contentPadding = innerPadding)
                    }
                    composable(MainRoutes.ALERTS) {
                        AlertsScreen(contentPadding = innerPadding)
                    }
                }

                // Banner de notificación (aparece encima del Scaffold)
                NotificacionBanner(
                    solicitud = serviceRequestViewModel.nuevaSolicitudNotificacion,
                    visible = serviceRequestViewModel.mostrarBannerNotificacion,
                    onDismiss = { serviceRequestViewModel.ocultarBannerNotificacion() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }

        if (showProfileModal) {
            ProfileBottomSheet(onDismiss = { showProfileModal = false })
        }

        if (showScannerModal) {
            ScannerBottomSheet(onDismiss = { showScannerModal = false })
        }
    }
}
