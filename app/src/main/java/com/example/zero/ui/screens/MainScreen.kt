package com.example.zero.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.zero.data.model.ServiceRequestWithEquipment
import com.example.zero.data.model.UserRole
import com.example.zero.ui.components.NotificacionBanner
import com.example.zero.ui.screens.alerts.AlertsScreen
import com.example.zero.ui.screens.dashboard.DashboardScreen
import com.example.zero.ui.screens.detail.ServiceRequestDetailScreen
import com.example.zero.ui.screens.equipment.EquipmentManagementScreen
import com.example.zero.ui.screens.equipment.SparePartsScreen
import com.example.zero.ui.screens.history.HistoryScreen
import com.example.zero.ui.screens.rating.ServiceRatingScreen
import com.example.zero.ui.screens.reports.ReportsScreen
import com.example.zero.ui.screens.request.NewServiceRequestScreen
import com.example.zero.ui.screens.supervisor.AssignTechnicianScreen
import com.example.zero.ui.screens.supervisor.BackupExportScreen
import com.example.zero.ui.screens.supervisor.UsersManagementScreen
import com.example.zero.ui.screens.technician.TechnicianAvailabilityScreen
import com.example.zero.ui.viewmodel.AuthViewModel
import com.example.zero.ui.viewmodel.ServiceRequestViewModel
import kotlinx.coroutines.launch

// ============================================================================
// MainScreen — Navegación completa por roles según doc 0 Grados
// Roles: CLIENTE / TÉCNICO / SUPERVISOR
// Cada rol ve un conjunto diferente de tabs en el bottom bar
// ============================================================================

object AppRoutes {
    // Comunes
    const val DASHBOARD = "dashboard"
    const val ALERTS = "alerts"
    const val PROFILE = "profile"

    // Solicitudes
    const val SERVICE_LIST = "service_list"
    const val SERVICE_DETAIL = "service_detail/{requestId}"
    const val NEW_REQUEST = "new_request"
    const val RATE_SERVICE = "rate_service/{assigmentId}"

    // Equipos
    const val CLIENT_EQUIPMENT = "client_equipment"
    const val EQUIPMENT_MGMT = "equipment_mgmt"

    // Repuestos
    const val SPARE_PARTS = "spare_parts"

    // Técnico
    const val TECHNICIAN_PANEL = "technician_panel"
    const val TECHNICAL_REPORT = "technical_report/{assigmentId}"

    // Supervisor
    const val ASSIGN_TECHNICIAN = "assign_technician/{requestId}"
    const val USERS_MGMT = "users_mgmt"
    const val REPORTS = "reports"
    const val BACKUP = "backup"

    // Historial
    const val HISTORY = "history"

    fun serviceDetail(id: String) = "service_detail/$id"
    fun rateService(assigmentId: String) = "rate_service/$assigmentId"
    fun technicalReport(assigmentId: String) = "technical_report/$assigmentId"
    fun assignTechnician(requestId: String) = "assign_technician/$requestId"
}

// Ítem de tab del bottom bar
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel = viewModel(),
    serviceRequestViewModel: ServiceRequestViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: AppRoutes.DASHBOARD

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val userRole = authViewModel.userRole
    val profile = authViewModel.profile

    // ── Tabs por rol ──────────────────────────────────────────────────────
    val bottomTabs = remember(userRole) {
        when (userRole) {
            UserRole.CLIENTE -> listOf(
                BottomNavItem(AppRoutes.DASHBOARD, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
                BottomNavItem(AppRoutes.SERVICE_LIST, "Servicios", Icons.Outlined.Build, Icons.Filled.Build),
                BottomNavItem(AppRoutes.CLIENT_EQUIPMENT, "Equipos", Icons.Outlined.Memory, Icons.Filled.Memory),
                BottomNavItem(AppRoutes.SPARE_PARTS, "Repuestos", Icons.Outlined.Inventory2, Icons.Filled.Inventory2),
            )
            UserRole.TECNICO -> listOf(
                BottomNavItem(AppRoutes.DASHBOARD, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
                BottomNavItem(AppRoutes.SERVICE_LIST, "Mis Trabajos", Icons.Outlined.Build, Icons.Filled.Build),
                BottomNavItem(AppRoutes.TECHNICIAN_PANEL, "Mi Panel", Icons.Outlined.Person, Icons.Filled.Person),
                BottomNavItem(AppRoutes.ALERTS, "Alertas", Icons.Outlined.Notifications, Icons.Filled.Notifications),
            )
            UserRole.SUPERVISOR -> listOf(
                BottomNavItem(AppRoutes.DASHBOARD, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
                BottomNavItem(AppRoutes.SERVICE_LIST, "Servicios", Icons.Outlined.Build, Icons.Filled.Build),
                BottomNavItem(AppRoutes.REPORTS, "Reportes", Icons.Outlined.BarChart, Icons.Filled.BarChart),
                BottomNavItem(AppRoutes.USERS_MGMT, "Usuarios", Icons.Outlined.Group, Icons.Filled.Group),
                BottomNavItem(AppRoutes.EQUIPMENT_MGMT, "Equipos", Icons.Outlined.Settings, Icons.Filled.Settings),
            )
        }
    }

    // Realtime para técnicos y supervisores
    LaunchedEffect(userRole) {
        if (userRole == UserRole.TECNICO || userRole == UserRole.SUPERVISOR) {
            serviceRequestViewModel.iniciarRealtimeTecnico()
        }
    }

    val darkBg = Color(0xFF050E17)
    val cardBg = Color(0xFF081624)
    val cyan = Color(0xFF00C8F0)
    val blue = Color(0xFF1A8FBF)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF081624),
                drawerContentColor = Color.White,
            ) {
                // Header con perfil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color(0xFF0B4F7A), Color(0xFF081624))))
                        .padding(20.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Brush.linearGradient(listOf(blue, cyan)), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = profile?.name?.firstOrNull()?.toString() ?: "?",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column {
                            Text(
                                text = "${profile?.name ?: ""} ${profile?.lastName ?: ""}".trim().ifBlank { "Usuario" },
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            )
                            Text(
                                text = userRole.value,
                                color = cyan.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Opciones del drawer
                if (userRole == UserRole.SUPERVISOR) {
                    DrawerItem(
                        label = "Backup & Exportar",
                        icon = Icons.Outlined.Backup,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(AppRoutes.BACKUP)
                        },
                        cyan = cyan,
                    )
                }
                DrawerItem(
                    label = "Repuestos",
                    icon = Icons.Outlined.Inventory2,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(AppRoutes.SPARE_PARTS)
                    },
                    cyan = cyan,
                )
                DrawerItem(
                    label = "Alertas del Sistema",
                    icon = Icons.Outlined.Notifications,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(AppRoutes.ALERTS)
                    },
                    cyan = cyan,
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                NavigationDrawerItem(
                    label = {
                        Text("Cerrar Sesión", color = Color(0xFFFF6B6B))
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                    },
                    icon = {
                        Icon(Icons.AutoMirrored.Outlined.ExitToApp, null, tint = Color(0xFFFF6B6B))
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).padding(bottom = 16.dp),
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(darkBg)) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = darkBg,
                topBar = {
                    // TopBar dinámico
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.AcUnit, null, tint = cyan, modifier = Modifier.size(22.dp))
                                Text("0 Grados", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Outlined.Menu, null, tint = Color.White)
                            }
                        },
                        actions = {
                            // FAB contextual por rol en el top bar
                            if (userRole == UserRole.CLIENTE && currentRoute == AppRoutes.SERVICE_LIST) {
                                IconButton(onClick = { navController.navigate(AppRoutes.NEW_REQUEST) }) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Brush.linearGradient(listOf(blue, cyan)), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            // Avatar perfil
                            IconButton(onClick = { navController.navigate(AppRoutes.PROFILE) }) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Brush.linearGradient(listOf(blue, cyan)), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = profile?.name?.firstOrNull()?.toString() ?: "U",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = cardBg,
                            titleContentColor = Color.White,
                        ),
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = cardBg,
                        contentColor = Color.White,
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(AppRoutes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (selected) cyan else Color.White.copy(alpha = 0.4f),
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        fontSize = 10.sp,
                                        color = if (selected) cyan else Color.White.copy(alpha = 0.4f),
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = blue.copy(alpha = 0.2f),
                                ),
                            )
                        }
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.DASHBOARD,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    // ── Dashboard (todos) ──────────────────────────────────────
                    composable(AppRoutes.DASHBOARD) {
                        DashboardScreen(
                            contentPadding = PaddingValues(0.dp),
                            onScannerClick = {},
                            onSolicitudClick = { solicitud ->
                                solicitud.id?.let {
                                    navController.navigate(AppRoutes.serviceDetail(it))
                                }
                            },
                            authViewModel = authViewModel,
                            serviceRequestViewModel = serviceRequestViewModel,
                        )
                    }

                    // ── Lista de solicitudes (todos) ──────────────────────────
                    composable(AppRoutes.SERVICE_LIST) {
                        DashboardScreen(
                            contentPadding = PaddingValues(0.dp),
                            onScannerClick = {},
                            onSolicitudClick = { solicitud ->
                                solicitud.id?.let {
                                    navController.navigate(AppRoutes.serviceDetail(it))
                                }
                            },
                            authViewModel = authViewModel,
                            serviceRequestViewModel = serviceRequestViewModel,
                        )
                    }

                    // ── Detalle de solicitud ──────────────────────────────────
                    composable(
                        route = AppRoutes.SERVICE_DETAIL,
                        arguments = listOf(navArgument("requestId") { type = NavType.StringType }),
                    ) { backStack ->
                        val requestId = backStack.arguments?.getString("requestId") ?: ""
                        val solicitud = serviceRequestViewModel.solicitudes.find { it.id == requestId }
                        if (solicitud != null) {
                            ServiceRequestDetailScreen(
                                solicitud = solicitud,
                                onBack = { navController.popBackStack() },
                                authViewModel = authViewModel,
                                serviceRequestViewModel = serviceRequestViewModel,
                                onAssignTechnician = { navController.navigate(AppRoutes.assignTechnician(requestId)) },
                                onFillReport = { assigmentId -> navController.navigate(AppRoutes.technicalReport(assigmentId)) },
                                onRateService = { assigmentId -> navController.navigate(AppRoutes.rateService(assigmentId)) },
                            )
                        }
                    }

                    // ── Nueva solicitud (CLIENTE) ─────────────────────────────
                    composable(AppRoutes.NEW_REQUEST) {
                        NewServiceRequestScreen(
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() },
                            onSuccess = {
                                serviceRequestViewModel.cargarSolicitudes()
                                navController.popBackStack()
                            },
                        )
                    }

                    // ── Calificar servicio (CLIENTE) ──────────────────────────
                    composable(
                        route = AppRoutes.RATE_SERVICE,
                        arguments = listOf(navArgument("assigmentId") { type = NavType.StringType }),
                    ) { backStack ->
                        val assigmentId = backStack.arguments?.getString("assigmentId") ?: ""
                        ServiceRatingScreen(
                            assigmentId = assigmentId,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    // ── Reporte técnico (TÉCNICO) ────────────────────────────
                    composable(
                        route = AppRoutes.TECHNICAL_REPORT,
                        arguments = listOf(navArgument("assigmentId") { type = NavType.StringType }),
                    ) { backStack ->
                        val assigmentId = backStack.arguments?.getString("assigmentId") ?: ""
                        com.example.zero.ui.screens.report.TechnicalReportScreen(
                            assigmentId = assigmentId,
                            onBack = { navController.popBackStack() },
                            onSuccess = {
                                serviceRequestViewModel.cargarSolicitudes()
                                navController.popBackStack()
                            },
                        )
                    }

                    // ── Panel del técnico ────────────────────────────────────
                    composable(AppRoutes.TECHNICIAN_PANEL) {
                        TechnicianAvailabilityScreen(
                            authViewModel = authViewModel,
                            onSolicitudClick = { id ->
                                navController.navigate(AppRoutes.serviceDetail(id))
                            },
                        )
                    }

                    // ── Asignar técnico (SUPERVISOR) ──────────────────────────
                    composable(
                        route = AppRoutes.ASSIGN_TECHNICIAN,
                        arguments = listOf(navArgument("requestId") { type = NavType.StringType }),
                    ) { backStack ->
                        val requestId = backStack.arguments?.getString("requestId") ?: ""
                        AssignTechnicianScreen(
                            serviceRequestId = requestId,
                            onBack = { navController.popBackStack() },
                            onAssigned = {
                                serviceRequestViewModel.cargarSolicitudes()
                                navController.popBackStack()
                            },
                        )
                    }

                    // ── Gestión de usuarios (SUPERVISOR) ─────────────────────
                    composable(AppRoutes.USERS_MGMT) {
                        UsersManagementScreen(authViewModel = authViewModel)
                    }

                    // ── Reportes (SUPERVISOR/TÉCNICO) ─────────────────────────
                    composable(AppRoutes.REPORTS) {
                        ReportsScreen(authViewModel = authViewModel)
                    }

                    // ── Gestión de equipos (SUPERVISOR) ──────────────────────
                    composable(AppRoutes.EQUIPMENT_MGMT) {
                        EquipmentManagementScreen(authViewModel = authViewModel, isSupervisor = true)
                    }

                    // ── Equipos del cliente (CLIENTE) ─────────────────────────
                    composable(AppRoutes.CLIENT_EQUIPMENT) {
                        EquipmentManagementScreen(authViewModel = authViewModel, isSupervisor = false)
                    }

                    // ── Repuestos ─────────────────────────────────────────────
                    composable(AppRoutes.SPARE_PARTS) {
                        SparePartsScreen(authViewModel = authViewModel)
                    }

                    // ── Alertas ───────────────────────────────────────────────
                    composable(AppRoutes.ALERTS) {
                        AlertsScreen(contentPadding = PaddingValues(0.dp))
                    }

                    // ── Historial ─────────────────────────────────────────────
                    composable(AppRoutes.HISTORY) {
                        HistoryScreen(contentPadding = PaddingValues(0.dp))
                    }

                    // ── Backup (SUPERVISOR) ───────────────────────────────────
                    composable(AppRoutes.BACKUP) {
                        BackupExportScreen(onBack = { navController.popBackStack() })
                    }

                    // ── Perfil ────────────────────────────────────────────────
                    composable(AppRoutes.PROFILE) {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }

                // Banner Realtime
                NotificacionBanner(
                    solicitud = serviceRequestViewModel.nuevaSolicitudNotificacion,
                    visible = serviceRequestViewModel.mostrarBannerNotificacion,
                    onDismiss = { serviceRequestViewModel.ocultarBannerNotificacion() },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}

// ── Drawer item helper ────────────────────────────────────────────────────
@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    cyan: Color,
) {
    NavigationDrawerItem(
        label = { Text(label, color = Color.White) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, null, tint = cyan.copy(alpha = 0.8f)) },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
        ),
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    )
}
