package pl.tajchert.paczko.fast.core.domain

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.data.repository.CollectApiException
import pl.tajchert.paczko.fast.core.data.repository.CollectRepository
import pl.tajchert.paczko.fast.core.model.collect.CollectErrorCode
import pl.tajchert.paczko.fast.core.model.collect.CollectState
import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

class CollectParcelUseCaseTest {

    @Test
    fun collectFlowRunsValidateOpenOpenedClosedClosedClaimInOrder() = runTest {
        val repository = FakeCollectRepository()
        val location = FakeLocationProvider(GeoPoint(52.1, 21.0, 12.0))
        val useCase = CollectParcelUseCase(repository, location)

        val states = useCase.collect(
            shipmentNumber = "123",
            openCode = "456",
        ).toList()

        assertEquals(
            listOf(
                CollectState.Validating,
                CollectState.Opening("session"),
                CollectState.WaitingForOpened("session"),
                CollectState.Opened("session"),
                CollectState.WaitingForClosed("session"),
                CollectState.ConfirmingClosed("session"),
                CollectState.Claiming("session"),
                CollectState.Completed,
            ),
            states,
        )
        assertEquals(
            listOf("validate", "open", "status-OPENED", "status-CLOSED", "closed", "claim"),
            repository.calls,
        )
    }

    @Test
    fun sessionExpiredApiFailureCanRestartValidation() = runTest {
        val repository = FakeCollectRepository(failOnValidate = CollectErrorCode.SessionExpired)
        val location = FakeLocationProvider(GeoPoint(52.1, 21.0, 12.0))
        val useCase = CollectParcelUseCase(repository, location)

        val states = useCase.collect("123", "456").toList()

        assertEquals(
            CollectState.Failed(
                message = "sessionExpired",
                canRetryFromValidation = true,
            ),
            states.last(),
        )
    }

    @Test
    fun locationFailureEmitsClearNonRetryableFailure() = runTest {
        val repository = FakeCollectRepository()
        val location = FakeLocationProvider(failure = IllegalStateException("Location permission is required"))
        val useCase = CollectParcelUseCase(repository, location)

        val states = useCase.collect("123", "456").toList()

        assertEquals(
            CollectState.Failed(
                message = "Location permission is required",
                canRetryFromValidation = false,
            ),
            states.last(),
        )
        assertEquals(emptyList<String>(), repository.calls)
    }
}

private class FakeCollectRepository(
    private val failOnValidate: CollectErrorCode? = null,
) : CollectRepository {
    val calls = mutableListOf<String>()

    override suspend fun validate(shipmentNumber: String, openCode: String, geoPoint: GeoPoint): String {
        calls += "validate"
        failOnValidate?.let { throw CollectApiException(it.apiValue) }
        assertEquals("123", shipmentNumber)
        assertEquals("456", openCode)
        assertEquals(GeoPoint(52.1, 21.0, 12.0), geoPoint)
        return "session"
    }

    override suspend fun open(sessionUuid: String) {
        calls += "open"
        assertEquals("session", sessionUuid)
    }

    override suspend fun pollStatus(sessionUuid: String, expectedStatus: ExpectedCompartmentStatus) {
        calls += "status-${expectedStatus.name}"
        assertEquals("session", sessionUuid)
    }

    override suspend fun closed(sessionUuid: String) {
        calls += "closed"
        assertEquals("session", sessionUuid)
    }

    override suspend fun claim(sessionUuid: String, shipmentNumbers: List<String>) {
        calls += "claim"
        assertEquals("session", sessionUuid)
        assertEquals(listOf("123"), shipmentNumbers)
    }
}

private class FakeLocationProvider(
    private val geoPoint: GeoPoint? = null,
    private val failure: Throwable? = null,
) : LocationProvider {
    override suspend fun currentLocation(): GeoPoint {
        failure?.let { throw it }
        return requireNotNull(geoPoint)
    }
}
