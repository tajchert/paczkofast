package pl.tajchert.paczko.fast

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pl.tajchert.paczko.fast.core.data.repository.AuthRepository
import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository
import pl.tajchert.paczko.fast.core.domain.ObserveAuthSessionUseCase
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.ThemeMode
import pl.tajchert.paczko.fast.core.model.UserPreferences
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber
import pl.tajchert.paczko.fast.core.testing.util.MainDispatcherRule
import pl.tajchert.paczko.fast.feature.auth.api.AuthRoute
import pl.tajchert.paczko.fast.feature.auth.api.OnboardingRoute
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelListRoute

class MainActivityViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun storedAuthSessionStartsAtParcelListWhenOnboardingSeen() = runTest {
        val viewModel = MainActivityViewModel(
            userPreferencesRepository = FakeUserPreferencesRepository(hasSeenOnboarding = true),
            observeAuthSession = ObserveAuthSessionUseCase(
                FakeAuthRepository(AuthSession(authToken = "access", refreshToken = "refresh")),
            ),
        )

        assertEquals(
            MainActivityUiState.Success(
                preferences = UserPreferences(hasSeenOnboarding = true),
                initialRoute = ParcelListRoute,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun missingAuthSessionStartsAtAuthWhenOnboardingSeen() = runTest {
        val viewModel = MainActivityViewModel(
            userPreferencesRepository = FakeUserPreferencesRepository(hasSeenOnboarding = true),
            observeAuthSession = ObserveAuthSessionUseCase(
                FakeAuthRepository(AuthSession(authToken = "", refreshToken = "")),
            ),
        )

        assertEquals(
            MainActivityUiState.Success(
                preferences = UserPreferences(hasSeenOnboarding = true),
                initialRoute = AuthRoute,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun unseenOnboardingStartsAtOnboardingEvenWhenAuthenticated() = runTest {
        val viewModel = MainActivityViewModel(
            userPreferencesRepository = FakeUserPreferencesRepository(hasSeenOnboarding = false),
            observeAuthSession = ObserveAuthSessionUseCase(
                FakeAuthRepository(AuthSession(authToken = "access", refreshToken = "refresh")),
            ),
        )

        assertEquals(
            MainActivityUiState.Success(
                preferences = UserPreferences(hasSeenOnboarding = false),
                initialRoute = OnboardingRoute,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun unseenOnboardingStartsAtOnboardingWhenNotAuthenticated() = runTest {
        val viewModel = MainActivityViewModel(
            userPreferencesRepository = FakeUserPreferencesRepository(hasSeenOnboarding = false),
            observeAuthSession = ObserveAuthSessionUseCase(
                FakeAuthRepository(AuthSession(authToken = "", refreshToken = "")),
            ),
        )

        assertEquals(
            MainActivityUiState.Success(
                preferences = UserPreferences(hasSeenOnboarding = false),
                initialRoute = OnboardingRoute,
            ),
            viewModel.uiState.value,
        )
    }
}

private class FakeUserPreferencesRepository(
    hasSeenOnboarding: Boolean = false,
) : UserPreferencesRepository {
    override val userPreferences: Flow<UserPreferences> =
        MutableStateFlow(UserPreferences(hasSeenOnboarding = hasSeenOnboarding))

    override suspend fun setThemeMode(themeMode: ThemeMode) = Unit

    override suspend fun setHasSeenOnboarding(seen: Boolean) = Unit

    override suspend fun setLockerOpenMode(mode: LockerOpenMode) = Unit
}

private class FakeAuthRepository(
    private val session: AuthSession,
) : AuthRepository {
    override fun observeAuthSession(): Flow<AuthSession> = MutableStateFlow(session)

    override fun observePhoneNumber(): Flow<String?> = MutableStateFlow(null)

    override suspend fun requestSmsCode(phoneNumber: PhoneNumber) = Unit

    override suspend fun confirmSmsCode(phoneNumber: PhoneNumber, smsCode: String): AuthSession = session

    override suspend fun refreshToken(): AuthSession = session

    override suspend fun logout() = Unit
}
