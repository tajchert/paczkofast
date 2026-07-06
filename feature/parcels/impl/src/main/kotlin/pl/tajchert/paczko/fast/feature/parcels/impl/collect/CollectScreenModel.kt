package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.collect.CollectState

enum class CollectHero { Distance, OpenBox, Check, Error }

enum class CollectAction { HoldOnly, NearbyOpen, BackOnly, RetrySupport, None }

/**
 * Pure mapping from collect state + ui state to the fixed-scaffold slots. Keeping this
 * side-effect-free makes the "no layout shift" contract unit-testable and keeps
 * [CollectScreen] declarative.
 */
data class CollectScreenModel(
    val header: String,
    val hero: CollectHero,
    val headline: String,
    val subline: String?,
    val action: CollectAction,
    val openEnabled: Boolean,
    val showOverrideHold: Boolean,
)

private fun lockerHeader(lockerName: String?): String =
    (lockerName?.let { "Locker $it" } ?: "Locker").uppercase()

fun collectScreenModel(state: CollectState, uiState: CollectUiState): CollectScreenModel {
    val locker = uiState.lockerName
    return when (state) {
        is CollectState.Idle -> when (uiState.openMode) {
            LockerOpenMode.HOLD -> CollectScreenModel(
                header = lockerHeader(locker),
                hero = CollectHero.Distance,
                headline = "Hold to open",
                subline = collectSubline(uiState.members.size),
                action = CollectAction.HoldOnly,
                openEnabled = false,
                showOverrideHold = false,
            )
            LockerOpenMode.NEARBY -> CollectScreenModel(
                header = lockerHeader(locker),
                hero = CollectHero.Distance,
                headline = if (uiState.nearbyReady) "Ready to open" else "Get closer",
                subline = nearbySubline(uiState),
                action = CollectAction.NearbyOpen,
                openEnabled = uiState.nearbyReady,
                showOverrideHold = true,
            )
        }

        CollectState.Validating,
        is CollectState.Opening,
        is CollectState.WaitingForOpened -> CollectScreenModel(
            header = lockerHeader(locker),
            hero = CollectHero.Distance,
            headline = transitionalHeadline(state),
            subline = null,
            action = CollectAction.None,
            openEnabled = false,
            showOverrideHold = false,
        )

        is CollectState.Opened,
        is CollectState.WaitingForClosed,
        is CollectState.ConfirmingClosed,
        is CollectState.Claiming -> CollectScreenModel(
            header = lockerHeader(locker),
            hero = CollectHero.OpenBox,
            headline = "The box is open",
            subline = null,
            action = CollectAction.None,
            openEnabled = false,
            showOverrideHold = false,
        )

        CollectState.Completed -> CollectScreenModel(
            header = "Box closed".uppercase(),
            hero = CollectHero.Check,
            headline = if (uiState.members.size > 1) "All picked up!" else "Picked up!",
            subline = null,
            action = CollectAction.BackOnly,
            openEnabled = false,
            showOverrideHold = false,
        )

        is CollectState.Failed -> CollectScreenModel(
            header = (locker?.let { "Error · Locker $it" } ?: "Error").uppercase(),
            hero = CollectHero.Error,
            headline = "The box didn't open",
            subline = state.message,
            action = CollectAction.RetrySupport,
            openEnabled = false,
            showOverrideHold = false,
        )

        CollectState.Canceled -> CollectScreenModel(
            header = lockerHeader(locker),
            hero = CollectHero.Distance,
            headline = "Collection canceled",
            subline = null,
            action = CollectAction.BackOnly,
            openEnabled = false,
            showOverrideHold = false,
        )
    }
}

private fun transitionalHeadline(state: CollectState): String = when (state) {
    CollectState.Validating -> "Checking parcel and location"
    is CollectState.Opening -> "Opening compartment"
    is CollectState.WaitingForOpened -> "Waiting for the door to open"
    else -> ""
}

private fun nearbySubline(uiState: CollectUiState): String? = when {
    uiState.nearbyReady -> "You're at the locker — tap to open"
    uiState.distanceMeters != null -> "Move closer — ${uiState.distanceMeters} m away"
    else -> "Waiting for a precise GPS fix…"
}

internal fun collectSubline(count: Int): String = when {
    count > 1 -> "$count parcels share this box — you'll take them all at once"
    else -> "Stand at the locker before you start"
}
