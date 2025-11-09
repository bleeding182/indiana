package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.preview.PressedInteractionSource
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import androidx.compose.material3.FilterChip as M3SelectableChip

@Composable
fun Chip(
    selected: Boolean,
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) = M3SelectableChip(
    selected = selected,
    label = label,
    modifier = modifier,
    onClick = onClick,
    interactionSource = interactionSource,
    shape = if (selected) FilterChipDefaults.shape else IndianaTheme.shapes.extraLarge
)

@Composable
@PreviewLightDark
private fun Preview() {
    PreviewSurface {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(onClick = {}, label = { Text("Action") }, selected = false)

                Chip(
                    onClick = {},
                    label = { Text("Action") },
                    selected = false,
                    interactionSource = PressedInteractionSource
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(onClick = {}, label = { Text("Action") }, selected = true)

                Chip(
                    onClick = {},
                    label = { Text("Action") },
                    selected = true,
                    interactionSource = PressedInteractionSource
                )
            }
        }
    }
}
