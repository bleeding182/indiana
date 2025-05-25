package com.davidmedenjak.indiana.screens.about

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AboutGraph : NavKey

@Composable
fun AboutRoute(
    navKey: AboutGraph,
    onNavigateUp: () -> Unit,
) {
    AboutScreen(
        onNavigateUp = onNavigateUp,
    )
}
