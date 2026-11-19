package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class DairyExtendedColors(
    val eveningAccent: Color,
    val eveningContainer: Color,
    val morningAccent: Color,
    val morningContainer: Color,
    val deliveredAccent: Color,
    val deliveredContainer: Color,
    val absentAccent: Color,
    val absentContainer: Color
)

val LocalDairyColors = staticCompositionLocalOf {
    DairyExtendedColors(
        eveningAccent = EveningIndigoLight,
        eveningContainer = EveningIndigoContainerLight,
        morningAccent = MarigoldTertiary,
        morningContainer = MarigoldContainer,
        deliveredAccent = FreshEmeraldSecondary,
        deliveredContainer = FreshEmeraldContainer,
        absentAccent = TerracottaError,
        absentContainer = TerracottaErrorContainer
    )
}

private val LightColorScheme = lightColorScheme(
    primary = DairyTealPrimary,
    onPrimary = DairyTealOnPrimary,
    primaryContainer = DairyTealContainer,
    onPrimaryContainer = DairyTealOnContainer,
    secondary = FreshEmeraldSecondary,
    onSecondary = FreshEmeraldOnSecondary,
    secondaryContainer = FreshEmeraldContainer,
    onSecondaryContainer = FreshEmeraldOnContainer,
    tertiary = MarigoldTertiary,
    onTertiary = MarigoldOnTertiary,
    tertiaryContainer = MarigoldContainer,
    onTertiaryContainer = MarigoldOnContainer,
    error = TerracottaError,
    onError = TerracottaOnError,
    errorContainer = TerracottaErrorContainer,
    onErrorContainer = TerracottaOnErrorContainer,
    background = WarmMilkBackground,
    onBackground = InkOnSurface,
    surface = WarmMilkSurface,
    onSurface = InkOnSurface,
    surfaceVariant = WarmMilkSurfaceContainerLow,
    onSurfaceVariant = InkOnSurfaceVariant,
    surfaceContainerLowest = WarmMilkSurfaceContainerLowest,
    surfaceContainerLow = WarmMilkSurfaceContainerLow,
    surfaceContainer = WarmMilkSurfaceContainer,
    surfaceContainerHigh = WarmMilkSurfaceContainerHigh,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkTealPrimary,
    onPrimary = DarkTealOnPrimary,
    primaryContainer = DarkTealContainer,
    onPrimaryContainer = DarkTealOnContainer,
    secondary = DarkEmeraldSecondary,
    onSecondary = DarkEmeraldOnSecondary,
    secondaryContainer = DarkEmeraldContainer,
    onSecondaryContainer = DarkEmeraldOnContainer,
    tertiary = DarkMarigoldTertiary,
    onTertiary = DarkMarigoldOnTertiary,
    tertiaryContainer = DarkMarigoldContainer,
    onTertiaryContainer = DarkMarigoldOnContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkEspressoBackground,
    onBackground = DarkCreamOnSurface,
    surface = DarkEspressoSurface,
    onSurface = DarkCreamOnSurface,
    surfaceVariant = DarkEspressoSurfaceLow,
    onSurfaceVariant = DarkCreamOnSurfaceVariant,
    surfaceContainerLowest = DarkEspressoSurfaceLowest,
    surfaceContainerLow = DarkEspressoSurfaceLow,
    surfaceContainer = DarkEspressoSurfaceContainer,
    surfaceContainerHigh = DarkEspressoSurfaceHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val DairyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DoodhHisabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        DairyExtendedColors(
            eveningAccent = EveningIndigoDark,
            eveningContainer = EveningIndigoContainerDark,
            morningAccent = DarkMarigoldTertiary,
            morningContainer = DarkMarigoldContainer,
            deliveredAccent = DarkEmeraldSecondary,
            deliveredContainer = DarkEmeraldContainer,
            absentAccent = DarkError,
            absentContainer = DarkErrorContainer
        )
    } else {
        DairyExtendedColors(
            eveningAccent = EveningIndigoLight,
            eveningContainer = EveningIndigoContainerLight,
            morningAccent = MarigoldTertiary,
            morningContainer = MarigoldContainer,
            deliveredAccent = FreshEmeraldSecondary,
            deliveredContainer = FreshEmeraldContainer,
            absentAccent = TerracottaError,
            absentContainer = TerracottaErrorContainer
        )
    }

    CompositionLocalProvider(LocalDairyColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = DairyShapes,
            content = content
        )
    }
}
