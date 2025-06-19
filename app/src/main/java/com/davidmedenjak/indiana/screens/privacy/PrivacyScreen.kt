package com.davidmedenjak.indiana.screens.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface

@Composable
fun PrivacyScreen(
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text("Privacy Policy") },
                actions = {},
                navigationIcon = { Up(onNavigateUp) }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
            Text(
                "This application stores the provided API token locally on your phone to deliver its service. It will only be used to communicate with the service provider that issued the token and not shared with any third parties.\n\n" +
                        "To enhance the user experience of this app it contains anonymous tracking of events and crash reporting with Google Firebase.\n\n" +
                        "To install APKs on the phone this app requires the REQUEST_INSTALL_PACKAGES permission.\n\n" +
                        "This policy may change with any future update and all the changes will be reflected here."
            )
        }
    }
}


@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        PrivacyScreen(onNavigateUp = {})
    }
}
