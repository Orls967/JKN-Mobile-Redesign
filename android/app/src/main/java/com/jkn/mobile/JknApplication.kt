package com.jkn.mobile

import android.app.Application
import com.google.firebase.FirebaseApp

import com.jkn.mobile.utils.NotificationHelper

class JknApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Story 1.5 — Firebase Crashlytics initialization
        // FirebaseApp must be initialized before any Firebase service is used
        FirebaseApp.initializeApp(this)

        // Setup Notification Channel (Android 13+)
        NotificationHelper.createNotificationChannel(this)
    }
}
