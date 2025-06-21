package com.davidmedenjak.indiana.theme.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt

@Stable
fun Arrangement.percentage(percentage: Float = 0.25f) = object : Arrangement.HorizontalOrVertical {
    override val spacing = 0.dp

    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray,
    ) =
        if (layoutDirection == LayoutDirection.Ltr) {
            placeAtPercent(totalSize, sizes, outPositions, reverseInput = false)
        } else {
            placeAtPercent(totalSize, sizes, outPositions, reverseInput = true)
        }

    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) =
        placeAtPercent(totalSize, sizes, outPositions, reverseInput = false)

    override fun toString() = "Arrangement#percentage($percentage)"

    private fun placeAtPercent(
        totalSize: Int,
        size: IntArray,
        outPosition: IntArray,
        reverseInput: Boolean,
    ) {
        val consumedSize = size.fold(0) { a, b -> a + b }
        var current = (totalSize - consumedSize).toFloat() * percentage
        size.forEachIndexed(reverseInput) { index, it ->
            outPosition[index] = current.fastRoundToInt()
            current += it.toFloat()
        }
    }

    private inline fun IntArray.forEachIndexed(reversed: Boolean, action: (Int, Int) -> Unit) {
        if (!reversed) {
            forEachIndexed(action)
        } else {
            for (i in (size - 1) downTo 0) {
                action(i, get(i))
            }
        }
    }
}
