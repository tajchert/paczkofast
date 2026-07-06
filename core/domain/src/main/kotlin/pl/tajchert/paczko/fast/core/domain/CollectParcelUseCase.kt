package pl.tajchert.paczko.fast.core.domain

import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.data.repository.CollectApiException
import pl.tajchert.paczko.fast.core.data.repository.CollectRepository
import pl.tajchert.paczko.fast.core.model.collect.CollectErrorCode
import pl.tajchert.paczko.fast.core.model.collect.CollectState
import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus

class CollectParcelUseCase @Inject constructor(
    private val repository: CollectRepository,
    private val locationProvider: LocationProvider,
) {
    fun collect(
        shipmentNumber: String,
        openCode: String,
        claimShipmentNumbers: List<String> = listOf(shipmentNumber),
    ): Flow<CollectState> = flow {
        // Once the compartment opens, the parcel is physically collectable, so a
        // later failure (closing/claim/network) is a soft, snackbar-level problem
        // rather than a full-screen error.
        var boxAlreadyOpen = false
        try {
            emit(CollectState.Validating)
            val geoPoint = locationProvider.currentLocation()
            val sessionUuid = repository.validate(shipmentNumber, openCode, geoPoint)

            emit(CollectState.Opening(sessionUuid))
            repository.open(sessionUuid)

            emit(CollectState.WaitingForOpened(sessionUuid))
            repository.pollStatus(sessionUuid, ExpectedCompartmentStatus.OPENED)

            emit(CollectState.Opened(sessionUuid))
            boxAlreadyOpen = true
            emit(CollectState.WaitingForClosed(sessionUuid))
            repository.pollStatus(sessionUuid, ExpectedCompartmentStatus.CLOSED)

            emit(CollectState.ConfirmingClosed(sessionUuid))
            repository.closed(sessionUuid)

            emit(CollectState.Claiming(sessionUuid))
            repository.claim(sessionUuid, claimShipmentNumbers)

            emit(CollectState.Completed)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable

            emit(throwable.toFailedState(boxAlreadyOpen))
        }
    }

    private fun Throwable.toFailedState(boxAlreadyOpen: Boolean): CollectState.Failed {
        if (this is CollectApiException) {
            val code = CollectErrorCode.fromApiValue(apiValue)
            return CollectState.Failed(
                message = if (code == CollectErrorCode.Unknown) apiValue else code.apiValue,
                canRetryFromValidation = code.canRestartValidation,
                boxAlreadyOpen = boxAlreadyOpen,
            )
        }

        return CollectState.Failed(
            message = message ?: CollectErrorCode.Unknown.apiValue,
            canRetryFromValidation = false,
            boxAlreadyOpen = boxAlreadyOpen,
        )
    }
}
