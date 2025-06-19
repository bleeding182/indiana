package com.davidmedenjak.indiana.screens.auth

import androidx.lifecycle.ViewModel
import com.davidmedenjak.indiana.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {

    suspend fun tryToken(token: String): String? = sessionManager.authenticate(token)
}
