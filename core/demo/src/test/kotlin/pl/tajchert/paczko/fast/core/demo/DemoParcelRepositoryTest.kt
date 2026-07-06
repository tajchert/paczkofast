package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.OffsetDateTime

class DemoParcelRepositoryTest {

    private val repository = DemoParcelRepository()

    @Test
    fun `observeParcels emits the demo catalog with a collectable ready parcel`() = runTest {
        val parcels = repository.observeParcels().first()
        assertEquals(DemoData.parcels.size, parcels.size)
        assertTrue(parcels.any { it.canCollectRemotely })
        assertTrue(parcels.any { it.isMultiPackage })
        assertTrue(parcels.any { it.status == "claimed" }) // history present
        // Regression guard: the app's FINISHED_STATUSES (feature/parcels/impl
        // ParcelUiFormatters.kt) recognizes "pickup_time_expired", not "expired".
        // If this drifts, the expired demo parcel wrongly renders as active.
        val expired = parcels.first { it.shipmentNumber == DemoData.EXPIRED }
        assertEquals("pickup_time_expired", expired.status)
    }

    @Test
    fun `every parcel carries the demo pickup point`() = runTest {
        // Regression guard for object init-order: `parcels` must be declared AFTER
        // `demoPickupPoint`, or every parcel captures a null pickup point (and the UI
        // shows "Pickup point pending" with no locker name/distance on the collect screen).
        val parcels = repository.observeParcels().first()
        assertTrue(parcels.all { it.pickupPoint != null })
        val point = parcels.first().pickupPoint!!
        assertEquals("WAW01A", point.name)
        assertNotNull(point.latitude)
        assertNotNull(point.longitude)
    }

    @Test
    fun `ready demo parcels have varied pickup deadlines`() = runTest {
        val now = OffsetDateTime.now()
        val remainingHours = repository.observeParcels().first()
            .filter { it.status == "ready_to_pickup" }
            .mapNotNull { parcel ->
                parcel.expiryDate?.let { Duration.between(now, OffsetDateTime.parse(it)).toHours() }
            }

        assertTrue(remainingHours.any { it < 12 })
        assertTrue(remainingHours.any { it >= 70 })
        assertTrue(remainingHours.distinct().size >= 4)
    }

    @Test
    fun `demo catalog includes several active in-transit parcels`() = runTest {
        val transitStatuses = setOf(
            "in_transit",
            "created",
            "dispatched_by_sender",
            "adopted_at_source_branch",
            "sent_from_sorting_center",
            "adopted_at_target_branch",
            "out_for_delivery",
        )

        val transitParcels = repository.observeParcels().first()
            .filter { it.status in transitStatuses }

        assertTrue(transitParcels.size >= 7)
    }

    @Test
    fun `demo catalog includes a full history list`() = runTest {
        val historyStatuses = setOf(
            "claimed",
            "returned_to_sender",
            "pickup_time_expired",
            "canceled",
            "undelivered",
        )

        val historyParcels = repository.observeParcels().first()
            .filter { it.status in historyStatuses }

        assertTrue(historyParcels.size in 30..40)
    }

    @Test
    fun `observeParcel finds a known parcel and returns null for unknown`() = runTest {
        assertNotNull(repository.observeParcel(DemoData.READY_SUCCESS).first())
        assertNull(repository.observeParcel("does-not-exist").first())
    }

    @Test
    fun `observeParcelDetails returns a timeline`() = runTest {
        val details = repository.observeParcelDetails(DemoData.READY_SUCCESS).first()
        assertTrue(details.events.isNotEmpty())
    }

    @Test
    fun `refresh is a no-op and does not throw`() = runTest {
        repository.refreshTrackedParcels()
        repository.refreshParcelDetails(DemoData.READY_SUCCESS)
    }
}
