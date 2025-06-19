package com.davidmedenjak.indiana.screens.build

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.AppBackStack.RequiresLogin
import com.davidmedenjak.indiana.model.V0ArtifactListElementResponseModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class BuildDetailGraph(
    val projectTitle: String,
    val buildTitle: String,
    val appSlug: String,
    val buildSlug: String,
) : NavKey, RequiresLogin

@Composable
fun BuildDetailRoute(
    navKey: BuildDetailGraph,
    onNavigateUp: () -> Unit,
) {
    val viewModel = hiltViewModel<BuildDetailViewModel>()
    viewModel.navKey = navKey
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    BuildDetailScreen(
        projectName = navKey.projectTitle,
        buildName = navKey.buildTitle,
        artifacts = viewModel.pagedArtifacts,
        onNavigateUp = onNavigateUp,
        onArtifactSelected = {
            scope.launch {
                viewModel.downloadArtifact(it)
            }
        }
    )
}
