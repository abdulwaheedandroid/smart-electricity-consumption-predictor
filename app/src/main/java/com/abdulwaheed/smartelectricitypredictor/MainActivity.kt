package com.abdulwaheed.smartelectricitypredictor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
// No Hilt: using simple ServiceLocator for dependencies
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.abdulwaheed.smartelectricitypredictor.features.auth.AuthViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abdulwaheed.smartelectricitypredictor.ui.theme.SmartElectricityPredictorTheme
import com.abdulwaheed.smartelectricitypredictor.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm: AuthViewModel by viewModels()
        // launcher for FirebaseUI sign-in flow
        val firebaseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                // Signed in successfully, inform ViewModel to refresh auth state
                vm.checkAuthAndNavigate()
            } else {
                // handle error if needed
                val message = response?.error?.localizedMessage
                // update VM state with error
                // Note: ViewModel has no direct method for error from FirebaseUI; instead check Auth state
            }
        }
        enableEdgeToEdge()
        setContent {
            SmartElectricityPredictorTheme {
                // Host the app navigation; pass a lambda to start FirebaseUI sign-in flow
                AppNavHost(startFirebaseSignIn = {
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )
                    val signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build()
                    firebaseLauncher.launch(signInIntent)
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
