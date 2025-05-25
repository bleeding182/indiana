package com.davidmedenjak.indiana.screens.build

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.davidmedenjak.indiana.model.V0ArtifactListElementResponseModel
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.Button
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.atoms.loading
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import kotlinx.coroutines.flow.Flow

@Composable
fun BuildDetailScreen(
    projectName: String,
    buildName: String,
    artifacts: Flow<PagingData<V0ArtifactListElementResponseModel>>,
    onNavigateUp: () -> Unit,
    onArtifactSelected: (V0ArtifactListElementResponseModel) -> Unit,
) {
    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text(buildName) },
                subtitle = { Text(projectName) },
                actions = {},
                navigationIcon = { Up(onNavigateUp) }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val projects = artifacts.collectAsLazyPagingItems()

        var showPermissionInfo by remember { mutableStateOf(false) }
        val context = LocalContext.current
        LifecycleStartEffect(Unit) {
            val packageManager = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
                showPermissionInfo = true
            } else {
                showPermissionInfo = false
            }
            onStopOrDispose { }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                innerPadding.calculateTopPadding() + 16.dp,
                innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                innerPadding.calculateBottomPadding() + 16.dp,
            )
        ) {
            if (showPermissionInfo) {
                item(key = "permission", contentType = "permission") {
                    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                        Text("Enable unknown sources for this app to immediately install apps after download")
                        Button(
                            text = "Okay",
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                        "package:${context.packageName}".toUri()
                                    )
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        )
                    }
                }
            }

            if (projects.loadState.refresh == LoadState.Loading) {
                loading("refresh")
            }

            val itemModifier = Modifier.fillMaxWidth()
            items(
                count = projects.itemCount,
                key = { projects.peek(it)?.slug!! },
                contentType = { "project" }) { index ->
                val item = projects[index]!!
                Artifact(item, modifier = itemModifier.clickable { onArtifactSelected(item) })
            }

            if (projects.loadState.append == LoadState.Loading) {
                loading("append")
            }
        }
    }
}

@Composable
private fun Artifact(artifact: V0ArtifactListElementResponseModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .padding(start = 12.dp, end = 16.dp)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
//        Icon(
//            buildDrawable(),
//            contentDescription = artifact.status.toString(), // fixme
//            modifier = Modifier
//                .size(40.dp)
//                .padding(4.dp)
//        )
        Text(
            artifact.title ?: "",
            style = IndianaTheme.typography.bodyMedium,
            modifier = Modifier,
        )
        Text(
            artifact.artifactType ?: "",
            style = IndianaTheme.typography.bodySmall,
            modifier = Modifier,
            color = IndianaTheme.colorScheme.onSurfaceVariant,
        )
    }
}


@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        Artifact(
            V0ArtifactListElementResponseModel(
                title = "Title",
                artifactType = "Type",
            )
        )
    }
}
