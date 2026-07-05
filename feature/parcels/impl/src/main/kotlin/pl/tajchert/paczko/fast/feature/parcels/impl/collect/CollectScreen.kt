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
import androidx.compose.ui.platform.LocalContext
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
import pl.tajchert.paczko.fast.core.designsystem.component.HoldToOpenPanel
import pl.tajchert.paczko.fast.core.designsystem.component.OutlinedActionButton
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastCard
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.component.PrimaryActionButton
import pl.tajchert.paczko.fast.core.designsystem.component.SizeBadge
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.collect.CollectState

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
        shipmentNumber = shipmentNumber,
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

private val CollectState.isFinishing: Boolean
    get() = this is CollectState.ConfirmingClosed || this is CollectState.Claiming

@Composable
private fun CollectContent(
    shipmentNumber: String,
    uiState: CollectUiState,
    onConfirmed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = uiState.state
    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        topBar = {
            if (state !is CollectState.Completed) {
                DetailTopBar(title = if (state.isBoxOpen) "Box open" else "Open box", onBack = onBack)
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            when {
                state is CollectState.Idle -> HoldToOpenPanel(
                    distanceText = uiState.distanceMeters?.let { "$it m" },
                    lockerCaption = uiState.lockerName?.let { "to locker $it" } ?: "to the locker",
                    subline = collectSubline(uiState.members.size),
                    onConfirmed = onConfirmed,
                    modifier = Modifier.fillMaxSize(),
                )

                state.isBoxOpen -> BoxOpenScreen(
                    members = uiState.members,
                    lockerName = uiState.lockerName,
                    finishing = state.isFinishing,
                )

                state is CollectState.Completed -> SuccessScreen(
                    members = uiState.members,
                    onBack = onBack,
                )

                state is CollectState.Failed -> ErrorScreen(
                    message = state.message,
                    lockerName = uiState.lockerName,
                    onRetry = onBack,
                    onContactSupport = onBack,
                )

                else -> CollectStatus(state = state, onClose = onBack)
            }
        }
    }
}

private fun collectSubline(count: Int): String = when {
    count > 1 -> "$count parcels share this box — you'll take them all at once"
    else -> "Stand at the locker before you start"
}

/**
 * Plain status text for the brief transitional states that have no
 * dedicated mock (validating / opening / waiting-for-open / canceled) —
 * [CollectState.Completed] and [CollectState.Failed] are routed to
 * [SuccessScreen] and [ErrorScreen] respectively before reaching here.
 */
@Composable
private fun CollectStatus(
    state: CollectState,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = collectMessage(state),
            style = MaterialTheme.typography.displaySmall,
            color = PaczkofastTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (state is CollectState.Canceled) {
            PrimaryActionButton(
                text = "Close",
                onClick = onClose,
                modifier = Modifier.padding(top = 28.dp),
            )
        }
    }
}

/**
 * Design 5b/5c — the box is open: a yellow open-box blob, a mono locker
 * caption and either a plain parcel card (single) or a [CheckOffParcelRow]
 * checklist (shared/multi compartment). The door closing (detected by
 * polling) advances the flow to [SuccessScreen] — the checklist here is a
 * safety affordance only, there is no manual "close" action to wire up.
 */
@Composable
private fun BoxOpenScreen(
    members: ImmutableList<CollectMember>,
    lockerName: String?,
    finishing: Boolean,
) {
    val colors = PaczkofastTheme.colors
    val checked = remember(members) { mutableStateListOf<String>() }
    val allChecked = members.isNotEmpty() && checked.size == members.size
    val count = members.size

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            OpenBoxBlob()
            Text(
                text = (lockerName?.let { "Locker $it" } ?: "Locker").uppercase(),
                style = MonoLabel,
                color = colors.monoLabel,
                modifier = Modifier.padding(top = 22.dp),
            )
            Text(
                text = "The box is open",
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = if (count > 1) {
                    "$count parcels share this box — check off both before you close it."
                } else {
                    "Take your parcel, then close the door firmly."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (count > 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp),
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
                }
            } else {
                members.firstOrNull()?.let { member ->
                    PaczkofastCard(modifier = Modifier.padding(top = 22.dp)) {
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

        if (count > 1) {
            Text(
                text = when {
                    finishing -> "Finishing up…"
                    allChecked -> "${members.size} of ${members.size} checked".uppercase()
                    else -> "${checked.size} of ${members.size} checked — check both to close".uppercase()
                },
                style = MonoLabel,
                color = if (allChecked) colors.monoLabel else colors.alertText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        } else {
            Text(
                text = if (finishing) "Finishing up…" else "Close the door firmly — it locks automatically",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}

/** Yellow open-box "blob": circular neo-brutalist surface with an ink glyph. */
@Composable
private fun OpenBoxBlob(modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    Box(
        modifier = modifier
            .size(150.dp)
            .background(colors.accent, CircleShape)
            .border(3.dp, colors.borderStrong, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = colors.borderStrong,
            modifier = Modifier.size(52.dp),
        )
    }
}

/**
 * Design 5d/5e — pickup succeeded: a yellow check blob (with a "×N" badge
 * for multi-parcel pickups), a collected-parcel summary and a way back to
 * the parcel list.
 */
@Composable
private fun SuccessScreen(
    members: ImmutableList<CollectMember>,
    onBack: () -> Unit,
) {
    val colors = PaczkofastTheme.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CheckBlob(count = members.size)
            Text(
                text = if (members.size > 1) "All picked up!" else "Picked up!",
                style = MaterialTheme.typography.displayMedium,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                text = "Box closed".uppercase(),
                style = MonoLabel,
                color = colors.monoLabel,
                modifier = Modifier.padding(top = 6.dp),
            )
            if (members.isNotEmpty()) {
                PaczkofastCard(modifier = Modifier.padding(top = 22.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = (if (members.size > 1) "${members.size} parcels collected" else "1 parcel collected").uppercase(),
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
        }
        PrimaryActionButton(
            text = "Back to my parcels",
            onClick = onBack,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

/** Yellow check "blob", with an optional "×N" badge for multi-parcel pickups. */
@Composable
private fun CheckBlob(count: Int, modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(colors.accent, CircleShape)
                .border(3.dp, colors.borderStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.borderStrong,
                modifier = Modifier.size(56.dp),
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

/**
 * Design 6a — a reusable error state: a red "!" blob, an error caption, a
 * headline, reassurance text and a "what you can do" tip card, followed by
 * a primary retry action and a secondary support action.
 */
@Composable
private fun ErrorScreen(
    message: String,
    lockerName: String?,
    onRetry: () -> Unit,
    onContactSupport: () -> Unit,
) {
    val colors = PaczkofastTheme.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(colors.alertFill, CircleShape)
                    .border(3.dp, colors.borderStrong, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PriorityHigh,
                    contentDescription = null,
                    tint = colors.borderStrong,
                    modifier = Modifier.size(48.dp),
                )
            }
            Text(
                text = (lockerName?.let { "Error · Locker $it" } ?: "Error").uppercase(),
                style = MonoLabel,
                color = colors.monoLabel,
                modifier = Modifier.padding(top = 22.dp),
            )
            Text(
                text = "The box didn't open",
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            PaczkofastCard(modifier = Modifier.padding(top = 22.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(
                        text = "What you can do".uppercase(),
                        style = MonoLabel,
                        color = colors.monoLabel,
                    )
                    Text(
                        text = "Try again — a second attempt usually works.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                    )
                }
            }
        }
        Column(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PrimaryActionButton(text = "Try again", onClick = onRetry)
            OutlinedActionButton(text = "Contact support", onClick = onContactSupport)
        }
    }
}

private fun collectMessage(state: CollectState): String = when (state) {
    CollectState.Idle -> "Ready"
    CollectState.Validating -> "Checking parcel and location"
    is CollectState.Opening -> "Opening compartment"
    is CollectState.WaitingForOpened -> "Waiting for the door to open"
    is CollectState.Opened -> "Take the parcel and close the door"
    is CollectState.WaitingForClosed -> "Waiting for the door to close"
    is CollectState.ConfirmingClosed -> "Confirming closed compartment"
    is CollectState.Claiming -> "Confirming pickup"
    CollectState.Completed -> "Parcel collected"
    is CollectState.Failed -> state.message
    CollectState.Canceled -> "Collection canceled"
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

@PaczkofastPreviews
@Composable
private fun BoxOpenSinglePreview() {
    PaczkofastTheme {
        Box(modifier = Modifier.background(PaczkofastTheme.colors.background).padding(20.dp)) {
            BoxOpenScreen(
                members = persistentListOf(PreviewSingleMember),
                lockerName = "WAW01A",
                finishing = false,
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun BoxOpenMultiPreview() {
    PaczkofastTheme {
        Box(modifier = Modifier.background(PaczkofastTheme.colors.background).padding(20.dp)) {
            BoxOpenScreen(
                members = PreviewMultiMembers,
                lockerName = "WAW01A",
                finishing = false,
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun SuccessSinglePreview() {
    PaczkofastTheme {
        Box(modifier = Modifier.background(PaczkofastTheme.colors.background).padding(20.dp)) {
            SuccessScreen(members = persistentListOf(PreviewSingleMember), onBack = {})
        }
    }
}

@PaczkofastPreviews
@Composable
private fun SuccessMultiPreview() {
    PaczkofastTheme {
        Box(modifier = Modifier.background(PaczkofastTheme.colors.background).padding(20.dp)) {
            SuccessScreen(members = PreviewMultiMembers, onBack = {})
        }
    }
}

@PaczkofastPreviews
@Composable
private fun ErrorPreview() {
    PaczkofastTheme {
        Box(modifier = Modifier.background(PaczkofastTheme.colors.background).padding(20.dp)) {
            ErrorScreen(
                message = "Nothing happened on our end — your parcel is safe and your pickup code still works.",
                lockerName = "WAW01A",
                onRetry = {},
                onContactSupport = {},
            )
        }
    }
}
