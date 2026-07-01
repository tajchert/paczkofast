package pl.tajchert.paczko.fast.feature.parcels.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ParcelListRoute : NavKey

@Serializable
data class ParcelDetailRoute(val shipmentNumber: String) : NavKey

@Serializable
data class ParcelCollectRoute(val shipmentNumber: String) : NavKey
