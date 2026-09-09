package com.abdulwaheed.smartelectricitypredictor.navigation

import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthStage

/** Returns a replacement root only when the current destination violates the session gate. */
internal fun reconciliationTarget(stage: AuthStage, currentRoute: String?): String? {
    val target = when (stage) {
        AuthStage.CHECKING, AuthStage.PROFILE_ERROR -> NavDest.Splash.route
        AuthStage.SIGNED_OUT -> NavDest.Login.route
        AuthStage.NEEDS_PROFILE -> NavDest.ProfileSetup.route
        AuthStage.READY -> if (currentRoute == null || currentRoute in setOf(
            NavDest.Splash.route, NavDest.Login.route, NavDest.ProfileSetup.route
        )) NavDest.Home.route else return null
    }
    return target.takeUnless { it == currentRoute }
}
