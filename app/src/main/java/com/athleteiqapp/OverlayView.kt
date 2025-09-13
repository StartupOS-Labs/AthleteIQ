package com.athleteiqapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var keyPoints: List<KeyPoint> = listOf()
    private val pointPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
    }
    private val linePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val bodyJoints = listOf(
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        // Draw the points (joints)
        keyPoints.filter { it.score > 0.5f }.forEach { keyPoint ->
            canvas.drawCircle(keyPoint.coordinate.x, keyPoint.coordinate.y, 10f, pointPaint)
        }
        // Draw the lines (bones)
        bodyJoints.forEach {
            val start = keyPoints.find { k -> k.bodyPart == it.first && k.score > 0.5f }
            val end = keyPoints.find { k -> k.bodyPart == it.second && k.score > 0.5f }
            if (start != null && end != null) {
                canvas.drawLine(start.coordinate.x, start.coordinate.y, end.coordinate.x, end.coordinate.y, linePaint)
            }
        }
    }

    // This function is called by MainActivity to update the skeleton data
    fun setResults(personKeyPoints: List<KeyPoint>) {
        keyPoints = personKeyPoints
        invalidate() // This tells the view to redraw itself immediately
    }
}

