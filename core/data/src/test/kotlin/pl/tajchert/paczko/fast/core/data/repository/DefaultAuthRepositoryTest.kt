package pl.tajchert.paczko.fast.core.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import pl.tajchert.paczko.fast.core.datastore.AuthTokensDataSource
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber
import pl.tajchert.paczko.fast.core.network.InpostAuthApi
import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsRequestDto
import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsResponseDto
import pl.tajchert.paczko.fast.core.network.dto.PhoneNumberDto
import pl.tajchert.paczko.fast.core.network.dto.RefreshTokenRequestDto
import pl.tajchert.paczko.fast.core.network.dto.RefreshTokenResponseDto
import pl.tajchert.paczko.fast.core.network.dto.SendSmsCodeRequestDto
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAuthRepositoryTest {

    private val authApi = FakeInpostAuthApi()
    private val tokensDataSource = FakeAuthTokensDataSource()
    private val repository: AuthRepository = DefaultAuthRepository(
        authApi = authApi,
        authTokensDataSource = tokensDataSource,
    )

    @Test
    fun requestSmsCodeSendsPhoneNumberToApi() = runTest {
        repository.requestSmsCode(PhoneNumber(prefix = "48", value = "600123456"))

        assertEquals(
            SendSmsCodeRequestDto(
                phoneNumber = PhoneNumberDto(prefix = "+48", value = "600123456"),
            ),
            authApi.requestSmsCodeBody,
        )
    }

    @Test
    fun confirmSmsCodeSendsAndroidPlatformAndStoresReturnedTokens() = runTest {
        authApi.confirmSmsCodeResponse = ConfirmSmsResponseDto(
            authToken = "Bearer confirmed-auth-token",
            refreshToken = "confirmed-refresh-token",
        )

        repository.confirmSmsCode(
            phoneNumber = PhoneNumber(prefix = "48", value = "600123456"),
            smsCode = "123456",
        )

        assertEquals(
            ConfirmSmsRequestDto(
                phoneNumber = PhoneNumberDto(prefix = "+48", value = "600123456"),
                smsCode = "123456",
                devicePlatform = "Android",
            ),
            authApi.confirmSmsCodeBody,
        )
        assertEquals(
            AuthSession(
                authToken = "confirmed-auth-token",
                refreshToken = "confirmed-refresh-token",
            ),
            tokensDataSource.currentSession,
        )
        assertEquals("+48600123456", tokensDataSource.currentPhoneNumber)
    }

    @Test
    fun observePhoneNumberFormatsStoredNumber() = runTest {
        tokensDataSource.savePhoneNumber("+48601480312")

        repository.observePhoneNumber().test {
            assertEquals("+48 601 480 312", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeAuthSessionEmitsStoredSession() = runTest {
        tokensDataSource.saveTokens(
            authToken = "stored-auth-token",
            refreshToken = "stored-refresh-token",
        )

        repository.observeAuthSession().test {
            assertEquals(
                AuthSession(
                    authToken = "stored-auth-token",
                    refreshToken = "stored-refresh-token",
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshTokenUsesStoredRefreshTokenAndSavesReturnedTokens() = runTest {
        tokensDataSource.saveTokens(
            authToken = "old-auth-token",
            refreshToken = "old-refresh-token",
        )
        authApi.refreshTokenResponse = RefreshTokenResponseDto(
            authToken = "Bearer refreshed-auth-token",
        )

        repository.refreshToken()

        assertEquals(
            RefreshTokenRequestDto(
                refreshToken = "old-refresh-token",
                phoneOS = "Android",
            ),
            authApi.refreshTokenBody,
        )
        assertEquals(
            AuthSession(
                authToken = "refreshed-auth-token",
                refreshToken = "old-refresh-token",
            ),
            tokensDataSource.currentSession,
        )
    }

    @Test
    fun logoutClearsStoredTokens() = runTest {
        tokensDataSource.saveTokens(
            authToken = "stored-auth-token",
            refreshToken = "stored-refresh-token",
        )

        repository.logout()

        assertEquals(AuthSession(authToken = "", refreshToken = ""), tokensDataSource.currentSession)
    }
}

private class FakeAuthTokensDataSource : AuthTokensDataSource {
    private val authSessionFlow = MutableStateFlow(AuthSession(authToken = "", refreshToken = ""))

    override val authSession: Flow<AuthSession> = authSessionFlow

    private val phoneNumberFlow = MutableStateFlow<String?>(null)
    override val phoneNumber: Flow<String?> = phoneNumberFlow

    val currentSession: AuthSession
        get() = authSessionFlow.value

    val currentPhoneNumber: String?
        get() = phoneNumberFlow.value

    override suspend fun saveTokens(authToken: String, refreshToken: String) {
        authSessionFlow.value = AuthSession(authToken = authToken, refreshToken = refreshToken)
    }

    override suspend fun savePhoneNumber(phoneNumber: String) {
        phoneNumberFlow.value = phoneNumber
    }

    override suspend fun clearTokens() {
        authSessionFlow.value = AuthSession(authToken = "", refreshToken = "")
        phoneNumberFlow.value = null
    }
}

private class FakeInpostAuthApi : InpostAuthApi {
    var requestSmsCodeBody: SendSmsCodeRequestDto? = null
    var confirmSmsCodeBody: ConfirmSmsRequestDto? = null
    var refreshTokenBody: RefreshTokenRequestDto? = null

    var confirmSmsCodeResponse = ConfirmSmsResponseDto(
        authToken = "auth-token",
        refreshToken = "refresh-token",
    )
    var refreshTokenResponse = RefreshTokenResponseDto(
        authToken = "refreshed-auth-token",
    )

    override suspend fun requestSmsCode(body: SendSmsCodeRequestDto) {
        requestSmsCodeBody = body
    }

    override suspend fun confirmSmsCode(body: ConfirmSmsRequestDto): ConfirmSmsResponseDto {
        confirmSmsCodeBody = body
        return confirmSmsCodeResponse
    }

    override suspend fun refreshToken(body: RefreshTokenRequestDto): RefreshTokenResponseDto {
        refreshTokenBody = body
        return refreshTokenResponse
    }
}
