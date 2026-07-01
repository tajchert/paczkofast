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
    fun collect(shipmentNumber: String, openCode: String): Flow<CollectState> = flow {
        try {
            emit(CollectState.Validating)
            val geoPoint = locationProvider.currentLocation()
            val sessionUuid = repository.validate(shipmentNumber, openCode, geoPoint)

            emit(CollectState.Opening(sessionUuid))
            repository.open(sessionUuid)

            emit(CollectState.WaitingForOpened(sessionUuid))
            repository.pollStatus(sessionUuid, ExpectedCompartmentStatus.OPENED)

            emit(CollectState.Opened(sessionUuid))
            emit(CollectState.WaitingForClosed(sessionUuid))
            repository.pollStatus(sessionUuid, ExpectedCompartmentStatus.CLOSED)

            emit(CollectState.ConfirmingClosed(sessionUuid))
            repository.closed(sessionUuid)

            emit(CollectState.Claiming(sessionUuid))
            repository.claim(sessionUuid, shipmentNumber)

            emit(CollectState.Completed)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable

            emit(throwable.toFailedState())
        }
    }

    private fun Throwable.toFailedState(): CollectState.Failed {
        if (this is CollectApiException) {
            val code = CollectErrorCode.fromApiValue(apiValue)
            return CollectState.Failed(
                message = if (code == CollectErrorCode.Unknown) apiValue else code.apiValue,
                canRetryFromValidation = code.canRestartValidation,
            )
        }

        return CollectState.Failed(
            message = message ?: CollectErrorCode.Unknown.apiValue,
            canRetryFromValidation = false,
        )
    }
}
