package pl.tajchert.paczko.fast.feature.parcels.impl

import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Presentation helpers turning raw [Parcel] fields (ISO date strings,
 * snake_case statuses) into the strings and fractions the design shows.
 */

internal val Parcel.isReadyForPickup: Boolean
    get() = statusGroup?.equals("ready", ignoreCase = true) == true ||
        status.equals("ready_to_pickup", ignoreCase = true)

/** "out_for_delivery" / "DELIVERED" → "Out for delivery" / "Delivered" */
internal fun humanizeStatus(status: String): String =
    status.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

internal val Parcel.isDelivered: Boolean
    get() = statusGroup?.equals("delivered", ignoreCase = true) == true ||
        status.equals("delivered", ignoreCase = true) ||
        status.equals("returned_to_sender", ignoreCase = true)

/**
 * Rough delivery stage (0..[TRANSIT_SEGMENTS]) for the segmented progress
 * bar on in-transit cards.
 */
internal const val TRANSIT_SEGMENTS = 4

internal fun transitCompletedSegments(status: String): Int = when (status.lowercase()) {
    "created", "confirmed", "offers_prepared", "offer_selected", "dispatched_by_sender" -> 1
    "collected_from_sender", "taken_by_courier", "adopted_at_source_branch", "sent_from_source_branch", "adopted_at_sorting_center", "sent_from_sorting_center" -> 2
    "out_for_delivery", "adopted_at_target_branch" -> 3
    else -> 1
}

/**
 * Pickup deadline presentation for a ready-for-pickup parcel.
 *
 * @param progress Fraction of the pickup window still remaining (1f = just
 *   stored, 0f = expired).
 * @param urgent Less than [URGENT_THRESHOLD_HOURS] hours left.
 */
internal data class PickupCountdown(
    val deadlineText: String,
    val timeLeftText: String,
    val countdownText: String,
    val progress: Float,
    val urgent: Boolean,
)

private const val URGENT_THRESHOLD_HOURS = 12L

/** Assumed pickup window when the stored date is unknown. */
private val DEFAULT_PICKUP_WINDOW: Duration = Duration.ofHours(48)

private val DEADLINE_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", Locale.ENGLISH)
internal val TIMELINE_TIME_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yy · HH:mm", Locale.ENGLISH)

internal fun pickupCountdown(parcel: Parcel, now: Instant = Instant.now()): PickupCountdown? {
    val expiry = parseInstant(parcel.expiryDate) ?: return null
    val stored = parseInstant(parcel.storedDate)
    val remaining = Duration.between(now, expiry)
    if (remaining.isNegative) return null

    val window = if (stored != null && stored.isBefore(expiry)) {
        Duration.between(stored, expiry)
    } else {
        DEFAULT_PICKUP_WINDOW
    }
    val progress = (remaining.seconds.toFloat() / window.seconds.toFloat()).coerceIn(0f, 1f)

    val countdownText = when {
        remaining.toMinutes() < 60 -> "${remaining.toMinutes()} min"
        remaining.toHours() < 72 -> "${remaining.toHours()} h"
        else -> "${remaining.toDays()} d"
    }
    return PickupCountdown(
        deadlineText = "Pick up by " + DEADLINE_FORMAT.format(expiry.atZone(ZoneId.systemDefault())),
        timeLeftText = "$countdownText left",
        countdownText = countdownText,
        progress = progress,
        urgent = remaining.toHours() < URGENT_THRESHOLD_HOURS,
    )
}

internal fun formatTimelineTime(value: String?): String? =
    parseInstant(value)?.let { TIMELINE_TIME_FORMAT.format(it.atZone(ZoneId.systemDefault())) }

internal fun parseInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(value).toInstant() }
        .recoverCatching { Instant.parse(value) }
        .recoverCatching {
            LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant()
        }
        .getOrNull()
}

/** "WAW04B · Górczewska 12" style locker line for parcel cards. */
internal fun lockerLine(parcel: Parcel): String {
    val point = parcel.pickupPoint ?: return "Pickup point pending"
    return listOfNotNull(
        "Locker ${point.name}",
        point.addressLine?.takeIf { it.isNotBlank() },
    ).joinToString(" · ")
}

/** "0000 0000 0000 0000 0000 0000" — shipment number grouped in fours. */
internal fun formatShipmentNumber(shipmentNumber: String): String =
    shipmentNumber.chunked(4).joinToString(" ")
