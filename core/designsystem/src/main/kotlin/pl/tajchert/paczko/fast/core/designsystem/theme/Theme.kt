package pl.tajchert.paczko.fast.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Material 3 color scheme for the dark ("Black Amber") palette.
 *
 * Material components pick these up automatically; app-specific roles
 * (progress tracks, badges, QR panel, ...) live in [PaczkofastColors].
 */
private val DarkColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = OnAmber,
    primaryContainer = NightBadge,
    onPrimaryContainer = Sand,
    secondary = SandSoft,
    onSecondary = Night0,
    secondaryContainer = NightBadge,
    onSecondaryContainer = SandSoft,
    tertiary = UrgentDark,
    onTertiary = OnAmber,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Night0,
    onBackground = Sand,
    surface = Night0,
    onSurface = Sand,
    surfaceVariant = Night1,
    onSurfaceVariant = SandMuted,
    surfaceContainer = Night1,
    surfaceContainerLow = Night2,
    surfaceContainerHigh = NightBadge,
    outline = Color(0xFFF5D78C).copy(alpha = 0.12f),
    outlineVariant = Color(0xFFF5D78C).copy(alpha = 0.07f),
)

/**
 * Material 3 color scheme for the light ("warm paper") palette.
 */
private val LightColorScheme = lightColorScheme(
    primary = Amber,
    onPrimary = OnAmber,
    primaryContainer = PaperBadge,
    onPrimaryContainer = InkStrong,
    secondary = InkSoft,
    onSecondary = Paper0,
    secondaryContainer = PaperBadge,
    onSecondaryContainer = InkSoft,
    tertiary = UrgentLight,
    onTertiary = Paper0,
    error = Red40,
    onError = Paper0,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Paper0,
    onBackground = InkStrong,
    surface = Paper0,
    onSurface = InkStrong,
    surfaceVariant = Paper1,
    onSurfaceVariant = InkMuted,
    surfaceContainer = Paper1,
    surfaceContainerLow = Paper2,
    surfaceContainerHigh = Paper1,
    outline = PaperBorder,
    outlineVariant = PaperBorderSubtle,
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
