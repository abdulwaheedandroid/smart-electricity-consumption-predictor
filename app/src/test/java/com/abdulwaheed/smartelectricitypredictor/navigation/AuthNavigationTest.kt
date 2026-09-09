package com.abdulwaheed.smartelectricitypredictor.navigation

import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthStage
import org.junit.Assert.*
import org.junit.Test

class AuthNavigationTest {
    @Test fun logoutReplacesAnyProtectedDestination() {
        for (route in listOf("home", "profile", "profile_setup", "appliances", "future_feature")) {
            assertEquals("login", reconciliationTarget(AuthStage.SIGNED_OUT, route))
        }
    }
    @Test fun deletionForcesSetupFromAnyFeature() {
        for (route in listOf("home", "profile", "appliances", "future_feature")) {
            assertEquals("profile_setup", reconciliationTarget(AuthStage.NEEDS_PROFILE, route))
        }
    }
    @Test fun authenticationAndCreationReplaceEntryWithHome() {
        for (route in listOf(null, "splash", "login", "profile_setup")) {
            assertEquals("home", reconciliationTarget(AuthStage.READY, route))
        }
    }
    @Test fun validFeaturesArePreservedIncludingFutureRoutes() {
        for (route in listOf("home", "profile", "appliances", "future_feature")) {
            assertNull(reconciliationTarget(AuthStage.READY, route))
        }
    }
    @Test fun repeatedStateDoesNotPushDuplicateRoot() {
        assertNull(reconciliationTarget(AuthStage.SIGNED_OUT, "login"))
        assertNull(reconciliationTarget(AuthStage.NEEDS_PROFILE, "profile_setup"))
        assertNull(reconciliationTarget(AuthStage.READY, "home"))
    }
    @Test fun failedProfileCheckNeverPermitsProtectedScreen() {
        assertEquals("splash", reconciliationTarget(AuthStage.PROFILE_ERROR, "home"))
        assertEquals("splash", reconciliationTarget(AuthStage.CHECKING, "appliances"))
    }
}
