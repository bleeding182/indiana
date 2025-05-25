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

    companion object {
        private const val PREF_API_TOKEN = "api_token"
    }
}
