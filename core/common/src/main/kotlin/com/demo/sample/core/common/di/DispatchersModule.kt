package com.demo.sample.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

/**
 * Qualifier annotation for injecting specific coroutine dispatchers.
 *
 * ## Why Custom Dispatcher Qualifiers?
 *
 * 1. **Testability**: In tests, we can swap [Dispatchers.IO] with [TestDispatcher]
 *    to make coroutines run synchronously and predictably.
 *
 * 2. **Explicitness**: Makes it clear which dispatcher is being used where.
 *    `@Dispatcher(SampleDispatchers.IO)` is more explicit than just using `Dispatchers.IO`.
 *
 * 3. **Flexibility**: If we ever need to customize dispatcher behavior (e.g., limiting
 *    parallelism), we can do it in one place.
 *
 * ## Usage
 *
 * ```kotlin
 * class MyRepository @Inject constructor(
 *     @Dispatcher(SampleDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
 * ) {
 *     suspend fun loadData() = withContext(ioDispatcher) {
 *         // This runs on IO dispatcher
 *     }
 * }
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val sampleDispatcher: SampleDispatchers)

/**
 * Enum of available dispatchers for dependency injection.
 */
enum class SampleDispatchers {
    /**
     * For CPU-intensive work like sorting, parsing, or calculations.
     * Maps to [Dispatchers.Default].
     */
    Default,

    /**
     * For I/O operations like network requests, database queries, or file operations.
     * Maps to [Dispatchers.IO].
     */
    IO,

    /**
     * For UI updates. Should be used sparingly - most Compose code doesn't need this.
     * Maps to [Dispatchers.Main].
     */
    Main,
}

/**
 * Hilt module providing coroutine dispatchers.
 *
 * ## Why a Hilt Module?
 *
 * Providing dispatchers through Hilt allows us to:
 * 1. Inject test dispatchers in tests
 * 2. Centralize dispatcher configuration
 * 3. Follow the dependency injection principle
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Dispatcher(SampleDispatchers.IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(SampleDispatchers.Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Dispatcher(SampleDispatchers.Main)
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
