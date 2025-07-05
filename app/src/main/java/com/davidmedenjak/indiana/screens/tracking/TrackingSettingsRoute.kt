package com.davidmedenjak.indiana.screens.tracking

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object TrackingSettingsGraph : NavKey

@Composable
fun TrackingSettingsRoute(
    navKey: TrackingSettingsGraph,
    onNavigateUp: () -> Unit,
) {
    TrackingSettingsScreen(
        onNavigateUp = onNavigateUp,
    )
}