package pl.tajchert.paczko.fast.core.network.di

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Before
import pl.tajchert.paczko.fast.core.network.InpostAuthApi
import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsRequestDto
import pl.tajchert.paczko.fast.core.network.dto.PhoneNumberDto
import pl.tajchert.paczko.fast.core.network.dto.RefreshTokenRequestDto
import pl.tajchert.paczko.fast.core.network.dto.SendSmsCodeRequestDto
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkModuleTest {
    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun unauthenticatedClientSendsJsonAcceptHeader() {
        server.enqueue(MockResponse.Builder().code(204).build())
        val request = Request.Builder()
            .url(server.url("/global/v1/account"))
            .post(
                """{"phoneNumber":{"prefix":"48","value":"600123456"}}"""
                    .toRequestBody("application/json".toMediaType()),
            )
            .build()

        NetworkModule.providesUnauthenticatedOkHttpClient()
            .newCall(request)
            .execute()
            .close()

        assertEquals("application/json", server.takeRequest().headers["Accept"])
    }

    @Test
    fun authApiCallsHostRootSmsPaths() = runTest {
        server.enqueue(jsonResponse("""{"expirationTime":null}"""))
        server.enqueue(jsonResponse("""{"authToken":"auth-token","refreshToken":"refresh-token"}"""))
        server.enqueue(jsonResponse("""{"authToken":"auth-token","refreshToken":"refresh-token"}"""))
        val api = authApi()

        api.requestSmsCode(
            SendSmsCodeRequestDto(
                phoneNumber = PhoneNumberDto(prefix = "48", value = "000000000"),
            ),
        )
        api.confirmSmsCode(
            ConfirmSmsRequestDto(
                phoneNumber = PhoneNumberDto(prefix = "48", value = "000000000"),
                smsCode = "0000",
                devicePlatform = "android",
            ),
        )
        api.refreshToken(
            RefreshTokenRequestDto(
                refreshToken = "refresh-token",
                phoneOS = "android",
            ),
        )

        assertEquals("/v1/account", server.takeRequest().url.encodedPath)
        assertEquals("/v1/account/verification", server.takeRequest().url.encodedPath)
        assertEquals("/v1/authenticate", server.takeRequest().url.encodedPath)
    }

    private fun authApi(): InpostAuthApi = Retrofit.Builder()
        .baseUrl(server.url("/global/"))
        .client(NetworkModule.providesUnauthenticatedOkHttpClient())
        .addConverterFactory(
            NetworkModule.providesJson().asConverterFactory("application/json".toMediaType()),
        )
        .build()
        .create(InpostAuthApi::class.java)

    private fun jsonResponse(body: String): MockResponse = MockResponse.Builder()
        .code(200)
        .addHeader("Content-Type", "application/json")
        .body(body)
        .build()
}
