package com.davidmedenjak.indiana.screens.tracking

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.analytics.ScreenTrackable
import kotlinx.serialization.Serializable

@Serializable
data object TrackingSettingsGraph : NavKey, ScreenTrackable {
    override val screenName = "TrackingSettings"
}

@Composable
fun TrackingSettingsRoute(
    navKey: TrackingSettingsGraph,
    onNavigateUp: () -> Unit,
) {
    TrackingSettingsScreen(
        onNavigateUp = onNavigateUp,
    )
}