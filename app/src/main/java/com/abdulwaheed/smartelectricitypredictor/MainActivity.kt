package com.abdulwaheed.smartelectricitypredictor

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
    private var firebaseSignInResultHandler: ((Boolean, Throwable?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // launcher for FirebaseUI sign-in flow
        val firebaseLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val response = IdpResponse.fromResultIntent(result.data)
                val handler = firebaseSignInResultHandler
                firebaseSignInResultHandler = null
                handler?.invoke(result.resultCode == Activity.RESULT_OK, response?.error)
            }
        enableEdgeToEdge()
        setContent {
            SmartElectricityPredictorTheme {
                // Host the app navigation; pass a lambda to start FirebaseUI sign-in flow
                AppNavHost(startFirebaseSignIn = { onResult ->
                    firebaseSignInResultHandler = onResult
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
