package pl.tajchert.paczko.fast.core.domain

import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent
import javax.inject.Inject

class GetTrackingEventsUseCase @Inject constructor(
    private val repository: ParcelRepository,
) {
    suspend operator fun invoke(shipmentNumber: String): List<TrackingEvent> =
        repository.getTrackingEvents(shipmentNumber)
}
