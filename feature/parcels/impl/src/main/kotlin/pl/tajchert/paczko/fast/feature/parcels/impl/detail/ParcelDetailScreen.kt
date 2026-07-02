package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.DeadlineCard
import pl.tajchert.paczko.fast.core.designsystem.component.DetailTopBar
import pl.tajchert.paczko.fast.core.designsystem.component.LockerCard
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastErrorState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastLoadingIndicator
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.component.PrimaryActionButton
import pl.tajchert.paczko.fast.core.designsystem.component.StatusChip
import pl.tajchert.paczko.fast.core.designsystem.component.TimelineEvent
import pl.tajchert.paczko.fast.core.designsystem.component.TrackingTimeline
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.core.ui.QrPanel
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelDetailRoute
import pl.tajchert.paczko.fast.feature.parcels.impl.formatShipmentNumber
import pl.tajchert.paczko.fast.feature.parcels.impl.formatTimelineTime
import pl.tajchert.paczko.fast.feature.parcels.impl.humanizeStatus
import pl.tajchert.paczko.fast.feature.parcels.impl.isPickedUp
import pl.tajchert.paczko.fast.feature.parcels.impl.isReadyForPickup
import pl.tajchert.paczko.fast.feature.parcels.impl.list.previewParcels
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelMetadataLines
import pl.tajchert.paczko.fast.feature.parcels.impl.pickupCountdown

/**
 * Parcel detail screen ("2c Parcel detail — Black Amber"): status chip,
 * deadline countdown, QR panel, remote-open action, locker card and
 * tracking timeline.
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
private fun ParcelDetailContent(
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
    onCollect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val countdown = pickupCountdown(parcel)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Status + shipment identity
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StatusChip(text = humanizeStatus(parcel.status))
            Text(
                text = "Parcel",
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
            parcelMetadataLines(parcel).forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.accentText,
                )
            }
        }

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
            PrimaryActionButton(
                text = "Open box remotely",
                onClick = onCollect,
            )
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

        val timeline = trackingEvents(parcel)
        if (timeline.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
                Text(
                    text = "TRACKING",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaczkofastTheme.colors.textMuted,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
                TrackingTimeline(events = timeline)
            }
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

internal class ParcelDetailPreviewProvider : PreviewParameterProvider<ParcelDetailUiState> {
    override val values: Sequence<ParcelDetailUiState> = sequenceOf(
        ParcelDetailUiState(isLoading = false, parcel = previewParcels.first()),
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
