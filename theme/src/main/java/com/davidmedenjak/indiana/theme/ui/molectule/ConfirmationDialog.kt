package com.davidmedenjak.indiana.theme.ui.molectule

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.davidmedenjak.indiana.theme.ui.atoms.TextButton

data class Confirmation(
    val title: String,
    val text: String,
    val action: String,
    val callback: () -> Unit,
)

data class ConfirmationDialogStateHolder(val initialConfirmation: Confirmation? = null) {
    var confirmation by mutableStateOf(initialConfirmation)
        private set

    fun confirm(confirmation: Confirmation) {
        this.confirmation = confirmation
    }

    fun dismiss() {
        confirmation = null
    }
}

@Composable
fun rememberConfirmationDialogState(
    initialConfirmation: Confirmation? = null
): ConfirmationDialogStateHolder {
    val dialogStateHolder = remember {
        ConfirmationDialogStateHolder(initialConfirmation)
    }

    val confirmation = dialogStateHolder.confirmation
    if (confirmation != null) {
        ConfirmationDialog(
            title = confirmation.title,
            text = confirmation.text,
            confirmAction = confirmation.action,
            onConfirm = {
                dialogStateHolder.dismiss()
                confirmation.callback()
            },
            onDismissRequest = (dialogStateHolder::dismiss)
        )
    }
    return dialogStateHolder
}

@Composable
private fun ConfirmationDialog(
    title: String,
    text: String,
    confirmAction: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        title = { Text(title) },
        text = { Text(text) },
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        confirmButton = {
            TextButton(text = confirmAction, onClick = onConfirm)
        }
    )
}
