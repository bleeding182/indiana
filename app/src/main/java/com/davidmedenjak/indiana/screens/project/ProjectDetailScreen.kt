package com.davidmedenjak.indiana.screens.project

import android.text.format.DateUtils
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.davidmedenjak.indiana.model.V0BuildResponseItemModel
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.IconButton
import com.davidmedenjak.indiana.theme.ui.atoms.IndeterminateProgressCircular
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Surface
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.atoms.loading
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
    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text(projectName) },
                subtitle = { Text("Builds") },
                actions = {},
                navigationIcon = { Up(onNavigateUp) }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val projects = builds.collectAsLazyPagingItems()
        LazyColumn(
            contentPadding = PaddingValues(
                innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                innerPadding.calculateTopPadding() + 16.dp,
                innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                innerPadding.calculateBottomPadding() + 16.dp,
            )
        ) {
            if (projects.loadState.refresh == LoadState.Loading) {
                loading("refresh")
            }

            val itemModifier = Modifier.fillMaxWidth()
            items(
                count = projects.itemCount,
                key = { projects.peek(it)?.slug!! },
                contentType = { "project" }) { index ->
                val item = projects[index]!!
                Build(item, modifier = itemModifier.clickable { onBuildSelected(item) })
            }

            if (projects.loadState.append == LoadState.Loading) {
                loading("append")
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
        Icon(
            buildDrawable(build.status),
            contentDescription = build.status.toString(), // fixme
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp)
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
                "Triggered $formattedStartTime",
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
private fun buildDrawable(status: Int?): VectorPainter {
    val icon = when (status) {
        0 -> Icons.Default.HourglassEmpty
        1 -> Icons.Default.Check
        2 -> Icons.Default.ErrorOutline
        3 -> Icons.Default.Block
        else -> Icons.Default.QuestionMark
    }
    return rememberVectorPainter(icon)
}

@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
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
                status = 1
            )
        )
    }
}
