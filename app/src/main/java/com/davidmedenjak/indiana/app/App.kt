package com.davidmedenjak.indiana.app

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.perf.performance
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApplicationEntryPoint {
    val userSettings: UserSettings
}

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.analytics.setUserId(null)
        
        // Initialize Firebase services based on user settings
        val entryPoint = EntryPoints.get(this, ApplicationEntryPoint::class.java)
        val userSettings = entryPoint.userSettings
        
        Firebase.analytics.setAnalyticsCollectionEnabled(userSettings.analyticsEnabled)
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = userSettings.crashlyticsEnabled
        Firebase.performance.isPerformanceCollectionEnabled = userSettings.performanceEnabled
    }
}
