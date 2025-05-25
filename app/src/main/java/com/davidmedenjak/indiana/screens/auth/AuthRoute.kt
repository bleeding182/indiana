package com.davidmedenjak.indiana.screens.auth

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object AuthGraph : NavKey

@Composable
fun AuthRoute(
    navKey: AuthGraph,
    onAboutSelected: () -> Unit,
    onPrivacySelected: () -> Unit,
) {
    val viewModel = hiltViewModel<AuthViewModel>()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    AuthScreen(
        onTryToken = { token ->
            scope.launch {
                try {
                    val name = viewModel.tryToken(token)
                    Toast.makeText(context, "Hello $name!", Toast.LENGTH_SHORT).show()
                } catch (ex: Exception) {
                    Toast.makeText(context, "Token invalid", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onAboutSelected = onAboutSelected,
        onPrivacySelected = onPrivacySelected,
    )
}
