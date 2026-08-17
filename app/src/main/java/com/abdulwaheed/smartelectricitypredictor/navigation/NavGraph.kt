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
import com.abdulwaheed.smartelectricitypredictor.features.auth.ui.SplashScreen
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
            LoginScreen(
                onSignIn = {
                    vm.onFirebaseUiSignInStarted()
                    startFirebaseSignIn(vm::handleFirebaseUiSignInResult)
                },
                isLoading = state.isLoading,
                errorMessage = state.errorMessage
            )
            LaunchedEffect(Unit) {
                vm.navEvents.collectLatest { route ->
                    navController.navigate(route) {
                        popUpTo(NavDest.Login.route) { inclusive = true }
                    }
                }
            }
        }
        composable(NavDest.Profile.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val authViewModel: com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel = hiltViewModel()
            val state by profileViewModel.uiState.collectAsStateWithLifecycle()
            ProfileSetupScreen(
                state = state,
                onFullNameChanged = profileViewModel::onFullNameChanged,
                onAgeChanged = profileViewModel::onAgeChanged,
                onGenderChanged = profileViewModel::onGenderChanged,
                onCellNumberChanged = profileViewModel::onCellNumberChanged,
                onSave = profileViewModel::saveProfile,
                onRetry = profileViewModel::loadProfile,
                onRequestDelete = profileViewModel::requestDeleteProfile,
                onConfirmDelete = profileViewModel::confirmDeleteProfile,
                onCancelDelete = profileViewModel::cancelDeleteProfile,
                onSignOut = authViewModel::signOut
            )
            LaunchedEffect(Unit) {
                authViewModel.navEvents.collectLatest { route ->
                    navController.navigate(route) {
                        popUpTo(NavDest.Profile.route) { inclusive = true }
                    }
                }
            }
        }
    }
}


