package com.davidmedenjak.indiana.screens.about

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.davidmedenjak.fontsubsetting.MaterialSymbols
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface

@Composable
fun AboutScreen(
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            val context = LocalContext.current
            val info = context.packageManager.getApplicationInfo(context.packageName, 0)

            LargeFlexible(
                title = { Text(stringResource(R.string.about_title)) },
                subtitle = { Text(info.loadLabel(context.packageManager).toString()) },
                navigationIcon = { Up(onNavigateUp) }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 16.dp)) {
            MaterialIcon(icon = MaterialSymbols.info, size = 24.dp)
            Text(stringResource(R.string.about_content))
//            Button("Show me", onClick = {})
        }
    }
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
            .clickable { isSelected = !isSelected }
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

@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        AboutScreen(onNavigateUp = {})
    }
}
