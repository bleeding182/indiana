@file:OptIn(ExperimentalMaterial3Api::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable TopBarScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
        Box(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
            content(paddingValues)
        }
    }
}
