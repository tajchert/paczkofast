package pl.tajchert.paczko.fast.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import pl.tajchert.paczko.fast.core.datastore.AuthTokensDataSource
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber
import pl.tajchert.paczko.fast.core.network.InpostAuthApi
import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsRequestDto
import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsResponseDto
import pl.tajchert.paczko.fast.core.network.dto.PhoneNumberDto
import pl.tajchert.paczko.fast.core.network.dto.RefreshTokenRequestDto
import pl.tajchert.paczko.fast.core.network.dto.SendSmsCodeRequestDto
import javax.inject.Inject

class DefaultAuthRepository @Inject constructor(
    private val authApi: InpostAuthApi,
    private val authTokensDataSource: AuthTokensDataSource,
) : AuthRepository {
    override fun observeAuthSession(): Flow<AuthSession> = authTokensDataSource.authSession

    override suspend fun requestSmsCode(phoneNumber: PhoneNumber) {
        authApi.requestSmsCode(
            SendSmsCodeRequestDto(phoneNumber = phoneNumber.toDto()),
        )
    }

    override suspend fun confirmSmsCode(phoneNumber: PhoneNumber, smsCode: String): AuthSession {
        val response = authApi.confirmSmsCode(
            ConfirmSmsRequestDto(
                phoneNumber = phoneNumber.toDto(),
                smsCode = smsCode,
                devicePlatform = ANDROID_PLATFORM,
            ),
        )
        return response.toAuthSession().also { session ->
            authTokensDataSource.saveTokens(
                authToken = session.authToken,
                refreshToken = session.refreshToken,
            )
        }
    }

    override suspend fun refreshToken(): AuthSession {
        val storedRefreshToken = authTokensDataSource.authSession.first().refreshToken
        require(storedRefreshToken.isNotBlank()) { "Refresh token is missing" }

        val response = authApi.refreshToken(
            RefreshTokenRequestDto(
                refreshToken = storedRefreshToken,
                phoneOS = ANDROID_PLATFORM,
            ),
        )
        return response.toAuthSession().also { session ->
            authTokensDataSource.saveTokens(
                authToken = session.authToken,
                refreshToken = session.refreshToken,
            )
        }
    }

    override suspend fun logout() {
        authTokensDataSource.clearTokens()
    }

    private fun PhoneNumber.toDto(): PhoneNumberDto = PhoneNumberDto(
        prefix = prefix,
        value = value,
    )

    private fun ConfirmSmsResponseDto.toAuthSession(): AuthSession = AuthSession(
        authToken = authToken,
        refreshToken = refreshToken,
    )

    private companion object {
        const val ANDROID_PLATFORM = "android"
    }
}
