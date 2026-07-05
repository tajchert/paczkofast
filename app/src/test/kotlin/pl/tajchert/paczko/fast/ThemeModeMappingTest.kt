package pl.tajchert.paczko.fast

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.ThemeMode
import pl.tajchert.paczko.fast.core.model.UserPreferences
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelListRoute

class ThemeModeMappingTest {

    private fun success(mode: ThemeMode) = MainActivityUiState.Success(
        preferences = UserPreferences(themeMode = mode),
        initialRoute = ParcelListRoute,
    )

    // Dark mode is temporarily disabled: every mode resolves to light regardless
    // of the system setting. When dark mode is re-enabled, restore the
    // per-mode expectations here.

    @Test
    fun systemModeIsNeverDarkWhileDisabled() {
        assertEquals(false, success(ThemeMode.SYSTEM).shouldUseDarkTheme(isSystemInDarkTheme = true))
        assertEquals(false, success(ThemeMode.SYSTEM).shouldUseDarkTheme(isSystemInDarkTheme = false))
    }

    @Test
    fun lightModeIsNeverDark() {
        assertEquals(false, success(ThemeMode.LIGHT).shouldUseDarkTheme(isSystemInDarkTheme = true))
    }

    @Test
    fun darkModePreferenceStillResolvesToLightWhileDisabled() {
        assertEquals(false, success(ThemeMode.DARK).shouldUseDarkTheme(isSystemInDarkTheme = true))
    }

    @Test
    fun loadingIsNeverDarkWhileDisabled() {
        assertEquals(false, MainActivityUiState.Loading.shouldUseDarkTheme(isSystemInDarkTheme = true))
    }
}
