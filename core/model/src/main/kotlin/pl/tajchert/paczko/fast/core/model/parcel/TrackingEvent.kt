package pl.tajchert.paczko.fast.core.model.parcel

/**
 * One entry in a parcel's tracking history.
 *
 * @param status canonical eventLog status code, e.g. "OUT_FOR_DELIVERY".
 * @param date raw ISO-8601 timestamp string; parsed/formatted in the UI layer.
 */
data class TrackingEvent(
    val status: String,
    val date: String?,
)
