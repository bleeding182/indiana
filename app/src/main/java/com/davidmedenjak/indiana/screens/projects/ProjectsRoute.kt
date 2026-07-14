package com.davidmedenjak.indiana.screens.projects

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.appupdate.rememberAppUpdateState
import com.davidmedenjak.indiana.AppBackStack.RequiresLogin
import com.davidmedenjak.indiana.analytics.ScreenTrackable
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days

@Serializable
object ProjectsGraph : NavKey, RequiresLogin, ScreenTrackable {
    override val screenName = "ProjectsList"
}

@Composable
fun ProjectsRoute(
    navKey: ProjectsGraph,
    navigateToProject: (project: Project) -> Unit,
    onAboutSelected: () -> Unit,
    onPrivacySelected: () -> Unit,
    onDownloadCleanupSelected: () -> Unit,
    onLogoutSelected: () -> Unit,
) {
    val viewModel = hiltViewModel<ProjectsViewModel>()
    val updateState = rememberAppUpdateState()

    ProjectsScreen(
        projects = viewModel.pagedProjects,
        recents = viewModel.recents,
        projectTypes = viewModel.projectTypes,
        filteredProjectTypes = viewModel.filteredProjectTypes,
        onProjectSelected = {
            navigateToProject(it)
            viewModel.updateRecents(it)
        },
        onAboutSelected = onAboutSelected,
        onPrivacySelected = onPrivacySelected,
        onDownloadCleanupSelected = onDownloadCleanupSelected,
        onLogoutSelected = onLogoutSelected,
        onUpdateSelected = { updateState.startFlexibleUpdate() },
        onCompleteUpdate = { updateState.completeFlexibleUpdate() },
        toggleFilterProjectType = viewModel::setFilterProjectType,
        updateAvailability = updateState.availability,
        installStatus = updateState.installStatus,
        hasUpdateForMoreThanThreeDays = updateState.hasNewVersionAvailableForMoreThan(3.days),
    )
}
