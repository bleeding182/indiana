package com.davidmedenjak.indiana.app

import android.content.SharedPreferences
import androidx.core.content.edit
import com.davidmedenjak.indiana.settings.CleanupPeriod
import com.davidmedenjak.indiana.settings.DownloadCleanupSettings
import dagger.Reusable
import java.time.Instant
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

    var downloadCleanupSettings: DownloadCleanupSettings
        get() = DownloadCleanupSettings(
            cleanupPeriod = CleanupPeriod.fromDays(preferences.getInt(PREF_CLEANUP_PERIOD_DAYS, 3)),
            lastCleanupTime = preferences.getLong(PREF_LAST_CLEANUP_TIME, 0L).takeIf { it > 0 }?.let { Instant.ofEpochMilli(it) },
            isEnabled = preferences.getBoolean(PREF_CLEANUP_ENABLED, true)
        )
        set(value) = preferences.edit {
            putInt(PREF_CLEANUP_PERIOD_DAYS, value.cleanupPeriod.days)
            putLong(PREF_LAST_CLEANUP_TIME, value.lastCleanupTime?.toEpochMilli() ?: 0L)
            putBoolean(PREF_CLEANUP_ENABLED, value.isEnabled)
        }

    companion object {
        private const val PREF_API_TOKEN = "api_token"
        private const val PREF_PROJECT_FILTER = "project_filter"
        private const val PREF_ANALYTICS_ENABLED = "analytics_enabled"
        private const val PREF_CRASHLYTICS_ENABLED = "crashlytics_enabled"
        private const val PREF_PERFORMANCE_ENABLED = "performance_enabled"
        private const val PREF_CLEANUP_PERIOD_DAYS = "cleanup_period_days"
        private const val PREF_LAST_CLEANUP_TIME = "last_cleanup_time"
        private const val PREF_CLEANUP_ENABLED = "cleanup_enabled"
    }
}
