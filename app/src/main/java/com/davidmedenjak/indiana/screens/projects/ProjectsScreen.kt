package com.davidmedenjak.indiana.screens.projects

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.AsyncImage
import com.davidmedenjak.indiana.theme.ui.atoms.Chip
import com.davidmedenjak.indiana.theme.ui.atoms.DropdownMenu
import com.davidmedenjak.indiana.theme.ui.atoms.DropdownMenuItem
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.IconButton
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Sticky
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.atoms.contentEmpty
import com.davidmedenjak.indiana.theme.ui.atoms.contentError
import com.davidmedenjak.indiana.theme.ui.atoms.pageError
import com.davidmedenjak.indiana.theme.ui.atoms.pageLoading
import com.davidmedenjak.indiana.theme.ui.atoms.rememberPullToRefreshState
import com.davidmedenjak.indiana.theme.ui.modifier.skeletonLoader
import com.davidmedenjak.indiana.theme.ui.modifier.textSkeletonLoader
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
private fun getProjectTypeDisplayName(projectType: String): String {
    return when (projectType.lowercase()) {
        "other" -> stringResource(R.string.project_type_other)
        "android" -> stringResource(R.string.project_type_android)
        "ios" -> stringResource(R.string.project_type_ios)
        "macos" -> stringResource(R.string.project_type_macos)
        "flutter" -> stringResource(R.string.project_type_flutter)
        else -> projectType
    }
}

private fun getProjectInitials(projectName: String?): String {
    val name = projectName?.trim() ?: return "?"
    return if (name.length >= 2) {
        "${name.first().uppercaseChar()}${name.last().uppercaseChar()}"
    } else if (name.length == 1) {
        name.uppercase()
    } else {
        "?"
    }
}

@Composable
fun ProjectsScreen(
    projects: Flow<PagingData<Project>>,
    recents: StateFlow<List<Project>?>,
    projectTypes: StateFlow<List<String>?>,
    filteredProjectTypes: MutableStateFlow<String?>,
    onProjectSelected: (Project) -> Unit,
    onAboutSelected: () -> Unit,
    onPrivacySelected: () -> Unit,
    onDownloadCleanupSelected: () -> Unit,
    onLogoutSelected: () -> Unit,
    onUpdateSelected: () -> Unit,
    toggleFilterProjectType: (String) -> Unit,
    updateState: com.davidmedenjak.indiana.app.InAppUpdateManager.UpdateState,
    hasUpdateForMoreThanThreeDays: Boolean,
) {
    val projects = projects.collectAsLazyPagingItems()
    val recents by recents.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.RESUMED
    )
    val projectTypes by projectTypes.collectAsStateWithLifecycle()
    val filteredProjectType by filteredProjectTypes.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState(
        isRefreshing = projects.loadState.refresh == LoadState.Loading && projects.itemCount > 0,
        onRefresh = projects::refresh
    )
    Scaffold(
        pullToRefreshState = pullToRefreshState,
        topBar = {
            Column {
                LargeFlexible(
                    title = { Text(stringResource(R.string.projects_title)) },
                    actions = {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.navigation_more_options_description)
                                )
                            }
                            if (hasUpdateForMoreThanThreeDays) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .offset(x = 6.dp, y = 6.dp)
                                        .background(
                                            color = IndianaTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.projects_menu_about)) },
                                onClick = {
                                    expanded = false
                                    onAboutSelected()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.projects_menu_privacy)) },
                                onClick = {
                                    expanded = false
                                    onPrivacySelected()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Download Settings") },
                                onClick = {
                                    expanded = false
                                    onDownloadCleanupSelected()
                                },
                            )
                            if (updateState == com.davidmedenjak.indiana.app.InAppUpdateManager.UpdateState.AVAILABLE ||
                                updateState == com.davidmedenjak.indiana.app.InAppUpdateManager.UpdateState.DOWNLOADED) {
                                DropdownMenuItem(
                                    text = { 
                                        Row {
                                            Text(stringResource(R.string.projects_menu_update))
                                            if (hasUpdateForMoreThanThreeDays) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .offset(x = 4.dp, y = (-2).dp)
                                                        .background(
                                                            color = IndianaTheme.colorScheme.primary,
                                                            shape = CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        expanded = false
                                        onUpdateSelected()
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.projects_menu_logout)) },
                                onClick = {
                                    expanded = false
                                    onLogoutSelected()
                                },
                            )
                        }

                    },
                )
                Sticky(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val filters by remember {
                        derivedStateOf {
                            listOfNotNull(filteredProjectType)
                                .plus(projectTypes ?: emptyList())
                                .distinct()
                        }
                    }
                    LazyRow(
                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filters.size, key = { filters[it] }) {
                            val projectType = filters[it]
                            val displayName = getProjectTypeDisplayName(projectType)
                            Chip(
                                selected = filteredProjectType == projectType,
                                label = { Text(displayName) },
                                onClick = { toggleFilterProjectType(projectType) },
                            )
                        }
                    }
                }
            }
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                innerPadding.calculateTopPadding() + 16.dp,
                innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                innerPadding.calculateBottomPadding() + 16.dp,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "stable_header") { }

            val itemModifier = Modifier.fillMaxWidth()

            val recents = recents
            Log.d("RecentsAs", recents?.size.toString())
            if (recents != null && recents.isNotEmpty()) {
                item(key = "recents_header", contentType = "header") {
                    Text(
                        stringResource(R.string.projects_section_recents),
                        style = IndianaTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(
                    count = recents.size,
                    key = { "recent:${recents[it].id}" },
                    contentType = { "project" }) { index ->
                    val item = recents[index]
                    Project(item, modifier = itemModifier.clickable { onProjectSelected(item) })
                }
                item(contentType = "spacer") {
                    Spacer(modifier = Modifier.height(height = 8.dp))
                }
                item(key = "other_header", contentType = "header") {
                    Text(
                        stringResource(R.string.projects_section_all),
                        style = IndianaTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            when (projects.loadState.refresh) {
                LoadState.Loading -> {
                    if (projects.itemCount == 0) {
                        items(6) {
                            ProjectLoader()
                        }
                    }
                }

                is LoadState.Error -> {
                    contentError("contentError", onRetryClicked = projects::retry)
                }

                is LoadState.NotLoading -> {
                    if (projects.itemCount == 0) {
                        contentEmpty("contentEmpty")
                    }
                }
            }

            items(
                count = projects.itemCount,
                key = { projects.peek(it)?.id!! },
                contentType = { "project" }) { index ->
                val item = projects[index]!!
                Project(item, modifier = itemModifier.clickable { onProjectSelected(item) })
            }

            when (projects.loadState.append) {
                LoadState.Loading -> {
                    pageLoading("append")
                }

                is LoadState.Error -> {
                    pageError("pageError", onRetryClicked = projects::retry)
                }

                is LoadState.NotLoading -> {}
            }
        }
    }
}

@Composable
private fun Project(project: Project, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (project.avatar != null) {
            AsyncImage(
                model = project.avatar,
                modifier = Modifier
                    .size(40.dp)
                    .clip(IndianaTheme.shapes.small)
                    .background(IndianaTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Inside,
                error = {
                    val initials = getProjectInitials(project.name)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = IndianaTheme.typography.labelMedium,
                            color = IndianaTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                contentDescription = null,
            )
        } else {
            val initials = getProjectInitials(project.name)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(IndianaTheme.shapes.small)
                    .background(IndianaTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = IndianaTheme.typography.labelMedium,
                    color = IndianaTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column {
            Text(
                text = project.name ?: stringResource(R.string.projects_unknown_name),
                style = IndianaTheme.typography.bodyMedium,
                color = IndianaTheme.colorScheme.onSurface,
            )
            project.projectType?.let { type ->
                val displayName = getProjectTypeDisplayName(type)
                Text(
                    text = displayName,
                    style = IndianaTheme.typography.labelSmall,
                    color = IndianaTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProjectLoader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(IndianaTheme.shapes.small)
                .skeletonLoader(),
        )
        Column {
            Box(
                modifier = Modifier
                    .width(112.dp)
                    .textSkeletonLoader(IndianaTheme.typography.bodyMedium)
            )
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .textSkeletonLoader(IndianaTheme.typography.labelSmall)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        Project(
            Project(
                id = "slug",
                avatar = null,
                projectType = "Type",
                name = "ProjectTitle"
            ),
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewLoader() {
    PreviewSurface {
        ProjectLoader()
    }
}
