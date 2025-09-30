package com.davidmedenjak.indiana.download

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.ui.atoms.DeterministicProgress
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.Icons
import com.davidmedenjak.indiana.theme.ui.atoms.IndeterminateProgress
import com.davidmedenjak.indiana.theme.ui.atoms.MaterialIcon

@Composable
fun DownloadProgressIndicator(
    downloadState: DownloadState?,
    modifier: Modifier = Modifier
) {
    when (downloadState) {
        is DownloadState.Pending -> {
            IndeterminateProgress(modifier = modifier.size(24.dp))
        }

        is DownloadState.InProgress -> {
            if (downloadState.isIndeterminate) {
                IndeterminateProgress(modifier = modifier.size(24.dp))
            } else {
                DeterministicProgress(
                    progress = { downloadState.progress },
                    modifier = modifier
                )
            }
        }

        is DownloadState.Completed -> {
            MaterialIcon(
                icon = Icons.arrowCircleDown,
                size = 24.dp,
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
