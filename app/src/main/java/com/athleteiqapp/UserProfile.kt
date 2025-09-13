package com.athleteiqapp

import com.google.firebase.firestore.DocumentId
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    var name: String = "",
    var age: Int = 0,
    var gender: String = "",
    var location: String = "",
    var dateOfBirth: String = "",
    @DocumentId
    var id: String = "" // ⭐ FIX: This is a crucial field for the leaderboard to work
) : Parcelable