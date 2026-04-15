package com.example.firstapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.firstapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class login_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient


    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResult(task)
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        Log.e("Mayur", "loginActivity==============>: onCreate() call")


        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.implicitibutton.setOnClickListener { signInWithGoogle() }

        // Sign In with Google (Force chooser)
        binding.implicitibutton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        }

        // Navigate to Sign Up Activity
        binding.textView.setOnClickListener {
            Log.e("SignInActivity", "TextView clicked")
            val intent = Intent(this, singupActivity::class.java)
            startActivity(intent)
        }

        // Forgot Password Logic
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            if (email.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            getString(R.string.password_reset_sent),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            getString(R.string.password_reset_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, getString(R.string.enter_registered_email), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Email/Password Login
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            Log.e("Mayur", "Login Button Clicked. Email: $email")

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                Log.e("Mayur", "Attempting FirebaseAuth signInWithEmailAndPassword")

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.e("Mayur", "Login successful for email: $email")

                        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        sharedPref.edit {
                            putBoolean("isUserLoggedOut", false)
                        }

                        if (email.equals("admin123@gmail.com", ignoreCase = true)) {
                            Log.e("Mayur", "Logged in user is ADMIN")
                            sharedPref.edit {
                                putString("userRole", "admin")
                            }
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Log.e("Mayur", "Logged in user is STUDENT")
                            sharedPref.edit {
                                putString("userRole", "student")
                            }
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                    } else {
                        Log.e("Mayur", "Login Failed: ${it.exception?.message}")
                        Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e("Mayur", "Email or Password field is empty.")
                Toast.makeText(this, getString(R.string.login_empty_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, getString(R.string.google_signin_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                sharedPref.edit {
                    putBoolean("isUserLoggedOut", false)
                }

                val email = account.email
                if (email.equals("admin123@gmail.com", ignoreCase = true)) {
                    sharedPref.edit {
                        putString("userRole", "admin")
                    }
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                } else {
                    sharedPref.edit {
                        putString("userRole", "student")
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            } else {
                Toast.makeText(this, getString(R.string.firebase_auth_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("Mayur", "loginActivity==============>: onStart() call")

    }

    override fun onResume() {
        super.onResume()
        Log.e("Mayur", "loginActivity==============>: onResume() call")

    }

    override fun onPause() {
        super.onPause()
        Log.e("Mayur", "loginActivity==============>: onPause() call")

    }

    override fun onStop() {
        super.onStop()
        Log.e("Mayur", "loginActivity==============>: onStop() call")

    }

    override fun onRestart() {
        super.onRestart()
        Log.e("Mayur", "loginActivity==============>: onRestart() call")

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Mayur", "loginActivity==============>: onDestroy() call")

    }
}