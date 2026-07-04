package pl.tajchert.paczko.fast.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
import pl.tajchert.paczko.fast.core.network.auth.normalizedAuthToken
import javax.inject.Inject

class DefaultAuthRepository @Inject constructor(
    private val authApi: InpostAuthApi,
    private val authTokensDataSource: AuthTokensDataSource,
) : AuthRepository {
    override fun observeAuthSession(): Flow<AuthSession> = authTokensDataSource.authSession

    override fun observePhoneNumber(): Flow<String?> =
        authTokensDataSource.phoneNumber.map { it?.let(::formatPhoneNumber) }

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
            authTokensDataSource.savePhoneNumber(
                "$PHONE_PREFIX_SIGN${phoneNumber.prefix}${phoneNumber.value}",
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
        return response.toAuthSession(storedRefreshToken).also { session ->
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
        prefix = "$PHONE_PREFIX_SIGN$prefix",
        value = value,
    )

    private fun ConfirmSmsResponseDto.toAuthSession(): AuthSession = AuthSession(
        authToken = authToken.normalizedAuthToken(),
        refreshToken = refreshToken,
    )

    private fun RefreshTokenResponseDto.toAuthSession(storedRefreshToken: String): AuthSession = AuthSession(
        authToken = authToken.normalizedAuthToken(),
        refreshToken = refreshToken ?: storedRefreshToken,
    )

    private companion object {
        const val ANDROID_PLATFORM = "Android"
        const val PHONE_PREFIX_SIGN = "+"

        /**
         * Formats a stored "+48500100200" number for display as
         * "+48 500 100 200": a country prefix followed by the national number
         * grouped in threes.
         */
        fun formatPhoneNumber(raw: String): String {
            if (!raw.startsWith(PHONE_PREFIX_SIGN)) return raw
            val digits = raw.drop(1)
            // Polish numbers use a two-digit country code; fall back gracefully
            // for anything shorter.
            val prefixLength = if (digits.length > 9) digits.length - 9 else 2
            val prefix = digits.take(prefixLength)
            val national = digits.drop(prefixLength)
            val grouped = national.chunked(3).joinToString(" ")
            return listOf("$PHONE_PREFIX_SIGN$prefix", grouped)
                .filter { it.isNotBlank() }
                .joinToString(" ")
        }
    }
}
