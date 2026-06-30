package pl.tajchert.paczko.fast.core.common.network

import kotlinx.coroutines.flow.Flow

/**
 * Interface for monitoring network connectivity.
 *
 * ## Why an Interface?
 *
 * Using an interface allows us to:
 * 1. Provide different implementations (real vs fake for testing)
 * 2. Decouple consumers from Android framework classes
 * 3. Make it easier to test components that depend on network state
 *
 * ## Implementation Note
 *
 * The actual implementation uses Android's ConnectivityManager and is
 * provided in the app module. This interface lives in core:common
 * so any module can depend on it without pulling in Android dependencies.
 */
interface NetworkMonitor {

    /**
     * Flow that emits the current network connectivity state.
     *
     * Emits `true` when network is available, `false` otherwise.
     * The flow is cold and will start monitoring when collected.
     */
    val isOnline: Flow<Boolean>
}
