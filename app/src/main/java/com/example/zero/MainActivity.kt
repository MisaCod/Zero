package com.example.zero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zero.ui.screens.MainScreen
import com.example.zero.ui.screens.login.LoginScreen
import com.example.zero.ui.theme.TechFlowTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.ui.viewmodel.AuthViewModel
import com.example.zero.ui.viewmodel.ServiceRequestViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TechFlowTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val serviceRequestViewModel: ServiceRequestViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = if (authViewModel.isLoggedIn) "main" else "login",
                ) {
                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                        )
                    }
                    composable("main") {
                        MainScreen(
                            authViewModel = authViewModel,
                            serviceRequestViewModel = serviceRequestViewModel
                        )
                    }
                }
            }
        }
    }
}