package com.davidmedenjak.indiana.screens.auth

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.analytics.ScreenTrackable
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object AuthGraph : NavKey, ScreenTrackable {
    override val screenName = "Authentication"
}

@Composable
fun AuthRoute(
    navKey: AuthGraph,
    onAboutSelected: () -> Unit,
    onPrivacySelected: () -> Unit,
) {
    val viewModel = hiltViewModel<AuthViewModel>()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)
    LaunchedEffect(Unit) {
        try {
            val getPasswordOption = GetPasswordOption()
            val credRequest = GetCredentialRequest(listOf(getPasswordOption))
            val result = credentialManager.getCredential(
                context = context,
                request = credRequest
            )

            when (val credential = result.credential) {
                is PasswordCredential -> {
                    try {
                        viewModel.tryToken(credential.password)
                    } catch (ex: Exception) {
                        Toast.makeText(context, "Token invalid", Toast.LENGTH_SHORT).show()
                    }
                }

                else -> null
            }
        } catch (e: GetCredentialException) {
        }
    }

    AuthScreen(
        onTryToken = { token ->
            scope.launch { authenticate(viewModel, token, context) }
        },
        onAboutSelected = onAboutSelected,
        onPrivacySelected = onPrivacySelected,
    )
}

private suspend fun authenticate(viewModel: AuthViewModel, token: String, context: Context) {
    try {
        val name = viewModel.tryToken(token)
        val request = CreatePasswordRequest(
            id = "bitrise.io ${name ?: ""}".trim(),
            password = token,
        )

        (context as ComponentActivity).lifecycleScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.createCredential(
                    context = context,
                    request = request
                )
            } catch (e: CreateCredentialException) {
                Toast.makeText(context, "Hello $name!", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (ex: Exception) {
        Toast.makeText(context, "Token invalid", Toast.LENGTH_SHORT).show()
    }
}
