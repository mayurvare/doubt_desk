package com.example.firstapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.databinding.ActivitySingupBinding
import com.google.firebase.auth.FirebaseAuth


class singupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySingupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingupBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        Log.e("Mayur", "signupActivity==============>: onCreate() call")


        firebaseAuth = FirebaseAuth.getInstance()


        binding.textView8.setOnClickListener {
            val intent = Intent(this, login_Activity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            Log.e("TAG", "onCreate: test 1")
            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                Log.e("TAG", "onCreate: test 2")
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, login_Activity::class.java)
                            startActivity(intent)
                        } else {
                            Log.e("TAG", "onCreate: ${it.exception.toString()}")
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("Mayur", "signupActivity==============>: onStart() call")

    }

    override fun onResume() {
        super.onResume()
        Log.e("Mayur", "signupActivity==============>: onResume() call")

    }

    override fun onPause() {
        super.onPause()
        Log.e("Mayur", "signupActivity==============>: onPause() call")

    }

    override fun onStop() {
        super.onStop()
        Log.e("Mayur", "signupActivity==============>: onStop() call")

    }

    override fun onRestart() {
        super.onRestart()
        Log.e("Mayur", "signupActivity==============>: onRestart() call")

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Mayur", "signupActivity==============>: onDestroy() call")

    }
}