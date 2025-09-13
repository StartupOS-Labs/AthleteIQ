package com.athleteiqapp

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
import kotlin.math.roundToInt
import com.google.firebase.firestore.DocumentId

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var leaderboardContainer: LinearLayout
    private lateinit var loadingTextView: TextView
    private lateinit var backButton: View
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        leaderboardContainer = findViewById(R.id.leaderboardContainer)
        loadingTextView = findViewById(R.id.loadingTextView)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        loadingTextView.visibility = View.VISIBLE
        loadingTextView.text = "Loading leaderboard..."

        db.collectionGroup("workouts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                loadingTextView.visibility = View.GONE
                val allWorkouts = querySnapshot.toObjects(WorkoutSession::class.java)

                if (allWorkouts.isEmpty()) {
                    loadingTextView.visibility = View.VISIBLE
                    loadingTextView.text = "No workouts found."
                    return@addOnSuccessListener
                }

                val leaderboardEntries = allWorkouts
                    .groupBy { it.userId }
                    .map { (userId, workouts) ->
                        val bestScore = workouts.maxByOrNull { it.fitnessScore }?.fitnessScore ?: 0
                        LeaderboardEntry(userId, "Loading...", bestScore)
                    }
                    .sortedByDescending { it.bestScore }
                    .distinctBy { it.userId }

                val userIds = leaderboardEntries.map { it.userId }.take(10)
                if (userIds.isNotEmpty()) {
                    db.collection("users")
                        .whereIn("id", userIds)
                        .get()
                        .addOnSuccessListener { usersQuery ->
                            val userProfiles = usersQuery.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                            val userNames = userProfiles.associateBy({ it.id }, { it.name })

                            val finalLeaderboard = leaderboardEntries.map { entry ->
                                entry.copy(userName = userNames[entry.userId] ?: "Unknown User")
                            }

                            leaderboardContainer.removeAllViews()
                            finalLeaderboard.forEachIndexed { index, entry ->
                                addLeaderboardEntryToView(entry, index + 1)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("LeaderboardActivity", "Error fetching user names for leaderboard", e)
                            loadingTextView.visibility = View.VISIBLE
                            loadingTextView.text = "Failed to load leaderboard."
                            Toast.makeText(this, "Failed to load leaderboard data.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    loadingTextView.visibility = View.VISIBLE
                    loadingTextView.text = "No workout data to display on leaderboard."
                }
            }
            .addOnFailureListener { e ->
                Log.e("LeaderboardActivity", "Error loading leaderboard data", e)
                loadingTextView.visibility = View.VISIBLE
                loadingTextView.text = "Failed to load leaderboard."
                Toast.makeText(this, "Failed to load leaderboard data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addLeaderboardEntryToView(entry: LeaderboardEntry, rank: Int) {
        val inflater = LayoutInflater.from(this)
        val entryView = inflater.inflate(R.layout.list_item_leaderboard, leaderboardContainer, false)

        val rankTextView: TextView = entryView.findViewById(R.id.rankTextView)
        val userNameTextView: TextView = entryView.findViewById(R.id.userNameTextView)
        val bestScoreTextView: TextView = entryView.findViewById(R.id.bestScoreTextView)

        rankTextView.text = rank.toString()
        userNameTextView.text = entry.userName
        bestScoreTextView.text = entry.bestScore.toString()

        leaderboardContainer.addView(entryView)
    }

    // You will need to define your data classes and constants outside of this class
}