package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface

fun LazyListScope.loading(key: Any?, paddingVertical: Dp = 24.dp) {
    item(key = key, contentType = "loading") {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IndeterminateProgress(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = paddingVertical)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        LazyColumn {
            loading("")
        }
    }
}
