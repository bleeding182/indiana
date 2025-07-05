package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import androidx.compose.material3.Switch as M3Switch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) = M3Switch(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
)


@Composable
@PreviewLightDark
private fun Preview() {
    PreviewSurface {
        Column {
            Row {
            }
        }
    }
}
