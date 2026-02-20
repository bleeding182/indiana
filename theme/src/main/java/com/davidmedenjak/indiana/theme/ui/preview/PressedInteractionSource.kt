package com.davidmedenjak.indiana.theme.ui.preview

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.coroutines.flow.flowOf

internal class PressedInteractionSource : MutableInteractionSource {
    override val interactions = flowOf(PressInteraction.Press(Offset(5f, 5f)))

    override suspend fun emit(interaction: Interaction) = Unit
    override fun tryEmit(interaction: Interaction): Boolean = false
}

@Composable
internal fun rememberPressedInteractionSource() = remember { PressedInteractionSource() }

internal enum class WidgetPreviewState(
    val enabled: Boolean = true,
    val interactionSource: @Composable () -> MutableInteractionSource? = { null },
) {

    Default,
    Disabled(enabled = false),
    Pressed(interactionSource = ::rememberPressedInteractionSource),
    ;
}

internal class WidgetStateProvider : PreviewParameterProvider<WidgetPreviewState> {
    override val values: Sequence<WidgetPreviewState>
        get() = WidgetPreviewState.entries.asSequence()

    override fun getDisplayName(index: Int): String {
        return values.toList()[index].name
    }
}
