package com.davidmedenjak.indiana.app

import android.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(app : App) = PreferenceManager.getDefaultSharedPreferences(app)

    @Provides
    @Singleton
    fun provideAnalytics(app : App) = FirebaseAnalytics.getInstance(app)

}