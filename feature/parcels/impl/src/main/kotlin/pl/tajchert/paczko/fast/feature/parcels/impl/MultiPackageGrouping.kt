package pl.tajchert.paczko.fast.feature.parcels.impl

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.tajchert.paczko.fast.core.model.parcel.Parcel

/**
 * 2+ ready parcels sharing one locker compartment (same
 * [Parcel.multiCompartmentUuid]).
 */
@Immutable
data class MultiPackageGroup(
    val uuid: String,
    val members: ImmutableList<Parcel>,
) {
    /**
     * The member to validate/open with — the one carrying the full member list
     * (`multiPackageShipmentNumbers`), falling back to the first.
     */
    val representative: Parcel
        get() = members.firstOrNull { it.multiPackageShipmentNumbers.isNotEmpty() } ?: members.first()
}

/** A display row: a standalone parcel or a multi-package group. */
sealed interface CompartmentItem {
    data class Single(val parcel: Parcel) : CompartmentItem
    data class Multi(val group: MultiPackageGroup) : CompartmentItem
}

/**
 * Collapses parcels into display rows, grouping those that share a non-null
 * [Parcel.multiCompartmentUuid] into a single [CompartmentItem.Multi] (only when
 * 2+ share it; a lone member stays a [CompartmentItem.Single]). A group takes the
 * list position of its first member; order is otherwise preserved. Used for both
 * the ready list and the History tab.
 */
fun groupByCompartment(parcels: List<Parcel>): List<CompartmentItem> {
    val byUuid = parcels
        .filter { !it.multiCompartmentUuid.isNullOrBlank() }
        .groupBy { it.multiCompartmentUuid }

    val emitted = mutableSetOf<String>()
    val items = mutableListOf<CompartmentItem>()
    for (parcel in parcels) {
        val uuid = parcel.multiCompartmentUuid
        val members = uuid?.let { byUuid[it] }
        if (uuid != null && members != null && members.size >= 2) {
            if (emitted.add(uuid)) {
                items += CompartmentItem.Multi(MultiPackageGroup(uuid, members.toImmutableList()))
            }
        } else {
            items += CompartmentItem.Single(parcel)
        }
    }
    return items
}
