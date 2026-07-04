package pl.tajchert.paczko.fast.feature.parcels.impl.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.BottomNavDestination
import pl.tajchert.paczko.fast.core.designsystem.component.CollapsedReadyParcelCard
import pl.tajchert.paczko.fast.core.designsystem.component.HeaderIconButton
import pl.tajchert.paczko.fast.core.designsystem.component.HistoryParcelCard
import pl.tajchert.paczko.fast.core.designsystem.component.HomeHeader
import pl.tajchert.paczko.fast.core.designsystem.component.MultiPackageCard
import pl.tajchert.paczko.fast.core.designsystem.component.MultiPackageHistoryCard
import pl.tajchert.paczko.fast.core.designsystem.component.MultiPackageMember
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastBottomBar
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastEmptyState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastErrorState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastLoadingIndicator
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.component.ReadyParcelCard
import pl.tajchert.paczko.fast.core.designsystem.component.TransitParcelCard
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.designsystem.theme.SpaceGroteskFamily
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.feature.parcels.impl.TRANSIT_SEGMENTS
import pl.tajchert.paczko.fast.feature.parcels.impl.formatShipmentNumber
import pl.tajchert.paczko.fast.feature.parcels.impl.historyMonthKey
import pl.tajchert.paczko.fast.feature.parcels.impl.historyMonthLabel
import pl.tajchert.paczko.fast.feature.parcels.impl.historyOutcome
import pl.tajchert.paczko.fast.feature.parcels.impl.historyOutcomeLine
import pl.tajchert.paczko.fast.feature.parcels.impl.historyDateLabel
import pl.tajchert.paczko.fast.feature.parcels.impl.historySortKey
import pl.tajchert.paczko.fast.feature.parcels.impl.humanizeStatus
import pl.tajchert.paczko.fast.feature.parcels.impl.isFinished
import pl.tajchert.paczko.fast.feature.parcels.impl.isReadyForPickup
import pl.tajchert.paczko.fast.feature.parcels.impl.CompartmentItem
import pl.tajchert.paczko.fast.feature.parcels.impl.MultiPackageGroup
import pl.tajchert.paczko.fast.feature.parcels.impl.groupByCompartment
import pl.tajchert.paczko.fast.feature.parcels.impl.lockerLine
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelSizeLabel
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelTitle
import pl.tajchert.paczko.fast.feature.parcels.impl.pickupCountdown
import pl.tajchert.paczko.fast.feature.parcels.impl.transitCompletedSegments
import java.time.YearMonth

/**
 * Home screen ("2a Home — Black Amber"): ready-for-pickup parcels on top —
 * the first one expanded with its QR code — followed by parcels on the way.
 */
@Composable
fun ParcelListScreen(
    onParcelClick: (shipmentNumber: String) -> Unit,
    onOpenBox: (shipmentNumber: String) -> Unit,
    onCollectClick: (shipmentNumber: String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ParcelListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ParcelListContent(
        uiState = uiState,
        onParcelClick = onParcelClick,
        onOpenBox = onOpenBox,
        onCollectClick = onCollectClick,
        onRefreshClick = viewModel::refresh,
        onOpenSettings = onOpenSettings,
        onErrorShown = viewModel::onErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParcelListContent(
    uiState: ParcelListUiState,
    onParcelClick: (shipmentNumber: String) -> Unit,
    onOpenBox: (shipmentNumber: String) -> Unit,
    onCollectClick: (shipmentNumber: String) -> Unit,
    onRefreshClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavDestination.Parcels) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Surface refresh failures as a self-dismissing snackbar, but only when we
    // still have cached parcels to show; an empty cache uses the full-screen
    // error state below instead.
    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null && uiState.parcels.isNotEmpty()) {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selectedTab == BottomNavDestination.History) {
                HomeHeader(title = "History", showLogo = false)
            } else {
                HomeHeader(
                    actions = {
                        HeaderIconButton(
                            onClick = onRefreshClick,
                            icon = Icons.Default.Refresh,
                            contentDescription = "Refresh parcels",
                        )
                    },
                )
            }
        },
        bottomBar = {
            PaczkofastBottomBar(
                selected = selectedTab,
                onSelect = { destination ->
                    if (destination == BottomNavDestination.Settings) {
                        onOpenSettings()
                    } else {
                        selectedTab = destination
                    }
                },
            )
        },
    ) { paddingValues ->
        val refreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefreshClick,
            state = refreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = refreshState,
                    isRefreshing = uiState.isRefreshing,
                    containerColor = PaczkofastTheme.colors.cardSurface,
                    color = PaczkofastTheme.colors.accent,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val activeParcels = remember(uiState.parcels) {
                    uiState.parcels.filter { !it.isFinished }
                }
                val historyParcels = remember(uiState.parcels) {
                    uiState.parcels
                        .filter { it.isFinished }
                        .sortedByDescending { it.historySortKey() }
                }

                when {
                    uiState.parcels.isEmpty() && uiState.errorMessage != null ->
                        PaczkofastErrorState(
                            message = uiState.errorMessage,
                            onRetry = onRefreshClick,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        )

                    uiState.isLoading ->
                        PaczkofastLoadingIndicator(modifier = Modifier.fillMaxSize())

                    selectedTab == BottomNavDestination.History ->
                        if (historyParcels.isNotEmpty()) {
                            HistoryList(
                                parcels = historyParcels,
                                onParcelClick = onParcelClick,
                                onOpenBox = onOpenBox,
                            )
                        } else {
                            PaczkofastEmptyState(
                                icon = Icons.Outlined.History,
                                title = "No history yet",
                                description = "Delivered and collected parcels will appear here.",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                            )
                        }

                    else ->
                        if (activeParcels.isNotEmpty()) {
                            ParcelSections(
                                parcels = activeParcels,
                                onParcelClick = onParcelClick,
                                onOpenBox = onOpenBox,
                                onCollectClick = onCollectClick,
                            )
                        } else {
                            PaczkofastEmptyState(
                                icon = Icons.Outlined.Inbox,
                                title = "Nothing incoming",
                                description = "Parcels on the way or ready for pickup will appear here.",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun ParcelSections(
    parcels: List<Parcel>,
    onParcelClick: (shipmentNumber: String) -> Unit,
    onOpenBox: (shipmentNumber: String) -> Unit,
    onCollectClick: (shipmentNumber: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (ready, onTheWay) = remember(parcels) {
        parcels.partition { it.isReadyForPickup }
    }
    val readyItems = remember(ready) { groupByCompartment(ready) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (ready.isNotEmpty()) {
            item(key = "ready-header") {
                ListSectionHeader(
                    text = "Ready for pickup",
                    modifier = Modifier.animateItem().padding(top = 6.dp),
                )
            }
            // Expand the first standalone parcel (deadline + action); collapse the rest.
            var expandedSingleUsed = false
            readyItems.forEach { readyItem ->
                when (readyItem) {
                    is CompartmentItem.Multi -> {
                        val group = readyItem.group
                        item(key = "multi-${group.uuid}") {
                            MultiPackageGroupCard(
                                group = group,
                                onClick = { onOpenBox(group.representative.shipmentNumber) },
                                onCollectClick = { onCollectClick(group.representative.shipmentNumber) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }

                    is CompartmentItem.Single -> {
                        val parcel = readyItem.parcel
                        val expand = !expandedSingleUsed
                        expandedSingleUsed = true
                        item(key = parcel.shipmentNumber) {
                            if (expand) {
                                ExpandedReadyCard(
                                    parcel = parcel,
                                    onClick = { onParcelClick(parcel.shipmentNumber) },
                                    onCollectClick = { onCollectClick(parcel.shipmentNumber) },
                                    modifier = Modifier.animateItem(),
                                )
                            } else {
                                CollapsedReadyCard(
                                    parcel = parcel,
                                    onClick = { onParcelClick(parcel.shipmentNumber) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (onTheWay.isNotEmpty()) {
            item(key = "transit-header") {
                ListSectionHeader(
                    text = "On the way",
                    modifier = Modifier.animateItem().padding(top = 10.dp),
                )
            }
            items(items = onTheWay, key = Parcel::shipmentNumber) { parcel ->
                TransitParcelCard(
                    title = parcelTitle(parcel),
                    statusText = humanizeStatus(parcel.status),
                    sizeLabel = parcelSizeLabel(parcel.parcelSize),
                    completedSegments = transitCompletedSegments(parcel.status),
                    totalSegments = TRANSIT_SEGMENTS,
                    onClick = { onParcelClick(parcel.shipmentNumber) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    parcels: List<Parcel>,
    onParcelClick: (shipmentNumber: String) -> Unit,
    onOpenBox: (shipmentNumber: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentMonth = remember { YearMonth.now() }
    // Parcels arrive newest-first; collapse multi-package siblings into one row,
    // then months fall out in descending order.
    val months = remember(parcels) {
        groupByCompartment(parcels).groupBy { historyMonthKey(it.anchor) }.toList()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        months.forEach { (month, monthItems) ->
            val muted = month != null && month != currentMonth
            if (month != null) {
                item(key = "month-$month") {
                    MonthHeader(
                        label = historyMonthLabel(month),
                        modifier = Modifier.animateItem().padding(top = 6.dp),
                    )
                }
            }
            items(items = monthItems, key = { it.anchor.shipmentNumber }) { historyItem ->
                when (historyItem) {
                    is CompartmentItem.Multi -> {
                        val group = historyItem.group
                        val anchor = group.representative
                        MultiPackageHistoryCard(
                            count = group.members.size,
                            outcomeLine = historyOutcomeLine(anchor),
                            dateText = historyDateLabel(anchor),
                            members = group.members.map { member ->
                                MultiPackageMember(
                                    title = parcelTitle(member),
                                    sizeLabel = parcelSizeLabel(member.parcelSize),
                                )
                            },
                            onClick = { onOpenBox(anchor.shipmentNumber) },
                            modifier = Modifier.animateItem(),
                        )
                    }

                    is CompartmentItem.Single -> {
                        val parcel = historyItem.parcel
                        HistoryParcelCard(
                            title = parcelTitle(parcel),
                            outcomeLine = historyOutcomeLine(parcel),
                            dateText = historyDateLabel(parcel),
                            outcome = historyOutcome(parcel),
                            muted = muted,
                            onClick = { onParcelClick(parcel.shipmentNumber) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

/** The parcel a history row is keyed/dated by — the group's representative. */
private val CompartmentItem.anchor: Parcel
    get() = when (this) {
        is CompartmentItem.Single -> parcel
        is CompartmentItem.Multi -> group.representative
    }

@Composable
private fun MonthHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label.uppercase(),
        style = MonoLabel,
        color = PaczkofastTheme.colors.monoLabel,
        modifier = modifier.padding(horizontal = 4.dp),
    )
}

/** Bold Space-Grotesk headline used for the Packages tab's "Ready for
 * pickup" / "On the way" group headers, with a small ink-bordered yellow
 * square bullet (mock "1c") — distinct from the mono [SectionHeader] used
 * elsewhere.
 */
private val ListSectionHeaderTextStyle = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 20.sp,
)

@Composable
private fun ListSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(PaczkofastTheme.colors.accent, RoundedCornerShape(3.dp))
                .border(2.dp, PaczkofastTheme.colors.borderStrong, RoundedCornerShape(3.dp)),
        )
        Text(
            text = text,
            style = ListSectionHeaderTextStyle,
            color = PaczkofastTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun MultiPackageGroupCard(
    group: MultiPackageGroup,
    onClick: () -> Unit,
    onCollectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val representative = group.representative
    val countdown = pickupCountdown(representative)
    MultiPackageCard(
        title = group.members.joinToString(" + ") { parcelTitle(it) },
        subtitle = lockerLine(representative),
        members = group.members.map { member ->
            MultiPackageMember(
                title = parcelTitle(member),
                sizeLabel = parcelSizeLabel(member.parcelSize),
            )
        },
        deadlineText = countdown?.deadlineText,
        timeLeftText = countdown?.timeLeftText,
        progress = countdown?.progress,
        urgent = countdown?.urgent == true,
        onClick = onClick,
        onActionClick = onCollectClick,
        modifier = modifier,
    )
}

@Composable
private fun ExpandedReadyCard(
    parcel: Parcel,
    onClick: () -> Unit,
    onCollectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val countdown = pickupCountdown(parcel)
    ReadyParcelCard(
        title = parcelTitle(parcel),
        subtitle = lockerLine(parcel),
        deadlineText = countdown?.deadlineText,
        timeLeftText = countdown?.timeLeftText,
        progress = countdown?.progress,
        urgent = countdown?.urgent == true,
        sizeLabel = parcelSizeLabel(parcel.parcelSize),
        qrContent = null,
        actionText = "Open box remotely".takeIf { parcel.canCollectRemotely },
        onActionClick = onCollectClick,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun CollapsedReadyCard(
    parcel: Parcel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val countdown = pickupCountdown(parcel)
    CollapsedReadyParcelCard(
        title = parcelTitle(parcel),
        subtitle = lockerLine(parcel),
        timeLeftText = countdown?.timeLeftText,
        progress = countdown?.progress,
        urgent = countdown?.urgent == true,
        onClick = onClick,
        modifier = modifier,
    )
}

// -----------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------

internal class ParcelListPreviewProvider : PreviewParameterProvider<ParcelListUiState> {
    override val values: Sequence<ParcelListUiState> = sequenceOf(
        ParcelListUiState(parcels = previewParcels),
    )
}

// All sample shipment numbers below are obviously-fake all-zero placeholders
// (never a real InPost tracking number), per the neo-brutalist redesign's
// PII policy for previews/mocks.
internal val previewParcels: List<Parcel> = listOf(
    previewParcel(
        shipmentNumber = "000000000000000000000001",
        status = "ready_to_pickup",
        statusGroup = "ready",
        openCode = "482913",
        qrCode = "P|482913|000000000000000000000001",
        collectable = true,
        senderName = "Example Shop",
    ),
    previewParcel(
        shipmentNumber = "000000000000000000000002",
        status = "ready_to_pickup",
        statusGroup = "ready",
        openCode = "111222",
        qrCode = null,
        collectable = false,
        senderName = "Example Sender sp. z o.o.",
    ),
    previewParcel(
        shipmentNumber = "000000000000000000000003",
        status = "adopted_at_sorting_center",
        statusGroup = "in_transit",
        senderName = "Example Merchant",
    ),
    previewParcel(
        shipmentNumber = "000000000000000000000004",
        status = "confirmed",
        statusGroup = "in_transit",
    ),
)

private fun previewParcel(
    shipmentNumber: String,
    status: String,
    statusGroup: String?,
    openCode: String? = null,
    qrCode: String? = null,
    collectable: Boolean = false,
    senderName: String? = null,
    parcelSize: String? = "A",
) = Parcel(
    shipmentNumber = shipmentNumber,
    status = status,
    statusGroup = statusGroup,
    openCode = openCode,
    qrCode = qrCode,
    senderName = senderName,
    parcelSize = parcelSize,
    pickupPoint = PickupPoint(
        name = "WAW01A",
        locationDescription = "Near Example Store",
        addressLine = "Example street 12, 00-000 Example City",
        latitude = 52.2402,
        longitude = 20.9319,
    ),
    expiryDate = java.time.OffsetDateTime.now().plusHours(46).toString(),
    storedDate = java.time.OffsetDateTime.now().minusHours(26).toString(),
    operations = ParcelOperations(collect = collectable),
)

@PaczkofastPreviews
@Composable
private fun ParcelListContentPreview(
    @PreviewParameter(ParcelListPreviewProvider::class) uiState: ParcelListUiState,
) {
    PaczkofastTheme {
        ParcelListContent(
            uiState = uiState,
            onParcelClick = {},
            onOpenBox = {},
            onCollectClick = {},
            onRefreshClick = {},
            onOpenSettings = {},
            onErrorShown = {},
        )
    }
}

// -----------------------------------------------------------------------------
// History tab preview — fake single history rows plus a multibox entry.
// -----------------------------------------------------------------------------

private fun previewHistoryParcel(
    shipmentNumber: String,
    status: String,
    senderName: String?,
    pickUpDate: java.time.OffsetDateTime? = null,
    returnedToSenderDate: java.time.OffsetDateTime? = null,
    parcelSize: String? = "A",
    multiCompartmentUuid: String? = null,
    multiPackageShipmentNumbers: List<String> = emptyList(),
) = Parcel(
    shipmentNumber = shipmentNumber,
    status = status,
    statusGroup = null,
    openCode = null,
    qrCode = null,
    senderName = senderName,
    parcelSize = parcelSize,
    multiCompartmentUuid = multiCompartmentUuid,
    multiPackageShipmentNumbers = multiPackageShipmentNumbers,
    pickupPoint = PickupPoint(
        name = "WAW01A",
        locationDescription = "Near Example Store",
        addressLine = "Example street 12, 00-000 Example City",
        latitude = 52.2402,
        longitude = 20.9319,
    ),
    expiryDate = null,
    storedDate = pickUpDate?.minusHours(30)?.toString(),
    pickUpDate = pickUpDate?.toString(),
    returnedToSenderDate = returnedToSenderDate?.toString(),
    operations = ParcelOperations(collect = false),
)

internal val previewHistoryParcels: List<Parcel> = run {
    val now = java.time.OffsetDateTime.now()
    listOf(
        previewHistoryParcel(
            shipmentNumber = "000000000000000000000011",
            status = "claimed",
            senderName = "Example Shop",
            pickUpDate = now.minusDays(2),
        ),
        previewHistoryParcel(
            shipmentNumber = "000000000000000000000012",
            status = "claimed",
            senderName = "Example Sender sp. z o.o.",
            pickUpDate = now.minusDays(6),
        ),
        // Multi-package group: two parcels sharing one locker compartment.
        previewHistoryParcel(
            shipmentNumber = "000000000000000000000021",
            status = "claimed",
            senderName = "Example Shop",
            pickUpDate = now.minusDays(33),
            parcelSize = "A",
            multiCompartmentUuid = "example-compartment-1",
            multiPackageShipmentNumbers = listOf(
                "000000000000000000000021",
                "000000000000000000000022",
            ),
        ),
        previewHistoryParcel(
            shipmentNumber = "000000000000000000000022",
            status = "claimed",
            senderName = "Example Sender sp. z o.o.",
            pickUpDate = now.minusDays(33),
            parcelSize = "C",
            multiCompartmentUuid = "example-compartment-1",
        ),
        previewHistoryParcel(
            shipmentNumber = "000000000000000000000023",
            status = "pickup_time_expired",
            senderName = "Example Merchant",
            returnedToSenderDate = now.minusDays(40),
        ),
    )
}

@PaczkofastPreviews
@Composable
private fun HistoryListPreview() {
    PaczkofastTheme {
        Column(modifier = Modifier.fillMaxSize().background(PaczkofastTheme.colors.background)) {
            HomeHeader(title = "History", showLogo = false)
            HistoryList(
                parcels = previewHistoryParcels.sortedByDescending { it.historySortKey() },
                onParcelClick = {},
                onOpenBox = {},
            )
        }
    }
}
