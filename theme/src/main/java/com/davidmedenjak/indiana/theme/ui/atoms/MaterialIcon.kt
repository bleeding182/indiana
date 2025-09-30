package com.davidmedenjak.indiana.theme.ui.atoms

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MaterialIcon(
    icon: String,
    size: Dp,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    var isSelected by remember { mutableStateOf(false) }

    val animatedFill by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "fill"
    )
    val animatedWeight by animateFloatAsState(
        targetValue = if (isSelected) 400f else 700f,
        label = "weight"
    )

    val context = LocalContext.current
    val typeface = remember {
        ResourcesCompat.getFont(context, R.font.symbols)!!
    }

    val tintColor = if (tint.isUnspecified) {
        IndianaTheme.colorScheme.onSurfaceVariant.toArgb()
    } else {
        tint.toArgb()
    }
    val pxSize = with(LocalDensity.current) { size.toPx() }
    Canvas(
        modifier = modifier
            .size(size)
    ) {
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = tintColor
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = pxSize
            this.typeface = typeface
            setFontVariationSettings("'FILL' $animatedFill, 'wght' $animatedWeight")
        }

        val textY = pxSize / 2 - (paint.ascent() + paint.descent()) / 2

        drawContext.canvas.nativeCanvas.drawText(
            icon,
            pxSize / 2,
            textY,
            paint
        )
    }
}