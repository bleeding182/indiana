package com.davidmedenjak.indiana

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.screens.auth.AuthGraph
import com.davidmedenjak.indiana.screens.projects.ProjectsGraph
import com.davidmedenjak.indiana.session.SessionManager
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityScoped
class AppBackStack @Inject constructor(
    val sessionManager: SessionManager,
    val appCoroutineScope: CoroutineScope,
) {
    private val loginRoute = AuthGraph

    interface RequiresLogin

    private var onLoginSuccessRoute: NavKey? = null

    private var isLoggedIn by mutableStateOf(false)
        private set

    val backStack =
        mutableStateListOf<NavKey>(if (sessionManager.state.value) ProjectsGraph else loginRoute)

    fun add(route: NavKey) {
        if (route is RequiresLogin && !isLoggedIn) {
            // Store the intended destination and redirect to login
            onLoginSuccessRoute = route
            backStack.add(AuthGraph)
        } else {
            backStack.add(route)
        }

        // If the user explicitly requested the login route, don't redirect them after login
        if (route == loginRoute) {
            onLoginSuccessRoute = null
        }
    }

    fun remove(): NavKey? = backStack.removeLastOrNull()
    fun logout() {
        backStack.clear()
        backStack.add(AuthGraph)
        sessionManager.logout()
    }

    init {
        appCoroutineScope.launch {
            sessionManager.state.collect { isLoggedIn ->
                this@AppBackStack.isLoggedIn = isLoggedIn
                if (isLoggedIn) {
                    val route = onLoginSuccessRoute ?: ProjectsGraph
                    backStack.add(route)
                    backStack.remove(loginRoute)
                } else {
                    backStack.removeAll { it is RequiresLogin }
                    if (backStack.isEmpty()) {
                        backStack.add(loginRoute)
                    }
                }
            }
        }
    }
}