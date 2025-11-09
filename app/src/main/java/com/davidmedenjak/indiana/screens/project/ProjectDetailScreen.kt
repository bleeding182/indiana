package com.davidmedenjak.indiana.screens.project

import android.text.format.DateUtils
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.model.V0BuildResponseItemModel
import com.davidmedenjak.indiana.screens.build.BuildStatus
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.Icons
import com.davidmedenjak.indiana.theme.ui.atoms.IndeterminateProgressCircular
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.MaterialIcon
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Surface
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
import java.time.Instant
import java.time.OffsetDateTime

@Composable
fun ProjectDetailScreen(
    projectName: String,
    builds: Flow<PagingData<V0BuildResponseItemModel>>,
    onNavigateUp: () -> Unit,
    onBuildSelected: (V0BuildResponseItemModel) -> Unit,
) {
    val builds = builds.collectAsLazyPagingItems()
    val pullToRefreshState = rememberPullToRefreshState(
        isRefreshing = builds.loadState.refresh == LoadState.Loading && builds.itemCount > 0,
        onRefresh = builds::refresh
    )
    LifecycleStartEffect(Unit) {
        builds.refresh()
        onStopOrDispose { }
    }
    Scaffold(
        pullToRefreshState = pullToRefreshState,
        topBar = {
            LargeFlexible(
                title = { Text(projectName) },
                subtitle = { Text(stringResource(R.string.project_detail_subtitle)) },
                actions = {},
                navigationIcon = { Up(onNavigateUp) }
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
            when (builds.loadState.refresh) {
                LoadState.Loading -> {
                    if (builds.itemCount == 0) {
                        items(15) {
                            BuildLoader()
                        }
                    }
                }

                is LoadState.Error -> {
                    contentError("contentError", onRetryClicked = builds::retry)
                }

                is LoadState.NotLoading -> {
                    if (builds.itemCount == 0) {
                        contentEmpty("contentEmpty")
                    }
                }
            }

            val itemModifier = Modifier.fillMaxWidth()
            items(
                count = builds.itemCount,
                key = { builds.peek(it)?.slug!! },
                contentType = { "project" }) { index ->
                val item = builds[index]!!
                Build(item, modifier = itemModifier.clickable { onBuildSelected(item) })
            }

            when (builds.loadState.append) {
                LoadState.Loading -> {
                    pageLoading("append")
                }

                is LoadState.Error -> {
                    pageError("pageError", onRetryClicked = builds::retry)
                }

                is LoadState.NotLoading -> {}
            }
        }
    }
}

@Composable
private fun Build(build: V0BuildResponseItemModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val formattedStartTime = remember(context, build.triggeredAt) {
        build.triggeredAt?.let(OffsetDateTime::parse)?.let {
            DateUtils.getRelativeDateTimeString(
                context,
                it.toInstant().toEpochMilli(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.HOUR_IN_MILLIS,
                0
            )
        }
    }

    Row(
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .padding(start = 12.dp, end = 16.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val backgroundColor = buildBackgroundColor(build.status)
        MaterialIcon(
            icon = buildDrawable(build.status),
//            contentDescription = build.status.toString(), // fixme
            tint = IndianaTheme.colorScheme.contentColorFor(backgroundColor),
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = IndianaTheme.shapes.medium
                )
                .size(40.dp)
                .padding(8.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    "${build.buildNumber} ${build.branch ?: ""}".trim(),
                    style = IndianaTheme.typography.bodyMedium,
                    modifier = Modifier
                        .alignByBaseline(),
                )
                build.tag?.let { tag ->
                    Surface(
                        modifier = Modifier
                            .padding(start = 4.dp),
                        shape = IndianaTheme.shapes.extraSmall,
                        color = IndianaTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            tag, style = IndianaTheme.typography.labelSmall,
                            modifier = Modifier
                                .alignByBaseline()
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                "${stringResource(R.string.project_detail_triggered_prefix)} $formattedStartTime",
                style = IndianaTheme.typography.labelSmall,
                color = IndianaTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentHeight(Alignment.Top)
        ) {
            Text(
                text = build.triggeredWorkflow ?: "",
                style = IndianaTheme.typography.labelSmall,
                color = IndianaTheme.colorScheme.onSurfaceVariant,
            )
            if (build.status == 0 && build.isOnHold != true) {
                IndeterminateProgressCircular(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun BuildLoader(modifier: Modifier = Modifier) {
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
        )
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .textSkeletonLoader(IndianaTheme.typography.bodyMedium),
            )
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .textSkeletonLoader(IndianaTheme.typography.labelSmall)
                    .padding(top = 4.dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentHeight(Alignment.Top)
        ) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .textSkeletonLoader(IndianaTheme.typography.labelSmall),
            )
        }
    }
}

@Composable
private fun buildDrawable(status: Int?): String = when (status) {
    BuildStatus.NotFinished -> Icons.HourglassEmpty
    BuildStatus.Successful -> Icons.Check
    BuildStatus.Failed -> Icons.ErrorOutline
    BuildStatus.AbortedWithFailure, BuildStatus.AbortedWithSuccess -> Icons.Block
    else -> Icons.QuestionMark
}

@Composable
private fun buildBackgroundColor(status: Int?): Color = when (status) {
    BuildStatus.NotFinished -> IndianaTheme.colorScheme.primaryContainer
    BuildStatus.Successful -> IndianaTheme.colorScheme.primaryContainer
    BuildStatus.Failed -> IndianaTheme.colorScheme.surfaceContainer
    BuildStatus.AbortedWithFailure, BuildStatus.AbortedWithSuccess -> IndianaTheme.colorScheme.surfaceContainer
    else -> IndianaTheme.colorScheme.tertiaryContainer
}

@PreviewDynamicColors
@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        Column {
            (0..4).forEach { status ->
                Build(
                    V0BuildResponseItemModel(
                        abortReason = "Aborted",
                        branch = "Branch",
                        buildNumber = 14,
                        commitHash = "affe12341234",
                        commitMessage = "Did stuff",
                        commitViewUrl = "foo",
                        tag = "1.0.0",
                        triggeredAt = Instant.now().toString(),
                        triggeredWorkflow = "release",
                        creditCost = 3,
                        environmentPrepareFinishedAt = null,
                        finishedAt = null,
                        isOnHold = null,
                        isProcessed = null,
                        isStatusSent = null,
                        status = status
                    )
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewLoader() {
    PreviewSurface {
        Column {
            (0..5).forEach { status ->
                Box {
                    BuildLoader()
                }
            }
        }
    }
}
