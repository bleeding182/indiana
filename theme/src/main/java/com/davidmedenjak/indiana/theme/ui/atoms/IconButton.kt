@file:OptIn(ExperimentalMaterial3Api::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import com.davidmedenjak.indiana.theme.ui.preview.WidgetPreviewState
import com.davidmedenjak.indiana.theme.ui.preview.WidgetStateProvider
import androidx.compose.material3.IconButton as M3IconButton

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) = M3IconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    content = content,
)

@Composable
@PreviewLightDark
private fun PreviewChip(@PreviewParameter(WidgetStateProvider::class) state: WidgetPreviewState) {
    PreviewSurface {
        IconButton(
            content = {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.Settings),
                    contentDescription = null
                )
            },
            onClick = {},
            enabled = state.enabled,
            interactionSource = state.interactionSource()
        )
    }
}
