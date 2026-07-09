package pl.tajchert.paczko.fast.core.data.repository

import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import pl.tajchert.paczko.fast.core.model.collect.CollectErrorCode
import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint
import pl.tajchert.paczko.fast.core.network.InpostCollectApi
import pl.tajchert.paczko.fast.core.network.dto.CollectClaimRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectSessionRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectStatusRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectValidateRequestDto
import pl.tajchert.paczko.fast.core.network.dto.ErrorResponseDto
import pl.tajchert.paczko.fast.core.network.dto.GeoPointDto
import pl.tajchert.paczko.fast.core.network.dto.ParcelCompartmentDto
import retrofit2.HttpException
import java.net.SocketTimeoutException

private const val MAX_STATUS_POLL_ATTEMPTS = 3

class DefaultCollectRepository @Inject constructor(
    private val api: InpostCollectApi,
    private val json: Json,
) : CollectRepository {
    override suspend fun validate(shipmentNumber: String, openCode: String, geoPoint: GeoPoint): String =
        collectApiCall {
            api.validate(
                CollectValidateRequestDto(
                    parcel = ParcelCompartmentDto(shipmentNumber, openCode),
                    geoPoint = GeoPointDto(geoPoint.latitude, geoPoint.longitude, geoPoint.accuracy),
                ),
            ).sessionUuid
        }

    override suspend fun open(sessionUuid: String) {
        collectApiCall {
            api.open(CollectSessionRequestDto(sessionUuid))
        }
    }

    /**
     * The status endpoint is a long-poll that resolves once the compartment
     * reaches [expectedStatus] — for CLOSED that means waiting for the user to
     * remove the parcel and shut the door, which can outlast even the extended
     * per-call read timeout. Re-issue on read timeout up to
     * [MAX_STATUS_POLL_ATTEMPTS] so a slow user doesn't get a spurious error.
     * Any real API error ([HttpException] → [CollectApiException]) or
     * cancellation propagates immediately.
     */
    override suspend fun pollStatus(sessionUuid: String, expectedStatus: ExpectedCompartmentStatus) {
        var attempt = 0
        while (true) {
            attempt++
            try {
                collectApiCall {
                    api.status(CollectStatusRequestDto(sessionUuid, expectedStatus.name))
                }
                return
            } catch (timeout: SocketTimeoutException) {
                if (attempt >= MAX_STATUS_POLL_ATTEMPTS) throw timeout
            }
        }
    }

    override suspend fun closed(sessionUuid: String) {
        collectApiCall {
            api.closed(CollectSessionRequestDto(sessionUuid))
        }
    }

    override suspend fun claim(sessionUuid: String, shipmentNumbers: List<String>) {
        collectApiCall {
            api.claim(CollectClaimRequestDto(sessionUuid, shipmentNumbers))
        }
    }

    private suspend fun <T> collectApiCall(block: suspend () -> T): T {
        try {
            return block()
        } catch (exception: HttpException) {
            throw exception.toCollectApiException()
        } catch (exception: SerializationException) {
            throw CollectUnexpectedResponseException(exception)
        }
    }

    private fun HttpException.toCollectApiException(): CollectApiException {
        val apiValue = response()
            ?.errorBody()
            ?.string()
            ?.takeIf { it.isNotBlank() }
            ?.let(::decodeError)
            ?: CollectErrorCode.Unknown.apiValue

        return CollectApiException(apiValue)
    }

    private fun decodeError(body: String): String? =
        try {
            json.decodeFromString<ErrorResponseDto>(body).error
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
}
