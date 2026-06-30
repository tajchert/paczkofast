package pl.tajchert.paczko.fast.core.data.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.tajchert.paczko.fast.core.datastore.AuthTokensDataSource
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DataStoreTokenProviderTest {
    private val authTokensDataSource = FakeAuthTokensDataSource()
    private val tokenProvider = DataStoreTokenProvider(authTokensDataSource)

    @Test
    fun returnsStoredTokens() {
        tokenProvider.saveTokens(
            authToken = "stored-auth-token",
            refreshToken = "stored-refresh-token",
        )

        assertEquals("stored-auth-token", tokenProvider.authToken())
        assertEquals("stored-refresh-token", tokenProvider.refreshToken())
    }

    @Test
    fun returnsNullForBlankTokens() {
        assertNull(tokenProvider.authToken())
        assertNull(tokenProvider.refreshToken())
    }

    @Test
    fun clearTokensClearsStoredSession() {
        tokenProvider.saveTokens(
            authToken = "stored-auth-token",
            refreshToken = "stored-refresh-token",
        )

        tokenProvider.clearTokens()

        assertNull(tokenProvider.authToken())
        assertNull(tokenProvider.refreshToken())
    }
}

private class FakeAuthTokensDataSource : AuthTokensDataSource {
    private val authSessionFlow = MutableStateFlow(AuthSession(authToken = "", refreshToken = ""))

    override val authSession: Flow<AuthSession> = authSessionFlow

    override suspend fun saveTokens(authToken: String, refreshToken: String) {
        authSessionFlow.value = AuthSession(authToken = authToken, refreshToken = refreshToken)
    }

    override suspend fun clearTokens() {
        authSessionFlow.value = AuthSession(authToken = "", refreshToken = "")
    }
}
