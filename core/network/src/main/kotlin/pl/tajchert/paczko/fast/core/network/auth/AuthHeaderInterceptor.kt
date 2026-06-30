package pl.tajchert.paczko.fast.core.network.auth

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthHeaderInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authToken = tokenProvider.authToken()
        val request = if (authToken.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header(AUTHORIZATION_HEADER, "Bearer $authToken")
                .build()
        }

        return chain.proceed(request)
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}
