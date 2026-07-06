package pl.tajchert.paczko.fast.feature.parcels.impl.list

import pl.tajchert.paczko.fast.core.model.ParcelListOpenButtonMode

internal data class OpenButtonDecision(
    val show: Boolean,
    val usedFirstButton: Boolean,
)

internal fun decideOpenButtonVisibility(
    mode: ParcelListOpenButtonMode,
    canCollect: Boolean,
    firstButtonAlreadyUsed: Boolean,
): OpenButtonDecision = when {
    !canCollect -> OpenButtonDecision(show = false, usedFirstButton = firstButtonAlreadyUsed)
    mode == ParcelListOpenButtonMode.NONE -> OpenButtonDecision(show = false, usedFirstButton = firstButtonAlreadyUsed)
    mode == ParcelListOpenButtonMode.ALL -> OpenButtonDecision(show = true, usedFirstButton = firstButtonAlreadyUsed)
    firstButtonAlreadyUsed -> OpenButtonDecision(show = false, usedFirstButton = true)
    else -> OpenButtonDecision(show = true, usedFirstButton = true)
}
