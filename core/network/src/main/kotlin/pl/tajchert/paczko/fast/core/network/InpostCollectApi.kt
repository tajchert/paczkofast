package pl.tajchert.paczko.fast.core.network

import pl.tajchert.paczko.fast.core.network.dto.CollectClaimRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectSessionRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectStatusRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectValidateRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectValidateResponseDto
import pl.tajchert.paczko.fast.core.network.dto.CompartmentResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Experimental integration for personal research/learning purposes only.
 * Paczkofast is unofficial and is not affiliated with, endorsed by, or
 * supported by the locker operator.
 */
interface InpostCollectApi {
    @POST("/v2/collect/validate")
    suspend fun validate(@Body body: CollectValidateRequestDto): CollectValidateResponseDto

    @POST("/v1/collect/compartment/open")
    suspend fun open(@Body body: CollectSessionRequestDto): CompartmentResponseDto

    @POST("/v1/collect/compartment/status")
    suspend fun status(@Body body: CollectStatusRequestDto): CompartmentResponseDto

    @POST("/v1/collect/compartment/closed")
    suspend fun closed(@Body body: CollectSessionRequestDto): CompartmentResponseDto

    @POST("/v1/collect/compartment/claim")
    suspend fun claim(@Body body: CollectClaimRequestDto): CompartmentResponseDto
}
