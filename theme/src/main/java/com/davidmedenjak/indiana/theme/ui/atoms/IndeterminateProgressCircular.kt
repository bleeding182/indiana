@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.LoadingIndicator as M3LoadingIndicator

@Composable
fun IndeterminateProgressCircular(modifier: Modifier = Modifier) = CircularWavyProgressIndicator(
    modifier = modifier,
)
