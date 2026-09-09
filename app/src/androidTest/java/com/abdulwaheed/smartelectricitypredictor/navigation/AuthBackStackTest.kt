package com.abdulwaheed.smartelectricitypredictor.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AuthBackStackTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var nav: NavHostController

    private fun start() {
        compose.setContent {
            nav = rememberNavController()
            NavHost(nav, startDestination = "login") {
                for (route in listOf("login", "home", "profile", "profile_setup", "appliances")) {
                    composable(route) { }
                }
            }
        }
        compose.waitForIdle()
    }

    @Test fun logoutRemovesEntireProtectedStack() {
        start()
        compose.runOnIdle {
            nav.replaceRoot("home")
            nav.navigate("profile")
            nav.navigate("appliances")
            nav.replaceRoot("login")
            assertEquals("login", nav.currentDestination?.route)
            assertNull(nav.previousBackStackEntry)
        }
    }

    @Test fun profileDeletionCannotGoBackToHome() {
        start()
        compose.runOnIdle {
            nav.replaceRoot("home")
            nav.navigate("profile")
            nav.replaceRoot("profile_setup")
            assertEquals("profile_setup", nav.currentDestination?.route)
            assertNull(nav.previousBackStackEntry)
        }
    }

    @Test fun editingReturnsToExistingHomeAndCreationRemovesSetup() {
        start()
        compose.runOnIdle {
            nav.replaceRoot("profile_setup")
            nav.replaceRoot("home")
            assertNull(nav.previousBackStackEntry)
            nav.navigate("profile")
            assertTrue(nav.popBackStack("home", false))
            assertEquals("home", nav.currentDestination?.route)
            assertNull(nav.previousBackStackEntry)
        }
    }
}
