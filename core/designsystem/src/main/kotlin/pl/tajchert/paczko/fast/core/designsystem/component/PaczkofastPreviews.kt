package pl.tajchert.paczko.fast.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multipreview rendering a component in both brand palettes:
 * dark ("Black Amber", the default) and light ("warm paper").
 *
 * Combine with [PaczkofastTheme][pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme],
 * which follows the preview's night mode automatically:
 *
 * ```kotlin
 * @PaczkofastPreviews
 * @Composable
 * private fun MyComponentPreview() {
 *     PaczkofastTheme { MyComponent() }
 * }
 * ```
 */
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF161511,
)
@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = 0xFFFDFAEF,
)
annotation class PaczkofastPreviews
