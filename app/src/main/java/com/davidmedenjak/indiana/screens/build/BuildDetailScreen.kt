package com.davidmedenjak.indiana.screens.build

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.download.DownloadProgressIndicator
import com.davidmedenjak.indiana.download.DownloadState
import com.davidmedenjak.indiana.model.V0ArtifactListElementResponseModel
import com.davidmedenjak.indiana.model.V0BuildResponseItemModel
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.Button
import com.davidmedenjak.indiana.theme.ui.atoms.Card
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.IconButton
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Surface
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.atoms.TextButton
import com.davidmedenjak.indiana.theme.ui.atoms.contentEmpty
import com.davidmedenjak.indiana.theme.ui.atoms.contentError
import com.davidmedenjak.indiana.theme.ui.atoms.pageError
import com.davidmedenjak.indiana.theme.ui.atoms.pageLoading
import com.davidmedenjak.indiana.theme.ui.atoms.rememberPullToRefreshState
import com.davidmedenjak.indiana.theme.ui.modifier.skeletonLoader
import com.davidmedenjak.indiana.theme.ui.modifier.textSkeletonLoader
import com.davidmedenjak.indiana.theme.ui.molectule.Confirmation
import com.davidmedenjak.indiana.theme.ui.molectule.PropertyLayout
import com.davidmedenjak.indiana.theme.ui.molectule.rememberConfirmationDialogState
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.recordException
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun BuildDetailScreen(
    projectName: String,
    buildName: String,
    buildDetails: V0BuildResponseItemModel?,
    isLoadingBuildDetails: Boolean,
    buildDetailsError: String?,
    artifacts: Flow<PagingData<V0ArtifactListElementResponseModel>>,
    onNavigateUp: () -> Unit,
    onArtifactSelected: (V0ArtifactListElementResponseModel) -> Unit,
    onAbortBuild: (String?) -> Unit,
    onRestartBuild: () -> Unit,
    onRetryLoadBuildDetails: () -> Unit,
    getDownloadForArtifact: (String) -> Flow<DownloadState?>,
) {
    val projects = artifacts.collectAsLazyPagingItems()
    val pullToRefreshState = rememberPullToRefreshState(
        isRefreshing = projects.loadState.refresh == LoadState.Loading && projects.itemCount > 0,
        onRefresh = projects::refresh
    )

    val confirmationDialogState = rememberConfirmationDialogState()

    Scaffold(
        pullToRefreshState = pullToRefreshState,
        topBar = {
            LargeFlexible(
                title = { Text(buildName) },
                subtitle = { Text(projectName) },
                actions = {
                    val context = LocalContext.current
                    if (buildDetails?.status == BuildStatus.NotFinished) {
                        IconButton(onClick = {
                            confirmationDialogState.confirm(
                                Confirmation(
                                    title = context.getString(R.string.build_detail_confirmation_dialog_abort_build_title),
                                    text = context.getString(R.string.build_detail_confirmation_dialog_abort_build_text),
                                    action = context.getString(R.string.build_detail_confirmation_dialog_abort_build_action_abort),
                                    callback = { onAbortBuild(null) }
                                ),
                            )
                        }) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Default.Stop),
                                contentDescription = stringResource(R.string.build_detail_abort_build),
                                tint = IndianaTheme.colorScheme.error,
                            )
                        }
                    }

                    if (buildDetails?.status in BuildStatus.Completed) {
                        IconButton(onClick = {
                            confirmationDialogState.confirm(
                                Confirmation(
                                    title = context.getString(R.string.build_detail_confirmation_dialog_restart_build_title),
                                    text = context.getString(R.string.build_detail_confirmation_dialog_restart_build_text),
                                    action = context.getString(R.string.build_detail_confirmation_dialog_restart_build_action_start),
                                    callback = { onRestartBuild() },
                                ),
                            )
                        }) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Default.Refresh),
                                contentDescription = stringResource(R.string.build_detail_restart_build),
                                tint = IndianaTheme.colorScheme.primary,
                            )
                        }
                    }
                },
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
            item(key = "stable_header") { }
            if (showPermissionInfo) {
                item(key = "permission", contentType = "permission") {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 16.dp)
                        ) {
                            Text(stringResource(R.string.build_detail_permission_message))
                            Button(
                                text = stringResource(R.string.build_detail_permission_button),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .align(Alignment.End),
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
            }

            // Build Details Section
            item(key = "build_details", contentType = "build_details") {
                BuildDetailsSection(
                    buildDetails = buildDetails,
                    isLoading = isLoadingBuildDetails,
                    error = buildDetailsError,
                    onRetry = onRetryLoadBuildDetails,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                )
            }


            when (projects.loadState.refresh) {
                LoadState.Loading -> {
                    if (projects.itemCount == 0) {
                        items(5) {
                            ArtifactLoader()
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

            val itemModifier = Modifier.fillMaxWidth()
            items(
                count = projects.itemCount,
                key = { projects.peek(it)?.slug!! },
                contentType = { "project" }) { index ->
                val item = projects[index]!!
                Artifact(
                    artifact = item,
                    downloadState = getDownloadForArtifact(
                        item.slug ?: ""
                    ).collectAsStateWithLifecycle(null).value,
                    modifier = itemModifier.clickable { onArtifactSelected(item) }
                )
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
private fun Artifact(
    artifact: V0ArtifactListElementResponseModel,
    downloadState: DownloadState?,
    modifier: Modifier = Modifier
) {
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
                SimpleArtifact(
                    modifier = modifier,
                    artifact = artifact,
                    downloadState = downloadState
                )
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
                        version_name
                    )
                        .filter { it.toString().isNotBlank() }
                        .joinToString(separator = " ")
                        .takeIf(String::isNotBlank)
                    val subTitle = listOfNotNull(
                        package_name,
                        product_flavour,
                    )
                        .filter { it.toString().isNotBlank() }
                        .joinToString(separator = " ")
                        .takeIf(String::isNotBlank)
                    val rows = listOfNotNull(title, subTitle, artifact.title)
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

                // Download progress indicator
                if (downloadState != null) {
                    DownloadProgressIndicator(
                        downloadState = downloadState,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    } else {
        SimpleArtifact(modifier, artifact, downloadState)
    }
}

@Composable
private fun ArtifactLoader(modifier: Modifier = Modifier) {

    Row(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .padding(start = 12.dp, end = 16.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .skeletonLoader(shape = IndianaTheme.shapes.medium)
                .padding(8.dp)
                .align(Alignment.Top)
        )
        Column(
            horizontalAlignment = Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .textSkeletonLoader(IndianaTheme.typography.bodyMedium),
            )
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .textSkeletonLoader(IndianaTheme.typography.bodySmall),
            )
        }
    }
}

@Composable
private fun SimpleArtifact(
    modifier: Modifier,
    artifact: V0ArtifactListElementResponseModel,
    downloadState: DownloadState? = null
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
            modifier = Modifier.weight(1f),
        ) {
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

        // Download progress indicator
        if (downloadState != null) {
            DownloadProgressIndicator(
                downloadState = downloadState,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun BuildDetailsSection(
    buildDetails: V0BuildResponseItemModel?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                isLoading -> {
                    BuildDetailsLoader()
                }

                error != null -> {
                    BuildDetailsError(
                        error = error,
                        onRetry = onRetry
                    )
                }

                buildDetails != null -> {
                    BuildDetailsContent(
                        buildDetails = buildDetails,
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildDetailsContent(
    buildDetails: V0BuildResponseItemModel,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BuildStatusChip(
            status = buildDetails.status,
            statusText = buildDetails.statusText,
            isOnHold = buildDetails.isOnHold ?: false,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        buildDetails.abortReason?.let { workflow ->
            InfoRow(
                label = stringResource(R.string.build_detail_abort_reason),
                value = workflow
            )
        }

        buildDetails.triggeredWorkflow?.let { workflow ->
            InfoRow(
                label = stringResource(R.string.build_detail_workflow),
                value = workflow
            )
        }

        buildDetails.tag?.let { branch ->
            InfoRow(
                label = stringResource(R.string.build_detail_tag),
                value = branch
            )
        }

        buildDetails.branch?.let { branch ->
            InfoRow(
                label = stringResource(R.string.build_detail_branch),
                value = branch
            )
        }

        buildDetails.commitHash?.let { hash ->
            InfoRow(
                label = stringResource(R.string.build_detail_commit),
                value = hash.take(8)
            )
        }

        buildDetails.commitMessage?.let { message ->
            InfoRow(
                label = stringResource(R.string.build_detail_message),
                value = message
            )
        }

        // Timing information
        buildDetails.triggeredAt?.let { triggered ->
            val time = try {
                Instant.parse(triggered).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
            } catch (ex: Exception) {
                Firebase.crashlytics.recordException(ex) {
                    key("value", triggered)
                }
                triggered
            }
            InfoRow(
                label = stringResource(R.string.build_detail_triggered),
                value = time
            )
        }
    }
}

@Composable
private fun BuildStatusChip(
    status: Int?,
    isOnHold: Boolean,
    statusText: String?,
    modifier: Modifier = Modifier,
) {
    val text = when (status) {
        BuildStatus.NotFinished -> if (isOnHold) {
            stringResource(R.string.build_detail_status_on_hold)
        } else {
            stringResource(R.string.build_detail_status_running)
        }

        BuildStatus.Successful -> stringResource(R.string.build_detail_status_success)
        BuildStatus.Failed -> stringResource(R.string.build_detail_status_failed)
        BuildStatus.AbortedWithFailure, BuildStatus.AbortedWithSuccess -> stringResource(R.string.build_detail_status_aborted)
        else -> statusText ?: stringResource(R.string.build_detail_status_unknown)
    }

    val color = when (status) {
        BuildStatus.NotFinished -> IndianaTheme.colorScheme.onSurfaceVariant
        BuildStatus.Successful -> IndianaTheme.colorScheme.primary
        BuildStatus.Failed -> IndianaTheme.colorScheme.error
        BuildStatus.AbortedWithSuccess, BuildStatus.AbortedWithFailure -> IndianaTheme.colorScheme.onSurfaceVariant
        else -> IndianaTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = IndianaTheme.shapes.extraSmall,
        color = IndianaTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            text,
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp),
            style = IndianaTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) = PropertyLayout(
    modifier = Modifier.fillMaxWidth(),
) {
    Text(
        text = label,
        style = IndianaTheme.typography.labelMedium,
        color = IndianaTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = value,
        style = IndianaTheme.typography.bodyMedium,
    )
}

@Composable
private fun BuildDetailsLoader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(4) {
            PropertyLayout(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .textSkeletonLoader(IndianaTheme.typography.labelMedium)
                )
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .textSkeletonLoader(IndianaTheme.typography.bodyMedium)
                )
            }
        }
    }
}

@Composable
private fun BuildDetailsError(
    error: String,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Icon(
            painter = rememberVectorPainter(Icons.Default.Error),
            contentDescription = null,
            tint = IndianaTheme.colorScheme.error
        )
        Text(
            text = error,
            style = IndianaTheme.typography.bodySmall,
            color = IndianaTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        TextButton(
            text = stringResource(R.string.build_detail_retry),
            onClick = onRetry,
            modifier = Modifier.align(Alignment.End)
        )
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
                ),
                downloadState = null,
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
                ),
                downloadState = DownloadState.InProgress(
                    id = "TODO()",
                    artifactId = "TODO()",
                    buildId = "TODO()",
                    projectId = "TODO()",
                    fileName = "TODO()",
                    fileSize = 123,
                    downloadUrl = "TODO()",
                    createdAt = Instant.now(),
                    downloadedBytes = 10,
                ),
            )
            Artifact(
                V0ArtifactListElementResponseModel(
                    title = "ApkApp.aab",
                    artifactType = "android-apk",
                    artifactMeta = meta
                ),
                downloadState = DownloadState.Pending(
                    id = "TODO()",
                    artifactId = "TODO()",
                    buildId = "TODO()",
                    projectId = "TODO()",
                    fileName = "TODO()",
                    fileSize = 123,
                    downloadUrl = "TODO()",
                    createdAt = Instant.now(),
                ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewLoader() {
    PreviewSurface {
        Column {
            (0..5).forEach {
                ArtifactLoader()
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewError() {
    PreviewSurface {
        BuildDetailsError("There was an error", {})
    }
}

@PreviewLightDark
@Composable
private fun PreviewLoading() {
    PreviewSurface {
        BuildDetailsLoader()
    }
}

object BuildStatus {
    val NotFinished = 0
    val Successful = 1
    val Failed = 2
    val AbortedWithFailure = 3
    val AbortedWithSuccess = 4
    val Completed = setOf(Successful, Failed, AbortedWithFailure, AbortedWithSuccess)
}
