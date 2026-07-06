package pl.tajchert.paczko.fast.core.common.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

class AndroidLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationProvider {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    private fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): GeoPoint {
        if (!hasPermission()) error("Location permission is required")
        return suspendCancellableCoroutine { cont ->
            val cts = com.google.android.gms.tasks.CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (!cont.isActive) return@addOnSuccessListener
                    if (location == null) {
                        cont.resumeWithException(IllegalStateException("Current location unavailable"))
                    } else {
                        cont.resume(location.toGeoPoint())
                    }
                }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    @SuppressLint("MissingPermission")
    override fun locationUpdates(): Flow<GeoPoint> = callbackFlow {
        if (!hasPermission()) { close(); return@callbackFlow }

        client.lastLocation.addOnSuccessListener { last ->
            if (last != null) trySend(last.toGeoPoint())
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
            .setMinUpdateIntervalMillis(500L)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it.toGeoPoint()) }
            }
        }
        client.requestLocationUpdates(request, callback, context.mainLooper)
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private fun android.location.Location.toGeoPoint() = GeoPoint(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy.toDouble(),
    )
}
