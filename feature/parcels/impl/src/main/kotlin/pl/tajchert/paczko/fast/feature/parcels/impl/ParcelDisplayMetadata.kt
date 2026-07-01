package pl.tajchert.paczko.fast.feature.parcels.impl

import pl.tajchert.paczko.fast.core.model.parcel.Parcel

internal fun parcelMetadataLines(parcel: Parcel): List<String> = buildList {
    if (parcel.isMultiPackage) {
        val shipments = parcel.multiPackageShipmentNumbers.joinToString(", ")
        add(if (shipments.isBlank()) "Multi-package" else "Multi-package: $shipments")
    }
    if (parcel.isSharedFromSomeone) {
        add("Shared package")
    }
}
