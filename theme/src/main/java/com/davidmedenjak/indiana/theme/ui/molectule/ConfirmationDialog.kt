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
class ConfirmationDialogState<A : Any, T : Any> internal constructor(
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

fun <T : Any> ConfirmationDialogState<Unit, T>.show() = show(Unit)

@Composable
fun <A : Any, T : Any> rememberConfirmationDialogState(
    onConfirm: (A, T) -> Unit,
    initialValue: A? = null,
    destructive: Boolean = false,
): ConfirmationDialogState<A, T> {
    val currentOnConfirm = rememberUpdatedState(onConfirm)
    return remember { ConfirmationDialogState(currentOnConfirm, initialValue, destructive) }
}

@Composable
fun <T : Any> rememberConfirmationDialogState(
    onConfirm: (T) -> Unit,
    initiallyShowing: Boolean = false,
    destructive: Boolean = false,
): ConfirmationDialogState<Unit, T> = rememberConfirmationDialogState<Unit, T>(
    onConfirm = { _, result -> onConfirm(result) },
    initialValue = if (initiallyShowing) Unit else null,
    destructive = destructive,
)

@Composable
fun rememberConfirmationDialogState(
    onConfirm: () -> Unit,
    initiallyShowing: Boolean = false,
    destructive: Boolean = false,
): ConfirmationDialogState<Unit, Unit> = rememberConfirmationDialogState<Unit>(
    onConfirm = { onConfirm() },
    initiallyShowing = initiallyShowing,
    destructive = destructive,
)

@Composable
fun <A : Any> ConfirmationDialog(
    state: ConfirmationDialogState<A, Unit>,
    title: String,
    confirmAction: String,
    modifier: Modifier = Modifier,
    content: @Composable (A) -> Unit,
) {
    if (!state.isShowing) return
    Dialog(
        title = title,
        text = { content(state.argument) },
        modifier = modifier,
        destructive = state.destructive,
        onDismiss = state::dismiss,
        confirmButton = {
            TextButton(text = confirmAction, onClick = { state.confirmAndDismiss(Unit) })
        },
    )
}

@Composable
fun <A : Any> ConfirmationDialog(
    state: ConfirmationDialogState<A, Unit>,
    title: String,
    text: String,
    confirmAction: String,
    modifier: Modifier = Modifier,
) = ConfirmationDialog(
    state = state,
    title = title,
    confirmAction = confirmAction,
    modifier = modifier,
    content = { Text(text) },
)

@Composable
fun <A : Any, T : Any> ConfirmationDialog(
    state: ConfirmationDialogState<A, T>,
    title: String,
    modifier: Modifier = Modifier,
    dismissButton: @Composable () -> Unit = {
        TextButton(text = stringResource(android.R.string.cancel), onClick = { state.dismiss() })
    },
    confirmButton: @Composable () -> Unit,
    content: @Composable (A) -> Unit,
) {
    if (!state.isShowing) return
    Dialog(
        title = title,
        text = { content(state.argument) },
        modifier = modifier,
        destructive = state.destructive,
        onDismiss = state::dismiss,
        dismissButton = dismissButton,
        confirmButton = confirmButton,
    )
}

@Composable
private fun Dialog(
    title: String,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    text: @Composable () -> Unit,
    onDismiss: () -> Unit,
    dismissButton: @Composable () -> Unit = {
        TextButton(text = stringResource(android.R.string.cancel), onClick = onDismiss)
    },
    confirmButton: @Composable () -> Unit,
) = M3AlertDialog(
    title = { Text(title) },
    text = text,
    onDismissRequest = onDismiss,
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
        val state = rememberConfirmationDialogState(onConfirm = {}, initiallyShowing = true)
        ConfirmationDialog(
            state = state,
            title = "Restart Build",
            text = "Are you sure you want to restart this build?",
            confirmAction = "Restart",
        )
    }
}

@Preview
@Composable
private fun PreviewConfirmationWithId() {
    PreviewScreen(modifier = Modifier.fillMaxSize()) {
        val state = rememberConfirmationDialogState<String, Unit>(
            onConfirm = { id, _ -> id + id },
            initialValue = "build-42",
        )
        ConfirmationDialog(
            state = state,
            title = "Delete Build",
            confirmAction = "Delete",
        ) { argument ->
            Text("Delete build $argument?")
        }
    }
}

@Preview
@Composable
private fun PreviewDestructiveConfirmation() {
    PreviewScreen(modifier = Modifier.fillMaxSize()) {
        val state = rememberConfirmationDialogState(
            onConfirm = {}, initiallyShowing = true, destructive = true,
        )
        ConfirmationDialog(
            state = state,
            title = "Clear All Downloads",
            text = "This will permanently delete all downloaded files.",
            confirmAction = "Clear All",
        )
    }
}

@Preview
@Composable
private fun PreviewConfirmationDialog() {
    PreviewScreen(modifier = Modifier.fillMaxSize()) {
        val state = rememberConfirmationDialogState(
            onConfirm = { _: String -> },
            initiallyShowing = true,
        )
        if (!state.isShowing) return@PreviewScreen
        var text by remember { mutableStateOf(TextFieldValue()) }
        ConfirmationDialog(
            state = state,
            title = "Abort Build",
            confirmButton = {
                TextButton(
                    text = "Abort",
                    onClick = { state.confirmAndDismiss(text.text) },
                )
            },
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Abort Reason") },
                singleLine = true,
            )
        }
    }
}
