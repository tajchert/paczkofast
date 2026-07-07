package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.tajchert.paczko.fast.core.designsystem.component.CheckOffParcelRow
import pl.tajchert.paczko.fast.core.designsystem.component.DetailTopBar
import pl.tajchert.paczko.fast.core.designsystem.component.HeroBlobSize
import pl.tajchert.paczko.fast.core.designsystem.component.HoldBar
import pl.tajchert.paczko.fast.core.designsystem.component.HoldRing
import pl.tajchert.paczko.fast.core.designsystem.component.hardShadow
import pl.tajchert.paczko.fast.core.designsystem.component.OutlinedActionButton
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastCard
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.component.PrimaryActionButton
import pl.tajchert.paczko.fast.core.designsystem.component.SizeBadge
import pl.tajchert.paczko.fast.core.designsystem.component.rememberHoldToOpenState
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.collect.CollectState
import pl.tajchert.paczko.fast.feature.parcels.impl.R

@Composable
fun CollectScreen(
    shipmentNumber: String,
    onBack: () -> Unit,
    viewModel: CollectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentShipmentNumber by rememberUpdatedState(shipmentNumber)
    var permissionRequested by rememberSaveable(shipmentNumber) {
        mutableStateOf(false)
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.arm(currentShipmentNumber)
        } else {
            viewModel.onLocationPermissionDenied(currentShipmentNumber)
        }
    }

    LaunchedEffect(shipmentNumber) {
        if (hasLocationPermission(context)) {
            viewModel.arm(shipmentNumber)
        } else if (!permissionRequested) {
            permissionRequested = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    CollectContent(
        uiState = uiState,
        onConfirmed = { viewModel.start(currentShipmentNumber) },
        onBack = onBack,
    )
}

private val CollectState.isBoxOpen: Boolean
    get() = this is CollectState.Opened ||
        this is CollectState.WaitingForClosed ||
        this is CollectState.ConfirmingClosed ||
        this is CollectState.Claiming

/** True once the box has closed and we're confirming/claiming — drives "Finishing up…". */
private val CollectState.isFinishing: Boolean
    get() = this is CollectState.ConfirmingClosed || this is CollectState.Claiming

/**
 * Drives the fixed [CollectScaffold] from the pure [collectScreenModel] mapping. Every
 * state renders the same three zones (header / 216.dp hero / bottom action) so switching
 * idle → holding → box open → success/error — and live distance updates — never moves an
 * anchor. A single shared [rememberHoldToOpenState] feeds both the Hold action bar and the
 * Nearby "override with hold", so a hold fills the hero ring in either mode.
 */
@Composable
internal fun CollectContent(
    uiState: CollectUiState,
    onConfirmed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = uiState.state
    val lockerOpenedUnconfirmedMessage = stringResource(R.string.locker_opened_unconfirmed)
    // A failure after the box already opened: the parcel is collectable, so show
    // the collected screen with a snackbar instead of a full-screen error.
    val collectedButUnconfirmed = (state as? CollectState.Failed)?.takeIf { it.boxAlreadyOpen }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(collectedButUnconfirmed) {
        if (collectedButUnconfirmed != null) {
            snackbarHostState.showSnackbar(
                lockerOpenedUnconfirmedMessage,
            )
        }
    }

    // Single hold state shared by Hold mode and the Nearby override — a completed hold
    // fires start() and fills the hero ring in both modes. Only armable while idle.
    val holdState = rememberHoldToOpenState(
        enabled = state is CollectState.Idle,
        onConfirmed = onConfirmed,
    )

    val baseModel = collectScreenModel(state, uiState).localized(state, uiState)
    // Box-already-open failure is presented as a success (with the snackbar caveat above),
    // so reuse the Completed slots rather than the Error ones.
    val model = if (collectedButUnconfirmed != null) {
        baseModel.copy(
            header = stringResource(R.string.box_closed).uppercase(),
            hero = CollectHero.Check,
            headline = if (uiState.members.size > 1) {
                stringResource(R.string.all_picked_up)
            } else {
                stringResource(R.string.picked_up_exclamation)
            },
            subline = null,
            action = CollectAction.BackOnly,
        )
    } else {
        baseModel
    }

    val showSummary = state is CollectState.Completed || collectedButUnconfirmed != null
    val detail: (@Composable () -> Unit)? = when {
        state.isBoxOpen -> {
            { BoxOpenDetail(members = uiState.members, finishing = state.isFinishing) }
        }
        showSummary -> {
            { CollectedSummary(members = uiState.members) }
        }
        // Plain failure (box never opened): wrap the full message in the variable-height
        // detail slot rather than the clipping single-line subline.
        state is CollectState.Failed -> {
            { ErrorDetail(message = state.message) }
        }
        else -> null
    }

    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Always render the top bar so its height is reserved in every collect state —
            // otherwise the box-open → success/box-already-open transition (which used to hide
            // it) would collapse the Scaffold content padding and shift the whole CollectScaffold
            // (header/hero/action) upward. On the terminal states the back CONTROL is suppressed
            // (success's affordance is the "Back to my parcels" button), but the bar HEIGHT stays.
            val terminal = state is CollectState.Completed || collectedButUnconfirmed != null
            DetailTopBar(
                title = if (terminal) "" else stringResource(R.string.collect_open_box_title),
                onBack = onBack,
                showBackButton = !terminal,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            CollectScaffold(
                header = model.header,
                hero = { hero ->
                    when (hero) {
                        // The hold fills the concentric ring; the distance moved to the headline.
                        CollectHero.Distance -> HoldRing(progress = holdState.progress)
                        CollectHero.OpenBox -> OpenBoxBlob()
                        CollectHero.Check -> CheckBlob(count = uiState.members.size)
                        CollectHero.Error -> ErrorBlob()
                    }
                },
                heroKey = model.hero,
                headline = model.headline,
                subline = model.subline,
                detail = detail,
                action = {
                    when (model.action) {
                        CollectAction.HoldOnly -> HoldBar(state = holdState)

                        CollectAction.NearbyOpen -> Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            PrimaryActionButton(
                                text = stringResource(R.string.open_locker),
                                enabled = model.openEnabled,
                                onClick = onConfirmed,
                            )
                            if (model.showOverrideHold) {
                                HoldBar(state = holdState, label = stringResource(R.string.collect_override_hold))
                            }
                        }

                        CollectAction.BackOnly -> PrimaryActionButton(
                            text = if (state is CollectState.Canceled) {
                                stringResource(R.string.collect_close)
                            } else {
                                stringResource(R.string.collect_back_to_parcels)
                            },
                            onClick = onBack,
                        )

                        CollectAction.RetrySupport -> Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            PrimaryActionButton(text = stringResource(R.string.try_again), onClick = onBack)
                            OutlinedActionButton(text = stringResource(R.string.contact_support), onClick = onBack)
                        }

                        CollectAction.None -> Box(modifier = Modifier)
                    }
                },
            )
        }
    }
}

@Composable
private fun CollectScreenModel.localized(
    state: CollectState,
    uiState: CollectUiState,
): CollectScreenModel {
    val locker = uiState.lockerName
    val lockerHeader = (locker?.let { stringResource(R.string.collect_locker_name, it) }
        ?: stringResource(R.string.collect_locker)).uppercase()

    return when (state) {
        is CollectState.Idle -> when (uiState.openMode) {
            LockerOpenMode.HOLD -> copy(
                header = lockerHeader,
                headline = uiState.distanceMeters?.let {
                    stringResource(R.string.collect_distance, it)
                } ?: stringResource(R.string.collect_hold_to_open),
                subline = if (uiState.members.size > 1) {
                    pluralStringResource(
                        R.plurals.collect_shared_box_subline,
                        uiState.members.size,
                        uiState.members.size,
                    )
                } else {
                    stringResource(R.string.collect_stand_at_locker)
                },
            )
            LockerOpenMode.NEARBY -> copy(
                header = lockerHeader,
                headline = if (uiState.nearbyReady) {
                    stringResource(R.string.collect_ready_to_open)
                } else {
                    stringResource(R.string.collect_get_closer)
                },
                subline = when {
                    uiState.nearbyReady -> stringResource(R.string.collect_at_locker)
                    uiState.distanceMeters != null -> stringResource(R.string.collect_move_closer, uiState.distanceMeters)
                    else -> stringResource(R.string.collect_waiting_gps)
                },
            )
        }

        CollectState.Validating -> copy(header = lockerHeader, headline = stringResource(R.string.collect_checking))
        is CollectState.Opening -> copy(header = lockerHeader, headline = stringResource(R.string.collect_opening))
        is CollectState.WaitingForOpened -> copy(header = lockerHeader, headline = stringResource(R.string.collect_waiting_open))
        is CollectState.Opened,
        is CollectState.WaitingForClosed,
        is CollectState.ConfirmingClosed,
        is CollectState.Claiming -> copy(header = lockerHeader, headline = stringResource(R.string.collect_box_open))

        CollectState.Completed -> copy(
            header = stringResource(R.string.box_closed).uppercase(),
            headline = if (uiState.members.size > 1) {
                stringResource(R.string.all_picked_up)
            } else {
                stringResource(R.string.picked_up_exclamation)
            },
        )

        is CollectState.Failed -> copy(
            header = (locker?.let { stringResource(R.string.collect_error_locker, it) }
                ?: stringResource(R.string.collect_error)).uppercase(),
            headline = stringResource(R.string.collect_box_didnt_open),
        )

        CollectState.Canceled -> copy(header = lockerHeader, headline = stringResource(R.string.collect_canceled))
    }
}

/**
 * Design 5b/5c detail — either a plain parcel card (single) or a [CheckOffParcelRow]
 * checklist with a live "checked" counter (shared/multi compartment). The door closing
 * (detected by polling) advances the flow to the success slot; the checklist is a safety
 * affordance only, there is no manual "close" action to wire up.
 */
@Composable
private fun BoxOpenDetail(members: ImmutableList<CollectMember>, finishing: Boolean) {
    val colors = PaczkofastTheme.colors
    val checked = remember(members) { mutableStateListOf<String>() }
    val allChecked = members.isNotEmpty() && checked.size == members.size
    val count = members.size
    val guidance = when {
        finishing -> stringResource(R.string.finishing_up)
        count > 1 -> pluralStringResource(R.plurals.collect_check_before_close, count, count)
        else -> stringResource(R.string.collect_close_door)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = guidance,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (count > 1) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                members.forEach { member ->
                    val isChecked = member.shipmentNumber in checked
                    CheckOffParcelRow(
                        sender = member.title,
                        size = member.sizeLabel ?: "—",
                        checked = isChecked,
                        onToggle = {
                            if (isChecked) checked.remove(member.shipmentNumber)
                            else checked.add(member.shipmentNumber)
                        },
                    )
                }
                Text(
                    text = if (allChecked) {
                        stringResource(R.string.collect_checked_all, members.size, members.size).uppercase()
                    } else {
                        stringResource(R.string.collect_checked_prompt, checked.size, members.size).uppercase()
                    },
                    style = MonoLabel,
                    color = if (allChecked) colors.monoLabel else colors.alertText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                )
            }
        } else {
            members.firstOrNull()?.let { member ->
                PaczkofastCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = member.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        member.sizeLabel?.let { SizeBadge(size = it) }
                    }
                }
            }
        }
    }
}

/**
 * Error detail — the full failure message (wrapping, centered) followed by a "What you can
 * do" tip card. Rendered in the variable-height detail slot so the whole sentence is visible
 * instead of clipping in the fixed single-line subline.
 */
@Composable
private fun ErrorDetail(message: String) {
    val colors = PaczkofastTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        PaczkofastCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.what_you_can_do).uppercase(),
                    style = MonoLabel,
                    color = colors.monoLabel,
                )
                Text(
                    text = stringResource(R.string.try_again_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                )
            }
        }
    }
}

/**
 * Circular neo-brutalist hero blob, sized to the [HoldRing]'s central core so the
 * icon appears to stay put while the ring recedes on success. Yellow/red fills keep
 * an ink border + theme-following hard shadow.
 */
@Composable
private fun HeroBlob(
    fill: Color,
    modifier: Modifier = Modifier,
    glyph: @Composable () -> Unit,
) {
    val colors = PaczkofastTheme.colors
    Box(
        modifier = modifier
            .size(HeroBlobSize)
            .hardShadow(4.dp, 4.dp, colors.hardShadow, CircleShape)
            .background(fill, CircleShape)
            .border(3.dp, colors.accentBorder, CircleShape),
        contentAlignment = Alignment.Center,
        content = { glyph() },
    )
}

/** Yellow open-box "blob": circular neo-brutalist surface with an ink glyph. */
@Composable
private fun OpenBoxBlob(modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    HeroBlob(fill = colors.accent, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = colors.onAccent,
            modifier = Modifier.size(44.dp),
        )
    }
}

/**
 * Design 5d/5e detail — the collected-parcel summary card shown on success (and on the
 * box-already-open caveat case).
 */
@Composable
private fun CollectedSummary(members: ImmutableList<CollectMember>) {
    if (members.isEmpty()) return
    val colors = PaczkofastTheme.colors
    PaczkofastCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = pluralStringResource(
                    R.plurals.parcels_collected,
                    members.size,
                    members.size,
                ).uppercase(),
                style = MonoLabel,
                color = colors.monoLabel,
            )
            members.forEach { member ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colors.borderStrong,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = member.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.5.sp),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    member.sizeLabel?.let { SizeBadge(size = it) }
                }
            }
        }
    }
}

/** Yellow check "blob", with an optional "×N" badge for multi-parcel pickups. */
@Composable
private fun CheckBlob(count: Int, modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    Box(modifier = modifier) {
        HeroBlob(fill = colors.accent) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(48.dp),
            )
        }
        if (count > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(colors.cardSurface, RoundedCornerShape(9.dp))
                    .border(2.5.dp, colors.borderStrong, RoundedCornerShape(9.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "×$count",
                    style = MonoLabel,
                    color = colors.textPrimary,
                )
            }
        }
    }
}

/** Red "!" blob for the collect error state. */
@Composable
private fun ErrorBlob(modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    HeroBlob(fill = colors.alertFill, modifier = modifier) {
        Icon(
            imageVector = Icons.Rounded.PriorityHigh,
            contentDescription = null,
            tint = colors.onAccent,
            modifier = Modifier.size(44.dp),
        )
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val hasFineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    return hasFineLocation || hasCoarseLocation
}

// =============================================================================
// PREVIEWS — all sample data is fake.
// =============================================================================

private val PreviewSingleMember = CollectMember(
    shipmentNumber = "PREVIEW-1",
    title = "Example Sender sp. z o.o.",
    sizeLabel = "M",
)

private val PreviewMultiMembers = persistentListOf(
    CollectMember(shipmentNumber = "PREVIEW-1", title = "Example Sender sp. z o.o.", sizeLabel = "S"),
    CollectMember(shipmentNumber = "PREVIEW-2", title = "Example Shop sp. z o.o.", sizeLabel = "S"),
)

@Composable
private fun CollectPreview(uiState: CollectUiState) {
    PaczkofastTheme {
        CollectContent(uiState = uiState, onConfirmed = {}, onBack = {})
    }
}

@PaczkofastPreviews
@Composable
private fun HoldIdlePreview() {
    CollectPreview(
        CollectUiState(
            state = CollectState.Idle,
            lockerName = "WAW01A",
            distanceMeters = 8,
            openMode = LockerOpenMode.HOLD,
            members = persistentListOf(PreviewSingleMember),
        ),
    )
}

@PaczkofastPreviews
@Composable
private fun NearbyReadyPreview() {
    CollectPreview(
        CollectUiState(
            state = CollectState.Idle,
            lockerName = "WAW01A",
            distanceMeters = 4,
            accuracyMeters = 6,
            openMode = LockerOpenMode.NEARBY,
            members = persistentListOf(PreviewSingleMember),
        ),
    )
}

@PaczkofastPreviews
@Composable
private fun NearbyFarPreview() {
    CollectPreview(
        CollectUiState(
            state = CollectState.Idle,
            lockerName = "WAW01A",
            distanceMeters = 120,
            accuracyMeters = 8,
            openMode = LockerOpenMode.NEARBY,
            members = persistentListOf(PreviewSingleMember),
        ),
    )
}

@PaczkofastPreviews
@Composable
private fun BoxOpenSinglePreview() {
    CollectPreview(
        CollectUiState(
            state = CollectState.Opened(sessionUuid = "PREVIEW"),
            lockerName = "WAW01A",
            members = persistentListOf(PreviewSingleMember),
        ),
    )
}

@PaczkofastPreviews
@Composable
private fun BoxOpenMultiPreview() {
    CollectPreview(
        CollectUiState(
            state = CollectState.Opened(sessionUuid = "PREVIEW"),
            lockerName = "WAW01A",
            members = PreviewMultiMembers,
        ),
    )
}

@PaczkofastPreviews
@Composable
private fun BoxOpenFinishingPreview() {
    CollectPreview(
        CollectUiState(
            state = CollectState.ConfirmingClosed(sessionUuid = "PREVIEW"),
            lockerName = "WAW01A",
            members = persistentListOf(PreviewSingleMember),
        ),
    )
}

@PaczkofastPreviews
@Composable
private fun SuccessSinglePreview() {
    CollectPreview(
        CollectUiState(
            state = CollectState.Completed,
            lockerName = "WAW01A",
            members = persistentListOf(PreviewSingleMember),
        ),
    )
}

@PaczkofastPreviews
@Composable
private fun SuccessMultiPreview() {
    CollectPreview(
        CollectUiState(
            state = CollectState.Completed,
            lockerName = "WAW01A",
            members = PreviewMultiMembers,
        ),
    )
}

@PaczkofastPreviews
@Composable
private fun ErrorPreview() {
    CollectPreview(
        CollectUiState(
            state = CollectState.Failed(
                message = "Nothing happened on our end — your parcel is safe and your pickup " +
                    "code still works. The compartment stayed shut, so no one else can reach " +
                    "your delivery. Give it another go, or use the code on the locker keypad.",
                canRetryFromValidation = false,
            ),
            lockerName = "WAW01A",
            members = persistentListOf(PreviewSingleMember),
        ),
    )
}
