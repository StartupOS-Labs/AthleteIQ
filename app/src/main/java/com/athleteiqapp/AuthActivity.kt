package com.athleteiqapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.athleteiqapp.MainActivity

class AuthActivity : AppCompatActivity() {

    // A simple flag to track if we are in "Login" mode or "Register" mode.
    private var isLoginMode = false

    // This is the modern way to handle the result of a permission request.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // If the user grants permission, we can now proceed with the registration.
                performRegistration()
            } else {
                // If permission is denied, show a message explaining why it's needed.
                Toast.makeText(this, "Camera permission is required to register.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Get references to all the UI elements from our activity_auth.xml layout
        val titleTextView: TextView = findViewById(R.id.titleTextView)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val actionButton: Button = findViewById(R.id.actionButton)
        val toggleTextView: TextView = findViewById(R.id.toggleTextView)

        // Set up the main action button (Login/Register)
        actionButton.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                // For registration, we must check for camera permission *before* creating the account.
                if (hasCameraPermission()) {
                    performRegistration()
                } else {
                    // If we don't have permission, trigger the pop-up.
                    requestPermission()
                }
            }
        }

        // Set up the toggle text to switch between Login and Register modes
        toggleTextView.setOnClickListener {
            isLoginMode = !isLoginMode // Flip the mode
            if (isLoginMode) {
                titleTextView.text = "Login"
                actionButton.text = "Login"
                toggleTextView.text = "Don't have an account? Register"
            } else {
                titleTextView.text = "Create Account"
                actionButton.text = "Register"
                toggleTextView.text = "Already have an account? Login"
            }
        }
    }

    // A simple helper function to check if we already have camera permission.
    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    // This function launches the permission pop-up.
    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // This function handles the logic for logging in.
    private fun performLogin() {
        val email = findViewById<EditText>(R.id.emailEditText).text.toString().trim()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Access the local storage (SharedPreferences)
        val sharedPreferences = getSharedPreferences("AthleteIQPrefs", Context.MODE_PRIVATE)
        val savedPassword = sharedPreferences.getString(email, null) // Try to get the password for this email

        if (savedPassword != null && savedPassword == password) {
            // If the password exists and matches, login is successful.
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            // Otherwise, show an error.
            Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
        }
    }

    // This function handles the logic for registering a new user.
    private fun performRegistration() {
        val email = findViewById<EditText>(R.id.emailEditText).text.toString().trim()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Access the local storage
        val sharedPreferences = getSharedPreferences("AthleteIQPrefs", Context.MODE_PRIVATE)
        // Save the new email and password pair
        with(sharedPreferences.edit()) {
            putString(email, password)
            apply() // apply() saves the data in the background
        }

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }

    // This function navigates to the main assessment screen.
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Call finish() to prevent the user from coming back to this screen
    }
}

