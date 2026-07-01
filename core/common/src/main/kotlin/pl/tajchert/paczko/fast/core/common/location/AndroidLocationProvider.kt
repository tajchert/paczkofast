package pl.tajchert.paczko.fast.core.common.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

class AndroidLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationProvider {
    override suspend fun currentLocation(): GeoPoint {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) error("Location permission is required")

        val manager = context.getSystemService(LocationManager::class.java)
            ?: error("Location service is unavailable")
        val provider = if (hasFine) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        return currentLocationFromProvider(manager, provider)
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocationFromProvider(
        manager: LocationManager,
        provider: String,
    ): GeoPoint =
        suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation {
                cancellationSignal.cancel()
            }

            try {
                manager.getCurrentLocation(provider, cancellationSignal, context.mainExecutor) { location ->
                    if (continuation.isActive) {
                        if (location == null) {
                            continuation.resumeWithException(
                                IllegalStateException("Current location unavailable"),
                            )
                        } else {
                            continuation.resume(
                                GeoPoint(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    accuracy = location.accuracy.toDouble(),
                                ),
                            )
                        }
                    }
                }
            } catch (exception: SecurityException) {
                cancellationSignal.cancel()
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        IllegalStateException("Location permission is required", exception),
                    )
                }
            }
        }
}
