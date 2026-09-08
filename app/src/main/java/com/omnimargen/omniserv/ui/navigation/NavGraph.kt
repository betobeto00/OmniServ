package com.omnimargen.omniserv.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.omnimargen.omniserv.ui.screens.clients.ClientFormScreen
import com.omnimargen.omniserv.ui.screens.clients.ClientListScreen
import com.omnimargen.omniserv.ui.screens.history.HistoryScreen
import com.omnimargen.omniserv.ui.screens.home.HomeScreen
import com.omnimargen.omniserv.ui.screens.legal.LegalScreen
import com.omnimargen.omniserv.ui.screens.operators.OperatorFormScreen
import com.omnimargen.omniserv.ui.screens.operators.OperatorListScreen
import com.omnimargen.omniserv.ui.screens.serviceTypes.ServiceTypeFormScreen
import com.omnimargen.omniserv.ui.screens.serviceTypes.ServiceTypeListScreen
import com.omnimargen.omniserv.ui.screens.services.ServiceDetailScreen
import com.omnimargen.omniserv.ui.screens.services.ServiceFormScreen
import com.omnimargen.omniserv.ui.screens.services.ServiceListScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Inicio", Icons.Default.Home)
    data object Clients : Screen("clients", "Clientes", Icons.Default.Person)
    data object Services : Screen("services", "Servicios", Icons.AutoMirrored.Filled.List)
    data object Operators : Screen("operators", "Operarios", Icons.Default.Star)
    data object History : Screen("history", "Historial", Icons.Default.DateRange)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Clients,
    Screen.Services,
    Screen.Operators,
    Screen.History
)

@Composable
fun OmniServNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToServiceForm = {
                        navController.navigate("service_form/-1")
                    },
                    onNavigateToLegal = {
                        navController.navigate("legal")
                    }
                )
            }

            composable(Screen.Clients.route) {
                ClientListScreen(
                    onNavigateToForm = { clientId ->
                        navController.navigate("client_form/${clientId ?: -1}")
                    }
                )
            }

            composable(
                "client_form/{clientId}",
                arguments = listOf(navArgument("clientId") { type = NavType.LongType })
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getLong("clientId")
                ClientFormScreen(
                    clientId = if (clientId == -1L) null else clientId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Services.route) {
                ServiceListScreen(
                    onNavigateToForm = { serviceId ->
                        navController.navigate("service_form/${serviceId ?: -1}")
                    },
                    onNavigateToDetail = { serviceId ->
                        navController.navigate("service_detail/$serviceId")
                    }
                )
            }

            composable(
                "service_form/{serviceId}",
                arguments = listOf(navArgument("serviceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getLong("serviceId")
                ServiceFormScreen(
                    serviceId = if (serviceId == -1L) null else serviceId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToServiceTypes = {
                        navController.navigate("service_types")
                    }
                )
            }

            composable(
                "service_detail/{serviceId}",
                arguments = listOf(navArgument("serviceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getLong("serviceId") ?: return@composable
                ServiceDetailScreen(
                    serviceId = serviceId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Operators.route) {
                OperatorListScreen(
                    onNavigateToForm = { operatorId ->
                        navController.navigate("operator_form/${operatorId ?: -1}")
                    }
                )
            }

            composable(
                "operator_form/{operatorId}",
                arguments = listOf(navArgument("operatorId") { type = NavType.LongType })
            ) { backStackEntry ->
                val operatorId = backStackEntry.arguments?.getLong("operatorId")
                OperatorFormScreen(
                    operatorId = if (operatorId == -1L) null else operatorId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("service_types") {
                ServiceTypeListScreen(
                    onNavigateToForm = { typeId ->
                        navController.navigate("service_type_form/${typeId ?: -1}")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                "service_type_form/{typeId}",
                arguments = listOf(navArgument("typeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val typeId = backStackEntry.arguments?.getLong("typeId")
                ServiceTypeFormScreen(
                    serviceTypeId = if (typeId == -1L) null else typeId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen()
            }

            composable("legal") {
                LegalScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
