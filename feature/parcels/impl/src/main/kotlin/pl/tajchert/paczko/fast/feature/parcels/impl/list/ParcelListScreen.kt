package pl.tajchert.paczko.fast.feature.parcels.impl.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.BottomNavDestination
import pl.tajchert.paczko.fast.core.designsystem.component.CollapsedReadyParcelCard
import pl.tajchert.paczko.fast.core.designsystem.component.HistoryParcelCard
import pl.tajchert.paczko.fast.core.designsystem.component.HomeHeader
import pl.tajchert.paczko.fast.core.designsystem.component.MultiPackageCard
import pl.tajchert.paczko.fast.core.designsystem.component.MultiPackageMember
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastBottomBar
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastEmptyState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastErrorState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.component.ReadyParcelCard
import pl.tajchert.paczko.fast.core.designsystem.component.SectionHeader
import pl.tajchert.paczko.fast.core.designsystem.component.TransitParcelCard
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
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
import pl.tajchert.paczko.fast.feature.parcels.impl.MultiPackageGroup
import pl.tajchert.paczko.fast.feature.parcels.impl.ReadyItem
import pl.tajchert.paczko.fast.feature.parcels.impl.groupReadyParcels
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
    onCollectClick: (shipmentNumber: String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ParcelListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    ParcelListContent(
        uiState = uiState,
        onParcelClick = onParcelClick,
        onCollectClick = onCollectClick,
        onRefreshClick = viewModel::refresh,
        onOpenSettings = onOpenSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParcelListContent(
    uiState: ParcelListUiState,
    onParcelClick: (shipmentNumber: String) -> Unit,
    onCollectClick: (shipmentNumber: String) -> Unit,
    onRefreshClick: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavDestination.Parcels) }
    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        topBar = {
            if (selectedTab == BottomNavDestination.History) {
                HomeHeader(title = "History", showLogo = false)
            } else {
                HomeHeader()
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
                if (uiState.parcels.isNotEmpty() && uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

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

                    selectedTab == BottomNavDestination.History ->
                        if (historyParcels.isNotEmpty()) {
                            HistoryList(
                                parcels = historyParcels,
                                onParcelClick = onParcelClick,
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
    onCollectClick: (shipmentNumber: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (ready, onTheWay) = remember(parcels) {
        parcels.partition { it.isReadyForPickup }
    }
    val readyItems = remember(ready) { groupReadyParcels(ready) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (ready.isNotEmpty()) {
            item(key = "ready-header") {
                SectionHeader(
                    label = "Ready for pickup",
                    count = ready.size,
                    highlighted = true,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            // Expand the first standalone parcel (deadline + action); collapse the rest.
            var expandedSingleUsed = false
            readyItems.forEach { readyItem ->
                when (readyItem) {
                    is ReadyItem.Multi -> {
                        val group = readyItem.group
                        item(key = "multi-${group.uuid}") {
                            MultiPackageGroupCard(
                                group = group,
                                onClick = { onParcelClick(group.representative.shipmentNumber) },
                                onCollectClick = { onCollectClick(group.representative.shipmentNumber) },
                            )
                        }
                    }

                    is ReadyItem.Single -> {
                        val parcel = readyItem.parcel
                        val expand = !expandedSingleUsed
                        expandedSingleUsed = true
                        item(key = parcel.shipmentNumber) {
                            if (expand) {
                                ExpandedReadyCard(
                                    parcel = parcel,
                                    onClick = { onParcelClick(parcel.shipmentNumber) },
                                    onCollectClick = { onCollectClick(parcel.shipmentNumber) },
                                )
                            } else {
                                CollapsedReadyCard(
                                    parcel = parcel,
                                    onClick = { onParcelClick(parcel.shipmentNumber) },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (onTheWay.isNotEmpty()) {
            item(key = "transit-header") {
                SectionHeader(
                    label = "On the way",
                    count = onTheWay.size,
                    modifier = Modifier.padding(top = 10.dp),
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
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    parcels: List<Parcel>,
    onParcelClick: (shipmentNumber: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentMonth = remember { YearMonth.now() }
    // Parcels arrive newest-first, so months fall out in descending order.
    val months = remember(parcels) {
        parcels.groupBy { historyMonthKey(it) }.toList()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        months.forEach { (month, monthParcels) ->
            val muted = month != null && month != currentMonth
            if (month != null) {
                item(key = "month-$month") {
                    MonthHeader(
                        label = historyMonthLabel(month),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            items(items = monthParcels, key = Parcel::shipmentNumber) { parcel ->
                HistoryParcelCard(
                    title = parcelTitle(parcel),
                    outcomeLine = historyOutcomeLine(parcel),
                    dateText = historyDateLabel(parcel),
                    outcome = historyOutcome(parcel),
                    muted = muted,
                    onClick = { onParcelClick(parcel.shipmentNumber) },
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = PaczkofastTheme.colors.textMuted,
        modifier = modifier.padding(horizontal = 4.dp),
    )
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

internal val previewParcels: List<Parcel> = listOf(
    previewParcel(
        shipmentNumber = "000000000000",
        status = "ready_to_pickup",
        statusGroup = "ready",
        openCode = "000000",
        qrCode = "P|000000|000000000000",
        collectable = true,
    ),
    previewParcel(
        shipmentNumber = "000000000000",
        status = "ready_to_pickup",
        statusGroup = "ready",
        openCode = "111222",
        qrCode = null,
        collectable = false,
    ),
    previewParcel(
        shipmentNumber = "000000000000",
        status = "adopted_at_sorting_center",
        statusGroup = "in_transit",
    ),
    previewParcel(
        shipmentNumber = "000000000000",
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
) = Parcel(
    shipmentNumber = shipmentNumber,
    status = status,
    statusGroup = statusGroup,
    openCode = openCode,
    qrCode = qrCode,
    pickupPoint = PickupPoint(
        name = "WAW04B",
        locationDescription = "By the Żabka store",
        addressLine = "Górczewska 12, 01-138 Warszawa",
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
            onCollectClick = {},
            onRefreshClick = {},
            onOpenSettings = {},
        )
    }
}
