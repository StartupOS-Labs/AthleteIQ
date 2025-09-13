package com.athleteiqapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.math.roundToInt
import com.google.firebase.firestore.DocumentId // Import DocumentId

// ⭐ FIX: Remove the data class definitions from here.
// They should be in their own files: UserProfile.kt and LeaderboardEntry.kt.

class ProfileActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userIdTextView: TextView
    private lateinit var totalTestsTextView: TextView
    private lateinit var averageScoreTextView: TextView
    private lateinit var bestScoreTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var dobTextView: TextView

    private lateinit var logOutButton: Button
    private lateinit var homeButton: LinearLayout
    private lateinit var testsButton: LinearLayout
    private lateinit var resultsButton: LinearLayout
    private lateinit var profileButton: LinearLayout

    private lateinit var personalStatsContainer: LinearLayout
    private lateinit var leaderboardContentContainer: LinearLayout
    private lateinit var toggleButton: Button
    private lateinit var titleTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var isShowingLeaderboard = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        userNameTextView = findViewById(R.id.userNameTextView)
        userIdTextView = findViewById(R.id.userIdTextView)
        totalTestsTextView = findViewById(R.id.totalTestsTextView)
        averageScoreTextView = findViewById(R.id.averageScoreTextView)
        bestScoreTextView = findViewById(R.id.bestScoreTextView)
        genderTextView = findViewById(R.id.genderTextView)
        locationTextView = findViewById(R.id.locationTextView)
        dobTextView = findViewById(R.id.dobTextView)

        personalStatsContainer = findViewById(R.id.personalStatsContainer)
        leaderboardContentContainer = findViewById(R.id.leaderboardContentContainer)
        toggleButton = findViewById(R.id.toggleButton)
        titleTextView = findViewById(R.id.titleTextView)

        val editProfileButton: Button = findViewById(R.id.editProfileButton)
        logOutButton = findViewById(R.id.logOutButton)

        homeButton = findViewById(R.id.homeButton)
        testsButton = findViewById(R.id.testsButton)
        resultsButton = findViewById(R.id.resultsButton)
        profileButton = findViewById(R.id.profileButton)

        toggleButton.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        logOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }



        setupBottomNavigation()
        showPersonalStats()
    }

    private fun showPersonalStats() {

        toggleButton.text = "Leaderboard"
        personalStatsContainer.visibility = View.VISIBLE
        leaderboardContentContainer.visibility = View.GONE
        isShowingLeaderboard = false
        loadPersonalStats()
    }

    private fun showLeaderboard() {

        toggleButton.text = "Your Stats"
        personalStatsContainer.visibility = View.GONE
        leaderboardContentContainer.visibility = View.VISIBLE
        isShowingLeaderboard = true
        loadLeaderboard()
    }

    private fun loadPersonalStats() {
        val user = auth.currentUser
        if (user == null) {
            userNameTextView.text = "Guest"
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val userProfile = document.toObject(UserProfile::class.java)
                if (userProfile != null) {
                    userNameTextView.text = userProfile.name
                    genderTextView.text = "Gender: ${userProfile.gender}"
                    locationTextView.text = "Location: ${userProfile.location}"
                    dobTextView.text = "Date of Birth: ${userProfile.dateOfBirth}"
                }
                loadWorkoutSummary(user.uid)
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error fetching user profile", e)
                Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadWorkoutSummary(userId: String) {
        totalTestsTextView.text = "0"
        averageScoreTextView.text = "0"
        bestScoreTextView.text = "0"

        db.collection("users").document(userId).collection("workouts")
            .get()
            .addOnSuccessListener { result ->
                val historyList = result.toObjects(WorkoutSession::class.java)

                val totalTests = historyList.size
                val averageScore = if (totalTests > 0) historyList.map { it.fitnessScore }.average().roundToInt() else 0
                val bestScore = if (historyList.isNotEmpty()) historyList.maxByOrNull { it.fitnessScore }?.fitnessScore ?: 0 else 0

                totalTestsTextView.text = totalTests.toString()
                averageScoreTextView.text = averageScore.toString()
                bestScoreTextView.text = bestScore.toString()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error fetching workout summary", e)
                Toast.makeText(this, "Failed to load workout summary.", Toast.LENGTH_SHORT).show()
            }
    }

    // ProfileActivity.kt
    private fun loadLeaderboard() {
        db.collectionGroup("workouts")
            .orderBy("fitnessScore", Query.Direction.DESCENDING) // ⭐ FIX: Order by fitnessScore
            .get()
            .addOnSuccessListener { querySnapshot ->
                val allWorkouts = querySnapshot.toObjects(WorkoutSession::class.java)

                // Group by userId to find the best score for each user
                val leaderboardEntries = allWorkouts
                    .groupBy { it.userId }
                    .map { (_, workouts) ->
                        val bestScore = workouts.maxByOrNull { it.fitnessScore }?.fitnessScore ?: 0
                        val userName = workouts.firstOrNull()?.userName ?: "Unknown User" // ⭐ FIX: Get the userName from the first workout session
                        LeaderboardEntry(workouts.first().userId, userName, bestScore)
                    }
                    .sortedByDescending { it.bestScore }
                    .distinctBy { it.userId }

                leaderboardContentContainer.removeAllViews()
                leaderboardEntries.forEachIndexed { index, entry ->
                    addLeaderboardEntryToView(entry, index + 1)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error loading leaderboard data", e)
                Toast.makeText(this, "Failed to load leaderboard data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addLeaderboardEntryToView(entry: LeaderboardEntry, rank: Int) {
        val inflater = LayoutInflater.from(this)
        val entryView = inflater.inflate(R.layout.list_item_leaderboard, leaderboardContentContainer, false)

        val rankTextView: TextView = entryView.findViewById(R.id.rankTextView)
        val userNameTextView: TextView = entryView.findViewById(R.id.userNameTextView)
        val bestScoreTextView: TextView = entryView.findViewById(R.id.bestScoreTextView)

        rankTextView.text = rank.toString()
        userNameTextView.text = entry.userName
        bestScoreTextView.text = entry.bestScore.toString()

        leaderboardContentContainer.addView(entryView)
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
            startActivity(Intent(this, ResultsHistoryActivity::class.java))
            finish()
        }
        profileButton.setOnClickListener {
            // Do nothing, already on this page
        }
    }
}