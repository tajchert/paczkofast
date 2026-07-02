package pl.tajchert.paczko.fast.feature.parcels.impl

import pl.tajchert.paczko.fast.core.designsystem.component.HistoryOutcome
import pl.tajchert.paczko.fast.core.designsystem.component.TimelineEvent
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
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

/**
 * Visible size badge label for a raw parcelSize wire code (A–J), or null when
 * the size is unknown/absent (no badge shown). D→XS; A,E,H→S; B,F,I→M; C,G,J→L.
 */
internal fun parcelSizeLabel(code: String?): String? = when (code?.uppercase()) {
    "D" -> "XS"
    "A", "E", "H" -> "S"
    "B", "F", "I" -> "M"
    "C", "G", "J" -> "L"
    else -> null
}

/**
 * Curated English label for a canonical eventLog status code, falling back to
 * a humanized form of the raw code for anything unmapped.
 */
internal fun trackingEventLabel(status: String): String = when (status.uppercase()) {
    "CREATED", "CONFIRMED" -> "Label created"
    "DISPATCHED_BY_SENDER", "COLLECTED_FROM_SENDER", "TAKEN_BY_COURIER" -> "Picked up from sender"
    "ADOPTED_AT_SOURCE_BRANCH", "ADOPTED_AT_SORTING_CENTER", "ADOPTED_AT_TARGET_BRANCH" -> "At logistics center"
    "SENT_FROM_SOURCE_BRANCH", "SENT_FROM_SORTING_CENTER" -> "In transit"
    "OUT_FOR_DELIVERY" -> "Out for delivery"
    "READY_TO_PICKUP" -> "Ready for pickup"
    "DELIVERED" -> "Delivered"
    "CLAIMED", "COLLECTED_BY_CUSTOMER" -> "Picked up"
    else -> humanizeStatus(status)
}

/**
 * Maps the real, newest-first [TrackingEvent] history to timeline rows: the
 * newest event is current, all are already-happened (none upcoming).
 */
internal fun trackingTimelineEvents(events: List<TrackingEvent>): List<TimelineEvent> =
    events.mapIndexed { index, event ->
        TimelineEvent(
            label = trackingEventLabel(event.status),
            time = formatTimelineTime(event.date),
            isCurrent = index == 0,
            isUpcoming = false,
        )
    }

internal val Parcel.isDelivered: Boolean
    get() = statusGroup?.equals("delivered", ignoreCase = true) == true ||
        status.equals("delivered", ignoreCase = true) ||
        status.equals("returned_to_sender", ignoreCase = true)

/**
 * Statuses meaning the parcel was successfully collected / picked up — the
 * final happy-path stage. "claimed" is what a Paczkomat parcel becomes after a
 * remote or in-person pickup.
 */
private val PICKED_UP_STATUSES = setOf(
    "claimed",
    "collected_by_customer",
    "collected_from_sender",
)

/**
 * True once the parcel has been picked up (locker collection) or delivered —
 * the "Picked up" end of the tracking pipeline.
 */
internal val Parcel.isPickedUp: Boolean
    get() = isDelivered || status.lowercase() in PICKED_UP_STATUSES

/** Terminal statuses beyond [isPickedUp] that belong in the History tab. */
private val FINISHED_STATUSES = setOf(
    "pickup_time_expired",
    "avizo",
    "undelivered",
    "undelivered_cod_cash_receiver",
    "undelivered_incomplete_address",
    "undelivered_lack_of_access_letterbox",
    "undelivered_no_mailbox",
    "undelivered_not_live_address",
    "undelivered_unknown_receiver",
    "undelivered_wrong_address",
    "canceled",
)

/**
 * A parcel is "finished" — belongs in the History tab — once it is delivered,
 * collected, returned, expired, undelivered or canceled. Anything else
 * (ready-for-pickup, in transit, created, or an unrecognized status) stays on
 * the main Parcels tab so an active parcel is never hidden.
 */
internal val Parcel.isFinished: Boolean
    get() = isPickedUp || status.lowercase() in FINISHED_STATUSES

/**
 * The moment a finished parcel reached its terminal state — the collection
 * time ([Parcel.pickUpDate]) or, for a return, [Parcel.returnedToSenderDate].
 * Falls back to the stored/expiry dates for parcels the carrier didn't stamp
 * with either. Null when nothing dateable is known.
 */
internal fun Parcel.historyCompletionInstant(): Instant? =
    parseInstant(pickUpDate)
        ?: parseInstant(returnedToSenderDate)
        ?: parseInstant(storedDate)
        ?: parseInstant(expiryDate)

/**
 * Recency key for ordering History-tab parcels newest-first; parcels with no
 * dateable completion timestamp sort to the bottom.
 */
internal fun Parcel.historySortKey(): Long =
    historyCompletionInstant()?.toEpochMilli() ?: Long.MIN_VALUE

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

/**
 * Human-facing parcel title: the sender/merchant name when the carrier
 * provides one (e.g. "Amazon Polska"), otherwise the formatted shipment
 * number.
 */
internal fun parcelTitle(parcel: Parcel): String =
    parcel.senderName?.takeIf { it.isNotBlank() } ?: formatShipmentNumber(parcel.shipmentNumber)

// -----------------------------------------------------------------------------
// History tab
// -----------------------------------------------------------------------------

/** Successfully-collected/delivered statuses — a positive (amber) outcome. */
private val OUTCOME_PICKED_UP = setOf(
    "delivered",
    "claimed",
    "collected_by_customer",
    "collected_from_sender",
)

/** Statuses where the pickup window simply ran out. */
private val OUTCOME_EXPIRED = setOf(
    "pickup_time_expired",
    "avizo",
)

/**
 * Classifies a finished parcel into the [HistoryOutcome] shown on its history
 * row. Anything finished that wasn't collected or expired (returned,
 * undelivered, canceled) is treated as [HistoryOutcome.Returned].
 */
internal fun historyOutcome(parcel: Parcel): HistoryOutcome = when (parcel.status.lowercase()) {
    in OUTCOME_PICKED_UP -> HistoryOutcome.PickedUp
    in OUTCOME_EXPIRED -> HistoryOutcome.Expired
    else -> HistoryOutcome.Returned
}

/**
 * The muted outcome line under a history row's title, e.g.
 * "Picked up · Locker WAW04B", "Expired · returned to sender" or
 * "Returned to sender".
 */
internal fun historyOutcomeLine(parcel: Parcel): String = when (historyOutcome(parcel)) {
    HistoryOutcome.PickedUp -> listOfNotNull(
        "Picked up",
        parcel.pickupPoint?.name?.let { "Locker $it" },
    ).joinToString(" · ")

    HistoryOutcome.Expired -> "Expired · returned to sender"
    HistoryOutcome.Returned -> humanizeStatus(parcel.status)
}

private val HISTORY_DATE_TIME_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH)
private val HISTORY_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
private val HISTORY_MONTH_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)
private val HISTORY_MONTH_YEAR_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

/**
 * Trailing date label for a history row. Parcels finished in the current month
 * show the time ("2 Jul, 14:32"); older ones show the date only ("28 Jun").
 * Falls back to an empty string when no completion date is known.
 */
internal fun historyDateLabel(
    parcel: Parcel,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val instant = historyInstant(parcel) ?: return ""
    val date = instant.atZone(zone)
    val nowDate = now.atZone(zone)
    val sameMonth = date.year == nowDate.year && date.month == nowDate.month
    return if (sameMonth) {
        HISTORY_DATE_TIME_FORMAT.format(date)
    } else {
        HISTORY_DATE_FORMAT.format(date)
    }
}

/** Grouping key for the History tab's month sections, newest month first. */
internal fun historyMonthKey(
    parcel: Parcel,
    zone: ZoneId = ZoneId.systemDefault(),
): YearMonth? = historyInstant(parcel)?.let { YearMonth.from(it.atZone(zone)) }

/**
 * Uppercase month heading, e.g. "July" for the current year, "June 2025" for
 * an earlier year.
 */
internal fun historyMonthLabel(
    yearMonth: YearMonth,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val format = if (yearMonth.year == now.atZone(zone).year) {
        HISTORY_MONTH_FORMAT
    } else {
        HISTORY_MONTH_YEAR_FORMAT
    }
    return format.format(yearMonth)
}

private fun historyInstant(parcel: Parcel): Instant? = parcel.historyCompletionInstant()
