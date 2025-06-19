@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.LargeFlexibleTopAppBar as M3LargeFlexibleTopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior as M3TopAppBarScrollBehavior

@OptIn(ExperimentalMaterial3Api::class)
typealias TopAppBarScrollBehavior = M3TopAppBarScrollBehavior

@Composable
internal fun LargeFlexibleTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = M3TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) = M3LargeFlexibleTopAppBar(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    navigationIcon = navigationIcon,
    actions = actions,
    windowInsets = windowInsets,
    scrollBehavior = scrollBehavior,
)
