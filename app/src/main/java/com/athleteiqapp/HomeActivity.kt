package com.athleteiqapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var helloTextView: TextView
    private lateinit var welcomeMessageTextView: TextView
    private lateinit var startTestButton: Button
    private lateinit var testsButton: LinearLayout
    private lateinit var resultsButton: LinearLayout
    private lateinit var profileButton: LinearLayout
    private lateinit var logOutButton: Button
    private lateinit var viewResultsButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        helloTextView = findViewById(R.id.helloTextView)
        welcomeMessageTextView = findViewById(R.id.welcomeMessageTextView)
        startTestButton = findViewById(R.id.startTestButton)
        testsButton = findViewById(R.id.testsButton)
        resultsButton = findViewById(R.id.resultsButton)
        profileButton = findViewById(R.id.profileButton)

        viewResultsButton = findViewById(R.id.viewResultsButton)

        setupClickListeners()
        loadUserData()
        // ⭐ FIX: Call the setupBottomNavigation() function here
        setupBottomNavigation()
    }

    private fun setupClickListeners() {
        startTestButton.setOnClickListener {
            startActivity(Intent(this, TestsActivity::class.java))
            finish()
        }

        viewResultsButton.setOnClickListener {
            startActivity(Intent(this, ResultsHistoryActivity::class.java))
            finish()
        }


    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            helloTextView.text = "Hello, Guest"
            return
        }

        helloTextView.text = "Hello, loading..."

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val userProfile = document.toObject(UserProfile::class.java)
                if (userProfile != null) {
                    helloTextView.text = "Hello, ${userProfile.name}"
                } else {
                    helloTextView.text = "Hello, User"
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeActivity", "Error fetching user data", e)
                helloTextView.text = "Hello, Guest"
                Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        val homeButton: LinearLayout = findViewById(R.id.homeButton)
        val testsButton: LinearLayout = findViewById(R.id.testsButton)
        val resultsButton: LinearLayout = findViewById(R.id.resultsButton)
        val profileButton: LinearLayout = findViewById(R.id.profileButton)

        homeButton.setOnClickListener {
            // Do nothing, already on this page
        }
        testsButton.setOnClickListener {
            startActivity(Intent(this, TestsActivity::class.java))
            finish()
        }
        resultsButton.setOnClickListener {
            startActivity(Intent(this, ResultsHistoryActivity::class.java))
            finish()
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}