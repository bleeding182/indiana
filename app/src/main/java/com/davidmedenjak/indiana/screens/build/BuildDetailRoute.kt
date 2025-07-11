package com.davidmedenjak.indiana.screens.build

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.AppBackStack.RequiresLogin
import com.davidmedenjak.indiana.analytics.ScreenTrackable
import com.davidmedenjak.indiana.download.ArtifactClickResult
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class BuildDetailGraph(
    val projectTitle: String,
    val buildTitle: String,
    val appSlug: String,
    val buildSlug: String,
) : NavKey, RequiresLogin, ScreenTrackable {
    override val screenName = "BuildDetail"
}

@Composable
fun BuildDetailRoute(
    navKey: BuildDetailGraph,
    onNavigateToBuild: (BuildDetailGraph) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val viewModel = hiltViewModel<BuildDetailViewModel>()
    viewModel.navKey = navKey
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(navKey) {
        viewModel.loadBuildDetails()
    }

    val newBuild by viewModel.newlyStartedBuild.collectAsState()
    LaunchedEffect(newBuild) {
        val newBuild = newBuild ?: return@LaunchedEffect
        viewModel.newlyStartedBuild.value = null
        onNavigateToBuild(newBuild)
    }

    // Cancel auto-open when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onScreenLeft()
        }
    }

    BuildDetailScreen(
        projectName = navKey.projectTitle,
        buildName = navKey.buildTitle,
        buildDetails = viewModel.buildDetails,
        isLoadingBuildDetails = viewModel.isLoadingBuildDetails,
        buildDetailsError = viewModel.buildDetailsError,
        artifacts = viewModel.pagedArtifacts,
        getDownloadForArtifact = { artifactId ->
            viewModel.getDownloadForArtifact(artifactId)
        },
        onNavigateUp = onNavigateUp,
        onArtifactSelected = { artifact ->
            scope.launch {
                val result = viewModel.handleArtifactClick(artifact)
                when (result) {
                    is ArtifactClickResult.DownloadStarted -> {
                        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
                    }
                    is ArtifactClickResult.DownloadInProgress -> {
                        Toast.makeText(context, "Download already in progress", Toast.LENGTH_SHORT).show()
                    }
                    is ArtifactClickResult.FileOpened -> {
                        // File was opened successfully, no toast needed
                    }
                    is ArtifactClickResult.Error -> {
                        Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                    }
                }
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
        },
    )
}
