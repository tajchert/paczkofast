package pl.tajchert.paczko.fast.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Material 3 color scheme for the dark ("Black Amber") palette.
 *
 * Material components pick these up automatically; app-specific roles
 * (progress tracks, badges, QR panel, ...) live in [PaczkofastColors].
 */
private val DarkColorScheme = darkColorScheme(
    primary = Yellow,
    onPrimary = Ink,
    primaryContainer = NightCard,
    onPrimaryContainer = Sand2,
    secondary = SandMuted2,
    onSecondary = NightBg,
    secondaryContainer = NightCard,
    onSecondaryContainer = SandMuted2,
    tertiary = AlertDark,
    onTertiary = Ink,
    error = AlertDark,
    onError = Ink,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = NightBg,
    onBackground = Sand2,
    surface = NightBg,
    onSurface = Sand2,
    surfaceVariant = NightCard,
    onSurfaceVariant = SandMuted2,
    surfaceContainer = NightCard,
    surfaceContainerLow = NightCard,
    surfaceContainerHigh = NightCard,
    outline = Sand2,
    outlineVariant = Sand2,
)

/**
 * Material 3 color scheme for the light ("warm paper") palette.
 */
private val LightColorScheme = lightColorScheme(
    primary = Yellow,
    onPrimary = Ink,
    primaryContainer = CardWhite,
    onPrimaryContainer = Ink,
    secondary = InkMutedNb,
    onSecondary = Cream,
    secondaryContainer = CardWhite,
    onSecondaryContainer = InkMutedNb,
    tertiary = AlertText,
    onTertiary = Cream,
    error = AlertText,
    onError = Cream,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = CardWhite,
    onSurfaceVariant = InkMutedNb,
    surfaceContainer = CardWhite,
    surfaceContainerLow = Cream,
    surfaceContainerHigh = CardWhite,
    outline = Ink,
    outlineVariant = Ink,
)

/**
 * Main theme composable for the Paczkofast app.
 *
 * Applies the fixed brand palettes from the "Black Amber" design direction —
 * dark by default, with a matching warm-paper light mode. Dynamic (wallpaper)
 * colors are intentionally not used so the brand palette is stable.
 *
 * Besides `MaterialTheme.colorScheme`, screens can read app-specific color
 * roles via [PaczkofastTheme.colors]:
 *
 * ```kotlin
 * Text(color = PaczkofastTheme.colors.textMuted)
 * ```
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param content The composable content to be themed.
 */
@Composable
fun PaczkofastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val paczkofastColors = if (darkTheme) DarkPaczkofastColors else LightPaczkofastColors

    CompositionLocalProvider(LocalPaczkofastColors provides paczkofastColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PaczkofastTypography,
            shapes = PaczkofastShapes,
            content = content,
        )
    }
}

/**
 * Accessor object for Paczkofast-specific theme values, mirroring
 * how `MaterialTheme` exposes its values.
 */
object PaczkofastTheme {
    val colors: PaczkofastColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPaczkofastColors.current
}
