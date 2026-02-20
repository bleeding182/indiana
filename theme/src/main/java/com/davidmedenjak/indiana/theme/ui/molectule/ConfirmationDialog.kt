package com.davidmedenjak.indiana.theme.ui.molectule

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.atoms.TextButton
import com.davidmedenjak.indiana.theme.ui.atoms.TextField
import com.davidmedenjak.indiana.theme.ui.preview.PreviewScreen
import androidx.compose.material3.AlertDialog as M3AlertDialog

@Stable
class DialogState<A : Any, T : Any> internal constructor(
    private val onConfirm: State<(A, T) -> Unit>,
    initialValue: A? = null,
    val destructive: Boolean = false,
) {
    private var _argument by mutableStateOf(initialValue)
    val argument: A get() = checkNotNull(_argument) { "Dialog is not showing" }

    var isShowing by mutableStateOf(initialValue != null)
        private set

    fun show(arg: A) {
        _argument = arg
        isShowing = true
    }

    fun dismiss() {
        isShowing = false
    }

    fun confirmAndDismiss(result: T) {
        val arg = argument
        dismiss()
        onConfirm.value(arg, result)
    }
}

fun <T : Any> DialogState<Unit, T>.show() = show(Unit)

@Composable
fun <A : Any, T : Any> rememberDialogState(
    onConfirm: (A, T) -> Unit,
    initialValue: A? = null,
    destructive: Boolean = false,
    content: @Composable DialogState<A, T>.() -> Unit,
): DialogState<A, T> {
    val currentOnConfirm = rememberUpdatedState(onConfirm)
    val state = remember { DialogState<A, T>(currentOnConfirm, initialValue, destructive) }
    if (state.isShowing) {
        state.content()
    }
    return state
}

@Composable
fun <T : Any> rememberDialogState(
    onConfirm: (T) -> Unit,
    initiallyShowing: Boolean = false,
    destructive: Boolean = false,
    content: @Composable DialogState<Unit, T>.() -> Unit,
): DialogState<Unit, T> = rememberDialogState<Unit, T>(
    onConfirm = { _, result -> onConfirm(result) },
    initialValue = if (initiallyShowing) Unit else null,
    destructive = destructive,
    content = content,
)

@Composable
fun rememberDialogState(
    onConfirm: () -> Unit,
    initiallyShowing: Boolean = false,
    destructive: Boolean = false,
    content: @Composable DialogState<Unit, Unit>.() -> Unit,
): DialogState<Unit, Unit> = rememberDialogState<Unit>(
    onConfirm = { onConfirm() },
    initiallyShowing = initiallyShowing,
    destructive = destructive,
    content = content,
)

@Composable
fun <A : Any> DialogState<A, Unit>.Confirmation(
    title: String,
    text: String,
    confirmAction: String,
    modifier: Modifier = Modifier,
) = Dialog(
    title = title,
    text = { Text(text) },
    modifier = modifier,
    confirmButton = {
        TextButton(text = confirmAction, onClick = { confirmAndDismiss(Unit) })
    },
)

@Composable
fun <A : Any, T : Any> DialogState<A, T>.Input(
    title: String,
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit = {
        TextButton(text = stringResource(android.R.string.cancel), onClick = { dismiss() })
    },
    confirmButton: @Composable () -> Unit,
) = Dialog(
    title = title,
    text = text,
    modifier = modifier,
    dismissButton = dismissButton,
    confirmButton = confirmButton,
)

@Composable
private fun <A : Any, T : Any> DialogState<A, T>.Dialog(
    title: String,
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit = {
        TextButton(text = stringResource(android.R.string.cancel), onClick = { dismiss() })
    },
    confirmButton: @Composable () -> Unit,
) = M3AlertDialog(
    title = { Text(title) },
    text = text,
    onDismissRequest = ::dismiss,
    modifier = modifier,
    confirmButton = {
        if (destructive) IndianaTheme.Error { confirmButton() } else confirmButton()
    },
    dismissButton = dismissButton,
)

@Preview
@Composable
private fun PreviewConfirmation() {
    PreviewScreen(modifier = Modifier.fillMaxSize()) {
        rememberDialogState(onConfirm = {}, initiallyShowing = true) {
            Confirmation(
                title = "Restart Build",
                text = "Are you sure you want to restart this build?",
                confirmAction = "Restart",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewConfirmationWithId() {
    PreviewScreen(modifier = Modifier.fillMaxSize()) {
        rememberDialogState(
            onConfirm = { id, _ -> id + id /* delete $id */ },
            initialValue = "build-42",
        ) {
            Confirmation(
                title = "Delete Build",
                text = "Delete build ${argument}?",
                confirmAction = "Delete",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDestructiveConfirmation() {
    PreviewScreen(modifier = Modifier.fillMaxSize()) {
        rememberDialogState(onConfirm = {}, initiallyShowing = true, destructive = true) {
            Confirmation(
                title = "Clear All Downloads",
                text = "This will permanently delete all downloaded files.",
                confirmAction = "Clear All",
            )
        }
    }
}

@Preview
@Composable
private fun PreviewInputDialog() {
    PreviewScreen(modifier = Modifier.fillMaxSize()) {
        rememberDialogState(onConfirm = { _: String -> }, initiallyShowing = true) {
            var text by remember { mutableStateOf(TextFieldValue()) }
            Input(
                title = "Abort Build",
                text = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Abort Reason") },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    TextButton(
                        text = "Abort",
                        onClick = { confirmAndDismiss(text.text) },
                    )
                },
            )
        }
    }
}
