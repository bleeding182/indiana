@file:OptIn(ExperimentalMaterial3Api::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState as rememberM3PullToRefreshState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.zIndex
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.preview.PreviewScreen
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

@Stable
class PullToRefreshState(
    private val isRefreshingState: State<Boolean>,
    private val onRefreshState: State<() -> Unit>,
) {
    val isRefreshing by isRefreshingState
    fun onRefresh() = onRefreshState.value()
}

@Composable
fun rememberPullToRefreshState(isRefreshing: Boolean, onRefresh: () -> Unit): PullToRefreshState {
    val isRefreshingState = remember { mutableStateOf(isRefreshing) }
    isRefreshingState.value = isRefreshing
    val onRefreshState = rememberUpdatedState(onRefresh)
    return remember {
        PullToRefreshState(isRefreshingState, onRefreshState = onRefreshState)
    }
}

@Composable
internal fun PullToRefreshContainer(
    state: PullToRefreshState,
    paddingValues: PaddingValues,
    scrollConnection: NestedScrollConnection,
    modifier: Modifier = Modifier,
    resistance: Float = 0.5f,
    maxDisplacement: Dp = 64.dp,
    content: @Composable () -> Unit,
) {
    val m3State = rememberM3PullToRefreshState()
    val isRefreshing = state.isRefreshing
    val scaleFraction = {
        if (isRefreshing) 1f
        else LinearOutSlowInEasing.transform(m3State.distanceFraction).coerceIn(0f, 1f)
    }
    val maxDisplacementPx = with(LocalDensity.current) { maxDisplacement.toPx() }
    val displacement = remember { Animatable(0f) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing && displacement.value > 0f) {
            displacement.animateTo(
                0f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            snapshotFlow { m3State.distanceFraction }.first { it == 0f }
            snapshotFlow { m3State.distanceFraction }
                .collect { fraction ->
                    if (fraction > 0f) {
                        displacement.snapTo(fraction * resistance * maxDisplacementPx)
                    } else if (displacement.value > 0f) {
                        displacement.animateTo(
                            0f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                    }
                }
        }
    }

    val threshold = PullToRefreshDefaults.PositionalThreshold + with(LocalDensity.current) {
        WindowInsets.safeDrawing.getTop(LocalDensity.current).toDp()
    }

    Box(
        modifier = modifier.pullToRefresh(
            state = m3State,
            isRefreshing = state.isRefreshing,
            onRefresh = state::onRefresh,
            threshold = threshold,
        )
    ) {
        Box(
            Modifier
                .nestedScroll(scrollConnection)
                .zIndex(-1f)
                .offset { IntOffset(0, displacement.value.roundToInt()) }
        ) {
            content()
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = paddingValues.calculateTopPadding() + 8.dp)
                .size(64.dp)
                .graphicsLayer {
                    val f = scaleFraction()
                    scaleX = f
                    scaleY = f
                    alpha = f
                },
            shape = CircleShape,
            tonalElevation = 6.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isRefreshing) {
                    IndeterminateProgressCircular(Modifier.size(40.dp))
                } else {
                    CircularProgressIndicator(
                        progress = { m3State.distanceFraction.coerceIn(0f, 1f) },
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 2.5.dp,
                    )
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun Preview() {
    PreviewScreen {
        Scaffold(
            topBar = {
                LargeFlexible(
                    title = { Text("Pull to Refresh") },
                    subtitle = { Text("Drag down to refresh") },
                )
            },
            pullToRefreshState = rememberPullToRefreshState(
                isRefreshing = false,
                onRefresh = {},
            ),
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                items(20) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(
                                if (index % 2 == 0) IndianaTheme.colorScheme.secondaryContainer
                                else IndianaTheme.colorScheme.primaryContainer
                            )
                    )
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun PreviewRefreshing() {
    PreviewScreen {
        Scaffold(
            topBar = {
                LargeFlexible(
                    title = { Text("Pull to Refresh") },
                    subtitle = { Text("Refreshing...") },
                )
            },
            pullToRefreshState = rememberPullToRefreshState(
                isRefreshing = true,
                onRefresh = {},
            ),
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                items(20) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(
                                if (index % 2 == 0) IndianaTheme.colorScheme.secondaryContainer
                                else IndianaTheme.colorScheme.primaryContainer
                            )
                    )
                }
            }
        }
    }
}
