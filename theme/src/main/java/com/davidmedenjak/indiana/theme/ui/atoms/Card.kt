package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Card as M3Card

@Composable
fun Card(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) = M3Card(modifier = modifier, content = content)