package com.abdulwaheed.smartelectricitypredictor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abdulwaheed.smartelectricitypredictor.features.auth.ui.LoginScreen
import com.abdulwaheed.smartelectricitypredictor.features.auth.ui.RegisterScreen
import com.abdulwaheed.smartelectricitypredictor.features.auth.ui.SplashScreen
import com.abdulwaheed.smartelectricitypredictor.features.home.ui.HomeScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startFirebaseSignIn: () -> Unit = {}
) {
    NavHost(navController = navController, startDestination = NavDest.Splash.route, modifier = modifier) {
        composable(NavDest.Splash.route) {
            val vm: com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel = viewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.checkAuthAndNavigate() }
            // Splash just shows loading while check runs
            SplashScreen(onCheckAuth = { /* handled by LaunchedEffect */ })
            // react to navigation events from ViewModel
            LaunchedEffect(Unit) {
                vm.navEvents.collectLatest { route ->
                    navController.navigate(route) {
                        popUpTo(NavDest.Splash.route) { inclusive = true }
                    }
                }
            }
        }
        composable(NavDest.Login.route) {
            val vm: com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel = viewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            // startFirebaseSignIn is forwarded to Activity
            LoginScreen(onLogin = { email, password -> vm.signInWithEmail(email, password) }, onGoogleSignIn = {
                startFirebaseSignIn()
            }, onNavigateToRegister = { navController.navigate(NavDest.Register.route) }, isLoading = state.isLoading, errorMessage = state.errorMessage)
            LaunchedEffect(Unit) {
                vm.navEvents.collectLatest { route ->
                    navController.navigate(route) {
                        popUpTo(NavDest.Login.route) { inclusive = true }
                    }
                }
            }
        }
        composable(NavDest.Register.route) {
            val vm: com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel = viewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            RegisterScreen(onRegister = { email, password -> vm.signUpWithEmail(email, password) }, onNavigateToLogin = {
                navController.popBackStack(); navController.navigate(NavDest.Login.route)
            }, isLoading = state.isLoading, errorMessage = state.errorMessage)
            LaunchedEffect(Unit) {
                vm.navEvents.collectLatest { route ->
                    navController.navigate(route) {
                        popUpTo(NavDest.Register.route) { inclusive = true }
                    }
                }
            }
        }
        composable(NavDest.Home.route) {
            val vm: com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel = viewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            HomeScreen(onSignOut = { vm.signOut() })
            LaunchedEffect(Unit) {
                vm.navEvents.collectLatest { route ->
                    navController.navigate(route) {
                        popUpTo(NavDest.Home.route) { inclusive = true }
                    }
                }
            }
        }
    }
}


