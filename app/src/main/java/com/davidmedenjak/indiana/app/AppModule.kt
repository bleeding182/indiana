package com.davidmedenjak.indiana.app

import android.app.Application
import android.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
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
    fun provideSharedPreferences(app: Application) = PreferenceManager.getDefaultSharedPreferences(app)

    @Provides
    @Singleton
    fun provideAnalytics(app: Application) = FirebaseAnalytics.getInstance(app)

    @Provides
    @Singleton
    fun appCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
