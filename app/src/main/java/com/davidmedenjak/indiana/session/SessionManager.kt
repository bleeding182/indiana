package com.davidmedenjak.indiana.session

import com.davidmedenjak.indiana.api.AuthApi
import com.davidmedenjak.indiana.app.UserSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val authApi: AuthApi,
    private val userSettings: UserSettings,
) {
    val apiToken: String?
        get() = userSettings.apiToken

    private val sessionState = MutableStateFlow(!apiToken.isNullOrBlank())
    val state: StateFlow<Boolean> = sessionState

    suspend fun authenticate(token: String): String? {
        try {
            val response = authApi.userProfile("token $token")
            if (response.isSuccessful) {
                userSettings.apiToken = token
                sessionState.emit(true)
                return response.body()?.data?.username
            }
            error("Error: " + response.code())
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    fun logout() {
        userSettings.apiToken = null
        sessionState.tryEmit(false)
    }
}
