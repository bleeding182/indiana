package com.davidmedenjak.indiana.app

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Reusable
import javax.inject.Inject

@Reusable
class UserSettings @Inject constructor(private val preferences: SharedPreferences) {

    var apiToken
        get() = preferences.getString(PREF_API_TOKEN, "")
        set(value) = preferences.edit { putString(PREF_API_TOKEN, value) }

    var projectFiler: String?
        get() = preferences.getString(PREF_PROJECT_FILTER, null)
        set(value) = preferences.edit { putString(PREF_PROJECT_FILTER, value) }

    var analyticsEnabled: Boolean
        get() = preferences.getBoolean(PREF_ANALYTICS_ENABLED, true)
        set(value) = preferences.edit { putBoolean(PREF_ANALYTICS_ENABLED, value) }

    var crashlyticsEnabled: Boolean
        get() = preferences.getBoolean(PREF_CRASHLYTICS_ENABLED, true)
        set(value) = preferences.edit { putBoolean(PREF_CRASHLYTICS_ENABLED, value) }

    var performanceEnabled: Boolean
        get() = preferences.getBoolean(PREF_PERFORMANCE_ENABLED, true)
        set(value) = preferences.edit { putBoolean(PREF_PERFORMANCE_ENABLED, value) }

    companion object {
        private const val PREF_API_TOKEN = "api_token"
        private const val PREF_PROJECT_FILTER = "project_filter"
        private const val PREF_ANALYTICS_ENABLED = "analytics_enabled"
        private const val PREF_CRASHLYTICS_ENABLED = "crashlytics_enabled"
        private const val PREF_PERFORMANCE_ENABLED = "performance_enabled"
    }
}
