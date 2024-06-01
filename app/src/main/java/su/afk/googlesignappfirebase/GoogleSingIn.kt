package su.afk.googlesignappfirebase

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import su.afk.googlesignappfirebase.ui.theme.GoogleSignAppFirebaseTheme
import java.security.MessageDigest
import java.util.UUID

class MainActivity2 : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)  // Инициализация Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            GoogleSignAppFirebaseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GoogleSingInButton(auth, db)
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleSingInButton(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val webId = stringResource(R.string.web_client_id)
    val onClick: () -> Unit = {

        val credentialManager = CredentialManager.create(context = context)

        val randomNonce = UUID.randomUUID().toString()
        val bytes = randomNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webId)
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)

                val googleIdToken = googleIdTokenCredential.idToken

                Log.d("TAG", "$googleIdToken")
//                Toast.makeText(context, "Вход успешен", Toast.LENGTH_LONG).show()
                // Аутентификация пользователя в Firebase
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Получаем информацию о пользователе
                        val user = auth.currentUser
                        val userData = hashMapOf(
                            "uid" to user?.uid,
                            "name" to user?.displayName,
                            "email" to user?.email,
                            "photoUrl" to user?.photoUrl
                        )

                        // Сохраняем информацию о пользователе в Firestore
                        db.collection("users").document(user?.uid ?: "").set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Пользователь сохранен", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
                            }

                        Toast.makeText(context, "Вход успешен", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Ошибка аутентификации", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: GetCredentialException) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            } catch (e: GoogleIdTokenParsingException) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }

        }
    }

    Button(onClick = onClick) {
        Text("Войти через гугл")
    }

}