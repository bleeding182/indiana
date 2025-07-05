package com.davidmedenjak.indiana.screens.privacy

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object PrivacyGraph : NavKey

@Composable
fun PrivacyRoute(
    navKey: PrivacyGraph,
    onNavigateUp: () -> Unit,
    onNavigateToTrackingSettings: () -> Unit,
) {
    PrivacyScreen(
        onNavigateUp = onNavigateUp,
        onNavigateToTrackingSettings = onNavigateToTrackingSettings,
    )
}
