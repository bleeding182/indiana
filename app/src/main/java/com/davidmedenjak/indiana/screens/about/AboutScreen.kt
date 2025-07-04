package com.davidmedenjak.indiana.screens.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface

@Composable
fun AboutScreen(
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            val context = LocalContext.current
            val info = context.packageManager.getApplicationInfo(context.packageName, 0)

            LargeFlexible(
                title = { Text(stringResource(R.string.about_title)) },
                subtitle = { Text(info.loadLabel(context.packageManager).toString()) },
                navigationIcon = { Up(onNavigateUp) }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.about_content))
//            Button("Show me", onClick = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        AboutScreen(onNavigateUp = {})
    }
}
