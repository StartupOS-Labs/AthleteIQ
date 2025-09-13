package com.athleteiqapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class WorkoutSummaryActivity : AppCompatActivity() {

    private lateinit var fitnessScoreTextView: TextView
    private lateinit var percentileTextView: TextView
    private lateinit var rankTextView: TextView
    private lateinit var radarChart: RadarChart
    private lateinit var detailedTestResultsContainer: LinearLayout
    private lateinit var backButton: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_summary)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        backButton = findViewById(R.id.backButton)
        fitnessScoreTextView = findViewById(R.id.fitnessScoreTextView)
        percentileTextView = findViewById(R.id.percentileTextView)
        rankTextView = findViewById(R.id.rankTextView)
        radarChart = findViewById(R.id.radarChart)
        detailedTestResultsContainer = findViewById(R.id.detailedTestResultsContainer)

        val workoutSession = intent.getParcelableExtra<WorkoutSession>("WORKOUT_SESSION")

        if (workoutSession != null) {
            updateUI(workoutSession)
        } else {
            Toast.makeText(this, "Workout data not found.", Toast.LENGTH_LONG).show()
            // Set default UI values to prevent crash
            fitnessScoreTextView.text = "0/100"
            percentileTextView.text = "N/A"
            rankTextView.text = "N/A"
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, ResultsHistoryActivity::class.java))
            finish()
        }
    }

    private fun updateUI(session: WorkoutSession) {
        fitnessScoreTextView.text = "${session.fitnessScore}/100"

        // Fetch all scores from Firestore to calculate rank and percentile
        db.collectionGroup("workouts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val allScores = querySnapshot.toObjects(WorkoutSession::class.java).map { it.fitnessScore }
                if (allScores.isNotEmpty()) {
                    val rank = calculateRank(session.fitnessScore, allScores)
                    val percentile = calculatePercentile(session.fitnessScore, allScores)

                    percentileTextView.text = "${percentile}th"
                    rankTextView.text = "Top ${rank}%"
                } else {
                    percentileTextView.text = "N/A"
                    rankTextView.text = "N/A"
                }
            }
            .addOnFailureListener {
                percentileTextView.text = "N/A"
                rankTextView.text = "N/A"
                Toast.makeText(this, "Failed to load global data.", Toast.LENGTH_SHORT).show()
            }

        populateDetailedResults(detailedTestResultsContainer, session.exerciseName, session.totalReps)
        setupRadarChart(session.fitnessScore)
    }

    private fun calculateRank(userScore: Int, allScores: List<Int>): Int {
        if (allScores.isEmpty()) return 1
        val sortedScores = allScores.sortedDescending().distinct()
        val userRank = sortedScores.indexOf(userScore) + 1
        return userRank
    }

    private fun calculatePercentile(userScore: Int, allScores: List<Int>): Int {
        if (allScores.isEmpty()) return 100
        val scoresBelow = allScores.count { it < userScore }
        return ((scoresBelow.toFloat() / allScores.size.toFloat()) * 100).roundToInt()
    }

    private fun setupRadarChart(fitnessScore: Int) {
        val entries = ArrayList<RadarEntry>()
        val scoreFloat = fitnessScore.toFloat()

        entries.add(RadarEntry(scoreFloat + 10))
        entries.add(RadarEntry(scoreFloat + 15))
        entries.add(RadarEntry(scoreFloat - 5))
        entries.add(RadarEntry(scoreFloat + 5))
        entries.add(RadarEntry(scoreFloat - 10))
        entries.add(RadarEntry(scoreFloat))

        val dataSet = RadarDataSet(entries, "Skill Breakdown").apply {
            color = Color.parseColor("#48BB78")
            fillColor = Color.parseColor("#48BB78")
            setDrawFilled(true)
            fillAlpha = 180
            lineWidth = 2f
            isDrawHighlightCircleEnabled = true
            setDrawHighlightIndicators(false)
            valueTextColor = Color.TRANSPARENT
        }

        radarChart.apply {
            data = RadarData(dataSet)
            description.isEnabled = false
            webLineWidth = 1f
            webColor = Color.parseColor("#A0AEC0")
            webColorInner = Color.parseColor("#A0AEC0")
            animateXY(1400, 1400)
            legend.isEnabled = false

            xAxis.apply {
                textSize = 12f
                textColor = Color.WHITE
                valueFormatter = IndexAxisValueFormatter(arrayOf("Flexibility", "Strength", "Endurance", "Power", "Agility", "Speed"))
            }
            yAxis.apply {
                setDrawLabels(false)
                axisMinimum = 0f
            }
        }
    }

    private fun populateDetailedResults(container: LinearLayout, exercise: String, reps: Int) {
        val inflater = LayoutInflater.from(this)
        val resultView = inflater.inflate(R.layout.list_item_test_result, container, false)

        val testNameTextView: TextView = resultView.findViewById(R.id.testNameTextView)
        val yourScoreTextView: TextView = resultView.findViewById(R.id.yourScoreTextView)
        val avgScoreTextView: TextView = resultView.findViewById(R.id.avgScoreTextView)
        val statusTextView: TextView = resultView.findViewById(R.id.statusTextView)
        val progressBar: ProgressBar = resultView.findViewById(R.id.progressBar)

        testNameTextView.text = "$exercise (1 min)"
        yourScoreTextView.text = "Reps: $reps"
        val avgReps = if (exercise.equals("SQUATS", ignoreCase = true)) 40 else 35
        avgScoreTextView.text = "Avg. Reps: $avgReps"

        val progress = (reps * 100) / (avgReps + 15)
        progressBar.progress = progress

        val statusColor: Int
        when {
            progress > 80 -> {
                statusTextView.text = "Elite"
                statusColor = ContextCompat.getColor(this, R.color.status_elite)
            }
            progress > 50 -> {
                statusTextView.text = "Developing"
                statusColor = ContextCompat.getColor(this, R.color.status_developing)
            }
            else -> {
                statusTextView.text = "Needs Improvement"
                statusColor = ContextCompat.getColor(this, R.color.status_needs_improvement)
            }
        }
        statusTextView.setTextColor(statusColor)

        val layerDrawable = progressBar.progressDrawable as LayerDrawable
        val progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress)
        progressDrawable.setTint(statusColor)

        container.addView(resultView)
    }
}