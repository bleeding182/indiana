package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import kotlinx.coroutines.flow.flowOf
import androidx.compose.material3.Button as M3Button
import androidx.compose.material3.TextButton as M3TextButton

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) = M3TextButton(
    modifier = modifier,
    shapes = ButtonDefaults.shapes(),
    onClick = onClick,
    interactionSource = interactionSource,
) { Text(text) }

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

@Composable
@PreviewLightDark
private fun Preview() {
    PreviewSurface {
        Column {
            Row {
                Button(onClick = {}, text = "Action")

                Button(
                    onClick = {}, text = "Action",
                    interactionSource = remember {
                        object : MutableInteractionSource {
                            override val interactions =
                                flowOf(PressInteraction.Press(Offset(5f, 5f)))

                            override suspend fun emit(interaction: Interaction) = Unit
                            override fun tryEmit(interaction: Interaction): Boolean = false
                        }
                    }
                )
            }
            Row {
                TextButton(onClick = {}, text = "Action")

                TextButton(
                    onClick = {}, text = "Action",
                    interactionSource = remember {
                        object : MutableInteractionSource {
                            override val interactions =
                                flowOf(PressInteraction.Press(Offset(5f, 5f)))

                            override suspend fun emit(interaction: Interaction) = Unit
                            override fun tryEmit(interaction: Interaction): Boolean = false
                        }
                    }
                )
            }
        }
    }
}
