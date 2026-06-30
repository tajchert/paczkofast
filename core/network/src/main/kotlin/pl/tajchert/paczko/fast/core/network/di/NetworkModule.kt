package pl.tajchert.paczko.fast.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

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
     * Provides OkHttp client with logging.
     *
     * ## Logging Levels
     *
     * - BASIC: Request/response lines only
     * - HEADERS: + headers
     * - BODY: + request/response bodies (use only in debug!)
     */
    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                // In production, set this based on BuildConfig.DEBUG
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Provides Retrofit instance.
     *
     * Note: This is configured but not used in the sample (we use fake).
     * In production, you would use this with a real API.
     */
    @Provides
    @Singleton
    fun providesRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com/") // Replace with real API
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
