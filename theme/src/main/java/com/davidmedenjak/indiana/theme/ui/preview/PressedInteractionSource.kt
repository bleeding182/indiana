package com.davidmedenjak.indiana.theme.ui.preview

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.flowOf

internal val PressedInteractionSource = object : MutableInteractionSource {
    override val interactions = flowOf(PressInteraction.Press(Offset(5f, 5f)))

    override suspend fun emit(interaction: Interaction) = Unit
    override fun tryEmit(interaction: Interaction): Boolean = false
}
