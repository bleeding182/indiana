package com.davidmedenjak.indiana.theme.ui.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.davidmedenjak.apidoc.Usage
import com.davidmedenjak.indiana.theme.IndianaTheme

@Usage("Wrap full-screen previews with theme")
@Composable
fun PreviewScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = IndianaTheme {
    Surface(modifier, content = content)
}
