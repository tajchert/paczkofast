package pl.tajchert.paczko.fast.feature.parcels.impl

import pl.tajchert.paczko.fast.core.model.parcel.Parcel

/**
 * 2+ ready parcels sharing one locker compartment (same
 * [Parcel.multiCompartmentUuid]).
 */
data class MultiPackageGroup(
    val uuid: String,
    val members: List<Parcel>,
) {
    /**
     * The member to validate/open with — the one carrying the full member list
     * (`multiPackageShipmentNumbers`), falling back to the first.
     */
    val representative: Parcel
        get() = members.firstOrNull { it.multiPackageShipmentNumbers.isNotEmpty() } ?: members.first()
}

/** A row on the ready list: a standalone parcel or a multi-package group. */
sealed interface ReadyItem {
    data class Single(val parcel: Parcel) : ReadyItem
    data class Multi(val group: MultiPackageGroup) : ReadyItem
}

/**
 * Collapses ready parcels into display rows, grouping those that share a
 * non-null [Parcel.multiCompartmentUuid] into a single [ReadyItem.Multi] (only
 * when 2+ share it; a lone member stays a [ReadyItem.Single]). A group takes the
 * list position of its first member; order is otherwise preserved.
 */
fun groupReadyParcels(parcels: List<Parcel>): List<ReadyItem> {
    val byUuid = parcels
        .filter { !it.multiCompartmentUuid.isNullOrBlank() }
        .groupBy { it.multiCompartmentUuid }

    val emitted = mutableSetOf<String>()
    val items = mutableListOf<ReadyItem>()
    for (parcel in parcels) {
        val uuid = parcel.multiCompartmentUuid
        val members = uuid?.let { byUuid[it] }
        if (uuid != null && members != null && members.size >= 2) {
            if (emitted.add(uuid)) {
                items += ReadyItem.Multi(MultiPackageGroup(uuid, members))
            }
        } else {
            items += ReadyItem.Single(parcel)
        }
    }
    return items
}
