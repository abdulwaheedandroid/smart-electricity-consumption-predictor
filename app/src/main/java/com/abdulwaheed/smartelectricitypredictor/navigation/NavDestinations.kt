package com.abdulwaheed.smartelectricitypredictor.navigation

sealed class NavDest(val route: String) {
    object Splash : NavDest("splash")
    object Login : NavDest("login")
    object ProfileSetup : NavDest("profile_setup")
    object Profile : NavDest("profile")
    object Home : NavDest("home")
    object Appliances : NavDest("appliances")
    object HistoricalConsumption : NavDest("historical_consumption")
}

