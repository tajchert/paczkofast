package pl.tajchert.paczko.fast.core.data.repository

import java.net.SocketTimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint
import pl.tajchert.paczko.fast.core.network.InpostCollectApi
import pl.tajchert.paczko.fast.core.network.dto.CollectClaimRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectSessionRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectStatusRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectValidateRequestDto
import pl.tajchert.paczko.fast.core.network.dto.CollectValidateResponseDto
import pl.tajchert.paczko.fast.core.network.dto.CompartmentResponseDto
import retrofit2.HttpException
import retrofit2.Response

class DefaultCollectRepositoryTest {

    @Test
    fun validateTranslatesCollectErrorBodyToApiException() = runTest {
        val repository = DefaultCollectRepository(
            api = FakeCollectApi(
                validateFailure = httpException("""{"error":"sessionExpired"}"""),
            ),
            json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            },
        )

        val exception = assertFailsWith<CollectApiException> {
            repository.validate(
                shipmentNumber = "123",
                openCode = "456",
                geoPoint = GeoPoint(52.1, 21.0, 12.0),
            )
        }

        assertEquals("sessionExpired", exception.message)
    }

    @Test
    fun validateMapsUnparseableSuccessBodyToTypedError() = runTest {
        val parserError = SerializationException(
            "Field 'sessionUuid' is required for type 'CollectValidateResponseDto', but it was missing",
        )
        val repository = DefaultCollectRepository(
            api = FakeCollectApi(validateFailure = parserError),
            json = leniency(),
        )

        val exception = assertFailsWith<CollectUnexpectedResponseException> {
            repository.validate(
                shipmentNumber = "123",
                openCode = "456",
                geoPoint = GeoPoint(52.1, 21.0, 12.0),
            )
        }

        // Human-readable — this message reaches the error screen verbatim.
        assertEquals("Unexpected response from the locker service", exception.message)
        assertEquals(parserError, exception.cause)
    }

    @Test
    fun pollStatusReissuesAfterReadTimeoutThenSucceeds() = runTest {
        val api = FakeCollectApi(statusTimeoutsBeforeSuccess = 2)
        val repository = DefaultCollectRepository(api = api, json = leniency())

        repository.pollStatus("session", ExpectedCompartmentStatus.CLOSED)

        // Two timeouts + one successful poll.
        assertEquals(3, api.statusCalls)
    }

    @Test
    fun pollStatusGivesUpAfterMaxAttempts() = runTest {
        val api = FakeCollectApi(statusTimeoutsBeforeSuccess = Int.MAX_VALUE)
        val repository = DefaultCollectRepository(api = api, json = leniency())

        assertFailsWith<SocketTimeoutException> {
            repository.pollStatus("session", ExpectedCompartmentStatus.CLOSED)
        }
        assertEquals(3, api.statusCalls)
    }
}

private fun leniency(): Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

private fun httpException(body: String): HttpException =
    HttpException(
        Response.error<CollectValidateResponseDto>(
            400,
            body.toResponseBody("application/json".toMediaType()),
        ),
    )

private class FakeCollectApi(
    private val validateFailure: Throwable? = null,
    private val statusTimeoutsBeforeSuccess: Int = 0,
) : InpostCollectApi {
    var statusCalls = 0
        private set

    override suspend fun validate(body: CollectValidateRequestDto): CollectValidateResponseDto {
        validateFailure?.let { throw it }
        return CollectValidateResponseDto(sessionUuid = "session")
    }

    override suspend fun open(body: CollectSessionRequestDto): CompartmentResponseDto =
        CompartmentResponseDto()

    override suspend fun status(body: CollectStatusRequestDto): CompartmentResponseDto {
        statusCalls++
        if (statusCalls <= statusTimeoutsBeforeSuccess) throw SocketTimeoutException("timeout")
        return CompartmentResponseDto()
    }

    override suspend fun closed(body: CollectSessionRequestDto): CompartmentResponseDto =
        CompartmentResponseDto()

    override suspend fun claim(body: CollectClaimRequestDto): CompartmentResponseDto =
        CompartmentResponseDto()
}
