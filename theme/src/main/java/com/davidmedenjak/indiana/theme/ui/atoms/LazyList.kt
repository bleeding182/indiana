package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.ui.percentage
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface

fun LazyListScope.contentLoading(key: Any?, paddingVertical: Dp = 24.dp) {
    item(key = key, contentType = "contentLoading") {
        Column(
            verticalArrangement = Arrangement.percentage(),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fillParentMaxSize()
        ) {
            IndeterminateProgress(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = paddingVertical)
            )
        }
    }
}

fun LazyListScope.pageLoading(key: Any?, paddingVertical: Dp = 24.dp) {
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

fun LazyListScope.contentError(
    key: Any?,
    onRetryClicked: () -> Unit,
    paddingVertical: Dp = 24.dp
) {
    item(key = key, contentType = "contentError") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.percentage(),
            modifier = Modifier
                .fillParentMaxSize()
                .padding(horizontal = 16.dp, vertical = paddingVertical)
        ) {
            Text(
                text = "Failed to load content",
                modifier = Modifier
            )
            TextButton("Retry", onClick = onRetryClicked)
        }
    }
}

fun LazyListScope.pageError(
    key: Any?,
    onRetryClicked: () -> Unit,
    paddingVertical: Dp = 24.dp
) {
    item(key = key, contentType = "pageError") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = paddingVertical)
        ) {
            Text(
                text = "Failed to load content",
                modifier = Modifier
            )
            TextButton("Retry", onClick = onRetryClicked)
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        LazyColumn {
            pageLoading("load")
            pageError("error", onRetryClicked = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewError() {
    PreviewSurface {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            contentError("error", onRetryClicked = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewLoading() {
    PreviewSurface {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            contentLoading("loading")
        }
    }
}
