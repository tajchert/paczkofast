package pl.tajchert.paczko.fast.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.tajchert.paczko.fast.core.network.InpostAuthApi
import pl.tajchert.paczko.fast.core.network.InpostCollectApi
import pl.tajchert.paczko.fast.core.network.InpostParcelApi
import pl.tajchert.paczko.fast.core.network.auth.AuthHeaderInterceptor
import pl.tajchert.paczko.fast.core.network.auth.RefreshingAuthenticator
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

private const val INPOST_BASE_URL = "https://api-inmobile-pl.easypack24.net/global/"

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedNetwork

/**
 * Hilt module for network dependencies.
 *
 * ## Architecture
 *
 * This module provides:
 * 1. JSON serializer configuration
 * 2. OkHttp client with logging
 * 3. Retrofit instance (for production use)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides configured JSON serializer.
     *
     * ## Configuration
     *
     * - `ignoreUnknownKeys`: Allows API to add fields without breaking the app
     * - `coerceInputValues`: Handles null for non-nullable fields with defaults
     * - `isLenient`: Accepts non-standard JSON (useful for debugging)
     */
    @Provides
    @Singleton
    fun providesJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /**
     * Provides OkHttp client with logging and no auth dependencies.
     *
     * InpostAuthApi uses this path so RefreshingAuthenticator can refresh
     * tokens without depending on the authenticated client it is attached to.
     */
    @Provides
    @Singleton
    @UnauthenticatedNetwork
    fun providesUnauthenticatedOkHttpClient(): OkHttpClient = baseOkHttpBuilder()
        .addInterceptor(loggingInterceptor())
        .build()

    /**
     * Provides OkHttp client that attaches auth headers and refreshes once
     * after a 401 response.
     */
    @Provides
    @Singleton
    @AuthenticatedNetwork
    fun providesAuthenticatedOkHttpClient(
        authHeaderInterceptor: AuthHeaderInterceptor,
        refreshingAuthenticator: RefreshingAuthenticator,
    ): OkHttpClient = baseOkHttpBuilder()
        .addInterceptor(authHeaderInterceptor)
        .addInterceptor(loggingInterceptor())
        .authenticator(refreshingAuthenticator)
        .build()

    @Provides
    @Singleton
    @UnauthenticatedNetwork
    fun providesUnauthenticatedRetrofit(
        @UnauthenticatedNetwork okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = retrofit(okHttpClient = okHttpClient, json = json)

    @Provides
    @Singleton
    @AuthenticatedNetwork
    fun providesAuthenticatedRetrofit(
        @AuthenticatedNetwork okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = retrofit(okHttpClient = okHttpClient, json = json)

    private fun retrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(INPOST_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun providesInpostAuthApi(
        @UnauthenticatedNetwork retrofit: Retrofit,
    ): InpostAuthApi =
        retrofit.create(InpostAuthApi::class.java)

    @Provides
    @Singleton
    fun providesInpostParcelApi(
        @AuthenticatedNetwork retrofit: Retrofit,
    ): InpostParcelApi =
        retrofit.create(InpostParcelApi::class.java)

    @Provides
    @Singleton
    fun providesInpostCollectApi(
        @AuthenticatedNetwork retrofit: Retrofit,
    ): InpostCollectApi =
        retrofit.create(InpostCollectApi::class.java)

    private fun baseOkHttpBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()
        .addInterceptor(commonHeadersInterceptor())
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

    private fun commonHeadersInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header(ACCEPT_HEADER, APPLICATION_JSON)
            .build()
        chain.proceed(request)
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        redactHeader("Authorization")
        // In production, set this based on BuildConfig.DEBUG
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // Fail fast on slow/stalled connections so the UI can surface an error
    // (and fall back to cached data) instead of hanging.
    private const val TIMEOUT_SECONDS = 5L

    private const val ACCEPT_HEADER = "Accept"
    private const val APPLICATION_JSON = "application/json"
}
