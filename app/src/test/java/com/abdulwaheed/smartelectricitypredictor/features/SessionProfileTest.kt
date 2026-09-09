package com.abdulwaheed.smartelectricitypredictor.features

import com.abdulwaheed.smartelectricitypredictor.domain.model.User
import com.abdulwaheed.smartelectricitypredictor.domain.model.UserProfile
import com.abdulwaheed.smartelectricitypredictor.domain.repository.AuthRepository
import com.abdulwaheed.smartelectricitypredictor.domain.repository.ProfileRepository
import com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel
import com.abdulwaheed.smartelectricitypredictor.features.auth.state.AuthStage
import com.abdulwaheed.smartelectricitypredictor.features.profile.ProfileViewModel
import com.abdulwaheed.smartelectricitypredictor.features.profile.state.ProfileCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionProfileTest {
    private val dispatcher = StandardTestDispatcher()
    private val account = FakeAuth()
    private val profiles = FakeProfiles()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun unauthenticatedStartupDoesNotReadProfile() = runTest(dispatcher) {
        account.user = null
        val vm = AuthViewModel(account, profiles)
        advanceUntilIdle()
        assertEquals(AuthStage.SIGNED_OUT, vm.uiState.value.stage)
        assertEquals(0, profiles.reads)
    }

    @Test fun restoredUserWithProfileGoesToHomeGate() = runTest(dispatcher) {
        val vm = AuthViewModel(account, profiles)
        advanceUntilIdle()
        assertEquals(AuthStage.READY, vm.uiState.value.stage)
    }

    @Test fun restoredUserWithoutProfileGoesToSetupGate() = runTest(dispatcher) {
        profiles.result = Result.success(null)
        val vm = AuthViewModel(account, profiles)
        advanceUntilIdle()
        assertEquals(AuthStage.NEEDS_PROFILE, vm.uiState.value.stage)
    }

    @Test fun failedReadCanRetryWithoutReauthenticating() = runTest(dispatcher) {
        profiles.result = Result.failure(IllegalStateException())
        val vm = AuthViewModel(account, profiles)
        advanceUntilIdle()
        assertEquals(AuthStage.PROFILE_ERROR, vm.uiState.value.stage)
        assertNotNull(vm.uiState.value.user)
        profiles.result = Result.success(profiles.profile)
        vm.checkSession()
        advanceUntilIdle()
        assertEquals(AuthStage.READY, vm.uiState.value.stage)
    }

    @Test fun cancellationReturnsToLandingAndAllowsRetry() = runTest(dispatcher) {
        account.user = null
        val vm = AuthViewModel(account, profiles)
        advanceUntilIdle()
        assertTrue(vm.onFirebaseUiSignInStarted())
        assertFalse(vm.onFirebaseUiSignInStarted())
        vm.handleFirebaseUiSignInResult(false, null)
        assertEquals(AuthStage.SIGNED_OUT, vm.uiState.value.stage)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Sign-in was cancelled.", vm.uiState.value.errorMessage)
        assertTrue(vm.onFirebaseUiSignInStarted())
    }

    @Test fun firebaseUiSuccessChecksProfile() = runTest(dispatcher) {
        account.user = null
        val vm = AuthViewModel(account, profiles)
        advanceUntilIdle()
        account.user = User("uid", "user@example.com")
        vm.handleFirebaseUiSignInResult(true, null)
        advanceUntilIdle()
        assertEquals(AuthStage.READY, vm.uiState.value.stage)
        assertEquals(1, profiles.reads)
    }

    @Test fun signOutFromReadErrorClearsUserAndSession() = runTest(dispatcher) {
        profiles.result = Result.failure(IllegalStateException())
        val vm = AuthViewModel(account, profiles)
        advanceUntilIdle()
        vm.signOut()
        vm.signOut()
        advanceUntilIdle()
        assertEquals(1, account.signOuts)
        assertEquals(AuthStage.SIGNED_OUT, vm.uiState.value.stage)
        assertNull(vm.uiState.value.user)
    }

    @Test fun failedSignOutKeepsSessionAndExposesError() = runTest(dispatcher) {
        val vm = AuthViewModel(account, profiles)
        advanceUntilIdle()
        account.signOutResult = Result.failure(IllegalStateException())
        vm.signOut()
        advanceUntilIdle()
        assertEquals(AuthStage.READY, vm.uiState.value.stage)
        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test fun failedProfileReadCannotBeClearedByEditingOrSaved() = runTest(dispatcher) {
        profiles.result = Result.failure(IllegalStateException())
        val vm = ProfileViewModel(account, profiles)
        advanceUntilIdle()
        vm.onFullNameChanged("New Name")
        vm.onAgeChanged("25")
        vm.onGenderChanged("Other")
        vm.onCellNumberChanged("1234567")
        vm.saveProfile()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.hasLoaded)
        assertNotNull(vm.uiState.value.errorMessage)
        assertEquals(0, profiles.saves)
        profiles.result = Result.success(null)
        vm.loadProfile()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasLoaded)
        assertFalse(vm.uiState.value.profileExists)
    }

    @Test fun profileSaveCompletionIsConsumedOnce() = runTest(dispatcher) {
        val vm = ProfileViewModel(account, profiles)
        advanceUntilIdle()
        vm.saveProfile()
        advanceUntilIdle()
        assertEquals(ProfileCompletion.SAVED, vm.uiState.value.completion)
        vm.consumeCompletion()
        assertNull(vm.uiState.value.completion)
        assertEquals(1, profiles.saves)
    }

    @Test fun deletionClearsFieldsButKeepsAuthAccount() = runTest(dispatcher) {
        val vm = ProfileViewModel(account, profiles)
        advanceUntilIdle()
        vm.requestDeleteProfile()
        vm.confirmDeleteProfile()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(ProfileCompletion.DELETED, state.completion)
        assertEquals("", state.fullName)
        assertEquals("", state.age)
        assertEquals("", state.gender)
        assertEquals("", state.cellNumber)
        assertEquals("uid", state.uid)
        assertEquals("user@example.com", state.email)
        assertFalse(state.profileExists)
        assertNotNull(account.user)
        assertEquals(0, account.signOuts)
        assertEquals(1, profiles.deletes)
    }

    @Test fun deletionFailureKeepsProfileAndDoesNotNavigate() = runTest(dispatcher) {
        profiles.deleteResult = Result.failure(IllegalStateException())
        val vm = ProfileViewModel(account, profiles)
        advanceUntilIdle()
        vm.requestDeleteProfile()
        vm.confirmDeleteProfile()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.profileExists)
        assertNull(vm.uiState.value.completion)
        assertNotNull(vm.uiState.value.errorMessage)
    }

    private class FakeAuth : AuthRepository {
        var user: User? = User("uid", "user@example.com")
        var signOuts = 0
        var signOutResult = Result.success(Unit)
        override fun getCurrentUser() = user
        override suspend fun signOut(): Result<Unit> {
            signOuts++
            if (signOutResult.isSuccess) user = null
            return signOutResult
        }
    }

    private class FakeProfiles : ProfileRepository {
        val profile = UserProfile("uid", "Valid Name", "user@example.com", 25, "Other", "1234567")
        var result: Result<UserProfile?> = Result.success(profile)
        var deleteResult = Result.success(Unit)
        var reads = 0
        var saves = 0
        var deletes = 0
        override suspend fun getProfile(uid: String): Result<UserProfile?> { reads++; return result }
        override suspend fun saveProfile(profile: UserProfile): Result<Unit> { saves++; return Result.success(Unit) }
        override suspend fun deleteProfile(uid: String): Result<Unit> { deletes++; return deleteResult }
    }
}
