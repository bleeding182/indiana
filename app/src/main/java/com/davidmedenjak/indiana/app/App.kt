package com.davidmedenjak.indiana.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.davidmedenjak.indiana.work.FileCleanupWorker
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.perf.performance
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApplicationEntryPoint {
    val userSettings: UserSettings
    val workerFactory: HiltWorkerFactory
}

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        Firebase.analytics.setUserId(null)
        
        // Initialize Firebase services based on user settings
        val entryPoint = EntryPoints.get(this, ApplicationEntryPoint::class.java)
        val userSettings = entryPoint.userSettings
        
        Firebase.analytics.setAnalyticsCollectionEnabled(userSettings.analyticsEnabled)
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = userSettings.crashlyticsEnabled
        Firebase.performance.isPerformanceCollectionEnabled = userSettings.performanceEnabled
        
        scheduleFileCleanupWorker()
    }
    
    private fun scheduleFileCleanupWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val fileCleanupWork = PeriodicWorkRequestBuilder<FileCleanupWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "file_cleanup_work",
            ExistingPeriodicWorkPolicy.KEEP,
            fileCleanupWork
        )
    }

    override val workManagerConfiguration: Configuration
        get() {
            val entryPoint = EntryPoints.get(this, ApplicationEntryPoint::class.java)
            return Configuration.Builder()
                .setWorkerFactory(entryPoint.workerFactory)
                .build()
        }
}
