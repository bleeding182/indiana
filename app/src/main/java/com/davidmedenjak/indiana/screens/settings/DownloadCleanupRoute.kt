package com.davidmedenjak.indiana.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import kotlinx.serialization.Serializable

@Serializable
data object DownloadCleanupGraph : NavKey

@Composable
fun DownloadCleanupRoute(
    onBack: () -> Unit,
    viewModel: DownloadCleanupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text("Download Settings") },
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