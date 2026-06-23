package com.jkn.mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jkn.mobile.data.Role
import com.jkn.mobile.ui.components.BottomNavigationBar
import com.jkn.mobile.ui.screens.QueueScreen
import com.jkn.mobile.ui.screens.FaqScreen
import com.jkn.mobile.ui.screens.HomeScreen
import com.jkn.mobile.ui.screens.KartuScreen
import com.jkn.mobile.ui.screens.OperatorScreen
import com.jkn.mobile.ui.screens.ProfileScreen
import com.jkn.mobile.ui.screens.BeritaScreen
import com.jkn.mobile.ui.screens.AntreanFaskesPertamaScreen
import com.jkn.mobile.ui.screens.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object UserFlow : Screen("user_flow")
    object OperatorFlow : Screen("operator_flow")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = if (role == Role.OPERATOR) {
                        Screen.OperatorFlow.route
                    } else {
                        Screen.UserFlow.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.OperatorFlow.route) {
            OperatorScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserFlow.route) {
            PatientShellScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun PatientShellScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Show bottom bar only on root destinations
    val showBottomBar = currentRoute in listOf("home", "berita", "kartu", "faq", "profil")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        onNavigateToQueue = { navController.navigate("antrean/faskes_pertama") }
                    )
                }
                composable("berita") {
                    BeritaScreen()
                }
                composable("kartu") {
                    KartuScreen()
                }
                composable("faq") {
                    FaqScreen()
                }
                composable("profil") {
                    ProfileScreen(
                        onNavigateToFaq = { 
                            navController.navigate("faq") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable("queue") {
                    QueueScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("antrean/faskes_pertama") {
                    AntreanFaskesPertamaScreen(
                        onBack = { navController.popBackStack() },
                        onSimpan = { navController.navigate("queue") }
                    )
                }
            }
        }
    }
}
