package com.abdulwaheed.smartelectricitypredictor.navigation

sealed class NavDest(val route: String) {
    object Splash : NavDest("splash")
    object Login : NavDest("login")
    object Register : NavDest("register")
    object Home : NavDest("home")
}

