package com.davidmedenjak.indiana.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.davidmedenjak.indiana.R
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.analytics.ScreenTrackable
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import kotlinx.serialization.Serializable

@Serializable
data object DownloadCleanupGraph : NavKey, ScreenTrackable {
    override val screenName = "DownloadCleanup"
}

@Composable
fun DownloadCleanupRoute(
    onBack: () -> Unit,
    viewModel: DownloadCleanupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text(stringResource(R.string.download_cleanup_title)) },
                navigationIcon = { Up(onBack) }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        DownloadCleanupScreen(
            uiState = uiState,
            onCleanupPeriodChanged = viewModel::updateCleanupPeriod,
            onRunCleanupNow = viewModel::runCleanupNow,
            onClearAllDownloads = viewModel::clearAllDownloads,
            onDismissError = viewModel::dismissError,
            modifier = Modifier.padding(innerPadding)
        )
    }
}