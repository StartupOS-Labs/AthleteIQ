package com.athleteiqapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LeaderboardEntry(
    var userId: String = "",
    var userName: String = "",
    var bestScore: Int = 0
) : Parcelable