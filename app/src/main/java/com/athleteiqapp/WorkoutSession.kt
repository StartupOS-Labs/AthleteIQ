package com.athleteiqapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class WorkoutSession(
    var exerciseName: String = "",
    var totalReps: Int = 0,
    var goodReps: Int = 0,
    var fitnessScore: Int = 0,
    var date: Date = Date(),
    var stability: Int = 0,
    // ⭐ FIX: Add userName and userId fields to the data class
    var userId: String = "",
    var userName: String = ""
) : Parcelable