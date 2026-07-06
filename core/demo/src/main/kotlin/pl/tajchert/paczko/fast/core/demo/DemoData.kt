package pl.tajchert.paczko.fast.core.demo

import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent
import java.time.OffsetDateTime

/**
 * Fixed, obviously-fake demo data. Shipment numbers are stable keys the collect
 * scenario map (see CollectScenario) uses to pick a locker outcome.
 */
object DemoData {

    // Ready-for-pickup, collectable. Each maps to a locker scenario in Task 5.
    const val READY_SUCCESS = "000000000000000000000001"
    const val READY_SESSION_EXPIRED = "000000000000000000000002"
    const val READY_BOX_OFFLINE = "000000000000000000000003"
    const val READY_SLOW_CLOSE = "000000000000000000000004"
    const val READY_POST_OPEN_FAIL = "000000000000000000000005"

    // Multi-package box (two members sharing one compartment).
    const val MULTI_A = "000000000000000000000006"
    const val MULTI_B = "000000000000000000000007"
    const val MULTI_UUID = "demo-multibox-0001"

    // Non-collectable / historical.
    const val IN_TRANSIT = "000000000000000000000008"
    const val DELIVERED = "000000000000000000000009"
    const val EXPIRED = "000000000000000000000010"

    val parcels: List<Parcel> = listOf(
        readyParcel(READY_SUCCESS, senderName = "Example Sender sp. z o.o."),
        readyParcel(READY_SESSION_EXPIRED, senderName = "Example Books Ltd."),
        readyParcel(READY_BOX_OFFLINE, senderName = "Example Pharmacy"),
        readyParcel(READY_SLOW_CLOSE, senderName = "Example Electronics"),
        readyParcel(READY_POST_OPEN_FAIL, senderName = "Example Fashion"),
        readyParcel(
            MULTI_A,
            senderName = "Example Marketplace",
            multiCompartmentUuid = MULTI_UUID,
            multiPackageShipmentNumbers = listOf(MULTI_A, MULTI_B),
        ),
        readyParcel(
            MULTI_B,
            senderName = "Example Marketplace",
            multiCompartmentUuid = MULTI_UUID,
        ),
        inTransitParcel(IN_TRANSIT, senderName = "Example Toys"),
        deliveredParcel(DELIVERED, senderName = "Example Garden"),
        expiredParcel(EXPIRED, senderName = "Example Sports"),
    )

    fun detailsFor(shipmentNumber: String): ParcelDetails = ParcelDetails(
        events = listOf(
            TrackingEvent(status = "CREATED", date = daysAgo(3)),
            TrackingEvent(status = "SENT_FROM_SOURCE_BRANCH", date = daysAgo(2)),
            TrackingEvent(status = "OUT_FOR_DELIVERY", date = daysAgo(1)),
            TrackingEvent(status = "READY_TO_PICKUP", date = hoursAgo(4)),
        ),
        sizeCode = "A",
        senderName = parcels.firstOrNull { it.shipmentNumber == shipmentNumber }?.senderName,
        shipmentType = "parcel",
    )

    private val demoPickupPoint = PickupPoint(
        name = "WAW01A",
        locationDescription = "Near Example Store",
        addressLine = "Example street 12, 00-000 Example City",
        latitude = 52.2402,
        longitude = 20.9319,
    )

    private fun readyParcel(
        shipmentNumber: String,
        senderName: String,
        multiCompartmentUuid: String? = null,
        multiPackageShipmentNumbers: List<String> = emptyList(),
    ) = Parcel(
        shipmentNumber = shipmentNumber,
        status = "ready_to_pickup",
        statusGroup = "ready",
        openCode = "000000",
        qrCode = "DEMO|$shipmentNumber",
        pickupPoint = demoPickupPoint,
        expiryDate = hoursFromNow(46),
        storedDate = hoursAgo(26),
        operations = ParcelOperations(collect = true),
        multiCompartmentUuid = multiCompartmentUuid,
        multiPackageShipmentNumbers = multiPackageShipmentNumbers,
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = "A",
    )

    private fun inTransitParcel(shipmentNumber: String, senderName: String) = Parcel(
        shipmentNumber = shipmentNumber,
        status = "in_transit",
        statusGroup = "other",
        openCode = null,
        qrCode = null,
        pickupPoint = demoPickupPoint,
        expiryDate = null,
        storedDate = null,
        operations = ParcelOperations(collect = false),
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = "B",
    )

    private fun deliveredParcel(shipmentNumber: String, senderName: String) = Parcel(
        shipmentNumber = shipmentNumber,
        status = "claimed",
        statusGroup = "delivered",
        openCode = "000000",
        qrCode = null,
        pickupPoint = demoPickupPoint,
        expiryDate = null,
        storedDate = daysAgo(6),
        operations = ParcelOperations(collect = false),
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = "C",
        pickUpDate = daysAgo(5),
    )

    private fun expiredParcel(shipmentNumber: String, senderName: String) = Parcel(
        shipmentNumber = shipmentNumber,
        // "pickup_time_expired" is the status the app's FINISHED_STATUSES set
        // recognizes (see feature/parcels/impl ParcelUiFormatters.kt), so this
        // parcel lands in History instead of the active list.
        status = "pickup_time_expired",
        statusGroup = "other",
        openCode = null,
        qrCode = null,
        pickupPoint = demoPickupPoint,
        expiryDate = daysAgo(2),
        storedDate = daysAgo(9),
        operations = ParcelOperations(collect = false),
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = "A",
    )

    private fun hoursFromNow(h: Long) = OffsetDateTime.now().plusHours(h).toString()
    private fun hoursAgo(h: Long) = OffsetDateTime.now().minusHours(h).toString()
    private fun daysAgo(d: Long) = OffsetDateTime.now().minusDays(d).toString()
}
