package com.davidmedenjak.indiana.screens.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.theme.ui.atoms.Button
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface

@Composable
fun PrivacyScreen(
    onNavigateUp: () -> Unit,
    onNavigateToTrackingSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text(stringResource(R.string.privacy_title)) },
                actions = {},
                navigationIcon = { Up(onNavigateUp) }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.privacy_content))
            
            Button(
                text = stringResource(R.string.tracking_settings_title),
                onClick = onNavigateToTrackingSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }
    }
}


@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        PrivacyScreen(
            onNavigateUp = {},
            onNavigateToTrackingSettings = {}
        )
    }
}
