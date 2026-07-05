package pl.tajchert.paczko.fast.core.network

import pl.tajchert.paczko.fast.core.network.dto.ParcelDto
import pl.tajchert.paczko.fast.core.network.dto.TrackedParcelsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Experimental integration for personal research/learning purposes only.
 * Paczkofast is unofficial and is not affiliated with, endorsed by, or
 * supported by the locker operator.
 */
interface InpostParcelApi {
    @GET("/v4/parcels/tracked")
    suspend fun getTrackedParcels(): TrackedParcelsResponseDto

    @GET("/v4/parcels/tracked/{shipmentNumber}")
    suspend fun getTrackedParcel(@Path("shipmentNumber") shipmentNumber: String): ParcelDto
}
