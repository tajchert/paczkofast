package pl.tajchert.paczko.fast.core.data.auth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import pl.tajchert.paczko.fast.core.datastore.AuthTokensDataSource
import pl.tajchert.paczko.fast.core.network.auth.TokenProvider
import javax.inject.Inject

class DataStoreTokenProvider @Inject constructor(
    private val authTokensDataSource: AuthTokensDataSource,
) : TokenProvider {
    override fun authToken(): String? = runBlocking {
        authTokensDataSource.authSession.first().authToken.takeIf { it.isNotBlank() }
    }

    override fun refreshToken(): String? = runBlocking {
        authTokensDataSource.authSession.first().refreshToken.takeIf { it.isNotBlank() }
    }

    override fun saveTokens(authToken: String, refreshToken: String) {
        runBlocking {
            authTokensDataSource.saveTokens(
                authToken = authToken,
                refreshToken = refreshToken,
            )
        }
    }

    override fun clearTokens() {
        runBlocking {
            authTokensDataSource.clearTokens()
        }
    }
}
