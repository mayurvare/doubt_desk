package com.example.firstapp

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth


class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val isUserLoggedOut = sharedPref.getBoolean("isUserLoggedOut", false)
            val userRole = sharedPref.getString("userRole", "")
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null && !isUserLoggedOut) {
                if (userRole == "admin") {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                } else {
                    // User is login
                    startActivity(Intent(this, MainActivity::class.java))
                }
            } else {
                // not a login or manually logout
                startActivity(Intent(this, login_Activity::class.java))
            }
            finish()
        }, 2500)
    }
}