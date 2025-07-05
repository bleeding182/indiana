package com.davidmedenjak.indiana.screens.tracking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidmedenjak.indiana.app.UserSettings
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingSettingsViewModel @Inject constructor(
    private val userSettings: UserSettings,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val firebasePerformance: FirebasePerformance,
) : ViewModel() {

    var analytics by mutableStateOf(userSettings.analyticsEnabled)
        private set

    var crashlytics by mutableStateOf(userSettings.crashlyticsEnabled)
        private set

    var performance by mutableStateOf(userSettings.performanceEnabled)
        private set

    fun setAnalyticsEnabled(enabled: Boolean) {
        analytics = enabled
        userSettings.analyticsEnabled = enabled
        viewModelScope.launch {
            firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
        }
    }

    fun setCrashlyticsEnabled(enabled: Boolean) {
        crashlytics = enabled
        userSettings.crashlyticsEnabled = enabled
        viewModelScope.launch {
            firebaseCrashlytics.setCrashlyticsCollectionEnabled(enabled)
        }
    }

    fun setPerformanceEnabled(enabled: Boolean) {
        performance = enabled
        userSettings.performanceEnabled = enabled
        viewModelScope.launch {
            firebasePerformance.isPerformanceCollectionEnabled = enabled
        }
    }
}