@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.LoadingIndicator as M3LoadingIndicator

private val polygons = listOf(
    MaterialShapes.ClamShell,
    MaterialShapes.PuffyDiamond,
    MaterialShapes.Pentagon,
    MaterialShapes.Cookie4Sided,
)

@Composable
fun IndeterminateProgress(modifier: Modifier = Modifier) = M3LoadingIndicator(
    polygons = polygons,
    modifier = modifier,
)
