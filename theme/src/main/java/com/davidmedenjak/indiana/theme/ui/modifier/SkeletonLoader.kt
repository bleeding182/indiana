package com.davidmedenjak.indiana.theme.ui.modifier

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sqrt

/**
 * Calculates the appropriate height for a text skeleton based on TextStyle typography metrics.
 *
 * @param textStyle The TextStyle to extract metrics from
 * @param includeLeading Whether to include line spacing (true for full lineHeight, false for just text bounds)
 * @return The height that the text would occupy
 */
@Composable
fun getTextSkeletonHeight(textStyle: TextStyle, includeLeading: Boolean = false): Dp {
    val density = LocalDensity.current

    return with(density) {
        val fontSize = if (textStyle.fontSize != TextUnit.Unspecified) {
            textStyle.fontSize.toDp()
        } else {
            16.sp.toDp()
        }

        if (includeLeading) {
            // Use full line height including leading space
            if (textStyle.lineHeight != TextUnit.Unspecified) {
                textStyle.lineHeight.toDp()
            } else {
                fontSize * 1.2f
            }
        } else {
            // Use just the text bounds (ascent + descent) without leading
            // Most fonts: ascent ≈ 0.75 * fontSize, descent ≈ 0.25 * fontSize
            // Total text bounds ≈ fontSize (since ascent + descent ≈ 1.0 * fontSize)
            fontSize
        }
    }
}

/**
 * Calculates detailed typography metrics for more precise skeleton dimensions.
 * This gives you access to individual metrics like ascent, descent, etc.
 */
@Composable
fun getTextMetrics(textStyle: TextStyle): TextMetrics {
    val density = LocalDensity.current

    return with(density) {
        val fontSize = if (textStyle.fontSize != TextUnit.Unspecified) {
            textStyle.fontSize.toDp()
        } else {
            16.sp.toDp()
        }

        val lineHeight = if (textStyle.lineHeight != TextUnit.Unspecified) {
            textStyle.lineHeight.toDp()
        } else {
            fontSize * 1.2f // Default line height is typically 1.2x font size
        }

        // Typography metrics based on typical font proportions
        // These are approximations - real values depend on the specific font
        val ascent = fontSize * 0.75f      // Distance from baseline to cap height
        val descent = fontSize * 0.25f     // Distance from baseline to descenders
        val leading = lineHeight - (ascent + descent)
        val textBounds = ascent + descent  // Just the visual text height

        TextMetrics(
            fontSize = fontSize,
            lineHeight = lineHeight,
            ascent = ascent,
            descent = descent,
            leading = leading,
            textBounds = textBounds
        )
    }
}

/**
 * Data class containing typography metrics for text skeleton calculations.
 */
data class TextMetrics(
    val fontSize: Dp,
    val lineHeight: Dp,     // Full line height including leading
    val ascent: Dp,         // Distance from baseline to top of tallest characters
    val descent: Dp,        // Distance from baseline to bottom of lowest characters
    val leading: Dp,        // Extra space between lines
    val textBounds: Dp      // Just ascent + descent (visual text height)
)

/**
 * Data class for custom drawing bounds within a component.
 */
data class TextBounds(
    val textHeight: Dp,     // Height of the text bounds to draw
    val lineHeight: Dp      // Full line height of the component
)

/**
 * Extension function to create a text skeleton with proper height based on TextStyle.
 * Width should be set separately using other modifiers.
 * The component maintains full line height for proper spacing, but only draws
 * the skeleton shape for the actual text bounds (excluding leading space).
 */
fun Modifier.textSkeletonLoader(
    textStyle: TextStyle,
    includeLeading: Boolean = false,
    baseColor: Color = Color.Unspecified,
    highlightColor: Color = Color.Unspecified,
    animationDuration: Int = 1500
): Modifier = composed {
    val metrics = getTextMetrics(textStyle)

    this
        .height(metrics.lineHeight) // Always use full line height for component
        .skeletonLoader(
            shape = RoundedCornerShape(2.dp),
            baseColor = baseColor,
            highlightColor = highlightColor,
            animationDuration = animationDuration,
            drawBounds = if (includeLeading) {
                // Draw the full line height
                null
            } else {
                // Draw only text bounds, centered within line height
                TextBounds(
                    textHeight = metrics.textBounds,
                    lineHeight = metrics.lineHeight
                )
            }
        )
}

/**
 * CompositionLocal for providing a base timestamp for skeleton animation synchronization
 */
val LocalSkeletonBaseTime = staticCompositionLocalOf<Long?> { null }

/**
 * Provider component that sets a base timestamp for skeleton animation synchronization.
 * Place this at the screen level for all skeletons on that screen to sync together.
 *
 * @param baseTime Optional custom base timestamp. If null, uses current time.
 */
@Composable
fun SkeletonTimeProvider(
    baseTime: Long? = null,
    content: @Composable () -> Unit
) {
    val actualBaseTime = baseTime ?: remember { System.currentTimeMillis() }

    CompositionLocalProvider(LocalSkeletonBaseTime provides actualBaseTime) {
        content()
    }
}

fun Modifier.skeletonLoader(
    shape: Shape = RoundedCornerShape(4.dp),
    baseColor: Color = Color.Unspecified,
    highlightColor: Color = Color.Unspecified,
    animationDuration: Int = 1500,
    drawBounds: TextBounds? = null
): Modifier = composed {

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    var componentPosition by remember { mutableStateOf(Offset.Zero) }

    // Get screen dimensions in pixels
    val screenSize = with(density) {
        Offset(
            x = windowInfo.containerSize.width.toFloat(),
            y = windowInfo.containerSize.height.toFloat(),
        )
    }

    // Define colors with proper defaults
    val actualBaseColor = if (baseColor != Color.Unspecified) {
        baseColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val actualHighlightColor = if (highlightColor != Color.Unspecified) {
        highlightColor
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    }

    // Get base time from provider, or use current time as fallback
    val baseTime = LocalSkeletonBaseTime.current ?: System.currentTimeMillis()

    // Calculate animation progress based on elapsed time since base time
    var animationProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(animationDuration, baseTime) {
        while (true) {
            val currentTime = System.currentTimeMillis()
            val elapsed = currentTime - baseTime
            animationProgress = ((elapsed % animationDuration) / animationDuration.toFloat())
            delay(16) // ~60fps updates
        }
    }

    this
        .onGloballyPositioned { coordinates ->
            componentPosition = coordinates.positionInRoot()
        }
        .drawBehind {
            drawSkeletonLoader(
                shape = shape,
                baseColor = actualBaseColor,
                highlightColor = actualHighlightColor,
                animationProgress = animationProgress,
                componentPosition = componentPosition,
                screenSize = screenSize,
                drawBounds = drawBounds,
                density = density
            )
        }
}

/**
 * Draws the skeleton loader with a diagonal wave animation effect.
 */
private fun DrawScope.drawSkeletonLoader(
    shape: Shape,
    baseColor: Color,
    highlightColor: Color,
    animationProgress: Float,
    componentPosition: Offset,
    screenSize: Offset,
    drawBounds: TextBounds? = null,
    density: Density
) {
    // Calculate the diagonal distance from top-left corner for this component
    val componentDiagonal = sqrt(
        componentPosition.x * componentPosition.x +
                componentPosition.y * componentPosition.y
    )

    // Calculate the maximum diagonal distance of the screen
    val screenDiagonal = sqrt(
        screenSize.x * screenSize.x +
                screenSize.y * screenSize.y
    )

    // Normalize the component's diagonal position (0 to 1)
    val normalizedPosition = if (screenDiagonal > 0f) {
        (componentDiagonal / screenDiagonal).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Calculate the wave effect
    val wavePosition = animationProgress // 0 to 1
    val distanceFromWave = kotlin.math.abs(wavePosition - normalizedPosition)

    // Create the pulse intensity based on distance from wave
    val pulseWidth = 0.3f // Width of the pulse effect
    val intensity = if (distanceFromWave <= pulseWidth) {
        1f - (distanceFromWave / pulseWidth)
    } else {
        0f
    }

    // Interpolate between base and highlight colors
    val currentColor = lerp(baseColor, highlightColor, intensity)

    if (drawBounds != null) {
        // Custom text bounds - draw only the text portion, centered vertically
        with(density) {
            val textHeightPx = drawBounds.textHeight.toPx()
            val lineHeightPx = drawBounds.lineHeight.toPx()
            val verticalOffset = (lineHeightPx - textHeightPx) / 2f

            // Create a clipping rectangle for the text bounds
            clipRect(
                left = 0f,
                top = verticalOffset,
                right = size.width,
                bottom = verticalOffset + textHeightPx
            ) {
                // Create shape outline with the text height
                val textSize = androidx.compose.ui.geometry.Size(
                    width = size.width,
                    height = textHeightPx
                )
                val outline = shape.createOutline(textSize, layoutDirection, this)

                // Translate to draw at the correct vertical position
                translate(0f, verticalOffset) {
                    drawOutline(
                        outline = outline,
                        color = currentColor
                    )
                }
            }
        }
    } else {
        // Use full component size
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(
            outline = outline,
            color = currentColor
        )
    }
}

/**
 * Interpolates between two colors
 */
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + fraction * (stop.red - start.red),
        green = start.green + fraction * (stop.green - start.green),
        blue = start.blue + fraction * (stop.blue - start.blue),
        alpha = start.alpha + fraction * (stop.alpha - start.alpha)
    )
}

// Preview composable to demonstrate the skeleton loader with synchronized animation
@Preview(showBackground = true)
@Composable
fun SkeletonLoaderPreview() {
    MaterialTheme {
        // Wrap content in SkeletonTimeProvider for synchronized animations
        SkeletonTimeProvider {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Synchronized skeleton animations:")

                // Text skeleton examples - all will animate in sync
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .textSkeletonLoader(
                                textStyle = MaterialTheme.typography.headlineLarge,
                                includeLeading = false
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .textSkeletonLoader(
                                textStyle = MaterialTheme.typography.bodyLarge,
                                includeLeading = false
                            )
                    )

                    Box(
                        modifier = Modifier
                            .width(250.dp)
                            .textSkeletonLoader(
                                textStyle = MaterialTheme.typography.bodyLarge,
                                includeLeading = false
                            )
                    )

                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .textSkeletonLoader(
                                textStyle = MaterialTheme.typography.bodyMedium,
                                includeLeading = false
                            )
                    )
                }

                // Mixed skeleton types - all synchronized
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .skeletonLoader(shape = RoundedCornerShape(40.dp))
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .textSkeletonLoader(
                                    textStyle = MaterialTheme.typography.titleMedium
                                )
                        )
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .textSkeletonLoader(
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                        )
                    }
                }

                // Comparison section
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Different drawing modes:")

                    Text("includeLeading = true:")
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .textSkeletonLoader(
                                textStyle = MaterialTheme.typography.bodyLarge,
                                includeLeading = true
                            )
                    )

                    Text("includeLeading = false:")
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .textSkeletonLoader(
                                textStyle = MaterialTheme.typography.bodyLarge,
                                includeLeading = false
                            )
                    )
                }
            }
        }
    }
}

/*
USAGE SUMMARY:

1. Screen-level synchronization (recommended for navigation):
   @Composable
   fun MyScreen() {
       SkeletonTimeProvider {
           // All skeleton loaders will sync to the same base time
           MyContent()
       }
   }

2. Custom base time (for specific sync scenarios):
   val navigationTime = remember { System.currentTimeMillis() }
   SkeletonTimeProvider(baseTime = navigationTime) {
       MyContent()
   }

3. Standalone usage (automatic fallback):
   Box(modifier = Modifier.width(200.dp).textSkeletonLoader(MaterialTheme.typography.bodyLarge))
   // Works without provider

4. Text skeleton options:
   - includeLeading = false: Draws only text bounds (good for tight layouts)
   - includeLeading = true: Draws full line height

5. General skeleton:
   Box(modifier = Modifier.size(80.dp).skeletonLoader(shape = CircleShape))
*/