@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.davidmedenjak.indiana.theme.ui.preview

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.material3.Button as M3Button

@Composable
fun ErrorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = MaterialTheme.colorScheme.error,
            primaryContainer = MaterialTheme.colorScheme.errorContainer,
            onPrimary = MaterialTheme.colorScheme.onError,
            onErrorContainer = MaterialTheme.colorScheme.onErrorContainer,
        ),
        content = content
    )
}

//@Composable
//fun Button(
//    text: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//    interactionSource: MutableInteractionSource? = null,
//) = M3Button(
//    modifier = modifier,
//    shapes = ButtonDefaults.shapes(MaterialTheme.shapes.extraSmall),
//    enabled = enabled,
//    onClick = onClick,
//    interactionSource = interactionSource,
//) { Text(text) }

//@Composable
//@PreviewLightDark
//private fun PreviewButton() {
//    PreviewSurface {
//        Column {
//            Row {
//                Button(onClick = {}, text = "Action")
//
//                Button(
//                    onClick = {}, text = "Action",
//                    interactionSource = PressedInteractionSource
//                )
//            }
//            ErrorTheme {
//                Row {
//                    Button(onClick = {}, text = "Action")
//
//                    Button(
//                        onClick = {}, text = "Action",
//                        interactionSource = PressedInteractionSource
//                    )
//                }
//            }
//        }
//    }
//}
