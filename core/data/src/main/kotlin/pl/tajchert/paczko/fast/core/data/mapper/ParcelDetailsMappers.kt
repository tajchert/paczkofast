package pl.tajchert.paczko.fast.core.data.mapper

import pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails
import pl.tajchert.paczko.fast.core.network.dto.ParcelDto

/** Maps a single-parcel detail response to its live [ParcelDetails]. */
fun ParcelDto.toParcelDetails(): ParcelDetails = ParcelDetails(
    events = toTrackingEvents(),
    sizeCode = parcelSize,
    senderName = sender?.name,
    shipmentType = shipmentType,
)
