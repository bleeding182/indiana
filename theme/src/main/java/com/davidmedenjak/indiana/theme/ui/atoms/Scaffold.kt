@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.R
import com.davidmedenjak.indiana.theme.ui.preview.PreviewScreen
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.ScaffoldDefaults as M3ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults

interface TopBarScope {
    val scrollBehavior: TopAppBarScrollBehavior

}

interface NavigationIconScope {
    @Composable
    fun Up(navigateUp: () -> Unit) = M3IconButton(onClick = navigateUp) {
        M3Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.navigation_back_description),
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
    windowInsets: WindowInsets = M3TopAppBarDefaults.windowInsets,
) = LargeFlexibleTopAppBar(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    navigationIcon = { navigationIcon(NavigationIconScopeImpl) },
    actions = actions,
    windowInsets = windowInsets,
    scrollBehavior = scrollBehavior,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarScope.Sticky(
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)
) {
    val appBarContainerColor = lerp(
        M3TopAppBarDefaults.topAppBarColors().containerColor,
        M3TopAppBarDefaults.topAppBarColors().scrolledContainerColor,
        FastOutLinearInEasing.transform(scrollBehavior.state.collapsedFraction),
    )
    Surface(
        modifier = modifier,
        color = appBarContainerColor,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable TopBarScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = M3ScaffoldDefaults.contentWindowInsets,
    pullToRefreshState: PullToRefreshState? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = M3TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    M3Scaffold(
        modifier = modifier,
        topBar = {
            topBar(object : TopBarScope {
                override val scrollBehavior: TopAppBarScrollBehavior
                    get() = scrollBehavior
            })
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = contentWindowInsets,
    ) { paddingValues ->
        if (pullToRefreshState != null) {
            PullToRefreshContainer(
                state = pullToRefreshState,
                paddingValues = paddingValues,
                scrollConnection = scrollBehavior.nestedScrollConnection,
            ) {
                content(paddingValues)
            }
        } else {
            Box(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
                content(paddingValues)
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
                Column {
                    LargeFlexible(
                        title = { Text("Projects") },
                        subtitle = { Text("3 projects") },
                        navigationIcon = { Up {} },
                    )
                    Sticky {
                        Text(
                            "Some more text", modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            },
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                (0..24).forEach { _ ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(IndianaTheme.colorScheme.secondaryContainer)
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(IndianaTheme.colorScheme.primaryContainer)
                        )
                    }
                }
            }
        }
    }
}

