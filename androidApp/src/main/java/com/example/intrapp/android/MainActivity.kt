package com.example.intrapp.android                                                                                                   
                                                                                                                                      
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity                                                                                            
import androidx.activity.compose.setContent                                                                                           
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.intrapp.ApiClient
import com.example.intrapp.SessionManager
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observar estados del ViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.authState.collect { state ->
                        when (state) {
                            is ProfileViewModel.AuthState.Error -> {
                                SessionManager.lastAuthError = state.message
                                Toast.makeText(
                                    this@MainActivity,
                                    state.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }


        // Configuración de la UI
        setContent {
            App(viewModel)
        }

        // Recoge los intent de Deep Link
        handleIntent(intent)
    }

    // Si la app estaba en segundo plano: se llama desde onNewIntent()
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("AuthIntra", "onNewIntent - Intent: ${intent?.data}")
        handleIntent(intent)
    }

    //Recibe el callback y procede a flujo WebFlowApplicaton de OAuth
    private fun handleIntent(intent: Intent?) {

        //Caso de Intent sin data , probablemente launcher. No es callback
        val uri = intent?.data ?: return

        // //Si es callback,  Analizar la URL de callback
        when {
            uri.getQueryParameter("error") != null -> {
                val error = uri.getQueryParameter("error_description") ?: "Error desconocido"
                Log.e("OAUTH_ERROR", "$error")
                SessionManager.lastAuthError = error
            }
            uri.getQueryParameter("code") != null -> {
                val code = uri.getQueryParameter("code")!!
                Log.d("AuthIntra", "Authorization code received: $code")
                lifecycleScope.launch {
                    try {
                        viewModel.handleAuthCallback(code)
                    } catch (e: Exception) {
                        SessionManager.lastAuthError = e.message
                        Log.e("AuthIntra", "Error handling callback", e)
                    }
                }
            }
        }
        this.intent = Intent()
    }

    override fun onDestroy() {
        super.onDestroy()
        ApiClient().close()
        Log.d("[APP]", "HttpClient cerrado")
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    val viewModel = ProfileViewModel()
    App(viewModel)
}











