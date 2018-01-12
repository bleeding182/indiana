package com.davidmedenjak.indiana.app

import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(app : App) = PreferenceManager.getDefaultSharedPreferences(app)

}