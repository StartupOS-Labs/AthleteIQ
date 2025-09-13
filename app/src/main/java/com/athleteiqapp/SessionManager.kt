package com.athleteiqapp

import android.content.Context
import com.google.gson.Gson

object SessionManager {

    private const val PREF_NAME = "UserSession"
    private const val KEY_USER_EMAIL = "user_email"
    private lateinit var sharedPreferences: android.content.SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var currentUserEmail: String?
        get() = sharedPreferences.getString(KEY_USER_EMAIL, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_USER_EMAIL, value).apply()
        }

    fun logout() {
        sharedPreferences.edit().remove(KEY_USER_EMAIL).apply()
    }

    // ⭐ FIX: Check if the user email key exists in SharedPreferences
    fun isLoggedIn(): Boolean = currentUserEmail != null

    fun getUserProfile(context: Context): UserProfile? {
        val userEmail = currentUserEmail ?: return null
        val prefs = context.getSharedPreferences("AthleteIQPrefs", Context.MODE_PRIVATE)
        val profileJson = prefs.getString("${userEmail}_profile", null)
        return if (profileJson != null) Gson().fromJson(profileJson, UserProfile::class.java) else null
    }
}