package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.data.repository.AuthRepository
import pl.tajchert.paczko.fast.core.datastore.AuthTokensDataSource
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber
import javax.inject.Inject

/**
 * Offline auth: pretends SMS was sent, accepts any code, and writes fake tokens
 * to the real [AuthTokensDataSource] so routing lands on the parcel list.
 */
class DemoAuthRepository @Inject constructor(
    private val authTokensDataSource: AuthTokensDataSource,
) : AuthRepository {
    override fun observeAuthSession(): Flow<AuthSession> = authTokensDataSource.authSession

    override fun observePhoneNumber(): Flow<String?> = authTokensDataSource.phoneNumber

    override suspend fun requestSmsCode(phoneNumber: PhoneNumber) = Unit

    override suspend fun confirmSmsCode(phoneNumber: PhoneNumber, smsCode: String): AuthSession {
        authTokensDataSource.saveTokens(DEMO_AUTH_TOKEN, DEMO_REFRESH_TOKEN)
        authTokensDataSource.savePhoneNumber(phoneNumber.prefix + phoneNumber.value)
        return AuthSession(DEMO_AUTH_TOKEN, DEMO_REFRESH_TOKEN)
    }

    override suspend fun refreshToken(): AuthSession = AuthSession(DEMO_AUTH_TOKEN, DEMO_REFRESH_TOKEN)

    override suspend fun logout() = authTokensDataSource.clearTokens()

    private companion object {
        const val DEMO_AUTH_TOKEN = "demo-auth-token"
        const val DEMO_REFRESH_TOKEN = "demo-refresh-token"
    }
}
