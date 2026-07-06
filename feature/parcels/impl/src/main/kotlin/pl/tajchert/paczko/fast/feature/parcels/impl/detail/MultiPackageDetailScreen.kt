package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.tajchert.paczko.fast.core.designsystem.component.DeadlineCard
import pl.tajchert.paczko.fast.core.designsystem.component.DetailTopBar
import pl.tajchert.paczko.fast.core.designsystem.component.OutlinedStatusChip
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastButton
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastCard
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastLoadingIndicator
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.component.SizeBadge
import pl.tajchert.paczko.fast.core.designsystem.component.StatusChip
import pl.tajchert.paczko.fast.core.designsystem.component.StatusChipStyle
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.core.ui.QrPanel
import pl.tajchert.paczko.fast.feature.parcels.api.MultiPackageDetailRoute
import pl.tajchert.paczko.fast.feature.parcels.impl.R
import pl.tajchert.paczko.fast.feature.parcels.impl.formatTimelineTime
import pl.tajchert.paczko.fast.feature.parcels.impl.isPickedUp
import pl.tajchert.paczko.fast.feature.parcels.impl.pickupWaitLabel

/**
 * Multi-package box detail (mocks "2b" ready-to-pickup / "3b" delivered):
 * shared identity, a tappable member sublist, one deadline, one shared QR/code
 * and a single open action. Per-parcel tracking lives on each member's own
 * detail screen.
 */
@Composable
fun MultiPackageDetailScreen(
    route: MultiPackageDetailRoute,
    onBack: () -> Unit,
    onOpenParcel: (shipmentNumber: String) -> Unit,
    onCollect: (shipmentNumber: String) -> Unit,
    viewModel: MultiPackageDetailViewModel = hiltViewModel<MultiPackageDetailViewModel, MultiPackageDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(route.shipmentNumber) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MultiPackageDetailContent(
        uiState = uiState,
        onBack = onBack,
        onOpenParcel = onOpenParcel,
        onCollect = onCollect,
    )
}

@Composable
internal fun MultiPackageDetailContent(
    uiState: MultiPackageDetailUiState,
    onBack: () -> Unit,
    onOpenParcel: (String) -> Unit,
    onCollect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        topBar = { DetailTopBar(title = stringResource(R.string.box_details), onBack = onBack) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            PaczkofastLoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            return@Scaffold
        }
        val count = uiState.members.size
        val delivered = uiState.representative?.isPickedUp == true
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    uiState.statusLabel?.let {
                        StatusChip(
                            text = it,
                            style = if (delivered) StatusChipStyle.Ink else StatusChipStyle.Accent,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedStatusChip(text = stringResource(R.string.one_box_count, count))
                }
                Text(
                    text = pluralStringResource(R.plurals.parcel_count, count, count),
                    style = MaterialTheme.typography.headlineSmall,
                    color = PaczkofastTheme.colors.textPrimary,
                )
                uiState.lockerLine?.let {
                    Text(
                        text = it.uppercase(),
                        style = MonoLabel,
                        color = PaczkofastTheme.colors.monoLabel,
                    )
                }
            }

            if (delivered) {
                PickedUpSummaryCard(
                    waitLabel = uiState.representative?.pickupWaitLabel(),
                    timestamp = formatTimelineTime(uiState.representative?.pickUpDate),
                    count = count,
                )
            }

            MemberListCard(members = uiState.members, onOpenParcel = onOpenParcel)

            if (!delivered && uiState.deadlineText != null && uiState.progress != null) {
                DeadlineCard(
                    deadlineText = uiState.deadlineText,
                    countdownText = uiState.countdownText.orEmpty(),
                    progress = uiState.progress,
                    urgent = uiState.urgent,
                )
            }

            if (delivered) {
                if (!uiState.qrCode.isNullOrBlank() || !uiState.openCode.isNullOrBlank()) {
                    ExpandablePickupCodeRow(
                        qrCode = uiState.qrCode,
                        openCode = uiState.openCode,
                    )
                }
            } else {
                uiState.qrCode?.takeIf(String::isNotBlank)?.let { qr ->
                    QrPanel(payload = qr, code = uiState.openCode, qrSize = 150)
                }

                if (uiState.canCollect && uiState.representativeShipmentNumber != null) {
                    PaczkofastButton(
                        text = stringResource(R.string.one_code_opens_box),
                        onClick = { onCollect(uiState.representativeShipmentNumber) },
                    )
                }
            }

            Text(
                text = stringResource(R.string.tracking_per_parcel),
                style = MaterialTheme.typography.bodySmall,
                color = PaczkofastTheme.colors.textFaint,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

/** Parcel-list card: one tappable row per box member, ink dividers between. */
@Composable
private fun MemberListCard(
    members: ImmutableList<BoxMember>,
    onOpenParcel: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PaczkofastTheme.colors
    PaczkofastCard(modifier = modifier) {
        members.forEachIndexed { index, member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenParcel(member.shipmentNumber) }
                    .padding(vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(16.dp),
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = member.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = member.shipmentNumberLine,
                        style = MonoLabel,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                member.sizeLabel?.let { SizeBadge(size = it) }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = colors.textFaint,
                )
            }
            if (index != members.lastIndex) {
                HorizontalDivider(thickness = 2.5.dp, color = colors.borderStrong)
            }
        }
    }
}

/**
 * Delivered-state summary card: a yellow check tile, "Picked up in Xd Yh"
 * headline and a mono collection timestamp + member-count caption underneath.
 */
@Composable
private fun PickedUpSummaryCard(
    waitLabel: String?,
    timestamp: String?,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val colors = PaczkofastTheme.colors
    val countLabel = when {
        count <= 1 -> pluralStringResource(R.plurals.parcel_count, 1, 1).uppercase()
        else -> pluralStringResource(R.plurals.parcel_count, count, count).uppercase()
    }
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
                    text = if (waitLabel != null) {
                        stringResource(R.string.picked_up_in, waitLabel)
                    } else {
                        stringResource(R.string.picked_up)
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.textPrimary,
                )
                val caption = listOfNotNull(timestamp, countLabel).joinToString(" · ")
                if (caption.isNotBlank()) {
                    Text(text = caption, style = MonoLabel, color = colors.textMuted)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------

// Sample shipment numbers/codes/addresses below are obviously-fake
// placeholders (never real InPost data), per the neo-brutalist redesign's
// PII policy for previews/mocks.
private val readyBoxMembers = persistentListOf(
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
    qrCode = "P|000000|000000000000000000000001",
    pickupPoint = PickupPoint(
        name = "WAW01A",
        locationDescription = "Open 24/7",
        addressLine = "Example street 12, 00-000 Example City",
        latitude = 52.2402,
        longitude = 20.9319,
    ),
    expiryDate = java.time.OffsetDateTime.now().plusHours(30).toString(),
    storedDate = java.time.OffsetDateTime.now().minusHours(6).toString(),
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
    expiryDate = java.time.OffsetDateTime.now().minusHours(2).toString(),
    storedDate = java.time.OffsetDateTime.now().minusDays(1).minusHours(6).toString(),
    pickUpDate = java.time.OffsetDateTime.now().toString(),
    operations = ParcelOperations(collect = false),
)

private val readyBoxUiState = MultiPackageDetailUiState(
    isLoading = false,
    statusLabel = "Ready to pickup",
    members = readyBoxMembers,
    lockerLine = "Locker WAW01A · Example street 12",
    deadlineText = "Pick up by Sat 4 Jul, 14:20",
    countdownText = "30 h left",
    progress = 0.42f,
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

internal class MultiPackageDetailPreviewProvider : PreviewParameterProvider<MultiPackageDetailUiState> {
    override val values: Sequence<MultiPackageDetailUiState> = sequenceOf(
        readyBoxUiState,
        deliveredBoxUiState,
    )
}

@PaczkofastPreviews
@Composable
private fun MultiPackageDetailContentPreview(
    @PreviewParameter(MultiPackageDetailPreviewProvider::class) uiState: MultiPackageDetailUiState,
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
