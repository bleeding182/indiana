package com.davidmedenjak.indiana.screens.about

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.analytics.ScreenTrackable
import kotlinx.serialization.Serializable

@Serializable
data object AboutGraph : NavKey, ScreenTrackable {
    override val screenName = "About"
}

@Composable
fun AboutRoute(
    navKey: AboutGraph,
    onNavigateUp: () -> Unit,
) {
    AboutScreen(
        onNavigateUp = onNavigateUp,
    )
}
