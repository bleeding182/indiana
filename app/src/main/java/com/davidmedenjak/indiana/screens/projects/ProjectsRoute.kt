package com.davidmedenjak.indiana.screens.projects

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.AppBackStack.RequiresLogin
import com.davidmedenjak.indiana.analytics.ScreenTrackable
import com.davidmedenjak.indiana.app.InAppUpdateManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UpdateManagerEntryPoint {
    val updateManager: InAppUpdateManager
}

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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val updateManager = EntryPointAccessors.fromApplication(
        context.applicationContext,
        UpdateManagerEntryPoint::class.java
    ).updateManager

    val updateState by updateManager.updateState.collectAsStateWithLifecycle()

    val updateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // Handle update result if needed
    }

    LaunchedEffect(context) {
        updateManager.initialize(context as androidx.activity.ComponentActivity, updateLauncher)
        scope.launch {
            updateManager.checkForUpdate()
        }
    }

    LaunchedEffect(updateState) {
        if (updateState == InAppUpdateManager.UpdateState.DOWNLOADED) {
            updateManager.completeFlexibleUpdate()
        }
    }

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
        onUpdateSelected = {
            updateManager.startFlexibleUpdate()
        },
        toggleFilterProjectType = viewModel::setFilterProjectType,
        updateState = updateState,
        hasUpdateForMoreThanThreeDays = updateManager.isUpdateAvailableForMoreThanThreeDays(),
    )
}
