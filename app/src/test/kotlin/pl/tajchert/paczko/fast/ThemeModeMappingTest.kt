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

    @Test
    fun systemModeFollowsSystemDark() {
        assertEquals(true, success(ThemeMode.SYSTEM).shouldUseDarkTheme(isSystemInDarkTheme = true))
        assertEquals(false, success(ThemeMode.SYSTEM).shouldUseDarkTheme(isSystemInDarkTheme = false))
    }

    @Test
    fun lightModeIsNeverDark() {
        assertEquals(false, success(ThemeMode.LIGHT).shouldUseDarkTheme(isSystemInDarkTheme = true))
    }

    @Test
    fun darkModeIsAlwaysDark() {
        assertEquals(true, success(ThemeMode.DARK).shouldUseDarkTheme(isSystemInDarkTheme = false))
    }

    @Test
    fun loadingFollowsSystemDark() {
        assertEquals(true, MainActivityUiState.Loading.shouldUseDarkTheme(isSystemInDarkTheme = true))
    }
}
