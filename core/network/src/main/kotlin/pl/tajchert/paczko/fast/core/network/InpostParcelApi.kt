package pl.tajchert.paczko.fast.core.network

import pl.tajchert.paczko.fast.core.network.dto.ParcelDto
import pl.tajchert.paczko.fast.core.network.dto.TrackedParcelsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface InpostParcelApi {
    @GET("/v4/parcels/tracked")
    suspend fun getTrackedParcels(): TrackedParcelsResponseDto

    @GET("/v4/parcels/tracked/{shipmentNumber}")
    suspend fun getTrackedParcel(@Path("shipmentNumber") shipmentNumber: String): ParcelDto
}
