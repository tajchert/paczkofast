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

    // Declared before `parcels` deliberately: Kotlin initialises object properties
    // top-to-bottom, so `parcels` would capture a null pickup point if this came later.
    private val demoPickupPoint = PickupPoint(
        name = "WAW01A",
        locationDescription = "Near Example Store",
        addressLine = "Example street 12, 00-000 Example City",
        latitude = 52.2402,
        longitude = 20.9319,
    )

    private val parcelSizes = listOf("A", "B", "C", "D", "E", "F")

    private val historySenders = listOf(
        "Example Bakery",
        "Example Bookshop",
        "Example Camera",
        "Example Craft Lab",
        "Example Denim",
        "Example Florist",
        "Example Games",
        "Example Hardware",
        "Example Instruments",
        "Example Jewelry",
        "Example Kitchen",
        "Example Lighting",
        "Example Music",
        "Example Notebook",
        "Example Outdoors",
        "Example Print House",
        "Example Quality Goods",
        "Example Running",
        "Example Stationery",
        "Example Tea",
        "Example Urban Wear",
        "Example Vintage",
        "Example Wellness",
        "Example Yoga",
        "Example Zero Waste",
        "Example Audio",
        "Example Baby",
        "Example Camping",
        "Example Design",
        "Example Eyewear",
        "Example Fitness",
        "Example Grocery",
        "Example Hobby",
        "Example Imports",
    )

    val parcels: List<Parcel> = listOf(
        readyParcel(READY_SUCCESS, senderName = "Example Sender sp. z o.o.", expiryHours = 46),
        readyParcel(READY_SESSION_EXPIRED, senderName = "Example Books Ltd.", expiryHours = 4),
        readyParcel(READY_BOX_OFFLINE, senderName = "Example Pharmacy", expiryHours = 9),
        readyParcel(READY_SLOW_CLOSE, senderName = "Example Electronics", expiryHours = 22),
        readyParcel(READY_POST_OPEN_FAIL, senderName = "Example Fashion", expiryHours = 72),
        readyParcel(
            MULTI_A,
            senderName = "Example Marketplace",
            expiryHours = 35,
            multiCompartmentUuid = MULTI_UUID,
            multiPackageShipmentNumbers = listOf(MULTI_A, MULTI_B),
        ),
        readyParcel(
            MULTI_B,
            senderName = "Example Marketplace",
            expiryHours = 35,
            multiCompartmentUuid = MULTI_UUID,
        ),
        inTransitParcel(IN_TRANSIT, senderName = "Example Toys"),
        deliveredParcel(DELIVERED, senderName = "Example Garden"),
        expiredParcel(EXPIRED, senderName = "Example Sports"),
    ) + transitParcels() + historyParcels()

    private fun transitParcels(): List<Parcel> = listOf(
        transitParcel(11, senderName = "Example Coffee Roasters", status = "created", size = "A"),
        transitParcel(12, senderName = "Example Home", status = "dispatched_by_sender", size = "B"),
        transitParcel(13, senderName = "Example Cosmetics", status = "adopted_at_source_branch", size = "C"),
        transitParcel(14, senderName = "Example Bike Parts", status = "sent_from_sorting_center", size = "B"),
        transitParcel(15, senderName = "Example Pet Store", status = "adopted_at_target_branch", size = "A"),
        transitParcel(16, senderName = "Example Office", status = "out_for_delivery", size = "C"),
    )

    private fun historyParcels(): List<Parcel> = historySenders.mapIndexed { index, senderName ->
        val sequence = index + 17
        val daysAgo = 3L + index * 5L
        val size = parcelSizes[index % parcelSizes.size]
        when (index % 5) {
            0 -> claimedHistoryParcel(sequence, senderName, daysAgo, size)
            1 -> returnedHistoryParcel(sequence, senderName, daysAgo, size)
            2 -> expiredHistoryParcel(sequence, senderName, daysAgo, size)
            3 -> canceledHistoryParcel(sequence, senderName, daysAgo, size)
            else -> undeliveredHistoryParcel(sequence, senderName, daysAgo, size)
        }
    }

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

    private fun readyParcel(
        shipmentNumber: String,
        senderName: String,
        expiryHours: Long,
        multiCompartmentUuid: String? = null,
        multiPackageShipmentNumbers: List<String> = emptyList(),
    ) = Parcel(
        shipmentNumber = shipmentNumber,
        status = "ready_to_pickup",
        statusGroup = "ready",
        openCode = "000000",
        qrCode = "DEMO|$shipmentNumber",
        pickupPoint = demoPickupPoint,
        expiryDate = hoursFromNow(expiryHours),
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

    private fun transitParcel(sequence: Int, senderName: String, status: String, size: String) = Parcel(
        shipmentNumber = demoShipmentNumber(sequence),
        status = status,
        statusGroup = "other",
        openCode = null,
        qrCode = null,
        pickupPoint = demoPickupPoint,
        expiryDate = null,
        storedDate = daysAgo((sequence % 4 + 1).toLong()),
        operations = ParcelOperations(collect = false),
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = size,
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

    private fun claimedHistoryParcel(
        sequence: Int,
        senderName: String,
        completionDaysAgo: Long,
        size: String,
    ) = Parcel(
        shipmentNumber = demoShipmentNumber(sequence),
        status = "claimed",
        statusGroup = "delivered",
        openCode = null,
        qrCode = null,
        pickupPoint = demoPickupPoint,
        expiryDate = null,
        storedDate = daysAgo(completionDaysAgo + 2),
        operations = ParcelOperations(collect = false),
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = size,
        pickUpDate = daysAgo(completionDaysAgo),
    )

    private fun returnedHistoryParcel(
        sequence: Int,
        senderName: String,
        completionDaysAgo: Long,
        size: String,
    ) = Parcel(
        shipmentNumber = demoShipmentNumber(sequence),
        status = "returned_to_sender",
        statusGroup = "other",
        openCode = null,
        qrCode = null,
        pickupPoint = demoPickupPoint,
        expiryDate = null,
        storedDate = daysAgo(completionDaysAgo + 4),
        operations = ParcelOperations(collect = false),
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = size,
        returnedToSenderDate = daysAgo(completionDaysAgo),
    )

    private fun expiredHistoryParcel(
        sequence: Int,
        senderName: String,
        completionDaysAgo: Long,
        size: String,
    ) = Parcel(
        shipmentNumber = demoShipmentNumber(sequence),
        status = "pickup_time_expired",
        statusGroup = "other",
        openCode = null,
        qrCode = null,
        pickupPoint = demoPickupPoint,
        expiryDate = daysAgo(completionDaysAgo),
        storedDate = daysAgo(completionDaysAgo + 3),
        operations = ParcelOperations(collect = false),
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = size,
    )

    private fun canceledHistoryParcel(
        sequence: Int,
        senderName: String,
        completionDaysAgo: Long,
        size: String,
    ) = Parcel(
        shipmentNumber = demoShipmentNumber(sequence),
        status = "canceled",
        statusGroup = "other",
        openCode = null,
        qrCode = null,
        pickupPoint = demoPickupPoint,
        expiryDate = null,
        storedDate = daysAgo(completionDaysAgo),
        operations = ParcelOperations(collect = false),
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = size,
    )

    private fun undeliveredHistoryParcel(
        sequence: Int,
        senderName: String,
        completionDaysAgo: Long,
        size: String,
    ) = Parcel(
        shipmentNumber = demoShipmentNumber(sequence),
        status = "undelivered",
        statusGroup = "other",
        openCode = null,
        qrCode = null,
        pickupPoint = demoPickupPoint,
        expiryDate = null,
        storedDate = daysAgo(completionDaysAgo),
        operations = ParcelOperations(collect = false),
        ownershipStatus = "OWN",
        senderName = senderName,
        parcelSize = size,
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

    private fun demoShipmentNumber(sequence: Int): String = sequence.toString().padStart(24, '0')
    private fun hoursFromNow(h: Long) = OffsetDateTime.now().plusHours(h).toString()
    private fun hoursAgo(h: Long) = OffsetDateTime.now().minusHours(h).toString()
    private fun daysAgo(d: Long) = OffsetDateTime.now().minusDays(d).toString()
}
