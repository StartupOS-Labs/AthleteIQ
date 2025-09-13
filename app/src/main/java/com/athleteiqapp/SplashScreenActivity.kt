package com.athleteiqapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 2000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            checkCurrentUser()
        }, SPLASH_DELAY)
    }

    private fun checkCurrentUser() {
        val intent: Intent
        // ⭐ FIX: Use SessionManager to check if a user is logged in
        if (SessionManager.isLoggedIn()) {
            intent = Intent(this, HomeActivity::class.java)
        } else {
            intent = Intent(this, WelcomeActivity::class.java)
        }

        startActivity(intent)
        finish() // Close the splash screen activity
    }
}