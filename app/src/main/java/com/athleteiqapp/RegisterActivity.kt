package com.athleteiqapp

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInToggle: TextView
    private lateinit var progressBar: ProgressBar // ⭐ Added a ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()
                performRegistration()
            } else {
                Toast.makeText(this, "Camera permission is required to create an account.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val backButton: ImageView = findViewById(R.id.backButton)
        nameEditText = findViewById(R.id.nameEditText)
        ageEditText = findViewById(R.id.ageEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        genderEditText = findViewById(R.id.genderEditText)
        locationEditText = findViewById(R.id.locationEditText)
        dobEditText = findViewById(R.id.dobEditText)
        signUpButton = findViewById(R.id.signUpButton)
        signInToggle = findViewById(R.id.signInToggle)
        progressBar = findViewById(R.id.progressBar) // ⭐ Initialized the ProgressBar

        dobEditText.setOnClickListener { showDatePicker() }
        backButton.setOnClickListener { finish() }
        signUpButton.setOnClickListener {
            if (hasCameraPermission()) { performRegistration() } else { requestPermission() }
        }
        signInToggle.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
    }

    // ... (rest of your helper functions)

    private fun performRegistration() {
        val name = nameEditText.text.toString().trim()
        val age = ageEditText.text.toString().trim().toIntOrNull() ?: 0
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        val gender = genderEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()
        val dob = dobEditText.text.toString().trim()

        if (name.isEmpty() || age == 0 || email.isEmpty() || password.isEmpty() || gender.isEmpty() || location.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐ FIX: Show the loading state
        progressBar.visibility = View.VISIBLE
        signUpButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // ⭐ FIX: Hide the loading state after the task is complete
                progressBar.visibility = View.GONE
                signUpButton.isEnabled = true

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userProfile = UserProfile(name, age, gender, location, dob)

                    if (user != null) {
                        db.collection("users").document(user.uid)
                            .set(userProfile)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                navigateToLogin()
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterActivity", "Failed to save profile: ${e.message}", e)
                                Toast.makeText(this, "Failed to save profile.", Toast.LENGTH_SHORT).show()
                                navigateToLogin()
                            }
                    }
                } else {
                    val exception = task.exception
                    if (exception is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this,
                            "This email is already in use. Please log in.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this, "Authentication failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    private fun requestPermission() { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedMonth + 1}/${selectedDay}/${selectedYear}"
                dobEditText.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}