package pl.tajchert.paczko.fast.core.network.auth

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Before
import pl.tajchert.paczko.fast.core.network.InpostAuthApi
import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsRequestDto
import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsResponseDto
import pl.tajchert.paczko.fast.core.network.dto.RefreshTokenRequestDto
import pl.tajchert.paczko.fast.core.network.dto.SendSmsCodeRequestDto
import pl.tajchert.paczko.fast.core.network.dto.SendSmsCodeResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshingAuthenticatorTest {
    private lateinit var server: MockWebServer
    private lateinit var tokenProvider: FakeTokenProvider
    private lateinit var authApi: FakeInpostAuthApi

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        tokenProvider = FakeTokenProvider(
            authToken = "expired-auth-token",
            refreshToken = "stored-refresh-token",
        )
        authApi = FakeInpostAuthApi()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun refreshesTokensAndRetriesUnauthorizedRequest() {
        authApi.refreshTokenResponse = ConfirmSmsResponseDto(
            authToken = "refreshed-auth-token",
            refreshToken = "refreshed-refresh-token",
        )
        server.enqueue(MockResponse.Builder().code(401).build())
        server.enqueue(MockResponse.Builder().code(200).body("ok").build())
        val client = client()

        val response = client.newCall(
            Request.Builder()
                .url(server.url("/protected"))
                .build(),
        ).execute()

        response.use {
            assertEquals(200, it.code)
            assertEquals("ok", it.body.string())
        }
        assertEquals(
            RefreshTokenRequestDto(
                refreshToken = "stored-refresh-token",
                phoneOS = "android",
            ),
            authApi.refreshTokenBody,
        )
        assertEquals("refreshed-auth-token", tokenProvider.authToken())
        assertEquals("refreshed-refresh-token", tokenProvider.refreshToken())
        assertEquals("Bearer expired-auth-token", server.takeRequest().headers["Authorization"])
        assertEquals("Bearer refreshed-auth-token", server.takeRequest().headers["Authorization"])
    }

    @Test
    fun retriesUnauthorizedRequestOnlyOnce() {
        server.enqueue(MockResponse.Builder().code(401).build())
        server.enqueue(MockResponse.Builder().code(401).build())
        val client = client()

        val response = client.newCall(
            Request.Builder()
                .url(server.url("/protected"))
                .build(),
        ).execute()

        response.use {
            assertEquals(401, it.code)
        }
        assertEquals(1, authApi.refreshTokenCalls)
        assertEquals(2, server.requestCount)
    }

    private fun client(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthHeaderInterceptor(tokenProvider))
        .authenticator(
            RefreshingAuthenticator(
                tokenProvider = tokenProvider,
                authApi = authApi,
            ),
        )
        .build()
}

private class FakeTokenProvider(
    private var authToken: String?,
    private var refreshToken: String?,
) : TokenProvider {
    override fun authToken(): String? = authToken

    override fun refreshToken(): String? = refreshToken

    override fun saveTokens(authToken: String, refreshToken: String) {
        this.authToken = authToken
        this.refreshToken = refreshToken
    }

    override fun clearTokens() {
        authToken = null
        refreshToken = null
    }
}

private class FakeInpostAuthApi : InpostAuthApi {
    var refreshTokenBody: RefreshTokenRequestDto? = null
    var refreshTokenCalls = 0
    var refreshTokenResponse = ConfirmSmsResponseDto(
        authToken = "refreshed-auth-token",
        refreshToken = "refreshed-refresh-token",
    )

    override suspend fun requestSmsCode(body: SendSmsCodeRequestDto): SendSmsCodeResponseDto {
        error("Not used in this test")
    }

    override suspend fun confirmSmsCode(body: ConfirmSmsRequestDto): ConfirmSmsResponseDto {
        error("Not used in this test")
    }

    override suspend fun refreshToken(body: RefreshTokenRequestDto): ConfirmSmsResponseDto {
        refreshTokenCalls += 1
        refreshTokenBody = body
        return refreshTokenResponse
    }
}
