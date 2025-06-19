package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.verticalScroll() = verticalScroll(androidx.compose.foundation.rememberScrollState())