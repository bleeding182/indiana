package com.davidmedenjak.indiana.theme.ui.preview

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.atoms.TextField

@Preview(showSystemUi = true)
@Composable
fun TempInputWithUnitSelection() {
    PreviewSurface(modifier = Modifier.fillMaxWidth()) {
        Row (
            modifier = Modifier.height(IntrinsicSize.Max).fillMaxWidth(),
        ) {
            TextField(
                value = TextFieldValue("Test"),
                onValueChange = {  },
                label = { Text("Temperature in Degrees") },
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.Blue),
            )

            val units = listOf<String>("°C", "°F", "°K")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxHeight().height(IntrinsicSize.Max))  {
                units.forEachIndexed { index, label ->
                    SegmentedButton (
                        label = { Text(label) },
                        selected = index ==1,
                        shape = RoundedCornerShape(4.dp),
                        onClick = {  },
                        icon = {},
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.requiredSizeIn(minWidth = 40.dp).defaultMinSize(40.dp).fillMaxHeight()
                    )
                }
            }
        }
    }
}