package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import com.davidmedenjak.indiana.theme.ui.preview.WidgetPreviewState
import com.davidmedenjak.indiana.theme.ui.preview.WidgetStateProvider
import androidx.compose.material3.Button as M3Button
import androidx.compose.material3.TextButton as M3TextButton


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) = M3Button(
    modifier = modifier,
    shapes = ButtonDefaults.shapes(),
    enabled = enabled,
    onClick = onClick,
    interactionSource = interactionSource,
) { Text(text) }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) = M3Button(
    modifier = modifier,
    shapes = ButtonDefaults.shapes(),
    enabled = enabled,
    onClick = onClick,
    interactionSource = interactionSource,
    content = content,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) = M3TextButton(
    modifier = modifier,
    shapes = ButtonDefaults.shapes(),
    enabled = enabled,
    onClick = onClick,
    interactionSource = interactionSource,
) { Text(text) }


@Composable
@PreviewLightDark
private fun PreviewButton(@PreviewParameter(WidgetStateProvider::class) state: WidgetPreviewState) {
    PreviewSurface {
        Button(
            text = "Action",
            enabled = state.enabled,
            interactionSource = state.interactionSource(),
            onClick = {}
        )
    }
}

@Composable
@PreviewLightDark
private fun PreviewTextButton(@PreviewParameter(WidgetStateProvider::class) state: WidgetPreviewState) {
    PreviewSurface {
        TextButton(
            text = "Action",
            enabled = state.enabled,
            interactionSource = state.interactionSource(),
            onClick = {}
        )
    }
}
