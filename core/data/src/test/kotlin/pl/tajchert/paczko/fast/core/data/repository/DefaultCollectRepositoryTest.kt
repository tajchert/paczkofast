package pl.tajchert.paczko.fast.core.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
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
) : InpostCollectApi {
    override suspend fun validate(body: CollectValidateRequestDto): CollectValidateResponseDto {
        validateFailure?.let { throw it }
        return CollectValidateResponseDto(sessionUuid = "session")
    }

    override suspend fun open(body: CollectSessionRequestDto): CompartmentResponseDto =
        CompartmentResponseDto()

    override suspend fun status(body: CollectStatusRequestDto): CompartmentResponseDto =
        CompartmentResponseDto()

    override suspend fun closed(body: CollectSessionRequestDto): CompartmentResponseDto =
        CompartmentResponseDto()

    override suspend fun claim(body: CollectClaimRequestDto): CompartmentResponseDto =
        CompartmentResponseDto()
}
