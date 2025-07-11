package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import androidx.compose.material3.RadioButton as M3RadioButton

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    M3RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = RadioButtonDefaults.colors(),
        interactionSource = interactionSource
    )
}

@Composable
fun RadioButtonGroup(
    options: List<String>,
    selectedOption: String,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier.selectableGroup()
    ) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = { onSelectionChange(option) },
                        role = Role.RadioButton,
                        enabled = enabled
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = null,
                    enabled = enabled
                )
                Text(
                    text = option,
                    style = IndianaTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun RadioButtonPreview() {
    PreviewSurface {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = true,
                    onClick = { }
                )
                Text("Selected")
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = false,
                    onClick = { }
                )
                Text("Not selected")
            }
            
            var selectedOption by remember { mutableStateOf("Option 1") }
            RadioButtonGroup(
                options = listOf("Option 1", "Option 2", "Option 3"),
                selectedOption = selectedOption,
                onSelectionChange = { selectedOption = it }
            )
        }
    }
}