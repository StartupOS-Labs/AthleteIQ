package com.athleteiqapp

import android.graphics.PointF

// This is a simple data structure to hold the information for a single body point.
// 'bodyPart' tells us if it's a nose, elbow, etc.
// 'coordinate' is the (x, y) position on the screen.
// 'score' is the AI's confidence that it found the point correctly.
data class KeyPoint(val bodyPart: BodyPart, val coordinate: PointF, val score: Float)

// This gives a clear, readable name to each of the 17 keypoints the model can detect.
enum class BodyPart {
    NOSE,
    LEFT_EYE,
    RIGHT_EYE,
    LEFT_EAR,
    RIGHT_EAR,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE
}

