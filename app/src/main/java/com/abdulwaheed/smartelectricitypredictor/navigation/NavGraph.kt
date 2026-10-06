package com.abdulwaheed.smartelectricitypredictor.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abdulwaheed.smartelectricitypredictor.features.appliance.ApplianceViewModel
import com.abdulwaheed.smartelectricitypredictor.features.appliance.ui.ApplianceScreen
import com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel
import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthStage
import com.abdulwaheed.smartelectricitypredictor.features.auth.ui.LoginScreen
import com.abdulwaheed.smartelectricitypredictor.features.auth.ui.SplashScreen
import com.abdulwaheed.smartelectricitypredictor.features.home.ui.HomeScreen
import com.abdulwaheed.smartelectricitypredictor.features.home.HomeViewModel
import com.abdulwaheed.smartelectricitypredictor.features.home.state.HomeUiState
import com.abdulwaheed.smartelectricitypredictor.features.historical.HistoricalConsumptionViewModel
import com.abdulwaheed.smartelectricitypredictor.features.historical.ui.HistoricalConsumptionScreen
import com.abdulwaheed.smartelectricitypredictor.features.historical.state.HistoricalConsumptionUiState
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
            val vm: HomeViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(auth.user?.uid, auth.stage) { vm.checkSession() }
            DisposableEffect(lifecycleOwner, vm) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) vm.checkSession()
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    vm.checkSession()
                }
            }
            if (auth.stage == AuthStage.READY) {
                HomeScreen(
                    state = if (state.uid == auth.user?.uid) state else HomeUiState(),
                    onRetry = vm::retryLoad,
                    onViewProfile = {
                        navController.navigate(NavDest.Profile.route) { launchSingleTop = true }
                    },
                    onViewAppliances = {
                        navController.navigate(NavDest.Appliances.route) { launchSingleTop = true }
                    },
                    onViewHistory = {
                        navController.navigate(NavDest.HistoricalConsumption.route) { launchSingleTop = true }
                    },
                    onSignOut = authViewModel::signOut,
                    isSigningOut = auth.isLoading,
                    signOutError = auth.errorMessage
                )
            }
        }
        composable(NavDest.HistoricalConsumption.route) {
            val vm: HistoricalConsumptionViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current
            // Keep session reconciliation outside READY: sign-out must reset the
            // history ViewModel even while this destination is being removed.
            LaunchedEffect(auth.user?.uid, auth.stage) { vm.checkSession() }
            DisposableEffect(lifecycleOwner, vm) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) vm.checkSession()
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    vm.checkSession()
                }
            }
            val signedIn = auth.stage == AuthStage.READY && auth.user != null
            val accountMatches = state.uid == auth.user?.uid
            BackHandler(enabled = signedIn && accountMatches && (state.isSaving || state.isDeleting)) { }
            HistoricalConsumptionScreen(
                state = if (accountMatches) state else HistoricalConsumptionUiState(),
                isSignedIn = signedIn,
                onBack = { navController.popBackStack() },
                onAdd = vm::startAddingConsumption,
                onEdit = vm::startEditingConsumption,
                onDelete = vm::requestDeleteConsumption,
                onMonthChanged = vm::onMonthChanged,
                onYearChanged = vm::onYearChanged,
                onUnitsChanged = vm::onUnitsConsumedChanged,
                onBillChanged = vm::onBillAmountChanged,
                onSave = vm::saveConsumption,
                onDismissEditor = vm::dismissEditor,
                onConfirmDelete = vm::confirmDeleteConsumption,
                onCancelDelete = vm::cancelDeleteConsumption,
                onRetryLoad = vm::retryLoad,
                onRetrySync = vm::retrySync
            )
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
                    onRetry = vm::retrySync
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
