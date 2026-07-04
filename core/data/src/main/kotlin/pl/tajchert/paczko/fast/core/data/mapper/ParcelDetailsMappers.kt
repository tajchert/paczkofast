package pl.tajchert.paczko.fast.core.data.mapper

import pl.tajchert.paczko.fast.core.database.entity.ParcelDetailsEntity
import pl.tajchert.paczko.fast.core.database.entity.TrackingEventEntity
import pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent
import pl.tajchert.paczko.fast.core.network.dto.ParcelDto

/** Maps a single-parcel detail response to its live [ParcelDetails]. */
fun ParcelDto.toParcelDetails(): ParcelDetails = ParcelDetails(
    events = toTrackingEvents(),
    sizeCode = parcelSize,
    senderName = sender?.name,
    shipmentType = shipmentType,
)

/** The scalar detail fields to cache for [shipmentNumber]. */
fun ParcelDetails.toDetailsEntity(shipmentNumber: String) = ParcelDetailsEntity(
    shipmentNumber = shipmentNumber,
    sizeCode = sizeCode,
    senderName = senderName,
    shipmentType = shipmentType,
)

/** The tracking timeline to cache, keeping the API's newest-first order via [TrackingEventEntity.position]. */
fun ParcelDetails.toTrackingEventEntities(shipmentNumber: String): List<TrackingEventEntity> =
    events.mapIndexed { position, event ->
        TrackingEventEntity(
            shipmentNumber = shipmentNumber,
            position = position,
            status = event.status,
            date = event.date,
        )
    }

/** Rebuilds [ParcelDetails] from the cached scalar fields + ordered timeline. */
fun ParcelDetailsEntity?.toParcelDetails(events: List<TrackingEventEntity>) = ParcelDetails(
    events = events.map { TrackingEvent(status = it.status, date = it.date) },
    sizeCode = this?.sizeCode,
    senderName = this?.senderName,
    shipmentType = this?.shipmentType,
)
