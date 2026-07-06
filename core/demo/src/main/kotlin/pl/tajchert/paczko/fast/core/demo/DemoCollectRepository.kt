package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.delay
import pl.tajchert.paczko.fast.core.data.repository.CollectApiException
import pl.tajchert.paczko.fast.core.data.repository.CollectRepository
import pl.tajchert.paczko.fast.core.model.collect.CollectErrorCode
import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint
import javax.inject.Inject

/**
 * Deterministic, offline collect flow. The scenario is chosen by the parcel's
 * shipment number at [validate] and drives delays/errors for the rest of the
 * sequence. Never touches the network — cannot open a real locker.
 */
class DemoCollectRepository @Inject constructor() : CollectRepository {

    private var scenario: CollectScenario = CollectScenario.Success

    override suspend fun validate(shipmentNumber: String, openCode: String, geoPoint: GeoPoint): String {
        scenario = scenarioFor(shipmentNumber)
        delay(STEP_DELAY_MS)
        when (scenario) {
            CollectScenario.InvalidParcelState -> fail(CollectErrorCode.InvalidParcelState)
            CollectScenario.BoxMachineNotFound -> fail(CollectErrorCode.BoxMachineNotFound)
            else -> Unit
        }
        return DEMO_SESSION_UUID
    }

    override suspend fun open(sessionUuid: String) {
        delay(STEP_DELAY_MS)
        when (scenario) {
            CollectScenario.SessionExpired -> fail(CollectErrorCode.SessionExpired)
            CollectScenario.InvalidCompartmentState -> fail(CollectErrorCode.InvalidCompartmentState)
            CollectScenario.CannotFindCompartment -> fail(CollectErrorCode.CannotFindCompartment)
            CollectScenario.InvalidSession -> fail(CollectErrorCode.InvalidSession)
            CollectScenario.Unknown -> fail(CollectErrorCode.Unknown)
            else -> Unit
        }
    }

    override suspend fun pollStatus(sessionUuid: String, expectedStatus: ExpectedCompartmentStatus) {
        when (expectedStatus) {
            ExpectedCompartmentStatus.OPENED -> {
                delay(STEP_DELAY_MS)
                if (scenario == CollectScenario.InvalidSessionState) fail(CollectErrorCode.InvalidSessionState)
            }
            ExpectedCompartmentStatus.CLOSED -> {
                // Slow-close showcases the "close the compartment" waiting state.
                delay(if (scenario == CollectScenario.SlowClose) SLOW_CLOSE_DELAY_MS else STEP_DELAY_MS)
            }
        }
    }

    override suspend fun closed(sessionUuid: String) {
        delay(STEP_DELAY_MS)
        // Box already opened; failing here exercises the soft (boxAlreadyOpen) path.
        // Uses a non-retryable code (canRestartValidation = false) because restarting
        // validation makes no sense once the box has already been opened.
        if (scenario == CollectScenario.PostOpenSoftFail) fail(CollectErrorCode.Unknown)
    }

    override suspend fun claim(sessionUuid: String, shipmentNumbers: List<String>) {
        delay(STEP_DELAY_MS)
    }

    private fun fail(code: CollectErrorCode): Nothing = throw CollectApiException(code.apiValue)

    private companion object {
        const val DEMO_SESSION_UUID = "demo-session-uuid"
        const val STEP_DELAY_MS = 600L
        const val SLOW_CLOSE_DELAY_MS = 6_000L
    }
}
