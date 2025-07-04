package com.davidmedenjak.indiana.theme.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun PreviewShapes() {
    PreviewScreen {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LazyVerticalGrid(
                columns = GridCells.FixedSize(64.dp),
                contentPadding = PaddingValues(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                allMaterialShapes().forEach { polygon ->
                    item {
                        Spacer(
                            modifier =
                                Modifier
                                    .requiredSize(56.dp)
                                    .clip(polygon.toShape())
                                    .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun allMaterialShapes(): List<RoundedPolygon> {
    return listOf(
        MaterialShapes.Circle,
        MaterialShapes.Square,
        MaterialShapes.Slanted,
        MaterialShapes.Arch,
        MaterialShapes.Fan,
        MaterialShapes.Arrow,
        MaterialShapes.SemiCircle,
        MaterialShapes.Oval,
        MaterialShapes.Pill,
        MaterialShapes.Triangle,
        MaterialShapes.Diamond,
        MaterialShapes.ClamShell,
        MaterialShapes.Pentagon,
        MaterialShapes.Gem,
        MaterialShapes.Sunny,
        MaterialShapes.VerySunny,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Cookie6Sided,
        MaterialShapes.Cookie7Sided,
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Cookie12Sided,
        MaterialShapes.Ghostish,
        MaterialShapes.Clover4Leaf,
        MaterialShapes.Clover8Leaf,
        MaterialShapes.Burst,
        MaterialShapes.SoftBurst,
        MaterialShapes.Boom,
        MaterialShapes.SoftBoom,
        MaterialShapes.Flower,
        MaterialShapes.Puffy,
        MaterialShapes.PuffyDiamond,
        MaterialShapes.PixelCircle,
        MaterialShapes.PixelTriangle,
        MaterialShapes.Bun,
        MaterialShapes.Heart
    )
}