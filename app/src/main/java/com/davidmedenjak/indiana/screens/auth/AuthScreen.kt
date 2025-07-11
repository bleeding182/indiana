package com.davidmedenjak.indiana.screens.auth

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.R
import androidx.core.net.toUri
import com.davidmedenjak.indiana.theme.ui.atoms.Button
import com.davidmedenjak.indiana.theme.ui.atoms.Card
import com.davidmedenjak.indiana.theme.ui.atoms.DropdownMenu
import com.davidmedenjak.indiana.theme.ui.atoms.DropdownMenuItem
import com.davidmedenjak.indiana.theme.ui.atoms.Icon
import com.davidmedenjak.indiana.theme.ui.atoms.IconButton
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.atoms.TextButton
import com.davidmedenjak.indiana.theme.ui.atoms.TextField
import com.davidmedenjak.indiana.theme.ui.preview.PreviewScreen

@Composable
fun AuthScreen(
    onTryToken: (token: String) -> Unit,
    onAboutSelected: () -> Unit,
    onPrivacySelected: () -> Unit,
) {
    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text(stringResource(R.string.auth_title)) },
                subtitle = { Text(stringResource(R.string.auth_subtitle)) },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.navigation_more_options_description))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.auth_menu_about)) },
                            onClick = {
                                expanded = false
                                onAboutSelected()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.auth_menu_privacy)) },
                            onClick = {
                                expanded = false
                                onPrivacySelected()
                            },
                        )
                    }

                },
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            val context = LocalContext.current
            Text(
                text = stringResource(R.string.auth_no_token_message),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
            )
            TextButton(
                text = stringResource(R.string.auth_create_token_button),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 8.dp),
                onClick = {
                    val uri = "https://app.bitrise.io/me/account/security".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                })

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    var text by remember { mutableStateOf(TextFieldValue("")) }
                    TextField(
                        text,
                        modifier = Modifier.fillMaxSize(),
                        onValueChange = { text = it },
                        label = { Text(stringResource(R.string.auth_token_field_label)) }
                    )
                    Button(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp),
                        onClick = { onTryToken(text.text) },
                        text = stringResource(R.string.auth_submit_button),
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    PreviewScreen {
        AuthScreen(
            onTryToken = {},
            onAboutSelected = {},
            onPrivacySelected = {},
        )
    }
}
