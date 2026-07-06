package pl.tajchert.paczko.fast.feature.parcels.impl.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.android.tools.screenshot.PreviewTest
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.ParcelDetailContent
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.ParcelDetailUiState

/**
 * Screenshots the parcel detail screen in its ready-to-pickup and delivered
 * states.
 *
 * Deliberately NOT [pl.tajchert.paczko.fast.feature.parcels.impl.detail.ParcelDetailPreviewProvider]
 * (the file's existing `@Preview` data): it builds `expiryDate`/`storedDate`
 * via `OffsetDateTime.now().plus/minusHours(...)`, which would drift between
 * the separate `updateDebugScreenshotTest` (record) and
 * `validateDebugScreenshotTest` (verify) Gradle invocations — see
 * `ParcelListScreenshotTest.kt` for the full explanation of why that fails.
 *
 * The ready-to-pickup fixture below uses `expiryDate = null`, so
 * [pl.tajchert.paczko.fast.feature.parcels.impl.pickupCountdown] short-circuits
 * to `null` and the deadline/progress row never renders — no wall-clock
 * dependency. The delivered fixture uses fixed absolute ISO instants (not
 * `now()`) for `storedDate`/`pickUpDate`; [pl.tajchert.paczko.fast.feature.parcels.impl.pickupWaitLabel]
 * and [pl.tajchert.paczko.fast.feature.parcels.impl.formatTimelineTime] only
 * take a `Duration.between`/format of those two fixed instants, so the
 * rendered "Picked up in ..." text is identical on every run.
 *
 * All values are obviously fake per the repo's public-safety rules: all-zero
 * shipment numbers, `WAW01A`, "Example ..." sender/address placeholders.
 */
private val screenshotPickupPoint = PickupPoint(
    name = "WAW01A",
    locationDescription = "Open 24/7",
    addressLine = "Example street 12, 00-000 Example City",
    latitude = 52.2402,
    longitude = 20.9319,
)

private val readyToPickupPreviewParcel = Parcel(
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
    parcelSize = "B",
)

private val deliveredPreviewParcel = Parcel(
    shipmentNumber = "000000000000000000000002",
    status = "delivered",
    statusGroup = "delivered",
    openCode = "000000",
    qrCode = "DEMO|000000000000000000000002",
    pickupPoint = screenshotPickupPoint,
    // Fixed absolute instants (not now()) — see the class-level KDoc above.
    expiryDate = null,
    storedDate = "2024-01-01T08:00:00+01:00",
    pickUpDate = "2024-01-02T10:00:00+01:00",
    operations = ParcelOperations(collect = false),
    senderName = "Example Sender sp. z o.o.",
    parcelSize = "B",
)

private class ParcelDetailScreenshotPreviewProvider : PreviewParameterProvider<ParcelDetailUiState> {
    override val values: Sequence<ParcelDetailUiState> = sequenceOf(
        ParcelDetailUiState(isLoading = false, parcel = readyToPickupPreviewParcel),
        ParcelDetailUiState(isLoading = false, parcel = deliveredPreviewParcel),
    )
}

@PreviewTest
@PaczkofastPreviews
@Composable
private fun ParcelDetailScreenshot(
    @PreviewParameter(ParcelDetailScreenshotPreviewProvider::class) uiState: ParcelDetailUiState,
) {
    PaczkofastTheme {
        ParcelDetailContent(
            uiState = uiState,
            onBack = {},
            onCollect = {},
        )
    }
}
