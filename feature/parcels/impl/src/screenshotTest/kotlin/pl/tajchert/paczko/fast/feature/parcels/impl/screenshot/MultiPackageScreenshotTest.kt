package pl.tajchert.paczko.fast.feature.parcels.impl.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.android.tools.screenshot.PreviewTest
import kotlinx.collections.immutable.persistentListOf
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.BoxMember
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.MultiPackageDetailContent
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.MultiPackageDetailUiState

/**
 * Screenshots the multi-package box detail screen in its ready-to-pickup and
 * delivered states.
 *
 * Deliberately NOT [pl.tajchert.paczko.fast.feature.parcels.impl.detail.MultiPackageDetailPreviewProvider]
 * (the file's existing `@Preview` data): its representative `Parcel` builds
 * `expiryDate`/`storedDate` via `OffsetDateTime.now().plus/minusHours(...)`,
 * which would drift between the separate `updateDebugScreenshotTest` (record)
 * and `validateDebugScreenshotTest` (verify) Gradle invocations — see
 * `ParcelListScreenshotTest.kt` for the full explanation of why that fails.
 *
 * Unlike the single-parcel detail screen, [MultiPackageDetailContent] never
 * calls `pickupCountdown` itself — the countdown/progress values are supplied
 * pre-computed on [MultiPackageDetailUiState]. The ready fixture below sets
 * `deadlineText`/`countdownText`/`progress` to `null` directly (so the
 * deadline row doesn't render, mirroring the detail screen's null-`expiryDate`
 * trick) and the representative `Parcel`'s own `expiryDate` is also left
 * `null` for hygiene. The delivered fixture uses fixed absolute ISO instants
 * (not `now()`) for `storedDate`/`pickUpDate`, which only feed a
 * `Duration.between`/format of two fixed instants — no wall-clock dependency.
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

private val screenshotBoxMembers = persistentListOf(
    BoxMember(
        shipmentNumber = "000000000000000000000001",
        title = "Example Sender sp. z o.o.",
        shipmentNumberLine = "0000 0000 0000 0000 0000 0001",
        sizeLabel = "S",
    ),
    BoxMember(
        shipmentNumber = "000000000000000000000002",
        title = "Example Merchant",
        shipmentNumberLine = "0000 0000 0000 0000 0000 0002",
        sizeLabel = "M",
    ),
)

private val readyBoxRepresentative = Parcel(
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
    multiPackageShipmentNumbers = listOf(
        "000000000000000000000001",
        "000000000000000000000002",
    ),
    senderName = "Example Sender sp. z o.o.",
    parcelSize = "A",
)

private val deliveredBoxRepresentative = readyBoxRepresentative.copy(
    status = "delivered",
    statusGroup = "delivered",
    // Fixed absolute instants (not now()) — see the class-level KDoc above.
    storedDate = "2024-01-01T08:00:00+01:00",
    pickUpDate = "2024-01-02T10:00:00+01:00",
    operations = ParcelOperations(collect = false),
)

private val readyBoxUiState = MultiPackageDetailUiState(
    isLoading = false,
    statusLabel = "Ready to pickup",
    members = screenshotBoxMembers,
    lockerLine = "Locker WAW01A · Example street 12",
    deadlineText = null,
    countdownText = null,
    progress = null,
    urgent = false,
    qrCode = readyBoxRepresentative.qrCode,
    openCode = readyBoxRepresentative.openCode,
    canCollect = true,
    representativeShipmentNumber = readyBoxRepresentative.shipmentNumber,
    representative = readyBoxRepresentative,
)

private val deliveredBoxUiState = readyBoxUiState.copy(
    statusLabel = "Delivered",
    deadlineText = null,
    countdownText = null,
    progress = null,
    canCollect = false,
    representative = deliveredBoxRepresentative,
)

private class MultiPackageScreenshotPreviewProvider : PreviewParameterProvider<MultiPackageDetailUiState> {
    override val values: Sequence<MultiPackageDetailUiState> = sequenceOf(
        readyBoxUiState,
        deliveredBoxUiState,
    )
}

@PreviewTest
@Preview(showBackground = true)
@Composable
private fun MultiPackageDetailScreenshot(
    @PreviewParameter(MultiPackageScreenshotPreviewProvider::class) uiState: MultiPackageDetailUiState,
) {
    PaczkofastTheme {
        MultiPackageDetailContent(
            uiState = uiState,
            onBack = {},
            onOpenParcel = {},
            onCollect = {},
        )
    }
}
