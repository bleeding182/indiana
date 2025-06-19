package com.davidmedenjak.indiana.screens.projects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.AppBackStack.RequiresLogin
import com.davidmedenjak.indiana.model.V0AppResponseItemModel
import kotlinx.serialization.Serializable

@Serializable
object ProjectsGraph : NavKey, RequiresLogin

@Composable
fun ProjectsRoute(
    navKey: ProjectsGraph,
    navigateToProject: (project: V0AppResponseItemModel) -> Unit,
    onAboutSelected: () -> Unit,
    onPrivacySelected: () -> Unit,
    onLogoutSelected: () -> Unit,
) {
    val viewModel = hiltViewModel<ProjectsViewModel>()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    ProjectsScreen(
        projects = viewModel.pagedProjects,
        onProjectSelected = navigateToProject,
        onAboutSelected = onAboutSelected,
        onPrivacySelected = onPrivacySelected,
        onLogoutSelected = onLogoutSelected,
    )
}
