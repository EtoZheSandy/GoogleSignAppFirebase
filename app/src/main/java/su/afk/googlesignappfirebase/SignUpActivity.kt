package su.afk.googlesignappfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import su.afk.googlesignappfirebase.databinding.ActivitySingUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonSingIn.setOnClickListener {
            val intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
        }

        // Кнопка регистрации
        binding.buttonSingUp.setOnClickListener {
            val email = binding.tvInputEmail.text.toString()
            val password = binding.tvInputPass.text.toString()
            val passwordConfirm = binding.tvInputPassCofrirm.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty() && passwordConfirm.isNotEmpty()) {
                if(password == passwordConfirm) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
//                        Log.d("TAG", "${it.result}")
//                        Log.d("TAG", "${it.exception}")
//                        Log.d("TAG", "${it}")
                        if(it.isSuccessful){
                            val intent = Intent(this, SingInActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Пароли различаются", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Пустые поля недопустимы", Toast.LENGTH_LONG).show()
            }
        }
    }
}