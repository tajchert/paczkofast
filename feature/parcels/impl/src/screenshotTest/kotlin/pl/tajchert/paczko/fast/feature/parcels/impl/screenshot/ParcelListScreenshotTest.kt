package pl.tajchert.paczko.fast.feature.parcels.impl.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import kotlinx.collections.immutable.toImmutableList
import pl.tajchert.paczko.fast.core.designsystem.component.BottomNavDestination
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.feature.parcels.impl.list.ParcelListContent
import pl.tajchert.paczko.fast.feature.parcels.impl.list.ParcelListUiState

/**
 * Smoke test for Google's Compose Preview Screenshot Testing plugin
 * (`com.android.compose.screenshot`, alpha) on this module. Renders the parcel
 * list ("Parcels" tab) with one ready-for-pickup and one in-transit parcel.
 *
 * Deliberately NOT [pl.tajchert.paczko.fast.feature.parcels.impl.list.ParcelListPreviewProvider]
 * (the file's existing `@Preview` data) or `DemoData.parcels`: both compute
 * `expiryDate`/`storedDate` as `OffsetDateTime.now().plus/minusHours(...)` at
 * evaluation time. `updateDebugScreenshotTest` (record) and
 * `validateDebugScreenshotTest` (verify) run as separate Gradle invocations
 * minutes apart, so a `now()`-relative deadline would re-evaluate to a
 * different absolute instant each time — drifting the pickup-countdown text
 * and, more importantly, the continuous progress-bar fraction in
 * `DeadlineRow`, which would never validate identically twice. The fixture
 * below leaves `expiryDate`/`storedDate` null, so [pl.tajchert.paczko.fast.feature.parcels.impl.pickupCountdown]
 * short-circuits to `null` and the deadline/progress row never renders —
 * making this screenshot fully time-independent.
 *
 * All values are obviously fake per the repo's public-safety rules: all-zero
 * shipment numbers, `WAW01A`, "Example ..." sender/address placeholders.
 */
@PreviewTest
@PaczkofastPreviews
@Composable
private fun ParcelListScreenshot() {
    PaczkofastTheme {
        ParcelListContent(
            uiState = ParcelListUiState(parcels = screenshotParcels.toImmutableList()),
            selectedTab = BottomNavDestination.Parcels,
            isCurrentDestination = true,
            onSelectTab = {},
            onParcelClick = {},
            onOpenBox = {},
            onCollectClick = {},
            onRefreshClick = {},
            onOpenSettings = {},
            onErrorShown = {},
        )
    }
}

private val screenshotPickupPoint = PickupPoint(
    name = "WAW01A",
    locationDescription = "Near Example Store",
    addressLine = "Example street 12, 00-000 Example City",
    latitude = 52.2402,
    longitude = 20.9319,
)

private val screenshotParcels: List<Parcel> = listOf(
    Parcel(
        shipmentNumber = "000000000000000000000001",
        status = "ready_to_pickup",
        statusGroup = "ready",
        openCode = "000000",
        qrCode = "DEMO|000000000000000000000001",
        pickupPoint = screenshotPickupPoint,
        // No expiryDate/storedDate on purpose — see the class-level KDoc above.
        expiryDate = null,
        storedDate = null,
        operations = ParcelOperations(collect = true),
        senderName = "Example Sender sp. z o.o.",
        parcelSize = "A",
    ),
    Parcel(
        shipmentNumber = "000000000000000000000002",
        status = "adopted_at_sorting_center",
        statusGroup = "in_transit",
        openCode = null,
        qrCode = null,
        pickupPoint = screenshotPickupPoint,
        expiryDate = null,
        storedDate = null,
        operations = ParcelOperations(collect = false),
        senderName = "Example Merchant",
        parcelSize = "B",
    ),
)
