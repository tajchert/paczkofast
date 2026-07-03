package pl.tajchert.paczko.fast.feature.parcels.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ParcelListRoute : NavKey

@Serializable
data class ParcelDetailRoute(val shipmentNumber: String) : NavKey

@Serializable
data class ParcelCollectRoute(val shipmentNumber: String) : NavKey

/**
 * Detail screen for a multi-package box (7a). [shipmentNumber] is any member of
 * the compartment (the representative); the screen resolves its siblings.
 */
@Serializable
data class MultiPackageDetailRoute(val shipmentNumber: String) : NavKey
