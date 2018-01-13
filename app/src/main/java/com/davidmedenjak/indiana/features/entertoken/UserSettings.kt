package com.davidmedenjak.indiana.features.entertoken

import android.content.SharedPreferences
import dagger.Reusable
import javax.inject.Inject

@Reusable
class UserSettings @Inject constructor(val preferences: SharedPreferences) {

    var apiToken
        get() = preferences.getString(PREF_API_TOKEN, "")
        set(value) = preferences.edit().putString(PREF_API_TOKEN, value).apply()

    companion object {
        private const val PREF_API_TOKEN = "api_token"
    }

}