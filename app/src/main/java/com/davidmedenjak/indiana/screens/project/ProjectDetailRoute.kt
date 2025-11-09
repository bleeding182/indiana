package com.davidmedenjak.indiana.screens.project

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.AppBackStack.RequiresLogin
import com.davidmedenjak.indiana.analytics.ScreenTrackable
import com.davidmedenjak.indiana.model.V0BuildResponseItemModel
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDetailGraph(
    val title: String,
    val slug: String,
) : NavKey, RequiresLogin, ScreenTrackable {
    override val screenName = "ProjectDetail"
}

@Composable
fun ProjectDetailRoute(
    navKey: ProjectDetailGraph,
    onNavigateUp: () -> Unit,
    navigateToBuild: (build: V0BuildResponseItemModel) -> Unit
) {
    val viewModel = hiltViewModel<ProjectDetailViewModel>()
    viewModel.navKey = navKey

    ProjectDetailScreen(
        projectName = navKey.title,
        builds = viewModel.pagedBuilds,
        onNavigateUp = onNavigateUp,
        onBuildSelected = navigateToBuild
    )
}
