package com.davidmedenjak.indiana.screens.build

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.atoms.loading
import com.davidmedenjak.indiana.theme.ui.atoms.rememberPullToRefreshState
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
    val projects = artifacts.collectAsLazyPagingItems()
    val pullToRefreshState = rememberPullToRefreshState(
        isRefreshing = projects.loadState.refresh == LoadState.Loading && projects.itemCount > 0,
        onRefresh = projects::refresh
    )
    Scaffold(
        pullToRefreshState = pullToRefreshState,
        topBar = {
            LargeFlexible(
                title = { Text(buildName) },
                subtitle = { Text(projectName) },
                actions = {},
                navigationIcon = { Up(onNavigateUp) }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
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
            ),
            modifier = Modifier.fillMaxSize(),
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

            if (projects.loadState.refresh == LoadState.Loading && projects.itemCount == 0) {
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

    if (artifact.artifactMeta != null && artifact.artifactMeta is Map<*, *> && artifact.artifactType == "android-apk") {
        val meta = artifact.artifactMeta as Map<*, *>
        val product_flavour = meta["product_flavour"]
        val build_type = meta["build_type"]
        val signed_by = meta["signed_by"]?.toString()
//        val module = meta["module"]
//        val file_size_bytes = meta["file_size_bytes"]
//        val include = meta["include"]
//        val universal = meta["universal"]
//        val aab = meta["aab"]
//        val apk = meta["apk"]
//        val split = meta["split"]
        (meta["app_info"] as? Map<*, *>)?.let { info ->
            val app_name = info["app_name"]
            val package_name = info["package_name"]
            val version_name = info["version_name"]
//            val version_code = info["version_code"]
//            val min_sdk_version = info["min_sdk_version"]

            if (signed_by.isNullOrBlank() || artifact.title?.endsWith(".aab") == true) {
                SimpleArtifact(modifier = modifier, artifact = artifact)
                return
            }

            Row(
                modifier = modifier
                    .sizeIn(minHeight = 48.dp)
                    .padding(start = 12.dp, end = 16.dp)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val backgroundColor = IndianaTheme.colorScheme.primaryContainer
                Icon(
                    painter = rememberVectorPainter(Icons.Default.InstallMobile),
                    contentDescription = null, // fixme
                    tint = IndianaTheme.colorScheme.contentColorFor(backgroundColor),
                    modifier = Modifier
                        .background(
                            color = backgroundColor,
                            shape = IndianaTheme.shapes.medium
                        )
                        .size(40.dp)
                        .padding(8.dp)
                        .align(Alignment.Top)
                )
                Column(modifier = Modifier.weight(1f)) {
                    val title = listOfNotNull(
                        app_name,
                        product_flavour,
                        build_type,
                        version_name
                    )
                        .filter { it.toString().isNotBlank() }
                        .joinToString()
                        .takeIf(String::isNotBlank)
                    val subTitle = listOfNotNull(
                        package_name
                    )
                        .filter { it.toString().isNotBlank() }
                        .joinToString()
                        .takeIf(String::isNotBlank)
                    val rows = listOfNotNull(title, subTitle, artifact.title, artifact.artifactType)
                    rows.getOrNull(0)?.let {
                        Text(
                            text = it,
                            style = IndianaTheme.typography.bodyMedium,
                        )
                    }
                    rows.getOrNull(1)?.let {
                        Text(
                            text = it,
                            style = IndianaTheme.typography.labelMedium,
                            color = IndianaTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    rows.getOrNull(2)?.let {
                        Text(
                            text = it,
                            style = IndianaTheme.typography.labelSmall,
                            color = IndianaTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    } else {
        SimpleArtifact(modifier, artifact)
    }
}

@Composable
private fun SimpleArtifact(
    modifier: Modifier,
    artifact: V0ArtifactListElementResponseModel
) {
    Row(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .padding(start = 12.dp, end = 16.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val backgroundColor = IndianaTheme.colorScheme.surfaceContainer
        Icon(
            painter = rememberVectorPainter(Icons.Default.FilePresent),
            contentDescription = null, // fixme
            tint = IndianaTheme.colorScheme.contentColorFor(backgroundColor),
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = IndianaTheme.shapes.medium
                )
                .size(40.dp)
                .padding(8.dp)
                .align(Alignment.Top)
        )
    Column(
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
}


@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        Column {
            Artifact(
                V0ArtifactListElementResponseModel(
                    title = "Title",
                    artifactType = "file",
                )
            )
            val meta = hashMapOf(
                "product_flavour" to "",
                "build_type" to "debug",
                "signed_by" to "C=US, O=Android, CN=Android Debug",
                "app_info" to hashMapOf(
                    "app_name" to "Apk App",
                    "package_name" to "com.foo.app",
                    "version_name" to "1.0.0",
                ),
            )
            Artifact(
                V0ArtifactListElementResponseModel(
                    title = "ApkApp.apk",
                    artifactType = "android-apk",
                    artifactMeta = meta
                )
            )
            Artifact(
                V0ArtifactListElementResponseModel(
                    title = "ApkApp.aab",
                    artifactType = "android-apk",
                    artifactMeta = meta
                )
            )
        }
    }
}
