package com.athleteiqapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
// ⭐ FIX: Change the import to your app's package
import com.athleteiqapp.R

class InstructionsActivity : AppCompatActivity() {

    private lateinit var exerciseTitle: TextView
    private lateinit var exerciseImage: ImageView
    private lateinit var instructionsText: TextView
    private lateinit var startButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        exerciseTitle = findViewById(R.id.exerciseTitle)
        exerciseImage = findViewById(R.id.exerciseImage)
        instructionsText = findViewById(R.id.instructionsText)
        startButton = findViewById(R.id.startButton)
        backButton = findViewById(R.id.backButton)

        val selectedExercise = intent.getStringExtra("SELECTED_EXERCISE") ?: "SQUATS"
        updateInstructions(selectedExercise)

        startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SELECTED_EXERCISE", selectedExercise)
            }
            startActivity(intent)
        }

        backButton.setOnClickListener {
            // Check if there is a parent activity in the stack
            if (isTaskRoot) {
                // If this is the only activity, navigate to HomeActivity
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                // Otherwise, just go back to the previous activity
                finish()
            }
        }
    }

    private fun updateInstructions(exercise: String) {
        when (exercise) {
            "SQUATS" -> {
                exerciseTitle.text = "Squats Assessment"
                // ⭐ FIX: Make sure these images exist in your res/drawable folder
                exerciseImage.setImageResource(R.drawable.squats_instructions_image)
                instructionsText.text = """
                    1. Stand with your feet shoulder-width apart.
                    2. Keep your back straight and chest up.
                    3. Lower your hips as if you are sitting down in a chair.
                    4. Go down until your thighs are parallel to the floor.
                    5. Push through your heels to return to the starting position.
                    6. Keep your knees in line with your feet.
                """.trimIndent()
            }
            "JUMPING_JACKS" -> {
                exerciseTitle.text = "Jumping Jacks"
                // ⭐ FIX: Make sure these images exist in your res/drawable folder
                exerciseImage.setImageResource(R.drawable.jumping_jack)
                instructionsText.text = """
                    1. Stand with your feet together and hands at your sides.
                    2. Jump while spreading your feet and raising your hands overhead.
                    3. Jump back to the starting position with your feet together and hands at your sides.
                    4. Keep your movements fluid and your core engaged.
                """.trimIndent()
            }
            else -> {
                exerciseTitle.text = "Generic Instructions"
                // ⭐ FIX: Make sure this image exists in your res/drawable folder
                exerciseImage.setImageResource(R.drawable.ic_generic_exercise)
                instructionsText.text = "Instructions for this exercise are not available."
            }
        }
    }
}