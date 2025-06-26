package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.davidmedenjak.indiana.theme.IndianaTheme
import androidx.compose.material3.FilterChip as M3SelectableChip

@Composable
fun Chip(
    selected: Boolean,
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = M3SelectableChip(
    selected = selected,
    label = label,
    modifier = modifier,
    onClick = onClick,
    shape = if (selected) FilterChipDefaults.shape else IndianaTheme.shapes.extraLarge
)