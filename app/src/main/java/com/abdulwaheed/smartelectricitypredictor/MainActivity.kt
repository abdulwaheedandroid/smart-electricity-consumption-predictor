package com.abdulwaheed.smartelectricitypredictor

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abdulwaheed.smartelectricitypredictor.navigation.AppNavHost
import com.abdulwaheed.smartelectricitypredictor.ui.theme.SmartElectricityPredictorTheme
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(this)[AuthViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // launcher for FirebaseUI sign-in flow
        val firebaseLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val response = IdpResponse.fromResultIntent(result.data)
                authViewModel.handleFirebaseUiSignInResult(
                    result.resultCode == Activity.RESULT_OK, response?.error
                )
            }
        val lightSystemBars = SystemBarStyle.light(Color.TRANSPARENT, Color.BLACK)
        enableEdgeToEdge(statusBarStyle = lightSystemBars, navigationBarStyle = lightSystemBars)
        setContent {
            SmartElectricityPredictorTheme {
                // Host the app navigation; pass a lambda to start FirebaseUI sign-in flow
                AppNavHost(authViewModel = authViewModel, startFirebaseSignIn = {
                    if (!authViewModel.onFirebaseUiSignInStarted()) return@AppNavHost
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )
                    val signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setCredentialManagerEnabled(false)
                        .setTheme(R.style.Theme_FirebaseUI)
                        .build()
                    try {
                        firebaseLauncher.launch(signInIntent)
                    } catch (exception: Exception) {
                        authViewModel.handleFirebaseUiSignInResult(false, exception)
                    }
                })
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmartElectricityPredictorTheme {
        Greeting("Android")
    }
}
