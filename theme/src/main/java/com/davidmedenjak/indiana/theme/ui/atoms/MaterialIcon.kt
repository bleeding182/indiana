package com.davidmedenjak.indiana.theme.ui.atoms

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.davidmedenjak.fontsubsetting.MaterialSymbols
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.R

object Icons {
    val arrowCircleDown = MaterialSymbols.arrowCircleDown
    val arrowBack = MaterialSymbols.arrowBack
    val Error = MaterialSymbols.error
    val MoreVert = MaterialSymbols.moreVert
    val Stop = MaterialSymbols.stop
    val Refresh = MaterialSymbols.refresh
    val InstallMobile = MaterialSymbols.installMobile
    val FilePresent = MaterialSymbols.filePresent
    val HourglassEmpty = MaterialSymbols.hourglassEmpty
    val Check = MaterialSymbols.check
    val ErrorOutline = MaterialSymbols.errorOutline
    val Block = MaterialSymbols.block
    val QuestionMark = MaterialSymbols.questionMark
    val Storage = MaterialSymbols.storage
    val Delete = MaterialSymbols.delete
    val PlayArrow = MaterialSymbols.playArrow

}

private object MaterialSymbolsTypeface {
    @Volatile
    private var instance: Typeface? = null

    fun get(context: Context): Typeface {
        if (instance == null) {
            instance = ResourcesCompat.getFont(context, R.font.material_symbols_rounded)!!
        }
        return instance!!
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MaterialIcon(
    icon: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    tint: Color = Color.Unspecified,
) {

    val animatedFill by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "fill"
    )
    val animatedWeight by animateFloatAsState(
        targetValue = if (isSelected) 600f else 400f,
        label = "weight"
    )
    val animatedGrad by animateFloatAsState(
        targetValue = if (isSelected) 200f else -25f,
        label = "grad"
    )

    val context = LocalContext.current
    val typeface = MaterialSymbolsTypeface.get(context)

    val tintColor = if (tint.isUnspecified) {
        IndianaTheme.colorScheme.onSurfaceVariant.toArgb()
    } else {
        tint.toArgb()
    }

    val drawModifier = modifier.drawWithCache {
        val pxSize = size.minDimension

        val paint = Paint().apply {
            isAntiAlias = true
            color = tintColor
            textAlign = Paint.Align.CENTER
            textSize = pxSize
            this.typeface = typeface
            setFontVariationSettings("'FILL' $animatedFill, 'GRAD' $animatedGrad, 'wght' $animatedWeight")
        }

        val textY = pxSize / 2 - (paint.ascent() + paint.descent()) / 2
        onDrawBehind {
            drawContext.canvas.nativeCanvas.drawText(icon, center.x, textY, paint)
        }
    }
    Box(modifier = drawModifier)
}

@Preview
@Composable
private fun Preview() {
    Surface {
        Column(modifier = Modifier.padding(24.dp)) {
            var isSelected by remember { mutableStateOf(false) }
            val size = Modifier
                .size(24.dp)
                .clickable { isSelected = !isSelected }
            val icons = listOf(
                Icons.arrowCircleDown,
                Icons.Stop,
                Icons.Block,
                Icons.Check,
                Icons.arrowBack,
                Icons.Delete,
                Icons.Error,
                Icons.FilePresent,
                Icons.HourglassEmpty,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                icons.forEach {
                    MaterialIcon(it, modifier = size, isSelected = isSelected)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                icons.forEach {
                    MaterialIcon(it, modifier = size, isSelected = !isSelected)
                }
            }
        }
    }
}