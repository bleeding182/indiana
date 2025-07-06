package com.davidmedenjak.indiana.screens.build

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.AppBackStack.RequiresLogin
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
    onNavigateToBuild: (BuildDetailGraph) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val viewModel = hiltViewModel<BuildDetailViewModel>()
    viewModel.navKey = navKey
    val scope = rememberCoroutineScope()

    LaunchedEffect(navKey) {
        viewModel.loadBuildDetails()
    }

    val newBuild by viewModel.newlyStartedBuild.collectAsState()
    LaunchedEffect(newBuild) {
        val newBuild = newBuild ?: return@LaunchedEffect
        viewModel.newlyStartedBuild.value = null
        onNavigateToBuild(newBuild)
    }

    BuildDetailScreen(
        projectName = navKey.projectTitle,
        buildName = navKey.buildTitle,
        buildDetails = viewModel.buildDetails,
        isLoadingBuildDetails = viewModel.isLoadingBuildDetails,
        buildDetailsError = viewModel.buildDetailsError,
        artifacts = viewModel.pagedArtifacts,
        onNavigateUp = onNavigateUp,
        onArtifactSelected = {
            scope.launch {
                viewModel.downloadArtifact(it)
            }
        },
        onAbortBuild = { reason ->
            viewModel.abortBuild(reason)
        },
        onRestartBuild = {
            viewModel.restartBuild()
        },
        onRetryLoadBuildDetails = {
            viewModel.loadBuildDetails()
        }
    )
}
