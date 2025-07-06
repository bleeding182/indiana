package com.davidmedenjak.indiana.theme.ui.molectule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import kotlin.math.abs
import kotlin.math.max

@Composable
fun PropertyLayout(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 4.dp,
    content: @Composable @UiComposable () -> Unit
) = Layout(
    content = content,
    modifier = modifier,
    measurePolicy = PropertyLayoutMeasurePolicy(horizontalPadding, verticalPadding)
)

class PropertyLayoutMeasurePolicy(
    private val horizontalPadding: Dp,
    private val verticalPadding: Dp
) : MeasurePolicy {

    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        require(measurables.size <= 2) {
            "Only 2 children allowed"
        }
        val horizontalPaddingPx = horizontalPadding.roundToPx()
        val maxWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth - horizontalPaddingPx
        } else {
            constraints.maxWidth
        }
        val verticalPaddingPx = verticalPadding.roundToPx()
        val maxHeight = if (constraints.hasBoundedHeight) {
            constraints.maxHeight - verticalPaddingPx
        } else {
            constraints.maxHeight
        }
        val childConstraints = Constraints(
            minWidth = 0,
            minHeight = 0,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
        )

        val placeables = measurables.map { it.measure(childConstraints) }

        val idealWidth = placeables.fastSumBy { it.width } + horizontalPaddingPx

        if (idealWidth <= constraints.maxWidth) {
            val layoutHeight =
                constraints.constrainHeight(placeables.maxByOrNull { it.height }?.height ?: 0)
            val layoutWidth = constraints.constrainWidth(idealWidth)
            return layout(width = layoutWidth, height = layoutHeight) {
                val label = placeables[0]
                val text = placeables[1]
                val labelBaseline = label[FirstBaseline]
                val textBaseline = text[FirstBaseline]
                if (labelBaseline == AlignmentLine.Unspecified || textBaseline == AlignmentLine.Unspecified) {
                    label.placeRelative(0, (layoutHeight - label.height) / 2)
                    text.placeRelative(layoutWidth - text.width, (layoutHeight - text.height) / 2)
                } else {
                    val baesline = abs(textBaseline - labelBaseline)
                    val offset = abs(text.height - label.height)
                    label.placeRelative(0, offset + baesline - labelBaseline)
                    text.placeRelative(layoutWidth - text.width, offset + baesline - textBaseline)
                }
            }
        }

        val idealHeight = placeables.fastSumBy { it.height } + verticalPaddingPx
        val layoutHeight = constraints.constrainHeight(idealHeight)
        val layoutWidth =
            constraints.constrainWidth(placeables.maxByOrNull { it.width }?.width ?: 0)

        return layout(layoutWidth, layoutHeight) {
            val label = placeables[0]
            val text = placeables[1]
            if (layoutHeight == idealHeight) {
                label.placeRelative(0, 0)
                text.placeRelative(layoutWidth - text.width, layoutHeight - text.height)
            } else {
                val labelTop = max(0, (layoutHeight - idealHeight) / 2)
                label.placeRelative(0, labelTop)
                text.placeRelative(
                    layoutWidth - text.width,
                    labelTop + verticalPaddingPx + label.height
                )
            }
        }
    }
}

@Preview
@Composable
private fun Fit() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val modifier = Modifier
                .border(Dp.Hairline, color = IndianaTheme.colorScheme.onSurface)

            PropertyLayout(modifier = modifier.widthIn(min = 120.dp), content = { Boxes() })
            PropertyLayout(modifier = modifier.widthIn(min = 240.dp), content = { Boxes() })
            PropertyLayout(modifier = modifier.widthIn(max = 240.dp), content = { Boxes() })

            PropertyLayout(modifier = modifier.width(width = 64.dp), content = { Boxes() })
            PropertyLayout(modifier = modifier.width(width = 64.dp), content = { Boxes() })
            PropertyLayout(modifier = modifier.width(width = 64.dp), content = { Boxes() })

            PropertyLayout(modifier = modifier, content = {
                Text("Hello", style = IndianaTheme.typography.labelSmall)
                Text("World", style = IndianaTheme.typography.titleLarge)
            })
            PropertyLayout(modifier = modifier, content = {
                Text("Hello", style = IndianaTheme.typography.titleLarge)
                Text("World", style = IndianaTheme.typography.labelSmall)
            })

            PropertyLayout(
                modifier = modifier
                    .width(width = 64.dp)
                    .height(height = 100.dp),
                content = { Boxes() })
            PropertyLayout(
                modifier = modifier
                    .width(width = 64.dp)
                    .heightIn(min = 100.dp),
                content = { Boxes() })
            PropertyLayout(
                modifier = modifier
                    .width(width = 64.dp)
                    .heightIn(max = 100.dp),
                content = { Boxes() })
            PropertyLayout(
                modifier = modifier
                    .width(width = 64.dp)
                    .heightIn(min = 20.dp),
                content = { Boxes() })
            PropertyLayout(
                modifier = modifier
                    .width(width = 64.dp)
                    .heightIn(max = 20.dp),
                content = { Boxes() })
        }
    }
}

@Composable
private fun Boxes() {
    Box(
        modifier = Modifier
            .background(Color.Red.copy(alpha = 0.4f))
            .size(40.dp, 20.dp)
    )
    Box(
        modifier = Modifier
            .background(Color.Blue.copy(alpha = 0.4f))
            .size(60.dp, 25.dp)
    )
}