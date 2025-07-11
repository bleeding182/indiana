package com.davidmedenjak.indiana.app

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import androidx.work.WorkManager
import com.davidmedenjak.indiana.db.AppDatabase
import com.davidmedenjak.indiana.db.DownloadDao
import com.davidmedenjak.indiana.db.ProjectDao
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(app: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app)

    @Provides
    @Singleton
    fun provideAnalytics(app: Application) = FirebaseAnalytics.getInstance(app)

    @Provides
    @Singleton
    fun provideCrashlytics() = FirebaseCrashlytics.getInstance()

    @Provides
    @Singleton
    fun providePerformance() = FirebasePerformance.getInstance()

    @Provides
    @Singleton
    fun appCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun appDatabase(context: Application): AppDatabase =
        Room.databaseBuilder<AppDatabase>(context, "indiana").build()

    @Provides
    fun projectDao(database: AppDatabase): ProjectDao = database.projects()

    @Provides
    fun downloadDao(database: AppDatabase): DownloadDao = database.downloads()

    @Provides
    @Singleton
    fun provideWorkManager(context: Application): WorkManager =
        WorkManager.getInstance(context)
}
