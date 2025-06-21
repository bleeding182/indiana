@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Scaffold as M3Scaffold

interface TopBarScope {
    val scrollBehavior: TopAppBarScrollBehavior

}

interface NavigationIconScope {
    @Composable
    fun Up(navigateUp: () -> Unit) = IconButton(onClick = navigateUp) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Navigate back",
        )
    }
}

private object NavigationIconScopeImpl : NavigationIconScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarScope.LargeFlexible(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable NavigationIconScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) = LargeFlexibleTopAppBar(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    navigationIcon = { navigationIcon(NavigationIconScopeImpl) },
    actions = actions,
    windowInsets = windowInsets,
    scrollBehavior = scrollBehavior,
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable TopBarScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    pullToRefreshState: PullToRefreshState? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val state = rememberPullToRefreshState()
    val isRefreshing = pullToRefreshState?.isRefreshing == true
    val scaleFraction = {
        if (isRefreshing) 1f
        else LinearOutSlowInEasing.transform(state.distanceFraction).coerceIn(0f, 1f)
    }
    val threshold = PullToRefreshDefaults.PositionalThreshold + with(LocalDensity.current) {
        WindowInsets.safeDrawing.getTop(LocalDensity.current).toDp()
    }
    val pullToRefreshModifier = if (pullToRefreshState != null) {
        Modifier.pullToRefresh(
            state = state,
            isRefreshing = pullToRefreshState.isRefreshing,
            onRefresh = pullToRefreshState::onRefresh,
            threshold = threshold,
        )
    } else {
        Modifier
    }
    M3Scaffold(
        modifier = modifier.then(pullToRefreshModifier),
        topBar = {
            topBar(object : TopBarScope {
                override val scrollBehavior: TopAppBarScrollBehavior
                    get() = scrollBehavior
            })
            if (pullToRefreshState != null) {
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                scaleX = scaleFraction()
                                scaleY = scaleFraction()
                                translationY = -(1 - scaleFraction()) * size.height / 2
                            }
                    ) {
                        PullToRefreshDefaults.LoadingIndicator(
                            modifier = Modifier,
                            threshold = threshold,
                            state = state,
                            isRefreshing = isRefreshing
                        )
                    }
                }
            }
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = contentWindowInsets,
    ) { paddingValues ->
        Box(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
            content(paddingValues)
        }
    }
}
