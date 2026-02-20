package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import com.davidmedenjak.indiana.theme.ui.preview.WidgetPreviewState
import com.davidmedenjak.indiana.theme.ui.preview.WidgetStateProvider
import androidx.compose.material3.FilterChip as M3SelectableChip

@Composable
fun Chip(
    selected: Boolean,
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) = M3SelectableChip(
    selected = selected,
    enabled = enabled,
    label = label,
    modifier = modifier,
    onClick = onClick,
    interactionSource = interactionSource,
    shape = if (selected) FilterChipDefaults.shape else IndianaTheme.shapes.extraLarge
)

@Composable
@PreviewLightDark
private fun PreviewChip(@PreviewParameter(WidgetStateProvider::class) state: WidgetPreviewState) {
    PreviewSurface {
        Chip(
            label = { Text("Action2") },
            selected = false,
            enabled = state.enabled,
            interactionSource = state.interactionSource(),
            onClick = {}
        )
    }
}
