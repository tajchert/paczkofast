package pl.tajchert.paczko.fast.core.data.mapper

import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent
import pl.tajchert.paczko.fast.core.network.dto.ParcelDto

private const val PARCEL_STATUS_TYPE = "PARCEL_STATUS"

/**
 * The status-history rows of a parcel's [ParcelDto.eventLog], newest-first as
 * returned by the API. Non-status log rows (e.g. invoice events) and nameless
 * entries are dropped.
 */
fun ParcelDto.toTrackingEvents(): List<TrackingEvent> =
    eventLog
        .filter { it.type == PARCEL_STATUS_TYPE && it.name != null }
        .map { TrackingEvent(status = it.name!!, date = it.date) }
