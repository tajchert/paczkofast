package pl.tajchert.paczko.fast.core.model

/**
 * How the user confirms opening a locker compartment.
 *
 * - [HOLD]   Press-and-hold a bar; opens only after a completed hold. Default.
 * - [NEARBY] Single tap opens, enabled only when the phone is at the locker
 *            (see isWithinNearbyThreshold); a hold override is always available.
 */
enum class LockerOpenMode {
    HOLD,
    NEARBY,
}
