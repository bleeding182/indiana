@file:OptIn(ExperimentalMaterial3Api::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon as M3Icon

@Composable
fun Icon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) = M3Icon(
    painter = painter,
    contentDescription = contentDescription,
    modifier = modifier,
    tint = tint,
)

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) = M3Icon(
    imageVector = imageVector,
    contentDescription = contentDescription,
    modifier = modifier,
    tint = tint,
)
