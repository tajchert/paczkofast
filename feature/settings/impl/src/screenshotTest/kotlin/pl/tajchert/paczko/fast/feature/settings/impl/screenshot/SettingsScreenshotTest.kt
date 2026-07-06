package pl.tajchert.paczko.fast.feature.settings.impl.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.ThemeMode
import pl.tajchert.paczko.fast.feature.settings.impl.SettingsContent

/**
 * Golden screenshot of the settings screen in both brand palettes (via
 * [PaczkofastPreviews]). Covers the theme + unlock segmented controls (whose
 * selected pill must show ink text/border on the yellow fill in dark mode too),
 * the account row and the About row's yellow logo tile.
 *
 * Fully static fixture (fixed version string, obviously-fake phone number), so
 * it is time-independent and public-safe per the repo rules.
 */
@PreviewTest
@PaczkofastPreviews
@Composable
private fun SettingsScreenshot() {
    PaczkofastTheme {
        SettingsContent(
            themeMode = ThemeMode.DARK,
            lockerOpenMode = LockerOpenMode.HOLD,
            phoneNumber = "+48 000 000 000",
            appVersion = "1.0.0 (1)",
            onThemeSelected = {},
            onOpenModeSelected = {},
            onLogout = {},
            onOpenParcels = {},
            onOpenHistory = {},
        )
    }
}
