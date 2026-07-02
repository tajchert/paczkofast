package pl.tajchert.paczko.fast.feature.settings.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pl.tajchert.paczko.fast.core.data.repository.AuthRepository
import pl.tajchert.paczko.fast.core.model.ThemeMode
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber
import pl.tajchert.paczko.fast.core.testing.repository.FakeUserPreferencesRepository
import pl.tajchert.paczko.fast.core.testing.util.MainDispatcherRule

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun setThemeModeWritesPreference() = runTest {
        val prefs = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(prefs, FakeAuthRepository())

        viewModel.setThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()

        assertEquals(ThemeMode.LIGHT, prefs.currentPreferences.themeMode)
    }

    @Test
    fun uiStateReflectsStoredThemeMode() = runTest {
        val prefs = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(prefs, FakeAuthRepository())
        prefs.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun logoutCallsRepositoryThenOnDone() = runTest {
        val auth = FakeAuthRepository()
        val viewModel = SettingsViewModel(FakeUserPreferencesRepository(), auth)
        var doneCalled = false

        viewModel.logout { doneCalled = true }
        advanceUntilIdle()

        assertEquals(1, auth.logoutCount)
        assertEquals(true, doneCalled)
    }
}

private class FakeAuthRepository(
    private val phoneNumber: String? = null,
) : AuthRepository {
    var logoutCount = 0
    override fun observeAuthSession(): Flow<AuthSession> =
        MutableStateFlow(AuthSession(authToken = "a", refreshToken = "b"))
    override fun observePhoneNumber(): Flow<String?> = MutableStateFlow(phoneNumber)
    override suspend fun requestSmsCode(phoneNumber: PhoneNumber) = Unit
    override suspend fun confirmSmsCode(phoneNumber: PhoneNumber, smsCode: String): AuthSession =
        AuthSession(authToken = "a", refreshToken = "b")
    override suspend fun refreshToken(): AuthSession =
        AuthSession(authToken = "a", refreshToken = "b")
    override suspend fun logout() { logoutCount++ }
}
