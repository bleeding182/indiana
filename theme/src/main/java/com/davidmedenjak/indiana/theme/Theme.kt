package com.davidmedenjak.indiana.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.ColorScheme as M3ColorScheme
import androidx.compose.material3.Shapes as M3Shapes
import androidx.compose.material3.Typography as M3Typography

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val LightColorScheme = expressiveLightColorScheme()

internal val LocalTypography = staticCompositionLocalOf { Typography(M3Typography()) }
internal val LocalShapes = staticCompositionLocalOf { Shapes() }
internal val LocalColorScheme = staticCompositionLocalOf { ColorScheme(LightColorScheme) }

@Immutable
class Shapes(val shapes: M3Shapes = M3Shapes()) {
    // Shapes None and Full are omitted as None is a RectangleShape and Full is a CircleShape.
    val extraSmall: CornerBasedShape = shapes.extraSmall
    val small: CornerBasedShape = shapes.small
    val medium: CornerBasedShape = shapes.medium
    val large: CornerBasedShape = shapes.large
    val extraLarge: CornerBasedShape = shapes.extraLarge
}

@Immutable
class Typography(val typography: M3Typography = M3Typography()) {
    val displayLarge: TextStyle = typography.displayLarge
    val displayMedium: TextStyle = typography.displayMedium
    val displaySmall: TextStyle = typography.displaySmall
    val headlineLarge: TextStyle = typography.headlineLarge
    val headlineMedium: TextStyle = typography.headlineMedium
    val headlineSmall: TextStyle = typography.headlineSmall
    val titleLarge: TextStyle = typography.titleLarge
    val titleMedium: TextStyle = typography.titleMedium
    val titleSmall: TextStyle = typography.titleSmall
    val bodyLarge: TextStyle = typography.bodyLarge
    val bodyMedium: TextStyle = typography.bodyMedium
    val bodySmall: TextStyle = typography.bodySmall
    val labelLarge: TextStyle = typography.labelLarge
    val labelMedium: TextStyle = typography.labelMedium
    val labelSmall: TextStyle = typography.labelSmall
}

@Immutable
class ColorScheme(val colorScheme: M3ColorScheme) {
    val primary: Color = colorScheme.primary
    val onPrimary: Color = colorScheme.onPrimary
    val primaryContainer: Color = colorScheme.primaryContainer
    val onPrimaryContainer: Color = colorScheme.onPrimaryContainer
    val inversePrimary: Color = colorScheme.inversePrimary
    val secondary: Color = colorScheme.secondary
    val onSecondary: Color = colorScheme.onSecondary
    val secondaryContainer: Color = colorScheme.secondaryContainer
    val onSecondaryContainer: Color = colorScheme.onSecondaryContainer
    val tertiary: Color = colorScheme.tertiary
    val onTertiary: Color = colorScheme.onTertiary
    val tertiaryContainer: Color = colorScheme.tertiaryContainer
    val onTertiaryContainer: Color = colorScheme.onTertiaryContainer
    val background: Color = colorScheme.background
    val onBackground: Color = colorScheme.onBackground
    val surface: Color = colorScheme.surface
    val onSurface: Color = colorScheme.onSurface
    val surfaceVariant: Color = colorScheme.surfaceVariant
    val onSurfaceVariant: Color = colorScheme.onSurfaceVariant
    val surfaceTint: Color = colorScheme.surfaceTint
    val inverseSurface: Color = colorScheme.inverseSurface
    val inverseOnSurface: Color = colorScheme.inverseOnSurface
    val error: Color = colorScheme.error
    val onError: Color = colorScheme.onError
    val errorContainer: Color = colorScheme.errorContainer
    val onErrorContainer: Color = colorScheme.onErrorContainer
    val outline: Color = colorScheme.outline
    val outlineVariant: Color = colorScheme.outlineVariant
    val scrim: Color = colorScheme.scrim
    val surfaceBright: Color = colorScheme.surfaceBright
    val surfaceDim: Color = colorScheme.surfaceDim
    val surfaceContainer: Color = colorScheme.surfaceContainer
    val surfaceContainerHigh: Color = colorScheme.surfaceContainerHigh
    val surfaceContainerHighest: Color = colorScheme.surfaceContainerHighest
    val surfaceContainerLow: Color = colorScheme.surfaceContainerLow
    val surfaceContainerLowest: Color = colorScheme.surfaceContainerLowest

    fun contentColorFor(backgroundColor: Color) = colorScheme.contentColorFor(backgroundColor)
}


object IndianaTheme {

    val colorScheme: ColorScheme
        @Composable @ReadOnlyComposable get() = LocalColorScheme.current

    val typography: Typography
        @Composable @ReadOnlyComposable get() = LocalTypography.current

    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = LocalShapes.current
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IndianaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalColorScheme provides ColorScheme(colorScheme),
        LocalTypography provides Typography(M3Typography()),
        LocalShapes provides Shapes(M3Shapes()),
    ) {
        MaterialExpressiveTheme(
            colorScheme = IndianaTheme.colorScheme.colorScheme,
            typography = IndianaTheme.typography.typography,
            shapes = IndianaTheme.shapes.shapes,
            content = content
        )
    }
}