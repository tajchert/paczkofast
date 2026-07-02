package pl.tajchert.paczko.fast.core.model.parcel

/**
 * The live, detail-only fields fetched when a parcel is opened — the tracking
 * history plus display metadata not stored in the cached [Parcel].
 *
 * @param sizeCode raw parcelSize letter (A–J); mapped to a label in the UI.
 */
data class ParcelDetails(
    val events: List<TrackingEvent> = emptyList(),
    val sizeCode: String? = null,
    val senderName: String? = null,
    val shipmentType: String? = null,
)
