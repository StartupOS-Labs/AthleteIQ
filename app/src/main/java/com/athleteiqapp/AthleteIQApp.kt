package com.athleteiqapp

import android.app.Application

class AthleteIQApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}