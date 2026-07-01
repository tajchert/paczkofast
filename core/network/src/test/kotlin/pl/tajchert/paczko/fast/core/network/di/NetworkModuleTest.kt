package pl.tajchert.paczko.fast.core.network.di

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Before
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
}
