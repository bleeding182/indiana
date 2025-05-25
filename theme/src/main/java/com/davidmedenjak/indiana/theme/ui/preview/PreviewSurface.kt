package com.davidmedenjak.indiana.theme.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.IndianaTheme

internal object PreviewSurfaceDefaults {
    val PaddingDefault = PaddingValues(8.dp)
    val PaddingNone = PaddingValues.Zero
}

@Composable
fun PreviewSurface(
    contentPadding: PaddingValues = PreviewSurfaceDefaults.PaddingDefault,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = IndianaTheme {
    Surface {
        Box(modifier = modifier.padding(contentPadding), content = { content() })
    }
}
