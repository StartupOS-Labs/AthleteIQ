package com.athleteiqapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TestsActivity : AppCompatActivity() {

    private lateinit var squatsButton: Button
    private lateinit var jumpingJacksButton: Button
    private lateinit var homeButton: LinearLayout
    private lateinit var testsButton: LinearLayout
    private lateinit var resultsButton: LinearLayout
    private lateinit var profileButton: LinearLayout
    private lateinit var backButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tests)

        squatsButton = findViewById(R.id.squatsButton)
        jumpingJacksButton = findViewById(R.id.jumpingJacksButton)
        homeButton = findViewById(R.id.homeButton)
        testsButton = findViewById(R.id.testsButton)
        resultsButton = findViewById(R.id.resultsButton)
        profileButton = findViewById(R.id.profileButton)
        backButton = findViewById(R.id.backButton)

        squatsButton.setOnClickListener {
            startInstructionsActivity("SQUATS")
        }

        jumpingJacksButton.setOnClickListener {
            startInstructionsActivity("JUMPING_JACKS")
        }

        backButton.setOnClickListener {
            finish()
        }

        setupBottomNavigation()
    }

    private fun startInstructionsActivity(exerciseName: String) {
        val intent = Intent(this, InstructionsActivity::class.java).apply {
            putExtra("SELECTED_EXERCISE", exerciseName)
        }
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        homeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        testsButton.setOnClickListener {
            // Do nothing, already on this page
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