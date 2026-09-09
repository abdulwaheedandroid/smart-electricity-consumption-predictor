package com.abdulwaheed.smartelectricitypredictor.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel
import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthStage
import com.abdulwaheed.smartelectricitypredictor.features.auth.ui.LoginScreen
import com.abdulwaheed.smartelectricitypredictor.features.auth.ui.SplashScreen
import com.abdulwaheed.smartelectricitypredictor.features.appliance.ApplianceViewModel
import com.abdulwaheed.smartelectricitypredictor.features.appliance.ui.ApplianceScreen
import com.abdulwaheed.smartelectricitypredictor.features.home.ui.HomeScreen
import com.abdulwaheed.smartelectricitypredictor.features.profile.ProfileViewModel
import com.abdulwaheed.smartelectricitypredictor.features.profile.state.ProfileCompletion
import com.abdulwaheed.smartelectricitypredictor.features.profile.ui.ProfileSetupScreen

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startFirebaseSignIn: () -> Unit = {}
) {
    val auth by authViewModel.uiState.collectAsStateWithLifecycle()
    val entry by navController.currentBackStackEntryAsState()
    BackHandler(enabled = auth.isLoading) { }
    NavHost(navController, startDestination = NavDest.Splash.route, modifier = modifier) {
        composable(NavDest.Splash.route) {
            SplashScreen(
                errorMessage = auth.errorMessage,
                onRetry = authViewModel::checkSession,
                onSignOut = if (auth.user != null) authViewModel::signOut else null,
                isSigningOut = auth.isLoading
            )
        }
        composable(NavDest.Login.route) {
            if (auth.stage == AuthStage.SIGNED_OUT) {
                LoginScreen(
                    onSignIn = startFirebaseSignIn,
                    isLoading = auth.isLoading,
                    errorMessage = auth.errorMessage
                )
            }
        }
        for (destination in listOf(NavDest.ProfileSetup, NavDest.Profile)) {
            composable(destination.route) {
                val setup = destination == NavDest.ProfileSetup
                if ((setup && auth.stage == AuthStage.NEEDS_PROFILE) ||
                    (!setup && auth.stage == AuthStage.READY)) {
                    val vm: ProfileViewModel = hiltViewModel()
                    val state by vm.uiState.collectAsStateWithLifecycle()
                    BackHandler(enabled = state.isSaving || state.isDeleting) { }
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
                        onCancelDelete = vm::cancelDeleteProfile,
                        onSignOut = authViewModel::signOut,
                        isSigningOut = auth.isLoading,
                        signOutError = auth.errorMessage
                    )
                    LaunchedEffect(state.completion, state.hasLoaded, state.profileExists) {
                        when (state.completion) {
                            ProfileCompletion.SAVED -> {
                                vm.consumeCompletion()
                                authViewModel.onProfileSaved()
                                if (!setup && !navController.popBackStack(NavDest.Home.route, false)) {
                                    navController.replaceRoot(NavDest.Home.route)
                                }
                            }
                            ProfileCompletion.DELETED -> {
                                vm.consumeCompletion()
                                authViewModel.onProfileDeleted()
                            }
                            null -> {
                                // A successful read can discover deletion from another session.
                                if (!setup && state.hasLoaded && !state.profileExists) {
                                    authViewModel.onProfileDeleted()
                                }
                            }
                        }
                    }
                }
            }
        }
        composable(NavDest.Home.route) {
            if (auth.stage == AuthStage.READY) {
                HomeScreen(
                    onViewProfile = {
                        navController.navigate(NavDest.Profile.route) { launchSingleTop = true }
                    },
                    onViewAppliances = {
                        navController.navigate(NavDest.Appliances.route) { launchSingleTop = true }
                    },
                    onSignOut = authViewModel::signOut,
                    isSigningOut = auth.isLoading,
                    signOutError = auth.errorMessage
                )
            }
        }
        composable(NavDest.Appliances.route) {
            if (auth.stage == AuthStage.READY) {
                val vm: ApplianceViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                ApplianceScreen(
                    state = state,
                    onSearchQueryChanged = vm::onSearchQueryChanged,
                    onAddAppliance = vm::startAddingAppliance,
                    onEditAppliance = vm::startEditingAppliance,
                    onDeleteAppliance = vm::requestDeleteAppliance,
                    onNameChanged = vm::onNameChanged,
                    onPowerWattsChanged = vm::onPowerWattsChanged,
                    onDailyUsageHoursChanged = vm::onDailyUsageHoursChanged,
                    onSaveAppliance = vm::saveAppliance,
                    onDismissEditor = vm::dismissEditor,
                    onConfirmDelete = vm::confirmDeleteAppliance,
                    onCancelDelete = vm::cancelDeleteAppliance,
                    onRetry = vm::loadAppliances
                )
            }
        }
    }
    LaunchedEffect(auth.stage, entry?.destination?.route) {
        reconciliationTarget(auth.stage, entry?.destination?.route)?.let(navController::replaceRoot)
    }
}

/** Remove every child destination, including any old authenticated back stack. */
internal fun NavHostController.replaceRoot(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = false }
        launchSingleTop = true
    }
}
