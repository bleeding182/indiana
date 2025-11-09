package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import androidx.compose.material3.HorizontalDivider as M3HorizontalDivider

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
) = M3HorizontalDivider(modifier)

@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        Column {
            Text("Hello")
            HorizontalDivider()
            Text("World")
        }
    }
}
