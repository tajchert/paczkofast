package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.DeadlineCard
import pl.tajchert.paczko.fast.core.designsystem.component.DetailTopBar
import pl.tajchert.paczko.fast.core.designsystem.component.LockerCard
import pl.tajchert.paczko.fast.core.designsystem.component.OutlinedStatusChip
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastButton
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastCard
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastErrorState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastLoadingIndicator
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.component.StatusChip
import pl.tajchert.paczko.fast.core.designsystem.component.StatusChipStyle
import pl.tajchert.paczko.fast.core.designsystem.component.TimelineEvent
import pl.tajchert.paczko.fast.core.designsystem.component.TrackingTimeline
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabelLarge
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent
import pl.tajchert.paczko.fast.core.ui.QrPanel
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelDetailRoute
import pl.tajchert.paczko.fast.feature.parcels.impl.formatShipmentNumber
import pl.tajchert.paczko.fast.feature.parcels.impl.formatTimelineTime
import pl.tajchert.paczko.fast.feature.parcels.impl.humanizeStatus
import pl.tajchert.paczko.fast.feature.parcels.impl.isPickedUp
import pl.tajchert.paczko.fast.feature.parcels.impl.isReadyForPickup
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelMetadataLines
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelSizeLabel
import pl.tajchert.paczko.fast.feature.parcels.impl.pickupCountdown
import pl.tajchert.paczko.fast.feature.parcels.impl.pickupWaitLabel
import pl.tajchert.paczko.fast.feature.parcels.impl.trackingTimelineEvents

/**
 * Parcel detail screen (mocks "2a" ready-to-pickup / "4a" rich locker /
 * "3a" delivered): status chip row, deadline countdown, QR panel,
 * remote-open action, locker card and tracking timeline.
 */
@Composable
fun ParcelDetailScreen(
    route: ParcelDetailRoute,
    onBack: () -> Unit,
    onCollect: () -> Unit,
    viewModel: ParcelDetailViewModel = hiltViewModel<ParcelDetailViewModel, ParcelDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(route.shipmentNumber) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ParcelDetailContent(
        uiState = uiState,
        onBack = onBack,
        onCollect = onCollect,
    )
}

@Composable
internal fun ParcelDetailContent(
    uiState: ParcelDetailUiState,
    onBack: () -> Unit,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        topBar = {
            DetailTopBar(
                title = "Parcel details",
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> PaczkofastLoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            uiState.errorMessage != null -> PaczkofastErrorState(
                message = uiState.errorMessage,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            uiState.parcel == null -> PaczkofastErrorState(
                message = "Parcel not found",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            else -> ParcelDetailBody(
                parcel = uiState.parcel,
                events = uiState.events,
                sizeCode = uiState.sizeCode,
                senderName = uiState.senderName,
                shipmentType = uiState.shipmentType,
                onCollect = onCollect,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun ParcelDetailBody(
    parcel: Parcel,
    events: List<TrackingEvent>,
    sizeCode: String?,
    senderName: String?,
    shipmentType: String?,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val countdown = pickupCountdown(parcel)
    // Prefer the cached list values (shown instantly); the detail fetch only
    // fills gaps when the cache predates sender/size support.
    val effectiveSender = senderName?.takeIf { it.isNotBlank() } ?: parcel.senderName
    val effectiveSizeCode = sizeCode ?: parcel.parcelSize
    val delivered = parcel.isPickedUp

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Status chip row + shipment identity
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusChip(
                    text = if (delivered) "Delivered" else humanizeStatus(parcel.status),
                    style = if (delivered) StatusChipStyle.Ink else StatusChipStyle.Accent,
                    textStyle = MonoLabelLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                parcelSizeLabel(effectiveSizeCode)?.let { sizeLabel ->
                    OutlinedStatusChip(text = "Size $sizeLabel", textStyle = MonoLabelLarge)
                }
            }
            Text(
                text = effectiveSender ?: "Parcel",
                style = MaterialTheme.typography.headlineSmall,
                color = PaczkofastTheme.colors.textPrimary,
            )
            Text(
                text = formatShipmentNumber(parcel.shipmentNumber),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                ),
                color = PaczkofastTheme.colors.textMuted,
            )
            shipmentType?.takeIf { it.isNotBlank() }?.let { type ->
                Text(
                    text = humanizeStatus(type),
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.accentText,
                )
            }
            parcelMetadataLines(parcel).forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.accentText,
                )
            }
        }

        if (delivered) {
            PickedUpSummaryCard(
                waitLabel = parcel.pickupWaitLabel(),
                timestamp = formatTimelineTime(parcel.pickUpDate),
            )
            CollapsedPickupCodeRow()
        } else {
            countdown?.let {
                DeadlineCard(
                    deadlineText = it.deadlineText,
                    countdownText = it.countdownText,
                    progress = it.progress,
                    urgent = it.urgent,
                )
            }

            parcel.qrCode?.takeIf(String::isNotBlank)?.let { qrCode ->
                QrPanel(
                    payload = qrCode,
                    code = parcel.openCode,
                    qrSize = 150,
                )
            }

            if (parcel.canCollectRemotely) {
                PaczkofastButton(
                    text = "Open box remotely",
                    onClick = onCollect,
                )
            }
        }

        parcel.pickupPoint?.let { point ->
            LockerCard(
                lockerName = "Locker ${point.name}",
                address = point.addressLine ?: point.name,
                note = point.locationDescription,
                onNavigate = navigateAction(point) { intent ->
                    context.startActivity(intent)
                },
            )
        }

        val timeline = if (events.isNotEmpty()) {
            trackingTimelineEvents(events)
        } else {
            trackingEvents(parcel)
        }
        if (timeline.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
                Text(
                    text = "TRACKING",
                    style = MonoLabel,
                    color = PaczkofastTheme.colors.monoLabel,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
                TrackingTimeline(events = timeline)
            }
        }
    }
}

/**
 * Delivered-state summary card: a yellow check tile, "Picked up in Xd Yh"
 * headline and a mono collection timestamp underneath.
 */
@Composable
private fun PickedUpSummaryCard(
    waitLabel: String?,
    timestamp: String?,
    modifier: Modifier = Modifier,
) {
    val colors = PaczkofastTheme.colors
    PaczkofastCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(colors.accent, RoundedCornerShape(11.dp))
                    .border(2.5.dp, colors.accentBorder, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = if (waitLabel != null) "Picked up in $waitLabel" else "Picked up",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.textPrimary,
                )
                timestamp?.let {
                    Text(text = it, style = MonoLabel, color = colors.textMuted)
                }
            }
        }
    }
}

/**
 * Collapsed row replacing the QR panel once a parcel has been delivered —
 * "Pickup code & QR / NO LONGER NEEDED" with a trailing chevron, no code
 * value shown since it can no longer be used.
 */
@Composable
private fun CollapsedPickupCodeRow(modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    PaczkofastCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colors.background, RoundedCornerShape(7.dp))
                    .border(2.5.dp, colors.borderStrong, RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(15.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pickup code & QR",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.textPrimary,
                )
                Text(text = "No longer needed", style = MonoLabel, color = colors.textMuted)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted,
            )
        }
    }
}

/**
 * Builds the geo intent action for the locker, or null when the pickup
 * point has no coordinates or address to navigate to.
 */
private fun navigateAction(
    point: PickupPoint,
    startActivity: (Intent) -> Unit,
): (() -> Unit)? {
    val uri = when {
        point.latitude != null && point.longitude != null ->
            "geo:${point.latitude},${point.longitude}?q=${point.latitude},${point.longitude}(${Uri.encode(point.name)})"

        !point.addressLine.isNullOrBlank() ->
            "geo:0,0?q=${Uri.encode(point.addressLine)}"

        else -> return null
    }
    return { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }
}

/**
 * Canonical delivery pipeline shown on the detail screen, newest stage
 * first. The InPost list API doesn't return the event history, so past
 * stages are derived from the current status (dates are only known for
 * the stage the parcel is in right now); stages the parcel hasn't reached
 * yet are rendered as upcoming.
 */
private val PIPELINE_STAGES = listOf(
    "Shipment created",
    "In transit",
    "Out for delivery",
    "Ready for pickup",
    "Picked up",
)

/** Index into [PIPELINE_STAGES] for the parcel's current status. */
private fun pipelineStage(parcel: Parcel): Int = when {
    parcel.isPickedUp -> 4
    parcel.isReadyForPickup -> 3
    else -> when (parcel.status.lowercase()) {
        "out_for_delivery", "adopted_at_target_branch" -> 2
        "created", "confirmed", "offers_prepared", "offer_selected", "dispatched_by_sender" -> 0
        else -> 1
    }
}

private fun trackingEvents(parcel: Parcel): List<TimelineEvent> {
    val currentStage = pipelineStage(parcel)
    return PIPELINE_STAGES.mapIndexed { stage, stageLabel ->
        val isCurrent = stage == currentStage
        TimelineEvent(
            // The current stage shows the real (humanized) status, which can
            // be more specific than the canonical stage name.
            label = when {
                isCurrent && currentStage == 3 -> "Ready for pickup"
                isCurrent -> humanizeStatus(parcel.status)
                else -> stageLabel
            },
            time = when {
                isCurrent && currentStage == 3 -> formatTimelineTime(parcel.storedDate)
                else -> null
            },
            isCurrent = isCurrent,
            isUpcoming = stage > currentStage,
        )
    }.reversed()
}

// -----------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------

// Sample shipment numbers/codes/addresses below are obviously-fake
// placeholders (never real InPost data), per the neo-brutalist redesign's
// PII policy for previews/mocks.
private val readyToPickupPreviewParcel = Parcel(
    shipmentNumber = "000000000000000000000000",
    status = "ready_to_pickup",
    statusGroup = "ready",
    openCode = "000000",
    qrCode = "P|000000|000000000000000000000000",
    pickupPoint = PickupPoint(
        name = "WAW01A",
        locationDescription = "Open 24/7",
        addressLine = "Example street 12, 00-000 Example City",
        latitude = 52.2402,
        longitude = 20.9319,
    ),
    expiryDate = java.time.OffsetDateTime.now().plusHours(46).toString(),
    storedDate = java.time.OffsetDateTime.now().minusHours(2).toString(),
    operations = ParcelOperations(collect = true),
    senderName = "Example Sender sp. z o.o.",
    parcelSize = "B",
)

private val deliveredPreviewParcel = Parcel(
    shipmentNumber = "000000000000000000000000",
    status = "delivered",
    statusGroup = "delivered",
    openCode = "000000",
    qrCode = "P|000000|000000000000000000000000",
    pickupPoint = PickupPoint(
        name = "WAW01A",
        locationDescription = null,
        addressLine = "Example street 12, 00-000 Example City",
        latitude = 52.2402,
        longitude = 20.9319,
    ),
    expiryDate = java.time.OffsetDateTime.now().minusHours(2).toString(),
    storedDate = java.time.OffsetDateTime.now().minusDays(2).minusHours(10).toString(),
    pickUpDate = java.time.OffsetDateTime.now().toString(),
    operations = ParcelOperations(collect = false),
    senderName = "Example Sender sp. z o.o.",
    parcelSize = "B",
)

internal class ParcelDetailPreviewProvider : PreviewParameterProvider<ParcelDetailUiState> {
    override val values: Sequence<ParcelDetailUiState> = sequenceOf(
        ParcelDetailUiState(isLoading = false, parcel = readyToPickupPreviewParcel),
        ParcelDetailUiState(isLoading = false, parcel = deliveredPreviewParcel),
    )
}

@PaczkofastPreviews
@Composable
private fun ParcelDetailContentPreview(
    @PreviewParameter(ParcelDetailPreviewProvider::class) uiState: ParcelDetailUiState,
) {
    PaczkofastTheme {
        ParcelDetailContent(
            uiState = uiState,
            onBack = {},
            onCollect = {},
        )
    }
}
