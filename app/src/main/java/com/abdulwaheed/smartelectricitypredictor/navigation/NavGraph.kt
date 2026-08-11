package com.abdulwaheed.smartelectricitypredictor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.abdulwaheed.smartelectricitypredictor.features.profile.ProfileViewModel
import com.abdulwaheed.smartelectricitypredictor.features.profile.ui.ProfileSetupScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startFirebaseSignIn: (((Boolean, Throwable?) -> Unit) -> Unit) = {}
) {
    NavHost(navController = navController, startDestination = NavDest.Splash.route, modifier = modifier) {
        composable(NavDest.Splash.route) {
            val vm: com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.checkAuthAndNavigate() }
            // Splash just shows loading while check runs
            SplashScreen(errorMessage = state.errorMessage, onRetry = vm::checkAuthAndNavigate)
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
            val vm: com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            // startFirebaseSignIn is forwarded to Activity
            LoginScreen(onLogin = { email, password -> vm.signInWithEmail(email, password) }, onGoogleSignIn = {
                vm.onGoogleSignInStarted()
                startFirebaseSignIn(vm::handleGoogleSignInResult)
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
            val vm: com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel = hiltViewModel()
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
        composable(NavDest.ProfileSetup.route) {
            val vm: ProfileViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            ProfileSetupScreen(
                state = state,
                onFullNameChanged = vm::onFullNameChanged,
                onAgeChanged = vm::onAgeChanged,
                onGenderChanged = vm::onGenderChanged,
                onCellNumberChanged = vm::onCellNumberChanged,
                onSave = vm::saveProfile,
                onRetry = vm::loadProfile,
                onRequestDelete = vm::requestDeleteProfile,
                onConfirmDelete = vm::confirmDeleteProfile,
                onCancelDelete = vm::cancelDeleteProfile
            )
            LaunchedEffect(Unit) {
                vm.profileSaved.collectLatest {
                    navController.navigate(NavDest.Home.route) {
                        popUpTo(NavDest.ProfileSetup.route) { inclusive = true }
                    }
                }
            }
            LaunchedEffect(Unit) {
                vm.profileDeleted.collectLatest {
                    navController.navigate(NavDest.ProfileSetup.route) {
                        popUpTo(NavDest.ProfileSetup.route) { inclusive = true }
                    }
                }
            }
        }
        composable(NavDest.Profile.route) {
            val vm: ProfileViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            ProfileSetupScreen(
                state = state,
                onFullNameChanged = vm::onFullNameChanged,
                onAgeChanged = vm::onAgeChanged,
                onGenderChanged = vm::onGenderChanged,
                onCellNumberChanged = vm::onCellNumberChanged,
                onSave = vm::saveProfile,
                onRetry = vm::loadProfile,
                onRequestDelete = vm::requestDeleteProfile,
                onConfirmDelete = vm::confirmDeleteProfile,
                onCancelDelete = vm::cancelDeleteProfile
            )
            LaunchedEffect(Unit) {
                vm.profileSaved.collectLatest {
                    navController.navigate(NavDest.Home.route) {
                        popUpTo(NavDest.Profile.route) { inclusive = true }
                    }
                }
            }
            LaunchedEffect(Unit) {
                vm.profileDeleted.collectLatest {
                    navController.navigate(NavDest.ProfileSetup.route) {
                        popUpTo(NavDest.Profile.route) { inclusive = true }
                    }
                }
            }
        }
        composable(NavDest.Home.route) {
            val vm: com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                onViewProfile = { navController.navigate(NavDest.Profile.route) },
                onSignOut = { vm.signOut() }
            )
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


