package pl.tajchert.paczko.fast.core.network.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import pl.tajchert.paczko.fast.core.network.InpostAuthApi
import pl.tajchert.paczko.fast.core.network.dto.RefreshTokenRequestDto
import javax.inject.Inject

class RefreshingAuthenticator @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val authApi: InpostAuthApi,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.responseCount >= MAX_AUTH_ATTEMPTS) return null

        val refreshToken = tokenProvider.refreshToken()
        if (refreshToken.isNullOrBlank()) return null

        return try {
            val refreshedTokens = runBlocking {
                authApi.refreshToken(
                    RefreshTokenRequestDto(
                        refreshToken = refreshToken,
                        phoneOS = ANDROID_PLATFORM,
                    ),
                )
            }
            val retainedRefreshToken = refreshedTokens.refreshToken ?: refreshToken
            val normalizedAuthToken = refreshedTokens.authToken.normalizedAuthToken()
            tokenProvider.saveTokens(
                authToken = normalizedAuthToken,
                refreshToken = retainedRefreshToken,
            )
            response.request.newBuilder()
                .header(AUTHORIZATION_HEADER, normalizedAuthToken.asBearerAuthorizationHeader())
                .build()
        } catch (exception: Exception) {
            tokenProvider.clearTokens()
            null
        }
    }

    private val Response.responseCount: Int
        get() {
            var result = 1
            var priorResponse = priorResponse
            while (priorResponse != null) {
                result++
                priorResponse = priorResponse.priorResponse
            }
            return result
        }

    private companion object {
        const val ANDROID_PLATFORM = "Android"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val MAX_AUTH_ATTEMPTS = 2
    }
}
