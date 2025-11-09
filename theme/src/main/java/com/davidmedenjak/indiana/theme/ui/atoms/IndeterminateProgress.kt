@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.davidmedenjak.indiana.theme.ui.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface
import androidx.compose.material3.LoadingIndicator as M3LoadingIndicator

private val polygons = listOf(
    MaterialShapes.ClamShell,
    MaterialShapes.PuffyDiamond,
    MaterialShapes.Pentagon,
    MaterialShapes.Cookie4Sided,
)

@Composable
fun IndeterminateProgress(modifier: Modifier = Modifier) = M3LoadingIndicator(
    polygons = polygons,
    modifier = modifier,
)

@Composable
fun DeterministicProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier
) = Box(contentAlignment = Alignment.Center, modifier = modifier.size(24.dp)) {
    MaterialIcon(Icons.arrowCircleDown, Modifier.size(24.dp))
    CircularWavyProgressIndicator(
        progress = progress,
        modifier = Modifier.size(20.dp),
        stroke = Stroke(
            width = with(LocalDensity.current) {
                2.dp.toPx()
            },
            cap = StrokeCap.Round,
        ),
        trackStroke = Stroke(
            width = with(LocalDensity.current) {
                2.dp.toPx()
            },
            cap = StrokeCap.Round,
        ),
        wavelength = 8.dp,
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewSurface {
        Column {
            DeterministicProgress({ 0.3f })
            DeterministicProgress({ 0.5f })
            DeterministicProgress({ 0.7f })
            DeterministicProgress({ 0.9f })
            DeterministicProgress({ 1f })
        }
    }
}
