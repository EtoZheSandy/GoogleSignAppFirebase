package su.afk.googlesignappfirebase

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import su.afk.googlesignappfirebase.Firebase.GoogleAuthUiClient
import su.afk.googlesignappfirebase.databinding.ActivityMainBinding
import su.afk.googlesignappfirebase.signIn.SignInViewModel
import su.afk.googlesignappfirebase.signIn.SignScreen

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
        setContent {
           MaterialTheme {
               Surface(
                   modifier = Modifier.fillMaxSize(),
                   color = Color.Black
               ) {
                   val navController = rememberNavController()
                   NavHost(navController = navController, startDestination = "sign_in") {
                       composable("sign_in") {
                           val viewmodel = viewModel<SignInViewModel>()
                           val state by viewmodel.state.collectAsStateWithLifecycle()


                           val launcher = rememberLauncherForActivityResult(
                               contract = ActivityResultContracts.StartIntentSenderForResult(),
                               onResult = { result ->
                                   if(result.resultCode == RESULT_OK) {
                                       lifecycleScope.launch {
                                           val signInResult = googleAuthUiClient.singInWithIntent(
                                               intent = result.data ?: return@launch
                                           )
                                           viewmodel.onSignInResult(signInResult)
                                       }
                                   }
                               }
                           )

                           LaunchedEffect(key1 = state.isSignInSuccess) {
                               if(state.isSignInSuccess) {
                                   Toast.makeText(applicationContext, "Вход успешен", Toast.LENGTH_LONG).show()
                               }
                           }

                           SignScreen(
                               state = state,
                               onSignInClick = {
                                   lifecycleScope.launch {
                                       val signInIntentSender = googleAuthUiClient.singIn()
                                       launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                       )
                                   }
                               }
                               )
                       }
                   }
               }
           }
        }



    }
}