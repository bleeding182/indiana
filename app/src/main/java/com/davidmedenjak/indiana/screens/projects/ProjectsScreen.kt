package com.davidmedenjak.indiana.screens.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.davidmedenjak.indiana.model.V0AppResponseItemModel
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.AsyncImage
import com.davidmedenjak.indiana.theme.ui.atoms.DropdownMenu
import com.davidmedenjak.indiana.theme.ui.atoms.DropdownMenuItem
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.IconButton
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.atoms.loading
import com.davidmedenjak.indiana.theme.ui.atoms.rememberPullToRefreshState
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import kotlinx.coroutines.flow.Flow

@Composable
fun ProjectsScreen(
    projects: Flow<PagingData<V0AppResponseItemModel>>,
    onProjectSelected: (project: V0AppResponseItemModel) -> Unit,
    onAboutSelected: () -> Unit,
    onPrivacySelected: () -> Unit,
    onLogoutSelected: () -> Unit,
) {
    val projects = projects.collectAsLazyPagingItems()
    val pullToRefreshState = rememberPullToRefreshState(
        isRefreshing = projects.loadState.refresh == LoadState.Loading && projects.itemCount > 0,
        onRefresh = projects::refresh
    )
    Scaffold(
        pullToRefreshState = pullToRefreshState,
        topBar = {
            LargeFlexible(
                title = { Text("Projects") },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("About") },
                            onClick = {
                                expanded = false
                                onAboutSelected()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Privacy Policy") },
                            onClick = {
                                expanded = false
                                onPrivacySelected()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                expanded = false
                                onLogoutSelected()
                            },
                        )
                    }

                },
            )
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
            if (projects.loadState.refresh == LoadState.Loading && projects.itemCount == 0) {
                loading("refresh")
            }

            val itemModifier = Modifier.fillMaxWidth()
            items(
                count = projects.itemCount,
                key = { projects.peek(it)?.slug!! },
                contentType = { "project" }) { index ->
                val item = projects[index]!!
                Project(item, modifier = itemModifier.clickable { onProjectSelected(item) })
            }

            if (projects.loadState.append == LoadState.Loading) {
                loading("append")
            }
        }
    }
}

@Composable
private fun Project(project: V0AppResponseItemModel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = project.avatarUrl,
            modifier = Modifier
                .size(40.dp)
                .clip(IndianaTheme.shapes.small)
                .background(IndianaTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Inside,
            error = rememberVectorPainter(Icons.Default.Image),
            contentDescription = null,
        )
        Text(project.title ?: "<unknown>")
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        Project(
            V0AppResponseItemModel(
                avatarUrl = null,
                isDisabled = false,
                isGithubChecksEnabled = false,
                isPublic = false,
                owner = null,
                projectId = "ID",
                projectType = "Type",
                provider = "Provider",
                repoOwner = "RepoOwner",
                repoSlug = "RepoSlug",
                repoUrl = "RepoUrl",
                slug = "slug",
                status = 3,
                title = "ProjectTitle"
            ),
        )
    }
}
