package com.athleteiqapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    // --- State Variables ---
    private var totalRepCount = 0
    private var goodRepCount = 0
    private var squatState = "up"
    private var jjState = "center"
    private var currentExercise = "SQUATS"
    private var fitnessScore = 0

    // --- UI & Services ---
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var repsCounterTextView: TextView
    private lateinit var exerciseNameTextView: TextView
    private lateinit var overlayView: OverlayView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val CAMERA_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        currentExercise = intent.getStringExtra("SELECTED_EXERCISE") ?: "SQUATS"
        repsCounterTextView = findViewById(R.id.repsCounter)
        exerciseNameTextView = findViewById(R.id.exerciseName)
        overlayView = findViewById(R.id.overlay)
        val stopButton: Button = findViewById(R.id.stopButton)

        stopButton.setOnClickListener {
            // ⭐ FIX: Call the correct function here
            saveWorkoutSession()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        checkCameraPermission()
    }

    private fun saveWorkoutSession() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("MainActivity", "User not logged in. Cannot save session.")
            val dummySession = WorkoutSession(
                exerciseName = currentExercise,
                totalReps = totalRepCount,
                goodReps = goodRepCount,
                fitnessScore = 0,
                date = Date()
            )
            navigateToSummary(dummySession)
            return
        }

        val accuracy = if (totalRepCount > 0) (goodRepCount.toFloat() / totalRepCount.toFloat()) * 100 else 0f
        val repsScore = (totalRepCount.toFloat() / 80.0f) * 70f
        val formScore = (accuracy / 100) * 30f

        fitnessScore = (repsScore + formScore).toInt().coerceAtMost(100)

        // ⭐ FIX: Fetch the user's name before saving
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val userProfile = document.toObject(UserProfile::class.java)
                val userName = userProfile?.name ?: "Unknown User"

                val session = WorkoutSession(
                    exerciseName = currentExercise,
                    totalReps = totalRepCount,
                    goodReps = goodRepCount,
                    fitnessScore = fitnessScore,
                    date = Date(),
                    userId = user.uid, // ⭐ FIX: Save the userId
                    userName = userName // ⭐ FIX: Save the userName
                )

                db.collection("users").document(user.uid)
                    .collection("workouts")
                    .add(session)
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Workout session saved to Firestore.")
                        Toast.makeText(this, "Session saved successfully!", Toast.LENGTH_SHORT).show()
                        navigateToSummary(session)
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Error saving workout session: $e")
                        Toast.makeText(this, "Failed to save session. Please check your network and try again.", Toast.LENGTH_LONG).show()
                        navigateToSummary(session)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to fetch user name for saving.", e)
                Toast.makeText(this, "Failed to save. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val viewFinder: PreviewView = findViewById(R.id.viewFinder)
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(viewFinder.surfaceProvider) }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PoseAnalyzer(applicationContext) { keyPoints ->
                        processKeypoints(keyPoints)
                        runOnUiThread {
                            overlayView.setResults(keyPoints)
                            repsCounterTextView.text = "Reps: $totalRepCount"
                            exerciseNameTextView.text = currentExercise
                        }
                    })
                }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("MainActivity", "Use case binding failed", exc)
                Toast.makeText(this, "Camera failed to start: ${exc.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processKeypoints(keypoints: List<KeyPoint>) {
        if (currentExercise.equals("SQUATS", ignoreCase = true)) {
            processSquat(keypoints)
        } else {
            processJumpingJack(keypoints)
        }
    }

    private fun processSquat(keypoints: List<KeyPoint>) {
        val hip = keypoints.find { it.bodyPart == BodyPart.LEFT_HIP }
        val knee = keypoints.find { it.bodyPart == BodyPart.LEFT_KNEE }
        val ankle = keypoints.find { it.bodyPart == BodyPart.LEFT_ANKLE }

        if (hip != null && knee != null && ankle != null && hip.score > 0.5f && knee.score > 0.5f && ankle.score > 0.5f) {
            val angle = getAngle(hip, knee, ankle)
            if (angle < 120) { // User is in the "down" position
                squatState = "down"
            } else if (angle > 160 && squatState == "down") { // User has returned to the "up" position
                squatState = "up"
                totalRepCount++
                // Simple accuracy check: was the squat deep enough? A good rep goes below 100 degrees.
                if (angle < 100) {
                    goodRepCount++
                }
            }
        }
    }

    private fun processJumpingJack(keypoints: List<KeyPoint>) {
        val shoulder = keypoints.find { it.bodyPart == BodyPart.LEFT_SHOULDER }
        val elbow = keypoints.find { it.bodyPart == BodyPart.LEFT_ELBOW }
        val wrist = keypoints.find { it.bodyPart == BodyPart.LEFT_WRIST }

        if (shoulder != null && elbow != null && wrist != null && shoulder.score > 0.5f && elbow.score > 0.5f && wrist.score > 0.5f) {
            val angle = getAngle(shoulder, elbow, wrist)
            if (angle > 160) { // Arms are up
                jjState = "up"
            } else if (angle < 90 && jjState == "up") { // Arms have returned to center
                jjState = "center"
                totalRepCount++
                goodRepCount++ // For jumping jacks, we'll assume all reps are good for this prototype
            }
        }
    }

    private fun getAngle(p1: KeyPoint, p2: KeyPoint, p3: KeyPoint): Double {
        val p1c = p1.coordinate
        val p2c = p2.coordinate
        val p3c = p3.coordinate
        val rad = acos(((p2c.x - p1c.x) * (p2c.x - p3c.x) + (p2c.y - p1c.y) * (p2c.y - p3c.y)) /
                (sqrt((p1c.x - p2c.x).pow(2) + (p1c.y - p2c.y).pow(2)) *
                        sqrt((p3c.x - p2c.x).pow(2) + (p3c.y - p2c.y).pow(2))))
        return Math.toDegrees(rad.toDouble())
    }

    private fun navigateToSummary(session: WorkoutSession?) {
        val intent = Intent(this, WorkoutSummaryActivity::class.java).apply {
            putExtra("WORKOUT_SESSION", session)
        }
        startActivity(intent)
        finish()
    }

    private fun Float.pow(n: Int): Float = this.toDouble().pow(n.toDouble()).toFloat()

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}