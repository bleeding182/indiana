package com.davidmedenjak.indiana.download

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.ui.atoms.DeterministicProgress
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.IndeterminateProgress

@Composable
fun DownloadProgressIndicator(
    downloadState: DownloadState?,
    modifier: Modifier = Modifier
) {
    when (downloadState) {
        is DownloadState.Pending -> {
            IndeterminateProgress(modifier = modifier)
        }

        is DownloadState.InProgress -> {
            if (downloadState.isIndeterminate) {
                IndeterminateProgress(modifier = modifier)
            } else {
                DeterministicProgress(
                    progress = { downloadState.progress },
                    modifier = modifier
                )
            }
        }

        is DownloadState.Completed -> {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = rememberVectorPainter(Icons.Default.ArrowCircleDown),
                contentDescription = null,
            )
        }

        is DownloadState.Failed -> {
            // Show error indicator or hide progress
        }

        null -> {
            // No download state, don't show progress
        }
    }
}
