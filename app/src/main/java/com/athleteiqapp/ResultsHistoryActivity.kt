package com.athleteiqapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class ResultsHistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var historyContainer: LinearLayout
    private lateinit var loadingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results_history)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        historyContainer = findViewById(R.id.historyContainer)
        loadingTextView = findViewById(R.id.loadingTextView)

        val backButton: View = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        setupBottomNavigation()
        loadWorkoutHistory()
    }

    private fun loadWorkoutHistory() {
        val user = auth.currentUser
        if (user == null) {
            loadingTextView.text = "Please log in to view your history."
            return
        }

        loadingTextView.text = "Loading workout history..."

        db.collection("users").document(user.uid).collection("workouts")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                loadingTextView.visibility = View.GONE
                if (querySnapshot.isEmpty) {
                    loadingTextView.visibility = View.VISIBLE
                    loadingTextView.text = "No workout sessions found."
                    return@addOnSuccessListener
                }

                historyContainer.removeAllViews()
                for (document in querySnapshot.documents) {
                    val session = document.toObject(WorkoutSession::class.java)
                    if (session != null) {
                        addSessionToView(session)
                    }
                }
            }
            .addOnFailureListener { e ->
                loadingTextView.visibility = View.VISIBLE
                loadingTextView.text = "Failed to load history: ${e.message}"
                Log.e("ResultsHistory", "Error fetching history: $e")
                Toast.makeText(this, "Failed to load history.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addSessionToView(session: WorkoutSession) {
        val inflater = LayoutInflater.from(this)
        val sessionView = inflater.inflate(R.layout.list_item_session_history, historyContainer, false)

        val exerciseNameTextView: TextView = sessionView.findViewById(R.id.exerciseNameTextView)
        val repsCountTextView: TextView = sessionView.findViewById(R.id.repsCountTextView)
        val scoreTextView: TextView = sessionView.findViewById(R.id.scoreTextView)
        val dateTextView: TextView = sessionView.findViewById(R.id.dateTextView)

        exerciseNameTextView.text = session.exerciseName
        repsCountTextView.text = "Reps: ${session.totalReps}"
        scoreTextView.text = "Score: ${session.fitnessScore}"

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        dateTextView.text = dateFormat.format(session.date)

        historyContainer.addView(sessionView)
    }

    private fun setupBottomNavigation() {
        val homeButton: LinearLayout = findViewById(R.id.homeButton)
        val testsButton: LinearLayout = findViewById(R.id.testsButton)
        val resultsButton: LinearLayout = findViewById(R.id.resultsButton)
        val profileButton: LinearLayout = findViewById(R.id.profileButton)

        homeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        testsButton.setOnClickListener {
            startActivity(Intent(this, TestsActivity::class.java))
            finish()
        }
        resultsButton.setOnClickListener {
            // Do nothing, already on this page
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}